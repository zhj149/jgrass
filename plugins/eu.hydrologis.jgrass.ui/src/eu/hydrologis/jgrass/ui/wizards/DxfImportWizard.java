/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.ui.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.CRS;
import org.jgrasstools.gears.io.dxfdwg.DxfFeatureReader;
import org.jgrasstools.gears.io.dxfdwg.libs.dxf.DxfFile;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.hydrologis.jgrass.ui.UiPlugin;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class DxfImportWizard extends Wizard implements INewWizard {

    private DxfImportWizardPage mainPage;

    public static boolean canFinish = false;

    private final Map<String, String> params = new HashMap<String, String>();

    public DxfImportWizard() {
        super();
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("Dxf file import");
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(UiPlugin.PLUGIN_ID, "icons/icon_dxf.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
        mainPage = new DxfImportWizardPage("Dxf file import", params); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

    public boolean canFinish() {
        return super.canFinish() && canFinish;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish() {

        final CoordinateReferenceSystem crs = mainPage.getCrs();
        final File inFile = mainPage.getDxfFile();

        /*
         * run with backgroundable progress monitoring
         */
        IRunnableWithProgress operation = new IRunnableWithProgress(){

            public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {

                pm.beginTask("Importing dxf file...", IProgressMonitor.UNKNOWN);
                try {

                    DxfFeatureReader reader = new DxfFeatureReader();
                    reader.file = inFile.getAbsolutePath();
                    String code = "EPSG:4326";
                    try {
                        Integer epsg = CRS.lookupEpsgCode(crs, true);
                        code = "EPSG:" + epsg;
                    } catch (Exception e) {
                        // try non epsg
                        code = CRS.lookupIdentifier(crs, true);
                    }
                    reader.pCode = code;
                    reader.readFeatureCollection();

                    HashMap<String, SimpleFeatureCollection> fMap = new HashMap<String, SimpleFeatureCollection>();
                    fMap.put("points", reader.pointsFC);
                    fMap.put("lines", reader.lineFC);
                    fMap.put("polygons", reader.polygonFC);

                    Set<String> set = fMap.keySet();
                    for( String typeName : set ) {

                        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = fMap.get(typeName);
                        if (featureCollection.size() < 1) {
                            continue;
                        }

                        IGeoResource resource = CatalogPlugin.getDefault().getLocalCatalog()
                                .createTemporaryResource(featureCollection.getSchema());

                        resource.resolve(FeatureStore.class, pm).addFeatures(featureCollection);
                        ApplicationGIS.addLayersToMap(ApplicationGIS.getActiveMap(), Collections.singletonList(resource), -1);

                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    Display.getDefault().syncExec(new Runnable(){
                        public void run() {
                            String message = "An error occurred while importing the dxf file.";
                            ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, UiPlugin.PLUGIN_ID, e);
                        }
                    });
                } finally {
                    pm.done();
                }

            }

        };

        PlatformGIS.runInProgressDialog("Importing data...", true, operation, true);
        System.out.println("Done");

        return true;
    }
}
