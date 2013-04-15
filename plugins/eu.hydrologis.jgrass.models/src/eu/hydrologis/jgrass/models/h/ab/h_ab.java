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
package eu.hydrologis.jgrass.models.h.ab;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
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
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the Ab model. It calculates the draining area per length
 * unit (A/b), where A is the total area and b is the length of the contour line which is assumed as
 * drained by the A area.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map of planar curvatures (-plan);</LI>
 * <LI>the map with the total contributing areas (obtained with multitca or tca) (-tca);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map of the areas per length unit (-ab);</LI>
 * <LI>the map of the contour line (-b).</LI>
 * </OL>
 * <P></DD>
 * </p>
 * <p>
 * Usage: h.ab --igrass-plan plan --igrass-tca tca --ograss-ab ab --ograss-b b 0/1
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Rigon Riccardo
 */
public class h_ab extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String planID = "plan";

    public final static String tcaID = "tca";

    public final static String alungID = "ab";

    public final static String bID = "b";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_ab.usage");

    private ILink planLink = null;

    private ILink tcaLink = null;

    private ILink alungLink = null;

    private ILink bLink = null;

    private IInputExchangeItem planDataInputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private IOutputExchangeItem alungDataOutputEI = null;

    private IOutputExchangeItem bDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private double xRes;

    private double yRes;

    private String locationPath;

    /*
     * JAI variable
     */

    private WritableRandomIter aLungRandomIter;
    private WritableRandomIter bRandomIter;

    private boolean doTile;
    private RandomIter tcaFileRandomIter;
    private RandomIter planFileRandomIter;

    private PlanarImage planImage;

    private PlanarImage tcaImage;
    WritableRaster alungImage;
    WritableRaster bImage;

    private Rectangle imageBounds;

    private JGrassGridCoverageValueSet jgrValueSet;


    public h_ab() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_ab( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(planID)) {
            planLink = link;
        }
        if (id.equals(tcaID)) {
            tcaLink = link;
        }
        if (id.equals(alungID)) {
            alungLink = link;
        }
        if (id.equals(bID)) {
            bLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: plan, tca
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return planDataInputEI;
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
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: ab & b
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return alungDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return bDataOutputEI;
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
        if (linkID.equals(alungLink.getID()) || linkID.equals(bLink.getID())) {
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

            if (tcaImage == null || planImage == null) {
                IValueSet planValueSet = planLink.getSourceComponent().getValues(time,
                        planLink.getID());
                IValueSet tcaValueSet = tcaLink.getSourceComponent().getValues(time,
                        tcaLink.getID());
                GridCoverage2D planData = null;
                GridCoverage2D tcaData = null;
                if (planValueSet != null && tcaValueSet != null) {
                    planData = ((JGrassGridCoverageValueSet) planValueSet).getGridCoverage2D();
                    tcaData = ((JGrassGridCoverageValueSet) tcaValueSet).getGridCoverage2D();
                } else {
                    String error = Messages.getString("erroreading"); //$NON-NLS-1$
                    err.println(error);
                    throw new IOException(error);
                }
                GridCoverage2D view = planData.view(ViewType.GEOPHYSICS);
                planImage = (PlanarImage) view.getRenderedImage();
                view = tcaData.view(ViewType.GEOPHYSICS);
                tcaImage = (PlanarImage) view.getRenderedImage();

                Rectangle planarRect = planImage.getBounds();
                Rectangle tcaRect = tcaImage.getBounds();
                if (planarRect.intersects(tcaRect)) {
                    if (planarRect.equals(tcaRect)) {
                        imageBounds = planarRect;

                    } else {
                        Rectangle.intersect(planarRect, tcaRect, imageBounds);
                        out
                                .print("the tca and plan Rectangle are different but the intersection isn't null");
                    }
                } else {
                    String error = "The regions of the files to read are different, the intersection is null";
                    err.println(error);
                    throw new IOException(error);
                }
                xRes = activeRegion.getWEResolution();
                yRes = activeRegion.getNSResolution();
                planFileRandomIter= RandomIterFactory.create(planImage, null);
                tcaFileRandomIter= RandomIterFactory.create(tcaImage, null);
                doTile = true;

                    int width = imageBounds.width;
                    int height = imageBounds.height;
                    alungImage =  FluidUtils.createDoubleWritableRaster(width, height, null, null, null);
                    aLungRandomIter = RandomIterFactory.createWritable(alungImage, null);
                    bImage =  FluidUtils.createDoubleWritableRaster(width, height, null, null, null);
                    bRandomIter = RandomIterFactory.createWritable(bImage, null);


                if (!ab()) {
                    err.println(MessageHelper.AN_ERROR_OCCURRED_WHILE_CALCULATING + " ab...");
                    return null;
                } else {
                    if (linkID.equals(alungLink.getID())) {
                        jgrValueSet = new JGrassGridCoverageValueSet(alungImage, activeRegion, crs);
                        return jgrValueSet;
                    } else if (linkID.equals(bLink.getID())) {
                        jgrValueSet = new JGrassGridCoverageValueSet(bImage, activeRegion, crs);
                        return jgrValueSet;
                    }
                }
            } else {
                if (linkID.equals(alungLink.getID())) {
                    jgrValueSet = new JGrassGridCoverageValueSet(alungImage, activeRegion, crs);
           
                    return jgrValueSet;
                } else if (linkID.equals(bLink.getID())) {
                    jgrValueSet = new JGrassGridCoverageValueSet(bImage, activeRegion, crs);
                    return jgrValueSet;
                }
            }
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
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.getBoolean(argument.getValue());
                }
            }
        }
        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
        + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.ab";
        componentId = null;

        /*
         * create the exchange items
         */
        // alung output

        alungDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        // b output

        bDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        // element set defining what we want to read
        // plan curvature

        planDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        // tca

        tcaDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(planLink.getID())) {
            planLink = null;
        }
        if (linkID.equals(tcaLink.getID())) {
            tcaLink = null;
        }
        if (alungID.equals(alungLink.getID())) {
            alungLink = null;
        }
        if (bID.equals(bLink.getID())) {
            bLink = null;
        }
    }

    /**
     * Calculates the ab in every pixel of the map
     * 
     * @return
     */
    private boolean ab() {
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        // get resolution of the active region

        // create new matrix
        
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.ab", rows);
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                double planSample = planFileRandomIter.getSampleDouble(j, i, 0);
                if (!isNovalue(planSample) && planSample != 0.0) {
                    if (xRes > 1 / planSample && planSample >= 0.0) {
                        bRandomIter.setSample(j, i, 0, 0.1 * xRes);
                    } else if (xRes > Math.abs(1 / planSample) && planSample < 0.0) {
                        bRandomIter.setSample(j, i, 0, xRes + 0.9 * xRes);
                    } else {
                        double bSample = 2 * Math.asin(xRes / (2 * (1 / planSample)))
                                * (1 / planSample - xRes);
                        bRandomIter.setSample(j, i, 0, bSample);
                        if (planSample >= 0.0 && bSample < 0.1 * xRes) {
                            bRandomIter.setSample(j, i, 0, 0.1 * xRes);
                        }
                        if (planSample < 0.0 && bSample > (xRes + 0.9 * xRes)) {
                            bRandomIter.setSample(j, i, 0, xRes + 0.9 * xRes);
                        }
                    }
                }
                if (planSample == 0.0) {
                    bRandomIter.setSample(j, i, 0, xRes);
                }
                aLungRandomIter.setSample(j, i, 0, tcaFileRandomIter.getSampleDouble(j, i, 0)
                        * xRes * xRes / bRandomIter.getSampleDouble(j, i, 0));
                if (isNovalue(planSample) ) {
                    aLungRandomIter.setSample(j, i, 0, JGrassConstants.doubleNovalue);
                    bRandomIter.setSample(j, i, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();
        
        return true;
    }
}
