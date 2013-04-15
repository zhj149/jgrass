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
package eu.hydrologis.jgrass.models.h.sumdownstream;



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
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the netnumbering model. It assign
 * numbers to the network's links and can be used by hillslope2channelattribute
 * to label the hillslope flowing into the link with the same number.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map containing the drainage directions (-flow);</LI>
 * <LI>the map containing the quantity to sum (-maptosum);
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map containing the summed quantities (-summ);</LI>
 * </OL>
 * <P></DD>
 * Usage h.sumdownstream --igrass-maptosum maptosum --igrass-flow flow
 * --ograss-summ summ
 * </p>
 * <p>
 * <DT><STRONG>Notes:</STRONG><BR>
 * </DT>
 * <DD>Including or excluding the final point does make the difference! (try for
 * example to sum the contributing areas: it can be proved also theoretically
 * that the scaling on the contributing areas is different).</DD>
 * <BR>
 * </OL> </DD>
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Andrea Cozzini, Antonello
 *         Andrea, Riccardo Rigon, (2004).
 */
public class h_sumdownstream extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String maptosumID = "maptosum";

    public final static String flowID = "flow";

    public final static String summID = "summ";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_sumdownstream.usage");

    private ILink maptosumLink = null;

    private ILink flowLink = null;

    private ILink summLink = null;

    private IOutputExchangeItem summDataOutputEI = null;

    private IInputExchangeItem maptosumDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private double number = 0;

    private boolean doTile;

    private String locationPath;

    private JGrassGridCoverageValueSet jgrValueSet;

    /** */
    public h_sumdownstream() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_sumdownstream

    /** */
    public h_sumdownstream( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_sumdownstream

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(maptosumID)) {
            maptosumLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(summID)) {
            summLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: pit, flow
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return maptosumDataInputEI;
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
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: summ
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return summDataOutputEI;
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
        if (linkID.equals(summLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D mapToSumGC = ModelsConstants.getGridCoverage2DFromLink(maptosumLink, time, err);
            
            PlanarImage flowImage =FluidUtils.setJaiNovalueBorder( (PlanarImage) flowGC.getRenderedImage());
            PlanarImage mapToSumImage = (PlanarImage) mapToSumGC.getRenderedImage();

            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            
            
            RandomIter mapToSumIter = RandomIterFactory.create(mapToSumImage, null);
            RandomIter flowIter = RandomIterFactory.create(flowImage, null);
            WritableRaster sumImage = FluidUtils.sum_downstream(flowIter, mapToSumIter, activeRegion.getCols(), activeRegion.getRows(), out);

            // the model
            if (sumImage == null) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(sumImage, activeRegion, crs);
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
                if (key.compareTo("number") == 0) {
                    number = Double.parseDouble(argument.getValue());
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
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.sumdownstream";
        componentId = null;

        /*
         * create the exchange items
         */
        // summ output

        summDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // maptosum input

        maptosumDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(maptosumLink.getID())) {
            maptosumLink = null;
        }
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(summLink.getID())) {
            summLink = null;
        }
    }

}
