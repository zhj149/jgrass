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
package eu.hydrologis.jgrass.models.h.wateroutlet;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;

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
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the wateroutlet model. Generates a watershed basin from a
 * drainage direction map and a set of coordinates representing the outlet point of watershed.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the map of the drainage directions (-flow)</LI>
 * <LI>the coordinates of the water outlet (-north, -east)</LI>
 * </OL>
 * </DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>the basin extracted mask (-basin)</LI>
 * <LI>a choosen map cutten on the basin mask (the name assigned is input.mask) (-trim)</LI>
 * </OL>
 * <P>
 * </DD>
 * Usage: h.wateroutlet --igrass-map map --igrass-flow flow --ograss-basin basin --ograss-trim trim
 * --north north --east east
 * </p>
 * <p>
 * Note: The most important thing in this module is to choose a good water outlet.
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini Andrea, Franceschi
 *         Silvia, Pisoni Silvano, Rigon Riccardo; Originally by Charles Ehlschlaeger, U.S. Army
 *         Construction Engineering Research Laboratory.
 */
public class h_wateroutlet extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String mapID = "map";

    public final static String flowID = "flow";

    public final static String basinID = "basin";

    public final static String trimID = "trim";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_wateroutlet.usage");

    private ILink mapLink = null;

    private ILink flowLink = null;

    private ILink basinLink = null;

    private ILink trimLink = null;

    private IOutputExchangeItem basinDataOutputEI = null;

    private IOutputExchangeItem trimDataOutputEI = null;

    private IInputExchangeItem mapDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private int nrows;

    private int ncols;

    private int total;

    private int[] pt_seg = new int[1];

    private int[] ba_seg = new int[1];

    private final int RAMSEGBITS = 4;

    private final int DOUBLEBITS = 8; /* 2 * ramsegbits */

    private final int SEGLENLESS = 15; /* 2 ^ ramsegbits - 1 */

    private static final int FLOW_NO_VALUE = (int) FluidConstants.flownovalue;

    // /private double[][] flowData = null;

    private double[] drain_ptrs = null;

    private double[] bas_ptrs = null;

    private double north = -1.0;

    private double east = -1.0;
    private String locationPath;
    private WritableRaster flowImage;
    private PlanarImage channelImage;
    private WritableRandomIter flowRandomIter;
    private RandomIter channelFileRandomIter;
    WritableRaster maskImage;
    WritableRaster maskMapImage;
    private Rectangle imageBounds;
    private JGrassGridCoverageValueSet jgrValueSet;

    private boolean doTile = false;

    private double xRes;

    private double yRes;

    private RenderedImage planImage;

    private WritableRandomIter maskRandomIter;

    private WritableRandomIter maskMapRandomIter;

    private CoordinateReferenceSystem crs;

    /** */
    public h_wateroutlet() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_rescaleddistance

    /** */
    public h_wateroutlet( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_rescaleddistance

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(mapID)) {
            mapLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(basinID)) {
            basinLink = link;
        }
        if (id.equals(trimID)) {
            trimLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: map, flow
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return mapDataInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return flowDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 2;
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: basin, trim
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return basinDataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return trimDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 2;
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (north == -1 || east == -1) {
            out.println(getModelDescription());
            return null;
        }
        if (channelImage == null) {
            xRes = activeRegion.getWEResolution();
            yRes = activeRegion.getNSResolution();

            GridCoverage2D channelGC = ModelsConstants.getGridCoverage2DFromLink(mapLink, time, err);
            channelImage = (PlanarImage) channelGC.getRenderedImage();
            channelFileRandomIter = RandomIterFactory.create(channelImage, null);

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            PlanarImage flowTmpImage = (PlanarImage) flowGC.getRenderedImage();
            flowImage = FluidUtils.createFromRenderedImage(flowTmpImage);
            flowRandomIter = RandomIterFactory.createWritable(flowImage, null);

            maskImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null, flowImage
                    .getSampleModel(), null);
            maskRandomIter = RandomIterFactory.createWritable(maskImage, null);
            maskMapImage = FluidUtils.createDoubleWritableRaster(flowImage.getWidth(), flowImage.getHeight(), null, channelImage
                    .getSampleModel(), null);
            maskMapRandomIter = RandomIterFactory.createWritable(maskMapImage, null);

            if (!wateroutlet()) {
                return null;
            }
        }

        if (linkID.equals(basinLink.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(maskImage, activeRegion, crs);
            return jgrValueSet;
        } else if (linkID.equals(trimLink.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(maskMapImage, activeRegion, crs);
            return jgrValueSet;
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
                if (key.compareTo("north") == 0) {
                    north = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("east") == 0) {
                    east = Double.parseDouble(argument.getValue());
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

        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        componentDescr = "h.wateroutlet";
        componentId = null;

        /*
         * create the exchange items
         */
        // basin output

        basinDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // trim output

        trimDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // map input

        mapDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(mapLink.getID())) {
            mapLink = null;
        }
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(basinLink.getID())) {
            basinLink = null;
        }
        if (linkID.equals(trimLink.getID())) {
            trimLink = null;
        }
    }

    /**
     * wateroutlet returns the basin
     */
    private boolean wateroutlet() {

        nrows = activeRegion.getRows();
        ncols = activeRegion.getCols();
        total = nrows * ncols;

        drain_ptrs = new double[size_array(pt_seg, nrows, ncols)];
        // bas = (CELL *) G_calloc (size_array (&ba_seg, nrows, ncols),
        // sizeof(CELL));
        bas_ptrs = new double[size_array(ba_seg, nrows, ncols)];

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.watreoutlet...", nrows + nrows + maskMapImage.getHeight());
        for( int r = 0; r < nrows; r++ ) {
            for( int c = 0; c < ncols; c++ ) {
                // adapt to the grass drainagedirection format "grass
                // flow=(fluidturtle flow-1)"
                if (isNovalue(flowRandomIter.getSampleDouble(c, r, 0)) || flowRandomIter.getSampleDouble(c, r, 0) == 0) {
                    flowRandomIter.setSample(c, r, 0, -1.0);
                } else if (flowRandomIter.getSampleDouble(c, r, 0) == 1.0) {
                    flowRandomIter.setSample(c, r, 0, 8.0);
                } else if (!isNovalue(flowRandomIter.getSampleDouble(c, r, 0))) {
                    flowRandomIter.setSample(c, r, 0, flowRandomIter.getSample(c, r, 0) - 1);

                }
                if (flowRandomIter.getSampleDouble(c, r, 0) == 0.0) {
                    total--;
                }
                drain_ptrs[seg_index(pt_seg, r, c)] = flowRandomIter.getSample(c, r, 0);
                // out.println("DRAIN_PTRS = " +
                // drain_ptrs[seg_index(pt_seg, r, c)]);

            }
            pm.worked(1);
        }

        int row = (int) ((activeRegion.getNorth() - north) / activeRegion.getNSResolution());
        int col = (int) ((east - activeRegion.getWest()) / activeRegion.getWEResolution());

        if (row >= 0 && col >= 0 && row < nrows && col < ncols)
            overland_cells(row, col);

        for( int r = 0; r < nrows; r++ ) {
            for( int c = 0; c < ncols; c++ ) {
                maskRandomIter.setSample(c, r, 0, bas_ptrs[seg_index(ba_seg, r, c)]);
                if (isNovalue(flowRandomIter.getSampleDouble(c, r, 0)) || maskRandomIter.getSampleDouble(c, r, 0) == 0.0) {
                    maskRandomIter.setSample(c, r, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }

        // extract
        for( int i = 0; i < maskMapImage.getHeight(); i++ ) {
            for( int j = 0; j < maskMapImage.getWidth(); j++ ) {
                if (isNovalue(maskRandomIter.getSampleDouble(j, i, 0))) {
                    maskMapRandomIter.setSample(j, i, 0, JGrassConstants.doubleNovalue);
                } else {
                    maskMapRandomIter.setSample(j, i, 0, channelFileRandomIter.getSampleDouble(j, i, 0));
                }
            }
            pm.worked(1);
        }
        pm.done();
        return true;
    }

    private int size_array( int[] ram_seg, int nrows, int ncols ) {
        int size, segs_in_col;

        segs_in_col = ((nrows - 1) >> RAMSEGBITS) + 1;
        ram_seg[0] = ((ncols - 1) >> RAMSEGBITS) + 1;
        size = ((((nrows - 1) >> RAMSEGBITS) + 1) << RAMSEGBITS) * ((((ncols - 1) >> RAMSEGBITS) + 1) << RAMSEGBITS);
        size -= ((segs_in_col << RAMSEGBITS) - nrows) << RAMSEGBITS;
        size -= (ram_seg[0] << RAMSEGBITS) - ncols;
        return (size);
    }

    private int seg_index( int[] s, int r, int c ) {
        int value = ((((r) >> RAMSEGBITS) * (s[0]) + (((c) >> RAMSEGBITS)) << DOUBLEBITS) + (((r) & SEGLENLESS) << RAMSEGBITS) + ((c) & SEGLENLESS));

        return value;
    }

    private void overland_cells( int row, int col ) {
        int r, rr, c, cc, num_cells, size_more;
        double value;
        double[][] draindir = {{7, 6, 5}, {8, -17, 4}, {1, 2, 3}};

        if (nrows > ncols) {
            size_more = nrows;
        } else {
            size_more = ncols;
        }

        // OneCell[] Acells = new OneCell[size_more];
        int[] AcellsR = new int[nrows * ncols];
        int[] AcellsC = new int[nrows * ncols];
        // OneCell Acells = new OneCell(nrows * ncols);

        // Acells = (ONE_CELL *) G_malloc (size_more * sizeof(ONE_CELL));
        num_cells = 1;
        AcellsR[0] = row;
        AcellsC[0] = col;
        while( num_cells != 0 ) {
            num_cells--;
            // out.println(" num_cell = " + num_cells);
            row = AcellsR[num_cells];
            col = AcellsC[num_cells];
            bas_ptrs[seg_index(ba_seg, row, col)] = 1.0;
            for( r = row - 1, rr = 0; r <= row + 1; r++, rr++ ) {
                for( c = col - 1, cc = 0; c <= col + 1; c++, cc++ ) {
                    if (r >= 0 && c >= 0 && r < nrows && c < ncols) {
                        value = drain_ptrs[seg_index(pt_seg, r, c)];

                        /*
                         * out.println("value == drain -> " + value + " == " + draindir[rr][cc] + " &&
                         * bas_ptrs == 0.0 -> " + bas_ptrs[seg_index(ba_seg, r, c)] + " == 0.0");
                         */

                        if ((value == draindir[rr][cc]) && (bas_ptrs[seg_index(ba_seg, r, c)] == 0.0)) {
                            if (num_cells == size_more) {
                                System.out.println("AAAAAAAAAAAARRRRRRRRRRRGGGGGGGGGGGHHHHHHHH");
                            }
                            AcellsR[num_cells] = r;
                            AcellsC[num_cells++] = c;
                        }
                    }
                }
            }
        }
    }
}
