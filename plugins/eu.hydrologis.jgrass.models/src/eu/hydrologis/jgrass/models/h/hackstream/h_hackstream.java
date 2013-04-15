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
package eu.hydrologis.jgrass.models.h.hackstream;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
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
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the hackstream model. HackStream
 * arranges a channel net starting from the identification of the branch
 * according to Hack. The main stream is of order 1 and its tributaries of order
 * 2 and so on, the sub-tributaries are of order 3 and so on.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map containing the drainage directions (-flow)</LI>
 * <LI>the map containing the contributing areas (-tca)</LI>
 * <LI>the map containing the network (-net)</LI>
 * <LI>the map containing the Hack lengths (-hack)</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map of the order according the Hack lengths (-hacks)</LI>
 * </OL>
 * <P></DD> Usage: mode 0: h.hackstream --mode 0 --igrass-flow flow --igrass-tca
 * tca --igrass-hackl hackl --igrass-net net --ograss-hacks hacks
 * </p>
 * <p>
 * Usage: mode 1: h.hackstream --mode 1 --igrass-flow flow --igrass-num num
 * --ograss-hacks hacks
 * </p>
 * <p>
 * Note: Such order correponds in some ways to the Horton numeration. It is
 * necessary that the output pixels present a drainage direction value equal to
 * 10. If there is not such identification of the mouth points, the program does
 * not function correctly.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Rigon
 *         Riccardo
 */
public class h_hackstream extends ModelsBackbone {
    public final static String flowID = "flow"; //$NON-NLS-1$

    public final static String tcaID = "tca"; //$NON-NLS-1$

    public final static String hacklID = "hackl"; //$NON-NLS-1$

    public final static String netID = "net"; //$NON-NLS-1$

    public final static String numID = "num"; //$NON-NLS-1$

    public final static String hacksID = "hacks"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_hackstream.usage"); //$NON-NLS-1$

    private ILink flowLink = null;

    private ILink tcaLink = null;

    private ILink hacklLink = null;

    private ILink netLink = null;

    private ILink numLink = null;

    private ILink hacksLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private IInputExchangeItem hacklDataInputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IInputExchangeItem numDataInputEI = null;

    private IOutputExchangeItem hacksDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private int mode = 0;

    private boolean doTile = false;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING; // Erica here had at the end
    public h_hackstream() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_hackstream( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

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
        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(numID)) {
            numLink = link;
        }
        if (id.equals(hacksID)) {
            hacksLink = link;
        }
    }

    public void finish() {

    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return tcaDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return hacklDataInputEI;
        }
        if (inputExchangeItemIndex == 3) {
            return netDataInputEI;
        }
        if (inputExchangeItemIndex == 4) {
            return numDataInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 5;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return hacksDataOutputEI;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

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

                if (key.compareTo("mode") == 0) {
                    try {
                        mode = Integer.parseInt(argument.getValue());
                    } catch (Exception e) {
                        err.println("Info: error in mode parsing, using mode = 0");
                    }
                }
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.hackstream";
        componentId = null;

        /*
         * create the exchange items
         */
        // hacks output
        hacksDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // flow input
        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        if (mode == 0) {
            // tca input
            tcaDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

            // hackl input
            hacklDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

            // net input
            netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        }
        if (mode == 1) {
            // num input
            numDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        }
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        int count = 0;

        if (linkID.equals(hacksLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();
            if (mode == 0) {
                GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);
                GridCoverage2D hacklGC = ModelsConstants.getGridCoverage2DFromLink(hacklLink, time, err);
                PlanarImage netImage = (PlanarImage) netGC.getRenderedImage();
                WritableRaster flowDiskImage = FluidUtils.createFromRenderedImage(flowImage);
                WritableRandomIter flowRandomIter = RandomIterFactory.createWritable(flowDiskImage, null);
                RandomIter netRandomIter = RandomIterFactory.create(netImage, null);
                for( int j = 0; j < rows; j++ ) {
                    for( int i = 0; i < cols; i++ ) {
                        if (netRandomIter.getSampleDouble(i, j, 0) != 2)
                            flowRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                        if (flowRandomIter.getSampleDouble(i, j, 0) == 10)
                            count++;
                    }
                }
                flowRandomIter.done();
                netRandomIter.done();
                if (count == 0) {
                    err.println("Please run the h.markoutlets command before.");
                    return null;
                }

                GridCoverage2D tcaGC = ModelsConstants.getGridCoverage2DFromLink(tcaLink, time, err);
                PlanarImage tcaImage = (PlanarImage) tcaGC.getRenderedImage();
                PlanarImage hacklImage = (PlanarImage) hacklGC.getRenderedImage();
                WritableRaster hacksImage = hackstream(flowDiskImage, tcaImage, hacklImage);

                if (hacksImage == null) {
                    err.println("Errors in execution...");
                    return null;
                } else {
                    CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                    jgrValueSet = new JGrassGridCoverageValueSet(hacksImage, activeRegion, crs);
                    return jgrValueSet;
                }
            } else if (mode == 1) {
                GridCoverage2D numGC = ModelsConstants.getGridCoverage2DFromLink(numLink, time, err);
                PlanarImage numImage = (PlanarImage) numGC.getRenderedImage();

                WritableRaster hacksImage = hackstreamNetFixed(flowImage, numImage, out);

                if (hacksImage == null) {
                    err.println("Errors in execution...");
                    return null;
                } else {
                    CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                    jgrValueSet = new JGrassGridCoverageValueSet(hacksImage, activeRegion, crs);
                    return jgrValueSet;
                }
            }
        }
        return null;
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
        if (linkID.equals(hacklLink.getID())) {
            hacklLink = null;
        }
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
        if (linkID.equals(numLink.getID())) {
            numLink = null;
        }
        if (linkID.equals(numLink.getID())) {
            hacksLink = null;
        }
    }

    /**
     * gives the channel numeration of the hydrographic network according to
     * Hack’s numeration.
     * 
     * @param m
     *            is the flow data
     * @param tca
     * @param hackl
     * @param hacks
     * @return
     */
    public WritableRaster hackstream( WritableRaster flowDiskImage, RenderedImage tcaImage, RenderedImage hacklImage ) {
        int contr = 0;
        int count = 0, kk = 0;

        int rows = flowDiskImage.getHeight();
        int cols = flowDiskImage.getWidth();

        int[] flow = new int[2], param = new int[2];
        int[] flow_p = new int[2];
        int[] punto = new int[2];

        // create new matrix
        WritableRaster segnaImage = FluidUtils.createFromRaster(flowDiskImage);

        RandomIter flowRandomIter = RandomIterFactory.create(flowDiskImage, null);
        WritableRandomIter segnaRandomIter = RandomIterFactory.createWritable(segnaImage, null);
        RandomIter tcaRandomIter = RandomIterFactory.create(tcaImage, null);
        RandomIter hacklRandomIter = RandomIterFactory.create(hacklImage, null);

        WritableRaster hacksImage = FluidUtils.createDoubleWritableRaster(flowDiskImage.getWidth(), flowDiskImage.getHeight(),
                null, flowDiskImage.getSampleModel(), doubleNovalue);

        WritableRandomIter hacksRandomIter = RandomIterFactory.createWritable(hacksImage, null);

        int iterations = 1;
        do {
            // verify if there is an output point in the segna matrix, this
            // matrix is a copy of the
            // flow matrix, but in the while cycle modify it, and add the point
            // with value equal to
            // 10 (fork) and delete the point already calculated.
            PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
            pm.beginTask(MessageHelper.PROCESSING + "iteration " + iterations++, rows);
            for( int j = 0; j < rows; j++ ) {
                for( int i = 0; i < cols; i++ ) {
                    contr = 0;
                    if (segnaRandomIter.getSampleDouble(i, j, 0) == 10) {
                        flow[0] = i;
                        flow[1] = j;
                        contr = 1;
                        // it s really an output point (the segna matrix can be
                        // modified into the
                        // loop).
                        if (flowRandomIter.getSampleDouble(i, j, 0) == 10) {
                            // the output value is setted as 1 in the hack
                            // matrix.
                            hacksRandomIter.setSample(i, j, 0, 1);
                        } else if (flowRandomIter.getSampleDouble(i, j, 0) != 10
                                || !isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                            // after the call to go_downstream the flow is the
                            // next pixel in the channel.
                            if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                return null;
                            // this if is true if there is a fork (segna==10 but
                            // m!=10) so add one to the hack number.
                            if (!isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                hacksRandomIter.setSample(i, j, 0, hacksRandomIter.getSampleDouble(flow[0], flow[1], 0) + 1);
                        }
                        // memorize where the cycle was
                        punto[0] = i;
                        punto[1] = j;
                        break;
                    }
                }
                pm.worked(1);
                if (contr == 1)
                    break;
            }
            pm.done();
            flow[0] = punto[0];
            flow[1] = punto[1];
            if (contr == 1) {
                flow_p[0] = flow[0];
                flow_p[1] = flow[1];
                kk = 0;
                // the flow point is changed in order to follow the drainage
                // direction.
                FluidUtils.go_upstream_a(flow, flowRandomIter, tcaRandomIter, hacklRandomIter, param);
                // the direction
                kk = param[0];
                // number of pixel which drainage into this pixel, N.B. in a
                // channel only one pixel
                // drain into the next (?), otherwise there is a fork
                count = param[1];

                double tmp = 0;
                if (count > 0) {
                    tmp = hacksRandomIter.getSampleDouble(punto[0], punto[1], 0);
                    hacksRandomIter.setSample(flow[0], flow[1], 0, tmp);
                }
                if (count > 1) {
                    for( int k = 1; k <= 8; k++ ) {
                        if (flowRandomIter.getSampleDouble(punto[0] + dir[k][0], punto[1] + dir[k][1], 0) == dir[k][2] && k != kk) {
                            segnaRandomIter.setSample(punto[0] + dir[k][0], punto[1] + dir[k][1], 0, 10);
                        }
                    }
                }
                while( count > 0 ) {
                    /* segna altro pixel */
                    flow_p[0] = flow[0];
                    flow_p[1] = flow[1];
                    kk = 0;
                    FluidUtils.go_upstream_a(flow, flowRandomIter, tcaRandomIter, hacklRandomIter, param);
                    kk = param[0];
                    count = param[1];
                    double temp = hacksRandomIter.getSampleDouble(punto[0], punto[1], 0);
                    hacksRandomIter.setSample(flow[0], flow[1], 0, temp);
                    if (count > 1) {
                        // attribuisco ai nodi che incontro direzione di
                        // drenaggio 10
                        for( int k = 1; k <= 8; k++ ) {
                            if (flowRandomIter.getSample(flow_p[0] + dir[k][0], flow_p[1] + dir[k][1], 0) == dir[k][2] && k != kk) {
                                segnaRandomIter.setSample(flow_p[0] + dir[k][0], flow_p[1] + dir[k][1], 0, 10);
                            }
                        }
                    }
                }
                segnaRandomIter.setSample(punto[0], punto[1], 0, 5);
            }
        } while( contr == 1 );
        segnaRandomIter.done();
        hacklRandomIter.done();
        hacksRandomIter.done();
        flowRandomIter.done();

        segnaImage = null;
        return hacksImage;
    }

    /**
     * Gives the channel enumeration of the hydrographic network according to
     * Hack’s enumeration using a fixed network.
     * 
     * @param flowData
     * @param netnum
     * @param hacks
     * @return
     */
    public WritableRaster hackstreamNetFixed( RenderedImage flowImage, RenderedImage netNumImage, PrintStream out ) {
        int contr = 0;
        int count = 0, kk = 0;

        int[] flow = new int[2], param = new int[2];
        int[] flow_p = new int[2];
        int[] punto = new int[2];
        // {{0, 0, 0}, {0, 1, 5}, {-1, 1, 6}, {-1, 0, 7}, {-1, -1, 8}, {0, -1,1},
        // {1, -1, 2}, {1, 0, 3}, {1, 1, 4}, {0, 0, 0}, {0, 0, 0}};

        // create new matrix, just a copy of the drainage directions input
        // matrix

        WritableRaster segnaImage = FluidUtils.createFromRenderedImage(flowImage);
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        WritableRandomIter segnaRandomIter = RandomIterFactory.createWritable(segnaImage, null);
        RandomIter netNumberinglRandomIter = RandomIterFactory.create(netNumImage, null);
        WritableRaster hacksImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null,
                flowImage.getSampleModel(), null);
        WritableRandomIter hacksRandomIter = RandomIterFactory.createWritable(hacksImage, null);

        int rows = flowImage.getHeight();
        int cols = flowImage.getWidth();

        int iterations = 1;
        do {
            PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
            pm.beginTask(MessageHelper.PROCESSING + "iteration " + iterations++, rows);
            for( int j = 0; j < rows; j++ ) {
                for( int i = 0; i < cols; i++ ) {
                    contr = 0;
                    // check of the drainage directions in the new matrix
                    // lool for the outlet
                    if (segnaRandomIter.getSampleDouble(i, j, 0) == 10) {
                        // marked outlet with its line and column
                        flow[0] = i;
                        flow[1] = j;
                        contr = 1;
                        if (flowRandomIter.getSampleDouble(i, j, 0) == 10) {
                            // set the hack order to 1 corresponding to the
                            // marked outlet
                            hacksRandomIter.setSample(i, j, 0, 1);
                            // why these checks if segnaRandomIter is a copy of
                            // flowRandomIter?
                        } else if (flowRandomIter.getSampleDouble(i, j, 0) != 10
                                && !isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                            if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                return null;
                            double tmp = 0;
                            if (!isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                tmp = hacksRandomIter.getSampleDouble(flow[0], flow[1], 0) + 1;
                            hacksRandomIter.setSample(i, j, 0, tmp);
                        }
                        // set punto as flow with row and column number of the
                        // outlet
                        punto[0] = i;
                        punto[1] = j;
                        /*
                         * if (copt != null && copt.isInterrupted()) return
                         * false;
                         */
                        break;
                    }
                }
                // why this check??
                pm.worked(1);
                if (contr == 1)
                    break;
            }
            pm.done();

            flow[0] = punto[0];
            flow[1] = punto[1];
            if (contr == 1) {
                flow_p[0] = flow[0];
                flow_p[1] = flow[1];
                kk = 0;
                FluidUtils.goUpStreamOnNetFixed(flow, flowRandomIter, netNumberinglRandomIter, param);
                kk = param[0];
                count = param[1];
                double tmp = 0;
                if (count > 0)
                    tmp = hacksRandomIter.getSampleDouble(punto[0], punto[1], 0);
                hacksRandomIter.setSample(flow[0], flow[1], 0, tmp);
                if (count > 1) {
                    for( int k = 1; k <= 8; k++ ) {
                        if (flowRandomIter.getSampleDouble(punto[0] + dir[k][0], punto[1] + dir[k][1], 0) == dir[k][2]) {
                            segnaRandomIter.setSample(punto[0] + dir[k][0], punto[1] + dir[k][1], 0, 10);
                        }
                    }
                }
                while( count > 0 && !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0)) ) {
                    /* segnaRandomIter altro pixel */
                    flow_p[0] = flow[0];
                    flow_p[1] = flow[1];
                    kk = 0;
                    FluidUtils.goUpStreamOnNetFixed(flow, flowRandomIter, netNumberinglRandomIter, param);
                    kk = param[0];
                    count = param[1];
                    tmp = hacksRandomIter.getSample(punto[0], punto[1], 0);
                    hacksRandomIter.setSample(flow[0], flow[1], 0, tmp);
                    if (count > 1) {
                        // attribuisco ai nodi che incontro direzione di
                        // drenaggio 10
                        for( int k = 1; k <= 8; k++ ) {
                            if (flowRandomIter.getSample(flow_p[0] + dir[k][0], flow_p[1] + dir[k][1], 0) == dir[k][2] && k != kk) {
                                segnaRandomIter.setSample(flow_p[0] + dir[k][0], flow_p[1] + dir[k][1], 0, 10);
                            }
                        }
                    }
                }
                segnaRandomIter.setSample(punto[0], punto[1], 0, 5);
            }
        } while( contr == 1 );

        segnaRandomIter.done();
        int channel;
        double hacksValue = 0;
        int channelLength = 0;
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating map...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (!isNovalue(netNumberinglRandomIter.getSample(i, j, 0)) && hacksRandomIter.getSampleDouble(i, j, 0) < 0) {
                    channelLength = 1;
                    channel = (int) netNumberinglRandomIter.getSampleDouble(i, j, 0);
                    for( int l = 0; l < rows; l++ ) {
                        for( int n = 0; n < cols; n++ ) {
                            if (netNumberinglRandomIter.getSampleDouble(n, l, 0) == channel) {
                                flow[0] = n;
                                flow[1] = l;
                                if (FluidUtils.sourcesNet(flowRandomIter, flow, channel, netNumberinglRandomIter)) {
                                    punto[0] = flow[0];
                                    punto[1] = flow[1];
                                    FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0));
                                    while( netNumberinglRandomIter.getSampleDouble(flow[0], flow[1], 0) == channel ) {
                                        flow_p[0] = flow[0];
                                        flow_p[1] = flow[1];
                                        FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0));
                                        channelLength++;
                                    }
                                    double tmp = 0.;
                                    tmp = hacksRandomIter.getSampleDouble(flow[0], flow[1], 0) + 1;
                                    hacksRandomIter.setSample(punto[0], punto[1], 0, tmp);
                                    hacksValue = hacksRandomIter.getSampleDouble(punto[0], punto[1], 0);
                                    flow_p[0] = punto[0];
                                    flow_p[1] = punto[1];
                                    int length = 1;
                                    while( netNumberinglRandomIter.getSampleDouble(flow_p[0], flow_p[1], 0) == channel
                                            && length <= channelLength ) {
                                        flow_p[0] = punto[0];
                                        flow_p[1] = punto[1];
                                        hacksRandomIter.setSample(punto[0], punto[1], 0, hacksValue);
                                        FluidUtils.go_downstream(punto, flowRandomIter.getSampleDouble(punto[0], punto[1], 0));
                                        length++;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        hacksRandomIter.done();
        netNumberinglRandomIter.done();
        flowRandomIter.done();
        return hacksImage;
    }
}
