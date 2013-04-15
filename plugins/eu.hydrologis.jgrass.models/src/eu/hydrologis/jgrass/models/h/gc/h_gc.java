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
package eu.hydrologis.jgrass.models.h.gc;

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
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the gc (GeomorphicClasses) model. It
 * subdivides the sites of a basin in 11 topographic classes. Such classes are
 * the nine classes based on the curvature individuated by Parsons (cfr.
 * h_tc.java); the points belonging to the channel net constitute a tenth class,
 * the points with high slope the eleventh class.
 * </p>
 * <p>
 * Differently from the program TC, the program GC considers also the existence
 * of the channel net, which is extracted from the DEM. The channel net is
 * thought as a topologically connected network, even though it is known that
 * this cannot be the real case. The cases are identified as in tc plus:
 * <LI>100 &#8658; channel sites (individuated by extract network)</LI>
 * <LI>110 &#8658; ravine sites (slope > critic value).</LI>
 * </p>
 * <p>
 * The second output file contains an aggregation of these classes in the four
 * fundamentals, indexed as follows:
 * <LI>15 &#8658; non-channeled valley sites (classi 70, 90, 30 )</LI>
 * <LI>25 &#8658; planar sites (classi 10)</LI>
 * <LI>35 &#8658; channel sites (classe 100)</LI>
 * <LI>45 &#8658; hillslope sites (classi 20, 40, 50, 60, 80)</LI>
 * <LI>55 &#8658; ravine sites (slope > critic value).</LI>
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the matrix of the slopes (-slope);</LI>
 * <LI>the matrix of the channel network (-net);</LI>
 * <LI>the matrix containing the subdivisions 9 classes (-cp9);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the matrix containing the subdivision in the 11 predefined classes
 * (-class);</LI>
 * <LI>the matrix of the aggregated classes (hillslope, valleys and net)
 * (-aggclass);</LI>
 * </OL>
 * <P></DD>
 * Usage mode 0:h.gc --igrass-slope slope --igrass-net net --igrass-cp9 cp9
 * --ograss-class class --ograss-aggclass aggclass --thgrad value
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Cozzini Andrea, Rigon
 *         Riccardo
 */

public class h_gc extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String slopeID = "slope";

    public final static String netID = "net";

    public final static String cp9ID = "cp9";

    public final static String classID = "class";

    public final static String aggClassID = "aggclass";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_gc.usage"); //$NON-NLS-1$

    private ILink slopeLink = null;

    private ILink netLink = null;

    private ILink cp9Link = null;

    private ILink classLink = null;

    private ILink aggClassLink = null;

    private IInputExchangeItem slopeDataInputEI = null;

    private IInputExchangeItem netDataInputEI = null;

    private IInputExchangeItem cp9DataInputEI = null;

    private IOutputExchangeItem classDataOutputEI = null;

    private IOutputExchangeItem aggClassDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private double thGrad = -1;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile;

    private String locationPath;

    private WritableRaster[] cpImage;

    private CoordinateReferenceSystem crs;

    public h_gc() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_gc( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(slopeID)) {
            slopeLink = link;
        }
        if (id.equals(netID)) {
            netLink = link;
        }
        if (id.equals(cp9ID)) {
            cp9Link = link;
        }
        if (id.equals(classID)) {
            classLink = link;
        }
        if (id.equals(aggClassID)) {
            aggClassLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: slope, net, cp9
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return slopeDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return netDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return cp9DataInputEI;
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
     * there is an IOutputExchangeItem: class & aggclass
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return classDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return aggClassDataOutputEI;
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
        if (linkID.equals(classLink.getID()) || linkID.equals(aggClassLink.getID())) {
            if (cpImage == null) {
                if (thGrad == -1) {
                    out.println(getModelDescription());
                    return null;
                }
                crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                GridCoverage2D slopeGC = ModelsConstants.getGridCoverage2DFromLink(slopeLink, time, err);
                GridCoverage2D netGC = ModelsConstants.getGridCoverage2DFromLink(netLink, time, err);
                GridCoverage2D cp9GC = ModelsConstants.getGridCoverage2DFromLink(cp9Link, time, err);
                PlanarImage slopeImage = (PlanarImage) slopeGC.getRenderedImage();
                PlanarImage netImage = (PlanarImage) netGC.getRenderedImage();
                PlanarImage cp9Image = (PlanarImage) cp9GC.getRenderedImage();

                cpImage = gc(slopeImage, netImage, cp9Image);
            }
            if (cpImage == null) {
                out.println("Errors in execution...\n");
                return null;
            } else {
                if (linkID.equals(classLink.getID())) {
                    jgrValueSet = new JGrassGridCoverageValueSet(cpImage[0], activeRegion, crs);
                    return jgrValueSet;
                } else if (linkID.equals(aggClassLink.getID())) {
                    jgrValueSet = new JGrassGridCoverageValueSet(cpImage[1], activeRegion, crs);
                    return jgrValueSet;
                }
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
                if (key.compareTo("thgrad") == 0) {
                    thGrad = Double.parseDouble(argument.getValue());
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

        componentDescr = "h.gc";
        componentId = null;

        /*
         * create the exchange items
         */
        // class output
        classDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // aggClass output
        aggClassDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // slope input
        slopeDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // net input
        netDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // cp9 input
        cp9DataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(slopeLink.getID())) {
            slopeLink = null;
        }
        if (linkID.equals(netLink.getID())) {
            netLink = null;
        }
        if (linkID.equals(cp9Link.getID())) {
            cp9Link = null;
        }
        if (classID.equals(classLink.getID())) {
            classLink = null;
        }
        if (aggClassID.equals(aggClassLink.getID())) {
            aggClassLink = null;
        }
    }

    /**
     * Calculates the gc in every pixel of the map
     * 
     * @return
     */
    private WritableRaster[] gc( PlanarImage slopeImage, PlanarImage netImage, PlanarImage cp9Image ) {
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();
        RandomIter slopeRandomIter = RandomIterFactory.create(slopeImage, null);
        RandomIter netRandomIter = RandomIterFactory.create(netImage, null);
        RandomIter cp9RandomIter = RandomIterFactory.create(cp9Image, null);

        int width = slopeImage.getWidth();
        int height = slopeImage.getHeight();

        WritableRaster cpClassImage = FluidUtils.createDoubleWritableRaster(width, height, null, null,
                JGrassConstants.doubleNovalue);
        WritableRandomIter cpClassRandomIter = RandomIterFactory.createWritable(cpClassImage, null);

        WritableRaster cpAggClassImage = FluidUtils.createDoubleWritableRaster(width, height, null, null,
                JGrassConstants.doubleNovalue);
        WritableRandomIter cpAggClassRandomIter = RandomIterFactory.createWritable(cpAggClassImage, null);
        // calculate ...

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "h.gc... (1/2)", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                // individuates the pixel with a slope greater than the
                // threshold
                if (slopeRandomIter.getSampleDouble(i, j, 0) >= thGrad) {
                    cpClassRandomIter.setSample(i, j, 0, 110);
                }
                // individuates the network
                else if (netRandomIter.getSampleDouble(i, j, 0) == 2) {
                    cpClassRandomIter.setSample(i, j, 0, 100);
                } else {
                    cpClassRandomIter.setSample(i, j, 0, cp9RandomIter.getSampleDouble(i, j, 0));
                }
                if (isNovalue(slopeRandomIter.getSampleDouble(i, j, 0))) {
                    cpClassRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();

        netRandomIter = null;
        slopeRandomIter = null;

        pm.beginTask(WORKING_ON + "h.gc... (2/2)", rows);
        // aggregation of these classes:
        // 15 ? non-channeled valley sites (classes 70, 90, 30 )
        // 25 ? planar sites (class 10)
        // 35 ? channel sites (class 100)
        // 45 ? hillslope sites (classes 20, 40, 50, 60, 80)
        // 55 ? ravine sites (slope > critic value) (class 110).
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (cpClassRandomIter.getSample(i, j, 0) == 70 || cpClassRandomIter.getSampleDouble(i, j, 0) == 90
                        || cpClassRandomIter.getSampleDouble(i, j, 0) == 30) {
                    cpAggClassRandomIter.setSample(i, j, 0, 15);
                } else if (cpClassRandomIter.getSampleDouble(i, j, 0) == 10) {
                    cpAggClassRandomIter.setSample(i, j, 0, 25);
                } else if (cpClassRandomIter.getSampleDouble(i, j, 0) == 100) {
                    cpAggClassRandomIter.setSample(i, j, 0, 35);
                } else if (cpClassRandomIter.getSampleDouble(i, j, 0) == 110) {
                    cpAggClassRandomIter.setSample(i, j, 0, 55);
                } else if (!isNovalue(cpClassRandomIter.getSampleDouble(i, j, 0))) {
                    cpAggClassRandomIter.setSample(i, j, 0, 45);
                } else if (isNovalue(cpClassRandomIter.getSampleDouble(i, j, 0))) {
                    cpAggClassRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();
        return new WritableRaster[]{cpClassImage, cpAggClassImage};
    }
}
