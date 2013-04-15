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

package eu.hydrologis.jgrass.uibuilder;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.hydrologis.jgrass.uibuilder.fields.DataField;
import eu.hydrologis.jgrass.uibuilder.fields.MissingValueException;
import eu.hydrologis.jgrass.uibuilder.fields.OptionField;
import eu.hydrologis.jgrass.uibuilder.parser.MissingAttributeException;
import eu.hydrologis.jgrass.uibuilder.parser.XMLReader;
import eu.hydrologis.jgrass.uibuilder.renderers.Renderer;
import eu.hydrologis.jgrass.uibuilder.renderers.RendererFactory;
import eu.hydrologis.jgrass.uibuilder.renderers.SWTRendererFactory;

/**
 * Represents a UI built from an XML file.
 * 
 * @author Patrick Ohnewein
 */
public class UIBuilder {

	public static String COMMANDLINEPROPERTY = "CommandLine"; //$NON-NLS-1$
	public static String STARTDATEPROPERTY = "StartDate"; //$NON-NLS-1$
	public static String ENDDATEPROPERTY = "EndDate"; //$NON-NLS-1$
	public static String TIMESTEPPROPERTY = "TimeStep"; //$NON-NLS-1$

	private ArrayList<DataField> nodes;

	/**
	 * Instantiate a new <code>GUIBuilder</code>.
	 * 
	 * @param nodeList
	 * @param parent
	 *            the parent of this <code>GUIBuilder</code>
	 * @param renderer
	 *            the <code>Renderer</code> to use
	 * @param childOffset TODO
	 * @throws MissingAttributeException
	 *             if a required attribute is missing
	 */
	public UIBuilder(NodeList nodeList, Object parent, RendererFactory renderer, int childOffset)
			throws SAXException, IOException, MissingAttributeException,
			ParserConfigurationException {
		nodes = XMLReader.buildTree(nodeList);
		renderTree(nodes, parent, renderer, childOffset);
	}

	/**
	 * Instantiate a new <code>GUIBuilder</code>.
	 * 
	 * @param xmlFile
	 *            the path to the XML file containing fields' definitions.
	 * @param parent
	 *            the parent of this <code>GUIBuilder</code>
	 * @param renderer
	 *            the <code>Renderer</code> to use
	 * @param childOffset TODO
	 * @throws MissingAttributeException
	 *             if a required attribute is missing
	 */
	public UIBuilder(String xmlFile, Object parent, RendererFactory renderer, int childOffset)
			throws SAXException, IOException, MissingAttributeException,
			ParserConfigurationException {
		this(XMLReader.readFromFile(xmlFile), parent, renderer, childOffset);
	}

	public UIBuilder(String xmlFile, Object parent, int childOffset) throws SAXException,
			IOException, MissingAttributeException,
			ParserConfigurationException {
		this(xmlFile, parent, new SWTRendererFactory(), childOffset);
	}

	/**
	 * Get the command line representation of the fields.
	 * 
	 * @return the command line representation of the fields
	 * @throws MissingValueException
	 *             if a required value is missing
	 */
	public String getCommandLineRepresentation() throws MissingValueException {
		return buildCommandLineRepresentation(this.nodes);
	}

	/**
	 * Recursively build the command line representation for a (sub)tree of
	 * <code>DataField</code>s.
	 * 
	 * @param nodes
	 *            a <code>ArrayList</code> of <code>DataField</code>s
	 * @return the command line representation of the (sub)tree of
	 *         <code>DataField</code>s
	 * @throws MissingValueException
	 *             if a required value is missing
	 */
	private String buildCommandLineRepresentation(ArrayList<DataField> nodes)
			throws MissingValueException {
		String result = "";
		for (int i = 0; i < nodes.size(); i++) {
			result += nodes.get(i).getCommandLineRepresentation()
					+ buildCommandLineRepresentation(nodes.get(i).getChildren());
		}
		return result;
	}

	private void renderTree(ArrayList<DataField> nodes, Object parent,
			RendererFactory renderer, int childOffset) {
		renderTree(nodes, parent, renderer, true, childOffset);
	}

	/**
	 * Recursively render a (sub)tree of <code>DataField</code>s.
	 * 
	 * @param nodes
	 *            a <code>ArrayList</code> of <code>DataField</code>s to
	 *            render
	 * @param parentContainer
	 *            the (rendered) parent of the current <code>ArrayList</code>
	 *            of <code>DataField</code>s
	 * @param rendererFactory
	 *            the <code>Renderer</code> to use
	 * @param setDisabled
	 *            whether children should be disabled at creation
	 * @param childOffset
	 *            count of how many children have been placed in the parent
	 *            container
	 * @return the actualized childOffset
	 */
	private int renderTree(ArrayList<DataField> nodes, Object parentContainer,
			RendererFactory rendererFactory, boolean setDisabled,
			int childOffset) {
		for (int i = 0; i < nodes.size(); i++) {
			DataField df = nodes.get(i);
			Renderer r = rendererFactory.createRenderer(df);

			// create label component
			Object label = r.hasLabelComponent() ? r.renderLabelComponent(
					parentContainer, "A" + childOffset + "M") // "A0M-A0"
					: null;

			// create value component
			Object valueComponent = null;
			// if (df instanceof ExclusiveOptionField) {
			// Composite composite = new Composite((Composite) parentContainer,
			// SWT.NONE);
			//
			// valueComponent = r.hasValueComponent()
			// ? r.renderValueComponent(composite, label == null ? "A" +
			// childOffset
			// + "-B" + childOffset : "B" + childOffset)
			// : null;
			// } else {
			valueComponent = r.hasValueComponent() ? r.renderValueComponent(
					parentContainer, label == null ? "A" + childOffset + "-B"
							+ childOffset : "B" + childOffset) : null;
			// }

			if (label != null || valueComponent != null)
				childOffset++;

			r.setEnabled(setDisabled);
			boolean disableChildren = (!(df instanceof OptionField))
					|| Boolean.parseBoolean(String.valueOf(df.getValue()));
			if (valueComponent != null && r.isContainer())
				renderTree(df.getChildren(), valueComponent, rendererFactory,
						disableChildren, 0);
			else
				childOffset = renderTree(df.getChildren(), parentContainer,
						rendererFactory, disableChildren, childOffset);
		}
		return childOffset;
	}
}