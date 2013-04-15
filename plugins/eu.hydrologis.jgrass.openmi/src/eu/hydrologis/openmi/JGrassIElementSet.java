package eu.hydrologis.openmi;

/*****************************************************************************
 *
 *    Copyright (C) 2006 OpenMI Association
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *    or look at URL www.gnu.org/licenses/lgpl.html
 *
 *    Contact info:
 *      URL: www.openmi.org
 *      Email: sourcecode@openmi.org
 *      Discussion forum available at www.sourceforge.net
 *
 *      Coordinator: Roger Moore, CEH Wallingford, Wallingford, Oxon, UK
 *
 *****************************************************************************
 *
 * @author Rob Knapen, Alterra B.V., The Netherlands
 *
 * @author Andrea Antonello (www.hydrologis.com)
 * - added OGC spatial objects
 *
 ****************************************************************************/

import nl.alterra.openmi.sdk.backbone.Element;
import nl.alterra.openmi.sdk.backbone.ElementSet;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IElementSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;

/**
 * Extension of {@link IElementSet} with spatial objects.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 *
 */
public interface JGrassIElementSet extends IElementSet {
    /**
     * The enumeration that holds the different {@link Element} types.
     * 
     * <p>NOTE: This is exactly the same as {@link ElementType} 
     * plus spatial types used in JGrass.</p>
     * 
     * @author Andrea Antonello (www.hydrologis.com)
     */
    public enum JGrassElementType {
        IDBased(0), XYPoint(1), XYLine(2), XYPolyLine(3), XYPolygon(4), XYZPoint(5), XYZLine(6), XYZPolyLine(
                7), XYZPolygon(8), XYZPolyhydron(9), RegionWindow(10), PointCollection(11), LineCollection(
                12), PolygonCollection(13), FeatureCollection(14), GridCoverage2D(15);

        private final int value;

        /**
         * Minimal constructor to use enum with int flag.
         * 
         * @param value The integer flag value
         */
        JGrassElementType( int value ) {
            this.value = value;
        }

        /**
         * int flag value to a given enum base type.
         * 
         * @return The int flag value
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * @return
     */
    // public JGrassElementType getJGrassElementType();
    /**
     * Getter for the {@link JGrassRegion jgrass region window}.
     * 
     * @return the region window.
     */
    public JGrassRegion getRegionWindow();

    /**
     * Getter for the {@link CoordinateReferenceSystem reference system}
     * for the elementset.
     * 
     * @return the CoordinateReferenceSystem of the {@link ElementSet}
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem();

    /**
     * Getter for the wrapped OGC {@link FeatureCollection feature collection}.
     * 
     * @return the wrapped feature collection.
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection();

    /**
     * Getter for the wrapped {@link GridCoverage2D grid coverage}.
     * 
     * @return the wrapped grid coverage.
     */
    public GridCoverage2D getGridCoverage2D();
}
