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
package eu.hydrologis.jgrass.console.core.internal.parser;

import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerError;
import eu.hydrologis.jgrass.console.core.internal.analysis.MessageCode;
import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerWarning;
import eu.hydrologis.jgrass.console.core.internal.nodes.APT;
import eu.hydrologis.jgrass.console.core.internal.nodes.Production;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symtable;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>The interface <code>Parser</code> provides the mechanism of a compiler's
 * second phase, called <i>syntax analysis</i> or <i>parsing</i>. The parser
 * uses syntactic units – tokens – to create a tree-like intermediate
 * representation that depicts the grammatical structure of the obtained token
 * stream from the lexical analyzer. A typical representation of the grammatical
 * structure of a source program is a syntax tree in which each interior node
 * represents an operation and the children of the node represents the arguments
 * of the operation.</p><p>After the syntax analysis, the syntax tree is passed
 * to the subsequent phase, the semantic analysis. The semantic analyzer uses
 * the syntax tree and the information in the symbol table to check the source
 * program for semantic consistency with the language definition. It also
 * gathers type information and saves it in either the syntax tree or the symbol
 * table, for subsequent use during intermediate code generation.</p><p>However,
 * this parser analysis the syntax and semantic at once and then uses the tokens
 * to create a tree-like intermediate representation. Instead of creating a
 * syntax tree that depicts the grammatical structure, it creates a tree-like
 * intermediate representation that depicts a mix out of the grammatical
 * structure and the grouped arguments of a command.</p><p>The subsequent phase,
 * the intermediate code generation, uses the tree-like intermediate
 * representation produced by the parser and translate it into a equivalent
 * syntax tree that depicts the programming constructs of the target language.
 * The syntax tree is passed to the code generator.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Production
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symtable
 * @see eu.hydrologis.jgrass.console.core.prefs.Projectspace 
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface Parser<P, X, S> {

// Operations
	/**
	 * <p>The method <code>analyse</code> does the <i>analysis</i> (syntax,
	 * semantic) during the <i>parsing</i> phase for the translator. The main
	 * task is to analyze the token stream: to check for syntactical errors
	 * in the token stream, to check for statemant completeness and to
	 * check for semantic consistency with the language definition using the
	 * parse tree and the informations in the symbole table.</p>
	 * @param symtable
	 * 		- the symbol table.
	 * @param parseTree
	 * 		- the parse tree.
	 * @param tstream
	 * 		- the token stream to analyse.
	 * @throws CompilerWarning
	 * @throws CompilerError
	 * @return
	 * 		A object of analysis success (success, continue, ignore
	 * 		and continue) or either one of error or warning.
	 */
	public abstract MessageCode analyse( Symtable<S> symtable,
			APT<P> parseTree, Token<X>... tstream
		) throws CompilerWarning, CompilerError;
	
	/**
	 * <p>Parses the source program. The method <code>parse</code> uses the
	 * lexical analyzer, which is in use by this parser, to obtain the token
	 * stream that it processes and produces with the help of productions as
	 * output the tree-like intermediate representation of the source
	 * programming constructs.</p>
	 * @param symtable
	 * 		- the symbole table.
	 * @param parseTree
	 * 		- the root of the parse tree.
	 * @return
	 * 		The tree-like intermediate represantion of the source program.
	 */
	public abstract APT<P> parse( final Symtable<S> symtable,
			final APT<P> parseTree
		) throws Exception;
	
	/**
	 * <p>The method <code>productions</code> returns the set of productions.</p>
	 * @return
	 * 		Returns the set of productions for the grammer that this parser
	 * 		analyze.
	 */
	public abstract Production<P, X, S>[] productions();
	
	/**
	 * <p>The method <code>projectSpace</code> returns the project-space this
	 * syntax analyzer (parser) is currently using.</p>
	 * @see eu.hydrologis.jgrass.console.core.prefs.Projectspace
	 * @return
	 * 		this syntax analyzer's (parser's) project-space.
	 */
	public abstract Projectspace projectSpace();
	
} // Parser
