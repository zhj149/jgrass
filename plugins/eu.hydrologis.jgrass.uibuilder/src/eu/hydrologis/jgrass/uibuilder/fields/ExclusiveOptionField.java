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

package eu.hydrologis.jgrass.uibuilder.fields;

import org.w3c.dom.Node;

/**
 * Represents a data field for an exclusive option.
 * 
 * @author Patrick Ohnewein
 */
public class ExclusiveOptionField extends GenericOptionField {

	public ExclusiveOptionField(Node xmlNode, String name, String desc, String repr, boolean required, String defaultValue, int order) {
		super(xmlNode, name, desc, repr, required, defaultValue, order);
	}
}
