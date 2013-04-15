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
package eu.hydrologis.jgrass.uibuilder.jgrassdependent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import eu.hydrologis.jgrass.uibuilder.UIBuilder;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class XmlCreator {

    private Element rootElement;
    private Document doc;
    private int order = 0;

    /**
     * Create an xml structure for a JGrass command gui command
     * 
     * @param commandName the name of the command
     * @param commandDescription a short description for the command
     */
    public XmlCreator( String commandName, String commandDescription ) {
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            doc = builder.newDocument();
            rootElement = doc.createElement(UIBuilderJGrassConstants.COMMAND_TAG_NAME);
            rootElement.setAttribute(UIBuilderJGrassConstants.COMMAND_TAG_ATTRIBUTECMDNAME,
                    commandName);
            rootElement.setAttribute(UIBuilderJGrassConstants.COMMAND_TAG_ATTRIBUTECMDDESC,
                    commandDescription);
            doc.appendChild(rootElement);
        } catch (DOMException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the xml Command definition structure from a resource passed as a stream
     * 
     * @param xmlCommandDefinitionStream the input xml stream
     */
    public XmlCreator( InputStream xmlCommandDefinitionStream ) {
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            doc = builder.parse(xmlCommandDefinitionStream);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the xml structure for a gui that would exactly ask for the attributes of a feature.
     * 
     * @param feature the feature for which to create the xml structure
     */
    public void createFeatureGui( SimpleFeature feature ) {
        SimpleFeatureType featureType = feature.getFeatureType();
        List<AttributeType> attributeTypes = featureType.getTypes();
        for( int i = 1; i < attributeTypes.size(); i++ ) {
            // TODO test the following, not sure if that is the way to hanlde attributes
            addLabeledStringItem(attributeTypes.get(i).getName().toString(), attributeTypes.get(i)
                    .getName().toString(), attributeTypes.get(i).getName().toString(), false);
        }
    }
    /**
     * @return the nodelist as needed by the {@link UIBuilder}
     */
    public NodeList getNodeList() {
        // return doc.getElementsByTagName(UIBuilderJGrassConstants.COMMAND_TAG_NAME);
        return doc.getChildNodes();
    }

    /**
     * Dump the created xml tree to a formatted xml file
     * 
     * @param outputXmlFile the file to which to dump to, if null, dump goes to the standard output
     * @throws IOException
     */
    public void dumpXmlToFile( String outputXmlFile ) throws IOException {
        // write back to file
        // print
        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        // to generate a file output use fileoutputstream instead of system.out
        OutputStream outputStream = null;
        if (outputXmlFile == null) {
            outputStream = System.out;
        } else {
            outputStream = new FileOutputStream(outputXmlFile);
        }
        XMLSerializer serializer = new XMLSerializer(outputStream, format);
        serializer.serialize(doc);
    }

    /**
     * Add a node that permits to choose an existing <b>raster map</b>
     * 
     * @param name name of the field
     * @param description description of the field
     * @param repr representation in the output command line string representation
     * @param required if the value is optional or not
     */
    public void addLabeledMapItem( String name, String description, String repr, boolean required ) {
        Element mapElement = doc.createElement(UIBuilderJGrassConstants.FIELD_NAME);
        mapElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_TYPE,
                UIBuilderJGrassConstants.FIELD_TYPE_STRING);
        mapElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_NAME, name);
        mapElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_DESCR, description);
        mapElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_ORDER, String
                .valueOf(order++));
        mapElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REQUIRED, String
                .valueOf(required));
        mapElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REPR, repr);
        rootElement.appendChild(mapElement);
    }

    /**
     * Add a node that holds a label and a textfield for <b>strings</b>
     * 
     * @param name name of the field
     * @param description description of the field
     * @param repr representation in the output command line string representation
     * @param required if the value is optional or not
     */
    public void addLabeledStringItem( String name, String description, String repr, boolean required ) {
        Element stringElement = doc.createElement(UIBuilderJGrassConstants.FIELD_NAME);
        stringElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_TYPE,
                UIBuilderJGrassConstants.FIELD_TYPE_STRING);
        stringElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_NAME, name);
        stringElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_DESCR, description);
        stringElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_ORDER, String
                .valueOf(order++));
        stringElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REQUIRED, String
                .valueOf(required));
        stringElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REPR, repr);
        rootElement.appendChild(stringElement);
    }

    /**
     * Add a node that holds a label and a textfield for <b>integers</b>
     * 
     * @param name name of the field
     * @param description description of the field
     * @param repr representation in the output command line string representation
     * @param required if the value is optional or not
     */
    public void addLabeledIntItem( String name, String description, String repr, boolean required ) {
        Element intElement = doc.createElement(UIBuilderJGrassConstants.FIELD_NAME);
        intElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_TYPE,
                UIBuilderJGrassConstants.FIELD_TYPE_INT);
        intElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_NAME, name);
        intElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_DESCR, description);
        intElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_ORDER, String
                .valueOf(order++));
        intElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REQUIRED, String
                .valueOf(required));
        intElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REPR, repr);
        rootElement.appendChild(intElement);
    }

    /**
     * Add a node that holds a label and a textfield for <b>doubles</b>
     * 
     * @param name name of the field
     * @param description description of the field
     * @param repr representation in the output command line string representation
     * @param required if the value is optional or not
     */
    public void addLabeledDoubleItem( String name, String description, String repr, boolean required ) {
        Element doubleElement = doc.createElement(UIBuilderJGrassConstants.FIELD_NAME);
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_TYPE,
                UIBuilderJGrassConstants.FIELD_TYPE_DOUBLE);
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_NAME, name);
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_DESCR, description);
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_ORDER, String
                .valueOf(order++));
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REQUIRED, String
                .valueOf(required));
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REPR, repr);
        rootElement.appendChild(doubleElement);
    }

    /**
     * Add a node that permits an option
     * 
     * @param name name of the field
     * @param description description of the field
     * @param repr representation in the output command line string representation
     * @param required if the value is optional or not
     */
    public void addCheckBoxItem( String name, String description, String repr, boolean isSelected ) {
        Element doubleElement = doc.createElement(UIBuilderJGrassConstants.FIELD_NAME);
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_TYPE,
                UIBuilderJGrassConstants.FIELD_TYPE_CHECKBOX);
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_NAME, name);
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_DESCR, description);
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_ORDER, String
                .valueOf(order++));
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_DEFAULT, String
                .valueOf(isSelected));
        doubleElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REPR, repr);
        rootElement.appendChild(doubleElement);
    }

    /**
     * Add a node that holds a label and a <b>filechooser</b>
     * 
     * @param name name of the field
     * @param description description of the field
     * @param repr representation in the output command line string representation
     * @param extentionToFilter the extention to be filtered, ex: *.shp
     * @param required if the value is optional or not
     */
    public void addFileChooserItem( String name, String description, String repr,
            String extentionToFilter, boolean required ) {
        Element fileChooserElement = doc.createElement(UIBuilderJGrassConstants.FIELD_NAME);
        fileChooserElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_TYPE,
                UIBuilderJGrassConstants.FIELD_TYPE_FILE);
        fileChooserElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_NAME, name + ":" //$NON-NLS-1$
                + extentionToFilter);
        fileChooserElement
                .setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_DESCR, description);
        fileChooserElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_ORDER, String
                .valueOf(order++));
        fileChooserElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REQUIRED, String
                .valueOf(required));
        fileChooserElement.setAttribute(UIBuilderJGrassConstants.FIELD_ATTRIBUTE_REPR, repr);
        rootElement.appendChild(fileChooserElement);
    }
}
