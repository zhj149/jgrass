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
package eu.hydrologis.jgrass.models.h.rescaleddistance3d;

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
 * The openmi compliant representation of the rescaleddistance 3D model. Rescaled distance 3D
 * calculates the distance of every pixel within the basin, considering also the vertical coordinate
 * (differently from recaleddistance which calculates its projection only).
 * <p>
 * x' = x_c + r x_h
 * </p>
 * <p>
 * where: x_c is the distance along the channels,
 * </p>
 * <p>
 * r = c / c_h
 * </p>
 * <p>
 * the ratio between the speed in the channel state, and the speed in the hillslopes, and x_h the
 * distance along the hillslopes.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the file containing the elevations of the DEM (-pit); </LI>
 * <LI>the map of the drainage directions (-flow); </LI>
 * <LI>the file containing the net (-net);
 * <LI>the channel-overland ratio (-number); </LI>
 * </LI>
 * </OL>
 * <P>
 * </DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the rescaled distances (-rdist3d); </LI>
 * </OL>
 * <P>
 * </DD>
 * Usage: h.rescaleddistance3d --igrass-pit pit --igrass-net net --igrass-flow flow --ograss-rdist3d
 * rdist3d --number number
 * </p>
 * <p>
 * Note: The program requests also the ratio between speed in the channel and speed in hillslopes.
 * The speed in channels is always greater than that in hillslopes.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Rigon Riccardo
 */
public class h_rescaleddistance3d extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit";

    public final static String netID = "net";

    public final static String flowID = "flow";

    public final static String rdist3dID = "rdist3d";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_rescaleddistance3d.usage");

    private ILink pitLink = null;

    private ILink netLink = null;

    private ILink flowLink = null;

    private ILink rdist3dLink = null;

    private IOutputExchangeItem rdist3dDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;


    private double r = 0;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile;

    private String locationPath;

    /** */
    public h_rescaleddistance3d() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_rescaleddistance

    /** */
    public h_rescaleddistance3d( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_rescaleddistance

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(rdist3dID)) {
            rdist3dLink = link;
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
        }
        if (inputExchangeItemIndex == 1) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return pitDataInputEI;
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
     * there is an IOutputExchangeItem: rdist
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return rdist3dDataOutputEI;
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
        if (linkID.equals(rdist3dLink.getID())) {
            // reads input maps
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);
            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D pitGC = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);
            PlanarImage netImage = (PlanarImage) netGC.getRenderedImage();
            PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();
            PlanarImage pitImage = (PlanarImage) pitGC.getRenderedImage();

            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            WritableRaster rDistImage = rdist(pitImage, flowImage, netImage);

            if (rDistImage == null) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(rDistImage, activeRegion, crs);
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
                if (key.compareTo("number") == 0) {
                    r = Double.parseDouble(argument.getValue());
                }   if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.getBoolean(argument.getValue());
                }
            }
        }

        /*
         * define the map path
         */      
        locationPath = grassDb + File.separator + location;

        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.rescaleddistance";
        componentId = null;

        /*
         * create the exchange items
         */
        // rdist output

        rdist3dDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // net input

        netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // pit input

        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(rdist3dLink.getID())) {
            rdist3dLink = null;
        }
    }

    /**
     * Calculates the rescaleddistance in every pixel of the map
     */
    private WritableRaster rdist(PlanarImage pitImage, PlanarImage flowImage, PlanarImage netImage) {
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        // get resolution of the active region
        double disX = activeRegion.getWEResolution();
        double disY = activeRegion.getNSResolution();
        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);
        RandomIter netRandomIter = RandomIterFactory.create(netImage, null);
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        WritableRaster rDistImage = FluidUtils.createDoubleWritableRaster(pitImage.getWidth(), pitImage.getHeight(), null,
                pitImage.getSampleModel(), null);
        WritableRandomIter rDistRandomIter = RandomIterFactory.createWritable(
                rDistImage, null);
        int[] flow = new int[2];
        int[] flow_p = new int[2];

        // create new matrix

        double count = 0.0, oldir = 0.0, dz = 0.0;

        // grid contains the dimension of pixels according with flow directions
        double[] grid = new double[11];

        grid[0] = grid[9] = grid[10] = 0.0;
        grid[1] = grid[5] = Math.abs(disX);
        grid[3] = grid[7] = Math.abs(disY);
        grid[2] = grid[4] = grid[6] = grid[8] = Math.sqrt(disX * disX + disY * disY);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.rescaleddistance3d...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                // looks for the source
                if (FluidUtils.sourcesqJAI(flowRandomIter, flow)) {
                    count = 0;
                    oldir = flowRandomIter.getSampleDouble(flow[0], flow[1],0);
                    flow_p[0] = flow[0];
                    flow_p[1] = flow[1];
                    if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1],0)))
                        return null;
                    while( !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1],0))
                            && flowRandomIter.getSampleDouble(flow[0], flow[1],0) != 10.0
                            && rDistRandomIter.getSampleDouble(flow[0], flow[1],0) <= 0 ) {
                        dz = pitRandomIter.getSampleDouble(flow_p[0], flow_p[1],0)
                                - pitRandomIter.getSampleDouble(flow[0], flow[1],0);
                        if (netRandomIter.getSampleDouble(flow[0], flow[1],0) != 2) {
                            count += (Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2)))
                                    * r;
                        } else {
                            count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                        }
                        oldir = flowRandomIter.getSampleDouble(flow[0], flow[1],0);
                        flow_p[0] = flow[0];
                        flow_p[1] = flow[1];
                        if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1],0)))
                            return null;
                    }
                    if (rDistRandomIter.getSampleDouble(flow[0], flow[1],0) > 0) {
                        dz = pitRandomIter.getSampleDouble(flow_p[0], flow_p[1],0)
                                - pitRandomIter.getSampleDouble(flow[0], flow[1],0);
                        if (netRandomIter.getSampleDouble(flow[0], flow[1],0) != 2) {
                            count += (Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2)))
                                    * r + rDistRandomIter.getSampleDouble(flow[0], flow[1],0);
                        } else {
                            count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2))
                                    + rDistRandomIter.getSampleDouble(flow[0], flow[1],0);
                        }
                        rDistRandomIter.setSample(i, j,0, count);
                    } else if (flowRandomIter.getSampleDouble(flow[0], flow[1],0) > 9) {
                        rDistRandomIter.setSample(flow[0], flow[1],0, 0);
                        dz = pitRandomIter.getSampleDouble(flow_p[0], flow_p[1],0)
                                - pitRandomIter.getSampleDouble(flow[0], flow[1],0);
                        if (netRandomIter.getSampleDouble(flow[0], flow[1],0) != 2) {
                            count += (Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2)))
                                    * r;
                        } else {
                            count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                        }
                        rDistRandomIter.setSample(i, j,0, count);
                    }

                    flow[0] = i;
                    flow[1] = j;
                    oldir = flowRandomIter.getSampleDouble(flow[0], flow[1],0);
                    flow_p[0] = flow[0];
                    flow_p[1] = flow[1];
                    if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1],0)))
                        return null;
                    while( !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1],0))
                            && flowRandomIter.getSampleDouble(flow[0], flow[1],0) != 10.0
                            && rDistRandomIter.getSampleDouble(flow[0], flow[1],0) <= 0 ) {
                        dz = pitRandomIter.getSampleDouble(flow_p[0], flow_p[1],0)
                                - pitRandomIter.getSampleDouble(flow[0], flow[1],0);
                        if (netRandomIter.getSampleDouble(flow[0], flow[1],0) != 2) {
                            count -= (Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2)))
                                    * r + rDistRandomIter.getSampleDouble(flow[0], flow[1],0);
                        } else {
                            count -= Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2))
                                    + rDistRandomIter.getSampleDouble(flow[0], flow[1],0);
                        }
                        rDistRandomIter.setSample(flow[0], flow[1],0, count);
                        if (rDistRandomIter.getSampleDouble(flow[0], flow[1],0) < 0)
                            rDistRandomIter.setSample(flow[0], flow[1],0, 0);
                        oldir = flowRandomIter.getSampleDouble(flow[0], flow[1],0);
                        flow_p[0] = flow[0];
                        flow_p[1] = flow[1];
                        if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1],0)))
                            return null;
                    }
                }
                if (isNovalue(flowRandomIter.getSampleDouble(i, j,0))) {
                    rDistRandomIter.setSample(i, j,0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();
        return rDistImage;
    }
}
