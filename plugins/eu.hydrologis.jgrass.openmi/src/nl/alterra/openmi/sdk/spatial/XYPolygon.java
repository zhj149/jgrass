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

/**
 * The XYPolygon class defines a polygon in the XY plane (no Z coordinate).
 * It has a number of usefull methods and XYPolygon objects are used as
 * argument in a number of the methods in the Spatial namespace. The polygon
 * inherits from the XYPolyline class.
 */
public class XYPolygon extends XYPolyline {

    /**
     * Create an instance with no points.
     */
    public XYPolygon() {
        // void
    }

    /**
     * Create an instance and copy values from a given instance.
     *
     * @param xyPolygon The polygon to copy
     */
    public XYPolygon(XYPolygon xyPolygon) {
        for (XYPoint p : xyPolygon.getPoints()) {
            points.add(new XYPoint(p.getX(), p.getY()));
        }
    }

    /**
     * Calculate the area of the polygon.
     *
     * @return the polygon area
     */
    public double getArea() {
        double x1,
         x2,
         y1,
         y2,
         xN,
         x0,
         yN,
         y0,
         area;
        area = 0.0;

        for (int i = 0; i < points.size() - 1; i++) {
            x1 = (points.get(i)).getX();
            x2 = (points.get(i + 1)).getX();
            y1 = (points.get(i)).getY();
            y2 = (points.get(i + 1)).getY();
            area += x1 * y2 - x2 * y1;
        }

        xN = (points.get(points.size() - 1)).getX();
        x0 = (points.get(0)).getX();
        yN = (points.get(points.size() - 1)).getY();
        y0 = (points.get(0)).getY();
        area += xN * y0 - x0 * yN;

        area = 0.5 * area;

        return area;
    }

    /**
     * Returns the XYline that connects XYPoint LineNumber and the next number (i.e.
     * LineNumber+1 or 0).
     *
     * @param lineNumber 0-based line number
     * @return The XYLine starting at node LineNumber
     */
    @Override
    public XYLine getLine(int lineNumber) {
        int index2;
        if (lineNumber == points.size() - 1) {
            index2 = 0;
        }
        else {
            index2 = lineNumber + 1;
        }

        return new XYLine((points.get(lineNumber)).getX(), (points.get(lineNumber)).getY(),
                (points.get(index2)).getX(), (points.get(index2)).getY());
    }

    /**
     * Returns an ArrayList of triangles of type XYPolygon describing the
     * triangalation of the polygon.
     *
     * @return A triangulation of the polygon
     */
    public ArrayList<XYPolygon> getTriangulation() {
        int i,
         im1,
         ip1,
         n;

        XYPolygon localPolygon = new XYPolygon(this);
        ArrayList<XYPolygon> triangleList = new ArrayList<XYPolygon>();

        while (localPolygon.points.size() > 3) {
            i = localPolygon.findEar();
            n = localPolygon.points.size();
            im1 = i - 1;
            ip1 = i + 1;
            if (i == 0) {
                im1 = n - 1;
            }
            else if (i == n - 1) {
                ip1 = 0;
            }
            XYPoint Nodeim1 = new XYPoint(localPolygon.points.get(im1));
            XYPoint Nodei = new XYPoint(localPolygon.points.get(i));
            XYPoint Nodeip1 = new XYPoint(localPolygon.points.get(ip1));
            XYPolygon triangle = new XYPolygon();
            triangle.points.add(Nodeim1);
            triangle.points.add(Nodei);
            triangle.points.add(Nodeip1);
            triangleList.add(triangle);
            localPolygon.points.remove(i);
        }
        triangleList.add(localPolygon);

        return triangleList;
    }

    /**
     * Decides if the angle at point P(i) is convex or concave.
     *
     * @param pointIndex Index
     * @return True if angle at the indexed point is convex, false if it
     *         is concave.
     */
    public boolean isConvex(int pointIndex) {
        boolean isConvex = true;

        int im1 = pointIndex - 1 < 0 ? points.size() - 1 : pointIndex - 1;  //previous point index
        int ip1 = pointIndex + 1 > points.size() - 1 ? 0 : pointIndex + 1;  //next point index

        double xim1 = (points.get(im1)).getX();
        double yim1 = (points.get(im1)).getY();

        double xi = (points.get(pointIndex)).getX();
        double yi = (points.get(pointIndex)).getY();

        double xip1 = (points.get(ip1)).getX();
        double yip1 = (points.get(ip1)).getY();

        if ((xip1 - xim1) * (yi - yim1) - (xi - xim1) * (yip1 - yim1) > 0) {
            isConvex = false;
        }

        return isConvex;
    }

    /**
     * Finds a set of three consecutive points that form a triangle, that
     * is not intersected by other parts of the polygon.
     *
     * @return Index for the "middle" point of triangle that forms an ear.
     *         The ear is formed by P(i-1), P(i) and P(i+1), where P are points
     *         included in the polygon.</p>
     */
    public int findEar() {
        int i = 0;
        int n = points.size() - 1;
        boolean found = false;

        while ((i < n - 1) && (!found)) {
            if (isConvex(i) && !isIntersected(i)) {
                found = true;
            }
            else {
                i++;
            }
        }

        return i;
    }

    /**
     * The method decides if the triangle formed by  P(i-1), P(i) and
     * P(i+1) from Polygon are intersected by any of the other points
     * of the polygon.
     *
     * @param i Middle index for the three points that forms the triangle
     * @return True if the triangle P(i-1), P(i), P(i+1) is intersected by
     *         other parts of Polygon, false otherwise
     */
    public boolean isIntersected(int i) {
        double x,
         y;
        int n = points.size();

        int im1 = i - 1;
        int ip1 = i + 1;
        if (i == 0) {
            im1 = n - 1;
        }
        else if (i == n - 1) {
            ip1 = 0;
        }

        XYPoint nodeim1 = new XYPoint(points.get(im1));
        XYPoint nodei = new XYPoint(points.get(i));
        XYPoint nodeip1 = new XYPoint(points.get(ip1));
        XYPolygon localPolygon = new XYPolygon();
        localPolygon.points.add(nodeim1);
        localPolygon.points.add(nodei);
        localPolygon.points.add(nodeip1);

        int j = 0;
        boolean intersected = false;
        while (((j < n - 1) && (!intersected))) {
            x = (points.get(j)).getX();
            y = (points.get(j)).getY();

            if (((((j != im1) && (j != i)) && (j != ip1)) && XYGeometryTools.isPointInPolygon(x, y, localPolygon))) {
                return true;
            }
            else {
                j++;
            }
        }
        return false;
    }

    /**
     * The validate method check if the XYPolyline is valid. The checks
     * made are:
     * - is number of points >= 3
     * - is the length of all line segments positive
     * - do any lines cross
     * - is the area positive
     *
     * @throws Exception raised if the constraints are not met
     */
    @Override
    public void validate() throws Exception {
        super.validate();

        if (points.size() < 3) {
            throw new Exception("Number of vertices in polygon element is less than 3.");
        }

        if (getArea() <= 0) {
            throw new Exception("Area of polygon is negative or zero. XYPolygons must be ordered counter clockwise.");
        }

        for (int j = 0; j < points.size(); j++) {
            for (int m = 0; m < j; m++) {
                if (XYGeometryTools.doLineSegmentsIntersect(getLine(j), getLine(m))) {
                    throw new Exception("Line no: " + Integer.toString(j) + " and line no: " +
                            Integer.toString(m) + " of XYPolygon crosses.");
                }
            }
        }
    }

}