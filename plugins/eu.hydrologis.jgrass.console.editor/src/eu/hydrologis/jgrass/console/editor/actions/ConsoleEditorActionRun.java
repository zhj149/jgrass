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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.examples.javaeditor.JavaEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import eu.hydrologis.jgrass.console.ConsolePlugin;
import eu.hydrologis.jgrass.console.core.JGrass;
import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class ConsoleEditorActionRun extends TextEditorAction
        implements
            IWorkbenchWindowActionDelegate {

    public static final String ID = "eu.hydrologis.jgrass.console.editor.consoleruncommand"; //$NON-NLS-1$

    // Attributes
    /** */
    private String m_szMsgFmtText = null;

    // Construction
    public ConsoleEditorActionRun( ResourceBundle bundle, String prefix, ITextEditor editor ) {

        super(bundle, prefix, editor);
        m_szMsgFmtText = getText();

        setId(ID);
    } // ConsoleEditorActionRun
    // Operations
    /*
     * @see org.eclipse.jface.action.Action#run()
     */

    public void run() {

        Display.getDefault().asyncExec(new Runnable(){

            public void run() {

                JGrass console = ConsolePlugin.console();
                if (null == console) {

                    MessageDialog dialog = new MessageDialog(null, "Info", null,
                            "Missing JGrass ConsoleEngine.", MessageDialog.INFORMATION,
                            new String[]{"Ok"}, 0);
                    dialog.setBlockOnOpen(true);
                    dialog.open();
                } else {
                    JavaEditor editor = (JavaEditor) getTextEditor();
                    IDocument doc = editor.getDocumentProvider().getDocument(
                            editor.getEditorInput());
                    editor.getTextConsole().clearConsole();

                    ProjectOptions projectOptions = editor.projectOptions();
                    PreferencesInitializer.initialize(projectOptions);
                    projectOptions.setOption(ProjectOptions.CONSOLE_COMPILE_ONLY,
                            new Boolean(false));

                    String text = null;
                    ISelection selection = editor.getSelectionProvider().getSelection();
                    if (selection instanceof ITextSelection) {
                        ITextSelection textSelection = (ITextSelection) selection;
                        if (!textSelection.isEmpty()) {
                            text = textSelection.getText();
                        }
                    }
                    if (text == null || 0 >= text.length()) {
                        text = doc.get();
                    }

                    if (text != null) {
                        List<String> commandsOnly = new ArrayList<String>();
                        List<String> settingsOnly = new ArrayList<String>();

                        // extract only the commands
                        String[] scriptSplit = text.split("\n"); //$NON-NLS-1$
                        for( String string : scriptSplit ) {
                            if (string.trim().startsWith("#")) {
                                continue;
                            }
                            commandsOnly.add(string);
                        }
                        // from the whole document extract the settings, even id not selected
                        String allText = doc.get();
                        String[] allSplit = allText.split("\n"); //$NON-NLS-1$
                        for( String string : allSplit ) {
                            if (string.trim().startsWith("#")) {
                                settingsOnly.add(string);
                            }
                        }

                        StringBuffer sB = new StringBuffer();
                        for( String string : settingsOnly ) {
                            sB.append(string).append("\n");
                        }
                        sB.append("\n\n");
                        for( String string : commandsOnly ) {
                            sB.append(string).append("\n");
                        }

                        text = sB.toString();
                    }



                    console.dispatch(projectOptions, text);
                }
            }
        });
    } // run

    /** @see org.eclipse.ui.texteditor.TextEditorAction#update() */

    public void update() {

        ITextEditor editor;
        if (null != m_szMsgFmtText && null != (editor = getTextEditor())) {

            Object[] argv = {editor.getTitle()};
            setText(MessageFormat.format(m_szMsgFmtText, argv));
        }
    } // update

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void init( IWorkbenchWindow window ) {
        // TODO Auto-generated method stub

    }

    public void run( IAction action ) {
        run();
    }

    public void selectionChanged( IAction action, ISelection selection ) {
        // TODO Auto-generated method stub

    }

} // ConsoleEditorActionRun
