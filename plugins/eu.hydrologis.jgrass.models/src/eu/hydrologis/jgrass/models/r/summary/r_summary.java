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
package eu.hydrologis.jgrass.models.r.summary;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.io.File;
import java.io.PrintStream;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IArgument;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.OneInOneOutModelsBackbone;
import eu.hydrologis.openmi.JGrassRasterValueSet;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class r_summary extends OneInOneOutModelsBackbone {

    private JGrassRegion activeRegion;
    private String mapset;

    private double[] minMaxMeans;

    public r_summary() {
        super();
        modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("r_summary.usage");
    }

    public r_summary( PrintStream output, PrintStream error ) {
        super(output, error);
        modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("r_summary.usage");
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        if (properties != null) {
            for( IArgument argument : properties ) {
                String key = argument.getKey();
                if (key.compareTo(ModelsConstants.GRASSDB) == 0) {
                    grassDb = argument.getValue();
                }
                if (key.compareTo(ModelsConstants.LOCATION) == 0) {
                    location = argument.getValue();
                }
                if (key.compareTo(ModelsConstants.MAPSET) == 0) {
                    mapset = argument.getValue();
                }
            }

        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        inputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        outputEI = ModelsConstants.createDummyOutputExchangeItem(this);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (minMaxMeans == null) {

            GridCoverage2D inputGC = ModelsConstants.getGridCoverage2DFromLink(inLink, time, err);
            RandomIter inputIter = RandomIterFactory.create(inputGC.getRenderedImage(), null);
            /*
             * calculate mean, max and min
             */
            double mean = 0.0;

            minMaxMeans = new double[]{Double.MAX_VALUE, Double.MIN_VALUE, 0, 0, 0};
            int cols = activeRegion.getCols();
            int rows = activeRegion.getRows();
            int validCells = 0;
            for( int y = 0; y < rows; y++ ) {
                for( int x = 0; x < cols; x++ ) {
                    double value = inputIter.getSampleDouble(x, y, 0);
                    if (!isNovalue(value)) {
                        if (value < minMaxMeans[0])
                            minMaxMeans[0] = value;
                        if (value > minMaxMeans[1])
                            minMaxMeans[1] = value;
                        mean = mean + value;
                        validCells++;
                    }
                }
            }

            mean = mean / (double) validCells;

            minMaxMeans[2] = mean;
            minMaxMeans[3] = validCells;
            minMaxMeans[4] = validCells * activeRegion.getWEResolution() * activeRegion.getNSResolution();

            /*
             * print out some system out
             */
            out.print("*********************************************\n");
            out.print("summary for the map:\n");
            out.print("\n");
            out.print("range: " + minMaxMeans[0] + " - " + minMaxMeans[1] + "\n");
            out.print("mean: " + minMaxMeans[2] + "\n");
            out.print("active cells: " + minMaxMeans[3] + "\n");
            out.print("active area (assuming metric resolution): " + minMaxMeans[4] + "\n");
            out.print(activeRegion.toString() + "\n");
            out.print("*********************************************\n");

            ScalarSet scalarSet = new ScalarSet(0);
            // scalarSet.add(0, 5.0);
            return scalarSet;
        }
        return null;
    }
}
