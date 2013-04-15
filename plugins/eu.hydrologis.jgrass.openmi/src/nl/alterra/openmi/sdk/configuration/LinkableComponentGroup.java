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
 ****************************************************************************/
package nl.alterra.openmi.sdk.configuration;

import nl.alterra.openmi.sdk.backbone.*;
import org.openmi.standard.*;

import java.util.List;

/**
 * A group of linkable components that present themselves to the outside
 * world as a single component. All calls to the methods from the linkable
 * component interface will be forwarded to every component in the set, or
 * aggregated results will be returned.
 * 
 * A LinkableComponentGroup to the outside world acts as a black box, hiding
 * input exchange items from components it contains when they are internally
 * linked to the output of another component in the group. All outputs from
 * components will remain visible outside since they all can be linked to by
 * many links.
 * 
 * Since the group must perform some internal book keeping, it is best to
 * surround a sequence of modifications (adding components and links) with
 * calls to beginUpdate() and endUpdate().
 * 
 * Use the provided createLink() and destroyLink() methods to construct the
 * links in the group between the linkable components. They will call the
 * addLink() and removeLink() methods when required. Since there should always
 * be at least one (outside) group to contain everything else, you should
 * never need to call addLink() and removeLink() directly. A Composition is
 * a good subclass of a LinkableComponentGroup to have as container, since
 * it supports the Triggers.
 */
public class LinkableComponentGroup extends LinkableComponent {

    /**
     * Flag indicating that the structure of the group is being modified.
     * Used to prevent too frequent updating of the group's exchange items.
     */
    private boolean updating = false;
    
    /**
     * Storage for all the grouped linkable components.
     */
    private LinkableComponents components = new LinkableComponents();
    
    /**
     * A LinkManager is used to maintain the list of links inside the group.
     */
    private LinkManager internalLinks = new LinkManager();

    /**
     * Creates an instance with the specified ID.
     *
     * @param ID String ID
     */
    public LinkableComponentGroup(String ID) {
        super(ID);
    }

    /**
     * Signals the group that a series of modifications to its internal
     * structure of components and links is going to be made. The group will
     * not update its exchange items etc. until endUpdate() is called.
     * 
     * Note that accessing properties of the group through a get method will
     * first do an endUpdate() automatically.
     */
    public void beginUpdate() {
        updating = true;
    }

    /**
     * Signals the group that a series of modifications to its internal
     * structure of components and links is finished. The group will then
     * update its exchange items etc. accordingly.
     * 
     * Note that accessing properties of the group through a get method will
     * first do an endUpdate() automatically.
     */
    public void endUpdate() {
        if (updating) {
            updating = false;
            updateExchangeItems();
        }
    }

    /**
     * Gets the component description string.
     *
     * @return The description string
     */
    @Override
    public String getComponentDescription() {
        return "A group of linkable components and links.";
    }

    /**
     * Gets the component ID.
     *
     * @return The ID as a string
     */
    @Override
    public String getComponentID() {
        return "LinkableComponentGroup";
    }

    /**
     * Gets the model description.
     *
     * @return The model description string
     */
    @Override
    public String getModelDescription() {
        return "n/a";
    }

    /**
     * Gets the model ID.
     *
     * @return The model ID string
     */
    @Override
    public String getModelID() {
        return "n/a";
    }

    /**
     * Calculates the common time span of all the linkable components in the
     * set.
     *
     * @return ITimeSpan, can be null when there are no linkable components
     *         in the set or none of the components has a time horizon. If
     *         a time span is returned the end time might be before the start
     *         time (the common time span is empty)!
     */
    @Override
    public ITimeSpan getTimeHorizon() {
        double start = Double.MIN_VALUE;
        double end = Double.MAX_VALUE;
        boolean knownTime = false;

        endUpdate();

        for (ILinkableComponent lc : components) {
            ITimeSpan lcSpan = lc.getTimeHorizon();
            if (lcSpan != null) {
                start = Math.max(start, lcSpan.getStart().getModifiedJulianDay());
                end = Math.min(end, lcSpan.getEnd().getModifiedJulianDay());
                knownTime = true;
            }
        }

        if (knownTime) {
            return new TimeSpan(new TimeStamp(start), new TimeStamp(end));
        }
        else {
            return null;
        }
    }

    /**
     * Calculates the minimum earliest input time for all the linkable
     * components in the set.
     *
     * @return ITimeStamp, can be null when there are no linkable components
     *         in the set or when none of the components knows about time.
     */
    @Override
    public ITimeStamp getEarliestInputTime() {
        double earliest = Double.MAX_VALUE;
        boolean knownTime = false;

        endUpdate();

        for (ILinkableComponent lc : components) {
            ITimeStamp lcStamp = lc.getEarliestInputTime();
            if (lcStamp != null) {
                earliest = Math.min(earliest, lcStamp.getModifiedJulianDay());
                knownTime = true;
            }
        }

        if (knownTime) {
            return new TimeStamp(earliest);
        }
        else {
            return null;
        }
    }

    /**
     * Checks if the component is valid (contains valid data).
     *
     * @return String, empty if all contained components are valid, otherwise
     *         combined result messages
     */
    @Override
    public String validate() {
        StringBuffer result = new StringBuffer();

        endUpdate();

        for (ILinkableComponent lc : components) {
            String lcResult = lc.validate();
            if ((lcResult != null) && (lcResult.length() > 0)) {
                result.append(lcResult).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Updates the internal lists of exchange items according to the current
     * linkable components and links in the group.
     * 
     * note: existing input exchange items may disappear when the
     * changed internal structure requires so; any external
     * links to those items will be removed.
     */
    private void updateExchangeItems() {
        if (updating) {
            return;
        }

        inputExchangeItems.clear();
        outputExchangeItems.clear();

        for (ILinkableComponent lc : components) {
            for (int i = 0; i < lc.getOutputExchangeItemCount(); i++) {
                outputExchangeItems.add(lc.getOutputExchangeItem(i));
            }

            for (int i = 0; i < lc.getInputExchangeItemCount(); i++) {
                IInputExchangeItem item = lc.getInputExchangeItem(i);

                // internalLinks contains the INTERNAL links. If an item is internally linked
                // it should not be advertised externally:
                Link internalLink = internalLinks.containsLinkTo(item);
                if (internalLink == null) {
                    inputExchangeItems.add(item);
                }

                // If an EXTERNAL link exists for an item that's already internally
                    // linked, the external link is to be removed:

                else {
                    Link externalLink = (Link) findLinkForInputExchangeItem(item);
                    if (externalLink != null) {
                        externalLink.getOwner().removeLinkToTarget(item);
                    }
                }
            }
        }
    }

    /**
     * Adds the specified ILinkableComponent to the group.
     *
     * @param component ILinkableComponent to add
     */
    public void addComponent(ILinkableComponent component) {
        components.add(component);
        updateExchangeItems();
    }

    /**
     * Removes the specified ILinkableComponent from the group, and
     * all the links that are connected to it.
     *
     * @param component ILinkableComponent
     */
    public void removeComponent(ILinkableComponent component) {
        if (components.contains(component)) {
            internalLinks.removeLinksToComponent(component);
            components.remove(component);
            updateExchangeItems();
        }
    }

    /**
     * Removes all links and components from the group.
     */
    public void clear() {
        internalLinks.clear();
        components.clear();
        updateExchangeItems();
    }

    /**
     * Creates a new link (if possible) and returns it. A reference to the
     * link will also be added to the internal list for future use.
     *
     * @param source     ILinkableComponent
     * @param sourceItem IOutputExchangeItem
     * @param target     ILinkableComponent
     * @param targetItem IInputExchangeItem
     * @return The created Link, or null
     */
    public Link createLink(ILinkableComponent source, IOutputExchangeItem sourceItem,
            ILinkableComponent target, IInputExchangeItem targetItem) {
        if (Link.canConnect(source, sourceItem, target, targetItem) == Link.ValidationStatus.Ok) {
            Link newLink = internalLinks.createLink();
            newLink.connect(source, sourceItem, target, targetItem);
            updateExchangeItems();
            return newLink;
        }
        else {
            return null;
        }
    }

    /**
     * Removes the specified link from the group.
     *
     * @param l Link to remove
     */
    public void destroyLink(Link l) {
        internalLinks.remove(l);
        updateExchangeItems();
    }

    /**
     * Returns a list of all the linkable components in the group.
     *
     * @return ILink[]
     */
    public ILinkableComponent[] getLinkableComponents() {
        return components.toArray(new ILinkableComponent[]{});
    }

    /**
     * Returns a list of all the links in the group.
     *
     * @return ILink[]
     */
    public ILink[] getLinks() {
        endUpdate();
        return internalLinks.getLinks();
    }

    /**
     * Calls prepare on all components in the group.
     */
    @Override
    public void prepareHook() {
        for (ILinkableComponent lc : components) {
            lc.prepare();
        }
    }

    /**
     * Calls finish on all components in the group.
     */
    @Override
    public void finishHook() {
        for (ILinkableComponent lc : components) {
            lc.finish();
        }
    }

    /**
     * Calls initialize on all components in the group. The initialisation
     * of linkable components might affect the input and output exchange
     * items they expose.
     *
     * @param properties The combined list of arguments for all the
     *                   linkable components in the group
     */
    @Override
    public void initializeHook(IArgument[] properties) {
        beginUpdate();

        for (ILinkableComponent lc : components) {
            lc.initialize(properties);
        }

        endUpdate();
    }

    /**
     * Finds out which linkable component the specified link connects to,
     * and calls its getValues() method. If an unknown link is given, a
     * NullValueSet will be returned.
     *
     * @param time The timestamp/timespan for which to return values
     * @param link The Link on which link values to return
     * @return The computed values
     */
    @Override
    public IValueSet getValuesHook(ITime time, ILink link) {

        ILinkableComponent realSource = link.getSourceComponent();
        if (realSource != null) {
            if (realSource != this) {
                return realSource.getValues(time, link.getID());
            }
        }
        else {
            throw new ConfigurationException(String.format("NO Source Component"));
        }

        return new NullValueSet();
    }

    /**
     * Gets the total list of required initialisation arguments for all the
     * components in the group.
     *
     * @return IArguments The combined list
     */
    public List<IArgument> getInitialisationArguments() {
        return components.getInitialisationArguments();
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to readSystemDeployer.
     * 
     * The value returned for a LinkableComponentGroup is its Caption.
     * If that is undefined, super.toString() is called.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        if (getCaption().length() == 0) {
            return super.toString();
        }
        return getCaption();
    }

    /**
     * Tests if all relevant field values are congruent. Member fields that
     * have a meaning for proper functioning of the application rather than
     * describing a "real world" property should not be included in the test.
     * 
     * The tested object therefore does not need to be the same instance.
     *
     * @param obj to compare with
     * @return true if object is congruent
     */
    @Override
    public boolean describesSameAs(Object obj) {
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }

        LinkableComponentGroup that = (LinkableComponentGroup) obj;

        if (!components.equals(that.components)) {
            return false;
        }
        if (!internalLinks.equals(that.internalLinks)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + components.hashCode();
        result = 31 * result + internalLinks.hashCode();
        return result;
    }

    /**
     * Informs the component that a link has been added to it. The component
     * will always retain a reference for future use, even if the link does
     * not directly reference the component (this is needed for constructing
     * the LinkableComponentGroup).
     * 
     * Please check if the link is really relevant for the component before
     * calling this method!
     *
     * @param link The link that has been added
     */
    @Override
    public void addLink(ILink link) {

        endUpdate();
        super.addLink(link);

        // note: link references to the group rather than the constituent components
        if (link.getSourceComponent() == this) {
            ILinkableComponent realSource = link.getSourceComponent();
            if (realSource != null) {
                if (realSource != this) {
                    realSource.addLink(link);
                }
            }
            else {
                throw new ConfigurationException(String.format("NO Source Component"));
            }
        }

        if (link.getTargetComponent() == this) {
            ILinkableComponent realTarget = link.getTargetComponent();
            if (realTarget != null) {
                if (realTarget != this) {
                    realTarget.addLink(link);
                }
            }
            else {
                throw new ConfigurationException(String.format("NO Target Component"));
            }
        }
    }

    public int getIndexOfComponent(ILinkableComponent component) {
        return components.indexOf(component);
    }

    /**
     * Notifies the component that a link is being removed. When needed the
     * component removes its references to the link so that it will no more
     * attempts will be made to use it in the future.
     *
     * @param ID The string ID of the link being removed
     */
    @Override
    public void removeLink(String ID) {
        endUpdate();
        ILink link = getLink(ID);

        // if the link does not concern us, just quit
        if (link == null) {
            return;
        }

        // note: link references to the group rather than the constituent components
        if (link.getSourceComponent() == this) {
            ILinkableComponent realSource = link.getSourceComponent();
            if (realSource != null) {
                if (realSource != this) {
                    realSource.removeLink(ID);
                }
            }
            else {
                throw new ConfigurationException(String.format("NO Source Component"));
            }
        }

        if (link.getTargetComponent() == this) {
            ILinkableComponent realTarget = link.getTargetComponent();
            if (realTarget != null) {
                if (realTarget != this) {
                    realTarget.removeLink(ID);
                }
            }
            else {
                throw new ConfigurationException(String.format("NO Target Component"));
            }
        }

        super.removeLink(ID);
    }

    @Override
    public int getInputExchangeItemCount() {
        endUpdate();
        return super.getInputExchangeItemCount();
    }

    @Override
    public IInputExchangeItem getInputExchangeItem(int index) {
        endUpdate();
        return super.getInputExchangeItem(index);
    }

    @Override
    public int getOutputExchangeItemCount() {
        endUpdate();
        return super.getOutputExchangeItemCount();
    }

    @Override
    public IOutputExchangeItem getOutputExchangeItem(int index) {
        endUpdate();
        return super.getOutputExchangeItem(index);
    }

    @Override
    public ILink findLinkForInputExchangeItem(IInputExchangeItem item) {
        endUpdate();
        return super.findLinkForInputExchangeItem(item);
    }

    @Override
    public boolean hasLinkForInputExchangeItem(IInputExchangeItem item) {
        endUpdate();
        return super.hasLinkForInputExchangeItem(item);
    }

}
