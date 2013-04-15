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

import eu.hydrologis.jgrass.uibuilder.fields.DataField;
import eu.hydrologis.jgrass.uibuilder.swt.SWTSpreadsheetLayout;

public class ExclusiveOptionFieldRenderer extends AbstractSWTRenderer {

	private Composite valueComponent;

	public ExclusiveOptionFieldRenderer(DataField df) {
        super(df);
	}

	public boolean hasValueComponent() {
		return true;
	}

	public Control renderValueComponent(Composite parent, String constraints) {
		if (valueComponent == null) {
			valueComponent = new Composite(parent, SWT.NONE);
			valueComponent.setLayoutData(constraints);
			valueComponent.setLayout(new SWTSpreadsheetLayout());
		}
	    return valueComponent;
	}

	public void setEnabled(boolean enabled) {
		if (valueComponent != null)
			valueComponent.setEnabled(enabled);
	}
}
