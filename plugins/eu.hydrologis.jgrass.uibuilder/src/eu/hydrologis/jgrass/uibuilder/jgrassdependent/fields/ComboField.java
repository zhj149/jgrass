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
package eu.hydrologis.jgrass.uibuilder.jgrassdependent.fields;

import org.w3c.dom.Node;

import eu.hydrologis.jgrass.uibuilder.fields.InputField;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class ComboField extends InputField {

    private String value;

    public ComboField( Node xmlNode, String name, String desc, String repr, boolean required,
            String defaultValue, int order ) {
        super(xmlNode, name, desc, repr, required, defaultValue, order);
        setValue(defaultValue);
    }

    public Object getValue() {
        return value;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    public void setValue( Object value ) {
        setValue(String.valueOf(value));
    }
}
