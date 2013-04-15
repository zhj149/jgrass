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
package eu.hydrologis.jgrass.models.h.multitca;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
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
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the multitca model. It calculates the contributing areas
 * differently in convex and concave areas. In the first ones, the flow of one pixel is subdivided
 * over all the lower adjacent pixels; in the second ones instead only one drainage direction is
 * used. In our case, the weight used for the partition of the flow is inversely proportional to the
 * difference in elevation between the pixel and a downstream pixel normalized by the total drop.
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of elevations (-pit);</LI>
 * <LI>the map of the drainage directions (-flow);</LI>
 * <LI>the map of the aggregated topographic classes (-cp3);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the multitca (-multitca);</LI>
 * </OL>
 * <P></DD> Usage: h.multitca --igrass-flow flow --igrass-pit pit --igrass-casi3 casi3
 * --ograss-multitca multitca
 * </p>
 * 
 * @author <a href="mailto:daniele.andreis@gmail.com">daniele andreis </a> Erica Ghesla -
 *         erica.ghesla@ing.unitn.it, Antonello Andrea, Cozzini Andrea, Franceschi Silvia, Pisoni
 *         Silvano, Rigon Riccardo
 */
/**
 * TODO Purpose of
 * <p>
 * </p>
 * 
 * @author daniele
 * @since 1.1.0
 */
public class h_multitca extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String pitID = "pit";

    public final static String cp3ID = "casi3";

    public final static String multitcaID = "multitca";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_multitca.usage"); //$NON-NLS-1$

    private ILink flowLink = null;

    private ILink pitLink = null;

    private ILink cp3Link = null;

    private ILink multitcaLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private IInputExchangeItem cp9DataInputEI = null;

    private IOutputExchangeItem multitcaDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    private boolean doTile;

    // the flow direction.
    private int[][] dir = ModelsConstants.DIR_WITHFLOW_EXITING;

    // the incoming flow direction.
    private int[][] dirIn = ModelsConstants.DIR_WITHFLOW_ENTERING;
    public h_multitca() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_multitca( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(cp3ID)) {
            cp3Link = link;
        }
        if (id.equals(multitcaID)) {
            multitcaLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: flow, pit, cp3
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return pitDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return cp9DataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 3;
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: multitca
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return multitcaDataOutputEI;
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
        if (linkID.equals(multitcaLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }
            IValueSet flowValueSet = flowLink.getSourceComponent()
                    .getValues(time, flowLink.getID());
            GridCoverage2D flowGC = null;
            IValueSet pitValueSet = pitLink.getSourceComponent().getValues(time, pitLink.getID());
            GridCoverage2D pitGC = null;
            IValueSet cp9ValueSet = cp3Link.getSourceComponent().getValues(time, cp3Link.getID());
            GridCoverage2D cp9GC = null;

            if (flowValueSet != null && pitValueSet != null && cp9ValueSet != null) {
                flowGC = ((JGrassGridCoverageValueSet) flowValueSet).getGridCoverage2D();
                pitGC = ((JGrassGridCoverageValueSet) pitValueSet).getGridCoverage2D();
                cp9GC = ((JGrassGridCoverageValueSet) cp9ValueSet).getGridCoverage2D();
            } else {
                String error = Messages.getString("erroreading"); //$NON-NLS-1$
                err.println(error);
                throw new IOException(error);
            }

            // out.println(Messages.getString("readsmap") + " AB");

            GridCoverage2D view = flowGC.view(ViewType.GEOPHYSICS);
            PlanarImage flowImage = FluidUtils.setJaiNovalueBorder((PlanarImage) view
                    .getRenderedImage());
            view = pitGC.view(ViewType.GEOPHYSICS);
            PlanarImage pitImage = FluidUtils.setJaiNovalueBorder((PlanarImage) view
                    .getRenderedImage());
            view = cp9GC.view(ViewType.GEOPHYSICS);
            PlanarImage cp9Image = FluidUtils.setJaiNovalueBorder((PlanarImage) view
                    .getRenderedImage());
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            WritableRaster multitcaImage = multitca(pitImage, flowImage, cp9Image);
            if (multitcaImage == null) {
                err.println("Errors in execution...\n");
                return null;
            } else {

                out.println(Messages.getString("writemap") + " MULTITCA");
                jgrValueSet = new JGrassGridCoverageValueSet(multitcaImage, activeRegion, crs);

                return jgrValueSet;
            }
        }
        return null;
    }

    /**
     * in this method map's properties are defined... location, mapset... and than
     * IInputExchangeItem and IOutputExchangeItem are reated
     * 
     * @throws Exception
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

        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.multitca";
        componentId = null;

        /*
         * create the exchange items
         */
        // multitca output

        multitcaDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // pit input

        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // cp3 input

        cp9DataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        // TODO Auto-generated method stub
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(cp3Link.getID())) {
            cp3Link = null;
        }
        if (multitcaID.equals(multitcaLink.getID())) {
            multitcaLink = null;
        }
    }

    /**
     * Calculates the multitca in every pixel of the map
     * 
     * @return
     */
    private WritableRaster multitca( PlanarImage pitImage, PlanarImage flowImage,
            PlanarImage cp3Image ) {

        out.println(Messages.getString("working") + " h.multitca");

        @SuppressWarnings("unused")
        int ipos, jpos, i, j, ncicli = 0;
        double sum, delta, pos;

        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        // create new matrix
        double[] vele = new double[cols * rows];
        double[] dd = new double[cols * rows];

        out.println(Messages.getString("initializematrix"));

        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);
        RandomIter cp9RandomIter = RandomIterFactory.create(cp3Image, null);
        WritableRaster segna = FluidUtils.createDoubleWritableRaster(pitImage.getWidth(), pitImage
                .getHeight(), null, pitImage.getSampleModel(), 0.0);
        WritableRandomIter segnaRandomIter = RandomIterFactory.createWritable(segna, null);
        WritableRaster multitcaImage = FluidUtils.createDoubleWritableRaster(pitImage.getWidth(),
                pitImage.getHeight(), null, pitImage.getSampleModel(), 1.0);
        WritableRandomIter multitcaRandomIter = RandomIterFactory.createWritable(multitcaImage,
                null);

        /*
         * store the value of elevation in an array
         */
        for( int t = 0; t < rows; t++ ) {
            for( int s = 0; s < cols; s++ ) {
                vele[((t) * cols) + s] = pitRandomIter.getSampleDouble(s, t, 0);
                dd[((t) * cols) + s] = ((t) * cols) + s + 1;
            }
        }

        /*
         * sorted the array of elevation.
         */
        out.println(Messages.getString("sortvector"));
        try {
            FluidUtils.sort2DoubleVectors(vele, dd, new PrintStreamProgressMonitor(out));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * start to working with the highest value of elevation.
         */
        for( int l = cols * rows - 1; l >= 0; l-- ) {
            ncicli = cols * rows - l;
            if (vele[l] <= 0) {
                break;
            } else {

                pos = dd[l];
                // extract the index of the matrix from the arrays index.
                i = (int) (pos - 1) % cols;
                j = (int) (pos - 1) / cols;

                if (segnaRandomIter.getSampleDouble(i, j, 0) == 0.0) {

                    segnaRandomIter.setSample(i, j, 0, 1.0);
                    if (cp9RandomIter.getSampleDouble(i, j, 0) == 10
                            || cp9RandomIter.getSampleDouble(i, j, 0) == 20
                            || cp9RandomIter.getSampleDouble(i, j, 0) == 30
                            || cp9RandomIter.getSampleDouble(i, j, 0) == 40
                            || cp9RandomIter.getSampleDouble(i, j, 0) == 50
                            || cp9RandomIter.getSampleDouble(i, j, 0) == 60) {
                        sum = 0;
                        for( int k = 1; k <= 8; k++ ) {
                            ipos = i + dir[k][0];
                            jpos = j + dir[k][1];
                            delta = pitRandomIter.getSampleDouble(i, j, 0)
                                    - pitRandomIter.getSampleDouble(ipos, jpos, 0);
                            if (delta == 0) {
                                if (segnaRandomIter.getSampleDouble(ipos, jpos, 0) == 0.0
                                        && flowRandomIter.getSampleDouble(ipos, jpos, 0) == dirIn[k][2]) {
                                    resolveFlat(ipos, jpos, cols, rows, pitRandomIter,
                                            multitcaRandomIter, segnaRandomIter, flowRandomIter,
                                            cp9RandomIter);
                                }
                            }
                            if (delta > 0.0 && pitRandomIter.getSampleDouble(ipos, jpos, 0) > 0.0) {
                                sum += delta;
                            }
                        }
                        for( int k = 1; k <= 8; k++ ) {
                            ipos = i + dir[k][0];
                            jpos = j + dir[k][1];
                            delta = pitRandomIter.getSampleDouble(i, j, 0)
                                    - pitRandomIter.getSampleDouble(ipos, jpos, 0);
                            if (delta > 0.0 && pitRandomIter.getSampleDouble(ipos, jpos, 0) > 0.0) {
                                multitcaRandomIter.setSample(ipos, jpos, 0, multitcaRandomIter
                                        .getSampleDouble(ipos, jpos, 0)
                                        + multitcaRandomIter.getSampleDouble(i, j, 0)
                                        * (delta / sum));
                            } else if (delta == 0.0
                                    && flowRandomIter.getSampleDouble(i, j, 0) == dirIn[k][2]) {
                                multitcaRandomIter.setSample(ipos, jpos, 0, multitcaRandomIter
                                        .getSampleDouble(ipos, jpos, 0)
                                        + multitcaRandomIter.getSampleDouble(i, j, 0));
                            }
                        }

                    } else if (cp9RandomIter.getSampleDouble(i, j, 0) == 70
                            || cp9RandomIter.getSampleDouble(i, j, 0) == 80
                            || cp9RandomIter.getSampleDouble(i, j, 0) == 90) {
                        for( int k = 1; k <= 8; k++ ) {
                            ipos = i + dir[k][0];
                            jpos = j + dir[k][1];
                            double delta2 = pitRandomIter.getSampleDouble(i, j, 0)
                                    - pitRandomIter.getSampleDouble(ipos, jpos, 0);
                            if (delta2 == 0) {
                                if (segnaRandomIter.getSampleDouble(ipos, jpos, 0) == 0.0
                                        && flowRandomIter.getSampleDouble(ipos, jpos, 0) == dirIn[k][2]) {

                                    resolveFlat(ipos, jpos, cols, rows, pitRandomIter,
                                            multitcaRandomIter, segnaRandomIter, flowRandomIter,
                                            cp9RandomIter);
                                }
                            }
                        }
                        for( int k = 1; k <= 8; k++ ) {
                            ipos = i + dir[k][0];
                            jpos = j + dir[k][1];
                            if (flowRandomIter.getSampleDouble(i, j, 0) != 10
                                    && flowRandomIter.getSampleDouble(i, j, 0) == dir[k][2]) {

                                multitcaRandomIter.setSample(ipos, jpos, 0, multitcaRandomIter
                                        .getSampleDouble(ipos, jpos, 0)
                                        + multitcaRandomIter.getSampleDouble(i, j, 0));
                                break;
                            }
                        }
                    }

                }

            }
        }
        for( int t = 0; t < rows; t++ ) {
            for( int s = 0; s < cols; s++ ) {
                if (isNovalue(cp9RandomIter.getSampleDouble(s, t, 0))
                        || isNovalue(flowRandomIter.getSampleDouble(s, t, 0)))
                    multitcaRandomIter.setSample(s, t, 0, JGrassConstants.doubleNovalue);
            }
        }
        return multitcaImage;
    }

    private int resolveFlat( int ipos, int jpos, int cols, int rows, RandomIter pitRandomIter,
            WritableRandomIter multitcaRandomIter, WritableRandomIter segnaRandomIter,
            RandomIter flowRandomIter, RandomIter cp3RandomIter ) {
        double delta2 = 0;
        double sum2 = 0;
        int count = 0;

        segnaRandomIter.setSample(ipos, jpos, 0, 1.0);
        if (cp3RandomIter.getSampleDouble(ipos, jpos, 0) == 10
                || cp3RandomIter.getSampleDouble(ipos, jpos, 0) == 20
                || cp3RandomIter.getSampleDouble(ipos, jpos, 0) == 30
                || cp3RandomIter.getSampleDouble(ipos, jpos, 0) == 40
                || cp3RandomIter.getSampleDouble(ipos, jpos, 0) == 50
                || cp3RandomIter.getSampleDouble(ipos, jpos, 0) == 60) {
            for( int k = 1; k <= 8; k++ ) {
                int ipos2 = ipos + dir[k][0];
                int jpos2 = jpos + dir[k][1];
                delta2 = pitRandomIter.getSampleDouble(ipos, jpos, 0)
                        - pitRandomIter.getSampleDouble(ipos2, jpos2, 0);
                if (delta2 == 0) {
                    if (segnaRandomIter.getSampleDouble(ipos2, jpos2, 0) == 0.0
                            && flowRandomIter.getSampleDouble(ipos2, jpos2, 0) == dirIn[k][2]) {
                        resolveFlat(ipos2, jpos2, cols, rows, pitRandomIter, multitcaRandomIter,
                                segnaRandomIter, flowRandomIter, cp3RandomIter);
                    }
                }
                if (delta2 > 0.0 && pitRandomIter.getSampleDouble(ipos2, jpos2, 0) > 0.0) {
                    sum2 += delta2;
                }
            }
            for( int k = 1; k <= 8; k++ ) {
                int ipos2 = ipos + dir[k][0];
                int jpos2 = jpos + dir[k][1];
                delta2 = pitRandomIter.getSampleDouble(ipos, jpos, 0)
                        - pitRandomIter.getSampleDouble(ipos2, jpos2, 0);
                if (delta2 > 0.0 && pitRandomIter.getSampleDouble(ipos2, jpos2, 0) > 0.0) {
                    multitcaRandomIter.setSample(ipos2, jpos2, 0, multitcaRandomIter
                            .getSampleDouble(ipos2, jpos2, 0)
                            + multitcaRandomIter.getSampleDouble(ipos, jpos, 0) * (delta2 / sum2));
                } else if (delta2 == 0.0
                        && flowRandomIter.getSampleDouble(ipos, jpos, 0) == dir[k][2]) {
                    multitcaRandomIter.setSample(ipos2, jpos2, 0, multitcaRandomIter
                            .getSampleDouble(ipos2, jpos2, 0)
                            + multitcaRandomIter.getSampleDouble(ipos, jpos, 0));
                }
            }
        } else if (cp3RandomIter.getSampleDouble(ipos, jpos, 0) == 70
                || cp3RandomIter.getSampleDouble(ipos, jpos, 0) == 80
                || cp3RandomIter.getSampleDouble(ipos, jpos, 0) == 90) {

            for( int k = 1; k <= 8; k++ ) {

                int ipos2 = ipos + dir[k][0];
                int jpos2 = jpos + dir[k][1];

                delta2 = pitRandomIter.getSampleDouble(ipos, jpos, 0)
                        - pitRandomIter.getSampleDouble(ipos2, jpos2, 0);
                if (delta2 == 0) {
                    if (segnaRandomIter.getSampleDouble(ipos2, jpos2, 0) == 0.0
                            && flowRandomIter.getSampleDouble(ipos2, jpos2, 0) == dirIn[k][2]) {
                        resolveFlat(ipos2, jpos2, cols, rows, pitRandomIter, multitcaRandomIter,
                                segnaRandomIter, flowRandomIter, cp3RandomIter);
                    }
                }

            }

            for( int k = 1; k <= 8; k++ ) {

                int ipos2 = ipos + dir[k][0];
                int jpos2 = jpos + dir[k][1];

                if (flowRandomIter.getSampleDouble(ipos, jpos, 0) != 10
                        && flowRandomIter.getSampleDouble(ipos, jpos, 0) == dir[k][2]) {

                    multitcaRandomIter.setSample(ipos2, jpos2, 0, multitcaRandomIter
                            .getSampleDouble(ipos2, jpos2, 0)
                            + multitcaRandomIter.getSampleDouble(ipos, jpos, 0));
                    break;
                }
            }
        }

        return count;
    }
}
