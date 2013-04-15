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
package eu.hydrologis.libs.openmi;

import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;

/**
 * <p>
 * Facility methods and constants used by the console engine
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class ModelsConstants {
    /** name for the JGrass database constant */
    public final static String GRASSDB = "grassdb"; //$NON-NLS-1$

    /** name for the JGrass location constant */
    public final static String LOCATION = "location"; //$NON-NLS-1$

    /** name for the JGrass mapset constant */
    public final static String MAPSET = "mapset"; //$NON-NLS-1$

    /** name for the startdate constant */
    public final static String STARTDATE = "time_start_up"; //$NON-NLS-1$

    /** name for the enddate constant */
    public final static String ENDDATE = "time_ending_up"; //$NON-NLS-1$

    /** name for the deltat constant */
    public final static String DELTAT = "time_delta"; //$NON-NLS-1$

    /** name for the remotedb constant */
    public final static String REMOTEDBURL = "remotedburl"; //$NON-NLS-1$

    /** name for the JGrass active region constant */
    public final static String ACTIVEREGIONWINDOW = "active region window"; //$NON-NLS-1$

    /** name for the JGrass featurecollection */
    public final static String FEATURECOLLECTION = "featurecollection"; //$NON-NLS-1$

    /** name for the JGrass unknown elements */
    public final static String UNKNOWN = "unknown"; //$NON-NLS-1$

    /** name for the raster unit id */
    public final static String UNITID_RASTER = "raster unit id"; //$NON-NLS-1$

    /** name for the color map unit id */
    public final static String UNITID_COLORMAP = "colormap unit id"; //$NON-NLS-1$

    /** name for the text unit id */
    public final static String UNITID_TEXTFILE = "text file unit id"; //$NON-NLS-1$

    /** name for the categories unit id */
    public final static String UNITID_CATS = "categories unit id"; //$NON-NLS-1$

    /** name for the scalar unit id */
    public final static String UNITID_SCALAR = "scalar unit id"; //$NON-NLS-1$

    /** name for the vector unit id */
    public final static String UNITID_FEATURE = "feature unit id"; //$NON-NLS-1$

    /** name for the generic unit id */
    public final static String UNITID_UNKNOWN = "unknown unit id"; //$NON-NLS-1$

    /** variable telling that the output should be redirected to console */
    public final static String CONSOLE = "CONSOLE"; //$NON-NLS-1$

    /** variable telling that the output should be redirected to gui table */
    public final static String UITABLE = "UITABLE"; //$NON-NLS-1$

    /**
     * The 9 directions around a pixel.
     * <p>
     * Also containing the central position 0,0
     * </p>
     * <p>
     * FIXME Erica used to add several {0,0} at the end, in order to catch certain values. Those
     * have to be tracked down.
     * </p>
     */
    public final static int[][] DIR = {{0, 0}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1},
            {1, -1}, {1, 0}, {1, 1}};

    /**
     * The 9 directions and their flow values around a pixel.
     * <p>
     * Also containing the central position 0,0
     * </p>
     * <p>
     * FIXME Erica used to add several {0,0,0} at the end, in order to catch certain values. Those
     * have to be tracked down.
     * </p>
     */
    public final static int[][] DIR_WITHFLOW_ENTERING= {{0, 0, 0}, {1, 0, 5}, {1, -1, 6},
                {0, -1, 7}, {-1, -1, 8}, {-1, 0, 1}, {-1, 1, 2}, {0, 1, 3}, {1, 1, 4}};

    /*
     * This is similar to exiting inverted, but is in cols and dows and have a particular order to
     * work (in tca3d) with triangle.
     */
    public static final int[][] DIR_WITHFLOW_EXITING = {{0, 0, 0}, {1, 0, 1}, {1, -1, 2},
            {0, -1, 3}, {-1, -1, 4}, {-1, 0, 5}, {-1, 1, 6}, {0, 1, 7}, {1, 1, 8}, {0, 0, 9},
            {0, 0, 10}};

    /**
     * Facility to create exchange items
     * 
     * @param owner the linkable component that will own this input exchangeitem
     * @param activeRegion the raster region information
     * @return the exchange item
     */
    public static IInputExchangeItem createRasterInputExchangeItem( ILinkableComponent owner,
            JGrassRegion activeRegion ) {
        IElementSet elementSet = new JGrassElementset(ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity quantity = UtilitiesFacade.createScalarQuantity(GRASSRASTERMAP, UNITID_RASTER);

        return UtilitiesFacade.createInputExchangeItem(owner, quantity, elementSet);
    }

    /**
     * Facility to create exchange items
     * 
     * @param owner the linkable component that will own this output exchangeitem
     * @param activeRegion the raster region information
     * @return the exchange item
     */
    public static IOutputExchangeItem createRasterOutputExchangeItem( ILinkableComponent owner,
            JGrassRegion activeRegion ) {
        IElementSet elementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW,
                activeRegion, null);
        IQuantity quantity = UtilitiesFacade.createScalarQuantity(GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);

        return UtilitiesFacade.createOutputExchangeItem(owner, quantity, elementSet);
    }

    /**
     * Facility to create exchange items
     * 
     * @param owner the linkable component that will own this input exchangeitem
     * @param featureCollection the feature collection
     * @return the exchange item
     */
    public static IInputExchangeItem createFeatureCollectionInputExchangeItem(
            ILinkableComponent owner,
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection ) {
        IElementSet featureElementSet = new JGrassElementset(ModelsConstants.FEATURECOLLECTION,
                featureCollection, null);
        IQuantity featureQuantity = UtilitiesFacade.createScalarQuantity(FEATURECOLLECTION,
                ModelsConstants.UNITID_FEATURE);
        return UtilitiesFacade.createInputExchangeItem(owner, featureQuantity, featureElementSet);
    }

    /**
     * Facility to create exchange items
     * 
     * @param owner the linkable component that will own this output exchangeitem
     * @param featureCollection the feature collection
     * @return the exchange item
     */
    public static IOutputExchangeItem createFeatureCollectionOutputExchangeItem(
            ILinkableComponent owner,
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection ) {
        IElementSet featureElementSet = new JGrassElementset(ModelsConstants.FEATURECOLLECTION,
                featureCollection, null);
        IQuantity featureQuantity = UtilitiesFacade.createScalarQuantity(FEATURECOLLECTION,
                ModelsConstants.UNITID_FEATURE);
        return UtilitiesFacade.createOutputExchangeItem(owner, featureQuantity, featureElementSet);
    }

    /**
     * Facility to create exchange items
     * 
     * @param owner the linkable component that will own this input exchangeitem
     * @return the exchange item
     */
    public static IInputExchangeItem createDummyInputExchangeItem( ILinkableComponent owner ) {
        IElementSet elementSet = new JGrassElementset(ModelsConstants.UNKNOWN, 0);
        IQuantity quantity = UtilitiesFacade.createScalarQuantity(UNKNOWN,
                ModelsConstants.UNITID_UNKNOWN);
        return UtilitiesFacade.createInputExchangeItem(owner, quantity, elementSet);
    }

    /**
     * Facility to create exchange items
     * 
     * @param owner the linkable component that will own this output exchangeitem
     * @return the exchange item
     */
    public static IOutputExchangeItem createDummyOutputExchangeItem( ILinkableComponent owner ) {
        IElementSet elementSet = new JGrassElementset(ModelsConstants.UNKNOWN, 0);
        IQuantity quantity = UtilitiesFacade.createScalarQuantity(UNKNOWN,
                ModelsConstants.UNITID_UNKNOWN);
        return UtilitiesFacade.createOutputExchangeItem(owner, quantity, elementSet);
    }

    /**
     * Facility that returns the {@link RasterData raster data} from a {@link ILink link}.
     * <p>
     * Checks are done on return types and errors are thrown to user console as well as the needed
     * Exceptions.
     * </p>
     * 
     * @param link the link from which to retrieve the data.
     * @param time the time for which to retrieve the data.
     * @param err the console {@link PrintStream error stream} to which to log.
     * @return the read {@link RasterData}
     * @throws IOException thrown if some linkage or read error occurred.
     */
    public static RasterData getRasterDataFromLink( ILink link, ITime time, PrintStream err )
            throws IOException {
        IValueSet valueSet = link.getSourceComponent().getValues(time, link.getID());
        JGrassRasterValueSet jgValueSet = null;
        if (valueSet instanceof JGrassRasterValueSet) {
            jgValueSet = (JGrassRasterValueSet) valueSet;
        }
        if (jgValueSet == null) {
            String msg = MessageFormat.format(
                    "An error occurred while connecting to link: {0} to read the map.", link
                            .getID());
            err.println(msg);
            throw new IOException(msg);
        }
        RasterData rasterData = jgValueSet.getJGrassRasterData();
        if (rasterData == null) {
            String msg = MessageFormat.format(
                    "An error occurred while reading the map from link {0}.", link.getID());
            err.println(msg);
            throw new IOException(msg);
        }
        return rasterData;
    }

    /**
     * Facility that returns the {@link GridCoverage2D coverage} from a {@link ILink link}.
     * <p>
     * Checks are done on return types and errors are thrown to user console as well as the needed
     * Exceptions.
     * </p>
     * 
     * @param link the link from which to retrieve the data.
     * @param time the time for which to retrieve the data.
     * @param err the console {@link PrintStream error stream} to which to log.
     * @return the read {@link GridCoverage2D}
     * @throws IOException thrown if some linkage or read error occurred.
     */
    public static GridCoverage2D getGridCoverage2DFromLink( ILink link, ITime time, PrintStream err )
            throws IOException {
        IValueSet valueSet = link.getSourceComponent().getValues(time, link.getID());
        JGrassGridCoverageValueSet jgValueSet = null;
        if (valueSet instanceof JGrassGridCoverageValueSet) {
            jgValueSet = (JGrassGridCoverageValueSet) valueSet;
        }
        if (jgValueSet == null) {
            String msg = MessageFormat.format(
                    "An error occurred while connecting to link: {0} to read the map.", link
                            .getID());
            err.println(msg);
            throw new IOException(msg);
        }
        GridCoverage2D gridCoverage = jgValueSet.getGridCoverage2D();
        if (gridCoverage == null) {
            String msg = MessageFormat.format(
                    "An error occurred while reading the map from link {0}.", link.getID());
            err.println(msg);
            throw new IOException(msg);
        }
        return gridCoverage.view(ViewType.GEOPHYSICS);
    }

    /**
     * Facility that returns the {@link FeatureCollection} from a {@link ILink link}.
     * <p>
     * Checks are done on return types and errors are thrown to user console as well as the needed
     * Exceptions.
     * </p>
     * 
     * @param link the link from which to retrieve the data.
     * @param time the time for which to retrieve the data.
     * @param err the console {@link PrintStream error stream} to which to log.
     * @return the read {@link FeatureCollection}
     * @throws IOException thrown if some linkage or read error occurred.
     */
    public static FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollectionFromLink(
            ILink link, ITime time, PrintStream err ) throws IOException {
        IValueSet valueSet = link.getSourceComponent().getValues(time, link.getID());
        JGrassFeatureValueSet featureSet = null;
        if (valueSet instanceof JGrassFeatureValueSet) {
            featureSet = (JGrassFeatureValueSet) valueSet;
        } else {
            String msg = MessageFormat.format(
                    "An error occurred while connecting to link: {0} to read the map.", link
                            .getID());
            err.println(msg);
            throw new IOException(msg);
        }
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSet
                .getFeatureCollection();

        if (featureCollection == null) {
            String msg = MessageFormat.format(
                    "An error occurred while reading the map from link {0}.", link.getID());
            err.println(msg);
            throw new IOException(msg);
        }
        return featureCollection;
    }

    /**
     * Facility that returns the {@link List} of {@link SimpleFeature}s from a {@link ILink link}.
     * <p>
     * Checks are done on return types and errors are thrown to user console as well as the needed
     * Exceptions.
     * </p>
     * 
     * @param link the link from which to retrieve the data.
     * @param time the time for which to retrieve the data.
     * @param err the console {@link PrintStream error stream} to which to log.
     * @return the read List of Features
     * @throws IOException thrown if some linkage or read error occurred.
     */
    public static List<SimpleFeature> getFeatureListFromLink( ILink link, ITime time,
            PrintStream err ) throws IOException {
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = getFeatureCollectionFromLink(
                link, time, err);
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
        while( featureIterator.hasNext() ) {
            SimpleFeature feature = featureIterator.next();
            featureList.add(feature);
        }
        featureCollection.close(featureIterator);
        return featureList;
    }

    /**
     * Facility that returns the {@link ScalarSet} from a {@link ILink link}.
     * <p>
     * Checks are done on return types and errors are thrown to user console as well as the needed
     * Exceptions.
     * </p>
     * 
     * @param link the link from which to retrieve the data.
     * @param time the time for which to retrieve the data.
     * @param err the console {@link PrintStream error stream} to which to log.
     * @return the read {@link ScalarSet}
     * @throws IOException thrown if some linkage or read error occurred.
     */
    public static ScalarSet getScalarSetFromLink( ILink link, ITime time, PrintStream err )
            throws IOException {
        IValueSet valueSet = link.getSourceComponent().getValues(time, link.getID());
        ScalarSet valueScalars = null;
        if (valueSet instanceof ScalarSet) {
            valueScalars = (ScalarSet) valueSet;
        } else {
            String msg = MessageFormat.format(
                    "An error occurred while connecting to link: {0} to read the data.", link
                            .getID());
            err.println(msg);
            throw new IOException(msg);
        }
        return valueScalars;
    }
    /**
     * Calculate the drainage direction factor (is used in some horton machine like pitfiller,
     * flow,...)
     * <p>
     * Is the distance betwen the central pixel, in a 3x3 kernel, and the neighboured pixels.
     * 
     * @param dx is the resolution of a raster map in the x direction.
     * @param dy is the resolution of the raster map in the y direction.
     * @return <b>fact</b> the direction factor or 1/lenght where lenght is the distance of the
     *         pixel from the central poxel.
     */
    public static double[] calculateDirectionFactor( double dx, double dy ) {
        // direction factor, where the components are 1/length

        double[] fact = new double[9];
        for( int k = 1; k <= 8; k++ ) {
            fact[k] = 1.0 / (Math.sqrt(DIR[k][0] * dy * DIR[k][0] * dy + DIR[k][1] * DIR[k][1] * dx
                    * dx));
        }
        return fact;
    }
    // ///////////////////////////////////////////////////
    // MAP TYPES
    // ///////////////////////////////////////////////////

    /**
     * color map type identificator
     */
    final public static String COLORMAP = "colormap"; //$NON-NLS-1$

    /**
     * text file type identificator
     */
    final public static String TEXTFILE = "textfile"; //$NON-NLS-1$

    /**
     * color map type identificator
     */
    final public static String CATSMAP = "catsmap"; //$NON-NLS-1$

    /**
     * raster map type identificator
     */
    final public static String GRASSRASTERMAP = "grassrastermap"; //$NON-NLS-1$

    final public static String DEFAULTKEY = "defaultkey"; //$NON-NLS-1$

    public static final String DOTILE = "doTile";

    public static final String RESOLUTION = "resolution";

}
