///*
// * UIBuilder - a framework to build user interfaces out from XML files
// * Copyright (C) 2007-2008 Patrick Ohnewein
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package eu.hydrologis.jgrass.uibuilder.tests;
//
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//
//import eu.hydrologis.jgrass.uibuilder.fields.DataField;
//import eu.hydrologis.jgrass.uibuilder.fields.IntegerField;
//import eu.hydrologis.jgrass.uibuilder.fields.StringField;
//import eu.hydrologis.jgrass.uibuilder.parser.MissingAttributeException;
//import eu.hydrologis.jgrass.uibuilder.parser.XMLReader;
//
//import junit.framework.TestCase;
//
//public class XMLReaderTest extends TestCase {
//
//	public void testReadFromFile() throws SAXException, IOException, ParserConfigurationException {
//		NodeList testXMLNodes = XMLReader.readFromFile("src/eu/hydrologis/jgrass/guibuilder/tests/test.xml");
//		assert(testXMLNodes.getLength() == 5);
//		assert(testXMLNodes.item(1).getNodeName() == "field");
//	}
//
//	public void testBuildTree() throws SAXException, IOException, MissingAttributeException, ParserConfigurationException {
//		NodeList testXMLNodes = XMLReader.readFromFile("src/eu/hydrologis/jgrass/guibuilder/tests/test.xml");
//		ArrayList<DataField> testXMLFields = XMLReader.buildTree(testXMLNodes);
//		assert(testXMLFields.get(0) instanceof StringField);
//		assert(testXMLFields.get(0).getDescription() == "A string");
//		assert(testXMLFields.get(1) instanceof IntegerField);
//		assert(testXMLFields.get(1).getDescription() == "An integer");
//	}
//}
