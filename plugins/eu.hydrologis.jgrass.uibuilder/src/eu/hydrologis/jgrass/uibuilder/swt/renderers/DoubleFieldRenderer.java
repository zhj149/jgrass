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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.uibuilder.fields.DataField;

public class DoubleFieldRenderer extends InputFieldRenderer {

    private Text valueComponent;

    public DoubleFieldRenderer( DataField df ) {
        super(df);
    }

    public boolean hasValueComponent() {
        return true;
    }

    public Control renderValueComponent( Composite parent, String constraints ) {
        if (valueComponent == null) {
            valueComponent = new Text(parent, SWT.SINGLE | SWT.BORDER);
            valueComponent.setLayoutData(constraints);
            valueComponent.setText(getDataField().getValueAsString());
            valueComponent.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    setValue(((Text) e.widget).getText());
                }
            });
        }
        return valueComponent;
    }

    public void setEnabled( boolean enabled ) {
        valueComponent.setEnabled(enabled);
    }

    /**
     * Set a user-entered value in the corresponding <code>DataField</code>
     * 
     * @param value the new value to set
     */
    private void setValue( String value ) {
        DataField df = getDataField();
        String oldValue = df.getValueAsString();
        try {
            df.setValue(value);
        } catch (IllegalArgumentException e) {
            valueComponent.setText(oldValue);
        }
    }
}
