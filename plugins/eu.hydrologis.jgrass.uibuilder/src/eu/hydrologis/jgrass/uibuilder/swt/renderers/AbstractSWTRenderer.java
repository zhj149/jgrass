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


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import eu.hydrologis.jgrass.uibuilder.fields.DataField;
import eu.hydrologis.jgrass.uibuilder.renderers.Renderer;


/**
 * Base class for DataField renderers.
 * 
 * @author Patrick Ohnewein
 */
public abstract class AbstractSWTRenderer implements Renderer {

	private DataField df;

	/**
	 * Constructor.
	 * 
	 * @param df The DataField to render.
	 */
	public AbstractSWTRenderer(DataField df) {
		this.df = df;
	}

	/**
	 * Get the DataField Object to be rendered.
	 * 
	 * @return The DataField object.
	 */
	protected DataField getDataField() {
		return df;
	}

	/**
	 * Does this renderer render a label for this DataField object? 
	 */
	public boolean hasLabelComponent() {
		return false;
	}

	/**
	 * Get the label component for this DataField object.
	 * 
	 * <p>Only available if <code>hasLabelComponent()</code> returns true.
	 * </p> 
	 * 
	 * @param parent Parent container for the label component.
	 * @param contraints Constraints for layouting.
	 * @return The label component.
	 */
	public Control renderLabelComponent(Composite parent, String contraints) {
		throw new UnsupportedOperationException("Unimplemented method!");
	}

	/**
	 * Get the label component for this DataField object.
	 * 
	 * <p>Only available if <code>hasLabelComponent()</code> returns true.
	 * </p> 
	 * 
	 * <p>This method delegates the work to the more specialized
	 * <code>renderLabelComponent(Composite parent, String contraints)</code> method.
	 * </p> 
	 * 
	 * @param parent Parent container for the label component.
	 * @param contraints Constraints for layouting.
	 * @return The label component.
	 */
	public final Object renderLabelComponent(Object parent, Object constraints) {
		return hasLabelComponent()
			? renderLabelComponent((Composite)parent, df.getAttributeValue("labellayoutconstraints", (String)constraints))
			: null;
	}

	/**
	 * Does this renderer render a value component for this DataField object? 
	 */
	public boolean hasValueComponent() {
		return false;
	}

	/**
	 * Get the value component for this DataField object.
	 * 
	 * <p>Only available if <code>hasValueComponent()</code> returns true.
	 * </p> 
	 * 
	 * @param parent Parent container for the value component.
	 * @param contraints Constraints for layouting.
	 * @return The label component.
	 */
	public Control renderValueComponent(Composite parent, String constraints) {
		throw new UnsupportedOperationException("Unimplemented method!");
	}

	/**
	 * Get the value component for this DataField object.
	 * 
	 * <p>Only available if <code>hasValueComponent()</code> returns true.
	 * </p> 
	 * 
	 * <p>This method delegates the work to the more specialized
	 * <code>renderValueComponent(Composite parent, String contraints)</code> method.
	 * 
	 * @param parent Parent container for the value component.
	 * @param contraints Constraints for layouting.
	 * @return The label component.
	 */
	public final Object renderValueComponent(Object parent, Object constraints) {
		return hasValueComponent()
			? renderValueComponent((Composite)parent, df.getAttributeValue("layoutconstraints", (String)constraints))
			: null;
	}

	/**
	 * Is this node a container type?
	 * 
	 * <p>Container types will create container components as valueComponent and it's
	 * children will be placed as child components into this container. Otherwise
	 * the children nodes will be placed into the parents container and no additional
	 * container component (panel, composite) will be allocated.</p>
	 * 
	 * <p>Container types will also use LayoutManager objects to position their
	 * children. This means that layoutsettings attributes can be used.</p>
	 */
	public boolean isContainer() {
		return false;
	}

	/**
	 * Set the enable attribute of the components of this renderer.
	 */
	public void setEnabled(boolean enabled) {
	}
}
