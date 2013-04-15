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

/**
 * Parameterized Integer version of the generic ValueSet.
 */
public class IntegerSet extends ValueSet<Integer> {
    
    /**
     * Creates and array of Integer objects, for the specified array of int
     * values.
     *
     * @param arr The int values to create the object array for
     * @return Integer[] for the specified int values
     */
    private static Integer[] integerArray(int[] arr) {
        Integer[] array = new Integer[arr.length];
        for (int i = 0; i < arr.length; ++i) {
            array[i] = arr[i];
        }
        return array;
    }

    /**
     * Creates an empty instance.
     */
    public IntegerSet() {
        super();
    }

    /**
     * Creates an instance for the specified list of int values.
     *
     * @param values ints to put in the collection
     */
    public IntegerSet(int... values) {
        super(integerArray(values));
    }

    /**
     * Creates an instance for the specified list of Integer objects.
     *
     * @param values Integers to put in the collection
     */
    public IntegerSet(Integer... values) {
        super(values);
    }
}
