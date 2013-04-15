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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>The abstract class <code>AbstractLexeme</code> implements basic behavior
 * of the method <code>analyse</code>, which is common to all future derived
 * lexeme implementations.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme
 * @see java.util.regex.Matcher
 * @see java.util.regex.Pattern
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractLexeme<X>
    implements Lexeme<X> {

// Attributes
	/** The compiled pattern representation of a lexeme's regular expression. */
	private final Pattern m_pattern;
	
// Construction
	/**
	 * <p>The copy constructor <code>AbstractLexeme</code> creates the compiled
	 * pattern of this lexeme's regular expression using the specified regular
	 * expression.</p>
	 * @param regex
	 * 		 - regular expression, which matches the lexeme and which will be
	 * 		compiled into a pattern.
	 */
	public AbstractLexeme( CharSequence regex ) {
		
		super();
		m_pattern = Pattern.compile( regex.toString() );
	} // AbstractLexeme
	
	/**
	 * <p>The copy constructor <code>AbstractLexeme</code> creates the compiled
	 * pattern of this lexeme's regular expression using the specified regular
	 * expression and the specified flags.</p>
	 * @param regex
	 * 		 - regular expression, which matches the lexeme and which will be
	 * 		compiled into a pattern.
	 * @param flags
	 * 		- match flags, a bit mask using the constant field values of the
	 * 		<code>Pattern</code> class. 
	 */
	public AbstractLexeme( CharSequence regex, int flags ) {
		
		super();
		m_pattern = Pattern.compile( regex.toString(), flags );
	} // AbstractLexeme
	
// Operations
	/**
	 * <p>Returns the compiled pattern representation of a lexeme's regular
	 * expression, which was used to create this <code>Lexeme</code> object.</p>
	 * @return
	 * 		The compiled pattern representation of a lexeme's regular
	 * 		expression.
	 */
	protected final Pattern pattern() {
		
		return m_pattern;
	} // pattern

	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.analysis.LexicalAnalysis#accepts(CharSequence)
	 */
	public boolean analyse( CharSequence sequence ) {
		
		boolean retval = false;
		try {
		
			if( null != pattern() ) {
			
				Matcher matcher = pattern().matcher( sequence );
				if( null != matcher && true == matcher.lookingAt() )
					retval = matcher.matches();
			}
		}
		catch( Exception e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			e.printStackTrace();
		}
		
		return retval;
	} // analyse
	
} // AbstractLexeme
