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
package eu.hydrologis.jgrass.console.core.internal.analysis;

/**
 * <p></p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
@SuppressWarnings("serial")
public class CompilerError
    extends Exception {

// Construction
	/**
	 * <p>Constructs a new error exception with null as its detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to <code>Throwable.initCause(java.lang.Throwable)</code>.</p> 
	 */
	public CompilerError() {
		
		super();
	} // Error
	
	/**
	 * <p>Constructs a new error exception with the specified detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to <code>Throwable.initCause(java.lang.Throwable)</code>.</p>
	 * @param message
	 * 		- the detail message. The detail message is saved for later
	 * 		retrieval by the <code>Throwable.getMessage()</code> method.
	 */
	public CompilerError( String message ) {
		
		super( message );
	} // Error
	
	/**
	 * <p>Constructs a new error exception with the specified detail message and
	 * cause.</p><p>Note that the detail message associated with cause is not
	 * automatically incorporated in this exception's detail message.</p>
	 * @param message
	 * 		- the detail message. The detail message is saved for later
	 * 		retrieval by the <code>Throwable.getMessage()</code> method.
	 * @param cause
	 * 		- the cause (which is saved for later retrieval by the
	 * 		<code>Throwable.getCause()</code> method). A <code>null</code>
	 * 		value is permitted, and indicates that the cause is nonexistent
	 * 		or unknown.
	 */
	public CompilerError( String message, Throwable cause ) {
		
		super( message, cause );
	} // Error
	
	/**
	 * <p>Constructs a new error exception with the specified cause and a
	 * detail message of (<code>cause==null ? null : cause.toString()</code>)
	 * (which typically contains the class and detail message of
	 * <code>cause</code>). This constructor is useful for exceptions that are
	 * little more than wrappers for other throwables (for example,
	 * <code>PrivilegedActionException</code>).</p>
	 * @param cause
	 * 		- the cause (which is saved for later retrieval by the
	 * 		<code>Throwable.getCause()</code> method). A <code>null</code>
	 * 		value is permitted, and indicates that the cause is nonexistent
	 * 		or unknown.
	 */
	public CompilerError( Throwable cause ) {
		
		super( cause );
	} // Error
	
} // Error
