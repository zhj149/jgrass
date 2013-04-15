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
import org.openmi.standard.IScalarSet;

/**
 * The ScalarSet contains a list of scalar values. It is implemented as a
 * parameterized version of the generic ValueSet, for Double objects.
 */
public class ScalarSet extends ValueSet<Double> implements IScalarSet, Serializable {

    /**
     * Creates and array of Double objects, for the specified array of double
     * values.
     *
     * @param arr The double values to create the object array for
     * @return Double[] for the specified float values
     */
    private static Double[] doubleArray(double[] arr) {
        Double[] array = new Double[arr.length];
        for (int i = 0; i < arr.length; ++i) {
            array[i] = arr[i];
        }
        return array;
    }

    /**
     * Creates an empty instance.
     */
    public ScalarSet() {
        super();
    }

    /**
     * Creates an instance that is a shallow copy of the specified IScalarSet.
     *
     * @param values Collection to copy from
     */
    public ScalarSet(IScalarSet values) {
        if (values != null) {
            for (int i = 0; i < values.getCount(); i++) {
                add(values.getScalar(i));
            }
        }
    }

    /**
     * Creates an instance for the specified list of double values.
     *
     * @param values doubles to put in the collection
     */
    public ScalarSet(double... values) {
        super(doubleArray(values));
    }

    /**
     * Creates an instance for the specified list of Double objects.
     *
     * @param values Doubles to put in the collection
     */
    public ScalarSet(Double... values) {
        super(values);
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();

        if (getCount() == 0) {
            str.append("Empty scalar");
        } else {
            str.append("Scalar: ");
            for (int i = 0; (i < getCount()) && (i < 10); i++) {
                str.append(Double.valueOf(getScalar(i)).toString()).append(" ");
            }
        }

        return str.toString();
    }

    /**
     * Gets the value for one of the elements in the set.
     *
     * @param elementIndex index in the scalar set
     * @return double scalar value
     */
    public double getScalar(int elementIndex) {
        // conversion from Double to double is automatic
        return getValue(elementIndex);
    }

}
