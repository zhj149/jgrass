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
package eu.hydrologis.libs.newage;

import java.util.ArrayList;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.ElementSet;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.SpatialReference;
import nl.alterra.openmi.sdk.backbone.TimeSpan;
import nl.alterra.openmi.sdk.backbone.TimeStamp;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IEvent;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IListener;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.ITimeSpan;
import org.openmi.standard.ITimeStamp;
import org.openmi.standard.IValueSet;
import org.openmi.standard.IEvent.EventType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.libs.newage.swig.kriging;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassIElementSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;

/**
 * This component will call the native methods generated by SWIG in the files
 * <code>kriging.java</code> and <code>krigingJNI.java</code>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class Kriging implements ILinkableComponent {

    private String quantityId;

    private String unitId;

    private String modelId;

    private String modelDescription;

    private IInputExchangeItem rawRainfallInputEI;

    private IOutputExchangeItem interpolatedRainfallOutputEI;

    private ILink rawRainfallInputLink = null;

    private ILink interpolatedRainfallOutputLink = null;

    private JGrassIElementSet rawRainfallElementSet;

    private JGrassIElementSet interpolatedRainfallElementSet;

    private double[] inputX = null;

    private double[] inputY = null;

    private double[] inputZ = null;

    private double[] outputX = null;

    private double[] outputY = null;

    private double[] outputZ = null;

    private JGrassIElementSet basinPositionElementSet;

    private IInputExchangeItem basinPositionInputEI;

    private ILink basinPositionInputLink;

    private int inputnum;

    private int outputnum;

    private double dtseconds;

    static {
        try {
            System.loadLibrary("kriging");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Native code library failed to load.\n" + e);
            System.exit(1);
        }
    }

    public void addLink( ILink link ) {
        if (link.getTargetComponent().equals(this)) {
            if (link.getTargetQuantity().equals(rawRainfallInputEI.getQuantity())
                    && link.getTargetElementSet().equals(rawRainfallInputEI.getElementSet())) {
                rawRainfallInputLink = link;
            } else if (link.getTargetQuantity().equals(basinPositionInputEI.getQuantity())
                    && link.getTargetElementSet().equals(basinPositionInputEI.getElementSet())) {
                basinPositionInputLink = link;
            }
        } else if (link.getSourceComponent().equals(this)) {
            if (link.getSourceQuantity().equals(interpolatedRainfallOutputEI.getQuantity())
                    && link.getSourceElementSet().equals(
                            interpolatedRainfallOutputEI.getElementSet())) {
                interpolatedRainfallOutputLink = link;
            }
        } else {
            throw new RuntimeException("Wrong components");
        }

    }

    public void dispose() {
    }

    public void finish() {
        /*
         * call the native kriging finish function
         */
        kriging.finish();
    }

    public String getComponentDescription() {
        return "krigingdescr";
    }

    public String getComponentID() {
        return "kriging";
    }

    public ITimeStamp getEarliestInputTime() {
        return null;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return rawRainfallInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return basinPositionInputEI;
        }

        return null;
    }

    public int getInputExchangeItemCount() {
        return 2;
    }

    public String getModelDescription() {
        return modelDescription;
    }

    public String getModelID() {
        return modelId;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return interpolatedRainfallOutputEI;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public ITimeSpan getTimeHorizon() {
        ITimeStamp begin = new TimeStamp(15020.0);// start of 1900
        ITimeStamp end = new TimeStamp(782029.0);// start of 4000

        return new TimeSpan(begin, end);
    }

    public void initialize( IArgument[] properties ) {
        /*
         * arguments needed for the initialization
         */
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.compareTo("quantityid") == 0) {
                quantityId = argument.getValue();
            }
            if (key.compareTo("unitid") == 0) {
                unitId = argument.getValue();
            }
            if (key.compareTo("dtseconds") == 0) {
                dtseconds = Double.parseDouble(argument.getValue());
            }

        }

        modelId = "kriging";
        modelDescription = modelId;
        /*
         * rainfall input exchange item
         */
        IQuantity rawRainfallQuantity = UtilitiesFacade.createScalarQuantity(quantityId, unitId);
        rawRainfallElementSet = new JGrassElementset("dummyrainfallelementset",
                "dummyrainfallelementset", JGrassIElementSet.JGrassElementType.PointCollection,
                new SpatialReference("some reference"));
        rawRainfallInputEI = UtilitiesFacade.createInputExchangeItem(this, rawRainfallQuantity,
                rawRainfallElementSet);

        /*
         * interpolated rainfall output exchange item
         */
        IQuantity interpolatedRainfallQuantity = UtilitiesFacade.createScalarQuantity(quantityId,
                unitId);
        interpolatedRainfallOutputEI = UtilitiesFacade.createOutputExchangeItem(this,
                interpolatedRainfallQuantity, rawRainfallElementSet);

        /*
         * basinPosition input exchange item
         */
        IQuantity basinPositionQuantity = UtilitiesFacade
                .createScalarQuantity("basinPosition", "m");
        basinPositionElementSet = new JGrassElementset("dummyareaelementset",
                "dummyareaelementset", JGrassIElementSet.JGrassElementType.PointCollection,
                new SpatialReference("some reference"));
        basinPositionInputEI = UtilitiesFacade.createInputExchangeItem(this, basinPositionQuantity,
                basinPositionElementSet);

    }

    public void prepare() {
        /*
         * at the point that the links are created and the components instantiated, we can retrieve
         * the elementsets from the source and target components.
         */
        rawRainfallElementSet = (JGrassIElementSet) rawRainfallInputLink.getSourceElementSet();

        /*
         * retrieve the positions from the right elementset
         */
        interpolatedRainfallElementSet = (JGrassIElementSet) basinPositionInputLink
                .getSourceElementSet();
        // interpolatedRainfallElementSet = interpolatedRainfallOutputLink.getTargetElementSet();

        inputnum = rawRainfallElementSet.getElementCount();
        outputnum = interpolatedRainfallElementSet.getElementCount();
        inputX = new double[inputnum];
        inputY = new double[inputnum];
        inputZ = new double[inputnum];
        outputX = new double[outputnum];
        outputY = new double[outputnum];
        outputZ = new double[outputnum];

        FeatureCollection<SimpleFeatureType, SimpleFeature> featC = rawRainfallElementSet
                .getFeatureCollection();
        List<Geometry> ingeoms = new ArrayList<Geometry>();
        FeatureIterator<SimpleFeature> fIterator = featC.features();
        while( fIterator.hasNext() ) {
            SimpleFeature f = fIterator.next();
            ingeoms.add((Geometry) f.getDefaultGeometry());
        }
        for( int i = 0; i < ingeoms.size(); i++ ) {
            Coordinate tmp = ingeoms.get(i).getCoordinate();
            inputX[i] = tmp.x;
            inputY[i] = tmp.y;
            inputZ[i] = tmp.z;
        }
        featC.close(fIterator);

        featC = interpolatedRainfallElementSet.getFeatureCollection();
        List<Geometry> outgeoms = new ArrayList<Geometry>();
        fIterator = featC.features();
        while( fIterator.hasNext() ) {
            SimpleFeature f = fIterator.next();
            outgeoms.add((Geometry) f.getDefaultGeometry());
        }
        for( int i = 0; i < outgeoms.size(); i++ ) {
            Coordinate tmp = outgeoms.get(i).getCoordinate();
            outputX[i] = tmp.x;
            outputY[i] = tmp.y;
            outputZ[i] = tmp.z;
        }
        featC.close(fIterator);

        /*
         * initialize the fortran kriging part
         */
        kriging.initializepoint(outputX, outputY, outputZ, outputnum);
    }
    public IValueSet getValues( ITime time, String linkID ) {
        /*
         * the pulling link is the output link. Before we use that, we need to get the raw rain from
         * the input exchange item
         */
        IValueSet rawRainValueSet = rawRainfallInputLink.getSourceComponent().getValues(time,
                rawRainfallInputLink.getID());
        int rainnum = ((ScalarSet) rawRainValueSet).size();
        if (rainnum != inputX.length) {
            System.out.println("Stations number and rain columns are not the same, but should!");
            return null;
        }
        /*
         * allocate array for the input data
         */
        double[] rawRainArray = new double[rainnum];
        for( int i = 0; i < rawRainArray.length; i++ ) {
            // we pass the rain intensity (divide rain by dt)
            rawRainArray[i] = ((ScalarSet) rawRainValueSet).getScalar(i) / dtseconds;
        }

        /*
         * allocate array for the output data
         */
        double[] interpolatedRainArray = new double[outputX.length];

        /*
         * do geostatistic interpolation
         */
        doKriging(rawRainArray, interpolatedRainArray);

        return new ScalarSet(interpolatedRainArray);
    }

    /**
     * Method that calls the native code that implements the kriging geostatistic interpolator.
     * 
     * @param rawRainArray raw rain data measured in the station coordinates
     * @param interpolatedRainArray rain data interpolated in the baricenters of the basins
     */
    private void doKriging( double[] rawRainArray, double[] interpolatedRainArray ) {
        kriging.getValues(inputX, inputY, inputZ, inputnum, rawRainArray, interpolatedRainArray);
    }

    public void removeLink( String linkID ) {
        if (rawRainfallInputLink.getID().equals(linkID)) {
            rawRainfallInputLink = null;
        } else if (interpolatedRainfallOutputLink.getID().equals(linkID)) {
            interpolatedRainfallOutputLink = null;
        } else if (basinPositionInputLink.getID().equals(linkID)) {
            basinPositionInputLink = null;
        }
    }

    public String validate() {
        if (rawRainfallInputLink == null || interpolatedRainfallOutputLink == null
                || basinPositionInputEI == null) {
            return "Not all required links have been connected";
        }
        return "";
    }

    public EventType getPublishedEventType( int providedEventTypeIndex ) {
        return null;
    }

    public int getPublishedEventTypeCount() {
        return 0;
    }

    public void sendEvent( IEvent Event ) {
    }

    public void subscribe( IListener listener, EventType eventType ) {
    }

    public void unSubscribe( IListener arg0, EventType arg1 ) {
    }

}
