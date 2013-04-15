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
package eu.hydrologis.libs.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.refractions.udig.catalog.URLUtils;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This creates all needed files for the commands and guis from their extention points.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
@SuppressWarnings("nls")
public class CommandsCreator implements IStartup {

    /**
     * 
     */
    private static final String EXE = ".exe";
    private static final String COMMAND = "command";
    private static final String COMMANDNAME = "commandname";
    private static final String EXCHANGEITEMS = "exchangeitems";
    private static final String GUIXML = "guixml";
    private static final String OUTPUTITEMS = "outputitems";
    private static final String INPUTITEMS = "inputitems";
    private static final String NAME = "name";
    private static final String ICON = "icon";
    private static final String CLASS = "class";
    private static final String IMPORT = "import";

    private List<ModelEntry> modelsList;
    private List<String> modelsNameList;
    private ArrayList<String> n_modelsNameList;
    private ArrayList<ModelEntry> n_modelsList;

    public void earlyStartup() {
        IExtensionRegistry reg = Platform.getExtensionRegistry();

        /*
         * JGrass commands
         */
        IConfigurationElement[] extensions = reg
                .getConfigurationElementsFor("eu.hydrologis.jgrass.models.openmimodel");

        modelsList = new ArrayList<ModelEntry>();
        modelsNameList = new ArrayList<String>();
        for( int i = 0; i < extensions.length; i++ ) {
            IConfigurationElement element = extensions[i];
            if (!element.getName().equals("model")) {
                continue;
            }
            String classStr = element.getAttribute(CLASS);
            String iconStr = element.getAttribute(ICON);
            String nameStr = element.getAttribute(NAME);
            String inputItemsStr = element.getAttribute(INPUTITEMS);
            String outputItemsStr = element.getAttribute(OUTPUTITEMS);
            String guiXmlStr = element.getAttribute(GUIXML);

            ModelEntry tmpEntry = new ModelEntry();
            tmpEntry.classStr = classStr;
            tmpEntry.iconStr = iconStr;
            tmpEntry.nameStr = nameStr;
            tmpEntry.inputItemsStr = inputItemsStr;
            tmpEntry.outputItemsStr = outputItemsStr;
            tmpEntry.guiXmlStr = guiXmlStr;
            modelsList.add(tmpEntry);
            modelsNameList.add(nameStr.trim());
        }
        updateJGrassCommands();
        /*
         * native GRASS commands
         */
        IConfigurationElement[] n_extensions = reg
                .getConfigurationElementsFor("eu.hydrologis.jgrass.models.nativemodel");

        n_modelsNameList = new ArrayList<String>();
        n_modelsList = new ArrayList<ModelEntry>();
        for( int i = 0; i < n_extensions.length; i++ ) {
            IConfigurationElement element = n_extensions[i];
            if (!element.getName().equals("nativemodel")) {
                continue;
            }
            String nameStr = element.getAttribute(COMMANDNAME);
            String guiXmlStr = element.getAttribute(GUIXML);

            ModelEntry tmpEntry = new ModelEntry();
            tmpEntry.nameStr = nameStr;
            tmpEntry.guiXmlStr = guiXmlStr;
            n_modelsList.add(tmpEntry);
            n_modelsNameList.add(nameStr.trim());
        }
        updateGrassCommands();

    }

    private void updateJGrassCommands() {
        try {
            URL rtUrl = Platform.getBundle("eu.hydrologis.jgrass.console").getResource("rt"); //$NON-NLS-1$
            String rtPath = null;
            URL fileURL = FileLocator.toFileURL(rtUrl);
            File urlToFile = URLUtils.urlToFile(fileURL);
            rtPath = urlToFile.getAbsolutePath();

            if (rtPath != null) {
                String nativeXml = rtPath + File.separator + "standard_linkables.xml";
                File xmlFile = new File(nativeXml);

                DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
                DocumentBuilder p;
                Document xmlDocument = null;
                try {
                    p = f.newDocumentBuilder();
                    xmlDocument = p.parse(xmlFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Element docEle = xmlDocument.getDocumentElement();

                /*
                 * remove old nodes, if they need to be overwritten by the new ones defined in the
                 * extention points. Existing definitions that are not in extention points, will be
                 * left.
                 */
                NodeList childNodes = docEle.getElementsByTagName(IMPORT); //$NON-NLS-1$
                List<Node> toRemove = new ArrayList<Node>();
                for( int i = 0; i < childNodes.getLength(); i++ ) {
                    Node item = childNodes.item(i);
                    NamedNodeMap attributes = item.getAttributes();
                    String name = attributes.getNamedItem(NAME).getNodeValue();
                    if (modelsNameList.contains(name.trim())) {
                        toRemove.add(item);
                    }
                }
                for( Node node : toRemove ) {
                    docEle.removeChild(node);
                }
                // add new nodes
                for( ModelEntry modelEntry : modelsList ) {
                    Element element = xmlDocument.createElement(IMPORT); //$NON-NLS-1$
                    element.setAttribute(NAME, modelEntry.nameStr);
                    element.setAttribute(CLASS, modelEntry.classStr);
                    String exchangeStr = null;
                    if (modelEntry.inputItemsStr != null) {
                        exchangeStr = modelEntry.inputItemsStr;
                    }
                    if (modelEntry.outputItemsStr != null) {
                        if (exchangeStr == null) {
                            exchangeStr = modelEntry.outputItemsStr;
                        } else {
                            exchangeStr = exchangeStr + "," + modelEntry.outputItemsStr;
                        }
                    }

                    if (exchangeStr != null) {
                        element.setAttribute(EXCHANGEITEMS, exchangeStr);
                    }
                    docEle.appendChild(element);
                }

                // write back to file
                // print
                OutputFormat format = new OutputFormat(xmlDocument);
                format.setIndenting(true);
                // to generate a file output use fileoutputstream instead of
                // system.out
                XMLSerializer serializer = new XMLSerializer(new FileOutputStream(xmlFile), format);
                serializer.serialize(xmlDocument);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateGrassCommands() {
        try {
            URL rtUrl = Platform.getBundle("eu.hydrologis.jgrass.console").getResource("rt"); //$NON-NLS-1$
            String rtPath = null;
            URL fileURL = FileLocator.toFileURL(rtUrl);
            File urlToFile = URLUtils.urlToFile(fileURL);
            rtPath = urlToFile.getAbsolutePath();

            if (rtPath != null) {
                String nativeXml = rtPath + File.separator + "nativegrass.xml";
                File xmlFile = new File(nativeXml);

                DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
                DocumentBuilder p;
                Document xmlDocument = null;
                try {
                    p = f.newDocumentBuilder();
                    xmlDocument = p.parse(xmlFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Element docEle = xmlDocument.getDocumentElement();

                /*
                 * remove old nodes, if they need to be overwritten by the new ones defined in the
                 * extention points. Existing definitions that are not in extention points, will be
                 * left.
                 */
                NodeList childNodes = docEle.getElementsByTagName(COMMAND); //$NON-NLS-1$
                List<Node> toRemove = new ArrayList<Node>();
                for( int i = 0; i < childNodes.getLength(); i++ ) {
                    Node item = childNodes.item(i);
                    NamedNodeMap attributes = item.getAttributes();
                    String name = attributes.getNamedItem(NAME).getNodeValue();
                    /*
                     * the commands could end with .exe on windows systems or on test environments,
                     * so trim that away
                     */
                    if (name.endsWith(EXE)) {
                        int lastExe = name.lastIndexOf(EXE);
                        name = name.substring(0, lastExe);
                    }

                    if (n_modelsNameList.contains(name.trim())) {
                        toRemove.add(item);
                    }
                }
                for( Node node : toRemove ) {
                    docEle.removeChild(node);
                }
                // add new nodes
                for( ModelEntry modelEntry : n_modelsList ) {
                    Element element = xmlDocument.createElement(COMMAND); //$NON-NLS-1$

                    /*
                     * here comes the fun: the grass build for windows is made of *.exe modules,
                     * therefore we need to add them to the names
                     */
                    String name = modelEntry.nameStr;
                    if (Platform.getOS().equals(Platform.OS_WIN32)) {
                        name = name + EXE;
                    }

                    element.setAttribute(NAME, name);
                    docEle.appendChild(element);
                }

                // write back to file
                // print
                OutputFormat format = new OutputFormat(xmlDocument);
                format.setIndenting(true);
                // to generate a file output use fileoutputstream instead of
                // system.out
                XMLSerializer serializer = new XMLSerializer(new FileOutputStream(xmlFile), format);
                serializer.serialize(xmlDocument);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ModelEntry {
        public String classStr = null;
        public String iconStr = null;
        public String nameStr = null;
        public String inputItemsStr = null;
        public String outputItemsStr = null;
        public String guiXmlStr = null;
    }

}
