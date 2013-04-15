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
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.APTs;

/**
 * <p>The <i>abstract parse tree</i> operator of a <b>statement</b> -
 * a model or a command statement.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.APTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class APT_statement
	extends AbstractAPT<APTs>
	implements APT<APTs> {

// Attributes
	/** The external variable. */
	private APT<APTs> m_external_variable;
	
	/** The input/output definitions. */
	private APT<APTs> m_io_definition;
	
	/** The model or command. */
	private APT<APTs> m_model;
	
// Construction
	/**
	 * <p>Constructs this object with the specified operator - interior node - as parent.</p>
	 * @param operator
	 * 		- a <code>APT<APTs></code> object providing the operator - interior node - as parent. 
	 */
	public APT_statement( APT<APTs> operator ) {
		
		super( operator
				, APTs.APT_STATEMENT.expression()
				, APTs.APT_STATEMENT.annotation()
				, APTs.APT_STATEMENT
			);
		try {
			
			if( null == operator )
				throw new IllegalArgumentException();
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
		finally {
			
			m_external_variable = null;
			m_io_definition = null;
			m_model = null;
		}
	} // APT_statement

// Operations	
	/**
	 * <p>Returns the external variable operand, if any, otherwise
	 * <code>null</code>.</p>
	 */
	public APT_variable __external_variable() {
		
		return ( APT_variable )m_external_variable;
	} // __external_variable
	
	/**
	 * <p>Returns the operand of input/output definitions.</p>
	 */
	public APT_io_definition __io_defs() {
		
		return ( APT_io_definition )m_io_definition;
	} // __io_defs
	
	/**
	 * <p>Returns the model or command operand.</p>
	 */
	public APT<APTs> __model() {
		
		return m_model;
	} // __model
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#addChild(APT)
	 */
	public APT<APTs> addChild( APT<APTs> operand ) {
		
		if( null == operand )
			throw new IllegalArgumentException();
		
		switch( operand.identifier() ) {
		case APT_VARIABLE:
			if( null != m_external_variable )
				throw new IllegalArgumentException();
			if( null != m_model )
				throw new IllegalArgumentException();
			return super.addChild(
					m_external_variable = operand
				);
			
		case APT_IO_DEFINITION:
			if( null != m_io_definition )
				throw new IllegalArgumentException();
			if( null == m_model )
				throw new IllegalArgumentException();
			return super.addChild(
					m_io_definition = operand
				);
		
		case APT_JAVA_MODEL:
		case APT_NATIVE_MODEL:
			if( null != m_model )
				throw new IllegalArgumentException();
			return super.addChild(
					m_model = operand
				);
			
		default:
			throw new IllegalArgumentException();
		}
	} // addChild
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#addChild(int,APT)
	 */
	public APT<APTs> addChild( int index, APT<APTs> operand ) {
		
		throw new InternalError();
	} // addChild
		
} // APT_statement