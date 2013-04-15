/*	History:
 ===============================================================================
 Rev.  Autor Date		PR		Text
             yy/dd/mm
 -------------------------------------------------------------------------------
 001   aha   yy/dd/mm			Date of publishing the first version.
 */
package eu.hydrologis.jgrass.console.core.runtime.analysis;

import java.text.MessageFormat;

import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerError;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <h1>Compiler Error 3102</h1>
 * <h2>Error Message</h2>
 * <p>use of java model identifier 'identifier' : expected native model identifier.</p>
 * <p>The compiler expected a native model identifier and found a java model
 * <i>identifier</i> instead.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
@SuppressWarnings("serial")
public class E3102
	extends CompilerError {

// Construction
	/**
	 * <p>The constructor <code>E3102</code> constructs the message.</p>
	 * @param projectSpace
	 * 		- the current project space.
	 * @param token
	 * 		- the identifier.
	 */
	public E3102( Projectspace projectSpace, Token<TOKs> token ) {
	
		super(
			MessageFormat.format(
					"{0}({1}) : error {2}: use of java model identifier ''{3}'' : expected native model identifier."
					, new Object[] {
						projectSpace.projectCaption()
						, token.line()
						, E3104.class.getSimpleName()
						, token.expression()
					}
				)
			);
	} // E3102
	
} // E3102
