package eu.hydrologis.jgrass.libs.scripting;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScriptingLibsPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "eu.hydrologis.jgrass.libs.scripting";

	// The shared instance
	private static ScriptingLibsPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ScriptingLibsPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ScriptingLibsPlugin getDefault() {
		return plugin;
	}

}
