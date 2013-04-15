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
import org.geotools.gce.geotiff.GeoTiffReader;
import org.openmi.standard.IArgument;
import org.openmi.standard.IExchangeItem;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * Utility {@link LinkableComponent linkable component} for tiffs.
 * 
 * <p>
 * A linkable data object that is able to supply data read from a tiff map.
 * </p>
 * <p>
 * <b>NOTE: this object gives the possibility to have more output exchange 
 * items, but internally always the same data is returned.</b>
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 * @see LinkableComponent
 */
public class InputTiffCoverageReader extends ModelsBackbone {

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
     * The raster map.
     */
    private File rasterMap;

    /**
     * Identifier of the outgoing link.
     */
    private static final String modelParameters = "map=0"; //$NON-NLS-1$

    /**
     * Constructs an {@link InputTiffCoverageReader} taking care to set
     * the output and error stream for logging to standard error and output.
     */
    public InputTiffCoverageReader() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    /**
     * Constructs an {@link InputTiffCoverageReader} with user supplied
     * error and output streams. If the passed streams are null, it defaults
     * back to standard error and output.
     *
     * @param output the {@link PrintStream} used to redirect output messages.
     * @param error the {@link PrintStream} used to redirect error messages.
     */
    public InputTiffCoverageReader( PrintStream output, PrintStream error ) {
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
        for( IArgument argument : properties ) {
            String key = argument.getKey();

            if (key.startsWith("itiff")) {
                String map = argument.getValue();
                rasterMap = new File(map);
                if (!rasterMap.exists()) {
                    throw new ModelsIllegalargumentException(
                            "The supplied tiff file doesn't exist: " + map, this);
                }
            }
        }

        /*
         * create the output exchange item that will be passed over the link to which the component
         * is link to other componentes
         */
        gridCoverageOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, null);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (gridCoverage2D == null) {

            GeoTiffReader reader = new GeoTiffReader(rasterMap);
            gridCoverage2D = (GridCoverage2D) reader.read(null);
        }
        return new JGrassGridCoverageValueSet(gridCoverage2D);
    }

    public void removeLink( String linkID ) {
        outputLinks.remove(linkID);
    }

}
