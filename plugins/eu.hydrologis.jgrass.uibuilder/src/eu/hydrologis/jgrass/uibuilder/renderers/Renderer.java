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

package eu.hydrologis.jgrass.uibuilder.renderers;

/**
 * An object that can be rendered in the UI.
 * 
 * @author Patrick Ohnewein
 */
public interface Renderer {
	
	/**
	 * If this renderer has a label component the <CODE>renderLabelComponent(Object parent, Object contraints)</code>
	 * method will render one.
	 * 
	 * @return true if this renderer can render a label component.
	 */
	public boolean hasLabelComponent();
	
	/**
	 * Get the label component associated with this object.
	 * @param parent The parent container into which the label has to be rendered.
	 * @param contraints Informations about how the LayoutManager of the parent container should layout this component. 
	 * 
	 * @return an object representing a label component, or <code>null</code> if no label is needed
	 */
	public Object renderLabelComponent(Object parent, Object contraints);

	/**
	 * If this renderer has a value component the <CODE>renderValueComponent(Object parent, Object contraints)</code>
	 * method will render one.
	 * 
	 * @return true if this renderer can render a value component.
	 */
	public boolean hasValueComponent();

	/**
	 * Get the value component associated with this object.
	 * @param parent The parent container into which the value component has to be rendered.
	 * @param contraints Informations about how the LayoutManager of the parent container should layout this component. 
	 * 
	 * @return an object representing a value component
	 */
	public Object renderValueComponent(Object parent, Object constraints);

	/**
	 * Is the value component, rendered by <CODE>renderValueComponent(Object parent, Object contraints)</code>,
	 * a container and can it contain other components (i.e. it's children).
	 * 
	 * @return true value component is a container.
	 */
	public boolean isContainer();

	/**
	 * Enable or disable the object.
	 * 
	 * @param enabled whether the object should be enabled or not 
	 */
	public void setEnabled(boolean enabled);
}
