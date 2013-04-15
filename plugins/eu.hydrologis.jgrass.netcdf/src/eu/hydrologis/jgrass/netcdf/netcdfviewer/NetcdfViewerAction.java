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
  package eu.hydrologis.jgrass.netcdf.netcdfviewer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import eu.hydrologis.jgrass.netcdf.NetcdfPlugin;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class NetcdfViewerAction extends Action implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window = null;

    public void dispose() {

    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
    }

    public void run( IAction action ) {
        IWorkbenchPage activePage = window.getActivePage();
        IViewPart netcdfView = activePage.findView(NetcdfView.ID);
        if (netcdfView != null) {
            
            try {
                
                activePage.showView(NetcdfView.ID);
                activePage.toggleZoom(activePage.findViewReference(NetcdfView.ID));

            } catch (PartInitException es) {
                NetcdfPlugin.log("NetcdfPlugin problem", es);  //$NON-NLS-1$
                es.printStackTrace();
            }
        }
    }
    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
