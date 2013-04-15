/* ***************************************************************************
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
 * The classes in the utilities package are mostly a direct translation from
 * the C# version. They successfully pass the unit tests (which were also
 * taken from the C# version), but so far no extensive time as been put into
 * them.
 *
 * @author Rob Knapen, Alterra B.V., The Netherlands
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.spatial;

import java.util.ArrayList;
import org.openmi.standard.IElementSet.ElementType;
import static org.openmi.standard.IElementSet.ElementType.*;

/**
 * Element Mapper enumerated type.
 */
public enum ElementMappingMethod {

    /**
     * Use nearest method to map XYPoint elements to XYPoint elements.
     */
    POINT_TO_POINT_NEAREST(100, "Nearest", XYPoint, XYPoint),
    
    /**
     * Use inverse method to map XYPoint elements to XYPoint elements.
     */
    POINT_TO_POINT_INVERSE(101, "Inverse", XYPoint, XYPoint),
    
    /**
     * Use nearest method to map XYPoint elements to XYPolyline elements.
     */
    POINT_TO_POLYLINE_NEAREST(200, "Nearest", XYPoint, XYPolyLine),
    
    /**
     * Use inverse method to map XYPoint elements to XYPolyline elements.
     */
    POINT_TO_POLYLINE_INVERSE(201, "Inverse", XYPoint, XYPolyLine),
    
    /**
     * Use mean method to map XYPoint elements to XYPolygon elements.
     */
    POINT_TO_POLYGON_MEAN(300, "Mean", XYPoint, XYPolygon),
    
    /**
     * Use sum method to map XYPoint elements to XYPolygon elements.
     */
    POINT_TO_POLYGON_SUM(301, "Sum", XYPoint, XYPolygon),
    
    /**
     * Use nearest method to map XYPolyline elements to XYPoint elements.
     */
    POLYLINE_TO_POINT_NEAREST(400, "Nearest", XYPolyLine, XYPoint),
    
    /**
     * Use mean method to map XYPoint elements to XYPolygon elements.
     */
    POLYLINE_TO_POINT_INVERSE(401, "Inverse", XYPolyLine, XYPoint),
    
    /**
     * Use weighted mean method to map XYPolyline elements to XYPolygon elements.
     */
    POLYLINE_TO_POLYGON_WEIGHTED_MEAN(500, "Weighted Mean", XYPolyLine, XYPolygon),
    
    /**
     * Use weighted sum method to map XYPolyline elements to XYPolygon elements.
     */
    POLYLINE_TO_POLYGON_WEIGHTED_SUM(501, "Weighted Sum", XYPolyLine, XYPolygon),
    
    /**
     * Use value method to map XYPolygon elements to XYPoint elements.
     */
    POLYGON_TO_POINT_VALUE(600, "Value", XYPolygon, XYPoint),
    
    /**
     * Use weighted mean method to map XYPolygon elements to XYPolyline elements.
     */
    POLYGON_TO_POLYLINE_WEIGHTED_MEAN(700, "Weighted Mean", XYPolygon, XYPolyLine),
    
    /**
     * Use weighted sum method to map XYPolygon elements to XYPolyline elements.
     */
    POLYGON_TO_POLYLINE_WEIGHTED_SUM(701, "Weighted Sum", XYPolygon, XYPolyLine),
    
    /**
     * Use weighted mean method to map XYPolygon elements to XYPolygon elements.
     */
    POLYGON_TO_POLYGON_WEIGHTED_MEAN(800, "Weighted Mean", XYPolygon, XYPolygon),
    
    /**
     * Use weighted sum method to map XYPolygon elements to XYPolygon elements.
     */
    POLYGON_TO_POLYGON_WEIGHTED_SUM(801, "Weighted Sum", XYPolygon, XYPolygon);
    
    private final int ID;
    private final String description;
    private final ElementType fromElementType;
    private final ElementType toElementType;

    /**
     * Minimal constructor to use enum with int flag.
     *
     * @param ID
     * @param description
     * @param fromType
     * @param toType
     */
    private ElementMappingMethod(int ID, String description, ElementType fromType, ElementType toType) {
        this.ID = ID;
        this.description = description;
        this.fromElementType = fromType;
        this.toElementType = toType;
    }

    /**
     * Get the ID for this mapping.
     *
     * @return Numeric ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Get the description for this mapping method.
     *
     * @return MappingEntry method description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the source element type for the mapping.
     *
     * @return The source element type
     */
    public ElementType getFromElementType() {
        return fromElementType;
    }

    /**
     * Get the target element type for the mapping.
     *
     * @return The target element type
     */
    public ElementType getToElementType() {
        return toElementType;
    }

    /**
     * Gives a list of descriptions (Strings) for available mapping methods
     * given the combination of from and to element types.
     *
     * @param fromType Element type of the source elements
     * @param toType   Element type of the target elements
     * @return ArrayList of method descriptions
     */
    public static ArrayList<String> getAvailableMethodDescriptions(ElementType fromType, ElementType toType) {
        ArrayList<String> descriptions = new ArrayList<String>();

        for (ElementMappingMethod m : ElementMappingMethod.values()) {
            if (m.getFromElementType().equals(fromType)) {
                if (m.getToElementType().equals(toType)) {
                    descriptions.add(m.getDescription());
                }
            }
        }
        return descriptions;
    }

    /**
     * Find the mapping method with the give parameters (i.e. source and target
     * element types and description).
     *
     * @param description Description of the mapping method
     * @param fromType    Source element type of the mapping method
     * @param toType      Target element type of the mapping method
     * @return ElementMappingMethod The found mapping method
     * @throws Exception when no matching method was found
     */
    public static ElementMappingMethod findMappingMethod(String description, ElementType fromType, ElementType toType)
            throws Exception {
        for (ElementMappingMethod m : ElementMappingMethod.values()) {
            if (m.getFromElementType().equals(fromType)) {
                if (m.getToElementType().equals(toType)) {
                    if (m.getDescription().equals(description)) {
                        return m;
                    }
                }
            }
        }

        throw new Exception("Method: " + description +
                " not known for fromElementType: " + fromType.toString() +
                " and to ElementType: " + toType.toString());
    }

    /**
     * Return an appropriate mapping strategy for the specified element mapping
     * method.
     *
     * @param method Element mapping method to create a strategy for
     * @return MappingStrategy for the given mapping method
     */
    public static MappingStrategy mappingMethodFactory(ElementMappingMethod method) {
        switch (method) {
            case POINT_TO_POINT_INVERSE:
            case POINT_TO_POINT_NEAREST:
                return new MapPointToPoint();

            case POINT_TO_POLYGON_MEAN:
            case POINT_TO_POLYGON_SUM:
                return new MapPointToPolygon();

            case POINT_TO_POLYLINE_INVERSE:
            case POINT_TO_POLYLINE_NEAREST:
                return new MapPointToPolyline();

            case POLYGON_TO_POINT_VALUE:
                return new MapPolygonToPoint();

            case POLYGON_TO_POLYGON_WEIGHTED_MEAN:
            case POLYGON_TO_POLYGON_WEIGHTED_SUM:
                return new MapPolygonToPolygon();

            case POLYGON_TO_POLYLINE_WEIGHTED_MEAN:
            case POLYGON_TO_POLYLINE_WEIGHTED_SUM:
                return new MapPolygonToPolyline();

            case POLYLINE_TO_POINT_INVERSE:
            case POLYLINE_TO_POINT_NEAREST:
                return new MapPolylineToPoint();

            case POLYLINE_TO_POLYGON_WEIGHTED_MEAN:
            case POLYLINE_TO_POLYGON_WEIGHTED_SUM:
                return new MapPolylineToPolygon();
        }

        return null;
    }

}