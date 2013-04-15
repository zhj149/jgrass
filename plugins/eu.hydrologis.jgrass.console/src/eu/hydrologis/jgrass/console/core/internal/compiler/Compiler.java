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
package eu.hydrologis.jgrass.console.core.internal.compiler;

import java.io.Reader;

import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>A compiler is a software system respectively a computer program, which
 * maps respectively can read a program in one language – the source language –
 * and translate it into an equivalent program in another language – the target
 * language. The mapping is roughly divided into two parts: <i>analysis</i> and
 * synthesis - the part analysis is often called the <i>front end</i>; the
 * synthesis part is often called the <i>back end</i>.</p><p>The interface
 * <code>Compiler</code> provides a basic approach of a translator.</p>
 * <p>A paramter of the interface represents the following identity
 * scope:<br/><table><tbody>
 * <tr>
 * <th>T</th><td>token idendity scope</td>
 * </tr><tr>
 * <th>S</th><td>symbol idendity scope</td>
 * </tr></tbody>
 * </table></p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface Compiler {

// Operations
	/**
	 * <p>Compiles the specified source program respectively the specified
	 * source code.</p>
	 * @param sourceCode
	 * 		- the input respectively the source program.
	 * @param line
	 * 		- the line number in the source program used by this compiler
	 * 		as start-point.
	 * @return
	 * 		The target code respectively the target program translated by this
	 * 		compiler.
	 */
	public abstract Reader compile( Reader sourceCode, int line )
		throws Exception;
	
	/**
	 * <p>The method <code>projectSpace</code> returns the project-space this
	 * compiler is currently using.</p>
	 * @see eu.hydrologis.jgrass.console.core.prefs.Projectspace
	 * @return
	 * 		this compiler's project-space.
	 */
	public abstract Projectspace projectSpace();
	
} // Compiler
