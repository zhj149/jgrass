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
import org.eclipse.swt.widgets.Label;

import eu.hydrologis.jgrass.uibuilder.fields.DataField;

public abstract class InputFieldRenderer extends AbstractSWTRenderer {

	private Label label;

	public InputFieldRenderer(DataField df) {
        super(df);
	}
	
	public boolean hasLabelComponent() {
		return true;
	}

	public Control renderLabelComponent(Composite parent, String constraints) {
		if (label == null) {
			label = new Label((Composite)parent, SWT.LEAD);
			label.setLayoutData(constraints);
			DataField df = getDataField();
			label.setText(df.translate(df.getDisplayName()) + ":");
		}
		return label;
	}
}
