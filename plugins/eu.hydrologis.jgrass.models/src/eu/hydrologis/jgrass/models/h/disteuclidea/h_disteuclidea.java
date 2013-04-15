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
package eu.hydrologis.jgrass.models.h.disteuclidea;

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
 * The openmi compliant representation of the disteuclidea model. It calculates the euclidean
 * distance of each pixel from the outlet of the bigger basin which contains it.
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
 * <LI>the file containing the matrix of the dist_euclidea (-dist);</LI>
 * </OL>
 * <P></DD> Usage: h.disteuclidea --igrass-flow flow --ograss-dist dist
 * </p>
 * <p>
 * Note: The program is a trivial application of the Pythagoras theorem formed by the plane
 * Cartesian axes with the line joining the pixel in question and the outlet. <BR>
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Rigon Riccardo
 */
public class h_disteuclidea extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String distID = "dist";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_disteuclidea.usage"); //$NON-NLS-1$

    private ILink flowLink = null;

    private ILink distLink = null;

    private IOutputExchangeItem distDataOutputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private RandomIter fileRandomIter;

    private WritableRandomIter distToOutRandomIter;
    private String locationPath;
    private boolean doTile = false;
    private JGrassGridCoverageValueSet jgrValueSet;

    private CoordinateReferenceSystem crs;

    public h_disteuclidea() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_disteuclidea( PrintStream output, PrintStream error ) {
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
        if (id.equals(distID)) {
            distLink = link;
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
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: D2O
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return distDataOutputEI;
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
        if (linkID.equals(distLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowData = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            PlanarImage flowImage = FluidUtils.setJaiNovalueBorder((PlanarImage) flowData.getRenderedImage());
          
            WritableRaster distToOutImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(),
                    null, null, null);
            distToOutRandomIter = RandomIterFactory.createWritable(distToOutImage, null);
            fileRandomIter = RandomIterFactory.create(flowImage, null);
            if (!disteuclidea()) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(distToOutImage, activeRegion, crs);
                return jgrValueSet;
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
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.disteucildea";
        componentId = null;

        /*
         * create the exchange items
         */
        // dist output

        distDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(distLink.getID())) {
            distLink = null;
        }
    }

    /**
     * Calculates the distance to outlets in every pixel of the map
     * 
     * @return
     */
    private boolean disteuclidea() {
        int out_i = 0, out_j = 0;

        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        double x = 0.0, y = 0.0;

        // get resolution of the active region
        double dx = activeRegion.getWEResolution();
        double dy = activeRegion.getNSResolution();

        // first step individuates the outlets (10 in flow matrix)
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "h.disteuclidea... ", 2 * rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                // coordinate of outlets (out_i,out_j)...
                if (fileRandomIter.getSampleDouble(i, j, 0) == 10) {
                    out_i = i;
                    out_j = j;
                }
            }
            pm.worked(1);
        }
        // second step calculates the distance...
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                // coordinate of outlets...
                if (!isNovalue(fileRandomIter.getSampleDouble(i, j, 0))) {
                    // calculates for every pixel the distance from the outlets
                    // in x and y...
                    x = Math.abs(i - out_i) * dx;
                    y = Math.abs(j - out_j) * dy;
                    distToOutRandomIter.setSample(i, j, 0, Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
                } else
                    distToOutRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
            }
            pm.worked(1);
        }
        pm.done();
        return true;
    }
}
