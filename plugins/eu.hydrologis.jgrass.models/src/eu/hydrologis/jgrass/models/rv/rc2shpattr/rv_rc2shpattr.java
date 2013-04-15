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
package eu.hydrologis.jgrass.models.rv.rc2shpattr;

import java.awt.Point;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.JGrassModelsPlugin;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.raster.RasterUtilities;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

/**
 * Model that adds a category from a raster as attribute to a shapefile.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 */
public class rv_rc2shpattr extends ModelsBackbone {
    private static final String MIDDLE = "middle";
    private static final String START = "start";
    private static final String END = "end";

    /*
     * OPENMI VARIABLES
     */
    public final static String rasterID = "raster"; //$NON-NLS-1$

    public final static String inShapeID = "inshp"; //$NON-NLS-1$

    public final static String outShapeID = "outshp"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("rv_rc2shpattr.usage"); //$NON-NLS-1$

    private ILink rasterLink = null;

    private ILink inShapeLink = null;

    private ILink outShapeLink = null;

    private IOutputExchangeItem outShapeDataOutputEI = null;

    private IInputExchangeItem inShapeDataInputEI = null;

    private IInputExchangeItem rasterDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private String fieldName = "new";

    private String coordPosition;

    public rv_rc2shpattr() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public rv_rc2shpattr( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        String mapset = null;
        coordPosition = MIDDLE;
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
                if (key.compareTo("coordposition") == 0) {
                    coordPosition = argument.getValue();
                }
            }
        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "rv.rc2shpattr";
        componentId = null;

        /*
         * create the exchange items
         */
        // shape output
        outShapeDataOutputEI = ModelsConstants
                .createFeatureCollectionOutputExchangeItem(this, null);

        // shape input
        inShapeDataInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this, null);

        // raster input
        rasterDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(outShapeLink.getID())) {

            RasterData rasterData = null;
            if (rasterLink != null) {
                IValueSet rasterValueSet = rasterLink.getSourceComponent().getValues(time,
                        rasterLink.getID());
                rasterData = ((JGrassRasterValueSet) rasterValueSet).getJGrassRasterData();
            }
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
            if (inShapeLink != null) {
                IValueSet shapeValueSet = inShapeLink.getSourceComponent().getValues(time,
                        inShapeLink.getID());
                featureCollection = ((JGrassFeatureValueSet) shapeValueSet).getFeatureCollection();
            }

            if (rasterData == null || featureCollection == null) {
                throw new RuntimeException(
                        "An error occurred while reading the input data. Check your syntax.");
            }

            FeatureCollection<SimpleFeatureType, SimpleFeature> newFeatureCollection = mergeRasterVector(
                    rasterData, featureCollection);
            return new JGrassFeatureValueSet(newFeatureCollection);
        }
        return null;
    }

    private FeatureCollection<SimpleFeatureType, SimpleFeature> mergeRasterVector(
            RasterData rasterData,
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection )
            throws Exception {

        try {

            SimpleFeatureType featureType = featureCollection.getSchema();
            List<AttributeDescriptor> oldAttributes = featureType.getAttributeDescriptors();

            AttributeTypeBuilder build = new AttributeTypeBuilder();
            build.setNillable(true);
            build.setBinding(Double.class);
            AttributeDescriptor descriptor = build.buildDescriptor(fieldName);

            List<AttributeDescriptor> newAttributesList = new ArrayList<AttributeDescriptor>();
            for( AttributeDescriptor attributeDescriptor : oldAttributes ) {
                newAttributesList.add(attributeDescriptor);
            }
            // add the new one
            newAttributesList.add(descriptor);

            // create the feature type
            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
            // set the name
            b.setName("rc2attr");
            // add a geometry property
            b.addAll(newAttributesList);
            // build the type
            SimpleFeatureType type = b.buildFeatureType();

            List<SimpleFeature> newFeaturesList = new ArrayList<SimpleFeature>();
            FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();

            int all = featureCollection.size();
            int ii = 1;
            while( featureIterator.hasNext() ) {
                out.println("Working on feature N." + ii + " / " + all);
                ii++;

                SimpleFeature feature = featureIterator.next();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                String geometryType = geometry.getGeometryType();
                double value = -1;
                int[] rowCols;
                if (geometryType.matches(".*[Pp][Oo][Ii][Nn][Tt].*")) {
                    rowCols = RasterUtilities.putClickToCenterOfCell(activeRegion, geometry
                            .getCoordinate());
                    value = rasterData.getValueAt(rowCols[0], rowCols[1]);
                } else if (geometryType.matches(".*[Ll][Ii][Nn][Ee].*")) {
                    Coordinate[] coordinates = geometry.getCoordinates();
                    if (coordPosition.trim().equals(START)) {
                         rowCols = RasterUtilities.putClickToCenterOfCell(activeRegion,
                                coordinates[0]);
                        value = rasterData.getValueAt(rowCols[0], rowCols[1]);
                    } else if (coordPosition.trim().equals(END)) {
                         rowCols = RasterUtilities.putClickToCenterOfCell(activeRegion,
                                coordinates[coordinates.length - 1]);
                        value = rasterData.getValueAt(rowCols[0], rowCols[1]);
                    } else if (coordPosition.trim().equals(MIDDLE)) {
                        int index = coordinates.length / 2;
                         rowCols = RasterUtilities.putClickToCenterOfCell(activeRegion,
                                coordinates[index]);
                        value = rasterData.getValueAt(rowCols[0], rowCols[1]);
                    }
                } else if (geometryType.matches(".*[Pp][Oo][Ll][Yy][Gg][Oo][Nn].*")) {
                    // try the centroid
                    com.vividsolutions.jts.geom.Point centroid = geometry.getCentroid();
                    if (geometry.contains(centroid)) {
                         rowCols = RasterUtilities.putClickToCenterOfCell(activeRegion,
                                centroid.getCoordinate());
                        value = rasterData.getValueAt(rowCols[0], rowCols[1]);
                    } else {
                         rowCols = RasterUtilities.putClickToCenterOfCell(activeRegion,
                                geometry.getCoordinate());
                        value = rasterData.getValueAt(rowCols[0], rowCols[1]);
                    }
                }

                Object[] attributes = feature.getAttributes().toArray();
                Object[] newAttributes = new Object[attributes.length + 1];
                System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
                newAttributes[newAttributes.length - 1] = value;

                // create the feature
                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
                // add the values
                builder.addAll(newAttributes);
                // build the feature with provided ID
                SimpleFeature f = builder.buildFeature(type.getTypeName() + "." + ii);

                newFeaturesList.add(f);
            }

            FeatureCollection<SimpleFeatureType, SimpleFeature> fcollection = FeatureCollections
                    .newCollection();
            for( SimpleFeature feature : newFeaturesList ) {
                fcollection.add(feature);
            }
            return fcollection;

        } catch (Exception e) {
            e.printStackTrace();
            JGrassModelsPlugin
                    .log(
                            "JGrassModelsPlugin problem: eu.hydrologis.jgrass.models.rv.rc2shpattr#rv_rc2shpattr#mergeRasterVector", e); //$NON-NLS-1$

            throw new Exception("An error occurred while merging the raster and vector data,");
        }

    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(rasterID)) {
            rasterLink = link;
        }
        if (id.equals(outShapeID)) {
            outShapeLink = link;
        }
        if (id.equals(inShapeID)) {
            inShapeLink = link;
        }
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return rasterDataInputEI;
        } else {
            return inShapeDataInputEI;
        }
    }

    public int getInputExchangeItemCount() {
        return 2;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return outShapeDataOutputEI;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(rasterLink.getID())) {
            rasterLink = null;
        }
        if (linkID.equals(inShapeLink.getID())) {
            inShapeLink = null;
        }
        if (linkID.equals(outShapeLink.getID())) {
            outShapeLink = null;
        }
    }

}
