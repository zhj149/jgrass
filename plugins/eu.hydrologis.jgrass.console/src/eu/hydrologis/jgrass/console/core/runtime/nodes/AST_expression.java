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
 * <p>The <i>abstract syntax tree</i> operator for an <b><i>expression</i></b>
 * punctuation respectively of a <b><i>statement programming construct</i></b>,
 * e.g., "oparand1<b>;</b> operand2<b>;</b> ...".</p>
 * <p>Example:<br/><br/><i>Java programming construct:</i><code><pre>
 * System.out.printf( "Hello World" );
 * </pre></code>
 * <i><b>Abstract Syntax Tree</b> tree-like intermediate representation:</i>
 * <code><pre>
 * <b>new</b> AST_expression(
 * 	<b>new</b> AST_method_call(
 * 		<b>new</b> AST_dot(
 * 			<b>new</b> AST_identifier( "System" )
 * 			, <b>new</b> AST_identifier( "out" )
 * 			, <b>new</b> AST_identifier( "printf" )
 * 			)
 * 		, <b>new</b> AST_elist( <b>new</b> AST_literal( "Hello World" ) )
 * 		)
 * 	);
 * </pre></code></p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAST
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.ASTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class AST_expression
    extends AbstractAST<ASTs>
    implements AST<ASTs> {

// Construction
	/**
	 * <p>Constructs this object with the specified operands.</p>
	 * @param operands
	 * 		- can be <code>null</code> or either a single operand or a list of
	 * 		operands.
	 */
	public AST_expression( AST<ASTs>... operands ) {
	
		super( ASTs.AST_EXPRESSION.expression()
				, ASTs.AST_EXPRESSION.annotation()
				, ASTs.AST_EXPRESSION
				, operands
			);
	} // AST_expression
	
} // AST_expression
