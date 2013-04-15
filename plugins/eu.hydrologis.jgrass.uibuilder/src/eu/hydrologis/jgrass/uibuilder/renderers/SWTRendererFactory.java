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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;

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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.hydrologis.jgrass.uibuilder.UIBuilderPlugin;
import eu.hydrologis.jgrass.uibuilder.fields.DataField;

/**
 * Implementation of a <code>Renderer</code> using SWT.
 * 
 * @author Patrick Ohnewein
 */
public class SWTRendererFactory implements RendererFactory {

    public Renderer createRenderer( DataField df ) {
        String dfClass = df.getClass().getSimpleName();
        Renderer r = null;
        try {
            Class< ? > cl = Class.forName(getClassForField(dfClass));
            Constructor< ? > constructor = cl.getConstructor(DataField.class);
            r = (Renderer) constructor.newInstance(df);
        } catch (ClassNotFoundException e) {
            UIBuilderPlugin.log("Unable to load the renderer class", e);
        } catch (NoSuchMethodException e) {
            UIBuilderPlugin.log("Unable to find a suitable constructor for the renderer class", e);
        } catch (IOException e) {
            UIBuilderPlugin.log("IO Exception", e);
        } catch (Exception e) {
            UIBuilderPlugin.log(e.getClass().getSimpleName(), e);
        }
        df.setUIRepresentation(r);
        return r;
    }

    /**
     * Associates field types to <code>Renderer</code> classes.
     * 
     * @param fieldType the type of the field
     * @return a string representing the <code>Renderer</code> class to load
     */
    private String getClassForField( String fieldType ) throws ParserConfigurationException,
            SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();

        String relURL = "conf/swtRenderers.xml";
        Bundle bundle = Platform.getBundle(UIBuilderPlugin.PLUGIN_ID);

        URL fileURL = FileLocator.toFileURL(bundle.getResource(relURL));
        File urlToFile = URLUtils.urlToFile(fileURL);
        String path = urlToFile.getAbsolutePath();

        String nodesPath = bundle == null ? relURL : path; //$NON-NLS-1$
        Document doc = builder.parse(nodesPath);
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression exp = xpath.compile("//field[@fieldClass='" + fieldType
                + "']/@rendererClass");
        Object result = exp.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        return nodes.item(0).getNodeValue();
    }
}
