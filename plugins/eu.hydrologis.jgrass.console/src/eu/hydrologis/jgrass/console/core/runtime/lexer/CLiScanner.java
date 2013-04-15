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
package eu.hydrologis.jgrass.console.core.runtime.lexer;

import java.io.Reader;
import java.text.MessageFormat;
import java.util.MissingResourceException;

import eu.hydrologis.jgrass.console.core.internal.analysis.Tokens;
import eu.hydrologis.jgrass.console.core.internal.lexer.AbstractLexer;
import eu.hydrologis.jgrass.console.core.internal.lexer.Lexer;
import eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_directive_compile;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_pathname;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_unknown;

/**
 * <p>This is the first scanner that is responsible for the lexical analysis of
 * the input stream, the so called command line scanner. The scanner is only
 * needed to detect, if the input stream denotes the name of a script file.</p>
 * <p>This scanner only detects the <b>/compile</b> directive or a pathname
 * token; for any other token it produces a token identifying a unknown lexeme,
 * this is because of this scanner is not used for language processing; this
 * scanner is only intended to analyze the input stream, if a script file is to
 * load or not.</p>
 * @see eu.hydrologis.jgrass.console.core.runtime.compiler.CLiMLCompiler
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class CLiScanner
	extends AbstractLexer<TOKs>
	implements Lexer<TOKs> {

// Construction
	/**
	 * <p>The copy constructor <code>CLiScanner</code> creates this
	 * lexical analyzer (scanner) with the specified project-space, the
	 * specified reader as input stream and with the specified line number
	 * information as the start-point.</p>
	 * @param projectSpace
	 * 		- the project-space.
	 * @param sourceCode
	 * 		- the input stream.
	 * @param line
	 * 		- the line number in the source program used by this lexical
	 * 		analyzer (scanner) as start-point.
	 */
	public CLiScanner( Projectspace projectSpace, Reader sourceCode,
			int line ) {
	
		super( projectSpace, sourceCode, line );
	} // CLiScanner

// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#lexemes()
	 */
	@SuppressWarnings("unchecked")
	public Lexeme<TOKs>[] lexemes() {
		
		Lexeme<TOKs>[] retval = null;
		try {
			
			Lexeme[] lexemes = {
					new X_directive_compile(
							projectSpace().languageBundle().getString(
									"HgisML.directive_compile" //$NON-NLS-1$
								)
						)
					, new X_pathname(
							MessageFormat.format(
									projectSpace().languageBundle().getString(
											"HgisML.directive_source_pathname_1" //$NON-NLS-1$
										)
									, "[jJ][gG][rR][aA][sS][sS]" //$NON-NLS-1$
								)
						)
					, new X_pathname(
							MessageFormat.format(
									projectSpace().languageBundle().getString(
											"HgisML.directive_source_pathname_2" //$NON-NLS-1$
										)
									, "[jJ][gG][rR][aA][sS][sS]" //$NON-NLS-1$
								)
						)
				};
			
			retval = lexemes;
		}
		catch( MissingResourceException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				projectSpace().err.println( e );
			
			throw e;
		}
		
		return retval;
	} // lexemes
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#delimiters()
	 */
	public Character[] punctuators() {
		
		Character[] retval = null;
		try {
			
			Character[] characters = {
					new Character(
							projectSpace().languageBundle().getString(
									"HgisML.character_operator_assign" //$NON-NLS-1$
								).charAt( 0 )
						)
					, new Character(
							projectSpace().languageBundle().getString(
									"HgisML.character_delimiter_braceclose" //$NON-NLS-1$
								).charAt( 0 )
						)
					, new Character(
							projectSpace().languageBundle().getString(
									"HgisML.character_delimiter_braceopen" //$NON-NLS-1$
								).charAt( 0 )
						)
					, new Character(
							projectSpace().languageBundle().getString(
									"HgisML.character_delimiter_bracketclose" //$NON-NLS-1$
								).charAt( 0 )
						)
					, new Character(
							projectSpace().languageBundle().getString(
									"HgisML.character_delimiter_bracketopen" //$NON-NLS-1$
								).charAt( 0 )
						)
					, new Character(
							projectSpace().languageBundle().getString(
									"HgisML.character_delimiter_colon" //$NON-NLS-1$
								).charAt( 0 )
						)
					, new Character(
							projectSpace().languageBundle().getString(
									"HgisML.character_delimiter_prensclose" //$NON-NLS-1$
								).charAt( 0 )
						)
					, new Character(
							projectSpace().languageBundle().getString(
									"HgisML.character_delimiter_prensopen" //$NON-NLS-1$
								).charAt( 0 )
						)
					, new Character(
							projectSpace().languageBundle().getString(
									"HgisML.character_delimiter_semicolon" //$NON-NLS-1$
								).charAt( 0 )
						)
				};
			
			retval = characters;
		}
		catch( MissingResourceException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				projectSpace().err.println( e );
			
			throw e;
		}
		
		return retval;
	} // punctuators
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#lexemes()
	 */
	public Lexeme<TOKs> rescue() {
		
		Lexeme<TOKs> retval = null;
		try {
			
			retval = new X_unknown(
					projectSpace().languageBundle().getString(
							"HgisML.unknown" //$NON-NLS-1$
						)
				);
		}
		catch( MissingResourceException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				projectSpace().err.println( e );
			
			throw e;
		}
		
		return retval;
	} // rescue
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.analysis.TokenAnalysis#analyse(Symtable<S>,Token<X>)
	 */
	public boolean tokskip( TOKs token ) {
		
		switch( token.token() ) {
		case Tokens.whitespace:
		case Tokens.comment:
			return true;
			
		case Tokens.constant:
		case Tokens.directive:
		case Tokens.identifier:
		case Tokens.keyword:
		case Tokens.operator:
		case Tokens.punctuator:
		default:
			return false;
		}
	} // tokskip

} // CLiScanner