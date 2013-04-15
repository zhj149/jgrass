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
 * <p>
 * The enumeration <code>ASTs</code> defines the namend constants to identify either an operator or
 * an operand of an <acronym title="abstract syntax tree" >AST</acronym> by its named constant -
 * this is the <i>idendity scope</i> of an operator or operand - instead to identify it using the
 * Java <code>instanceof</code> operator.
 * </p>
 * 
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public enum ASTs {

    /**
     * <p>
     * The identifier of an operator of an <b><i>assign statement</i></b>, e.g.,
     * "oparand1 <b>=</b> operand2".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_assign_statement
     */
    AST_ASSIGN_STATEMENT("=", "ASSIGN"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of a code <b><i>block</i></b>, e.g.,
     * "<b>{</b> operand1 operand2 ... <b>}</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_block
     */
    AST_BLOCK(null, "BLOCK"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operand of the boolean value <code><b>false</b></code>, e.g., as a
     * reserved word "<b>false</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_bool_false
     */
    AST_BOOL_FALSE("false", "BOOL_FALSE"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operand of the boolean value <code><b>true</b></code>, e.g., as a
     * reserved word "<b>true</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_bool_true
     */
    AST_BOOL_TRUE("true", "BOOL_TRUE"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of the <code><b>catch</b></code> keyword of a <code>try</code>-
     * <code>catch</code> block statement, e.g., "<b>catch(</b> operand1 operand2 ... <b>)</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_catch
     */
    AST_CATCH("catch", "CATCH_BLOCK"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator for <b><i>comma</i></b> punctuation, e.g.,
     * "oparand1<b>,</b> operand2<b>,</b> ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_comma
     */
    AST_COMMA(",", "COMMA"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator represents the <b><i>parenthesis of conditions</i></b>, e.g.,
     * "<b>(</b> operand1 operand2 ... <b>)</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_condition
     */
    AST_CONDITION(null, "CONDITION"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of a <b><i>conditional and</i></b> - <b>&&</b> -
     * <b><i>releation</i></b>, e.g., "operand1 <b>&&</b> operand2 <b>&&</b> ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_condition
     */
    AST_CONDITIONAL_AND("&&", "CONDITIONAL_AND"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of a <b><i>conditional or</i></b> - <b>||</b> -
     * <b><i>releation</i></b>, e.g., "operand1 <b>||</b> operand2 <b>||</b> ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_condition
     */
    AST_CONDITIONAL_OR("||", "CONDITIONAL_OR"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator for a <b><i>constructor call</i></b> respectively of the
     * <code><b>new</b></code> keyword, e.g., "<b>new</b> operand1 operand2 ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_ctor_call
     */
    AST_CTOR_CALL(null, "CTOR_CALL"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator for <b><i>dot</i></b> punctuation, e.g.,
     * "oparand1<b>.</b>operand2<b>.</b>...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_dot
     */
    AST_DOT(".", "DOT"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator for a <b><i>list of elements</i></b>, e.g.,
     * "<b>(</b> oparand1<b>,</b> operand2<b>,</b> ... <b>)</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_elist
     */
    AST_ELIST(null, "ELIST"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of the <b><i>else</i></b> block respectively the
     * <code><b>else</b></code> keyword of an <code>if</code> statement, e.g.,
     * "<b>else</b> <b>{</b> operand ... <b>}</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_else
     */
    AST_ELSE("else", "ELSE"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator for an <b><i>expression</i></b> punctuation respectively of a
     * <b><i>statement programming construct</i></b>, e.g., "oparand1<b>;</b> operand2<b>;</b> ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_expression
     */
    AST_EXPRESSION(null, "EXPR"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of the <code><b>finally</b></code> keyword of a
     * <code>try</code>-<code>catch</code>-<code>finally</code> or <code>try</code>-
     * <code>finally</code> block statement, e.g., "<b>finally</b> operand1 operand2 ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_finally
     */
    AST_FINALLY("finally", "FINALLY_BLOCK"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operand represents a <b><i>name of an identifier</i></b>, e.g., denoting
     * a variable name - "<i>identifier</i>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_identifier
     */
    AST_IDENTIFIER(null, "IDENT"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of the <code><b>if</b></code> keyword, e.g.,
     * "<b>if</b> operand1 operand2 ... ".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_if
     */
    AST_IF("if", "IF"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operand represents a <b><i>literal string</i></b>, e.g.,
     * "<b>\"</b><i>string</i><b>\"</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_literal
     */
    AST_LITERAL(null, "LITERAL"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator for the logical condition <b><i>equal</i></b>, e.g.,
     * "oparand1 <b>==</b> operand2".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_logical_equal
     */
    AST_LOGICAL_EQUAL("==", "LOGICAL_EQUAL"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator for the logical condition <b><i>not equal</i></b>, e.g.,
     * "oparand1 <b>!=</b> operand2".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_logical_unequal
     */
    AST_LOGICAL_UNEQUAL("!=", "LOGICAL_UNEQUAL"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator for a <b><i>method call</i></b>, e.g., "operand1 operand2 ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_method_call
     */
    AST_METHOD_CALL(null, "METHOD_CALL"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operand represents the <code>null</code> keyword, e.g., "<b>null</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_null
     */
    AST_NULL("null", "NULL"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operand represents an <b><i>integer number</i></b>, e.g., "60".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_number_integer
     */
    AST_NUMBER_INTEGER(null, "NUM_INTEGER"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator either of a <b><i>string concatenation</i></b> or an
     * <b><i>arithmetic plus</i></b> operation, e.g., "oparand1 <b>+</b> operand2 <b>+</b> ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_plus
     */
    AST_PLUS("+", "PLUS"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The <code>AST_ROOT</code> named constant identifies the root operator of the <acronym
     * title="abstract syntax tree">AST</acronym>.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_root
     */
    AST_ROOT("AST_ROOT", "0"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * This identifier of an operator, is an operator for a <b><i>list of statements</b></i>, e.g.,
     * "oparand1 operand2 ..." - makes sure that each one starts in a new line.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_slist
     */
    AST_SLIST(null, "SLIST"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator represents the <b><i>then</i></b> block of an <code>if</code>
     * statement, e.g., "<b>{</b> operand ... <b>}</b>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_then
     */
    AST_THEN(null, "THEN"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of the <code><b>throw</b></code> keyword, e.g.,
     * "<b>throw</b> operand1 operand2 ... ".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_throw_call
     */
    AST_THROW_CALL(null, "THROW_CALL"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of the <code><b>try</b></code> keyword of a <code>try</code>-
     * <code>catch</code>-<code>finally</code> or <code>try</code>-<code>finally</code> block
     * statement, e.g., "<b>try</b> operand1 operand2 ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_try
     */
    AST_TRY("try", "TRY_BLOCK"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operand of a <b><i>type name</i></b>, e.g., "<i>Integer</i>",
     * "<i>int</i>", "<i>java.lang.String</i>".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_type
     */
    AST_TYPE(null, "TYPE"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of a <b><i>variable definition</i></b>, e.g.,
     * "operand1 operand2 ...".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_variable_definition
     */
    AST_VARIABLE_DEFINITION(null, "VARIABLE_DEF"), //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of the <code><b>while</b></code> keyword for a
     * <code>while</code> loop programming construct, e.g., "<b>while</b> operand1 operand2 ... ".
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_while
     */
    AST_WHILE("while", "WHILE_LOOP"), //$NON-NLS-2$ //$NON-NLS-1$

    /**
     * <p>
     * The identifier of an operator of the <code><b>array</b></code> keyword for the creation of an
     * array e.g., "<b>array[2];</b> array[0] = string1; array[1] = string2;
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.nodes.AST_array
     */
    AST_ARRAY(null, "ARRAY"); //$NON-NLS-1$

    // Attributes
    /**
     * <p>
     * A meaningful abbreviation that describes this named constant.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#annotation()
     */
    private final String m_annotation;

    /**
     * <p>
     * A pre-defined expression, if any, otherwise <code>null</code>.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#expression()
     */
    private final String m_expression;

    // Construction
    /**
     * <p>
     * The constructor <code>ASTs</code> constructs the enumeration named constant.
     * </p>
     * 
     * @param expression A pre-defined expression, if available, otherwise <code>null</code>.
     * @param annotation A annotation describing the named constant.
     */
    private ASTs( String expression, String annotation ) {

        if (null == annotation || 0 >= annotation.length())
            throw new IllegalArgumentException();

        m_annotation = annotation;
        m_expression = expression;
    } // ASTs

    // Operations
    /**
     * <p>
     * The method <code>annotation</code> returns a human readable term, which describs the contents
     * of this named constant.
     * </p>
     * 
     * @return Returns the annotation of this named constant.
     * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#annotation()
     */
    public String annotation() {

        return m_annotation;
    } // annotation

    /**
     * <p>
     * The method <code>expression</code> returns the expression of this named constant.
     * </p>
     * 
     * @return Returns either the expression of this named constant or <code>null</code>, if this
     *         named constant has no pre-defined expression.
     * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#expression()
     */
    public String expression() {

        return m_expression;
    } // expression

} // ASTs
