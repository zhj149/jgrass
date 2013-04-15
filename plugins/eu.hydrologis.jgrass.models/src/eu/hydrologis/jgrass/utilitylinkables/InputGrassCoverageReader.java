/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.utilitylinkables;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;

import nl.alterra.openmi.sdk.backbone.LinkableComponent;
import nl.alterra.openmi.sdk.backbone.OutputExchangeItem;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IArgument;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IExchangeItem;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.io.GrassRasterReader;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReadParam;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReader;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * Utility {@link LinkableComponent linkable component} for GRASS raster maps.
 * 
 * <p>
 * A linkable data object that is able to supply data read from a raster map.
 * </p>
 * <p>
 * <b>NOTE: this object gives the possibility to have more output exchange 
 * items, but internally always the same data is returned.</b>
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 * @see GrassRasterReader
 * @see JGrassRegion
 * @see LinkableComponent
 */
public class InputGrassCoverageReader extends ModelsBackbone {

    /** 
     * The map that keeps track of linked objects. This is due to the fact that
     * more connections are accepted with just one {@link OutputExchangeItem}.  
     */
    private final HashMap<String, ILink> outputLinks = new HashMap<String, ILink>();

    /**
     * The {@link IExchangeItem exchange item} that will take care to ship
     * the read {@link GridCoverage2D raster map}.
     */
    private IOutputExchangeItem gridCoverageOutputEI = null;

    /**
     * The {@link GridCoverage2D grid coverage} created from the read raster map.
     */
    private GridCoverage2D gridCoverage2D = null;

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
    private String rasterMapPath;

    /**
     * Identifier of the outgoing link.
     */
    private static final String modelParameters = "map=0"; //$NON-NLS-1$


    /**
     * 
     */

    /**
     * Constructs an {@link InputGrassCoverageReader} taking care to set the
     * output and error stream for logging to standard error and output.
     */
    public InputGrassCoverageReader() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    /**
     * Constructs an {@link InputGrassCoverageReader} with user supplied
     * error and output streams. If the passed streams are null, it defaults
     * back to standard error and output.
     *
     * @param output the {@link PrintStream} used to redirect output messages.
     * @param error the {@link PrintStream} used to redirect error messages.
     */
    public InputGrassCoverageReader( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public void addLink( ILink link ) {
        if (!outputLinks.containsKey(link.getID())) {
            outputLinks.put(link.getID(), link);
        }

    }

    public void finish() {
    }

    public int getInputExchangeItemCount() {
        return 0;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return null;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return gridCoverageOutputEI;
    }

    /**
     * Input data can have infinite output exchange items of the same data
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String grassDb = null;
        String location = null;
        for( IArgument argument : properties ) {
            String key = argument.getKey();

            if (key.startsWith("igrass")) {
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
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapsetName + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        rasterMapPath = grassDb + File.separator + location + File.separator + mapsetName + File.separator + JGrassConstants.CELL
                + File.separator + rasterMapName;

        /*
         * create the output exchange item that will be passed over the link to which the component
         * is link to other componentes
         */
        gridCoverageOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (gridCoverage2D == null) {
            ILink link = outputLinks.get(linkID);

            /*
             * we read the region that the model asks us
             */
            JGrassRegion requestedWindow = null;
            IElementSet sourceElementSet = link.getTargetElementSet();
            if (sourceElementSet != null) {
                requestedWindow = ((JGrassElementset) sourceElementSet).getRegionWindow();
                gridCoverageOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, requestedWindow);
            }
            // if no region was asked, read all
            if (requestedWindow == null) {
                JGrassElementset jgElementSet = (JGrassElementset) gridCoverageOutputEI.getElementSet();
                requestedWindow = jgElementSet.getRegionWindow();
            }

            /*
             * read the data
             */
            GrassCoverageReader tmp = new GrassCoverageReader(null, null, true, false, new PrintStreamProgressMonitor(out));

            tmp.setInput(new File(rasterMapPath));
            /*
             * calculate the dimension of the tile.
             */
            GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(requestedWindow);
            /*
             *  I set to false this parameter in order to read without the tiling.
             */
            gridCoverage2D = tmp.read(gcReadParam);

        }
        return new JGrassGridCoverageValueSet(gridCoverage2D);

    }

    public void removeLink( String linkID ) {
        outputLinks.remove(linkID);
    }

}
