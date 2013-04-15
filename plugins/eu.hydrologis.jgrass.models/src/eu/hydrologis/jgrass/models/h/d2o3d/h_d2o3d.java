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
package eu.hydrologis.jgrass.models.h.d2o3d;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;
import static eu.hydrologis.libs.messages.MessageHelper.AN_ERROR_OCCURRED_DURING_MODEL_EXEC;
import static eu.hydrologis.libs.messages.MessageHelper.WORKING_ON;

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
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the d2o3d model. Distance to outlet 3D calculates the
 * distance of every pixel within the basin, considering also the vertical coordinate (differently
 * from D2O which calculates its projection only).
 * </p>
 * <dt><strong>Inputs: </strong></dt>
 * <ol>
 * <li>the map containing the elevations (-pit); </li>
 * <li>the map containing the drainage directions (-flow); </li>
 * </ol>
 * <dt><strong>Returns:<br>
 * </strong></dt>
 * <dd>
 * <ol>
 * <li>the map of distances (-d2o3d)</li>
 * </ol>
 * <p>
 * </dd>
 * <p>
 * Usage: h.d2o3d --igrass-pit pit --igrass-flow flow --ograss-d2o3d d2o3d
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Riccardo Rigon
 */
public class h_d2o3d extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit";

    public final static String flowID = "flow";

    public final static String d2o3dID = "d2o3d";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_d2o3d.usage"); //$NON-NLS-1$

    private ILink pitLink = null;

    private ILink flowLink = null;

    private ILink d2o3dLink = null;

    private IOutputExchangeItem d2o3dDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile;

    private WritableRandomIter distToOutRandomIter;

    private RandomIter filePitRandomIter;
    private RandomIter fileFlowRandomIter;

    private CoordinateReferenceSystem crs;
    public h_d2o3d() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_d2o3d( PrintStream output, PrintStream error ) {
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
        if (id.equals(d2o3dID)) {
            d2o3dLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: pit, flow
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return pitDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
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
     * there is an IOutputExchangeItem: d2o3dength
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return d2o3dDataOutputEI;
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
        if (linkID.equals(d2o3dLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowData = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D pitData = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);
            PlanarImage flowImage = FluidUtils.setJaiNovalueBorder((PlanarImage) flowData.getRenderedImage());
            PlanarImage pitImage = (PlanarImage) pitData.getRenderedImage();

            doTile = true;
            WritableRaster distToOutImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(),
                    null, null, null);
            distToOutRandomIter = RandomIterFactory.createWritable(distToOutImage, null);
            filePitRandomIter = RandomIterFactory.create(pitImage, null);
            fileFlowRandomIter = RandomIterFactory.create(flowImage, null);

            if (!d2o3d(flowImage)) {
                err.println(AN_ERROR_OCCURRED_DURING_MODEL_EXEC);
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(distToOutImage, activeRegion, crs);
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
            }
        }

        /*
         * define the map path
         */
        String locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        componentDescr = "h.d2o3d";
        componentId = null;

        /*
         * create the exchange items
         */
        // d2o3d output
        IElementSet d2o3dElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity d2o3dQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        d2o3dDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, d2o3dQuantity, d2o3dElementSet);
        // element set defining what we want to read
        // pit input
        IElementSet pitElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity pitQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        pitDataInputEI = UtilitiesFacade.createInputExchangeItem(this, pitQuantity, pitElementSet);

        // flow input
        IElementSet flowElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity flowQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        flowDataInputEI = UtilitiesFacade.createInputExchangeItem(this, flowQuantity, flowElementSet);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(d2o3dLink.getID())) {
            d2o3dLink = null;
        }
    }

    /**
     * Calculates the d2o3d in every pixel of the map
     * 
     * @return
     */
    private boolean d2o3d( PlanarImage m ) {
        RandomIter mIter = RandomIterFactory.create(m, null);

        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        int[] flow = new int[2];
        int[] flow_p = new int[2];

        // get rows and cols from the active region
        double dx = activeRegion.getWEResolution();
        double dy = activeRegion.getNSResolution();

        // create new matrix

        double oldir = 0.0, dz = 0.0, count = 0.0;

        double[] grid = new double[11];

        // grid contains the dimension of pixels according with flow directions
        grid[0] = grid[9] = grid[10] = 0;
        grid[1] = grid[5] = Math.abs(dx);
        grid[3] = grid[7] = Math.abs(dy);
        grid[2] = grid[4] = grid[6] = grid[8] = Math.sqrt(dx * dx + dy * dy);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "h.d2o3d...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                // looks for the source
                flow[0] = i;
                flow[1] = j;
                if (isNovalue(fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0))) {
                    distToOutRandomIter.setSample(flow[0], flow[1], 0, JGrassConstants.doubleNovalue);

                } else {

                    if (FluidUtils.sourcesqJAI(mIter, flow)) {
                        count = 0;
                        oldir = fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        flow_p[0] = flow[0];
                        flow_p[1] = flow[1];
                        if (!FluidUtils.go_downstream(flow, fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                            return false;
                        while( !isNovalue(fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                && distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) <= 0 ) {
                            dz = filePitRandomIter.getSampleDouble(flow_p[0], flow_p[1], 0)
                                    - filePitRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                            oldir = fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            flow_p[0] = flow[0];
                            flow_p[1] = flow[1];
                            if (!FluidUtils.go_downstream(flow, fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                return false;
                        }
                        if (distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) > 0) {
                            dz = filePitRandomIter.getSampleDouble(flow_p[0], flow_p[1], 0)
                                    - filePitRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2))
                                    + distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            distToOutRandomIter.setSample(i, j, 0, count);
                        } else if (fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0) > 9) {
                            distToOutRandomIter.setSample(flow[0], flow[1], 0, 0);
                            dz = filePitRandomIter.getSampleDouble(flow_p[0], flow_p[1], 0)
                                    - filePitRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            count += Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                            distToOutRandomIter.setSample(i, j, 0, count);
                        }

                        flow[0] = i;
                        flow[1] = j;
                        oldir = fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        flow_p[0] = flow[0];
                        flow_p[1] = flow[1];
                        if (!FluidUtils.go_downstream(flow, fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                            return false;
                        while( !isNovalue(fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                && distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) <= 0 ) {
                            dz = filePitRandomIter.getSampleDouble(flow_p[0], flow_p[1], 0)
                                    - filePitRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            count -= Math.sqrt(Math.pow(grid[(int) oldir], 2) + Math.pow(dz, 2));
                            distToOutRandomIter.setSample(flow[0], flow[1], 0, count);
                            if (distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) < 0)
                                distToOutRandomIter.setSample(flow[0], flow[1], 0, 0);
                            oldir = fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            flow_p[0] = flow[0];
                            flow_p[1] = flow[1];
                            if (!FluidUtils.go_downstream(flow, fileFlowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                return false;
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();

        return true;
    }
}
