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
 * <p>A symbol for a reference type - e.g. a class.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class SYM_type_class
	extends SYM_type_reference
	implements Symbol<SYMs> {

//Construction
	/**
	 * <p>The constructor <code>SYM_type_class</code> defines this symbol
	 * object according to the Java class reference type with the specified
	 * full qualified name.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractSymbole
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
	 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs
	 * @param fullQualifiedName
	 * 		- the name, full qualified, of a reference type, e.g.,
	 * 		<code>eu.hydrologis.jgrass.models.h.flow.h_flow</code>.
	 */
	public SYM_type_class( String fullQualifiedName ) {
	
		super( fullQualifiedName, SYMs.SYM_TYPE_CLASS );
	} // SYM_type_class

} // SYM_type_class
