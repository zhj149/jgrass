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
 * <h1>Compiler Error 1004</h1>
 * <h2>Error Message</h2>
 * <p>syntax error : missing 'token' before identifier 'identifier'.</p>
 * <p>The compiler expected <i>identifier</i> and found <i>token</i> instead.
 * Possible causes:<ul type="1"><li>Spelling or capitalization error of a model
 * identifier.</li><li>Missing '$' operator before variable identifier.</li>
 * </ul></p>
 * <p>This error may be caused by a typographical error.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
@SuppressWarnings("serial")
public class E1004
	extends CompilerError {
	
// Construction
	/**
	 * <p>The constructor <code>E1004</code> constructs the message.</p>
	 * @param projectSpace
	 * 		- the current project space.
	 * @param token
	 * 		- the identifier.
	 * @param missing
	 * 		- the missing token.
	 */
	public E1004( Projectspace projectSpace, Token<TOKs> token,
			String missing ) {

		super(
			MessageFormat.format(
					"{0}({1}) : error {2}: syntax error : missing ''{4}'' before identifier ''{3}''."
					, new Object[] {
						projectSpace.projectCaption()
						, token.line()
						, E1004.class.getSimpleName()
						, token.expression()
						, missing
					}
				)
			);
	} // E1004
	
} // E1004
