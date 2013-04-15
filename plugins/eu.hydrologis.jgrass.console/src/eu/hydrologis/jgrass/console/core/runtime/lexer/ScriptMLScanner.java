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
import java.util.MissingResourceException;

import eu.hydrologis.jgrass.console.core.internal.analysis.Tokens;
import eu.hydrologis.jgrass.console.core.internal.lexer.AbstractLexer;
import eu.hydrologis.jgrass.console.core.internal.lexer.Lexer;
import eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_braceclose;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_braceopen;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_bracketclose;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_bracketopen;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_commentblock;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_commentline;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_directive_compile;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_directive_grass;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_directive_jgrass;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_directive_r;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_prensclose;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_prensopen;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_unknown;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_backspace;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_blank;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_carriagereturn;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_formfeed;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_linefeed;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_newline;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_whitespace_tabulator;
import eu.hydrologis.jgrass.console.core.runtime.nodes.X_word;

/**
 * <p>The fact, after the first analysis of the command line, that the input
 * stream is ambiguous – at one hand it can be a source program and at the other
 * a GRASS or JGRASS command - the translator assumes a source program in its
 * input; this is the scanner used to scan the input stream of a source
 * program. The scanner preserves the programming constructs and white-space
 * character information; comments are omitted.</p>
 * @see eu.hydrologis.jgrass.console.core.runtime.compiler.CLiMLCompiler
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class ScriptMLScanner
	extends AbstractLexer<TOKs>
	implements Lexer<TOKs> {

// Construction
	/**
	 * <p>The constructor <code>ScriptMLScanner</code> creates this lexical
	 * analyzer (scanner) with the specified project-space, the specified
	 * reader as input stream and with the specified line number information
	 * as the start-point.</p>
	 * @param projectSpace
	 * 		- the project-space.
	 * @param sourceCode
	 * 		- the input stream.
	 * @param line
	 * 		- the line number in the source program used by this lexical
	 * 		analyzer (scanner) as start-point.
	 */
	public ScriptMLScanner( Projectspace projectSpace, Reader sourceCode,
			int line ) {
	
		super( projectSpace, sourceCode, line );
	} // ScriptMLScanner

// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#lexemes()
	 */
	@SuppressWarnings("unchecked")
	public Lexeme<TOKs>[] lexemes() {
		
		Lexeme<TOKs>[] retval = null;
		try {
			
			Lexeme[] lexemes = {
					new X_whitespace_blank(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_blank" //$NON-NLS-1$
								)
						)
					, new X_whitespace_tabulator(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_tabulator" //$NON-NLS-1$
								)
						)
					, new X_whitespace_formfeed(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_formfeed" //$NON-NLS-1$
								)
						)
					, new X_whitespace_newline(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_newline" //$NON-NLS-1$
								)
						)
					, new X_whitespace_carriagereturn(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_carriagereturn" //$NON-NLS-1$
								)
						)	
					, new X_whitespace_linefeed(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_linefeed" //$NON-NLS-1$
								)
						)
					, new X_whitespace_backspace(
							projectSpace().languageBundle().getString(
									"HgisML.whitespace_backspace" //$NON-NLS-1$
								)
						)
					, new X_braceclose(
							projectSpace().languageBundle().getString(
									"HgisML.delimiter_braceclose" //$NON-NLS-1$
								)
						)
					, new X_braceopen(
							projectSpace().languageBundle().getString(
									"HgisML.delimiter_braceopen" //$NON-NLS-1$
								)
						)
					, new X_bracketclose(
							projectSpace().languageBundle().getString(
									"HgisML.delimiter_bracketclose" //$NON-NLS-1$
								)
						)
					, new X_bracketopen(
							projectSpace().languageBundle().getString(
									"HgisML.delimiter_bracketopen" //$NON-NLS-1$
								)
						)
					, new X_prensclose(
							projectSpace().languageBundle().getString(
									"HgisML.delimiter_prensclose" //$NON-NLS-1$
								)
						)
					, new X_prensopen(
							projectSpace().languageBundle().getString(
									"HgisML.delimiter_prensopen" //$NON-NLS-1$
								)
						)
					, new X_commentblock(
							projectSpace().languageBundle().getString(
									"HgisML.commentblock" //$NON-NLS-1$
								)
						)
					, new X_commentline(
							projectSpace().languageBundle().getString(
									"HgisML.commentline" //$NON-NLS-1$
								)
						)
					, new X_directive_compile(
							projectSpace().languageBundle().getString(
									"HgisML.directive_compile" //$NON-NLS-1$
								)
						)
					, new X_directive_jgrass(
							projectSpace().languageBundle().getString(
									"HgisML.directive_jgrass" //$NON-NLS-1$
								)
						)
					, new X_directive_grass(
							projectSpace().languageBundle().getString(
									"HgisML.directive_grass" //$NON-NLS-1$
								)
						)
					, new X_directive_r(
							projectSpace().languageBundle().getString(
									"HgisML.directive_r" //$NON-NLS-1$
								)
						)
					, new X_word(
							projectSpace().languageBundle().getString(
									"HgisML.word" //$NON-NLS-1$
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
		case Tokens.comment:
			return true;
			
		case Tokens.whitespace:
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

} // ScriptMLScanner