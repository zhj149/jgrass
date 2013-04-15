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
package eu.hydrologis.jgrass.models.h.slope;

import java.io.File;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.operations.jai.slope.HMSlopeDescriptor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the slope model.
 * <p>
 * It estimates the slope in every site by employing the drainage directions. Differently from the
 * gradients, slope calculates the drop between each pixel and the adjacent points placed underneath
 * and it divides the result by the pixel length or by the length of the pixel diagonal, according
 * to the cases. The greatest value is the one chosen as slope.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of elevations (-pit);</LI>
 * <LI>the map of the drainage directions (-flow);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the slopes (-slope);</LI>
 * </OL>
 * <P></DD> Usage: h.slope --igrass-pit pit --igrass-flow flow --ograss-slope slope
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Cozzini Andrea, Rigon Riccardo
 */

public class h_slope extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit";

    public final static String flowID = "flow";

    public final static String slopeID = "slope";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_slope.usage");

    private ILink pitLink = null;

    private ILink flowLink = null;

    private ILink slopeLink = null;

    private IOutputExchangeItem slopeDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private RasterData pitData = null, flowData = null;

    private String readError = "ERROR! PROBLEM READING INPUT FILE";
    private String locationPath;
    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile;
    /** */
    public h_slope() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_slope

    /** */
    public h_slope( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_slope

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(slopeID)) {
            slopeLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {
        pitData = null;
        flowData = null;

    }

    /**
     * There is an IInputExchangeItem: pit, flow
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return pitDataInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return flowDataInputEI;
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
     * there is an IOutputExchangeItem: slope
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return slopeDataOutputEI;
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

        if (linkID.equals(slopeLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D pitData = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);
            GridCoverage2D flowData = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            PlanarImage pitRaster = (PlanarImage) pitData.getRenderedImage();
            PlanarImage flowRaster = (PlanarImage) flowData.getRenderedImage();
            if (pitRaster != null && flowRaster != null) {
                RenderedOp renderedOp = HMSlopeDescriptor.create(pitRaster, flowRaster, out, err, false, activeRegion
                        .getWEResolution(), activeRegion.getNSResolution());
                RenderedOp slope = FluidUtils.setJaiNovalueBorder(renderedOp);
                if (slope != null) {
                    CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                    jgrValueSet = new JGrassGridCoverageValueSet(slope, activeRegion, crs);
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
            }

        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);
        locationPath = grassDb + File.separator + location;
        componentDescr = "h.slope";
        componentId = null;

        /*
         * create the exchange items
         */
        // slope output

        slopeDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // pit input

        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
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
        if (linkID.equals(slopeLink.getID())) {
            slopeLink = null;
        }
    }

}
