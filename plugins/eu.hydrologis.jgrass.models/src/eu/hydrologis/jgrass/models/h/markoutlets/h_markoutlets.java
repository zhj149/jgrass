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
package eu.hydrologis.jgrass.models.h.markoutlets;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;

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
 * The openmi compliant representation of the markoutlets model. It marks all
 * the outlets of the considered region on the drainage directions map.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map of the drainage directions to modify (-flow)</LI>
 * </OL>
 * </DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map of the data assigned in input with the outlets set equal to 10
 * (-mflow)</LI>
 * </OL>
 * <P></DD> Usage: h.markoutlets --igrass-flow flow --ograss-mflow mflow
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini
 *         Andrea, Franceschi Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_markoutlets extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String mflowID = "mflow";

    public final static String flowID = "flow";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_markoutlets.usage");

    private static final double FLOW_NO_VALUE = JGrassConstants.doubleNovalue;

    private ILink mflowLink = null;

    private ILink flowLink = null;

    private IOutputExchangeItem mflowDataOutputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile = false;

    private String locationPath;

    public h_markoutlets() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_markoutlets( PrintStream output, PrintStream error ) {
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
        if (id.equals(mflowID)) {
            mflowLink = link;
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
     * there is an IOutputExchangeItem: mflow
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return mflowDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(mflowLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowData = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            WritableRaster flowRaster = FluidUtils.createFromRenderedImage(flowData.getRenderedImage());
            FluidUtils.setJAInoValueBorderIT(flowRaster);

            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

            int rows = activeRegion.getRows();
            int cols = activeRegion.getCols();

            RandomIter fileRandomIter = RandomIterFactory.create(flowRaster, null);

            WritableRaster markOutImage = markoutlets(fileRandomIter, rows, cols);
            jgrValueSet = new JGrassGridCoverageValueSet(markOutImage, activeRegion, crs);
            return jgrValueSet;
        }
        return null;
    }

    /**
     * in this method map's properties are defined... location, mapset... and
     * than IInputExchangeItem and IOutputExchangeItem are reated
     */
    public void safeInitialize( IArgument[] properties ) throws Exception {
        String unitId = "raster";

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

        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.matkoutlets";
        componentId = null;

        /*
         * create the exchange items
         */
        // dummy element set
        // mflow output

        mflowDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

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
        if (linkID.equals(mflowLink.getID())) {
            mflowLink = null;
        }
    }

    private WritableRaster markoutlets( RandomIter fileRandomIter, int rows, int cols ) {
        // setting novalue border...
        return mark_outlets(fileRandomIter, rows, cols);
    }

    /**
     * MARK_OUTLETS marks all the outlets of the considered region on the
     * drainage directions map. A convention has been adopted by which the the
     * preexisting drainage direction value of every outlet is changed with the
     * value 10.
     * 
     * @param flowData
     * @param copt
     */
    public WritableRaster mark_outlets( RandomIter flowRandomIter, int rows, int cols ) {
        /* char ch */
        int[] punto = new int[2];
        int[] oldpunto = new int[2];

        WritableRaster markOutImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, null);
        WritableRandomIter markOutRandomIter = RandomIterFactory.createWritable(markOutImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.markoutlets...", 2 * rows);
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                double value = flowRandomIter.getSampleDouble(j, i, 0);
                if (!isNovalue(value)) {
                    markOutRandomIter.setSample(j, i, 0, value);
                } else {
                    markOutRandomIter.setSample(j, i, 0, FLOW_NO_VALUE);
                }

            }
            pm.worked(1);
        }
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                punto[0] = j;
                punto[1] = i;
                double flowSample = flowRandomIter.getSampleDouble(punto[0], punto[1], 0);
                if (FluidUtils.sourcesqJAI(flowRandomIter, punto)) {
                    oldpunto[0] = punto[0];
                    oldpunto[1] = punto[1];

                    while( flowSample < 9.0 && (!isNovalue(flowSample)) ) {
                        oldpunto[0] = punto[0];
                        oldpunto[1] = punto[1];
                        if (!FluidUtils.go_downstream(punto, flowSample)) {
                            return null;
                        }
                        flowSample = flowRandomIter.getSampleDouble(punto[0], punto[1], 0);

                    }
                    if (flowSample != 10.0)
                        markOutRandomIter.setSample(oldpunto[0], oldpunto[1], 0, 10.0);
                }
            }
            pm.worked(1);
        }
        pm.done();
        return markOutImage;
    }

}
