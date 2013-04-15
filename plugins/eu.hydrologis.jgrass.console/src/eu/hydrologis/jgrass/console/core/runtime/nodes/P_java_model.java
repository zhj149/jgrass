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
 * <p>The production for a <i>JGRASS</i> model.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class P_java_model
	extends AbstractProduction<APTs, TOKs, SYMs>
	implements Production<APTs, TOKs, SYMs> {
	
// Construction
	/**
	 * <p>Constructs this production with their grammar productions.</p>
	 */
	public P_java_model() {
		
		super(
				new TOKs[][] {
						new TOKs[] {
								TOKs.JAVA_MODEL
							}
						, new TOKs[] {
								TOKs.VARIABLE
								, TOKs.CHARACTER_ASSIGN
								, TOKs.JAVA_MODEL
							}
					}
			);
	} // P_java_model
	
// Operations
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
			
			final N_variable lvalue;
			final Token<TOKs> operator;
			final N_java_model rvalue;
			switch( tstream.length ) {
			case 1:
				lvalue   = null;
				operator = null;
				rvalue   = ( N_java_model )tstream[ 0 ];
				break;
				
			case 3:
				lvalue   = ( N_variable )tstream[ 0 ];
				operator = tstream[ 1 ];
				rvalue   = ( N_java_model )tstream[ 2 ];
				break;
				
			default:
				lvalue   = null;
				operator = null;
				rvalue   = null;
			}
			
			final APT_statement __statement;
			switch( parseTree.identifier() ) {
			case APT_ROOT:
				(( APT_root )parseTree).addChild(
						__statement = new APT_statement(
								parseTree
							)
					);
				break;
				
			case APT_STATEMENT:
				__statement = ( APT_statement )parseTree;
				break;
			
			default:
				__statement = null;
			}
			
			final String variable_linkable_name;
			final String variable_link_name;
			final SYM_type_reference symbole;
			symbole = ( SYM_type_reference )symtable.lookup(
					SYM_type_reference.qualifier( rvalue.type() )
				);
			if( null == symbole ) {
				
				variable_linkable_name = null;
				variable_link_name = null;
			}
			else {
				
				variable_linkable_name = symtable.autoCreateIdentifier(
						"__" //$NON-NLS-1$
						+ symbole.type()
						+ "_" //$NON-NLS-1$
					);
				symtable.register(
						variable_linkable_name
						, symbole
					);
				
				variable_link_name = symtable.autoCreateIdentifier(
						"__link_" //$NON-NLS-1$
						+ symbole.type()
						+ "_" //$NON-NLS-1$
					);
				symtable.register(
						variable_link_name
						, symtable.lookup( "Link" ) //$NON-NLS-1$
					);
			}
			
			// Initializing the external variable...
			if( null != operator &&
					TOKs.CHARACTER_ASSIGN == operator.identifier() &&
					null != lvalue ) {
				
				__statement.addChild(
						new APT_variable(
								__statement
								, lvalue
							)
					);
			}
			
			// Initializing the model with the generated synthetic identifier
			//	(variable_name) and the corresponding model type identifier...
			final APT_java_model __model = new APT_java_model(
					__statement
					, rvalue
					, variable_linkable_name
					, symbole.type()
					, variable_link_name
					, "Link" //$NON-NLS-1$
				);
			__statement.addChild( __model );
			
			// Adding the default arguments (grassdb, location, mapset)
			//	to the "argument_definition" of the model...
			P_argument.defaults( __model.argument_defs() );
			
			retval = __statement;
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
		
		return retval;
	} // translate
	
} // P_java_model
