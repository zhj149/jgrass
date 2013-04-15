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
 * <p>The production for a <i>GRASS</i> command.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class P_native_model
	extends AbstractProduction<APTs, TOKs, SYMs>
	implements Production<APTs, TOKs, SYMs> {
	
// Construction
	/**
	 * <p>Constructs this production with their grammar productions.</p>
	 */
	public P_native_model() {
		
		super(
				new TOKs[][] {
						new TOKs[] {
								TOKs.NATIVE_MODEL
							}
					}
			);
	} // P_native_model
	
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
			
			// Initializing the model with the generated synthetic identifier
			//	(variable_name) and the corresponding model type identifier...
			final N_native_model rvalue;
				rvalue = ( N_native_model )tstream[ tstream.length - 1 ];
			final APT_native_model __model = new APT_native_model(
					__statement
					, rvalue
				);
			__statement.addChild( __model );
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
