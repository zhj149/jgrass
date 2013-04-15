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
import org.openmi.standard.IElementSet.ElementType;

import java.util.ArrayList;

/**
 * The XTGeometryYools class is a collection of general geometry functions.
 * All functions are static methods that perform calculations on input given
 * as parameters and return a result.
 * 
 * The parameters passed to the XYGeometryTools methods are typically of type
 * XYPoint, XYLine, XYPolyline or XYPolygon, defined in this package.
 */
public class XYGeometryTools {
    
    /**
     * Epsilon value used in floating point calculations.
     */
    private static final double EPSILON = 1e-5;

    /**
     * Create an XYPoint instance for the indexed location in a given element set.
     *
     * @param s     The IElementSet to create the XYPoint from
     * @param index The location in the element set
     * @return The created XYPoint
     * @throws Exception when the IElementSet does not contain XYPoints
     */
    public static XYPoint createXYPoint(IElementSet s, int index)
            throws Exception {
        if (s.getElementType() != IElementSet.ElementType.XYPoint) {
            throw new Exception("Cannot create XYPoint");
        }

        return new XYPoint(s.getXCoordinate(index, 0), s.getYCoordinate(index, 0));
    }

    /**
     * Create an XYPolyline instance for the indexed location in a given element set.
     *
     * @param s     The IElementSet to create the XYPolyline from
     * @param index The location in the element set
     * @return The created XYPolyline
     * @throws Exception when the IElementSet does not contain XYLines or XYPolylines
     */
    public static XYPolyline createXYPolyline(IElementSet s, int index)
            throws Exception {
        if (!(s.getElementType() == ElementType.XYPolyLine || s.getElementType() == ElementType.XYLine)) {
            throw new Exception("Cannot create XYPolyline");
        }

        XYPolyline l = new XYPolyline();
        for (int i = 0; i < s.getVertexCount(index); i++) {
            l.getPoints().add(new XYPoint(s.getXCoordinate(index, i), s.getYCoordinate(index, i)));
        }

        return l;
    }

    /**
     * Create an XYPolygon instance for the indexed location in a given element set.
     *
     * @param s     The IElementSet to create the XYPolygon from
     * @param index The location in the element set
     * @return The created XYPolygon
     * @throws Exception when the IElementSet does not contain XYPolygons
     */
    public static XYPolygon createXYPolygon(IElementSet s, int index)
            throws Exception {
        if (s.getElementType() != ElementType.XYPolygon) {
            throw new Exception("Cannot create XYPolygon");
        }

        XYPolygon xyPolygon = new XYPolygon();

        for (int i = 0; i < s.getVertexCount(index); i++) {
            xyPolygon.getPoints().add(new XYPoint(s.getXCoordinate(index, i), s.getYCoordinate(index, i)));
        }

        return xyPolygon;
    }

    /**
     * Static method that validates an object with an IElementSet interface. The method
     * raises an Exception in case IElementSet does not describe a valid ElementSet.
     * <p/>
     * The checks made are:
     * <p>ElementType: Check</p>
     * <p>XYPoint:     Only one vertex in each element.</p>
     * <p>XYPolyline:  At least two vertices in each element.</p>
     * <p>             All line segments in each element has length > 0</p>
     * <p>XYPolygon:   At least three vertices in each element.</p>
     * <p>             Area of each element is larger than 0</p>
     * <p>             All line segments in each element has length > 0</p>
     * <p>             No line segments within an element crosses.</p>
     *
     * @param elementSet Object that implement the IElementSet interface
     * @throws Exception
     */
    public static void checkElementSet(IElementSet elementSet) throws Exception {
        try {
            switch (elementSet.getElementType()) {
                case XYPoint:
                    for (int i = 0; i < elementSet.getElementCount(); i++) {
                        if (elementSet.getVertexCount(i) != 1) {
                            throw new Exception(String.format("Number of vertices in point element (%d) is different from 1.", i));
                        }
                    }
                    break;

                case XYPolyLine:
                    for (int i = 0; i < elementSet.getElementCount(); i++) {
                        XYGeometryTools.createXYPolyline(elementSet, i).validate();
                    }
                    break;

                case XYPolygon:
                    for (int i = 0; i < elementSet.getElementCount(); i++) {
                        XYGeometryTools.createXYPolygon(elementSet, i).validate();
                    }
                    break;
            }
        }
        catch (Exception e) {
            throw new Exception("ElementSet with ID = " + elementSet.getID() + " is invalid", e);
        }
    }

    /**
     * Returns the distance between the two points.
     *
     * @param p1 Point
     * @param p2 Point
     * @return Point to point distance
     */
    public static double calculatePointToPointDistance(XYPoint p1, XYPoint p2) {
        return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) *
                (p1.getY() - p2.getY()));
    }

    /**
     * Returns true if two line segments intersects. The lines are said to intersect if the lines
     * actually crosses and not if they only share a point.
     *
     * @param x1 x-coordiante for first point in first line segment
     * @param y1 y-coordinate for first point in first line segment
     * @param x2 x-cooedinate for second point in first line segment
     * @param y2 y-coordinate for second point in first line segment
     * @param x3 x-coordinate for the first point in second line segment
     * @param y3 y-coordinate for the first point in second line segment
     * @param x4 x-coordinate for the second point in the second line segment
     * @param y4 y-coordinate for the second point in the second line segment
     * @return true if the line segments intersects otherwise false
     */
    public static boolean doLineSegmentsIntersect(double x1, double y1, double x2, double y2, double x3,
            double y3, double x4, double y4) {
        double detP1P2P3,
         detP1P2P4,
         detP3P4P1,
         detP3P4P2;
        boolean intersect = false;

        detP1P2P3 = (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1);
        detP1P2P4 = (x2 - x1) * (y4 - y1) - (x4 - x1) * (y2 - y1);
        detP3P4P1 = (x3 - x1) * (y4 - y1) - (x4 - x1) * (y3 - y1);
        detP3P4P2 = detP1P2P3 - detP1P2P4 + detP3P4P1;

        if ((detP1P2P3 * detP1P2P4 < 0) && (detP3P4P1 * detP3P4P2 < 0)) {
            intersect = true;
        }
        return intersect;
    }

    /**
     * OverLoad of DoLineSegmentsIntersect(x1, y1, x2, y2, x3, y3, x4, y4).
     *
     * @param p1 First point in first line
     * @param p2 Second point in first line
     * @param p3 First point in second line
     * @param p4 Second point in second line
     * @return true if the line segments intersects otherwise false
     */
    public static boolean doLineSegmentsIntersect(XYPoint p1, XYPoint p2, XYPoint p3, XYPoint p4) {
        return doLineSegmentsIntersect(p1.getX(), p1.getY(), p2.getX(), p2.getY(),
                p3.getX(), p3.getY(), p4.getX(), p4.getY());
    }

    /**
     * OverLoad of DoLineSegmentsIntersect(x1, y1, x2, y2, x3, y3, x4, y4).
     *
     * @param line1 First line
     * @param line2 Second line
     * @return true if the line segments intersects otherwise false
     */
    public static boolean doLineSegmentsIntersect(XYLine line1, XYLine line2) {
        return doLineSegmentsIntersect(line1.getP1().getX(), line1.getP1().getY(),
                line1.getP2().getX(), line1.getP2().getY(), line2.getP1().getX(),
                line2.getP1().getY(), line2.getP2().getX(), line2.getP2().getY());
    }

    /**
     * Calculate intersection point between two line segments.
     *
     * @param p1 First point in first line
     * @param p2 Second point in first line
     * @param p3 First point in second line
     * @param p4 Second point in second line
     * @return Intersection point
     */
    public static XYPoint calculateIntersectionPoint(XYPoint p1, XYPoint p2, XYPoint p3, XYPoint p4)
            throws Exception {
        if (!doLineSegmentsIntersect(p1, p2, p3, p4)) {
            throw new Exception("Attempt to calculate intersection point between non intersecting lines. CalculateIntersectionPoint failed.");
        }
        XYPoint interSectionPoint = new XYPoint();

        double a = p1.getX() * p2.getY() - p2.getX() * p1.getY();
        double b = p3.getX() * p4.getY() - p4.getX() * p3.getY();
        double c = (p1.getX() - p2.getX()) * (p3.getY() - p4.getY()) -
                (p3.getX() - p4.getX()) * (p1.getY() - p2.getY());

        interSectionPoint.setX((a * (p3.getX() - p4.getX()) - (b * (p1.getX() - p2.getX()))) / c);
        interSectionPoint.setY((a * (p3.getY() - p4.getY()) - (b * (p1.getY() - p2.getY()))) / c);

        return interSectionPoint;
    }

    /**
     * OverLoad of CalculateIntersectionPoint(XYPoint p1, XYPoint p2, XYPoint p3, XYPoint p4).
     *
     * @param line1 first line
     * @param line2 second line
     * @return Intersection point
     */
    public static XYPoint calculateIntersectionPoint(XYLine line1, XYLine line2) throws Exception {
        return calculateIntersectionPoint(line1.getP1(), line1.getP2(), line2.getP1(), line2.getP2());
    }

    /**
     * Calculates the length of polyline inside polygon. Lines segments on the edges of
     * polygons are included with half their length.
     *
     * @param polyline the polyline
     * @param polygon  the polygon
     * @return Length of polyline inside polygon.
     */
    public static double calculateLengthOfPolylineInsidePolygon(XYPolyline polyline, XYPolygon polygon)
            throws Exception {
        double lengthInside = 0;
        double numberOfLineSegments = polyline.getPoints().size() - 1;
        for (int i = 0; i < numberOfLineSegments; i++) {
            XYLine line = new XYLine(polyline.getLine(i));
            lengthInside += calculateLengthOfLineInsidePolygon(line, polygon);
        }
        return lengthInside;
    }

    /**
     * Calculates length of line inside polygon. WorkflowParts of the line that is on the edge of
     * the polygon only counts with half their length.
     *
     * @param line    the line
     * @param polygon the polygon
     * @return Length of line inside polygon.
     */
    public static double calculateLengthOfLineInsidePolygon(XYLine line, XYPolygon polygon)
            throws Exception {
        ArrayList<XYLine> lineList = new ArrayList<XYLine>();
        lineList.add(new XYLine(line));

        // For all lines in the polygon
        for (int i = 0; i < polygon.getPoints().size(); i++) {
            for (int n = 0; n < lineList.size(); n++) {
                if (doLineSegmentsIntersect(lineList.get(n), polygon.getLine(i))) {
                    // Split the intersecting line into two lines
                    XYPoint intersectionPoint = new XYPoint(calculateIntersectionPoint(lineList.get(n), polygon.getLine(i)));
                    lineList.add(new XYLine(intersectionPoint, (lineList.get(n)).getP2()));
                    (lineList.get(n)).getP2().setX(intersectionPoint.getX());
                    (lineList.get(n)).getP2().setY(intersectionPoint.getY());
                }
            }
        }

        for (int i = 0; i < lineList.size(); i++) {
            for (int j = 0; j < polygon.getPoints().size(); j++) {
                if (isPointInLineInterior(polygon.getLine(j).getP1(), lineList.get(i))) {
                    lineList.add(new XYLine(polygon.getLine(j).getP1(), (lineList.get(i)).getP2()));
                    (lineList.get(i)).getP2().setX(polygon.getLine(j).getP1().getX());
                    (lineList.get(i)).getP2().setY(polygon.getLine(j).getP1().getY());
                }
            }
        }

        double lengthInside = 0;
        for (XYLine l : lineList) {
            double sharedLength = 0;
            for (int j = 0; j < polygon.points.size(); j++) {
                sharedLength += calculateSharedLength(l, polygon.getLine(j));
            }

            if (sharedLength > EPSILON) {
                lengthInside += sharedLength / 2;
            }
            else if (isPointInPolygon(l.getMidpoint(), polygon)) {
                lengthInside += l.getLength();
            }
        }

        return lengthInside;
    }

    /**
     * The method calculates the intersection area of triangle a and b both of type
     * XYPolygon.
     *
     * @param triangleA Triangle of type XYPolygon
     * @param triangleB Triangle of type XYPolygon
     * @return Intersection area between the triangles triangleA and triAngleB
     */
    public static double triangleIntersectionArea(XYPolygon triangleA, XYPolygon triangleB)
            throws Exception {
        try {
            if (triangleA.points.size() != 3 || triangleB.points.size() != 3) {
                throw new Exception("Argument must be a polygon with 3 points");
            }

            IntWrapper i = new IntWrapper(1);
            IntWrapper j = new IntWrapper(-1);

            // -1 indicates that the first has not yet been found

            double area;            // Intersection area. Returned.
            XYPolygon intersectionPolygon = new XYPolygon(); // Intersection polygon

            XYPoint p = new XYPoint(triangleA.points.get(0));
            intersect(triangleA, triangleB, p, i, j, intersectionPolygon);
            XYPoint pFirst = new XYPoint(p);

            if (j.getValue() != -1) {
                boolean complete = false;
                int count = 0;
                while (!complete) {
                    // coordinates for vectors pointing to next triangleA and triangleB point respectively
                    double vax = (triangleA.points.get(i.getValue())).getX() - p.getX();
                    double vay = (triangleA.points.get(i.getValue())).getY() - p.getY();
                    double vbx = (triangleB.points.get(j.getValue())).getX() - p.getX();
                    double vby = (triangleB.points.get(j.getValue())).getY() - p.getY();

                    if (isPointInPolygonOrOnEdge(p.getX() + EPSILON * vax, p.getY() + EPSILON * vay, triangleB)) {
                        intersect(triangleA, triangleB, p, i, j, intersectionPolygon);
                    }
                    else if (isPointInPolygonOrOnEdge(p.getX() + EPSILON * vbx, p.getY() + EPSILON * vby, triangleA)) {
                        intersect(triangleB, triangleA, p, j, i, intersectionPolygon);
                    }
                    else {
                        // triangleA and triangleB only touches one another but do not intersect
                        area = 0;
                        return area;
                    }

                    if (intersectionPolygon.points.size() > 1) {
                        complete = (calculatePointToPointDistance(p, pFirst) < EPSILON);
                    }

                    count++;
                    if (count > 20) {
                        throw new Exception("Failed to find intersection polygon");
                    }
                }
                area = intersectionPolygon.getArea();
            }
            else {
                XYPoint pa = new XYPoint(); // internal point in triangle a
                XYPoint pb = new XYPoint(); // internal point in triangle b

                pa.setX((triangleA.getX(0) + triangleA.getX(1) + triangleA.getX(2)) / 3);
                pa.setY((triangleA.getY(0) + triangleA.getY(1) + triangleA.getY(2)) / 3);
                pb.setX((triangleB.getX(0) + triangleB.getX(1) + triangleB.getX(2)) / 3);
                pb.setY((triangleB.getY(0) + triangleB.getY(1) + triangleB.getY(2)) / 3);

                // triangleA is completely inside triangleB
                // triangleA and triangleB do dot intersect
                if (isPointInPolygon(pa, triangleB) || isPointInPolygon(pb, triangleA)) {
                    area = Math.min(triangleA.getArea(), triangleB.getArea());
                }
                else {
                    area = 0;
                }
            }
            return area;
        }
        catch (Exception e) {
            throw new Exception("TriangleIntersectionArea failed", e);
        }
    }

    /**
     * The method calculates the intersection points of triangle a and b both of type
     * XYPolygon.
     *
     * @param triangleA           triangle. The search is started along triangleA.
     * @param triangleB           triangle. Intersection with this triangle are sought.
     * @param p                   Starting point for the search. p must be part of triangleA.
     * @param i                   on input: End index for the first line segment of triangleA in the
     *                            search. on output: End index for the last intersected line segment in triangleA.
     * @param j                   on input: -1 if vertices before intersection is not to be added to
     *                            list. on output: End index for last intersected line segment of triangleB.
     * @param intersectionPolygon polygon eventuallu describing the  intersection
     *                            area between triangleA and triangleB
     */
    private static void intersect(XYPolygon triangleA, XYPolygon triangleB,
            XYPoint p, IntWrapper i, IntWrapper j, XYPolygon intersectionPolygon)
            throws Exception {
        XYLine lineA;
        XYLine lineB;
        int im1 = decrease(i.getValue(), 2); // "i-1"
        int count1 = 0;
        boolean found = false;

        while ((count1 < 3) && (!found)) {
            lineA = triangleA.getLine(im1);
            if (count1 == 0) {
                lineA.getP1().setX(p.getX());
                lineA.getP1().setY(p.getY());
            }
            double minDist = -1; // Distance used when a line is crossed more than once
            int jm1 = 0;         // "j-1"
            int jm1Store = -1;
            while (jm1 < 3) {
                lineB = triangleB.getLine(jm1);
                found = intersectionPoint(lineA, lineB, p);
                double dist = calculatePointToPointDistance(lineA.getP1(), p);
                if (dist < EPSILON) {
                    found = false;
                }
                if (found) {
                    if ((minDist < 0) || (dist < minDist)) {
                        minDist = dist;
                        jm1Store = jm1;
                    }
                }
                jm1++;
            }
            if (jm1Store > -1) {
                lineB = triangleB.getLine(jm1Store);
                found = intersectionPoint(lineA, lineB, p);
                XYPoint helpCoordinate = new XYPoint(p.getX(), p.getY());
                XYPoint helpNode = new XYPoint(helpCoordinate);

                intersectionPolygon.points.add(helpNode);

                j.setValue(increase(jm1Store, 2));
            }
            if (!found) {
                count1++;
                im1 = increase(im1, 2);
                i.circularIncrease(2);
                if (j.getValue() != -1) {
                    XYPoint helpCoordinate = new XYPoint(lineA.getP2().getX(), lineA.getP2().getY());
                    XYPoint helpNode = new XYPoint(helpCoordinate);
                    intersectionPolygon.points.add(helpNode);
                }
            }
        }
        lineA = triangleA.getLine(decrease(i.getValue(), 2));
        if (calculatePointToPointDistance(p, lineA.getP2()) < EPSILON) {
            i.circularIncrease(2);
        }
        lineB = triangleB.getLine(decrease(j.getValue(), 2));
        if (calculatePointToPointDistance(p, lineB.getP2()) < EPSILON) {
            j.circularIncrease(2);
        }
    }

    /**
     * The method steps to the next index in a circular list 0, 1 ..., n.
     *
     * @param i Index to increase.
     * @param n Largest index
     * @return The increased index.
     */
    private static int increase(int i, int n) {
        i++;
        if (i > n) {
            i = 0;
        }
        return i;
    }

    /**
     * The method steps to the previous index in a circular list 0, 1 ..., n.
     *
     * @param i Index to decrease
     * @param n Largest index
     * @return The updated index
     */
    private static int decrease(int i, int n) {
        i--;
        if (i < 0) {
            i = n;
        }
        return i;
    }

    /**
     * Check if two lines shares a point either by real intersection or
     * by endpoints being in the other line. In case of intersection the
     * intersection point is returned in the parameter p.
     *
     * @param linea             Line
     * @param lineb             Line
     * @param intersectionPoint Intersection point
     * @return True if intersection
     */
    protected static boolean intersectionPoint(XYLine linea, XYLine lineb, XYPoint intersectionPoint)
            throws Exception {
        if (doLineSegmentsIntersect(linea, lineb)) {
            intersectionPoint.set(calculateIntersectionPoint(linea, lineb));
            return true;
        }
        if (isPointInLine(linea.getP2(), lineb)) {
            intersectionPoint.set(linea.getP2());
            return true;
        }
        if (isPointInLine(lineb.getP2(), linea)) {
            intersectionPoint.set(lineb.getP2());
            return true;
        }
        if (isPointInLine(lineb.getP1(), linea)) {
            intersectionPoint.set(lineb.getP1());
            return true;
        }
        if (isPointInLine(linea.getP1(), lineb)) {
            intersectionPoint.set(linea.getP1());
            return true;
        }
        return false;
    }

    /**
     * Determine if a point is included in a line either in the interior or as one of the end points.
     *
     * @param x    x-coordinate
     * @param y    y-coordinate
     * @param line the selected line
     * @return True of the point is on the line, false otherwise
     */
    protected static boolean isPointInLine(double x, double y, XYLine line) {
        boolean result = false;
        if (line.getP1().getX() - line.getP2().getX() != 0) {
            if ((x >= Math.min(line.getP1().getX(), line.getP2().getX())) &&
                    (x <= Math.max(line.getP1().getX(), line.getP2().getX()))) {
                if (Math.abs(y - line.getP1().getY() - (line.getP2().getY() - line.getP1().getY()) /
                        (line.getP1().getX() - line.getP2().getX()) * (line.getP1().getX() - x)) < EPSILON * EPSILON) {
                    result = true;
                }
            }
        }
        else {
            if (line.getP1().getX() == x) {
                if ((y >= Math.min(line.getP1().getY(), line.getP2().getY())) &&
                        (y <= Math.max(line.getP1().getY(), line.getP2().getY()))) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Overload to: IsPointInLine(double x, double y, XYLine line).
     *
     * @param point Point
     * @param line  Line
     * @return True if point is on the line, false otherwise
     */
    public static boolean isPointInLine(XYPoint point, XYLine line) {
        return isPointInLine(point.getX(), point.getY(), line);
    }

    /**
     * Calculate the distance from a polyline to a point in the plane. The algorithm
     * decides weather the point lies besides the line segment in which case the
     * distance is the length along a line perpendicular to the line. Alternatively
     * the distance is the smallest of the distances to either endpoint.
     *
     * @param line  Line
     * @param point Point
     * @return Length of the shortest path between the line and the point
     */
    public static double calculateLineToPointDistance(XYLine line, XYPoint point) {
        double dist;

        double a = Math.sqrt((line.getP2().getX() - point.getX()) * (line.getP2().getX() - point.getX()) +
                (line.getP2().getY() - point.getY()) * (line.getP2().getY() - point.getY()));
        double b = Math.sqrt((line.getP2().getX() - line.getP1().getX()) * (line.getP2().getX() - line.getP1().getX()) +
                (line.getP2().getY() - line.getP1().getY()) * (line.getP2().getY() - line.getP1().getY()));
        double c = Math.sqrt((line.getP1().getX() - point.getX()) * (line.getP1().getX() - point.getX()) +
                (line.getP1().getY() - point.getY()) * (line.getP1().getY() - point.getY()));

        if ((a == 0) || (c == 0)) {
            dist = 0;
        }
        else if (b == 0) {
            dist = a;
        }
        else {
            double alpha = Math.acos((b * b + c * c - a * a) / (2 * b * c));
            double beta = Math.acos((a * a + b * b - c * c) / (2 * a * b));
            if (Math.max(alpha, beta) < Math.PI / 2) {
                dist = Math.abs((line.getP2().getX() - line.getP1().getX()) * (line.getP1().getY() - point.getY()) -
                        (line.getP1().getX() - point.getX()) * (line.getP2().getY() - line.getP1().getY())) / b;
            }
            else {
                dist = Math.min(a, c);
            }
        }
        return dist;
    }

    /**
     * Finds the shortest distance between any line segment of the polyline and the
     * point.
     *
     * @param polyLine PolyLine
     * @param point    Point
     * @return Length of the shortest path between the polyline and the point
     */
    public static double calculatePolylineToPointDistance(XYPolyline polyLine, XYPoint point) {
        double dist = 0;
        int i = 0;
        while (i < polyLine.points.size() - 1) {
            if (i == 0) {
                dist = calculateLineToPointDistance(polyLine.getLine(0), point);
            }
            else {
                dist = Math.min(dist, calculateLineToPointDistance(polyLine.getLine(i), point));
            }
            i++;
        }
        return dist;
    }

    /**
     * Determines if a point in inside or outside a polygon. Works for both convex and
     * concave polygons (Winding number test).
     *
     * @param point   Point
     * @param polygon Polygon
     * @return True if the point is inside the polygon, false otherwise
     */
    public static boolean isPointInPolygon(XYPoint point, XYPolygon polygon) {
        return isPointInPolygon(point.getX(), point.getY(), polygon);
    }

    /**
     * Determines if a point in inside or outside a polygon. Works for both convex and
     * concave polygons (Winding number test).
     *
     * @param x       x-coordinate for the point
     * @param y       y-coordiante for the point
     * @param polygon Polygon
     * @return True if the point is inside the polygon, false otherwise
     */
    public static boolean isPointInPolygon(double x, double y, XYPolygon polygon) {
        double x1,
         x2,
         y1,
         y2;
        double xinters;
        boolean isInside = false;
        int n = polygon.points.size();

        for (int i = 0; i < n; i++) {
            if (i < n - 1) {
                x1 = (polygon.points.get(i)).getX();
                x2 = (polygon.points.get(i + 1)).getX();
                y1 = (polygon.points.get(i)).getY();
                y2 = (polygon.points.get(i + 1)).getY();
            }
            else {
                x1 = (polygon.points.get(n - 1)).getX();
                x2 = (polygon.points.get(0)).getX();
                y1 = (polygon.points.get(n - 1)).getY();
                y2 = (polygon.points.get(0)).getY();
            }

            if (y > Math.min(y1, y2)) {
                if (y <= Math.max(y1, y2)) {
                    if (x <= Math.max(x1, x2)) {
                        if (y1 != y2) {
                            xinters = (y - y1) * (x2 - x1) / (y2 - y1) + x1;
                            if (x1 == x2 || x <= xinters) {
                                isInside = !isInside;
                            }
                        }
                    }
                }
            }
        }
        return isInside;
    }

    /**
     * Determines if a point in inside or outside a polygon. Inside includes on the
     * edge for this method. Works for both convex and concave polygons (Winding
     * number test).
     *
     * @param x       x-coordinate for the point
     * @param y       y-coordiante for the point
     * @param polygon Polygon
     * @return True if point is inside the polygon or on its edge, false otherwise
     */
    public static boolean isPointInPolygonOrOnEdge(double x, double y, XYPolygon polygon) {
        boolean result = isPointInPolygon(x, y, polygon);
        if (result) {
            return result;
        }
        else {
            int iLine = 0;
            while ((!result) && (iLine < polygon.points.size())) {
                XYLine line = polygon.getLine(iLine);
                result = isPointInLine(x, y, line);
                iLine++;
            }
        }
        return result;
    }

    /**
     * The methods calculates the shared area of two arbitrarily shaped polygons.
     *
     * @param polygonA Polygon
     * @param polygonB Polygon
     * @return The shared area
     */
    public static double calculateSharedArea(XYPolygon polygonA, XYPolygon polygonB)
            throws Exception {
        ArrayList triangleListA = polygonA.getTriangulation();
        ArrayList triangleListB = polygonB.getTriangulation();

        double area = 0;
        for (Object aTriangleListA : triangleListA) {
            XYPolygon triangleA = new XYPolygon((XYPolygon) aTriangleListA);
            for (Object aTriangleListB : triangleListB) {
                XYPolygon triangleB = new XYPolygon((XYPolygon) aTriangleListB);
                area = area + triangleIntersectionArea(triangleA, triangleB);
            }
        }
        return area;
    }

    /**
     * Determines if a point is included in a lines interior. I.e. included in the line and not an endpoint.
     *
     * @param x    x-coordinate
     * @param y    y-coordinate
     * @param line Line.
     * @return Determines if a point is included in a line.
     */
    protected static boolean isPointInLineInterior(double x, double y, XYLine line) {
        boolean result = false;
        if (line.getP1().getX() - line.getP2().getX() != 0) {
            if ((x > Math.min(line.getP1().getX(), line.getP2().getX())) &&
                    (x < Math.max(line.getP1().getX(), line.getP2().getX()))) {
                if (Math.abs(y - line.getP1().getY() - (line.getP2().getY() - line.getP1().getY()) /
                        (line.getP1().getX() - line.getP2().getX()) * (line.getP1().getX() - x)) < EPSILON * EPSILON) {
                    result = true;
                }
            }
        }
        else {
            if (line.getP1().getX() == x) {
                if ((y >= Math.min(line.getP1().getY(), line.getP2().getY())) &&
                        (y <= Math.max(line.getP1().getY(), line.getP2().getY()))) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Overload to:IsPointInLineInterior(double x, double y, XYLine line)
     *
     * @param point Point.
     * @param line  Line.
     * @return Determines if a point is included in a line.
     */
    public static boolean isPointInLineInterior(XYPoint point, XYLine line) {
        return isPointInLineInterior(point.getX(), point.getY(), line);
    }

    /**
     * Calculates the length that two lines overlap.
     *
     * @param lineA first line.
     * @param lineB second line.
     * @return Length of shared line segment.
     */
    public static double calculateSharedLength(XYLine lineA, XYLine lineB) {
        if (Math.abs(lineA.getP2().getX() - lineA.getP1().getX()) < EPSILON &&
                Math.abs(lineB.getP2().getX() - lineB.getP1().getX()) < EPSILON &&
                Math.abs(lineA.getP1().getX() - lineB.getP1().getX()) < EPSILON) {
            double YP1A = Math.min(lineA.getP1().getY(), lineA.getP2().getY());
            double YP2A = Math.max(lineA.getP1().getY(), lineA.getP2().getY());
            double YP1B = Math.min(lineB.getP1().getY(), lineB.getP2().getY());
            double YP2B = Math.max(lineB.getP1().getY(), lineB.getP2().getY());

            double YP1 = Math.max(YP1A, YP1B);
            double YP2 = Math.min(YP2A, YP2B);
            if (YP1 < YP2) {
                return YP2 - YP1;
            }
            else {
                return 0;
            }
        }
        else {
            XYPoint P1A,
             P2A;
            if (lineA.getP1().getX() < lineA.getP2().getX()) {
                P1A = lineA.getP1();
                P2A = lineA.getP2();
            }
            else {
                P1A = lineA.getP2();
                P2A = lineA.getP1();
            }
            XYPoint P1B,
             P2B;
            if (lineB.getP1().getX() < lineB.getP2().getX()) {
                P1B = lineB.getP1();
                P2B = lineB.getP2();
            }
            else {
                P1B = lineB.getP2();
                P2B = lineB.getP1();
            }

            double alphaA = (P2A.getY() - P1A.getY()) / (P2A.getX() - P1A.getX());
            double betaA = -alphaA * P2A.getX() + P2A.getY();
            double alphaB = (P2B.getY() - P1B.getY()) / (P2B.getX() - P1B.getX());
            double betaB = -alphaA * P2B.getX() + P2B.getY();
            if (Math.abs(alphaA - alphaB) < EPSILON && Math.abs(betaA - betaB) < EPSILON) {
                double x1 = Math.max(P1A.getX(), P1B.getX());
                double x2 = Math.min(P2A.getX(), P2B.getX());
                if (x1 < x2) {
                    XYLine line = new XYLine(x1, alphaA * x1 + betaA, x2, alphaA * x2 + betaA);
                    return line.getLength();
                }
                else {
                    return 0;
                }
            }
            else {
                return 0;
            }
        }
    }

}