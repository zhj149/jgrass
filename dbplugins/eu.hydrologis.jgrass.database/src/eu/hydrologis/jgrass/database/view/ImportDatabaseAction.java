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
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
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
public class ImportDatabaseAction implements IViewActionDelegate {

    private IViewPart view;

    public void init( IViewPart view ) {
        this.view = view;
    }

    public void run( IAction action ) {
        if (view instanceof DatabaseView) {
            final DatabaseView dbView = (DatabaseView) view;

            FileDialog fileDialog = new FileDialog(view.getSite().getShell(), SWT.OPEN);
            fileDialog.setText(Messages.ImportDatabaseAction__select_zip);
            final String dbImportPath = fileDialog.open();

            DirectoryDialog folderDialog = new DirectoryDialog(view.getSite().getShell(), SWT.OPEN);
            folderDialog.setText(Messages.ImportDatabaseAction__select_folder);
            final String dbParentFolderPath = folderDialog.open();

            if (dbParentFolderPath != null && new File(dbParentFolderPath).isDirectory()) {
                IRunnableWithProgress operation = new IRunnableWithProgress(){
                    public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                        try {
                            String internalFolderName = null;
                            ZipFile zf = new ZipFile(dbImportPath);
                            Enumeration< ? extends ZipEntry> e = zf.entries();
                            if (e.hasMoreElements()) {
                                ZipEntry ze = (ZipEntry) e.nextElement();
                                internalFolderName = ze.getName();
                                int sep = internalFolderName.indexOf(File.separatorChar);
                                internalFolderName = internalFolderName.substring(0, sep);
                            }
                            zf.close();

                            CompressionUtilities.unzipFolder(dbImportPath, dbParentFolderPath);
                            String dbName = internalFolderName; // new File(dbImportPath).getName();
                            File databaseFile = new File(dbParentFolderPath, dbName);
                            if (databaseFile.exists()) {
                                DatabaseConnectionProperties connectionProperties = ConnectionManager
                                        .createPropertiesBasedOnFolder(databaseFile);
                                String name = new File(dbImportPath).getName().replaceFirst("\\.zip$", ""); //$NON-NLS-1$ //$NON-NLS-2$
                                connectionProperties.put(DatabaseConnectionProperties.TITLE, name);
                                List<DatabaseConnectionProperties> databaseConnectionProperties = DatabasePlugin.getDefault()
                                        .getAvailableDatabaseConnectionProperties();
                                databaseConnectionProperties.add(connectionProperties);
                                dbView.relayout();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            String message = Messages.ImportDatabaseAction__errmsg_db_import;
                            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, DatabasePlugin.PLUGIN_ID, e);
                        }
                    }
                };
                PlatformGIS.runInProgressDialog(Messages.ImportDatabaseAction__import_db, true, operation, true);
            }
        }
    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
