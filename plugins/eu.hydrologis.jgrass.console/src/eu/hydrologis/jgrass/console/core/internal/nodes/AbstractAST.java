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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>The class <code>AbstractAST</code> provides default implementation
 * for the <code>AST</code> interface and defines standard behavior for
 * the methods: <code>addChild</code>, <code>getChild</code>, <code>size</code>,
 * <code>annotation</code>, <code>expression</code> and <code>identifier</code>.
 * The developer need only subclass this abstract class.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractAST<E>
    implements AST<E> {

// Attributes
	/**
	 * <p>The static predefined format string to annotate an abstract syntax
	 * tree node.</p>
	 * @see java.text.MessageFormat
	 */
	private final static String m_szMsgFmtAnnotation = "[{0} ,<{1}>]"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#annotation()
	 */
	private final String m_annotation;
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#expression()
	 */
	private final String m_expression;
	
	/** An abstract symbole representing the contents of this node. */
	private final E m_identifier;
	
	/** The list of added or inserted operands of this node. */
	private final List<AST<E>> m_nodeChilds;
	
// Construction
	/**
	 * <p>The copy constructor <code>AbstractAST</code> is used to create a
	 * node (operator) or leaf node (operand) of an abstract syntax tree with
	 * the specified expression, the specified annotation and the specified
	 * identifier and the ability to add or insert leaf nodes (operands).</p>
	 * @param expression
	 * 		- this argument can be either <code>null</code> or can be the
	 * 		tokens attribute value <acronym title="respectively">resp.</acronym>
	 * 		the tokens expression.
	 * @param annotation
	 * 		- a <code>String</code>, describing the contents of this node.
	 * @param identifier
	 * 		- a identifier, which identifies this node object in an unique way.
	 * @param operands
	 * 		- can be <code>null</code> or either a single operand or a list of
	 * 		operands.
	 * @throws IllegalArgumentException
	 * 		- if <code>identifier</code> references the null type.
	 */
	public AbstractAST( String expression, String annotation, E identifier,
			AST<E>... operands ) {
		
		try {	
			
			if( null == identifier )
				throw new IllegalArgumentException();
			
			m_annotation = annotation;
			m_expression = expression;
			m_identifier = identifier;
			m_nodeChilds = new ArrayList<AST<E>>();
			if( null != operands ) {
				
				for( int i = 0; i < operands.length; ++i )
					addChild( operands[ i ] );
			}
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
	} // AbstractAST
	
// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#addChild()
	 */
	public AST<E> addChild( AST<E> operand ) {
		
		try {
			
			if( null == operand )
				throw new IllegalArgumentException();
			
			if( null != m_nodeChilds )
				m_nodeChilds.add( operand );
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
		
		return operand;
	} // addChild
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#addChild()
	 */
	public AST<E> addChild( int index, AST<E> operand ) {
		
		try {
			
			if( null == operand )
				throw new IllegalArgumentException();
			
			m_nodeChilds.add( index, operand );
		}
		catch( IllegalArgumentException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			throw e;
		}
		
		return operand;
	} // addChild
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#annotation()
	 */
	public String annotation() {
		
		String retval = null;
		if( null == expression() ) {
			
			retval = m_annotation;
		}
		else {
			
			Object[] argv = {
					expression()
					, m_annotation
				};
			retval = MessageFormat.format(
					m_szMsgFmtAnnotation
					, argv
				);
		}
		
		return retval;
	} // annotation
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#expression()
	 */
	public String expression() {
		
		return m_expression;
	} // expression
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#getChild()
	 */
	public AST<E> getChild( int index ) {
	
		if( null == m_nodeChilds || 0 >= m_nodeChilds.size() )
			return null;
		
		return m_nodeChilds.get( index );
	} // getChild
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#identifier()
	 */
	public E identifier() {
		
		return m_identifier;
	} // identifier
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST#size()
	 */
	public int size() {
		
		if( null == m_nodeChilds || 0 >= m_nodeChilds.size() )
			return 0;
		
		return m_nodeChilds.size();
	} // size
	
} // AbstractAST
