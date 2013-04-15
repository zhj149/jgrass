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
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.jgrasstools.gears.io.dxfdwg.libs.DwgHandler;
import org.jgrasstools.gears.io.dxfdwg.libs.DwgReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.hydrologis.jgrass.ui.UiPlugin;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class DwgImportWizard extends Wizard implements INewWizard {

    private DwgImportWizardPage mainPage;

    public static boolean canFinish = false;

    private final Map<String, String> params = new HashMap<String, String>();

    public DwgImportWizard() {
        super();
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
        setWindowTitle("Dwg file import");
        setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(UiPlugin.PLUGIN_ID, "icons/icon_dwg.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
        mainPage = new DwgImportWizardPage("Dwg file import", params); //$NON-NLS-1$
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
        final File dwgFile = mainPage.getDwgFile();

        /*
         * run with backgroundable progress monitoring
         */
        IRunnableWithProgress operation = new IRunnableWithProgress(){

            public void run( IProgressMonitor pm ) throws InvocationTargetException, InterruptedException {
                HashMap<String, FeatureCollection<SimpleFeatureType, SimpleFeature>> map = null;
                try {
                    DwgHandler dataHandler = new DwgHandler(dwgFile, crs);
                    dataHandler.getLayerTypes();
                    DwgReader dwgReader = dataHandler.getDwgReader();
                    map = dwgReader.getFeatureCollectionsMap();
                } catch (Exception e1) {
                    String message = "An error occurred while opening the dwg file";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, UiPlugin.PLUGIN_ID, e1);

                    e1.printStackTrace();
                    return;
                }

                Set<String> keySet = map.keySet();

                try {
                    for( String typeName : keySet ) {

                        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = map.get(typeName);
                        if (featureCollection.size() < 1) {
                            continue;
                        }

                        SimpleFeatureType featureType = featureCollection.getSchema();
                        IGeoResource resource = CatalogPlugin.getDefault().getLocalCatalog().createTemporaryResource(featureType);

                        resource.resolve(FeatureStore.class, pm).addFeatures(featureCollection);
                        ApplicationGIS.addLayersToMap(ApplicationGIS.getActiveMap(), Collections.singletonList(resource), -1);

                    }
                } catch (Exception e) {
                    String message = "An error occurred while importing the DWG file.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, UiPlugin.PLUGIN_ID, e);
                }

            }

        };

        PlatformGIS.runInProgressDialog("Importing data...", true, operation, true);

        return true;
    }
}
