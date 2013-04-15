/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
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
package eu.hydrologis.jgrass.database.view;

import i18n.database.Messages;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.jgrasstools.gears.utils.CompressionUtilities;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ExportDatabaseAction implements IViewActionDelegate {

    private IViewPart view;

    public void init( IViewPart view ) {
        this.view = view;
    }

    public void run( IAction action ) {
        if (view instanceof DatabaseView) {
            DatabaseView dbView = (DatabaseView) view;

            DatabaseConnectionProperties properties = dbView.getCurrentSelectedConnectionProperties();
            if (ConnectionManager.isLocal(properties)) {
                FileDialog fileDialog = new FileDialog(view.getSite().getShell(), SWT.SAVE);
                fileDialog.setText(Messages.ExportDatabaseAction__select_zip_file);
                fileDialog.setFileName(properties.getTitle() + ".zip"); //$NON-NLS-1$
                fileDialog.setOverwrite(true);
                final String newDbPath = fileDialog.open();

                if (newDbPath != null && new File(newDbPath).getParentFile().isDirectory()) {
                    final String dbPath = properties.getPath();
                    IRunnableWithProgress operation = new IRunnableWithProgress(){
                        public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                            try {
                                CompressionUtilities.zipFolder(dbPath, newDbPath, true);
                            } catch (IOException e) {
                                e.printStackTrace();
                                String message = Messages.ExportDatabaseAction__errmsg_db_export;
                                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, DatabasePlugin.PLUGIN_ID, e);
                            }
                        }
                    };
                    PlatformGIS.runInProgressDialog(Messages.ExportDatabaseAction__export_db, true, operation, true);
                }
            }
        }
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
