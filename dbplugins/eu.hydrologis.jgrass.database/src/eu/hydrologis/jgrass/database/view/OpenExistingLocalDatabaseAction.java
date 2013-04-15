package eu.hydrologis.jgrass.database.view;

import i18n.database.Messages;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;

public class OpenExistingLocalDatabaseAction implements IViewActionDelegate {

    private IViewPart view;

    public void init( IViewPart view ) {
        this.view = view;
    }

    public void run( IAction action ) {
        if (view instanceof DatabaseView) {
            DatabaseView dbView = (DatabaseView) view;

            DirectoryDialog fileDialog = new DirectoryDialog(view.getSite().getShell(), SWT.OPEN);
            String path = fileDialog.open();
            if (path == null || path.length() < 1) {
                return;
            }

            File file = new File(path);
            if (!file.exists()) {
                MessageDialog.openError(view.getSite().getShell(), Messages.OpenExistingLocalDatabaseAction__db_error,
                        Messages.OpenExistingLocalDatabaseAction__inserted_folder_not_exist);
                return;
            }

            DatabaseConnectionProperties props = null;
            try {
                props = ConnectionManager.createPropertiesBasedOnFolder(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (props == null) {
                MessageDialog.openError(view.getSite().getShell(), Messages.OpenExistingLocalDatabaseAction__db_error,
                        Messages.OpenExistingLocalDatabaseAction__inserted_folder_not_dbfolder);
                return;
            }

            dbView.createExistingLocalDatabaseDefinition(props);
        }
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
