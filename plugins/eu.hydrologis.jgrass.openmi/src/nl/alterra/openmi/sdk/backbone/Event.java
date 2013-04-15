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

import org.openmi.standard.*;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Events are used to send informative and warning messages.
 */
public class Event implements IEvent, Serializable {

    private HashMap<String, Object> attributeTable;
    private ILinkableComponent sender;
    private ITimeStamp simulationTime;
    private IEvent.EventType type;
    private String description;

    /**
     * Creates an instance, defaults to an informative event with empty
     * description.
     */
    public Event() {
        this(null, IEvent.EventType.Informative, null, "");
    }

    /**
     * Creates an instance with the given type and an empty description.
     *
     * @param type Type of event
     */
    public Event(IEvent.EventType type) {
        this(null, type, null, "");
    }

    /**
     * Creates an instance with the specified values.
     *
     * @param type        The IEvent.EventType for the event
     * @param description The description for the event
     */
    public Event(IEvent.EventType type, String description) {
        this(null, type, null, description);
    }

    /**
     * Creates an instance with the specified values.
     *
     * @param simulationTime the simulation time at which the event occurred
     * @param type           The IEvent.EventType for the event
     * @param sender         The ILinkableComponent that sends the event
     * @param description    The description for the event
     */
    public Event(ITime simulationTime, IEvent.EventType type, ILinkableComponent sender, String description) {
        this.simulationTime = null;

        if (simulationTime instanceof ITimeSpan) {
            this.simulationTime = ((ITimeSpan) simulationTime).getStart();
        }
        if (simulationTime instanceof ITimeStamp) {
            this.simulationTime = ((ITimeStamp) simulationTime);
        }

        this.type = type;
        this.sender = sender;
        this.description = description;

        attributeTable = new HashMap<String, Object>();
    }

    /**
     * Gets the type of the event.
     *
     * @return Type of event
     */
    public IEvent.EventType getType() {
        return type;
    }

    /**
     * Sets the type of the event.
     *
     * @param value Type of event
     */
    public void setType(IEvent.EventType value) {
        type = value;
    }

    /**
     * Gets the sender of the event.
     *
     * @return The event sender
     */
    public ILinkableComponent getSender() {
        return sender;
    }

    /**
     * Sets the sender of the event.
     *
     * @param obj The sender object
     */
    public void setSender(ILinkableComponent obj) {
        sender = obj;
    }

    /**
     * Gets the timestamp for the event occurence.
     *
     * @return The timestamp
     */
    public ITimeStamp getSimulationTime() {
        return simulationTime;
    }

    /**
     * Sets the timestamp for the event occurence.
     *
     * @param stamp The timestamp
     */
    public void setSimulationTime(ITimeStamp stamp) {
        simulationTime = stamp;
    }

    /**
     * Gets the event description.
     *
     * @return The description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the event description.
     *
     * @param string The description string
     */
    public void setDescription(String string) {
        description = string;
    }

    /**
     * Sets an attribute for the event with a (key,value) pair.
     *
     * @param key key value for the attribute
     * @param val value (Objet) attribute
     */
    public void setAttribute(String key, Object val) {
        attributeTable.put(key, val);
    }

    /**
     * Gets an attribute for a given key.
     *
     * @param key Key value for the attribute
     * @return value The (object) attribute
     */
    public Object getAttribute(String key) {
        return (attributeTable.get(key));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        Event e = (Event) obj;

        if (!description.equals(e.description)) {
            return false;
        }

        if (!type.equals(e.type)) {
            return false;
        }

        if (!simulationTime.equals(e.simulationTime)) {
            return false;
        }

        if (sender != e.sender) {
            return false;
        }

        if (!attributeTable.equals(e.attributeTable)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + description.hashCode() + type.hashCode() +
                simulationTime.hashCode() + sender.hashCode() + attributeTable.hashCode();
    }

    @Override
    public String toString() {
        if (simulationTime == null) {
            return String.format("%-10s | %-20s | %-15s | %s", "", sender, type.name(), description);
        } else {
            return String.format("T=%-8s | %-20s | %-15s | %s", simulationTime, sender, type.name(), description);
        }
    }

}
