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
 * <h1>Compiler Error 2107</h1>
 * <h2>Error Message</h2>
 * <p>'token' unexpected : java model identifier 'identifier' declares no
 * default key.</p>
 * <p></p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
@SuppressWarnings("serial")
public class E2107
	extends CompilerError {

// Construction
	/**
	 * <p>The constructor <code>E1004</code> constructs the message.</p>
	 * @param projectSpace
	 * 		- the current project space.
	 * @param token
	 * 		- the identifier.
	 * @param unexpected
	 * 		- the unexpected token.
	 */
	public E2107( Projectspace projectSpace, Token<TOKs> token,
			Token<TOKs> unexpected ) {

		super(
			MessageFormat.format(
					"{0}({1}) : error {2}: ''{4}'' unexpected : java model identifier ''{3}'' declares no default key."
					, new Object[] {
						projectSpace.projectCaption()
						, token.line()
						, E2107.class.getSimpleName()
						, token.expression()
						, unexpected.expression()
					}
				)
			);
	} // E2107
	
} // E2107
