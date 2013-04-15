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
 * <p>A <i>token</i> is the smallest element of a source program that is
 * meaningful to a translator. A token typically consists of a token name, an
 * attribute value and the information, where the token has been found in
 * the source program – the line number. The token name is an abstract symbol
 * representing a kind of lexical unit, e.g., a particular keyword – sometimes
 * referred to as reserved word – or a sequence of input characters denoting
 * e.g. an identifier, an operator or a delimiter. The token names are the input
 * symbols that the parser process, therefore, we often refer to a instance of
 * a token and to the token name synonymously.</p>
 * <p>The user of the interface <code>Token</code> can get the tokens name by
 * calling the method <code>identifier</code>.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface Token<E> {

// Operations
	/**
	 * <p>The method <code>expression</code> returns a copy of the input
	 * characters that matches the lexeme of this token.</p>
	 * @return
	 * 		Returns the lexeme represented by this token.
	 */
	public abstract String expression();
	
	/**
	 * <p>The method <code>identifier</code> of a token uses the token idendity
	 * scope to identify itself. The identifier of a token, also called token
	 * name, is an abstract symbol representing a kind of lexical unit. However,
	 * the identifier or name of a token should always be a unique lexical unit
	 * and a enumerable value because of all future processing, especially
	 * parsing decisions will typically fall back to the returned token identity
	 * <acronym title="respectively">resp.</acronym> token name information.</p>
	 * <p>The data type of the returned value by the <code>identifier</code> 
	 * method is naturally a constant value of type <code>Integer</code> or a
	 * recommanded custom enumeration type.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme
	 * @return
	 * 		Returns the unique, enumerable tokens identifier of this token
	 * 		object.
	 */
	public abstract E identifier();
	
	/**
	 * <p>The method <code>isWhitespace</code> is the fastest way to detect, if
	 * the method <code>expression</code> of this token object only returns
	 * whitespace character information or a authentic lexeme, without any
	 * whitespace character informations.</p><p>Typically whitespace characters
	 * and also comments are irgnored by a scanner (lexer) because of a
	 * compilers parser builds up a parse tree, where whitespace character
	 * informations and also comments are not needed for syntax analysis.
	 * However, a scanner (lexer) should have the capability to tokenize a
	 * comment or a single whitespace character information, regardless if
	 * comments are of interest or not, or the information about whitespace
	 * characters is needed or not.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token#expression()
	 * @return
	 * 		Returns <code>true</code> if the method <code>expression</code> of
	 * 		this token object holds whitespace character informations,
	 * 		otherwise <code>false</code>.
	 */
	public abstract boolean isWhitespace();
	
	/**
	 * <p>The method <code>line</code> returns at which line number the lexeme
	 * has been detect in the source code.</p>
	 * @return
	 * 		Returns the line number, where this token can be found in
	 * 		the source code.
	 */
	public abstract int line();
	
} // Token
