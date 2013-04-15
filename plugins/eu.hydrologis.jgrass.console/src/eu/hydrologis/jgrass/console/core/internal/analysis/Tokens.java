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
package eu.hydrologis.jgrass.console.core.internal.analysis;

/**
 * <p>A token is the smallest element of a source program that is meaningful to
 * a translator and identify themself by using an abstract symbol that
 * represents its kind of lexical unit. The parser can recognize these kinds of
 * tokens: identifiers, keywords, literals, operators, punctuators, and other
 * separators enclosing whitespace character information. A stream of these
 * tokens makes up a translation unit.</p><p>The interface <code>Tokens</code>
 * provides a selection of lexical units.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface Tokens {

	/**
	 * <p>The named constant <code>comment</code> identifies a the token as
	 * comment token either of a single-line or of a multi-line comment.</p>
	 */
	public final static int comment = 0;
	
	/**
	 * <p>The named constant <code>constant</code> identifies the token as a
	 * constant expression.</p>
	 */
	public final static int constant = 1;
	
	/**
	 * <p>The named constant <code>directive</code> identifies the token as a
	 * compiler directive.</p>
	 */
	public final static int directive = 2;
	
	/**
	 * <p>The named constant <code>identifier</code> identifies the token as an
	 * identifier token.</p>
	 */
	public final static int identifier = 3;
	
	/**
	 * <p>The named constant <code>keyword</code> identifies the token as a
	 * keyword respectively as a reserved word token.</p>
	 */
	public final static int keyword = 4;
	
	/**
	 * <p>The named constant <code>operator</code> identifies the token as an
	 * operator token.</p>
	 */
	public final static int operator = 5;
	
	/**
	 * <p>The named constant <code>punctuator</code> identifies the token as a
	 * delimiter token.</p>
	 */
	public final static int punctuator = 6;
	
	/**
	 * <p>The named constant <code>whitespace</code> identifies the token as a
	 * whitespace token.</p>
	 */
	public final static int whitespace = 7;
	
} // Tokens
