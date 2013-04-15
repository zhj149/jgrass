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
  package eu.hydrologis.jgrass.console.editor.actions;

import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;

/**
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public class ConsoleTimedeltaActionSet
    extends TextEditorAction {

// Attributes
	private static long m_nTimeDeltaInMillis = 0;
	
// Construction
	/** */
	public ConsoleTimedeltaActionSet( ResourceBundle bundle, String prefix,
	    	ITextEditor editor ) {

		super( bundle, prefix, editor );
	} // ConsoleTimedeltaActionSet

// Operations
	/** */
	public static void updateOptions( ProjectOptions projectOptions ) {
		
		if( 0 >= m_nTimeDeltaInMillis )
			projectOptions.setOption(
					ProjectOptions.JAVA_MODEL_TIME_DELTA
					, null
				);
		else
			projectOptions.setOption(
					ProjectOptions.JAVA_MODEL_TIME_DELTA
					, Long.toString( m_nTimeDeltaInMillis )
				);
	} // updateOptions
	
	/* (
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {

		Display.getDefault().asyncExec(
			new Runnable() {
				public void run() {		
					IInputValidator validator =
						new IInputValidator() {
							public String isValid( String newText ) {
								String retval = null;
								try {
									if( 0 == newText.length() ) {
										m_nTimeDeltaInMillis = 0;
									}
									else {
										m_nTimeDeltaInMillis = Long.parseLong(
												newText
											);
										if( 0 > m_nTimeDeltaInMillis )
											retval = "Enter a positive delta/interval number";
									}
								}
								catch( Exception e ) {
									
									retval = "Enter the interval in milliseconds";
								}
								return retval;
							}
						};
					InputDialog dialog = new InputDialog(
							PlatformUI
								.getWorkbench()
								.getDisplay()
								.getActiveShell()
							, "Time delta"
							, "Enter the interval in milliseconds"
							, Long.toString( m_nTimeDeltaInMillis )
							, validator
						);
					
					dialog.open();
				} // run
			} // Runnable
		);
	} // run
	
} // ConsoleTimedeltaActionSet
