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
 * MapPolygonToPolygon is an implementation of a MappingStrategy to be used by
 * the ElementMapper and maps XYPolygons from the source element set on
 * XYPolygons of the target element set.
 */
public class MapPolygonToPolygon extends MappingStrategy {

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
            XYPolygon toPolygon = XYGeometryTools.createXYPolygon(target, i);
            double area = toPolygon.getArea();

            for (int j = 0; j < numberOfColumns; j++) {
                XYPolygon fromPolygon = XYGeometryTools.createXYPolygon(source, j);
                mappingMatrix[i][j] = XYGeometryTools.calculateSharedArea(toPolygon, fromPolygon);
            }

            switch (method) {
                case POLYGON_TO_POLYGON_WEIGHTED_MEAN:
                    double denominator = 0;
                    for (int j = 0; j < numberOfColumns; j++) {
                        denominator = denominator + mappingMatrix[i][j];
                    }

                    if (Double.compare(denominator, 0.0) != 0) {
                        for (int j = 0; j < numberOfColumns; j++) {
                            mappingMatrix[i][j] = mappingMatrix[i][j] / denominator;
                        }
                    }
                    break;

                case POLYGON_TO_POLYGON_WEIGHTED_SUM:
                    for (int j = 0; j < numberOfColumns; j++) {
                        mappingMatrix[i][j] = mappingMatrix[i][j] / area;
                    }
                    break;

                default:
                    throw new Exception("Method unknown for polygon to polygon mapping");
            }
        }
    }

}
