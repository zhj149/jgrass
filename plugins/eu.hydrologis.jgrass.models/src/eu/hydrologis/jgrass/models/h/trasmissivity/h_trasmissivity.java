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
package eu.hydrologis.jgrass.models.h.trasmissivity;

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
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.jai.ConstantRandomIter;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the trasmissivity model. It calculates the trasmissivity
 * of the basin.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of slope (-slopemap);</LI>
 * <LI>the map of condicibility (-conducibilitymap);
 * <LI>the map of (-hsmap);</LI> </LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of trasmissivity (-trasmissivitymap);</LI>
 * </OL>
 * <P></DD> Usage mode0: h.trasmissivity --mode 0 --igrass-slopemap slope --igrass-hsmap hs
 * --igrass-conducibilitymap conducibility --ograss-trasmissivitymap trasmissivity
 * </p>
 * <p>
 * Usage mode1: h.trasmissivity --mode 1 --igrass-slopemap slope --igrass-hsmap hs
 * --ograss-trasmissivitymap trasmissivity --conducibilityconst value
 * </p>
 * <p>
 * Usage mode2: h.trasmissivity --mode 2 --igrass-slopemap slope --igrass-conducibilitymap
 * conducibility --ograss-trasmissivitymap trasmissivity --hsconst value
 * </p>
 * <p>
 * Usage mode3: h.trasmissivity --mode 3 --igrass-slopemap slope --ograss-trasmissivitymap
 * trasmissivity --conducibilityconst value --hsconst value
 * </p>
 * <p>
 * Note: It is possible to use a map or a constant value for conducibility and hs.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 */
public class h_trasmissivity extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String slopeID = "slopemap";

    public final static String hsID = "hsmap";

    public final static String conducibilityID = "conducibilitymap";

    public final static String trasmissivityID = "trasmissivitymap";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_trasmissivity.usage");

    private ILink slopeLink = null;

    private ILink hsLink = null;

    private ILink conducibilityLink = null;

    private ILink trasmissivityLink = null;

    private IInputExchangeItem slopeDataInputEI = null;

    private IInputExchangeItem hsDataInputEI = null;

    private IInputExchangeItem conducibilityDataInputEI = null;

    private IOutputExchangeItem trasmissivityDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private double hsConst = -1.0;

    private double conducibilityConst = -1.0;

    private int mode = 0;

    private RandomIter conducibilityMapIterator = null;

    private RandomIter hsMapIterator = null;

    private WritableRandomIter trasmissivityRandomIter;

    private String locationPath;

    private PlanarImage slopeImage;

    public h_trasmissivity() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_trasmissivity( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(slopeID)) {
            slopeLink = link;
        }
        if (id.equals(hsID)) {
            hsLink = link;
        }
        if (id.equals(conducibilityID)) {
            conducibilityLink = link;
        }
        if (id.equals(trasmissivityID)) {
            trasmissivityLink = link;
        }
    }

    public void finish() {
    }

    /**
     * There is an IInputExchangeItem: slope, hs, conducibility
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return slopeDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return hsDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return conducibilityDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 3;
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: trasmissivity
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return trasmissivityDataOutputEI;
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
        if (linkID.equals(trasmissivityLink.getID())) {

            GridCoverage2D slopeGC = ModelsConstants.getGridCoverage2DFromLink(slopeLink, time, err);
            slopeImage = (PlanarImage) slopeGC.getRenderedImage();

            IValueSet hsValueSet = null;
            if (hsLink != null && (hsValueSet = hsLink.getSourceComponent().getValues(time, hsLink.getID())) != null) {
                GridCoverage2D hsData = ((JGrassGridCoverageValueSet) hsValueSet).getGridCoverage2D();
                hsMapIterator = RandomIterFactory.create(hsData.getRenderedImage(), null);
            }

            IValueSet conducibilityValueSet = null;
            if (conducibilityLink != null
                    && (conducibilityValueSet = conducibilityLink.getSourceComponent().getValues(time, conducibilityLink.getID())) != null) {
                GridCoverage2D conducibilityData = ((JGrassGridCoverageValueSet) conducibilityValueSet).getGridCoverage2D();
                conducibilityMapIterator = RandomIterFactory.create(conducibilityData.getRenderedImage(), null);
            }

            WritableRaster trasmissivityImage = FluidUtils.createDoubleWritableRaster(slopeImage.getWidth(), slopeImage
                    .getHeight(), null, slopeImage.getSampleModel(), JGrassConstants.doubleNovalue);

            trasmissivityRandomIter = RandomIterFactory.createWritable(trasmissivityImage, null);

            if (!trasmissivity()) {
                throw new ModelsIllegalargumentException("An error occurred in the trasmissivity module.", this);
            } else {
                CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                JGrassGridCoverageValueSet jgrValueSet = new JGrassGridCoverageValueSet(trasmissivityImage, activeRegion, crs);
                return jgrValueSet;
            }
        }
        return null;
    }

    /**
     * Calculates the trasmissivity in every pixel of the map
     * 
     * @return
     */
    private boolean trasmissivity() {
        // get rows and cols from the active region
        int cols = activeRegion.getCols();
        int rows = activeRegion.getRows();

        RandomIter slopeIterator = RandomIterFactory.create(slopeImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.trasmissivity...", rows);
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                double slope = slopeIterator.getSampleDouble(j, i, 0);
                double conduc = conducibilityMapIterator.getSampleDouble(j, i, 0);
                double hs = hsMapIterator.getSampleDouble(j, i, 0);
                if (!isNovalue(slope) && !isNovalue(conduc) && !isNovalue(hs)) {
                    double trasmissivity = conduc * hs * Math.cos(Math.atan(slope)) * 3600 * 24 / 1000;
                    trasmissivityRandomIter.setSample(j, i, 0, trasmissivity);
                } else {
                    trasmissivityRandomIter.setSample(j, i, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();
        return true;
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
                if (key.compareTo("hsconst") == 0) {
                    hsConst = Double.parseDouble(argument.getValue());
                    hsMapIterator = new ConstantRandomIter(hsConst);
                }
                if (key.compareTo("conducibilityconst") == 0) {
                    conducibilityConst = Double.parseDouble(argument.getValue());
                    conducibilityMapIterator = new ConstantRandomIter(conducibilityConst);
                }
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.trasmissivity";
        componentId = null;

        /*
         * create the exchange items
         */
        // trasmissivity output
        IElementSet trasmissivityElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity trasmissivityQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        trasmissivityDataOutputEI = UtilitiesFacade
                .createOutputExchangeItem(this, trasmissivityQuantity, trasmissivityElementSet);

        // element set defining what we want to read
        // slope input
        IElementSet slopeElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity slopeQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        slopeDataInputEI = UtilitiesFacade.createInputExchangeItem(this, slopeQuantity, slopeElementSet);

        // hs input
        IElementSet hsElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity hsQuantity = UtilitiesFacade
                .createScalarQuantity(ModelsConstants.GRASSRASTERMAP, ModelsConstants.UNITID_RASTER);
        hsDataInputEI = UtilitiesFacade.createInputExchangeItem(this, hsQuantity, hsElementSet);

        // conducibility input
        IElementSet conducibilityElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity conducibilityQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        conducibilityDataInputEI = UtilitiesFacade.createInputExchangeItem(this, conducibilityQuantity, conducibilityElementSet);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(slopeLink.getID())) {
            slopeLink = null;
        }
        if (linkID.equals(hsLink.getID())) {
            hsLink = null;
        }
        if (linkID.equals(conducibilityLink.getID())) {
            conducibilityLink = null;
        }
        if (trasmissivityID.equals(trasmissivityLink.getID())) {
            trasmissivityLink = null;
        }
    }

}
