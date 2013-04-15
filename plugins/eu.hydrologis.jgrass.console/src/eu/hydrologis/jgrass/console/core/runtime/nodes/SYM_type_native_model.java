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
 * <p>A symbol for a <i>GRASS</i> native based command keyword representing a
 * primitive type.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class SYM_type_native_model
	extends SYM_type_primitive
	implements Symbol<SYMs> {
	
// Construction
	/**
	 * <p>The constructor <code>SYM_type_native_model</code> defines this symbol
	 * object with the specified primitive type name - name of the <i>GRASS</i>
	 * command.</p>
	 * @param command
	 * 		- the name of the <i>GRASS</i> command.
	 */
	public SYM_type_native_model( String command ) {
	
		super( command, SYMs.SYM_TYPE_NATIVE_MODEL );
	} // SYM_type_native_model
	
} // SYM_type_native_model
