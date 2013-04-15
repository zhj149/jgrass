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
 * <p>The class <code>SYM_type_reference</code> provides the implementation of
 * the <code>AbstractSymbol</code> interface for the abstraction of a Java
 * reference type, e.g., <code>eu.hydrologis.jgrass.models.h.flow.h_flow</code>.
 * The developer need only subclasses this abstract class and extend it with
 * self-defined attributes and operations.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractSymbole
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class SYM_type_reference
	extends AbstractSymbole<SYMs>
	implements Symbol<SYMs> {

// Attributes
	/** The signature of the symbol in the symbol table. */
	private final static String signature = "@reference"; //$NON-NLS-1$
	
	/** The name, full qualified, of the primitive type. */
	private final String m_fullQualifiedName;
	
	/** The name of the reference type. */
	private final String m_type;
	
// Construction
	/**
	 * <p>The constructor <code>SYM_type_reference</code> defines this symbol
	 * object with the specified full qualified name and the specified
	 * symbol identifier.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractSymbole
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
	 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs
	 * @param fullQualifiedName
	 * 		- the name, full qualified, of a reference type, e.g.,
	 * 		<code>eu.hydrologis.jgrass.models.h.flow.h_flow</code>.
	 * @param symbole
	 * 		- an abstract symbole representing the kind of stored information.
	 */
	public SYM_type_reference( String fullQualifiedName, SYMs symbole ) {
	
		super( symbole );
		m_fullQualifiedName = fullQualifiedName;
		
		int position = fullQualifiedName.lastIndexOf( "." ); //$NON-NLS-1$
		if( -1 == position ) {
			
			m_type = fullQualifiedName;
		}
		else {
			
			m_type = fullQualifiedName.substring(
					position + 1
				);
		}
	} // SYM_type_reference

// Operations
	/**
	 * <p>Returns the qualifier of the model.</p>
	 */
	public static String qualifier( String type ) {
		
		return type + signature;
	} // qualifier
	
	/**
	 * <p>Returns the full qualified name of the reference type of this
	 * symbol.</p>
	 * @return
	 * 		Returns the full qualified name of the reference type.
	 */
	public String fullQualifiedName() {
		
		return m_fullQualifiedName;
	} // fullQualifiedName
	
	/**
	 * <p>Returns the name of the reference type of this symbol.</p>
	 * @return
	 * 		Returns the name of the reference type.
	 */
	public String type() {
		
		return m_type;
	} // type
	
} // SYM_type_reference
