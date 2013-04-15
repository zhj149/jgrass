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
package eu.hydrologis.jgrass.models.h.strahler;

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
 * The openmi compliant representation of the Strahler model. It makes it possible to calculate the
 * Strahler order in a basin in two possible ways:
 * <OL>
 * <LI>calculate the Strahler order in whole the basin <B>mode 0</B>; </LI>
 * <LI>calculate the Strahler order only on the network <B>mode 1</B>;
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the drainage directions (-flow); </LI>
 * <LI>the map of the network (-net); </LI>
 * </OL>
 * <P>
 * </DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the net with the branches numerated according to Strahler (-strahler); </LI>
 * </OL>
 * <P>
 * </DD>
 * <p>
 * Usage mode 0: h.strahler --mode 0 --igrass-flow flow --ograss-strahler strahler
 * </p>
 * <p>
 * Usage mode 1: h.strahler --mode 1 --igrass-flow flow --igrass-net net --ograss-strahler strahler
 * </p>
 * <p>
 * <DT><STRONG>Notes:</STRONG><BR>
 * </DT>
 * The Strahler order of a basin depends obviously on how the net has been delineated. If we use the
 * file of the drainage dircections without any filter, all points are considered as net. Instead we
 * can use for example the estract network output to obtain a realistic networks. <BR>
 * </P>
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Andrea Antonello, Riccardo Rigon
 */

public class h_strahler extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String netID = "net";

    public final static String strahlerID = "strahler";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_strahler.usage");

    private ILink flowLink = null;

    private ILink netLink = null;

    private ILink strahlerLink = null;

    private IOutputExchangeItem strahlerDataOutputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private int mode = 0;

    private String locationPath;

    private JGrassGridCoverageValueSet jgrValueSet;

    /** */
    public h_strahler() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_strahler

    /** */
    public h_strahler( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_strahler

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
                if (key.compareTo("mode") == 0) {
                    mode = Integer.parseInt(argument.getValue());
                }
            }

        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;

        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.strahler";
        componentId = null;

        /*
         * create the exchange items
         */
        // strahler output

        strahlerDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // net input

        netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(strahlerLink.getID())) {
            // reads input maps
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();
            PlanarImage netImage = null;
            if (mode == 1) {
                GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);
                netImage = (PlanarImage) netGC.getRenderedImage();
            }

            // the model
            WritableRaster strahlerImage = strahler(netImage, flowImage, mode, out);
            if (strahlerImage == null) {
                return null;
            } else {
                CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                jgrValueSet = new JGrassGridCoverageValueSet(strahlerImage, activeRegion, crs);
                return jgrValueSet;
            }
        }
        return null;
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(strahlerID)) {
            strahlerLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {
    }

    /**
     * There is an IInputExchangeItem: flow, net
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return netDataInputEI;
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
     * there is an IOutputExchangeItem: strahler
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return strahlerDataOutputEI;
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
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
        if (linkID.equals(strahlerLink.getID())) {
            strahlerLink = null;
        }
    }

    /**
     * returns the strahler map.
     * 
     * @param netImage the map of the net.
     * @param flowImage the map of flow direction.
     * @param cmode
     * @param outPrintStream
     * @return the map of strahler order.
     */
    private WritableRaster strahler( PlanarImage netImage, PlanarImage flowImage, int cmode, PrintStream outPrintStream ) {

        int cols = flowImage.getWidth();
        int rows = flowImage.getHeight();

        int[] flow = new int[2];

        // it memorize the number of pixel which are draining into the pixel examine.
        int contr = 0;
        int counter = 0, io, jo, s = 0;

        int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING;

        int[] vett_contr;
        double max;
        WritableRaster flow2 = FluidUtils.createFromRenderedImage(flowImage);
        WritableRandomIter flowRandomIter = RandomIterFactory.createWritable(flow2, null);
        if (cmode == 1) {
            // netImage= FluidUtils.createConstantImage(flowImage.getWidth(), flowImage.getHeight(),
            // 3);
            // set novalue border...
            // FluidUtils.setNovalueBorder(netRasterData);
            RandomIter netRandomIter = RandomIterFactory.create(netImage, null);
            for( int j = 0; j < rows; j++ ) {
                for( int i = 0; i < cols; i++ ) {
                    if (netRandomIter.getSampleDouble(i, j, 0) != 2) {
                        flowRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                    }
                }
            }
            netRandomIter.done();
        }

        /*
         * initialize the iterator for the map and create the output image
         */
        WritableRaster strahlerImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null,
                flow2.getSampleModel(), null);
        WritableRandomIter strahlerRandomIter = RandomIterFactory.createWritable(strahlerImage, null);
        // start to calculate the output map.

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.strahler...", 2 * rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                /* verify if the pixel is a source. If it is then set the value to 1.
                 * 
                 * */
                if (FluidUtils.sourcesqJAI(flowRandomIter, flow)) {
                    strahlerRandomIter.setSample(i, j, 0, 1);
                    /*
                     *It go downstream, following the flow direction 
                     * 
                     */
                    if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                        return null;
                    /*
                     * while it isn't an outlet point and flow and net have valid value, it loop and go downstream.
                     */
                    while( flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10
                            && !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0)) ) {
                        contr = 0;
                        /*
                         * Verify if the pixel have a number of pixel which are drained into greater than 1.
                         */
                        vett_contr = new int[10];
                        for( int k = 1; k <= 8; k++ ) {
                            if (flowRandomIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]) {
                                contr += 1;
                                vett_contr[contr] = k;
                            }
                        }
                        if (contr > 1)
                        /*
                         * If the numeber of pixel which are going in this pixel then verify which have a greater strahler number. 
                         */
                        {
                            max = 0;
                            for( int ii = 1; ii <= contr; ii++ ) {
                                s = vett_contr[ii];
                                io = flow[0] + dir[s][0];
                                jo = flow[1] + dir[s][1];
                                if (max < strahlerRandomIter.getSampleDouble(io, jo, 0))
                                    max = strahlerRandomIter.getSampleDouble(io, jo, 0);
                            }
                            counter = 0;
                            for( int ii = 1; ii <= contr; ii++ ) {
                                s = vett_contr[ii];
                                io = flow[0] + dir[s][0];
                                jo = flow[1] + dir[s][1];
                                if (max == strahlerRandomIter.getSampleDouble(io, jo, 0))
                                    counter += 1;
                            }
                            /*
                            *if counter is greater than 1 then the strahler order is equal to the previous plus 1, otherwise is equal to the previus.
                             */
                            if (counter > 1)
                                strahlerRandomIter.setSample(flow[0], flow[1], 0, max + 1);
                            if (counter == 1)
                                strahlerRandomIter.setSample(flow[0], flow[1], 0, max);
                        } else
                        /*
                         * If there is only one drained pixel then the order is equal to this pixel.
                         */
                        {
                            s = vett_contr[1];
                            io = flow[0] + dir[s][0];
                            jo = flow[1] + dir[s][1];
                            max = strahlerRandomIter.getSampleDouble(io, jo, 0);
                            strahlerRandomIter.setSample(flow[0], flow[1], 0, max);
                        }
                        max = strahlerRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                        /*
                         * Go to the next pixel, if the order of previuos pixel is lesser than the next value then break and keep the old value.
                         */
                        if (strahlerRandomIter.getSampleDouble(flow[0], flow[1], 0) > max)
                            break;
                    }
                }
            }
            pm.worked(1);
        }

        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                vett_contr = new int[10];
                contr = 0;
                /*
                 * calcolo l'ordine anche per il pixel di uscita: primo passo calcolo il numero dei
                 * pixel drenenti
                 */
                if (flowRandomIter.getSampleDouble(flow[0], flow[1], 0) == 10) {
                    for( int k = 1; k <= 8; k++ ) {
                        if (flowRandomIter.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]) {
                            contr += 1;
                            vett_contr[contr] = k;
                        }
                    }
                    /* calcolo il max valore di strahler */
                    max = 0;
                    for( int ii = 1; ii <= contr; ii++ ) {
                        s = vett_contr[ii];
                        io = flow[0] + dir[s][1];
                        jo = flow[1] + dir[s][0];
                        if (max < strahlerRandomIter.getSampleDouble(io, jo, 0))
                            max = strahlerRandomIter.getSampleDouble(io, jo, 0);
                    }
                    /*
                     * calcolo quanti pixel (conta) presentano massimo valore di strahler
                     */
                    counter = 0;
                    for( int ii = 1; ii <= contr; ii++ ) {
                        s = vett_contr[ii];
                        io = flow[0] + dir[s][0];
                        jo = flow[1] + dir[s][1];
                        if (max == strahlerRandomIter.getSampleDouble(io, jo, 0))
                            counter += 1;
                    }
                    /*
                     * se conta e' maggiore di 1 si aumenta il numero di strahler di 1
                     */
                    if (counter > 1)
                        strahlerRandomIter.setSample(flow[0], flow[1], 0, max + 1);
                    if (counter == 1)
                        strahlerRandomIter.setSample(flow[0], flow[1], 0, max);
                }
            }
            pm.worked(1);
        }
        pm.done();
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (strahlerRandomIter.getSampleDouble(i, j, 0) == 0.0)
                    strahlerRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
            }
        }

        strahlerRandomIter.done();
        flowRandomIter.done();
        return strahlerImage;
    }

    /**
     * The Strahler class to be able to use the algorithm safe from other modules
     */
    public class Strahler {
        public WritableRaster calculateStrahler( PlanarImage netImage, PlanarImage flowImage, int cmode,
                PrintStream outPrintStream ) {
            return strahler(netImage, flowImage, cmode, outPrintStream);
        }
    }
}
