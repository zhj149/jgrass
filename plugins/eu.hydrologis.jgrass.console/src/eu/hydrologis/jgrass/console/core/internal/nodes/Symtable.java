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
package eu.hydrologis.jgrass.console.core.internal.nodes;

import java.util.HashMap;

/**
 * <p>The class <code>Symtable</code> is the implementation of a <i>symbol
 * table</i>.</p><p>Symbol tables are data structures used to hold information
 * about source program constructs. The information is collected incrementally
 * during the analysis phases (syntactical, semantically) and is used by the
 * synthesis phases to generate the target code. Entries in the symbol table
 * contain information about identifiers. The information about an identifier
 * may consists of its character string, its type, and any other relevant
 * information.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class Symtable<E> {
	
// Attributes
	/** The hash table. */
	private final HashMap<String, Symbol<E>> m_symbolMap;
	
	// Construction
	/**
	 * <p>The constructor <code>Symtable</code> creates a new symbol table.</p>
	 */
	public Symtable() {
		
		super();
		m_symbolMap = newSymbolMap();
	} // Symtable
	
	/**
	 * <p>The method <code>newSymbolMap</code> creates a new
	 * <code>HashMap&lt;String, Symbol&lt;E&gt;&gt;</code> for the symbol
	 * table.</p>
	 * @return
	 * 		A new <code>HashMap&lt;String, Symbol&lt;E&gt;&gt;</code>.
	 */
	private final HashMap<String, Symbol<E>> newSymbolMap() {
		
		return new HashMap<String, Symbol<E>>();
	} // newSymboleMap
	
// Operations
	/**
	 * <p>The method <code>autoCreateIdentifier</code> auto creates a name, a
	 * synthetical name, for an identifier with the specified prefix.</p>
	 * @param identifierPrefix
	 * 		- the prefix of the auto created name. 
	 * @return
	 * 		The auto created synthetical name for an identifier.
	 */
	public String autoCreateIdentifier( String identifierPrefix ) {
		
		return identifierPrefix + m_symbolMap.size();
	} // autoCreateIdentifier
	
	/**
	 * <p>The method <code>lookup</code> returns the associated entry, a symbol,
	 * for a specified identifier, if any.</p>
	 * @param identifier
	 * 		- is a string, or rather a reference to a string, which identifys
	 * 		e.g. either the name of an identifier or a variable name.
	 * @return
	 * 		The associated symbol of the specified identifier, if any,
	 * 		otherwise <code>null</code>.
	 */
	public Symbol<E> lookup( String identifier ) {

		if( true == m_symbolMap.containsKey( identifier ) )
			return m_symbolMap.get( identifier );
		
		return null;
	} // lookup

	/**
	 * <p>The method <code>register</code> puts a new entry in the current
	 * table.</p>
	 * @param identifier
	 * 		- is a string, or rather a reference to a string, which identifys
	 * 		e.g. either the name of an identifier or a variable name.
	 * @param symbol
	 * 		- the associated symbol.
	 */
	public void register( String identifier, Symbol<E> symbol ) {

		if( null == lookup( identifier ) )
			m_symbolMap.put( identifier, symbol );
	} // register
	
} // Symtable
