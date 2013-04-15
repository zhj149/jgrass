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
package eu.hydrologis.jgrass.console.ui.platform;

import eu.hydrologis.jgrass.console.core.JGrass;
import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.core.runtime.ConsoleEngine;

/**
 * <p>The class <code>PlatformConsole</code> is a approache implementation of
 * character/text based console.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class PlatformConsole {

// Interfaces
	/**
	 * <p>The static interface <code>Command</code> defines console specific
	 * commands and console specific text-message outputs.</p>
	 */
	private static interface Command {
		
	// Attributes
		public final static String EXIT_CONSOLE  = "exit"; //$NON-NLS-1$
		public final static String QUIT_CONSOLE  = "quit"; //$NON-NLS-1$
		
	} // Command
	
	/**
	 * <p>The static interface <code>Command</code> defines console specific
	 * commands and console specific text-message outputs.</p>
	 */
	private static interface Message {
		
	// Attributes
		public final static String CONSOLE_NA    = "PlatformConsole not available."; //$NON-NLS-1$
		public final static String PROMPT        = ">"; //$NON-NLS-1$
		public final static String TERM_INSTANCE = "Good bye."; //$NON-NLS-1$
		
	} // Message
	
// Attributes
	
// Operations
	/**
	 * <p>The static <code>main</code> method of the textual console version for
	 * the <code>JGrass</code> interface.</p>
	 * 
	 * @param args
	 *  <p>The argument <code>args</code> as array of <code>String</code> holds
	 *  the optional arguments passed to the class on the java command line.</p>
	 * @throws Exception
	 * 	
	 */
	public static void main( String[] args ) throws Exception {
		
		try {
			
			ProjectOptions projectOptions = new ProjectOptions();
			projectOptions.projectCaption( "Console" ); //$NON-NLS-1$
			projectOptions.setOption(
					ProjectOptions.CONSOLE_ASYNC_MODE
					, new Boolean( false )
				);
			projectOptions.setOption(
					ProjectOptions.CONSOLE_COMPILE_ONLY
					, false
				);
			projectOptions.setOption(
					ProjectOptions.CONSOLE_THREAD_RESTRICTION
					, 0
				);
			projectOptions.setOption(
					ProjectOptions.CONSOLE_DIRECTORY_INCLUDE
					, new String[] { "rt" } //$NON-NLS-1$
				);
			projectOptions.setOption(
					ProjectOptions.CONSOLE_DIRECTORY_SOURCE
					, "D:/Dokumente und Einstellungen/Internet/Eigene Dateien" //$NON-NLS-1$
				);
			projectOptions.setOption(
					ProjectOptions.CONSOLE_LOGGING_LEVEL_DEBUG
					, true
				);
			projectOptions.setOption(
					ProjectOptions.CONSOLE_LOGGING_LEVEL_TRACE
					, true
				);
			projectOptions.setOption(
					ProjectOptions.COMMON_GRASS_MAPSET
					, "D:/CVSHOME/grassdb/flanginec/prova" //$NON-NLS-1$
				);
			projectOptions.setOption(
					ProjectOptions.NATIVE_MODEL_DEBUG
					, true
				);
			projectOptions.setOption(
					ProjectOptions.NATIVE_MODEL_GISBASE
					, "D:/CVSHOME/Java/HydroloGIS/6/grass63RC1/grass-6.3.0RC1" //$NON-NLS-1$
				);
			projectOptions.setOption(
					ProjectOptions.NATIVE_MODEL_USER_HOME
					, "C:/Dokumente und Einstellungen/Internet" //$NON-NLS-1$
				);
			projectOptions.setOption(
					ProjectOptions.NATIVE_MODEL_USER_NAME
					, "Internet" //$NON-NLS-1$
				);
			
			JGrass console = new ConsoleEngine( args );
			StringBuffer stringBuffer = new StringBuffer();
			do {
				
				System.out.print( Message.PROMPT );
				
				// Reading from the console stream a single command. Reading
				//	from the console stream is interrupted by the user when
				//	pressing the keyboard key Enter or respectively Return.
				//
				//	Caution		The escape sequences such like linefeed (LF) 
				//				and carriage return (CR) are ignored and
				//				will not be copied into the string buffer.
				int ch;
				do {
					
					ch = System.in.read();
					stringBuffer.append( ( char )ch );
				} while( '\n' != ch );
				
				// Proceed to process or to dispatch the current command in
				//	the command line, if it is a command. Furthermore,
				//	prepare the state of the console application for user
				//	prompt renewal.
				if( 0 != stringBuffer.length() ) {
				
					// Cleaning up command line from leading and trailing
					//	whitespace, furthermore compare the content of the
					//	command line to console specific commands first. In
					//	the case of the command line does not matches any
					//	console specific command, the content of the command
					//	line then should be dispatched to the JGrass
					//	backyard for further processes.
					String string = stringBuffer.toString();
					string = string.trim();
					if( 0 != string.length() ) {
					
						if( 0 == string.compareToIgnoreCase( 
									Command.EXIT_CONSOLE
								)
							)
							break;
						else if( 0 == string.compareToIgnoreCase( 
									Command.QUIT_CONSOLE
								)
							)
							break;
						else
							console.dispatch( projectOptions, string );
					}
					
					// Prepare for a renewal user prompt by a consequent
					//	reset of the string buffer object content...
					stringBuffer.setLength( 0 );
				}
			} while( true );
		}
		catch( Exception e ) {
			
			e.printStackTrace();
			throw e;
		}
		finally {
			
			System.out.println( Message.TERM_INSTANCE );
		}
	} // main

} // PlatformConsole