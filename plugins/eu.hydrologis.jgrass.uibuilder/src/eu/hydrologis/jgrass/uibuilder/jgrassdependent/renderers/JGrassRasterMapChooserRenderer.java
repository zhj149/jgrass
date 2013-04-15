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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;
import eu.hydrologis.jgrass.ui.utilities.JGRasterChooserDialog;
import eu.hydrologis.jgrass.uibuilder.fields.DataField;
import eu.hydrologis.jgrass.uibuilder.swt.SWTSpreadsheetLayout;
import eu.hydrologis.jgrass.uibuilder.swt.renderers.InputFieldRenderer;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class JGrassRasterMapChooserRenderer extends InputFieldRenderer {

    private Text text;
    private Button button;
    private Composite valueComponent;

    public JGrassRasterMapChooserRenderer( DataField df ) {
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
            text.setText(getDataField().getValueAsString());
            text.setEditable(true);
            text.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    super.keyReleased(e);
                    setValue(text.getText());
                }
            });

            button = new Button(valueComponent, SWT.PUSH);
            button.setLayoutData("B0");
            button.setText("...");
            button.addListener(SWT.Selection, new Listener(){
                public void handleEvent( Event event ) {
                    ScopedPreferenceStore m_preferences = (ScopedPreferenceStore) ConsoleEditorPlugin
                            .getDefault().getPreferenceStore();
                    final String mapset = m_preferences
                            .getString(PreferencesInitializer.CONSOLE_ARGV_MAPSET);
                    JGRasterChooserDialog tree = new JGRasterChooserDialog(mapset);
                    tree.open(button.getShell(), SWT.SINGLE);
                    String newValue = tree.getNameOfResourceAtIndex(0);
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
