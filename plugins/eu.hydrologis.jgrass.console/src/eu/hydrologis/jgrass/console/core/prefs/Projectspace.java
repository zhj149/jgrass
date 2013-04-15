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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.refractions.udig.catalog.URLUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.hydrologis.jgrass.console.ConsolePlugin;
import eu.hydrologis.jgrass.libs.scripting.VariablesAndCommands;
import eu.hydrologis.jgrass.libs.utils.xml.Attribute;
import eu.hydrologis.jgrass.libs.utils.xml.Tag;

/**
 * <p>
 * The class <code>Projectspace</code> specifies a summary of the user settings and the required
 * informations by a compiler/interpreter run.
 * </p>
 * 
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public class Projectspace {

    // typedefs
    /**
     * <p>
     * The class <code>JavaExchangeWordDescriptor</code> describes a Java based input/output
     * exchange.
     * </p>
     * 
     * @since 2.0.0
     * @version 1.0.0.1
     * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
     */
    public class JavaExchangeWordDescriptor {

        // Attributes
        /** The full qualified name of the class. */
        private final String m_fullQualifiedName;

        /** The class name of this linkable component. */
        private final String m_identifier;

        // Construction
        /**
         * <p>
         * Constructs this word descriptor with the specified identifier and the full qualified
         * name.
         * </p>
         * 
         * @param identifier - the class name of the linkable component.
         * @param fullQualifiedName - the full qullified name of the class.
         */
        public JavaExchangeWordDescriptor( String identifier, String fullQualifiedName ) {

            m_fullQualifiedName = fullQualifiedName;
            m_identifier = identifier;
        } // NativeModelWordDescriptor

        // Operations
        /** Returns the full qualified name of the class. */
        public String fullQualifiedName() {

            return m_fullQualifiedName;
        } // fullQualifiedName

        /** Returns the class name. */
        public String identifier() {

            return m_identifier;
        } // identifier

    } // JavaExchangeWordDescriptor

    /**
     * <p>
     * The class <code>JavaModelWordDescriptor</code> describes a Java based model.
     * </p>
     * 
     * @since 2.0.0
     * @version 1.0.0.1
     * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
     */
    public class JavaModelWordDescriptor {

        // Attributes
        /** The keyword of this model. */
        private final String m_identifier;

        /** The full qualified name of the class. */
        private final String m_fullQualifiedName;

        /** The default key, if any, otherwise <code>null</code>. */
        private final String m_defaultKey;

        /** The exchange items of this model. */
        private final String m_quantities;

        // Construction
        /**
         * <p>
         * Constructs this word descriptor with the specified identifier, the specified full
         * qualified name, the specified defaultKey and the specified exchange items.
         * </p>
         * 
         * @param identifier - the class name of the model.
         * @param fullQualifiedName - the full qullified name of the class.
         * @param defaultKey - can be either a default key or <code>null</code>.
         * @param quantities - a literl string with exchange items definitions.
         */
        public JavaModelWordDescriptor( String identifier, String fullQualifiedName,
                String defaultKey, String quantities ) {

            m_defaultKey = defaultKey;
            m_fullQualifiedName = fullQualifiedName;
            m_identifier = identifier;
            m_quantities = quantities;
        } // JavaModelWordDescriptor

        // Operations
        /**
         * <p>
         * Returns the keyword of this model.
         * </p>
         */
        public String identifier() {

            return m_identifier;
        } // identifier

        /**
         * <p>
         * Returns the default key, if any, otherwise <code>null</code>.
         * </p>
         */
        public String defaultKey() {

            return m_defaultKey;
        } // defaultKey

        /**
         * <p>
         * Returns the the full qualified name of the class.
         * </p>
         */
        public String fullQualifiedName() {

            return m_fullQualifiedName;
        } // fullQualifiedName

        /**
         * <p>
         * Returns the exchange items of this model.
         * </p>
         */
        public String quantities() {

            return m_quantities;
        } // quantities

    } // JavaModelWordDescriptor

    /**
     * <p>
     * The class <code>NativeModelWordDescriptor</code> describes a native based (JNI) command
     * respectively model.
     * </p>
     * 
     * @since 2.0.0
     * @version 1.0.0.1
     * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
     */
    public class NativeModelWordDescriptor {

        // Attributes
        /** The executeable file name of this native command. */
        private final String m_executeable;

        /** The keyword of this native command. */
        private final String m_identifier;

        // Construction
        /**
         * <p>
         * Constructs this word descriptor with the specified identifier.
         * </p>
         * 
         * @param identifier - the commands identifier.
         */
        public NativeModelWordDescriptor( String identifier ) {

            if (null != identifier) {

                m_executeable = identifier.replace('.', '_');
                m_identifier = identifier;
            } else {

                m_executeable = null;
                m_identifier = null;
            }
        } // NativeModelWordDescriptor

        // Operations
        /**
         * <p>
         * Returns the executeable file name of this native command.
         * </p>
         */
        public String executable() {

            return m_executeable;
        } // fullQualifiedName

        /**
         * <p>
         * Returns the keyword of this native command.
         * </p>
         */
        public String identifier() {

            return m_identifier;
        } // identifier

    } // NativeModelWordDescriptor

    // Attributes
    /** Bitmask for enabled error logging level, reports error messages. */
    private final static int LOGGING_LEVEL_ERROR = 0x0000;

    /** Bitmask for enabled debug logging level, reports debug messages. */
    private final static int LOGGING_LEVEL_DEBUG = 0x0010;

    /** Bitmask for enabled trace logging level, reports trace messages. */
    private final static int LOGGING_LEVEL_TRACE = 0x0011;

    /** The format string for the binary path. */
    private final static String MSG_FMT_ARGV_2_PATH_VARIABLE = "{1}{0}{2}{0}"; //$NON-NLS-1$

    /** The JGRASS/GRASS resource language bundle. */
    private final static String RESOURCE_ML_JAVA4_01 = "eu.hydrologis.jgrass.console.core.resources.hgisml_2-0-0_java4"; //$NON-NLS-1$

    /**
     * A non-termination character a assignment operation -- used to enumerate a default-key.
     */
    private final static String N_ASSIGN_OPERATOR = "="; //$NON-NLS-1$

    /** A termination character to seperate assignment operation expressions. */
    private final static String T_COMMA = ","; //$NON-NLS-1$

    /** XML attribute for a full qualified name. */
    private final static String XML_ATTR_CLASS = "class"; //$NON-NLS-1$

    /** XML tag representing a list of native commands. */
    private final static String XML_ATTR_DEFAULT_MAPSET = "defaultmapset"; //$NON-NLS-1$

    /** XML attribute for a default key. */
    private final static String XML_ATTR_DEFAULT_KEY = "defaultkey"; //$NON-NLS-1$

    /** XML attribute for the definition of exchange items. */
    private final static String XML_ATTR_EXCHANGE_ITEM = "exchangeitems"; //$NON-NLS-1$

    /** XML tag representing a list of native commands. */
    private final static String XML_ATTR_GISBASE = "gisbase"; //$NON-NLS-1$

    /** XML attribute for a native command or a java model identifier. */
    private final static String XML_ATTR_NAME = "name"; //$NON-NLS-1$

    /** XML tag representing a native command definition. */
    private final static String XML_TAG_GRASS_CMD = "command"; //$NON-NLS-1$

    /** XML tag representing a list of native commands. */
    private final static String XML_TAG_GRASS_ENV = "grassenvironment"; //$NON-NLS-1$

    /** XML tag representing a model definition. */
    private final static String XML_TAG_JGRASS_IMPORT = "import"; //$NON-NLS-1$

    /** The set of annotated keywords. */
    private Vector<String> m_annotationKeywords;

    /** The map of "ARG_VERB...". */
    private HashMap<String, String> m_argv_literals;

    /** The dictionary of linkable component. */
    private Vector<JavaExchangeWordDescriptor> m_dictionaryJavaExchangeWords;

    /** The dictionary of models. */
    private Vector<JavaModelWordDescriptor> m_dictionaryJavaModelWords;

    /** The dictionary of native commands. */
    private Vector<NativeModelWordDescriptor> m_dictionaryNativeModelWords;

    /** The list of classes to import. */
    private Vector<String> m_importedClasses;

    /** The set of imported linkable component keywords. */
    private Vector<String> m_importedExchangeKeywords;

    /** The set of imported model keywords. */
    private Vector<String> m_importedJavaModelKeywords;

    /** The set of imported native command keywords. */
    private Vector<String> m_importedNativeModelKeywords;

    /** The set of imported exchange item keywords. */
    private Vector<String> m_importedQuantityKeywords;

    /** The <i>JGRASS</i>, <i>GRASS</i> language defintions. */
    private ResourceBundle m_languageBundle;

    /** The logging level for this compiler/interpreter run. */
    private static int m_loggingLevel;

    /** The set of pre-processor keywords. */
    private Vector<String> m_preprocessorKeywords;

    /**
     * <p>
     * The projects caption, if any.
     * </p>
     * 
     * @since 2.0.0
     */
    private String m_projectCaption;

    /** The set of reserved constants. */
    private Vector<String> m_reservedConstants;

    /** The set of reserved keywords. */
    private Vector<String> m_reservedKeywords;

    /** The set of reserved types. */
    private Vector<String> m_reservedTypes;

    /**
     * A <code>String</code> value; specifies the path to the <strong>GRASS</strong> database
     * folder.
     * </p>
     */
    public final static String ARG_VERB_GRASSDB = "GRASSDB"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the path to the <strong>GRASS</strong> database
     * folder.
     * </p>
     */
    public final static String ARG_VERB_LOCATION = "LOCATION"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the path to the <strong>GRASS</strong> database
     * folder.
     * </p>
     */
    public final static String ARG_VERB_LOCATION_NAME = "LOCATION_NAME"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the mapset folder of the <strong>GRASS</strong>
     * database.
     * </p>
     */
    public final static String ARG_VERB_MAPSET = "MAPSET"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the path to the biniaries folder of the native GRASS
     * installation.
     */
    public final static String ARG_VERB_BINARY_PATH = "BINARY_PATH"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the path to the msys shell executable of the native GRASS
     * installation.
     */
    public final static String ARG_VERB_GRASS_SH = "GRASS_SH";

    /**
     * A <code>String</code> value; specify a "0" for debug mode off and "1" for debug mode.
     */
    public final static String ARG_VERB_DEBUG = "DEBUG"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the path to the library folder for the Mac OS-X
     * operating system of the native GRASS installation.
     */
    public final static String ARG_VERB_DYLD_LIBRARY_PATH = "DYLD_LIBRARY_PATH"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the path to the library folder of the native GRASS
     * installation
     */
    public final static String ARG_VERB_LD_LIBRARY_PATH = "LD_LIBRARY_PATH"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the path to the installation folder of GRASS.
     */
    public final static String ARG_VERB_GISBASE = "GISBASE"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the path to the <strong>GRASS</strong> database
     * folder.
     */
    public final static String ARG_VERB_GISDBASE = "GISDBASE"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies a temporally file name, which is then used by the
     * native command.</i>
     */
    public final static String ARG_VERB_GISRC = "GISRC"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the path to the library folder of the native GRASS
     * installation.
     */
    public final static String ARG_VERB_GRASS_LD_LIBRARY_PATH = "GRASS_LD_LIBRARY_PATH"; //$NON-NLS-1$ 

    /**
     * A <code>String</code> value; specifies the GUI argument for the native command.
     */
    public final static String ARG_VERB_GRASS_GUI = "GRASS_GUI"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the monitor argument for the native command.
     */
    public final static String ARG_VERB_MONITOR = "MONITOR"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifies the system environment varible for a native command.
     */
    public final static String ARG_VERB_PATH = Projectspace.__initializeSystemProperty("path"); //$NON-NLS-1$

    /**
     * A <code>float</code> value; specifies the delta of a time-dependend simulation model.
     */
    public final static String ARG_VERB_TIME_DELTA = "TIME_DELTA"; //$NON-NLS-1$

    /**
     * A <code>Date</code> value; specifying the end of the time-dependend simulation model.
     */
    public final static String ARG_VERB_TIME_ENDING_UP = "TIME_ENDING_UP"; //$NON-NLS-1$

    /**
     * A <code>Date</code> value; specifying the start-point of the time-dependend simulation
     * model.
     */
    public final static String ARG_VERB_TIME_START_UP = "TIME_START_UP"; //$NON-NLS-1$

    /**
     * A <code>String</code> value; specifying the database url needed for connection.
     */
    public final static String ARG_VERB_REMOTEDB = "REMOTEDBURL"; //$NON-NLS-1$

    /**
     * <p>
     * A <code>String</code> value; the option specifies the home directory of the current user.
     * </p>
     */
    public final static String ARG_VERB_USER_HOME = "HOME"; //$NON-NLS-1$

    /**
     * <p>
     * A <code>String</code> value; the option specifies the user name of the current user.
     * </p>
     */
    public final static String ARG_VERB_USER_NAME = "USERNAME"; //$NON-NLS-1$

    /**
     * <p>
     * The project-space ERROR output stream. This stream is already open and ready to accept output
     * data. Typically this stream corresponds to display output or another output destination
     * specified by the host environment or user. By convention, this output stream is used to
     * display ERROR messages or other information that should come to the immediate attention of a
     * user even if the principal output stream, the value of the variable out, has been redirected
     * to a file or other destination that is typically not continuously monitored.
     * </p>
     */
    public final PrintStream err;

    /**
     * <p>
     * The internal stream. This stream is already open and ready to accept output data. Typically
     * this stream corresponds to display output or another output destination specified by the host
     * environment or user.
     * </p>
     */
    public final PrintStream internal;

    /**
     * <p>
     * The project-space output stream. This stream is already open and ready to accept output data.
     * Typically this stream corresponds to display output or another output destination specified
     * by the host environment or user.
     * </p>
     * <p>
     * For simple stand-alone Java applications, a typical way to write a line of output data is:<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;
     * <code>out.println(data)</code><br/><br/>See the <code>println</code> methods in class
     * <code>PrintStream</code>.
     * </p>
     * 
     * @see <code>PrintStream.println()</code>, <code>PrintStream.println(boolean)</code>,
     *      <code>PrintStream.println(char)</code>, <code>PrintStream.println(char[])</code>,
     *      <code>PrintStream.println(double)</code>, <code>PrintStream.println(float)</code>,
     *      <code>PrintStream.println(int)</code>, <code>PrintStream.println(long)</code>,
     *      <code>PrintStream.println(java.lang.Object)</code>,
     *      <code>PrintStream.println(java.lang.String)</code>
     */
    public final PrintStream out;

    // Construction
    /**
     * <p>
     * The constructor <code>Projectspace</code> creates a project space object using the current
     * standard output streams provided by the <code>System</code> class to initialize its print
     * streams.
     * </p>
     */
    public Projectspace() {

        super();
        err = newPrintStream(null, System.err);
        internal = newPrintStream(null, System.out);
        out = newPrintStream(null, System.out);
        __initialize();
    } // Projectspace

    /**
     * <p>
     * The copy constructor <code>Projectspace</code> defines a project space object with the
     * specified internal, output and ERROR stream.
     * </p>
     * 
     * @param internalStream - output stream, used as internal stream.
     * @param outStream - output stream, used as output stream.
     * @param errorStream - output stream, used as ERROR stream.
     */
    public Projectspace( OutputStream internalStream, OutputStream outStream,
            OutputStream errorStream ) {

        super();
        err = newPrintStream(errorStream, System.err);
        internal = newPrintStream(internalStream, System.out);
        out = newPrintStream(outStream, System.out);
        __initialize();
    } // Projectspace

    /** */
    private static String __initializeSystemProperty( String key ) {

        String retval = null;

        Set<Entry<String, String>> set = System.getenv().entrySet();
        Iterator<Entry<String, String>> iter = set.iterator();
        while( null == retval && true == iter.hasNext() ) {

            Entry<String, String> entry = iter.next();
            if (true == entry.getKey() instanceof String) {

                String candidate = entry.getKey();
                if (0 == candidate.compareToIgnoreCase(key))
                    retval = candidate;
            }
        }

        return retval;
    } // __initializeSystemProperty

    /**
     * <p>
     * The method <code>__initialize</code> helps to initialize this object at construction time.
     * </p>
     */
    private void __initialize() {

        m_languageBundle = ResourceBundle.getBundle(RESOURCE_ML_JAVA4_01);

        m_projectCaption = ""; //$NON-NLS-1$

        m_dictionaryJavaExchangeWords = new Vector<JavaExchangeWordDescriptor>();
        m_dictionaryJavaModelWords = new Vector<JavaModelWordDescriptor>();
        m_dictionaryNativeModelWords = new Vector<NativeModelWordDescriptor>();
        m_importedExchangeKeywords = new Vector<String>();
        m_importedQuantityKeywords = new Vector<String>();
        m_importedClasses = new Vector<String>();
        m_importedJavaModelKeywords = new Vector<String>();
        m_importedNativeModelKeywords = new Vector<String>();
        m_argv_literals = new HashMap<String, String>();

        // Annotation keywords are static and therefore get pre-defined
        // hard coded...
        m_annotationKeywords = new Vector<String>();
        m_annotationKeywords.add("@author"); //$NON-NLS-1$
        // - unused - m_annotationKeywords.add( "@deprecated" ); //$NON-NLS-1$
        // - unused - m_annotationKeywords.add( "@exception" ); //$NON-NLS-1$
        // - unused - m_annotationKeywords.add( "@param" ); //$NON-NLS-1$
        // - unused - m_annotationKeywords.add( "@return" ); //$NON-NLS-1$
        m_annotationKeywords.add("@see"); //$NON-NLS-1$
        // - unused - m_annotationKeywords.add( "@serial" ); //$NON-NLS-1$
        // - unused - m_annotationKeywords.add( "@serialData" ); //$NON-NLS-1$
        // - unused - m_annotationKeywords.add( "@serialField" ); //$NON-NLS-1$
        m_annotationKeywords.add("@since"); //$NON-NLS-1$
        // - unused - m_annotationKeywords.add( "@throws" ); //$NON-NLS-1$
        m_annotationKeywords.add("@version"); //$NON-NLS-1$			

        // Preprocessor keywords are static and therefore get pre-defined
        // hard coded...
        m_preprocessorKeywords = new Vector<String>();
        m_preprocessorKeywords.add("jgrass"); //$NON-NLS-1$
        m_preprocessorKeywords.add("grass"); //$NON-NLS-1$
        m_preprocessorKeywords.add("R"); //$NON-NLS-1$

        // Jave keywords are static and therefore get pre-defined
        // hard coded...
        m_reservedConstants = new Vector<String>();
        m_reservedConstants.add("false"); //$NON-NLS-1$
        m_reservedConstants.add("null"); //$NON-NLS-1$
        m_reservedConstants.add("true"); //$NON-NLS-1$

        // Java Keywords are static and therefore get pre-defined
        // hard coded...
        m_reservedKeywords = new Vector<String>();
        // - unused - m_reservedKeywords.add( "abstract" ); //$NON-NLS-1$
        m_reservedKeywords.add("break"); //$NON-NLS-1$
        m_reservedKeywords.add("case"); //$NON-NLS-1$
        m_reservedKeywords.add("catch"); //$NON-NLS-1$
        m_reservedKeywords.add("class"); //$NON-NLS-1$
        m_reservedKeywords.add("continue"); //$NON-NLS-1$
        m_reservedKeywords.add("default"); //$NON-NLS-1$
        m_reservedKeywords.add("do"); //$NON-NLS-1$
        m_reservedKeywords.add("else"); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "extends" ); //$NON-NLS-1$
        m_reservedKeywords.add("final"); //$NON-NLS-1$
        m_reservedKeywords.add("finally"); //$NON-NLS-1$
        m_reservedKeywords.add("for"); //$NON-NLS-1$
        m_reservedKeywords.add("if"); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "implements" ); //$NON-NLS-1$
        m_reservedKeywords.add("import"); //$NON-NLS-1$
        m_reservedKeywords.add("instanceof"); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "interface" ); //$NON-NLS-1$
        m_reservedKeywords.add("native"); //$NON-NLS-1$
        m_reservedKeywords.add("new"); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "package" ); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "private" ); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "protected" ); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "public" ); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "return" ); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "static" ); //$NON-NLS-1$
        m_reservedKeywords.add("super"); //$NON-NLS-1$
        m_reservedKeywords.add("switch"); //$NON-NLS-1$
        m_reservedKeywords.add("synchronized"); //$NON-NLS-1$
        m_reservedKeywords.add("this"); //$NON-NLS-1$
        m_reservedKeywords.add("throw"); //$NON-NLS-1$
        // - unused - m_reservedKeywords.add( "throws" ); //$NON-NLS-1$
        m_reservedKeywords.add("transient"); //$NON-NLS-1$
        m_reservedKeywords.add("try"); //$NON-NLS-1$
        m_reservedKeywords.add("volatile"); //$NON-NLS-1$
        m_reservedKeywords.add("while"); //$NON-NLS-1$

        /*
         * add to reserved keywords also particular console syntax extention
         */
        
        m_reservedKeywords.addAll(VariablesAndCommands.variables); 
        m_reservedKeywords.addAll(VariablesAndCommands.commands); 
        
        // Jave Keywords are static and therefore get pre-defined
        // hard coded...
        m_reservedTypes = new Vector<String>();
        m_reservedTypes.add("def"); //$NON-NLS-1$
        m_reservedTypes.add("void"); //$NON-NLS-1$
        m_reservedTypes.add("boolean"); //$NON-NLS-1$
        m_reservedTypes.add("char"); //$NON-NLS-1$
        m_reservedTypes.add("byte"); //$NON-NLS-1$
        m_reservedTypes.add("short"); //$NON-NLS-1$
        m_reservedTypes.add("int"); //$NON-NLS-1$
        m_reservedTypes.add("long"); //$NON-NLS-1$
        m_reservedTypes.add("float"); //$NON-NLS-1$
        m_reservedTypes.add("double"); //$NON-NLS-1$

        // Initializing the options...
        m_argv_literals.put(ARG_VERB_BINARY_PATH, null);
        m_argv_literals.put(ARG_VERB_DEBUG, null);
        m_argv_literals.put(ARG_VERB_DYLD_LIBRARY_PATH, null);
        m_argv_literals.put(ARG_VERB_GISBASE, null);
        m_argv_literals.put(ARG_VERB_GISDBASE, null);
        m_argv_literals.put(ARG_VERB_GISRC, null);
        m_argv_literals.put(ARG_VERB_GRASS_GUI, null);
        m_argv_literals.put(ARG_VERB_GRASS_LD_LIBRARY_PATH, null);
        m_argv_literals.put(ARG_VERB_GRASSDB, null);
        m_argv_literals.put(ARG_VERB_LD_LIBRARY_PATH, null);
        m_argv_literals.put(ARG_VERB_LOCATION, null);
        m_argv_literals.put(ARG_VERB_LOCATION_NAME, null);
        m_argv_literals.put(ARG_VERB_MAPSET, null);
        m_argv_literals.put(ARG_VERB_REMOTEDB, null);
        m_argv_literals.put(ARG_VERB_MONITOR, null);
        m_argv_literals.put(ARG_VERB_PATH, null);
        m_argv_literals.put(ARG_VERB_TIME_DELTA, null);
        m_argv_literals.put(ARG_VERB_TIME_ENDING_UP, null);
        m_argv_literals.put(ARG_VERB_TIME_START_UP, null);
        m_argv_literals.put(ARG_VERB_USER_HOME, null);
        m_argv_literals.put(ARG_VERB_USER_NAME, null);
        m_argv_literals.put(ARG_VERB_GRASS_SH, null);
    } // __initialize

    // Operations
    /**
     * <p>
     * Initializes the binary and library paths.
     * </p>
     * 
     * @param path - path to the GRASS installation.
     */
    private void __initializeBinaryLibraryPaths( String path ) {

        if (null == path || 0 >= path.length()) {

            m_argv_literals.put(ARG_VERB_GISBASE, null);
            m_argv_literals.put(ARG_VERB_BINARY_PATH, null);
            m_argv_literals.put(ARG_VERB_DYLD_LIBRARY_PATH, null);
            m_argv_literals.put(ARG_VERB_GRASS_LD_LIBRARY_PATH, null);
            m_argv_literals.put(ARG_VERB_LD_LIBRARY_PATH, null);
            m_argv_literals.put(ARG_VERB_PATH, null);
            m_argv_literals.put(ARG_VERB_GRASS_SH, null);
        } else {

            File f = new File(path);
            if (true == f.exists() && true == f.isDirectory()) {

                // Path to the installation folder of the Geological
                // Information System (GRASS)...
                m_argv_literals.put(ARG_VERB_GISBASE, path);

                // Path to binaries...
                final String binaryPathString = path + File.separator + "bin"; //$NON-NLS-1$
                m_argv_literals.put(ARG_VERB_BINARY_PATH, binaryPathString);

                /*
                 * for library paths the windows version of GRASS requiers us to supply several
                 * path, since we include the install in the JGrass root folder and the path are not
                 * in the global variables. For now, just let's define the path for all operating
                 * systems, since having unexisting paths in the system PATHS doesn't give problems.
                 */
                // Path to libraries...
                StringBuffer libraryPathBuffer = new StringBuffer();
                libraryPathBuffer.append(path + File.separator + "lib"); //$NON-NLS-1$ 
                libraryPathBuffer.append(File.pathSeparator);
                libraryPathBuffer.append(path + File.separator + "extrabin"); //$NON-NLS-1$
                libraryPathBuffer.append(File.pathSeparator);
                libraryPathBuffer.append(path + File.separator + "extralib"); //$NON-NLS-1$
                libraryPathBuffer.append(File.pathSeparator);
                libraryPathBuffer.append(path + File.separator + "gpsbabel"); //$NON-NLS-1$
                libraryPathBuffer.append(File.pathSeparator);
                libraryPathBuffer.append(path + File.separator + "sqlite" + File.separator + "lib"); //$NON-NLS-1$
                libraryPathBuffer.append(File.pathSeparator);
                libraryPathBuffer.append(path + File.separator + "tcl-tk" + File.separator + "bin"); //$NON-NLS-1$
                libraryPathBuffer.append(File.pathSeparator);
                libraryPathBuffer.append(path + File.separator + "sqlite" + File.separator + "bin"); //$NON-NLS-1$
                libraryPathBuffer.append(File.pathSeparator);
                String msysBinFolder = path + File.separator + "msys" + File.separator + "bin";
                libraryPathBuffer.append(msysBinFolder); //$NON-NLS-1$

                m_argv_literals.put(ARG_VERB_DYLD_LIBRARY_PATH, libraryPathBuffer.toString());
                m_argv_literals.put(ARG_VERB_GRASS_LD_LIBRARY_PATH, libraryPathBuffer.toString());
                m_argv_literals.put(ARG_VERB_LD_LIBRARY_PATH, libraryPathBuffer.toString());

                String msysShellExe = msysBinFolder + File.separator + "sh.exe";
                m_argv_literals.put(ARG_VERB_GRASS_SH, msysShellExe);

                // Additional environment path informations...
                final Object[] __argv_path = {File.pathSeparator, binaryPathString,
                        libraryPathBuffer.toString()};
                final String pathString = MessageFormat.format(MSG_FMT_ARGV_2_PATH_VARIABLE,
                        __argv_path);
                m_argv_literals.put(ARG_VERB_PATH, pathString);
                m_argv_literals.put("TEST", pathString);
            }
        }
    } // __initializeBinaryLibraryPaths

    /**
     * <p>
     * Initializes the grass database path and the location, mapset folder.
     * </p>
     * 
     * @param path - path to the GRASS installation.
     */
    private void __initializeGrassdbLocationMapset( String path ) {

        if (null == path || 0 >= path.length() || !(new File(path).exists())) {

            m_argv_literals.put(ARG_VERB_MAPSET, null);
            m_argv_literals.put(ARG_VERB_LOCATION_NAME, null);
            m_argv_literals.put(ARG_VERB_LOCATION, null);
            m_argv_literals.put(ARG_VERB_GISDBASE, null);
            m_argv_literals.put(ARG_VERB_GRASSDB, null);
        } else {

            String locationString = null;
            String mapsetString = null;
            String grassdbString = null;

            File f = new File(path);
            if (true == f.exists() && true == f.isDirectory()) {

                mapsetString = f.getName();

                f = new File(f.getParent());
                if (true == f.exists() && true == f.isDirectory()) {

                    locationString = f.getName();

                    f = new File(f.getParent());
                    if (true == f.exists() && true == f.isDirectory()) {

                        grassdbString = f.getPath();
                    }
                }
            }

            if (null != mapsetString && null != locationString && null != grassdbString) {

                m_argv_literals.put(ARG_VERB_MAPSET, mapsetString);
                m_argv_literals.put(ARG_VERB_LOCATION_NAME, locationString);
                m_argv_literals.put(ARG_VERB_LOCATION, locationString);
                m_argv_literals.put(ARG_VERB_GISDBASE, grassdbString);
                m_argv_literals.put(ARG_VERB_GRASSDB, grassdbString);
            }
        }
    } // __initializeGrassdbLocationMapset

    /**
     * <p>
     * Loads the runtime information from the specified file or from the XML documents found at the
     * specified path.
     * </p>
     * 
     * @param filename - a path to the XML documents or a file name.
     */
    private void loadFromFullPathName( String filename ) {

        if (null != filename) {

            File file = new File(toAbsolutePathname(filename));
            if (true == file.isFile()) {

                loadFromXML(file);
            } else if (true == file.isDirectory()) {

                File[] files = file.listFiles();
                if (null != files && 0 < files.length) {

                    int i = 0;
                    do {

                        File in = files[i];
                        if (true == in.isFile()) {

                            final String s = in.getAbsolutePath();
                            if (s.length() - 4 == s.lastIndexOf(".xml")) //$NON-NLS-1$
                                loadFromXML(in);
                        }

                        ++i;
                    } while( i < files.length );
                }
            }
        }
    } // loadFromFullPathName

    /**
     * <p>
     * Parses the specified file or XML document, using a reader object, twice, first for native
     * based commands and then for Java based/Open MI complaint identifier (keyword) declarations
     * and maps their definition, if found any.
     * </p>
     * 
     * @param in - a reader of a XML document.
     */
    @SuppressWarnings("unchecked")
    private void loadFromXML( File in ) {

        try {

            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            DocumentBuilder p;
            Document xmlDocument = null;
            try {
                p = f.newDocumentBuilder();
                xmlDocument = p.parse(in);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // try {
            Element docEle = xmlDocument.getDocumentElement();
            NodeList childNodes = docEle.getElementsByTagName(XML_TAG_GRASS_CMD); //$NON-NLS-1$
            for( int i = 0; i < childNodes.getLength(); i++ ) {
                Node item = childNodes.item(i);
                parseXMLGrassCmdTag(item);
            }

            childNodes = docEle.getElementsByTagName(XML_TAG_JGRASS_IMPORT); //$NON-NLS-1$
            for( int i = 0; i < childNodes.getLength(); i++ ) {
                Node item = childNodes.item(i);
                parseXMLJGrassImportTag(item);
            }

            // final Reader reader = new FileReader(in);
            //
            // final Tag xmlroot = TagExtractor.extractTags(reader);
            // if (null != xmlroot && false == xmlroot.isEmpty()) {
            //
            // Vector<Tag> vecGrassEnv = xmlroot.getTagsByType(XML_TAG_GRASS_ENV);
            // for( int i = 0; i < vecGrassEnv.size(); ++i ) {
            //
            // Tag tagGrassEnv = vecGrassEnv.elementAt(i);
            // parseXMLGrassEnvTag(tagGrassEnv);
            // Vector<Tag> vecGrassCmd = tagGrassEnv.getTagsByType(XML_TAG_GRASS_CMD);
            // for( int n = 0; n < vecGrassCmd.size(); ++n )
            // parseXMLGrassCmdTag(vecGrassCmd.elementAt(n));
            // }
            //
            // Vector<Tag> vecImport = xmlroot.getTagsByType(XML_TAG_JGRASS_IMPORT);
            // for( int i = 0; i < vecImport.size(); ++i ) {
            //
            // Tag tagJGrassImport = vecImport.elementAt(i);
            // parseXMLJGrassImportTag(tagJGrassImport);
            // }
            // }
            // } finally {
            // reader.close();
            // }
        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                System.out.println(e);

            e.printStackTrace();
        }
    } // loadFromXML

    /**
     * <p>
     * Creates a new <code>PrintStream</code> object.
     * </p>
     * 
     * @return A new <code>PrintStream</code>.
     */
    private PrintStream newPrintStream( OutputStream arg0, OutputStream arg1 ) {

        return new PrintStream((null != arg0) ? arg0 : arg1, true);
    } // newPrintStream

    /**
     * <p>
     * Analyzes the attributes of the specified XML document TAG to identify a single native based
     * GRASS command and maps their definition into the list of native command keywords, if found
     * any.
     * </p>
     * 
     * @param item - a <code>XML_TAG_GRASS_CMD</code> tag of a XML document.
     */
    private void parseXMLGrassCmdTag( Node item ) {

        if (null == item)
            throw new IllegalArgumentException();
        if (0 != item.getNodeName().compareToIgnoreCase(XML_TAG_GRASS_CMD))
            throw new IllegalArgumentException();

        NamedNodeMap attributes = item.getAttributes();
        String name = attributes.getNamedItem(XML_ATTR_NAME).getNodeValue();

        if (null != name) {

            if (0 < name.length()) {

                m_importedNativeModelKeywords.add(name);
                m_dictionaryNativeModelWords.add(new NativeModelWordDescriptor(name));
            }
        }
    } // parseXMLGrassCmdTag

    /**
     * <p>
     * Analyzes the attributes of the specified XML document TAG to pre-initialize the GRASS
     * environment, if found any information.
     * </p>
     * 
     * @param tag - a <code>XML_TAG_GRASS_ENV</code> tag of a XML document.
     * @deprecated should not be used any longer, since now we can put environment variables inside
     *             the scripts
     */
    private void parseXMLGrassEnvTag( Tag tag ) {

        if (null == tag)
            throw new IllegalArgumentException();
        if (0 != tag.getType().compareToIgnoreCase(XML_TAG_GRASS_ENV))
            throw new IllegalArgumentException();

        final Attribute attrGisbase = tag.getAttribute(XML_ATTR_GISBASE);
        if (null != attrGisbase) {

            final String valueGisbase = attrGisbase.getValue();
            if (null != valueGisbase && 0 < valueGisbase.length())
                __initializeBinaryLibraryPaths(valueGisbase);
        }

        final Attribute attrMapset = tag.getAttribute(XML_ATTR_DEFAULT_MAPSET);
        if (null != attrMapset) {

            final String valueMapset = attrMapset.getValue();
            if (null != valueMapset && 0 < valueMapset.length())
                __initializeGrassdbLocationMapset(valueMapset);
        }
    } // parseGrassEnvTag

    /**
     * <p>
     * Analyzes the attributes of the specified XML document TAG to identify a single Java or JGRASS
     * model class and maps their definition into the list of native command keywords, if found any.
     * </p>
     * 
     * @param item - a <code>XML_TAG_JGRASS_IMPORT</code> tag of a XML document.
     */
    private void parseXMLJGrassImportTag( Node item ) {

        if (null == item)
            throw new IllegalArgumentException();
        if (0 != item.getNodeName().compareToIgnoreCase(XML_TAG_JGRASS_IMPORT))
            throw new IllegalArgumentException();

        NamedNodeMap attributes = item.getAttributes();

        Node classItem = attributes.getNamedItem(XML_ATTR_CLASS);
        final String valueClass = classItem != null ? classItem.getNodeValue() : null;
        Node defKeyItem = attributes.getNamedItem(XML_ATTR_DEFAULT_KEY);
        final String valueKey = defKeyItem != null ? defKeyItem.getNodeValue() : null;
        Node nameItem = attributes.getNamedItem(XML_ATTR_NAME);
        final String valueName = nameItem != null ? nameItem.getNodeValue() : null;
        Node exchItem = attributes.getNamedItem(XML_ATTR_EXCHANGE_ITEM);
        final String valueXchng = exchItem != null ? exchItem.getNodeValue() : null;

        if (null != valueClass && 0 < valueClass.length() && null != valueName
                && 0 < valueName.length()) {

            m_importedClasses.add(valueClass);
            if (valueName.indexOf('.') != 1 && valueName.indexOf('.') != 2) {

                if (true == valueName.startsWith("i") || //$NON-NLS-1$
                        true == valueName.startsWith("o")) { //$NON-NLS-1$

                    m_importedExchangeKeywords.add(valueName);
                    m_dictionaryJavaExchangeWords.add(new JavaExchangeWordDescriptor(valueName,
                            valueClass));
                }
            } else {

                m_importedJavaModelKeywords.add(valueName);
                m_dictionaryJavaModelWords.add(new JavaModelWordDescriptor(valueName, valueClass,
                        valueKey, valueXchng));

                if (null != valueXchng && 0 < valueXchng.length()) {

                    final Vector<String> vector = new Vector<String>();

                    String[] strings = valueXchng.split(T_COMMA);
                    for( int i = 0; i < strings.length; ++i ) {

                        if (0 != strings[i].compareTo(T_COMMA)) {

                            String[] tokens = strings[i].split(N_ASSIGN_OPERATOR);
                            if (2 == tokens.length)
                                vector.add(tokens[0].trim());
                        }
                    }

                    for( int i = 0; i < vector.size(); ++i ) {

                        String s = vector.get(i);
                        if (false == m_importedQuantityKeywords.contains(s))
                            m_importedQuantityKeywords.add(s);
                    }
                }
            }
        }
    } // parseImportTag

    /**
     * <p>
     * Returns the absolute pathname of the specified file.
     * </p>
     * 
     * @param filename - a relative path and file name.
     */
    private String toAbsolutePathname( String filename ) {

        String retval = null;

        File file = new File(filename);
        if (true == file.exists()) {

            retval = file.getAbsolutePath();
        } else {

            Bundle bundle = Platform.getBundle(ConsolePlugin.PLUGIN_ID);
            if (null != bundle) {

                URL url = bundle.getResource(filename);
                try {

                    URL fileURL = FileLocator.toFileURL(url);
                    File urlToFile = URLUtils.urlToFile(fileURL);
                    retval = urlToFile.getAbsolutePath();
                } catch (IOException e) {

                    if (true == Projectspace.isErrorEnabled())
                        System.out.println(e);

                    e.printStackTrace();
                }
            }
        }

        return retval;
    } // toAbsolutePathname

    /**
     * <p>
     * Returns <code>true</code>, if debug is enabled, otherwise <code>false</code>.
     * </p>
     */
    public static final boolean isDebugEnabled() {

        return (LOGGING_LEVEL_DEBUG == (m_loggingLevel & LOGGING_LEVEL_DEBUG)) ? true : false;
    } // isDebugEnabled

    /**
     * <p>
     * Returns <code>true</code>, if error is enabled, otherwise <code>false</code>.
     * </p>
     */
    public static final boolean isErrorEnabled() {

        return (LOGGING_LEVEL_ERROR == (m_loggingLevel & LOGGING_LEVEL_ERROR)) ? true : false;
    } // isErrorEnabled

    /**
     * <p>
     * Returns <code>true</code>, if trace is enabled, otherwise <code>false</code>.
     * </p>
     */
    public static final boolean isTraceEnabled() {

        return (LOGGING_LEVEL_TRACE == (m_loggingLevel & LOGGING_LEVEL_TRACE)) ? true : false;
    } // isTraceEnabled

    /** */
    public void release() {

        m_loggingLevel = Projectspace.LOGGING_LEVEL_ERROR;
        if (null != m_importedClasses)
            m_importedClasses.removeAllElements();
        if (null != m_importedExchangeKeywords)
            m_importedExchangeKeywords.removeAllElements();
        if (null != m_importedQuantityKeywords)
            m_importedQuantityKeywords.removeAllElements();
        if (null != m_importedJavaModelKeywords)
            m_importedJavaModelKeywords.removeAllElements();
        if (null != m_importedNativeModelKeywords)
            m_importedNativeModelKeywords.removeAllElements();
    } // release

    // Operations
    /**
     * <p>
     * Returns the list of annotation keywords.
     * </p>
     */
    public Vector<String> annotationKeywords() {

        return m_annotationKeywords;
    } // annotationKeywords

    /**
     * <p>
     * Returns a described list of the available input/output exchanges.
     * </p>
     */
    public Vector<JavaExchangeWordDescriptor> describeJavaExchangeWords() {

        return m_dictionaryJavaExchangeWords;
    } // describeJavaExchangeWords

    /**
     * <p>
     * Returns a described list of the available <i>JGRASS</i> models.
     * </p>
     */
    public Vector<JavaModelWordDescriptor> describeJavaModelWords() {

        return m_dictionaryJavaModelWords;
    } // describeJavaModelWords

    /**
     * <p>
     * Returns a described list of the available <i>GRASS</i> commands.
     * </p>
     */
    public Vector<NativeModelWordDescriptor> describeNativeModelWords() {

        return m_dictionaryNativeModelWords;
    } // describeNativeModelWords

    /**
     * <p>
     * Returns the literal string value of the specified argument.
     * </p>
     * 
     * @param argument - a named <code>ARG_VERB_...</code> constant.
     */
    public String getArgVerbValue( String argument ) {

        if (false == m_argv_literals.containsKey(argument))
            throw new IllegalArgumentException();

        return m_argv_literals.get(argument);
    } // getArgVerbValue

    /**
     * <p>
     * Returns the list of Java classes to import by the script engine.
     * </p>
     */
    public Vector<String> importedClasses() {

        return m_importedClasses;
    } // importedClasses

    /**
     * <p>
     * Returns the list of input/output exchange keywords.
     * </p>
     */
    public Vector<String> importedExchangeKeywords() {

        return m_importedExchangeKeywords;
    } // importedExchangeArguments

    /**
     * <p>
     * Returns the list of available <i>JGRASS</i> model keywords.
     * </p>
     */
    public Vector<String> importedJavaModelKeywords() {

        return m_importedJavaModelKeywords;
    } // importedJavaModelKeywords

    /**
     * <p>
     * Returns the list of available <i>JGRASS</i> model classes.
     * </p>
     */
    public Vector<String> importedJavaModelClasses() {
        int num = m_dictionaryJavaModelWords.size();
        Vector<String> javaModelClasses = new Vector<String>(num);
        for( JavaModelWordDescriptor descr : m_dictionaryJavaModelWords ) {
            javaModelClasses.add(descr.fullQualifiedName());
        }

        return javaModelClasses;
    } // importedJavaModelClasses

    /**
     * <p>
     * Returns the list of available <i>GRASS</i> command keywords.
     * </p>
     */
    public Vector<String> importedNativeModelKeywords() {

        return m_importedNativeModelKeywords;
    } // importedNativeModelKeywords

    /**
     * <p>
     * Returns the list of available keywords denoting a quantity.
     * </p>
     */
    public Vector<String> importedQuantityKeywords() {

        return m_importedQuantityKeywords;
    } // importedQuantityKeywords

    /**
     * <p>
     * The method <code>initialize</code> initializes this project-space object using the
     * specified project-options.
     * </p>
     */
    public void initialize( ProjectOptions projectOptions ) {

        release();
        try {

            if (null != projectOptions.projectCaption())
                m_projectCaption = projectOptions.projectCaption();
            else
                m_projectCaption = ""; //$NON-NLS-1$

            // Setting up the logging level...
            m_loggingLevel = ((false == (Boolean) projectOptions.getOption(
                    ProjectOptions.CONSOLE_LOGGING_LEVEL_DEBUG, false))
                    ? LOGGING_LEVEL_ERROR
                    : LOGGING_LEVEL_DEBUG)
                    | ((false == (Boolean) projectOptions.getOption(
                            ProjectOptions.CONSOLE_LOGGING_LEVEL_TRACE, false))
                            ? LOGGING_LEVEL_ERROR
                            : LOGGING_LEVEL_TRACE);

            // Loading runtime informations and getting the reserved words from
            // the include directory files...
            String[] includes = (String[]) projectOptions
                    .getOption(ProjectOptions.CONSOLE_DIRECTORY_INCLUDE);
            if (null == includes[0] || 0 >= includes[0].length()) {

                loadFromFullPathName("rt"); //$NON-NLS-1$
            } else {

                for( int i = 0; i < includes.length; ++i ) {

                    loadFromFullPathName(includes[i]);
                }
            }

            Boolean debug = (Boolean) projectOptions.getOption(ProjectOptions.NATIVE_MODEL_DEBUG,
                    new Boolean(false));
            m_argv_literals.put(ARG_VERB_DEBUG, (debug) ? "1" : "0"); //$NON-NLS-2$ //$NON-NLS-1$

            m_argv_literals.put(ARG_VERB_MONITOR, "x0"); //$NON-NLS-1$

            m_argv_literals.put(ARG_VERB_GRASS_GUI, "text"); //$NON-NLS-1$

            m_argv_literals.put(ARG_VERB_USER_HOME, (String) projectOptions.getOption(
                    ProjectOptions.NATIVE_MODEL_USER_HOME, System.getProperty("user.home") //$NON-NLS-1$
                    ));

            m_argv_literals.put(ARG_VERB_USER_NAME, (String) projectOptions.getOption(
                    ProjectOptions.NATIVE_MODEL_USER_NAME, System.getProperty("user.name") //$NON-NLS-1$
                    ));

            final String gisbase = (String) projectOptions.getOption(
                    ProjectOptions.NATIVE_MODEL_GISBASE, "" //$NON-NLS-1$
            );
            if (null != gisbase && 0 < gisbase.length())
                __initializeBinaryLibraryPaths(gisbase);

            final String mapset = (String) projectOptions.getOption(
                    ProjectOptions.COMMON_GRASS_MAPSET, "" //$NON-NLS-1$
            );
            if (null != mapset && 0 < mapset.length())
                __initializeGrassdbLocationMapset(mapset);

            m_argv_literals.put(ARG_VERB_TIME_DELTA, Long.toString((Long) projectOptions.getOption(
                    ProjectOptions.JAVA_MODEL_TIME_DELTA, (long) -1)));

            m_argv_literals.put(ARG_VERB_TIME_ENDING_UP, (String) projectOptions.getOption(
                    ProjectOptions.JAVA_MODEL_TIME_ENDING_UP, null));

            m_argv_literals.put(ARG_VERB_TIME_START_UP, (String) projectOptions.getOption(
                    ProjectOptions.JAVA_MODEL_TIME_START_UP, null));

            m_argv_literals.put(ARG_VERB_REMOTEDB, (String) projectOptions.getOption(
                    ProjectOptions.CONSOLE_REMOTEDBURL, null));
        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                System.out.println(e);

            e.printStackTrace();
        }
    } // initialize
    /**
     * <p>
     * Returns the resource bundle with the <i>JGRASS</i>, <i>GRASS</i> language defintions.
     * </p>
     */
    public ResourceBundle languageBundle() {

        return m_languageBundle;
    } // languageBundle

    /**
     * <p>
     * Returns the list of preprocessor respectively compiler directive keywords.
     * </p>
     */
    public Vector<String> preprocessorKeywords() {

        return m_preprocessorKeywords;
    } // preprocessorKeywords

    /**
     * <p>
     * Returns the project caption.
     * </p>
     */
    public String projectCaption() {

        return m_projectCaption;
    } // projectCaption

    /**
     * <p>
     * Returns the list of reserved constant keywords.
     * </p>
     */
    public Vector<String> reservedConstants() {

        return m_reservedConstants;
    } // reservedJavaConstants

    /**
     * <p>
     * Returns the list of reserved keywords.
     * </p>
     */
    public Vector<String> reservedKeywords() {

        return m_reservedKeywords;
    } // reservedJavaKeywords

    /**
     * <p>
     * Returns the list of type name keywords.
     * </p>
     */
    public Vector<String> reservedTypes() {

        return m_reservedTypes;
    } // reservedJavaTypes

} // Projectspace
