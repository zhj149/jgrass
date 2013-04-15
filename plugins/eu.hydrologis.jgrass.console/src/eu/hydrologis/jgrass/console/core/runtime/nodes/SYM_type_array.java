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

import eu.hydrologis.jgrass.console.core.internal.nodes.Symbol;
import eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs;

/**
 * <p>A symbol for an array.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class SYM_type_array
	extends SYM_type_primitive
	implements Symbol<SYMs> {

// Construction
	/**
	 * <p>The constructor <code>SYM_type_array</code> defines this symbol
	 * object with the specified qualifier.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractSymbole
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
	 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs
	 * @param qualifier
	 * 		- a qualifier.
	 */
	public SYM_type_array( String qualifier ) {
	
		super( __initialize( qualifier ), SYMs.SYM_TYPE_ARRAY );
	} // SYM_type_array
	
	/**
	 * <p>Helps to initialize the primitive array symbol and puts the postfix
	 * "[]" to the type name specified by the qualifier.</p>
	 * @return
	 * 		Returns the type name for an array definition -
	 * 		<code><i>qualifier</i>[]</code>.
	 */
	private static String __initialize( String qualifier ) {
		
		String retval = null;
		if( null != qualifier ) {
			
			String __type = qualifier.trim();
			if( 0 < __type.length() )
				retval = __type + "[]"; //$NON-NLS-1$
		}
			
		return retval;
	} // __initialize

} // SYM_type_array
