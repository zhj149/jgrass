package eu.hydrologis.jgrass.models.h.seol;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
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
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.jgrass.models.h.magnitudo.h_magnitudo;
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIOException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

public class h_seol extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String quantityID = "quantity";

    public final static String netID = "net";

    public final static String flowID = "flow";

    public final static String seolID = "seol";

    public final static String seolShapeID = "seolshape";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_seol.usage");

    private ILink quantityLink = null;

    private ILink netLink = null;

    private ILink flowLink = null;

    private ILink seolLink = null;

    private ILink seolShapeLink = null;

    private IOutputExchangeItem seolDataOutputEI = null;

    private IInputExchangeItem quantityDataInputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IOutputExchangeItem seolShapeDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private GeometryFactory newFactory = new GeometryFactory();

    private FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;

    private boolean doTile = false;

    private String locationPath;

    private WritableRaster seolImage;

    /** */
    public h_seol() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_rescaleddistance

    /** */
    public h_seol( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_rescaleddistance

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(quantityID)) {
            quantityLink = link;
        }
        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(seolID)) {
            seolLink = link;
        }
        if (id.equals(seolShapeID)) {
            seolShapeLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: quantity, flow
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return netDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return quantityDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 3;
    }

    /**
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: seol
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return seolDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return seolShapeDataOutputEI;
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
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        // reads input maps
        if (seolImage == null) {

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);
            GridCoverage2D quantityGC = ModelsConstants.getGridCoverage2DFromLink(quantityLink, time, err);

            PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();
            PlanarImage netImage = (PlanarImage) netGC.getRenderedImage();
            PlanarImage quantityImage = (PlanarImage) quantityGC.getRenderedImage();

            WritableRaster flowImage2 = FluidUtils.createFromRenderedImage(FluidUtils.setJaiNovalueBorder(flowImage));
            seolImage = seol(flowImage2, netImage, quantityImage);

        }
        if (linkID.equals(seolLink.getID())) {
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            JGrassGridCoverageValueSet jgrValueSet = new JGrassGridCoverageValueSet(seolImage, activeRegion, crs);
            return jgrValueSet;
        } else if (linkID.equals(seolShapeLink.getID())) {
            if (featureCollection != null) {
                return new JGrassFeatureValueSet(featureCollection);
            } else {
                throw new ModelsIOException("An error occurred while calculating seol.", this);
            }
        }
        return null;
    }

    /**
     * in this method map's properties are defined... location, mapset... and
     * than IInputExchangeItem and IOutputExchangeItem are reated
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
                }
                if (key.compareTo(ModelsConstants.LOCATION) == 0) {
                    location = argument.getValue();
                }
                if (key.compareTo(ModelsConstants.MAPSET) == 0) {
                    mapset = argument.getValue();
                }
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.getBoolean(argument.getValue());
                }

            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.seol";
        componentId = null;

        /*
         * create the exchange items
         */
        // seol output
        seolDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // seol Shape output
        seolShapeDataOutputEI = ModelsConstants.createFeatureCollectionOutputExchangeItem(this, null);

        // net input
        netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // flow input
        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // quantity input
        quantityDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(quantityLink.getID())) {
            quantityLink = null;
        }
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(seolLink.getID())) {
            seolLink = null;
        }
    }

    /**
     * Calculates the seol
     */
    private WritableRaster seol( WritableRaster flowImage, PlanarImage netImage, PlanarImage quantityImage ) {
        int[] flow = new int[2];

        int cols = activeRegion.getCols();
        int rows = activeRegion.getRows();
        double Min_x = activeRegion.getWest();
        double Max_y = activeRegion.getNorth();
        double ResolutionNS = activeRegion.getNSResolution();
        double ResolutionWE = activeRegion.getWEResolution();

        double valore, valore_prec, th = 0;

        // NEW GEOMETRY CONTAINING THE NODES OF THE NET

        // creates a vector of geometry
        List<Geometry> newGeometryList = new ArrayList<Geometry>();
        List<Double> attributeListValue = new ArrayList<Double>();
        List<Integer> attributeListNumber = new ArrayList<Integer>();

        SimpleFeatureTypeBuilder ftBuilder = null;

        CoordinateList coordList = null;

        // if the creation of shape files is selected it inizialize the geometry
        if (seolShapeLink != null) {
            // create the feature type
            ftBuilder = new SimpleFeatureTypeBuilder();
            // set the name
            ftBuilder.setName("seol"); //$NON-NLS-1$
            // add a geometry property
            ftBuilder.add("the_geom", Point.class); //$NON-NLS-1$
            // add some properties
            ftBuilder.add("number", Integer.class); //$NON-NLS-1$
            ftBuilder.add("value", Double.class); //$NON-NLS-1$

            coordList = new CoordinateList();
        }

        WritableRandomIter flowIter = RandomIterFactory.createWritable(flowImage, null);
        
        RandomIter netRandomIter = RandomIterFactory.create(netImage, null);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (netRandomIter.getSampleDouble(i, j, 0) != 2) {
                    flowIter.setSample(i, j, 0, doubleNovalue);
                }
            }
        }
        
        
        WritableRaster magnitudoImage = new h_magnitudo().magnitudo(flowIter, cols, rows, out);
        WritableRandomIter magnitudoRandomIter = RandomIterFactory.createWritable(magnitudoImage, null);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (netRandomIter.getSampleDouble(i, j, 0) != 2) {
                    magnitudoRandomIter.setSample(i, j, 0, doubleNovalue);
                }
            }
        }
        netRandomIter.done();
        netImage = null;
        RandomIter quantityRandomIter = RandomIterFactory.create(quantityImage, null);

        WritableRaster soelImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null,
                flowImage.getSampleModel(), doubleNovalue);

        int pointNum = 0;
        WritableRandomIter soelRandomIter = RandomIterFactory.createWritable(soelImage, null);
        WritableRandomIter flowRandomIter = RandomIterFactory.createWritable(flowImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.seol...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (flowRandomIter.getSampleDouble(i, j, 0) == 10.0 && magnitudoRandomIter.getSampleDouble(i, j, 0) > th) {
                    if (!seolLink.equals(null)) {
                        soelRandomIter.setSample(i, j, 0, quantityRandomIter.getSampleDouble(i, j, 0));
                    }
                    if (seolShapeLink != null) {
                        // creates new Object Coordinate... NODE
                        // POINT...
                        Coordinate coord = new Coordinate(Min_x + i * ResolutionWE + ResolutionWE * 0.5, Max_y - j
                                * ResolutionNS - ResolutionNS * 0.5);
                        // adds new points to CoordinateList
                        coordList.add(coord);
                        // creates new point (node)
                        Point newPoint = newFactory.createPoint(coord);
                        // adds the point to the geometry
                        newGeometryList.add(newPoint);
                        // adds the attributes to the List...
                        attributeListValue.add(quantityRandomIter.getSampleDouble(i, j, 0));
                        attributeListNumber.add(pointNum + 1);
                        pointNum++;
                    }
                } else if (flowRandomIter.getSampleDouble(i, j, 0) < 9.0 && flowRandomIter.getSampleDouble(i, j, 0) > 0.0
                        && magnitudoRandomIter.getSampleDouble(i, j, 0) > th
                        && !isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    flow[0] = i;
                    flow[1] = j;
                    valore_prec = magnitudoRandomIter.getSampleDouble(flow[0], flow[1], 0);
                    if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                        return null;
                    if (!isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0))) {
                        valore = magnitudoRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        if (valore_prec != valore) {
                            if (!seolLink.equals(null)) {
                                soelRandomIter.setSample(i, j, 0, quantityRandomIter.getSampleDouble(i, j, 0));
                            }
                            if (seolShapeLink != null) {
                                // creates new Object Coordinate... NODE
                                // POINT...
                                Coordinate coord = new Coordinate(Min_x + i * ResolutionWE + ResolutionWE * 0.5, Max_y - j
                                        * ResolutionNS - ResolutionNS * 0.5);
                                // adds new points to CoordinateList
                                coordList.add(coord);
                                // creates new point (node)
                                Point newPoint = newFactory.createPoint(coord);
                                // adds the point to the geometry
                                newGeometryList.add(newPoint);
                                // adds the attributes to the List...
                                attributeListValue.add(quantityRandomIter.getSampleDouble(i, j, 0));
                                attributeListNumber.add(pointNum + 1);
                                pointNum++;
                            }
                        }

                    }

                } else {
                    soelRandomIter.setSample(i, j, 0, doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();

        if (seolShapeLink != null) {

            // build the type
            SimpleFeatureType type = ftBuilder.buildFeatureType();
            // create the feature
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);

            featureCollection = FeatureCollections.newCollection();

            for( int k = 0; k < newGeometryList.size(); k++ ) {
                Geometry geometry = newGeometryList.get(k);
                Integer pointNumber = attributeListNumber.get(k);
                Double pointValue = attributeListValue.get(k);

                Object[] values = new Object[]{geometry, pointNumber, pointValue};
                // add the values
                builder.addAll(values);
                // build the feature with provided ID
                SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + k); //$NON-NLS-1$
                featureCollection.add(feature);
            }
        }
        return soelImage;
    }
}
