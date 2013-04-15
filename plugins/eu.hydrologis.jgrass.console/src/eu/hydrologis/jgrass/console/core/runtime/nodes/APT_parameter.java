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
import eu.hydrologis.jgrass.console.core.runtime.analysis.APTs;

/**
 * <p>The <i>abstract parse tree</i> operator of an <b>parameter</b> of a
 * command.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.APTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class APT_parameter
    extends AbstractAPT<APTs>
    implements APT<APTs> {

// Construction
	/**
	 * <p>Constructs this object with the specified operator - interior node - as parent.</p>
	 * @param operator
	 * 		- a <code>APT<APTs></code> object providing the operator - interior node - as parent. 
	 */
	public APT_parameter( APT<APTs> operator ) {
		
		super( operator
				, APTs.APT_PARAMETER.expression()
				, APTs.APT_PARAMETER.annotation()
				, APTs.APT_PARAMETER
			);
	} // APT_parameter
	
// Operations
	/**
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT#addChild(int,APT)
	 */
	public APT<APTs> addChild( APT<APTs> operand ) {
		
		if( null == operand )
			throw new IllegalArgumentException();
		
		return super.addChild( operand );
	} // addChild
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#addChild(int,APT)
	 */

	public APT<APTs> addChild( int index, APT<APTs> operand ) {
		
		if( null == operand )
			throw new IllegalArgumentException();
		
		return super.addChild( index, operand );
	} // addChild
	
} // APT_parameter
