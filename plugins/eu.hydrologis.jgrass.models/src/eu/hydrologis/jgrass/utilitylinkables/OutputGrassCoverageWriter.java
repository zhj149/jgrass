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
package eu.hydrologis.jgrass.utilitylinkables;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import nl.alterra.openmi.sdk.backbone.LinkableComponent;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IExchangeItem;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.io.GrassRasterWriter;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.GrassBinaryImageWriter;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.spi.GrassBinaryImageWriterSpi;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * Utility {@link LinkableComponent linkable component} for GRASS raster maps.
 * 
 * <p>
 * A linkable data object that is able to write a raster map to disk.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 * @see GrassRasterWriter
 * @see JGrassRegion
 * @see LinkableComponent
 */
public class OutputGrassCoverageWriter extends ModelsBackbone {

    /** 
     * The input link through which the raster to write is got. 
     */
    private ILink inputLink = null;

    /**
     * The {@link IExchangeItem exchange item} that will take care to ship
     * the {@link GridCoverage2D raster map} to write.
     */
    private IInputExchangeItem gridCoverageInputEI = null;

    /**
     * The {@link JGrassRegion active reading region} from which data are read.
     */
    private JGrassRegion activeRegion;

    /**
     * Absolute path to the GRASS Location.
     */
    private String locationPath;

    /**
     * Name of the Mapset.
     */
    private String mapsetName;

    /**
     * Name of the read raster map.
     */
    private String rasterMapName;

    /**
     * Absolute path to the read raster map.
     */
    private String rasterMapParentFolderPath;

    /**
     * Identifier of the outgoing link.
     */
    private static final String modelParameters = "map=0"; //$NON-NLS-1$

    /**
     * Constructs an {@link OutputGrassCoverageWriter} taking care to set
     * the output and error stream for logging to standard error and output.
     */
    public OutputGrassCoverageWriter() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    /**
     * Constructs an {@link OutputGrassCoverageWriter} with user supplied
     * error and output streams. If the passed streams are null, it defaults
     * back to standard error and output.
     *
     * @param output the {@link PrintStream} used to redirect output messages.
     * @param error the {@link PrintStream} used to redirect error messages.
     */
    public OutputGrassCoverageWriter( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public void addLink( ILink link ) {
        inputLink = link;
    }

    public void finish() {
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return gridCoverageInputEI;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 0;
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String grassDb = null;
        String location = null;
        for( IArgument argument : properties ) {
            String key = argument.getKey();

            if (key.equals("ograss")) {
                rasterMapName = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.GRASSDB) == 0) {
                grassDb = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.LOCATION) == 0) {
                location = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.MAPSET) == 0) {
                mapsetName = argument.getValue();
            }

        }

        locationPath = grassDb + File.separator + location;
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapsetName
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        rasterMapParentFolderPath = grassDb + File.separator + location + File.separator
                + mapsetName + File.separator + JGrassConstants.CELL + File.separator;

        /*
         * create the output exchange item that will be passed over the link to which the component
         * is link to other componentes
         */
        gridCoverageInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (inputLink == null) {
            err.println("Link not defined: " + linkID);
            throw new IllegalArgumentException("Link not defined: " + linkID);
        }
        /*
         * trigger the linked model
         */
        IValueSet valueSet = inputLink.getSourceComponent().getValues(time, inputLink.getID());
        GridCoverage2D gridCoverage2D = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();

        /*
         * write the data
         */
        // if DATE has been inserted, replace that with the timestamp
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
        String dateStr = time instanceof Date ? dateFormatter.format((Date) time) : dateFormatter
                .format(new Date());
        String newMapName = rasterMapName.replaceAll("DATE", dateStr);

        out.println("Writing map: " + newMapName); //$NON-NLS-1$
        GrassBinaryImageWriterSpi writerSpi = new GrassBinaryImageWriterSpi();
        GrassBinaryImageWriter writer = (GrassBinaryImageWriter) writerSpi.createWriterInstance();
        RenderedImage renderedImage = gridCoverage2D.view(ViewType.GEOPHYSICS).getRenderedImage();
        String rasterMapPath = rasterMapParentFolderPath + File.separator + newMapName;
        File file = new File(rasterMapPath);
        writer.setOutput(file);
        writer.write(renderedImage);

        if (JGrassPlugin.getDefault() != null) {
            JGrassMapGeoResource addedMap = JGrassCatalogUtilities.addMapToCatalog(locationPath,
                    mapsetName, newMapName, JGrassConstants.GRASSBINARYRASTERMAP);
            if (addedMap == null)
                return null;

            IMap activeMap = ApplicationGIS.getActiveMap();
            ApplicationGIS.addLayersToMap(activeMap, Collections
                    .singletonList((IGeoResource) addedMap), activeMap.getMapLayers().size());
        }

        return null;

    }
    public void removeLink( String linkID ) {
        inputLink = null;
    }

}
