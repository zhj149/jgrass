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
package nl.alterra.openmi.sdk.buffer;

import org.openmi.standard.*;

/**
 * The support class contains support functions for the SmartBuffer.
 */
public class Support {

    /**
     * GetVal will get the index�th number of the axisNumber�th component of the
     * ValueSet.
     *
     * @param values     ValueSet to read the value from.
     * @param index      Index of the value in the ValueSet.
     * @param axisNumber Relevant for VectorSets only. 1: x, 2: y, 3: z.
     * @return The index�th number of the axisNumber�th component
     * @throws Exception
     */
    public static double getVal(IValueSet values, int index, int axisNumber) throws Exception {

        double x;

        if (values instanceof IScalarSet) {
            if (axisNumber != 1) {
                throw new Exception("Illegal axis number for ScalarSet");
            }
            return ((IScalarSet) values).getScalar(index);
        }
        else if (values instanceof IVectorSet) {
            if (axisNumber == 1) {
                return ((IVectorSet) values).getVector(index).getXComponent();
            }

            if (axisNumber == 2) {
                return ((IVectorSet) values).getVector(index).getYComponent();
            }

            if (axisNumber == 3) {
                return ((IVectorSet) values).getVector(index).getZComponent();
            }

            throw new Exception("Illegal axis number for VectorSet");
        }
        else {
            throw new Exception("Unsupported value type");
        }
    }

    /**
     * return true if Time A is before Time B (evaluates (Ta < Tb). Both
     * time spans and time stamps can be tested.
     *
     * @param ta First ITime to compare
     * @param tb Second ITime to compare
     * @return True if (Ta < Tb)
     */
    public static boolean isBefore(ITime ta, ITime tb) {
        double a;
        double b;

        if (ta instanceof ITimeSpan) {
            a = ((ITimeSpan) ta).getEnd().getModifiedJulianDay();
        }
        else {
            a = ((ITimeStamp) ta).getModifiedJulianDay();
        }

        if (tb instanceof ITimeSpan) {
            b = ((ITimeSpan) tb).getStart().getModifiedJulianDay();
        }
        else {
            b = ((ITimeStamp) tb).getModifiedJulianDay();
        }

        return (a < b);
    }

}
