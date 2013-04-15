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
 * <p>The production for a parameter of a <i>GRASS</i> command.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class P_parameter
    extends AbstractProduction<APTs, TOKs, SYMs>
    implements Production<APTs, TOKs, SYMs> {

// Construction
	/**
	 * <p>Constructs this production with their grammar productions.</p>
	 */
	public P_parameter() {
		
		super(
				new TOKs[][] {
						new TOKs[] {
								TOKs.LITERAL
							}
						, new TOKs[] {
								TOKs.UNKNOWN
							}
						, new TOKs[] {
								TOKs.VARIABLE
							}
						, new TOKs[] {
								TOKs.WORD
							}
						, new TOKs[] {
								TOKs.CHARACTER_ASSIGN
								, TOKs.LITERAL
							}
						, new TOKs[] {
								TOKs.CHARACTER_ASSIGN
								, TOKs.UNKNOWN
							}
						, new TOKs[] {
								TOKs.CHARACTER_ASSIGN
								, TOKs.VARIABLE
							}
						, new TOKs[] {
								TOKs.CHARACTER_ASSIGN
								, TOKs.WORD
							}
					}
			);
	} // P_parameter
	
// Operations
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
			final APT_native_model model;
			switch( parseTree.identifier() ) {
			case APT_STATEMENT:
				model = ( APT_native_model )
					(( APT_statement )parseTree).__model();
				break;
				
			case APT_NATIVE_MODEL:
				model = ( APT_native_model )parseTree;
				break;
				
			case APT_ROOT:
				model = ( APT_native_model )
					(( APT_root )parseTree).statement().__model();
				break;
				
			default:
				final APT<APTs> candidate = parseTree.lookup(
						APTs.APT_STATEMENT
					);
				model = ( APT_native_model )
					(( APT_statement )candidate).__model();
				break;
			}
			
			final APT_parameter_definition root;
			if( null != model.parameter_defs() )
				root = model.parameter_defs();
			else
				root = ( APT_parameter_definition )model.addChild(
						new APT_parameter_definition( model )
					);
			
			final APT_parameter tree;
			switch( tstream[ 0 ].identifier() ) {
			case CHARACTER_ASSIGN:
				tree = ( APT_parameter )root.getChild( root.size() - 1 );
				break;
				
			default:
				tree = ( APT_parameter )root.addChild(
						new APT_parameter( root )
					);
			}
			for( int i = 0; i < tstream.length; ++ i ) {
				
				Token<TOKs> __token = tstream[ i ];
				switch( __token.identifier() ) {
				case VARIABLE:
					tree.addChild(
							new APT_variable(
									root
									, __token
								)
						);
					break;
					
				case LITERAL:
					tree.addChild(
							new APT_literal(
									root
									, __token
								)
						);
					break;
					
				case UNKNOWN:
				case WORD:
					tree.addChild(
							new APT_literal(
									root
									, new N_literal(
											__token.expression()
											, __token.line()
										)
								)
						);
					break;
					
				default:
					tree.addChild(
							new APT_literal(
									root
									, __token
								)
						);
				}
			}
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
		
		return parseTree;
	} // translate
	
} // P_parameter
