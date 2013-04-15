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
 * <p>An <i>abstract parse tree</i> <i>(<acronym title="abstract parse tree"
 * >APT</acronym>)</i> is a finite, labeled, directed tree used as intermediate
 * representation produced as output by a parser. In an abstract parse tree for
 * an expression, each interior node represents a operator and the leaf nodes
 * represent the operands of the operator; any source programming construct is
 * being handled by making up an operator for the programming construct and
 * treating as operands the semantically meaningful components of that construct
 * - if the leaves are <code>null</code> operands, the node either represents a
 * variable name or a constant. Instead of a parse tree, in an abstract parse
 * tree parenthesis can be omitted.</p>
 * <p>The interface <code>APT</code> is the abstract representation of either
 * an operator or an operand of a programming construct, e.g. for a programming
 * construct: expression, statement, block.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface APT<E> {

// Operations
	/**
	 * <p>Adds an existing operand to the list of legal children.</p>
	 * @param operand
	 * 		- existing operand to be added as a child.
	 * @return
	 * 		Returns the added operand.
	 */
	public abstract APT<E> addChild( APT<E> operand );
	
	/**
	 * <p>Inserts the specified operand at the specified position this operator.
	 * Shifts the operand currently at that position (if any) and any subsequent
	 * operand to the right (adds one to their indices).</p>
	 * @param index
	 * 		- index at which the specified operand is to be inserted. 
	 * @param operand
	 * 		- existing operand to be inserted as a child.
	 * @return
	 * 		Returns the inserted operand.
	 */
	public abstract APT<E> addChild( int index, APT<E> operand );
	
	/**
	 * <p>The method <code>annotation</code> returns a human readable term,
	 * which describs the contents of this node.</p>
	 * @return
	 * 		Returns the annotation of this node.
	 */
	public abstract String annotation();
	
	/**
	 * <p>The method <code>expression</code> returns the expression of this
	 * node.</p>
	 * @return
	 * 		Returns the expression of this node.
	 */
	public abstract String expression();
	
	/**
	 * <p>Returns an <code>array</code> of operands, which matches the
	 * specified identifier in this operator.</p>
	 * @param idOperand
	 * 		- abstract symbole identifying an operand of this operator.
	 * @return
	 * 		Returns an <code>array</code> of child nodes, which matches the
	 * 		specified identifier, if any, otherwise	<code>null</code>.
	 */
	public abstract APT<E>[] getChild( E idOperand );
	
	/**
	 * <p>Returns the operand at the specified position of this operator.</p>
	 * @param index
	 * 		- index of operand.
	 * @return
	 * 		Returns the operand at the specified position.
	 */
	public abstract APT<E> getChild( int index );
	
	/** <p>The method <code>identifier</code> of a <acronym
	 * title="abstract parse tree">APT</acronym> object uses the <acronym
	 * title="abstract parse tree">APT</acronym> idendity scope to identify
	 * itself. The identifier is an abstract symbole representing the syntax
	 * information. However, the identifier of a symbole should always be a
	 * unique and a enumerable value because of all future processing will
	 * typically fall back to the returned symbole identity.</p>
	 * @return
	 * 		Returns the unique, enumerable symbole identifier of this node
	 * 		object.
	 */
	public abstract E identifier();
	
	/**
	 * <p>Returns the first occured node with the specified identifier in the
	 * abstract parse tree. The method <code>lookup</code> starts searching the
	 * abstract parse tree with the parent of this node and then walks upward,
	 * looking for a node with the specified identifier; if a node with the
	 * specified identifier could not be found the methods return value
	 * is <code>null</code>.</p> 
	 * @param idOperand
	 * 		- the identifier of the <code>APT</code> object reference to
	 * 		look up. 
	 * @return
	 * 		A node with the specified identifier which has occured first, if
	 * 		any, otherwise <code>null</code>.
	 */
	public abstract APT<E> lookup( E idOperand );
	
	/**
	 * <p>The method <code>parent</code> returns the parent, the operator, of
	 * this operand, or <code>null</code> if this is the root.</p>
	 * @return
	 * 		Returns a <code>APT</code> mirroring the parent of this node,
	 * 		or <code>null</code> if this is the root. 
	 */
	public abstract APT<E> parent();
	
	/**
	 * <p>The method <code>parent</code> returns the parent node of this
	 * <acronym title="Abstract Parse Tree">APT</acronym> node, or
	 * <code>null</code> if this is the root.</p>
	 * @return
	 * 		Returns a <code>APT</code> mirroring the parent of this node,
	 * 		or <code>null</code> if this is the root. 
	 */
	public abstract APT<E> root();
	
	/**
	 * <p>Returns the number of operands of this operator.</p>
	 * @return
	 * 		Returns the number of operands.
	 */
	public abstract int size();
	
} // APT
