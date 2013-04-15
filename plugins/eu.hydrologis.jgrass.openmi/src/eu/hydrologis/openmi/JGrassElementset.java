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

import nl.alterra.openmi.sdk.backbone.ElementSet;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.ISpatialReference;

import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;

/**
 * Extension of {@link ElementSet} with spatial objects.
 * 
 * <p>
 * Spatially addicted ElementSet. Supports geotools OGC {@link FeatureCollection
 * feature collections} and {@link GridCoverage2D grid coverages}.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 * @see GridCoverage2D
 * @see FeatureCollection
 * @see ElementSet
 */
public class JGrassElementset extends ElementSet implements JGrassIElementSet {

    /**
     * Identifier of this {@link ElementSet}.
     */
    private String id = null;

    /** 
     * OGC {@link FeatureCollection feature collection} that can be used to
     * get positions through the contained {@link Geometry geometry objects}.
     */
    private FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;

    /**
     * @deprecated this is replaced by the {@link GridCoverage2D}.
     */
    private JGrassRegion regionWindow = null;

    /**
     * {@link GridCoverage2D grid coverage} object from which the grid properties 
     * can be extracted.
     */
    private GridCoverage2D gridCoverage2D = null;

    /**
     * The {@link CoordinateReferenceSystem} of the contained spatial objects.
     */
    private CoordinateReferenceSystem crs = null;

    private int elementCount;
    private ISpatialReference spatialReference;
    private JGrassElementType elementType;

    /**
     * Constructs an {@link ElementSet} on top of a {@link FeatureCollection 
     * feature collection}.
     * 
     * @param id identifier of the elementset.
     * @param featureCollection the {@link FeatureCollection feature collection} 
     *          to be wrapped.
     * @param crs the {@link CoordinateReferenceSystem spatial reference system} 
     *          of the spatial data. If this is null, the spatial reference system is
     *          queried from the featureCollection parameter.
     */
    public JGrassElementset( String id,
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            CoordinateReferenceSystem crs ) {
        this.id = id;
        if (featureCollection != null) {
            elementCount = featureCollection.size();
            this.featureCollection = featureCollection;
        }
        if (crs != null) {
            this.crs = crs;
        } else {
            if (featureCollection != null) {
                this.crs = featureCollection.getSchema().getCoordinateReferenceSystem();
            }
        }
    }

    /**
     * Constructs an {@link ElementSet} on top of a {@link JGrassRegion jgrass 
     * region window}.
     * 
     * @param id identifier of the elementset.
     * @param region the {@link JGrassRegion region} 
     *          to be wrapped.
     * @param crs the {@link CoordinateReferenceSystem spatial reference system} 
     *          of the spatial data.
     */
    public JGrassElementset( String id, JGrassRegion region, CoordinateReferenceSystem crs ) {
        this.id = id;
        if (region != null) {
            this.regionWindow = region;
        }
        if (crs != null)
            this.crs = crs;
        elementCount = 1;
    }

    /**
     * Constructs an {@link ElementSet} on top of a {@link GridCoverage2D grid 
     * coverage}.
     * 
     * @param id identifier of the elementset.
     * @param coverage the {@link GridCoverage2D coverage} 
     *          to be wrapped.
     * @param crs the {@link CoordinateReferenceSystem spatial reference system} 
     *          of the spatial data. If this is null, the spatial reference system is
     *          queried from the coverage parameter.
     */
    public JGrassElementset( String id, GridCoverage2D coverage, CoordinateReferenceSystem crs ) {
        this.id = id;
        if (coverage != null) {
            this.gridCoverage2D = coverage;
        }
        if (crs != null) {
            this.crs = crs;
        } else {
            this.crs = coverage.getCoordinateReferenceSystem();
        }
        elementCount = 1;
    }

    /**
     * What do they need this for???? - Andrea
     * 
     * @param id
     * @param numcol
     */
    public JGrassElementset( String id, int numcol ) {
        this.id = id;
        elementCount = numcol;
    }

    /**
     * Also pretty useless??? - Andrea
     * 
     * @param description
     * @param ID
     * @param elementType
     * @param spatialReference
     */
    public JGrassElementset( String description, String ID, JGrassElementType elementType,
            ISpatialReference spatialReference ) {
        this.setDescription(description);
        this.setID(ID);
        this.elementType = elementType;
        this.spatialReference = spatialReference;
    }

    public String getElementID( int elementIndex ) {
        // FIXME this is crab!
        if (featureCollection != null) {
            return featureCollection.toString();
        } else if (regionWindow != null) {
            return regionWindow.toString();
        }
        return "column " + (elementIndex + 1);
    }

    public int getElementIndex( String elementID ) {
        // FIXME this is crab!
        if (regionWindow != null) {
            return 1;
        }
        // TODO check if it starts with "column" and in case extract the index
        // -1
        return -1;
    }

    public JGrassElementType getJGrassElementType() {
        if (regionWindow != null) {
            return JGrassElementType.RegionWindow;
        } else if (featureCollection != null) {
            return JGrassElementType.FeatureCollection;
        } else if (gridCoverage2D != null) {
            return JGrassElementType.GridCoverage2D;
        } else {
            return JGrassElementType.IDBased;
        }
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        if (crs != null)
            return crs;
        return null;
    }

    public JGrassRegion getRegionWindow() {
        if (regionWindow != null)
            return regionWindow;
        return null;
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection() {
        return featureCollection;
    }

    public ElementType getElementType() {
        // FIXME this is temporarily not implemented.
        return null;
    }

    public GridCoverage2D getGridCoverage2D() {
        return gridCoverage2D;
    }
}
