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

package eu.hydrologis.jgrass.uibuilder.swt;

import java.util.Properties;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.jgrass.uibuilder.UIBuilder;
import eu.hydrologis.jgrass.uibuilder.UIBuilderPlugin;
import eu.hydrologis.jgrass.uibuilder.fields.MissingValueException;
import eu.hydrologis.jgrass.uibuilder.parser.MissingAttributeException;

/**
 * Implements a <code>Dialog</code> for generated UIs.
 * 
 * @author Patrick Ohnewein
 */
public class SWTUIBuilderDialog extends Dialog {

    private UIBuilder guiBuilder;
    private String xmlFile;
    private Properties properties;

    /**
     * Instantiate a new <code>SWTDialog</code>.
     * 
     * @param parent the parent of this <code>Dialog</code>
     * @param xmlFile the path to the XML file containing fields' definitions.
     * @param properties a <code>Properties</code> object used to pass values
     */
    public SWTUIBuilderDialog( Shell parent, String xmlFile, Properties properties ) {
        super(parent);
        setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
        this.xmlFile = xmlFile;
        this.properties = properties;
    }

    protected Control createDialogArea( Composite parent ) {
        Composite composite = (Composite) super.createDialogArea(parent);
        try {
            composite.setLayout(new SWTSpreadsheetLayout("B=D"));
            this.guiBuilder = new UIBuilder(this.xmlFile, composite, 0);
        } catch (MissingAttributeException e) {
            UIBuilderPlugin.log("Missing attribute", e);
            System.out.println("Missing attribute: " + e.getMessage());
        } catch (Exception e) {
            UIBuilderPlugin.log(e.getMessage(), e);
        }
        return composite;
    }

    protected void buttonPressed( int buttonId ) {
        if (buttonId == OK) {
            String commandLineRepr;
            try {
                commandLineRepr = this.guiBuilder.getCommandLineRepresentation();
                this.properties.put("CommandLine", commandLineRepr);
                close();
            } catch (MissingValueException e) {
                MessageBox m = new MessageBox(getShell(), SWT.OK);
                m.setMessage(e.getMessage());
                m.open();
            }
        } else {
            close();
        }
    }
}