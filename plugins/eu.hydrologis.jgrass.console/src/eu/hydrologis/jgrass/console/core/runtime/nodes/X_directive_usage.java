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

import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractLexeme;
import eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;

/**
 * <p>The usage directive, <b>--help</b>; provides the pattern that matches this
 * kind of lexical unit. A lexical analyzer uses the implemented functionality
 * to verify if the sequence of input characters matches the pattern and also
 * the functionality of transition into a token.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractLexeme
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class X_directive_usage
	extends AbstractLexeme<TOKs>
	implements Lexeme<TOKs> {

// Construction
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractLexeme#AbstractLexeme(X,CharSequence)
	 */
	public X_directive_usage( CharSequence regex ) {
	
		super( regex );
	} // X_directive_usage

// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme#transition(CharSequence,int)
	 */
	public Token<TOKs> transition( CharSequence sequence, int line ) {
		
		return new T_directive_usage( sequence.toString(), line );
	} // transition

} // X_directive_usage
