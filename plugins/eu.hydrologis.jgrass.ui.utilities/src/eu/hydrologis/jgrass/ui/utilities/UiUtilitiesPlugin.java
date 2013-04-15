package eu.hydrologis.jgrass.ui.utilities;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class UiUtilitiesPlugin extends AbstractUIPlugin {

    // Attributes
    // The shared instance
    private static UiUtilitiesPlugin plugin;

    // The plug-in ID
    public final static String PLUGIN_ID = "eu.hydrologis.jgrass.ui.utilities";

    // Construction
    /**
     * The constructor
     */
    public UiUtilitiesPlugin() {

        plugin = this;
    } // UiUtilitiesPlugin

    // Operations
    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static UiUtilitiesPlugin getDefault() {

        return plugin;
    } // getDefault

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */

    public void start( BundleContext context ) throws Exception {

        super.start(context);
    } // start

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */

    public void stop( BundleContext context ) throws Exception {

        plugin = null;
        super.stop(context);
    } // stop

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
