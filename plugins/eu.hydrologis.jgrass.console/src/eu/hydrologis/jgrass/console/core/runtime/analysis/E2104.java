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
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <h1>Compiler Error 2104</h1>
 * <h2>Error Message</h2>
 * <p>unknown java model identifier 'identifier'.</p>
 * <p>The compiler expected a java model <i>identifier</i> and found <i>token</i> instead.
 * Possible causes:<ul type="1"><li>Spelling or capitalization error of a model
 * identifier.</li>
 * <li></li>Missing '$' operator before variable identifier.</ul></p>
 * <p>This error may be caused by a typographical error.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
@SuppressWarnings("serial")
public class E2104
    extends CompilerError {

// Construction
	/**
	 * <p>The constructor <code>E2104</code> constructs the message.</p>
	 * @param projectSpace
	 * 		- the current project space.
	 * @param token
	 * 		- the identifier.
	 */
	public E2104( Projectspace projectSpace, Token<TOKs> token ) {

		super(
			MessageFormat.format(
					"{0}({1}) : error {2}: unknown java model identifier ''{3}''."
					, new Object[] {
						projectSpace.projectCaption()
						, token.line()
						, E2104.class.getSimpleName()
						, token.expression()
					}
				)
			);
	} // E2104
	
} // E2104
