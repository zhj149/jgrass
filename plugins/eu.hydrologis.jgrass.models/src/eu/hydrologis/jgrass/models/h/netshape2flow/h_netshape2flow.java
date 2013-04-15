/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.models.h.netshape2flow;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.RasterFactory;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.MultiPoint;

import eu.hydrologis.JGrassModelsPlugin;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.features.FeatureUtilities;
import eu.hydrologis.jgrass.libs.utils.raster.RasterUtilities;
import eu.hydrologis.libs.messages.help.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;
/**
 * OpenMi based netshape2flow model
 * 
 * @author Andrea Antonello - www.hydrologis.com
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 */
public class h_netshape2flow extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */

    public final static String flowNetID = "flownet"; //$NON-NLS-1$

    public final static String channelNetID = "channelnet"; //$NON-NLS-1$

    public final static String netShapeID = "netshape"; //$NON-NLS-1$

    private final static String modelParameters = Messages.getString("h_netshape2flow.usage"); //$NON-NLS-1$

    private ILink flowNetLink = null;

    private ILink channelNetLink = null;

    private ILink netShapeLink = null;

    private IInputExchangeItem netShapeDataInputEI = null;

    private IOutputExchangeItem flowNetDataOutputEI = null;

    private IOutputExchangeItem channelNetDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private String activeField;

    private String idField;

    private GeometryFactory gFac = new GeometryFactory();

    private boolean createproblemshpField = false;

    private List<Coordinate> problemPointsList = new ArrayList<Coordinate>();

    private boolean doTile;

    private String locationPath;

    private WritableRaster flowImage = null;

    private WritableRaster netImage = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private final static double NaN = JGrassConstants.doubleNovalue;
    public h_netshape2flow() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_netshape2flow( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * in this method map's properties are defined... location, mapset... and than
     * IInputExchangeItem and IOutputExchangeItem are created
     */
    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        String mapset = null;
        if (properties != null) {
            for( IArgument argument : properties ) {
                String key = argument.getKey();
                if (key.compareTo(ModelsConstants.GRASSDB) == 0) {
                    grassDb = argument.getValue();
                } else if (key.compareTo(ModelsConstants.LOCATION) == 0) {
                    location = argument.getValue();
                } else if (key.compareTo(ModelsConstants.MAPSET) == 0) {
                    mapset = argument.getValue();
                } else if (key.compareTo("activefield") == 0) { //$NON-NLS-1$
                    activeField = argument.getValue();
                    if (activeField == null) {
                        isOkToGo = false;
                        String pattern = "Parameter {0} supposed to be used but not supplied.";
                        Object[] args = new Object[]{key};
                        pattern = MessageFormat.format(pattern, args);
                        throw new ModelsIllegalargumentException(pattern, this);
                    }
                } else if (key.compareTo("idfield") == 0) { //$NON-NLS-1$
                    idField = argument.getValue();
                    if (idField == null) {
                        isOkToGo = false;
                        String pattern = "Parameter {0} supposed to be used but not supplied.";
                        Object[] args = new Object[]{key};
                        pattern = MessageFormat.format(pattern, args);
                        throw new ModelsIllegalargumentException(pattern, this);
                    }
                } else if (key.compareTo("createproblemshp") == 0) { //$NON-NLS-1$
                    try {
                        createproblemshpField = Boolean.parseBoolean(argument.getValue());
                    } catch (Exception e) {
                        String pattern = "Parameter {0} not recognized for model h.netshape2flow.";
                        Object[] args = new Object[]{key};
                        pattern = MessageFormat.format(pattern, args);
                        throw new ModelsIllegalargumentException(pattern, this);
                    }
                    if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                        doTile = Boolean.getBoolean(argument.getValue());
                    }
                }
            }
        }
        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);
        componentDescr = "h.netshape2flow"; //$NON-NLS-1$
        componentId = null;

        /*
         * create the exchange items
         */
        // flow net output
        flowNetDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        // channel net output
        channelNetDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        // netshape output
        netShapeDataInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this, null);
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (netImage == null || flowImage == null) {
            if (linkID.equals(flowNetLink.getID()) || linkID.equals(channelNetLink.getID())) {
                // the output links are calling
                IValueSet netShapeValueSet = netShapeLink.getSourceComponent().getValues(time, netShapeLink.getID());
                FeatureCollection<SimpleFeatureType, SimpleFeature> fcNet = ((JGrassFeatureValueSet) netShapeValueSet)
                        .getFeatureCollection();
                /*
                 * adapt the active region to the bounds of the network
                 */
                // Envelope bounds = fcNet.getBounds();
                // activeRegion = Window.adaptActiveRegionToEnvelope(bounds,
                // activeRegion);
                // just do it
                WritableRaster[] result = netShapeToFlow(fcNet, activeRegion);
                flowImage = result[0];
                netImage = result[1];

                if (createproblemshpField && problemPointsList.size() > 0) {

                    // create the feature type
                    SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
                    // set the name
                    String typeName = "problemslayer"; //$NON-NLS-1$
                    b.setName(typeName);
                    // add a geometry property
                    b.setCRS(ApplicationGIS.getActiveMap().getViewportModel().getCRS());
                    b.add("the_geom", MultiPoint.class); //$NON-NLS-1$
                    // add some properties
                    b.add("cat", Integer.class); //$NON-NLS-1$
                    // build the type
                    SimpleFeatureType type = b.buildFeatureType();
                    // create the feature
                    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);

                    FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = FeatureCollections.newCollection();
                    for( int i = 0; i < problemPointsList.size(); i++ ) {
                        MultiPoint mPoint = gFac.createMultiPoint(new Coordinate[]{problemPointsList.get(i)});
                        Object[] values = new Object[]{mPoint, i};
                        // add the values
                        builder.addAll(values);
                        // build the feature with provided ID
                        SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + i); //$NON-NLS-1$
                        featureCollection.add(feature);
                    }

                    JGrassCatalogUtilities.removeMemoryServiceByTypeName(typeName);
                    FeatureUtilities.featureCollectionToTempLayer(featureCollection);

                    // message
                    if (JGrassModelsPlugin.getDefault() != null) {
                        Display.getDefault().asyncExec(new Runnable(){

                            public void run() {

                                Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                                MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING);
                                msgBox
                                        .setMessage("Problems were encountered while creating the map. It is possible that you will have to modify the start shapefile in some parts. Have a look a the temporary layer with the points that was created.");
                                msgBox.open();

                            }

                        });
                    }
                }
                // check that there is only one exit
                out.println("********************************************");
                out.println("*             checking for exits           *");
                out.println("********************************************");
                RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
                RandomIter channelNetRandomIter = RandomIterFactory.create(netImage, null);

                int minX = flowImage.getMinX();
                int minY = flowImage.getMinY();
                int maxX = minX + flowImage.getWidth();
                int maxY = minY + flowImage.getHeight();
                for( int j = minY; j < maxY; j++ ) {
                    for( int i = minX; i < maxX; i++ ) {
                        if (flowRandomIter.getSampleDouble(i, j, 0) == 10) {
                            out.println("Exit found at end of channel of id: " + channelNetRandomIter.getSampleDouble(i, j, 0));
                        }
                    }
                }
                channelNetRandomIter.done();
                flowRandomIter.done();

            }
        }
        // writes the maps
        CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
        if (linkID.equals(flowNetLink.getID())) {
            FluidUtils.setJAInoValueBorderIT(netImage);
            jgrValueSet = new JGrassGridCoverageValueSet(netImage, activeRegion, crs);
            return jgrValueSet;
        } else if (linkID.equals(channelNetLink.getID())) {
            FluidUtils.setJAInoValueBorderIT(netImage);
            jgrValueSet = new JGrassGridCoverageValueSet(netImage, activeRegion, crs);
            return jgrValueSet;
        }

        return null;

    }

    private WritableRaster[] netShapeToFlow( FeatureCollection<SimpleFeatureType, SimpleFeature> fcNet, JGrassRegion activeRegion ) {

        // create new matrixes for the outputs
        // get rows and cols from the active region
        int activerows = activeRegion.getRows();
        int activecols = activeRegion.getCols();
        // the resolution to take care of. This should be a square environment
        double res = activeRegion.getNSResolution();
        SampleModel sm = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_DOUBLE, activecols, activerows, 1);
        // flow[][] contains drainage direction
        WritableRaster flowTmpImage = FluidUtils.createDoubleWritableRaster(activecols, activerows, null, sm, NaN);
        // netNu contains the number of the channels
        WritableRaster netTmpImage = FluidUtils.createDoubleWritableRaster(activecols, activerows, null, sm, NaN);

        WritableRandomIter flowRandomIter = RandomIterFactory.createWritable(flowTmpImage, null);
        WritableRandomIter netRandomIter = RandomIterFactory.createWritable(netTmpImage, null);

        int activeFieldPosition = -1;
        // if a field for active reach parts was passed
        SimpleFeatureType simpleFeatureType = fcNet.getSchema();
        if (activeField != null) {
            activeFieldPosition = simpleFeatureType.indexOf(activeField);
        }
        int idFieldPosition = -1;
        if (idField != null) {
            idFieldPosition = simpleFeatureType.indexOf(idField);
        }
        FeatureIterator<SimpleFeature> featureIterator = fcNet.features();
        int index = 1;
        while( featureIterator.hasNext() ) {
            out.println("Processing reach N." + index++);
            SimpleFeature feature = featureIterator.next();
            // if the reach is not active, do not use it
            if (activeFieldPosition != -1) {
                String attr = (String) feature.getAttribute(activeFieldPosition);
                if (attr == null) {
                    // do what? Means to be active or not? For now it is dealt
                    // as active.
                } else if (attr.trim().substring(0, 1).equalsIgnoreCase("n")) { //$NON-NLS-1$
                    // reach is not active
                    continue;
                }
            }
            // find the id of the reach
            int id = -1;
            try {
                id = Integer.parseInt(String.valueOf(feature.getAttribute(idFieldPosition)));
            } catch (Exception e) {
                String[] idSplit = feature.getID().split("\\."); //$NON-NLS-1$
                id = Integer.parseInt(idSplit[idSplit.length - 1]);
            }
            // if the feature is active, start working on it
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            Coordinate[] coordinates = geometry.getCoordinates();

            // boolean isLastCoordinateOfSegment = false;
            // boolean isSecondLastCoordinateOfSegment = false;

            Coordinate lastCoord = coordinates[coordinates.length - 1];
            int[] lastPoint = RasterUtilities.putClickToCenterOfCell(activeRegion, lastCoord);
            for( int i = 0; i < coordinates.length - 1; i++ ) {
                // if (i == coordinates.length - 2) {
                // isLastCoordinateOfSegment = true;
                // }
                // if (i == coordinates.length - 3) {
                // isSecondLastCoordinateOfSegment = true;
                // }
                Coordinate first = coordinates[i];
                Coordinate second = coordinates[i + 1];

                LineSegment lineSegment = new LineSegment(first, second);
                double segmentLength = lineSegment.getLength();
                double runningLength = 0.0;
                while( runningLength <= segmentLength ) {
                    Coordinate firstPoint = lineSegment.pointAlong(runningLength / segmentLength);
                    // if the resolution is bigger than the length, use the
                    // length, i.e. 1
                    double perc = (runningLength + res) / segmentLength;
                    Coordinate secondPoint = lineSegment.pointAlong(perc > 1.0 ? 1.0 : perc);
                    int[] firstOnRaster = RasterUtilities.putClickToCenterOfCell(activeRegion, firstPoint);
                    int[] secondOnRaster = RasterUtilities.putClickToCenterOfCell(activeRegion, secondPoint);
                    /*
                     * if there is already a value in that point, and if the point in the output
                     * matrix is an outlet (10), then it is an end of another reach and it is ok to
                     * write over it. If it is another value and my actual reach is not at the end,
                     * then jump over it, the outlet is threated at the after the loop. Let's create
                     * a temporary resource that contains all the problem points. The user will for
                     * now have to change the shapefile to proceed.
                     */
                    if (!isNovalue(flowRandomIter.getSampleDouble(firstOnRaster[1], firstOnRaster[0], 0))
                            && flowRandomIter.getSampleDouble(firstOnRaster[1], firstOnRaster[0], 0) != 10.0) {

                        if (i > coordinates.length - 2) {
                            runningLength = runningLength + res;
                            continue;
                        }
                    }
                    /*
                     * if the two analized points are equal, the point will be added at the next
                     * round
                     */
                    if (firstOnRaster.equals(secondOnRaster)) {
                        runningLength = runningLength + res;
                        continue;
                    }
                    // find the flowdirection between the point and the one
                    // after it
                    int rowDiff = secondOnRaster[0] - firstOnRaster[0];
                    int colDiff = secondOnRaster[1] - firstOnRaster[1];
                    int flowDirection = FluidUtils.getFlowDirection( colDiff,rowDiff);

                    if (isNovalue(flowRandomIter.getSampleDouble(firstOnRaster[1], firstOnRaster[0], 0))
                            || (lastPoint[0] != secondOnRaster[0] && lastPoint[1] != secondOnRaster[1])) {
                        flowRandomIter.setSample(firstOnRaster[1], firstOnRaster[0], 0, flowDirection);
                    }

                    /* I have add this if statment in order to preserve the continuity of the main
                    * river.(first problem in the report)
                    */
                    if (isNovalue(netRandomIter.getSampleDouble(firstOnRaster[1], firstOnRaster[0], 0))
                            || (lastPoint[0] != secondOnRaster[0] && lastPoint[1] != secondOnRaster[1])) {
                        netRandomIter.setSample(firstOnRaster[1], firstOnRaster[0], 0, id);
                    }
                    // increment the distance
                    runningLength = runningLength + res;
                }
                /*
                 * note that the last coordinate is always threated when it is the first of the next
                 * segment. That is good, so we are able to threat the last coordinate of the reach
                 * differently.
                 */

            }
            Coordinate lastCoordinate = coordinates[coordinates.length - 1];
            int[] lastOnRaster = RasterUtilities.putClickToCenterOfCell(activeRegion, lastCoordinate);
            /*
             * the last is 10, but if there is already another value in the grid, then a major reach
             * has already put its value in it. If that is true, then we do not add this reach's
             * outlet, since it is just a confluence
             */
            if (isNovalue(flowRandomIter.getSampleDouble(lastOnRaster[1], lastOnRaster[0], 0))) {
                flowRandomIter.setSample(lastOnRaster[1], lastOnRaster[0], 0, 10.0);
                netRandomIter.setSample(lastOnRaster[1], lastOnRaster[0], 0, id);
            }
        }
        flowRandomIter.done();
        netRandomIter.done();
        return new WritableRaster[]{flowTmpImage, netTmpImage};
    }
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(netShapeID)) {
            netShapeLink = link;
        }
        if (id.equals(flowNetID)) {
            flowNetLink = link;
        }
        if (id.equals(channelNetID)) {
            channelNetLink = link;
        }
    }

    public void finish() {
        flowImage = null;
        netImage = null;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return netShapeDataInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: flownet & channelnet
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return flowNetDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return channelNetDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 2;
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowNetLink.getID())) {
            flowNetLink = null;
        }
        if (linkID.equals(channelNetLink.getID())) {
            channelNetLink = null;
        }
        if (linkID.equals(netShapeLink.getID())) {
            netShapeLink = null;
        }
    }

}
