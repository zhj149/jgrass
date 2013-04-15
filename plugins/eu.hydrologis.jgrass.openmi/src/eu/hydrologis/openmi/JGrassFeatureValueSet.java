/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) { 
 * HydroloGIS - www.hydrologis.com                                                   
 * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam                               
 * The JGrass developer team - www.jgrass.org                                         
 * }
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.openmi;

import nl.alterra.openmi.sdk.backbone.ValueSet;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * *
 * <p>
 * A valueset that returns a FeatureCollection
 * </p>
 * 
 *@author Andrea Antonello - www.hydrologis.com
 */
public class JGrassFeatureValueSet extends ValueSet {
    private FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;

    public JGrassFeatureValueSet(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection ) {
        this.featureCollection = featureCollection;
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection() {
        return featureCollection;
    }
}
