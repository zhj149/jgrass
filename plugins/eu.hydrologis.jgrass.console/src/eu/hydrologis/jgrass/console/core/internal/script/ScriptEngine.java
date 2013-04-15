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
package eu.hydrologis.jgrass.console.core.internal.script;

import java.io.Reader;

import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p><code>ScriptEngine</code> is the fundamental interface whose methods must
 * be fully functional in every implementation of this specification.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface ScriptEngine {

// Operations
	/**
	 * <p>Same as <code>eval(String)</code> except that the source of the script
	 * is provided as a Reader.</p>
	 * @param reader
	 * 		- the source of the script.
	 * @return
	 * 		The value returned by the script.
	 * @throws RuntimeException
	 * 		- if an ERROR occurrs in script.
	 * @throws NullPointerException
	 * 		- if the argument is null.
	 */
	public abstract Object eval( Reader reader ) throws Exception;
	
	/**
	 * <p>Executes the specified script.</p>
	 * @param string
	 * 		- the source of the script.
	 * @return
	 * 		The value returned by the script.
	 * @throws RuntimeException
	 * 		- if an ERROR occurrs in script.
	 * @throws NullPointerException
	 * 		- if the argument is null.
	 */
	public abstract Object eval( String string ) throws Exception;
	
	/**
	 * <p>The method <code>projectSpace</code> returns the project-space this
	 * script engine is currently using.</p>
	 * @see eu.hydrologis.jgrass.console.core.prefs.Projectspace
	 * @return
	 * 		this script engine's project-space.
	 */
	public abstract Projectspace projectSpace();
	
} // ScriptEngine
