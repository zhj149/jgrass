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
package eu.hydrologis.jgrass.models.h.topindex;

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
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

public class h_topindex extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String tcaID = "tca";

    public final static String slopeID = "slope";

    public final static String topindexID = "topindex";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_topindex.usage");

    private ILink tcaLink = null;

    private ILink slopeLink = null;

    private ILink topindexLink = null;

    private IOutputExchangeItem topindexDataOutputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private IInputExchangeItem slopeDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    private boolean doTile;

    /** */
    public h_topindex() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_topindex

    /** */
    public h_topindex( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_topindex

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(tcaID)) {
            tcaLink = link;
        }
        if (id.equals(slopeID)) {
            slopeLink = link;
        }
        if (id.equals(topindexID)) {
            topindexLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: tca, slope
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return tcaDataInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return slopeDataInputEI;
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
     * there is an IOutputExchangeItem: topindex
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return topindexDataOutputEI;
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
        if (linkID.equals(topindexLink.getID())) {
            // reads input maps
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D tcaGC = ModelsConstants.getGridCoverage2DFromLink(tcaLink, time, err);
            GridCoverage2D slopeGC = ModelsConstants.getGridCoverage2DFromLink(slopeLink, time, err);

            PlanarImage tcaImage = (PlanarImage) tcaGC.getRenderedImage();
            PlanarImage slopeImage = (PlanarImage) slopeGC.getRenderedImage();

            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            WritableRaster topindexImage = topindex(tcaImage, slopeImage);

            // the model
            if (topindexImage == null) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(topindexImage, activeRegion, crs);
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
                    doTile = Boolean.getBoolean(argument.getValue());
                }
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;

        String activeRegionPath = locationPath + File.separator + mapset + File.separator + "WIND";
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.topindex";
        componentId = null;

        /*
         * create the exchange items
         */
        // topindex output

        topindexDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // tca input

        tcaDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // slope input

        slopeDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(tcaLink.getID())) {
            tcaLink = null;
        }
        if (linkID.equals(slopeLink.getID())) {
            slopeLink = null;
        }
        if (linkID.equals(topindexLink.getID())) {
            topindexLink = null;
        }
    }

    /**
     * returns the topindex in every pixel
     */
    private WritableRaster topindex( PlanarImage tcaImage, PlanarImage slopeImage ) {
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();
        RandomIter tcaRandomIter = RandomIterFactory.create(tcaImage, null);
        RandomIter slopeRandomIter = RandomIterFactory.create(slopeImage, null);
        // create new matrix
        WritableRaster topindexImage = FluidUtils.createDoubleWritableRaster(slopeImage.getWidth(), slopeImage.getHeight(), null,
                slopeImage.getSampleModel(), null);
        WritableRandomIter topindexRandomIter = RandomIterFactory.createWritable(topindexImage, null);
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating topindex...", rows);
        for( int j = 0; j < rows; j++ ) {
            for( int i = 0; i < cols; i++ ) {
                if (isNovalue(tcaRandomIter.getSampleDouble(i, j, 0))) {
                    topindexRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                } else {
                    if (slopeRandomIter.getSampleDouble(i, j, 0) != 0) {
                        topindexRandomIter.setSample(i, j, 0, Math.log(tcaRandomIter.getSampleDouble(i, j, 0)
                                / slopeRandomIter.getSampleDouble(i, j, 0)));
                    } else {
                        topindexRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        return topindexImage;
    }
}
