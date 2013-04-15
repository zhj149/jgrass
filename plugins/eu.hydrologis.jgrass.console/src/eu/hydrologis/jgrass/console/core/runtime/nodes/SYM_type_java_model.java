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

import java.util.HashMap;
import java.util.Set;

import eu.hydrologis.jgrass.console.ConsolePlugin;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symbol;
import eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs;

/**
 * <p>
 * A symbol for a <i>JGRASS</i> java based model keyword as reference type.
 * </p>
 * 
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class SYM_type_java_model extends SYM_type_reference implements Symbol<SYMs> {

    // Attributes
    /** The assign operator. */
    private final static String __character_assign = "="; //$NON-NLS-1$

    /** The comma operator. */
    private final static String __character_comma = ","; //$NON-NLS-1$

    /**
     * The attribute <code>m_defaultKey</code> holds the name of the models pre-defined <i>default
     * key</i> - argument flag -, if any, otherwise <code>null</code>.
     */
    private final String m_defaultKey;

    /**
     * The attribute <code>m_exchangeItems</code> holds the list of the models input/output
     * <i>exchange items</i>; a exchange item is a pair consisting of an item name - a word - and
     * an integer number attribute value.
     */
    private final HashMap<String, Integer> m_exchangeItems;

    // Construction
    /**
     * <p>
     * The constructor <code>SYM_type_java_model</code> defines this symbol object with the
     * specified class name - full qualified - the specified default key, if any, and the specified
     * input/output <i>exchange items</i> of the model.
     * </p>
     * 
     * @param fullQualifiedName - the class name, full qualified, of the linkable model, e.g.,
     *        <code>eu.hydrologis.jgrass.models.h.flow.h_flow</code>.
     * @param defaultKey - the literal name, e.g. <code>"default"</code>, of the default key, if
     *        any, otherwise <code>null</code>.
     * @param exchangeItems - a string representing a list, separeted by a comma, of the models
     *        exchange items, for each: consting of a literal name and an integer number; e.g.,
     *        <code>"pit=0, flow=0"</code>.
     */
    public SYM_type_java_model( String fullQualifiedName, String defaultKey, String exchangeItems ) {

        super(fullQualifiedName, SYMs.SYM_TYPE_JAVA_MODEL);
        m_defaultKey = __safe_defaultkey(defaultKey);
        m_exchangeItems = new HashMap<String, Integer>();
        __assign_exchangeitems(exchangeItems);
    } // SYM_type_java_model

    /**
     * Maps the specified string representing a list, separeted by a comma, of the models exchange
     * items into the hash map of this symbol.
     * 
     * @param string - a string representing a list, separeted by a comma, of the models exchange
     *        items, for each: consting of a literal name and an integer number; e.g.,
     *        <code>"pit=0, flow=0"</code>.
     */
    private void __assign_exchangeitems( String string ) {

        if (null != string) {

            String[] tokens = string.split(__character_comma);
            for( int i = 0; i < tokens.length; ++i ) {

                final String token = tokens[i];
                if (0 != token.compareToIgnoreCase(__character_comma)) {

                    String[] s = token.split(__character_assign);
                    if (2 == s.length) {

                        String expr = s[0].trim();
                        Integer term = null;
                        try {

                            term = new Integer(Integer.parseInt(s[1]));
                        } catch (Exception e) {

                            term = null;
                        } finally {

                            if (null != expr && null != term)
                                m_exchangeItems.put(expr, term);
                        }
                    }
                }
            }
        }
    } // __assign_exchangeitems

    /**
     * <p>
     * The method <code>__safe_defaultkey</code> returns the name of the default key only if the
     * specified candidate denotes a name.
     * </p>
     * 
     * @param candidate - the literal name of the default key, otherwise <code>null</code>.
     * @return The name of the default key according to this model, otherwise <code>null</code>.
     */
    private String __safe_defaultkey( String candidate ) {

        final String string;
        if (null == candidate || 0 >= (string = candidate.trim()).length())
            return null;

        return string;
    } // __safe_defaultkey

    // Operations
    /**
     * <p>
     * Returns the name of the default key.
     * </p>
     * 
     * @return The name of the default key according to this model if the model has a pre-defined
     *         default key, otherwise <code>null</code>.
     */
    public String defaultKey() {

        return m_defaultKey;
    } // default_key

    /**
     * <p>
     * Returns the analogical integer value of the specified exchange item.
     * </p>
     * 
     * @param identifier - a name of an exchange item. In the case that
     *        {@link ConsolePlugin#genericInExchangeItemID} or
     *        {@link ConsolePlugin#genericOutExchangeItemID} are passed, then it is forced to work
     *        even if the key is not assigen, and will return 0.
     * @return The analogical integer value of the specified exchange item.
     * @throws IllegalArgumentException - if the specified exchange item is not defined.
     */
    public String exchangeItem( String identifier ) {
        System.out.println("is identifier = " + identifier);

        if (identifier.equals(ConsolePlugin.genericInExchangeItemID)
                || identifier.equals(ConsolePlugin.genericOutExchangeItemID)) {
            Integer obj = m_exchangeItems.get(identifier);
            if (obj == null) {
                return "0";
            }
            return String.valueOf(obj);
        }
        if (identifier.matches(".*@@@.*")) {
            if (identifier.startsWith(ConsolePlugin.genericInExchangeItemID)
                    && identifier.endsWith(ConsolePlugin.genericOutExchangeItemID)) {
                String[] split = identifier.split("@@@");
                Integer obj = m_exchangeItems.get(split[0]);
                if (obj == null) {
                    return "0";
                }
                return String.valueOf(obj);
            } else if (identifier.startsWith(ConsolePlugin.genericOutExchangeItemID)
                    && identifier.endsWith(ConsolePlugin.genericInExchangeItemID)) {
                String[] split = identifier.split("@@@");
                Integer obj = m_exchangeItems.get(split[0]);
                if (obj == null) {
                    return "0";
                }
                return String.valueOf(obj);
            }
        }

        if (false == m_exchangeItems.containsKey(identifier)) {
            // make a last try to identify if it is a single output item, i.e. there is an out key
            // in the exchangeItems
            if (m_exchangeItems.containsKey(ConsolePlugin.genericOutExchangeItemID)) {
                Integer obj = m_exchangeItems.get(ConsolePlugin.genericOutExchangeItemID);
                if (obj == null) {
                    return "0";
                } else {
                    return String.valueOf(obj);
                }
            } else
                throw new IllegalArgumentException();
        }

        return String.valueOf(m_exchangeItems.get(identifier));
    } // exchange_item_identifier

    /**
     * <p>
     * Returns <code>true</code> if the model has a default key defined, otherwise
     * <code>false</code>.
     * </p>
     * 
     * @return Returns <code>true</code> if the model has a default key defined, otherwise
     *         <code>false</code>.
     */
    public boolean hasDefaultKey() {

        return (null != m_defaultKey) ? true : false;
    } // hasDefaultKey

    /**
     * <p>
     * Returns <code>true</code> if the model defines the specified exchange item, otherwise
     * <code>false</code>.
     * </p>
     * 
     * @param identifier - a name of an exchange item.
     * @return Returns <code>true</code> if the model defines the specified exchange item,
     *         otherwise <code>false</code>.
     */
    public boolean hasExchangeItem( String identifier ) {
        System.out.println("has identifier = " + identifier);
        // see doc of exchangeItem(String) method
        // if (identifier.equals(ConsolePlugin.genericInExchangeItemID)
        // || identifier.equals(ConsolePlugin.genericOutExchangeItemID)) {
        // return true;
        // }

        if (m_exchangeItems.containsKey(identifier)) {
            return true;
        } else if (m_exchangeItems.containsKey("*")) {
            return true;
        } else {
            return false;
        }

    } // hasExchangeItem
    /**
     * <p>
     * Returns <code>true</code> if the model has exchange items, otherwise <code>false</code>.
     * </p>
     * 
     * @return Returns <code>true</code> if the model has exchange items, otherwise
     *         <code>false</code>.
     */
    public boolean hasExchangeItems() {

        return (0 < m_exchangeItems.size()) ? true : false;
    } // hasExchangeItems

} // SYM_type_java_model
