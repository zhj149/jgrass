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
 * <p>The class <code>AbstractToken</code> provides default implementation for
 * the <code>Token</code> interface and defines standard behavior for the
 * methods: <code>expression</code>, <code>line</code> and
 * <code>isWhitespace</code>. The developer need only subclass this abstract
 * class and implement the method <code>identifier</code>.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractToken<E>
    implements Token<E> {

// Attributes
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token#isWhitespace()
	 */
	private boolean m_bWhitespace;
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token#expression()
	 */
	private String m_expression;
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token#line()
	 */
	private int m_line;
	
// Construction
	/**
	 * <p>The copy constructor <code>AbstractToken</code> defines a token object
	 * with the specified line number and a copy of the specified expression,
	 * where leading and trailing whitespace character information is beging
	 * omitted, except the specified expression only contains whitespace
	 * character information.</p>
	 * @param expression
	 * 		- character string in the source program detected by the lexical
	 * 		analyzer that matches the pattern of the corresponding lexeme for
	 * 		this token.
	 * @param line
	 * 		- line number in the source program.
	 * @throws IllegalArgumentException
	 * 		- if <code>expression</code> references the null type or the length
	 * 		of the character string is zero.
	 */
	protected AbstractToken( String expression, int line ) {
		
		super();
		if( null == expression || 0 >= expression.length() )
			throw new IllegalArgumentException();
		
		// Copying first, the line number...
		m_line = line;
		
		// Assuming this is a expression, therefore removing first any
		//	whitespace form the expression, if any...
		m_expression = expression.trim();
		//	if the length of the newly assigned expression is equal to zero,
		//	these expression is a whitespace expression and therefore we
		//	assign to the expression the original expression...
		if( true == (m_bWhitespace = (0 == m_expression.length())) )
			m_expression = expression;
	} // AbstractToken
	
// Operations
	/**
	 * <p>The method <code>__safe_expression</code> returns the expression of
	 * a token without throwing a <code>NullPointerException</code>, when the
	 * given token does not references a existing token object. Typically this
	 * method is called during the construction phase of a token object to
	 * prevent a <code>NullPointerException</code>, when the tokens expression
	 * is needed as an argument by a super constructor.</p>
	 * @param token
	 * 		- can be either <code>null</code> or representing an existing token
	 * 		object.
	 * @return
	 * 		Returns <code>null</code> or the expression of the given token.
	 */
	@SuppressWarnings("unchecked")
    public static String __safe_expression( Token token ) {
		
		if( null != token )
			return token.expression();
		
		return null;
	} // __safe_expression

	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token#expression()
	 */
	public String expression() {
		
		return m_expression;
	} // expression
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token#isWhitespace()
	 */
	public boolean isWhitespace() {
	
		return m_bWhitespace;
	} // isWhitespace
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token#line()
	 */
	public int line() {
	
		return m_line;
	} // line
		
} // AbstractToken
