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
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.APTs;

/**
 * <p>The <i>abstract parse tree</i> operand for a <b>variable
 * definition</b>.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.APTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class APT_variable_definition extends AbstractAPT<APTs> implements APT<APTs> {

    // Attributes
    /** The type of this variable. */
    private final String m_type_name;

    /** The name of the variable. */
    private final String m_variable_name;

    // Construction
    /**
     * <p>Constructs this object with the specified operator - interior node -
     * as parent and the specified variable name, variable type.</p>
     * @param operator
     * 		- a <code>APT<APTs></code> object providing the operator - interior
     * 		node - as parent.
     * @param variable_name
     * 		- the variable name.
     * @param type_name
     * 		- the type of the variable.
     */
    public APT_variable_definition( APT<APTs> operator, String variable_name, String type_name ) {

        super(operator, null, APTs.APT_VARIABLE_DEFINITION.annotation(),
                APTs.APT_VARIABLE_DEFINITION);
        try {

            m_type_name = type_name;
            m_variable_name = variable_name;
        } catch (IllegalArgumentException e) {

            if (true == Projectspace.isErrorEnabled())
                System.out.println(e);

            throw e;
        }
    } // APT_variable_definition

    /**
     * <p>Returns the type of this variable definition.</p>
     */
    public String type_name() {

        return m_type_name;
    } // type_name

    /**
     * <p>Returns the name of the variable.</p>
     */
    public String variable_name() {

        return m_variable_name;
    } // variable_name

    public String toString() {
        StringBuilder sB = new StringBuilder();
        sB.append("m_type_name = ");
        sB.append(m_type_name);
        sB.append("\nm_variable_name = ");
        sB.append(m_variable_name);
        String tostring = sB.toString();
        return tostring;
    }

} // APT_variable_definition