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
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Vector;

import eu.hydrologis.jgrass.console.core.internal.analysis.Tokens;
import eu.hydrologis.jgrass.console.core.internal.lexer.AbstractLexer;
import eu.hydrologis.jgrass.console.core.internal.lexer.Lexer;
import eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_assign;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_commentblock;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_commentline;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_directive_compile;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_java_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_literal;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_native_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_unknown;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_variable;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_blank;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_carriagereturn;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_formfeed;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_linefeed;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_newline;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_tabulator;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_word;

/**
 * <p>This scanner scans <i>GRASS</i> native based commands.</p>
 * @see eu.hydrologis.jgrass.console.core.runtime.parser.NativeMLParser
 * @see eu.hydrologis.jgrass.console.core.internal.lexer.AbstractLexer
 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class NativeMLScanner
	extends AbstractLexer<TOKs>
	implements Lexer<TOKs> {

// Construction
	/**
	 * <p>The copy constructor <code>NativeMLScanner</code> creates this
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
	public NativeMLScanner( Projectspace projectSpace, Reader sourceCode,
			int line ) {
	
		super( projectSpace, sourceCode, line );
	} // NativeMLScanner
	
// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#lexemes()
	 */
	@SuppressWarnings("unchecked")
	public Lexeme<TOKs>[] lexemes() {
		
		Lexeme<TOKs>[] retval = null;
		try {
			
			List<Lexeme<TOKs>> lexemes = new ArrayList<Lexeme<TOKs>>();
			
			// Adding whitespace and punctuators...
			lexemes.add( 
					new X_whitespace_blank(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_blank" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_whitespace_tabulator(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_tabulator" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_whitespace_formfeed(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_formfeed" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_whitespace_newline(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_newline" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_whitespace_carriagereturn(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_carriagereturn" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_whitespace_linefeed(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_linefeed" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_commentblock(
							projectSpace().languageBundle().getString(
									"HgisML.commentblock" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_commentline(
							projectSpace().languageBundle().getString(
									"HgisML.commentline" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_directive_compile(
							projectSpace().languageBundle().getString(
									"HgisML.directive_compile" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_assign(
							projectSpace().languageBundle().getString(
									"HgisML.operator_assign" //$NON-NLS-1$
								)
						)
				);
			
			// Adding the native model keywords first...
			Vector<String> keywords = projectSpace().importedNativeModelKeywords();
			for( int i = 0; i < keywords.size(); ++i ) {
				
				lexemes.add(
						new X_native_model(
								keywords.elementAt( i )
								, projectSpace().languageBundle().getString(
										"HgisML.native_model" //$NON-NLS-1$
									)
							)
					);
			}
			
			// Adding the java model keywords...
			keywords = projectSpace().importedJavaModelKeywords();
			for( int i = 0; i < keywords.size(); ++i ) {
				
				lexemes.add(
						new X_java_model(
								keywords.elementAt( i )
								, projectSpace().languageBundle().getString(
										"HgisML.java_model" //$NON-NLS-1$
									)
								, new Character(
										projectSpace().languageBundle().getString(
												"HgisML.character_operator_asterisk" //$NON-NLS-1$
											).charAt( 0 )
									)
							)
					);
			}
			
			// Adding the regular expressions...
			lexemes.add(
					new X_variable(
							projectSpace().languageBundle().getString(
									"HgisML.variable" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_literal(
							projectSpace().languageBundle().getString(
									"HgisML.number_float" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_literal(
							projectSpace().languageBundle().getString(
									"HgisML.number_integer" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_literal(
							projectSpace().languageBundle().getString(
									"HgisML.literal_string1" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_literal(
							projectSpace().languageBundle().getString(
									"HgisML.literal_string2" //$NON-NLS-1$
								)
						)
				);
			lexemes.add(
					new X_word(
							projectSpace().languageBundle().getString(
									"HgisML.word" //$NON-NLS-1$
								)
						)
				);
			
			lexemes.toArray( retval = new Lexeme[ lexemes.size() ] );
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
		case Tokens.directive:
		case Tokens.comment:
			return true;

		case Tokens.constant:
		case Tokens.identifier:
		case Tokens.keyword:
		case Tokens.operator:
		case Tokens.punctuator:
		default:
			return false;
		}
	} // tokskip
	
} // NativeMLScanner
