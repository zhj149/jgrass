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
 * @author Rob Knapen, Alterra B.V., The Netherlands
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.backbone;

import java.io.Serializable;

/**
 * The Vertex class contains a (x,y,z) coordinate. It is part of the
 * implementation of the OpenMI ElementSet in the backbone package.
 */
public class Vertex implements Serializable {

    private double x;
    private double y;
    private double z;

    /**
     * Creates a default (0, 0, 0) vertex.
     */
    public Vertex() {
        this(0, 0, 0);
    }

    /**
     * Creates a vertex copied from a specified vertex.
     *
     * @param source The vertex to copy
     */
    public Vertex(Vertex source) {
        this(source.x, source.y, source.z);
    }

    /**
     * Creates a vertex with the specified coordinates.
     *
     * @param x X position
     * @param y Y position
     * @param z Z position
     */
    public Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Gets the X position.
     *
     * @return X position
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the Y position.
     *
     * @return Y position
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the Z position.
     *
     * @return Z position
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the X position.
     *
     * @param d The X position
     */
    public void setX(double d) {
        x = d;
    }

    /**
     * Sets the Y position.
     *
     * @param d The Y position
     */
    public void setY(double d) {
        y = d;
    }

    /**
     * Sets the Z position.
     *
     * @param d The Z position
     */
    public void setZ(double d) {
        z = d;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        Vertex v = (Vertex) obj;
        return (this.x == v.x && this.y == v.y && this.z == v.z);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Double.valueOf(x).hashCode() +
                Double.valueOf(y).hashCode() + Double.valueOf(z).hashCode();
    }

    @Override
    public String toString() {
        return String.format("Vertex: %f %f %f", x, y, z);
    }

}
