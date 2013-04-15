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
package eu.hydrologis.jgrass.tests.utils;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;

import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.map.JGrassRasterData;
import eu.hydrologis.jgrass.libs.map.JGrassRasterMapReader;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassRasterValueSet;

/**
 * Fake input reader for existing raster data.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class DummyInputGrassRasterMap extends ModelsBackbone {

    /** ILink outLink field */
    private final HashMap<String, ILink> outputLinks = new HashMap<String, ILink>();

    private IOutputExchangeItem rasterMapOutputEI = null;
    private RasterData rasterData = null;
    private JGrassRegion activeRegion;
    private JGrassRasterMapReader jgrassMapReader = null;

    private String locationPath;

    private String rasterMapName;

    private String mapset;

    private static final String modelParameters = "map=0";

    private String mapType = JGrassConstants.GRASSBINARYRASTERMAP;

    private double[][] rasterMap;

    public DummyInputGrassRasterMap() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public DummyInputGrassRasterMap( PrintStream output, PrintStream error, double[][] rasterMap ) {
        super();
        this.rasterMap = rasterMap;
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
        return rasterMapOutputEI;
    }

    /**
     * Input data can have infinite output exchange items of the same data
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String unitId = "raster";
        String grassDb = null;
        String location = null;
        for( IArgument argument : properties ) {
            String key = argument.getKey();

            if (key.equals("igrass")) {
                rasterMapName = argument.getValue();
            }
            if (key.equals("igrassascii")) {
                rasterMapName = argument.getValue();
                mapType = JGrassConstants.GRASSASCIIRASTERMAP;
            }
            if (key.equals("iesrigrid")) {
                rasterMapName = argument.getValue();
                mapType = JGrassConstants.ESRIRASTERMAP;
            }
            if (key.equals("ifluidturtle")) {
                rasterMapName = argument.getValue();
                mapType = JGrassConstants.FTRASTERMAP;
            }
            if (key.compareTo("grassdb") == 0) {
                grassDb = argument.getValue();
            }
            if (key.compareTo("location") == 0) {
                location = argument.getValue();
            }
            if (key.compareTo("mapset") == 0) {
                mapset = argument.getValue();
            }

        }

        componentId = mapType;
        componentDescr = componentId;

        locationPath = grassDb + File.separator + location;
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        /*
         * create the output exchange item that will be passed over the link to which the component
         * is link to other componentes
         */
        rasterMapOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (rasterData == null) {
            rasterData = new JGrassRasterData(rasterMap);
        }
        return new JGrassRasterValueSet(rasterData);
    }

    public void removeLink( String linkID ) {
        outputLinks.remove(linkID);
    }

}
