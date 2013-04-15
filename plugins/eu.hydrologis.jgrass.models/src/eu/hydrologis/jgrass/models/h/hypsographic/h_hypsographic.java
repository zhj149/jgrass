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
package eu.hydrologis.jgrass.models.h.hypsographic;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * @author Silvia Franceschi (www.hydrologis.com)
 */
public class h_hypsographic extends ModelsBackbone {

    private ILink elevationLink = null;
    private ILink hypsographicLink = null;

    private IInputExchangeItem elevationInputExchangeItem = null;
    private IOutputExchangeItem hypsographicOutputExchangeItem = null;

    public static final String elevationId = "elevation";
    public static final String hypsographicId = "hypsographic";

    private int binsNumber = 100;
    private JGrassRegion activeRegion;

    private final static String modelParameters = "...Usage";

    public h_hypsographic() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String grassDb = null;
        String location = null;
        String mapset = null;
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
                if (key.equals("bins")) {
                    binsNumber = new Integer(argument.getValue());
                }
            }

        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        /*
         * define the I/O Exchange Items
         */
        elevationInputExchangeItem = ModelsConstants.createRasterInputExchangeItem(this,
                activeRegion);
        hypsographicOutputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (!linkID.equals(hypsographicId)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Wrong link of id {0} entered in model. Expected id {1}.", linkID,
                    hypsographicId));
        }

        /*
         * get the input raster
         */
        
        GridCoverage2D elevationRasterData =ModelsConstants.getGridCoverage2DFromLink(elevationLink, time, err);
        RenderedImage elevImage = elevationRasterData.getRenderedImage();
        RandomIter elevIterator = RandomIterFactory.create(elevImage, null);
        /*
         * calculate the maximum and minimum value of the raster data
         */

        int rows = elevImage.getHeight();
        int cols = elevImage.getWidth();
        
        double maxRasterValue = 0.0;
        double minRasterValue = 0.0;
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        
        pm.beginTask("Calculating extrema...", rows);
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                double value = elevIterator.getSampleDouble(j, i, 0);
                if (isNovalue(value)) {
                    continue;
                }
                if (value > maxRasterValue) {
                    maxRasterValue = value;
                }
                if (value < minRasterValue) {
                    minRasterValue = value;
                }
            }
            pm.worked(1);
        }
        pm.done();

        /*
         * subdivide the whole value range in bins and count the number of pixels in each bin
         */
        double binWidth = (maxRasterValue - minRasterValue) / (binsNumber);
        double[] pixelPerBinCount = new double[binsNumber];
        double[] areaAtGreaterElevation = new double[binsNumber];

        pm.beginTask("Performing calculation of hypsographic curve...", rows);
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                double value = elevIterator.getSampleDouble(j, i, 0);
                if (isNovalue(value)) {
                    continue;
                }
                for( int k = 0; k < pixelPerBinCount.length; k++ ) {
                    if (value > (minRasterValue + (k) * binWidth)
                    // && elevationRasterData.getValueAt(i, j) < (minRasterValue + (k + 1)
                    // * binWidth)
                    ) {
                        pixelPerBinCount[k] = pixelPerBinCount[k] + 1;
                        areaAtGreaterElevation[k] = areaAtGreaterElevation[k]
                                + (activeRegion.getNSResolution() * activeRegion.getWEResolution());
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        
        ScalarSet hypsographicScalarSet = new ScalarSet();

        hypsographicScalarSet.add(2.0);
        for( int i = 0; i < pixelPerBinCount.length; i++ ) {
            hypsographicScalarSet.add(minRasterValue + (i * binWidth) + (binWidth / 2.0));
            hypsographicScalarSet.add(areaAtGreaterElevation[i] / 1000000.0);
        }

        return hypsographicScalarSet;
    }

    public h_hypsographic( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void addLink( ILink link ) {
        String linkId = link.getID();

        if (linkId.equals(elevationId)) {
            elevationLink = link;
        } else if (linkId.equals(hypsographicId)) {
            hypsographicLink = link;
        }
    }

    public void finish() {

    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {

        if (inputExchangeItemIndex == 0) {
            return elevationInputExchangeItem;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return hypsographicOutputExchangeItem;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkId ) {
        if (linkId.equals(elevationId)) {
            elevationLink = null;
        } else if (linkId.equals(hypsographicId)) {
            hypsographicLink = null;
        }
    }

}
