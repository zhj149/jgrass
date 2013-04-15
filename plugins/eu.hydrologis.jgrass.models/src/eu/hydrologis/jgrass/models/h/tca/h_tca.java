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
package eu.hydrologis.jgrass.models.h.tca;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;

import javax.media.jai.PlanarImage;
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
import eu.hydrologis.libs.openmi.ModelsIOException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the tca model. The upslope catchment (or simply
 * contributing) areas represent the planar projection of the areas afferent to a point in the
 * basin. Once the drainage directions have been defined, it is possible to calculate, for each
 * site, the total drainage area afferent to it, indicated as TCA (Total Contributing Area).
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of elevations (-pit);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of total contributing area (-tca);</LI>
 * </OL>
 * <P></DD> Usage: h.tca --igrass-flow flow --ograss-tca tca
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini Andrea, Franceschi
 *         Silvia, Pisoni Silvano, Rigon Riccardo
 */
public class h_tca extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String tcaID = "tca";

    public final static String flowID = "flow";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_tca.usage");

    private ILink tcaLink = null;

    private ILink flowLink = null;

    private IOutputExchangeItem tcaDataOutputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile;

    private double xRes;

    private double yRes;

    private String locationPath;

    private WritableRandomIter tcaRandomIter;

    private RandomIter fileRandomIter;

    /** */
    public h_tca() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_tca

    /** */
    public h_tca( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_tca

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(tcaID)) {
            tcaLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {
        tcaRandomIter = null;

        fileRandomIter = null;
    }

    /**
     * There is an IInputExchangeItem: flow
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 1;
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: tca
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return tcaDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(tcaLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowData = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            PlanarImage flowImage = (PlanarImage) flowData.getRenderedImage();
            xRes = activeRegion.getWEResolution();
            yRes = activeRegion.getNSResolution();
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

            WritableRaster tcaImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null,
                    flowImage.getSampleModel(), null);
            tcaRandomIter = RandomIterFactory.createWritable(tcaImage, null);
            fileRandomIter = RandomIterFactory.create(flowImage, null);

            if (!area()) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(tcaImage, activeRegion, crs);
                return jgrValueSet;
            }
        }
        return null;
    }

    /**
     * in this method map's properties are defined... location, mapset... and than
     * IInputExchangeItem and IOutputExchangeItem are reated
     */
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
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.getBoolean(argument.getValue());
                }
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.tca";
        componentId = null;

        /*
         * create the exchange items
         */
        // tca output

        tcaDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(tcaLink.getID())) {
            tcaLink = null;
        }
    }

    /**
     * Set the tca raster map.
     * <p>
     * It loop on the rows and on the columns. In each iteration it start from the pixel (which
     * correspond to the indexes) and move downstream, along the drainage direction, and add the
     * value of the tca previous pixel value to the next. If the value of flow is 10 then it is the
     * outlet of the basin.
     * </p>
     * 
     * @return true if the tca matrix is filled, otherwise return false.
     * @throws ModelsIOException
     */
    private boolean area() throws ModelsIOException {

        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Working on tca...", rows);
        // the drainage directions
        int[][] dirs = ModelsConstants.DIR;
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                // get the girections of the current pixel.
                int flowValue = (int) fileRandomIter.getSampleDouble(j, i, 0);
                /*
                 * I have put flowValue == 0 because the cast to an int value of a NaN is 0, and 0
                 * is an invalid value for the drainage direction.
                 */
                if (isNovalue(flowValue) || flowValue == 0) {
                    tcaRandomIter.setSample(j, i, 0, JGrassConstants.doubleNovalue);
                } else {
                    int rRow = i;
                    int rCol = j;
                    double tcaValue = tcaRandomIter.getSampleDouble(rCol, rRow, 0);
                    while( flowValue < 9 && !isNovalue(flowValue) && flowValue != 0 ) {
                        // it update the value of tca in the next pixel.
                        tcaRandomIter.setSample(rCol, rRow, 0, tcaValue + 1);
                        // it move to the next pixel.
                        int newRow = rRow + dirs[flowValue][0];
                        int newCol = rCol + dirs[flowValue][1];
                        // get the new value of drainage direction.
                        int nextFlow = (int) fileRandomIter.getSampleDouble(newCol, newRow, 0);
                        /*
                         * verify that the next pixel doesn't drain in the previous, otherwise there
                         * is an infinite loop.
                         */
                        if (nextFlow < 9 && (!isNovalue(nextFlow) || nextFlow != 0)) {
                            int r = newRow + dirs[nextFlow][0];
                            int c = newCol + dirs[nextFlow][1];
                            if (r == rRow && c == rCol) {
                                throw new ModelsIOException(MessageFormat.format(
                                        "Detected loop between rows/cols = {0}/{1} and {2}/{3}", rRow, rCol, newRow, newCol),
                                        this);
                            }
                        }
                        // update the indexes.
                        rRow = newRow;
                        rCol = newCol;
                        // memorize the value of the pixel in order to add it to the new pixel.
                        tcaValue = tcaRandomIter.getSampleDouble(rCol, rRow, 0);
                        // extract the new value of drainage direction.
                        flowValue = (int) fileRandomIter.getSampleDouble(rCol, rRow, 0);
                    }
                    if (flowValue == 10) {
                        tcaRandomIter.setSample(rCol, rRow, 0, tcaValue + 1);
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        return true;
    }
}
