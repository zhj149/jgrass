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
package eu.hydrologis.jgrass.console.core.runtime.analysis;

import java.text.MessageFormat;

import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerError;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <h1>Fatal Error 1001</h1>
 * <h2>Error Message</h2>
 * <p>unexpected end of statement in macro expansion.</p>
 * <p>The compiler reached the end of a source file without resolving a
 * construct. The code may be missing one of the following elements:
 * <ul type="1"><li>A closing brace.</li><li>A closing parenthesis.</li><li>A
 * closing comment marker.</li><li>A semicolon.</li></ul></p> 
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
@SuppressWarnings("serial")
public class E1001
	extends CompilerError {

// Construction
	/**
	 * <p>The constructor <code>E1001</code> constructs the message.</p>
	 * @param projectSpace
	 * 		- the current project space.
	 * @param line
	 * 		- the last line number.
	 */
	public E1001( Projectspace projectSpace, int line ) {

		super(
			MessageFormat.format(
					"{0}({1}) : error {2}: unexpected end of statement in macro expansion."
					, new Object[] {
						projectSpace.projectCaption()
						, line
						, E1001.class.getSimpleName()
					}
				)
			);
	} // E1001
	
} // E1001
