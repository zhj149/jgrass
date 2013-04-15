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
 * <p>The <i>abstract parse tree</i> operand for a identifier of a
 * <b>variable</b>.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.APTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class APT_variable
	extends AbstractAPT<APTs>
	implements APT<APTs> {

// Attributes
	/** The token. */
	private Token<TOKs> m_token;
	
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
	public APT_variable( APT<APTs> operator, Token<TOKs> token ) {
		
		super( operator
				, AbstractToken.__safe_expression( token )
				, APTs.APT_VARIABLE.annotation()
				, APTs.APT_VARIABLE
			);
		try {
	
			if( null == operator )
				throw new IllegalArgumentException();
			if( null == token )
				throw new IllegalArgumentException();
			if( TOKs.VARIABLE != token.identifier() )
				throw new IllegalArgumentException();
			
			m_token = token;
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
	} // APT_variable
	
// Operations
	/**
	 * <p>Returns the token of the external variable.</p>
	 */
	public N_variable token() {
		
		return ( N_variable )m_token;
	} // token
	
	/**
	 * <p>Returns the identifier of the variable.</p>
	 */
	public String variable_name() {
		
		return (( N_variable )m_token).variable_name();
	} // variable_name
	
} // APT_variable
