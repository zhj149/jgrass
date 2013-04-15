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

import nl.alterra.openmi.sdk.backbone.Link;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import java.util.ArrayList;

/**
 * The composition class is a linkable component group and adds functionality
 * for defining and handling triggers.
 * 
 * When constructing a composition use the canConnect() and createLink()
 * methods provided by it to verify what links between linkable components
 * (including triggers) in the composition can be created and to actually
 * create them. The composition knows about triggers, and will allow them
 * to adapt to inputs they are to be linked to. The LinkableComponentGroup
 * does not support this.
 * 
 * The composition will find all the Triggers that are placed within in (even
 * when they are in nested LinkableComponentGroups) and allows the 'pulling'
 * of each trigger to obtain its value.
 */
public class Composition extends LinkableComponentGroup {

    /**
     * Creates an instance with the specified ID.
     *
     * @param ID String ID
     */
    public Composition(String ID) {
        super(ID);
    }

    /**
     * 'Pulls' a trigger (OpenMI slang) to get its value. Usually the trigger
     * is linked to a chain of linkable components and requesting the values
     * from the trigger will start the calculation process.
     * 
     * An ConfigurationException will be raised when a trigger is pulled that
     * is not part of the Composition (and its nested compositions).
     * 
     * The pull() itself does not return the values, when needed they can be
     * retrieved by calling trigger.getLastCalculatedValues() after the pull
     * method returns.
     *
     * @param trigger The trigger to get the values from
     * @param time    ITime to get values for
     */
    public void pull(Trigger trigger, ITime time) {
        if (getTriggers().contains(trigger)) {
            validate();
            trigger.pull(time);
        }
        else {
            throw new ConfigurationException(String.format("Requested trigger '%s' is unknown, can not provide values!", trigger));
        }
    }

    /**
     * Gets a list of all the triggers in the composition, including those
     * in nested compositions.
     *
     * @return ArrayList<Trigger>
     */
    public ArrayList<Trigger> getTriggers() {
        ArrayList<Trigger> triggers = new ArrayList<Trigger>();

        ILinkableComponent[] comps = getLinkableComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof Trigger) {
                triggers.add((Trigger) comps[i]);
            }

            if (comps[i] instanceof Composition) {
                triggers.addAll(((Composition) comps[i]).getTriggers());
            }
        }

        return triggers;
    }

    /**
     * Verifies wether a link between a given source and target can be
     * constructed. When the target is a Trigger it will be checked if it can
     * adapt to handle the specified source.
     *
     * @param source     ILinkableComponent
     * @param sourceItem IOutputExchangeItem
     * @param target     ILinkableComponent
     * @param targetItem IInputExchangeItem
     * @return Link.ValidationStatus
     */
    public Link.ValidationStatus canConnect(ILinkableComponent source, IOutputExchangeItem sourceItem,
            ILinkableComponent target, IInputExchangeItem targetItem) {
        if ((target instanceof Trigger) && ((Trigger) target).canAdaptTo(sourceItem)) {
            return Link.ValidationStatus.Ok;
        }
        else {
            return Link.canConnect(source, sourceItem, target, targetItem);
        }
    }

    /**
     * Creates a link between the given source and target. If the target is
     * a Trigger it will adapt (if possible) to accept the specified source
     * input. Returns the created link, or null when failed. You can use
     * the canConnect() method to validate a connection before actually
     * creating a link.
     *
     * @param source     ILinkableComponent
     * @param sourceItem IOutputExchangeItem
     * @param target     ILinkableComponent
     * @param targetItem IInputExchangeItem
     * @return The created Link, or null
     */
    @Override
    public Link createLink(ILinkableComponent source, IOutputExchangeItem sourceItem, ILinkableComponent target,
            IInputExchangeItem targetItem) {
        if (canConnect(source, sourceItem, target, targetItem) != Link.ValidationStatus.Ok) {
            return null;
        }
        else {
            if (target instanceof Trigger) {
                ((Trigger) target).adaptTo(sourceItem);
            }

            return super.createLink(source, sourceItem, target, targetItem);
        }
    }

}