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
package eu.hydrologis.jgrass.models.h.kriging;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.jgrass.models.h.kriging.jama.Matrix;
import eu.hydrologis.jgrass.models.h.kriging.sextante.Point3D;
import eu.hydrologis.jgrass.models.h.kriging.sextante.PtAndDistance;
import eu.hydrologis.jgrass.models.h.kriging.sextante.RTreeJsi;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class h_kriging extends ModelsBackbone {

    public final static String valuesInputID = "inputvalues"; //$NON-NLS-1$ 
    public final static String valuesPositionsInputID = "positions"; //$NON-NLS-1$
    public final static String interpolatedValuesPositionsInputID = "interpolatedpositions"; //$NON-NLS-1$
    public final static String interpolatedValuesOutputID = "outputvalues"; //$NON-NLS-1$

    public static final String MAXPOINTS = "maxpoints"; //$NON-NLS-1$
    public static final String MINPOINTS = "minpoints"; //$NON-NLS-1$
    public static final String MODEL = "model"; //$NON-NLS-1$
    public static final String NUGGET = "nugget"; //$NON-NLS-1$
    public static final String SILL = "sill"; //$NON-NLS-1$
    public static final String RANGE = "range"; //$NON-NLS-1$
    public static final String DOVARIANCE = "dovariance"; //$NON-NLS-1$
    public static final String SEARCHRADIUS = "searchradius"; //$NON-NLS-1$

    public static final String IDFIELD = "idfield"; //$NON-NLS-1$
    public static final String IDFIELDINTERPOLATED = "idfieldinterpolated"; //$NON-NLS-1$

    private String modelDescription = "usage...";// eu.hydrologis.jgrass.models.messages.help.Messages
    // .getString("h_kriging.usage"); //$NON-NLS-1$

    private IInputExchangeItem valuesInputEI;
    private IInputExchangeItem positionsInputEI;
    private IInputExchangeItem interpolatedPositionsInputEI;
    private IOutputExchangeItem valuesOutputEI;

    private ILink valuesInputLink = null;
    private ILink positionsInputLink = null;
    private ILink interpolatedPositionsInputLink = null;
    private ILink valuesOutputLink = null;

    private JGrassRegion activeRegion;

    private int maxPoints;
    private int minPoints;
    private int model;
    private double nugget;
    private double sill;
    private double scale;
    private double range;
    private double searchRadius;
    private boolean createVarianceLayer;
    private double[][] weights;
    private double[] gammas;
    private Matrix weightMatrix;
    private RTreeJsi m_SearchEngine;
    private PtAndDistance[] m_NearestPoints;
    private String idField = null;
    private String idFieldinterpolated = null;

    private HashMap<Integer, Double> id2valueMap = new HashMap<Integer, Double>();
    private HashMap<Integer, Coordinate> id2CoordinatesMap;
    private HashMap<Integer, Coordinate> interpolatedId2CoordinatesMap;
    private String locationPath;

    public h_kriging() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_kriging( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        String mapset = null;

        maxPoints = -1;
        minPoints = -1;
        model = -1;
        nugget = -1.0;
        sill = -1.0;
        scale = sill - nugget;
        range = -1;
        createVarianceLayer = true;
        searchRadius = Double.MAX_VALUE;

        /*
         * arguments needed for the initialization
         */
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.compareTo(ModelsConstants.GRASSDB) == 0) {
                grassDb = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.LOCATION) == 0) {
                location = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.MAPSET) == 0) {
                mapset = argument.getValue();
            }
            if (key.compareTo(IDFIELD) == 0) {
                idField = argument.getValue();
            }
            if (key.compareTo(IDFIELDINTERPOLATED) == 0) {
                idFieldinterpolated = argument.getValue();
            }
            if (key.compareTo(MAXPOINTS) == 0) {
                try {
                    maxPoints = new Integer(argument.getValue());
                } catch (Exception e) {
                }
            }
            if (key.compareTo(MINPOINTS) == 0) {
                try {
                    minPoints = new Integer(argument.getValue());
                } catch (Exception e) {
                }
            }
            if (key.compareTo(MODEL) == 0) {
                try {
                    model = new Integer(argument.getValue());
                } catch (Exception e) {
                }
            }
            if (key.compareTo(NUGGET) == 0) {
                try {
                    nugget = new Double(argument.getValue());
                } catch (Exception e) {
                }
            }
            if (key.compareTo(SILL) == 0) {
                try {
                    sill = new Double(argument.getValue());
                } catch (Exception e) {
                }
            }

            if (key.compareTo(RANGE) == 0) {
                try {
                    range = new Double(argument.getValue());
                } catch (Exception e) {
                }
            }
            if (key.compareTo(SEARCHRADIUS) == 0) {
                try {
                    searchRadius = new Double(argument.getValue());
                } catch (Exception e) {
                }
            }
            if (key.compareTo(DOVARIANCE) == 0) {
                try {
                    createVarianceLayer = new Boolean(argument.getValue());
                } catch (Exception e) {
                }
            }
        }

        if (maxPoints == -1 || minPoints == -1 || model == -1 || nugget == -1.0 || sill == -1.0
                || range == -1 || idField == null || idFieldinterpolated == null) {
            StringBuilder msgB = new StringBuilder();
            msgB.append("maxpoints = ");
            msgB.append(maxPoints);
            msgB.append("\n");
            msgB.append("minpoints = ");
            msgB.append(minPoints);
            msgB.append("\n");
            msgB.append("model = ");
            msgB.append(model);
            msgB.append("\n");
            msgB.append("nugget = ");
            msgB.append(nugget);
            msgB.append("\n");
            msgB.append("sill = ");
            msgB.append(sill);
            msgB.append("\n");
            msgB.append("range = ");
            msgB.append(range);
            msgB.append("\n");
            msgB.append("idfield = ");
            msgB.append(idField);
            msgB.append("\n");
            msgB.append("idfieldinterpolated = ");
            msgB.append(idFieldinterpolated);
            String msg = msgB.toString();
            throw new IOException(
                    "An error occurred while parsing the input arguments. Check your syntax.\nValues supplied:\n"
                            + msg);
        }

        weights = new double[maxPoints + 1][maxPoints + 1];
        gammas = new double[maxPoints + 1];
        weightMatrix = new Matrix(weights);

        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        valuesInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this, null);
        positionsInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this, null);
        interpolatedPositionsInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(
                this, null);
        valuesOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        /*
         * read the values to be interpolated
         */
        ScalarSet valuesScalarSet = ModelsConstants
                .getScalarSetFromLink(valuesInputLink, time, err);
        id2valueMap.clear();
        for( int i = 1; i < valuesScalarSet.size(); i = i + 2 ) {
            Double value = valuesScalarSet.get(i + 1);
            if (!isNovalue(value)) {
                /*
                 * only regular values are picked out
                 */
                int id = valuesScalarSet.get(i).intValue();
                id2valueMap.put(id, value);
            }
        }

        if (id2CoordinatesMap == null) {
            // read time independent info

            /*
             * the input ids & positions
             */
            List<SimpleFeature> valuesPositionsList = ModelsConstants.getFeatureListFromLink(
                    positionsInputLink, time, err);
            SimpleFeatureType featureType = valuesPositionsList.get(0).getFeatureType();
            int idFieldIndex = featureType.indexOf(idField);
            id2CoordinatesMap = new HashMap<Integer, Coordinate>();
            for( SimpleFeature feature : valuesPositionsList ) {
                int id = ((Number) feature.getAttribute(idFieldIndex)).intValue();
                Coordinate point = ((Geometry) feature.getDefaultGeometry()).getCoordinate();
                id2CoordinatesMap.put(id, point);
            }

            /*
             * the interpolated ids and positions
             */
            if (interpolatedPositionsInputLink != null) {
                List<SimpleFeature> interpolatedPositionsList = ModelsConstants
                        .getFeatureListFromLink(interpolatedPositionsInputLink, time, err);
                featureType = interpolatedPositionsList.get(0).getFeatureType();
                idFieldIndex = featureType.indexOf(idFieldinterpolated);
                interpolatedId2CoordinatesMap = new HashMap<Integer, Coordinate>();
                for( SimpleFeature feature : interpolatedPositionsList ) {
                    int id = ((Number) feature.getAttribute(idFieldIndex)).intValue();
                    Coordinate point = ((Geometry) feature.getDefaultGeometry()).getCentroid()
                            .getCoordinate();
                    interpolatedId2CoordinatesMap.put(id, point);
                }
            }
        }

        /*
         * now see if we need to work on grid or on points
         */
        IValueSet returnSet = null;
        if (interpolatedPositionsInputLink != null) {
            // we have points on which to interpolate

            int interpPosSize = interpolatedId2CoordinatesMap.size();
            double[] varianceArray = null;
            if (createVarianceLayer) {
                varianceArray = new double[interpPosSize];
            }

            ScalarSet interpolatedValuesScalarSet = new ScalarSet();
            interpolatedValuesScalarSet.add(interpPosSize * 2.0);

            m_SearchEngine = new RTreeJsi(id2valueMap, id2CoordinatesMap);
            Set<Integer> interpolatedIdSet = interpolatedId2CoordinatesMap.keySet();
            int index = 0;
            for( Integer interpolatedId : interpolatedIdSet ) {
                Coordinate coordinate = interpolatedId2CoordinatesMap.get(interpolatedId);
                m_NearestPoints = m_SearchEngine.getClosestPoints(coordinate.x, coordinate.y,
                        searchRadius, maxPoints, true);

                interpolatedValuesScalarSet.add(interpolatedId.doubleValue());
                double interpolatedInPoints = interpolateInPoints(coordinate.x, coordinate.y,
                        index, varianceArray);
                if (interpolatedInPoints >= 0.0) {
                    interpolatedValuesScalarSet.add(interpolatedInPoints);
                } else {
                    out
                            .println("Found a negative data in the interpolated values. Set it to zero.");
                    interpolatedInPoints = 0.0;
                    interpolatedValuesScalarSet.add(interpolatedInPoints);
                }
                index++;
            }

            returnSet = interpolatedValuesScalarSet;
        } else {
            // do it on a grid
            double[][] varianceRaster = null;
            if (createVarianceLayer) {
                varianceRaster = new double[activeRegion.getRows()][activeRegion.getCols()];
            }
            WritableRaster interpolatedValuesImage = FluidUtils.createDoubleWritableRaster(activeRegion.getCols(), activeRegion
                    .getRows(), null, null, doubleNovalue);

            processGridAlgorithm(interpolatedValuesImage, varianceRaster);
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            returnSet = new JGrassGridCoverageValueSet(interpolatedValuesImage, activeRegion, crs);
        }

        if (valuesOutputLink != null && valuesOutputLink.getID().equals(linkID)) {
            return returnSet;
        }

        return null;
    }

    public void addLink( ILink link ) {
        if (link.getID().equals(valuesInputID)) {
            valuesInputLink = link;
        } else if (link.getID().equals(interpolatedValuesOutputID)) {
            valuesOutputLink = link;
        } else if (link.getID().equals(valuesPositionsInputID)) {
            positionsInputLink = link;
        } else if (link.getID().equals(interpolatedValuesPositionsInputID)) {
            interpolatedPositionsInputLink = link;
        } else {
            throw new RuntimeException("Wrong components");
        }
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return valuesInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return positionsInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return interpolatedPositionsInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 3;
    }

    public String getModelDescription() {
        return modelDescription;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return valuesOutputEI;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (valuesInputLink.getID().equals(linkID)) {
            valuesInputLink = null;
        } else if (valuesOutputLink.getID().equals(linkID)) {
            valuesOutputLink = null;
        } else if (positionsInputLink.getID().equals(linkID)) {
            positionsInputLink = null;
        } else if (interpolatedPositionsInputLink.getID().equals(linkID)) {
            interpolatedPositionsInputLink = null;
        }
    }

    /**
     * The kriging in the case in which the interpolation is performed on a grid.
     */
    private void processGridAlgorithm( WritableRaster interpolatedValuesImage, double[][] varianceRaster ) {
        m_SearchEngine = new RTreeJsi(id2valueMap, id2CoordinatesMap);

        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        WritableRandomIter interpIterator = RandomIterFactory.createWritable(interpolatedValuesImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Processing Kriging...", rows);
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                Coordinate worldCoordinates = JGrassUtilities.rowColToCenterCoordinates(activeRegion, i, j);

                m_NearestPoints = m_SearchEngine.getClosestPoints(worldCoordinates.x, worldCoordinates.y, searchRadius,
                        maxPoints, true);

                double interpolatedOnGrid = interpolateOnGrid(worldCoordinates.x, worldCoordinates.y, varianceRaster);
                interpIterator.setSample(j, i, 0, interpolatedOnGrid);
            }
            pm.worked(1);
        }
        pm.done();

        // createCrossValidationTable();
    }

    // protected void createCrossValidationTable() {
    //
    // int i;
    // int iPoints;
    // double x, y, z;
    // double dValue;
    // double geomdata[] = new double[6];
    // IGeometry pt;
    // PathIterator pathIterator;
    // Value[] values = new Value[5];
    // String sFields[] = {"X", "Y", PluginServices.getText(this, "Valor_real"),
    // PluginServices.getText(this, "Valor_estimado"),
    // PluginServices.getText(this, "Diferencia")};
    // int iTypes[] = {Types.DOUBLE, Types.DOUBLE, Types.DOUBLE, Types.DOUBLE,
    // Types.DOUBLE};
    // String sTableName = PluginServices.getText(this,
    // "Validacion_cruzada_corchete")
    // + m_Layer.getName() + "]";
    // TableMemoryDriver table = getNewTableDriver("CROSSVALIDATION",
    // sTableName, iTypes, sFields);
    // ReadableVectorial rv = m_Layer.getSource();
    //
    // try {
    // rv.start();
    // setProgressText(PluginServices.getText(this,
    // "Creando_validacion_cruzada"));
    // iPoints = rv.getShapeCount();
    // for( i = 0; i < iPoints && setProgress(i, iPoints); i++ ) {
    // pt = rv.getShape(i);
    // pathIterator = pt.getPathIterator(null);
    // pathIterator.currentSegment(geomdata);
    // x = geomdata[0];
    // y = geomdata[1];
    // values[0] = ValueFactory.createValue(x);
    // values[1] = ValueFactory.createValue(y);
    // try {
    // z = new Double(m_Layer.getRecordset().getRow(i)[m_iField].toString())
    // .doubleValue();
    // } catch (NumberFormatException e) {
    // z = 0;
    // }
    // values[2] = ValueFactory.createValue(z);
    // dValue = getValueAt(x, y);
    // values[3] = ValueFactory.createValue(dValue);
    // values[4] = ValueFactory.createValue(dValue - z);
    // table.addRow(values);
    // }
    // rv.stop();
    // } catch (Exception e) {
    // e.printStackTrace();
    // return;
    // }
    //
    // }

    private double interpolateOnGrid( double x, double y, double[][] varianceRaster ) {

        int i, j, nPoints;
        double dLambda, dValue, dVariance;
        Point3D pt;

        if ((nPoints = getWeights(x, y)) >= minPoints) {
            for( i = 0; i < nPoints; i++ ) {
                gammas[i] = getWeight(m_NearestPoints[i].getDist());
            }

            gammas[nPoints] = 1.0;

            for( i = 0, dValue = 0.0, dVariance = 0.0; i < nPoints; i++ ) {
                pt = m_NearestPoints[i].getPt();
                for( j = 0, dLambda = 0.0; j <= nPoints; j++ ) {
                    dLambda += weights[i][j] * gammas[j];
                }

                dValue += dLambda * pt.getZ();

                if (createVarianceLayer) {
                    dVariance += dLambda * gammas[i];
                }
            }

            if (createVarianceLayer) {
                int[] rowCol = JGrassUtilities.coordinateToNearestRowCol(activeRegion,
                        new Coordinate(x, y));
                varianceRaster[rowCol[0]][rowCol[1]] = dVariance;
            }

            return dValue;
        }

        if (createVarianceLayer) {
            int[] rowCol = JGrassUtilities.coordinateToNearestRowCol(activeRegion, new Coordinate(
                    x, y));
            varianceRaster[rowCol[0]][rowCol[1]] = JGrassConstants.doubleNovalue;
        }

        return JGrassConstants.doubleNovalue;
    }

    private double interpolateInPoints( double x, double y, int index, double[] varianceVector ) {

        int i, j, nPoints;
        double dLambda, dValue, dVariance;
        Point3D pt;

        if ((nPoints = getWeights(x, y)) >= minPoints) {
            for( i = 0; i < nPoints; i++ ) {
                gammas[i] = getWeight(m_NearestPoints[i].getDist());
            }

            gammas[nPoints] = 1.0;

            for( i = 0, dValue = 0.0, dVariance = 0.0; i < nPoints; i++ ) {
                pt = m_NearestPoints[i].getPt();
                for( j = 0, dLambda = 0.0; j <= nPoints; j++ ) {
                    dLambda += weights[i][j] * gammas[j];
                }

                dValue += dLambda * pt.getZ();

                if (createVarianceLayer) {
                    dVariance += dLambda * gammas[i];
                }
            }

            if (createVarianceLayer) {
                varianceVector[index] = dVariance;
            }

            return dValue;
        }

        if (createVarianceLayer) {
            varianceVector[index] = JGrassConstants.doubleNovalue;
        }

        return JGrassConstants.doubleNovalue;
    }

    private double getWeight( double d ) {

        if (d == 0.0) {
            d = 0.0001;
        }
        scale = sill - nugget;
        switch( model ) {
        case 0: // Spherical Model
            if (d >= range) {
                d = nugget + scale;
            } else {
                d = nugget + scale
                        * (3 * d / (2 * range) - d * d * d / (2 * range * range * range));
            }
            break;
        case 1: // Exponential Model
            d = nugget + scale * (1 - Math.exp(-3 * d / range));
            break;
        case 2: // Gaussian Model
        // d = 1 - Math.exp(-3 * d / (range * range));
        // d = nugget + scale * d * d;
            d = nugget + scale * (1 - Math.exp(-3 * Math.pow(d, 2)) / (range * range));
            break;
        case 3: // Sillian Model
            d = 1 - Math.exp(-3 * d / (range * range));
            d = nugget + scale * d * d;
            break;
        }

        return d;
    }

    private int getWeights( double x, double y ) {
        int i, j, n;
        double dx, dy;
        Point3D pt, pt2;

        double[][] m_dWeights = weightMatrix.getArray();

        if ((n = Math.min(m_NearestPoints.length, maxPoints)) >= minPoints) {
            n = Math.min(n, maxPoints);
            for( i = 0; i < n; i++ ) {
                pt = (Point3D) m_NearestPoints[i].getPt();
                m_dWeights[i][i] = 0.0;
                m_dWeights[i][n] = m_dWeights[n][i] = 1.0;
                for( j = i + 1; j < n; j++ ) {
                    pt2 = (Point3D) m_NearestPoints[j].getPt();
                    dx = pt.getX() - pt2.getX();
                    dy = pt.getY() - pt2.getY();
                    m_dWeights[i][j] = m_dWeights[j][i] = getWeight(Math.sqrt(dx * dx + dy * dy));
                }
            }

            m_dWeights[n][n] = 0.0;

            Matrix subMatrix = weightMatrix.getMatrix(0, n, 0, n);

            try {
                Matrix inverse = subMatrix.inverse();
                weightMatrix.setMatrix(0, n, 0, n, inverse);
            } catch (RuntimeException e) {
                return 0;
            }

            // m_dWeights = inverse.getArray();

        }

        return n;

    }

}
