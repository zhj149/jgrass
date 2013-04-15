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

import org.openmi.standard.IValueSet;

/**
 * Parameterized Float version of the generic ValueSet.
 */
public class FloatSet extends ValueSet<Float> implements IValueSet {

    /**
     * Creates and array of Float objects, for the specified array of float
     * values.
     *
     * @param arr The float values to create the object array for
     * @return Float[] for the specified float values
     */
    private static Float[] floatArray(float[] arr) {
        Float[] array = new Float[arr.length];
        for (int i = 0; i < arr.length; ++i) {
            array[i] = arr[i];
        }
        return array;
    }

    /**
     * Creates an empty instance.
     */
    public FloatSet() {
        super();
    }

    /**
     * Creates an instance for the specified list of float values.
     *
     * @param values floats to put in the collection
     */
    public FloatSet(float... values) {
        super(floatArray(values));
    }

    /**
     * Creates an instance for the specified list of Float objects.
     *
     * @param values Floats to put in the collection
     */
    public FloatSet(Float... values) {
        super(values);
    }

}
