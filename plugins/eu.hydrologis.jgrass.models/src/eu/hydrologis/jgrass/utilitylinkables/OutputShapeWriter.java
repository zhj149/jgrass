/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) { 
 * HydroloGIS - www.hydrologis.com                                                   
 * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam                               
 * The JGrass developer team - www.jgrass.org                                         
 * }
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
package eu.hydrologis.jgrass.utilitylinkables;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.feature.FeatureCollection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.JGrassModelsPlugin;
import eu.hydrologis.jgrass.libs.utils.features.FeatureUtilities;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.util.HydrologisDate;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * This class allowed to write ShapeFile. Openmi compliant.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @author Erica Ghesla erica.ghesla@ing.unitn.it
 */
public class OutputShapeWriter extends ModelsBackbone {

    private ILink inputLink = null;

    private IInputExchangeItem shapeFileInputEI = null;

    private static final String modelParameters = "...";

    private String filePath = null;

    private CoordinateReferenceSystem locationCrs;

    private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");

    public OutputShapeWriter() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public OutputShapeWriter( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("oshapefile")) { //$NON-NLS-1$
                filePath = argument.getValue();
            }
        }

        /*
         * the input exchange item
         */
        shapeFileInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this, null);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (filePath.length() == 0) {
            return null;
        }
        /*
         * trigger the linked model
         */
        IValueSet valueSet = inputLink.getSourceComponent().getValues(time, inputLink.getID());

        if (valueSet == null) {
            throw new ModelsIllegalargumentException(
                    "Nothing was passed to the Shapefile writer. Check your syntax.", this);
        }
        if (!(valueSet instanceof JGrassFeatureValueSet)) {
            throw new ModelsIllegalargumentException(
                    "The object passed to the Shapefile writer is not a Featurecollection. Unable to write. Please check your syntax.",
                    this);
        }

        JGrassFeatureValueSet jgValueSet = (JGrassFeatureValueSet) valueSet;
        FeatureCollection fcollection = jgValueSet.getFeatureCollection();

        String newfilePath = null;
        if (time instanceof HydrologisDate) {
            HydrologisDate hDate = (HydrologisDate) time;
            String dateString = "_" + hDate.getPrintableFormat();
            newfilePath = filePath + dateString + ".shp";
        } else {
            if (!filePath.endsWith("shp")) {
                newfilePath = filePath + ".shp";
            } else {
                newfilePath = filePath;
            }
        }

        File f = new File(newfilePath);
        out.println("Writing ShapeFile to " + f.getAbsolutePath());
        IMap activeMap = ApplicationGIS.getActiveMap();
        if (JGrassModelsPlugin.getDefault() != null) {
            locationCrs = activeMap.getViewportModel().getCRS();
        }
        if (FeatureUtilities.collectionToShapeFile(f.getAbsolutePath(), locationCrs, fcollection)) {
            out.println("Shapefile successfully written.");

            JGrassCatalogUtilities.addServiceToCatalog(f.getAbsolutePath(),
                    new NullProgressMonitor());
            JGrassCatalogUtilities.addResourceFromUrlToMap(f.toURI().toURL(),
                    new NullProgressMonitor(), activeMap.getMapLayers().size(), activeMap);

        } else {
            err.println("Problems encountered in writing shapefile.");
        }

        return null;
    }

    public void addLink( ILink link ) {
        inputLink = link;
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return shapeFileInputEI;
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 0;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(inputLink.getID()))
            inputLink = null;
    }

}
