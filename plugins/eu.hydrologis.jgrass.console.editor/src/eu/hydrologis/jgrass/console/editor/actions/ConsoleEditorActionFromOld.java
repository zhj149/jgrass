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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.examples.javaeditor.JavaEditor;

/**
 * Action to open an editor
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class ConsoleEditorActionFromOld implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
    }

    public void run( IAction action ) {

        Display.getDefault().asyncExec(new Runnable(){

            public void run() {

                try {

                    FileDialog fileDialog = new FileDialog(window.getShell(), SWT.OPEN);
                    String path = fileDialog.open();

                    if (path == null || path.length() < 1) {
                        return;
                    }

                    File f = new File(path);
                    if (!f.exists()) {
                        return;
                    }

                    JavaFileEditorInput jFile = new JavaFileEditorInput(f);
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .openEditor(jFile, JavaEditor.ID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
