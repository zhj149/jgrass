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
package eu.hydrologis.jgrass.models.h.diameters;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;
import static eu.hydrologis.libs.messages.MessageHelper.WORKING_ON;

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
 * The openmi compliant representation of the diametres model. It calculates the
 * diameter of the basin subtended to a point. This is the distance between the
 * basin outlet and the point on the boundary farest from it. The calculus is
 * repeated for each significant point contained in a DEM. There could be
 * alternative definitions of diameter (as for instance the distance between any
 * two points of a basin), here not considered.
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
 * <LI>the map of the diameters (-diameters);</LI>
 * <LI>it is necessary to choose if in the calculus only the
 * &#8242;source&#8242; points, or all points (as possible points belonging to
 * the sub-basins boundaries) have to be considered. This if effected by typing,
 * when requested, 0 or 1.</LI>
 * </OL>
 * <P></DD> Usage: h.diameters --mode mode (0/1) --igrass-flow flow
 * --ograss-diameters diameters
 * </p>
 * <p>
 * Note: Since the diameter is calculated for the basin subtended to every
 * point, the computation is quite slow.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Riccardo Rigon
 */
public class h_diameters extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow"; //$NON-NLS-1$

    public final static String diametresID = "diameters"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_diameters.usage"); //$NON-NLS-1$

    private ILink flowLink = null;

    private ILink diametersLink = null;

    private IOutputExchangeItem diametersDataOutputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private int mode = 0;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    private boolean doTile;

    public h_diameters() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_diameters( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(diametresID)) {
            diametersLink = link;
        }
    }

    public void finish() {

    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return diametersDataOutputEI;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(diametersLink.getID())) {

            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            PlanarImage flowImage = (PlanarImage) flowGC.getRenderedImage();
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            WritableRaster diametersImage = null;
            if (mode == 0) {
                diametersImage = diameters_a(flowImage);
            } else {
                diametersImage = diameters_b(flowImage);
            }

            if (diametersImage == null) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(diametersImage, activeRegion, crs);
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
                if (key.compareTo("mode") == 0) { //$NON-NLS-1$
                    mode = Integer.parseInt(argument.getValue());
                }
                if (key.compareTo("doTile") == 0) { //$NON-NLS-1$
                    doTile = Boolean.parseBoolean(argument.getValue());
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

        componentDescr = "h.diameters"; //$NON-NLS-1$
        componentId = null;

        /*
         * create the exchange items
         */
        diametersDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
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
        if (linkID.equals(diametersLink.getID())) {
            diametersLink = null;
        }
    }

    /**
     * Calculates the diameters in every pixel of the map
     * 
     * @return
     */
    private WritableRaster diameters_a( PlanarImage flowImage ) {
        int[] flow = new int[2];
        int[] origin = new int[2];

        int difx = 0, dify = 0;

        double diss = 0.0;
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);

        WritableRaster diametersRaster = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(),
                null, null, null);

        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        // get resolution of the active region
        double dx = activeRegion.getWEResolution();
        double dy = activeRegion.getNSResolution();

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "h.diameters...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    diametersRaster.setSample(i, j, 0, doubleNovalue);
                } else {
                    flow[0] = i;
                    flow[1] = j;
                    // looks for the source
                    if (FluidUtils.sourcesqJAI(flowRandomIter, flow)) {
                        origin[0] = flow[0];
                        origin[1] = flow[1];
                        while( !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0 ) {
                            difx = origin[0] - flow[0];
                            dify = origin[1] - flow[1];
                            diss = Math.sqrt(Math.pow(difx * dx, 2) + Math.pow(dify * dy, 2));
                            if (diametersRaster.getSampleDouble(flow[0], flow[1], 0) < diss)
                                diametersRaster.setSample(flow[0], flow[1], 0, diss);
                            {
                                if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                    return null;
                            }
                        }
                        difx = origin[0] - flow[0];
                        dify = origin[1] - flow[1];
                        diss = Math.sqrt(Math.pow(difx * dx, 2) + Math.pow(dify * dy, 2));
                        if (diametersRaster.getSampleDouble(flow[0], flow[1], 0) < diss)
                            diametersRaster.setSample(flow[0], flow[1], 0, diss);
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        return diametersRaster;
    }

    /**
     * Calculates the diameters in every pixel of the map
     * 
     * @return
     */
    private WritableRaster diameters_b( PlanarImage flowImage ) {
        int[] flow = new int[2];
        int[] origin = new int[2];

        int difx = 0, dify = 0;

        double diss = 0.0;
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        WritableRaster diametersRaster = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(),
                null, null, null);
        
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        // get resolution of the active region
        double dx = activeRegion.getWEResolution();
        double dy = activeRegion.getNSResolution();

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(WORKING_ON + "h.diameters...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (!isNovalue(flowRandomIter.getSampleDouble(i, j, 0)) && flowRandomIter.getSampleDouble(i, j, 0) != 10.0) {
                    flow[0] = i;
                    flow[1] = j;
                    origin[0] = flow[0];
                    origin[1] = flow[1];
                    while( !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0))
                            && flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0 ) {
                        difx = origin[0] - flow[0];
                        dify = origin[1] - flow[1];
                        diss = Math.sqrt(Math.pow(difx * dx, 2) + Math.pow(dify * dy, 2));
                        if (diametersRaster.getSampleDouble(flow[0], flow[1], 0) < diss)
                            diametersRaster.setSample(flow[0], flow[1], 0, diss);
                        {
                            if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                return null;
                        }
                    }
                    difx = origin[0] - flow[0];
                    dify = origin[1] - flow[1];
                    diss = Math.sqrt(Math.pow(difx * dx, 2) + Math.pow(dify * dy, 2));
                    if (diametersRaster.getSampleDouble(flow[0], flow[1], 0) < diss)
                        diametersRaster.setSample(flow[0], flow[1], 0, diss);
                } else
                    diametersRaster.setSample(i, j, 0, doubleNovalue);
            }
            pm.worked(1);
        }
        pm.done();
        return diametersRaster;
    }
}
