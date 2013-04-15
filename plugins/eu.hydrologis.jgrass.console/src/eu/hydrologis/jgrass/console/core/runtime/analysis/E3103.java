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
 * <h1>Compiler Error 3103</h1>
 * <h2>Error Message</h2>
 * <p>unknown native model identifier 'identifier'.</p>
 * <p>The compiler expected <i>identifier</i> and found <i>token</i> instead.
 * Possible causes:<ul type="1"><li>Spelling or capitalization error of a model
 * identifier.</li>
 * <li></li>Missing '$' operator before variable identifier.</ul></p>
 * <p>This error may be caused by a typographical error.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
@SuppressWarnings("serial")
public class E3103
	extends CompilerError {

// Construction
	/**
	 * <p>The constructor <code>E3103</code> constructs the message.</p>
	 * @param projectSpace
	 * 		- the current project space.
	 * @param token
	 * 		- the identifier.
	 */
	public E3103( Projectspace projectSpace, Token<TOKs> token ) {
	
		super(
			MessageFormat.format(
					"{0}({1}) : error {2}: unknown native model identifier ''{3}''."
					, new Object[] {
						projectSpace.projectCaption()
						, token.line()
						, E3103.class.getSimpleName()
						, token.expression()
					}
				)
			);
	} // E3103
	
} // E3103
