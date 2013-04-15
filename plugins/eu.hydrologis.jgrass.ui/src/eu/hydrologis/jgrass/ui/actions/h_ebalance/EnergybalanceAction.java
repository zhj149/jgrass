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
package eu.hydrologis.jgrass.ui.actions.h_ebalance;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * Action to open the energybalance form.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class EnergybalanceAction implements IWorkbenchWindowActionDelegate {

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
    }

    public void run( IAction action ) {

        Display.getDefault().asyncExec(new Runnable(){

            public void run() {

                try {

                    IEditorInput in = new IEditorInput(){
                        public boolean exists() {
                            return false;
                        }
                        public ImageDescriptor getImageDescriptor() {
                            return null;
                        }

                        public String getName() {
                            return "h.energybalance"; //$NON-NLS-1$
                        }

                        public IPersistableElement getPersistable() {
                            return null;
                        }

                        public String getToolTipText() {
                            return "h.energybalance"; //$NON-NLS-1$
                        }

                        public Object getAdapter( Class adapter ) {
                            return null;
                        }
                    };

                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .openEditor(in, EnergybalanceEditor.ID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
