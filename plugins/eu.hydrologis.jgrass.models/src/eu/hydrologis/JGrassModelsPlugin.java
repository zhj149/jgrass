package eu.hydrologis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import net.refractions.udig.catalog.URLUtils;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.libs.utils.NativeUtilities;

/**
 * The activator class controls the plug-in life cycle
 */
public class JGrassModelsPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.models"; //$NON-NLS-1$

    // The shared instance
    private static JGrassModelsPlugin plugin;

    /**
     * The constructor
     */
    public JGrassModelsPlugin() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    @SuppressWarnings("nls")
    public void start( BundleContext context ) throws Exception {
        super.start(context);

        /*
         * CREATE COMMANDS AND FILES
         */
        // createCommandsAndGuis();
        /*
         * LOAD NATIVE LIBRARIES PATHS
         */
        // add the ones from the jaba.library.path
        NativeUtilities.addJAVAlibraryToJNAplatform();
        try {
            // add the plugin root folder
            URL nativeFolderurl = FileLocator.find(Platform.getBundle(PLUGIN_ID), new Path(
                    "/nativelibs"), null);
            URL fileURL = FileLocator.toFileURL(nativeFolderurl);
            File urlToFile = URLUtils.urlToFile(fileURL);
            String nativeFolder = urlToFile.getAbsolutePath();
            NativeUtilities.addPathToJna(nativeFolder);
            NativeUtilities.addPathToPlatformJna(nativeFolder);
            NativeUtilities.addPathToJavaLibraryPath(nativeFolder);

            // read the file that could hold some other folders
            URL extraFolderPath = FileLocator.find(Platform.getBundle(PLUGIN_ID), new Path(
                    "/nativelibs/ext_native_paths.properties"), null);
            fileURL = FileLocator.toFileURL(extraFolderPath);
            urlToFile = URLUtils.urlToFile(fileURL);
            String extraFolderFile = urlToFile.getAbsolutePath();
            BufferedReader bR = new BufferedReader(new FileReader(extraFolderFile));
            String line = null;
            while( (line = bR.readLine()) != null ) {
                if (line.length() < 1) {
                    continue;
                }
                NativeUtilities.addPathToJna(line);
                NativeUtilities.addPathToPlatformJna(line);
                NativeUtilities.addPathToJavaLibraryPath(line);
            }
        } catch (Exception e) {
            // Report the problem some how
        }
        String property = System.getProperty("jna.library.path");
        System.out.println(property);
        property = System.getProperty("jna.platform.library.path");
        System.out.println(property);

        // String paths =
        // "/home/moovida/rcpdevelopment/WORKSPACES/eclipseGanimede/eu.hydrologis.jgrass.models/native/:/home/moovida/codeapisexes/fortran_intel/fc/9.0/lib/"
        // ;
        // // String paths = "/home/moovida/codeapisexes/fortran_intel/fc/9.0/lib/";
        // String property = System.getProperty("jna.library.path"); //$NON-NLS-1$
        // System.out.println(property);
        // System.setProperty("jna.library.path", paths); //$NON-NLS-1$
        // property = System.getProperty("jna.library.path"); //$NON-NLS-1$
        // System.out.println(property);

        // just one time, take them to JGrass group page
        // ScopedPreferenceStore m_preferences = (ScopedPreferenceStore) getDefault()
        // .getPreferenceStore();
        // String frapps = m_preferences.getString("@@@jgrass4frappr@@@");
        // if (frapps != null && frapps.length() < 1) {
        // boolean launch = Program.launch("http://www.frappr.com/jgrass/");
        // m_preferences.setValue("@@@jgrass4frappr@@@", "true");
        // }
    }

    private void createCommandsAndGuis() {
        StringBuffer buffer = new StringBuffer();
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg
                .getConfigurationElementsFor("eu.hydrologis.jgrass.models.openmimodel");
        for( int i = 0; i < extensions.length; i++ ) {
            IConfigurationElement element = extensions[i];
            if (!element.getName().equals("model")) {
                continue;
            }
            buffer.append(element.getAttribute("inputitems"));
            buffer.append(" (");
            String cost = "unknown";
            if (element.getAttribute("cost") != null) {
                cost = element.getAttribute("cost");
            }
            buffer.append(cost);
            buffer.append("\n");
        }

        System.out.println(buffer.toString());
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static JGrassModelsPlugin getDefault() {
        return plugin;
    }

    /**
     * Logs the Throwable in the plugin's log.
     * <p>
     * This will be a user visable ERROR iff:
     * <ul>
     * <li>t is an Exception we are assuming it is human readable or if a message is provided
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

}
