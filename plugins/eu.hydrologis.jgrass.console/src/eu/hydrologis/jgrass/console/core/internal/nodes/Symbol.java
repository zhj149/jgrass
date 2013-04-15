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

/**
 * <p>A symbol is a entry in a symbol table and can be seen as record, which
 * holds various informations about a single source-program construct. The
 * symbol name is an abstract symbol representing the kind of stored
 * information, e.g., a type definition.</p>
 * <p>The user of the interface <code>Symbol</code> can get the symbols name
 * by calling the interface method <code>identifier</code>.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symtable
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface Symbol<E> {
	
// Operations
	/**
	 * <p>The method <code>identifier</code> of a symbol uses the symbol
	 * idendity scope to identify itself. The identifier of a symbol is an
	 * abstract symbol representing the kind of stored informations. However,
	 * the identifier of a symbol should always be a unique and a enumerable
	 * value because of all future processing will typically fall back to the
	 * returned symbol identity.</p>
	 * <p>The data type of the returned value by the <code>identifier</code> 
	 * method is naturally a constant value of type <code>Integer</code> or a
	 * recommanded custom enumeration type.</p>
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symtable
	 * @return
	 * 		Returns the unique, enumerable symbol identifier of this symbol
	 * 		object.
	 */
	public abstract E identifier();
		
} // Symbol
