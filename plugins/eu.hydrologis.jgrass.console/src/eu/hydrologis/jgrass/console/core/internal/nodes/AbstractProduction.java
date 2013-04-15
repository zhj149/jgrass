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
 * <p>The class <code>AbstractProduction</code> provides default implementation
 * for the <code>Production</code> interface and defines standard behavior for
 * the method <code>produces</code>. The developer need only subclass this
 * abstract class and implement the method <code>translate</code>.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Production
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractProduction<P, X, S>
    implements Production<P, X, S> {

// Attributes
	/** The productions of the language definition. */
	private final X[][] m_produces;
	
// Construction
	/**
	 * <p>The copy constructor <code>AbstractProduction</code> defines a
	 * production used by a syntax analyzer (parser) with the specified set of
	 * one of the written forms of a grammatical construct.</p>
	 * @param produces
	 * 		- specifies one of the written froms of a construct.
	 */
	public AbstractProduction( X[][] produces ) {
		
		super();
		m_produces = produces;
	} // AbstractProduction
	
// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Production#produces(Symtable,APT)
	 */
	public X[][] produces( APT<P> parseTree ) {
		
		return m_produces;
	} // produces
	
} // AbstractProduction
