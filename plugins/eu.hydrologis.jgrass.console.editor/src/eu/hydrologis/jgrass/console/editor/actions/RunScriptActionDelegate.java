package eu.hydrologis.jgrass.console.editor.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.examples.javaeditor.JavaEditor;

import eu.hydrologis.jgrass.console.ConsolePlugin;
import eu.hydrologis.jgrass.console.core.JGrass;
import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;

public class RunScriptActionDelegate implements IEditorActionDelegate {

    private JavaEditor javaEditor;

    public void setActiveEditor( IAction action, IEditorPart targetEditor ) {
        if (targetEditor instanceof JavaEditor) {
            javaEditor = (JavaEditor) targetEditor;
        }
    }

    public void run( IAction action ) {

        if (javaEditor == null) {
            return;
        }

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
                    IDocument doc = javaEditor.getDocumentProvider().getDocument(
                            javaEditor.getEditorInput());
                    javaEditor.getTextConsole().clearConsole();

                    ProjectOptions projectOptions = javaEditor.projectOptions();
                    PreferencesInitializer.initialize(projectOptions);
                    projectOptions.setOption(ProjectOptions.CONSOLE_COMPILE_ONLY,
                            new Boolean(false));

                    String text = null;
                    ISelection selection = javaEditor.getSelectionProvider().getSelection();
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

    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
