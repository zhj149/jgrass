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
package eu.hydrologis.jgrass.models.h.eicalculator;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.geotools.coverage.grid.GridCoverage2D;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.utils.FluidConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.StringSet;

/**
 * @author Stefano Endrizzi
 */
public class EnergyIndexCalculator {

    private static final double NOVALUE = JGrassConstants.intNovalue;

    private HashMap<Integer, Integer> id2indexMap = null;
    private HashMap<Integer, Integer> index2idMap = null;

    private final int numEs;
    private final int numEi;
    private final double dtData;

    private double latitude;
    private final PrintStream out;
    private double dx;
    private double dy;
    private int[][] eibasinID;
    private int[][] outputShadow;
    private double[][][] eibasinEmonth;
    private double[][] eibasinESrange;
    private double[][] eibasinES;
    private double[][] eibasinEI_mean;
    private double[][][] eibasinEI;
    private double[][] eibasinE;
    private double[][][] eibasinA;
    private final GeomorphUtilities geomorphUtilities = new GeomorphUtilities();
    private int eibasinNum;

    private RandomIter idbasinImageIterator;

    private RandomIter elevImageIterator;

    private WritableRaster curvatureImage;

    private RandomIter aspectImageIterator;

    private RandomIter slopeImageIterator;

    private int rows;

    private int cols;

    /**
     * Calculates the Energy Indexes.
     * 
     * @param numEs
     * @param numEi
     * @param dtData temporal step in hours for the integration of the radiation.
     * @param latitude latitude in degrees.
     * @param activeRegion the {@link JGrassRegion active region} on which calculus occurs.
     * @param idbasinRasterData basins ids map.
     * @param elevRasterData elevation model data.
     * @param curvRasterData curvature map 
     * @param aspRasterData aspect map in radiants.
     * @param slopeRasterData slope map in radiants.
     * @param out the output stream for user feedback.
     */
    public EnergyIndexCalculator( int numEs, int numEi, double dtData, double latitude, JGrassRegion activeRegion,
            GridCoverage2D idbasinRasterData, GridCoverage2D elevRasterData, GridCoverage2D curvRasterData,
            GridCoverage2D aspRasterData, GridCoverage2D slopeRasterData, PrintStream out ) {
        this.numEs = numEs;
        this.numEi = numEi;
        this.dtData = dtData;
        this.latitude = latitude;
        this.out = out;

        RenderedImage idbasinImage = idbasinRasterData.getRenderedImage();
        idbasinImageIterator = RandomIterFactory.create(idbasinImage, null);

        RenderedImage elevImage = elevRasterData.getRenderedImage();
        elevImageIterator = RandomIterFactory.create(elevImage, null);

        RenderedImage tmpImage = curvRasterData.getRenderedImage();
        curvatureImage = FluidUtils.createDoubleWritableRaster(tmpImage.getWidth(), tmpImage.getHeight(), null, null, null);
        RandomIter tmpIterator = RandomIterFactory.create(tmpImage, null);

        for( int i = 0; i < tmpImage.getHeight(); i++ ) {
            for( int j = 0; j < tmpImage.getWidth(); j++ ) {
                curvatureImage.setSample(j, i, 0, tmpIterator.getSampleDouble(j, i, 0));
            }
        }

        RenderedImage aspectImage = aspRasterData.getRenderedImage();
        aspectImageIterator = RandomIterFactory.create(aspectImage, null);

        RenderedImage slopeImage = slopeRasterData.getRenderedImage();
        slopeImageIterator = RandomIterFactory.create(slopeImage, null);

        dx = activeRegion.getWEResolution();
        dy = activeRegion.getNSResolution();
        rows = activeRegion.getRows();
        cols = activeRegion.getCols();

    }

    public void execute() {
        latitude *= (PI / 180.0);

        out.println("Preparing inputs...");
        eibasinNum = prepareInputsOutputs();

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Compute EI...", 6);
        for( int m = 0; m < 6; m++ ) {
            pm.worked(1);
            compute_EI(m + 1);
        }
        pm.done();

        average_EI(10, 6);

        pm.beginTask("Calculate areas...", eibasinNum);
        for( int i = 0; i < eibasinNum; i++ ) {
            pm.worked(1);
            area(i);
        }
        pm.done();
    }

    /**
     * Filles the {@link StringSet}s for atimetry, energy and area in sql format.
     * 
     * <p>
     * This has to be called after the {@link EnergyIndexCalculator#execute()}
     * method.
     * </p>
     * 
     * @param altimetricSet the {@link StringSet} to be filled with the altimetric
     *                                  bands values.
     * @param energeticSet {@link StringSet} to be filled with the energetic
     *                                  bands values.
     * @param areaSet {@link StringSet} to be filled with the area 
     *                                  bands values for altimetric and energetic cells.
     */
    public void fillSqlResults( StringSet altimetricSet, StringSet energeticSet, StringSet areaSet ) {
        energeticSet.add("\n-- ENERGY BANDS: \nbasinid,bandid,virtualmonth,energyvalue\n");
        energeticSet.add("-- -------------------------------------------\n");

        altimetricSet.add("\n-- ALTIMETRIC BANDS: \nbasinid,bandid,elevationbaricenter,range\n");
        altimetricSet.add("-- -------------------------------------------\n");

        areaSet.add("\n-- AREA ENERGY-ALTIMETRIC BANDS: \nbasinid,altimetricid,energeticid,areavalue\n");
        areaSet.add("-- -------------------------------------------\n");

        for( int i = 0; i < eibasinNum; i++ ) {
            int realBasinId = index2idMap.get(i + 1);
            /*
             * ENERGY BANDS
             * 
             * Cycle over the virtual months:
             * 0: 22 DICEMBRE - 20 GENNAIO
             * 1: 21 GENNAIO - 20 FEBBRAIO
             * 2: 21 FEBBRAIO - 22 MARZO
             * 3: 23 MARZO - 22 APRILE
             * 4: 23 APRILE - 22 MAGGIO
             * 5: 23 MAGGIO - 22 GIUGNO
             */
            for( int j = 0; j < 6; j++ ) {
                for( int k = 0; k < numEi; k++ ) {
                    StringBuilder sqlBuilder = new StringBuilder();
                    sqlBuilder.append("INSERT INTO ENERGYTABLEPLACEHOLDER VALUES {");
                    sqlBuilder.append(realBasinId);
                    sqlBuilder.append(",");
                    sqlBuilder.append(k);
                    sqlBuilder.append(",");
                    sqlBuilder.append(j);
                    sqlBuilder.append(",");
                    sqlBuilder.append(eibasinEI[0][k][i]);
                    sqlBuilder.append("};\n");
                    // the basin id, band id, first virtual month, energy value
                    energeticSet.add(sqlBuilder.toString());
                }
            }

            /*
             * ALTIMETRIC BANDS
             */
            for( int k = 0; k < numEs; k++ ) {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("INSERT INTO ALTIMETRICTABLEPLACEHOLDER VALUES {");
                sqlBuilder.append(realBasinId);
                sqlBuilder.append(",");
                sqlBuilder.append(k);
                sqlBuilder.append(",");
                sqlBuilder.append(eibasinES[k][i]);
                sqlBuilder.append(",");
                sqlBuilder.append(eibasinESrange[k][i]);
                sqlBuilder.append(" };\n");
                // the basin id, band id, elevation, range
                altimetricSet.add(sqlBuilder.toString());
            }

            /*
             * AREAS
             */
            for( int j = 0; j < numEs; j++ ) {
                for( int k = 0; k < numEi; k++ ) {
                    StringBuilder sqlBuilder = new StringBuilder();
                    sqlBuilder.append("INSERT INTO AREATABLEPLACEHOLDER VALUES {");
                    sqlBuilder.append(realBasinId);
                    sqlBuilder.append(",");
                    sqlBuilder.append(j);
                    sqlBuilder.append(",");
                    sqlBuilder.append(k);
                    sqlBuilder.append(",");
                    sqlBuilder.append(eibasinA[j][k][i]);
                    sqlBuilder.append(" };\n");
                    // the basin id, altimetric id, energetic id, area value
                    areaSet.add(sqlBuilder.toString());
                }
            }

        }
    }

    /**
     * Filles the {@link ScalarSet}s for atimetry, energy and area.
     * 
     * <p>
     * This has to be called after the {@link EnergyIndexCalculator#execute()}
     * method.
     * </p>
     * 
     * @param altimetricSet the {@link ScalarSet} to be filled with the altimetric
     *                                  bands values.
     * @param energeticSet {@link ScalarSet} to be filled with the energetic
     *                                  bands values.
     * @param areaSet {@link ScalarSet} to be filled with the area 
     *                                  bands values for altimetric and energetic cells.
     */
    public void fillScalarValuesResults( ScalarSet altimetricSet, ScalarSet energeticSet, ScalarSet areaSet ) {

        energeticSet.add(4.0);
        altimetricSet.add(4.0);
        areaSet.add(4.0);

        for( int i = 0; i < eibasinNum; i++ ) {
            int realBasinId = index2idMap.get(i + 1);
            /*
             * ENERGY BANDS
             * 
             * Cycle over the virtual months:
             * 0: 22 DICEMBRE - 20 GENNAIO
             * 1: 21 GENNAIO - 20 FEBBRAIO
             * 2: 21 FEBBRAIO - 22 MARZO
             * 3: 23 MARZO - 22 APRILE
             * 4: 23 APRILE - 22 MAGGIO
             * 5: 23 MAGGIO - 22 GIUGNO
             */
            for( int j = 0; j < 6; j++ ) {
                for( int k = 0; k < numEi; k++ ) {
                    // the basin id
                    energeticSet.add((double) realBasinId);
                    // band id
                    energeticSet.add((double) k);
                    // first virtual month:
                    energeticSet.add((double) j);
                    // energy value
                    energeticSet.add(eibasinEI[0][k][i]);
                }
            }

            /*
             * ALTIMETRIC BANDS
             */
            for( int k = 0; k < numEs; k++ ) {
                // the basin id
                altimetricSet.add((double) realBasinId);
                // band id
                altimetricSet.add((double) k);
                // elevation
                altimetricSet.add(eibasinES[k][i]);
                // range
                altimetricSet.add(eibasinESrange[k][i]);
            }

            /*
             * AREAS
             */
            for( int j = 0; j < numEs; j++ ) {
                for( int k = 0; k < numEi; k++ ) {
                    // the basin id
                    areaSet.add((double) realBasinId);
                    // altimetric id
                    areaSet.add((double) j);
                    // energetic id
                    areaSet.add((double) k);
                    // area value
                    areaSet.add(eibasinA[j][k][i]);
                }
            }

        }

    }

    private int prepareInputsOutputs() {

        List<Integer> idList = new ArrayList<Integer>();
        eibasinID = new int[rows][cols];

        for( int r = 0; r < rows; r++ ) {
            for( int c = 0; c < cols; c++ ) {
                // get the value
                if (idbasinImageIterator.getSampleDouble(c, r, 0) <= 0) {
                    eibasinID[r][c] = (int) NOVALUE;
                } else {
                    eibasinID[r][c] = (int) idbasinImageIterator.getSampleDouble(c, r, 0);
                    // put the value in the id list, if it isn't already there
                    if (!idList.contains(eibasinID[r][c])) {
                        idList.add(eibasinID[r][c]);
                    }
                }

            }
        }
        // sort the id list
        Collections.sort(idList);
        /*
         * now the number of involved subbasins is known
         */
        int eibasinNum = idList.size();

        /*
         * now substitute the numbers in the ID matrix with a sequential index without wholes
         */
        // first create the mapping
        id2indexMap = new HashMap<Integer, Integer>();
        index2idMap = new HashMap<Integer, Integer>();
        for( int i = 1; i <= idList.size(); i++ ) {
            id2indexMap.put(idList.get(i - 1), i);
            index2idMap.put(i, idList.get(i - 1));
        }
        for( int r = 0; r < eibasinID.length; r++ ) {
            for( int c = 0; c < eibasinID[0].length; c++ ) {
                if (eibasinID[r][c] != NOVALUE) {
                    eibasinID[r][c] = id2indexMap.get(eibasinID[r][c]);
                }
            }
        }

        out.println("Number of subbasins: " + eibasinNum);

        /*
         * prepare outputs
         */
        outputShadow = new int[rows][cols];
        for( int r = 0; r < outputShadow.length; r++ ) {
            for( int c = 0; c < outputShadow[0].length; c++ ) {
                outputShadow[r][c] = (int) NOVALUE;
            }
        }

        eibasinE = new double[rows][cols];
        for( int r = 0; r < eibasinE.length; r++ ) {
            for( int c = 0; c < eibasinE[0].length; c++ ) {
                eibasinE[r][c] = NOVALUE;
            }
        }

        eibasinEmonth = new double[6][rows][cols];
        for( int r = 0; r < eibasinEmonth.length; r++ ) {
            for( int c = 0; c < eibasinEmonth[0].length; c++ ) {
                for( int t = 0; t < eibasinEmonth[0][0].length; t++ ) {
                    eibasinEmonth[r][c][t] = NOVALUE;
                }
            }
        }

        eibasinES = new double[numEs][eibasinNum];
        eibasinESrange = new double[numEs][eibasinNum];
        eibasinEI_mean = new double[numEi][eibasinNum];

        eibasinEI = new double[6][numEi][eibasinNum];

        eibasinA = new double[numEs][numEi][eibasinNum];

        return eibasinNum;
    }

    private boolean compute_EI( int month ) {

        int[] day_beg = new int[1], day_end = new int[1], daymonth = new int[1], monthyear = new int[1];
        int day;
        double hour;
        double[] Rad_morpho = new double[1], Rad_flat = new double[1];
        double[] E0 = new double[1], alpha = new double[1], direction = new double[1];
        double[][] Rad_morpho_cum, Rad_flat_cum;

        find_days(month, day_beg, day_end);

        hour = 0.5 * dtData;
        day = day_beg[0];

        Rad_morpho_cum = new double[rows][cols];
        Rad_flat_cum = new double[rows][cols];

        get_date(day, monthyear, daymonth);

        if ((hour - (long) hour) * 60 < 10) {
            out.println("giorno: " + daymonth[0] + "/" + monthyear[0] + "  ora: " + hour + ":0" + ((hour - (long) hour) * 60));
        } else {
            out.println("giorno: " + daymonth[0] + "/" + monthyear[0] + "  ora: " + hour + ":" + ((hour - (long) hour) * 60));
        }

        do {
            sun(hour, day, E0, alpha, direction);

            for( int r = 0; r < eibasinID.length; r++ ) {
                for( int c = 0; c < eibasinID[0].length; c++ ) {
                    if (eibasinID[r][c] != (int) NOVALUE) {
                        radiation(Rad_morpho, Rad_flat, E0[0], alpha[0], direction[0], aspectImageIterator.getSampleDouble(c, r,
                                0), slopeImageIterator.getSampleDouble(c, r, 0), outputShadow[r][c]);
                        Rad_morpho_cum[r][c] += Rad_morpho[0];
                        Rad_flat_cum[r][c] += Rad_flat[0];
                    }
                }
            }

            hour += dtData;
            if (hour >= 24) {
                hour -= 24.0;
                day += 1;
            }

            get_date(day, monthyear, daymonth);

            if ((hour - (long) hour) * 60 < 10) {
                out
                        .println("giorno: " + daymonth[0] + "/" + monthyear[0] + "  ora: " + hour + ":0"
                                + ((hour - (long) hour) * 60));
            } else {
                out.println("giorno: " + daymonth[0] + "/" + monthyear[0] + "  ora: " + hour + ":" + ((hour - (long) hour) * 60));
            }

        } while( day <= day_end[0] );

        for( int r = 0; r < eibasinID.length; r++ ) {
            for( int c = 0; c < eibasinID[0].length; c++ ) {
                if (eibasinID[r][c] != (int) NOVALUE) {
                    out.println("Bacino: " + index2idMap.get(eibasinID[r][c]));
                    if (Rad_flat_cum[r][c] == 0) {
                        out.println("Rad flat nulla");
                        Rad_morpho_cum[r][c] = 1;
                        Rad_flat_cum[r][c] = 1;
                    }
                    eibasinEmonth[month - 1][r][c] = Rad_morpho_cum[r][c] / Rad_flat_cum[r][c];
                    out.println("Rad morfo: " + Rad_morpho_cum[r][c]);
                    out.println("Rad flat: " + Rad_flat_cum[r][c]);
                } else {
                    eibasinEmonth[month - 1][r][c] = NOVALUE;
                }
            }
        }

        return true;

    }

    private void find_days( int month, int[] day_begin, int[] day_end ) {

        if (month == 1) {
            day_begin[0] = -9;
            day_end[0] = 20;
        } else if (month == 2) {
            day_begin[0] = 21;
            day_end[0] = 51;
        } else if (month == 3) {
            day_begin[0] = 52;
            day_end[0] = 81;
        } else if (month == 4) {
            day_begin[0] = 82;
            day_end[0] = 112;
        } else if (month == 5) {
            day_begin[0] = 113;
            day_end[0] = 142;
        } else if (month == 6) {
            day_begin[0] = 143;
            day_end[0] = 173;
        } else {
            out.println("Incorrect in find_days");
        }
    }

    private void get_date( int julianday, int[] month, int[] daymonth ) {

        if (julianday <= 0) {
            month[0] = 12;
            daymonth[0] = 31 + julianday;
        } else if (julianday >= 1 && julianday <= 31) {
            month[0] = 1;
            daymonth[0] = julianday;
        } else if (julianday >= 32 && julianday <= 59) {
            month[0] = 2;
            daymonth[0] = julianday - 31;
        } else if (julianday >= 60 && julianday <= 90) {
            month[0] = 3;
            daymonth[0] = julianday - 59;
        } else if (julianday >= 91 && julianday <= 120) {
            month[0] = 4;
            daymonth[0] = julianday - 90;
        } else if (julianday >= 121 && julianday <= 151) {
            month[0] = 5;
            daymonth[0] = julianday - 120;
        } else if (julianday >= 152 && julianday <= 181) {
            month[0] = 6;
            daymonth[0] = julianday - 151;
        } else if (julianday >= 182 && julianday <= 212) {
            month[0] = 7;
            daymonth[0] = julianday - 181;
        } else if (julianday >= 213 && julianday <= 243) {
            month[0] = 8;
            daymonth[0] = julianday - 212;
        } else if (julianday >= 244 && julianday <= 273) {
            month[0] = 9;
            daymonth[0] = julianday - 243;
        } else if (julianday >= 274 && julianday <= 304) {
            month[0] = 10;
            daymonth[0] = julianday - 273;
        } else if (julianday >= 305 && julianday <= 334) {
            month[0] = 11;
            daymonth[0] = julianday - 304;
        } else if (julianday >= 335 && julianday <= 365) {
            month[0] = 12;
            daymonth[0] = julianday - 334;
        }

    }

    private void sun( double hour, int day, double[] E0, double[] alpha, double[] direction ) {

        // latitudine, longitudine in [rad]

        double G, Et, local_hour, D, Thr, beta;

        // correction sideral time
        G = 2.0 * PI * (day - 1) / 365.0;
        Et = 0.000075 + 0.001868 * cos(G) - 0.032077 * sin(G) - 0.014615 * cos(2 * G) - 0.04089 * sin(2 * G);

        // local time
        local_hour = hour + Et / FluidConstants.omega; // Iqbal: formula 1.4.2

        // earth-sun distance correction
        E0[0] = 1.00011 + 0.034221 * cos(G) + 0.00128 * sin(G) + 0.000719 * cos(2 * G) + 0.000077 * sin(2 * G);

        // solar declination
        D = 0.006918 - 0.399912 * cos(G) + 0.070257 * sin(G) - 0.006758 * cos(2 * G) + 0.000907 * sin(2 * G) - 0.002697
                * cos(3 * G) + 0.00148 * sin(3 * G);

        // Sunrise and sunset with respect to midday [hour]
        Thr = (acos(-tan(D) * tan(latitude))) / FluidConstants.omega;

        if (local_hour >= 12.0 - Thr && local_hour <= 12.0 + Thr) {

            // alpha: solar height (complementar to zenith angle), [rad]
            alpha[0] = asin(sin(latitude) * sin(D) + cos(latitude) * cos(D) * cos(FluidConstants.omega * (12.0 - local_hour)));

            // direction: azimuth angle (0 Nord, clockwise) [rad]
            if (local_hour <= 12) {
                if (alpha[0] == PI / 2.0) { /* sole allo zenit */
                    direction[0] = PI / 2.0;
                } else {
                    direction[0] = PI - acos((sin(alpha[0]) * sin(latitude) - sin(D)) / (cos(alpha[0]) * cos(latitude)));
                }
            } else {
                if (alpha[0] == PI / 2.0) { /* sole allo zenit */
                    direction[0] = 3 * PI / 2.0;
                } else {
                    direction[0] = PI + acos((sin(alpha[0]) * sin(latitude) - sin(D)) / (cos(alpha[0]) * cos(latitude)));
                }
            }

            // CALCOLO OMBRE
            /*
             * Chiama Orizzonte# Inputs: dx: dim. pixel (funziona solo per pixel quadrati)
             * 2(basin.Z.nch + basin.Z.nrh): dimensione matrice alpha: altezza solare Z: matrice
             * elevazioni curv: matrice curvature beta: azimuth +#PI/4 NOVALUE: novalue per Z0
             * Outputs: shadow: matrice ombre (1 ombra 0 sole)
             */

            if (direction[0] >= 0. && direction[0] <= PI / 4.) {
                beta = direction[0];
                geomorphUtilities.orizzonte1(dx, 2 * (cols + rows), beta, alpha[0], elevImageIterator, curvatureImage,
                        outputShadow, NOVALUE);

            } else if (direction[0] > PI / 4. && direction[0] <= PI / 2.) {
                beta = (PI / 2. - direction[0]);
                geomorphUtilities.orizzonte2(dx, 2 * (cols + rows), beta, alpha[0], elevImageIterator, curvatureImage,
                        outputShadow, NOVALUE);

            } else if (direction[0] > PI / 2. && direction[0] <= PI * 3. / 4.) {
                beta = (direction[0] - PI / 2.);

                geomorphUtilities.orizzonte3(dx, 2 * (cols + rows), beta, alpha[0], elevImageIterator, curvatureImage,
                        outputShadow, NOVALUE);

            } else if (direction[0] > PI * 3. / 4. && direction[0] <= PI) {
                beta = (PI - direction[0]);
                geomorphUtilities.orizzonte4(dx, 2 * (cols + rows), beta, alpha[0], elevImageIterator, curvatureImage,
                        outputShadow, NOVALUE);

            } else if (direction[0] > PI && direction[0] <= PI * 5. / 4.) {
                beta = (direction[0] - PI);
                geomorphUtilities.orizzonte5(dx, 2 * (cols + rows), beta, alpha[0], elevImageIterator, curvatureImage,
                        outputShadow, NOVALUE);

            } else if (direction[0] > PI * 5. / 4. && direction[0] <= PI * 3. / 2.) {
                beta = (PI * 3. / 2. - direction[0]);
                geomorphUtilities.orizzonte6(dx, 2 * (cols + rows), beta, alpha[0], elevImageIterator, curvatureImage,
                        outputShadow, NOVALUE);

            } else if (direction[0] > PI * 3. / 2. && direction[0] <= PI * 7. / 4.) {
                beta = (direction[0] - PI * 3. / 2.);
                geomorphUtilities.orizzonte7(dx, 2 * (cols + rows), beta, alpha[0], elevImageIterator, curvatureImage,
                        outputShadow, NOVALUE);

            } else if (direction[0] > PI * 7. / 4. && direction[0] < 2. * PI) {
                beta = (2. * PI - direction[0]);
                /*
                 * here we should have Orizzonte8, but the routine has an error. So the 1 is called.
                 * Explanation: quello è un errore dovuto al fatto che la routine orizzonte8 è
                 * sbagliata e dà errore, allora ci ho messo una pezza e ho richiamato la
                 * orizzonte1, invece che la orizzonte 8. tuttavia le orizzonte 1 e 8 vengono
                 * chiamate solo quando il sole è a nord e da noi questo non capita mai.. quindi
                 * puoi lasciare così com'è
                 */
                geomorphUtilities.orizzonte1(dx, 2 * (cols + rows), beta, alpha[0], elevImageIterator, curvatureImage,
                        outputShadow, NOVALUE);
                // error!!!
            }

        } else {

            for( int r = 0; r < eibasinID.length; r++ ) {
                for( int c = 0; c < eibasinID[0].length; c++ ) {
                    if (eibasinID[r][c] != (int) NOVALUE)
                        outputShadow[r][c] = 1;
                }
            }
            alpha[0] = 0.0;
            direction[0] = 0.0;

        }

    }

    private void radiation( double[] Rad_morpho, double[] Rad_flat, double E0, double alpha, double direction, double aspect,
            double slope, int shadow ) {

        Rad_flat[0] = E0 * sin(alpha);
        if (shadow == 1 || alpha == 0.0) { // in ombra o di notte
            Rad_morpho[0] = 0.0;
        } else {
            Rad_morpho[0] = E0 * (cos(slope) * sin(alpha) + sin(slope) * cos(alpha) * cos(-aspect + direction));
        }

        if (Rad_morpho[0] < 0)
            Rad_morpho[0] = 0.0;
    }

    private void average_EI( int month_begin, int month_end ) {

        int m, month;

        if (month_end < month_begin)
            month_end += 12;

        for( int r = 0; r < eibasinID.length; r++ ) {
            for( int c = 0; c < eibasinID[0].length; c++ ) {
                if (eibasinID[r][c] != (int) NOVALUE) {
                    eibasinE[r][c] = 0.0;
                    for( m = month_begin - 1; m < month_end; m++ ) {
                        if (m > 11) {
                            month = m - 12;
                        } else {
                            month = m;
                        }
                        if (month < 6) {
                            eibasinE[r][c] += eibasinEmonth[month][r][c];
                        } else {
                            eibasinE[r][c] += eibasinEmonth[12 - month][r][c];
                        }
                    }
                    eibasinE[r][c] /= (double) (month_end - month_begin + 1);
                }
            }
        }

    }

    private void area( int i ) {

        double minES, maxES, minEI, maxEI;

        maxES = Double.MIN_VALUE;
        minES = Double.MAX_VALUE;
        maxEI = Double.MIN_VALUE;
        minEI = Double.MAX_VALUE;

        for( int r = 0; r < eibasinID.length; r++ ) {
            for( int c = 0; c < eibasinID[0].length; c++ ) {
                if (eibasinID[r][c] != (long) NOVALUE) {
                    // System.out.println("Bacino: " + eibasinID[r][c]);
                    if (eibasinID[r][c] == i + 1) {
                        double value = elevImageIterator.getSampleDouble(c, r, 0);
                        if (value < minES)
                            minES = value;
                        if (value > maxES)
                            maxES = value;
                        if (eibasinE[r][c] < minEI)
                            minEI = eibasinE[r][c];
                        if (eibasinE[r][c] > maxEI)
                            maxEI = eibasinE[r][c];
                    }
                }
            }
            // System.out.println("minEi: " + minEI);
            // System.out.println("maxEi: " + maxEI);
        }

        for( int r = 0; r < eibasinID.length; r++ ) {
            for( int c = 0; c < eibasinID[0].length; c++ ) {
                if (eibasinID[r][c] == (i + 1)) {
                    for( int j = 0; j < numEs; j++ ) {
                        double minCurrentAltimetricBand = minES + (j) * (maxES - minES) / (double) numEs;
                        double maxCurrentAltimetricBand = minES + (j + 1) * (maxES - minES) / (double) numEs;
                        double value = elevImageIterator.getSampleDouble(c, r, 0);
                        if ((value > minCurrentAltimetricBand && value <= maxCurrentAltimetricBand) || (j == 0 && value == minES)) {
                            for( int k = 0; k < numEi; k++ ) {
                                double minCurrentEnergeticBand = minEI + (k) * (maxEI - minEI) / (double) numEi;
                                double maxCurrentEnergeticBand = minEI + (k + 1) * (maxEI - minEI) / (double) numEi;
                                if ((eibasinE[r][c] > minCurrentEnergeticBand && eibasinE[r][c] <= maxCurrentEnergeticBand)
                                        || (k == 0 && eibasinE[r][c] == minEI)) {
                                    eibasinA[j][k][i] += 1.0;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        for( int j = 0; j < numEs; j++ ) {
            for( int k = 0; k < numEi; k++ ) {
                eibasinA[j][k][i] *= (dx * dy * 1.0E-6); // in [km2]
            }
        }

        for( int j = 0; j < numEs; j++ ) {
            eibasinESrange[j][i] = (maxES - minES) / (double) numEs;
            eibasinES[j][i] = minES + (j + 1 - 0.5) * (maxES - minES) / (double) numEs;
        }

        int cont = 0;
        for( int m = 0; m < 6; m++ ) {
            for( int k = 0; k < numEi; k++ ) {
                cont = 0;
                for( int r = 0; r < eibasinID.length; r++ ) {
                    for( int c = 0; c < eibasinID[0].length; c++ ) {
                        if (eibasinID[r][c] != (int) NOVALUE) {
                            double test1 = minEI + (k) * (maxEI - minEI) / (double) numEi;
                            double test2 = minEI + (k + 1) * (maxEI - minEI) / (double) numEi;
                            if ((eibasinE[r][c] > test1 && eibasinE[r][c] <= test2) || (k == 0 && eibasinE[r][c] == minEI)) {
                                cont += 1;
                                eibasinEI[m][k][i] += eibasinEmonth[m][r][c];
                            }
                        }
                    }
                }
                if (cont == 0) {
                    eibasinEI[m][k][i] = 0.00001;
                } else {
                    eibasinEI[m][k][i] /= (double) cont;
                }
            }
        }

    }

    // private void output( int i ) {
    //
    // String filename = outputPath + formatter.format(reverseidMappings.get(i + 1)) + ".txt";
    //
    // out.println(MessageFormat.format("Writing output {0} to file {1}", i, filename));
    // if (new File(filename).exists()) {
    // // copy file to backup
    // try {
    // // Create channel on the source
    // FileChannel srcChannel = new FileInputStream(filename).getChannel();
    //
    // // Create channel on the destination
    // FileChannel dstChannel = new FileOutputStream(filename + ".old").getChannel();
    //
    // // Copy file contents from source to destination
    // dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    //
    // // Close the channels
    // srcChannel.close();
    // dstChannel.close();
    //
    // // remove the file
    // boolean success = (new File(filename)).delete();
    // if (!success) {
    // out.println("Cannot remove file: " + filename + "!");
    // return;
    // }
    //
    // } catch (IOException e) {
    // e.printStackTrace();
    // return;
    // }
    // }
    //
    // BufferedWriter bw;
    // try {
    // bw = new BufferedWriter(new FileWriter(filename, true));
    //
    // bw.write("# generated by EIcalculator*/");
    // bw.write("\n@15");
    // bw.write("\n");
    // bw
    // .write("\n# 1 block - INDICE ENERGETICO PER LE BANDE ENERGETICHE PER OGNI MESE DELL'ANNO (-)")
    // ;
    // bw.write("\n");
    //
    // bw.write("\n# 22 DICEMBRE - 20 GENNAIO ");
    // bw.write("\n% " + numEi + "\n");
    // for( int k = 0; k < numEi - 1; k++ ) {
    // bw.write(eibasinEI[0][k][i] + " ");
    // }
    // bw.write("" + eibasinEI[0][numEi - 1][i]);
    // bw.write("\n");
    //
    // bw.write("\n# 21 GENNAIO - 20 FEBBRAIO");
    // bw.write("\n% " + numEi + "\n");
    // for( int k = 0; k < numEi - 1; k++ ) {
    // bw.write(eibasinEI[1][k][i] + " ");
    // }
    // bw.write("" + eibasinEI[1][numEi - 1][i]);
    // bw.write("\n");
    //
    // bw.write("\n# 21 FEBBRAIO - 22 MARZO");
    // bw.write("\n% " + numEi + "\n");
    // for( int k = 0; k < numEi - 1; k++ ) {
    // bw.write(eibasinEI[2][k][i] + " ");
    // }
    // bw.write("" + eibasinEI[2][numEi - 1][i]);
    // bw.write("\n");
    //
    // bw.write("\n# 23 MARZO - 22 APRILE");
    // bw.write("\n% " + numEi + "\n");
    // for( int k = 0; k < numEi - 1; k++ ) {
    // bw.write(eibasinEI[3][k][i] + " ");
    // }
    // bw.write("" + eibasinEI[3][numEi - 1][i]);
    // bw.write("\n");
    //
    // bw.write("\n# 23 APRILE - 22 MAGGIO");
    // bw.write("\n% " + numEi + "\n");
    // for( int k = 0; k < numEi - 1; k++ ) {
    // bw.write(eibasinEI[4][k][i] + " ");
    // }
    // bw.write("" + eibasinEI[4][numEi - 1][i]);
    // bw.write("\n");
    //
    // bw.write("\n#23 MAGGIO - 22 GIUGNO");
    // bw.write("\n% " + numEi + "\n");
    // for( int k = 0; k < numEi - 1; k++ ) {
    // bw.write(eibasinEI[5][k][i] + " ");
    // }
    // bw.write("" + eibasinEI[5][numEi - 1][i]);
    // bw.write("\n");
    //
    // bw.write("\n");
    // bw.write("\n");
    // bw.write("\n");
    // bw
    // .write(
    // "\n# 2 block - QUOTA DEL BARICENTRO DELLE FASCIE ALTIMETRICHE e RANGE DI QUOTA PER OGNI FASCIA (m)"
    // );
    // bw.write("\n");
    //
    // bw.write("\n% " + numEs + "\n");
    // for( int j = 0; j < numEs - 1; j++ ) {
    // bw.write(eibasinES[j][i] + " ");
    // }
    // bw.write("" + eibasinES[numEs - 1][i]);
    //
    // bw.write("\n% " + numEs + "\n");
    // for( int j = 0; j < numEs - 1; j++ ) {
    // bw.write(eibasinESrange[j][i] + " ");
    // }
    // bw.write("" + eibasinESrange[numEs - 1][i]);
    //
    // bw.write("\n");
    // bw.write("\n");
    // bw.write("\n");
    //
    // bw
    // .write("\n# 3 block - AREE PER FASCIA ALTIMETRICA (riga) E BANDA ENERGETICA (colonna) (km2)");
    // bw.write("\n% " + numEs + " " + numEi + "\n");
    // for( int j = 0; j < numEs; j++ ) {
    // for( int k = 0; k < numEi; k++ ) {
    // bw.write(eibasinA[j][k][i] + " ");
    // }
    // bw.write("\n");
    // }
    //
    // bw.close();
    //
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    //
    // }
}
