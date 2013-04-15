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

import org.openmi.standard.IArgument;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Type safe collection of IArgumentEx implementors.
 */
public class Arguments extends ArrayList<IArgument> {

    /**
     * Returns the Value for the first found IArgumentEx in the collection that
     * has the specified key.
     *
     * @param key
     * @return String Value, empty if key does not exist
     */
    public String getValueForKey(String key) {
        for (IArgument a : this) {
            if (a.getKey().equals(key)) {
                return a.getValue();
            }
        }

        return "";
    }

    /**
     * Changes values of all Arguments with the specified key and that are
     * not ReadOnly to the specified string.
     *
     * @param key   The key of arguments to find and change
     * @param value The value to set for matching arguments
     */
    public void setValueForKey(String key, String value) {
        for (IArgument a : this) {
            if (a.getKey().equals(key) && !a.isReadOnly()) {
                a.setValue(value);
            }
        }
    }

    /**
     * Returns true if the specified key exists in the collection.
     *
     * @param key The key to locate
     * @return boolean, true if key exists
     */
    public boolean containsKey(String key) {
        for (IArgument a : this) {
            if (a.getKey().equals(key)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds all the IArgumentEx instances from the given collection to
     * this collection. No checks will be performed!
     *
     * @param c The arguments to add
     */
    @Override
    public boolean addAll(Collection<? extends IArgument> c) {
        if (c != null) {
            for (IArgument item : c) {
                add(item);
            }
        }

        return true;
    }

    /**
     * Adds all the IArgument instances from the given array to the
     * collection. No checks will be performed!
     *
     * @param arguments The arguments to add
     */
    public void addAll(IArgument[] arguments) {
        for (IArgument arg : arguments) {
            add(arg);
        }
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param o element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    public boolean add(IArgument o) {
        return super.add(o);
    }

    /**
     * Returns the index for the specified IArgumentEx.
     *
     * @param elem
     * @return index
     */
    public int indexOf(IArgument elem) {
        return super.indexOf(elem);
    }

    /**
     * Returns the index for the specified argument key.
     *
     * @param argumentKey
     * @return index, -1 if not found
     */
    public int indexOfKey(String argumentKey) {
        if (argumentKey != null) {
            for (int i = 0; i < size(); i++) {
                if (get(i).getKey().equals(argumentKey)) {
                    return i;
                }
            }
        }

        return -1;
    }

}
