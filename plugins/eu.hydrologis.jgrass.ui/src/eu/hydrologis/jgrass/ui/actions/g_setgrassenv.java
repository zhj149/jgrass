package eu.hydrologis.jgrass.ui.actions;

import java.io.File;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;
import eu.hydrologis.jgrass.grass.GrassPlugin;
import eu.hydrologis.jgrass.uibuilder.jgrassdependent.utils.UIBuilderActionSupporter;

public class g_setgrassenv extends UIBuilderActionSupporter
        implements
            IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;
    private String gisbase;

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
    }

    public void run( IAction action ) {
        final ScopedPreferenceStore m_preferences = (ScopedPreferenceStore) ConsoleEditorPlugin
                .getDefault().getPreferenceStore();
        gisbase = m_preferences.getString(PreferencesInitializer.CONSOLE_ARGV_GISBASE);
        if (gisbase != null) {
            File f = new File(gisbase);
            if (!f.exists()) {
                gisbase = "";
            }
        }

        Dialog dialog = new Dialog(window.getShell()){

            private Text text;

            protected Control createDialogArea( Composite parent ) {
                Composite composite = (Composite) super.createDialogArea(parent);
                composite.setLayout(new GridLayout(3, false));

                Label label = new Label(composite, SWT.NONE);
                label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
                label.setText("grass gisbase path");

                text = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                text.setText(gisbase);

                Button button = new Button(composite, SWT.PUSH);
                button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
                button.setText("...");
                button.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
                    public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                        DirectoryDialog fileDialog = new DirectoryDialog(window.getShell(),
                                SWT.OPEN);
                        String path = fileDialog.open();
                        if (path != null) {
                            File f = new File(path);
                            if (f.exists() && GrassPlugin.getDefault().isGrassAvailable(path)) {
                                text.setText(f.getAbsolutePath());
                            } else {
                                String msg = "This is not a valid GISBASE folder. Please supply a valid GISBASE folder.";
                                popupError(msg);
                            }
                        }
                    }

                });

                return composite;
            }

            protected void configureShell( Shell shell ) {
                super.configureShell(shell);
                shell.setText("g.setgrassenv");
                shell.setSize(new Point(450, 120));
                
                Shell mainshell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                Rectangle bounds = mainshell.getBounds();
                Rectangle rect = shell.getBounds();
                int x = bounds.x + (bounds.width - rect.width) / 2;
                int y = bounds.y + (bounds.height - rect.height) / 2;
                shell.setLocation(x, y);
            }

            protected void okPressed() {
                m_preferences.setValue(PreferencesInitializer.CONSOLE_ARGV_GISBASE, text.getText());
                super.okPressed();
            }

        };

        dialog.setBlockOnOpen(true);
        dialog.open();

    }

    private void popupError( String msg ) {
        MessageBox msgBox = new MessageBox(window.getShell(), SWT.ICON_ERROR);
        msgBox.setMessage(msg);
        msgBox.open();
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }
}
