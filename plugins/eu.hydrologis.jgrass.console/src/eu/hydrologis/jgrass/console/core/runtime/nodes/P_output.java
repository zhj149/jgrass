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

import eu.hydrologis.jgrass.console.core.internal.nodes.APT;
import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractProduction;
import eu.hydrologis.jgrass.console.core.internal.nodes.Production;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symtable;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.APTs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;

/**
 * <p>The production for a output argument of a <i>JGRASS</i> model.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class P_output
    extends AbstractProduction<APTs, TOKs, SYMs>
    implements Production<APTs, TOKs, SYMs> {

// Attributes
	/** Alternative production for a none-nested output argument. */
	private final TOKs[][] m_alternative;
	
// Construction
	/**
	 * <p>Constructs this production with their grammar productions.</p>
	 */
	public P_output() {
		
		super(
				new TOKs[][] {
						new TOKs[] {
								TOKs.CHARACTER_BRACKET_CLOSE
							}
						, new TOKs[] {
								TOKs.OUTPUT
								, TOKs.CHARACTER_ASTERISK
							}
						, new TOKs[] {
								TOKs.OUTPUT
								, TOKs.CHARACTER_BRACKET_OPEN
							}
						, new TOKs[] {
								TOKs.OUTPUT
								, TOKs.LITERAL
							}
						, new TOKs[] {
								TOKs.OUTPUT
								, TOKs.DIRECTIVE_USAGE
							}
						, new TOKs[] {
								TOKs.OUTPUT
								, TOKs.VARIABLE
							}
						, new TOKs[] {
								TOKs.OUTPUT
								, TOKs.WORD
							}
					}
			);
		m_alternative = new TOKs[][] {
				new TOKs[] {
						TOKs.OUTPUT
						, TOKs.CHARACTER_ASTERISK
					}
				, new TOKs[] {
						TOKs.OUTPUT
						, TOKs.CHARACTER_BRACKET_OPEN
					}
				, new TOKs[] {
						TOKs.OUTPUT
						, TOKs.LITERAL
					}
				, new TOKs[] {
						TOKs.OUTPUT
						, TOKs.DIRECTIVE_USAGE
					}
				, new TOKs[] {
						TOKs.OUTPUT
						, TOKs.VARIABLE
					}
				, new TOKs[] {
						TOKs.OUTPUT
						, TOKs.WORD
					}
			};
	} // P_output
	
// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Production#produces(Symtable,APT)
	 */
	public TOKs[][] produces( APT<APTs> parseTree ) {
		
		final APT<APTs> parent = parseTree.parent(); 
		if( null == parent || APTs.APT_OUTPUT != parent.identifier() )
			return m_alternative;
			
		return super.produces( parseTree );
	} // produces
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Production#translate()
	 */
	public APT<APTs> translate( Symtable<SYMs> symtable,
			APT<APTs> parseTree, Token<TOKs>[] tstream ) {
		
		final APT<APTs> retval;
		try {
		
			if( null == symtable )
				throw new IllegalArgumentException();
			if( null == parseTree )
				throw new IllegalArgumentException();
			if( null == tstream )
				throw new IllegalArgumentException();
			
			switch( tstream.length ) {
			case 1:
				switch( tstream[ 0 ].identifier() ) {
				case CHARACTER_BRACKET_CLOSE:
					retval = parseTree.lookup( APTs.APT_STATEMENT );
					break;
					
				default:
					retval = parseTree;
					break;
				}
				break;
				
			case 2:
				final N_output lvalue    = ( N_output )tstream[ 0 ];
				final Token<TOKs> rvalue = tstream[ 1 ];
				
				final String variable_linkable_name;
				final String variable_link_name;
				final SYM_type_reference symbole;
				symbole = ( SYM_type_reference )symtable.lookup(
						SYM_type_reference.qualifier( lvalue.type() )
					);
				if( null == symbole ) {
					
					variable_linkable_name = null;
					variable_link_name = null;
				}
				else {
					
					variable_linkable_name = symtable.autoCreateIdentifier(
							"__" //$NON-NLS-1$
							+ lvalue.type()
							+ "_" //$NON-NLS-1$
							+ lvalue.__quantity()
							+ "_" //$NON-NLS-1$
						);
					symtable.register(
							variable_linkable_name
							, symbole
						);
					
					variable_link_name = symtable.autoCreateIdentifier(
							"__" //$NON-NLS-1$
							+ lvalue.type() 
							+ "_link_" //$NON-NLS-1$
							+ lvalue.__quantity()
							+ "_" //$NON-NLS-1$
						);
					symtable.register(
							variable_link_name
							, symtable.lookup( "Link" ) //$NON-NLS-1$
						);
				}
				
				final APT_statement __modeldef;
				final APT_io_definition __iodefs;
				switch( parseTree.identifier() ) {
				case APT_STATEMENT:
					__modeldef = ( APT_statement )parseTree;
					if( null != __modeldef.__io_defs() )
						__iodefs = __modeldef.__io_defs();
					else
						__modeldef.addChild(
								__iodefs = new APT_io_definition( __modeldef )
							);
					break;
					
				default:
					__modeldef = null;
					__iodefs = null;
				}
							
				// Initializing the model with the generated synthetic identifier
				//	(variable_name) and the corresponding model type identifier...
				final APT_output __output = new APT_output(
						__iodefs
						, lvalue
						, variable_linkable_name
						, symbole.type()
						, variable_link_name
						, "Link" //$NON-NLS-1$
						, rvalue
					);
				__iodefs.addChild( __output );
				
				// Initializing the arguments...
				final APT_argument_definition __argsdef;
					// Adding the default arguments (grassdb, location, mapset)
					//	to the "argument_definition" of the input...				
				__argsdef = ( APT_argument_definition )__output.argument_defs();
				P_argument.defaults( __argsdef );
					// Adding the type information arguments to the input...
				// Assign, flag: "quantityid", value: "quantity"
				final APT_argument __quantity;
				__quantity = ( APT_argument )__argsdef.addChild(
						0
						, new APT_argument( __argsdef )
					);
				__quantity.addChild(
						new APT_literal(
								__quantity
								, new N_literal(
										N_output.__idQuantity
										, 0
									)
							)
					);
				__quantity.addChild(
						new APT_literal(
								__quantity
								, new N_literal(
										lvalue.__quantity()
										, 0
									)
							)
					);
					// Adding the source arguments to the input...
				switch( tstream[ 1 ].identifier() ) {
				case CHARACTER_BRACKET_OPEN:
					// Assign, flag: "type_identifier", value: "*"
					final APT_argument __asterisk;
					__asterisk = ( APT_argument )__argsdef.addChild(
							0
							, new APT_argument( __argsdef )
						);
					__asterisk.addChild(
							new APT_literal(
									__asterisk
									, new N_literal(
											lvalue.type()
											, 0
										)
								)
						);
					__asterisk.addChild(
							new APT_literal(
									__asterisk
									, new T_asterisk( "*", 0 ) //$NON-NLS-1$
								)
						);
					retval = __output.nested_statement();
					break;

				case VARIABLE:
					// Assign, flag: "type_identifier", value: "literal"
					final APT_argument __variable;
					__variable = ( APT_argument )__argsdef.addChild(
							0
							, new APT_argument( __argsdef )
						);
					__variable.addChild(
							new APT_literal(
									__variable
									, new N_literal(
											lvalue.type()
											, 0
										)
								)
						);
					__variable.addChild(
							new APT_variable(
									__variable
									, rvalue
								)
						);
					retval = parseTree;
					break;
					
				case CHARACTER_ASTERISK:
				case LITERAL:
				case WORD:
					// Assign, flag: "type_identifier", value: "literal"
					final APT_argument __literal;
					__literal = ( APT_argument )__argsdef.addChild(
							0
							, new APT_argument( __argsdef )
						);
					__literal.addChild(
							new APT_literal(
									__literal
									, new N_literal(
											lvalue.type()
											, 0
										)
								)
						);
					__literal.addChild(
							new APT_literal(
									__literal
									, new N_literal(
											rvalue.expression()
											, 0
										)
								)
						);
					retval = parseTree;
					break;
					
				case DIRECTIVE_USAGE:
					(( APT_root )parseTree.root()).m_usage = true;
					__output.m_usage = true;
					retval = parseTree;
					break;
					
				default:
					retval = parseTree;
					break;
				}
				break;
				
			default:
				retval = parseTree;
				break;
			}
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
		
		return retval;
	} // translate
	
} // P_output
