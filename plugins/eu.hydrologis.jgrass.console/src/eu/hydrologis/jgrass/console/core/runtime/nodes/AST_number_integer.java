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

import eu.hydrologis.jgrass.console.core.internal.nodes.AST;
import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAST;
import eu.hydrologis.jgrass.console.core.runtime.analysis.ASTs;

/**
 * <p>The <i>abstract syntax tree</i> operand represents an <b><i>integer
 * number</i></b>, e.g., "60".</p>
 * <p>Example:<br/><br/><i>Java programming construct:</i><code><pre>
 * i = 60
 * </pre></code>
 * <i><b>Abstract Syntax Tree</b> tree-like intermediate representation:</i>
 * <code><pre>
 * <b>new</b> AST_assign_statement(
 * 	<b>new</b> AST_identifier( "i" )
 * 	, <b>new</b> AST_number_integer( "60" )
 * 	);
 * </pre></code></p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAST
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.ASTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class AST_number_integer
	extends AbstractAST<ASTs>
	implements AST<ASTs> {

// Construction
	/**
	 * <p>Constructs this object with the specified integer number.</p>
	 * @param number
	 * 		- an integer number.
	 */
	public AST_number_integer( String number ) {
	
		super( number
				, ASTs.AST_NUMBER_INTEGER.annotation()
				, ASTs.AST_NUMBER_INTEGER
			);
	} // AST_number_integer

} // AST_number_integer