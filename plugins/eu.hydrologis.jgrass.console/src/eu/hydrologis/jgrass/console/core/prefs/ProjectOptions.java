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
package eu.hydrologis.jgrass.console.core.prefs;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

/**
 * <p>The class <code>ProjectOptions</code> is a mapping of the current user
 * preferences respectively of the settings for a compiler/interpreter run.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public class ProjectOptions {

    // Attributes
    public final static String CONSOLE_REMOTEDBURL = "option@string@@__console_remotedburl"; //$NON-NLS-1$
    public final static String CONSOLE_LANGUAGE = "option@string@@__console_language"; //$NON-NLS-1$
    /**
     * <p>A <code>Boolean</code> option; if <code>true</code> the compilation
     * and execution of the statement in the command line runs in its own
     * thread, otherwise not.</p>
     * <p>Example:<br/><br/><code><pre>
     * projectOptions.setOption(
     * 	ProjectOptions.<i>CONSOLE_THREAD_RESTRICTION</i>
     * 	, <b>new</b> Boolean( false )
     * );
     * </pre></code></p>
     * @since 2.0.0
     * @see #CONSOLE_THREAD_RESTRICTION
     */
    public final static String CONSOLE_ASYNC_MODE = "option@bool@@__console_async_mode"; //$NON-NLS-1$

    /**
     * <p>A <code>Boolean</code> option; if <code>true</code> the statement in
     * the command is only being compiled, otherwise also executed.</p>
     * <p>Example:<br/><br/><code><pre>
     * projectOptions.setOption(
     * 	ProjectOptions.CONSOLE_COMPILE_ONLY
     * 	, <b>new</b> Boolean( false )
     * );
     * </pre></code></p>
     * @since 2.0.0
     */
    public final static String CONSOLE_COMPILE_ONLY = "option@bool@@__console_compile_only"; //$NON-NLS-1$

    /**
     * <p>A <code>Boolean</code> option; if <code>true</code> the debug
     * messages are displayed, otherwise not.</p>
     * <p>Example:<br/><br/><code><pre>
     * projectOptions.setOption(
     * 	ProjectOptions.CONSOLE_LOGGING_LEVEL_DEBUG
     * 	, <b>new</b> Boolean( true )
     * );
     * </pre></code></p>
     * @since 2.0.0
     */
    public final static String CONSOLE_LOGGING_LEVEL_DEBUG = "option@bool@@__console_logging_level_debug"; //$NON-NLS-1$

    /**
     * <p>A <code>Boolean</code> option; if <code>true</code> the trace
     * messages are displayed, otherwise not.</p>
     * <p>Example:<br/><br/><code><pre>
     * projectOptions.setOption(
     * 	ProjectOptions.CONSOLE_LOGGING_LEVEL_TRACE
     * 	, <b>new</b> Boolean( true )
     * );
     * </pre></code></p>
     * @since 2.0.0
     */
    public final static String CONSOLE_LOGGING_LEVEL_TRACE = "option@bool@@__console_logging_level_trace"; //$NON-NLS-1$

    /**
     * <p>A <code>String</code> or <code>String[]</code> option; the project
     * option specifies one or more include directories, files for the
     * initialization of the console engine.</p>
     * TODO
     * @since 2.0.0
     */
    public final static String CONSOLE_DIRECTORY_INCLUDE = "option@string[]@@__console_include"; //$NON-NLS-1$

    /**
     * <p>A <code>String</code> option; specifying the source directory of the
     * users script files.</p>
     * <p>Example:<br/><br/><code><pre>
     * projectOptions.setOption(
     * 	ProjectOptions.CONSOLE_DIRECTORY_SOURCE
     * 	, System.getProperty( "user.home" ) //$NON-NLS-1$
     * );
     * </code></pre></p>
     * @since 2.0.0
     */
    public final static String CONSOLE_DIRECTORY_SOURCE = "option@string@@__console_source"; //$NON-NLS-1$

    /**
     * <p>A <code>Integer</code> option; specifying the maximum count of
     * concurrent running compile/interprete threads; a value <code><= 0</code>
     * indicates no restriction.</p>
     * @since 2.0.0
     * @see #CONSOLE_ASYNC_MODE
     */
    public final static String CONSOLE_THREAD_RESTRICTION = "option@int@@__console_thread_restriction"; //$NON-NLS-1$

    /**
     * <p>A <code>String</code> option; the project option specifies the
     * mapset folder of the <strong>GRASS</strong> database.</p>
     * @since 2.0.0
     */
    public final static String COMMON_GRASS_MAPSET = "value@string@@__common_grass_mapset"; //$NON-NLS-1$

    /**
     * <p>A <code>Long</code> option; specifies the delta of a time-depended
     * simulation model.</p>
     * @since 2.0.0
     * @see #JAVA_MODEL_TIME_ENDING_UP
     * @see #JAVA_MODEL_TIME_START_UP 
     */
    public final static String JAVA_MODEL_TIME_DELTA = "value@long@@timedelta"; //$NON-NLS-1$

    /**
     * <p>A <code>Date</code> option; specifying the end of the time-depended
     * simulation model.</p>
     * @since 2.0.0
     * @see #JAVA_MODEL_TIME_DELTA
     * @see #JAVA_MODEL_TIME_START_UP
     */
    public final static String JAVA_MODEL_TIME_ENDING_UP = "value@datetime@@timeends"; //$NON-NLS-1$

    /**
     * <p>A <code>Date</code> option; specifying the start-point of the
     * time-depended simulation model.</p>
     * @since 2.0.0
     * @see #JAVA_MODEL_TIME_DELTA
     * @see #JAVA_MODEL_TIME_ENDING_UP
     */
    public final static String JAVA_MODEL_TIME_START_UP = "value@datetime@@timestart"; //$NON-NLS-1$

    /**
     * <p>A <code>Boolean</code> option; TODO the project option specify a "0" for
     * debug mode off and "1" for enabling debug mode.</p>
     * @since 2.0.0
     */
    public final static String NATIVE_MODEL_DEBUG = "option@boolean@@__native_debug"; //$NON-NLS-1$

    /**
     * <p>A <code>String</code> option; the project option specifies the path
     * to the installation folder of GRASS.</p>
     * @since 2.0.0
     */
    public final static String NATIVE_MODEL_GISBASE = "option@string@@__native_gisbase"; //$NON-NLS-1$

    /**
     * <p>A <code>String</code> value; the option specifies the home directory
     * of the current user.</p>
     * @since 2.0.0
     */
    public final static String NATIVE_MODEL_USER_HOME = "option@string@@__native_user_home"; //$NON-NLS-1$

    /**
     * <p>A <code>String</code> value; the option specifies the user name of
     * the current user.</p>
     * @since 2.0.0
     */
    public final static String NATIVE_MODEL_USER_NAME = "option@string@@__native_user_name"; //$NON-NLS-1$

    /**
     * <p>The projects caption, if any.</p>
     * @since 2.0.0
     */
    private String m_projectCaption;

    /** Map of a unique string identifier to a single object. */
    private HashMap<String, Object> m_projectOptions;

    /**
     * <p>The error output stream. This stream is already open and ready to
     * accept output data.</p><p> Typically this stream corresponds to display
     * output or another output destination specified by the host environment
     * or user. By convention, this output stream is used to display ERROR
     * messages or other information that should come to the immediate
     * attention of a user even if the principal output stream, the value of
     * the variable out, has been redirected to a file or other destination
     * that is typically not continuously monitored.</p>
     * @since 2.0.0
     */
    public final PrintStream err;

    /**
     * <p>The internal stream. This stream is already open and ready to accept
     * output data. Typically this stream corresponds to display output or
     * another output destination specified by the host environment or user.</p>
     * @since 2.0.0
     */
    public final PrintStream internal;

    /**
     * <p>The output stream. This stream is already open and ready to accept
     * output data. Typically this stream corresponds to display output or
     * another output destination specified by the host environment or user.</p> 
     * <p>For simple stand-alone Java applications, a typical way to write a
     * line of output data is:<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;
     * <code>out.println(data)</code><br/><br/><br/>See the
     * <code>println</code> methods in class <code>PrintStream</code>.</p>
     * @since 2.0.0
     * @see <code>PrintStream.println()</code>,
     * 		<code>PrintStream.println(boolean)</code>,
     * 		<code>PrintStream.println(char)</code>,
     * 		<code>PrintStream.println(char[])</code>,
     * 		<code>PrintStream.println(double)</code>,
     * 		<code>PrintStream.println(float)</code>,
     * 		<code>PrintStream.println(int)</code>,
     * 		<code>PrintStream.println(long)</code>,
     * 		<code>PrintStream.println(java.lang.Object)</code>,
     * 		<code>PrintStream.println(java.lang.String)</code>
     */
    public final PrintStream out;

    // Construction
    /**
     * <p>The constructor <code>ProjectOptions</code> creates a project options
     * object using the current standard output streams provided by the
     * <code>System</code> class to initialize its print streams.</p>
     */
    public ProjectOptions() {

        super();
        m_projectOptions = new HashMap<String, Object>();
        m_projectCaption = null;
        err = newPrintStream(null, System.err);
        internal = newPrintStream(null, System.out);
        out = newPrintStream(null, System.out);
    } // ProjectOptions

    /**
     * <p>The copy constructor <code>ProjectOptions</code> defines a project
     * options object with the specified internal, output and error stream.</p>
     * @param internalStream
     * 		- output stream, used as internal stream.
     * @param outStream
     * 		- output stream, used as output stream.
     * @param errorStream
     * 		- output stream, used as ERROR stream.
     */
    public ProjectOptions( OutputStream internalStream, OutputStream outStream,
            OutputStream errorStream ) {

        super();
        m_projectOptions = new HashMap<String, Object>();
        m_projectCaption = null;
        err = newPrintStream(errorStream, System.err);
        internal = newPrintStream(internalStream, System.out);
        out = newPrintStream(outStream, System.out);
    } // ProjectOptions

    // Operations
    /**
     * <p>Creates a new <code>PrintStream</code> object.</p>
     * @return
     * 		A new <code>PrintStream</code>.
     */
    private PrintStream newPrintStream( OutputStream arg0, OutputStream arg1 ) {

        return new PrintStream((null != arg0) ? arg0 : arg1, true);
    } // newPrintStream

    /**
     * <p>Returns the specified option, if any, otherwise <code>null</code>.</p>
     * @param option
     * 		- a option.
     */
    public Object getOption( String option ) {

        if (true == m_projectOptions.containsKey(option))
            return m_projectOptions.get(option);

        return null;
    } // getOption

    /**
     * <p>Returns the specified option, if any, otherwise returns the specified
     * default value.</p>
     * @param option
     * 		- a option.
     * @param defaultObject
     * 		- a default value.
     */
    public Object getOption( String option, Object defaultObject ) {

        Object retval = getOption(option);
        if (null == retval)
            retval = defaultObject;

        return retval;
    } // getOption

    /**
     * <p>Returns the project caption.</p>
     */
    public String projectCaption() {

        return m_projectCaption;
    } // projectCaption

    /**
     * <p>Sets the specified project caption.</p>
     * @param projectCaption
     * 		- a caption.
     */
    public void projectCaption( String projectCaption ) {

        m_projectCaption = projectCaption;
    } // projectCaption

    /**
     * <p>Adds the specified option and value to the options list, if the option
     * has not already set before or replaces the value in the options list with
     * the specified value.</p>
     * @param option
     * 		- a option.
     * @param value
     * 		- a value.
     */
    public void setOption( String option, Object value ) {

        if (true == m_projectOptions.containsKey(option))
            m_projectOptions.remove(option);

        m_projectOptions.put(option, value);
    } // setOption

} // ProjectOptions
