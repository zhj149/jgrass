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
package eu.hydrologis.jgrass.console.core.internal.parser;

import java.util.ArrayList;
import java.util.List;

import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerError;
import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerWarning;
import eu.hydrologis.jgrass.console.core.internal.lexer.Lexer;
import eu.hydrologis.jgrass.console.core.internal.nodes.APT;
import eu.hydrologis.jgrass.console.core.internal.nodes.Production;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symtable;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>The class <code>AbstractParser</code> provides default implementation
 * for the <code>Parser</code> interface and defines standard behavior for
 * the methods: <code>projectSpace</code> and <code>parse</code>. The developer
 * subclasses this abstract class, initializes the parser with the productions
 * of the grammar and implements the methods for the analysis part, syntax and
 * semantic analysis, of the parsing phase: <code>analyse</code> and
 * <code>analysisMessage</code>.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractParser<P, X, S>
    implements Parser<P, X, S> {

// Attributes
	/**
	 * The lexical analyzer, which is in use by this syntax analyzer (parser).
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer
	 */
	private final Lexer<X> m_lexer;
	
	/**
	 * The language dependend set of productions used to create the abstract
	 * parse tree. 
	 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Production
	 */
	private final Production<P, X, S>[] m_productions;
	
	/**
	 * The project-space, which is currently in use by this syntax analyzer
	 * (parser).
	 * @see eu.hydrologis.jgrass.console.core.internal.parser.Parser#projectSpace()
	 */
	private final Projectspace m_projectSpace;
	
// Construction
	/**
	 * <p>The copy constructor <code>AbstractParser</code> creates this syntax
	 * analyzer (parser) with the specified project-space and uses the
	 * specified lexical analyzer (scanner) for the lexical analysis of the
	 * input stream.</p>
	 * @param projectSpace
	 * 		- the project-space.
	 * @param lexer
	 * 		- the lexical analyzer (scanner) used by this syntax analyzer
	 * 		(parser) for the lexical analysis of the input stream.
	 * @throws IllegalArgumentException
	 * 		- if <code>projectSpace</code> or <code>lexer</code> references
	 * 		the null type.
	 */
	public AbstractParser( Projectspace projectSpace, Lexer<X> lexer ) {
		
		super();
		if( null == projectSpace )
			throw new IllegalArgumentException();
		if( null == lexer )
			throw new IllegalArgumentException();
		
		m_productions = productions();
		m_projectSpace = projectSpace;
		m_lexer = lexer;
	} // AbstractParser
	
// Operations
	/**
	 * <p>The method <code>intermediate</code> creates a tree-like intermediate
	 * representation of the obtained token stream and extends the specified
	 * parse tree by using its production.</p>
	 * @param symtable
	 * 		- the symbole table.
	 * @param parseTree
	 * 		- the parsetree.
	 * @param tstream
	 * 		- the token stream.
	 * @return
	 * 		The operator for a subsequent production; the root for a tree-like
	 * 		intermediate representation of a subsequent programming construct.
	 * @throws IllegalArgumentException
	 * 		- if the specified symbol table, parse tree or token stream
	 * 		references the <code>null</code> type.
	 */
	private APT<P> intermediate( Symtable<S> symtable, APT<P> parseTree,
			Token<X>... tstream ) {
		
		if( null == symtable )
			throw new IllegalArgumentException();
		if( null == parseTree )
			throw new IllegalArgumentException();
		if( null == tstream || 0 >= tstream.length )
			throw new IllegalArgumentException();
		
		Production<P, X, S> production = null;
		int candidate = 0;
		while( null == production && candidate < m_productions.length ) {
		
			X[][] produces = m_productions[ candidate ].produces( parseTree);
			if( null != produces && 0 < produces.length ) {
				
				int statement = 0;
				while( null == production && statement < produces.length ) {
					
					X[] rule = produces[ statement++ ];
					if( null != rule && tstream.length == rule.length ) {
						
						int x = 0;
						while( x < tstream.length &&
									rule[ x ] == tstream[ x ].identifier()
								) {
							++x;
						}
						if( x == tstream.length )
							production = m_productions[ candidate ];
					}
				}
			}
			
			++candidate;
		}
		
		if( null != production )
			return production.translate( symtable, parseTree, tstream );
		
		return parseTree;
	} // intermediate
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.parser.Parser#parse()
	 */
	@SuppressWarnings("unchecked")
    public APT<P> parse( final Symtable<S> symtable, final APT<P> parseTree )
    	throws Exception {

		APT<P> __parseTree = parseTree;
		
		List<Token<X>> tstream = new ArrayList<Token<X>>();
		Token<X> token;
		do {
			
			tstream.add( token = m_lexer.tokscn() );
			try {
				
				Token<X>[] __tstream = new Token[ tstream.size() ];
				tstream.toArray( __tstream );
				switch( analyse( symtable, __parseTree, __tstream ) ) {
				case SUCCESS:
					// The analyzes of the current token stream is
					//	successful completed therefore a intermediate
					//	representation can be created of the token
					//	stream...
					__parseTree = intermediate(
							symtable
							, __parseTree
							, __tstream
						);
					
				case IGNORE_AND_CONTINUE:
					// Clean sweep: clear the list of current
					//	read ahead tokens to zero...
					tstream.clear();
					
				case CONTINUE:
					break;
					
				default:
					throw new InternalError();
				}
			}
			catch( CompilerWarning e ) {
				
				projectSpace().err.println( e.getLocalizedMessage() );
				e.printStackTrace();
			}
			catch( CompilerError e ) {
				
				projectSpace().err.println( e.getLocalizedMessage() );
				e.printStackTrace();
				throw new Exception( e );
			}
		} while( null != token );
		
		return parseTree;
	} // parse
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.parser.Parser#projectSpace()
	 */
	public Projectspace projectSpace() {
		
		return m_projectSpace;
	} // projectSpace
	
} // AbstractParser
