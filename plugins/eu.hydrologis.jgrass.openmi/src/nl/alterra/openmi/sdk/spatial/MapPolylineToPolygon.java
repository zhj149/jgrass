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

import org.openmi.standard.IElementSet;

/**
 * MapPolylineToPolygon is an implementation of a MappingStrategy to be used by
 * the ElementMapper and maps XYPolylines from the source element set on
 * XYPolygons of the target element set.
 */
public class MapPolylineToPolygon extends MappingStrategy {

    /**
     * Fill the mapping matrix with values according to the selected mapping
     * method and the source and target element sets.
     *
     * @param method The mapping method to use
     * @param source The element set to map from
     * @param target The element set to map to
     * @throws Exception
     */
    public void updateMappingMatrix(ElementMappingMethod method, IElementSet source, IElementSet target) throws Exception {
        for (int i = 0; i < numberOfRows; i++) {
            XYPolygon polygon = XYGeometryTools.createXYPolygon(target, i);

            switch (method) {
                case POLYLINE_TO_POLYGON_WEIGHTED_MEAN:
                    double totalLineLengthInPolygon = 0;
                    for (int n = 0; n < numberOfColumns; n++) {
                        XYPolyline polyline = XYGeometryTools.createXYPolyline(source, n);
                        mappingMatrix[i][n] = XYGeometryTools.calculateLengthOfPolylineInsidePolygon(polyline, polygon);
                        totalLineLengthInPolygon += mappingMatrix[i][n];
                    }

                    if (totalLineLengthInPolygon > 0) {
                        for (int n = 0; n < numberOfColumns; n++) {
                            mappingMatrix[i][n] = mappingMatrix[i][n] / totalLineLengthInPolygon;
                        }
                    }
                    break;

                case POLYLINE_TO_POLYGON_WEIGHTED_SUM:
                    for (int n = 0; n < numberOfColumns; n++) {
                        XYPolyline polyline = XYGeometryTools.createXYPolyline(source, n);
                        mappingMatrix[i][n] = XYGeometryTools.calculateLengthOfPolylineInsidePolygon(polyline, polygon) / polyline.getLength();
                    }
                    break;

                default:
                    throw new Exception("Method unknown for polyline to polygon mapping");
            }
        }
    }

}
