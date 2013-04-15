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
package eu.hydrologis.jgrass.console.core.internal.compiler;

import java.io.Reader;
import java.io.Writer;

import eu.hydrologis.jgrass.console.core.internal.nodes.APT;
import eu.hydrologis.jgrass.console.core.internal.nodes.AST;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symtable;

/** 
 * <p>A pre-processor is a an entrusted, separate program used by compilers to
 * a) extend the syntax and grammar definition of a programming-language with
 * abbreviations, sometimes referred to as macros, for complex source
 * programming constructs, while others b) have the power of fully-fledged
 * programming languages. <i>Macro expansion</i> is probably the most powerful
 * feature of a pre-processor: The pre-processor expands the macro with its
 * definition into one or many programming constructs based upon the source
 * language and then it replaces the macro with the produced programming
 * constructs. The modified source program is then fed to a translator. The
 * interface <code>Preprocessor</code> provides the basic mechanism based upon
 * concepts of a translator for processing respectively the translation of a
 * self-defined fully-fledged programming-language.</p><p>A translator maps
 * source code, programming constructs of a source program, into semantically
 * equivalent target code; the mapping is roughly divided into two parts:
 * <i>analysis</i> and <i>synthesis</i>. The analysis breaks up the source
 * program into its constituent pieces and imposes a grammatical structure on
 * them and the synthesis part constructs the desired target program from the
 * intermediate representation; the methods of the interface provides basic
 * functionality for the analysis and the functionality of the synthesis
 * part. A concrete implementation uses for the analysis part a lexical analyzer
 * and parser; however, each of them are using abstract symboles,
 * programming-language depended, of different idendity scopes to identify
 * them-self, for this reason the interface is parametrized.</p>
 * <p>A paramter of the interface represents the following identity
 * scope:<br/><table><tbody>
 * <tr>
 * <th>P</th><td>syntax tree idendity scope</td>
 * </tr><tr>
 * <th>X</th><td>parse tree idendity scope</td>
 * </tr><tr>
 * <th>T</th><td>token idendity scope</td>
 * </tr><tr>
 * <th>S</th><td>symbol idendity scope</td>
 * </tr></tbody>
 * </table></p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface Preprocessor<P, X, T, S>
	extends Compiler {

// Operations
	/**
	 * <p>The method <code>generate</code> takes as input a syntax tree of the
	 * target program and maps it into the target language, known as <i>code
	 * generation</i> - the phase of target code generation belongs to the
	 * <i>synthesis</i> part of a translator.</p>
	 * <p>Simply stated the principle, the developer uses the <i>abstract
	 * symbole</i> to evaluate the target programming construct specified by
	 * the syntax tree - either a operator or a operand - currently being
	 * processed and then generates its equivalent target code; writes the
	 * generated code into the target program and then passes the operands -
	 * one at a time - of the specified syntax tree recursively - in case of
	 * an operator has a prefix and a postfix sequence the recursive call is
	 * performed between them.</p>
	 * @param indentCount
	 * 		- counts the tabulator indentions for the generated target code.  
	 * @param op
	 * 		- either an operator or operand of the syntax tree representing a
	 * 		programming construct of the target	code.
	 * @param targetCode
	 * 		- the output respectively the target program, where the generated
	 * 		code is written into.
	 */
	public abstract void generate( int indentCount, AST<P> op,
			Writer targetCode
		);
	
	/**
	 * <p>The method <code>intermediate</code> creates a tree-like intermediate
	 * representation that depicts more or less the grammatical structure of
	 * the input respectively of the source program depending on the
	 * requirements. The transition of a source program into a intermediate
	 * representation is known as <i>analysis</i> part of a translator.</p>
	 * @param symtable
	 * 		- the symbol table.
	 * @param sourceCode
	 * 		- the input respectively the source program.
	 * @param line
	 * 		- the line number in the source program used by this
	 * 		pre-processor as start-point.
	 * @return
	 * 		A tree-like intermediate represantation of the source program
	 * 		being processed by this pre-processor object. 
	 */
	public abstract APT<X> intermediate( Symtable<S> symtable,
			Reader sourceCode, int line
		);
	
	/**
	 * <p>The main task of the method <code>translate</code> is to use the
	 * tree-like intermediate representation and translate it into a equivalent
	 * syntax tree that depicts the programming constructs of the target
	 * language - known as <i>intermediate code generation</i> - the
	 * intermediate code generation belongs to the <i>synthesis</i> part of a
	 * translator.</p>
	 * @param symtable
	 * 		- the symbol table.
	 * @param parseTree
	 * 		- the root of the tree-like intermediate represantation of the
	 * 		source program.
	 * @return
	 * 		A syntax tree that depicts the equivalent programming constructs of
	 * 		the target language in which the intermediate represantation of the
	 * 		source program has been translated.
	 */
	public abstract AST<P> translate( Symtable<S> symtable,
			APT<X> parseTree
		);
	
} // Preprocessor
