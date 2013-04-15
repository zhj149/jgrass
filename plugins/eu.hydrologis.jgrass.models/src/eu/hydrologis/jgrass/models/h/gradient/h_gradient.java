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
package eu.hydrologis.jgrass.models.h.gradient;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.PrintStream;

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
import eu.hydrologis.jgrass.operations.jai.gradient.HMGradientDescriptor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the gradient model. Calculates the
 * gradient in each point of the map,
 * </p>
 * <p>
 * It estimate the gradient with a finite difference formula:
 * 
 * <pre>
 *  p=&radic{f<sub>x</sub>&sup2;+f<sub>y</sub>&sup2;}
 * f<sub>x</sub>=(f(x+1,y)-f(x-1,y))/(2 &#916 x) 
 * f<sub>y</sub>=(f(x,y+1)-f(x,y-1))/(2 &#916 y)
 * </pre>
 * 
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the matrix of elevations (-pit);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>matrix of the gradients (-gradient);</LI>
 * </OL>
 * <P></DD> Usage: h.gradient --igrass-pit pit --ograss-gradient gradient
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_gradient extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit";

    public final static String gradientID = "gradient";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_gradient.usage"); //$NON-NLS-1$

    private ILink pitLink = null;

    private ILink gradientLink = null;

    private IOutputExchangeItem gradientDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private RasterData pitData = null, gradientData = null;

    private boolean doTile = false;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    public h_gradient() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_gradient( PrintStream output, PrintStream error ) {
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
        if (id.equals(gradientID)) {
            gradientLink = link;
        }
    }

    public void finish() {
        pitData = null;
        gradientData = null;
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
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: gradient
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return gradientDataOutputEI;
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
        if (linkID.equals(gradientLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D pitData = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);
            RenderedImage pitImage = pitData.getRenderedImage();
            // set it to true in order to test the tiling
            // extract the resolution which are parameters for the gradient
            // operation
            double dx = activeRegion.getWEResolution();
            double dy = activeRegion.getNSResolution();
            // extract, as an Image, the map of elevation
            // gradient operation, input: map, x resolution, y resolution and if
            // set the tiling.
            if (pitImage != null) {
                RenderedOp gradient = HMGradientDescriptor.create(pitImage, dx, dy, false, err, out);
                // set the border to noValue
                RenderedOp gradientNoV = FluidUtils.setJaiNovalueBorder(gradient);
                CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                jgrValueSet = new JGrassGridCoverageValueSet(gradientNoV, activeRegion, crs);
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
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.parseBoolean(argument.getValue());

                }
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;

        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.gradient";
        componentId = null;

        /*
         * create the exchange items
         */
        // gradient output
        gradientDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        // element set defining what we want to read
        // pit input

        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(gradientLink.getID())) {
            gradientLink = null;
        }
    }

}
