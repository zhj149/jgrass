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
import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT;
import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractToken;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.APTs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;

/**
 * <p>The <i>abstract parse tree</i> operator of an <b>output argument</b> of
 * a model.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.APTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class APT_output extends AbstractAPT<APTs> implements APT<APTs> {

    // Attributes
    /** The arguments of this linkable component. */
    private APT<APTs> m_argdefs;

    /**
     * The variable definition of the link object of this linkable
     * component.
     */
    private APT<APTs> m_link_variable_def;

    /** The variable definition of this linkable component object. */
    private APT<APTs> m_linkable_variable_def;

    /** The nested statment, if any. */
    private APT<APTs> m_nested_statement;

    /** The token of the argument value. */
    private Token<TOKs> m_token_lvalue;

    /** The token of the argument value. */
    private Token<TOKs> m_token_rvalue;

    /**
     * The usage operator is used in the statement <code>true</code>, otherwise
     * <code>false</code>.
     */
    public boolean m_usage;

    private final String linkable_variable_name;

    private final String linkable_type_name;

    private final String link_variable_name;

    private final String link_type_name;

    // Construction
    /**
     * <p>Constructs this object with the specified operator - interior node -
     * as parent and with the other required informations.</p>
     * @param operator
     * 		- a <code>APT<APTs></code> object providing the operator - interior
     * 		node - as parent.
     * @param lvalue
     * 		- the input token.
     * @param linkable_variable_name
     * 		- a variable name for this linkable component object.
     * @param linkable_type_name
     * 		- the type of this linkable component.
     * @param link_variable_name
     * 		- the variable name for the link object of this linkable component.
     * @param link_type_name
     * 		- the type of the link.
     * @param rvalue
     * 		- a argument value.
     * @throws IllegalArgumentException
     * 		- if one of the parameter references the <code>null</code> type.
     */
    public APT_output( APT<APTs> operator, Token<TOKs> lvalue, String linkable_variable_name,
            String linkable_type_name, String link_variable_name, String link_type_name,
            Token<TOKs> rvalue ) {

        super(operator, AbstractToken.__safe_expression(lvalue), APTs.APT_OUTPUT.annotation(),
                APTs.APT_OUTPUT);
        this.linkable_variable_name = linkable_variable_name;
        this.linkable_type_name = linkable_type_name;
        this.link_variable_name = link_variable_name;
        this.link_type_name = link_type_name;
        try {

            final String __safe_expression = AbstractToken.__safe_expression(lvalue);
            if (null == __safe_expression)
                throw new IllegalArgumentException();
            if (null == operator)
                throw new IllegalArgumentException();
            if (null == linkable_variable_name)
                throw new IllegalArgumentException();
            if (null == linkable_type_name)
                throw new IllegalArgumentException();
            if (null == link_variable_name)
                throw new IllegalArgumentException();
            if (null == link_type_name)
                throw new IllegalArgumentException();

            m_argdefs = new APT_argument_definition(this);
            m_nested_statement = new APT_statement(this);
            m_token_lvalue = lvalue;
            m_token_rvalue = rvalue;
            m_link_variable_def = new APT_variable_definition(this, link_variable_name,
                    link_type_name);
            m_linkable_variable_def = new APT_variable_definition(this, linkable_variable_name,
                    linkable_type_name);
            m_usage = false;
        } catch (IllegalArgumentException e) {

            if (true == Projectspace.isErrorEnabled())
                System.out.println(e);

            throw e;
        }
    } // APT_output

    // Operations
    /**
     * <p>Returns the operand of argument definitions for this linkable input
     * component.</p>
     */
    public APT_argument_definition argument_defs() {

        return (APT_argument_definition) m_argdefs;
    } // argument_defs

    /**
     * <p>Returns the variable definition operand of the link object for
     * this linkable component.</p>
     */
    public APT_variable_definition link_variable_def() {

        return (APT_variable_definition) m_link_variable_def;
    } // link_variable_def

    /**
     * <p>Returns the variable definition of this linkable component object.</p>
     */
    public APT_variable_definition linkable_variable_def() {

        return (APT_variable_definition) m_linkable_variable_def;
    } // linkable_variable_def

    /**
     * <p>Returns the nested statment, if any.</p>
     */
    public APT_statement nested_statement() {

        return (APT_statement) m_nested_statement;
    } // model_nested_def

    /**
     * <p>Returns the token of the argument flag.</p>
     */
    public N_output token_flag() {

        return (N_output) m_token_lvalue;
    } // token_flag

    /**
     * <p>Returns the token of the argument value.</p>
     */
    public Token<TOKs> token_value() {

        return m_token_rvalue;
    } // token_value

    public String toString() {
        StringBuilder sB = new StringBuilder();
        sB.append("linkable_variable_name = ");
        sB.append(linkable_variable_name);
        sB.append("\nlinkable_type_name = ");
        sB.append(linkable_type_name);
        sB.append("\nlink_variable_name = ");
        sB.append(link_variable_name);
        sB.append("\nlink_type_name = ");
        sB.append(link_type_name);
        String tostring = sB.toString();
        return tostring;
    }

} // APT_output