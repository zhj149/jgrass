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
 * <p>The production for an argument of a <i>JGRASS</i> model.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class P_argument
    extends AbstractProduction<APTs, TOKs, SYMs>
    implements Production<APTs, TOKs, SYMs> {

// Construction
	/**
	 * <p>Constructs this production with their grammar productions.</p>
	 */
	public P_argument() {
		
		super(
				new TOKs[][] {
						new TOKs[] {
								TOKs.ARGUMENT
							}
						, new TOKs[] {
								TOKs.LITERAL
							}
						, new TOKs[] {
								TOKs.DIRECTIVE_USAGE
							}
						, new TOKs[] {
								TOKs.VARIABLE
							}
						, new TOKs[] {
								TOKs.WORD
							}
						, new TOKs[] {
								TOKs.ARGUMENT
								, TOKs.LITERAL
							}
						, new TOKs[] {
								TOKs.ARGUMENT
								, TOKs.VARIABLE
							}
						, new TOKs[] {
								TOKs.ARGUMENT
								, TOKs.WORD
							}
					}
			);
	} // P_argument
	
// Operations
	/**
	 * <p>Adds the default arguments to a linkable component.</p>
	 * @param operand
	 * 		- a <b>argument definition</b> operand.
	 */
	public static void defaults( APT_argument_definition operand ) {
		
		// Assign, flag: "grassdb", value: grassdb
		final APT_argument __grassdb = ( APT_argument )operand.addChild(
				new APT_argument( operand )
			);
		__grassdb.addChild(
				new APT_literal(
						__grassdb
						, new N_literal(
								Projectspace.ARG_VERB_GRASSDB.toLowerCase()
								, 0
							)
					)
			);
		__grassdb.addChild(
				new APT_variable(
						__grassdb
						, new N_variable(
								"$__global_grassdb" //$NON-NLS-1$
								, 0
							)
					)
			);
		
		// Assign, flag: "location", value: location
		final APT_argument __location = ( APT_argument )operand.addChild(
				new APT_argument( operand )
			);
		__location.addChild(
				new APT_literal(
						__location
						, new N_literal(
								Projectspace.ARG_VERB_LOCATION.toLowerCase()
								, 0
							)
					)
			);
		__location.addChild(
				new APT_variable(
						__location
						, new N_variable(
								"$__global_location" //$NON-NLS-1$
								, 0
							)
					)
			);
		
		// Assign, flag: "mapset", value: mapset
		final APT_argument __mapset = ( APT_argument )operand.addChild(
				new APT_argument( operand )
			);
		__mapset.addChild(
				new APT_literal(
						__mapset
						, new N_literal(
								Projectspace.ARG_VERB_MAPSET.toLowerCase()
								, 0
							)
					)
			);
		__mapset.addChild(
				new APT_variable(
						__mapset
						, new N_variable(
								"$__global_mapset" //$NON-NLS-1$
								, 0
							)
					)
			);
		
		// START ADDITION Andrea Antonello - andrea.antonello@gmail.com
		// Assign, flag: "startdate", value: startdate
		final APT_argument __startdate = ( APT_argument )operand.addChild(
		        new APT_argument( operand )
		);
		__startdate.addChild(
		        new APT_literal(
		                __startdate
		                , new N_literal(
		                        Projectspace.ARG_VERB_TIME_START_UP.toLowerCase()
		                        , 0
		                )
		        )
		);
		__startdate.addChild(
		        new APT_variable(
		                __startdate
		                , new N_variable(
		                        "$__global_startdate" //$NON-NLS-1$
		                        , 0
		                )
		        )
		);
		// Assign, flag: "enddate", value: enddate
		final APT_argument __enddate = ( APT_argument )operand.addChild(
		        new APT_argument( operand )
		);
		__enddate.addChild(
		        new APT_literal(
		                __enddate
		                , new N_literal(
		                        Projectspace.ARG_VERB_TIME_ENDING_UP.toLowerCase()
		                        , 0
		                )
		        )
		);
		__enddate.addChild(
		        new APT_variable(
		                __enddate
		                , new N_variable(
		                        "$__global_enddate" //$NON-NLS-1$
		                        , 0
		                )
		        )
		);
		// Assign, flag: "deltat", value: deltat
		final APT_argument __deltat = ( APT_argument )operand.addChild(
		        new APT_argument( operand )
		);
		__deltat.addChild(
		        new APT_literal(
		                __deltat
		                , new N_literal(
		                        Projectspace.ARG_VERB_TIME_DELTA.toLowerCase()
		                        , 0
		                )
		        )
		);
		__deltat.addChild(
		        new APT_variable(
		                __deltat
		                , new N_variable(
		                        "$__global_deltat" //$NON-NLS-1$
		                        , 0
		                )
		        )
		);
        // Assign, flag: "remotedburl", value: remotedburl
        final APT_argument __remotedburl = ( APT_argument )operand.addChild(
                new APT_argument( operand )
        );
        __remotedburl.addChild(
                new APT_literal(
                        __remotedburl
                        , new N_literal(
                                Projectspace.ARG_VERB_REMOTEDB.toLowerCase()
                                , 0
                        )
                )
        );
        __remotedburl.addChild(
                new APT_variable(
                        __remotedburl
                        , new N_variable(
                                "$__global_remotedb" //$NON-NLS-1$
                                , 0
                        )
                )
        );
		// STOP ADDITION
	} // defaults
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Production#produces(Symtable,APT)
	 */
	public TOKs[][] produces( APT<APTs> parseTree ) {
		
		return super.produces( parseTree );
	} // produces
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Production#translate()
	 */
	public APT<APTs> translate( Symtable<SYMs> symtable,
			APT<APTs> parseTree, Token<TOKs>[] tstream ) {
		
		try {
		
			if( null == symtable )
				throw new IllegalArgumentException();
			if( null == parseTree )
				throw new IllegalArgumentException();
			if( null == tstream )
				throw new IllegalArgumentException();
			
			// Gathering required informations
			final APT_java_model __model;
			switch( parseTree.identifier() ) {
			case APT_STATEMENT:
				__model = ( APT_java_model )
					(( APT_statement )parseTree).__model();
				break;
				
			case APT_JAVA_MODEL:
				__model = ( APT_java_model )parseTree;
				break;
				
			case APT_ROOT:
				__model = ( APT_java_model )
					(( APT_root )parseTree).statement().__model();
				break;
				
			default:
				final APT<APTs> candidate = parseTree.lookup(
						APTs.APT_STATEMENT
					);
				__model = ( APT_java_model )
					(( APT_statement )candidate).__model();
				break;
			}
			final APT_argument_definition __model_argsdef;
				__model_argsdef = __model.argument_defs();
			final APT_variable_definition __model_variable_def;
				__model_variable_def = __model.linkable_variable_def();
			final SYM_type_java_model __model_typedef;
				__model_typedef = ( SYM_type_java_model ) 
					symtable.lookup(
							__model_variable_def.variable_name()
						);
			
			final N_argument lvalue;
			final Token<TOKs> rvalue;
			switch( tstream.length ) {
			case 1:
				rvalue = tstream[ 0 ];
				switch( rvalue.identifier() ) {
				case DIRECTIVE_USAGE:
					(( APT_root )parseTree.root()).m_usage = true;
					__model.m_usage = true;
					lvalue = null;
					break;
					
				default:
					if( false == __model_typedef.hasDefaultKey() ) {
						
						lvalue = null;
					}
					else {
					
						lvalue = new N_argument(
								__model_typedef.defaultKey()
								, rvalue.line()
							);
					}
				}
				break;
				
			case 2:
				lvalue = ( N_argument )tstream[ 0 ];
				rvalue = tstream[ 1 ];
				break;
				
			default:
				lvalue = null;
				rvalue = null;
			}
			
			if( null != lvalue && null != rvalue ) {
				
				final APT_argument __argument;
					__argument = ( APT_argument )__model_argsdef.addChild(
							new APT_argument( __model_argsdef )
						);
				final APT<APTs> __argument_flag = new APT_literal(
						__argument
						, new N_literal(
								lvalue.argument_flag()
								, lvalue.line()
							)
					);
				final APT<APTs> __argument_value;
				switch( rvalue.identifier() ) {
				case VARIABLE:
					__argument_value = new APT_variable(
							__argument
							, rvalue
						);
					break;
					
				case LITERAL:
				case WORD:
				default:
					__argument_value = new APT_literal(
							__argument
							, new N_literal(
									rvalue.expression()
									, rvalue.line()
								)
						);
					break;
				}
				__argument.addChild( __argument_flag );
				__argument.addChild( __argument_value );
			}
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
		
		return parseTree;
	} // translate
	
} // P_argument
