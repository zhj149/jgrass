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
package eu.hydrologis.jgrass.uibuilder.swt.renderers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.hydrologis.jgrass.uibuilder.fields.DataField;
import eu.hydrologis.jgrass.uibuilder.renderers.Renderer;
import eu.hydrologis.jgrass.uibuilder.swt.SWTSpreadsheetLayout;

public class CheckBoxFieldRenderer extends AbstractSWTRenderer {

    private Button button;
    private Composite valueComponent;

    public CheckBoxFieldRenderer( DataField df ) {
        super(df);
    }

    public boolean hasValueComponent() {
        return true;
    }

    public Control renderValueComponent( Composite parent, String constraints ) {
        if (valueComponent == null) {
            valueComponent = new Composite(parent, SWT.NONE);
            valueComponent.setLayoutData(constraints);
            valueComponent.setLayout(new SWTSpreadsheetLayout("A=D"));
            button = new Button(valueComponent, SWT.CHECK);
            button.setLayoutData("A0");
            DataField df = getDataField();
            button.setText(df.translate(df.getDisplayName()));
            Object value = df.getValue();
            final boolean defaultValue = value instanceof Boolean
                    && ((Boolean) value).booleanValue();
            setValue(defaultValue);
            Display.getDefault().asyncExec(new Runnable(){
                public void run() {
                    updateChildren(defaultValue);
                }
            });
            button.setSelection(defaultValue);
            button.addSelectionListener(new SelectionListener(){
                public void widgetDefaultSelected( SelectionEvent e ) {
                    System.out.println("widgetDefaultSelected(SelectionEvent e)");
                }
                public void widgetSelected( SelectionEvent e ) {
                    boolean newValue = ((Button) e.widget).getSelection();
                    setValue(newValue);
                    updateChildren(newValue);
                }
            });
        }
        return valueComponent;
    }

    /**
     * Set a user-entered value in the corresponding <code>DataField</code>
     * 
     * @param value the new value to set
     */
    private void setValue( boolean value ) {
        getDataField().setValue(value ? "true" : "false");
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
            int timeout = 0;
            while( r == null && timeout < 1000 ) {
                r = (Renderer) df.getChildren().get(i).getUIRepresentation();
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                timeout = timeout + 300;
            }
            r.setEnabled(enabled);
        }
    }
}
