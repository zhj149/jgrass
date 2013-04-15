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
package eu.hydrologis.jgrass.models.h.netnumbering;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.StringSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the netnumbering model. It assign
 * numbers to the network's links and can be used by hillslope2channelattribute
 * to label the hillslope flowing into the link with the same number.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map containing the drainage directions (-flow);</LI>
 * <LI>the map containing the channel network (-net);
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map containing the net with the streams numerated (-netnumber);</LI>
 * <LI>the map containing the sub-basins (-basin).</LI>
 * </OL>
 * <P></DD>
 * <p>
 * Usage mode 0: h.netnumbering --mode 0 --igrass-flow flow --igrass-net net
 * --ograss-netnumber netnumber --ograss-basin basin
 * </p>
 * <p>
 * Usage mode 1: h.netnumbering --mode 1 --thtca value --igrass-flow flow
 * --igrass-net net --igrass-tca tca --ograss-netnumber netnumber--ograss-basin
 * basin
 * </p>
 * <p>
 * Usage mode 2: h.netnumbering --mode 2 --igrass-flow flow --igrass-net net
 * --ishapefile-pointshape "filepath" --ograss-netnumber netnumber--ograss-basin
 * basin
 * </p>
 * <p>
 * Usage mode 3: h.netnumbering --mode 3 --thtca value --igrass-flow flow
 * --igrass-net net --igrass-tca tca --ishapefile-pointshape "filepath"
 * --ograss-netnumber netnumber--ograss-basin basin
 * </p>
 * <p>
 * With color map: h.netnumbering --igrass-flow flow --igrass-net net
 * --ograss-netnumber netnumberx --ograss-basin basinx --ocolor-colornumbers
 * netnumber --ocolor-colorbasins basin
 * <p>
 * <DT><STRONG>Notes:</STRONG><BR>
 * </DT>
 * <DD>The algorithm start from the channel heads which are numbered first.
 * Then, starting again from each source, the drainage direction are followed
 * till a junction is found. If the link downhill the junction was already
 * numbered, a new source is chosen. Otherwise the network is scanned downstream
 * ad a new number is attributed to the link's pixels. Was extensively used for
 * the calculations in [11] (See also: Tca) <BR>
 * </OL></DD>
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Andrea Cozzini, Riccardo
 *         Rigon, (2004).
 */
public class h_netnumbering extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String netID = "net";

    public final static String tcaID = "tca";

    public final static String netnumberID = "netnumber";

    public final static String basinID = "basin";

    public final static String pointShapeID = "pointshape";

    public final static String colorNumID = "colornumbers";

    public final static String colorBasinsID = "colorbasins";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_netnumbering.usage");

    private ILink flowLink = null;

    private ILink netLink = null;

    private ILink tcaLink = null;

    private ILink netnumberLink = null;

    private ILink basinLink = null;

    private ILink colorNumLink = null;

    private ILink colorBasinsLink = null;

    private ILink pointShapeLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private IInputExchangeItem pointShapeDataInputEI = null;

    private IOutputExchangeItem netnumberDataOutputEI = null;

    private IOutputExchangeItem basinDataOutputEI = null;

    private IOutputExchangeItem colorNumDataOutputEI = null;

    private IOutputExchangeItem colorBasinsDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private static List<Geometry> geomVect = null;

    private static List<HashMap<String, ? >> attributeVect = null;

    private List<Integer> nstream;

    private String[] st = null;

    private int mode = 0;

    private double thtca = 0;

    private final PrintStream out;

    private final PrintStream err;

    private boolean doTile;

    private String locationPath;

    private WritableRaster[] result;

    private CoordinateReferenceSystem crs;

    /** */
    public h_netnumbering() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_netnumbering

    /** */
    public h_netnumbering( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_netnumbering

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(tcaID)) {
            tcaLink = link;
        }
        if (id.equals(pointShapeID)) {
            pointShapeLink = link;
        }
        if (id.equals(netnumberID)) {
            netnumberLink = link;
        }
        if (id.equals(basinID)) {
            basinLink = link;
        }
        if (id.equals(colorNumID)) {
            colorNumLink = link;
        }
        if (id.equals(colorBasinsID)) {
            colorBasinsLink = link;
        }
    }

    public void finish() {
        result = null;
    }

    /**
     * There is an IInputExchangeItem: flow, net
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return netDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return tcaDataInputEI;
        }
        if (inputExchangeItemIndex == 3) {
            return pointShapeDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 4;
    }

    /**
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: netnumber & basin
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return netnumberDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return basinDataOutputEI;
        }
        if (outputExchangeItemIndex == 2) {
            return colorNumDataOutputEI;
        }
        if (outputExchangeItemIndex == 3) {
            return colorBasinsDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 4;
    }

    /**
     * return the results of the model...
     * @throws IOException 
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws IOException {
        if (result == null) {
            int cols = activeRegion.getCols();
            int rows = activeRegion.getRows();

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);

            RenderedImage flowImage = flowGC.getRenderedImage();
            WritableRaster flowRaster = FluidUtils.createFromRenderedImageWithNovalueBorder(flowImage);
            WritableRandomIter flowIter = RandomIterFactory.createWritable(flowRaster, null);

            RenderedImage netImage = netGC.getRenderedImage();
            RandomIter netIter = RandomIterFactory.create(netImage, null);

            RenderedImage tcaImage = null;
            RandomIter tcaIter = null;
            if (mode == 1) {
                GridCoverage2D tcaGC = ModelsConstants.getGridCoverage2DFromLink(tcaLink, time, err);
                tcaImage = tcaGC.getRenderedImage();
                tcaIter = RandomIterFactory.create(tcaImage, null);
            }
            if (pointShapeLink != null) {
                HashMap<String, Object> geomMap = null;

                FeatureCollection<SimpleFeatureType, SimpleFeature> fcNet = ModelsConstants.getFeatureCollectionFromLink(pointShapeLink, time, err);

                List<String> key = new ArrayList<String>();
                // print out a feature type header and wait for user input
                SimpleFeatureType ft = fcNet.getSchema();
                for( int i = 0; i < ft.getAttributeCount(); i++ ) {
                    AttributeType at = ft.getType(i);
                    /*
                     * if (!Geometry.class.isAssignableFrom(at.getType()))
                     * System.out.print(at.getName() + "\t");
                     */
                    key.add(at.getName().toString());
                }
                FeatureIterator<SimpleFeature> featureIterator;
                geomVect = new ArrayList<Geometry>();
                attributeVect = new ArrayList<HashMap<String, ? >>();
                featureIterator = fcNet.features();
                while( featureIterator.hasNext() ) {
                    SimpleFeature feature = featureIterator.next();
                    geomMap = new HashMap<String, Object>();
                    for( int i = 0; i < feature.getAttributeCount(); i++ ) {
                        Object attribute = feature.getAttribute(i);
                        if (attribute != null) {
                            feature.getAttribute(i).getClass();
                            if (!(attribute instanceof Geometry))
                                geomMap.put(key.get(i), attribute);
                        } else {
                            geomMap.put(key.get(i), "null");
                        }
                    }
                    geomMap.put("id", feature.getID());
                    geomVect.add((Geometry) feature.getDefaultGeometry());
                    attributeVect.add(geomMap);
                    geomMap = null;
                }
                featureIterator.close();
            }

            result = netnumbering(flowIter, netIter, tcaIter, rows, cols);
        }

        JGrassGridCoverageValueSet jgrValueSet;
        if (linkID.equals(netnumberLink.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(result[0], activeRegion, crs);
            return jgrValueSet;
        } else if (linkID.equals(basinLink.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(result[1], activeRegion, crs);
            return jgrValueSet;
        }

        if (linkID.equals(colorNumLink.getID())) {
            StringSet jgrStringValueSet = new StringSet(st);
            return jgrStringValueSet;
        }
        if (linkID.equals(colorBasinsLink.getID())) {
            StringSet jgrStringValueSet = new StringSet(st);
            return jgrStringValueSet;
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
                if (key.compareTo("mode") == 0) {
                    mode = Integer.parseInt(argument.getValue());
                }
                if (key.compareTo("thtca") == 0) {
                    thtca = Double.parseDouble(argument.getValue());
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

        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        componentDescr = "h.netnumbering";
        componentId = null;

        /*
         * create the exchange items
         */
        // netnumber output

        netnumberDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        // basin output

        basinDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // colorNum map output
        IElementSet colorNumMapElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity colorNumMapQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.COLORMAP, ModelsConstants.UNITID_COLORMAP);
        colorNumDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, colorNumMapQuantity, colorNumMapElementSet);

        // colorBasins map output
        IElementSet colorBasinsMapElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity colorBasinsMapQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.COLORMAP, ModelsConstants.UNITID_COLORMAP);
        colorBasinsDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, colorBasinsMapQuantity, colorBasinsMapElementSet);

        // element set defining what we want to read
        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // net input

        netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // tca input

        tcaDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // pointShape input
        IElementSet pointShapeElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity pointShapeQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.FEATURECOLLECTION, ModelsConstants.UNITID_FEATURE);
        pointShapeDataInputEI = UtilitiesFacade.createInputExchangeItem(this, pointShapeQuantity, pointShapeElementSet);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
        if (linkID.equals(tcaLink.getID())) {
            tcaLink = null;
        }
        if (linkID.equals(netnumberLink.getID())) {
            netnumberLink = null;
        }
        if (linkID.equals(basinLink.getID())) {
            basinLink = null;
        }
        if (linkID.equals(colorNumLink.getID())) {
            colorNumLink = null;
        }
        if (linkID.equals(colorBasinsLink.getID())) {
            colorBasinsLink = null;
        }
    }

    /**
     * Calculates the netnumbering in every pixel of the map
     * 
     * @return
     */
    private WritableRaster[] netnumbering( WritableRandomIter flowIter, RandomIter netIter, RandomIter tcaIter, int height, int width ) {

        nstream = new ArrayList<Integer>();

        // setting novalues...
        WritableRaster netNumberImage = null;

        if (mode == 0) {
            // calls netnumbering in FluiUtils...
            netNumberImage = FluidUtils.netNumbering(nstream, flowIter, netIter, width, height, out);
            if (netNumberImage == null) {
                return null;
            }
        } else if (mode == 1) {
            // calls netnumbering in FluiUtils...
            if (tcaIter == null) {
                err.println("Error!! This method needs the map of TCA");
            }
            netNumberImage = FluidUtils.netNumberingWithTca(nstream, flowIter, netIter, tcaIter, width, height, thtca, out);
            if (netIter == null) {
                return null;
            }
        } else if (mode == 2) {
            // calls netnumbering in FluiUtils...
            if (attributeVect == null || geomVect == null) {
                err.println("Error!! This method needs the shapefile with points");
            }
            netNumberImage = FluidUtils.netNumberingWithPoints(nstream, flowIter, netIter, activeRegion, attributeVect, geomVect, err, out);
            if (netIter == null) {
                return null;
            }
        } else {
            // calls netnumbering in FluiUtils...
            if (attributeVect == null || geomVect == null || tcaIter == null) {
                err.println("Error!! This method needs the shapefile with points ant the map of TCA");
            }
            netNumberImage = FluidUtils.netNumberingWithPointsAndTca(nstream, flowIter, netIter, tcaIter, thtca, activeRegion, attributeVect, geomVect, err, out);
            if (netIter == null) {
                return null;
            }
        }

        // calls extractSubbasins in FluiUtils...
        WritableRandomIter netNumIter = RandomIterFactory.createWritable(netNumberImage, null);
        WritableRaster basinImage = FluidUtils.extractSubbasins(flowIter, netIter, netNumIter, height, width, out);
        if (basinImage == null) {
            return null;
        }

        flowIter = null;
        netIter = null;
        return new WritableRaster[]{netNumberImage, basinImage};
    }

    /**
     * Create a color map for a net where every stream has a different number or
     * for a map of basins where every basin has a different number.
     * 
     * @return
     */
    private void assigneColorToStreams() {
        Random rand = new Random();
        int colorindex;
        int[] colortriplet = null;
        st = new String[nstream.size() + 1];
        // Continually call nextInt() for more random integers ...
        st[0] = "%" + "1.0 " + nstream.size() + ".0";
        for( int i = 1; i <= nstream.size(); i++ ) {
            // Random integers that range from from 0 to n
            colorindex = rand.nextInt(JGrassUtilities.numberOfAvailableColors());
            colortriplet = JGrassUtilities.getColorTripletByIndex(colorindex);
            st[i] = (nstream.get(i - 1).doubleValue() + ":" + colortriplet[0] + ":" + colortriplet[1] + ":" + colortriplet[2] + " " + nstream.get(i - 1).doubleValue() + ":" + colortriplet[0] + ":"
                    + colortriplet[1] + ":" + colortriplet[2]);
        }
    }
}
