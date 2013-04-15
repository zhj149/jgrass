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
package eu.hydrologis.jgrass.models.h.d2o;

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
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the d2o (Distance To Outlet) model.
 * Distance to outlet calculates the projection on the plane of the distance of
 * each pixel from the outlet, measured along the drainage directions. By
 * aggregating the matrix so obtained, we get the so called width function. The
 * program can work in two different ways: it can calculate the distance from
 * the outlet either in pixel number (0:topological distance mode), or in meters
 * (1:simple distance mode).
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the drainage directions (-flow);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the distances to outlet (-d2o);</LI>
 * </OL>
 * <P></DD> Usage: h.d2o --mode mode --igrass-flow flow --ograss-d2o d2o
 * </p>
 * <p>
 * Note: The distance is estimated by following the path joining each pixel with
 * the outlet following the drainage directions. In the topological mode, the
 * distance is measured in pixel number and without distinguishing between
 * directions parallel to the coordinates and diagonal directions. In the simple
 * mode, the distance is obtained in meters and oblique directions (D8 flow is
 * assumed) are calculated applying the Pithagorean theorem.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Rigon Riccardo
 */
public class h_d2o extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow"; //$NON-NLS-1$

    public final static String d2oID = "d2o"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_d2o.usage"); //$NON-NLS-1$

    private static final double NaN = JGrassConstants.doubleNovalue;

    private ILink flowLink = null;

    private ILink d2oLink = null;

    private IOutputExchangeItem d2oDataOutputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile;

    private String locationPath;

    private int mode = 0;

    public h_d2o() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_d2o( PrintStream output, PrintStream error ) {
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
        if (id.equals(d2oID)) {
            d2oLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: flow
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
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
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: D2O
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return d2oDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(d2oLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowData = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            PlanarImage flowImage = (PlanarImage) flowData.getRenderedImage();

            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            WritableRaster distToOutImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(),
                    null, null, null);

            if (!D2O(flowImage, distToOutImage)) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(distToOutImage, activeRegion, crs);
                return jgrValueSet;

            }
        }
        return null;
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
                if (key.compareTo("mode") == 0) {
                    try {
                        mode = Integer.parseInt(argument.getValue());
                    } catch (Exception e) {
                        err.println("Couldn't understand the supplied mode parameter. Falling back to default...");
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

        componentDescr = "h.d2o"; //$NON-NLS-1$
        componentId = null;

        /*
         * create the exchange items
         */
        // d2o output
        IElementSet d2oElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity d2oQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        d2oDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, d2oQuantity, d2oElementSet);

        // element set defining what we want to read
        // flow input
        IElementSet flowElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity flowQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        flowDataInputEI = UtilitiesFacade.createInputExchangeItem(this, flowQuantity, flowElementSet);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(d2oLink.getID())) {
            d2oLink = null;
        }
    }

    /**
     * Calculates the distance to outlets in every pixel of the map
     * 
     * @return
     */
    private boolean D2O( RenderedImage flowTmpImage, WritableRaster distToOutImage ) {
        // get resolution of the active region
        double dx = activeRegion.getWEResolution();
        double dy = activeRegion.getNSResolution();

        // FluidUtils.setJaiNovalueBorder();
        RenderedImage flowImage = FluidUtils.setJaiNovalueBorder(flowTmpImage);
        RandomIter flowIterator = RandomIterFactory.create(flowImage, null);
        if (mode == 1) {
            FluidUtils.outletdistance(flowIterator, distToOutImage, dx, dy, out);
        } else if (mode == 0) {
            FluidUtils.topological_outletdistance(flowIterator, distToOutImage, out);
        }
        return true;
    }

}
