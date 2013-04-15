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
import org.openmi.standard.IVectorSet;

/**
 * Parameterized IVector version of the generic ValueSet.
 */
public class VectorSet extends ValueSet<IVector> implements IVectorSet, Serializable {

    /**
     * Creates a default instance.
     */
    public VectorSet() {
        super();
    }

    /**
     * Creates an instance that is a shallow copy of the specified IVectorSet.
     *
     * @param values Collection to copy from
     */
    public VectorSet(IVectorSet values) {
        if (values != null) {
            for (int i = 0; i < values.getCount(); i++) {
                add(values.getVector(i));
            }
        }
    }

    /**
     * Creates an instance for the specified array of IVectors.
     *
     * @param values IVectors to put in the collection
     */
    public VectorSet(IVector[] values) {
        super(values);
    }

    /**
     * Gets the vector with the given index.
     *
     * @param index The index
     * @return The vector
     */
    public IVector getVector(int index) {
        return getValue(index);
    }

}
