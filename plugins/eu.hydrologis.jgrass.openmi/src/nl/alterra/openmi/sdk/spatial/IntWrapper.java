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
 * A simple wrapper for an integer variable. Sometimes needed to simulate a
 * pass by reference in methods. Java does not support this for primitive
 * data types, only for objects. Even the Integer class does not allow its
 * value to be changed (yet).
 */
public class IntWrapper {

    int value;

    /**
     * Creates an instance with the given value.
     *
     * @param value
     */
    public IntWrapper(int value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return Value as int
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value New int value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Steps to the next index in a circular list 0, 1 ..., n.
     *
     * @param n Largest value
     * @return The increased value
     */
    public int circularIncrease(int n) {
        value++;
        if (value > n) {
            value = 0;
        }
        return value;
    }

    /**
     * Steps to the previous index in a circular list 0, 1 ..., n.
     *
     * @param n Largest value
     * @return The updated value
     */
    public int circularDecrease(int n) {
        value--;
        if (value < 0) {
            value = n;
        }
        return value;
    }

}
