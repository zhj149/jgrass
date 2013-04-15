package eu.hydrologis.jgrass.libs.utils.features;

import java.util.ArrayList;
import java.util.List;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility to clone features by modify only the geometry.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FeatureGeometrySubstitutor {
    private SimpleFeatureType newFeatureType;

    /**
     * @param oldFeatureType the {@link FeatureType} of the existing features.
     * @throws FactoryRegistryException 
     * @throws SchemaException
     */
    public FeatureGeometrySubstitutor( SimpleFeatureType oldFeatureType ) throws FactoryRegistryException, SchemaException {

        List<AttributeDescriptor> oldAttributeDescriptors = oldFeatureType.getAttributeDescriptors();

        // create the feature type
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.setName(oldFeatureType.getName());
        b.addAll(oldAttributeDescriptors);
        newFeatureType = b.buildFeatureType();
    }

    /**
     * @param oldFeature the feature from which to clone the existing attributes from.
     * @param newGeometry new geometry to insert.
     * @param index the index for the feature id creation.
     * @return the new created feature, as merged from the old feature plus the new attributes.
     */
    public SimpleFeature extendFeature( SimpleFeature oldFeature, Geometry newGeometry, int index ) {
        Object[] attributes = oldFeature.getAttributes().toArray();
        Object[] newAttributes = new Object[attributes.length];
        System.arraycopy(attributes, 0, newAttributes, 0, attributes.length);
        newAttributes[0] = newGeometry;
        // create the feature
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(newFeatureType);
        builder.addAll(newAttributes);
        SimpleFeature f = builder.buildFeature(newFeatureType.getTypeName() + "." + index);
        return f;
    }

}