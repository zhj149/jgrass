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

import org.openmi.standard.IDataOperation;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IOutputExchangeItem;

/**
 * An output exchange item is an exchange item for an output in a provider.
 * It contains a quantity, element set, and a list of data operations.
 */
public class OutputExchangeItem extends ExchangeItem implements IOutputExchangeItem {

    private DataOperations dataOperations;

    /**
     * Creates an instance with the specified values. If the owner
     * ILinkableComponent also implements the ILinkableComponentEx interface
     * the created input exchange item will automatically be added to this
     * owner linkable component. The caption will be set to the ID and the
     * description will be left empty.
     *
     * @param owner ILinkableComponent that owns the exchange item
     * @param id    String ID of the exchange item
     */
    public OutputExchangeItem(ILinkableComponent owner, String id) {
        this(owner, id, id, "");
    }

    /**
     * Creates an instance with the specified values. If the owner
     * ILinkableComponent also implements the ILinkableComponentEx interface
     * the created input exchange item will automatically be added to this
     * owner linkable component.
     *
     * @param owner       ILinkableComponent that owns the exchange item
     * @param id          String ID of the exchange item
     * @param caption     String caption of the exchange item
     * @param description String description of the exchange item
     */
    public OutputExchangeItem(ILinkableComponent owner, String id, String caption, String description) {
        super(owner, id, caption, description);
        dataOperations = new DataOperations();
    }

    /**
     * Gets the collection of data operations.
     *
     * @return Returns the dataOperations
     */
    public DataOperations getDataOperations() {
        return dataOperations;
    }

    /**
     * Returns the number of data operations.
     *
     * @return Returns the number of data operations
     */
    public int getDataOperationCount() {
        return dataOperations.size();
    }

    /**
     * Returns a data operation.
     *
     * @param dataOperationIndex Index for the data operation
     * @return The data operation
     */
    public IDataOperation getDataOperation(int dataOperationIndex) {
        return dataOperations.get(dataOperationIndex);
    }

    /**
     * Adds a data operation.
     *
     * @param dataOperation The data operation to add
     */
    public void addDataOperation(IDataOperation dataOperation) {
        dataOperations.add(dataOperation);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + dataOperations.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        return (super.equals(obj) && (dataOperations.equals(((OutputExchangeItem) obj).dataOperations)));
    }

    /**
     * Checks if this item and a given item can be connected.
     *
     * @param itm The exchange item to compare with
     * @return True if the items are considered connectable
     */
    public boolean isConnectableWith(IInputExchangeItem itm) {
        return super.isConnectableWith(itm);
    }

}