package eu.hydrologis.jgrass.database.view;

import i18n.database.Messages;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

import eu.hydrologis.jgrass.database.DatabasePlugin;

public class OpenInBrowserViewAction implements IViewActionDelegate {


    private IViewPart view;

    public void init( IViewPart view ) {
        this.view = view;
    }

    public void run( IAction action ) {
        try {
           view.getViewSite().getWorkbenchWindow().getActivePage().showView(DatabaseBrowserView.ID);
        } catch (PartInitException e) {
            e.printStackTrace();
            String message = Messages.OpenInBrowserViewAction__errmsg_open_dbview;
            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, DatabasePlugin.PLUGIN_ID, e);
        }

    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
