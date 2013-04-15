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
import eu.hydrologis.jgrass.console.core.runtime.nodes.N_input;
import eu.hydrologis.jgrass.console.core.runtime.nodes.N_output;

/**
 * <h1>Compiler Error 2110</h1>
 * <h2>Error Message</h2>
 * <p>'identifier' : use of undefined quantity 'token'.</p>
 * <p></p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
@SuppressWarnings("serial")
public class E2110
	extends CompilerError {

// Attributes
	private static String m_message = "{0}({1}) : error {2}: ''{3}'' : use of undefined quantity ''{4}''.";
	
// Construction
	/**
	 * <p>The constructor <code>E2110</code> constructs the message.</p>
	 * @param projectSpace
	 * 		- the current project space.
	 * @param identifier
	 * 		- the identifier.
	 * @param token
	 * 		- the undefined quantity token.
	 */
	public E2110( Projectspace projectSpace, Token<TOKs> identifier,
			Token<TOKs> token ) {

		super( __initialize( projectSpace, identifier, token ) );
	} // E2110
	
	/**
	 * <p>The method <code>__initialize</code> constructs the message.</p>
	 * @param projectSpace
	 * 		- the current project space.
	 * @param token
	 * 		- the identifier.
	 * @param token
	 * 		- the undefined quantity token.
	 * @return
	 * 		The message.
	 */
	private static String __initialize( Projectspace projectSpace,
			Token<TOKs> identifier, Token<TOKs> token ) {
		
		final String retval;
		switch( token.identifier() ) {
		case INPUT:
			retval = MessageFormat.format(
					m_message
					, new Object[] {
						projectSpace.projectCaption()
						, identifier.line()
						, E2110.class.getSimpleName()
						, identifier.expression()
						, (( N_input )token).__quantity()
					}
				);
			break;
			
		case OUTPUT:
			retval = MessageFormat.format(
					m_message
					, new Object[] {
						projectSpace.projectCaption()
						, identifier.line()
						, E2110.class.getSimpleName()
						, identifier.expression()
						, (( N_output )token).__quantity()
					}
				);
			break;
			
		default:
			retval = MessageFormat.format(
					m_message
					, new Object[] {
						projectSpace.projectCaption()
						, identifier.line()
						, E2110.class.getSimpleName()
						, identifier.expression()
						, token.expression()
					}
				);
		}
		
		return retval;
	} // __initialize
	
} // E2110
