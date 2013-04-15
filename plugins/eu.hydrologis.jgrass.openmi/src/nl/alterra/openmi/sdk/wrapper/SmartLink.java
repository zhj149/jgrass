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
 ****************************************************************************/
package nl.alterra.openmi.sdk.wrapper;

import java.io.Serializable;
import org.openmi.standard.ILink;

/**
 * SmartLink class
 * a part of the smat wrapper engine
 */
public abstract class SmartLink implements Serializable {

    protected ILink link = null;
    protected IRunEngine engineApiAccess = null;

    /**
     * constructor
     */
    public SmartLink() {
        // void
    }

    /**
     * GETTER for the link
     *
     * @return the link
     */
    public ILink getLink() {
        return link;
    }

    /**
     * SETTER for the link
     */
    public void setLink(ILink value) {
        link = value;
    }

    /**
     * The initialize method
     *
     * @param engineApiAccess the engine api access
     */
    public abstract void initialize(IRunEngine engineApiAccess);

}