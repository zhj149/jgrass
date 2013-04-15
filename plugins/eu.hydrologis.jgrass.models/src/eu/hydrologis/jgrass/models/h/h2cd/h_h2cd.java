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
package eu.hydrologis.jgrass.models.h.h2cd;

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
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the h2cd (Hillslope2ChannelDistance) model. It calculates
 * for each hillslope pixel its distance from the river networks, following the steepest descent
 * (i.e. the drainage directions). The program can work in two different ways: it can calculate the
 * distance from the outlet either in number of pixels (<I>0: simple distance</I> mode (mode=0)),
 * or in meters ( <I>1: topological distance</I> mode (mode=1)).
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the drainage directions (-flow); </LI>
 * <LI>the map containing the network (-net); </LI>
 * <LI>the method (-mode); </LI>
 * </OL>
 * <P>
 * </DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map containing the distance of every point from the river network (-h2cd); </LI>
 * </OL>
 * <P>
 * </DD>
 * Usage: h.h2cd --igrass-net net --igrass-flow flow --ograss-h2cd h2cD --mode 0/1
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini Andrea, Francheschi
 *         Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_h2cd extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String netID = "net";

    public final static String flowID = "flow";

    public final static String h2cDID = "h2cd";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_h2cd.usage");

    private ILink netLink = null;

    private ILink flowLink = null;

    private ILink h2cDLink = null;

    private IOutputExchangeItem h2cDDataOutputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private int mode = 0;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile;

    private String locationPath;

    public h_h2cd() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_h2cd( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(h2cDID)) {
            h2cDLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: pit, flow
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return netDataInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return flowDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 2;
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: h2cd
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return h2cDDataOutputEI;
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
        if (linkID.equals(h2cDLink.getID())) {
            // reads input maps
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);
            PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();
            PlanarImage netImage = (PlanarImage) netGC.getRenderedImage();
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            PlanarImage flowImageTmp = FluidUtils.setJaiNovalueBorder(flowImage);

            WritableRaster flowImage2 = FluidUtils.createFromRenderedImage(flowImageTmp);
            WritableRaster h2cDImage = h2cD(flowImage2, netImage);
            if (h2cDImage == null) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(h2cDImage, activeRegion, crs);
                return jgrValueSet;
            }
        }
        return null;
    }

    /**
     * in this method map's properties are defined... location, mapset... and than
     * IInputExchangeItem and IOutputExchangeItem are reated
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

        componentDescr = "h.h2cd";
        componentId = null;

        /*
         * create the exchange items
         */
        // h2cd output

        h2cDDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // net input

        netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(h2cDLink.getID())) {
            h2cDLink = null;
        }
    }

    /**
     * Calculates the h2cd in every pixel of the map
     */
    private WritableRaster h2cD( WritableRaster flowImage2, RenderedImage netImage ) {
        // get rows and cols from the active regio
        // netImage.getData(new Rectangle(0,0,9,7));
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        // get resolution of the active region
        double dx = activeRegion.getWEResolution();
        double dy = activeRegion.getNSResolution();
        FluidUtils.setJAInoValueBorderIT(flowImage2);
        WritableRandomIter flowRandomIter = RandomIterFactory.createWritable(flowImage2, null);
        RandomIter netRandomIter = RandomIterFactory.create(netImage, null);

        WritableRaster h2cDImage = FluidUtils.createDoubleWritableRaster(flowImage2.getWidth(), flowImage2.getHeight(), null,
                flowImage2.getSampleModel(), 0.0);
        WritableRandomIter h2cDRandomIter = RandomIterFactory.createWritable(h2cDImage, null);
        // create new matrix

        out.println(Messages.getString("working") + " h.h2cd");
        // setting novalue border...

        // FluidUtils.setNovalueBorder(netData);

        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (netRandomIter.getSample(i, j, 0) == 2)
                    flowRandomIter.setSample(i, j, 0, 10);
            }
        }

        if (mode == 1) {
            FluidUtils.outletdistance(flowRandomIter, h2cDImage, dx, dy, out);
            for( int j = 0; j < rows; j++ ) {
                for( int i = 0; i < cols; i++ ) {
                    if (netRandomIter.getSample(i, j, 0) == 2 && !isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                        h2cDRandomIter.setSample(i, j, 0, 0);
                    } else if (isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                        h2cDRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                    }
                }
            }
            netRandomIter.done();
            h2cDRandomIter.done();
            flowRandomIter.done();
        } else if (mode == 0) {
            FluidUtils.topological_outletdistance(flowRandomIter, h2cDImage, out);
            h2cDRandomIter = RandomIterFactory.createWritable(h2cDImage, null);
            // create new matrix
            for( int j = 0; j < rows; j++ ) {
                for( int i = 0; i < cols; i++ ) {
                    if (netRandomIter.getSample(i, j, 0) == 2 && !isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                        h2cDRandomIter.setSample(i, j, 0, 0);
                    } else if (isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                        h2cDRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                    }
                }
            }
        }
        return h2cDImage;
    }
}
