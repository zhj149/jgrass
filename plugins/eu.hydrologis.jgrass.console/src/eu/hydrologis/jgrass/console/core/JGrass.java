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
package eu.hydrologis.jgrass.console.core;

import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;

/**
 * <p>The interface <code>JGrass</code> of the <i>JGRASS <b>console core</b></i>
 * is a simple abstraction for the interaction with the internal non-visual,
 * character-based <i>command line processor</i>, also known as the <i>JGRASS
 * engine</i>, of the <i>Java based Geographic Resources Analysis Support
 * System</i>. The command line processor is using one, or in the future more
 * than one, third-party supplier scripting engine for primarily the execution
 * of native based (JNI) respectively <i>GRASS</i> commands and the <i>JGRASS</i>
 * Java based, OpenMI compliant models enclosing the ability to execute script
 * files based in the language provided by the script engine. However, any
 * statement is to be passed through the <code>dispatch</code> method provided
 * by this interface.</p>
 * <h3>Use of the Engine</h3><p>The engine is really easy to use. A simple call
 * of the method <code>dispatch</code> provided by the <code>JGrass</code>
 * interface and a command, model or a script file is being immediately executed
 * or only compiled, if specified.</p><p>In general, the developer of a console
 * uses one console engine during the life-cycle of the program. Additionally,
 * the console application must provide the user preferences and map them into
 * a project options object, each time the engine is used to compile or execute
 * a command, model or script file.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface JGrass {

// Operations
	/**
	 * <p>The method <code>dispatch</code> invokes a internal non-visual,
	 * character-based command line interpreter with the specified project
	 * options and the specified command line.</p>
	 * @param projectOptions
	 * 		- the project-options.
	 * @param commandLine
	 * 		- the command enclosing optional pre-processor directives; typically
	 * 		a native based command or either a simple or complex Java based
	 * 		model, or simply the name of a script file.
	 * @see eu.hydrologis.jgrass.console.core.prefs.ProjectOptions
	 * @return
	 * 		Reserved for future use.
	 */
	public abstract Object dispatch( ProjectOptions projectOptions,
			String commandLine
		);
	
} // JGrass