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
 * The XYPoint class contains an X and Y coordinate.
 */
public class XYPoint extends XYGeometry {

    private double x;
    private double y;

    /**
     * Create an instance with coordinates (-9999, -9999).
     */
    public XYPoint() {
        x = -9999;
        y = -9999;
    }

    /**
     * Create an instance with the give coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     */
    public XYPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create an instance and copy coordinates from the give instance.
     *
     * @param xypoint Point to copy coordinates from
     */
    public XYPoint(XYPoint xypoint) {
        x = xypoint.getX();
        y = xypoint.getY();
    }

    /**
     * Simultanously set the x and y coordinates.
     *
     * @param x
     * @param y
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Simultanously set the x and y coordinates from a given point.
     *
     * @param p
     */
    public void set(XYPoint p) {
        this.x = p.x;
        this.y = p.y;
    }

    /**
     * Get the X coordinate.
     *
     * @return The X coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Set the X coordinate
     *
     * @param x The x coordinate to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Get the Y coordinate.
     *
     * @return The Y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Set the Y coordinate
     *
     * @param y The y coordinate to set
     */
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object source) {
        if (source == null || getClass() != source.getClass()) {
            return false;
        }
        else {
            XYPoint p = (XYPoint) source;
            return (Double.compare(getX(), p.getX()) == 0) && (Double.compare(getY(), p.getY()) == 0);
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Double.valueOf(getX()).hashCode() + Double.valueOf(getY()).hashCode();
    }

    /**
     * Void method, currently there are no conditions for a point.
     *
     * @throws Exception
     */
    public void validate() throws Exception {
        // void
    }

}