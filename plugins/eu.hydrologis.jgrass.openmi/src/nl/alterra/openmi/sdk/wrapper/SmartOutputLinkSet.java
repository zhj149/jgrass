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

import org.openmi.standard.ILink;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;
import nl.alterra.openmi.sdk.backbone.TimeStamp;

/**
 * SmartOutputLinkSet class
 * a part of the smart buffer engine
 */
public class SmartOutputLinkSet extends SmartLinkSet {

    /**
     * The getvalue method
     *
     * @param time   at a given time
     * @param LinkID on a given link
     * @return it return values
     * @throws Exception
     */
    public IValueSet getValue(ITime time, String LinkID) throws Exception {
        int matchLinkNumber = -1;
        for (int i = 0; i < smartLinkList.size(); i++) {
            if (((SmartOutputLink) smartLinkList.get(i)).link.getID().equals(LinkID)) {
                matchLinkNumber = i;
            }
        }
        if (matchLinkNumber == -1) {
            throw new Exception("Failed to find matching SmartOutputLink in method SmartOutputLinkSet.getValue()");
        }
        return ((SmartOutputLink) smartLinkList.get(matchLinkNumber)).getSmartBuffer().getValues(time);
    }

    /**
     * the implementation of the underlying method
     *
     * @see SmartLinkSet#addLink(ILink)
     */
    public void addLink(ILink link) {
        SmartOutputLink smartOutputLink = new SmartOutputLink();
        smartOutputLink.link = link;
        smartOutputLink.initialize(engineApiAccess);
        smartLinkList.add(smartOutputLink);
    }

    /**
     * to get the lastest time stored in the buffer
     *
     * @return the latest time
     */
    public ITime getLatestBufferTime() {
        return new TimeStamp();
    }

    /**
     * to update the buffer data at a given time
     *
     * @param time the time to update
     */
    public void updateBuffers(ITime time) throws Exception {
        for (int i = 0; i < smartLinkList.size(); i++) {
            ((SmartOutputLink) smartLinkList.get(i)).updateBuffer(time);
        }
    }

    /**
     * To clear the buffer after a given time
     *
     * @param time the time to clear the data
     * @throws Exception
     */
    public void clearBuffersAfter(ITime time) throws Exception {
        for (int i = 0; i < smartLinkList.size(); i++) {
            ((SmartOutputLink) smartLinkList.get(i)).getSmartBuffer().clearAfter(time);
        }
    }

}