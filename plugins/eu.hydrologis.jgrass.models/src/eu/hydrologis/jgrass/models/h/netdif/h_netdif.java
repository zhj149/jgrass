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
package eu.hydrologis.jgrass.models.h.netdif;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;
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
 * The openmi compliant representation of the netdif model. It calculates the
 * difference between the value of a quantity in one point and the value of the
 * same quantity in another point across a basin. The points in which
 * calculating the difference are individuated by an opportune matrix. Typically
 * this matrix could contain the values of the Strahler numbers of a net, i.e.
 * the network pixels are labeled by the stream number and the same stream
 * contains a group of subsequent pixel. The points chosen for the calculation
 * of the difference are the first and the last of any stream, i.e. those in
 * which the numeration changes. If the matrix of the quantity to calculate is
 * that of elevations, then, again in the case shown, netdiff calculates the
 * elevation difference along a Strahler branch. If instead of the file
 * containing the Strahler numeration the matrix of the magnitude is used, the
 * variation of a quantity in a link is measured.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map containing the drainage directions (-flow);</LI>
 * <LI>the file containing thand date on which estimating the difference (in the
 * example above the matrix of the Strahler numeration) (-stream);
 * <LI>the file containing the quantity of which calculating the difference (in
 * the example above the matrix containing the elevations) (-mapdiff);
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the file containing the differences (-diff).</LI>
 * </OL>
 * <P></DD>
 * Usage h.netdif --igrass-flow flow --igrass-stream stream --igrass-mapdiff
 * mapdiff --ograss-diff diff
 * </p>
 * <p>
 * <DT><STRONG>Notes:</STRONG><BR>
 * </DT>
 * <DD><BR>
 * </OL></DD>
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Marco Pegoretti, Riccardo
 *         Rigon.
 */
public class h_netdif extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String streamID = "stream";

    public final static String mapdiffID = "mapdiff";

    public final static String diffID = "diff";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_netdif.usage"); //$NON-NLS-1$

    private ILink flowLink = null;

    private ILink streamLink = null;

    private ILink mapDiffLink = null;

    private ILink diffLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem streamDataInputEI = null;

    private IInputExchangeItem mapdiffDataInputEI = null;

    private IOutputExchangeItem diffDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile;

    private String locationPath;

    public h_netdif() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_netdif( PrintStream output, PrintStream error ) {
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
        if (id.equals(streamID)) {
            streamLink = link;
        }
        if (id.equals(mapdiffID)) {
            mapDiffLink = link;
        }
        if (id.equals(diffID)) {
            diffLink = link;
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
            return streamDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return mapdiffDataInputEI;
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
     * there is an IOutputExchangeItem: h2cd3d
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return diffDataOutputEI;
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
        if (linkID.equals(diffLink.getID())) {

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D streamGC = ModelsConstants.getGridCoverage2DFromLink(streamLink, time, err);
            GridCoverage2D mapDiffValueSet = ModelsConstants.getGridCoverage2DFromLink(mapDiffLink, time, err);
            PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();
            PlanarImage streamImage = (PlanarImage) streamGC.getRenderedImage();
            PlanarImage mapDiffImage = (PlanarImage) mapDiffValueSet.getRenderedImage();

            WritableRaster diffImage = netdif(flowImage, streamImage, mapDiffImage);
            if (diffImage == null) {
                err.println("Errors in execution...\n");
                return null;
            } else {
                CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                jgrValueSet = new JGrassGridCoverageValueSet(diffImage, activeRegion, crs);
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
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.netdif";
        componentId = null;

        /*
         * create the exchange items
         */
        // diff output
        IElementSet diffElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity diffQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        diffDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, diffQuantity, diffElementSet);

        // element set defining what we want to read
        // flow input
        IElementSet flowElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity flowQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        flowDataInputEI = UtilitiesFacade.createInputExchangeItem(this, flowQuantity, flowElementSet);

        // stream input
        IElementSet streamElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity streamQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        streamDataInputEI = UtilitiesFacade.createInputExchangeItem(this, streamQuantity, streamElementSet);

        // mapdiff input
        IElementSet mapdiffElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity mapdiffQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        mapdiffDataInputEI = UtilitiesFacade.createInputExchangeItem(this, mapdiffQuantity, mapdiffElementSet);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(streamLink.getID())) {
            streamLink = null;
        }
        if (linkID.equals(mapDiffLink.getID())) {
            mapDiffLink = null;
        }
        if (diffID.equals(diffLink.getID())) {
            diffLink = null;
        }
    }

    /**
     * Calculates the h2cd3d in every pixel of the map
     * 
     * @return
     */
    private WritableRaster netdif( PlanarImage flowImage, PlanarImage streamImage, PlanarImage netDiffImage ) {
        // get rows and cols from the active region
        int cols = activeRegion.getCols();
        int rows = activeRegion.getRows();

        int[] flow = new int[2];
        int[] oldflow = new int[2];

        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        RandomIter streamRandomIter = RandomIterFactory.create(streamImage, null);
        RandomIter mapDiffRandomIter = RandomIterFactory.create(netDiffImage, null);

        int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING;

        // create new matrix
        double[][] segna = new double[cols][rows];

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "h.netdif", 3 * rows);
        // First step: It marks with 1 the points which are at the upstream
        // beginning
        // of a link or stream
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                // looks for the source
                if (FluidUtils.sourcesqJAI(flowRandomIter, flow)) {
                    segna[i][j] = 1;
                } else if (!isNovalue(flowRandomIter.getSampleDouble(i, j, 0)) && flowRandomIter.getSampleDouble(i, j, 0) != 10.0) {
                    for( int k = 1; k <= 8; k++ ) {
                        if (flowRandomIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]) {
                            if (streamRandomIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == streamRandomIter
                                    .getSampleDouble(i, j, 0)) {
                                segna[i][j] = 0;
                                break;
                            } else {
                                segna[i][j] = 1;
                            }
                        }
                    }
                }
            }
            pm.worked(1);
        }
        WritableRaster diffImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null,
                flowImage.getSampleModel(), null);
        WritableRandomIter diffRandomIter = RandomIterFactory.createWritable(diffImage, null);
        // Second step: It calculate the difference among the first and the last
        // point of a link
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (segna[i][j] > 0) {
                    flow[0] = i;
                    flow[1] = j;
                    oldflow[0] = i;
                    oldflow[1] = j;
                    if (!isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0))) {
                        // call go_downstream in FluidUtils
                        FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0));
                        while( segna[flow[0]][flow[1]] < 1
                                && !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                && streamRandomIter.getSampleDouble(flow[0], flow[1], 0) == streamRandomIter.getSampleDouble(i,
                                        j, 0) ) {
                            oldflow[0] = flow[0];
                            oldflow[1] = flow[1];
                            if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                return null;
                        }
                        diffRandomIter.setSample(i, j, 0, Math.abs(mapDiffRandomIter.getSampleDouble(i, j, 0)
                                - mapDiffRandomIter.getSampleDouble(oldflow[0], oldflow[1], 0)));
                        // Assign to any point inside the link the value of the
                        // difference
                        flow[0] = i;
                        flow[1] = j;
                        if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                        while( !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                && streamRandomIter.getSampleDouble(flow[0], flow[1], 0) == streamRandomIter.getSampleDouble(i,
                                        j, 0) ) {
                            diffRandomIter.setSample(flow[0], flow[1], 0, diffRandomIter.getSampleDouble(i, j, 0));
                            if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                return null;
                        }
                        if (flowRandomIter.getSampleDouble(flow[0], flow[1], 0) == 10
                                && streamRandomIter.getSampleDouble(flow[0], flow[1], 0) == streamRandomIter.getSampleDouble(i,
                                        j, 0)) {
                            diffRandomIter.setSample(flow[0], flow[1], 0, diffRandomIter.getSampleDouble(i, j, 0));
                        }
                    }
                }
            }
            pm.worked(1);
        }
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (isNovalue(streamRandomIter.getSampleDouble(i, j, 0))) {
                    diffRandomIter.setSample(i, j, 0, doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();

        diffRandomIter.done();
        flowRandomIter.done();
        mapDiffRandomIter.done();
        streamRandomIter.done();
        return diffImage;
    }
}
