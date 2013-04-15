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

import java.io.Serializable;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IExchangeItem;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IQuantity;

/**
 * The ExchangeItem is a combination of a quantity and an element set.
 */
public class ExchangeItem extends BackboneObject implements IExchangeItem, Serializable {

    private IQuantity quantity;
    private IElementSet elementSet;
    private ILinkableComponent owner;

    /**
     * Creates an instance with default values.
     *
     * @param owner The ILinkableComponent that contains the exchange item
     * @param Id    The String ID for the exchange item
     */
    public ExchangeItem(ILinkableComponent owner, String Id) {
        this(owner, "New ExchangeItem", Id);
    }

    /**
     * Creates an instance with the specified caption and a default Quantity
     * and ElementSet.
     *
     * @param owner   The linkablecomponent to which this exchangeitem belongs
     * @param id      The id string identifying this exchangeitem
     * @param caption The caption string for this ExchangeItem
     */
    public ExchangeItem(ILinkableComponent owner, String id, String caption) {
        this(owner, id, caption, "No description");
    }

    /**
     * Creates an instance with the specified caption and description and a default
     * Quantity and ElementSet.
     *
     * @param owner       The linkablecomponent to which this exchangeitem belongs
     * @param id          The id string identifying this exchangeitem
     * @param caption     The caption string for this ExchangeItem
     * @param description The string description for this ExchangeItem
     */
    public ExchangeItem(ILinkableComponent owner, String id, String caption, String description) {
        super(id);

        assert (owner != null);

        this.owner = owner;
        setCaption(caption);
        setDescription(description);

        quantity = new Quantity();
        elementSet = new ElementSet();
    }

    /**
     * Gets the element set.
     *
     * @return Returns the elementSet
     */
    public IElementSet getElementSet() {
        return elementSet;
    }

    /**
     * Gets the type of elements in the element set.
     *
     * @return The ElementType
     */
    public IElementSet.ElementType getElementType() {
        return elementSet.getElementType();
    }

    /**
     * Sets the element set.
     *
     * @param elementSet The elementSet
     */
    public void setElementSet(IElementSet elementSet) {
        this.elementSet = elementSet;
    }

    /**
     * Gets the quantity.
     *
     * @return Returns the quantity
     */
    public IQuantity getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity.
     *
     * @param quantity The quantity
     */
    public void setQuantity(IQuantity quantity) {
        this.quantity = quantity;
    }

    /**
     * Checks whether exchange items are connectable.
     * 
     * within openMI this method is called 'equals', which cannot be used with
     * Standard Java implementation of equals() method has already a different
     * meaning than what is actually checked in this implementation. This
     * method is there for named differently
     *
     * @param itm The exchange item to check for
     * @return True if the exchang items can be connected
     */
    protected boolean isConnectableWith(IExchangeItem itm) {
        if (itm == null) {
            return false;
        }

        if (!itm.getQuantity().equals(quantity)) {
            return false;
        }
        if (!itm.getElementSet().equals(elementSet)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        ExchangeItem item = (ExchangeItem) obj;

        if (!this.getID().equals(item.getID())) {
            return false;
        }

        // exchange items must belong to same linkable component to be equal
        // (as id for different instances are the same)
        if (!this.getOwner().equals(item.getOwner())) {
            return false;
        }

        return true;
    }

    /**
     * Gets the ILinkableComponent the exchange item belongs too.
     *
     * @return ILinkableComponent who owns the exchange item
     */
    public ILinkableComponent getOwner() {
        return owner;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + quantity.hashCode() + elementSet.hashCode();
    }

    public IQuantity.ValueType getValueSetType() {
        IQuantity q = getQuantity();
        if (q != null) {
            return q.getValueType();
        } else {
            return null;
        }
    }

    /**
     * As string return one of the following (in order):
     * - caption
     * - ID
     * - super.toString()
     *
     * @return String
     */
    public String toString() {
        if ((getCaption() != null) && (getCaption().length() > 0)) {
            return getCaption();
        }

        if ((getID() != null) && (getID().length() > 0)) {
            return getID();
        }

        return super.toString();
    }

}
