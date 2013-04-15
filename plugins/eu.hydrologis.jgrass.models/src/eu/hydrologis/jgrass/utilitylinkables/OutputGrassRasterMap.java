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
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.map.JGrassRasterMapWriter;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassRasterValueSet;
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * A linkable data object that is able to take raster data and write them to disk
 * </p>
 * 
 * @author moovida
 */
public class OutputGrassRasterMap extends ModelsBackbone {

    private ILink inputLink = null;

    private IInputExchangeItem rasterMapInputEI = null;
    private JGrassRegion activeRegion;
    private String rasterMapName;

    private static final String modelParameters = "map=0";

    private String mapType = JGrassConstants.GRASSBINARYRASTERMAP;

    private String locationPath;

    private String mapsetName;

    public OutputGrassRasterMap() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public OutputGrassRasterMap( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public void addLink( ILink link ) {
        inputLink = link;
    }

    public void finish() {
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return rasterMapInputEI;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 0;
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String grassDb = null;
        String location = null;
        String mapset = null;
        String unitId = "raster";
        for( IArgument argument : properties ) {
            String key = argument.getKey();

            if (key.equals("ograss")) {
                rasterMapName = argument.getValue();
            }
            if (key.equals("ograssascii")) {
                rasterMapName = argument.getValue();
                mapType = JGrassConstants.GRASSASCIIRASTERMAP;
            }
            if (key.equals("oesrigrid")) {
                rasterMapName = argument.getValue();
                mapType = JGrassConstants.ESRIRASTERMAP;
            }
            if (key.equals("ofluidturtle")) {
                rasterMapName = argument.getValue();
                mapType = JGrassConstants.FTRASTERMAP;
            }
            if (key.compareTo("grassdb") == 0) {
                grassDb = argument.getValue();
            }
            if (key.compareTo("location") == 0) {
                location = argument.getValue();
            }
            if (key.compareTo("mapset") == 0) {
                mapset = argument.getValue();
            }

        }

        componentId = mapType;
        componentDescr = "output_raster_map_writer";

        locationPath = grassDb + File.separator + location;
        mapsetName = mapset;
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        /*
         * create the output exchange item that will be passed over the link to which the component
         * is link to other componentes
         */
        rasterMapInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        /*
         * trigger the linked model
         */
        IValueSet valueSet = inputLink.getSourceComponent().getValues(time, inputLink.getID());

        if (valueSet == null) {
            out.println("ograss warning: ignoring null value passed.");
            return null;
        }

        RasterData rasterData = ((JGrassRasterValueSet) valueSet).getJGrassRasterData();

        /*
         * write the data
         */
        // if DATE has been inserted, replace that with the timestamp
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
        String dateStr = time instanceof Date ? dateFormatter.format((Date) time) : dateFormatter
                .format(new Date());
        String newMapName = rasterMapName.replaceAll("DATE", dateStr);
        JGrassRasterMapWriter jgrassMapWriter = new JGrassRasterMapWriter(activeRegion, newMapName,
                mapsetName, locationPath, mapType, new PrintStreamProgressMonitor(out));
        String mapName = new File(jgrassMapWriter.getFullMapPath()).getName();
        out.println("Writing map: " + mapName); //$NON-NLS-1$
        if (!jgrassMapWriter.open())
            return null;
        if (!jgrassMapWriter.write(rasterData)) {
            return null;
        }
        jgrassMapWriter.close();
        if (JGrassPlugin.getDefault() != null) {
            JGrassMapGeoResource addedMap = JGrassCatalogUtilities.addMapToCatalog(locationPath,
                    mapsetName, mapName, mapType);
            if (addedMap == null)
                return null;

            IMap activeMap = ApplicationGIS.getActiveMap();
            ApplicationGIS.addLayersToMap(activeMap, Collections
                    .singletonList((IGeoResource) addedMap), activeMap.getMapLayers().size());
        }
        return null;

    }
    public void removeLink( String linkID ) {
        inputLink = null;
    }

}
