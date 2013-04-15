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
package eu.hydrologis.jgrass.models.h.extractnetwork;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;
import static eu.hydrologis.libs.messages.MessageHelper.WORKING_ON;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
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
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidFeatureUtils;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;
/**
 * <p>
 * The openmi compliant representation of the extractnetwork model. It extracts
 * the channel net from the drainage directions.
 * </p>
 * <p>
 * Usage: mode 0: h.extractnetwork --mode 0 --igrass-flow flow --igrass-tca tca
 * --threshold threshold --ograss-net net
 * </p>
 * <p>
 * Usage: mode 1: h.extractnetwork --mode 1 --igrass-flow flow --igrass-tca tca
 * --igrass-slope slope --threshold threshold --ograss-net net
 * </p>
 * <p>
 * Usage: mode 2: h.extractnetwork --mode 2 --igrass-flow flow --igrass-tca tca
 * --igrass-classi classi --threshold threshold --ograss-net net
 * </p>
 * <p>
 * It's also possible to create a ShapeFile containing the network:
 * </p>
 * <p>
 * Usage: mode 0: h.extractnetwork --mode 0 --igrass-flow flow --igrass-tca tca
 * --threshold threshold --ograss-net net --oshapefile-netshape "filePath"
 * </p>
 * <p>
 * Usage: mode 1: h.extractnetwork --mode 1 --igrass-flow flow --igrass-tca tca
 * --igrass-slope slope --threshold threshold --ograss-net net
 * --oshapefile-netshape "filePath"
 * </p>
 * <p>
 * Usage: mode 2: h.extractnetwork --mode 2 --igrass-flow flow --igrass-tca tca
 * --igrass-classi classi --threshold threshold --ograss-net net
 * --oshapefile-netshape "filePath"
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_extractnetwork extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */

    public final static String flowID = "flow"; //$NON-NLS-1$

    public final static String tcaID = "tca"; //$NON-NLS-1$

    public final static String slopeID = "slope"; //$NON-NLS-1$

    public final static String classID = "class"; //$NON-NLS-1$

    public final static String netID = "net"; //$NON-NLS-1$

    public final static String netShapeID = "netshape"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_extractnetwork.usage"); //$NON-NLS-1$

    private ILink flowLink = null;

    private ILink tcaLink = null;

    private ILink slopeLink = null;

    private ILink classLink = null;

    private ILink netLink = null;

    private ILink netShapeLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private IInputExchangeItem slopeDataInputEI = null;

    private IInputExchangeItem classDataInputEI = null;

    private IOutputExchangeItem netDataOutputEI = null;

    private IOutputExchangeItem netShapeDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private double threshold = 0;

    private int mode = 0;

    private JGrassGridCoverageValueSet jgrValueSet;

    private WritableRaster networkImage;

    private PlanarImage slopeImage;

    private PlanarImage classImage;

    private PlanarImage flowImage;

    private PlanarImage tcaImage;

    private String locationPath;

    public h_extractnetwork() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_extractnetwork( PrintStream output, PrintStream error ) {
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
        if (id.equals(slopeID)) {
            slopeLink = link;
        }
        if (id.equals(classID)) {
            classLink = link;
        }
        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(netShapeID)) {
            netShapeLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There are 4 IInputExchangeItem: flow, tca, slope and class
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return tcaDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return slopeDataInputEI;
        }
        if (inputExchangeItemIndex == 3) {
            return classDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 4;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: net
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return netDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return netShapeDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 2;
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (networkImage == null) {
            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D tcaGC = ModelsConstants.getGridCoverage2DFromLink(tcaLink, time, err);
            flowImage = (PlanarImage) flowGC.getRenderedImage();
            tcaImage = (PlanarImage) tcaGC.getRenderedImage();

            if (mode == 0) {
                networkImage = extractNetMode0(flowImage, tcaImage);
            } else if (mode == 1) {
                GridCoverage2D slopeGC = ModelsConstants.getGridCoverage2DFromLink(slopeLink, time, err);
                slopeImage = (PlanarImage) slopeGC.getRenderedImage();
                networkImage = extractNetMode1(flowImage, tcaImage, slopeImage);
            } else if (mode == 2) {
                GridCoverage2D classGC = ModelsConstants.getGridCoverage2DFromLink(classLink, time, err);
                classImage = (PlanarImage) classGC.getRenderedImage();
                networkImage = extractNetMode2(flowImage, tcaImage, classImage);
            }
        }

        if (linkID.equals(netLink.getID())) {
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            if (networkImage == null) {
                throw new ModelsIllegalargumentException("An error occurred while extracting the network.", this);
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(networkImage, activeRegion, crs);
                return jgrValueSet;
            }
        } else if (linkID.equals(netShapeLink.getID())) {
            List<Integer> nstream = new ArrayList<Integer>();
            // setting novalues...

            RandomIter flowIter = RandomIterFactory.create(flowImage, null);
            RandomIter netIter = RandomIterFactory.create(networkImage, null);

            WritableRaster netNumImage = FluidUtils.netNumbering(nstream, flowIter, netIter, activeRegion.getCols(), activeRegion.getRows(), out);
            FluidUtils.setJAInoValueBorderIT(netNumImage);
            // calls netnumbering in FluiUtils...
            if (netNumImage == null) {
                return null;
            }
            // calculates the shape...
            try {
                FeatureCollection fcollection = new FluidFeatureUtils().net2ShapeOnly(flowImage, netNumImage, nstream, activeRegion, out);
                return new JGrassFeatureValueSet(fcollection);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ModelsIllegalargumentException(e.getLocalizedMessage(), this);
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
                if (key.compareTo("threshold") == 0) { //$NON-NLS-1$
                    threshold = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("mode") == 0) { //$NON-NLS-1$
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

        componentDescr = "h.exractnetwork"; //$NON-NLS-1$
        componentId = null;

        /*
         * create the exchange items
         */
        // net output
        netDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // netshape output
        netShapeDataOutputEI = ModelsConstants.createFeatureCollectionOutputExchangeItem(this, null);

        // element set defining what we want to read
        // flow input
        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // tca input
        tcaDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        if (mode == 1) {
            // slope input
            slopeDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        } else if (mode == 2) {
            // class input
            classDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        }
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
        if (linkID.equals(slopeLink.getID())) {
            slopeLink = null;
        }
        if (linkID.equals(classLink.getID())) {
            classLink = null;
        }
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
    }

    /**
     * this method calculates the network using a threshold value on the
     * contributing areas or on magnitudo
     */
    private WritableRaster extractNetMode0( PlanarImage flowImage, PlanarImage tcaImage ) {

        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        // create new RasterData for the network matrix
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        RandomIter tcaRandomIter = RandomIterFactory.create(tcaImage, null);
        WritableRaster netImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null, null, doubleNovalue);

        // try the operation!!

        WritableRandomIter netRandomIter = RandomIterFactory.createWritable(netImage, null);

        int flw[] = new int[2];

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "h.extractnetwork...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {

                double tcaValue = tcaRandomIter.getSampleDouble(i, j, 0);
                if (!isNovalue(tcaValue) && !isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    if (tcaValue >= threshold) {
                        netRandomIter.setSample(i, j, 0, 2);
                        flw[0] = i;
                        flw[1] = j;
                        if (!FluidUtils.go_downstream(flw, flowRandomIter.getSampleDouble(flw[0], flw[1], 0)))
                            return null;
                        while( netRandomIter.getSampleDouble(flw[0], flw[1], 0) != 2 && flowRandomIter.getSampleDouble(flw[0], flw[1], 0) < 9
                                && !isNovalue(flowRandomIter.getSampleDouble(flw[0], flw[1], 0)) ) {
                            netRandomIter.setSample(flw[0], flw[1], 0, 2);
                            if (!FluidUtils.go_downstream(flw, flowRandomIter.getSampleDouble(flw[0], flw[1], 0)))
                                return null;
                        }

                    } else if (flowRandomIter.getSampleDouble(i, j, 0) == 10) {
                        netRandomIter.setSample(i, j, 0, 2);
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
     * this method calculates the network imposing a threshold value on the
     * product of two quantities, for example the contributing area and the
     * slope.
     */
    private WritableRaster extractNetMode1( PlanarImage flowImage, PlanarImage tcaImage, PlanarImage slopeImage ) {

        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        RandomIter tcaRandomIter = RandomIterFactory.create(tcaImage, null);
        RandomIter slopeRandomIter = RandomIterFactory.create(slopeImage, null);

        // create new RasterData for the network matrix
        WritableRaster netImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null, null, doubleNovalue);

        WritableRandomIter netRandomIter = RandomIterFactory.createWritable(netImage, null);

        int flw[] = new int[2];

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "h.extractnetwork...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (!isNovalue(tcaRandomIter.getSampleDouble(i, j, 0)) && !isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    if (tcaRandomIter.getSampleDouble(i, j, 0) * slopeRandomIter.getSampleDouble(i, j, 0) >= threshold) {
                        netRandomIter.setSample(i, j, 0, 2);
                        flw[0] = i;
                        flw[1] = j;
                        if (!FluidUtils.go_downstream(flw, flowRandomIter.getSampleDouble(flw[0], flw[1], 0)))
                            return null;
                        while( netRandomIter.getSampleDouble(flw[0], flw[1], 0) != 2 && flowRandomIter.getSampleDouble(flw[0], flw[1], 0) < 9
                                && flowRandomIter.getSampleDouble(flw[0], flw[1], 0) != JGrassConstants.doubleNovalue ) {
                            netRandomIter.setSample(flw[0], flw[1], 0, 2);
                            if (!FluidUtils.go_downstream(flw, flowRandomIter.getSampleDouble(flw[0], flw[1], 0)))
                                return null;
                        }
                    } else if (flowRandomIter.getSampleDouble(i, j, 0) == 10) {
                        netRandomIter.setSample(i, j, 0, 2);
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
     * this method the network is extracted by considering only concave points
     * as being part of the channel network.
     */
    private WritableRaster extractNetMode2( PlanarImage flowImage, PlanarImage tcaImage, PlanarImage classImage ) {

        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        RandomIter tcaRandomIter = RandomIterFactory.create(tcaImage, null);
        RandomIter classRandomIter = RandomIterFactory.create(classImage, null);
        // create new RasterData for the network matrix
        // create new RasterData for the network matrix
        WritableRaster netImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null, null, doubleNovalue);

        // try the operation!!

        WritableRandomIter netRandomIter = RandomIterFactory.createWritable(netImage, null);

        int flw[] = new int[2];

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "h.extractnetwork...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (!isNovalue(tcaRandomIter.getSampleDouble(i, j, 0)) && !isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    if (tcaRandomIter.getSampleDouble(i, j, 0) >= threshold && classRandomIter.getSample(i, j, 0) == 15.0) {
                        netRandomIter.setSample(i, j, 0, 2);
                        flw[0] = i;
                        flw[1] = j;
                        if (!FluidUtils.go_downstream(flw, flowRandomIter.getSampleDouble(flw[0], flw[1], 0)))
                            return null;
                        while( netRandomIter.getSampleDouble(flw[0], flw[1], 0) != 2 && flowRandomIter.getSampleDouble(flw[0], flw[1], 0) < 9
                                && !isNovalue(flowRandomIter.getSampleDouble(flw[0], flw[1], 0)) ) {
                            netRandomIter.setSample(flw[0], flw[1], 0, 2);
                            if (!FluidUtils.go_downstream(flw, flowRandomIter.getSampleDouble(flw[0], flw[1], 0)))
                                return null;
                        }
                    } else if (flowRandomIter.getSampleDouble(i, j, 0) == 10) {
                        netRandomIter.setSample(i, j, 0, 2);
                    }
                } else {
                    netRandomIter.setSample(i, j, 0, doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();
        return netImage;
    }
}
