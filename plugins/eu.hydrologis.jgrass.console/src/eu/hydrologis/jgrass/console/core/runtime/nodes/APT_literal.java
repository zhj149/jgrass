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
package eu.hydrologis.jgrass.console.core.runtime.nodes;

import eu.hydrologis.jgrass.console.core.internal.nodes.APT;
import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT;
import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractToken;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.APTs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;

/**
 * <p>The <i>abstract parse tree</i> operand for a <b>literal string</b>.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.APTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class APT_literal
	extends AbstractAPT<APTs>
	implements APT<APTs> {

// Attributes
	/** The token of the literal string. */
	private final Token<TOKs> m_token;
	
// Construction
	/**
	 * <p>Constructs this object with the specified operator - interior node -
	 * as parent and the specified token.</p>
	 * @param operator
	 * 		- a <code>APT<APTs></code> object providing the operator - interior
	 * 		node - as parent.
	 * @param token
	 * 		- the token.
	 */
	public APT_literal( APT<APTs> operator, Token<TOKs> token ) {
		
		super( operator
				, __cleanSweep( token )
				, APTs.APT_LITERAL.annotation()
				, APTs.APT_LITERAL
			);
		try {
			
			if( null == operator )
				throw new IllegalArgumentException();
			if( null == token )
				throw new IllegalArgumentException();
			
			m_token = token;
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
	} // APT_literal
	
	/**
	 * <p>The method <code>__cleanSweep</code> formats a specified string into
	 * a literal string.</p>
	 * @param token
	 * 		- a token.
	 * @return
	 * 		The literal string.
	 */
	private static String __cleanSweep( Token<TOKs> token ) {
		
		String retval = null;
		try {
			
			// First, getting the tokens 'safe expression' and if the expression
			//	is not equal to 'null', we can move forward to format the
			//	tokens expression in the way we need...
			String expression = AbstractToken.__safe_expression( token );
			if( null != expression ) {
				
				// Creating the literal string buffer by replacing the first
				//	and the last quotation character in the expression with
				//	space character -- leading and trailing whitespace should
				// 	already be omitted...
				final StringBuffer literal = new StringBuffer();
				final int length = expression.length();
				for( int index = 0; index < length; ++index ) {
					
					Character character = expression.charAt( index );
					switch( character.charValue() ) {
                    case '\"':
                    case '\'':
                    	if( index > 0 && index < (length - 1) )
                    		literal.append( character.charValue() );
                        break;
                        
                    default:
                    	literal.append( character.toString() );
					}
				}
				
				retval = new String( literal.toString().trim() );
			}
		}
		catch( Exception e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			e.printStackTrace();
		}
		
		return retval;
	} // __cleanSweep

// Operations
	/**
	 * <p>Returns the token of the literal string.</p>
	 */
	public Token<TOKs> token() {
		
		return m_token;
	} // token
	
} // APT_literal
