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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.jgrass.uibuilder.fields.DataField;
import eu.hydrologis.jgrass.uibuilder.swt.SWTSpreadsheetLayout;

public class CommandFieldRenderer extends AbstractSWTRenderer {

	private Composite valueComponent;

	public CommandFieldRenderer(DataField df) {
        super(df);
	}

	@Override
	public boolean isContainer() {
		// The CommandFieldRenderer is the main container. This allows us to specify special
		// layout settings and other attributes like minimum size of the shell.
		return true;
	}
	
	public boolean hasValueComponent() {
		return true;
	}

	public Control renderValueComponent(Composite parent, String constraints) {
		if (valueComponent == null) {
			DataField df = getDataField();

			valueComponent = new Composite(parent, SWT.NONE);
			valueComponent.setLayout(new SWTSpreadsheetLayout(df.getAttributeValue("layoutsettings", "B=D")));
			valueComponent.setLayoutData(constraints);

			Shell shell = parent.getShell();
			shell.setText(df.translate(df.getDisplayName()));
			
			// if minwidth and/or minheight attributes are specified, set minimum size of the shell
			int minWidth = -1;
			int minHeight = -1;
			try {
				String str = df.getAttributeValue("minwidth");
				if (str != null)
					minWidth = Integer.parseInt(str);
			}
			catch (Exception ignore) {
			}
			try {
				String str = df.getAttributeValue("minheight");			
				if (str != null)
					minHeight = Integer.parseInt(str);
			}
			catch (Exception ignore) {
			}
			if (minWidth >= 0 || minHeight >= 0)
				shell.setMinimumSize(minWidth, minHeight);
		}
		return valueComponent;
	}
}
