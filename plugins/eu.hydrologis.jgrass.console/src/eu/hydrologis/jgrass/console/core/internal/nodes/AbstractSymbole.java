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
 * <p>The class <code>AbstractSymbole</code> provides default implementation
 * for the <code>Symbol</code> interface and defines standard behavior for the
 * method <code>identifier</code>. The developer need only subclass this
 * abstract class and implement the method <code>identifier</code>.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractSymbole<E>
	implements Symbol<E> {

// Attributes
	/**
	 * An abstract symbole representing the kind of stored information.
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol#identifier()
	 */
	private final E m_identifier;
	
// Construction
	/**
	 * <p>The copy constructor <code>AbstractSymbole</code> defines this
	 * symbole with the specified identifier.</p>
	 * @param identifier
	 * 		- abstract symbole representing the kind of stored informations.
	 * @throws IllegalArgumentException
	 * 		- if <code>identifier</code> references the null type.
	 */
	public AbstractSymbole( E identifier ) {
	
		super();
		if( null == identifier )
			throw new IllegalArgumentException();
		
		m_identifier = identifier;
	} // AbstractSymbole
	
// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol#identifier()
	 */
	public E identifier() {
	
		return m_identifier;
	} // identifier
	
} // AbstractSymbole
