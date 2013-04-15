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
package eu.hydrologis.jgrass.models.v.to.rast;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;

import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class v_to_rast extends ModelsBackbone {

    public final static String rasterID = "out"; //$NON-NLS-1$

    public final static String inShapeID = "in"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("v_to_rast.usage"); //$NON-NLS-1$

    private ILink rasterLink = null;

    private ILink inShapeLink = null;

    private IInputExchangeItem inShapeDataInputEI = null;

    private IOutputExchangeItem rasterDataOutputEI = null;

    private String fieldName;

    private String value;

    private JGrassRegion activeRegion;

    private JGrassGridCoverageValueSet jgRVS;

    private CoordinateReferenceSystem mapsetCrs;

    public v_to_rast() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public v_to_rast( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * in this method map's properties are defined... location, mapset... and than
     * IInputExchangeItem and IOutputExchangeItem are reated
     */
    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        String mapset = null;
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
                if (key.compareTo("fieldname") == 0) {
                    fieldName = argument.getValue();
                }
                if (key.compareTo("value") == 0) {
                    value = argument.getValue();
                }
            }
        }

        /*
         * if a fieldname is given, then use the filed value. If a number is given use the number.
         * If nothing is given, put everywhere 0.
         */

        /*
         * define the map path
         */
        String locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        mapsetCrs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        /*
         * create the exchange items
         */
        // shape input
        inShapeDataInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this, null);

        // raster input
        rasterDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(rasterLink.getID()) && jgRVS == null) {

            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
            if (inShapeLink != null) {
                IValueSet shapeValueSet = inShapeLink.getSourceComponent().getValues(time, inShapeLink.getID());
                featureCollection = ((JGrassFeatureValueSet) shapeValueSet).getFeatureCollection();
            }

            if (featureCollection == null) {
                throw new ModelsIllegalargumentException("A problem occurred while reading the input shapes.", this);
            }
            if (featureCollection.size() < 1) {
                throw new ModelsIllegalargumentException("The featurecollection passed to rasterize seems to be empty. check your data.", this);
            }

            String type = featureCollection.getSchema().getGeometryDescriptor().getType().getName().toString();
            int rows = activeRegion.getRows();
            int cols = activeRegion.getCols();

            WritableRaster outRaster = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, JGrassConstants.doubleNovalue);
            WritableRandomIter outIter = RandomIterFactory.createWritable(outRaster, null);

            /*
             * need to reproject the featurecollection to the mapset projection
             */
            CoordinateReferenceSystem featuresCrs = featureCollection.getBounds().getCoordinateReferenceSystem();

            MathTransform transform = CRS.findMathTransform(featuresCrs, mapsetCrs);

            FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();
            FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
            while( featureIterator.hasNext() ) {
                SimpleFeature feature = featureIterator.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                Geometry targetGeometry = JTS.transform(geometry, transform);
                feature.setDefaultGeometry(targetGeometry);
                newCollection.add(feature);
            }
            featureCollection.close(featureIterator);

            if (type.matches(".*[Pp][Oo][Ii][Nn][Tt].*")) {
                rasterizepoint(newCollection, outIter);
            } else if (type.matches(".*[Ll][Ii][Nn][Ee].*")) {
                rasterizeLine(newCollection, outIter);
            } else if (type.matches(".*[Pp][Oo][Ll][Yy][Gg][Oo][Nn].*")) {
                rasterizepolygon(newCollection, outIter);
            } else {
                throw new ModelsIllegalargumentException("Couldn't recognize the geometry type of the file.", this);
            }

            jgRVS = new JGrassGridCoverageValueSet(outRaster, activeRegion, mapsetCrs);
        }
        return jgRVS;
    }

    private void rasterizepoint( FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection, WritableRandomIter outIter ) throws IOException {
        int num = featureCollection.size();
        Envelope envelope = activeRegion.getEnvelope();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        int index = 1;
        while( featureIterator.hasNext() ) {
            out.println(MessageFormat.format("Rasterizing feature {0} of {1}", index++, num));
            SimpleFeature feature = featureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            double fillValue = 0.0;
            try {
                if (fieldName != null) {
                    Object attribute = feature.getAttribute(fieldName);
                    if (attribute instanceof Number) {
                        fillValue = ((Number) attribute).doubleValue();
                    } else if (attribute instanceof String) {
                        fillValue = Double.parseDouble(((String) attribute));
                    }
                } else if (value != null) {
                    fillValue = Double.parseDouble(value);
                }
            } catch (NumberFormatException e) {
                throw new IOException("A problem occurred while choosing the value with which to fill in the raster.");
            }
            Coordinate[] coordinates = geometry.getCoordinates();
            for( Coordinate coordinate : coordinates ) {
                if (envelope.contains(coordinate)) {
                    int[] rowCol = JGrassUtilities.coordinateToNearestRowCol(activeRegion, coordinate);
                    outIter.setSample(rowCol[1], rowCol[0], 0, fillValue);
                } else {
                    err.println("Info: Skipping point outside of given active region...");
                }
            }
        }
        featureCollection.close(featureIterator);
    }

    private void rasterizeLine( FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection, WritableRandomIter outIter ) throws IOException {
        double res = activeRegion.getNSResolution(); // 2; // to be sure to get them all, use res/2
        int num = featureCollection.size();
        Envelope envelope = activeRegion.getEnvelope();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        int index = 1;
        while( featureIterator.hasNext() ) {
            out.println(MessageFormat.format("Rasterizing feature {0} of {1}", index++, num));
            SimpleFeature feature = featureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if (!envelope.contains(geometry.getEnvelopeInternal())) {
                err.println("Info: Skipping point outside of given active region...");
                continue;
            }
            double fillValue = 0.0;
            try {
                if (fieldName != null) {
                    Object attribute = feature.getAttribute(fieldName);
                    if (attribute instanceof Number) {
                        fillValue = ((Number) attribute).doubleValue();
                    } else if (attribute instanceof String) {
                        fillValue = Double.parseDouble(((String) attribute));
                    }
                } else if (value != null) {
                    fillValue = Double.parseDouble(value);
                }
            } catch (NumberFormatException e) {
                throw new IOException("A problem occurred while choosing the value with which to fill in the raster.");
            }
            Coordinate[] coordinates = geometry.getCoordinates();
            for( int i = 0; i < coordinates.length - 1; i++ ) {
                Coordinate coordinate1 = coordinates[i];
                Coordinate coordinate2 = coordinates[i + 1];
                LineSegment lSegment = new LineSegment(coordinate1, coordinate2);
                double distance = coordinate1.distance(coordinate2);
                double perc = 1.0 * res / distance;

                double runningPerc = 0;
                while( runningPerc <= 1.0 ) {
                    Coordinate pointAlong = lSegment.pointAlong(runningPerc);

                    int[] rowCol = JGrassUtilities.coordinateToNearestRowCol(activeRegion, pointAlong);
                    outIter.setSample(rowCol[1], rowCol[0], 0, fillValue);

                    runningPerc = runningPerc + perc;
                }
            }
        }
        featureCollection.close(featureIterator);
    }

    private void rasterizepolygon( FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection, WritableRandomIter outIter ) throws IOException {
        int num = featureCollection.size();
        Envelope envelope = activeRegion.getEnvelope();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        int index = 1;
        while( featureIterator.hasNext() ) {
            out.println(MessageFormat.format("Rasterizing feature {0} of {1}", index++, num));
            SimpleFeature feature = featureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if (!envelope.contains(geometry.getEnvelopeInternal())) {
                err.println("Info: Skipping point outside of given active region...");
                continue;
            }

            double fillValue = 0.0;
            try {
                if (fieldName != null) {
                    Object attribute = feature.getAttribute(fieldName);
                    if (attribute instanceof Number) {
                        fillValue = ((Number) attribute).doubleValue();
                    } else if (attribute instanceof String) {
                        fillValue = Double.parseDouble(((String) attribute));
                    }
                } else if (value != null) {
                    fillValue = Double.parseDouble(value);
                }
            } catch (NumberFormatException e) {
                throw new IOException("A problem occurred while choosing the value with which to fill in the raster.");
            }
            JGrassUtilities.rasterizePolygonGeometry(activeRegion, geometry, outIter, null, fillValue, new NullProgressMonitor());
        }
        featureCollection.close(featureIterator);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(rasterID)) {
            rasterLink = link;
        }
        if (id.equals(inShapeID)) {
            inShapeLink = link;
        }
    }

    public void finish() {
    }

    /**
     * There is an IInputExchangeItem: pit, basins
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return inShapeDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 1;
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: basinShapeength
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return rasterDataOutputEI;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(rasterLink.getID())) {
            rasterLink = null;
        }
        if (linkID.equals(inShapeLink.getID())) {
            inShapeLink = null;
        }
    }

}
