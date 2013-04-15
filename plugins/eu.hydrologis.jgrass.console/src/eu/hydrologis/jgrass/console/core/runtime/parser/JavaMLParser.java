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
import eu.hydrologis.jgrass.console.core.runtime.analysis.E1003;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E1004;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2105;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2102;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2107;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2108;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2109;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2110;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2111;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2101;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2106;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2104;
import eu.hydrologis.jgrass.console.core.runtime.analysis.E2103;
import eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_java_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_statement;
import eu.hydrologis.jgrass.console.core.runtime.nodes.N_input;
import eu.hydrologis.jgrass.console.core.runtime.nodes.N_java_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.N_output;
import eu.hydrologis.jgrass.console.core.runtime.nodes.P_argument;
import eu.hydrologis.jgrass.console.core.runtime.nodes.P_input;
import eu.hydrologis.jgrass.console.core.runtime.nodes.P_java_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.P_output;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_java_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_reference;

/**
 * <p>This parser analysis the syntax and semantic of <i>JGRASS</i> Java based,
 * OpenMI compliant models.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.parser.AbstractParser
 * @see eu.hydrologis.jgrass.console.core.internal.parser.Parser
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class JavaMLParser
	extends AbstractParser<APTs, TOKs, SYMs>
	implements Parser<APTs, TOKs, SYMs> {

// Construction
	/**
	 * <p>The copy constructor <code>JavaMLParser</code> creates this syntax
	 * analyzer (parser) with the specified project-space and uses the
	 * specified lexical analyzer (scanner) for the lexical analysis of the
	 * input stream.</p>
	 * @param projectSpace
	 * 		- the project-space.
	 * @param lexer
	 * 		- the lexical analyzer (scanner) used by this syntax analyzer
	 * 		(parser) for the lexical analysis of the input stream.
	 */
	public JavaMLParser( Projectspace projectSpace, Lexer<TOKs> lexer ) {
	
		super( projectSpace, lexer );
	} // JavaMLParser
	
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
	} // __APT_model
	
	/**
	 * <p>Returns the model in scope, if any, otherwise <code>null</code>.</p>
	 * @param parsetree
	 * 		- a parsetree
	 */
	private static APT_java_model __APT_java_model( APT<APTs> parsetree ) {
		
		final APT_java_model retval;
		
		final APT<APTs> candidate = __APT_model( parsetree );
		if( null != candidate &&
					APTs.APT_JAVA_MODEL == candidate.identifier()
				) {
			
			retval = ( APT_java_model )candidate;
		}
		else {
			
			retval = null;
		}
		
		return retval;
	} // __APT_java_model
	
	/**
	 * <p>Semantic analysis, if the specified java model has a default key
	 * declared or not.</p>
	 * @param symtable
	 * 		- the symbol table.
	 * @param model
	 * 		- a java model.
	 * @return
	 * 		A object of analysis success (success, continue, ignore
	 * 		and continue) or either one of error or warning.
	 */
	private MessageCode analyseJavaModelDefaultKey( Symtable<SYMs> symtable,
			APT_java_model model, Token<TOKs> token )
		throws CompilerWarning, CompilerError {
		
		if( null == symtable )
			throw new IllegalArgumentException();
		if( null == model )
			throw new IllegalArgumentException();
		if( null == model )
			throw new IllegalArgumentException();
		
		final SYM_type_java_model __typedef;
		__typedef = ( SYM_type_java_model )symtable.lookup(
				SYM_type_reference.qualifier( model.token().type() )
			);
		if( false == __typedef.hasDefaultKey() )
			throw new E2107( projectSpace(), model.token(), token );
		
		return MessageCode.SUCCESS;
	} // analyseJavaModelDefaultKey
	
	/**
	 * <p>Semantic analysis, if the specified input or output token of the
	 * exchange item and the quantity are according to the model or not.</p>
	 * @param symtable
	 * 		- the symbol table.
	 * @param model
	 * 		- a java model.
	 * @param token
	 * 		- a model token.
	 * @return
	 * 		A object of analysis success (success, continue, ignore
	 * 		and continue) or either one of error or warning.
	 */
	private MessageCode analyseJavaModelInOuts( Symtable<SYMs> symtable,
			APT_java_model model, Token<TOKs> token )
		throws CompilerWarning, CompilerError {
	
		if( null == symtable )
			throw new IllegalArgumentException();
		if( null == model )
			throw new IllegalArgumentException();
		if( null == token )
			throw new IllegalArgumentException();
		
		final String exchangeKeyword;
		final String quantityKeyword;
		switch( token.identifier() ) {
		case INPUT:
			quantityKeyword = (( N_input )token).__quantity();
			exchangeKeyword = (( N_input )token).type();
			break;
			
		case OUTPUT:
			quantityKeyword = (( N_output )token).__quantity();
			exchangeKeyword = (( N_output )token).type();
			break;
			
		default:
			throw new IllegalArgumentException();
		}
		
		Symbol<SYMs> __typedef = symtable.lookup(
				SYM_type_reference.qualifier( exchangeKeyword )
			);
		if( null == __typedef || SYMs.SYM_TYPE_CLASS != __typedef.identifier() )
			throw new E2109(
					projectSpace()
					, token
				);
			
		final SYM_type_java_model __mtypedef;
		__mtypedef = ( SYM_type_java_model )symtable.lookup(
				SYM_type_reference.qualifier( model.token().type() )
			);
		if( false == __mtypedef.hasExchangeItems() )
			throw new E2108(
					projectSpace()
					, model.token()
					, token
				);
		else if( false == __mtypedef.hasExchangeItem(
							quantityKeyword
						)
				)
			throw new E2110(
					projectSpace()
					, model.token()
					, token
				);
		
		return MessageCode.CONTINUE;
	} // analyseJavaModelInOuts
	
	/**
	 * <p>Semantic analysis, if the specified token identifies a model or
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
	private MessageCode analyseJavaModel( Symtable<SYMs> symtable,
			APT<APTs> parseTree, N_java_model token )
		throws CompilerWarning, CompilerError {
		
		if( null == symtable )
			throw new IllegalArgumentException();
		if( null == parseTree )
			throw new IllegalArgumentException();
		if( null == token )
			throw new IllegalArgumentException();
		
		APT<APTs> __APT_model = __APT_model( parseTree );
		if( null != __APT_model )
			throw new E2103(
					projectSpace()
					, token
				);
		
		Symbol<SYMs> __typedef = symtable.lookup(
				SYM_type_reference.qualifier( token.type() )
			);
		if( null == __typedef )
			throw new E2104(
					projectSpace()
					, token
				);
		
		switch( __typedef.identifier() ) {
		case SYM_TYPE_JAVA_MODEL:
			break;
			
		case SYM_TYPE_NATIVE_MODEL:
			throw new E2102(
					projectSpace()
					, token
				);
			
		default:
			throw new E2104(
					projectSpace()
					, token
				);
		}
		
		return MessageCode.SUCCESS;
	} // analyseJavaModel
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.parser.Parser#analyse(Symtable,APT,Token[])
	 */
	public MessageCode analyse( Symtable<SYMs> symtable, APT<APTs> parseTree,
			Token<TOKs>... tstream )
		throws CompilerWarning, CompilerError {
		
		if( null == tstream || 0 >= tstream.length )
			throw new IllegalArgumentException();
		if( null == symtable )
			throw new IllegalArgumentException();
		if( null == parseTree )
			throw new IllegalArgumentException();
		
		final MessageCode retval;
		switch( tstream.length ) {
		case 0:
			retval = MessageCode.CONTINUE;
			break;
			
		case 1:
			if( null == tstream[ 0 ] ) {
				
				retval = MessageCode.IGNORE_AND_CONTINUE;
			}
			else {
				
				switch( tstream[ 0 ].identifier() ) {
				case ARGUMENT:
					if( null == __APT_java_model( parseTree ) )
						throw new E2105(
								projectSpace()
								, tstream[ 0 ]
							);
					
					retval = MessageCode.CONTINUE;
					break;
					
				case CHARACTER_BRACKET_CLOSE:
					if( null == __APT_java_model( parseTree ) ) {
						
						throw new E1002(
								projectSpace()
								, tstream[ 0 ]
							);
					}
					else {
						
						final APT<APTs> parent = parseTree.parent();
						if( null == parent ) {
							
							throw new E1002(
									projectSpace()
									, tstream[ 0 ]
								);
						}
						else {
							
							switch( parent.identifier() ) {
							case APT_INPUT:
							case APT_OUTPUT:
								retval = MessageCode.SUCCESS;
								break;
								
							default:
								throw new E1002(
										projectSpace()
										, tstream[ 0 ]
									);
							}
						}
					}
					break;
				
				case INPUT:
				case OUTPUT:
					if( null == __APT_java_model( parseTree ) )
						throw new E2105(
								projectSpace()
								, tstream[ 0 ]
							);
					else
						retval = analyseJavaModelInOuts(
								symtable
								, __APT_java_model( parseTree )
								, tstream[ 0 ]
							);
					break;
					
				case LITERAL:
				case WORD:
					if( null == __APT_java_model( parseTree ) )
						throw new E2101(
								projectSpace()
								, tstream[ 0 ]
							);
					else 
						retval = analyseJavaModelDefaultKey(
								symtable
								, __APT_java_model( parseTree )
								, tstream[ 0 ]
							);
					break;
					
				case DIRECTIVE_USAGE:
					if( null == __APT_java_model( parseTree ) )
						throw new E2105(
								projectSpace()
								, tstream[ 0 ]
							);
					
					retval = MessageCode.SUCCESS;
					break;
					
				case JAVA_MODEL:
					retval = analyseJavaModel(
							symtable
							, parseTree
							, ( N_java_model )tstream[ 0 ]
						);
					break;
					
				case NATIVE_MODEL:
					throw new E2102(
							projectSpace()
							, tstream[ 0 ]
						);
					
				case VARIABLE:
					if( null == __APT_java_model( parseTree ) )
						retval = MessageCode.CONTINUE;
					else
						retval = analyseJavaModelDefaultKey(
								symtable
								, __APT_java_model( parseTree )
								, tstream[ 0 ]
							);
					break;
					
				default:
					if( null == __APT_java_model( parseTree ) )
						throw new E2101(
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
			case ARGUMENT:
				if( null == tstream[ 1 ] )
					throw new E2111(
							projectSpace()
							,tstream[ 0 ]
						);
				
				switch( tstream[ 1 ].identifier() ) {
				case LITERAL:
				case VARIABLE:
				case WORD:
					retval = MessageCode.SUCCESS;
					break;
					
				default:
					throw new E2111(
							projectSpace()
							, tstream[ 0 ]
						);
				}
				break;
				
			case INPUT:
			case OUTPUT:
				if( null == tstream[ 1 ] )
					throw new E2111(
							projectSpace()
							, tstream[ 0 ]
						);
				
				switch( tstream[ 1 ].identifier() ) {
				case CHARACTER_ASTERISK:
				case CHARACTER_BRACKET_OPEN:
				case LITERAL:
				case DIRECTIVE_USAGE:
				case VARIABLE:
				case WORD:
					retval = MessageCode.SUCCESS;
					break;
					
				default:
					throw new E2111(
							projectSpace()
							, tstream[ 0 ]
						);
				}
				break;
				
			case VARIABLE:
				if( null == tstream[ 1 ] )
					throw new E1003(
							projectSpace()
							, tstream[ 0 ]
							, "=" //$NON-NLS-1$
						);
				
				switch( tstream[ 1 ].identifier() ) {
				case CHARACTER_ASSIGN:
					retval = MessageCode.CONTINUE;
					break;
					
				case JAVA_MODEL:
					throw new E1004(
							projectSpace()
							, tstream[ 1 ]
							, "=" //$NON-NLS-1$
						);
					
				case NATIVE_MODEL:
					throw new E2102(
							projectSpace()
							, tstream[ 0 ]
						);
					
				default:
					throw new E1003(
							projectSpace()
							, tstream[ 0 ]
							, "=" //$NON-NLS-1$
						);
				}
				break;
				
			default:
				if( null == tstream[ 1 ] )
					throw new E1001(
							projectSpace()
							, tstream[ 0 ].line()
						);
			
				 throw new E1002(
						 projectSpace()
						, tstream[ 1 ]
					);
			}
			break;
			
		case 3:
			switch( tstream[ 0 ].identifier() ) {
			case VARIABLE:
				switch( tstream[ 1 ].identifier() ) {
				case CHARACTER_ASSIGN:
					if( null == tstream[ 2 ] )
						throw new E2106(
								projectSpace()
								, tstream[ 1 ]
							);
					switch( tstream[ 2 ].identifier() ) {
					case JAVA_MODEL:
						retval = analyseJavaModel(
								symtable
								, parseTree
								, ( N_java_model )tstream[ 2 ]
							);
						break;
						
					case NATIVE_MODEL:
						throw new E2102(
								projectSpace()
								, tstream[ 0 ]
							);
						
					default:
						throw new E2106(
								projectSpace()
								, tstream[ 1 ]
							);
					}
					break;
					
				default:
					throw new E1003(
							projectSpace()
							, tstream[ 0 ]
							, tstream[ 1 ].expression()
						);
				}
				break;
				
			default:
				throw new E1002(
						projectSpace()
						, tstream[ 2 ]
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
					new P_output()
					, new P_input()
					, new P_argument()
					, new P_java_model()
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
	
} // JavaMLParser
