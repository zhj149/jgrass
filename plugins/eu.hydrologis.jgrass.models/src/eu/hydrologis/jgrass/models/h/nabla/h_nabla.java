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
package eu.hydrologis.jgrass.models.h.nabla;

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

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.operations.jai.nabla.HMNablaDescriptor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the nabla model.
 * </p>
 * <p>
 * It estimates, for each site, the Laplace operator of the quantity given in input, with a scheme
 * at the finite differences.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of elevations (-pit);</LI>
 * <LI>the choice between the calculation of the real value or of the classes</LI>
 * <LI>if we choose the second option, we must specify the threshold to define planarity.</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>file containing the matrix of the Laplace operator, or the topographic classes (-nabla);</LI>
 * </OL>
 * <P></DD> Usage mode 0:\n h.nabla --mode 0 --igrass-pit pit --ograss-nabla nabla --threshold
 * threshold
 * </p>
 * <p>
 * Usage mode 1:\n h.nabla --mode 1 --igrass-pit pit --ograss-nabla nabla
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini Andrea, Franceschi
 *         Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_nabla extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit";

    public final static String nablaID = "nabla";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_nabla.usage");

    private ILink pitLink = null;

    private ILink nablaLink = null;

    private IOutputExchangeItem nablaDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private double thNabla = 0;

    private int mode = 0;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile = false;

    private String locationPath;


    public h_nabla() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_nabla( PrintStream output, PrintStream error ) {
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
        if (id.equals(nablaID)) {
            nablaLink = link;
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
     * there is an IOutputExchangeItem: nabla
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return nablaDataOutputEI;
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
        if (linkID.equals(nablaLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D pitData = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);
            // extract the resolution which are parameters for the gradient operation
            double dx = activeRegion.getWEResolution();
            double dy = activeRegion.getNSResolution();
            // extract, as an Image, the map of elevation
            RenderedImage pitImage = pitData.getRenderedImage();
            // gradient operation, input: map, x resolution, y resolution and if set the tiling.
            RenderedOp nabla = HMNablaDescriptor.create(pitImage, dx, dy, err, out, mode, thNabla, doTile);
            // set the border to noValue
            RenderedOp nablaNoValueBorder = FluidUtils.setJaiNovalueBorder(nabla);
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            jgrValueSet = new JGrassGridCoverageValueSet(nablaNoValueBorder, activeRegion, crs);
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
                if (key.compareTo("threshold") == 0) {
                    thNabla = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("mode") == 0) {
                    mode = Integer.parseInt(argument.getValue());
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
        String activeRegionPath = locationPath + File.separator + mapset   + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.nabla";
        componentId = null;

        /*
         * create the exchange items
         */
        // nabla output

        nablaDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
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
        if (linkID.equals(nablaLink.getID())) {
            nablaLink = null;
        }
    }

}
