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

/**
 * The XYPolyline class is a collection of points (at least 2) connected with
 * straigth lines. Polylines are typically used for presentation of 1D data,
 * e.g. river networks.
 */
public class XYPolyline extends XYGeometry {

    protected XYPoints points;

    /**
     * Create an instance with no points.
     */
    public XYPolyline() {
        points = new XYPoints();
    }

    /**
     * Create an instance and copy values from the specified instance.
     *
     * @param xyPolyline Polyline to copy
     */
    public XYPolyline(XYPolyline xyPolyline) {
        this();
        for (XYPoint p : xyPolyline.getPoints()) {
            points.add(new XYPoint(p.getX(), p.getY()));
        }
    }

    /**
     * Get the collection of XYPoints.
     *
     * @return Point collection
     */
    public XYPoints getPoints() {
        return points;
    }

    /**
     * Retrieve the x-coordinate of the point at the given index.
     *
     * @param index Index number of the point
     * @return X-coordinate of the point in the polyline
     */
    public double getX(int index) {
        return points.get(index).getX();
    }

    /**
     * Retrieve the y-coordinate of the point at the given index.
     *
     * @param index Index number of the point
     * @return Y-coordinate of the point in the polyline
     */
    public double getY(int index) {
        return points.get(index).getY();
    }

    /**
     * Retrieve the line segment of the polyline by index.
     *
     * @param lineNumber Index number of the line to retrieve
     * @return The line segment of the polyline
     */
    public XYLine getLine(int lineNumber) {
        return new XYLine(points.get(lineNumber), points.get(lineNumber + 1));
    }

    /**
     * Calculate the length of the polyline.
     *
     * @return The length of the polyline
     */
    public double getLength() {
        double length = 0.0;
        XYPoint p1 = null;

        for (XYPoint p2 : points) {
            if (p1 != null) {
                length += XYGeometryTools.calculatePointToPointDistance(p1, p2);
            }

            p1 = p2;
        }

        return length;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return points.equals(((XYPolyline) obj).points);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + points.hashCode();
    }

    /**
     * Check if the XYPolyline is valid. It verifies that:
     * - The number of points is >= 2
     * - The length of all line segments is positive (and not zero)
     *
     * @throws Exception Raised if the contstraints are not met
     */
    public void validate() throws Exception {
        if (points.size() < 2) {
            throw new Exception("Number of vertices in polyline element is less than 2.");
        }

        for (int j = 0; j < points.size() - 1; j++) {
            try {
                getLine(j).validate();
            }
            catch (Exception e) {
                throw new Exception(String.format("Line segment (%d) is invalid: %s", j, e.getMessage()));
            }
        }
    }

}
