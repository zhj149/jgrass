/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) C.U.D.A.M. Universita' di Trento
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
package eu.hydrologis.jgrass.console.editor;

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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.hydrologis.jgrass.console.ConsolePlugin;
import eu.hydrologis.jgrass.ui.console.ConsoleUIPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class ConsoleEditorPlugin extends AbstractUIPlugin {

    // Attributes
    /** The shared editor instance. */
    private static ConsoleEditorPlugin m_plugin;

    /** The unique <code>JGrass</code> console object. */
    /*
     * private final static JGrass m_console = ( JGrass )new ConsoleEngine();
     */

    /** The plug-in ID of the console editor. */
    public final static String PLUGIN_ID = "eu.hydrologis.jgrass.console.editor"; //$NON-NLS-1$

    // Construction
    /**
     * The constructor
     */
    public ConsoleEditorPlugin() {

    } // ConsoleEditorPlugin

    // Operations
    /**
     * Returns the shared editor instance, if any.
     * 
     * @return The shared editor instance, if any, otherwise <code>null</code>.
     */
    public static ConsoleEditorPlugin getDefault() {

        return m_plugin;
    } // getDefault

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */

    public void start( BundleContext context ) throws Exception {

        super.start(context);
        m_plugin = this;
    } // start

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */

    public void stop( BundleContext context ) throws Exception {

        m_plugin = null;
        super.stop(context);
    } // stop

    /**
     * Logs the Throwable in the plugin's log.
     * <p>
     * This will be a user visable ERROR iff:
     * <ul>
     * <li>t is an Exception we are assuming it is human readable or if a
     * message is provided
     */
    public static void log( String message2, Throwable t ) {
        if (getDefault() == null) {
            t.printStackTrace();
            return;
        }
        String message = message2;
        if (message == null)
            message = ""; //$NON-NLS-1$
        int status = t instanceof Exception || message != null ? IStatus.ERROR : IStatus.WARNING;
        getDefault().getLog().log(new Status(status, PLUGIN_ID, IStatus.OK, message, t));
    }

    /**
     * Update teh xml file for GRASS commands
     * 
     * @param mapset
     *            the default mapset to use
     * @param gisbase
     *            the gisbase to set - cannot be null
     */
    public static synchronized void updateNativeGrassXml( String mapsetPath, String gisbase ) {
        if (gisbase == null)
            return;
        try {
            URL rtUrl = Platform.getBundle(ConsolePlugin.PLUGIN_ID).getResource("rt"); //$NON-NLS-1$
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
                    ConsoleUIPlugin
                            .log(
                                    "ConsoleUIPlugin problem: eu.hydrologis.jgrass.ui.console#ConsoleCommandExecutor#getApplicationPathFromXml", e); //$NON-NLS-1$
                    e.printStackTrace();
                }

                Element docEle = xmlDocument.getDocumentElement();

                if (mapsetPath != null) {
                    docEle.setAttribute("defaultmapset", mapsetPath);
                }
                // set gisbase
                docEle.setAttribute("gisbase", gisbase);
                // scan for all the executables in the gisbase/bin
                File gisbaseFile = new File(gisbase + File.separator + "bin");
                String[] nativeExes = gisbaseFile.list();

                if (nativeExes == null) {
                    Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                    MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
                    msgBox
                            .setMessage("Wrong GRASS native commands path supplied. Please supply the right path in the Console preferences.");
                    msgBox.open();
                    return;
                }
                boolean hasGregion = false;
                for( String nat : nativeExes ) {
                    if (nat.startsWith("g.region")) {
                        hasGregion = true;
                    }
                }
                if (!hasGregion) {
                    Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                    MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
                    msgBox
                            .setMessage("Wrong GRASS native commands path supplied. Please supply the right path in the Console preferences.");
                    msgBox.open();
                    return;
                }

                // remove old nodes
                NodeList childNodes = docEle.getElementsByTagName("command"); //$NON-NLS-1$
                List<Node> toRemove = new ArrayList<Node>();
                for( int i = 0; i < childNodes.getLength(); i++ ) {
                    Node item = childNodes.item(i);
                    toRemove.add(item);
                }
                for( Node node : toRemove ) {
                    docEle.removeChild(node);
                }
                // add new nodes
                for( String exe : nativeExes ) {
                    Element element = xmlDocument.createElement("command"); //$NON-NLS-1$
                    element.setAttribute("name", exe);
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

} // ConsoleEditorPlugin
