/* ***************************************************************************
 *
 *    Copyright (C) 2006 Alterra, Wageningen University and Research centre.
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
 *****************************************************************************
 *
 * @author Rob Knapen, Alterra B.V., The Netherlands
 * @author Wim de Winter, Alterra B.V., The Netherlands
 *
 *****************************************************************************
 * Changes:
 * 16oct2006 - Rob Knapen
 *      Default caption number to used instance counter instead of a static
 *      class variable. Numbering is now unique within each instance of the
 *      LinkManager.
 ****************************************************************************/
package nl.alterra.openmi.sdk.configuration;

import java.util.ArrayList;
import java.rmi.server.UID;
import nl.alterra.openmi.sdk.backbone.Link;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.ILinkableComponent;

/**
 * The LinkManager has a list of references to links that are created by
 * it and helps to keep them in a valid state.
 */
public class LinkManager {

    /**
     * Local sequence number for created links, not necessarily unique.
     */
    private int defaultCaptionIndex = 0;

    /**
     * Internal list of Link references.
     */
    private ArrayList<Link> allLinks = new ArrayList<Link>();

    /**
     * Creates a link with a generated ID and adds a reference to the internal
     * list of the LinkManager.
     *
     * @return Link
     */
    public Link createLink() {
        Link result = new Link(this, (new UID()).toString());
        result.setCaption(String.format("Link %d", defaultCaptionIndex++));
        return result;
    }

    /**
     * Gets a link by its ID.
     *
     * @param ID The ID to retrieve the link for
     * @return The Link with the specified ID, or null
     */
    private Link getByID(String ID) {
        for (Link l : allLinks) {
            if (ID.equals(l.getID())) {
                return l;
            }
        }
        return null;
    }

    /**
     * Adds a link to the LinkManager. The link manager should already be set
     * as the owner of the link.
     *
     * @param l Link
     * @return True if the link was added, false otherwise
     */
    public boolean addLink(Link l) {
        if (l.getOwner() == this) {
            allLinks.add(l);
        }
        return allLinks.contains(l);
    }

    /**
     * Removes the (first) link with the specified ID from the collection.
     *
     * @param ID The string ID for the link to remove
     */
    public void removeByID(String ID) {
        remove(getByID(ID));
    }

    /**
     * Calls reset() for the specified link and removes it from the list.
     *
     * @param l Link
     */
    public void remove(Link l) {
        if (l != null) {
            l.reset();
            allLinks.remove(l);
        }
    }

    /**
     * Finds, resets and removes the link(s) that are connected to the
     * specified target exchange item. In theory there can be only one
     * link already connected to the input exchange item. If such a link
     * exists its reset() method will be called.
     *
     * @param targetExchangeItem IInputExchangeItem
     */
    public void removeLinkToTarget(IInputExchangeItem targetExchangeItem) {
        Link l = containsLinkTo(targetExchangeItem);
        if (l != null) {
            remove(l);
        }
    }

    /**
     * Removes all links maintained by the LinkManager to the specified
     * linkable component.
     *
     * @param component ILinkableComponent
     */
    public void removeLinksToComponent(ILinkableComponent component) {
        for (int i = allLinks.size() - 1; i > 0; i--) {
            Link l = allLinks.get(i);
            if ((l.getSourceComponent() == component) || (l.getTargetComponent() == component)) {
                remove(l);
            }
        }
    }

    /**
     * Removes all links maintained by the LinkManager.
     */
    public void clear() {
        for (int i = allLinks.size() - 1; i > 0; i--) {
            remove(allLinks.get(i));
        }
    }

    /**
     * Returns a list of all the links.
     *
     * @return ILink[]
     */
    public ILink[] getLinks() {
        return allLinks.toArray(new ILink[]{});
    }

    /**
     * Checks if the manager contains a link that connects to the specified
     * IInputExchangeItem. If so that Link will be returned, otherwise it
     * will not (i.e. the return value will be NULL).
     *
     * @param item IInputExchangeItem
     * @return Link
     */
    public Link containsLinkTo(IInputExchangeItem item) {
        for (Link l : allLinks) {
            if (l.getTargetExchangeItem() == item) {
                return l;
            }
        }
        return null;
    }

    /**
     * Returns the link with the specified ID, or null when the LinkManager
     * does not contain a matching a link.
     *
     * @param linkID The ID to look for
     * @return Link found, or null
     */
    public Link getLink(String linkID) {
        for (Link l : allLinks) {
            if (l.getID().equals(linkID)) {
                return l;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LinkManager that = (LinkManager) o;

        if (!allLinks.equals(that.allLinks)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return allLinks.hashCode();
    }

}