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
package eu.hydrologis.jgrass.console.editor.preferences;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.refractions.udig.catalog.URLUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.examples.javaeditor.util.JavaColorProvider;
import org.osgi.service.prefs.BackingStoreException;

import eu.hydrologis.jgrass.console.ConsolePlugin;
import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

    // Attributes
    // Declaration of ConsoleEngine preferences reps. Project Options used by the
    // ConsoleEngine, Command Line Interpreter (CLi) and the HydroloGIS Model
    // Language (HgisML) Preprocessor (HgisMLpp4j)...
    public final static String CONSOLE_ASYNC_MODE = "boolean@option@@ConsoleDirectiveAsyncMode"; //$NON-NLS-1$
    public final static String CONSOLE_DIRECTORY_INCLUDE = "string@option@ConsoleIncludeDirectory"; //$NON-NLS-1$
    public final static String CONSOLE_DIRECTORY_SOURCE = "string@option@ConsoleSourceDirectory"; //$NON-NLS-1$
    public final static String CONSOLE_LOGGING_LEVEL_DEBUG = "boolean@option@@__console_logging_level_debug"; //$NON-NLS-1$
    public final static String CONSOLE_LOGGING_LEVEL_TRACE = "boolean@option@@__console_logging_level_trace"; //$NON-NLS-1$
    public final static String CONSOLE_THREAD_MAX_COUNT = "boolean@option@@ConsoleDirectiveMaxThreads"; //$NON-NLS-1$
    public final static String CONSOLE_THREAD_RESTRICTION = "boolean@option@@ConsoleDirectiveLimitedThreadCount"; //$NON-NLS-1$
    public final static String CONSOLE_ARGV_MAPSET = "string@option@ConsoleMapsetFolder"; //$NON-NLS-1$
    // ConsoleEngine preferences resp. Project Options for the java native
    // interface...
    public final static String CONSOLE_ARGV_DEBUG = "string@option@@ConsoleGrassenvDebug"; //$NON-NLS-1$
    public final static String CONSOLE_ARGV_GISBASE = "string@option@@ConsoleGrassenvGisbase"; //$NON-NLS-1$
    public final static String CONSOLE_ARGV_USER_HOME = "string@option@@ConsoleGrassenvUserHome"; //$NON-NLS-1$
    public final static String CONSOLE_ARGV_USER_NAME = "string@option@@ConsoleGrassenvUserName"; //$NON-NLS-1$

    // Editor...
    public static final String IDC_COMMENT = "rgb@color@@comment"; //$NON-NLS-1$
    public static final String IDC_ML_PARAMETERS_KEYWORD = "rgb@color@@asteriskkeyword"; //$NON-NLS-1$
    public static final String IDC_CONSTANT = "rgb@color@@constant"; //$NON-NLS-1$
    public static final String IDC_JAVADOC_KEYWORD = "rgb@color@@javadockeyword"; //$NON-NLS-1$
    public static final String IDC_JAVADOC_LINK = "rgb@color@@javadoclink"; //$NON-NLS-1$
    public static final String IDC_JAVADOC_TAG = "rgb@color@@javadoctag"; //$NON-NLS-1$
    public static final String IDC_KEYWORD = "rgb@color@@keyword"; //$NON-NLS-1$
    public static final String IDC_ML_ASTERISK_KEYWORD = "rgb@color@@asteriskkeyword"; //$NON-NLS-1$
    public static final String IDC_ML_EXCHANGE_KEYWORD = "rgb@color@@exchangekeyword"; //$NON-NLS-1$
    public static final String IDC_ML_INPUT_KEYWORD = "rgb@color@@inputkeyword"; //$NON-NLS-1$
    public static final String IDC_ML_MODEL_KEYWORD = "rgb@color@@modelkeyword"; //$NON-NLS-1$
    public static final String IDC_ML_OUTPUT_KEYWORD = "rgb@color@@outputkeyword"; //$NON-NLS-1$
    public static final String IDC_OTHER = "rgb@color@@others"; //$NON-NLS-1$
    public static final String IDC_ML_PREPROCESSOR_KEYWORD = "rgb@color@@preprocessorkeyword"; //$NON-NLS-1$
    public static final String IDC_STRING = "rgb@color@@string"; //$NON-NLS-1$
    public static final String IDC_TYPE = "rgb@color@@type"; //$NON-NLS-1$

    // Default RGB keyword colors...

    public final static RGB RGB_COMMENT = new RGB(63, 127, 95);
    public static final RGB RGB_CONSTANT = new RGB(127, 0, 85);
    public static final RGB RGB_KEYWORD = new RGB(127, 0, 85);
    public final static RGB RGB_JAVADOC_KEYWORD = new RGB(127, 159, 191);
    public final static RGB RGB_JAVADOC_LINK = new RGB(63, 63, 191);
    public final static RGB RGB_JAVADOC_TAG = new RGB(127, 159, 191);
    public final static RGB RGB_ML_ASTERISK_KEYWORD = new RGB(217, 85, 63);
    public final static RGB RGB_ML_EXCHANGE_KEYWORD = new RGB(191, 63, 63);
    public final static RGB RGB_ML_INPUT_KEYWORD = new RGB(63, 95, 191);
    public final static RGB RGB_ML_MODEL_KEYWORD = new RGB(63, 95, 191);
    public final static RGB RGB_ML_OUTPUT_KEYWORD = new RGB(63, 95, 191);
    public final static RGB RGB_ML_PREPROCESSOR_KEYWORD = new RGB(127, 0, 85);
    public final static RGB RGB_OTHER = new RGB(0, 0, 0);
    public final static RGB RGB_STRING = new RGB(0, 0, 255);
    public static final RGB RGB_TYPE = new RGB(127, 0, 85);

    // Operations
    /** */
    public static void initialize( JavaColorProvider provider ) {

        IEclipsePreferences preferences;
        IEclipsePreferences defaults;
        try {

            preferences = new ConfigurationScope().getNode(ConsoleEditorPlugin.PLUGIN_ID);
            defaults = new DefaultScope().getNode(ConsoleEditorPlugin.PLUGIN_ID);

            provider.add(IDC_COMMENT, preferences.get(IDC_COMMENT, defaults.get(IDC_COMMENT,
                    JavaColorProvider.toString(RGB_COMMENT))));
            provider.add(IDC_ML_PARAMETERS_KEYWORD, preferences.get(IDC_ML_PARAMETERS_KEYWORD,
                    defaults.get(IDC_ML_PARAMETERS_KEYWORD, JavaColorProvider
                            .toString(RGB_ML_ASTERISK_KEYWORD))));
            provider.add(IDC_CONSTANT, preferences.get(IDC_CONSTANT, defaults.get(IDC_CONSTANT,
                    JavaColorProvider.toString(RGB_CONSTANT))));
            provider.add(IDC_JAVADOC_KEYWORD, preferences.get(IDC_JAVADOC_KEYWORD, defaults.get(
                    IDC_JAVADOC_KEYWORD, JavaColorProvider.toString(RGB_JAVADOC_KEYWORD))));
            provider.add(IDC_JAVADOC_LINK, preferences.get(IDC_JAVADOC_LINK, defaults.get(
                    IDC_JAVADOC_LINK, JavaColorProvider.toString(RGB_JAVADOC_LINK))));
            provider.add(IDC_JAVADOC_TAG, preferences.get(IDC_JAVADOC_TAG, defaults.get(
                    IDC_JAVADOC_TAG, JavaColorProvider.toString(RGB_JAVADOC_TAG))));
            provider.add(IDC_KEYWORD, preferences.get(IDC_KEYWORD, defaults.get(IDC_KEYWORD,
                    JavaColorProvider.toString(RGB_KEYWORD))));
            provider.add(IDC_ML_ASTERISK_KEYWORD, preferences.get(IDC_ML_ASTERISK_KEYWORD, defaults
                    .get(IDC_ML_ASTERISK_KEYWORD, JavaColorProvider
                            .toString(RGB_ML_ASTERISK_KEYWORD))));
            provider.add(IDC_ML_EXCHANGE_KEYWORD, preferences.get(IDC_ML_EXCHANGE_KEYWORD, defaults
                    .get(IDC_ML_EXCHANGE_KEYWORD, JavaColorProvider
                            .toString(RGB_ML_EXCHANGE_KEYWORD))));
            provider.add(IDC_ML_INPUT_KEYWORD, preferences.get(IDC_ML_INPUT_KEYWORD, defaults.get(
                    IDC_ML_INPUT_KEYWORD, JavaColorProvider.toString(RGB_ML_INPUT_KEYWORD))));
            provider.add(IDC_ML_MODEL_KEYWORD, preferences.get(IDC_ML_MODEL_KEYWORD, defaults.get(
                    IDC_ML_MODEL_KEYWORD, JavaColorProvider.toString(RGB_ML_MODEL_KEYWORD))));
            provider
                    .add(IDC_ML_OUTPUT_KEYWORD, preferences.get(IDC_ML_OUTPUT_KEYWORD, defaults
                            .get(IDC_ML_OUTPUT_KEYWORD, JavaColorProvider
                                    .toString(RGB_ML_OUTPUT_KEYWORD))));
            provider.add(IDC_ML_PREPROCESSOR_KEYWORD, preferences.get(IDC_ML_PREPROCESSOR_KEYWORD,
                    defaults.get(IDC_ML_PREPROCESSOR_KEYWORD, JavaColorProvider
                            .toString(RGB_ML_PREPROCESSOR_KEYWORD))));
            provider.add(IDC_OTHER, preferences.get(IDC_OTHER, defaults.get(IDC_OTHER,
                    JavaColorProvider.toString(RGB_OTHER))));
            provider.add(IDC_STRING, preferences.get(IDC_STRING, defaults.get(IDC_STRING,
                    JavaColorProvider.toString(RGB_STRING))));
            provider.add(IDC_TYPE, preferences.get(IDC_TYPE, defaults.get(IDC_TYPE,
                    JavaColorProvider.toString(RGB_TYPE))));
        } catch (Exception e) {

            e.printStackTrace();
        }
    } // initialize

    /** */
    public static void initialize( ProjectOptions options ) {

        IEclipsePreferences preferences;
        IEclipsePreferences defaults;
        try {

            preferences = new ConfigurationScope().getNode(ConsoleEditorPlugin.PLUGIN_ID);
            defaults = new DefaultScope().getNode(ConsoleEditorPlugin.PLUGIN_ID);

            // Initializing the asynchronous mode and thread restriction
            // settings...
            final boolean restrictedAsyncMode = preferences.getBoolean(CONSOLE_THREAD_RESTRICTION,
                    defaults.getBoolean(CONSOLE_THREAD_RESTRICTION, false));
            final boolean asyncMode = preferences.getBoolean(CONSOLE_ASYNC_MODE, defaults
                    .getBoolean(CONSOLE_ASYNC_MODE, true));
            options.setOption(ProjectOptions.CONSOLE_ASYNC_MODE, asyncMode);
            if (false == asyncMode) {

                options.setOption(ProjectOptions.CONSOLE_THREAD_RESTRICTION, 0);
            } else if (false == restrictedAsyncMode) {

                options.setOption(ProjectOptions.CONSOLE_THREAD_RESTRICTION, -1);
            } else {

                options.setOption(ProjectOptions.CONSOLE_THREAD_RESTRICTION, preferences.getInt(
                        CONSOLE_THREAD_MAX_COUNT, 1));
            }

            // Logging Level: DEBUG
            options.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_DEBUG, preferences.getBoolean(
                    CONSOLE_LOGGING_LEVEL_DEBUG, defaults.getBoolean(CONSOLE_LOGGING_LEVEL_DEBUG,
                            false)));
            // Logging Level: TRACE
            options.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_TRACE, preferences.getBoolean(
                    CONSOLE_LOGGING_LEVEL_TRACE, defaults.getBoolean(CONSOLE_LOGGING_LEVEL_TRACE,
                            false)));

            // Include directory; RTTI - runtime type informations - the reserved
            // words...
            options
                    .setOption(ProjectOptions.CONSOLE_DIRECTORY_INCLUDE, new String[]{preferences
                            .get(CONSOLE_DIRECTORY_INCLUDE, defaults.get(CONSOLE_DIRECTORY_INCLUDE,
                                    null))});

            // Source directory; default script file location...
            options.setOption(ProjectOptions.CONSOLE_DIRECTORY_SOURCE, preferences.get(
                    CONSOLE_DIRECTORY_SOURCE, defaults.get(CONSOLE_DIRECTORY_SOURCE, null)));

            // User information - the home directory of the current user...
            options.setOption(ProjectOptions.NATIVE_MODEL_USER_HOME, preferences.get(
                    CONSOLE_ARGV_USER_HOME, System.getProperty("user.home") //$NON-NLS-1$
                    ));
            // User information - the user name of the current user...
            options.setOption(ProjectOptions.NATIVE_MODEL_USER_NAME, preferences.get(
                    CONSOLE_ARGV_USER_NAME, System.getProperty("user.name") //$NON-NLS-1$
                    ));

            // Debug mode...
            options.setOption(ProjectOptions.NATIVE_MODEL_DEBUG, preferences.getBoolean(
                    CONSOLE_ARGV_DEBUG, defaults.getBoolean(CONSOLE_ARGV_DEBUG, false)));

            // Installation folder of GRASS...
            URL pluginInternalURL = Platform.getInstallLocation().getURL();
            File urlToFile = URLUtils.urlToFile(pluginInternalURL);
            String grassGisbasePath = new File(urlToFile, "grass").getAbsolutePath();
            options.setOption(ProjectOptions.NATIVE_MODEL_GISBASE, preferences.get(
                    CONSOLE_ARGV_GISBASE, defaults.get(CONSOLE_ARGV_GISBASE, grassGisbasePath)));
            // GRASS database, location, mapset path...
            Object mapsetPath = options.getOption(ProjectOptions.COMMON_GRASS_MAPSET);
            if (null == mapsetPath || ((String) mapsetPath).length() == 0)
                options.setOption(ProjectOptions.COMMON_GRASS_MAPSET, preferences.get(
                        CONSOLE_ARGV_MAPSET, defaults.get(CONSOLE_ARGV_MAPSET, null)));

            if (null == options.getOption(ProjectOptions.JAVA_MODEL_TIME_DELTA))
                options.setOption(ProjectOptions.JAVA_MODEL_TIME_DELTA, null);
            if (null == options.getOption(ProjectOptions.JAVA_MODEL_TIME_ENDING_UP))
                options.setOption(ProjectOptions.JAVA_MODEL_TIME_ENDING_UP, null);
            if (null == options.getOption(ProjectOptions.JAVA_MODEL_TIME_START_UP))
                options.setOption(ProjectOptions.JAVA_MODEL_TIME_START_UP, null);
        } catch (Exception e) {

            e.printStackTrace();
        }
    } // initializeStartupProjectOptions

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */

    public void initializeDefaultPreferences() {

        IEclipsePreferences defaults = new DefaultScope().getNode(ConsoleEditorPlugin.PLUGIN_ID);
        if (null != defaults) {

            defaults.putBoolean(CONSOLE_ASYNC_MODE, true);
            defaults.putBoolean(CONSOLE_THREAD_RESTRICTION, false);
            defaults.putInt(CONSOLE_THREAD_MAX_COUNT, 0);

            URL rtUrl = Platform.getBundle(ConsolePlugin.PLUGIN_ID).getResource("rt"); //$NON-NLS-1$
            String rtPath = null;
            try {
                URL fileURL = FileLocator.toFileURL(rtUrl);
                File urlToFile = URLUtils.urlToFile(fileURL);
                rtPath = urlToFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
             * if it is windows, try to get the paths from the grass plugin
             */
            String grassGisbasePath = null;
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                // try {
                // URL grassGisbaseUrl = Platform.getBundle(GrassPlugin.PLUGIN_ID).getResource(
                // "windows/grass/grass"); //$NON-NLS-1$
                // grassGisbasePath = FileLocator.toFileURL(grassGisbaseUrl).getPath();
                // } catch (Exception e) {
                // ConsoleEditorPlugin
                // .log(
                // "ConsoleEditorPlugin problem:
                // eu.hydrologis.jgrass.console.editor.preferences#PreferencesInitializer#initializeDefaultPreferences",
                // e); //$NON-NLS-1$
                // e.printStackTrace();
                // }

                URL pluginInternalURL = Platform.getInstallLocation().getURL();
                File urlToFile = URLUtils.urlToFile(pluginInternalURL);
                grassGisbasePath = urlToFile.getAbsolutePath() + File.separator + "grass";

            } else if (Platform.getOS().equals(Platform.OS_LINUX)) {
                // do linux specific stuff
            } else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
                // do macosx specific stuff
            } else {
                // throw exceptions
            }

            defaults.put(CONSOLE_DIRECTORY_INCLUDE, rtPath);
            defaults.putBoolean(CONSOLE_LOGGING_LEVEL_DEBUG, false);
            defaults.putBoolean(CONSOLE_LOGGING_LEVEL_TRACE, false);
            defaults.put(CONSOLE_ARGV_MAPSET, ""); //$NON-NLS-1$
            defaults.put(CONSOLE_DIRECTORY_SOURCE, System.getProperty("user.home")); //$NON-NLS-1$
            defaults.putBoolean(CONSOLE_ARGV_DEBUG, false);

            if (grassGisbasePath != null && new File(grassGisbasePath).exists()) {
                defaults.put(CONSOLE_ARGV_GISBASE, grassGisbasePath);
                ConsoleEditorPlugin.updateNativeGrassXml(null, grassGisbasePath);
            } else {
                defaults.put(CONSOLE_ARGV_GISBASE, ""); //$NON-NLS-1$
            }
            defaults.put(CONSOLE_ARGV_USER_HOME, System.getProperty("user.home")); //$NON-NLS-1$
            defaults.put(CONSOLE_ARGV_USER_NAME, System.getProperty("user.name")); //$NON-NLS-1$
            defaults.put(IDC_ML_ASTERISK_KEYWORD, JavaColorProvider
                    .toString(RGB_ML_ASTERISK_KEYWORD));
            defaults.put(IDC_COMMENT, JavaColorProvider.toString(RGB_COMMENT));
            defaults.put(IDC_ML_PARAMETERS_KEYWORD, JavaColorProvider
                    .toString(RGB_ML_ASTERISK_KEYWORD));
            defaults.put(IDC_CONSTANT, JavaColorProvider.toString(RGB_CONSTANT));
            defaults.put(IDC_KEYWORD, JavaColorProvider.toString(RGB_KEYWORD));
            defaults.put(IDC_ML_INPUT_KEYWORD, JavaColorProvider.toString(RGB_ML_INPUT_KEYWORD));
            defaults.put(IDC_ML_EXCHANGE_KEYWORD, JavaColorProvider
                    .toString(RGB_ML_EXCHANGE_KEYWORD));
            defaults.put(IDC_ML_OUTPUT_KEYWORD, JavaColorProvider.toString(RGB_ML_OUTPUT_KEYWORD));
            defaults.put(IDC_ML_MODEL_KEYWORD, JavaColorProvider.toString(RGB_ML_MODEL_KEYWORD));
            defaults.put(IDC_OTHER, JavaColorProvider.toString(RGB_OTHER));
            defaults.put(IDC_ML_PREPROCESSOR_KEYWORD, JavaColorProvider
                    .toString(RGB_ML_PREPROCESSOR_KEYWORD));
            defaults.put(IDC_STRING, JavaColorProvider.toString(RGB_STRING));
            defaults.put(IDC_TYPE, JavaColorProvider.toString(RGB_TYPE));

            try {

                defaults.flush();
            } catch (BackingStoreException e) {

                e.printStackTrace();
            }
        }
    } // initializeDefaultPreferences

} // PreferencesInitializer
