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
package eu.hydrologis.jgrass.ui.console;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.refractions.udig.catalog.URLUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.hydrologis.jgrass.console.ConsolePlugin;
import eu.hydrologis.jgrass.console.core.JGrass;
import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class ConsoleCommandExecutor {
    public final static String OUTPUTTYPE_SYSOUT = "sysout"; //$NON-NLS-1$
    public final static String OUTPUTTYPE_BTCONSOLE = "btconsole"; //$NON-NLS-1$
    public final static String OUTPUTTYPE_SUPPLIED = "supplied"; //$NON-NLS-1$

    private boolean isProblem = false;
    private String rtPath = null;

    public ConsoleCommandExecutor() {
        URL rtUrl = Platform.getBundle(ConsolePlugin.PLUGIN_ID).getResource("rt"); //$NON-NLS-1$
        try {
            URL fileURL = FileLocator.toFileURL(rtUrl);
            File urlToFile = URLUtils.urlToFile(fileURL);
            rtPath = urlToFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            isProblem = true;
        }
    }
    /**
     * Executes a command through console just by passing it's command string
     *
     * @param command the command string
     * @param mapsetPath the mapset path, can't be null
     * @param applicationpath the application path in case of native commands
     * @param outputType the type of output/error stream to use (if null defaults to system.out)
     * @param out a {@link PrintStream} if custom is needed. May be null.
     * @param err a {@link PrintStream} if custom is needed. May be null.
     * 
     * @return the started {@link Thread}.
     */
    public Object execute( String title, String command, String mapsetPath, String applicationpath,
            String outputType, PrintStream out, PrintStream err ) {
        if (isProblem)
            return null;

        // set grass application path and mapset info if available, else try to guess it from the
        // xml
        if (mapsetPath == null || applicationpath == null) {
            String[] grassPathsFromXml = getGrassPathsFromXml();
            if (mapsetPath == null) {
                mapsetPath = grassPathsFromXml[1];
            }
            if (applicationpath == null) {
                applicationpath = grassPathsFromXml[0];
            }
        }

        if (mapsetPath == null)
            return null;

        /*
         * prepare to use the console environment
         */
        JGrass console = ConsolePlugin.console();
        if (null == console) {
            return null;
        }

        OutputStream internalStream = null;
        OutputStream outputStream = null;
        OutputStream errorStream = null;
        if (outputType == null || outputType.equals(OUTPUTTYPE_SYSOUT)) {
            internalStream = System.out;
            outputStream = System.out;
            errorStream = System.err;
        } else if (outputType.equals(OUTPUTTYPE_BTCONSOLE)) {
            BacktraceConsole textConsole = new BacktraceConsole();
            internalStream = textConsole.internal;
            outputStream = textConsole.out;
            errorStream = textConsole.err;
            textConsole.setName(title);
            IConsoleManager manager = org.eclipse.ui.console.ConsolePlugin.getDefault()
                    .getConsoleManager();
            manager.addConsoles(new IConsole[]{textConsole});
            manager.showConsoleView(textConsole);
        } else if (outputType.equals(OUTPUTTYPE_SUPPLIED) && out != null && err != null) {
            internalStream = out;
            outputStream = out;
            errorStream = err;
        } else {
            return null;
        }

        ProjectOptions projectOptions = new ProjectOptions(internalStream, outputStream,
                errorStream);
        // set grass database info
        projectOptions.setOption(ProjectOptions.COMMON_GRASS_MAPSET, mapsetPath);

        if (applicationpath != null) {
            String binPath = ProjectOptions.NATIVE_MODEL_GISBASE + File.separator
                    + JGrassConstants.GRASSBIN;
            String libPath = ProjectOptions.NATIVE_MODEL_GISBASE + File.separator
                    + JGrassConstants.GRASSLIB;
            projectOptions.setOption(ProjectOptions.NATIVE_MODEL_GISBASE, applicationpath);
            projectOptions.setOption(binPath, applicationpath + File.separator
                    + JGrassConstants.GRASSBIN);
            projectOptions.setOption(libPath, applicationpath + File.separator
                    + JGrassConstants.GRASSLIB);
            projectOptions.setOption(libPath, applicationpath + File.separator
                    + JGrassConstants.GRASSLIB);
            projectOptions.setOption(libPath, applicationpath + File.separator
                    + JGrassConstants.GRASSLIB);
        }

        projectOptions.setOption(ProjectOptions.CONSOLE_COMPILE_ONLY, new Boolean(false));
        projectOptions.setOption(ProjectOptions.CONSOLE_ASYNC_MODE, new Boolean(false));
        projectOptions.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_DEBUG, new Boolean(false));
        projectOptions.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_TRACE, new Boolean(false));

        projectOptions.setOption(ProjectOptions.CONSOLE_DIRECTORY_INCLUDE, new String[]{rtPath});

        /*
         * now execute finally
         */
        System.out.println("Executing command: " + command);
        Object thread = console.dispatch(projectOptions, command);
        return thread;
    }

    private String[] getGrassPathsFromXml() {
        String nativeXml = rtPath + File.separator + "nativegrass.xml";
        File xmlFile = new File(nativeXml);
        if (!xmlFile.exists()) {
            return null;
        }

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
        String[] gisbaseMapset = new String[2];
        gisbaseMapset[0] = docEle.getAttribute("gisbase");
        gisbaseMapset[1] = docEle.getAttribute("defaultmapset");
        return gisbaseMapset;
    }
}
