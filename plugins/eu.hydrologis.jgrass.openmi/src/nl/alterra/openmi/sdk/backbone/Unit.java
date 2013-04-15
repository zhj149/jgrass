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
import org.openmi.standard.IUnit;

/**
 * The Unit class defines a unit for a quantity.
 */
public class Unit extends BackboneObject implements IUnit, Serializable {

    private double conversionFactor;
    private double conversionOffSet;

    /**
     * Creates an instance with default values, i.e. an empty ID and
     * Description, a conversion factor of 1.0 and a conversion offset
     * of 0.0.
     */
    public Unit() {
        this("", 1.0, 0.0, "");
    }

    /**
     * Creates an instance based on the values of a specified Unit.
     *
     * @param source The unit to copy values from
     */
    public Unit(IUnit source) {
        this(source.getID(), source.getConversionFactorToSI(), source.getOffSetToSI(), source.getDescription());
    }

    /**
     * Creates an instance with the specified values and an empty description.
     *
     * @param ID               the ID
     * @param conversionFactor The conversion factor to SI
     * @param conversionOffSet The conversion offset
     */
    public Unit(String ID, double conversionFactor, double conversionOffSet) {
        this(ID, conversionFactor, conversionOffSet, "");
    }

    /**
     * Creates an instance with the specified values.
     *
     * @param ID               The ID
     * @param conversionFactor The conversion factor to SI
     * @param conversionOffSet The conversion ooffset
     * @param description      The description string
     */
    public Unit(String ID, double conversionFactor, double conversionOffSet, String description) {
        this.setID(ID);
        this.conversionFactor = conversionFactor;
        this.conversionOffSet = conversionOffSet;
        this.setDescription(description);
    }

    /**
     * Gets the conversion Factor to SI.
     *
     * @return The conversion factor to SI for this unit
     */
    public double getConversionFactorToSI() {
        return conversionFactor;
    }

    /**
     * Gets the conversion offset to SI.
     *
     * @return The conversion offset to SI
     */
    public double getOffSetToSI() {
        return conversionOffSet;
    }

    /**
     * Sets the conversion Factor to SI.
     *
     * @param conversionFactor The conversionFactor to set
     */
    public void setConversionFactorToSI(double conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    /**
     * Sets the conversion offset to SI.
     *
     * @param conversionOffSet The conversionOffSet to set.
     */
    public void setOffSetToSI(double conversionOffSet) {
        this.conversionOffSet = conversionOffSet;
    }

    @Override
    public boolean describesSameAs(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!super.describesSameAs(obj)) {
            return false;
        }

        Unit u = (Unit) obj;

        if (!getID().equals(u.getID())) {
            return false;
        }
        if (this.getConversionFactorToSI() != u.getConversionFactorToSI()) {
            return false;
        }
        if (this.getOffSetToSI() != u.getOffSetToSI()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + getID().hashCode() + getDescription().hashCode() +
                Double.valueOf(getConversionFactorToSI()).hashCode() +
                Double.valueOf(getOffSetToSI()).hashCode();
    }

}