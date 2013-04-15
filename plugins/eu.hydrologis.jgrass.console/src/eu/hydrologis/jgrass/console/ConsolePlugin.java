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
package eu.hydrologis.jgrass.console;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.console.core.JGrass;
import eu.hydrologis.jgrass.console.core.runtime.ConsoleEngine;

/**
 * The activator class controls the plug-in life cycle.
 */
public class ConsolePlugin extends AbstractUIPlugin {

    public final static String genericInExchangeItemID = "in"; //$NON-NLS-1$
    public final static String genericOutExchangeItemID = "out"; //$NON-NLS-1$

    // Attributes
    /** The shared <code>JGrass</code> <b>console engine</b> object. */
    private final static JGrass m_console = (JGrass) new ConsoleEngine();

    // The shared instance
    private static ConsolePlugin m_instance;

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.console"; //$NON-NLS-1$

    // Construction
    /**
     * The constructor
     */
    public ConsolePlugin() {
        m_instance = this;
    }

    /**
     * <p>
     * Returns the unique <code>JGrass</code> object associated with the current console editor,
     * if any.
     * </p>
     * 
     * @return The editors <code>JGrass</code> console, if any, otherwise <code>null</code>.
     */
    public static JGrass console() {

        return m_console; // AbstractJGrass.getInstance();
    } // console

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static ConsolePlugin getDefault() {
        return m_instance;
    }

    /**
     * @param message2
     * @param t
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start( BundleContext context ) throws Exception {

        super.start(context);
        m_instance = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop( BundleContext context ) throws Exception {

        m_instance = null;
        super.stop(context);
    }

} // ConsolePlugin
