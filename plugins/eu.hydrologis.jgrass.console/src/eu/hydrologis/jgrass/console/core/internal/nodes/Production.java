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
package eu.hydrologis.jgrass.console.core.internal.nodes;

/**
 * <p>The interface <code>Production</code> is based upon a component of a
 * notation - the <i>context-free grammar</i> or <i>grammar</i> for short -
 * that is used to specify the syntax of a language. A context-free grammar
 * consists of four components:<ul>
 * <li>A set of <i>terminal symbols</i>, sometimes referred to as
 * <i>&quot;tokens&quot;</i>. The terminals are the elementary symbols of
 * the language defined by the grammar.</li>
 * <li>A set of <i>nonterminals</i>, sometimes called <i>&quot;syntactic
 * variables&quot;</i>. Each nonterminal represents a set of strings of
 * terminals, in a manner which has to be described.</li>
 * <li>A set of <i>productions</i>, where each production consists of a
 * nonterminal, called the <i>head</i> or <i>left side</i> of the production,
 * an arrow, and a sequence of terminals and/or nonterminals, called the
 * <i>body</i> or <i>right side</i> of the production. The intuitive intent of
 * a production is to specify one of the written forms of a construct; if the
 * head nonterminal represents a construct, then the body represents a written
 * form of the construct.</li>
 * <li>A designation of one of the nonterminals as the start symbol.</li>
 * </ul></p>
 * <p>A grammar is specified by listing their productions. For notational
 * convenience, we assume that:<ul>
 * <li>a digit, a sign, a boldface string, a non-italicized name or symbol is
 * a terminal.</i>
 * <li>an italicized name is a nonterminal.</li>
 * <li>productions with the same nonterminal as head can have their bodies
 * grouped, with the alternative bodies separated by the symbol |, which is
 * read as logical &quot;or&quot;.</li>
 * </ul></p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface Production<P, X, S> {

// Operations
	/**
	 * <p>The method <code>produces</code> returns one or more alternative
	 * sequences of terminals and/or nonterminals describing a construct of
	 * the grammar, which is being translatable by this production.</p>
	 * @param parseTree
	 * 		- the parse tree.
	 * @return
	 * 		One or more alternative sequences of terminals and/or nonterminals
	 * 		describing a construct of the grammar, which is being translatable
	 * 		by this production.
	 */
	public abstract X[][] produces(	APT<P> parseTree );
	
	/**
	 * <p>The method <code>translate</code> does the translation of the incoming
	 * token stream.</p>
	 * @param symtable
	 * 		- symbol table.
	 * @param parseTree
	 * 		- parent node.
	 * @param tstream
	 * 		- the incoming token stream.
	 * @return
	 * 		The operator for a subsequent production; a interior node, used as
	 * 		the root for a tree-like represenatation of a subsequent
	 * 		programming construct.
	 */
	public abstract APT<P> translate( Symtable<S> symtable,
			APT<P> parseTree, Token<X>[] tstream
		);
		
} // Production
