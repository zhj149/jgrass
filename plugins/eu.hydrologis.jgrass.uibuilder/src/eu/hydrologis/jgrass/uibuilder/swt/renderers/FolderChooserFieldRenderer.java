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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.uibuilder.fields.DataField;
import eu.hydrologis.jgrass.uibuilder.swt.SWTSpreadsheetLayout;

public class FolderChooserFieldRenderer extends InputFieldRenderer {

    private Text text;
    private Button button;
    private Composite valueComponent;

    public FolderChooserFieldRenderer( DataField df ) {
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

            text = new Text(valueComponent, SWT.SINGLE | SWT.BORDER);
            text.setLayoutData("A0");
            text.setText("");
            text.setEditable(true);
            text.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    setValue(((Text) e.widget).getText());
                }
            });

            button = new Button(valueComponent, SWT.PUSH);
            button.setLayoutData("B0");
            button.setText("...");
            button.addListener(SWT.Selection, new Listener(){
                public void handleEvent( Event event ) {
                    DirectoryDialog f = new DirectoryDialog(parent.getShell(), SWT.OPEN);
                    String newValue = f.open();
                    if (newValue != null) {
                        updateTextField(newValue);
                        setValue(newValue);
                    }
                }
            });
        }
        return valueComponent;
    }

    public void setEnabled( boolean enabled ) {
        button.setEnabled(enabled);
        text.setEnabled(enabled);
    }

    /**
     * Set a user-entered value in the corresponding <code>DataField</code>
     * 
     * @param value the new value to set
     */
    private void setValue( String value ) {
        getDataField().setValue(value);
    }

    /**
     * Update the value component in the UI according to the user selection
     * 
     * @param value the new value to set
     */
    private void updateTextField( String value ) {
        text.setText(value);
    }
}
