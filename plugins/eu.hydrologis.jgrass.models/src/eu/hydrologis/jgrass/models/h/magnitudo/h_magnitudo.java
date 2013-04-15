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
package eu.hydrologis.jgrass.models.h.magnitudo;

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
 * The openmi compliant representation of the magnitudo model. It calculates the
 * magnitude of a basin, defined as the number of sources upriver with respect
 * to every point. If the river net is a trifurcated tree (a node in which three
 * channels enter and one exits), then between number of springs and channels
 * there exists a bijective correspondence
 * </p>
 * <p>
 * h_{c}=2n_{s}-1
 * </p>
 * <p>
 * where is the number of channels and the number of sources; the magnitude is
 * then also an indicator of the contributing area.
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
 * <LI>the map of the basin magnitude (-magnitudo);</LI>
 * </OL>
 * <P></DD>
 * Usage: h.magnitudo --igrass-flow flow --ograss-magnitudo magnitudo
 * </p>
 * <p>
 * Note: Due to the difficult existing calculating the aspect on the borders of
 * the region, in this cases the direction of the gradient is assumed to be the
 * maximum slope gradient.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_magnitudo extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String magnitudoID = "magnitudo";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_magnitudo.usage");

    private ILink flowLink = null;

    private ILink magnitudoLink = null;

    private IOutputExchangeItem magnitudoDataOutputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private String locationPath;

    private JGrassGridCoverageValueSet jgrValueSet;

    public h_magnitudo() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_magnitudo( PrintStream output, PrintStream error ) {
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
        if (id.equals(magnitudoID)) {
            magnitudoLink = link;
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
     * there is an IOutputExchangeItem: magnitudo
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return magnitudoDataOutputEI;
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
        if (linkID.equals(magnitudoLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }
            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            PlanarImage flowImage = FluidUtils.setJaiNovalueBorder((PlanarImage) flowGC.getRenderedImage());
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            RandomIter flowIter = RandomIterFactory.create(flowImage, null);
            WritableRaster magnitudoImage = magnitudo(flowIter, activeRegion.getCols(), activeRegion.getRows(), out);
            if (magnitudoImage == null) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(magnitudoImage, activeRegion, crs);
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
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.magnitudo";
        componentId = null;

        /*
         * create the exchange items
         */
        // magnitudo output

        magnitudoDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

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
        if (linkID.equals(magnitudoLink.getID())) {
            magnitudoLink = null;
        }
    }

    /**
     * calculates the magnitudo of a basin
     * 
     * @param flowImage
     *            the map of flow directions
     * 
     * @param progressBar
     * @return the map with magnitudo value
     */
    public WritableRaster magnitudo( RandomIter flowRandomIter, int width, int height, PrintStream out ) {
        int[] flow = new int[2];
        // get rows and cols from the active region
        int cols = width;
        int rows = height;
        // setting novalue border...
        // FluidUtils.setJaiNovalueBorder(flowImage);
        WritableRaster magnitudoImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, null);
        WritableRandomIter magnitudoRandomIter = RandomIterFactory.createWritable(magnitudoImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.magnitudo...", 2 * rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                // looks for the source
                if (FluidUtils.sourcesqJAI(flowRandomIter, flow)) {
                    magnitudoRandomIter.setSample(flow[0], flow[1], 0,
                            magnitudoRandomIter.getSampleDouble(flow[0], flow[1], 0) + 1.0);
                    if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                        return null;
                    while( !isNovalue(flowRandomIter.getSampleDouble(flow[0], flow[1], 0))
                            && flowRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10 ) {
                        magnitudoRandomIter.setSample(flow[0], flow[1], 0, magnitudoRandomIter.getSampleDouble(flow[0], flow[1],
                                0) + 1.0);
                        if (!FluidUtils.go_downstream(flow, flowRandomIter.getSampleDouble(flow[0], flow[1], 0)))
                            return null;
                    }

                    if (flowRandomIter.getSampleDouble(flow[0], flow[1], 0) == 10) {
                        magnitudoRandomIter.setSample(flow[0], flow[1], 0, magnitudoRandomIter.getSampleDouble(flow[0], flow[1],
                                0) + 1.0);
                    }
                }
            }
            pm.worked(1);
        }
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (magnitudoRandomIter.getSampleDouble(i, j, 0) == 0.0 && flowRandomIter.getSampleDouble(i, j, 0) == 10.0) {
                    magnitudoRandomIter.setSample(i, j, 0, 1.0);
                } else if (magnitudoRandomIter.getSampleDouble(i, j, 0) == 0.0
                        && isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    magnitudoRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();
        // calculates...
        return magnitudoImage;
    }

}
