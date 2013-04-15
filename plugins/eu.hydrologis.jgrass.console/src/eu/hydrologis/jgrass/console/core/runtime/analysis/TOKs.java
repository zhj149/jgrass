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
package eu.hydrologis.jgrass.console.core.runtime.analysis;

import eu.hydrologis.jgrass.console.core.internal.analysis.Tokens;

/**
 * <p>The enumeration type <code>TOKs</code> defines the named constants to
 * identify the lexical unit of a token by its named constant - this is the
 * tokens <i>identity scope</i> - instead to identify the lexical unit using
 * the <code>instanceof</code> operator.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public enum TOKs implements Tokens {
	
	/**
	 * <p>The named constant <code>ARGUMENT</code> identifies the token as an
	 * argument flag (key) expression of a Java-based model.</p>
	 */
	ARGUMENT( Tokens.keyword ),
	
	/**
	 * <p>The named constant <code>CHARACTER_ASSIGN</code> identifies the token
	 * as an operator representing the assign character.</p>
	 */
	CHARACTER_ASSIGN( Tokens.operator ),
	
	/**
	 * <p>The named constant <code>CHARACTER_ASTERISK</code> identifies the
	 * token as an operator representing the asterisk character.</p>
	 */
	CHARACTER_ASTERISK( Tokens.operator ),
	
	/**
	 * <p>The named constant <code>CHARACTER_BACKSPACE</code> identifies the
	 * token as whitespace representing the backspace character.</p>
	 */
	CHARACTER_BACKSPACE( Tokens.whitespace ),
	
	/**
	 * <p>The named constant <code>CHARACTER_BLANK</code> identifies the
	 * token as whitespace representing the space character.</p>
	 */
	CHARACTER_BLANK( Tokens.whitespace ),
	
	/**
	 * <p>The named constant <code>CHARACTER_BRACE_CLOSE</code> identifies the
	 * token as delimiter representing the closing curly bracket character.</p>
	 */
	CHARACTER_BRACE_CLOSE( Tokens.punctuator ),
	
	/**
	 * <p>The named constant <code>CHARACTER_BRACE_OPEN</code> identifies the
	 * token as delimiter representing the opening curly bracket character.</p>
	 */
	CHARACTER_BRACE_OPEN( Tokens.punctuator ),
	
	/**
	 * <p>The named constant <code>CHARACTER_BRACKET_CLOSE</code> identifies the
	 * token as delimiter representing the closing square bracket character.</p>
	 */
	CHARACTER_BRACKET_CLOSE( Tokens.punctuator ),
	
	/**
	 * <p>The named constant <code>CHARACTER_BRACKET_OPEN</code> identifies the
	 * token as delimiter representing the opening square bracket character.</p>
	 */
	CHARACTER_BRACKET_OPEN( Tokens.punctuator ),
	
	/**
	 * <p>The named constant <code>CHARACTER_CARRIAGE_RETURN</code> identifies
	 * the token as whitespace representing the carriage return character.</p>
	 */
	CHARACTER_CARRIAGE_RETURN( Tokens.whitespace ),
	
	/**
	 * <p>The named constant <code>CHARACTER_FORM_FEED</code> identifies the
	 * token as whitespace representing the form feed character.</p>
	 */
	CHARACTER_FORM_FEED( Tokens.whitespace ),
	
	/**
	 * <p>The named constant <code>CHARACTER_LINE_FEED</code> identifies the
	 * token as whitespace representing the line feed character.</p>
	 */
	CHARACTER_LINE_FEED( Tokens.whitespace ),
	
	/**
	 * <p>The named constant <code>CHARACTER_PRENS_CLOSE</code> identifies the
	 * token as delimiter representing the closing round bracket character.</p>
	 */
	CHARACTER_PRENS_CLOSE( Tokens.punctuator ),
	
	/**
	 * <p>The named constant <code>CHARACTER_PRENS_OPEN</code> identifies the
	 * token as delimiter representing the opening round bracket character.</p>
	 */
	CHARACTER_PRENS_OPEN( Tokens.punctuator ),
	
	/**
	 * <p>The named constant <code>CHARACTER_TABULATOR</code> identifies the
	 * token as whitespace representing the tabulator character.</p>
	 */
	CHARACTER_TABULATOR( Tokens.whitespace ),
	
	/**
	 * <p>The named constant <code>COMMENT_BLOCK</code> identifies the token
	 * as whitespace representing comment block.</p>
	 */
	COMMENT_BLOCK( Tokens.comment ),
	
	/**
	 * <p>The named constant <code>COMMENT_LINE</code> identifies the token
	 * as whitespace representing comment line.</p>
	 */
	COMMENT_LINE( Tokens.comment ),
	
	/**
	 * <p>The named constant <code>DIRECTIVE_COMPILE</code> identifies the
	 * token as preprocessor keyword representing the compiler directive to
	 * compile only.</p>
	 */
	DIRECTIVE_COMPILE( Tokens.directive ),
	
	/**
	 * <p>The named constant <code>DIRECTIVE_JGRASS</code> identifies the
	 * token as preprocessor keyword representing the compiler directive to
	 * compile a inline model language Java-based model.</p>
	 */
	DIRECTIVE_JGRASS( Tokens.directive ),
	
	/**
	 * <p>The named constant <code>DIRECTIVE_GRASS</code> identifies the
	 * token as preprocessor keyword representing the compiler directive to
	 * compile a inline model language native-based model.</p>
	 */
	DIRECTIVE_GRASS( Tokens.directive ),
	
	/**
	 * <p>The named constant <code>DIRECTIVE_R</code> identifies the
	 * token as preprocessor keyword representing the compiler directive to
	 * compile inline R language statements.</p>
	 */
	DIRECTIVE_R( Tokens.directive ),
	
	/**
	 * <p>The named constant <code>INPUT</code> identifies the token as a
	 * input expression of a Java-based model.</p>
	 */
	INPUT( Tokens.keyword ),
	
	/**
	 * <p>The named constant <code>JAVA_MODEL</code> identifies the token as a
	 * identifier of a Java-based model.</p>
	 */
	JAVA_MODEL( Tokens.keyword ),
	
	/**
	 * <p>The named constant <code>LITERAL</code> identifies the token as a
	 * literal string.</p>
	 */
	LITERAL( Tokens.constant ),
	
	/**
	 * <p>The named constant <code>NATIVE_MODEL</code> identifies the token as
	 * a identifier of a native-based model.</p>
	 */
	NATIVE_MODEL( Tokens.keyword ),
	
	/**
	 * <p>The named constant <code>NEW_LINE</code> identifies the token as
	 * whitespace representing a new line.</p>
	 */
	NEW_LINE( Tokens.whitespace ),
	
	/**
	 * <p>The named constant <code>OUTPUT</code> identifies the token as a
	 * output expression of a Java-based model.</p>
	 */
	OUTPUT( Tokens.keyword ),
	
	/**
	 * <p>The named constant <code>PATHNAME</code> identifies the token as a
	 * pathname expression.</p>
	 */
	PATHNAME( Tokens.keyword ),
	
	/**
	 * <p>The named constant <code>UNKNOWN</code> identifies the token as a
	 * unknown expression.</p>
	 */
	UNKNOWN( Tokens.keyword ),
	
	/**
	 * <p>The named constant <code>DIRECTIVE_USAGE</code> identifies the token
	 * as preprocessor keyword representing the usage respectively the context
	 * help keyword.</p>
	 */
	DIRECTIVE_USAGE( Tokens.keyword ),
	
	/**
	 * <p>The named constant <code>VARIABLE</code> identifies the token as a
	 * variable identifier.</p>
	 */
	VARIABLE( Tokens.identifier ),
	
	/**
	 * <p>The named constant <code>WORD</code> identifies the token as a
	 * word.</p>
	 */
	WORD( Tokens.constant );
	
// Attributes
	/**
	 * <p>An abstract symbole that represents the kind of the lexical unit of
	 * this named constant for a token.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.analysis.Tokens
	 */
	private final int m_token;
	
// Construction
	/**
	 * <p>The constructor <code>TOKs</code> constructs the enumeration named 
	 * constant.</p>
	 * @param token
	 * 		- abstract symbole representing the kind of this token.
	 */
	private TOKs( int token ) {
		
		m_token = token;
	} // TOKs
	
// Operations
	/**
	 * <p>The method <code>token</code> returns an abstract symbole that
	 * represents the kind of lexical unit of this token.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.analysis.Tokens
	 * @return
	 * 		An abstract symbole that represents the kind of lexical unit.
	 */
	public final int token() {
		
		return m_token;
	} // token
	
} // TOKs
