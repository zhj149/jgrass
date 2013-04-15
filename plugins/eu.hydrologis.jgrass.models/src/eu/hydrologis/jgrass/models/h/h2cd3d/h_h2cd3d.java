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
package eu.hydrologis.jgrass.models.h.h2cd3d;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

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
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the h2cd3d model. It calculates for each hillslope pixel
 * its distance from the river networks, following the steepest descent (i.e. the drainage
 * directions), considering also the vertical coordinate (differently from distance2outlet which
 * calculates its projection only).
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map containing the drainage directions (-flow); </LI>
 * <LI>the map containing the channel network (-net);
 * <LI>the map containing the elevations (-pit);
 * </OL>
 * <P>
 * </DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the file containing the distance of every point from the river network (-h2cd3d). </LI>
 * </OL>
 * <P>
 * </DD>
 * Usage h.h2cd3d --igrass-flow flow --igrass-pit pit --igrass-net net --ograss-h2cd3d h2cd3d
 * </p>
 * <p>
 * <DT><STRONG>Notes:</STRONG><BR>
 * </DT>
 * <DD>Each river network pixel presents a value of distance equal to 0. <BR>
 * </OL>
 * </DD>
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Riccardo Rigon.
 */
public class h_h2cd3d extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String pitID = "pit";

    public final static String netID = "net";

    public final static String h2cd3dID = "h2cd3d";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_h2cd3d.usage");

    private ILink flowLink = null;

    private ILink pitLink = null;

    private ILink netLink = null;

    private ILink h2cd3dLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IOutputExchangeItem h2cd3dDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private String locationPath;

    private boolean doTile = false;

    private JGrassGridCoverageValueSet jgrValueSet;

    public h_h2cd3d() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_h2cd3d( PrintStream output, PrintStream error ) {
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
        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(h2cd3dID)) {
            h2cd3dLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: flow, pit, net
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return pitDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return netDataInputEI;
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
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: h2cd3d
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return h2cd3dDataOutputEI;
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
        if (linkID.equals(h2cd3dLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }
            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);
            GridCoverage2D pitGC = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);

            PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();
            PlanarImage netImage = (PlanarImage) netGC.getRenderedImage();
            PlanarImage pitImage = (PlanarImage) pitGC.getRenderedImage();

            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            WritableRaster flowImage2 = FluidUtils.createFromRenderedImage(flowImage);
            WritableRaster h2cdImage = h2cd3d(pitImage, flowImage2, netImage);
            if (h2cdImage == null) {
                err.println("Errors in execution...\n");
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(h2cdImage, activeRegion, crs);
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
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.getBoolean(argument.getValue());
                }
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;

        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.h2cd3d";
        componentId = null;

        /*
         * create the exchange items
         */
        // h2cd3d output

        h2cd3dDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // flow input
        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // net input

        netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // pit input

        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
        if (h2cd3dID.equals(h2cd3dLink.getID())) {
            h2cd3dLink = null;
        }
    }

    /**
     * Calculates the h2cd3d in every pixel of the map
     * 
     * @return
     */
    private WritableRaster h2cd3d( PlanarImage pitImage, WritableRaster flowImage2, PlanarImage netImage ) {
        // get rows and cols from the active region
        int cols = activeRegion.getCols();
        int rows = activeRegion.getRows();

        int[] flow = new int[2];
        int[] flow_p = new int[2];

        // get resolution of the active region
        double dx = activeRegion.getWEResolution();
        double dy = activeRegion.getNSResolution();

        double oldir = 0.0, dz = 0.0, count = 0.0;

        double[] grid = new double[11];

        // grid contains the dimension of pixels according with flow directions
        grid[0] = grid[9] = grid[10] = 0;
        grid[1] = grid[5] = Math.abs(dx);
        grid[3] = grid[7] = Math.abs(dy);
        grid[2] = grid[4] = grid[6] = grid[8] = Math.sqrt(dx * dx + dy * dy);

        // setting novalue border...
        FluidUtils.setJAInoValueBorderIT(flowImage2);
        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);
        WritableRandomIter flowRandomIter = RandomIterFactory.createWritable(flowImage2, null);
        RandomIter netRandomIter = RandomIterFactory.create(netImage, null);

        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (netRandomIter.getSampleDouble(i, j, 0) == 2)
                    flowRandomIter.setSample(i, j, 0, 10);
            }
        }
        netRandomIter = null;
        netImage = null;

        WritableRaster h2cdImage = FluidUtils.createDoubleWritableRaster(pitImage.getWidth(), pitImage.getHeight(), null,
                pitImage.getSampleModel(), null);
        WritableRandomIter h2cdRandomIter = RandomIterFactory.createWritable(h2cdImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.h2cd3d...", rows - 2);
        for( int j = 1; j < rows - 1; j++ ) {
            for( int i = 1; i < cols - 1; i++ ) {
                flow[0] = i;
                flow[1] = j;
                // looks for the source
                if (FluidUtils.sourcesqJAI(flowRandomIter, flow)) {
                    count = 0;
                    oldir = flowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                    flow_p[0] = flow[0];
                    flow_p[1] = flow[1];
                    if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                        return null;
                    // calculates the distance from the river networks
                    while( !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0))
                            && flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                            && h2cdRandomIter.getSampleDouble(flow[0], flow[1], 0) <= 0 ) {
                        dz = pitRandomIter.getSampleDouble(flow_p[0], flow_p[1], 0)
                                - pitRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                        oldir = flowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        flow_p[0] = flow[0];
                        flow_p[1] = flow[1];
                        // calls go_downstream in FluidUtils
                        if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                    }
                    if (h2cdRandomIter.getSampleDouble(flow[0], flow[1], 0) > 0) {
                        dz = pitRandomIter.getSampleDouble(flow_p[0], flow_p[1], 0)
                                - pitRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2))
                                + h2cdRandomIter.getSampleDouble(flow[0], flow[1], 0);;
                        h2cdRandomIter.setSample(i, j, 0, count);
                    } else if (flowRandomIter.getSampleDouble(flow[0], flow[1], 0) > 9) {
                        h2cdRandomIter.setSample(flow[0], flow[1], 0, 0);
                        dz = pitRandomIter.getSampleDouble(flow_p[0], flow_p[1], 0)
                                - pitRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                        h2cdRandomIter.setSample(i, j, 0, count);
                    }
                    flow[0] = i;
                    flow[1] = j;
                    oldir = flowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                    flow_p[0] = flow[0];
                    flow_p[1] = flow[1];
                    // calls go_downstream in FluidUtils
                    if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                        return null;
                    while( !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0))
                            && flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                            && h2cdRandomIter.getSampleDouble(flow[0], flow[1], 0) <= 0 ) {
                        dz = pitRandomIter.getSampleDouble(flow_p[0], flow_p[1], 0)
                                - pitRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        count -= Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                        h2cdRandomIter.setSample(flow[0], flow[1], 0, count);
                        if (h2cdRandomIter.getSampleDouble(flow[0], flow[1], 0) < 0)
                            h2cdRandomIter.setSample(flow[0], flow[1], 0, 0);
                        oldir = flowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        flow_p[0] = flow[0];
                        flow_p[1] = flow[1];
                        // calls go_downstream in FluidUtils
                        if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    h2cdRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
        }
        return h2cdImage;
    }
}
