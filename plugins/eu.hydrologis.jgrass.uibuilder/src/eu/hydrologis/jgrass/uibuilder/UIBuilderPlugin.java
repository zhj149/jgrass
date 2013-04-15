/*
 * UIBuilder - a framework to build user interfaces out from XML files
 * Copyright (C) 2007-2008 Patrick Ohnewein
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

package eu.hydrologis.jgrass.uibuilder;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Patrick Ohnewein
 */
public class UIBuilderPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.uibuilder"; //$NON-NLS-1$

    // The shared instance
    private static UIBuilderPlugin plugin;

    /**
     * The constructor
     */
    public UIBuilderPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
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
    public static UIBuilderPlugin getDefault() {
        return plugin;
    }

    public static void log( String logMessage, Throwable t ) {
        if (getDefault() == null) {
            t.printStackTrace();
            return;
        }
        String message = (logMessage == null ? "" : logMessage);
        int status = t instanceof Exception || message != null ? IStatus.ERROR : IStatus.WARNING;
        UIBuilderPlugin plugin = getDefault();
        ILog logger = plugin == null ? null : plugin.getLog();
        if (logger != null)
            logger.log(new Status(status, PLUGIN_ID, IStatus.OK, message, t));
        else {
            System.err.println(logMessage);
            t.printStackTrace(System.err);
        }
    }
}
