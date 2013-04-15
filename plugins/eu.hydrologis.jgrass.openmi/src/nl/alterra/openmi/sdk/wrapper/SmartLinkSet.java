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
package nl.alterra.openmi.sdk.wrapper;

import org.openmi.standard.ILink;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * SmartLinkSet class a part of the smart wrapper engine.
 */
public abstract class SmartLinkSet implements Serializable {

    protected IRunEngine engineApiAccess;
    protected ArrayList smartLinkList;

    /**
     * constructor
     */
    public SmartLinkSet() {
        smartLinkList = new ArrayList();
    }

    /**
     * GETTER for the list of links
     *
     * @return the list of links
     */
    public ArrayList getSmartLinkList() {
        return smartLinkList;
    }

    /**
     * The initialize method
     *
     * @param engineApiAccess the engine api access
     */
    public void initialize(IRunEngine engineApiAccess) {
        this.engineApiAccess = engineApiAccess;
    }

    /**
     * To add a link
     *
     * @param link The link to be added
     */
    public abstract void addLink(ILink link);

    /**
     * To remove a link
     *
     * @param linkID the link ID to be removed
     */
    public boolean removeLink(String linkID)// , BaseEvents baseEvents)
    {
        int index = -999;
        boolean wasFoundAndRemoved = false;

        for (int i = 0; i < smartLinkList.size(); i++) {
            if (((SmartLink) smartLinkList.get(i)).link.getID() == linkID) {
                index = i;
            }
        }

        if (index >= 0) {
            smartLinkList.remove(index);
            wasFoundAndRemoved = true;
        }

        return wasFoundAndRemoved;
    }

    /**
     * To get a link
     *
     * @param LinkID the link ID of the link
     */
    public ILink getLink(String LinkID) throws Exception {
        for (int i = 0; i < smartLinkList.size(); i++) {
            if (((SmartLink) smartLinkList.get(i)).link.getID().equals(LinkID)) {
                return ((SmartLink) smartLinkList.get(i)).link;
            }
        }
        throw new Exception("Failed to find link in linkList");
    }

}