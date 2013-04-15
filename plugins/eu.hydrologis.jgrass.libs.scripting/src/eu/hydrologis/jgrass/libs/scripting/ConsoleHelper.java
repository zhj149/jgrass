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
package eu.hydrologis.jgrass.libs.scripting;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import eu.hydrologis.jgrass.libs.io.RasterWritingFailureException;
import eu.hydrologis.jgrass.libs.map.JGrassRasterData;
import eu.hydrologis.jgrass.libs.map.JGrassRasterMapReader;
import eu.hydrologis.jgrass.libs.map.JGrassRasterMapWriter;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.FileUtilities;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;
import groovy.sql.Sql;

/**
 * Helper class for the scripting engine.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 */
public class ConsoleHelper {

    private static PrintStream p_out;
    private static PrintStream p_err;
    private static String p_locationPath;
    private static String p_mapsetName;
    private static JGrassRegion p_activeRegion;
    private static PrintStreamProgressMonitor monitor;
    private static String p_remotedbUrl;
    private static Sql sqlInstance;
    private static String p_mapsetPath;
    private static double north;
    private static double south;
    private static double east;
    private static double west;
    private static double weres;
    private static double nsres;
    private static int rows;
    private static int cols;

    public ConsoleHelper( String mapsetPath, String remotedbUrl, PrintStream out, PrintStream err ) {
        p_remotedbUrl = remotedbUrl;
        File mapsetFile = new File(mapsetPath);
        p_mapsetPath = mapsetFile.getAbsolutePath();
        p_locationPath = mapsetFile.getParent();
        p_mapsetName = mapsetFile.getName();

        if (out == null) {
            p_out = System.out;
        } else {
            p_out = out;
        }
        if (err == null) {
            p_err = System.err;
        } else {
            p_err = err;
        }
        String windPath = p_mapsetPath + File.separator + JGrassConstants.WIND;
        try {
            p_activeRegion = new JGrassRegion(windPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        north = p_activeRegion.getNorth();
        south = p_activeRegion.getSouth();
        east = p_activeRegion.getEast();
        west = p_activeRegion.getWest();
        weres = p_activeRegion.getWEResolution();
        nsres = p_activeRegion.getNSResolution();
        rows = p_activeRegion.getRows();
        cols = p_activeRegion.getCols();

        monitor = new PrintStreamProgressMonitor(p_out);

    }
    public static Sql getDbConnection() throws Exception {
        if (sqlInstance != null) {
            return sqlInstance;
        }

        /*
         * db connection if there is one
         * postgresql:host:port:database:user:passwd
         */
        if (p_remotedbUrl != null) {
            String[] urlSplit = p_remotedbUrl.split(":");
            StringBuilder jdbcUrlBuilder = new StringBuilder();
            jdbcUrlBuilder.append("jdbc:");
            jdbcUrlBuilder.append(urlSplit[0]);
            jdbcUrlBuilder.append("://");
            jdbcUrlBuilder.append(urlSplit[1]);
            jdbcUrlBuilder.append(":");
            jdbcUrlBuilder.append(urlSplit[2]);
            jdbcUrlBuilder.append("/");
            jdbcUrlBuilder.append(urlSplit[3]);

            String driverString = "";
            if (urlSplit[0].equals("postgresql")) {
                driverString = "org.postgresql.Driver";
            }

            try {
                sqlInstance = Sql.newInstance(jdbcUrlBuilder.toString(), urlSplit[4], urlSplit[5], driverString);
            } catch (Exception e) {
                e.printStackTrace();
                p_err.println(e.getLocalizedMessage());
            }
            return sqlInstance;
        }
        return null;
    }

    public static void updateActiveregion() {
        String windPath = p_mapsetPath + File.separator + JGrassConstants.WIND;
        try {
            p_activeRegion = new JGrassRegion(windPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        north = p_activeRegion.getNorth();
        south = p_activeRegion.getSouth();
        east = p_activeRegion.getEast();
        west = p_activeRegion.getWest();
        weres = p_activeRegion.getWEResolution();
        nsres = p_activeRegion.getNSResolution();
        rows = p_activeRegion.getRows();
        cols = p_activeRegion.getCols();
        p_out.println("Updated active region...");
    }

    public static double NORTH() {
        return north;
    }

    public static double SOUTH() {
        return south;
    }

    public static double EAST() {
        return east;
    }

    public static double WEST() {
        return west;
    }

    public static double WERES() {
        return weres;
    }

    public static double NSRES() {
        return nsres;
    }

    public static int ROWS() {
        return rows;
    }

    public static int COLS() {
        return cols;
    }

    public static void ping() {
        p_out.println("ping...");
    }

    public static void toMap( String mapname, double[][] matrix ) {
        toMap(mapname, matrix, JGrassConstants.doubleNovalue);
    }

    public static void toMap( String mapname, double[][] matrix, double novalue ) {
        p_out.println("Writing data to map: " + mapname);
        try {
            RasterData data = new JGrassRasterData(matrix);
            JGrassRasterMapWriter mw = new JGrassRasterMapWriter(p_activeRegion, mapname, p_mapsetName, p_locationPath,
                    new PrintStreamProgressMonitor(p_out));
            if (mw.open()) {
                mw.write(data);
            }
            mw.close();
        } catch (RasterWritingFailureException e) {
            e.printStackTrace();
            p_err.println(MessageFormat.format("An error occurred while writing: {0} to disk.", mapname));
            return;
        }
        p_out.println("The map was successfully to disk");
    }

    public static double[][] fromMap( String mapName ) {
        JGrassRasterMapReader jgrassMapReader = new JGrassRasterMapReader.BuilderFromPathAndNames(p_activeRegion, mapName,
                p_mapsetName, p_locationPath).maptype(JGrassConstants.GRASSBINARYRASTERMAP).monitor(
                new PrintStreamProgressMonitor(p_out)).build();
        if (!jgrassMapReader.open()) {
            p_err.println("An error occurred while reading the map: " + mapName);
            return null;
        }
        double[][] data = null;
        try {
            if (jgrassMapReader.hasMoreData()) {
                RasterData rasterData = jgrassMapReader.getNextData();
                data = rasterData.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        jgrassMapReader.close();

        return data;
    }

    public static List<SimpleFeature> fromFeatureLayer( String mapName ) throws IOException {

        ILayer selectedLayer = ApplicationGIS.getActiveMap().getEditManager().getSelectedLayer();
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = selectedLayer.getResource(FeatureSource.class,
                new NullProgressMonitor());
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
        Filter filter = selectedLayer.getFilter();
        if (filter.equals(Filter.EXCLUDE)) {
            featureCollection = featureSource.getFeatures();
        } else {
            featureCollection = featureSource.getFeatures(filter);
        }
        
        List<SimpleFeature> featuresList = new ArrayList<SimpleFeature>();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        while( featureIterator.hasNext() ) {
            SimpleFeature feature = featureIterator.next();
            featuresList.add(feature);
        }
        featureCollection.close(featureIterator);

        return featuresList;
    }

    public static List<SimpleFeature> fromShapefile( String shapePath ) throws IOException {

        FileDataStore store = FileDataStoreFinder.getDataStore(new File(shapePath));
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = store.getFeatureSource();
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

        List<SimpleFeature> featuresList = new ArrayList<SimpleFeature>();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        while( featureIterator.hasNext() ) {
            SimpleFeature feature = featureIterator.next();
            featuresList.add(feature);
        }
        featureIterator.close();

        return featuresList;
    }

    public static void load( String mapname ) {
        if (JGrassPlugin.getDefault() != null) {
            JGrassMapGeoResource addedMap = JGrassCatalogUtilities.addMapToCatalog(p_locationPath, p_mapsetName, mapname,
                    JGrassConstants.GRASSBINARYRASTERMAP);
            if (addedMap == null)
                p_err.println("An error occurred while trying to add the map to the catalog.");

            IMap activeMap = ApplicationGIS.getActiveMap();
            ApplicationGIS.addLayersToMap(activeMap, Collections.singletonList((IGeoResource) addedMap), activeMap.getMapLayers()
                    .size());
        }
    }

    public static void copyToNewMapset( String mapsetName, String... maps ) {
        String originalMapsetPath = p_locationPath + File.separator + p_mapsetName;
        String mapsetPath = p_locationPath + File.separator + mapsetName;

        File f = new File(mapsetPath);
        if (f.exists()) {
            p_err.println("The mapset already exists. Can't export to an existing mapset. Choose a different name.");
            return;
        }

        boolean createdMapset = JGrassCatalogUtilities.createMapset(p_locationPath, mapsetName, null, null);
        if (!createdMapset) {
            p_err.println("An error occurred while creating the new mapset structure. Check your permissions.");
        }

        StringBuilder warnings = new StringBuilder();
        monitor.beginTask("Copy maps...", maps.length);
        for( String mapName : maps ) {
            monitor.worked(1);
            String[] originalMapsPath = JGrassUtilities.filesOfRasterMap(originalMapsetPath, mapName);
            String[] copiedMapsPath = JGrassUtilities.filesOfRasterMap(mapsetPath, mapName);

            for( int i = 0; i < originalMapsPath.length; i++ ) {
                File orig = new File(originalMapsPath[i]);
                if (!orig.exists()) {
                    warnings.append("\nWarning: The following file didn't exist: " + originalMapsPath[i]);
                    continue;
                }
                if (orig.isDirectory()) {
                    continue;
                }
                File copiedParent = new File(copiedMapsPath[i]).getParentFile();
                if (!copiedParent.exists()) {
                    copiedParent.mkdirs();
                }
                FileUtilities.copyFile(originalMapsPath[i], copiedMapsPath[i]);
            }
        }
        monitor.done();
        p_out.println(warnings.toString());
    }

    public static void deleteMaps( String... maps ) {
        String mapsetPath = p_locationPath + File.separator + p_mapsetName;
        for( String map : maps ) {
            p_out.println("Removing map: " + map);
            if (!JGrassUtilities.removeGrassRasterMap(mapsetPath, map)) {
                p_err.println("Map " + map + " could not be removed");
            }
        }
    }

}
