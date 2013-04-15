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
 * The XYLine class is used for representing line segments. XYPolylines and
 * XYPolygons are composed of XYLines.
 */
public class XYLine extends XYGeometry {

    private XYPoint p1;
    private XYPoint p2;

    /**
     * Create an instance from two default XYPoints.
     */
    public XYLine() {
        p1 = new XYPoint();
        p2 = new XYPoint();
    }

    /**
     * Create an instance with the given coordinates.
     *
     * @param x1 X-coordinate for line start point
     * @param y1 Y-coordinate for line start point
     * @param x2 X-coordinate for line end point
     * @param y2 Y-coordinate for line end point
     */
    public XYLine(double x1, double y1, double x2, double y2) {
        p1 = new XYPoint(x1, y1);
        p2 = new XYPoint(x2, y2);
    }

    /**
     * Create an instance with coordinates copied from the give XYPoints.
     *
     * @param point1 Line start point
     * @param point2 Line end point
     */
    public XYLine(XYPoint point1, XYPoint point2) {
        p1 = new XYPoint(point1);
        p2 = new XYPoint(point2);
    }

    /**
     * Create an instance and copy coordinates from the given XYLine.
     *
     * @param line XYLine to copy
     */
    public XYLine(XYLine line) {
        p1 = new XYPoint(line.getP1());
        p2 = new XYPoint(line.getP2());
    }

    /**
     * Get the first point.
     *
     * @return First XYPoint of the line
     */
    public XYPoint getP1() {
        return p1;
    }

    /**
     * Get the second point.
     *
     * @return Second XYPoint of the line
     */
    public XYPoint getP2() {
        return p2;
    }

    /**
     * Calculate the length of line.
     *
     * @return Line length
     */
    public double getLength() {
        return XYGeometryTools.calculatePointToPointDistance(p1, p2);
    }

    /**
     * Calculate the mid point of the line.
     *
     * @return The line mid point as a XYPoint
     */
    public XYPoint getMidpoint() {
        return new XYPoint((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return getP1().equals(((XYLine) obj).getP1()) && getP2().equals(((XYLine) obj).getP2());
    }

    @Override
    public int hashCode() {
        return super.hashCode() + getP1().hashCode() + getP2().hashCode();
    }

    /**
     * Validate the line geometry.
     *
     * @throws Exception When length of the line is zero.
     */
    public void validate() throws Exception {
        if (Double.compare(getLength(), 0.0) == 0) {
            throw new Exception("Length of line is zero");
        }
    }

}
