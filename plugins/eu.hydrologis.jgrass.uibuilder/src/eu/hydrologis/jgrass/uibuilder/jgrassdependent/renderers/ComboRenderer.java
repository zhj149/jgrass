/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
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
package eu.hydrologis.jgrass.uibuilder.jgrassdependent.renderers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import eu.hydrologis.jgrass.uibuilder.fields.DataField;
import eu.hydrologis.jgrass.uibuilder.swt.SWTSpreadsheetLayout;
import eu.hydrologis.jgrass.uibuilder.swt.renderers.InputFieldRenderer;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class ComboRenderer extends InputFieldRenderer {

    private Composite valueComponent;
    private Combo combo;

    public ComboRenderer( DataField df ) {
        super(df);
    }

    public boolean hasValueComponent() {
        return true;
    }

    public Control renderValueComponent( final Composite parent, String constraints ) {
        if (valueComponent == null) {
            valueComponent = new Composite(parent, SWT.NONE);
            valueComponent.setLayoutData(constraints);

            SWTSpreadsheetLayout layout = new SWTSpreadsheetLayout("A=D");
            layout.marginWidth = layout.marginHeight = 0;
            valueComponent.setLayout(layout); // new RowLayout(SWT.HORIZONTAL));

            String defValue = (String) getDataField().getValue();

            String[] valueSplit = defValue.split(","); //$NON-NLS-1$

            combo = new Combo(valueComponent, SWT.DROP_DOWN | SWT.READ_ONLY);
            combo.setLayoutData("A0");
            combo.setItems(valueSplit);
            combo.select(0);
            setValue(valueSplit[0]);
            combo.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    int selectionIndex = combo.getSelectionIndex();
                    setValue(combo.getItem(selectionIndex));
                }
            });

        }
        return valueComponent;
    }

    public void setEnabled( boolean enabled ) {
        combo.setEnabled(enabled);
    }

    /**
     * Set a user-entered value in the corresponding <code>DataField</code>
     * 
     * @param value the new value to set
     */
    private void setValue( String value ) {
        getDataField().setValue(value);
    }

}
