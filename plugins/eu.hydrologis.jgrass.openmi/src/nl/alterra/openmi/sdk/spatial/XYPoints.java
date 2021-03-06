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

import java.util.ArrayList;

/**
 * Type safe collection of XYPoint objects.
 */
public class XYPoints extends ArrayList<XYPoint> {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XYPoints points = (XYPoints) o;
        if (size() != points.size()) {
            return false;
        }

        for (int i = 0; i < size(); i++) {
            if (!get(i).equals(points.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int h = super.hashCode();

        for (XYPoint p : this) {
            h += p.hashCode();
        }

        return h;
    }

}
