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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.grassraster.JGrassConstants;
import org.geotools.gce.grassraster.JGrassMapEnvironment;
import org.geotools.gce.grassraster.JGrassRegion;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.jgrasstools.gears.io.grasslegacy.modules.GrassMosaicLegacy;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.hydrologis.jgrass.ui.utils.EclipseProgressMonitorAdapter;
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * Action that patches grass rasters.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PatchMapsAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate, IWorkbenchWindowPulldownDelegate {

    IStructuredSelection selection = null;

    public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
    }

    public void run( IAction action ) {

        IRunnableWithProgress operation = new IRunnableWithProgress(){
            public void run( final IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                Display.getDefault().syncExec(new Runnable(){
                    public void run() {

                        final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();

                        final List< ? > toList = selection.toList();
                        if (toList.size() < 1) {
                            MessageDialog.openWarning(shell, "Mosaic Warning",
                                    "To use the mosaic command, at least two maps have to be supplied.");
                            return;
                        }

                        InputDialog iDialog = new InputDialog(shell, "Mosaic map name",
                                "Please enter the name for the patched map", "patched", null);
                        iDialog.open();
                        String patchedName = iDialog.getValue();
                        if (patchedName != null && patchedName.length() > 0) {
                            patchedName = patchedName.replaceAll("\\s+", "_");
                        } else {
                            return;
                        }

                        try {
                            List<File> mapsList = new ArrayList<File>();
                            JGrassMapEnvironment mapEnvironment = null;
                            for( Object object : toList ) {
                                if (object instanceof JGrassMapGeoResource) {
                                    JGrassMapGeoResource mr = (JGrassMapGeoResource) object;
                                    File mapFile = mr.getMapFile();
                                    mapsList.add(mapFile);
                                    if (mapEnvironment == null) {
                                        mapEnvironment = mr.getjGrassMapEnvironment();
                                    }

                                }
                            }

                            GrassMosaicLegacy mosaic = new GrassMosaicLegacy();
                            mosaic.inGeodataFiles = mapsList;
                            mosaic.pm = new EclipseProgressMonitorAdapter(pm);
                            File newFile = new File(mapEnvironment.getCellFolder(), patchedName);
                            mosaic.outFile = newFile.getAbsolutePath();
                            JGrassRegion region = mapEnvironment.getActiveRegion();
                            mosaic.pRes = region.getNSResolution();
                            mosaic.process();
                            File mapsetFile = mapEnvironment.getMAPSET();
                            JGrassCatalogUtilities.addMapToCatalog(mapsetFile.getParent(), mapsetFile.getName(), patchedName,
                                    JGrassConstants.GRASSBINARYRASTERMAP);
                        } catch (Exception e) {
                            String message = "An error occurred while exporting the maps.";
                            ExceptionDetailsDialog.openError("ERROR", message, IStatus.ERROR, JGrassPlugin.PLUGIN_ID, e);
                        }

                    }
                });
            }
        };

        PlatformGIS.runInProgressDialog("Patch maps...", true, operation, true);

    }

    /**
    * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
    *      org.eclipse.jface.viewers.ISelection)
    */
    public void selectionChanged( IAction action, ISelection selection ) {

        if (selection instanceof IStructuredSelection)
            this.selection = (IStructuredSelection) selection;
    }

    /*
    * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
    */
    public void dispose() {
    }

    /*
    * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
    */
    public void init( IWorkbenchWindow window ) {
        // do nothing
    }

    /*
    * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
    */
    public Menu getMenu( Control parent ) {
        return null;
    }
    /**
     * Utility method to create read parameters for {@link GridCoverageReader} 
     * 
     * @param width the needed number of columns.
     * @param height the needed number of columns.
     * @param north the northern boundary.
     * @param south the southern boundary.
     * @param east the eastern boundary.
     * @param west the western boundary.
     * @param crs the {@link CoordinateReferenceSystem}. Can be null, even if it should not.
     * @return the {@link GeneralParameterValue array of parameters}.
     */
    public static GeneralParameterValue[] createGridGeometryGeneralParameter( int width, int height, double north, double south,
            double east, double west, CoordinateReferenceSystem crs ) {
        GeneralParameterValue[] readParams = new GeneralParameterValue[1];
        Parameter<GridGeometry2D> readGG = new Parameter<GridGeometry2D>(AbstractGridFormat.READ_GRIDGEOMETRY2D);
        GridEnvelope2D gridEnvelope = new GridEnvelope2D(0, 0, width, height);
        Envelope env;
        if (crs != null) {
            env = new ReferencedEnvelope(west, east, south, north, crs);
        } else {
            DirectPosition2D minDp = new DirectPosition2D(west, south);
            DirectPosition2D maxDp = new DirectPosition2D(east, north);
            env = new Envelope2D(minDp, maxDp);
        }
        readGG.setValue(new GridGeometry2D(gridEnvelope, env));
        readParams[0] = readGG;

        return readParams;
    }

}