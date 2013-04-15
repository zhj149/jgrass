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
package eu.hydrologis.jgrass.models.h.h2ca;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the h2ca (Hillslope2ChannelAttribute)
 * model. It is a simple way to select a hillslope or some of its property from
 * the DEM. Since hillslope are identified by channel links, if a numbering of
 * links is available, h2cattribute gives to any pixel draining into a given
 * link the link number. Eventually, one can select all the hillslope points
 * which share the same link number, i.e. the points which belongs to the same
 * hillslope. Another use of this application (see []) is to associate to any
 * hillslope point its channel path length. In general, it labels any hillslope
 * pixel with the channel quantity found in the position where the hillslope
 * pixel drains.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the drainage directions (-flow);</LI>
 * <LI>the map containing the net (-net);</LI>
 * <LI>the map containing the attribute to estimate (-attribute);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the Hillslope2ChannelAttribute (-h2ca);</LI>
 * </OL>
 * <P></DD>
 * Usage: h.h2ca --igrass-flow flow --igrass-net net --igrass-attribute
 * attribute --ograss-h2ca h2ca
 * </p>
 * <p>
 * Note: The program actually does NOT distinguish between left and right
 * hydrographic hillslope. This would be corrected soon.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_h2ca extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String netID = "net";

    public final static String attributeID = "attribute";

    public final static String h2caID = "h2ca";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_h2ca.usage");

    private ILink flowLink = null;

    private ILink netLink = null;

    private ILink attributeLink = null;

    private ILink h2caLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IInputExchangeItem attributeDataInputEI = null;

    private IOutputExchangeItem h2caDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile;

    private String locationPath;

    public h_h2ca() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_h2ca( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

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
        if (id.equals(attributeID)) {
            attributeLink = link;
        }
        if (id.equals(h2caID)) {
            h2caLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: flow, net, attribute
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return netDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return attributeDataInputEI;
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
     * there is an IOutputExchangeItem: h2ca & aggh2ca
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return h2caDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(h2caLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D attributeGC = ModelsConstants.getGridCoverage2DFromLink(attributeLink, time, err);
            GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);

            RenderedImage flowImage = flowGC.getRenderedImage();
            PlanarImage netImage = (PlanarImage) netGC.getRenderedImage();
            PlanarImage attributeImage = (PlanarImage) attributeGC.getRenderedImage();

            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

            WritableRaster writableFlowImage = FluidUtils.createFromRenderedImage(flowImage);

            WritableRaster h2caImage = h2ca(writableFlowImage, netImage, attributeImage);
            if (h2caImage == null) {
                err.print("Errors in execution...\n");
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(h2caImage, activeRegion, crs);
                return jgrValueSet;
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

        componentDescr = "h.h2ca";
        componentId = null;

        /*
         * create the exchange items
         */
        // h2ca output

        h2caDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // net input

        netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // attribute input

        attributeDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
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
        if (linkID.equals(attributeLink.getID())) {
            attributeLink = null;
        }
        if (h2caID.equals(h2caLink.getID())) {
            h2caLink = null;
        }
    }

    /**
     * Calculates the h2ca in every pixel of the map
     * 
     * @return
     */
    private WritableRaster h2ca( WritableRaster writableFlowImage, PlanarImage netImage, PlanarImage attributeImage ) {
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();
        FluidUtils.setJAInoValueBorderIT(writableFlowImage);
        WritableRandomIter flowRandomIter = RandomIterFactory.createWritable(writableFlowImage, null);
        RandomIter netRandomIter = RandomIterFactory.create(netImage, null);
        out.println(MessageHelper.WORKING_ON + "h.h2ca..."); //$NON-NLS-1$

        // setting novalue border...
        // FluidUtils.setNovalueBorder(netData);

        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (netRandomIter.getSampleDouble(i, j, 0) == 2)
                    flowRandomIter.setSample(i, j, 0, 10);
            }
        }

        netRandomIter = null;
        netImage = null;
        // create new matrix
        RandomIter attrRandomIter = RandomIterFactory.create(attributeImage, null);
        WritableRaster h2caImage = FluidUtils.go2channel(flowRandomIter, attrRandomIter, cols, rows, out);
        WritableRandomIter h2caRandomIter = RandomIterFactory.createWritable(h2caImage, null);

        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    h2caRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
        }
        return h2caImage;
    }
}
