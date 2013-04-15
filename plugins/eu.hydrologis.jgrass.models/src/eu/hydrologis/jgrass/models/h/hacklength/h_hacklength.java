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
package eu.hydrologis.jgrass.models.h.hacklength;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

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
 * The openmi compliant representation of the flow model. It calculates the Hack
 * quantities, namely, assigned a point in a basin, the projection on the plane
 * of the distance from the watershed measured along the net (until it exists)
 * and then, proceeding again from valley upriver, along the maximal slope
 * lines. For each net confluence, the direction of the tributary with maximal
 * contributing area is chosen. If the tributaries have the same area, one of
 * the two directions is chosen at random.
 * </p>
 * <dt><strong>Inputs: </strong></dt>
 * <ol>
 * <li>the map containing the drainage directions (-flow);</li>
 * <li>the map containing the contributing areas (-tca);</li>
 * </ol>
 * <dt><strong>Returns:<br>
 * </strong></dt> <dd>
 * <ol>
 * <li>the map of the Hack distances (-hackl)</li>
 * </ol>
 * <p></dd>
 * <p>
 * Usage: h.hacklength --igrass-flow flow --igrass-tca tca --ograss-hackl hackl
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_hacklength extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String tcaID = "tca";

    public final static String hacklID = "hackl";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_hacklength.usage");

    private ILink flowLink = null;

    private ILink tcaLink = null;

    private ILink hacklLink = null;

    private IOutputExchangeItem hacklDataOutputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    private boolean doTile;

    public h_hacklength() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_hacklength( PrintStream output, PrintStream error ) {
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
        if (id.equals(hacklID)) {
            hacklLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: flow, tca
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return tcaDataInputEI;
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
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: hacklength
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return hacklDataOutputEI;
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
        if (linkID.equals(hacklLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D tcaGC = ModelsConstants.getGridCoverage2DFromLink(tcaLink, time, err);
          
            RenderedImage flowImage = flowGC.getRenderedImage();
            WritableRaster flowRaster = FluidUtils.createFromRenderedImageWithNovalueBorder(flowImage);

            RenderedImage tcaImage = tcaGC.getRenderedImage();

            RandomIter flowIter = RandomIterFactory.create(flowRaster, null);
            RandomIter tcaIter = RandomIterFactory.create(tcaImage, null);

            int rows = activeRegion.getRows();
            int cols = activeRegion.getCols();
            double dx = activeRegion.getWEResolution();
            double dy = activeRegion.getNSResolution();
            WritableRaster hacklImage = hacklength(flowIter, tcaIter, rows, cols, dx, dy);
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
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.hacklength";
        componentId = null;

        /*
         * create the exchange items
         */
        // hackl output
        IElementSet hacklElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity hacklQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        hacklDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, hacklQuantity, hacklElementSet);

        // element set defining what we want to read
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
        if (hacklID.equals(hacklLink.getID())) {
            hacklLink = null;
        }
    }

    /**
     * Calculates the hacklength in every pixel of the map
     * @param dy 
     * @param dx 
     * @param cols 
     * @param rows 
     * 
     * @return
     */
    private WritableRaster hacklength( RandomIter flowIter, RandomIter tcaIter, int rows, int cols, double dx, double dy ) {

        int[] flow = new int[2];
        double oldir;

        double[] grid = new double[11];
        double count = 0.0, maz;
        WritableRaster hacklengthImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null,
                JGrassConstants.doubleNovalue);

        WritableRandomIter hacklRandomIter = RandomIterFactory.createWritable(hacklengthImage, null);
        grid[0] = grid[9] = grid[10] = 0;
        grid[1] = grid[5] = abs(dx);
        grid[3] = grid[7] = abs(dy);
        grid[2] = grid[4] = grid[6] = grid[8] = sqrt(dx * dx + dy * dy);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.hacklength", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (isNovalue(flowIter.getSampleDouble(i, j, 0))) {
                    hacklRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                } else {
                    flow[0] = i;
                    flow[1] = j;
                    if (FluidUtils.sourcesqJAI(flowIter, flow)) {
                        count = 0;
                        maz = 1;
                        hacklRandomIter.setSample(flow[0], flow[1], 0, count);
                        oldir = flowIter.getSampleDouble(flow[0], flow[1], 0);
                        if (!FluidUtils.go_downstream(flow, flowIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                        while( (!isNovalue(flowIter.getSampleDouble(flow[0], flow[1], 0)) && flowIter.getSampleDouble(flow[0],
                                flow[1], 0) != 10.0)
                                && FluidUtils.tcaMax(flowIter, tcaIter, hacklRandomIter, flow, maz, count) ) {
                            count += grid[(int) oldir];
                            hacklRandomIter.setSample(flow[0], flow[1], 0, count);
                            maz = tcaIter.getSampleDouble(flow[0], flow[1], 0);
                            oldir = flowIter.getSampleDouble(flow[0], flow[1], 0);
                            if (!FluidUtils.go_downstream(flow, flowIter.getSampleDouble(flow[0], flow[1], 0)))
                                return null;
                        }
                        if (flowIter.getSampleDouble(flow[0], flow[1], 0) == 10) {
                            if (FluidUtils.tcaMax(flowIter, tcaIter, hacklRandomIter, flow, maz, count)) {
                                count += grid[(int) oldir];
                                hacklRandomIter.setSample(flow[0], flow[1], 0, count);
                            }
                        }

                    }

                }
            }
            pm.worked(1);
        }
        pm.done();
        return hacklengthImage;
    }

}
