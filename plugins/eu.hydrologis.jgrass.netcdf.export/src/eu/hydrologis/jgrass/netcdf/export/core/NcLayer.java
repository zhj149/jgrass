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
package eu.hydrologis.jgrass.netcdf.export.core;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReadParam;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReader;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.monitor.IProgressMonitorJGrass;

/**
 * Class representing a raster layer.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class NcLayer {
    private String name;
    private String[] rasterPaths;
    private String time;
    private Double level;
    private NcDataType dataType;

    /**
     * Constructs a grid layer.
     * 
     * @param rasterPaths the name of the raster from which to take data. 
     *          TODO if the rasters are 2, create a vector set.
     * @param time the timestep for this layer.
     * @param level the elevation for this layer.
     * @param dataType
     */
    public NcLayer( String[] rasterPaths, String time, Double level, NcDataType dataType ) {
        this.rasterPaths = rasterPaths;
        File f = new File(rasterPaths[0]);
        name = f.getName();
        this.time = time;
        this.level = level;
        this.dataType = dataType;
    }

    /**
     * Read data from a GRASS raster to be data of the layer.
     * 
     * @param pm a progress monitor.
     * @param activeRegion the {@link JGrassRegion region} from which the data are read. 
     * @param locationCrs the {@link CoordinateReferenceSystem} of the original GRASS data.
     * @return a {@link RandomIter} to iterate over the read data.
     * @throws IOException
     * @throws FactoryException
     * @throws TransformException
     */
    public RandomIter getData( IProgressMonitorJGrass pm, JGrassRegion activeRegion,
            CoordinateReferenceSystem locationCrs ) throws IOException, FactoryException,
            TransformException {
        File rasterFile = new File(rasterPaths[0]);

        // FIXME should be true to use rowcol for subsampling
        GrassCoverageReader tmp = new GrassCoverageReader(null, null, true, false, pm);
        tmp.setInput(rasterFile);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(activeRegion);

        GridCoverage2D gridCoverage2D = tmp.read(gcReadParam);
        
        
        GridCoverage2D gridCoverage2DLatlong = (GridCoverage2D) Operations.DEFAULT.resample(
                gridCoverage2D, DefaultGeographicCRS.WGS84);

        RenderedImage renderedImage = gridCoverage2DLatlong.getRenderedImage();
        RandomIter randomIter = RandomIterFactory.create(renderedImage, null);

        return randomIter;
    }

    public String[] getRasterPaths() {
        return rasterPaths;
    }

    public String getTime() {
        return time;
    }

    public Double getLevel() {
        return level;
    }
    
    public String getName() {
        return name;
    }
}
