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

import org.openmi.standard.IDimension;
import java.io.Serializable;
import java.util.Arrays;

/**
 * The dimension class contains the dimension for a unit.
 */
public class Dimension extends BackboneObject implements IDimension, Serializable {

    private int[] powers;

    /**
     * Creates an instance.
     */
    public Dimension() {
        powers = new int[DimensionBase.values().length];
    }

    /**
     * Returns the power of a base quantity.
     *
     * @param baseQuantity Dimension to get power of
     * @return The power for the specified base quantity dimension
     */
    public double getPower(DimensionBase baseQuantity) {
        return powers[baseQuantity.ordinal()];
    }

    @Override
    public boolean describesSameAs(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!super.describesSameAs(obj)) {
            return false;
        }

        return equals((IDimension) obj);
    }

    /**
     * Sets a power for a base quantity.
     *
     * @param baseQuantity The base quantity
     * @param power        The power
     */
    public void setPower(DimensionBase baseQuantity, int power) {
        powers[baseQuantity.ordinal()] = power;
    }

    public boolean equals(IDimension otherDimension) {
        
        /* TODO: Change the unlucky choice of name for this method. For the
         * Java 1.2 version a describesSameAs method was introduced to replace
         * the equals method, and to differentiate between standard equals
         * implementation and testing more properties of the class. To be
         * compatible with the 1.4 OpenMI interfaces this had to be removed.
         * Note that now there is a conflict of having an equals method, but
         * no hashCode.
         */
        
        // code previously in the describesSameAs method:
        
        if (otherDimension == this) {
            return true;
        }

        if (!super.describesSameAs(otherDimension)) {
            return false;
        }

        if (otherDimension == null || this.getClass() != otherDimension.getClass()) {
            return false;
        }

        if (!Arrays.equals(powers, ((Dimension) otherDimension).powers)) {
            return false;
        }

        return true;
    }

}
