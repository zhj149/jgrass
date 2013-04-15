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
package eu.hydrologis.jgrass.models.h.rescaleddistance;

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
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;
/**
 * <p>
 * The openmi compliant representation of the rescaleddistance model. It
 * calculates the rescaled distance of each pixel from the outlet. *
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
 * the ratio between the speed in the channel state, and the speed in the
 * hillslopes, and x_h the distance along the hillslopes.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the drainage directions (-flow);</LI>
 * <LI>the file containing the net (-net);
 * <LI>the channel-overland ratio (-number);</LI>
 * </LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the rescaled distances (-rdist);</LI>
 * </OL>
 * <P></DD>
 * Usage: h.rescaleddistance --igrass-net net --igrass-flow flow --ograss-rdist
 * rdist --number number
 * </p>
 * <p>
 * Note: The program requests also the ratio between speed in the channel and
 * speed in hillslopes. The speed in channels is always greater than that in
 * hillslopes.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_rescaleddistance extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String netID = "net";

    public final static String flowID = "flow";

    public final static String rdistID = "rdist";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_rescaleddistance.usage"); //$NON-NLS-1$

    private ILink netLink = null;

    private ILink flowLink = null;

    private ILink rdistLink = null;

    private IOutputExchangeItem rdistDataOutputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private double number = 0;

    private boolean doTile;

    private String locationPath;

    private JGrassGridCoverageValueSet jgrValueSet;

    /** */
    public h_rescaleddistance() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_rescaleddistance

    /** */
    public h_rescaleddistance( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_rescaleddistance

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
        if (id.equals(rdistID)) {
            rdistLink = link;
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
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: rdist
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return rdistDataOutputEI;
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
        if (linkID.equals(rdistLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);

            PlanarImage netImage = (PlanarImage) netGC.getRenderedImage();
            PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();

            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            WritableRaster rDistImage = rdist(netImage, flowImage);
            // the model
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
                if (key.compareTo("number") == 0) {
                    number = Double.parseDouble(argument.getValue());
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

        componentDescr = "h.rescaleddistance";
        componentId = null;

        /*
         * create the exchange items
         */
        // rdist output

        rdistDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // net input

        netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
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
        if (linkID.equals(rdistLink.getID())) {
            rdistLink = null;
        }
    }

    /**
     * Calculates the rescaleddistance in every pixel of the map
     */
    private WritableRaster rdist( PlanarImage netImage, PlanarImage flowImage ) {
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        // get resolution of the active region
        double disX = activeRegion.getWEResolution();
        double disY = activeRegion.getNSResolution();
        RandomIter netRandomIter = RandomIterFactory.create(netImage, null);
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        WritableRaster rDistImage = FluidUtils.createDoubleWritableRaster(netImage.getWidth(), netImage.getHeight(), null,
                netImage.getSampleModel(), null);
        WritableRandomIter rDistRandomIter = RandomIterFactory.createWritable(rDistImage, null);

        // create new matrix

        int[] flow = new int[2];
        double count = 0.0, a = 0.0;

        // grid contains the dimension of pixels according with flow directions
        double[] grid = new double[11];

        grid[0] = grid[9] = grid[10] = 0.0;
        grid[1] = grid[5] = Math.abs(disX);
        grid[3] = grid[7] = Math.abs(disY);
        grid[2] = grid[4] = grid[6] = grid[8] = Math.sqrt(disX * disX + disY * disY);

        // FluidUtils.setNovalueBorder(flowData);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating rescaled distances...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                count = 0.0;
                flow[0] = i;
                flow[1] = j;
                if (!isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    while( netRandomIter.getSampleDouble(flow[0], flow[1], 0) != 2.0
                            && flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                            && !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0)) ) {
                        a = flowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        count += grid[(int) a] * number;
                        if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                    }
                    while( flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                            && !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0)) ) {
                        a = flowRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        count = count + grid[(int) a];
                        if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                    }
                    rDistRandomIter.setSample(i, j, 0, count);

                } else if (isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    rDistRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();
        return rDistImage;
    }
}
