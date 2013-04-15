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
 * <p>An <i>abstract syntax tree</i> <i>(<acronym title="abstract syntax tree"
 * >AST</acronym>)</i> is a finite, labeled, directed tree used as an additional
 * intermediate between a parse tree and a intermediate data structure, which is
 * often used by a compiler or an interpreter as an internal representation of a
 * source program - a pre-processor treats them as intermediate representation
 * of the produced target code. In an abstract syntax tree for an expression,
 * each interior node represents a operator and the leaf nodes represent the
 * operands of the operator; any programming construct is being handled by
 * making up an operator for the programming construct and treating as operands
 * the semantically meaningful components of that construct - if the leaves are
 * <code>null</code> operands, the node either represents a variable name or a
 * constant. Instead of a parse tree, in an abstract syntax tree parenthesis is
 * omitted.</p>
 * <p>The interface <code>AST</code> is the abstract representation of either
 * an operator or an operand of a programming construct, e.g. for a programming
 * construct: expression, statement, block.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public interface AST<E> {

// Operations
	/**
	 * <p>Adds an existing operand to the list of legal children.</p>
	 * @param operand
	 * 		- existing operand to be added as a child.
	 * @return
	 * 		Returns the added operand.
	 */
	public abstract AST<E> addChild( AST<E> operand );
	
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
	public abstract AST<E> addChild( int index, AST<E> operand );
	
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
	 * <p>Returns the operand at the specified position of this operator.</p>
	 * @param index
	 * 		- index of operand.
	 * @return
	 * 		Returns the operand at the specified position.
	 */
	public abstract AST<E> getChild( int index );
	
	/**
	 * <p>The method <code>identifier</code> of an <acronym
	 * title="abstract syntax tree">AST</acronym> object uses the <acronym
	 * title="abstract syntax tree">AST</acronym> idendity scope to identify
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
	 * <p>Returns the number of operands of this operator.</p>
	 * @return
	 * 		Returns the number of operands.
	 */
	public abstract int size();
	
} // AST
