package eu.hydrologis.jgrass.grass;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class GrassPlugin extends Plugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.grass"; //$NON-NLS-1$

    // The shared instance
    private static GrassPlugin plugin;

    /**
     * The constructor
     */
    public GrassPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;
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
    public static GrassPlugin getDefault() {
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

    /**
     * Checks if a GRASS native installation is available and properly configured.
     * 
     * @param gisbase the path to the GRASS gisbase. This can be null, in which case the gisbase is taken from the preferences.
     * @return true if the GRASS installation is properly set.
     */
    public boolean isGrassAvailable( String gisbase ) {
        if (gisbase == null) {
            ScopedPreferenceStore preferences = (ScopedPreferenceStore) ConsoleEditorPlugin
                    .getDefault().getPreferenceStore();
            gisbase = preferences.getString(PreferencesInitializer.CONSOLE_ARGV_GISBASE);
            if (gisbase == null) {
                return false;
            }
        }

        /*
         * do some checks
         */
        File tmpfile = new File(gisbase);
        if (!tmpfile.exists()) {
            return false;
        }
        // we suppose that GRASS has mapcalc
        File f1 = new File(tmpfile, "bin/r.mapcalc");
        File f2 = new File(tmpfile, "bin/r.mapcalc.exe");
        if (!f1.exists() && !f2.exists()) {
            return false;
        }
        return true;
    }

    /**
     * Checks if a GRASS mapset is consistent.
     * 
     * <p>
     * Checks are done on existence of the base folders
     * and the WIND file.
     * </p>
     * 
     * @param mapset the path to the GRASS mapset. 
     * @return true if the GRASS mapset is consistent.
     */
    public boolean isMapsetConsistent( String mapset ) {
        if (mapset == null) {
            return false;
        }
        File mapsetFile = new File(mapset);
        if (!mapsetFile.exists() || !mapsetFile.isDirectory()) {
            return false;
        }
        File f = new File(mapsetFile, JGrassConstants.CELL);
        if (!f.exists())
            return false;
        f = new File(mapsetFile, JGrassConstants.FCELL);
        if (!f.exists())
            return false;
        f = new File(mapsetFile, JGrassConstants.CATS);
        if (!f.exists())
            return false;
        f = new File(mapsetFile, JGrassConstants.COLR);
        if (!f.exists())
            return false;
        f = new File(mapsetFile, JGrassConstants.CELL_MISC);
        if (!f.exists())
            return false;
        f = new File(mapsetFile, JGrassConstants.CELLHD);
        if (!f.exists())
            return false;
        f = new File(mapsetFile, JGrassConstants.WIND);
        if (!f.exists())
            return false;
        return true;
    }

}
