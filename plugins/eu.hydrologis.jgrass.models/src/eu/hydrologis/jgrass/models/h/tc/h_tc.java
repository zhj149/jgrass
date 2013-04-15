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
package eu.hydrologis.jgrass.models.h.tc;

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
 * The openmi compliant representation of the tc (TopographicClasses) model. It subdivides the sites
 * of a basin in the 9 topographic classes identified by the longitudinal and transversal
 * curvatures.
 * </p>
 * <p>
 * The program asks as input the threshold values of the longitudinal and normal curvatures which
 * define their planarity (i.e. those sites presenting a curvature with absolute value lesser than
 * the threshold). This is a value which has to be "calibrated" for each basin. The program produces
 * two different output matrixes, one with the 9 classes schematized conventionally in the following
 * way:
 * <UL>
 * <LI>10 planar -planar sites </LI>
 * <LI>20 convex-planar sites </LI>
 * <LI>30 concave- planar sites </LI>
 * <LI>40 planar- convex sites </LI>
 * <LI>50 convex-convex sites </LI>
 * <LI>60 concave-convex sites </LI>
 * <LI>70 planar-concave sites </LI>
 * <LI>80 convex-concave sites </LI>
 * <LI>90 concave-concave sites. </LI>
 * </UL>
 * </p>
 * <p>
 * The second output file contains an aggregation of these classes in the three fundamentals,
 * indexed as follows:
 * <UL>
 * <LI>15 concave sites (classes 30,70, 90) </LI>
 * <LI>25 planar sites (class 10) </LI>
 * <LI>35 convex sites (classes 20, 40, 50, 60, 80). </LI>
 * </UL>
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map of the longitudinal curvatures (-prof); </LI>
 * <LI>the map of the normal curvatures (-tang); </LI>
 * <LI>the threshold value for the longitudinal curvatures (-thprof); </LI>
 * <LI>the threshold value for the normal curvatures (-thtang); </LI>
 * </OL>
 * <P>
 * </DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map of the 9 curvatures classes (-cp9); </LI>
 * <LI>the map of the concave, convex and planar sites (-cp3); </LI>
 * </OL>
 * <P>
 * </DD>
 * Usage h.tc --igrass-prof prof --igrass-tang tang --ograss-cp3 cp3 --ograss-cp9 cp9 --thprof value
 * --thtang value
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini Andrea, Franceschi
 *         Silvia, Pisoni Silvano, Rigon Riccardo
 */

public class h_tc extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String profID = "prof";

    public final static String tangID = "tang";

    public final static String cp3ID = "cp3";

    public final static String cp9ID = "cp9";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_tc.usage");

    private ILink profLink = null;

    private ILink tangLink = null;

    private ILink cp3Link = null;

    private ILink cp9Link = null;

    private IInputExchangeItem profDataInputEI = null;

    private IInputExchangeItem tangDataInputEI = null;

    private IOutputExchangeItem cp3DataOutputEI = null;

    private IOutputExchangeItem cp9DataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private double thProf = -1;

    private double thTang = -1;

    private boolean doTile;

    private String locationPath;
    private JGrassGridCoverageValueSet jgrValueSet;

    private WritableRaster[] cpImages;

    private CoordinateReferenceSystem crs;

    /** */
    public h_tc() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_tc

    /** */
    public h_tc( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_tc

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(profID)) {
            profLink = link;
        }
        if (id.equals(tangID)) {
            tangLink = link;
        }
        if (id.equals(cp3ID)) {
            cp3Link = link;
        }
        if (id.equals(cp9ID)) {
            cp9Link = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: prof, tang
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return profDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return tangDataInputEI;
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
     * there is an IOutputExchangeItem: cp3 & cp9
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return cp3DataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return cp9DataOutputEI;
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
        if (cpImages == null) {
            if (thProf == -1 || thTang == -1) {
                out.println(getModelDescription());
                return null;
            }

            GridCoverage2D profGC = ModelsConstants.getGridCoverage2DFromLink(profLink, time, err);
            GridCoverage2D tangGC = ModelsConstants.getGridCoverage2DFromLink(tangLink, time, err);

            PlanarImage profImage = (PlanarImage) profGC.getRenderedImage();
            PlanarImage tangImage = (PlanarImage) tangGC.getRenderedImage();

            cpImages = tc(profImage, tangImage);

            if (cpImages == null) {
                out.println("Errors in execution...");
                return null;
            } else {

            }
        }
        if (linkID.equals(cp3Link.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(cpImages[0], activeRegion, crs);
            return jgrValueSet;
        } else if (linkID.equals(cp9Link.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(cpImages[1], activeRegion, crs);
            return jgrValueSet;
        }
        return null;
    }

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
                if (key.compareTo("thprof") == 0) {
                    thProf = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("thtang") == 0) {
                    thTang = Double.parseDouble(argument.getValue());
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

        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        componentDescr = "h.tc";
        componentId = null;

        /*
         * create the exchange items
         */
        // cp3 output
        cp3DataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // cp9 output
        cp9DataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        // element set defining what we want to read
        // prof input

        profDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // tang input
        tangDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        // TODO Auto-generated method stub
        if (linkID.equals(profLink.getID())) {
            profLink = null;
        }
        if (linkID.equals(tangLink.getID())) {
            tangLink = null;
        }
        if (cp3ID.equals(cp3Link.getID())) {
            cp3Link = null;
        }
        if (cp9ID.equals(cp9Link.getID())) {
            cp9Link = null;
        }
    }

    /**
     * Calculates the tc in every pixel of the map
     * 
     * @return
     */
    private WritableRaster[] tc( PlanarImage profImage, PlanarImage tangImage ) {
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();
        RandomIter profRandomIter = RandomIterFactory.create(profImage, null);
        RandomIter tangRandomIter = RandomIterFactory.create(tangImage, null);

        WritableRaster cp3Image = FluidUtils.createDoubleWritableRaster(profImage.getWidth(), profImage.getHeight(), null,
                profImage.getSampleModel(), null);
        WritableRandomIter cp3RandomIter = RandomIterFactory.createWritable(cp3Image, null);
        WritableRaster cp9Image = FluidUtils.createDoubleWritableRaster(profImage.getWidth(), profImage.getHeight(), null,
                profImage.getSampleModel(), null);
        WritableRandomIter cp9RandomIter = RandomIterFactory.createWritable(cp9Image, null);

        // calculate ...
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "tc9...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                double tangValue = tangRandomIter.getSampleDouble(i, j, 0);
                if (isNovalue(tangValue)) {
                    cp9RandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                } else {
                    double profValue = profRandomIter.getSampleDouble(i, j, 0);
                    if (Math.abs(tangValue) <= thTang) {
                        if (Math.abs(profValue) <= thProf) {
                            cp9RandomIter.setSample(i, j, 0, 10);
                        } else if (profValue < -thProf) {
                            cp9RandomIter.setSample(i, j, 0, 20);
                        } else if (profValue > thProf) {
                            cp9RandomIter.setSample(i, j, 0, 30);
                        }
                    } else if (tangValue < -thTang) {
                        if (Math.abs(profValue) <= thProf) {
                            cp9RandomIter.setSample(i, j, 0, 40);
                        } else if (profValue < -thProf) {
                            cp9RandomIter.setSample(i, j, 0, 50);
                        } else if (profValue > thProf) {
                            cp9RandomIter.setSample(i, j, 0, 60);
                        }
                    } else if (tangValue > thTang) {
                        if (Math.abs(profValue) <= thProf) {
                            cp9RandomIter.setSample(i, j, 0, 70);
                        } else if (profValue < -thProf) {
                            cp9RandomIter.setSample(i, j, 0, 80);
                        } else if (profValue > thProf) {
                            cp9RandomIter.setSample(i, j, 0, 90);
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();

        pm.beginTask(MessageHelper.WORKING_ON + "tc3...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                double cp9Value = cp9RandomIter.getSampleDouble(i, j, 0);
                if (!isNovalue(cp9Value)) {
                    if (cp9Value == 70 || cp9Value == 90 || cp9Value == 30) {
                        cp3RandomIter.setSample(i, j, 0, 15);
                    } else if (cp9Value == 10) {
                        cp3RandomIter.setSample(i, j, 0, 25);
                    } else {
                        cp3RandomIter.setSample(i, j, 0, 35);
                    }
                } else {
                    cp3RandomIter.setSample(i, j, 0, cp9Value);
                }
            }
            pm.worked(1);
        }
        pm.done();
        return new WritableRaster[]{cp3Image, cp9Image};
    }
}
