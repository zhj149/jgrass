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

import java.util.ArrayList;
import java.util.Locale;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a data field of some type.
 * 
 * @author Patrick Ohnewein
 */
public abstract class DataField implements Comparable<DataField> {

	/**
	 * The XML which rapresents this DataField.
	 * Can be null, because it is not required that DataField objects
	 * get loaded from XML files. But if loaded the node can contain
	 * additional attributes.
	 */
	private Node xmlNode;
	
	/**
	 * The (unique) field name
	 */
	protected String name;

	/**
	 * A short description of the field which will be shown in the UI
	 */
	protected String desc;

	/**
	 * The command line representation of the field
	 */
	protected String repr;

	/**
	 * Whether this field must be non-empty
	 */
	protected boolean required;

	/**
	 * The default value of the field
	 */
	protected String defaultValue;

	/**
	 * The order in which the field should appear in the UI
	 */
	protected int order;

	/**
	 * The parent of this field
	 */
	protected DataField parent;

	/**
	 * A <code>ArrayList</code> of this field's children
	 */
	protected ArrayList<DataField> children;

	/**
	 * The <code>Object</code> representing this field in the UI
	 */
	protected Object uiRepr;

	/**
	 * Instantiate a new <code>DataField</code>.
	 * 
	 * @param xmlNode optional xml node representing the data field
	 * @param name  the (unique) name of the field
	 * @param desc  a short description of the field
	 * @param repr  the command line representation of the field (<em>#</em> gets replaced by the actual value)
	 * @param order the order in which the field should appear when rendered in the UI together with other fields
	 */
	public DataField(Node xmlNode, String name, String desc, String repr, boolean required, String defaultValue, int order) {
		this.xmlNode = xmlNode;
		this.name = name;
		this.desc = desc;
		this.repr = repr;
		this.required = required;
		this.defaultValue = defaultValue;
		this.order = order;
		if (defaultValue != null) {
			setValue(defaultValue);
		}
	}

	/**
	 * Get the field value.
	 * 
	 * @return an <code>Object</code> representing the value of the field
	 */
	public abstract Object getValue();

	/**
	 * Get the field value as a String Object.
	 * 
	 * A null value will return a void String object, because
	 * this method doesn't distinguish between null and "", being
	 * it primarily used for converting the value into a displayable
	 * form for the renderer components.
	 * 
	 * @return a <code>String</code> representing the value of the field
	 */
	public String getValueAsString() {
		Object value = getValue();
		// we do not distinguish between null and void strings
		return value == null ? "" : String.valueOf(value);
	}

	/**
	 * Set the field value.
	 * 
	 * @param value the new value
	 */
	public abstract void setValue(Object value);

	/**
	 * Get the command line representation of the field.
	 * 
	 * @return the command line representation of the field
	 * @throws MissingValueException if the field is required and empty
	 */
	public abstract String getCommandLineRepresentation() throws MissingValueException;

	/**
	 * Get the value of the specified attribute.
	 * 
	 * @param attributeName The name of the attribute which value has to be returned
	 * @param defaultValue The value to return if the attribute was not specified.
	 * @return The value or defaultValue, if the attribute was not specified.
	 */
	public String getAttributeValue(String attributeName, String defaultValue) {
		String value = defaultValue;
		if (xmlNode != null) {
			NamedNodeMap map = xmlNode.getAttributes();
			if (map != null) {
				Node attrNode = map.getNamedItem(attributeName);
				if (attrNode != null)
					value = attrNode.getNodeValue();
			}
		}
		return value;
	}
	
	/**
	 * Get the value of the specified attribute.
	 * 
	 * @param attributeName The name of the attribute which value has to be returned
	 * @return The value or null, if the attribute was not specified.
	 */
	public String getAttributeValue(String attributeName) {
		return getAttributeValue(attributeName, null);
	}

	/**
	 * Get the name of this field.
	 * 
	 * @return the name of this field
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Get the parent of this field.
	 * 
	 * @return the parent of this field
	 */
	public final DataField getParent() {
		return parent;
	}

	/**
	 * Get the <code>ArrayList</code> of children of this field.
	 * 
	 * @return a <code>ArrayList</code> of children
	 */
	public final ArrayList<DataField> getChildren() {
		return children;
	}

	/**
	 * Get the description of this field.
	 * 
	 * @return the description of this field
	 */
	public final String getDescription() {
		return desc;
	}

	/**
	 * Get the name that will be displayed in the UI.
	 * 
	 * @return the name that will be displayed in the UI
	 */
	public final String getDisplayName() {
		String desc = getDescription();
		return desc != null && desc.length() > 0 ? desc : getName();
	}

	/**
	 * Looks recursively for i18n nodes with the right translation in the 
	 * given XML node and if needed in its parent nodes.
	 *  
	 * @param xmlNode The node to start looking for the right translation.
	 * @param key The key to be translated.
	 * @param lang The language code of the needed translation.
	 * @return The translation or null.
	 */
	private static String translateFromXMLNodeTree(Node xmlNode, String key, String lang) {
		String translation = null;
		NodeList nodeList = xmlNode.getChildNodes();
		for (int i = nodeList.getLength(); i-- > 0; ) {
			Node node = nodeList.item(i);
			if ("i18n".equals(node.getNodeName())) {
				NamedNodeMap attributeMap = node.getAttributes();
				Node nodeKeyAttr = attributeMap.getNamedItem("key");
				if (nodeKeyAttr != null && key.equals(nodeKeyAttr.getNodeValue())) {
					Node nodeValueAttr = attributeMap.getNamedItem(lang);
					if (nodeValueAttr != null)
						translation = nodeValueAttr.getNodeValue();
				}
			}
		}
		if (translation == null) {
			// no translation found, look if parent is able to translate
			Node parent = xmlNode.getParentNode();
			if (parent != null)
				translation = translateFromXMLNodeTree(parent, key, lang);
		}
		return translation;
	}

	/**
	 * Translate key into the specified language.
	 * 
	 * @param key Text to be translated.
	 * @param lang Language into which the text should be translated.
	 * @return The translation or the key, if no translation available.
	 */
	public String translate(String key, String lang, String defaultValue) {
		String translation = translateFromXMLNodeTree(xmlNode, key, lang);
		// hook for system wide translations
		//if (translation == null) {
		// check translation in resource bundles other than the XML file
		// (other property files, JGrass, ...)
		//}
		return translation == null ? defaultValue : translation;
	}

	/**
	 * Translate key into the specified language.
	 * 
	 * @param key Text to be translated.
	 * @param lang Language into which the text should be translated.
	 * @return The translation or the key, if no translation available.
	 */
	public String translate(String key, String lang) {
		return translate(key, lang, key);
	}

	/**
	 * Translate key into the actual language.
	 * 
	 * @see DataField#getActualLanguage()
	 * 
	 * @param key Text to be translated.
	 * @return The translation or the key, if no translation available.
	 */
	public String translate(String key) {
		return translate(key, getActualLanguage(), key);
	}

	/**
	 * Get the actual language.
	 * @return The language ISO code.
	 */
	public String getActualLanguage() {
		return Locale.getDefault().getLanguage();
	}
	
	/**
	 * Get the <code>Object</code> representing this field in the UI.
	 * 
	 * @return the <code>Object</code> representing this field in the UI
	 */
	public final Object getUIRepresentation() {
		return uiRepr;
	}

	/**
	 * Set the parent of this field.
	 * 
	 * @param parent the parent of this field
	 */
	public final void setParent(DataField parent) {
		this.parent = parent;
	}

	/**
	 * Set the <code>ArrayList</code> of children of this field.
	 * 
	 * @param children a <code>ArrayList</code> of children
	 */
	public final void setChildren(ArrayList<DataField> children) {
		this.children = children;
	}

	/**
	 * Set the <code>Object</code> representing this field in the UI.
	 * @param uiRepr the <code>Object</code> representing this field in the UI
	 */
	public final void setUIRepresentation(Object uiRepr) {
		this.uiRepr = uiRepr;
	}

	/**
	 * Compare fields (used for ordering).
	 * 
	 * @param df the object to compare to (must be of type <code>DataField</code>)
	 */
	public final int compareTo(DataField df) {
		return (order - df.order);
	}

	/**
	 * Check whether a field is required.
	 * 
	 * @return whether this field must be non-empty
	 */
	public final boolean isRequired() {
		return required;
	}

	/**
	 * Get a meaningful representation of the instance.
	 * 
	 * @return a meaningful representation of the instance
	 */
	public String toString() {
		return getClass().getSimpleName() + "(" + name + ")";
	}
}