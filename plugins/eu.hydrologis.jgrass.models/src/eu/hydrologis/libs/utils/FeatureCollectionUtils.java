///*
// * JGrass - Free Open Source Java GIS http://www.jgrass.org 
// * (C) { 
// * HydroloGIS - www.hydrologis.com                                                   
// * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam                               
// * The JGrass developer team - www.jgrass.org                                         
// * }
// * 
// * This library is free software; you can redistribute it and/or modify it under
// * the terms of the GNU Library General Public License as published by the Free
// * Software Foundation; either version 2 of the License, or (at your option) any
// * later version.
// * 
// * This library is distributed in the hope that it will be useful, but WITHOUT
// * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
// * details.
// * 
// * You should have received a copy of the GNU Library General Public License
// * along with this library; if not, write to the Free Foundation, Inc., 59
// * Temple Place, Suite 330, Boston, MA 02111-1307 USA
// */
//package eu.hydrologis.libs.utils;
//
//import java.util.List;
//
//import org.geotools.factory.FactoryConfigurationError;
//import org.geotools.feature.AttributeType;
//import org.geotools.feature.AttributeTypeFactory;
//import org.geotools.feature.Feature;
//import org.geotools.feature.FeatureCollection;
//import org.geotools.feature.FeatureCollections;
//import org.geotools.feature.FeatureType;
//import org.geotools.feature.FeatureTypeFactory;
//import org.geotools.feature.GeometryAttributeType;
//import org.geotools.feature.IllegalAttributeException;
//import org.geotools.feature.SchemaException;
//
//import com.vividsolutions.jts.geom.Geometry;
//
//import eu.hydrologis.jgrass.libs.utils.features.FeatureUtilities;
//
///**
// * This class contains a set of method for FeatureCollection.
// * 
// * @author Erica Ghesla - erica.ghesla@ing.unitn.it
// */
//public class FeatureCollectionUtils {
//
//    FeatureCollection fcollection = null;
//
//    /**
//     * Creates a FeatureCollection using geometries and attribute
//     * 
//     * @param geomName
//     * @param geomClass
//     * @param geometryVector
//     * @param attributeNames
//     * @param attributeClasses
//     * @param attributeValue
//     * @return
//     * @deprecated this method should not be used. It is based on too much deprecated stuff! There
//     *             are already methods for featurecollection creation in the
//     *             {@link FeatureUtilities} methods. This class will be erased as soon as possible.
//     */
//    @SuppressWarnings({"deprecation", "unchecked"})
//    public FeatureCollectionUtils( String geomName, Class< ? extends Geometry> geomClass,
//            List< ? extends Geometry> geometryVector, List<String> attributeNames,
//            List<Class> attributeClasses, List<Object[]> attributeValue ) {
//        fcollection = FeatureCollections.newCollection();
//        /*
//         * define the geometry field
//         */
//        AttributeType[] attributesArray = new AttributeType[attributeNames.size() + 1];
//        GeometryAttributeType geometryAttribute = (GeometryAttributeType) AttributeTypeFactory
//                .newAttributeType(geomName, geomClass);
//        attributesArray[0] = geometryAttribute;
//        /*
//         * define the other non-geometric attributes
//         */
//        if (attributeNames.size() != attributeClasses.size()
//                || geometryVector.size() != attributeValue.get(0).length) {
//            // throw some exception
//            return;
//        }
//        for( int i = 0; i < attributesArray.length - 1; i++ ) {
//            attributesArray[i + 1] = (AttributeType) AttributeTypeFactory.newAttributeType(
//                    attributeNames.get(i), attributeClasses.get(i));
//        }
//
//        FeatureType ftContour = null;
//        try {
//            ftContour = FeatureTypeFactory.newFeatureType(attributesArray, "default");
//        } catch (FactoryConfigurationError e1) {
//            e1.printStackTrace();
//        } catch (SchemaException e1) {
//            e1.printStackTrace();
//        }
//        Feature[] features = new Feature[attributeValue.get(0).length];
//        for( int i = 0; i < features.length; i++ ) {
//            // create the feature attributes
//            Object[] featureAttribs = new Object[attributesArray.length];
//            featureAttribs[0] = geometryVector.get(i);
//            for( int j = 1; j < featureAttribs.length; j++ ) {
//                featureAttribs[j] = attributeValue.get(j - 1)[i];
//            }
//
//            // create the feature
//
//            try {
//                features[i] = ftContour.create(featureAttribs);
//            } catch (IllegalAttributeException e) {
//                e.printStackTrace();
//                return;
//            }
//        }
//
//        for( int i = 0; i < features.length; i++ ) {
//            fcollection.add(features[i]);
//        }
//
//        int i = 0;
//        /*
//         * FeatureType ftContour = null; try { ftContour =
//         * FeatureTypeFactory.newFeatureType(attributesArray, "default"); } catch
//         * (FactoryConfigurationError e1) { e1.printStackTrace(); } catch (SchemaException e1) {
//         * e1.printStackTrace(); } // create the feature attributes Object[] featureAttribs = new
//         * Object[attributesArray.length]; featureAttribs[0] = geometry; for (int j = 1; j <
//         * featureAttribs.length; j++) { featureAttribs[j] = attributeValue.elementAt(j - 1); } //
//         * create the feature try { fcollection.add(ftContour.create(featureAttribs)); } catch
//         * (IllegalAttributeException e) { e.printStackTrace(); return; }
//         */
//
//        // fcollection.add(new Feature(geometry.));
//    }
//
//    /**
//     * Creates a FeatureCollection using only geometries
//     * 
//     * @param geomName
//     * @param geomClass
//     * @param ctor
//     * @return 
//     * @deprecated this method should not be used. It is based on too much deprecated stuff! There
//     *             are already methods for featurecollection creation in the
//     *             {@link FeatureUtilities} methods. This class will be erased as soon as possible.
//     */
//    @SuppressWarnings({"deprecation", "unchecked"})
//    public FeatureCollectionUtils( String geomName, List<Geometry> geometryVector ) {
//        fcollection = FeatureCollections.newCollection();
//        for( Geometry geometry : geometryVector ) {
//            /*
//             * define the geometry field
//             */
//            // create a class
//            Class<Geometry> geomClass = Geometry.class;
//            AttributeType[] attributesArray = new AttributeType[1];
//            GeometryAttributeType geometryAttribute = (GeometryAttributeType) AttributeTypeFactory
//                    .newAttributeType(geomName, geomClass);
//            attributesArray[0] = geometryAttribute;
//            FeatureType ftContour = null;
//            try {
//                ftContour = FeatureTypeFactory.newFeatureType(attributesArray, "default");
//            } catch (FactoryConfigurationError e1) {
//                e1.printStackTrace();
//            } catch (SchemaException e1) {
//                e1.printStackTrace();
//            }
//
//            // create the feature attributes
//            Object[] featureAttribs = new Object[attributesArray.length];
//            featureAttribs[0] = geometry;
//
//            // create the feature
//            try {
//                fcollection.add(ftContour.create(featureAttribs));
//            } catch (IllegalAttributeException e) {
//                e.printStackTrace();
//                return;
//            }
//
//            // fcollection.add(new Feature(geometry.));
//        }
//    }
//
//    public FeatureCollection getFeatureCollection() {
//        return fcollection;
//    }
//}
