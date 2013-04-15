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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;

/**
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public class ConsoleStartupTimeActionSet extends TextEditorAction {

    // Attributes
    private static Date m_tsStartup = null;

    // Construction
    /** */
    public ConsoleStartupTimeActionSet( ResourceBundle bundle, String prefix, ITextEditor editor ) {

        super(bundle, prefix, editor);
    } // ConsoleStartupTimeActionSet

    // Operations
    /** */
    public static void updateOptions( ProjectOptions projectOptions ) {

        if (null == m_tsStartup)
            projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_START_UP, null);
        else
            projectOptions.setOption(ProjectOptions.JAVA_MODEL_TIME_START_UP, DateFormat
                    .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault())
                    .format(m_tsStartup));
    } // updateOptions

    /*
     * (
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {

        Display.getDefault().asyncExec(new Runnable(){
            public void run() {

                Display display = Display.getDefault();
                final Shell shell = new Shell(display);
                shell.setText("Nebula CDateTime");
                shell.setLayout(new GridLayout());

                GridLayout layout = new GridLayout();
                shell.setLayout(layout);

                // final CDateTime cdt = new CDateTime(shell, CDT.BORDER | CDT.DROP_DOWN
                // | CDT.CLOCK_24_HOUR);
                // cdt.setPattern("'Starttime is on' EEEE, MMMM d yyyy 'at' h:mm a");
                // cdt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

                shell.pack();
                Point size = shell.getSize();
                Rectangle screen = display.getMonitors()[0].getBounds();
                shell.setBounds((screen.width - size.x) / 2, (screen.height - size.y) / 2, size.x,
                        size.y);
                shell.open();
                while( !shell.isDisposed() ) {
                    if (!display.readAndDispatch())
                        display.sleep();
                }
                // display.dispose();
                System.out.println("Passed");

                // IInputValidator validator = new IInputValidator() {
                // public String isValid(String newText) {
                // String retval = null;
                // try {
                // if (0 == newText.length()) {
                // m_tsStartup = null;
                // } else {
                // DateFormat formatter = DateFormat
                // .getDateTimeInstance(DateFormat.MEDIUM,
                // DateFormat.MEDIUM, Locale
                // .getDefault());
                // Date dtLocale = formatter.parse(newText);
                // m_tsStartup = new Date(dtLocale.getTime());
                // }
                // } catch (Exception e) {
                //
                // retval = "Set the time";
                // }
                // return retval;
                // }
                // };
                //
                // String szLocaleDate;
                // if (null == m_tsStartup)
                // szLocaleDate = null;
                // else
                // szLocaleDate = DateFormat.getDateTimeInstance(
                // DateFormat.MEDIUM, DateFormat.MEDIUM,
                // Locale.getDefault()).format(m_tsStartup);
                // InputDialog dialog = new
                // InputDialog(PlatformUI.getWorkbench()
                // .getDisplay().getActiveShell(), "Start-up time",
                // "Enter a locale date", szLocaleDate, validator);
                //
                // dialog.open();
            } // run
        } // Runnable
                );
    } // run

} // ConsoleStartupTimeActionSet
