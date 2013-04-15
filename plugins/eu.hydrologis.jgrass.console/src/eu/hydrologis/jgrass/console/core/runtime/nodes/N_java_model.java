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

import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractToken;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;

/**
 * <p>A non-terminal <b>java-model</b> token.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class N_java_model
	extends AbstractToken<TOKs>
	implements Token<TOKs> {
	
// Attributes
	/**
	 * The model has a postfix asterisk operator <code>true</code>, otherwise
	 * <code>false</code>.
	 */
	private boolean m_asterisk;
	
	/** The type name of the model. */
	private String m_type;
	
	/** The tokens identity name. */
	public final static TOKs identifier = TOKs.JAVA_MODEL;

    private final String expression;
	
// Construction
	/**
	 * <p>Constructs this object with the specified line number and a copy of
	 * the specified expression. According to evaluate the expression this
	 * token needs to know the symbolic character of the asterisk operator.</p>
	 * @param expression
	 * 		- character string in the source program detected by the lexical
	 * 		analyzer that matches the pattern of the corresponding lexeme for
	 * 		this token.
	 * @param line
	 * 		- line number in the source program.
	 * @param asterisk
	 * 		- the character which identifies the asterisk operator.
	 * @throws IllegalArgumentException
	 * 		- if <code>expression</code> references the null type or the length
	 * 		of the character string is zero.
	 */
	public N_java_model( String expression, int line, Character asterisk ) {
		
		super( expression, line );
        this.expression = expression;
		__initialize( asterisk );
	} // N_java_model

	/** 
	 * <p>The method <code>__initialize</code> dissect's at construction time
	 * the given expression and evaluates, if the expression has a postfix
	 * asterisk operator.</p>
	 * @param asterisk
	 * 		- the asterisk operator.
	 */
	private void __initialize( Character asterisk ) {
		
		final String term = AbstractToken.__safe_expression( this );
		m_asterisk =
			( term.indexOf( asterisk ) == term.length() - 1 ) ?
					true:false;
		if( true == m_asterisk ) {
			
			m_type = term.substring( 0, term.lastIndexOf( asterisk ) );
		}
		else {
			
			m_type = term;
		}
	} // __initialize
	
// Operations
	/**
	 * <p>Returns <code>true</code>, if the expression has a postfix asterisk
	 * operator, otherwise <code>false</code>.</p>
	 */
	public boolean __asterisk() {
		
		return m_asterisk;
	} // __asterisk
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Token#identifier()
	 */
	public TOKs identifier() {
	
		return identifier;
	} // identifier
	
	/**
	 * <p>Returns the name of the model.</p>
	 */
	public String type() {
		
		return m_type;
	} // type

    /**
     * Constructs a <code>String</code> with all attributes
     * in name = value format.
     *
     * @return a <code>String</code> representation 
     * of this object.
     */
    public String toString()
    {
        final String TAB = "    ";
        final String LF = "\n";
        StringBuilder retValue = new StringBuilder();
        
        retValue.append("N_java_model (").append(LF)
            .append(super.getClass().getSimpleName()).append(LF)
            .append(TAB).append("expression = ").append(this.expression).append(LF)
            .append(TAB).append("m_asterisk = ").append(this.m_asterisk).append(LF)
            .append(TAB).append("m_type = ").append(this.m_type).append(LF)
            .append(" )");
        
        return retValue.toString();
    }


	
} // N_java_model
