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

import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>The class <code>AbstractScriptEngine</code> provides a standard
 * implementation for the <code>projectSpace</code> method.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractScriptEngine
    implements ScriptEngine {

// Attributes
	/**
	 * The project-space, which is currently in use by this script engine.
	 * @see eu.hydrologis.jgrass.console.core.internal.script.ScriptEngine#projectSpace()
	 */
	private final Projectspace m_projectSpace;
	
// Construction
	/**
	 * <p>The copy constructor <code>AbstractScriptEngine</code> defines this
	 * script engine object using the specified project-space.</p>
	 * @param projectSpace
	 * 		- the project-space.
	 * @throws IllegalArgumentException
	 * 		- if <code>projectSpace</code> references the null type.
	 */
	public AbstractScriptEngine( Projectspace projectSpace ) {
		
		if( null == projectSpace )
			throw new IllegalArgumentException();
		
		m_projectSpace = projectSpace;
	} // AbstractScriptEngine
	
// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.parser.ScriptEngine#projectSpace()
	 */
	public Projectspace projectSpace() {
		
		return m_projectSpace;
	} // projectSpace
	
} // AbstractScriptEngine
