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
 * <p>The <i>abstract parse tree</i> operand for a <b>model</b>.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.APTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class APT_java_model
	extends AbstractAPT<APTs>
	implements APT<APTs> {

// Attributes
	/** The arguments of this linkable component. */
	private APT<APTs> m_argument_defs;
	
	/** The token of the model flag. */
	private Token<TOKs> m_token;
	
	/**
	 * The variable definition of the link object of this linkable
	 * component.
	 */
	private APT<APTs> m_link_variable_def;
	
	/** The variable definition of this linkable component object. */
	private APT<APTs> m_linkable_variable_def;
	
	/**
	 * The usage operator is used in the statement <code>true</code>, otherwise
	 * <code>false</code>.
	 */
	public boolean m_usage;
	
// Construction
	/**
	 * <p>Constructs this object with the specified operator - interior node -
	 * as parent and with the other required informations.</p>
	 * @param operator
	 * 		- a <code>APT<APTs></code> object providing the operator - interior
	 * 		node - as parent.
	 * @param token
	 * 		- the input token.
	 * @param linkable_variable_name
	 * 		- a variable name for this linkable component object.
	 * @param linkable_type_name
	 * 		- the type of this linkable component.
	 * @param link_variable_name
	 * 		- the variable name for the link object of this linkable component.
	 * @param link_type_name
	 * 		- the type of the link.
	 * @throws IllegalArgumentException
	 * 		- if one of the parameter references the <code>null</code> type.
	 */
	public APT_java_model( APT<APTs> operator, Token<TOKs> token,
			String linkable_variable_name, String linkable_type_name,
			String link_variable_name, String link_type_name ) {
		
		super( operator
				, AbstractToken.__safe_expression( token )
				, APTs.APT_JAVA_MODEL.annotation()
				, APTs.APT_JAVA_MODEL
			);
		try {
		
			final String __safe_expression = AbstractToken.__safe_expression(
					token
				);
			if( null == __safe_expression )
				throw new IllegalArgumentException();
			if( TOKs.JAVA_MODEL != token.identifier() )
				throw new IllegalArgumentException();
			if( null == operator )
				throw new IllegalArgumentException();
			if( null == linkable_variable_name )
				throw new IllegalArgumentException();
			if( null == linkable_type_name )
				throw new IllegalArgumentException();
			if( null != link_variable_name ) {
				
				if( null == link_type_name )
					throw new IllegalArgumentException();
			}
			
			m_argument_defs = new APT_argument_definition( this );
			m_token = token;
			if( null != link_variable_name )
				m_link_variable_def = new APT_variable_definition(
						this
						, link_variable_name
						, link_type_name
					);
			m_linkable_variable_def = new APT_variable_definition(
					this
					, linkable_variable_name
					, linkable_type_name
				);
			m_usage = false;
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
	} // APT_java_model
	
// Operations
	/**
	 * <p>Returns the operand of argument definitions for this linkable input
	 * component.</p>
	 */
	public APT_argument_definition argument_defs() {
		
		return ( APT_argument_definition )m_argument_defs;
	} // argument_defs
	
	/**
	 * <p>Returns the variable definition operand of the link object for
	 * this linkable component.</p>
	 */
	public APT_variable_definition link_variable_def() {
		
		return ( APT_variable_definition )m_link_variable_def;
	} // link_variable_def
	
	/**
	 * <p>Returns the variable definition of this linkable component object.</p>
	 */
	public APT_variable_definition linkable_variable_def() {
		
		return ( APT_variable_definition )m_linkable_variable_def;
	} // linkable_variable_def
	
	/**
	 * <p>Returns the token of the model.</p>
	 */
	public N_java_model token() {
	
		return ( N_java_model )m_token;
	} // token
	
} // APT_java_model