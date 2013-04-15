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

package eu.hydrologis.jgrass.models.h.draindir;

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
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the draindir model. It calculates the
 * drainage directions minimizing the deviation from the real flow. The
 * deviation is calculated using a triangular construction and it could be given
 * in degrees (D8 LAD method) or as trasversal distance (D8 LTD method). The
 * deviation could be cumulated along the path using the &#955; parameter, and
 * when it assumes a limit value the flux is redirect to the real direction. In
 * certain cases, for example in the plains areas or where there are manmade
 * constructions, it can happen that the extracted channel network does not
 * coincide with the real channel network. The fixed network method allows you
 * to assign a known channel network and to then correct the drainage
 * directions.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the depitted map (-pit)</LI>
 * <LI>the old drainage direction map (-flow)</LI>
 * <LI>the &#955; parameter (a value in the range 0 - 1) (-lambda)</LI>
 * <LI>the method choosen: LAD (angular deviation) and LTD (trasversal
 * distance)(-mode)</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map with the new drainage directions (-dir)</LI>
 * <LI>the map with the total contributing areas calculated with this drainage
 * directions (-tca)</LI>
 * </OL>
 * <P></DD> Usage method LAD: h.draindir --mode 1 --igrass-pit pit --igrass-flow
 * flow --lambda lambda --ograss-dir dir --ograss-tca tca
 * </p>
 * <p>
 * Usage method LTD: h.draindir --mode 2 --igrass-pit pit --igrass-flow flow
 * --lambda lambda --ograss-dir dir --ograss-tca tca
 * </p>
 * <p>
 * Usage method FLOW FIXED: h.draindir --mode 1-2 --flowfixed 1 --igrass-pit pit
 * --igrass-flow flow --igrass-flowfixed flowfixed --lambda lambda --ograss-dir
 * dir
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_draindir extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit"; //$NON-NLS-1$

    public final static String flowID = "flow"; //$NON-NLS-1$

    public final static String flowFixedID = "flowfixed"; //$NON-NLS-1$

    public final static String dirID = "dir"; //$NON-NLS-1$

    public final static String tcaID = "tca"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_draindir.usage"); //$NON-NLS-1$

    private ILink pitLink = null;

    private ILink flowLink = null;

    private ILink flowFixedLink = null;

    private ILink dirLink = null;

    private ILink tcaLink = null;

    private IOutputExchangeItem dirDataOutputEI = null;

    private IOutputExchangeItem tcaDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem flowFixedDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private double lambda;

    private int mode, fixedMode = 0;

    private static final double PI = Math.PI;

    private static final double NaN = JGrassConstants.doubleNovalue;

    /*
     * it indicates the position of the triangle's vertexes
     */
    int[][] order = ModelsConstants.DIR;

    private String locationPath;

    private boolean doTile = false;

    private JGrassGridCoverageValueSet jgrValueSet;

    private WritableRaster[] result;

    public h_draindir() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_draindir( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(flowFixedID)) {
            flowFixedLink = link;
        }
        if (id.equals(dirID)) {
            dirLink = link;
        }
        if (id.equals(tcaID)) {
            tcaLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {
    }

    /**
     * There are three IInputExchangeItem: one for pit, one for flow and one for
     * flowfixed
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return pitDataInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return flowDataInputEI;
        } else if (inputExchangeItemIndex == 2) {
            return flowFixedDataInputEI;
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
     * there are two IOutputExchangeItem: one for dir and one for tca
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return dirDataOutputEI;
        } else if (outputExchangeItemIndex == 1) {
            return tcaDataOutputEI;
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
        if (linkID.equals(dirLink.getID()) || linkID.equals(tcaLink.getID())) {
            if (result == null) {
                GridCoverage2D pitGC = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);
                GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);

                PlanarImage pitImage = (PlanarImage) pitGC.getRenderedImage();
                PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();

                PlanarImage flowfixedImage = null;
                if (fixedMode == 1 && flowFixedLink == null) {
                    err.println("Error! fowfixed map not set\n"); //$NON-NLS-1$
                    return null;
                } else if (fixedMode == 1) {
                    // only if necessary reads this map (flow fixed on net)
                    GridCoverage2D flowfixedGC = ModelsConstants.getGridCoverage2DFromLink(flowFixedLink, time, err);
                    flowfixedImage = (PlanarImage) flowfixedGC.getRenderedImage();
                }
                result = dirdren(mode, fixedMode, pitImage, flowImage, flowfixedImage);
            }
            if (result == null) {
                return null;
            } else {
                CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                if (linkID.equals(dirLink.getID())) {
                    jgrValueSet = new JGrassGridCoverageValueSet(result[0], activeRegion, crs);
                    return jgrValueSet;
                } else {
                    jgrValueSet = new JGrassGridCoverageValueSet(result[1], activeRegion, crs);
                    return jgrValueSet;
                }
            }
        }
        return null;
    }

    /**
     * in this method map's properties are defined... location, mapset... and
     * than IInputExchangeItem and IOutputExchangeItem are created
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
                if (key.compareTo("lambda") == 0) { //$NON-NLS-1$
                    lambda = Double.parseDouble(argument.getValue());
                    if (lambda > 1 || lambda < 0) {
                        out.println(Messages.getString("h_draindir.errLambda"));
                        throw new Exception();
                    }
                }
                if (key.compareTo("mode") == 0) { //$NON-NLS-1$
                    mode = Integer.parseInt(argument.getValue());
                }
                if (key.compareTo("flowfixed") == 0) { //$NON-NLS-1$
                    fixedMode = Integer.parseInt(argument.getValue());
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

        componentDescr = "h.draindir"; //$NON-NLS-1$
        componentId = null;

        /*
         * create the exchange items
         */
        // dir output

        dirDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        // tca output
        tcaDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // pit input

        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        if (fixedMode == 1) {

            flowFixedDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        }
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
        if (linkID.equals(dirLink.getID())) {
            dirLink = null;
        }
        if (linkID.equals(tcaLink.getID())) {
            tcaLink = null;
        }
    }

    /**
     * Calculates new drainage directions
     * 
     * @throws Exception
     */
    private WritableRaster[] dirdren( int mode, int fixedMode, PlanarImage pitTmpImage, PlanarImage flowTmpImage,
            PlanarImage flowfixedImage ) throws Exception {
        double[] orderedelev, indexes;
        int nelev;

        // get rows and cols from the active region
        int minX = pitTmpImage.getMinX();
        int minY = pitTmpImage.getMinY();
        int maxX = pitTmpImage.getMaxX();
        int maxY = pitTmpImage.getMaxY();
        int activecols = pitTmpImage.getWidth();
        int activerows = pitTmpImage.getHeight();
        // setting novalues...
        PlanarImage flowImage = FluidUtils.setJaiNovalueBorder(flowTmpImage);
        PlanarImage pitImage = FluidUtils.setJaiNovalueBorder(pitTmpImage);
        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);

        // create new matrix
        orderedelev = new double[activecols * activerows];
        indexes = new double[activecols * activerows];

        nelev = 0;
        for( int j = minY; j < maxY; j++ ) {
            for( int i = minX; i < maxX; i++ ) {
                orderedelev[((j) * activecols) + i] = pitRandomIter.getSampleDouble(i, j, 0);
                indexes[((j) * activecols) + i] = ((j) * activecols) + i + 1;
                if (!isNovalue(pitRandomIter.getSampleDouble(i, j, 0))) {
                    nelev = nelev + 1;
                }
            }
        }

        FluidUtils.sort2DoubleVectors(orderedelev, indexes, null);

        // free the memory
        orderedelev = null;

        out.println(Messages.getString("initializematrix")); //$NON-NLS-1$
        // inizialize new RasterData and set value
        WritableRaster tcaImage = FluidUtils.createDoubleWritableRaster(activecols, activerows, null, null, NaN);
        WritableRaster dirImage = FluidUtils.createDoubleWritableRaster(activecols, activerows, null, null, NaN);

        // it contains the analyzed cells
        WritableRaster analyzeImage = FluidUtils.createDoubleWritableRaster(activecols, activerows, null, null, null);
        WritableRaster deviationsImage = FluidUtils.createDoubleWritableRaster(activecols, activerows, null, null, null);

        if (mode == 1) {
            OrlandiniD8_LAD(indexes, deviationsImage, analyzeImage, pitImage, flowImage, tcaImage, dirImage, nelev, activeRegion);
        } else {
            OrlandiniD8_LTD(indexes, deviationsImage, analyzeImage, pitImage, flowImage, tcaImage, dirImage, nelev, activeRegion);
            // only if required executes this method
            if (fixedMode == 1) {
                newDirections(flowfixedImage, pitImage, dirImage);
            }

        }
        return new WritableRaster[]{dirImage, tcaImage};

    };

    /**
     * routine that defines the draining directions
     * 
     * @param indexes
     *            vector containing the order of elevation
     * @param deviationsImage
     *            the map containing the deviation
     * @param analyzeImage
     * @param nelev
     * @return
     */
    private short OrlandiniD8_LAD( double[] indexes, WritableRaster deviationsImage, WritableRaster analyzeImage,
            PlanarImage pitImage, PlanarImage flowImage, WritableRaster tcaImage, WritableRaster dirImage, int nelev,
            JGrassRegion activeRegion ) {
        int row, col, ncelle, nr, nc;
        int realrows, realcols;
        double dev1, dev2, sumdev1, sumdev2, sumdev;
        double[] dati = new double[10]; /*
                                         * it contains:
                                         * pend,dir,e0,e1,e2,sumdev,
                                         * didren1,dirdren2,sigma
                                         */
        double count, flow;

        double[] u = {activeRegion.getWEResolution(), activeRegion.getNSResolution()};
        double[] v = {NaN, NaN};
        // get rows and cols from the active region
        realrows = pitImage.getHeight();
        realcols = pitImage.getWidth();

        ncelle = 0;
        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);

        WritableRandomIter tcaRandomIter = RandomIterFactory.createWritable(tcaImage, null);
        WritableRandomIter analyseRandomIter = RandomIterFactory.createWritable(analyzeImage, null);
        WritableRandomIter deviationRandomIter = RandomIterFactory.createWritable(deviationsImage, null);
        WritableRandomIter dirRandomIter = RandomIterFactory.createWritable(dirImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "OrlandiniD8 LAD", realrows * realcols);
        for( int i = realrows * realcols - 1; i >= 0; i-- ) {
            count = indexes[i];
            col = (int) count % realcols - 1;
            row = (int) count / realcols;
            if (!isNovalue(pitRandomIter.getSampleDouble(col, row, 0)) && !isNovalue(flowRandomIter.getSampleDouble(col, row, 0))) {
                ncelle = ncelle + 1;
                compose(analyseRandomIter, pitRandomIter, tcaRandomIter, dati, u, v, col, row);

                if (dati[1] > 0) {
                    dev1 = dati[2];
                    dev2 = (PI / 4) - dati[2];

                    if (dati[9] == 1) {
                        dev2 = -dev2;
                    } else {
                        dev1 = -dev1;
                    }
                    calcarea(row, col, dati, v, analyseRandomIter, deviationRandomIter, pitRandomIter, tcaRandomIter,
                            dirRandomIter, i, i);

                    sumdev = dati[6];
                    sumdev1 = dev1 + (lambda * sumdev);
                    sumdev2 = dev2 + (lambda * sumdev);
                    if ((Math.abs(sumdev1) <= Math.abs(sumdev2)) && ((dati[3] - dati[4]) > 0.0)) {
                        dirRandomIter.setSample(col, row, 0, dati[7]);
                        deviationRandomIter.setSample(col, row, 0, sumdev1);
                    } else if (Math.abs(sumdev1) > Math.abs(sumdev2) || (dati[3] - dati[5]) > 0.0) {
                        dirRandomIter.setSample(col, row, 0, dati[8]);
                        deviationRandomIter.setSample(col, row, 0, sumdev2);
                    } else {
                        break;
                    }
                } else if (dati[1] == 0) {
                    if (ncelle == nelev) {
                        /* sono all'uscita */
                        calcarea(row, col, dati, v, analyseRandomIter, deviationRandomIter, pitRandomIter, tcaRandomIter,
                                dirRandomIter, realcols, realrows);
                        dirRandomIter.setSample(col, row, 0, 10);
                        deviationRandomIter.setSample(col, row, 0, lambda * dati[6]);

                        if (tcaRandomIter.getSampleDouble(col, row, 0) != ncelle) {
                            pm.done();
                            return (1);
                        } else {
                            pm.done();
                            return (2);
                        }
                    } else {
                        calcarea(row, col, dati, v, analyseRandomIter, deviationRandomIter, pitRandomIter, tcaRandomIter,
                                dirRandomIter, realcols, realrows);
                        sumdev = lambda * dati[6];
                        dirRandomIter.setSample(col, row, 0, flowRandomIter.getSampleDouble(col, row, 0));
                        flow = dirRandomIter.getSampleDouble(col, row, 0);
                        nr = row + order[(int) flow][0];
                        nc = col + order[(int) flow][1];
                        while( analyseRandomIter.getSampleDouble(nc, nr, 0) == 1 ) {
                            tcaRandomIter.setSample(nc, nr, 0, tcaRandomIter.getSampleDouble(nc, nr, 0)
                                    + tcaRandomIter.getSampleDouble(col, row, 0));
                            flow = dirRandomIter.getSampleDouble(nc, nr, 0);
                            nr = nr + order[(int) flow][0];
                            nc = nc + order[(int) flow][1];
                        }
                        deviationRandomIter.setSample(col, row, 0, sumdev);
                    }
                }
            } else if (isNovalue(pitRandomIter.getSampleDouble(col, row, 0))) {
                break;
            }
            pm.worked(1);
        }
        pm.done();

        dirRandomIter.done();
        pitRandomIter.done();
        flowRandomIter.done();
        deviationRandomIter.done();
        analyseRandomIter.done();
        tcaRandomIter.done();
        return 1;
    }

    /**
     * routine that defines the draining directions
     * 
     * @param indexes
     *            vector containing the order of elevation
     * @param deviationsImage
     *            the map containing the deviation
     * @param analyzeImage
     * @param nelev
     * @return
     */
    private short OrlandiniD8_LTD( double[] indexes, WritableRaster deviationsImage, WritableRaster analyzeImage,
            PlanarImage pitImage, PlanarImage flowImage, WritableRaster tcaImage, WritableRaster dirImage, int nelev,
            JGrassRegion activeRegion ) {

        int row, col, ncelle, nr, nc;
        int realrows, realcols;
        double dx, dev1, dev2, sumdev1, sumdev2, sumdev;
        double[] dati = new double[10]; /*
                                         * it contains:
                                         * pend,dir,e0,e1,e2,sumdev,
                                         * didren1,dirdren2,sigma
                                         */
        double count, flow;
        /*
         * it indicates the position of the triangle's vertexes
         */
        realrows = pitImage.getHeight();
        realcols = pitImage.getWidth();
        dx = activeRegion.getWEResolution();
        ncelle = 0;
        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);

        WritableRandomIter tcaRandomIter = RandomIterFactory.createWritable(tcaImage, null);
        WritableRandomIter analyseRandomIter = RandomIterFactory.createWritable(analyzeImage, null);
        WritableRandomIter deviationRandomIter = RandomIterFactory.createWritable(deviationsImage, null);
        WritableRandomIter dirRandomIter = RandomIterFactory.createWritable(dirImage, null);
        double[] u = {activeRegion.getWEResolution(), activeRegion.getNSResolution()};
        double[] v = {NaN, NaN};
        dx = u[0];
        // get rows and cols from the active region
        realrows = pitImage.getHeight();
        realcols = pitImage.getWidth();

        ncelle = 0;
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "OrlandiniD8 LAD", realrows * realcols);
        for( int i = realrows * realcols - 1; i >= 0; i-- ) {
            count = indexes[i];
            col = (int) count % realcols - 1;
            row = (int) count / realcols;

            if (!isNovalue(pitRandomIter.getSampleDouble(col, row, 0)) && !isNovalue(flowRandomIter.getSampleDouble(col, row, 0))) {
                ncelle = ncelle + 1;

                compose(analyseRandomIter, pitRandomIter, tcaRandomIter, dati, u, v, col, row);

                if (dati[1] > 0) {
                    dev1 = dx * Math.sin(dati[2]);
                    dev2 = dx * Math.sqrt(2.0) * Math.sin(PI / 4 - dati[2]);
                    if (dati[9] == 1) {
                        dev2 = -dev2;
                    } else {
                        dev1 = -dev1;
                    }
                    calcarea(row, col, dati, v, analyseRandomIter, deviationRandomIter, pitRandomIter, tcaRandomIter,
                            dirRandomIter, realcols, realrows);
                    sumdev = dati[6];
                    sumdev1 = dev1 + lambda * sumdev;
                    sumdev2 = dev2 + lambda * sumdev;
                    if (Math.abs(sumdev1) <= Math.abs(sumdev2) && (dati[3] - dati[4]) > 0.0) {
                        dirRandomIter.setSample(col, row, 0, dati[7]);
                        deviationRandomIter.setSample(col, row, 0, sumdev1);
                    } else if (Math.abs(sumdev1) > Math.abs(sumdev2) || (dati[3] - dati[5]) > 0.0) {
                        dirRandomIter.setSample(col, row, 0, dati[8]);
                        deviationRandomIter.setSample(col, row, 0, sumdev2);
                    } else {
                        break;
                    }
                } else if (dati[1] == 0) {
                    if (ncelle == nelev) {
                        /* sono all'uscita */
                        calcarea(row, col, dati, v, analyseRandomIter, deviationRandomIter, pitRandomIter, tcaRandomIter,
                                dirRandomIter, realcols, realrows);
                        dirRandomIter.setSample(col, row, 0, 10);
                        deviationRandomIter.setSample(col, row, 0, lambda * dati[6]);

                        if (tcaRandomIter.getSampleDouble(col, row, 0) != ncelle) {
                            pm.done();
                            return (1);
                        } else {
                            pm.done();
                            return (2);
                        }
                    } else {
                        calcarea(row, col, dati, v, analyseRandomIter, deviationRandomIter, pitRandomIter, tcaRandomIter,
                                dirRandomIter, realcols, realrows);
                        sumdev = lambda * dati[6];
                        dirRandomIter.setSample(col, row, 0, flowRandomIter.getSampleDouble(col, row, 0));
                        flow = dirRandomIter.getSampleDouble(col, row, 0);
                        nr = row + order[(int) flow][0];
                        nc = col + order[(int) flow][1];
                        while( analyseRandomIter.getSampleDouble(nc, nr, 0) == 1 ) {
                            tcaRandomIter.setSample(nc, nr, 0, (tcaRandomIter.getSampleDouble(nc, nr, 0) + tcaRandomIter
                                    .getSampleDouble(col, row, 0)));
                            flow = dirRandomIter.getSampleDouble(nc, nr, 0);
                            nr = nr + order[(int) flow][0];
                            nc = nc + order[(int) flow][1];
                        }
                        deviationRandomIter.setSample(col, row, 0, sumdev);
                    }
                }
            } else if (!isNovalue(pitRandomIter.getSampleDouble(col, row, 0))) {
                break;
            }
            pm.worked(1);
        }
        pm.done();

        dirRandomIter.done();
        pitRandomIter.done();
        flowRandomIter.done();
        deviationRandomIter.done();
        analyseRandomIter.done();
        tcaRandomIter.done();
        return 1;
    }

    /**
     * It calculates the drainage area for a cell[rows][cols]
     * 
     * @param row
     * @param col
     * @param dati
     * @param v
     * @param analyse
     * @param deviation
     */
    private void calcarea( int row, int col, double[] dati, double[] v, WritableRandomIter analyse, WritableRandomIter deviation,
            RandomIter pitRandomIter, WritableRandomIter tcaRandomIter, WritableRandomIter dirRandomIter, int nCols, int nRows ) {
        int conta, ninflow;
        int outdir;
        double sumdev;
        double[] dev = new double[8];
        double[] are = new double[8];

        ninflow = 0;
        sumdev = 0;
        for( int n = 1; n <= 8; n++ ) {
            conta = (col + order[n][1] - 1) * nCols + row + order[n][0];
            /*
             * verifico se la cella che sto considerando è stata già processata
             */
            if (analyse.getSampleDouble(col + order[n][1], row + order[n][0], 0) == 1) {
                if (!isNovalue(pitRandomIter.getSampleDouble(col + order[n][1], row + order[n][0], 0)) || conta <= nRows * nCols) {
                    outdir = (int) dirRandomIter.getSampleDouble(col + order[n][1], row + order[n][0], 0);
                    /*
                     * verifico se la cella che sto considerando drena nel pixel
                     * centrale
                     */
                    if (outdir - n == 4 || outdir - n == -4) {
                        ninflow = ninflow + 1;
                        tcaRandomIter.setSample(col, row, 0, tcaRandomIter.getSampleDouble(col, row, 0)
                                + tcaRandomIter.getSampleDouble(col + order[n][1], row + order[n][0], 0));
                        dev[ninflow] = deviation.getSampleDouble(col + order[n][1], row + order[n][0], 0);
                        are[ninflow] = tcaRandomIter.getSampleDouble(col + order[n][1], row + order[n][0], 0);
                    }
                }
            }
        }

        for( int i = 1; i <= ninflow; i++ ) {
            sumdev = sumdev + are[i] * dev[i] / tcaRandomIter.getSampleDouble(col, row, 0);
        }
        dati[6] = sumdev;

    }

    /**
     * It calculates the direction of maximun slope.
     * 
     * @param analyse
     * @param dati
     * @param u
     * @param v
     * @param col
     * @param row
     */
    private void compose( WritableRandomIter analyse, RandomIter pitRandomIter, WritableRandomIter tcaRandomIter, double[] dati,
            double[] u, double[] v, int col, int row ) {
        int n = 1, m = 1;

        double pendmax, dirmax = 0.0, e1min = -9999.0, e2min = -9999.0;
        int[][] tri = {{1, 2, 1}, /* tri 012 */
        {3, 2, -1}, /* tri 023 |4|3|2| */
        {3, 4, 1}, /* tri 034 |5|0|1| drainage direction. */
        {5, 4, -1}, /* tri 045 |6|7|8| */
        {5, 6, 1}, /*
                    * tri 056 indico direzioni di drenaggio corrispondenti ai
                    * verici
                    */
        {7, 6, -1}, /*
                     * tri 067 dei triangoli (colonne 1,2) e il segno (sigma)
                     * associato
                     */
        {7, 8, 1}, /* tri 078 al triangolo stesso (colonna 3). */
        {1, 8, -1} /* tri 089 */
        };

        analyse.setSample(col, row, 0, 1.0);
        tcaRandomIter.setSample(col, row, 0, 1.0);
        pendmax = 0.0;
        dati[3] = pitRandomIter.getSampleDouble(col, row, 0);
        /*
         * per ogni triangolo calcolo la pendenza massima e la direzione di
         * deflusso reale.
         */
        for( int j = 0; j <= 7; j++ ) {
            n = tri[j][0];
            m = tri[j][1];

            dati[4] = pitRandomIter.getSampleDouble(col + order[n][1], row + order[n][0], 0);
            dati[5] = pitRandomIter.getSampleDouble(col + order[m][1], row + order[m][0], 0);
            /*
             * verifico che i punti attorno al pixel considerato non siano
             * novalue. In questo caso trascuro il triangolo.
             */
            if (!isNovalue(dati[4]) && !isNovalue(dati[5])) {

                triangoli(u, dati);
                if (dati[1] > pendmax) {
                    dirmax = dati[2];
                    pendmax = dati[1];
                    dati[7] = tri[j][0]; /* - direzione cardinale */
                    dati[8] = tri[j][1]; /* - direzione diagonale */
                    dati[9] = tri[j][2]; /* - segno del triangolo */
                    e1min = dati[4]; /*
                                      * - quote del triangolo avente pendenza
                                      * maggiore
                                      */
                    e2min = dati[5]; /*
                                      * non necessariamente sono le quote
                                      * minime.
                                      */
                }
            }
        }
        dati[1] = pendmax;
        dati[2] = dirmax;
        dati[4] = e1min;
        dati[5] = e2min;

    }

    /**
     * Calcola per ogni triangolo la direzione e la pendenza massima.
     * 
     * @param u
     * @param dati
     */
    private void triangoli( double[] u, double[] dati ) {
        double pend1, pend2, sp, sd, dx, dy;
        /* definsco le dim. del pixel */
        dx = u[0];
        dy = u[1];

        pend1 = (dati[3] - dati[4]) / dy;
        pend2 = (dati[4] - dati[5]) / dx;
        if (pend1 == 0.0) {
            if (pend2 >= 0.0) {
                dati[2] = +PI / 2;
            } else {
                dati[2] = -PI / 2;
            }
        } else {
            dati[2] = Math.atan(pend2 / pend1);
        }
        sp = Math.sqrt(pend1 * pend1 + pend2 * pend2);
        sd = (dati[3] - dati[5]) / Math.sqrt(dx * dx + dy * dy);

        if (dati[2] >= 0 && dati[2] <= PI / 4 && pend1 >= 0) {
            dati[1] = sp;
        } else {
            if (pend1 > sd) {
                dati[1] = pend1;
                dati[2] = 0;
            } else {
                dati[1] = sd;
                dati[2] = PI / 4;
            }
        }
    }

    /**
     * The fixed network method allows you to assign a known channel network and
     * to then correct the drainage directions.
     */
    private void newDirections( PlanarImage flowfixedTmpImage, PlanarImage pitImage, WritableRaster dirImage ) {
        int[][] odir = {{0, 0, 0}, {0, 1, 1}, {-1, 1, 2}, {-1, 0, 3}, {-1, -1, 4}, {0, -1, 5}, {1, -1, 6}, {1, 0, 7}, {1, 1, 8},
                {0, 0, 9}, {0, 0, 10}};
        double elev = 0.0;
        int[] flow = new int[2], nflow = new int[2];
        int cols = pitImage.getWidth();
        int rows = pitImage.getHeight();
        int minX = pitImage.getMinX();
        int minY = pitImage.getMinY();
        int maxX = pitImage.getMaxX();
        int maxY = pitImage.getMaxY();
        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);

        WritableRandomIter dirRandomIter = RandomIterFactory.createWritable(dirImage, null);

        // setting novalues...
        PlanarImage flowFixedImage = FluidUtils.setJaiNovalueBorder(flowfixedTmpImage);
        RandomIter flowRandomIter = RandomIterFactory.create(flowFixedImage, null);

        WritableRaster modflowImage = FluidUtils.createDoubleWritableRaster(pitImage.getWidth(), pitImage.getHeight(), null,
                null, null);

        WritableRandomIter modflowRandomIter = RandomIterFactory.createWritable(modflowImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "new directions...", maxY - minY);
        for( int j = minY; j <= maxY; j++ ) {
            for( int i = minX; i <= maxX; i++ ) {
                if (!isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    flow[0] = i;
                    flow[1] = j;
                    for( int k = 1; k <= 8; k++ ) {
                        nflow[0] = flow[0] + odir[k][1];
                        nflow[1] = flow[1] + odir[k][0];
                        if (modflowRandomIter.getSampleDouble(nflow[0], nflow[1], 0) == 0
                                && isNovalue(flowRandomIter.getSampleDouble(nflow[0], nflow[1], 0))) {
                            elev = pitRandomIter.getSampleDouble(nflow[0] + odir[1][1], nflow[1] + odir[1][0], 0);
                            for( int n = 2; n <= 8; n++ ) {
                                if (nflow[0] + odir[n][0] >= 0 && nflow[0] + odir[n][1] <= rows && nflow[1] + odir[n][0] >= 0
                                        && nflow[1] + odir[n][0] <= cols) {
                                    if (pitRandomIter.getSampleDouble(nflow[0] + odir[n][1], nflow[1] + odir[n][0], 0) >= elev) {
                                        elev = pitRandomIter.getSampleDouble(nflow[0] + odir[n][1], nflow[1] + odir[n][0], 0);
                                        dirRandomIter.setSample(nflow[0], nflow[1], 0, odir[n][2]);
                                    }
                                }
                            }
                            for( int s = 1; s <= 8; s++ ) {
                                if (nflow[0] + odir[s][0] >= 0 && nflow[0] + odir[s][0] <= rows && nflow[1] + odir[s][1] >= 0
                                        && nflow[1] + odir[s][1] <= cols) {
                                    if (!isNovalue(flowRandomIter
                                            .getSampleDouble(nflow[0] + odir[s][1], nflow[1] + odir[s][0], 0))) {

                                        if (pitRandomIter.getSampleDouble(nflow[0] + odir[s][1], nflow[1] + odir[s][0], 0) <= elev) {
                                            elev = pitRandomIter.getSampleDouble(nflow[0] + odir[s][1], nflow[1] + odir[s][0], 0);
                                            dirRandomIter.setSample(nflow[0], nflow[1], 0, odir[s][2]);
                                        }
                                    }
                                }
                            }
                            modflowRandomIter.setSample(nflow[0], nflow[1], 0, 1);
                        }

                    }
                }
                if (!isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    dirRandomIter.setSample(i, j, 0, flowRandomIter.getSampleDouble(i, j, 0));
                }
            }
            pm.worked(1);
        }
        pm.done();

        dirRandomIter.done();
        pitRandomIter.done();
        modflowRandomIter.done();
        flowRandomIter.done();

    }

}
