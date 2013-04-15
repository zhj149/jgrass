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
import org.openmi.standard.IDimension;
import org.openmi.standard.IQuantity;
import org.openmi.standard.IUnit;

/**
 * The quantity class contains a unit, a description and dimensions.
 */
public class Quantity extends BackboneObject implements IQuantity, Serializable {

    private IUnit unit;
    private IDimension dimension;
    private ValueType valueType;

    /**
     * Creates an instance with all default values, i.e. empty ID and
     * description, default Unit and Dimension and value type SCALAR.
     */
    public Quantity() {
        this("");
    }

    /**
     * Creates an instance with all default values and the specified ID.
     *
     * @param ID String identifier
     */
    public Quantity(String ID) {
        this(new Unit(), "", ID);
    }

    /**
     * Creates an instance copying values from the specified IQuantity.
     *
     * @param source The IQuantity to copy values from
     */
    public Quantity(IQuantity source) {
        this(source.getUnit(), source.getDescription(), source.getID(), source.getValueType(), source.getDimension());
    }

    /**
     * Creates an instance with the specified values and using defaults
     * for all others.
     *
     * @param unit        The unit
     * @param description The description
     * @param ID          The string identifier
     */
    public Quantity(IUnit unit, String description, String ID) {
        this(unit, description, ID, ValueType.Scalar);
    }

    /**
     * Creates an instance with the specified values and a default Dimension.
     *
     * @param unit        The unit
     * @param description The description
     * @param ID          The string identifier
     * @param valueType   Value type (vector or scalar)
     */
    public Quantity(IUnit unit, String description, String ID, ValueType valueType) {
        this(unit, description, ID, valueType, new Dimension());
    }

    /**
     * Creates an instance with the specified values.
     *
     * @param unit        The unit
     * @param description The description
     * @param ID          The string identifier
     * @param valueType   Value type (vector or scalar)
     * @param dimension   The dimension
     */
    public Quantity(IUnit unit, String description, String ID, ValueType valueType, IDimension dimension) {
        this.unit = unit;
        this.setDescription(description);
        this.setID(ID);
        this.valueType = valueType;
        this.dimension = dimension;
    }

    /**
     * Gets the dimension.
     *
     * @return The dimension
     */
    public IDimension getDimension() {
        return dimension;
    }

    /**
     * Gets the Unit.
     *
     * @return The Unit
     */
    public IUnit getUnit() {
        return unit;
    }

    /**
     * Gets the ValueType.
     *
     * @return The ValueType
     */
    public ValueType getValueType() {
        return valueType;
    }

    /**
     * Sets the Dimension.
     *
     * @param dimension The dimension to set
     */
    public void setDimension(IDimension dimension) {
        this.dimension = dimension;
    }

    /**
     * Sets the Unit.
     *
     * @param unit The unit to set
     */
    public void setUnit(IUnit unit) {
        this.unit = unit;
    }

    /**
     * Sets the ValueType.
     *
     * @param valueType The valueType to set
     */
    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    @Override
    public String toString() {
        return getID();
    }

    /**
     * Tests if all relevant field values are congruent. Member fields that
     * have a meaning for proper functioning of the application rather than
     * describing a "real world" property should not be included in the test.
     * 
     * The tested object therefore does not need to be the same instance.
     * 
     * Quantities are considered congruent if they have the same value type
     * and when the dimensions match. For the units of the quantities it is
     * checked that they have a SI factor set, so that conversion can take
     * place.
     *
     * @param obj to compare with
     * @return true if object is congruent
     */
    @Override
    public boolean describesSameAs(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!super.describesSameAs(obj)) {
            return false;
        }

        Quantity q = (Quantity) obj;

        if (!getValueType().equals(q.getValueType())) {
            return false;
        }

        if (!getDimension().equals(q.getDimension())) {
            return false;
        }

        // verify that units can be mapped to SI
        if ((unit.getConversionFactorToSI() == 0) || (q.unit.getConversionFactorToSI() == 0)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + getID().hashCode() + (getUnit() != null ? getUnit().hashCode() : 0) + getValueType().hashCode() +
                getDescription().hashCode() + getDimension().hashCode();
    }

}
