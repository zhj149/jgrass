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
package eu.hydrologis.jgrass.console.core.runtime.parser;

import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerError;
import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerWarning;
import eu.hydrologis.jgrass.console.core.internal.analysis.MessageCode;
import eu.hydrologis.jgrass.console.core.internal.lexer.Lexer;
import eu.hydrologis.jgrass.console.core.internal.nodes.APT;
import eu.hydrologis.jgrass.console.core.internal.nodes.Production;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symbol;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symtable;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.internal.parser.AbstractParser;
import eu.hydrologis.jgrass.console.core.internal.parser.Parser;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.APTs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E1001;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E1002;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E3101;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E3103;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E3102;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E3104;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E3105;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E3106;
import eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_native_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_statement;
import eu.hydrologis.jgrass.console.core.runtime.nodes.N_native_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.P_native_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.P_parameter;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_primitive;

/**
 * <p>This parser analysis the syntax of <i>GRASS</i> native based commands.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class NativeMLParser
	extends AbstractParser<APTs, TOKs, SYMs>
	implements Parser<APTs, TOKs, SYMs> {

// Construction
	/**
	 * <p>The copy constructor <code>NativeMLParser</code> creates this syntax
	 * analyzer (parser) with the specified project-space and uses the
	 * specified lexical analyzer (scanner) for the lexical analysis of the
	 * input stream.</p>
	 * @param projectSpace
	 * 		- the project-space.
	 * @param lexer
	 * 		- the lexical analyzer (scanner) used by this syntax analyzer
	 * 		(parser) for the lexical analysis of the input stream.
	 */
	public NativeMLParser( Projectspace projectSpace, Lexer<TOKs> lexer ) {
	
		super( projectSpace, lexer );
	} // NativeMLParser
	
// Operations
	/**
	 * <p>Returns the model/command in scope, if any, otherwise
	 * <code>null</code>.</p>
	 * @param parsetree
	 * 		- a parsetree
	 */
	private static APT<APTs> __APT_model( APT<APTs> parsetree ) {
		
		final APT<APTs> retval;
		
		if( null == parsetree ) {
			
			retval = null;
		}
		else {
			
			switch( parsetree.identifier() ) {
			case APT_STATEMENT:
				retval = (( APT_statement )parsetree).__model();
				break;
				
			case APT_JAVA_MODEL:
			case APT_NATIVE_MODEL:
				retval = parsetree;
				break;
					
			default:
				retval = null;
			}
		}
		
		return retval;
	} // __model
	
	/**
	 * <p>Returns the command in scope, if any, otherwise <code>null</code>.</p>
	 * @param parsetree
	 * 		- a parsetree
	 */
	private static APT_native_model __APT_native_model(
			APT<APTs> parsetree ) {
		
		final APT_native_model retval;
		
		final APT<APTs> candidate = __APT_model( parsetree );
		if( null != candidate &&
					APTs.APT_NATIVE_MODEL == candidate.identifier()
				) {
			
			retval = ( APT_native_model )candidate;
		}
		else {
			
			retval = null;
		}
		
		return retval;
	} // __APT_native_model
	
	/**
	 * <p>Semantic analysis, if the specified token identifies a command or
	 * not.</p>
	 * @param symtable
	 * 		- the symbol table.
	 * @param parseTree
	 * 		- the parse tree.
	 * @param token
	 * 		- a model token.
	 * @return
	 * 		A object of analysis success (success, continue, ignore
	 * 		and continue) or either one of error or warning.
	 */
	private MessageCode analyseNativeModel( Symtable<SYMs> symtable,
			APT<APTs> parseTree, N_native_model token )
		throws CompilerWarning, CompilerError {
		
		if( null == symtable )
			throw new IllegalArgumentException();
		if( null == parseTree )
			throw new IllegalArgumentException();
		if( null == token )
			throw new IllegalArgumentException();
		
		APT<APTs> __APT_model = __APT_model( parseTree );
		if( null != __APT_model )
			throw new E3104(
					projectSpace()
					, token
				);
		
		Symbol<SYMs> __typedef = symtable.lookup(
				SYM_type_primitive.qualifier( token.type() )
			);
		if( null == __typedef )
			throw new E3103(
					projectSpace()
					, token
				);
		
		switch( __typedef.identifier() ) {
		case SYM_TYPE_NATIVE_MODEL:
			break;
			
		case SYM_TYPE_JAVA_MODEL:
			throw new E3102(
					projectSpace()
					, token
				);
			
		default:
			throw new E3103(
					projectSpace()
					, token
				);
		}
		
		return MessageCode.SUCCESS;
	} // analyseSemantic
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.parser.Parser#analyse(Symtable,APT,Token[])
	 */
	public MessageCode analyse( Symtable<SYMs> symtable, APT<APTs> parseTree,
			Token<TOKs>... tstream )
		throws CompilerWarning, CompilerError {
		
		if( null == tstream || 0 >= tstream.length )
			throw new IllegalArgumentException();
		
		final MessageCode retval;
		switch( tstream.length ) {
		case 0:
			retval = MessageCode.CONTINUE;
			break;
			
		case 1:
			if( null == tstream[ tstream.length - 1 ] ) {
				
				retval = MessageCode.IGNORE_AND_CONTINUE;
			}
			else {
				
				switch( tstream[ tstream.length - 1 ].identifier() ) {
				case CHARACTER_ASSIGN:
					if( null == __APT_native_model( parseTree ) )
						throw new E3105(
								projectSpace()
								, tstream[ 0 ]
							);
					retval = MessageCode.CONTINUE;
					break;
					
				case LITERAL:
				case UNKNOWN:
				case VARIABLE:
				case WORD:
					if( null == __APT_native_model( parseTree ) )
						throw new E3105(
								projectSpace()
								, tstream[ 0 ]
							);
					
					retval = MessageCode.SUCCESS;
					break;
					
				case JAVA_MODEL:
					throw new E3102(
							projectSpace()
							, tstream[ 0 ]
						);
					
				case NATIVE_MODEL:
					retval = analyseNativeModel(
							symtable
							, parseTree
							, ( N_native_model )tstream[ 0 ]
						);
					break;
							
				default:
					if( null == __APT_native_model( parseTree ) )
						throw new E3101(
								projectSpace()
								, tstream[ 0 ]
							);
				
					throw new E1002(
							projectSpace()
							, tstream[ 0 ]
						);
				}
			}
			break;
			
		case 2:
			switch( tstream[ 0 ].identifier() ) {
			case CHARACTER_ASSIGN:
				if( null == tstream[ tstream.length - 1 ] )
					throw new E3106(
							projectSpace()
							, tstream[ 0 ]
						);
					
				switch( tstream[ tstream.length - 1 ].identifier() ) {
				case LITERAL:
				case UNKNOWN:
				case VARIABLE:
				case WORD:
					retval = MessageCode.SUCCESS;
					break;
					
				default:
					throw new E1002(
							projectSpace()
							, tstream[ 0 ]
						);
				}
				break;
				
			default:
				if( null == tstream[ tstream.length - 1 ] )
					throw new E1001(
							projectSpace()
							, tstream[ 0 ].line()
						);
			
				 throw new E1002(
						 projectSpace()
						, tstream[ tstream.length - 1 ]
					);
			}
			break;
			
		default:
			throw new E1002(
					projectSpace()
					, tstream[ tstream.length - 2 ]
				);
		}
		
		return retval;
	} // analyseSyntax
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.parser.Parser#productions()
	 */
	@SuppressWarnings("unchecked")
	public Production<APTs, TOKs, SYMs>[] productions() {
		
		Production<APTs, TOKs, SYMs>[] retval = null;
		try {
		
			Production[] productions = {
					new P_parameter()
					, new P_native_model()
				};
			
			retval = productions;
		}
		catch( Exception e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				projectSpace().err.println( e );
			
			e.printStackTrace();
		}
		
		return retval;
	} // productions
	
} // NativeMLParser
