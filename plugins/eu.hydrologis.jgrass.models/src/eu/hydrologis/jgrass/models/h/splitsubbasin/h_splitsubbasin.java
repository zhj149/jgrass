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
package eu.hydrologis.jgrass.models.h.splitsubbasin;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.StringSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the Spiltsubbasin model. A tool for
 * labeling the subbasins of a basin. Given the Hack�s number of the channel
 * network, the subbasin up to a selected order are labeled. If Hack order 2 was
 * selected, the subbasins of Hack order 1 and 2 and the network of the same
 * order are extracted.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the matrix of the drainage directions (-flow);</LI>
 * <LI>the matrix of the order according the Hack lengths (-hacks);</LI>
 * <LI>the matrix containing the contributing areas (-tca);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the file containing the net with the streams numerated</LI>
 * <LI>the file containing the subbasin of 2 order</LI>
 * </OL>
 * <P></DD>
 * Usage h.splitsubbasin --hackorder hackorder --threshold value --igrass-flow
 * flow --igrass-hacks hacks --igrass-tca tca --ograss-netnumber netnumber
 * --ograss-subbasin subbasin
 * </p>
 * <p>
 * <DT><STRONG>Notes:</STRONG><BR>
 * </DT>
 * <DD>The user can choose the order at which to stop the splitting of the
 * subbasin (in fact the landscape dissection physically terminate at the
 * hillslope scale but this complete subdivision of the basin could be not
 * useful for the purposes of the user). <BR>
 * </OL></DD>
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Rigon Riccardo
 */
public class h_splitsubbasin extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String hacksID = "hacks";

    public final static String tcaID = "tca";

    public final static String netnumberID = "netnumber";

    public final static String subbasinID = "subbasin";

    public final static String colorNumID = "colornumbers";

    public final static String colorBasinsID = "colorbasins";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_splitsubbasin.usage");

    private ILink flowLink = null;

    private ILink hacksLink = null;

    private ILink tcaLink = null;

    private ILink netnumberLink = null;

    private ILink subbasinLink = null;

    private ILink colorNumLink = null;

    private ILink colorBasinsLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem hacksDataInputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private IOutputExchangeItem netnumberDataOutputEI = null;

    private IOutputExchangeItem subbasinDataOutputEI = null;

    private IOutputExchangeItem colorNumDataOutputEI = null;

    private IOutputExchangeItem colorBasinsDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private List<Integer> nstream;

    private String[] st = null;

    private double threshold = -1.0;

    private double hackorder = -1.0;

    private boolean doTile;

    private String locationPath;

    // Is the "in" flow direction
    private int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING;

    private JGrassGridCoverageValueSet jgrValueSet;

    private WritableRaster netNumberImage;

    private WritableRaster subbasinImage;

    private CoordinateReferenceSystem crs;

    public h_splitsubbasin() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_splitsubbasin( PrintStream output, PrintStream error ) {
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
        if (id.equals(hacksID)) {
            hacksLink = link;
        }
        if (id.equals(tcaID)) {
            tcaLink = link;
        }
        if (id.equals(netnumberID)) {
            netnumberLink = link;
        }
        if (id.equals(subbasinID)) {
            subbasinLink = link;
        }
        if (id.equals(colorNumID)) {
            colorNumLink = link;
        }
        if (id.equals(colorBasinsID)) {
            colorBasinsLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: flow, hacks, tca
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return hacksDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return tcaDataInputEI;
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
     * there is an IOutputExchangeItem: netnumber & subbasin
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return netnumberDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return subbasinDataOutputEI;
        }
        if (outputExchangeItemIndex == 2) {
            return colorNumDataOutputEI;
        }
        if (outputExchangeItemIndex == 3) {
            return colorBasinsDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 4;
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (threshold == -1) {
            out.print(getModelDescription());
            return null;
        }
        if (subbasinImage == null) {
            int rows = activeRegion.getRows();
            int cols = activeRegion.getCols();

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D hacksGC = ModelsConstants.getGridCoverage2DFromLink(hacksLink, time, err);
            GridCoverage2D tcaGC = ModelsConstants.getGridCoverage2DFromLink(tcaLink, time, err);

            RenderedImage flowImage = flowGC.getRenderedImage();
            WritableRaster flowRaster = FluidUtils.createFromRenderedImageWithNovalueBorder(flowImage);
            RenderedImage hacksImage = hacksGC.getRenderedImage();
            RenderedImage tcaImage = tcaGC.getRenderedImage();

            WritableRandomIter flowIter = RandomIterFactory.createWritable(flowRaster, null);
            RandomIter hacksIter = RandomIterFactory.create(hacksImage, null);
            RandomIter tcaIter = RandomIterFactory.create(tcaImage, null);

            WritableRaster netImage = net(hacksIter, tcaIter, hackorder, threshold, rows, cols);
            RandomIter netIter = RandomIterFactory.create(netImage, null);

            netNumberImage = netNumber(flowIter, hacksIter, tcaIter, netIter, rows, cols);
            if (netNumberImage == null) {
                out.print("Errors in execution...\n");
                return null;
            }
            WritableRandomIter netNumIter = RandomIterFactory.createWritable(netNumberImage, null);
            subbasinImage = FluidUtils.extractSubbasins(flowIter, netIter, netNumIter, rows, cols, out);
            if (subbasinImage == null) {
                out.print("Errors in execution...\n");
                return null;
            }

            assigneColorToStreams();
        }

        if (linkID.equals(netnumberLink.getID())) {
            FluidUtils.setJAInoValueBorderIT(netNumberImage);
            jgrValueSet = new JGrassGridCoverageValueSet(netNumberImage, activeRegion, crs);
            return jgrValueSet;
        } else if (linkID.equals(subbasinLink.getID())) {
            FluidUtils.setJAInoValueBorderIT(subbasinImage);
            jgrValueSet = new JGrassGridCoverageValueSet(subbasinImage, activeRegion, crs);
            return jgrValueSet;
        }

        if (linkID.equals(colorNumLink.getID())) {
            StringSet jgrValueStringSet = new StringSet(st);
            return jgrValueStringSet;
        }
        if (linkID.equals(colorBasinsLink.getID())) {
            StringSet jgrValueStringSet = new StringSet(st);
            return jgrValueStringSet;
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
                if (key.compareTo("threshold") == 0) {
                    threshold = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("hackorder") == 0) {
                    hackorder = Double.parseDouble(argument.getValue());
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
        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.splitsubbasin";
        componentId = null;

        /*
         * create the exchange items
         */
        // netnumber output
        netnumberDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // subbasin output

        subbasinDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // colorNum map output
        IElementSet colorNumMapElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity colorNumMapQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.COLORMAP, ModelsConstants.UNITID_COLORMAP);
        colorNumDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, colorNumMapQuantity, colorNumMapElementSet);

        // colorBasins map output
        IElementSet colorBasinsMapElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity colorBasinsMapQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.COLORMAP, ModelsConstants.UNITID_COLORMAP);
        colorBasinsDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, colorBasinsMapQuantity, colorBasinsMapElementSet);

        // element set defining what we want to read
        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // hacks input

        hacksDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // tca input

        tcaDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(hacksLink.getID())) {
            hacksLink = null;
        }
        if (linkID.equals(tcaLink.getID())) {
            tcaLink = null;
        }
        if (linkID.equals(netnumberLink.getID())) {
            netnumberLink = null;
        }
        if (linkID.equals(subbasinLink.getID())) {
            subbasinLink = null;
        }
        if (linkID.equals(colorNumLink.getID())) {
            colorNumLink = null;
        }
        if (linkID.equals(colorBasinsLink.getID())) {
            colorBasinsLink = null;
        }
    }

    /**
     * Return the map of the network with only the river of the choosen order.
     * 
     * @param flowImage
     *            the flow direction map.
     * @param hacksIter
     *            the hack stram map.
     * @param tcaIter
     *            the total contribouting area.
     * @param rows 
     * @param cols 
     * @return the map of the network woth the choosen order.
     */
    public WritableRaster net( RandomIter hacksIter, RandomIter tcaIter, double hackOrder, double thresholdValue, int rows, int cols ) {
        // calculates the max order of basin (max hackstream value)
        double hacksValue = 0.0;
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (!isNovalue(hacksIter.getSampleDouble(i, j, 0))) {
                    if (hacksIter.getSampleDouble(i, j, 0) > hacksValue) {
                        hacksValue = hacksIter.getSampleDouble(i, j, 0);
                    }
                }
            }
        }
        if (hackOrder > hacksValue) {
            err.println("Error on max hackstream");
            return null;
        }

        /*
         * Calculate the new network choosing the stream of n order and area
         * greater than the threshold.
         */
        // create the net map with the value choose.
        WritableRaster netImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, null);
        WritableRandomIter netRandomIter = RandomIterFactory.createWritable(netImage, null);
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "extraction of rivers of chosen order...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (!isNovalue(hacksIter.getSampleDouble(i, j, 0))) {
                    // calculates the network selecting the streams of 1, 2,..,n
                    // order
                    if (hacksIter.getSampleDouble(i, j, 0) <= hackOrder) {
                        if (tcaIter.getSampleDouble(i, j, 0) > thresholdValue) {
                            netRandomIter.setSample(i, j, 0, 2);
                        } else {
                            netRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                        }
                    } else if (hacksIter.getSampleDouble(i, j, 0) > hackOrder) {
                        netRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                    }
                } else {
                    netRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();

        return netImage;
    }

    /**
     * return the netNumebering map.
     * 
     * @param flowIter
     *            the flow direction map.
     * @param hacksIter
     *            the hack stream map.
     * @param tcaIter
     *            the map of total contributing area.
     * @param netIter
     *            the map of the network (only with the requested hack value).
     * @param rows 
     * @param cols 
     * @return
     */
    public WritableRaster netNumber( RandomIter flowIter, RandomIter hacksIter, RandomIter tcaIter, RandomIter netIter, int rows, int cols ) {
        int gg = 0, n = 0, f;
        int[] flow = new int[2];
        double area = 0.0;
        double[] tcavalue = new double[2];

        WritableRaster netNumberingImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, null);
        WritableRandomIter netNumberRandomIter = RandomIterFactory.createWritable(netNumberingImage, null);
        nstream = new ArrayList<Integer>();

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "netnumbering...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                if (netIter.getSampleDouble(i, j, 0) == 2 && flowIter.getSampleDouble(i, j, 0) != 10.0 && netNumberRandomIter.getSampleDouble(i, j, 0) == 0.0) {
                    f = 0;
                    // looks for the source
                    for( int k = 1; k <= 8; k++ ) {
                        /* test if neighbor drains in the cell */
                        if (flowIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2] && netIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == 2) {
                            break;
                        } else
                            f++;
                    }
                    // se f=8 nessun pixel appartenete alla rete drena nel pixel
                    // considerato quindi
                    // questo e' sorgente
                    if (f == 8) {
                        n++;
                        nstream.add(n);
                        netNumberRandomIter.setSample(i, j, 0, n);
                        tcavalue[0] = tcaIter.getSampleDouble(i, j, 0);
                        tcavalue[1] = 0.0;
                        if (!FluidUtils.go_downstream(flow, flowIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                        // while it is into the network.
                        while( !isNovalue(flowIter.getSampleDouble(flow[0], flow[1], 0)) && netNumberRandomIter.getSampleDouble(flow[0], flow[1], 0) == 0 ) {
                            gg = 0;
                            for( int k = 1; k <= 8; k++ ) {
                                // calculate how much pixel drining into the
                                // pixel.
                                if (netIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == 2 && flowIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]) {
                                    gg++;
                                }
                            }
                            if (gg >= 2) {
                                // the value of the upstream area of the node.
                                for( int k = 1; k <= 8; k++ ) {
                                    if (flowIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]
                                            && hacksIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == 1) {
                                        if (tcaIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) + tcavalue[1] - tcavalue[0] > threshold) {
                                            tcavalue[1] = 0;
                                            n++;
                                            nstream.add(n);
                                        }
                                        area = tcaIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) + tcavalue[1];
                                    }
                                }
                            }
                            /*
                             * if there is 2 pixel which are draining in the
                             * same node then increase the order of this pixels.
                             */
                            if (gg >= 2 && (area - tcavalue[0]) > threshold) {
                                // n++;
                                netNumberRandomIter.setSample(flow[0], flow[1], 0, n);
                                tcavalue[0] = tcaIter.getSampleDouble(flow[0], flow[1], 0);
                            }

                            /*
                             * If the pixel is a node and is inside a main
                             * channel (hacks ==1) and the tca which drain in
                             * the previous tract is less than th then I keep
                             * the previuos value.
                             */

                            else if (gg >= 2 && (area - tcavalue[0]) < threshold && hacksIter.getSampleDouble(flow[0], flow[1], 0) == 1) {
                                netNumberRandomIter.setSample(flow[0], flow[1], 0, n);
                                tcavalue[1] = area - tcavalue[0];
                                tcavalue[0] = tcaIter.getSampleDouble(flow[0], flow[1], 0);
                            }
                            // otherwise cointinuing with the previous number.
                            else {
                                netNumberRandomIter.setSample(flow[0], flow[1], 0, n);
                            }
                            if (!FluidUtils.go_downstream(flow, flowIter.getSampleDouble(flow[0], flow[1], 0)))
                                return null;
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();

        return netNumberingImage;
    }

    /**
     * Create a color map for a net where every stream has a different number or
     * for a map of basins where every basin has a different number.
     * 
     * @return
     */
    private void assigneColorToStreams() {
        Random rand = new Random();
        int colorindex;
        int[] colortriplet = null;
        st = new String[nstream.size() + 1];
        // Continually call nextInt() for more random integers ...
        st[0] = "%" + "1.0 " + nstream.size() + ".0";
        for( int i = 1; i <= nstream.size(); i++ ) {
            // Random integers that range from from 0 to n
            colorindex = rand.nextInt(JGrassUtilities.numberOfAvailableColors());
            colortriplet = JGrassUtilities.getColorTripletByIndex(colorindex);
            st[i] = (nstream.get(i - 1).doubleValue() + ":" + colortriplet[0] + ":" + colortriplet[1] + ":" + colortriplet[2] + " " + nstream.get(i - 1).doubleValue() + ":" + colortriplet[0] + ":"
                    + colortriplet[1] + ":" + colortriplet[2]);
        }
    }
}
