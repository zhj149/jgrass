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
package eu.hydrologis.libs.utils;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.intNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;
import static eu.hydrologis.libs.messages.MessageHelper.NUMBERING_STREAMS;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import jaitools.tiledimage.DiskMemImage;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.media.jai.BorderExtenderConstant;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRandomIter;
import javax.vecmath.Point4d;

import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.IProgressMonitorJGrass;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.jgrass.libs.utils.raster.RasterUtilities;
import eu.hydrologis.jgrass.models.h.cb.SplittedVectors;
import eu.hydrologis.libs.openmi.ModelsConstants;

/**
 * <p>
 * This class contains a set of method that are used in Horton Machine.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 */
public class FluidUtils {

    private static int[][] dirOut = ModelsConstants.DIR_WITHFLOW_EXITING;
    private static int[][] dirIn = ModelsConstants.DIR_WITHFLOW_ENTERING;

    /**
     * Creates a new {@link PrintStream} to be used for output.
     * 
     * @param arg0
     * @param arg1
     * @return
     */
    public synchronized static PrintStream newPrintStream( PrintStream arg0, PrintStream arg1 ) {
        return new PrintStream((null != arg0) ? arg0 : arg1, true);
    } // newPrintStream

    /**
     * Creates a border of novalue.
     * 
     * @param flowData
     */
    public static void setNovalueBorder( RasterData flowData ) {
        for( int j = 0; j < flowData.getCols(); j++ ) {

            flowData.setValueAt(0, j, doubleNovalue);
            flowData.setValueAt(flowData.getRows() - 1, j, doubleNovalue);
        }
        for( int i = 0; i < flowData.getRows(); i++ ) {
            flowData.setValueAt(i, 0, doubleNovalue);
            flowData.setValueAt(i, flowData.getCols() - 1, doubleNovalue);
        }
    }

    /**
     * Creates a border of novalue.
     * 
     * @param matrix
     */
    public static void setNovalueBorderInt( int[][] matrix ) {
        for( int j = 0; j < matrix[0].length; j++ ) {

            matrix[0][j] = intNovalue;
            matrix[matrix.length - 1][j] = intNovalue;
        }
        for( int i = 0; i < matrix.length; i++ ) {
            matrix[i][0] = intNovalue;
            matrix[i][matrix[0].length - 1] = intNovalue;
        }

    }

    /**
     * Creates a border of novalue.
     * 
     * @param writableImage
     * @return the new {@link RenderedOp} that backs the {@link WritableRenderedImage}.
     */
    public static RenderedOp setJaiNovalueBorder( WritableRenderedImage writableImage ) {

        // extract the image without the border
        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(writableImage);
        parameterBlock.add(1.0F);
        parameterBlock.add(1.0F);
        parameterBlock.add((float) writableImage.getWidth() - 2.0F);
        parameterBlock.add((float) writableImage.getHeight() - 2.0F);
        PlanarImage crop = JAI.create("crop", parameterBlock);
        parameterBlock = new ParameterBlock();
        parameterBlock.addSource(crop);
        // set the size of the board: links, right, up, down.
        parameterBlock.add(1);
        parameterBlock.add(1);
        parameterBlock.add(1);
        parameterBlock.add(1);
        // select which type of border: fillthe pixel with 0
        parameterBlock.add(new BorderExtenderConstant(new double[]{doubleNovalue}));

        return JAI.create("Border", parameterBlock);

    }

    /**
     * Creates a border of novalue.
     * 
     * @param writableImage
     * @return the new {@link RenderedOp} that backs the {@link RenderedImage}.
     */
    public static RenderedOp setJaiNovalueBorder( RenderedImage writableImage ) {

        // extract the image without the border
        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(writableImage);
        parameterBlock.add(1.0F);
        parameterBlock.add(1.0F);
        parameterBlock.add((float) writableImage.getWidth() - 2.0F);
        parameterBlock.add((float) writableImage.getHeight() - 2.0F);
        PlanarImage crop = JAI.create("crop", parameterBlock);
        parameterBlock = new ParameterBlock();
        parameterBlock.addSource(crop);
        // set the size of the board: links, right, up, down.
        parameterBlock.add(1);
        parameterBlock.add(1);
        parameterBlock.add(1);
        parameterBlock.add(1);
        // select which type of border: fillthe pixel with 0
        parameterBlock.add(new BorderExtenderConstant(new double[]{doubleNovalue}));

        return JAI.create("Border", parameterBlock);

    }

    /**
     * Add a Novalue border to a Writable image in other way than the chain of operation.
     * 
     * @param image
     */

    public static void setJAInoValueBorterIT( WritableRenderedImage image ) {
        WritableRandomIter imageRandomIter = RandomIterFactory.createWritable(image, null);
        int minY = image.getMinY();
        int maxY = minY + image.getHeight();
        for( int y = minY; y < maxY; y++ ) {
            imageRandomIter.setSample(0, y, 0, doubleNovalue);
            imageRandomIter.setSample(image.getWidth() - 1, y, 0, doubleNovalue);
        }
        int minX = image.getMinX();
        int maxX = minX + image.getWidth();
        for( int x = minX; x < maxX; x++ ) {
            imageRandomIter.setSample(x, 0, 0, doubleNovalue);
            imageRandomIter.setSample(x, image.getHeight() - 1, 0, doubleNovalue);
        }
        imageRandomIter.done();
    }

    public static void setJAInoValueBorderIT( WritableRaster image ) {
        WritableRandomIter imageRandomIter = RandomIterFactory.createWritable(image, null);
        int minY = image.getMinY();
        int maxY = minY + image.getHeight();
        for( int y = minY; y < maxY; y++ ) {
            imageRandomIter.setSample(0, y, 0, doubleNovalue);
            imageRandomIter.setSample(image.getWidth() - 1, y, 0, doubleNovalue);
        }
        int minX = image.getMinX();
        int maxX = minX + image.getWidth();
        for( int x = minX; x < maxX; x++ ) {
            imageRandomIter.setSample(x, 0, 0, doubleNovalue);
            imageRandomIter.setSample(x, image.getHeight() - 1, 0, doubleNovalue);
        }
        imageRandomIter.done();
    }

    /**
     * Creates a border of novalue.
     * 
     * @param writableImage
     * @return the new {@link RenderedOp} that backs the {@link RenderedOp}.
     */
    public static RenderedOp setJaiNovalueBorder( RenderedOp writableImage ) {

        // extract the image without the border
        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(writableImage);
        parameterBlock.add(1.0F);
        parameterBlock.add(1.0F);
        parameterBlock.add((float) writableImage.getWidth() - 2.0F);
        parameterBlock.add((float) writableImage.getHeight() - 2.0F);
        PlanarImage crop = JAI.create("crop", parameterBlock);
        parameterBlock = new ParameterBlock();
        parameterBlock.addSource(crop);
        // set the size of the board: links, right, up, down.
        parameterBlock.add(1);
        parameterBlock.add(1);
        parameterBlock.add(1);
        parameterBlock.add(1);
        // select which type of border: fillthe pixel with 0
        parameterBlock.add(new BorderExtenderConstant(new double[]{doubleNovalue}));

        return JAI.create("Border", parameterBlock);

    }

    /**
     * Sorts vector1 in ascending order, moving the values of the vector2 of the same indexes.
     * 
     * @param vector1
     * @param vector2
     * @param monitor
     * @throws Exception
     */
    public static void sort2DoubleVectors( double[] vector1, double[] vector2,
            IProgressMonitorJGrass monitor ) throws Exception {
        QuickSortAlgorithm t = new QuickSortAlgorithm(monitor);
        t.sort(vector1, vector2);
    }

    // /**
    // * IS_ONTHEBORDER it controls if a pixel (i,j) is found on the border of the
    // * river basin
    // *
    // * @param pitData
    // * @param novalue
    // * @param i
    // * @param j
    // * @return
    // */
    // public static int is_ontheborder(RasterData pitData, double novalue,
    // int i, int j) {
    // int[][] dir = { { 0, 0, 0 }, { 0, 1, 1 }, { -1, 1, 2 }, { -1, 0, 3 },
    // { -1, -1, 4 }, { 0, -1, 5 }, { 1, -1, 6 }, { 1, 0, 7 },
    // { 1, 1, 8 }, { 0, 0, 9 }, { 0, 0, 10 } };
    //
    // for (int k = 1; k <= 8; k++) {
    // if (pitData.getValueAt(i + dir[k][0], j + dir[k][1]) == novalue) {
    // return 1;
    // }
    // }
    // return 0;
    // }
    //
    // /**
    // * is_thesamebasin it controls if two near points belong to the same
    // * sub-basin
    // *
    // * @param map
    // * @param i
    // * @param j
    // * @return
    // */
    // public static int is_thesamebasin(double[][] map, int i, int j) {
    // int[][] dir = { { 0, 0, 0 }, { 0, 1, 1 }, { -1, 1, 2 }, { -1, 0, 3 },
    // { -1, -1, 4 }, { 0, -1, 5 }, { 1, -1, 6 }, { 1, 0, 7 },
    // { 1, 1, 8 }, { 0, 0, 9 }, { 0, 0, 10 } };
    //
    // int n = 0;
    // for (int k = 1; k <= 8; k++) {
    // if (map[i + dir[k][0]][j + dir[k][1]] != map[i][j]) {
    // n++;
    // }
    // }
    // if (n == 1)
    // return 1;
    // if (n > 1)
    // return 1;
    // else
    // return 0;
    // }

    /**
     * Moves one pixel downstream.
     * 
     * @param rowCol the array containing the row and column of the current pixel. It will be
     *        modified here to represent the next downstream pixel.
     * @param flowdirection the current flowdirection number.
     * @return true if everything went well.
     */
    public static boolean go_downstream( int[] rowCol, double flowdirection ) {

        int n = (int) flowdirection;
        if (n == 10) {
            return true;
        } else if (n < 1 || n > 9) {
            return false;
        } else {
            rowCol[1] += dirOut[n][1];
            rowCol[0] += dirOut[n][0];
            return true;
        }
    }

    /**
     * Moves one pixel upstream.
     * 
     * @param p
     * @param m
     * @param tca
     * @param l
     * @param param
     */
    public static void go_upstream_a( int[] p, RandomIter flowRandomIter, RandomIter tcaRandomIter,
            RandomIter lRandomIter, int[] param ) {
        double area = 0, lenght = 0;
        int[] point = new int[2];
        int kk = 0, count = 0;

        point[0] = p[0];
        point[1] = p[1];
        // check how many pixels are draining in the considered pixel and select
        // the pixel with maximun tca
        for( int k = 1; k <= 8; k++ ) {
            if (flowRandomIter.getSampleDouble(p[0] + dirIn[k][0], p[1] + dirIn[k][1], 0) == dirIn[k][2]) {
                // counts how many pixels are draining in the considere
                count++;
                if (tcaRandomIter.getSampleDouble(p[0] + dirIn[k][0], p[1] + dirIn[k][1], 0) >= area) {
                    // if two pixels has the same tca select the pixel with the
                    // maximum vale of hacklength
                    if (tcaRandomIter.getSampleDouble(p[0] + dirIn[k][0], p[1] + dirIn[k][1], 0) == area) {
                        if (lRandomIter.getSampleDouble(p[0] + dirIn[k][0], p[1] + dirIn[k][1], 0) > lenght) {
                            kk = k;
                            area = tcaRandomIter.getSampleDouble(p[0] + dirIn[k][0], p[1]
                                    + dirIn[k][1], 0);
                            lenght = lRandomIter.getSampleDouble(p[0] + dirIn[k][0], p[1]
                                    + dirIn[k][1], 0);
                            point[0] = p[0] + dirIn[k][0];
                            point[1] = p[1] + dirIn[k][1];
                        }
                    } else {
                        kk = k;
                        area = tcaRandomIter.getSampleDouble(p[0] + dirIn[k][0],
                                p[1] + dirIn[k][1], 0);
                        lenght = lRandomIter.getSampleDouble(p[0] + dirIn[k][0],
                                p[1] + dirIn[k][1], 0);
                        point[0] = p[0] + dirIn[k][0];
                        point[1] = p[1] + dirIn[k][1];
                    }
                }
            }

        }
        p[0] = point[0];
        p[1] = point[1];
        param[0] = kk;
        param[1] = count;
    }

    /**
     * Moves one pixel upstream following the supplied network. TODO Daniele doc
     * 
     * @param colRow
     * @param flowIterator
     * @param netnumIterator
     * @param param
     */
    public static void goUpStreamOnNetFixed( int[] colRow, RandomIter flowIterator,
            RandomIter netnumIterator, int[] param ) {

        int kk = 0, count = 0;
        int[] point = new int[2];

        for( int k = 1; k <= 8; k++ ) {
            if (flowIterator.getSampleDouble(colRow[0] + dirIn[k][0], colRow[1] + dirIn[k][1], 0) == dirIn[k][2]) {
                count++;
                if (netnumIterator.getSampleDouble(colRow[0] + dirIn[k][0],
                        colRow[1] + dirIn[k][1], 0) == netnumIterator.getSampleDouble(colRow[0],
                        colRow[1], 0)) {
                    kk = k;
                    point[0] = colRow[0] + dirIn[k][0];
                    point[1] = colRow[1] + dirIn[k][1];
                }
            }
        }
        if (kk == 0) {
            for( int k = 1; k <= 8; k++ ) {
                if (flowIterator.getSampleDouble(colRow[0] + dirIn[k][0], colRow[1] + dirIn[k][1],
                        0) == dirIn[k][2]) {
                    kk = k;
                    point[0] = colRow[0] + dirIn[k][0];
                    point[1] = colRow[1] + dirIn[k][1];
                }
            }
        }
        colRow[0] = point[0];
        colRow[1] = point[1];
        param[0] = kk;
        param[1] = count;
    }

    /**
     * Verifies if the point is a source pixel in the supplied flow raster.
     * 
     * @param flowRaster
     * @param colRow the col and row of the point to check.
     * @return
     */
    public static boolean sourcesqJAI( RandomIter flowRaster, int[] colRow ) {

        if (flowRaster.getSampleDouble(colRow[0], colRow[1], 0) < 9.0
                && flowRaster.getSampleDouble(colRow[0], colRow[1], 0) > 0.0) {

            for( int k = 1; k <= 8; k++ ) {
                if (flowRaster.getSampleDouble(colRow[0] + dirIn[k][0], colRow[1] + dirIn[k][1], 0) == dirIn[k][2]) {
                    return false;

                }
            }
            return true;
        } else {

            return false;
        }
    }

    // /**
    // * SOURCESQ check if a pixel is a source
    // *
    // * @param flowData
    // * @param punto
    // * @return
    // */
    // public static boolean sourcesq(RasterData flowData, int[] punto) {
    //
    // int[][] dir = { { 0, 0, 0 }, { 0, 1, 5 }, { -1, 1, 6 }, { -1, 0, 7 },
    // { -1, -1, 8 }, { 0, -1, 1 }, { 1, -1, 2 }, { 1, 0, 3 },
    // { 1, 1, 4 }, { 0, 0, 0 }, { 0, 0, 0 } };
    //
    // if (flowData.getValueAt(punto[0], punto[1]) < 9.0
    // && flowData.getValueAt(punto[0], punto[1]) > 0.0) {
    //
    // for (int k = 1; k <= 8; k++) {
    // if (flowData.getValueAt(punto[0] + dir[k][0], punto[1]
    // + dir[k][1]) == dir[k][2]) {
    // return false;
    // }
    // }
    // return true;
    // } else {
    //
    // return false;
    // }
    //
    // }

    /**
     * It controls if the considered point is a source in the network map.
     * 
     * @param flowIterator {@link RandomIter iterator} of flowdirections map
     * @param colRow the col and row of the point to check.
     * @param num channel number
     * @param netNum {@link RandomIter iterator} of the netnumbering map.
     * @return
     */
    public static boolean sourcesNet( RandomIter flowIterator, int[] colRow, int num,
            RandomIter netNum ) {

        if (flowIterator.getSampleDouble(colRow[0], colRow[1], 0) <= 10.0
                && flowIterator.getSampleDouble(colRow[0], colRow[1], 0) > 0.0) {

            for( int k = 1; k <= 8; k++ ) {
                if (flowIterator.getSampleDouble(colRow[0] + dirIn[k][0], colRow[1] + dirIn[k][1],
                        0) == dirIn[k][2]
                        && netNum.getSampleDouble(colRow[0] + dirIn[k][0], colRow[1] + dirIn[k][1],
                                0) == num) {
                    return false;
                }
            }
            return true;
        } else {

            return false;
        }

    }

    // /**
    // * IS_DRAINED
    // *
    // * @param m
    // * @param rete
    // * @param i
    // * @param j
    // * @return
    // */
    // public static int is_drained(RasterData m, RasterData rete, int i, int j) {
    // int[][] odir = { { 0, 0, 0 }, { 0, 1, 5 }, { -1, 1, 6 }, { -1, 0, 7 },
    // { -1, -1, 8 }, { 0, -1, 1 }, { 1, -1, 2 }, { 1, 0, 3 },
    // { 1, 1, 4 }, { 0, 0, 0 }, { 0, 0, 0 }
    //
    // };
    //
    // if (m.getValueAt(i, j) != 9.0 && m.getValueAt(i, j) != 0.0) {
    // for (int k = 1; k <= 8; k++) {
    // if (m.getValueAt(i + odir[k][0], j + odir[k][1]) == odir[k][2]
    // && rete.getValueAt(i + odir[k][0], j + odir[k][1]) == 2) {
    // return 1;
    // }
    // }
    // return 0;
    // } else {
    // System.out
    // .println("A point outside the network was checked :: This should never happen");
    // }
    //
    // return -1;
    // }

    // /**
    // * MARK_OUTLETS marks all the outlets of the considered region on the
    // * drainage directions map. A convention has been adopted by which the the
    // * preexisting drainage direction value of every outlet is changed with the
    // * value 10.
    // *
    // * @param flowData
    // * @param copt
    // */
    // public static void mark_outlets(RasterData flowData) {
    // /* char ch */
    // int[] punto = new int[2];
    // int[] oldpunto = new int[2];
    // int activecols = flowData.getCols();
    // int activerows = flowData.getRows();
    //
    // for (int i = 0; i < activerows; i++) {
    // for (int j = 0; j < activecols; j++) {
    // punto[0] = i;
    // punto[1] = j;
    // if (FluidUtils.sourcesq(flowData, punto)) {
    // oldpunto[0] = punto[0];
    // oldpunto[1] = punto[1];
    // while (flowData.getValueAt(punto[0], punto[1]) < 9.0
    // && !isNovalue(flowData.getValueAt(punto[0], punto[1]) )) {
    // oldpunto[0] = punto[0];
    // oldpunto[1] = punto[1];
    // if (!FluidUtils.go_downstream(punto, flowData
    // .getValueAt(punto[0], punto[1])))
    // return;
    // }
    // if (flowData.getValueAt(punto[0], punto[1]) != 10.0)
    // flowData.setValueAt(oldpunto[0], oldpunto[1], 10.0);
    // }
    // }
    // }
    // }

    /**
     * OUTLETDISTANCE calculates the distance of every pixel of the catchment basin from the outlet,
     * calculated along the drainage directions
     * 
     * @param flowImage
     * @param distToOutImage
     * @param dx
     * @param dy
     * @param out
     */
    public static void outletdistance( RandomIter fileRandomIter, WritableRaster distToOutImage,
            double dx, double dy, PrintStream out ) {
        WritableRandomIter distToOutRandomIter = RandomIterFactory.createWritable(distToOutImage,
                null);
        int activeCols = distToOutImage.getWidth();
        int activeRows = distToOutImage.getHeight();
        int[] flow = new int[2];
        double oldir = 0.0;
        double[] grid = new double[11];
        double count = 0.0;

        grid[0] = grid[9] = grid[10] = 0;
        grid[1] = grid[5] = abs(dx);
        grid[3] = grid[7] = abs(dy);
        grid[2] = grid[4] = grid[6] = grid[8] = sqrt(dx * dx + dy * dy);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating outlet distance...", activeRows);
        for( int i = 0; i < activeRows; i++ ) {
            for( int j = 0; j < activeCols; j++ ) {
                flow[0] = j;
                flow[1] = i;
                if (isNovalue(fileRandomIter.getSampleDouble(flow[0], flow[1], 0))) {
                    distToOutRandomIter.setSample(flow[0], flow[1], 0, doubleNovalue);
                } else {
                    if (FluidUtils.sourcesqJAI(fileRandomIter, flow)) {
                        count = 0;
                        oldir = fileRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        FluidUtils.go_downstream(flow, fileRandomIter.getSampleDouble(flow[0],
                                flow[1], 0));
                        while( !isNovalue(fileRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && !isNovalue(fileRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && fileRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                && distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) <= 0 ) {
                            count += grid[(int) oldir];
                            oldir = fileRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            FluidUtils.go_downstream(flow, fileRandomIter.getSampleDouble(flow[0],
                                    flow[1], 0));
                        }
                        if (distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) > 0) {
                            count += grid[(int) oldir]
                                    + distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            distToOutRandomIter.setSample(j, i, 0, count);
                        } else if (fileRandomIter.getSampleDouble(flow[0], flow[1], 0) > 9) {
                            distToOutRandomIter.setSample(flow[0], flow[1], 0, 0);
                            count += grid[(int) oldir];
                            distToOutRandomIter.setSample(j, i, 0, count);
                        }

                        flow[0] = j;
                        flow[1] = i;
                        oldir = fileRandomIter.getSampleDouble(flow[0], flow[1], 0);
                        FluidUtils.go_downstream(flow, fileRandomIter.getSampleDouble(flow[0],
                                flow[1], 0));
                        while( !isNovalue(fileRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && !isNovalue(fileRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && fileRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                && distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) <= 0 ) {
                            count -= grid[(int) oldir];
                            distToOutRandomIter.setSample(flow[0], flow[1], 0, count);
                            oldir = fileRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            FluidUtils.go_downstream(flow, fileRandomIter.getSampleDouble(flow[0],
                                    flow[1], 0));
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
    }

    /**
     * Calculates the distance of every pixel of the catchment basin from the outlet, calculated
     * along the drainage directions. It is topological disance.
     * 
     * @param flowImage
     * @param distToOutImage
     * @param out
     */
    public static void topological_outletdistance( RandomIter fileRandomIter,
            WritableRaster distToOutImage, PrintStream out ) {

        int[] flow = new int[2];
        double count = 0.0;
        int activecols = distToOutImage.getWidth();
        int activerows = distToOutImage.getHeight();
        WritableRandomIter distToOutRandomIter = RandomIterFactory.createWritable(distToOutImage,
                null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating topological outlet distance...", activerows);
        for( int i = 0; i < activerows; i++ ) {
            for( int j = 0; j < activecols; j++ ) {
                flow[0] = j;
                flow[1] = i;
                if (isNovalue(fileRandomIter.getSampleDouble(flow[0], flow[1], 0))) {
                    distToOutRandomIter.setSample(flow[0], flow[1], 0, doubleNovalue);

                } else {
                    flow[0] = j;
                    flow[1] = i;
                    if (FluidUtils.sourcesqJAI(fileRandomIter, flow)) {
                        count = 0;
                        FluidUtils.go_downstream(flow, fileRandomIter.getSampleDouble(flow[0],
                                flow[1], 0));
                        while( !isNovalue(fileRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && fileRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                && distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) <= 0 ) {
                            count += 1;
                            FluidUtils.go_downstream(flow, fileRandomIter.getSampleDouble(flow[0],
                                    flow[1], 0));
                        }
                        if (distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) > 0) {
                            count += 1 + distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            distToOutRandomIter.setSample(j, i, 0, count);
                        } else if (fileRandomIter.getSampleDouble(flow[0], flow[1], 0) > 9) {
                            distToOutRandomIter.setSample(flow[0], flow[1], 0, 0);
                            count += 1;
                            distToOutRandomIter.setSample(j, i, 0, count);
                        }

                        flow[0] = j;
                        flow[1] = i;
                        FluidUtils.go_downstream(flow, fileRandomIter.getSampleDouble(flow[0],
                                flow[1], 0));
                        while( !isNovalue(fileRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && fileRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                && distToOutRandomIter.getSampleDouble(flow[0], flow[1], 0) <= 0 ) {
                            count -= 1;
                            distToOutRandomIter.setSample(flow[0], flow[1], 0, count);
                            FluidUtils.go_downstream(flow, fileRandomIter.getSampleDouble(flow[0],
                                    flow[1], 0));
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
    }

    /**
     * SUM_DOWNSTREAM calculates the sum of the values of a specified quantity from every point to
     * the outlet, during the calculation the drainage directions are followed.
     * 
     * @param flow
     * @param netImage
     * @param out
     * @param dist
     * @return
     */
    public static WritableRaster sum_downstream( RandomIter flowRandomIter,
            RandomIter netRandomIter, int width, int height, PrintStream out ) {
        int[] punto = new int[2];
        WritableRaster distImage = FluidUtils.createDoubleWritableRaster(width, height, null, null,
                null);
        WritableRandomIter distRandomIter = RandomIterFactory.createWritable(distImage, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating downstream sum...", height);
        for( int j = 0; j < height; j++ ) {
            for( int i = 0; i < width; i++ ) {
                if (!isNovalue(flowRandomIter.getSampleDouble(i, j, 0))) {
                    punto[0] = i;
                    punto[1] = j;
                    while( flowRandomIter.getSampleDouble(punto[0], punto[1], 0) < 9
                            && !isNovalue(flowRandomIter.getSampleDouble(punto[0], punto[1], 0)) ) {
                        distRandomIter.setSample(punto[0], punto[1], 0, distRandomIter
                                .getSampleDouble(punto[0], punto[1], 0)
                                + netRandomIter.getSampleDouble(i, j, 0));
                        if (!FluidUtils.go_downstream(punto, flowRandomIter.getSampleDouble(
                                punto[0], punto[1], 0)))
                            return null;
                    }

                    distRandomIter.setSample(punto[0], punto[1], 0, distRandomIter.getSampleDouble(
                            punto[0], punto[1], 0)
                            + netRandomIter.getSampleDouble(i, j, 0));
                } else {
                    distRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
            pm.worked(1);
        }
        pm.done();

        return distImage;
    }

    /**
     * Creates a constant image on which can be written.
     * 
     * @param width
     * @param height
     * @param sm
     * @param value
     * @return
     */
    public static DiskMemImage createConstantDiskMemImage( int width, int height, SampleModel sm,
            double value ) {
        DiskMemImage image = new DiskMemImage(0, 0, width, height, 0, 0, sm, null);
        WritableRandomIter iter = RandomIterFactory.createWritable(image, null);

        for( int y = 0; y < height; y++ ) {
            for( int x = 0; x < width; x++ ) {
                iter.setSample(x, y, 0, value);
            }
        }
        iter.done();
        return image;
    }

    /**
     * Creates a single banded diskmem image.
     * 
     * @param width the width of the image.
     * @param height the height of the image.
     * @param dataClass the class of the datatype to use. If null, {@link DataBuffer#TYPE_DOUBLE} is
     *        used.
     * @param sampleModel the {@link SampleModel}. If null one is created.
     * @param value the value to put in the image. If null, the default java 0 is used.
     * @return the {@link DiskMemImage}.
     */
    public static DiskMemImage createDoubleDiskMemImage( int width, int height,
            Class< ? > dataClass, SampleModel sampleModel, Double value ) {
        int dataType = DataBuffer.TYPE_DOUBLE;
        if (dataClass != null) {
            if (dataClass.isAssignableFrom(Integer.class)) {
                dataType = DataBuffer.TYPE_INT;
            } else if (dataClass.isAssignableFrom(Float.class)) {
                dataType = DataBuffer.TYPE_FLOAT;
            } else if (dataClass.isAssignableFrom(Byte.class)) {
                dataType = DataBuffer.TYPE_BYTE;
            }
        }
        if (sampleModel == null) {
            sampleModel = new ComponentSampleModel(dataType, width, height, 1, width, new int[]{0});
        }
        DiskMemImage image = new DiskMemImage(0, 0, width, height, 0, 0, sampleModel, null);
        if (value != null) {
            // autobox once
            double v = value;

            WritableRandomIter iter = RandomIterFactory.createWritable(image, null);
            for( int y = 0; y < height; y++ ) {
                for( int x = 0; x < width; x++ ) {
                    iter.setSample(x, y, 0, v);
                }
            }
            iter.done();
        }
        return image;
    }

    public static WritableRaster createDoubleWritableRaster( int width, int height,
            Class< ? > dataClass, SampleModel sampleModel, Double value ) {
        int dataType = DataBuffer.TYPE_DOUBLE;
        if (dataClass != null) {
            if (dataClass.isAssignableFrom(Integer.class)) {
                dataType = DataBuffer.TYPE_INT;
            } else if (dataClass.isAssignableFrom(Float.class)) {
                dataType = DataBuffer.TYPE_FLOAT;
            } else if (dataClass.isAssignableFrom(Byte.class)) {
                dataType = DataBuffer.TYPE_BYTE;
            }
        }
        if (sampleModel == null) {
            sampleModel = new ComponentSampleModel(dataType, width, height, 1, width, new int[]{0});
        }

        WritableRaster raster = RasterFactory.createWritableRaster(sampleModel, null);
        // TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, null);

        // DiskMemImage image = new DiskMemImage(0, 0, width, height, 0, 0, sampleModel, null);
        if (value != null) {
            // autobox once
            double v = value;

            // WritableRandomIter iter = RandomIterFactory.createWritable(image, null);
            for( int y = 0; y < height; y++ ) {
                for( int x = 0; x < width; x++ ) {
                    raster.setSample(x, y, 0, v);
                }
            }
            // iter.done();
        }
        return raster;
    }

    /**
     * TODO
     * 
     * @param flowIterator
     * @param tcaIterator
     * @param dist
     * @param flow
     * @param maz
     * @param diss
     * @return
     */
    public static boolean tcaMax( RandomIter flowIterator, RandomIter tcaIterator, RandomIter dist,
            int[] flow, double maz, double diss ) {
        int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING;

        for( int k = 1; k <= 8; k++ ) {
            if (flowIterator.getSample(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]) {
                if (tcaIterator.getSample(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) >= maz) {
                    if (tcaIterator.getSample(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == maz) {
                        if (dist.getSample(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) > diss)
                            return false;
                    } else
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Takes a input raster and vectorializes it.
     * 
     * @param input
     * @return
     */
    public static double[] vectorizeDoubleMatrix( RenderedImage input ) {
        double[] U = new double[input.getWidth() * input.getHeight()];
        RandomIter inputRandomIter = RandomIterFactory.create(input, null);

        int j = 0;
        for( int i = 0; i < input.getHeight() * input.getWidth(); i = i + input.getWidth() ) {
            double tmp[] = new double[input.getWidth()];
            for( int k = 0; k < input.getWidth(); k++ ) {
                tmp[k] = inputRandomIter.getSampleDouble(k, j, 0);
            }

            System.arraycopy(tmp, 0, U, i, input.getWidth());
            j++;
        }

        return U;
    }

    /**
     * Split 2 vector in some classes (bins). The two vector (U and T) usually are sorted with
     * FluidUtils.sort2DoubleVectors(U, T, null) (before this step). If N is minor or equals to 1
     * then a bin have only one value of U otherwise the value are group together following the
     * number of bins.
     * 
     * @param U
     * @param T
     * @param theSplit
     * @param N
     * @param num_max
     * @param out
     * @return
     */
    public static double split2realvectors( double[] U, double[] T, SplittedVectors theSplit,
            int N, int num_max, PrintStream out ) {

        double delta = 0, min, max;
        int i, count, count1, minposition = 0, maxposition, bin_vuoti;
        int[] bins;
        int head = 0;

        // minposition = 0; //pippo 1;
        // maxposition = U.length - 1; //pippo U.length;
        bins = new int[U.length];

        if (N <= 1) {
            count1 = 1;
            count = 1;

            int index = 0;
            while( count < U.length ) {

                while( U[count] == U[count - 1] && count <= U.length ) {
                    count++;
                }

                index++;
                bins[index] = count - count1;
                head++;
                count1 = count;
                count++;
                if (head > num_max)
                    out.println("The number of bin eccedes the maximum number allowed.");
            }

        } else if (N > 1) {

            minposition = 0; // here Ricci had made it be two (i.e. 1 for no
            // fluidturtles) WHY?!?!?
            max = U[U.length - 1]; // pippo
            while( isNovalue(U[minposition]) ) {
                minposition++;
            }
            min = U[minposition];
            maxposition = U.length - 1;

            delta = (max - min) / (N - 1);

            out.println("The minimum value is  " + min);
            out.println("The maximum value is " + max);
            out.println("Delta is " + delta);

            int index = 0;

            count1 = minposition; // the novalues are already left aside
            count = minposition;
            bin_vuoti = 0;

            while( count < maxposition ) {

                if (U[count] < min + 0.5 * delta) {
                    while( U[count] < min + 0.5 * delta && count < maxposition ) {
                        count++;
                    }

                    bins[index] = count - count1; // number of values
                    // contained in the bin
                    index++; // starts from position 1!!!

                    head++;
                    count1 = count;
                    count++;

                } else {
                    bin_vuoti++;
                }
                min += delta;
            }

            if (bin_vuoti != 0) {
                out.println(bin_vuoti + " empty bins where found");
            }

        }

        if (head < 1) {
            out.println("Something wrong happened in binning");
        } else {
            theSplit.initIndex(head);

            int maxnumberinbin = 0;
            for( i = 0; i < head; i++ ) {
                theSplit.splittedindex[i] = bins[i];
                if (bins[i] > maxnumberinbin)
                    maxnumberinbin = bins[i];
            }

            /*
             * now a list of the values inside the bins are put into the matrixes, therefore we need
             * as many rows as bins and a column number high enough to hold the major number of
             * values hold inside a bin.
             */
            theSplit.initValues(head, maxnumberinbin);

            int index = minposition;
            for( int j = 0; j < head; j++ ) {
                for( int k = 0; k < theSplit.splittedindex[j]; k++ ) {
                    theSplit.splittedvalues1[j][k] = U[index];
                    theSplit.splittedvalues2[j][k] = T[index];
                    index++;
                }
            }
        }

        if (N < 2)
            delta = 0;

        return delta;

    }

    
    /**
     *Calculates the statistical moments.
     *
     * @param m the vector of value to calculate the moment.
     * @param nh the maximum index of the vector, it estimate the moment while the index is nh.
     * @param mean the mean of the vector m if the moment is greater than 1.
     * @param NN the order of the moment to calculate.
     * @param out
     * @return
     */
    public static double doubleNMoment( double[] m, int nh, double mean, double NN, PrintStream out ) {
        double moment = 0.0, n;

        n = 0;

        if (NN == 1.0) {

            for( int i = 0; i < nh; i++ ) {

                if (!isNovalue(m[i])) {
                    moment += m[i];
                    n++;
                }

            }

            if (n >= 1) {
                moment /= n;
            } else {
                out.println("No valid data were processed, setting moment value to zero.");
                moment = 0.0;
            }

        } else if (NN == 2.0) {
            for( int i = 0; i < nh; i++ ) {
                if (!isNovalue(m[i])) {
                    moment += (m[i]) * (m[i]);
                    n++;
                }

            }
            if (n >= 1) {
                moment = (moment / n - mean * mean);
            } else {
                out.println("No valid data were processed, setting moment value to zero.");
                moment = 0.0;
            }

        } else {
            for( int i = 0; i < nh; i++ ) {
                if (!isNovalue(m[i])) {
                    moment += pow((m[i] - mean), NN);
                    n++;
                }

            }
            if (n >= 1) {
                moment /= n;

            } else {
                out.println("No valid data were processed, setting moment value to zero.");
                moment = 0.0;
            }

        }

        return moment;
    }

    public static double log10( double value ) {
        return (log(value) / log(10.0));
    }

    /**
     * Creates a {@link DiskMemImage} from a {@link RenderedImage}.
     * 
     * @param image
     * @return
     */
    public static WritableRaster createFromRenderedImage( RenderedImage image ) {

        int width = image.getWidth();
        int height = image.getHeight();
        WritableRaster writableRaster = createDoubleWritableRaster(width, height, null, null, null);

        WritableRandomIter disckRandomIter = RandomIterFactory.createWritable(writableRaster, null);
        RandomIter imageRandomIter = RandomIterFactory.create(image, null);

        for( int x = 0; x < width; x++ ) {
            for( int y = 0; y < height; y++ ) {
                double value = imageRandomIter.getSampleDouble(x, y, 0);
                disckRandomIter.setSample(x, y, 0, value);
            }
        }

        disckRandomIter.done();
        imageRandomIter.done();
        return writableRaster;
    }

    public static WritableRaster createFromRenderedImageWithNovalueBorder( RenderedImage image ) {

        int width = image.getWidth();
        int height = image.getHeight();
        WritableRaster writableRaster = createDoubleWritableRaster(width, height, null, null, null);

        WritableRandomIter disckRandomIter = RandomIterFactory.createWritable(writableRaster, null);
        RandomIter imageRandomIter = RandomIterFactory.create(image, null);

        for( int x = 0; x < width; x++ ) {
            for( int y = 0; y < height; y++ ) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    disckRandomIter.setSample(x, y, 0, doubleNovalue);
                } else {
                    disckRandomIter.setSample(x, y, 0, imageRandomIter.getSampleDouble(x, y, 0));
                }

            }
        }
        disckRandomIter.done();
        imageRandomIter.done();
        return writableRaster;
    }

    public static WritableRaster createFromRaster( Raster image ) {

        int width = image.getWidth();
        int height = image.getHeight();
        WritableRaster writableRaster = createDoubleWritableRaster(width, height, null, null, null);

        WritableRandomIter disckRandomIter = RandomIterFactory.createWritable(writableRaster, null);
        RandomIter imageRandomIter = RandomIterFactory.create(image, null);

        for( int x = 0; x < width; x++ ) {
            for( int y = 0; y < height; y++ ) {
                disckRandomIter.setSample(x, y, 0, imageRandomIter.getSampleDouble(x, y, 0));
            }
        }
        disckRandomIter.done();
        imageRandomIter.done();
        return writableRaster;
    }

    public static WritableRaster createFromMatrix( double[][] matrix ) {
        int height = matrix.length;
        int width = matrix[0].length;
        WritableRaster writableRaster = createDoubleWritableRaster(width, height, null, null, null);

        WritableRandomIter disckRandomIter = RandomIterFactory.createWritable(writableRaster, null);

        for( int x = 0; x < width; x++ ) {
            for( int y = 0; y < height; y++ ) {
                disckRandomIter.setSample(x, y, 0, matrix[y][x]);
            }
        }
        disckRandomIter.done();

        return writableRaster;
    }

    /**
     * Interpolates the width function in a given tp.
     * 
     * @param data
     * @param tp
     * @return
     */
    public static double henderson( double[][] data, int tp ) {

        int rows = data.length;

        int j = 1, n = 0;
        double dt = 0, muno, mdue, a, b, x, y, ydue, s_uno, s_due, smax = 0, tstar;

        for( int i = 1; i < rows; i++ ) {

            if (data[i][0] + tp <= data[(rows - 1)][0]) {
                /**
                 * ***trovo parametri geometrici del segmento di retta y=muno x+a******
                 */

                muno = (data[i][1] - data[(i - 1)][1]) / (data[i][0] - data[(i - 1)][0]);
                a = data[i][1] - (data[i][0] + tp) * muno;

                /**
                 * ***trovo i valori di x per l'intersezione tra y=(muno x+tp)+a e y=mdue x+b ******
                 */
                for( j = 1; j <= (rows - 1); j++ ) {
                    mdue = (data[j][1] - data[(j - 1)][1]) / (data[j][0] - data[(j - 1)][0]);

                    b = data[j][1] - data[j][0] * mdue;
                    x = (a - b) / (mdue - muno);
                    y = muno * x + a;
                    if (x >= data[(j - 1)][0] && x <= data[j][0] && x - tp >= data[(i - 1)][0]
                            && x - tp <= data[i][0]) {

                        ydue = width_interpolate(data, x - tp, 0, 1);
                        n++;

                        s_uno = width_interpolate(data, x - tp, 0, 2);

                        s_due = width_interpolate(data, x, 0, 2);

                        if (s_due - s_uno > smax) {
                            smax = s_due - s_uno;
                            dt = x - tp;
                            tstar = x;

                        }
                    }
                }

            }
        }
        return dt;

    }

    /**
     * linear interpolation between two values
     * 
     * @param data - matrix of values to interpolate
     * @param x - value to interpolate
     * @param nx - column of data in which you find the x values
     * @param ny - column of data in which you find the y values
     * @return
     */
    public static double width_interpolate( double[][] data, double x, int nx, int ny ) {

        int rows = data.length;
        double xuno = 0, xdue = 0, yuno = 0, ydue = 0, y = 0;

        // if 0, interpolate between 0 and the first value of data
        if (x >= 0 && x < data[0][nx]) {
            xuno = 0;
            xdue = data[0][nx];
            yuno = 0;
            ydue = data[0][ny];
            y = ((ydue - yuno) / (xdue - xuno)) * (x - xuno) + yuno;
        }

        // if it is less than 0 and bigger than the maximum, throw error
        if (x > data[(rows - 1)][nx] || x < 0) {
            throw new RuntimeException("Error in the interpolation algorithm");
        }

        /* trovo i valori limite entro i quali effettuo l'interpolazione lineare */
        for( int i = 0; i < rows - 1; i++ ) {

            if (x > data[i][nx] && x <= data[(i + 1)][nx]) {
                xuno = data[i][nx];
                xdue = data[(i + 1)][nx];
                yuno = data[i][ny];
                ydue = data[(i + 1)][ny];
                y = ((ydue - yuno) / (xdue - xuno)) * (x - xuno) + yuno;

            }
        }

        return y;
    }

    /**
     * TODO
     * 
     * @param flowImage
     * @param att
     * @param dist
     */
    public static WritableRaster go2channel( RandomIter mRandomIter, RandomIter attRandomIter,
            int width, int height, PrintStream out ) {
        int[] flow = new int[2];
        double value = 0.0;
        int maxRows = height;
        int maxCols = width;

        WritableRaster dist = createDoubleWritableRaster(maxCols, maxRows, null, null, null);

        WritableRandomIter distRandomIter = RandomIterFactory.createWritable(dist, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating the distance along the flowstream...", maxRows - 2);
        for( int j = 1; j < maxRows - 1; j++ ) {
            for( int i = 1; i < maxCols - 1; i++ ) {
                flow[0] = i;
                flow[1] = j;

                // Rectangle aroundSample = new Rectangle(i - 1, j - 1, 3, 3);
                // Raster aroundRaster = flowImage.getData(aroundSample);

                if (!isNovalue(mRandomIter.getSampleDouble(flow[0], flow[1], 0))
                        && FluidUtils.sourcesqJAI(mRandomIter, flow)) {
                    while( !isNovalue(mRandomIter.getSampleDouble(flow[0], flow[1], 0))
                            && mRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0 ) {
                        if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return null;
                    }

                    if (isNovalue(mRandomIter.getSampleDouble(flow[0], flow[1], 0))) {
                        out.println("Not proper outlets in flow file");
                    } else if (mRandomIter.getSampleDouble(flow[0], flow[1], 0) == 10) {
                        value = attRandomIter.getSampleDouble(flow[0], flow[1], 0);
                    }
                    flow[0] = i;
                    flow[1] = j;
                    distRandomIter.setSample(i, j, 0, value);
                    while( !isNovalue(mRandomIter.getSampleDouble(flow[0], flow[1], 0))
                            && mRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0 ) {
                        distRandomIter.setSample(flow[0], flow[1], 0, value);
                        if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return null;
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        return dist;
    }

    /**
     * this method numerating every stream
     */
    public static WritableRaster netNumbering( List<Integer> nstream, RandomIter mRandomIter,
            RandomIter netRandomIter, int width, int height, PrintStream out ) {
        int[] flow = new int[2];
        int gg = 0, n = 0, f;
        WritableRaster outImage = FluidUtils.createDoubleWritableRaster(width, height, null, null,
                null);

        WritableRandomIter oMatrixRandomIter = RandomIterFactory.createWritable(outImage, null);

        int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING;

        /* numerating every stream */
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(NUMBERING_STREAMS + "...", height);
        for( int j = 0; j < height; j++ ) {
            for( int i = 0; i < width; i++ ) {
                flow[0] = i;
                flow[1] = j;
                if (!isNovalue(netRandomIter.getSampleDouble(i, j, 0))
                        && mRandomIter.getSampleDouble(i, j, 0) != 10.0
                        && oMatrixRandomIter.getSampleDouble(i, j, 0) == 0.0) {
                    f = 0;
                    for( int k = 1; k <= 8; k++ ) {
                        if (mRandomIter
                                .getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]
                                && !isNovalue(netRandomIter.getSampleDouble(flow[0] + dir[k][0],
                                        flow[1] + dir[k][1], 0))) {
                            break;
                        } else
                            f++;
                    }
                    // if the pixel is a source...
                    if (f == 8) {
                        n++;
                        nstream.add(n);
                        oMatrixRandomIter.setSample(i, j, 0, n);
                        if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return null;
                        while( !isNovalue(mRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && oMatrixRandomIter.getSampleDouble(flow[0], flow[1], 0) == 0 ) {
                            gg = 0;
                            for( int k = 1; k <= 8; k++ ) {
                                if (!isNovalue(netRandomIter.getSampleDouble(flow[0] + dir[k][0],
                                        flow[1] + dir[k][1], 0))
                                        && mRandomIter.getSampleDouble(flow[0] + dir[k][0], flow[1]
                                                + dir[k][1], 0) == dir[k][2]) {
                                    gg++;
                                }
                            }
                            if (gg >= 2) {
                                n++;
                                nstream.add(n);
                                oMatrixRandomIter.setSample(flow[0], flow[1], 0, n);
                            } else {
                                oMatrixRandomIter.setSample(flow[0], flow[1], 0, n);
                            }
                            if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(
                                    flow[0], flow[1], 0)))
                                return null;
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        return outImage;
    }

    /**
     * this method numerating every stream and subdivide the stream when tca is greater than a
     * threshold
     */
    public static WritableRaster netNumberingWithTca( List<Integer> nstream,
            RandomIter mRandomIter, RandomIter netRandomIter, RandomIter tcaRandomIter, int cols,
            int rows, double tcaTh, PrintStream out ) {
        int[] flow = new int[2];
        int gg = 0, n = 0, f;

        WritableRaster outImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null,
                null);
        WritableRandomIter oMatrixRandomIter = RandomIterFactory.createWritable(outImage, null);

        int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING;

        double tcaValue = 0;

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(NUMBERING_STREAMS + "with tca...", rows);
        /* numerating every stream */
        for( int j = 0; j < rows; j++ ) {
            // ShowPercent.getPercent(copt, i, rows - 1, 1);
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                if (!isNovalue(netRandomIter.getSampleDouble(i, j, 0))
                        && mRandomIter.getSampleDouble(i, j, 0) != 10.0
                        && oMatrixRandomIter.getSampleDouble(i, j, 0) == 0.0) {
                    f = 0;
                    for( int k = 1; k <= 8; k++ ) {
                        if (mRandomIter
                                .getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]
                                && !isNovalue(netRandomIter.getSampleDouble(flow[0] + dir[k][0],
                                        flow[1] + dir[k][1], 0))) {
                            break;
                        } else
                            f++;
                    }
                    // if the pixel is a source...
                    if (f == 8) {
                        n++;
                        nstream.add(n);
                        tcaValue = tcaRandomIter.getSampleDouble(i, j, 0);
                        oMatrixRandomIter.setSample(i, j, 0, n);
                        if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return null;
                        while( !isNovalue(mRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && oMatrixRandomIter.getSampleDouble(flow[0], flow[1], 0) == 0 ) {
                            gg = 0;
                            for( int k = 1; k <= 8; k++ ) {
                                if (!isNovalue(netRandomIter.getSampleDouble(flow[0] + dir[k][0],
                                        flow[1] + dir[k][1], 0))
                                        && mRandomIter.getSampleDouble(flow[0] + dir[k][0], flow[1]
                                                + dir[k][1], 0) == dir[k][2]) {
                                    gg++;
                                }
                            }
                            if (gg >= 2) {
                                // it is a node
                                n++;
                                nstream.add(n);
                                oMatrixRandomIter.setSample(flow[0], flow[1], 0, n);
                                tcaValue = tcaRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            } else if (tcaRandomIter.getSampleDouble(flow[0], flow[1], 0)
                                    - tcaValue > tcaTh) {
                                // tca greater than threshold
                                n++;
                                nstream.add(n);
                                oMatrixRandomIter.setSample(flow[0], flow[1], 0, n);
                                tcaValue = tcaRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            } else {
                                // normal point
                                oMatrixRandomIter.setSample(flow[0], flow[1], 0, n);
                            }
                            if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(
                                    flow[0], flow[1], 0)))
                                return null;
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        return outImage;
    }

    /**
     * this method numerating every stream dividing the channels in fixed points
     */
    public static WritableRaster netNumberingWithPoints( List<Integer> nstream,
            RandomIter mRandomIter, RandomIter netRandomIter, JGrassRegion active,
            List<HashMap<String, ? >> attributePoints, List<Geometry> geomVect, PrintStream err,
            PrintStream out ) {
        int[] flow = new int[2];
        int cols = active.getCols();
        int rows = active.getRows();
        int gg = 0, n = 0, f;
        WritableRaster outImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null,
                null);
        WritableRandomIter oMatrixRandomIter = RandomIterFactory.createWritable(outImage, null);

        int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING;

        List<Point4d> points = new ArrayList<Point4d>();
        // new rectangle for active region
        Rectangle2D regionBox = active.getRectangle();
        Number nodoId;
        int l = 0;
        int numGeometry = 0;
        // insert the points in a Vector of points
        for( Geometry pointV : geomVect ) {
            for( int i = 0; i < pointV.getNumGeometries(); i++ ) {
                int[] clickedRowCol = RasterUtilities.putClickToCenterOfCell(active,
                        new Point2D.Double(pointV.getCoordinates()[0].x,
                                pointV.getCoordinates()[0].y));
                nodoId = (Number) attributePoints.get(numGeometry).get("RETE_ID");
                if (nodoId == null) {
                    err.println("RETE_ID not found");
                    return null;
                }
                if (nodoId.intValue() != -1
                        && regionBox.contains(new Point2D.Double(pointV.getCoordinates()[0].x,
                                pointV.getCoordinates()[0].y))) {
                    points.add(new Point4d(clickedRowCol[1], clickedRowCol[0],
                            nodoId.doubleValue(), 0));
                    l++;
                }
            }
            numGeometry++;
        }
        // if the points isn't on the channel net, move the point
        int p = 0;
        for( Point4d point4d : points ) {
            if (netRandomIter.getSampleDouble((int) point4d.x, (int) point4d.y, 0) != point4d.z) {
                for( int i = 1; i < 9; i++ ) {
                    int indexI = (int) point4d.x + dir[i][0];
                    int indexJ = (int) point4d.y + dir[i][1];
                    if (netRandomIter.getSampleDouble(indexI, indexJ, 0) == point4d.z) {
                        point4d.x = indexI;
                        point4d.y = indexJ;
                    }
                }
            }
        }
        for( Point4d point4d : points ) {
            if (netRandomIter.getSampleDouble((int) point4d.y, (int) point4d.x, 0) == point4d.z) {
                p++;
            }
        }
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(NUMBERING_STREAMS + "with points...", rows);
        /* Selects every node and go downstream */
        for( int j = 0; j < rows; j++ ) {
            // ShowPercent.getPercent(copt, i, rows - 1, 1);
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                if (!isNovalue(netRandomIter.getSampleDouble(i, j, 0))
                        && mRandomIter.getSampleDouble(i, j, 0) != 10.0
                        && oMatrixRandomIter.getSampleDouble(i, j, 0) == 0.0) {
                    f = 0;
                    // look for the source...
                    for( int k = 1; k <= 8; k++ ) {
                        if (mRandomIter
                                .getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]
                                && !isNovalue(netRandomIter.getSampleDouble(flow[0] + dir[k][0],
                                        flow[1] + dir[k][1], 0))) {
                            break;
                        } else
                            f++;
                    }
                    // if the pixel is a source...starts to assigne a number to
                    // every stream
                    if (f == 8) {
                        n++;
                        nstream.add(n);
                        oMatrixRandomIter.setSample(i, j, 0, n);
                        if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return null;
                        for( Point4d point4d : points ) {
                            if (point4d.x == flow[1] && point4d.y == flow[0]) {
                                n++;
                                nstream.add(n);
                                point4d.w = n - 1;
                                /*
                                 * omatrix.getSampleDouble(i,j) = n; if
                                 * (!FluidUtils.go_downstream(flow,
                                 * m.getSampleDouble(flow[0],flow[1]), copt)) ;
                                 */
                            }
                        }
                        while( !isNovalue(mRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && oMatrixRandomIter.getSampleDouble(flow[0], flow[1], 0) == 0
                                && mRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10 ) {
                            gg = 0;
                            for( int k = 1; k <= 8; k++ ) {
                                if (!isNovalue(netRandomIter.getSampleDouble(flow[0] + dir[k][0],
                                        flow[1] + dir[k][1], 0))
                                        && mRandomIter.getSampleDouble(flow[0] + dir[k][0], flow[1]
                                                + dir[k][1], 0) == dir[k][2]) {
                                    gg++;
                                }
                            }
                            if (gg >= 2) {
                                n++;
                                nstream.add(n);
                                oMatrixRandomIter.setSample(flow[0], flow[1], 0, n);
                            } else {
                                oMatrixRandomIter.setSample(flow[0], flow[1], 0, n);
                            }
                            if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(
                                    flow[0], flow[1], 0)))
                                return null;
                            for( Point4d point4d : points ) {
                                if (point4d.x == flow[1] && point4d.y == flow[0]) {
                                    n++;
                                    nstream.add(n);
                                    point4d.w = n - 1;
                                }
                            }
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();
        return outImage;
    }

    /**
     * this method numerating every stream dividing the channels in fixed points
     * 
     * @param out
     */
    @SuppressWarnings("unchecked")
    public static WritableRaster netNumberingWithPointsAndTca( List<Integer> nstream,
            RandomIter mRandomIter, RandomIter netRandomIter, RandomIter tcaRandomIter,
            double tcaTh, JGrassRegion active, List<HashMap<String, ? >> attributePoints,
            List<Geometry> geomVect, PrintStream err, PrintStream out ) {
        int[] flow = new int[2];
        int cols = active.getCols();
        int rows = active.getRows();
        int gg = 0, n = 0, f;

        int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING;

        double tcaValue = 0;

        List<Point4d> points = new ArrayList<Point4d>();
        // new rectangle for active region
        Rectangle2D regionBox = active.getRectangle();
        Number nodoId;
        int l = 0;
        int numGeometry = 0;
        // insert the points in a Vector of points
        for( Geometry pointV : geomVect ) {
            for( int i = 0; i < pointV.getNumGeometries(); i++ ) {
                int[] clickedRowCol = RasterUtilities.putClickToCenterOfCell(active,
                        new Point2D.Double(pointV.getCoordinates()[0].x,
                                pointV.getCoordinates()[0].y));
                nodoId = (Number) attributePoints.get(numGeometry).get("RETE_ID");
                if (nodoId == null) {
                    err.println("RETE_ID not found");
                    return null;
                }
                if (nodoId.intValue() != -1
                        && regionBox.contains(new Point2D.Double(pointV.getCoordinates()[0].x,
                                pointV.getCoordinates()[0].y))) {
                    points.add(new Point4d(clickedRowCol[1], clickedRowCol[0],
                            nodoId.doubleValue(), 0));
                    l++;
                }
            }
            numGeometry++;
        }
        // if the points isn't on the channel net, move the point
        int p = 0;

        WritableRaster outImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null,
                null);
        WritableRandomIter oRandomIter = RandomIterFactory.createWritable(outImage, null);

        for( Point4d point4d : points ) {
            if (netRandomIter.getSampleDouble((int) point4d.x, (int) point4d.y, 0) != point4d.z) {
                for( int i = 1; i < 9; i++ ) {
                    int indexI = (int) point4d.x + dir[i][1];
                    int indexJ = (int) point4d.y + dir[i][0];
                    if (netRandomIter.getSampleDouble(indexJ, indexI, 0) == point4d.z) {
                        point4d.x = indexI;
                        point4d.y = indexJ;
                    }
                }
            }
        }
        for( Point4d point4d : points ) {
            if (netRandomIter.getSampleDouble((int) point4d.y, (int) point4d.x, 0) == point4d.z) {
                p++;
            }
        }

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(NUMBERING_STREAMS + "with tca...", rows);
        /* Selects every node and go downstream */
        for( int j = 0; j < rows; j++ ) {
            // ShowPercent.getPercent(copt, i, rows - 1, 1);
            for( int i = 0; i < cols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                if (!isNovalue(netRandomIter.getSampleDouble(i, j, 0))
                        && mRandomIter.getSampleDouble(i, j, 0) != 10.0
                        && oRandomIter.getSampleDouble(i, j, 0) == 0.0) {
                    f = 0;
                    // look for the source...
                    for( int k = 1; k <= 8; k++ ) {
                        if (mRandomIter
                                .getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1], 0) == dir[k][2]
                                && !isNovalue(netRandomIter.getSampleDouble(flow[0] + dir[k][0],
                                        flow[1] + dir[k][1], 0))) {
                            break;
                        } else
                            f++;
                    }
                    // if the pixel is a source...starts to assigne a number to
                    // every stream
                    if (f == 8) {
                        n++;
                        nstream.add(n);
                        oRandomIter.setSample(i, j, 0, n);
                        if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return null;
                        for( Point4d point4d : points ) {
                            if (point4d.x == flow[1] && point4d.y == flow[0]) {
                                n++;
                                nstream.add(n);
                                point4d.w = n - 1;
                                /*
                                 * omatrix.getValueAt(i,j) = n; if (!FluidUtils.go_downstream(flow,
                                 * m.getValueAt(flow[0],flow[1]), copt)) ;
                                 */
                            }
                        }
                        while( !isNovalue(mRandomIter.getSampleDouble(flow[0], flow[1], 0))
                                && oRandomIter.getSampleDouble(flow[0], flow[1], 0) == 0
                                && mRandomIter.getSampleDouble(flow[0], flow[1], 0) != 10 ) {
                            gg = 0;
                            for( int k = 1; k <= 8; k++ ) {
                                if (!isNovalue(netRandomIter.getSampleDouble(flow[0] + dir[k][0],
                                        flow[1] + dir[k][1], 0))
                                        && mRandomIter.getSampleDouble(flow[0] + dir[k][0], flow[1]
                                                + dir[k][1], 0) == dir[k][2]) {
                                    gg++;
                                }
                            }
                            if (gg >= 2) {
                                // it is a node
                                n++;
                                oRandomIter.setSample(flow[0], flow[1], 0, n);
                                tcaValue = tcaRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            } else if (tcaRandomIter.getSampleDouble(flow[0], flow[1], 0)
                                    - tcaValue > tcaTh) {
                                // tca greater than threshold
                                n++;
                                nstream.add(n);
                                oRandomIter.setSample(flow[0], flow[1], 0, n);
                                tcaValue = tcaRandomIter.getSampleDouble(flow[0], flow[1], 0);
                            } else {
                                // normal point
                                oRandomIter.setSample(flow[0], flow[1], 0, n);
                            }
                            if (!FluidUtils.go_downstream(flow, mRandomIter.getSampleDouble(
                                    flow[0], flow[1], 0)))
                                return null;
                            for( Point4d point4d : points ) {
                                if (point4d.x == flow[1] && point4d.y == flow[0]) {
                                    n++;
                                    nstream.add(n);
                                    point4d.w = n - 1;
                                }
                            }
                        }
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();

        oRandomIter.done();
        tcaRandomIter.done();
        mRandomIter.done();
        netRandomIter.done();
        return outImage;
    }

    /**
     * Extract subbasin of a raster.
     * 
     * @param flowRandomIter usually is the flow map.
     * @param cols
     * @param rows
     * @param net the network map.
     * @param netNumber the netnumbering map.
     * @return the map of subbasin
     */
    public static WritableRaster extractSubbasins( WritableRandomIter flowRandomIter,
            RandomIter netRandomIter, WritableRandomIter netNumberRandomIter, int rows, int cols,
            PrintStream out ) {

        for( int l = 0; l < rows; l++ ) {
            for( int k = 0; k < cols; k++ ) {
                if (!isNovalue(netRandomIter.getSampleDouble(k, l, 0)))
                    flowRandomIter.setSample(k, l, 0, 10);
            }
        }

        WritableRaster subbImage = FluidUtils.go2channel(flowRandomIter, netNumberRandomIter, cols,
                rows, out);

        WritableRandomIter subbRandomIter = RandomIterFactory.createWritable(subbImage, null);

        for( int l = 0; l < rows; l++ ) {
            for( int k = 0; k < cols; k++ ) {
                if (!isNovalue(netRandomIter.getSampleDouble(k, l, 0)))
                    subbRandomIter.setSample(k, l, 0, netNumberRandomIter.getSampleDouble(k, l, 0));
                if (netNumberRandomIter.getSampleDouble(k, l, 0) == 0)
                    netNumberRandomIter.setSample(k, l, 0, doubleNovalue);
                if (subbRandomIter.getSampleDouble(k, l, 0) == 0)
                    subbRandomIter.setSample(k, l, 0, doubleNovalue);
            }
        }

        return subbImage;
    }

    /**
     * Returns the flow direction value for a given point as indexes (i, j) of the dir matrix.
     * 
     * @param i is the cols difference (dx);
     * @param j is the rows difference (dy);
     * @return
     */
    public static int getFlowDirection( int i, int j ) {
        int flow = -1;
        for( int k = 1; k < 9; k++ ) {
            if (dirOut[k][0] == i && dirOut[k][1] == j) {
                flow = k;
            }
        }
        return flow;
    }

    /**
     * The Gamma function.
     * 
     * @param x
     * @return the calculated gamma function.
     */
    public static double gamma( double x ) {
        double tmp = (x - 0.5) * log(x + 4.5) - (x + 4.5);
        double ser = 1.0 + 76.18009173 / (x + 0) - 86.50532033 / (x + 1) + 24.01409822 / (x + 2)
                - 1.231739516 / (x + 3) + 0.00120858003 / (x + 4) - 0.00000536382 / (x + 5);
        double gamma = exp(tmp + log(ser * sqrt(2 * PI)));
        return gamma;
    }

}
