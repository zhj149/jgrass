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

package eu.hydrologis.jgrass.uibuilder.parser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.refractions.udig.catalog.URLUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.hydrologis.jgrass.uibuilder.UIBuilderPlugin;
import eu.hydrologis.jgrass.uibuilder.fields.DataField;

/**
 * Parses the XML file containing the UI description.
 * 
 * @author Patrick Ohnewein
 */
public class XMLReader {

    /**
     * Get a <code>NodeList</code> from an XML document.
     * 
     * @param xmlFilePath the path to the XML document
     * @return a <code>NodeList</code> of the nodes in the document
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static NodeList readFromFile( String xmlFilePath ) throws SAXException, IOException,
            ParserConfigurationException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(new File(xmlFilePath));
        return document.getChildNodes();
    }

    /**
     * Associates node types to <code>DataField</code> classes.
     * 
     * @param nodeType the type of the node
     * @return a string representing the <code>DataField</code> class to load
     */
    private static String getClassForNode( String nodeType ) throws ParserConfigurationException,
            SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();

        // get the nodes.xml path
        String relURL = "conf/nodes.xml";
        Bundle bundle = Platform.getBundle(UIBuilderPlugin.PLUGIN_ID);
        URL fileURL = FileLocator.toFileURL(bundle.getResource(relURL));
        File urlToFile = URLUtils.urlToFile(fileURL);
        String path = urlToFile.getAbsolutePath();
        
        String nodesPath = bundle == null ? relURL : path; //$NON-NLS-1$
        Document doc = builder.parse(nodesPath);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression exp = xpath.compile("//node[@nodeType='" + nodeType + "']/@fieldClass");
        Object result = exp.evaluate(doc, XPathConstants.NODESET);
        Node node;
        return result != null && (node = ((NodeList) result).item(0)) != null
                ? node.getNodeValue()
                : null;
    }

    /**
     * Constructs a <code>DataField</code> of some kind from a <code>Node</code>.
     * 
     * @param node the node to use
     * @return a <code>DataField</code> representing the node
     * @throws MissingAttributeException if a required attribute is missing in the XML declaration
     */
    public static DataField getFieldFromNode( Node node ) throws MissingAttributeException {
        String nodeType = node.getNodeName();
        if (!nodeType.startsWith("#") && !"i18n".equals(nodeType)) {
            String fieldType = nodeType;
            try {
                fieldType = node.getAttributes().getNamedItem("type").getNodeValue();
            } catch (NullPointerException e) {
            }
            String fieldName;
            try {
                fieldName = node.getAttributes().getNamedItem("name").getNodeValue();
            } catch (NullPointerException e) {
                throw new MissingAttributeException("A field must have a name.");
            }
            String fieldDesc = fieldName;
            try {
                fieldDesc = node.getAttributes().getNamedItem("desc").getNodeValue();
            } catch (NullPointerException e) {
            }
            String fieldRepr = "#";
            try {
                fieldRepr = node.getAttributes().getNamedItem("repr").getNodeValue();
            } catch (NullPointerException e) {
            }
            boolean required = false;
            try {
                Node nodeRequired = node.getAttributes().getNamedItem("required");
                if (nodeRequired != null) {
                    String value = nodeRequired.getNodeValue();
                    if ("true".equalsIgnoreCase(value) || "1".equals(value))
                        required = true;
                }
            } catch (NullPointerException e) {
            }
            String defaultValue = null;
            try {
                defaultValue = node.getAttributes().getNamedItem("default").getNodeValue();
            } catch (NullPointerException e) {
            }
            int fieldOrder = Integer.MAX_VALUE;
            try {
                fieldOrder = Integer.parseInt(node.getAttributes().getNamedItem("order")
                        .getNodeValue());
            } catch (NullPointerException e) {
            }
            DataField df = null;
            try {
                String dfClassname = getClassForNode(fieldType);
                if (dfClassname != null) {
                    Class< ? > cl = Class.forName(dfClassname);
                    Constructor< ? > constructor = cl.getConstructor(Node.class, String.class,
                            String.class, String.class, boolean.class, String.class, int.class);
                    df = (DataField) constructor.newInstance(node, fieldName, fieldDesc, fieldRepr,
                            required, defaultValue, fieldOrder);
                }
            } catch (ClassNotFoundException e) {
                UIBuilderPlugin.log("Unable to load the field class", e);
            } catch (NoSuchMethodException e) {
                UIBuilderPlugin.log("Unable to find a suitable constructor for the field class", e);
            } catch (IOException e) {
                UIBuilderPlugin.log("IO Exception", e);
            } catch (Exception e) {
                UIBuilderPlugin.log(e.getClass().getSimpleName(), e);
            }
            return df;
        }
        return null;
    }

    /**
     * Builds a tree of <code>DataField</code>s from a <code>NodeList</code>.
     * 
     * @param nodeList
     * @return the complete tree of <code>DataField</code>s for the given <code>NodeList</code>
     * @throws MissingAttributeException if a required attribute is missing in the XML declaration
     */
    public static ArrayList<DataField> buildTree( NodeList nodeList )
            throws MissingAttributeException {
        return recursiveBuild(nodeList, null);
    }

    /**
     * Recursively build a tree of <code>DataField</code>s from a <code>NodeList</code>
     * 
     * @param nodeList a <code>NodeList</code> of nodes to build from
     * @param head the <code>DataField</code> for which we are building
     * @return a <code>ArrayList</code> of <code>DataField</code>s to use in the next iteration
     * @throws MissingAttributeException if a required attribute is missing in the XML declaration
     */
    private static ArrayList<DataField> recursiveBuild( NodeList nodeList, DataField head )
            throws MissingAttributeException {
        ArrayList<DataField> result = new ArrayList<DataField>();
        for( int i = 0; i < nodeList.getLength(); i++ ) {
            DataField df = getFieldFromNode(nodeList.item(i));
            if (df != null) {
                ArrayList<DataField> children = recursiveBuild(nodeList.item(i).getChildNodes(), df);
                df.setParent(head);
                df.setChildren(children);
                result.add(df);
            }
        }
        Collections.sort(result);
        return result;
    }
}