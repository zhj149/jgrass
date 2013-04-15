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
package eu.hydrologis.jgrass.console.core.internal.lexer;

import java.io.IOException;
import java.io.Reader;

import eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>The interface <code>Lexer</code> provides the mechanism of a compiler's
 * or respectively an interpreter's first phase, called <i>lexical analysis</i>
 * or <i>scanning</i>. The main task of the lexical analyzer is to read the
 * stream of characters making up the source program, groups them into
 * meaningful sequences – called <i>lexemes</i> – and produces for each of them
 * as output a token. The stream of tokens it passes on to the subsequent phase,
 * the syntax analysis.</p><p><b>Tokens</b><br/> A <i>token</i> is the smallest
 * element of a source program that is meaningful to a language processor. A
 * token typically consists of a token name, an attribute value and the
 * information, where the token has been found in the source program – the line
 * number. The token name is an abstract symbol representing a kind of lexical
 * unit, e.g., a particular keyword – sometimes referred to as reserved word –
 * or a sequence of input characters denoting e.g. an identifier, an operator or
 * a delimiter.</p><p><b>Patterns</b><br/>A <i>pattern</i> describes the form of
 * a lexical unit that the lexemes of a token may take. In the case of a
 * keyword, the pattern is just the sequence of characters that form the word,
 * and then again for other tokens, a more complex structure that is matched by
 * many strings.</p><p><b>Lexemes</b><br/>A <i>lexeme</i> is a sequence of input
 * characters that matches the pattern of a lexical unit for a token.</p>
 * <p><b>Punctuators</b><br/>A <i>punctuator</i> is a character or a sequence of
 * characters that matches either a punctuation mark or an operator of the
 * programming language - e.g. bracket, dot, comma, asterisk.</p><p>A scanner
 * primarily needs a set of lexemes, each of them consisting of a pattern, a
 * regular expression, describing the lexeme itself and the information of its
 * analogous token: a set of <code>Lexeme</code>/<code>AbstractLexeme</code>
 * objects. A <code>Lexeme</code>/<code>AbstractLexeme</code> object, each of
 * them in theory, provides a self-defined analysis method, which is then called
 * by the scanner to match the given input character sequence against the
 * pattern; also provided is the production of a analogous token, whereby
 * production means, the transition from lexeme, the given input character
 * sequence, into a token.</p><p>However, a developer first creates the token
 * identity scope based on the syntax definition, before he starts to subclass
 * the abstract class <code>AbstractToken</code>, <code>AbstractLexeme</code>,
 * for each lexical unit in the token identity scope. The developer only need
 * then subclass the abstract class <code>AbstractLexer</code> and define the
 * characters being treat as white-space or punctuation by the scanner, to
 * define the set of lexemes and an optional lexeme, used to produce a token as
 * output, in the case of the lexical analysis fails; however, a token uses an
 * abstract symbol of the token identity scope to identify themselves, for this
 * reason the interface is parameterized.</p>
 * <p>A parameter of the interface represents the following identity
 * scope:<br/><table><tbody>
 * <tr>
 * <th>X</th><td>token identity scope</td>
 * </tr></tbody>
 * </table></p>
 * @see eu.hydrologis.jgrass.console.core.internal.parser.Parser
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface Lexer<X> {
	
// Operations
	/**
	 * <p>The method <code>blockRead</code> reads out a part of the source
	 * program, beginning at the first occurence of a specified start symbole
	 * and ends at the occurence of a specified stop symbole. The method uses
	 * the parenthesis syntax, therefore, it reads out nested occurences.</p>
	 * @param t_start
	 * 		- the start symbol.
	 * @param t_end
	 * 		- the stop symbol.
	 * @return
	 * 		The read out part of the source program, if any occurence has been
	 * 		during the scan, otherwise <code>null</code>. 
	 */
	public abstract Reader blockRead( X t_start, X t_end
		) throws IOException;
	
	/**
	 * <p>The method <code>lexemes</code> returns the set of available lexemes,
	 * which can be used by the lexical analyzer (scanner) to produce a
	 * associated token.</p>
	 * @return
	 * 		The set of lexemes.
	 */
	public abstract Lexeme<X>[] lexemes();
	
	/**
	 * <p>The method <code>line</code> returns the current line number being
	 * processed in the source program by this lexical analyzer.</p>
	 * @return
	 * 		The current line number being processed by this lexical analyzer.
	 */
	public abstract int line();
	
	/**
	 * <p>The method <code>projectSpace</code> returns the project-space this
	 * lexical analyzer (scanner) is currently using.</p>
	 * @see eu.hydrologis.jgrass.console.core.prefs.Projectspace
	 * @return
	 * 		this lexical analyzer's (scanner's) project-space.
	 */
	public abstract Projectspace projectSpace();
	
	/**
	 * <p>The method <code>punctuators</code> returns a set of characters that
	 * the lexical analyzer (scanner) treat as delimiters during the lexical
	 * analysis phase. A token is delimited naturally by a whitespace character,
	 * but also by punctuators (punctuation mark or operator) of the programming
	 * language, which are defined by the returned set of punctuators by this
	 * method.</p>
	 * @return
	 * 		A set of characters, each one to be treat as punctuator (punctuation
	 * 		mark or operator) by the scanner.
	 */
	public abstract Character[] punctuators();
	
	/**
	 * <p>The method <code>rescue</code> returns an optional
	 * <code>lexeme</code> object, used to produce a token as output, in the
	 * case of the lexical analysis fails.</p>
	 * @return
	 * 		A <code>lexeme</code> object, if any, otherwise <code>null</code>.
	 */
	public abstract Lexeme<X> rescue();
	
	/**
	 * <p>The method <code>tokenized</code> returns the token stream
	 * incrementally collected during the lexical analysis phase.</p>
	 * @return
	 * 		The current token stream, if this method is called when the lexical
	 * 		analysis has not yet completed, otherwise the complete token stream
	 * 		produced by this lexical analyzer (scanner) during the lexical
	 * 		analysis.
	 */
	public abstract Token<X>[] tokenized();
	
	/**
	 * <p>The method <code>tokscn</code> scans for another token and returns it,
	 * if this lexical analyzer (scanner) has another token in its input.</p>
	 * @return
	 * 		Returns another token object if and only if this lexical analyzer
	 * 		(scanner) has another token, otherwise <code>null</code>.  
	 */
	public abstract Token<X> tokscn();
	
	/**
	 * <p>The method <code>tokskip</code> is untypical for a lexical analyzer;
	 * however, the methods return value tells the lexical analyzer either to
	 * skip and to continue the lexical analysis or to pass the specified token
	 * as output to the parser.</p>
	 * @param token
	 * 		- a token object.
	 * @return
	 * 		If <code>true</code> the lexical analyzer skips the specified token
	 * 		and continue the lexical analysis, otherwise <code>false</code> the
	 * 		lexical analyzer passes the specified token as output to the parser.
	 */
	public abstract boolean tokskip( X token );
	
} // Lexer
