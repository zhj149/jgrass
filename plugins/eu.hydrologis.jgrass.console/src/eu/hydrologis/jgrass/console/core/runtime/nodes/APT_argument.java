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
 * <p>The <i>abstract parse tree</i> operator of an <b>argument</b> of a Java
 * based model statement consisting of two operands: at the left-side the
 * argument flag (key) and on the right-side, the argument value.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.APTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class APT_argument
	extends AbstractAPT<APTs>
	implements APT<APTs> {

// Attributes
	/** The argument flag operand. */
	private APT<APTs> m_flag;
	
	/** The argument value operand. */
	private APT<APTs> m_value;

// Construction
	/**
	 * <p>Constructs this object with the specified operator - interior node - as parent.</p>
	 * @param operator
	 * 		- a <code>APT<APTs></code> object providing the operator - interior node - as parent. 
	 */
	public APT_argument( APT<APTs> operator ) {
		
		super( operator
				, APTs.APT_ARGUMENT.expression()
				, APTs.APT_ARGUMENT.annotation()
				, APTs.APT_ARGUMENT
			);
		try {
		
			if( null == operator )
				throw new IllegalArgumentException();
			
			m_flag  = null;
			m_value = null;
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
	} // APT_argument

// Operations	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#addChild(APT)
	 */
	public APT<APTs> addChild( APT<APTs> operand ) {
		
		if( null == operand )
			throw new IllegalArgumentException();
		
		switch( size() ) {
		case 0:
			if( null != m_flag )
				throw new IllegalArgumentException();
			
			return super.addChild( m_flag = operand );
			
		case 1:
			if( null != m_value )
				throw new IllegalArgumentException();
			if( null == m_flag )
				throw new IllegalArgumentException();
			
			return super.addChild( m_value = operand );
		
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
	
	/**
	 * <p>Returns the argument flag operand.</p>
	 */
	public APT<APTs> flag() {
		
		return m_flag;
	} // flag
	
	/**
	 * <p>Returns the argument value operand.</p>
	 */
	public APT<APTs> value() {
		
		return m_value;
	} // value

} // APT_argument