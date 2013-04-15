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
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.examples.javaeditor.JavaEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import eu.hydrologis.jgrass.console.ConsolePlugin;
import eu.hydrologis.jgrass.console.core.JGrass;
import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;

public class ConsoleEditorActionCompile extends TextEditorAction {

// Attributes
	/** */
	private String m_szMsgFmtText = null;

// Construction
    public ConsoleEditorActionCompile( ResourceBundle bundle, String prefix, ITextEditor editor ) {

        super(bundle, prefix, editor);
        m_szMsgFmtText = getText();
    } // ConsoleEditorActionCompile

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

                    // JavaEditor editor = (JavaEditor) getTextEditor();
                    // ProjectOptions projectOptions = editor.projectOptions();
                    // PreferencesInitializer.initialize(projectOptions);
                    // projectOptions
                    // .setOption(ProjectOptions.CONSOLE_COMPILE_ONLY, new Boolean(true));
                    // IDocument doc = editor.getDocumentProvider().getDocument(
                    // editor.getEditorInput());
                    // editor.getTextConsole().clearConsole();
                    // console.dispatch(projectOptions, doc.get());
                    //                    
                    String text = null;
                    JavaEditor editor = (JavaEditor) getTextEditor();
                    ISelection selection = editor.getSelectionProvider().getSelection();
                    if (selection instanceof ITextSelection) {
                        ITextSelection textSelection = (ITextSelection) selection;
                        if (!textSelection.isEmpty()) {
                            text = textSelection.getText();
                        }
                    }

                    ProjectOptions projectOptions = editor.projectOptions();
                    PreferencesInitializer.initialize(projectOptions);

                    // FIXME check how GRASS preferences are saved in the preferencespage
                    // Object option = projectOptions.getOption(ProjectOptions.COMMON_GRASS_MAPSET);

                    projectOptions.setOption(ProjectOptions.CONSOLE_COMPILE_ONLY,
                            new Boolean(true));
                    IDocument doc = editor.getDocumentProvider().getDocument(
                            editor.getEditorInput());
                    editor.getTextConsole().clearConsole();
                    if (text == null || 0 >= text.length()) {
                        text = doc.get();
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

} // ConsoleEditorActionCompile
