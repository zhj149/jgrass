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

import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractToken;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;

/**
 * <p>A non-terminal <b>native-model</b> token.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public class N_native_model
	extends AbstractToken<TOKs>
	implements Token<TOKs> {

// Attributes
	/** The tokens identity name. */
	public final static TOKs identifier = TOKs.NATIVE_MODEL;
	
	// Construction
	/**
	 * <p>Constructs this token object with the specified line number and a
	 * copy of the specified expression.</p>
	 * @param expression
	 * 		- character string in the source program detected by the lexical
	 * 		analyzer that matches the pattern of the corresponding lexeme for
	 * 		this token.
	 * @param line
	 * 		- line number in the source program.
	 * @throws IllegalArgumentException
	 * 		- if <code>expression</code> references the null type or the length
	 * 		of the character string is zero.
	 */
	public N_native_model( String expression, int line ) {
		
		super( expression, line );
	} // N_native_model

// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token#identifier()
	 */
	public TOKs identifier() {
	
		return identifier;
	} // identifier
	
	/**
	 * <p>Returns the name of the command.</p>
	 */
	public String type() {
		
		return expression();
	} // type

} // N_native_model