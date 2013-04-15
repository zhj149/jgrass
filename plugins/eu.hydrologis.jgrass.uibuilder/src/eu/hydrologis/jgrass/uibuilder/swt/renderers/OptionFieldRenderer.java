/*
 * UIBuilder - a framework to build user interfaces out from XML files
 * Copyright (C) 2007-2008 Patrick Ohnewein
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.hydrologis.jgrass.uibuilder.swt.renderers;

import java.util.IdentityHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import eu.hydrologis.jgrass.uibuilder.fields.DataField;
import eu.hydrologis.jgrass.uibuilder.renderers.Renderer;
import eu.hydrologis.jgrass.uibuilder.swt.SWTButtonGroup;
import eu.hydrologis.jgrass.uibuilder.swt.SWTSpreadsheetLayout;

public class OptionFieldRenderer extends AbstractSWTRenderer {

    private Button button;
    private Composite valueComponent;

    public OptionFieldRenderer( DataField df ) {
        super(df);
    }

    public boolean hasValueComponent() {
        return true;
    }

    // private SWTButtonGroup testButtonGroup;
    private static IdentityHashMap<DataField, SWTButtonGroup> buttongroupForCompositeMap = new IdentityHashMap<DataField, SWTButtonGroup>();

    public Control renderValueComponent( Composite parent, String constraints ) {
        if (valueComponent == null) {

            // the button will be placed in an empty composite,
            // this prevents it from talking to other buttons and
            // we can use an independent ButtonGroup

            valueComponent = new Composite(parent, SWT.NONE);
            valueComponent.setLayoutData(constraints);
            valueComponent.setLayout(new SWTSpreadsheetLayout("A=D"));
            button = new Button(valueComponent, allowsMulti() ? SWT.CHECK : SWT.RADIO);

            SelectionListener selListener = new SelectionListener(){
                public void widgetDefaultSelected( SelectionEvent e ) {
                }
                public void widgetSelected( SelectionEvent e ) {
                    boolean newValue = ((Button) e.widget).getSelection();
                    setValue(newValue);
                    updateChildren(newValue);
                }
            };

            DataField df = getDataField();
            DataField parentField = df.getParent();
            SWTButtonGroup currentButtonGroup = buttongroupForCompositeMap.get(parentField);
            if (currentButtonGroup == null) {
                currentButtonGroup = new SWTButtonGroup(allowsMulti());
                buttongroupForCompositeMap.put(parentField, currentButtonGroup);
            }
            currentButtonGroup.registerButton(button, selListener);

            // if (testButtonGroup == null)
            // testButtonGroup = new SWTButtonGroup(allowsMulti());
            // testButtonGroup.registerButton(button, selListener);

            button.setLayoutData("A0");

            button.setText(df.translate(df.getDisplayName()));
            Object value = df.getValue();
            boolean defaultValue = value instanceof Boolean && ((Boolean) value).booleanValue();
            setValue(defaultValue);
            button.setSelection(defaultValue);
            button.addSelectionListener(selListener);
        }
        return valueComponent;
    }

    /**
     * Whether this option allows multiple choices or not.
     * 
     * @return <code>true</code> if this option allows multiple choices, <code>false</code>
     *         otherwise
     */
    private boolean allowsMulti() {
        return (getDataField().getParent().getClass().getSimpleName().equals("MultiOptionField"));
    }

    /**
     * Set a user-entered value in the corresponding <code>DataField</code>
     * 
     * @param value the new value to set
     */
    private void setValue( boolean value ) {
        getDataField().setValue(value ? "true" : "");
    }

    /**
     * Enable or disable children based on the user selection
     * 
     * @param enabled whether to enable the children or not
     */
    private void updateChildren( boolean enabled ) {
        DataField df = getDataField();
        for( int i = 0; i < df.getChildren().size(); i++ ) {
            Renderer r = (Renderer) df.getChildren().get(i).getUIRepresentation();
            r.setEnabled(enabled);
        }
    }
}
