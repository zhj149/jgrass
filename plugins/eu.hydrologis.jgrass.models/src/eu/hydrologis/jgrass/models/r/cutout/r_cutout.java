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
package eu.hydrologis.jgrass.models.r.cutout;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 * @author Silvia Franceschi (www.hydrologis.com)
 */
public class r_cutout extends ModelsBackbone {

    public static final String maskId = "mask";
    public static final String tocutId = "tocut";
    public static final String cutId = "cut";

    private ILink maskLink = null;
    private ILink tocutLink = null;
    private ILink cutLink = null;

    private IInputExchangeItem maskInputExchangeItem = null;
    private IInputExchangeItem tocutInputExchangeItem = null;
    private IOutputExchangeItem cutOutputExchangeItem = null;
    private JGrassRegion activeRegion;

    private boolean doInvert = false;
    private String valuesString;

    private final static String modelParameters = "...Usage";
    private CoordinateReferenceSystem crs;

    public r_cutout() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public r_cutout( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
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
                if (key.compareTo("values") == 0) {
                    valuesString = argument.getValue();
                }
                if (key.equals("invert")) {
                    doInvert = new Boolean(argument.getValue());
                }
            }

        }

        /*
         * define the map path
         */
        String locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);
        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        maskInputExchangeItem = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        tocutInputExchangeItem = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        cutOutputExchangeItem = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (!linkID.equals(cutId)) {
            throw new IllegalArgumentException(MessageFormat.format("Wrong link of id {0} entered in model. Expected id {1}.",
                    linkID, cutId));
        }
        if (maskLink == null && valuesString == null) {
            throw new IllegalArgumentException("No mask and no values have been defined. One of the two is mandatory.");
        }

        GridCoverage2D tocutRasterData = ModelsConstants.getGridCoverage2DFromLink(tocutLink, time, err);
        RandomIter toCutIter = RandomIterFactory.create(tocutRasterData.getRenderedImage(), null);

        RandomIter maskIter = null;
        boolean doMask = false;
        if (maskLink != null) {
            GridCoverage2D maskRasterData = ModelsConstants.getGridCoverage2DFromLink(maskLink, time, err);
            maskIter = RandomIterFactory.create(maskRasterData.getRenderedImage(), null);
            doMask = true;
        }

        double[] values = null;
        if (valuesString != null) {
            String[] valuesSplit = valuesString.split(",");
            values = new double[valuesSplit.length];
            for( int i = 0; i < valuesSplit.length; i++ ) {
                values[i] = Double.parseDouble(valuesSplit[i]);
            }
            doMask = false;
        }

        int cols = activeRegion.getCols();
        int rows = activeRegion.getRows();
        WritableRaster cutRaster = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, null);
        WritableRandomIter cutIterator = RandomIterFactory.createWritable(cutRaster, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        if (!doInvert) {
            pm.beginTask("Performing cutout of the map...", rows);
            for( int y = 0; y < rows; y++ ) {

                for( int x = 0; x < cols; x++ ) {
                    if (doMask) {
                        double maskValue = maskIter.getSampleDouble(x, y, 0);
                        if (isNovalue(maskValue)) {
                            cutIterator.setSample(x, y, 0, JGrassConstants.doubleNovalue);
                        } else {
                            cutIterator.setSample(x, y, 0, toCutIter.getSampleDouble(x, y, 0));
                        }
                    } else {
                        double rasterValue = toCutIter.getSampleDouble(x, y, 0);
                        for( int k = 0; k < values.length; k++ ) {
                            double tmp = values[k];
                            if (rasterValue == tmp) {
                                cutIterator.setSample(x, y, 0, JGrassConstants.doubleNovalue);
                                break;
                            } else {
                                cutIterator.setSample(x, y, 0, rasterValue);
                            }
                        }
                    }
                }
                pm.worked(1);
            }
        } else {
            pm.beginTask("Performing inverse cutout of the map...", rows);
            for( int y = 0; y < rows; y++ ) {
                for( int x = 0; x < cols; x++ ) {
                    if (doMask) {
                        double maskValue = maskIter.getSampleDouble(x, y, 0);
                        if (isNovalue(maskValue)) {
                            cutIterator.setSample(x, y, 0, toCutIter.getSampleDouble(x, y, 0));
                        } else {
                            cutIterator.setSample(x, y, 0, JGrassConstants.doubleNovalue);
                        }
                    } else {
                        double rasterValue = toCutIter.getSampleDouble(x, y, 0);
                        for( int k = 0; k < values.length; k++ ) {
                            double tmp = values[k];
                            if (rasterValue == tmp) {
                                cutIterator.setSample(x, y, 0, rasterValue);
                                break;
                            } else {
                                cutIterator.setSample(x, y, 0, JGrassConstants.doubleNovalue);
                            }
                        }
                    }
                }
                pm.worked(1);
            }
        }
        pm.done();

        JGrassGridCoverageValueSet cutRasterValueSet = new JGrassGridCoverageValueSet(cutRaster, activeRegion, crs);

        return cutRasterValueSet;
    }

    public void addLink( ILink link ) {
        String linkId = link.getID();

        if (linkId.equals(maskId)) {
            maskLink = link;
        } else if (linkId.equals(tocutId)) {
            tocutLink = link;
        } else if (linkId.equals(cutId)) {
            cutLink = link;
        }

    }

    public void finish() {

    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {

        if (inputExchangeItemIndex == 0) {
            return maskInputExchangeItem;
        } else if (inputExchangeItemIndex == 1) {
            return tocutInputExchangeItem;
        }

        return null;
    }

    public int getInputExchangeItemCount() {

        return 2;
    }

    public String getModelDescription() {

        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return cutOutputExchangeItem;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkId ) {
        if (linkId.equals(maskId)) {
            maskLink = null;
        } else if (linkId.equals(tocutId)) {
            tocutLink = null;
        } else if (linkId.equals(cutId)) {
            cutLink = null;
        }
    }

}
