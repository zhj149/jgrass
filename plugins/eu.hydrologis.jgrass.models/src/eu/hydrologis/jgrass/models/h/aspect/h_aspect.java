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
package eu.hydrologis.jgrass.models.h.aspect;

import java.io.File;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.operations.jai.aspect.HMAspectDescriptor;
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.StringSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the aspect model. Generates raster map layers of aspect
 * from a raster map layer of true elevation values. The value of aspect is calculated
 * counterclockwise from north.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <OL>
 * <LI>the depitted map (-pit)</LI>
 * </OL>
 * <P>
 * </DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map with the aspect (-aspect)</LI>
 * </OL>
 * <P></DD>
 * </p>
 * <p>
 * Usage: h.aspect --igrass-pit pit --ograss-aspect aspect
 * </p>
 * <p>
 * With color table: h.aspect --igrass-pit pit --ograss-aspect aspect --ocolor-color aspect
 * </p>
 * <p>
 * Note: Due to the difficult existing calculating the aspect on the borders of the region, in this
 * cases the direction of the gradient is assumed to be the maximum slope gradient.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini Andrea, Franceschi
 *         Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_aspect extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit"; //$NON-NLS-1$

    public final static String aspectID = "aspect"; //$NON-NLS-1$

    public final static String colorID = "color"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_aspect.usage");

    private ILink pitLink = null;

    private ILink aspectLink = null;

    private ILink colorLink = null;

    private IOutputExchangeItem aspectDataOutputEI = null;

    private IOutputExchangeItem colorDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private RasterData pitData = null, aspectData = null;

    private double radtodeg = 360.0 / (2 * Math.PI);

    private String locationPath;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile = false;

    public h_aspect() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_aspect( PrintStream output, PrintStream error ) {
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
        if (id.equals(aspectID)) {
            aspectLink = link;
        }
        if (id.equals(colorID)) {
            colorLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {
        pitData = null;
        aspectData = null;
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
     * there is an IOutputExchangeItem: aspect
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return aspectDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return colorDataOutputEI;
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
        if (linkID.equals(aspectLink.getID())) {
            // don't do things twice

            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D pitData = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);
            
            // set it to true in order to test the tiling
            double dx = activeRegion.getWEResolution();
            double dy = activeRegion.getNSResolution();
            PlanarImage pitImage = (PlanarImage) pitData.view(ViewType.GEOPHYSICS).getRenderedImage();
            RenderedOp aspect = HMAspectDescriptor.create(pitImage, dx, dy, err, out, doTile);
            RenderedOp aspectNoV = FluidUtils.setJaiNovalueBorder(aspect);
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            jgrValueSet = new JGrassGridCoverageValueSet(aspectNoV, activeRegion, crs);
            return jgrValueSet;
        }
        if (linkID.equals(colorLink.getID())) {
            String[] st = new String[3];
            st[0] = "% 0.0   360.0"; //$NON-NLS-1$
            st[1] = "0.:255:255:255 180:0:0:0"; //$NON-NLS-1$
            st[2] = "180:0:0:0 360.:255:255:255"; //$NON-NLS-1$
            StringSet jgrValueSet = new StringSet(st);
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
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    try {
                        doTile = Boolean.parseBoolean(argument.getValue());
                        throw new IllegalArgumentException();
                    } catch (IllegalArgumentException e) {
                        out.println(Messages.getString("ModelsBackbone.invalidDOTile"));
                        doTile = false;
                        e.printStackTrace();
                    }
                    // } finally {
                    // doTile = false;
                    // }

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

        componentDescr = "h.aspect"; //$NON-NLS-1$
        componentId = null;

        /*
         * create the exchange items
         */
        // aspect output

        aspectDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // color map output
        IElementSet colorMapElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW,
                activeRegion, null);
        IQuantity colorMapQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.COLORMAP,
                ModelsConstants.UNITID_COLORMAP);
        colorDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, colorMapQuantity,
                colorMapElementSet);

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
        if (linkID.equals(aspectLink.getID())) {
            aspectLink = null;
        }
        if (linkID.equals(colorLink.getID())) {
            colorLink = null;
        }
    }

}
