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
package eu.hydrologis.jgrass.ui.popups;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.geotools.gce.grassraster.JGrassRegion;
import org.jgrasstools.gears.utils.CrsUtilities;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.activeregion.ActiveRegionStyle;
import eu.udig.catalog.jgrass.activeregion.ActiveregionStyleContent;
import eu.udig.catalog.jgrass.core.JGrassMapsetGeoResource;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * Action to set the current active mapset.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SetActiveMapsetAction
        implements
            IObjectActionDelegate,
            IWorkbenchWindowActionDelegate,
            IWorkbenchWindowPulldownDelegate {

    IStructuredSelection selection = null;

    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }

    public void run( IAction action ) {
        if (selection == null)
            return;
        Object first = selection.getFirstElement();
        if (first instanceof JGrassMapsetGeoResource) {
            JGrassMapsetGeoResource mapsetr = (JGrassMapsetGeoResource) first;
            String mapsetPath = mapsetr.getFile().getAbsolutePath();

            final ScopedPreferenceStore m_preferences = (ScopedPreferenceStore) ConsoleEditorPlugin.getDefault()
                    .getPreferenceStore();
            m_preferences.setValue(PreferencesInitializer.CONSOLE_ARGV_MAPSET, mapsetPath);

            // update the active region
            JGrassRegion activeRegion = mapsetr.getActiveRegionWindow();
            IMap activeMap = ApplicationGIS.getActiveMap();
            IBlackboard blackboard = activeMap.getBlackboard();
            ActiveRegionStyle style = (ActiveRegionStyle) blackboard.get(ActiveregionStyleContent.ID);
            if (style == null) {
                style = ActiveregionStyleContent.createDefault();
            }
            style.north = (float) activeRegion.getNorth();
            style.south = (float) activeRegion.getSouth();
            style.east = (float) activeRegion.getEast();
            style.west = (float) activeRegion.getWest();
            style.rows = activeRegion.getRows();
            style.cols = activeRegion.getCols();
            style.windPath = mapsetr.getActiveRegionWindowPath();
            try {
                String code = CrsUtilities.getCodeFromCrs(mapsetr.getJGrassCrs());
                style.crsString = code;
            } catch (Exception e) {
                e.printStackTrace();
            }

            blackboard.put(ActiveregionStyleContent.ID, style);

            ILayer activeRegionMapGraphic = JGrassPlugin.getDefault().getActiveRegionMapGraphic();
            activeRegionMapGraphic.refresh(null);

        }
    }

    /**
    * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
    *      org.eclipse.jface.viewers.ISelection)
    */
    public void selectionChanged( IAction action, ISelection selection ) {
        if (selection instanceof IStructuredSelection)
            this.selection = (IStructuredSelection) selection;
    }

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
    }

    public Menu getMenu( Control parent ) {
        return null;
    }

}