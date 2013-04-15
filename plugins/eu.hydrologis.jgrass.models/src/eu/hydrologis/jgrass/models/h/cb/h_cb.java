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

package eu.hydrologis.jgrass.models.h.cb;

import java.io.File;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;

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
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * <p>
 * The openmi compliant representation of the cb model (coupledfieldmoments). It
 * calculates the histogram of a set of data contained in a matrix with respect
 * to the set of data contained in another matrix. In substance, a map of
 * R<SUP>2</SUP> &#8658; R<SUP>2</SUP>, in which each point of a bidimensional
 * system (identified by the values contained in a matrix) is mapped in a second
 * bidimensional system, is produced. The data of the first set are then grouped
 * in a prefixed number of intervals and the mean value of the independent
 * variable for each interval is calculated. To every interval corresponds a
 * certain set of values of the second set, of which the mean value is
 * calculated, and a designate number of moments which can be either centered,
 * if the functioning mode is &#8242;histogram&#8242;, or non-centered, if the
 * mode is &#8242;moments&#8242;. If the number of intervals assigned is lesser
 * than one, the data are subdivided in classes of data having the same
 * abscissa. <BR>
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG><BR>
 * </DT>
 * <OL>
 * <LI>the file containing the data of the independent variable;</LI>
 * <LI>the file containing the data which will be used as dependent variable;</LI>
 * <LI>the first moment to calculate;</LI>
 * <LI>the last moment to calculate;</LI>
 * <LI>the insertion of an optional comment is also requested;</LI>
 * </OL>
 * <P>
 * </DD>
 * <DT><STRONG>Returns:</STRONG><BR>
 * </DT>
 * <DD>
 * <OL>
 * <LI>file containing: 1) the number of elements in each interval; 2) the mean
 * value of the data in abscissa; 3) the mean value of the data in ordinate;
 * n+2) the n-esimal moment of the data in ordinate.</LI>
 * </OL>
 * <P></DD>
 * </p>
 * <p>
 * Usage: h.cb --igrass-map1 map1 --igrass-map2 map2 --otable-file1
 * nvalues#meanx#meany#mom_...#/file_path/#file1
 * " --otable-file2 meanx#tbins#/file_path/#file2" --firstmoment value
 * --lastmoment value --numbins value --binmode value
 * </p>
 * <p>
 * Note: The program uses the memory intensely. Therefore if we decide to have
 * so many intervals as the data in abscissa, the program could not function
 * correctly. Moreover the program assumes that the real data are preceded by
 * two arrays, like in the files derived from a DEM.
 * </p>
 */
public class h_cb extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String map1ID = "map1"; //$NON-NLS-1$

    public final static String map2ID = "map2"; //$NON-NLS-1$

    public final static String file1ID = "out1"; //$NON-NLS-1$

    public final static String file2ID = "out2"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_cb.usage"); //$NON-NLS-1$

    private ILink map1Link = null;

    private ILink map2Link = null;

    private ILink file1Link = null;

    private ILink file2Link = null;

    private IOutputExchangeItem file1DataOutputEI = null;

    private IOutputExchangeItem file2DataOutputEI = null;

    private IInputExchangeItem map1DataInputEI = null;

    private IInputExchangeItem map2DataInputEI = null;

    private JGrassRegion activeRegion = null;

    private double[][] moments;

    private int numbins;

    private int firstmoment;

    private int secondmoment;

    private int bintype;

    private int binmode = 1;

    private float base;

    private SplittedVectors theSplit;

    private ScalarSet outputScalarSet1;

    /** */
    public h_cb() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_cb

    /** */
    public h_cb( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_cb

    /**
     * in this method map's properties are defined... location, mapset... and
     * than IInputExchangeItem and IOutputExchangeItem are reated
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
                if (key.compareTo("numbins") == 0) { //$NON-NLS-1$
                    numbins = Integer.parseInt(argument.getValue());
                }
                if (key.compareTo("firstmoment") == 0) { //$NON-NLS-1$
                    firstmoment = Integer.parseInt(argument.getValue());
                }
                if (key.compareTo("lastmoment") == 0) { //$NON-NLS-1$
                    secondmoment = Integer.parseInt(argument.getValue());
                }
                // if (key.compareTo("binmode") == 0) { //$NON-NLS-1$
                // binmode = Integer.parseInt(argument.getValue());
                // }
            }

        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.cb"; //$NON-NLS-1$
        componentId = null; //$NON-NLS-1$

        /*
         * create the exchange items
         */
        // element set defining what we want to write
        // output1
        file1DataOutputEI = ModelsConstants.createDummyOutputExchangeItem(this);
        // output2
        file2DataOutputEI = ModelsConstants.createDummyOutputExchangeItem(this);

        // element set defining what we want to read
        // plan curvature
        map1DataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        // tca
        map2DataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (theSplit == null) {
            GridCoverage2D map1GC = ModelsConstants.getGridCoverage2DFromLink(map1Link, time, err);
            GridCoverage2D map2GC = ModelsConstants.getGridCoverage2DFromLink(map2Link, time, err);
            PlanarImage map1Image = (PlanarImage) map1GC.getRenderedImage();
            PlanarImage map2Image = (PlanarImage) map2GC.getRenderedImage();
            if (!cb(map1Image, map2Image)) {
                return null;
            }
        }

        // supply the results
        if (file1Link != null && linkID.equals(file1Link.getID())) {
            outputScalarSet1 = new ScalarSet();
            int columns = moments[0].length + 1;
            outputScalarSet1.add((double) columns);
            for( int i = 0; i < theSplit.splittedindex.length; i++ ) {
                outputScalarSet1.add(theSplit.splittedindex[i]);
                for( int j = 0; j < moments[0].length; j++ ) {
                    outputScalarSet1.add(moments[i][j]);
                }
            }
            return outputScalarSet1;
        } else {
            ScalarSet outputScalarSet2 = new ScalarSet();
            int columns = 2;
            outputScalarSet2.add((double) columns);
            for( int i = 0; i < moments.length; i++ ) {
                outputScalarSet2.add(moments[i][0]);
                outputScalarSet2.add(theSplit.splittedindex[i]);
            }
            return outputScalarSet2;
        }

    }

    private boolean cb( PlanarImage image1, PlanarImage image2 ) throws Exception {

        out.println(Messages.getString("h_cb.vectorize")); //$NON-NLS-1$
        double[] U = FluidUtils.vectorizeDoubleMatrix(image1);
        double[] T = FluidUtils.vectorizeDoubleMatrix(image2);

        out.println(Messages.getString("h_cb.sortvector")); //$NON-NLS-1$
        FluidUtils.sort2DoubleVectors(U, T, null);

        /** ******************************************* */
        theSplit = new SplittedVectors();

        int num_max = 1000;
        /*
         * if (bintype == 1) {
         */
        out.println(Messages.getString("h_cb.splitvector")); //$NON-NLS-1$
        FluidUtils.split2realvectors(U, T, theSplit, numbins, num_max, out);
        /*
         * } else { delta = FluidUtils.exponentialsplit2realvectors(U, T,
         * theSplit, N, num_max, base); }
         */

        out.println(Messages.getString("h_cb.creatematrix")); //$NON-NLS-1$
        moments = new double[theSplit.splittedindex.length][secondmoment - firstmoment + 2];
        if (binmode == 1) // always true for now, other modes not implemented
        // yet
        {
            for( int h = 0; h < theSplit.splittedindex.length; h++ ) {
                moments[h][0] = FluidUtils.doubleNMoment(theSplit.splittedvalues1[h], (int) theSplit.splittedindex[h], 0.0, 1.0,
                        out);
                moments[h][1] = FluidUtils.doubleNMoment(theSplit.splittedvalues2[h], (int) theSplit.splittedindex[h], 0.0, 1.0,
                        out);
                if (firstmoment == 1)
                    firstmoment++;
                for( int k = firstmoment; k <= secondmoment; k++ ) {
                    moments[h][k - firstmoment + 2] = FluidUtils.doubleNMoment(theSplit.splittedvalues2[h],
                            (int) theSplit.splittedindex[h], moments[h][1], (double) k, out);
                }
            }

        }
        // else if (binmode == 2) // why is this exactly the same as the mode
        // // 'H' ???
        // {
        // for( int h = 0; h < theSplit.splittedindex.length; h++ ) {
        // moments[h][0] =
        // FluidUtils.double_n_moment(theSplit.splittedvalues1[h],
        // (int) theSplit.splittedindex[h], 0.0, 1.0);
        // moments[h][1] =
        // FluidUtils.double_n_moment(theSplit.splittedvalues2[h],
        // (int) theSplit.splittedindex[h], 0.0, 1.0);
        //
        // if (firstmoment == 1)
        // firstmoment++;
        // for( int k = firstmoment; k <= secondmoment; k++ ) {
        // moments[h][k - firstmoment + 2] = FluidUtils.double_n_moment(
        // theSplit.splittedvalues2[h], (int) theSplit.splittedindex[h],
        // moments[h][k - firstmoment + 1], (double) k);
        // }
        // }
        // }

        return true;
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(map1ID)) {
            map1Link = link;
        }
        if (id.equals(map2ID)) {
            map2Link = link;
        }
        if (id.equals(file1ID)) {
            file1Link = link;
        }
        if (id.equals(file2ID)) {
            file2Link = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: map1, map2
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return map1DataInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return map2DataInputEI;
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
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: file1
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return file1DataOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return file2DataOutputEI;
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
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(map1Link.getID())) {
            map1Link = null;
        }
        if (linkID.equals(map2Link.getID())) {
            map2Link = null;
        }
        if (linkID.equals(file1Link.getID())) {
            file1Link = null;
        }
        if (linkID.equals(file2Link.getID())) {
            file2Link = null;
        }
    }

    /**
     * 
     */

}
