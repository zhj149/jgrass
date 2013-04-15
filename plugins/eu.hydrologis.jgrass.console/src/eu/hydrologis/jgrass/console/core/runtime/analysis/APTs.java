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
package eu.hydrologis.jgrass.console.core.runtime.analysis;

/**
 * <p>The enumeration <code>APTs</code> defines the named constants to identify
 * either an operator or an operand of an <acronym title="abstract parse tree"
 * >APT</acronym> by its named constant - this is the <i>identity scope</i> of
 * an operator or operand - instead to identify it using the Java
 * <code>instanceof</code> operator.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public enum APTs {

	/**
	 * <p>This named constant identifies an argument expression of a Java based
	 * model. In general, an argument expression is an abstract parse tree
	 * operator consisting of two operands: at the left-side the argument flag
	 * (key) and on the right-side, the argument value.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_argument
	 */
	APT_ARGUMENT( null, "ARGUMENT" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies an argument definition operator, either
	 * of a simulation model or of a linkable input/output exchange in a Java
	 * based model statement, which groups the arguments for each of them - an
	 * argument definition operator has as operands only one or more argument
	 * expressions.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_argument_definition
	 */
	APT_ARGUMENT_DEFINITION( null, "ARGUMENT_DEF" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies a linkable input exchange identifier
	 * operator/operand of a Java based simulation model.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_input
	 */
	APT_INPUT( null, "INPUT" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies a linkable input/output exchange
	 * definition, which groups the input/output arguments of the corresponding
	 * Java based simulation model. A linkable input/output exchange definition
	 * operator has one or more input/output argument operands.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_io_definition
	 */
	APT_IO_DEFINITION( null, "IO_DEF" ), //$NON-NLS-1$
	
	/**
	 * <p>A literal string operand is identified by this named constant.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_literal
	 */
	APT_LITERAL( null, "LITERAL" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies a Java based simulation model
	 * identifier operator/operand.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_java_model
	 */
	APT_JAVA_MODEL( null, "JAVA_MODEL" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies a native based command identifier
	 * operator/operand.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_native_model
	 */
	APT_NATIVE_MODEL( null, "NATIVE_MODEL" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies a linkable output exchange identifier
	 * operator/operand of a Java based simulation model.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_output
	 */
	APT_OUTPUT( null, "OUTPUT" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies a parameter expression of a native
	 * based command. In general, a parameter expression is an abstract parse
	 * tree operator consisting of one or more operands making up a parameter
	 * of a native based command.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_parameter
	 */
	APT_PARAMETER( null, "PARAMETER" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies a parameter definition operator, which
	 * groups the parameters of a native based command - a parameter definition
	 * operator has as operands only one or more parameter expressions.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_parameter_definition
	 */
	APT_PARAMETER_DEFINITION( null, "PARAMETER_DEFINITION" ), //$NON-NLS-1$
	
	/**
	 * <p>The <code>APT_ROOT</code> named constant identifies the root operator
	 * of the <acronym title="abstract parse tree">APT</acronym>.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_root
	 */
	APT_ROOT( "APT_ROOT", "0" ), //$NON-NLS-2$ //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies a simulation model statement operator
	 * that groups the argument definitions and the linkable input/output
	 * exchange definitions of a simulation model.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_statement
	 */
	APT_STATEMENT( null, "STMT" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies an external variable identifier
	 * operand.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_variable
	 */
	APT_VARIABLE( null, "IDENTIFIER" ), //$NON-NLS-1$
	
	/**
	 * <p>This named constant identifies an opertor of a synthetic variable
	 * definition for either a simulation model or a linkable input/outout
	 * exchange operand.</p>
	 * @see eu.hydrologis.jgrass.console.core.runtime.nodes.APT_variable_definition
	 */
	APT_VARIABLE_DEFINITION( null, "VARIABLE_DEF" ); //$NON-NLS-1$
	
// Attributes
	/**
	 * <p>A meaningfull abbreviation that describs this named constant.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#annotation()
	 */
	private final String m_annotation;
	
	/**
	 * <p>A pre-defined expression, if any, otherwise <code>null</code>.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#expression()
	 */
	private final String m_expression;
	
// Construction
	/**
	 * <p>The constructor <code>APTs</code> constructs the enumeration named 
	 * constant.</p>
	 * @param expression
	 * 		A pre-defined expression, if available, otherwise <code>null</code>.
	 * @param annotation
	 * 		A annotation describing the named constant.
	 */
	private APTs( String expression, String annotation ) {
		
		if( null == annotation || 0 >= annotation.length() )
			throw new IllegalArgumentException();
		
		m_annotation = annotation.trim();
		m_expression = expression;
	} // APTs
	
// Operations
	/**
	 * <p>The method <code>annotation</code> returns a human readable term,
	 * which describs the contents of this named constant.</p>
	 * @return
	 * 		Returns the annotation of this named constant.
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#annotation()
	 */
	public String annotation() {
		
		return m_annotation;
	} // annotation
	
	/**
	 * <p>The method <code>expression</code> returns the expression of this
	 * named constant.</p>
	 * @return
	 * 		Returns either the expression of this named constant or
	 * 		<code>null</code>, if this named constant has no pre-defined
	 * 		expression.
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#expression()
	 */
	public String expression() {
		
		return m_expression;
	} // expression
	
} // APTs
