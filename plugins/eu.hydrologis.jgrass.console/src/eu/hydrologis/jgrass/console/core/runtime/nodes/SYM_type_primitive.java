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
 * <p>The class <code>SYM_type_primitive</code> provides the implementation of
 * the <code>AbstractSymbol</code> interface for the abstraction of a Java
 * primitive type - e.g., <code>int</code>, <code>long</code>,
 * <code>double</code>, <code>boolean</code>. A developer need only subclasses
 * this abstract class and extend it with self-defined attributes and
 * operations.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class SYM_type_primitive
	extends AbstractSymbole<SYMs>
	implements Symbol<SYMs> {

// Attributes
	/** The signature of the symbol in the symbol table. */
	private final static String signature = "@primitive"; //$NON-NLS-1$
	
	/** The name of the primitive type. */
	private final String m_type;
	
// Construction
	/**
	 * <p>The constructor <code>SYM_type_primitive</code> defines this symbol
	 * object with the specified type name and the specified symbol
	 * identifier.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractSymbole
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
	 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs
	 * @param type
	 * 		- a type name.
	 * @param symbole
	 * 		- abstract symbole representing the kind of stored information.
	 */
	public SYM_type_primitive( String type, SYMs symbole ) {
	
		super( symbole );
		m_type = type;
	} // SYM_type_primitive
	
// Operations
	/**
	 * <p>Returns the qualifier of the model.</p>
	 */
	public static String qualifier( String type ) {
		
		return type + signature;
	} // qualifier
	
	/**
	 * <p>Returns the name of the primitive type of this symbol.</p>
	 * @return
	 * 		Returns the name of the primitive type.
	 */
	public String type() {
		
		return m_type;
	} // type

} // SYM_type_primitive
