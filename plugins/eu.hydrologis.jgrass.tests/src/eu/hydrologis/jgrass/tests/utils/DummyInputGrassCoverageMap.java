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

import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;

import javax.media.jai.TiledImage;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
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

/**
 * Fake input reader for existing raster data.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class DummyInputGrassCoverageMap extends ModelsBackbone {

    /** ILink outLink field */
    private final HashMap<String, ILink> outputLinks = new HashMap<String, ILink>();

    private IOutputExchangeItem rasterMapOutputEI = null;
    private JGrassRegion activeRegion;
    private String locationPath;

    private String rasterMapName;

    private String mapset;

    private static final String modelParameters = "map=0";

    private String mapType = JGrassConstants.GRASSBINARYRASTERMAP;

    private double[][] rasterMap;

    public DummyInputGrassCoverageMap() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public DummyInputGrassCoverageMap( PrintStream output, PrintStream error, double[][] rasterMap ) {
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
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        /*
         * create the output exchange item that will be passed over the link to which the component
         * is link to other componentes
         */
        rasterMapOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        GridCoverageFactory gcf = CoverageFactoryFinder.getGridCoverageFactory(null);

        // Assume data array is in row-major order
        final int dataW = rasterMap[0].length;
        final int dataH = rasterMap.length;

        WritableRaster img = FluidUtils.createDoubleWritableRaster(dataW, dataH, null, null, null);
        
        for( int y = 0; y < dataH; y++ ) {
            for( int x = 0; x < dataW; x++ ) {
                double value = rasterMap[y][x];
                img.setSample(x, y, 0, value);
            }
        }

        // Set world coords as 1:1 with image coords for this example
        ReferencedEnvelope env = new ReferencedEnvelope(new Rectangle2D.Double(0, 0, dataW, dataH), null);

        GridCoverage2D gridCoverage2D = gcf.create("coverage", img, env);
        JGrassGridCoverageValueSet jgCVS = new JGrassGridCoverageValueSet(gridCoverage2D);
        
        return jgCVS;

    }

    public void removeLink( String linkID ) {
        outputLinks.remove(linkID);
    }

}
