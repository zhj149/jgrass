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
import org.openmi.standard.IVector;

/**
 * The Vector class contains X, Y, Z components.
 */
public class Vector implements IVector, Serializable {

    private double x;
    private double y;
    private double z;

    /**
     * Creates an instance with default values, i.e. a (0, 0, 0) vector.
     */
    public Vector() {
        this(0, 0, 0);
    }

    /**
     * Creates an instance and copy values from the source instance.
     *
     * @param source The source to copy from
     */
    public Vector(IVector source) {
        this(source.getXComponent(), source.getYComponent(), source.getZComponent());
    }

    /**
     * Creates an instance with the specified values.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     */
    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Adds values of a given vector.
     *
     * @param v IVector to add
     */
    public void addVector(IVector v) {
        this.x += v.getXComponent();
        this.y += v.getYComponent();
        this.z += v.getZComponent();
    }

    /**
     * Adds calculated vector m * v + a.
     *
     * @param m Constant multiplier
     * @param a Constant addition
     * @param v IVector
     */
    public void addVector(double m, double a, IVector v) {
        this.x += m * v.getXComponent() + a;
        this.y += m * v.getYComponent() + a;
        this.z += m * v.getZComponent() + a;
    }

    /**
     * Adds calculated vector m * v + a.
     *
     * @param m Constant multiplier
     * @param a IVector addition
     * @param v IVector
     */
    public void addVector(double m, IVector a, IVector v) {
        this.x += m * v.getXComponent() + a.getXComponent();
        this.y += m * v.getYComponent() + a.getYComponent();
        this.z += m * v.getZComponent() + a.getZComponent();
    }

    /**
     * Sets the x, y and z values of the vector.
     *
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Gets the X component.
     *
     * @return The X component
     */
    public double getXComponent() {
        return x;
    }

    /**
     * Gets the Y component.
     *
     * @return The Y component
     */
    public double getYComponent() {
        return y;
    }

    /**
     * Gets the Z component.
     *
     * @return The Z component
     */
    public double getZComponent() {
        return z;
    }

    /**
     * Sets the X component.
     *
     * @param x The X component to set
     */
    public void setXComponent(double x) {
        this.x = x;
    }

    /**
     * Sets the Y component.
     *
     * @param y The Y component to set
     */
    public void setYComponent(double y) {
        this.y = y;
    }

    /**
     * Sets the Z component.
     *
     * @param z The Z component to set
     */
    public void setZComponent(double z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        Vector v = (Vector) obj;
        return ((getXComponent() == v.getXComponent()) &&
                (getYComponent() == v.getYComponent()) &&
                (getZComponent() == v.getZComponent()));
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Double.valueOf(getXComponent()).hashCode() +
                Double.valueOf(getYComponent()).hashCode() +
                Double.valueOf(getZComponent()).hashCode();
    }
    
}