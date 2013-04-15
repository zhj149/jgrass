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
package eu.hydrologis.jgrass.models.r.mosaic;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.ViewType;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.openmi.standard.IArgument;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReader;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageWriter;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.JGrassGridCoverage2D;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.JGrassGridCoverage2D.WritableGridCoverageBuilder;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.openmi.OneInOneOutModelsBackbone;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassRasterValueSet;
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 * @author Silvia Franceschi (www.hydrologis.com)
 */
public class r_mosaic extends OneInOneOutModelsBackbone {

    private String input = null;
    private String output = null;;

    private final static String modelParameters = "...Usage";
    private String cellFolderPath;
    private CoordinateReferenceSystem crs;
    private JGrassRegion activeRegion;
    private String locationPath;
    private String mapset;

    public r_mosaic() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public r_mosaic( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        mapset = null;
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
                if (key.compareTo("output") == 0) {
                    output = argument.getValue();
                }
                if (key.equals("input")) {
                    input = argument.getValue();
                }
            }
        }

        locationPath = grassDb + File.separator + location;
        cellFolderPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.CELL;
        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        if (output == null || input == null) {
            throw new ModelsIllegalargumentException("The input and output parameters are mandatory. Check your syntax.", this);
        }

        inputEI = ModelsConstants.createDummyInputExchangeItem(this);
        outputEI = ModelsConstants.createDummyOutputExchangeItem(this);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        PrintStreamProgressMonitor monitor = new PrintStreamProgressMonitor(out);
        String[] mapsSplit = input.split(","); //$NON-NLS-1$
        File cellFolderFile = new File(cellFolderPath);
        GridGeometry2D referenceGridGeometry = null;

        double n = Double.MIN_VALUE;
        double s = Double.MAX_VALUE;
        double e = Double.MIN_VALUE;
        double w = Double.MAX_VALUE;
        int np = Integer.MIN_VALUE;
        int sp = Integer.MAX_VALUE;
        int ep = Integer.MIN_VALUE;
        int wp = Integer.MAX_VALUE;

        List<GridCoverage2D> coveragesList = new ArrayList<GridCoverage2D>();
        for( String mapName : mapsSplit ) {
            mapName = mapName.trim();
            File mapFile = new File(cellFolderFile, mapName);
            if (mapFile.exists()) {
                GrassCoverageReader coverageReader = new GrassCoverageReader(PixelInCell.CELL_CENTER, null, true, false, monitor);
                coverageReader.setInput(mapFile);
                GridCoverage2D coverage = coverageReader.read(null);
                coverage = coverage.view(ViewType.GEOPHYSICS);

                CoordinateReferenceSystem tmpCrs = coverage.getCoordinateReferenceSystem();
                MathTransform transform = CRS.findMathTransform(crs, tmpCrs);
                if (!transform.isIdentity()) {
                    err.println("Ignoring map of different coordinate system: " + mapName);
                    continue;
                }

                if (referenceGridGeometry == null) {
                    // take the first as reference
                    referenceGridGeometry = coverage.getGridGeometry();
                }

                Envelope2D worldEnv = coverage.getEnvelope2D();
                GridEnvelope2D pixelEnv = referenceGridGeometry.worldToGrid(worldEnv);

                int minPX = (int) pixelEnv.getMinX();
                int minPY = (int) pixelEnv.getMinY();
                int maxPX = (int) pixelEnv.getMaxX();
                int maxPY = (int) pixelEnv.getMaxY();
                if (minPX < wp)
                    wp = minPX;
                if (minPY < sp)
                    sp = minPY;
                if (maxPX > ep)
                    ep = maxPX;
                if (maxPY > np)
                    np = maxPY;

                double minWX = worldEnv.getMinX();
                double minWY = worldEnv.getMinY();
                double maxWX = worldEnv.getMaxX();
                double maxWY = worldEnv.getMaxY();
                if (minWX < w)
                    w = minWX;
                if (minWY < s)
                    s = minWY;
                if (maxWX > e)
                    e = maxWX;
                if (maxWY > n)
                    n = maxWY;

                coveragesList.add(coverage);
            } else {
                err.println("Ignoring non existing map: " + mapName);
            }
        }

        int endWidth = ep - wp;
        int endHeight = np - sp;
        WritableRaster outRaster = FluidUtils.createDoubleWritableRaster(endWidth, endHeight, null, null,
                JGrassConstants.doubleNovalue);
        WritableRandomIter outIter = RandomIterFactory.createWritable(outRaster, null);

        int offestX = Math.abs(wp);
        int offestY = Math.abs(sp);
        int index = 1;
        for( GridCoverage2D coverage : coveragesList ) {
            RenderedImage renderedImage = coverage.getRenderedImage();
            RandomIter randomIter = RandomIterFactory.create(renderedImage, null);

            Envelope2D env = coverage.getEnvelope2D();

            GridEnvelope2D repEnv = referenceGridGeometry.worldToGrid(env);

            GridGeometry2D tmpGG = coverage.getGridGeometry();
            GridEnvelope2D tmpEnv = tmpGG.worldToGrid(env);

            int startX = (int) (repEnv.getMinX() + offestX);
            int startY = (int) (repEnv.getMinY() + offestY);

            System.out.println();

            double tmpW = tmpEnv.getWidth();
            monitor.beginTask("Patch map " + index++, (int) tmpW); //$NON-NLS-1$
            for( int x = 0; x < tmpW; x++ ) {
                for( int y = 0; y < tmpEnv.getHeight(); y++ ) {
                    double value = randomIter.getSampleDouble(x, y, 0);
                    outIter.setSample(x + startX, y + startY, 0, value);
                }
                monitor.worked(1);
            }
            monitor.done();

            randomIter.done();
        }

        JGrassRegion writeRegion = new JGrassRegion(w, e, s, n, activeRegion.getWEResolution(), activeRegion.getNSResolution());
        WritableGridCoverageBuilder gridCoverageBuilder = new JGrassGridCoverage2D.WritableGridCoverageBuilder(outRaster);
        JGrassGridCoverage2D jgrassGridCoverage2D = gridCoverageBuilder.writeRegion(writeRegion).crs(crs).dataRange(
                new double[]{0.0, 4000.0}).build();
        GridCoverage2D gridCoverage2D = jgrassGridCoverage2D.getGridCoverage2D();

        String rasterMapPath = cellFolderPath + File.separator + output;
        File file = new File(rasterMapPath);
        GrassCoverageWriter writer = new GrassCoverageWriter(file, monitor);
        writer.write(gridCoverage2D);

        //        out.println("Writing map: " + output); //$NON-NLS-1$
        // GrassBinaryImageWriterSpi writerSpi = new GrassBinaryImageWriterSpi();
        // GrassBinaryImageWriter writer = (GrassBinaryImageWriter)
        // writerSpi.createWriterInstance();
        // RenderedImage renderedImage =
        // gridCoverage2D.view(ViewType.GEOPHYSICS).getRenderedImage();
        // writer.setOutput(file);
        // writer.write(renderedImage);

        if (JGrassPlugin.getDefault() != null) {
            JGrassMapGeoResource addedMap = JGrassCatalogUtilities.addMapToCatalog(locationPath, mapset, output,
                    JGrassConstants.GRASSBINARYRASTERMAP);
            if (addedMap == null)
                return null;

            IMap activeMap = ApplicationGIS.getActiveMap();
            ApplicationGIS.addLayersToMap(activeMap, Collections.singletonList((IGeoResource) addedMap), activeMap.getMapLayers()
                    .size());
        }

        JGrassRasterValueSet cutRasterValueSet = null; // new JGrassRasterValueSet(cutRasterData);

        return cutRasterValueSet;
    }
}
