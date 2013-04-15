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

import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;

import org.openmi.standard.*;

/**
 * The LinkableComponent class is a default abstract implementation of the
 * ILinkableComponent interface.
 * 
 * TODO: Add a locked state to the component. Should be set to true when
 * prepare() is called, and back to false when finish() is called. If locked
 * is true no changes to the component should be allowed and result in an
 * exception.
 */
public class LinkableComponent extends Publisher implements ILinkableComponent {

    private String instanceID;
    protected InputExchangeItems inputExchangeItems;
    protected OutputExchangeItems outputExchangeItems;
    private Arguments customArguments;

    /**
     * Collection of references to links for internal use only. The component
     * is not responsible for the management of the links, nor their creation
     * and removal!
     */
    protected ArrayList<ILink> allLinks = new ArrayList<ILink>();

    /**
     * Creates an instance with the specified ID.
     *
     * @param ID String ID
     */
    public LinkableComponent(String ID) {
        super(ID);
        instanceID = new UID().toString(); // create unique id
        inputExchangeItems = new InputExchangeItems();
        outputExchangeItems = new OutputExchangeItems();
        customArguments = new Arguments();
    }

    /**
     * Gets the component description string.
     *
     * @return The description string
     */
    public String getComponentDescription() {
        return "";
    }

    /**
     * Gets the number of input exchange items.
     *
     * @return The number of input exchange items
     */
    public int getInputExchangeItemCount() {
        return inputExchangeItems.size();
    }

    /**
     * Returns the time horizon, which is the simulation start and stop time
     * for this component.
     *
     * @return The time horizon as an ITimeSpan
     */
    public ITimeSpan getTimeHorizon() {
        return null;
    }

    /**
     * Returns the earliest time for which input is needed.
     *
     * @return The earliest input time as ITimeStamp
     */
    public ITimeStamp getEarliestInputTime() {
        return null;
    }

    /**
     * Gets the component ID.
     *
     * @return The ID as a string
     */
    public String getComponentID() {
        return "";
    }

    /**
     * Gets the model description.
     *
     * @return The model description string
     */
    public String getModelDescription() {
        return "";
    }

    /**
     * Gets the model ID.
     *
     * @return The model ID string
     */
    public String getModelID() {
        return "";
    }

    /**
     * Gets the number of output exchange items.
     *
     * @return The number of output exchange items
     */
    public int getOutputExchangeItemCount() {
        return outputExchangeItems.size();
    }

    /**
     * Creates and adds an InputExchangeItem
     *
     * @param id          String ID of the exchange item
     * @param caption     String caption of the exchange item
     * @param description String description of the exchange item
     * @return newly created and added exchange item
     */
    public InputExchangeItem createInputExchangeItem(String id, String caption, String description) {
        InputExchangeItem item = new InputExchangeItem(this, id, caption, description);
        inputExchangeItems.add(item);
        return item;
    }

    /**
     * Creates and adds an OutputExchangeItem
     *
     * @param id          String ID of the exchange item
     * @param caption     String caption of the exchange item
     * @param description String description of the exchange item
     * @return newly created and added exchange item
     */
    public OutputExchangeItem createOutputExchangeItem(String id, String caption, String description) {
        OutputExchangeItem item = new OutputExchangeItem(this, id, caption, description);
        outputExchangeItems.add(item);
        return item;
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
    public void addLink(ILink link) {
        if ((link != null) && (!allLinks.contains(link))) {
            allLinks.add(link);
        }
    }

    @Override
    public boolean describesSameAs(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!super.describesSameAs(obj)) {
            return false;
        }

        LinkableComponent component = (LinkableComponent) obj;

        if (!nullEquals(getComponentID(), component.getComponentID())) {
            return false;
        }

        if (!nullEquals(getModelID(), component.getModelID())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!describesSameAs(obj)) {
            return false;
        }

        LinkableComponent component = (LinkableComponent) obj;
        return getInstanceID().equals(component.getInstanceID());
    }

    /**
     * Finds the link for the specified input exchange item.
     *
     * @param item
     * @return ILinkEx for specified exchange item (could be null)
     */
    public ILink findLinkForInputExchangeItem(IInputExchangeItem item) {
        return null;
    }

    /**
     * Checks if the component has a link for a input exchange item.
     *
     * @param item
     * @return True if there is a link
     */
    public boolean hasLinkForInputExchangeItem(IInputExchangeItem item) {
        return (findLinkForInputExchangeItem(item) != null);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + getComponentID().hashCode() + getModelID().hashCode() + getInstanceID().hashCode();
    }

    /**
     * Finish clears up allocated memory and closes files used by the model
     * and the component. After this method is called no other methods should
     * be called on the LinkableComponent. Doing so might have unexpected
     * results.
     * <p/>
     * This method is final. All standard OpenMI work will be done here,
     * custom operations are to be implemented in the finishHook().
     */
    public final void finish() {
        sendEvent(new Event(null, IEvent.EventType.Informative, this, "Start Finishing"));
        finishHook();
        sendEvent(new Event(null, IEvent.EventType.Informative, this, "... Finishing Done"));
    }

    /**
     * Hook method called from the finish() method at the appropriate time.
     * Classes deriving from LinkableComponent should override this method
     * to implement custom behaviour.
     */
    public void finishHook() {
        // void
    }

    /**
     * Gets an input exchange item by its index.
     *
     * @param index The index of the input exchange item
     * @return The IInputExchangeItem
     */
    public IInputExchangeItem getInputExchangeItem(int index) {
        return inputExchangeItems.get(index);
    }

    /**
     * Gets a link by its ID.
     *
     * @param ID The ID for the link to return
     * @return The ILink found, or null
     */
    protected ILink getLink(String ID) {
        for (ILink l : allLinks) {
            if (ID.equals(l.getID())) {
                return l;
            }
        }
        return null;
    }

    /**
     * Gets an output exchange item by its index.
     *
     * @param index The index of the output exchange item
     * @return The IOutputExchangeItem for the specified index
     */
    public IOutputExchangeItem getOutputExchangeItem(int index) {
        return outputExchangeItems.get(index);
    }

    /**
     * Gets the computed values.
     * <p/>
     * This method is final. All standard OpenMI work will be done here,
     * custom operations are to be implemented in the getValuesHook().
     *
     * @param time   The timestamp/timespan for which to return values
     * @param linkID The linkID describing on which link values to return
     * @return The computed values
     */
    public final IValueSet getValues(ITime time, String linkID) {
        sendEvent(new Event(time, IEvent.EventType.Informative, this, "Start Get Values"));

        IValueSet result = new NullValueSet();

        ILink link = getLink(linkID);
        if (link == null) {
            sendEvent(new Event(time,
                    IEvent.EventType.Warning,
                    this,
                    String.format("... Request for non existing link '%s', proceeding anyway", linkID)));
        }

        result = getValuesHook(time, link);

        sendEvent(new Event(time, IEvent.EventType.Informative, this, "... Get Values Done"));

        return result;
    }

    /**
     * Hook method called from the getValues() method at the appropriate time.
     * Classes deriving from LinkableComponent should override this method
     * to implement custom behaviour.
     *
     * @param time  The timestamp/timespan for which to return values
     * @param link The Link on which link values to return (can be null!)
     * @return The computed values, when not overwritten a NullValueSet()!
     */
    public IValueSet getValuesHook(ITime time, ILink link) {
        return new NullValueSet();
    }

    /**
     * Initializes the component with the given arguments.
     * 
     * This method is final. All standard OpenMI work will be done here,
     * custom operations are to be implemented in the initializeHook().
     *
     * @param properties The properties to be initialized
     */
    public final void initialize(IArgument[] properties) {
        sendEvent(new Event(null, IEvent.EventType.Informative, this, "Start Initialising"));
        initializeHook(properties);
        sendEvent(new Event(null, IEvent.EventType.Informative, this, "... Initialising Done"));
    }

    /**
     * Hook method called from the initialize() method at the appropriate time.
     * Classes deriving from LinkableComponent should override this method
     * to implement custom behaviour.
     *
     * @param properties The properties to be initialized
     */
    public void initializeHook(IArgument[] properties) {
        // void
    }

    /**
     * Called before computation.
     * 
     * This method is final. All standard OpenMI work will be done here,
     * custom operations are to be implemented in the prepareHook().
     */
    public final void prepare() {
        sendEvent(new Event(null, IEvent.EventType.Informative, this, "Start Preparing"));
        prepareHook();
        sendEvent(new Event(null, IEvent.EventType.Informative, this, "... Preparing Done"));
    }

    /**
     * Hook method called from the prepare() method at the appropriate time.
     * Classes deriving from LinkableComponent should override this method
     * to implement custom behaviour.
     */
    public void prepareHook() {
        // void
    }

    /**
     * Notifies the component that a link is being removed. When needed the
     * component removes its references to the link so that it will no more
     * attempts will be made to use it in the future.
     *
     * @param ID The string ID of the link being removed
     */
    public void removeLink(String ID) {
        allLinks.remove(getLink(ID));
    }

    @Override
    public String toString() {
        return getID();
    }

    /**
     * Checks if the component is valid (contains valid data).
     *
     * @return Empty string if the component is valid, otherwise some message
     *         explaining why the component is invalid
     */
    public String validate() {
        return "Component does not implement the validate method and is maybe not valid!";
    }


    /**
     * Dispose of object... Use carefully to avoid problems with the standard
     * garbage collection process.
     */
    public void dispose() {
        // void
    }

    /**
     * Gets the ID of this instance.
     *
     * @return the instance ID
     */
    public String getInstanceID() {
        return instanceID;
    }

    /**
     * Sets the ID of this instance.
     *
     * @param instanceID the new ID of the instance of a component
     */
    public void setInstanceID(String instanceID) {
        this.instanceID = instanceID;
    }
}