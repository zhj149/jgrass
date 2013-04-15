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
package eu.hydrologis.jgrass.console.core.internal.lexer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import eu.hydrologis.jgrass.console.core.internal.nodes.Lexeme;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>The class <code>AbstractLexer</code> provides default implementation for
 * the <code>Lexer</code> interface and defines standard behavior for the
 * methods: <code>scan</code>, <code>line</code>, <code>tokenized</code> and
 * <code>projectSpace</code>. The developer subclasses this abstract class,
 * defines the characters being treat as white-space or punctuation by the
 * scanner, defines the set of lexemes and an optional lexeme, used to produce
 * a token as output, in the case of the lexical analysis fails; also, the
 * developer need to define the behavior of the method <code>tokskip</code>.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractLexer<X>
    implements Lexer<X> {

// Attributes
	/** The maximum size of the look ahead buffer in bytes. */
	private final static int MAX_LOOK_AHAED_BUF_CAPACITY = 4096;
	
	/**
	 * A copy of the lexemes set.
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#lexemes()
	 */
	private final Lexeme<X>[] m_lexemes;
	
	/**
	 * Holds the last processed line number information.
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#line()
	 */
	private int m_nSaveLine;
	
	/**
	 * Holds the last processed line number information.
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#line()
	 */
	private int m_line;
	
	/**
	 * The project-space, which is currently in use by this lexical analyzer
	 * (scanner).
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#projectSpace()
	 */
	private final Projectspace m_projectSpace;
	
	/**
	 * A copy of the punctuators set.
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#punctuators()
	 */
	private final Character[] m_punctuators;
	
	/**
	 * A copy of the input respectively of the source program.
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.AbstractLexer#AbstractLexer(Projectspace, Reader, int)
	 */
	private final Reader m_sourceCode;
	
	/**
	 * The set of tokens incrementally collected by the lexical analyzer
	 * (scanner) during the lexical analysis phase.
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#tokenized()
	 */
	private final List<Token<X>> m_tokenized;
	
	// Construction
	/**
	 * <p>The copy constructor <code>AbstractLexer</code> creates this lexical
	 * analyzer (scanner) with the specified project-space, the specified
	 * reader as input stream and the specified line number information as the
	 * start-point.</p>
	 * @param projectSpace
	 * 		- the project-space.
	 * @param sourceCode
	 * 		- the input stream.
	 * @param line
	 * 		- the line number in the source program used by this lexical
	 * 		analyzer (scanner) as start-point.
	 * @throws IllegalArgumentException
	 * 		- if <code>projectSpace</code> or <code>reader</code> references
	 * 		the null type.
	 */
	public AbstractLexer( Projectspace projectSpace, Reader sourceCode,
			int line ) {
		
		super();
		
		if( null == projectSpace )
			throw new IllegalArgumentException();
		if( null == sourceCode )
			throw new IllegalArgumentException();
		
		m_projectSpace = projectSpace;
		if( true == sourceCode.markSupported() ) {
			
			m_sourceCode = sourceCode;
		}
		else {
			
			StringBuffer stringBuffer = new StringBuffer();
			char[] chbuf = new char[ MAX_LOOK_AHAED_BUF_CAPACITY ];
			int count;
			try {
				
				do {
					
					count = sourceCode.read( chbuf );
					if( -1 != count )
						stringBuffer.append( chbuf, 0, count );
				} while( -1 != count );
			}
			catch( IOException e ) {
				
				if( true == Projectspace.isErrorEnabled() )
					projectSpace().err.println( e );
				
				stringBuffer.setLength( 0 );
			}
			finally {
				
				m_sourceCode = new StringReader( stringBuffer.toString() );
			}
		}
		
		m_punctuators = punctuators();
		m_lexemes     = lexemes();
		m_line        = line;
		m_nSaveLine   = line;
		m_tokenized   = new ArrayList<Token<X>>();
	} // AbstractLexer
	
// Operations
	/**
	 * <p>The method <code>__tokscn</code> scans for another token and returns
	 * it, if this lexical analyzer (scanner) has another token in its
	 * input.</p>
	 * @return
	 * 		Returns another token object if and only if this lexical analyzer
	 * 		(scanner) has another token, otherwise <code>null</code>.  
	 */
    private Token<X> __tokscn() {
		
		Token<X> token = null;
		try {
			
			// First, before reading a character from the the input stream,
			//	do marking the current position...
			if( true == m_sourceCode.markSupported() )
				m_sourceCode.mark( 0 );
			
			final int __nStartupLine = m_line;
				// Setting up the read ahead buffer...
			StringBuffer s = new StringBuffer();
				s.ensureCapacity( MAX_LOOK_AHAED_BUF_CAPACITY );
				// Initialize the character informations...
			Character character;
			do {
				
				// Reading a single character from stream...
				character = getch();
				
				// Analyzing the single character... attempt to classify the
				//	single character into whitespace, delimiter or to a
				//	non-classified alphanumeric character. 
				boolean whitespace = ( true == whitespace( character ) );
				boolean delimits = ( true == delimits( character ) );
				boolean tokenized = (
						null == character || true == (delimits|whitespace)
					);
				
				if( false == tokenized ) {
					
					// Appending the character to expression, if we have
					//	not completed the tokenizing mission, yet...
					s.append( character );
				}
				else {
					
					// In case of, the expression (s) length is zero, we have
					//	to append the current read whitespace, delimiter or
					//	non-classified character to the expression (s)...
					if( 0 == s.length() && null != character )
						s.append( character );
						
					// Attempt to uncover the expression to already known axiom
					//	resp. lexeme, which will then result into a transition
					//	of the lexeme to a lexical, evaluable token readable
					//	by the upstream process parser...
					//
					//	Yet, if the attempt to uncover fails, the scanning
					//	progress repeats, until the next tokeninzable is
					//	detected...
					Lexeme<X> lexeme = null;
					if( 0 < s.length() ) {
						
						int n = 0;
						do {
							
							int k = 0;
							while( k < m_lexemes.length && null == lexeme ) {
								
								Lexeme<X> candidate = m_lexemes[ k ]; 
								if( true == candidate.analyse( s ) )
									lexeme = candidate;
								++k;
							}
							
							if( null == lexeme ) {
								
								if( 0 == n ) {
									
									if( null == character ) {
										
										s.append( Lexeme.LF );
									}
									else {
										
										s.append( character );
									}
								}
								
								++n;
							}
						} while( null == lexeme && 1 >= n );
						
						if( true == (delimits|whitespace) ) {
							
							switch( character ) {
							case Lexeme.LF:
								if( m_nSaveLine == m_line )
									++m_line;
								m_nSaveLine = m_line;
								break;
								
							case Lexeme.CR:
								m_nSaveLine = m_line;
								++m_line;
								break;
								
							default:
								if( m_nSaveLine != m_line )
									m_nSaveLine = m_line;
							}
							
							if( ( 1 <  s.length() && null != lexeme ) ||
								( 1 == s.length() &&
										s.charAt( 0 ) != character.charValue()
											)
									) {
								
								if( true == m_sourceCode.markSupported() ) {
									
									m_sourceCode.skip( -1 );
									
									switch( character ) {
									case Lexeme.LF:
										--m_nSaveLine;
										--m_line;
										break;
										
									case Lexeme.CR:
										--m_line;
										break;
									}
								}
							}
						}
						
						if( null != lexeme ) {
							
							// Transition from lexeme to token and to a
							//	candidate as return value...
							token = lexeme.transition( s, m_line );
							if( null != token )
								m_tokenized.add( token );
							
							// Marking the current position because we got
							//	a lexeme...
							if( true == m_sourceCode.markSupported() )
								m_sourceCode.mark( 0 );
							
							// Clean sweep: reset the length of the current
							//	read ahead characters to zero, to prepare
							//	the attemption to detect the next token,
							//	without returning to the caller...
							s.setLength( 0 );
						}						
					}
				}
			} while( null == token && null != character );
			
			// If the attemption to tokenize above has failed and if it's
			//	possible, the scanner will at last attempt to repair resp.
			//	rescue the parsing session... the following code section
			//	is the detection and damage repair center of the scanner,
			//	if no token has been uncovered by the above already made
			//	lexical analysis...
			if( null == token && 0 < s.length() && null != rescue() ) {
			
				// Rescue the parsing progress by attempting to instantiate a 
				//	garbage lexeme, which is so far called rescue lexeme in
				//	the meaning of unknown...
				//
				// but before, moving the stream to the startup position, to
				//	prepare a newly attemption to tokenize...
				if( true == m_sourceCode.markSupported() )
					m_sourceCode.reset();
				
				try {
					
					// Using simply the default scanner shipped by the JDK to
					//	extract the yet not known token, if there is one...
					//
					//	Warning		If the attemption to uncover a token fails,
					//				this indicates a un-repairable(=irreparable)
					//				ERROR, which should result in a termination
					//				of the passing progress...
					//
					//	Caution		Possible loss of whitespace character data
					//				information -- should not but could, if the
					//				whitespace character is not registered.
					//
					Scanner scanner = new Scanner( m_sourceCode );
					if( true == scanner.hasNext() ) 
						token = rescue().transition(
								scanner.next()
								, __nStartupLine
							);
					if( null != token )
						m_tokenized.add( token );
				}
				finally {
					
					// The class Scanner of the JDK caches all tokens at once,
					//	therefore we need to reset the input stream - twice...
					if( true == m_sourceCode.markSupported() )
						m_sourceCode.reset();
					//	and move forward -- skip, if any token is resolved.
					if( true == m_sourceCode.markSupported() &&
								null != token
							) {
						
						char[] cb = new char[ token.expression().length() ];
						m_sourceCode.read( cb );
						m_sourceCode.mark( 0 );
					}
				}
			}
		}
		catch( Exception e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				projectSpace().err.println(  e );
			
			e.printStackTrace();
		}
		
		return token;
	} // __tokscn
    
	/**
	 * <p>Determines, if the specified character is a punctuator. The method
	 * <code>delimits</code> uses the set of characters returned by the method
	 * <code>punctuators</code>.</p>
	 * @param character
	 * 		- the character to be tested.
	 * @return
	 * 		<code>true</code> if the character is a delimiter character;
	 * 		<code>false</code> otherwise.
	 */
	private boolean delimits( Character character ) {
		
		boolean retval = false;
		if( null != character && null != m_punctuators ) {
		
			int i = 0;
			while( i < m_punctuators.length && false == retval ) {
				
				retval = ( 0 == character.compareTo(m_punctuators[ i ]) ) ?
						true:false;
				++i;
			}
		}
		
		return retval;
	} // delimits
	
	/**
	 * <p>Get a character from the lexical analyzer's input stream. The method
	 * <code>getch</code> reads a single character from the input stream and
	 * moves forward to the next character position.</p>
	 * @return
	 * 		Returns the character read or <code>null</code>, if the end of the
	 * 		input stream is reached. There is no ERROR return.
	 */
	private Character getch( ) {
		
		Character retval = null;
		try {
			
			int ch = m_sourceCode.read();
			if( -1 != ch )
				retval = new Character( ( char )ch );
		}
		catch( Exception e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				projectSpace().err.println(  e );
			
			e.printStackTrace();
		}
		
		return retval;
	} // getch
	
	/**
	 * <p>The method <code>whitespace</code> determines, if the specified
	 * character is white space.</p>
	 * @param character
	 * 		- the character to be tested.
	 * @return
	 * 		Returns <code>true</code> if the character is a whitespace
	 * 		character; otherwise, if character is <code>null</code> or not a
	 * 		whitespace character the method returns <code>false</code>.
	 */
	private boolean whitespace( Character character ) {
		
		return(
				null != character &&
					true == Character.isWhitespace( character )
			);
	} // whitespace
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#blockRead(Object,Object)
	 */
    final public Reader blockRead( X t_start, X t_end )
		throws IOException {
		
		Reader retval = null;
		try {
			
			Writer writer = new StringWriter();
			int __blkcnt = 0;
			int __tokcnt = 0;
			boolean block = false;
			Token<X> token;
			do {
				
				token = __tokscn();
				if( null == token ) {
					
					block = true;
				}
				else {
					
					if( t_end == token.identifier() ) {
						
						if( 0 >= __blkcnt )
							throw new Exception();
						
						--__blkcnt;
						if( 0 < __blkcnt )
							writer.append( token.expression() );
						block = ( 0 >= __blkcnt );
					}
					else if( t_start == token.identifier() ) {
						
						if( 0 >= __blkcnt && 0 < __tokcnt )
							throw new Exception();
						
						++__blkcnt;
						if( 1 < __blkcnt )
							writer.append( token.expression() );
					}
					else {
						
						writer.append( token.expression() );
						
						if( false == token.isWhitespace() ) {
							
							++__tokcnt;
						}
						else if( 0 >= __blkcnt ) {
							
							String whitespace = token.expression();
							if( -1 != whitespace.indexOf( '\r' ) ||
								-1 != whitespace.indexOf( '\n' ) ) {
								
								block = true;
							}
						}
					}
				}
			} while( false == block );
			
			if( 0 != __tokcnt ) {
			
				writer.flush();
				retval = new StringReader( writer.toString() );
			}
		}
		catch( IOException e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				projectSpace().err.println( e );

			throw e;
		}
		catch( Exception e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				projectSpace().err.println( e );
			
			e.printStackTrace();
		}
		
		return retval;
	} // blockRead
    
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#line()
	 */
	public int line() {
		
		return m_line;
	} // line
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#projectSpace()
	 */
	public Projectspace projectSpace() {
		
		return m_projectSpace;
	} // projectSpace
    
    /* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.parser.Lexer#tokenized()
	 */
	@SuppressWarnings("unchecked")
    final public Token<X>[] tokenized() {
		
		Token<X>[] retval = null;
		if( null != m_tokenized && 0 < m_tokenized.size() ) {
			
			m_tokenized.toArray( 
					retval = new Token[ m_tokenized.size() ]
				);
		}
		
		return retval;
	} // tokenized
	
	/* (non-Javadoc)
	 * @see eu.hydrologis.jgrass.console.core.internal.lexer.Lexer#tokscn()
	 */
    final public Token<X> tokscn() {
		
		Token<X> token = null;
		do {
							
			token = __tokscn();
		} while( null != token && true == tokskip( token.identifier() ) );
		
		return token;
	} // tokscn
	
} // AbstractLexer
