/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org
 * (C) {
 * HydroloGIS - www.hydrologis.com
 * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam
 * The JGrass developer team - www.jgrass.org
 * }
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.models.h.variogram;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * @author Silvia Franceschi (www.hydrologis.com)
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class h_variogram extends ModelsBackbone {

    private static final String DISTANCE = "distance";
    private static final String IDFIELD = "idfield"; //$NON-NLS-1$

    private String modelDescription = "usage...";

    public static String valuesInputId = "inputvalues";
    public static String valuesPositionsInputId = "positions";
    public static String outputId = "cloud";

    private IInputExchangeItem valuesInputEI;
    private IInputExchangeItem valuesPositionsInputEI;
    private IOutputExchangeItem outputEI;

    private ILink valuesInputLink = null;
    private ILink valuesPositionsInputLink = null;
    private ILink outputLink = null;

    private double binAmplitude;
    private double maxDist = 0;
    private double mean;
    private List<double[]> cloud;

    private List<SimpleFeature> positionsList;
    private HashMap<Integer, Double> id2valueMap = new HashMap<Integer, Double>();

    private HashMap<Integer, Coordinate> id2coordinateMap;
    private String idField;
    private List<Integer> idList = new ArrayList<Integer>();

    public h_variogram() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_variogram( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        binAmplitude = 10000.0;

        /*
         * arguments needed for the initialization
         */
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.compareTo(DISTANCE) == 0) {
                try {
                    binAmplitude = Double.parseDouble(argument.getValue());
                } catch (Exception e) {
                }
            }
            if (key.compareTo(IDFIELD) == 0) {
                idField = argument.getValue();
            }
        }

        if (idField == null) {
            throw new IOException("The idfield argument is missing. Check your syntax.");
        }

        valuesInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        valuesPositionsInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this,
                null);
        outputEI = ModelsConstants.createDummyOutputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (positionsList == null) {
            positionsList = ModelsConstants.getFeatureListFromLink(valuesPositionsInputLink, time,
                    err);
            id2coordinateMap = new HashMap<Integer, Coordinate>();
            int idFieldIndex = -1;
            for( SimpleFeature feature : positionsList ) {
                if (idFieldIndex == -1) {
                    SimpleFeatureType featureType = feature.getFeatureType();
                    idFieldIndex = featureType.indexOf(idField);
                }
                int id = ((Number) feature.getAttribute(idFieldIndex)).intValue();
                Coordinate coordinate = ((Geometry) feature.getDefaultGeometry()).getCoordinate();
                id2coordinateMap.put(id, coordinate);
            }
        }

        ScalarSet valuesScalarSet = ModelsConstants
                .getScalarSetFromLink(valuesInputLink, time, err);
        id2valueMap.clear();
        idList.clear();
        for( int i = 1; i < valuesScalarSet.size(); i = i + 2 ) {
            Double value = valuesScalarSet.get(i + 1);
            if (!isNovalue(value)) {
                /*
                 * only regular values are picked out
                 */
                int id = valuesScalarSet.get(i).intValue();
                id2valueMap.put(id, value);
                idList.add(id);
            }
        }

        processAlgorithm();

        if (cloud != null && cloud.size() > 0) {
            ScalarSet scalarSet = new ScalarSet();
            scalarSet.add(4.0);
            for( int i = 0; i < cloud.size(); i++ ) {
                double[] record = cloud.get(i);
                for( double d : record ) {
                    scalarSet.add(d);
                }
            }
            return scalarSet;
        } else {
            throw new ModelsIllegalargumentException(
                    "An error occurred while creating the output values. Consider checking if there are enough monitoring points inside the used region.",
                    this);
        }

    }

    /*
     * calculate variogram
     */
    private boolean processAlgorithm() {

        double x1, x2, y1, y2;
        double dDifX, dDifY;
        double dValue;
        int iCount;

        mean = 0;

        try {

            /*
             * set iCount as the number of active station to analyse 
             * set d as a matrix with each
             * station related to the other stations
             */
            iCount = id2valueMap.size();
            if (iCount == 0) {
                return false;
            }
            double[][] d = new double[iCount][iCount];

            /*
             * main cycle on the stations to get the distance and the 
             * semivariance from each station
             * related to the others
             */
            for( int i = 0; i < idList.size(); i++ ) {
                Integer id = idList.get(i);
                Coordinate coordinate = id2coordinateMap.get(id);
                /*
                 * first point: coordinate and value
                 */
                x1 = coordinate.x;
                y1 = coordinate.y;
                dValue = id2valueMap.get(id);

                mean += dValue;
                // id2valueMap.put(id, dValue);

                /*
                 * calculate the distance between all the other station and the station taken in the
                 * main cycle
                 */
                for( int j = 0; j < iCount; j++ ) {
                    Integer id2 = idList.get(j);
                    Coordinate coordinate2 = id2coordinateMap.get(id2);
                    x2 = coordinate2.x;
                    y2 = coordinate2.y;
                    dDifX = x2 - x1;
                    dDifY = y2 - y1;
                    d[i][j] = Math.sqrt(dDifX * dDifX + dDifY * dDifY);
                    maxDist = Math.max(maxDist, d[i][j]);
                }

            }

            mean /= (double) iCount;

            if (!calculate(d)) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    private boolean calculate( double[][] dist ) {

        int i, j;
        int iClasses;
        int iClass;
        int[] iPointsInClass;
        double[] dDen;
        double dSemivar;
        boolean bIsInClass[];

        /*
         * calculate the distance classes: number of bins in which divide the entire range of
         * distance
         */
        iClasses = (int) (maxDist / binAmplitude + 2);
        double[] m_dMoran = new double[iClasses];
        double[] m_dGeary = new double[iClasses];
        dDen = new double[iClasses];
        /*
         * initialization of the semivariogram with the number of classes of distance
         */
        double[] m_dSemivar = new double[iClasses];
        double[] m_ddist = new double[iClasses];

        /*
         * number of couple of station in each distance class
         */
        iPointsInClass = new int[iClasses];
        bIsInClass = new boolean[iClasses];

        cloud = new ArrayList<double[]>();

        /*
         * for each station
         */
        for( i = 0; i < dist.length; i++ ) {
            Arrays.fill(bIsInClass, false);
            Integer id1 = idList.get(i);
            double value1 = id2valueMap.get(id1);
            /*
             * second cycle on stations relative to the one in the main cycle
             */
            for( j = 0; j < dist.length; j++ ) {
                /*
                 * first check in which class is the current distance
                 */
                // if (i != j) {
                iClass = (int) Math.floor((dist[i][j] + binAmplitude / 2.) / binAmplitude);
                /*
                 * update the number of point in the relative class
                 */
                iPointsInClass[iClass]++;
                /*
                 * calculate the variance
                 */
                Integer id2 = idList.get(j);
                double value2 = id2valueMap.get(id2);
                dSemivar = Math.pow((value1 - value2), 2.);

                /*
                 * add the element distance - variance to the cloud
                 */
                // cloud.add(new double[]{dist[i][j], dSemivar / 2.});
                /*
                 * sum the variance of each class of distance
                 */
                m_dSemivar[iClass] += dSemivar;
                m_dMoran[iClass] += (value1 - mean) * (value2 - mean);
                m_dGeary[iClass] = m_dSemivar[iClass];
                bIsInClass[iClass] = true;
                // }
            }
            for( j = 0; j < iClasses; j++ ) {
                if (bIsInClass[j]) {
                    dDen[j] += Math.pow(value1 - mean, 2.);
                }
            }
        }

        for( i = 0; i < iClasses; i++ ) {
            if (dDen[i] != 0) {
                m_dMoran[i] /= dDen[i];
                m_dGeary[i] *= ((iPointsInClass[i] - 1) / (2. * iPointsInClass[i] * dDen[i]));
                /*
                 * calculate the semivariance for each class as the sum of the variance of each
                 * couples of measurments divided by the number of couple of station in each class
                 * of distance
                 */
                m_dSemivar[i] /= (2. * iPointsInClass[i]);
                m_ddist[i] = binAmplitude * i + binAmplitude / 2.;
                cloud.add(new double[]{m_ddist[i], m_dSemivar[i], m_dMoran[i], m_dGeary[i]});
            }
        }

        return true;

    }

    public void addLink( ILink link ) {
        if (link.getID().equals(valuesInputId)) {
            valuesInputLink = link;
        } else if (link.getID().equals(valuesPositionsInputId)) {
            valuesPositionsInputLink = link;
        } else if (link.getID().equals(outputId)) {
            outputLink = link;
        } else {
            throw new RuntimeException("Trying to link wrong components");
        }

    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return valuesInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return valuesPositionsInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 2;
    }

    public String getModelDescription() {
        return modelDescription;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return outputEI;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(valuesInputId)) {
            valuesInputLink = null;
        } else if (linkID.equals(valuesPositionsInputId)) {
            valuesPositionsInputLink = null;
        } else if (linkID.equals(outputId)) {
            outputLink = null;
        }
    }

}
