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
package eu.hydrologis.jgrass.console.core.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import eu.hydrologis.jgrass.console.core.AbstractJGrass;
import eu.hydrologis.jgrass.console.core.JGrass;
import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.compiler.CLiMLCompiler;
import eu.hydrologis.jgrass.libs.scripting.ScriptingLibsPlugin;
import eu.hydrologis.jgrass.libs.utils.NativeUtilities;

/**
 * <p>
 * This class <code>ConsoleEngine</code> implements the <code>JGrass</code> interface
 * specifications</i>; it is expected that any statement (command, model or the name of a script
 * file) is passed through the <code>dispatch</code> method of the console engine object, because
 * of, only the <code>ConsoleEngine</code> object provides the mechanism to execute or compile more
 * than one concurrently.
 * </p>
 * <p>
 * Simply stated, the engine delegate the process of translation and execution to a entrusted, so
 * called "Command Line interpreter / Model Language Compiler" - <i>CLiMLCompiler</i> or compiler
 * for short. For each statement passed through the dispatch method, the engine creates a project
 * space (<code>Projectspace</code>) object that holds, e.g., the declarations of native based
 * commands and Java based models, the directory of source script files and any other required
 * information needed by the compiler to perform a compiler/interpreter run in its own thread.
 * </p>
 * <p>
 * However, the compiler treats the commands and models as keywords which can be setup in separated
 * XML-files, additionally requires the information about input/output linkable components and for
 * an interpreter run also their full qualified names � the engine parses all XML-files of a
 * specified include directory for the desired information about commands and models.
 * </p>
 * 
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public class ConsoleEngine extends AbstractJGrass implements JGrass {

    // Construction
    /**
     * <p>
     * The constructor <code>ConsoleEngine</code> creates this <i>JGRASS console</i> object.
     * </p>
     */
    public ConsoleEngine() {

        super(null);
    } // ConsoleEngine

    /**
     * <p>
     * The copy constructor <code>ConsoleEngine</code> creates this <i>JGRASS console</i> object
     * with the specified program arguments.
     * </p>
     * 
     * @param args The parameter <code>args</code> holds the optional arguments in an array of
     *        <code>String</code> passed through the Java Virtual Machine command line. The value of
     *        the parameter <code>args</code> can be <code>null</code>.
     */
    public ConsoleEngine( String[] args ) {

        super(args);
    } // ConsoleEngine

    // Operations
    /*
     * (non-Javadoc)
     * @see eu.hydrologis.jgrass.console.core.JGrass#dispatch()
     */
    /**
     * <p>
     * The method <code>dispatch</code> invokes the internal non-visual, character-based -
     * <code>CLiMLCompiler</code> - so called "Command Line interpreter / Model Language Compiler"
     * with the specified project options to either compile or to compile/execute the specified
     * command line.
     * </p>
     * 
     * @param projectOptions - the project-options.
     * @param commandLine - the command enclosing optional pre-processor directives; typically the
     *        name of a script file or a native based command or either a simple or complex Java
     *        based model.
     * @see eu.hydrologis.jgrass.console.core.prefs.ProjectOptions
     * @return Reserved for future use.
     */
    public Object dispatch( ProjectOptions projectOptions, String commandLine ) {

        if (null == projectOptions)
            throw new IllegalArgumentException();

        synchronized (this) {

            Object retval = null;
            try {

                Boolean bAsyncMode = (Boolean) projectOptions.getOption(
                        ProjectOptions.CONSOLE_ASYNC_MODE, new Boolean(false));
                Integer nMaxAllowed = (Integer) projectOptions.getOption(
                        ProjectOptions.CONSOLE_THREAD_RESTRICTION, new Integer(1));

                commandLine = parseSessionParameters(commandLine, projectOptions);

                // Creating a new runnable Command Line Interpreter instance
                // because it is assumed that pre-compilation done by the
                // pre-processor and also the interpretation of the resulting
                // script is done synchronously.
                Runnable __ci = new CLiMLCompiler(projectOptions, commandLine);
                // Marshaling the command line string to the so far called
                // Command Line Interpreter.
                if (false == bAsyncMode) {
                    retval = new Thread(__ci);
                    ((Thread) retval).start();
                } else {

                    ThreadGroup __threadGroup = getThreadGroup();
                    if (null != __threadGroup) {

                        if (0 <= nMaxAllowed && __threadGroup.activeCount() > nMaxAllowed) {

                            retval = new Exception("Maximum thread count exeeds."); //$NON-NLS-1$
                        } else {

                            retval = new Thread(__threadGroup, __ci);
                            ((Thread) retval).start();
                        }
                    }
                }
            } catch (Exception e) {

                if (true == Projectspace.isDebugEnabled())
                    projectOptions.internal.println(commandLine);
                if (true == Projectspace.isErrorEnabled())
                    projectOptions.err.println(e);

                e.printStackTrace();
            }

            return retval;
        }
    } // dispatch

    /**
     * If the
     * 
     * @param retval
     * @throws IOException
     */
    @SuppressWarnings("nls")
    private String parseSessionParameters( String commandLine, ProjectOptions m_projectOptions )
            throws IOException {

        Reader retval = null;

        if (new File(commandLine).exists()) {
            retval = new InputStreamReader(new FileInputStream(commandLine));
        } else {
            retval = new StringReader(commandLine);
        }

        BufferedReader bR = new BufferedReader(retval);
        StringBuffer sB = new StringBuffer();
        List<String> params = new ArrayList<String>();

        String line = null;
        while( (line = bR.readLine()) != null ) {
            if (line.startsWith("#")) { //$NON-NLS-1$
                params.add(line.substring(1).trim());
            } else {
                sB.append(line).append("\n");
            }
        }

        String mapsetString = null;
        // now deal with parameters supplied inline
        // # STARTDATE:2008-12-01 00:00
        // # ENDDATE: 2009-01-01 00:00
        // # DELTAT: 15
        // # MAPSET: /home/moovida
        // # GISBASE: /home/moovida/grass/
        // # RT: /home/moovida/rt/
        // # DEBUG: false
        // # NATIVEDEBUG: false
        // # TRACE: true
        // # NATIVELIBS = /home/moovida/codeapisexes/fortran_intel/fc/9.0/lib/
        // # REMOTEDBURL = postgresql:host:port:database:user:passwd
        long timeDelta = 60l * 1000l;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$
        String remotedbUrl = null;
        for( String parameterString : params ) {
            String[] parameterSplit = parameterString.split("="); //$NON-NLS-1$

            // LANGUAGE
            if (parameterSplit[0].trim().equals("LANGUAGE")) {
                try {
                    String language = parameterSplit[1].trim();
                    m_projectOptions.setOption(ProjectOptions.CONSOLE_LANGUAGE, language);
                } catch (Exception e1) {
                    m_projectOptions.setOption(ProjectOptions.CONSOLE_LANGUAGE, "groovy");
                }
            }
            // REMOTEDBURL
            if (parameterSplit[0].trim().equals("REMOTEDBURL")) {
                try {
                    remotedbUrl = parameterSplit[1].trim();
                    m_projectOptions.setOption(ProjectOptions.CONSOLE_REMOTEDBURL, remotedbUrl);
                } catch (Exception e1) {
                    m_projectOptions.setOption(ProjectOptions.CONSOLE_REMOTEDBURL, null);
                }
            }
            // TIMESTEP
            if (parameterSplit[0].trim().equals("DELTAT")) {
                try {
                    String dt = parameterSplit[1].trim();
                    if (dt != null && dt.length() > 0) {
                        final long __delta = Long.parseLong(dt);
                        m_projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_DELTA,
                                (long) (__delta * timeDelta));
                    } else
                        m_projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_DELTA, null);
                } catch (Exception e1) {
                    m_projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_DELTA, null);
                }
            }
            // ENDING UP
            if (parameterSplit[0].trim().equals("ENDDATE")) {
                try {
                    String dateString = parameterSplit[1].trim();
                    if (dateString != null && dateString.length() > 0) {
                        Date date = dateFormatter.parse(dateString);
                        m_projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_ENDING_UP,
                                DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                        DateFormat.MEDIUM, Locale.getDefault()).format(date));
                    } else {
                        m_projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_ENDING_UP, null);
                    }
                } catch (Exception e1) {
                    m_projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_ENDING_UP, null);
                }
            }
            // START UP
            if (parameterSplit[0].trim().equals("STARTDATE")) {
                try {
                    String dateString = parameterSplit[1].trim();
                    if (dateString != null && dateString.length() > 0) {
                        Date date = dateFormatter.parse(dateString);
                        m_projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_START_UP,
                                DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                        DateFormat.MEDIUM, Locale.getDefault()).format(date));
                    } else {
                        m_projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_START_UP, null);
                    }
                } catch (Exception e1) {
                    m_projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_START_UP, null);
                }
            }
            // MAPSET
            if (parameterSplit[0].trim().equals("MAPSET")) {
                mapsetString = parameterSplit[1].trim();
                mapsetString = mapsetString.replaceAll("\\\\","\\\\\\\\");
                File f = new File(mapsetString);
                if (f.exists() && f.isDirectory()) {
                    m_projectOptions.setOption(ProjectOptions.COMMON_GRASS_MAPSET, mapsetString);
                }
            }
            // GISBASE
            if (parameterSplit[0].trim().equals("GISBASE")) {
                String gisbaseString = parameterSplit[1].trim();
                File f = new File(gisbaseString);
                if (f.exists() && f.isDirectory()) {
                    m_projectOptions.setOption(ProjectOptions.NATIVE_MODEL_GISBASE, gisbaseString);
                }
            }
            // DEBUG
            if (parameterSplit[0].trim().equals("DEBUG")) {
                try {
                    final boolean bol = Boolean.parseBoolean(parameterSplit[1].trim());
                    m_projectOptions.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_DEBUG, bol);
                } catch (Exception e1) {
                    m_projectOptions.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_DEBUG, false);
                }
            }
            // TRACE
            if (parameterSplit[0].trim().equals("TRACE")) {
                try {
                    final boolean bol = Boolean.parseBoolean(parameterSplit[1].trim());
                    m_projectOptions.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_TRACE, bol);
                } catch (Exception e1) {
                    m_projectOptions.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_TRACE, false);
                }
            }
            // NATIVEDEBUG
            if (parameterSplit[0].trim().equals("NATIVEDEBUG")) {
                try {
                    final boolean bol = Boolean.parseBoolean(parameterSplit[1].trim());
                    m_projectOptions.setOption(ProjectOptions.NATIVE_MODEL_DEBUG, bol);
                } catch (Exception e1) {
                    m_projectOptions.setOption(ProjectOptions.NATIVE_MODEL_DEBUG, false);
                }
            }
            // NATIVELIBS
            if (parameterSplit[0].trim().equals("NATIVELIBS")) {
                String nativelibsString = parameterSplit[1].trim();
                String[] nativeSplit = nativelibsString.split(":|;");
                // add the content to the native libs path for java and jna
                for( String path : nativeSplit ) {
                    NativeUtilities.addPathToJavaLibraryPath(path);
                    NativeUtilities.addPathToJna(path);
                    NativeUtilities.addPathToPlatformJna(path);
                }
                NativeUtilities.printPaths(System.out);
            }

        }
        if (mapsetString==null) {
            // this fix is currently needed in the case commands are executed from gui
            mapsetString = (String) m_projectOptions.getOption(ProjectOptions.COMMON_GRASS_MAPSET);
            mapsetString = mapsetString.replaceAll("\\\\","\\\\\\\\");
        }
        StringBuffer stringBuffer = new StringBuffer(sB);
        return stringBuffer.toString();
    }


} // ConsoleEngine
