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

package eu.hydrologis.jgrass.models.h.hacklength3d;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;

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
 * It calculates the hack lengths, namely, assigned a point in the basin, the
 * distance from the watershed measured along the net (until it exists) and
 * then, proceeding again from valley upriver, along the maximal slope lines.
 * For each net confluence, the direction of the tributary with maximal
 * contributing area is chosen. If the tributaries have the same area, one of
 * the two directions is chosen at random.
 * </p>
 * <dt><strong>Inputs: </strong></dt>
 * <ol>
 * <LI>the file containing the elevations of the DEM (-pit);</LI>
 * <li>the map containing the drainage directions (-flow);</li>
 * <li>the map containing the contributing areas (-tca);</li>
 * </ol>
 * <p>
 * <dt><strong>Returns:<br>
 * </strong></dt>
 * <dd>
 * <ol>
 * <li>the map of the Hack distances (-hackl3d)</li>
 * </ol>
 * <p></dd>
 * <p>
 * Usage: h.hacklength3d --igrass-pit pit --igrass-flow flow --igrass-tca tca
 * --ograss-hackl3d hackl3d
 * </p>
 * <p>
 * <DT><STRONG>Notes:</STRONG><BR>
 * </DT>
 * <DD>Differently from Hacklength, the distance is calculated also by
 * calculating the contribution of elevation. <BR>
 * </p>
 * </OL>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Marco Pegoretti, Rigon
 *         Riccardo
 */
public class h_hacklength3d extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit";

    public final static String flowID = "flow";

    public final static String tcaID = "tca";

    public final static String hackl3dID = "hackl3d";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_hacklength3d.usage");

    private ILink pitLink = null;

    private ILink flowLink = null;

    private ILink tcaLink = null;

    private ILink hackl3dLink = null;

    private IOutputExchangeItem hackl3dDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private String locationPath;

    private boolean doTile;

    private JGrassGridCoverageValueSet jgrValueSet;

    public h_hacklength3d() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_hacklength3d( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(tcaID)) {
            tcaLink = link;
        }
        if (id.equals(hackl3dID)) {
            hackl3dLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: pit, flow, tca
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return tcaDataInputEI;
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
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: hacklength3d
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return hackl3dDataOutputEI;
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
        if (linkID.equals(hackl3dLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D tcaGC = ModelsConstants.getGridCoverage2DFromLink(tcaLink, time, err);
            GridCoverage2D pitGC = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);

            RenderedImage flowImage = flowGC.getRenderedImage();
            WritableRaster flowRaster = FluidUtils.createFromRenderedImageWithNovalueBorder(flowImage);

            RenderedImage tcaImage = tcaGC.getRenderedImage();

            RandomIter flowIter = RandomIterFactory.create(flowRaster, null);
            RandomIter tcaIter = RandomIterFactory.create(tcaImage, null);

            RenderedImage pitImage = pitGC.getRenderedImage();
            RandomIter pitIter = RandomIterFactory.create(pitImage, null);

            int rows = activeRegion.getRows();
            int cols = activeRegion.getCols();
            double dx = activeRegion.getWEResolution();
            double dy = activeRegion.getNSResolution();
            WritableRaster hacklImage = hacklength3d(pitIter, flowIter, tcaIter, rows, cols, dx, dy);

            if (hacklImage == null) {
                err.println("Errors in execution...");
                return null;
            } else {
                CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                jgrValueSet = new JGrassGridCoverageValueSet(hacklImage, activeRegion, crs);

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
        String unitId = "raster";

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
                if (key.compareTo("doTile") == 0) { //$NON-NLS-1$
                    doTile = Boolean.parseBoolean(argument.getValue());
                }
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.hacklength3d";
        componentId = null;

        /*
         * create the exchange items
         */
        // hackl3d output

        hackl3dDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // tca input

        tcaDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // pit input

        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        // TODO Auto-generated method stub
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(tcaLink.getID())) {
            tcaLink = null;
        }
        if (linkID.equals(hackl3dLink.getID())) {
            hackl3dLink = null;
        }
    }

    /**
     * Calculates the hacklength3d in every pixel of the map
     * 
     * @return
     */
    private WritableRaster hacklength3d( RandomIter pitIter, RandomIter flowIter, RandomIter tcaIter, int rows, int cols,
            double dx, double dy ) {

        WritableRaster hacklengthImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, null);
        WritableRandomIter hacklRandomIter = RandomIterFactory.createWritable(hacklengthImage, null);

        int[] flow = new int[2];
        int[] flow_p = new int[2];

        double oldir = 0.0, maz = 0.0, dz = 0.0, count = 0.0;

        double[] grid = new double[11];

        // grid contains the dimension of pixels according with flow directions
        grid[0] = grid[9] = grid[10] = 0;
        grid[1] = grid[5] = Math.abs(dx);
        grid[3] = grid[7] = Math.abs(dy);
        grid[2] = grid[4] = grid[6] = grid[8] = Math.sqrt(dx * dx + dy * dy);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.hacklength3d...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                // looks for the source
                if (FluidUtils.sourcesqJAI(flowIter, flow)) {
                    count = 0;
                    maz = 1;
                    hacklRandomIter.setSample(flow[0], flow[1], 0, count);
                    oldir = flowIter.getSampleDouble(flow[0], flow[1], 0);
                    flow_p[0] = flow[0];
                    flow_p[1] = flow[1];
                    // call go_downstream in FluidUtils
                    if (!FluidUtils.go_downstream(flow, flowIter.getSampleDouble(flow[0], flow[1], 0)))
                        return null;
                    while( (!isNovalue(flowIter.getSampleDouble(flow[0], flow[1], 0)) && flowIter.getSampleDouble(flow[0],
                            flow[1], 0) != 10.0)
                            && FluidUtils.tcaMax(flowIter, tcaIter, hacklRandomIter, flow, maz, count) ) {
                        dz = pitIter.getSampleDouble(flow_p[0], flow_p[1], 0) - pitIter.getSampleDouble(flow[0], flow[1], 0);
                        count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                        hacklRandomIter.setSample(flow[0], flow[1], 0, count);
                        maz = tcaIter.getSampleDouble(flow[0], flow[1], 0);
                        oldir = flowIter.getSampleDouble(flow[0], flow[1], 0);
                        flow_p[0] = flow[0];
                        flow_p[1] = flow[1];
                        // call go_downstream in FluidUtils
                        if (!FluidUtils.go_downstream(flow, flowIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                    }
                    if (flowIter.getSampleDouble(flow[0], flow[1], 0) == 10) {
                        if (FluidUtils.tcaMax(flowIter, tcaIter, hacklRandomIter, flow, maz, count)) {
                            dz = pitIter.getSampleDouble(flow_p[0], flow_p[1], 0) - pitIter.getSampleDouble(flow[0], flow[1], 0);
                            count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                            hacklRandomIter.setSample(flow[0], flow[1], 0, count);
                            if (hacklRandomIter.getSampleDouble(flow[0], flow[1], 0) < 0)
                                hacklRandomIter.setSample(flow[0], flow[1], 0, 0);
                        }
                    }
                }
                if (isNovalue(flowIter.getSampleDouble(i, j, 0))) {
                    hacklRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();
        return hacklengthImage;
    }
}
