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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
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
public class ConsoleEndingupTimeActionSet
extends TextEditorAction {

// Attributes
	private static Date m_tsEndingup = null;
	
// Construction
	/** */
	public ConsoleEndingupTimeActionSet( ResourceBundle bundle, String prefix,
	    	ITextEditor editor ) {

		super( bundle, prefix, editor );
	} // ConsoleEndingupTimeActionSet

// Operations
	/** */
	public static void updateOptions( ProjectOptions projectOptions ) {
		
		if( null == m_tsEndingup )
			projectOptions.setOption(
					ProjectOptions.JAVA_MODEL_TIME_ENDING_UP
					, null
				);
		else
			projectOptions.setOption(
					ProjectOptions.JAVA_MODEL_TIME_ENDING_UP
					, DateFormat.getDateTimeInstance(
							DateFormat.MEDIUM
							, DateFormat.MEDIUM
							, Locale.getDefault()
						).format(
								m_tsEndingup
						)
				);
	} // updateOptions
	
	/* 
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
										m_tsEndingup = null;
									}
									else {
										DateFormat formatter =
											DateFormat.getDateTimeInstance(
													DateFormat.MEDIUM
													, DateFormat.MEDIUM
													, Locale.getDefault()
												);
										Date dtLocale = formatter.parse(
												newText
											);
										m_tsEndingup = new Date(
												dtLocale.getTime()
											);
									}
								}
								catch( Exception e ) {
									
									retval = "Set the time";
								}
								return retval;
							}
						};
					String szLocaleDate;
					if( null == m_tsEndingup )
						szLocaleDate = null;
					else
						szLocaleDate =
							DateFormat.getDateTimeInstance(
									DateFormat.MEDIUM
									, DateFormat.MEDIUM
									, Locale.getDefault()
								).format(
										m_tsEndingup
								);
					InputDialog dialog = new InputDialog(
							PlatformUI
								.getWorkbench()
								.getDisplay()
								.getActiveShell()
							, "Ending up time"
							, "Enter a locale date"
							, szLocaleDate
							, validator
						);
					
					dialog.open();
				} // run
			} // Runnable
		);
	} // run
	
} // ConsoleEndingupTimeActionSet
