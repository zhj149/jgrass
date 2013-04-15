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

import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractSymbole;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symbol;
import eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs;

/**
 * <p>A symbol for a constant named value.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class SYM_constant_value
	extends AbstractSymbole<SYMs>
	implements Symbol<SYMs> {

// Attributes
	/** The literal constant value. */
	private final String m_value;
	
// Construction
	/**
	 * <p>The constructor <code>SYM_constant_value</code> defines this symbol
	 * object with the specified value.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractSymbole
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
	 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs
	 * @param value
	 * 		- a literal value.
	 */
	public SYM_constant_value( String value ) {

		super( SYMs.SYM_CONSTANT_VALUE );
		m_value = value;
	} // SYM_constant_value
	
// Operations
	/**
	 * <p>Returns the constant value defined by this symbol.</p>
	 * @return
	 * 		Returns the constant value.
	 */
	public String value() {
		
		return m_value;
	} // value
	
} // SYM_constant_value
