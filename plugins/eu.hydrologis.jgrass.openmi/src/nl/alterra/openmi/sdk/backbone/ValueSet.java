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

import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.ParameterizedType;
import org.openmi.standard.IValueSet;

/**
 * Generic version of the ValueSet.
 */
public class ValueSet<MType> extends ArrayList<MType> implements IValueSet {

    /**
     * Creates an instance that references the specified values.
     *
     * @param values The values of this set
     */
    public ValueSet(MType... values) {
        setData(values);
    }

    /**
     * Creates a default instance.
     */
    public ValueSet() {
        super();
    }

    /**
     * Creates an instance that is a shallow copy of the specified source.
     *
     * @param source Collection to copy from
     */
    public ValueSet(Collection<? extends MType> source) {
        super(source);
    }

    /**
     * Sets the data.
     *
     * @param values The data to set
     */
    public void setData(MType... values) {
        ensureCapacity(values.length);
        for (int i = 0; i < values.length; i++) {
            add(i, values[i]);
        }
    }

    /**
     * Returns the size of the set
     *
     * @return int the size of the set
     */
    public int getCount() {
        return size();
    }

    /**
     * Tests if the specified index can be accessed.
     *
     * @param elementIndex The index to test
     * @return True if the index is valid, false when not
     */
    public boolean isValid(int elementIndex) {
        return elementIndex >= 0 && elementIndex < size();
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index The index to get the value for
     * @return MType The value
     */
    public MType getValue(int index) {
        return get(index);
    }

    /**
     * Sets a value of the set.
     *
     * @param index The index to update
     * @param value The new value to set
     */
    public void setValue(int index, MType value) {
        set(index, value);
    }

    /**
     * Converts the set to a Collection.
     *
     * @return Collection<MType> created from the set
     */
    public Collection<MType> toCollection() {
        return this;
    }

    /**
     * Gives some string information about the set. This method inform also
     * on the actual type of the set.
     *
     * @return String a string representation of the set
     */
    @Override
    public String toString() {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        String types = "";
        for (java.lang.reflect.Type param : type.getActualTypeArguments()) {
            if (param instanceof Class) {
                types += ((Class) param).getSimpleName();
            }
        }
        return getClass().getSimpleName() + "[" + getCount() + "] values are " + types;
    }

}