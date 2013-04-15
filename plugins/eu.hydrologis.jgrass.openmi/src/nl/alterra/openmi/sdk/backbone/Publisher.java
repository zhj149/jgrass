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

import java.util.ArrayList;
import java.util.HashMap;
import org.openmi.standard.IEvent;
import org.openmi.standard.IEvent.EventType;
import org.openmi.standard.IListener;
import org.openmi.standard.IPublisher;

/**
 * Default implementation of an event publisher.
 */
public class Publisher extends BackboneObject implements IPublisher {

    /**
     * Map to register event listeners.
     */
    private HashMap<IEvent.EventType, Listeners> eventTable;
    
    /**
     * List of event types sent by this publisher.
     */
    private ArrayList<IEvent.EventType> eventTypes;
    
    /**
     * Boolean controlling if events are sent or not.
     */
    private boolean eventsBlocked;

    /**
     * Creates an instance that by default can only sent INFORMATIVE events.
     *
     * @param ID String ID
     */
    public Publisher(String ID) {
        this(ID, IEvent.EventType.Informative);
    }

    /**
     * Creates an instance that can sent the specified type of event.
     *
     * @param ID String ID
     * @param type IEvent.EventType that can be published
     */
    public Publisher(String ID, IEvent.EventType type) {
        this(ID, new IEvent.EventType[]{type});
    }

    /**
     * Creates an instance that can sent the specified types of events.
     *
     * @param id String ID
     * @param types Array of IEvent.EventTypes that can be published
     */
    private Publisher(String ID, IEvent.EventType[] types) {
        super(ID);
        eventsBlocked = false;
        eventTable = new HashMap<IEvent.EventType, Listeners>();
        eventTypes = new ArrayList<IEvent.EventType>();

        for (IEvent.EventType t : types) {
            eventTypes.add(t);
        }
    }

    /**
     * Sends an event to the registered listeners.
     *
     * @param event The event to be sent
     */
    public void sendEvent(IEvent event) {
        if (!eventsBlocked) {
            if (eventTable.containsKey(event.getType())) {
                eventTable.get(event.getType()).send(event);
            }
        }
    }

    /**
     * Gets the number of published event types.
     *
     * @return Number of provided event types
     */
    public int getPublishedEventTypeCount() {
        return eventTypes.size();
    }

    /**
     * Gets provided event type with the specified index.
     *
     * @param providedEventTypeIndex index in provided event types
     * @return Provided event type
     */
    public EventType getPublishedEventType(int providedEventTypeIndex) {
        return eventTypes.get(providedEventTypeIndex);
    }

    /**
     * Subscribes a listener to an event type.
     *
     * @param listener  The IListener to subscribe
     * @param eventType The type of event to subscribe to
     */
    public void subscribe(IListener listener, IEvent.EventType eventType) {
        if (!eventTable.containsKey(eventType)) {
            eventTable.put(eventType, new Listeners());
        }
        eventTable.get(eventType).add(listener);
    }

    /**
     * Subscribes a listener for multiple event types.
     *
     * @param listener The listener to subscribe
     * @param types    The event types
     */
    public void subscribe(IListener listener, IEvent.EventType[] types) {
        for (IEvent.EventType type : types) {
            subscribe(listener, type);
        }
    }

    /**
     * Unsubscribes a listener for an event type.
     *
     * @param listener  The IListener to unsubscribe
     * @param eventType The type of event to unsubscribe for
     */
    public void unSubscribe(IListener listener, IEvent.EventType eventType) {
        if (eventTable.containsKey(eventType)) {
            eventTable.get(eventType).remove(listener);
        }
    }

    /**
     * Unsubscribes a listener for multiple event types.
     *
     * @param listener The listener to unsubscribe
     * @param types    The event types
     */
    public void unSubscribe(IListener listener, IEvent.EventType[] types) {
        for (IEvent.EventType type : types) {
            unSubscribe(listener, type);
        }
    }

    /**
     * Returns true if the Publisher has one or more subscribed listeners.
     *
     * @return True if there are listeners
     */
    public boolean hasListeners() {
        for (IEvent.EventType et : eventTable.keySet()) {
            if (eventTable.get(et).size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the list of publishable events.
     * 
     * Note: Though this gives access to the internal list of event types,
     * editing it should be done with care. E.g. subscribed listeners to
     * a certain event type are not automatically removed when the event
     * type is removed from the returned collection. A future version should
     * implement this differently.
     *
     * @return The list of publishable events
     */
    public ArrayList<IEvent.EventType> getEventTypes() {
        return eventTypes;
    }

    /**
     * Get the eventsBlocked value, when it is true no events will be
     * published.
     *
     * @return True if events are not published
     */
    public boolean areEventsBlocked() {
        return eventsBlocked;
    }

    /**
     * Sets the eventsBlocked value, when true events will not be published.
     *
     * @param eventsBlocked
     */
    public void setEventsBlocked(boolean eventsBlocked) {
        this.eventsBlocked = eventsBlocked;
    }

}
