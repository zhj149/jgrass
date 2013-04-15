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
package nl.alterra.openmi.sdk.persistence;

import nl.alterra.openmi.sdk.backbone.InputExchangeItem;
import nl.alterra.openmi.sdk.backbone.LinkableComponent;
import nl.alterra.openmi.sdk.backbone.OutputExchangeItem;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.IOutputExchangeItem;

/**
 * A placeholder linkable component to be used when reading an OpenMI
 * composition and trying to recreate its structure, but when some components
 * can no longer be created (invalid, missing class, etc.). Such components
 * should be replaced with this placeholder so that reading of the rest of the
 * composition can continue.
 */
public class PlaceholderComponent extends LinkableComponent {

    private static final String PLACEHOLDER =
            "Component could not be created: ";
    private static final String DUMMY_ITEM =
            "dummy item ";
    private static final String PLACEHOLDER_LINKABLE_COMPONENT =
            "Placeholder Linkable Component present that does not calculate!";

    /**
     * Creates an instance with the specified ID.
     *
     * @param ID String ID
     */
    public PlaceholderComponent(String ID) {
        super(ID);
    }

    @Override
    public String getComponentDescription() {
        return PLACEHOLDER + super.getComponentDescription();
    }

    @Override
    public String getComponentID() {
        return PLACEHOLDER + super.getComponentID();
    }

    @Override
    public String getModelDescription() {
        return PLACEHOLDER + super.getModelDescription();
    }

    @Override
    public String getModelID() {
        return PLACEHOLDER + super.getModelID();
    }

    @Override
    public String getCaption() {
        return PLACEHOLDER + super.getCaption();
    }

    @Override
    public String getDescription() {
        return PLACEHOLDER + super.getDescription();
    }

    @Override
    public String validate() {
        return PLACEHOLDER_LINKABLE_COMPONENT;
    }

    /**
     * Ensures that the placeholder has an output exchange item for the given
     * index. All the required dummy exchange items will be added.
     *
     * @param index
     */
    public void ensureOutputExchangeItem(Integer index) {
        String id;
        while (getOutputExchangeItemCount() < index + 1) {
            id = DUMMY_ITEM + String.valueOf(getOutputExchangeItemCount());
            createOutputExchangeItem(id, id, id);
        }
    }

    /**
     * Ensures that the placeholder has an input exchange item for the given
     * index. All the required dummy exchange items will be added.
     *
     * @param index
     */
    public void ensureInputExchangeItem(Integer index) {
        String id;
        while (getInputExchangeItemCount() < index + 1) {
            id = DUMMY_ITEM + String.valueOf(getInputExchangeItemCount());
            createInputExchangeItem(id, id, id);
        }
    }

    /**
     * Adapts the input exchange item indicated by the index to the specified
     * output exchange item, so that a link between them can be created.
     *
     * @param inputIndex
     * @param item
     */
    public void adaptInputTo(int inputIndex, IOutputExchangeItem item) {
        InputExchangeItem input = (InputExchangeItem) getInputExchangeItem(inputIndex);
        input.setQuantity(item.getQuantity());
        input.setElementSet(item.getElementSet());
    }

    /**
     * Adapts the output exchange item indicated by the index to the specified
     * input exchange item, so that a link between them can be created.
     *
     * @param outputIndex
     * @param item
     */
    public void adaptOutputTo(int outputIndex, IInputExchangeItem item) {
        OutputExchangeItem output = (OutputExchangeItem) getOutputExchangeItem(outputIndex);
        output.setQuantity(item.getQuantity());
        output.setElementSet(item.getElementSet());
    }

}