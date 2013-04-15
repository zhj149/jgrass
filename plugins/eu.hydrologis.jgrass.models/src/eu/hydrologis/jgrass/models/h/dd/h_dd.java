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
package eu.hydrologis.jgrass.models.h.dd;

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
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the dd model. It estimates the
 * drainage density function for the basin upstream of each pixel. Drainage
 * density is defined as the total network length (i.e. the sum of all the
 * stream lengths) divided by the total length of the up-slope catchment area:
 * Z/A. It has the the dimension of the inverse of a length and such a length
 * was shown by Horton to be an estimator of the average hillslope length.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the file of the drainage directions (-flow);</LI>
 * <LI>the file of the contributing areas (-tca);</LI>
 * <LI>the file containing the network (-net);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the file containing the drainage dinsity</LI>
 * </OL>
 * <P></DD> Usage h.dd --igrass-flow flow --igrass-tca tca --igrass-net net
 * --ograss-dd dd
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Cozzini Andrea, Rigon
 *         Riccardo
 */
public class h_dd extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String tcaID = "tca";

    public final static String netID = "net";

    public final static String ddID = "dd";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_dd.usage"); //$NON-NLS-1$

    private static final double NaN = JGrassConstants.doubleNovalue;

    private ILink flowLink = null;

    private ILink tcaLink = null;

    private ILink netLink = null;

    private ILink ddLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IOutputExchangeItem ddDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private RenderedImage flowImage;

    private RenderedImage tcaImage;

    private RenderedImage netImage;

    private boolean doTile;

    private JGrassGridCoverageValueSet jgrValueSet;

    private int mode = 0;

    private String locationPath;

    private CoordinateReferenceSystem crs;

    public h_dd() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_dd( PrintStream output, PrintStream error ) {
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
        if (id.equals(tcaID)) {
            tcaLink = link;
        }
        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(ddID)) {
            ddLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: flow, tca, net
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return tcaDataInputEI;
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
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: dd
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return ddDataOutputEI;
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
        if (linkID.equals(ddLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowData = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D netData = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);
            GridCoverage2D tcaData = ModelsConstants.getGridCoverage2DFromLink(tcaLink, time, err);

            flowImage = flowData.getRenderedImage();
            tcaImage = tcaData.getRenderedImage();
            netImage = netData.getRenderedImage();

            WritableRaster ddImage = dd(netImage, flowImage, tcaImage, activeRegion.getWEResolution(), activeRegion
                    .getNSResolution());

            if (ddImage == null) {
                err.println("Errors in execution...\n");
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(ddImage, activeRegion, crs);
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
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        componentDescr = "h.dd";
        componentId = null;

        /*
         * create the exchange items
         */
        // dd output
        IElementSet ddElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity ddQuantity = UtilitiesFacade
                .createScalarQuantity(ModelsConstants.GRASSRASTERMAP, ModelsConstants.UNITID_RASTER);
        ddDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, ddQuantity, ddElementSet);

        // flow input
        IElementSet flowElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity flowQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        flowDataInputEI = UtilitiesFacade.createInputExchangeItem(this, flowQuantity, flowElementSet);

        // tca input
        IElementSet tcaElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity tcaQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        tcaDataInputEI = UtilitiesFacade.createInputExchangeItem(this, tcaQuantity, tcaElementSet);

        // net input
        IElementSet netElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity netQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        netDataInputEI = UtilitiesFacade.createInputExchangeItem(this, netQuantity, netElementSet);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(tcaLink.getID())) {
            tcaLink = null;
        }
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
        if (ddID.equals(ddLink.getID())) {
            ddLink = null;
        }
    }

    /**
     * Calculates the dd in every pixel of the map
     * 
     * @return
     */
    private WritableRaster dd( RenderedImage netTmpImage, RenderedImage flowTmpImage, RenderedImage tcaTmpImage, double dx,
            double dy ) {
        // get rows and cols from the active region
        int cols = activeRegion.getCols();
        int rows = activeRegion.getRows();
        WritableRaster flowImage = FluidUtils.createFromRenderedImage(flowTmpImage);
        WritableRaster netImage = FluidUtils.createFromRenderedImage(netTmpImage);
        WritableRaster tcaImage = FluidUtils.createFromRenderedImage(tcaTmpImage);
        WritableRaster ddImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, null);

        double[] grid = new double[11];

        // grid contains the dimension of pixels according with flow directions
        grid[0] = grid[9] = grid[10] = 0;
        grid[1] = grid[5] = Math.abs(dx);
        grid[3] = grid[7] = Math.abs(dy);
        grid[2] = grid[4] = grid[6] = grid[8] = Math.sqrt(dx * dx + dy * dy);

        // first step calculates the total length of the up-slope catchment area
        WritableRandomIter flowRandomIter = RandomIterFactory.createWritable(flowImage, null);
        WritableRandomIter tcaRandomIter = RandomIterFactory.createWritable(tcaImage, null);
        WritableRandomIter netRandomIter = RandomIterFactory.createWritable(netImage, null);

        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (netRandomIter.getSampleDouble(i, j, 0) == 2) {
                    netRandomIter.setSample(i, j, 0, grid[(int) flowRandomIter.getSampleDouble(i, j, 0)]);
                } else {
                    netRandomIter.setSample(i, j, 0, NaN);
                    flowRandomIter.setSample(i, j, 0, NaN);
                    tcaRandomIter.setSample(i, j, 0, NaN);
                }
            }
        }

        netRandomIter.done();
        flowRandomIter.done();
        // create new matrix (total length of the up-slope catchment area)
        WritableRaster lenghtImage = FluidUtils.sum_downstream(flowRandomIter, netRandomIter, netImage.getWidth(), netImage
                .getHeight(), out);
        if (lenghtImage == null) {
            return null;
        }
        netImage = null;

        WritableRandomIter ddRandomIter = RandomIterFactory.createWritable(ddImage, null);
        RandomIter lengthRandomIter = RandomIterFactory.create(lenghtImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.dd...", 2 * rows);
        // second step calculates the drainage density (z/A)
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (!isNovalue(tcaRandomIter.getSampleDouble(i, j, 0)))
                    ddRandomIter.setSample(i, j, 0, lengthRandomIter.getSampleDouble(i, j, 0)
                            / (tcaRandomIter.getSampleDouble(i, j, 0) * Math.pow(dx, 2)));
                else
                    ddRandomIter.setSample(i, j, 0, NaN);
            }
            pm.worked(1);
        }
        pm.done();

        ddRandomIter.done();
        lengthRandomIter.done();
        tcaRandomIter.done();
        return ddImage;
    }
}
