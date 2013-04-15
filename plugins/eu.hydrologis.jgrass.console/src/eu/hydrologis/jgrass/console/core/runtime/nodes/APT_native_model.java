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
 * <p>The <i>abstract parse tree</i> operand for a <b>command</b>.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.APTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class APT_native_model
	extends AbstractAPT<APTs>
	implements APT<APTs> {

// Attributes
	/** The parameters of this command. */
	private APT<APTs> m_paramdefs;
	
	/** The token of the command. */
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
	public APT_native_model( APT<APTs> operator, Token<TOKs> token ) {
		
		super( operator
				, AbstractToken.__safe_expression( token )
				, APTs.APT_NATIVE_MODEL.annotation()
				, APTs.APT_NATIVE_MODEL
			);
		try {
			
			final String __safe_expression = AbstractToken.__safe_expression(
					token
				);
			if( null == __safe_expression )
				throw new IllegalArgumentException();
			if( TOKs.NATIVE_MODEL != token.identifier() )
				throw new IllegalArgumentException();
			if( null == operator )
				throw new IllegalArgumentException();
			
			m_token = token;
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
		finally {
			
			m_paramdefs = null;
		}
	} // APT_java_model
	
// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#addChild(APT)
	 */
	public APT<APTs> addChild( APT<APTs> operand ) {
		
		if( null == operand )
			throw new IllegalArgumentException();
		if( operand.identifier() != APTs.APT_PARAMETER_DEFINITION )
			throw new IllegalArgumentException();
		if( 0 < size() )
			throw new IllegalArgumentException();
		
		return super.addChild( m_paramdefs = operand );
	} // addChild
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#addChild(int,APT)
	 */
	public APT<APTs> addChild( int index, APT<APTs> operand ) {
		
		throw new InternalError();
	} // addChild
	
	/**
	 * <p>Returns the operand of parameter definitions for this command.</p>
	 */
	public APT_parameter_definition parameter_defs() {
	
		return ( APT_parameter_definition )m_paramdefs;
	} // parameter_defs
	
	/**
	 * <p>Returns the token of the command.</p>
	 */
	public N_native_model token() {
	
		return ( N_native_model )m_token;
	} // token
	
} // APT_java_model