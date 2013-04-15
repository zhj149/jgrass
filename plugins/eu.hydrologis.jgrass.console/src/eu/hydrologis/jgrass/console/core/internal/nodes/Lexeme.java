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
 * <p>The interface <code>Lexeme</code> provides the mechanism for the
 * <i>analysis</i> and for the <i>transition</i> of input characters - a so
 * called <i>lexeme</i> - into a token during the lexical analysis phase of
 * a translator.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractLexeme
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token
 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface Lexeme<X> {

// Attributes
	/** Whitespace character #8: backspace */
	public final static char BS  = '\b';
	
	/** Whitespace character #13: carriage return */
	public final static char CR  = '\r';
	
	/** Whitespace character #12: form-feed */
	public final static char FF  = '\f';
	
	/** Whitespace character #9: horizontal tabulator */
	public final static char HT = '\t';
	
	/** Whitespace character #10: line-feed */
	public final static char LF  = '\n';
	
	/** Whitespace character #32: space, blank */
	public final static char SP  = '\u0020'; 
	
	/** Whitespace character #11: vertical tabulator */
	public final static char VT = '\u000b';
		
// Operations
	/**
	 * <p>The method <code>analyse</code> analysis a character sequence by
	 * verifying, if the character sequence matches the pattern of the
	 * lexeme.</p>
	 * @param sequence
	 * 		- a character sequence to be matched.
	 * @return
	 * 		Returns <code>true</code>, if the given character sequence
	 * 		<code>sequence</code> matches the pattern of the lexeme,
	 * 		otherwise <code>false</code>.
	 */
	public abstract boolean analyse( CharSequence sequence );
	
	/**
	 * <p>The method <code>transition</code> does the transition from a lexeme
	 * into a analogous token by creating a new token object.</p><p>The method
	 * uses the given character sequence and the given line number to create a
	 * new token object. However, there is no gurantee that calling this method
	 * will automatically result in instantiating a new token object, which is
	 * then returned to the caller, or by returning a reference to a already
	 * existing token; the methods result value can also be <code>null</code>,
	 * which signals a fatal internal ERROR.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token
	 * @param sequence
	 * 		- a character sequence, which will end up as the tokens expression.
	 * 		It is recommanded, that the character sequence that was used to call
	 * 		the objects method <code>accepts</code>, is the same sequence which
	 * 		is used to call the objects <code>transition</code> method.
	 * @param line
	 * 		- the line number inside the source code, where this matching
	 * 		character sequence is to be found.
	 * @return
	 * 		Returns a new token for this character sequence, the so-called
	 * 		lexeme. If the returned value is equal to <code>null</code>, this
	 * 		signifies a fatal internal ERROR.
	 */
	public abstract Token<X> transition( CharSequence sequence, int line );
	
} // Lexeme
