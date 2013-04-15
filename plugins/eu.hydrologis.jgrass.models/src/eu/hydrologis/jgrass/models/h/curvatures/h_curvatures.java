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
package eu.hydrologis.jgrass.models.h.curvatures;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

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
 * The openmi compliant representation of the aspect model. It estimates the longitudinal, normal
 * and planar curvatures for each site through a finite difference schema.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of elevations (-pit);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of longitudinal curvatures (-prof);</LI>
 * <LI>the map of normal (or tangent) curvatures (-tang);</LI>
 * <LI>the file containing the matrix of planar curvatures (--plan);</LI>
 * </OL>
 * <P></DD> Usage: h.curvatures --igrass-pit pit --ograss-prof prof --ograss-plan plan --ograss-tang
 * tang
 * </p>
 * <p>
 * Note: The planar and normal (or tangent) curvatures are proportional to each other. To function,
 * the program uses a matrix in input with a NOVALUE boundary and as a rule it places the curve
 * equal to zero on the catchment boundary.<BR>
 * <BR>
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini Andrea, Franceschi
 *         Silvia, Pisoni Silvano, Rigon Riccardo,
 */
public class h_curvatures extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit"; //$NON-NLS-1$

    public final static String porfID = "prof"; //$NON-NLS-1$

    public final static String planID = "plan"; //$NON-NLS-1$

    public final static String tangID = "tang"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_curvatures.usage");

    private ILink pitLink = null;

    private ILink profLink = null;

    private ILink planLink = null;

    private ILink tangLink = null;

    private IOutputExchangeItem tangDataOutputEI = null;

    private IOutputExchangeItem planDataOutputEI = null;

    private IOutputExchangeItem profDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    private WritableRaster profMemImage;

    private WritableRaster planMemImage;

    private WritableRaster tangMemImage;

    public h_curvatures() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_curvatures( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(porfID)) {
            profLink = link;
        }
        if (id.equals(planID)) {
            planLink = link;
        }
        if (id.equals(tangID)) {
            tangLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: pit
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return pitDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 1;
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: prof, plan, tang
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return profDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return planDataOutputEI;
        }
        if (outputExchangeItemIndex == 2) {
            return tangDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 3;
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        if (profMemImage == null) {

            GridCoverage2D pitData = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);

            double dx = activeRegion.getWEResolution();
            double dy = activeRegion.getNSResolution();
            PlanarImage pitImage = (PlanarImage) pitData.getRenderedImage();
            int width = pitImage.getWidth();
            int height = pitImage.getHeight();

            profMemImage = FluidUtils.createDoubleWritableRaster(width, height, null, null, doubleNovalue);
            planMemImage = FluidUtils.createDoubleWritableRaster(width, height, null, null, doubleNovalue);
            tangMemImage = FluidUtils.createDoubleWritableRaster(width, height, null, null, doubleNovalue);
            curvatures(pitImage, profMemImage, tangMemImage, planMemImage, dx, dy);

        }
        if (linkID.equals(profLink.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(profMemImage, activeRegion, crs);
            return jgrValueSet;
        } else if (linkID.equals(planLink.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(planMemImage, activeRegion, crs);
            return jgrValueSet;
        } else if (linkID.equals(tangLink.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(tangMemImage, activeRegion, crs);
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
            }

        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;

        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.curvatures"; //$NON-NLS-1$
        componentId = null; //$NON-NLS-1$

        /*
         * create the exchange items
         */
        // dummy element set
        // prof output

        profDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // plan output

        planDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // tang output

        tangDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // pit input

        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(profLink.getID())) {
            profLink = null;
        }
        if (linkID.equals(planLink.getID())) {
            planLink = null;
        }
        if (linkID.equals(tangLink.getID())) {
            tangLink = null;
        }
    }

    /**
     * Calculates the curvatures in every pixel of the map
     * 
     * @return
     */
    private void curvatures( RenderedImage elevationImage, WritableRaster profImage, WritableRaster tangImage,
            WritableRaster planImage, double xRes, double yRes ) {

        int width = elevationImage.getWidth();
        int height = elevationImage.getHeight();

        WritableRaster sxData = FluidUtils.createDoubleWritableRaster(width, height, null, null, doubleNovalue);
        WritableRaster syData = FluidUtils.createDoubleWritableRaster(width, height, null, null, doubleNovalue);
        // second derivative
        WritableRaster sxxData = FluidUtils.createDoubleWritableRaster(width, height, null, null, doubleNovalue);
        WritableRaster syyData = FluidUtils.createDoubleWritableRaster(width, height, null, null, doubleNovalue);
        WritableRaster sxyData = FluidUtils.createDoubleWritableRaster(width, height, null, null, doubleNovalue);
        double plan = 0.0;
        double tang = 0.0;
        double prof = 0.0;

        RandomIter elevationIterator = RandomIterFactory.create(elevationImage, null);

        // calculate ...

        /*------------------------------------first derivative ----------------------------------------------*/

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating first derivates...", width - 2);
        // calculate the firs order derivative
        for( int x = 1; x < width - 1; x++ ) {
            for( int y = 1; y < height - 1; y++ ) {

                if (isNovalue(elevationIterator.getSampleDouble(x, y, 0))) {
                    sxData.setSample(x, y, 0, doubleNovalue);
                    syData.setSample(x, y, 0, doubleNovalue);
                } else {
                    sxData.setSample(x, y, 0, 0.5
                            * (elevationIterator.getSampleDouble(x, y + 1, 0) - elevationIterator.getSampleDouble(x, y - 1, 0))
                            / xRes);
                    syData.setSample(x, y, 0, 0.5
                            * (elevationIterator.getSampleDouble(x + 1, y, 0) - elevationIterator.getSampleDouble(x - 1, y, 0))
                            / yRes);
                }
            }
            pm.worked(1);
        }
        pm.done();

        /*-------------------------------------second derivative----------------------------------------*/

        pm.beginTask("Calculating second derivates...", height - 2);
        double disXX = Math.pow(xRes, 2.0);
        double disYY = Math.pow(yRes, 2.0);
        // calculate the second order derivative
        for( int j = 1; j < height - 1; j++ ) {
            for( int i = 1; i < width - 1; i++ ) {
                if (isNovalue(elevationIterator.getSampleDouble(i, j, 0))) {
                    sxxData.setSample(i, j, 0, doubleNovalue);
                    syyData.setSample(i, j, 0, doubleNovalue);
                    sxyData.setSample(i, j, 0, doubleNovalue);

                } else {
                    sxxData
                            .setSample(i, j, 0,
                                    ((elevationIterator.getSampleDouble(i, j + 1, 0) - 2
                                            * elevationIterator.getSampleDouble(i, j, 0) + elevationIterator.getSampleDouble(i,
                                            j - 1, 0)) / disXX));
                    syyData
                            .setSample(i, j, 0,
                                    ((elevationIterator.getSampleDouble(i + 1, j, 0) - 2
                                            * elevationIterator.getSampleDouble(i, j, 0) + elevationIterator.getSampleDouble(
                                            i - 1, j, 0)) / disYY));
                    sxyData.setSample(i, j, 0, 0.25 * ((elevationIterator.getSampleDouble(i + 1, j + 1, 0)
                            - elevationIterator.getSampleDouble(i + 1, j - 1, 0)
                            - elevationIterator.getSampleDouble(i - 1, j + 1, 0) + elevationIterator.getSampleDouble(i - 1,
                            j - 1, 0)) / (xRes * yRes)));
                }
            }
            pm.worked(1);
        }
        pm.done();

        /*---------------------------------------curvatures---------------------------------------------*/

        double p, q;

        // calculate curvatures
        pm.beginTask("Calculating curvatures...", height - 2);
        for( int j = 1; j < height - 1; j++ ) {
            for( int i = 1; i < width - 1; i++ ) {
                if (isNovalue(elevationIterator.getSampleDouble(i, j, 0))) {
                    plan = doubleNovalue;
                    tang = doubleNovalue;
                    prof = doubleNovalue;

                } else {
                    double sxSample = sxData.getSampleDouble(i, j, 0);
                    double sySample = syData.getSampleDouble(i, j, 0);
                    p = Math.pow(sxSample, 2.0) + Math.pow(sySample, 2.0);
                    q = p + 1;
                    if (p == 0.0) {
                        plan = 0.0;
                        tang = 0.0;
                        prof = 0.0;

                    } else {

                        double sxxSample = sxxData.getSampleDouble(i, j, 0);
                        double sxySample = sxyData.getSampleDouble(i, j, 0);
                        double syySample = syyData.getSampleDouble(i, j, 0);
                        plan = (sxxSample * Math.pow(sySample, 2.0) - 2 * sxySample * sxSample * sySample + syySample
                                * Math.pow(sxSample, 2.0))
                                / (Math.pow(p, 1.5));
                        tang = (sxxSample * Math.pow(sySample, 2.0) - 2 * sxySample * sxSample * sySample + syySample
                                * Math.pow(sxSample, 2.0))
                                / (p * Math.pow(q, 0.5));
                        prof = (sxxSample * Math.pow(sxSample, 2.0) + 2 * sxySample * sxSample * sySample + syySample
                                * Math.pow(sySample, 2.0))
                                / (p * Math.pow(q, 1.5));
                    }

                }
                profImage.setSample(i, j, 0, prof);
                tangImage.setSample(i, j, 0, tang);
                planImage.setSample(i, j, 0, plan);
            }
            pm.worked(1);
        }
        pm.done();

    }
}
