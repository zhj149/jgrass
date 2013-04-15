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
package eu.hydrologis.jgrass.console.core.internal.compiler;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import eu.hydrologis.jgrass.console.core.internal.nodes.Symtable;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>The class <code>AbstractPreprocessor</code> provides the default behavior
 * of the method <code>compile</code>, which rules the compilation process, and
 * implements a helper for code indentions at generation time. A developer who
 * subclasses this abstract class need to define the <code>projectSymbols</code>
 * and all other missing methods.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractPreprocessor<P, X, T, S>
	extends AbstractCompiler
    implements Preprocessor<P, X, T, S> {
	
// Construction
	/**
	 * <p>The copy constructor <code>AbstractPreprocessor</code> creates this
	 * pre-processor with the specified project-space.</p>
	 * @param projectSpace
	 * 		- the project-space.
	 * @throws IllegalArgumentException
	 * 		- if <code>projectSpace</code> references the null type.
	 */
	public AbstractPreprocessor( Projectspace projectSpace ) {
		
		super( projectSpace );
	} // AbstractPreprocessor
	
// Operations
	/**
	 * <p>The method <code>indentions</code> is the helper for code indentions
	 * at generation time. Typically the method <code>indentions</code> is used
	 * by the method <code>generate</code> in order to structure the generated
	 * target code.</p>
	 * @param indentCount
	 * 		- counts the tabulator indentions to produced.
	 * @return
	 * 		A string comprising only tabulator characters.
	 */
	protected static String indentions( int indentCount ) {
		
		StringBuffer retval = new StringBuffer( indentCount );
		for( int i = 0; i < indentCount; ++i )
			retval.append( '\t' );
		
		return retval.toString();
	} // indentions
	
	/**
	 * <p>The method <code>projectSymbols</code> creates a symbole table and
	 * initialize it using the specified project-space to map the required 
	 * information for the subsequent phases analysis (syntax and semantic) and
	 * synthesis - e.g., reserved words, type definitions -  into the symbol
	 * table.</p>
	 * @param projectSpace
	 * 		- the project-space.
	 * @return
	 * 		A initialized symbol table with reserved words, type definitions and
	 * 		constant values.
	 */
	protected abstract Symtable<S> projectSymbols( Projectspace projectSpace );

	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.compiler.Preprocessor#compile()
	 */
	public Reader compile( Reader sourceCode, int line ) {
		
		Reader retval = null;
			
		Writer writer = null;
		try {
		
			Symtable<S> __symtable = projectSymbols( projectSpace() );
			writer = new StringWriter();
			generate(
					0
					, translate(
							__symtable
							, intermediate(
									__symtable
									, sourceCode
									, line
								)
						)
					, writer
				);
			writer.flush();
		}
		catch( IOException e ) {

			if( true == Projectspace.isErrorEnabled() )
				projectSpace().err.println( e );

			e.printStackTrace();
		}
		finally {
			
			if( null != writer ) {
				
				String targetCode = writer.toString();
				if( null != targetCode && 0 < targetCode.length() )
					retval = new StringReader( targetCode );
			}
		}
		
		return retval;
	} // compile
	
} // AbstractPreprocessor
