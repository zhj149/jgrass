package eu.hydrologis.jgrass.netcdf;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ucar.nc2.dataset.NetcdfDataset;

/**
 * The activator class controls the plug-in life cycle
 */
public class NetcdfPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.netcdf";

    // The shared instance
    private static NetcdfPlugin plugin;

    /**
     * The constructor
     */
    public NetcdfPlugin() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
    public static NetcdfPlugin getDefault() {
        return plugin;
    }

    /**
     * Logs the Throwable in the plugin's log.
     * <p>
     * This will be a user visable ERROR if:
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
    
    
    private NetcdfDataset netcdfDataset;

    public void setNetcdfDataset(NetcdfDataset netcdfDataset){
        this.netcdfDataset = netcdfDataset;
    }
    
    public NetcdfDataset getNetcdfDataset() {
        return netcdfDataset;
    }
}
