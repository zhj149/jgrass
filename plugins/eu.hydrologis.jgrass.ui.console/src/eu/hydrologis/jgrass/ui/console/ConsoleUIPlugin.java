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
package eu.hydrologis.jgrass.ui.console;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ConsoleUIPlugin extends AbstractUIPlugin {

    // Attributes
    // The shared instance
    private static ConsoleUIPlugin plugin;

    // The plug-in ID
    public final static String PLUGIN_ID = "eu.hydrologis.jgrass.ui.console"; //$NON-NLS-1$

    public static Color COLOR_GRAY;
    public static Color COLOR_RED;
    public static Color COLOR_BLACK;

    // Construction
    /**
     * The constructor
     */
    public ConsoleUIPlugin() {

        plugin = this;

        Display.getDefault().syncExec(new Runnable(){

            public void run() {

                COLOR_GRAY = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
                COLOR_RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);
                COLOR_BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);

            }

        });

    } // ConsoleUIPlugin

    // Operations
    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static ConsoleUIPlugin getDefault() {

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
} // ConsoleUIPlugin
