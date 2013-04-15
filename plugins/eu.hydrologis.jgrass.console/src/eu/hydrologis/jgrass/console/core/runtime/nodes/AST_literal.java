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

import java.io.File;

import eu.hydrologis.jgrass.console.core.internal.nodes.AST;
import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAST;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.ASTs;

/**
 * <p>The <i>abstract syntax tree</i> operand represents a <b><i>literal
 * string</i></b>, e.g., "<b>\"</b><i>string</i><b>\"</b>".</p>
 * <p>Example:<br/><br/><i>Java programming construct:</i><code><pre>
 * string = "Hello World"
 * </pre></code>
 * <i><b>Abstract Syntax Tree</b> tree-like intermediate representation:</i>
 * <code><pre>
 * <b>new</b> AST_assign_statement(
 * 	<b>new</b> AST_identifier( "string" )
 * 	, <b>new</b> AST_number_integer( "Hello World" )
 * 	);
 * </pre></code></p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAST
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.ASTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class AST_literal
    extends AbstractAST<ASTs>
    implements AST<ASTs> {

// Construction
	/**
	 * <p>Constructs this object with the specified string.</p>
	 * @param string
	 * 		- a string.
	 */
	public AST_literal( String string ) {
	
		super( __cleanSweep( string )
				, ASTs.AST_LITERAL.annotation()
				, ASTs.AST_LITERAL
			);
	} // AST_literal
	
	/**
	 * <p>The method <code>__cleanSweep</code> formats a specified string into
	 * a literal string.</p>
	 * @param string
	 * 		- a string.
	 * @return
	 * 		The literal string.
	 */
	private static String __cleanSweep( String string ) {
		
		String retval = null;
		try {
			
			// First, getting the tokens 'safe expression' and if the expression
			//	is not equal to 'null', we can move forward to format the
			//	tokens expression in the way we need...
			if( null != string ) {
				
				// Creating the literal string buffer by removing resp. first by
				//	replacing any quotation character in the expression with
				//	space characters before we omitt leading and trailing
				//	whitespace...
				final StringBuffer literalString = new StringBuffer();
				final int length = string.length();
				for( int index = 0; index < length; ++index ) {
					
					Character character = string.charAt( index );
					switch( character.charValue() ) {
					case '\'':
						literalString.append( "\\" + character.charValue() ); //$NON-NLS-1$
                        break;

                    case '\"':
                    	literalString.append( "\\" + character.charValue() ); //$NON-NLS-1$
                        break;
                        
                    case '\\':
                    	if( File.separatorChar != character.charValue() ) {
                    		
                    		literalString.append( File.separator );
                    	}
                    	else {
                    		
                    		literalString.append(
                    				File.separator
                    				+ File.separator
                    			);
                    	}
                    	break;
                    	
                    case '/':
                        literalString.append( character.charValue() );
//                    	if( File.separatorChar == character.charValue() ) {
//                    	
//                    		literalString.append( File.separator );
//                    	}
//                    	else {
//                    		
//                    		literalString.append(
//                    				File.separator
//                    				+ File.separator
//                    			);
//                    	}
                    	break;
                    
                    case '\b':
                    	// backspace
                    	literalString.append( "\\b" ); //$NON-NLS-1$
                    	break;
                    	
                    case '\f':
                    	// form-feed
                    	literalString.append( "\\f" ); //$NON-NLS-1$
                    	break;
                    	
                    case '\n':
                    	// line-feed
                    	literalString.append( "\\n" ); //$NON-NLS-1$
                    	break;
                    	
                    case '\r':
                    	// carriage return
                    	literalString.append( "\\r" ); //$NON-NLS-1$
                    	break;
                    	
                    case '\t':
                    	// Horizontal tab 
                    	literalString.append( "\\t" ); //$NON-NLS-1$
                    	break;
                    	
                    default:
                    	literalString.append( character.toString() );
					}
				}
				
				retval = new String( literalString.toString() );
			}
		}
		catch( Exception e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			e.printStackTrace();
		}
		
		return retval;
	} // __cleanSweep
	
} // AST_literal