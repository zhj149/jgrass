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
 * <p>The class <code>AbstractAPT</code> provides default implementation
 * for the <code>APT</code> interface and defines standard behavior for
 * the methods: <code>addChild</code>, <code>getChild</code>, <code>size</code>,
 * <code>root</code>, <code>parent</code>, <code>lookup</code>,
 * <code>annotation</code>, <code>expression</code> and <code>identifier</code>.
 * The developer need only subclass this abstract class.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractAPT<E>
	implements APT<E> {

// Attributes
	/**
	 * <p>The static predefined format string to annotate an abstract parse
	 * tree node.</p>
	 * @see java.text.MessageFormat
	 */
	private final static String m_szMsgFmtAnnotation = "[{0} ,<{1}>]"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#annotation()
	 */
	private final String m_annotation;

	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#expression()
	 */
	private final String m_expression;

	/** An abstract symbole representing the contents of this node. */
	private final E m_identifier;
	
	/** The list of added or inserted nodes as children of this node. */
	private final List<APT<E>> m_nodeChilds;
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAPT#AbstractAPTNode(String,String,E)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#parent()
	 */
	private final APT<E> m_parent;

// Construction
	/**
	 * <p>The copy constructor <code>AbstractAPT</code> is used to create
	 * the root of an abstract parse tree with the specified expression, the
	 * specified annotation and the specified identifier.</p>
	 * @param expression
	 * 		- this argument can be <code>null</code> or a <code>String</code>,
	 * 		which describes the contents of the root.
	 * @param annotation
	 * 		- a <code>String</code>, describing the contents of this node.
	 * @param identifier
	 * 		- a identifier, which identifies the root in an unique way.
	 * @throws IllegalArgumentException
	 * 		- if <code>annotation</code> or <code>identifier</code> references
	 * 		the null type.
	 */
	protected AbstractAPT( String expression, String annotation,
			E identifier ) {

		super();
		try {

			if( null == annotation )
				throw new IllegalArgumentException();
			if( null == identifier )
				throw new IllegalArgumentException();

			m_annotation = annotation;
			m_expression = expression;
			m_identifier = identifier;
			m_nodeChilds = __newChildsArrayList();
			m_parent     = null;
		}
		catch( IllegalArgumentException e ) {

			if( true == Projectspace.isErrorEnabled() )
				System.out.println(e);

			throw e;
		}
	} // AbstractAPT
	
	/**
	 * <p>The copy constructor <code>AbstractAPT</code> is used to create a
	 * node (operator) or leaf node (operand) in an abstract parse tree with the
	 * specified operator - interior node - as parent, the specified expression,
	 * the specified annotation and the specified identifier.</p>
	 * @param operator
	 * 		- the parent node.
	 * @param expression
	 * 		- this argument can be either <code>null</code> or can be the
	 * 		tokens attribute value <acronym title="respectively">resp.</acronym>
	 * 		the tokens expression.
	 * @param annotation
	 * 		- a <code>String</code>, describing the contents of this node.
	 * @param identifier
	 * 		- a identifier, which identifies this node object in an unique way.
	 * @throws IllegalArgumentException
	 * 		- if <code>parent</code>, <code>annotation</code> or
	 * 		<code>identifier</code> references the null type.
	 */
	public AbstractAPT( APT<E> operator, String expression,
			String annotation, E identifier ) {

		super();
		try {

			if( null == operator )
				throw new IllegalArgumentException();
			if( null == annotation )
				throw new IllegalArgumentException();
			if( null == identifier )
				throw new IllegalArgumentException();

			m_annotation = annotation;
			m_expression = expression;
			m_identifier = identifier;
			m_nodeChilds = __newChildsArrayList();
			m_parent     = operator;
		}
		catch( IllegalArgumentException e ) {

			if( true == Projectspace.isErrorEnabled() )
				System.out.println(e);

			throw e;
		}
	} // AbstractAPT
	
	/**
	 * <p>The helper method <code>__newChildsArrayList</code> creates a new
	 * <code>ArrayList</code>, where nodes can be added or inserted.</p>
	 * @return
	 * 		Returns a new <code>ArrayList</code>, where nodes can be added or
	 * 		inserted.
	 */
	private ArrayList<APT<E>> __newChildsArrayList() {
		
		return new ArrayList<APT<E>>();
	} // __newChildsArrayList

// Operations
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#addChild(int,APT<E>)
	 */
	public APT<E> addChild( APT<E> operand ) {
		
		try {
			
			if( null == operand )
				throw new IllegalArgumentException();
			
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
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#addChild(int,APT<E>)
	 */
	public APT<E> addChild( int index, APT<E> operand ) {
		
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
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#annotation()
	 */
	public String annotation() {

		String retval = null;
		if( null == expression() ) {

			retval = m_annotation;
		}
		else {

			retval = MessageFormat.format(
					m_szMsgFmtAnnotation
					, new Object[] { expression(), m_annotation } 
				);
		}

		return retval;
	} // annotation

	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#expression()
	 */
	public String expression() {

		return m_expression;
	} // expression
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#getChild(E)
	 */
	@SuppressWarnings("unchecked")
    public APT<E>[] getChild( E idOperand ) {

		ArrayList<APT<E>> result = null;
		APT<E>[] retval;
		try {
			
			result = null;
			if( 0 < m_nodeChilds.size() ) {
					
				result = __newChildsArrayList();
				for( int i = 0; i < m_nodeChilds.size(); ++i ) {
					
					APT<E> candidate = m_nodeChilds.get( i );
					if( candidate.identifier() == idOperand )
						result.add( candidate );
				}
			}
		}
		catch( Exception e ) {
			
			e.printStackTrace();
		}
		finally {
			
			if( null == result || 0 >= result.size() ) {
				
				retval = null;
			}
			else {
				
				retval = ( APT<E>[] )new APT[ result.size() ];
				result.toArray( retval );
			}
		}
		
		return retval;
    } // getChild
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#getChild(int)
	 */
	public APT<E> getChild( int index ) {
		
		if( 0 >= m_nodeChilds.size() ||
				index < 0 || index >= m_nodeChilds.size() )
			return null;
		
		return m_nodeChilds.get( index );
	} // getChild

	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#identifier()
	 */
	public E identifier() {

		return m_identifier;
	} // identifier

	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#lookup()
	 */
	public APT<E> lookup( E idOperand ) {

		APT<E> candidate = this.parent();
		APT<E> retval = null;
		while( null == retval && null != candidate ) {

			if( candidate.identifier() != idOperand )
				candidate = candidate.parent();
			else
				retval = candidate;
		}

		return retval;
	} // lookup

	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#parent()
	 */
	public APT<E> parent() {

		return m_parent;
	} // parent
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#root()
	 */
	public APT<E> root() {

		APT<E> candidate = this;
		APT<E> retval;
		do {

			retval = candidate;
			candidate = candidate.parent();
		} while( null != candidate && retval != candidate );

		return retval;
	} // root
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.APT#size()
	 */
	public int size() {
		
		return m_nodeChilds.size();
	} // size

} // AbstractAPT
