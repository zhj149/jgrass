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
 *      Added a getValuesHook() to the trigger that calls the pull() method
 *      and returns the lastCalculatedValues. This makes the trigger behave
 *      more like an ordinary linkable component and avoids some confusion.
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.configuration;

import nl.alterra.openmi.sdk.backbone.InputExchangeItem;
import nl.alterra.openmi.sdk.backbone.LinkableComponent;
import nl.alterra.openmi.sdk.backbone.NullValueSet;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

/**
 * Trigger is a sink, i.e. a linkable component that only has a single input
 * exchange item to link to. It provides a pull() method that starts the
 * calculation of a linkable component chain. On a adaptTo() request the
 * trigger can adapt its input to match a specified output, so that a link
 * can be created.
 */
public class Trigger extends LinkableComponent {

    /**
     * The single input for the trigger.
     */
    protected InputExchangeItem input;

    /**
     * Stores the results of the last executed 'pull' of the trigger.
     */
    protected IValueSet lastCalculatedValues = new NullValueSet();

    /**
     * Creates an instance with the specified ID.
     *
     * @param ID String ID
     */
    public Trigger(String ID) {
        super(ID);
        input = createInputExchangeItem("input of " + ID, "input of " + ID, ID);
    }

    /**
     * 'Pulls' a trigger (OpenMI slang) to get its value. Usually the trigger
     * is linked to a chain of linkable components and requesting the values
     * from the trigger will start the calculation process. When the trigger
     * is not connected it will get a NullValueSet() as value.
     * 
     * The pull() itself does not return the values, when needed they can be
     * retrieved by calling trigger.getLastCalculatedValues() after the pull
     * method returns.
     *
     * @param time ITime to get values for
     */
    protected void pull(ITime time) {
        if (allLinks.size() == 1) {
            ILink link = allLinks.get(0);
            lastCalculatedValues = link.getSourceComponent().getValues(time, link.getID());
        }
        else {
            lastCalculatedValues = new NullValueSet();
        }
    }

    /**
     * Performs a pull() method on the trigger for the specified ITime. Since
     * a trigger can not have incoming links, the link argument will be
     * ignored. Most likely it will be null when the hook is called from the
     * getValues() method of the root LinkableComponent. After the pull()
     * completes the method returns the lastCalculatedValues.
     * 
     * @param time ITime to get values for
     * @param link The link argument is ignored
     * @return lastCalculatedValues
     */
    @Override
    public IValueSet getValuesHook(ITime time, ILink link) {
        pull(time);
        return lastCalculatedValues;
    }

    /**
     * Modifies the input of the trigger so that it can accept the specified
     * output exchange item as source. After the input is adapted, a link
     * between the output exchange item and the trigger can be created.
     *
     * @param item IOutputExchangeItem to adapt to
     */
    public void adaptTo(IOutputExchangeItem item) {
        if (canAdaptTo(item)) {
            input.setQuantity(item.getQuantity());
            input.setElementSet(item.getElementSet());
        }
    }

    /**
     * Checks if the trigger can be adapted to the specified output exchange
     * item. Currently always returns true, but might be overwritten by more
     * specialised trigger subclasses.
     *
     * @param item IOutputExchangeItem to test adaptability to
     * @return True if the trigger can be adapted to the output item
     */
    public boolean canAdaptTo(IOutputExchangeItem item) {
        return true;
    }

    /**
     * Gets the IValueSet that was the result of the last 'pull' of the
     * trigger (i.e. the values for its last calculation). If the trigger
     * has never been pulled (calculated) or is not connected the last
     * calculated values will be a NullValueSet.
     *
     * @return IValueSet The results of the last 'pull' (calculation)
     */
    public IValueSet getLastCalculatedValues() {
        return lastCalculatedValues;
    }

    @Override
    public void setCaption(String caption) {
        super.setCaption(caption);
        if (input != null) {
            input.setCaption("input of " + caption);
        }
    }

    @Override
    public void setDescription(String description) {
        super.setDescription(description);
        if (input != null) {
            input.setDescription(description);
        }
    }

    @Override
    public void setID(String id) {
        super.setID(id);
        if (input != null) {
            input.setID("input of " + id);
        }
    }

}
