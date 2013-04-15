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
package eu.hydrologis.jgrass.models.h.profile;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.raster.RasterUtilities;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class h_profile extends ModelsBackbone {

    /*
     * OPENMI VARIABLES
     */
    public final static String mapID = "map"; //$NON-NLS-1$

    public final static String profileID = "out"; //$NON-NLS-1$

    public final static String featureID = "line"; //$NON-NLS-1$

    private final static String modelParameters = "h.profile --igrass-map map [--coords x0,y0,x1,y1...xn,yn] [--ishapefile-line filename]";

    private ILink mapLink = null;

    private ILink profileLink = null;

    private ILink featureLink = null;

    private IOutputExchangeItem profileOutputEI = null;

    private IInputExchangeItem mapInputEI = null;

    private IInputExchangeItem featureInputEI = null;

    private JGrassRegion activeRegion = null;

    private List<Coordinate> profileNodesList;

    public h_profile() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_profile( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        String mapset = null;
        String coords = null;
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
                if (key.compareTo("coords") == 0) {
                    coords = argument.getValue();
                }
            }
        }

        profileNodesList = new ArrayList<Coordinate>();
        try {
            if (coords != null) {
                String[] split = coords.split(",");
                for( int i = 0; i < split.length; i++ ) {
                    double east = Double.parseDouble(split[i].trim());
                    i++;
                    double north = Double.parseDouble(split[i].trim());
                    Coordinate tmp = new Coordinate(east, north);
                    profileNodesList.add(tmp);
                }
            }

        } catch (Exception e) {
            throw new ModelsIllegalargumentException(
                    "A problem occurred while parsing the supplied profile nodes coordinates. Check your syntax.", this);
        }

        /*
         * define the region path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        /*
         * create the exchange items
         */
        profileOutputEI = ModelsConstants.createDummyOutputExchangeItem(this);
        mapInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        featureInputEI = ModelsConstants.createDummyInputExchangeItem(this);
    }

    /**
     * 
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(profileLink.getID())) {
            if (featureLink != null) {
                IValueSet featValueSet = featureLink.getSourceComponent().getValues(time,
                        featureLink.getID());
                FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = ((JGrassFeatureValueSet) featValueSet)
                        .getFeatureCollection();
                // take just the first feature, we do not do them all
                FeatureIterator<SimpleFeature> featuresIterator = featureCollection.features();
                if (featuresIterator.hasNext()) {
                    SimpleFeature f = featuresIterator.next();
                    Geometry geom = (Geometry) f.getDefaultGeometry();
                    if (geom.getGeometryType().matches(".*[Ll][Ii][Nn][Ee].*")) {
                        out.println("Using supplied features to trace the profile...");
                        Coordinate[] coordinates = geom.getCoordinates();
                        for( Coordinate coordinate : coordinates ) {
                            profileNodesList.add(coordinate);
                        }
                    }
                }
            } else {
                out.println("Using supplied coordinates to trace the profile...");
            }

            if (profileNodesList.size() < 2) {
                throw new ModelsIllegalargumentException(
                        "We need at least two coordinates to create a profile. Check your syntax.", this);
            }

            IValueSet mapValueSet = mapLink.getSourceComponent().getValues(time, mapLink.getID());
            GridCoverage2D mapData = ((JGrassGridCoverageValueSet) mapValueSet).getGridCoverage2D();
            if (mapData != null) {
                RandomIter mapIterator = RandomIterFactory.create(mapData.getRenderedImage(), null);
                
                ScalarSet outputSet = new ScalarSet();
                outputSet.add(2.0);
                RasterUtilities rUtilities = new RasterUtilities();
                double lastProgressive = 0.0;
                for( int i = 0; i < profileNodesList.size() - 1; i++ ) {
                    out.println("Creating profile " + (i + 1) + "/" + (profileNodesList.size() - 1)
                            + "...");
                    Coordinate first = profileNodesList.get(i);
                    Coordinate second = profileNodesList.get(i + 1);
                    List<Double[]> profile = rUtilities.doProfile(first.x, first.y, second.x,
                            second.y, activeRegion.getWEResolution(), activeRegion
                                    .getNSResolution(), activeRegion, mapIterator);
                    double last = 0.0;
                    for( Double[] doubles : profile ) {
                        last = doubles[0] + lastProgressive;
                        outputSet.add(last);
                        outputSet.add(doubles[1]);
                    }
                    lastProgressive = last;
                }
                return outputSet;
            }
        }
        return null;
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(mapID)) {
            mapLink = link;
        }
        if (id.equals(profileID)) {
            profileLink = link;
        }
        if (id.equals(featureID)) {
            featureLink = link;
        }

    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return mapInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return featureInputEI;
        } else {
            return null;
        }
    }

    public int getInputExchangeItemCount() {
        return 2;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return profileOutputEI;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void finish() {
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(mapLink.getID())) {
            mapLink = null;
        }
        if (linkID.equals(profileLink.getID())) {
            profileLink = null;
        }
        if (linkID.equals(featureLink.getID())) {
            featureLink = null;
        }
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

}
