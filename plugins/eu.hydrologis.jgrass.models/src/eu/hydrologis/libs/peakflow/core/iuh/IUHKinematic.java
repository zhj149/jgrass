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
package eu.hydrologis.libs.peakflow.core.iuh;

import java.io.PrintStream;

import eu.hydrologis.jgrass.libs.utils.monitor.PrintstreamProgress;
import eu.hydrologis.libs.peakflow.utils.EffectsBox;
import eu.hydrologis.libs.peakflow.utils.ParameterBox;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class IUHKinematic implements IUHCalculator {

    private double[][] ampikinematic = null;

    private double[][] ampisubsurface = null;

    private double[][] totalampikinematic = null;

    private double tpmax = 0f;

    private double tstarmax = 0f;

    private double tstar = 0f;

    private double error = 100f;

    private final PrintStream out;

    /**
     * @param effectsBox
     * @param fixedParams
     * @param out printstream for info purposes
     */
    public IUHKinematic( EffectsBox effectsBox, ParameterBox fixedParams, PrintStream out ) {

        this.out = out;
        double area = fixedParams.getArea();
        double area_sub = fixedParams.getArea_sub();
        double delta_sup = fixedParams.getDelta();
        double delta_sub = fixedParams.getDelta_sub();
        double vc = fixedParams.getVc();
        double timestep = fixedParams.getTimestep();
        double n_idf = fixedParams.getN_idf();
        double area_tot = 0f;

        double[][] ampi = effectsBox.getAmpi();
        double tcorr = ampi[ampi.length - 1][0];

        /*
         * Copy the ampi matrix in the ampikinematic because it represents the superficial
         * contribute in the kinematic algorithm.
         */
        ampikinematic = new double[ampi.length][ampi[0].length];
        for( int i = 0; i < ampi.length; i++ ) {
            System.arraycopy(ampi[i], 0, ampikinematic[i], 0, ampi[0].length);
        }

        if (effectsBox.ampi_subExists()) {
            area_sub = fixedParams.getArea_sub();
            double[][] ampi_help_sub = effectsBox.getAmpi_help_sub();

            IUHSubSurface iuhSubSurface = new IUHSubSurface(ampi_help_sub, fixedParams, out);
            ampisubsurface = iuhSubSurface.calculateIUH();
        }

        totalampikinematic = calculateTotalKinematic(ampikinematic, ampisubsurface, delta_sup,
                delta_sub, vc, tcorr, area_sub, area);

        /*
         * solve the equation of henderson W(dt)=W(dt+tp). Calculate tpmax e tstar max for the
         * kinematic case.
         */

        if (effectsBox.ampi_subExists()) {
            area_tot = area_sub + area;
        } else {
            area_tot = area;
        }

        /*
         * Skip the tpmax calculation if real rainfall data
         */
        if (effectsBox.rainDataExists()) {
            tpmax = 0f;
            return;
        }

        int rows = totalampikinematic.length;
        double dt = 0f;
        double prov = 0f;

        int index = 0;
        int threshold = (int) (tcorr / 100);
        out.println("IUH kinematic..."); //$NON-NLS-1$
        PrintstreamProgress p = new PrintstreamProgress(1, (int) tcorr, out);
        for( int tp = 1; tp <= tcorr; tp += timestep ) {
            p.printPercent(tp);

            if (index > threshold) {
                index = 0;
            } else {
                index++;
            }
            dt = FluidUtils.henderson(totalampikinematic, tp);
            tstar = tp + dt;
            if (tstar < tcorr) {
                prov = n_idf
                        - 1
                        + (tp
                                * (double) FluidUtils.width_interpolate(totalampikinematic, tstar,
                                        0, 1) / (area * ((double) FluidUtils.width_interpolate(
                                totalampikinematic, tstar, 0, 2) - (double) FluidUtils
                                .width_interpolate(totalampikinematic, dt, 0, 2))));

                if (Math.abs(prov) < error) {
                    tpmax = tp;
                    tstarmax = tpmax + dt;
                    error = Math.abs(prov);

                }
            }
        }
    }
    /**
     * Calculate the total IUH by summing the superficial and the subsuperficial IUH
     * 
     * @param ampikinesurface
     * @param ampisubsurface
     * @param delta_sup
     * @param delta_sub
     * @param vc
     * @param tcorr
     * @param area_sub
     * @param area
     * @return
     */
    private double[][] calculateTotalKinematic( double[][] ampikinesurface,
            double[][] ampisubsurface, double delta_sup, double delta_sub, double vc, double tcorr,
            double area_sub, double area_super ) {

        double[][] totalKinematic = null;

        if (ampisubsurface == null) {
            totalKinematic = new double[ampikinesurface.length][3];
            totalKinematic = ampikinesurface;

        } else {
            /*
             * calculate how many rows are in ampi_sub after ampi_sup has finished
             */
            int rowinampisubwhereampisupfinishes = 0;
            for( int i = 0; i < ampisubsurface.length; i++ ) {
                if (ampisubsurface[i][0] >= ampikinesurface[ampikinesurface.length - 1][0]) {
                    rowinampisubwhereampisupfinishes = i;
                    break;
                }
            }

            int totallength = ampikinesurface.length + ampisubsurface.length
                    - rowinampisubwhereampisupfinishes;

            totalKinematic = new double[totallength][3];

            double intsub = 0f;
            double intsup = 0f;
            for( int i = 0; i < ampikinesurface.length; i++ ) {
                totalKinematic[i][0] = ampikinesurface[i][0];
                intsub = (double) FluidUtils.width_interpolate(ampisubsurface,
                        ampikinesurface[i][0], 0, 1);
                intsup = ampikinesurface[i][1];

                totalKinematic[i][1] = intsup + intsub;
            }
            for( int i = ampikinesurface.length, j = rowinampisubwhereampisupfinishes; i < totallength; i++, j++ ) {
                totalKinematic[i][0] = ampisubsurface[j][0];
                totalKinematic[i][1] = ampisubsurface[j][1];
            }

            /*
             * calculation of the third column = cumulated The normalization occurs by means of the
             * superficial delta in the first part of the hydrogram, i.e. until the superficial
             * contributes, after that the delta is the one of the subsuperficial.
             */
            double cum = 0f;
            for( int i = 0; i < ampikinesurface.length; i++ ) {
                cum = cum + (totalKinematic[i][1] * delta_sup) / ((area_super + area_sub) * vc);
                totalKinematic[i][2] = cum;
            }
            for( int i = ampikinesurface.length, j = rowinampisubwhereampisupfinishes; i < totallength; i++, j++ ) {
                cum = cum + (totalKinematic[i][1] * delta_sub) / ((area_super + area_sub) * vc);
                totalKinematic[i][2] = cum;
            }
        }

        return totalKinematic;
    }

    public double[][] calculateIUH() {
        return totalampikinematic;
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.peakflow.iuh.IUHCalculator#getTpMax()
     */
    public double getTpMax() {
        return tpmax;
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.peakflow.iuh.IUHCalculator#getTstarMax()
     */
    public double getTstarMax() {
        return tstarmax;
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.hydropeak.core.iuh.IUHCalculator#getIUHSuperficial()
     */
    public double[][] getIUHSuperficial() {
        return ampikinematic;
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.hydropeak.core.iuh.IUHCalculator#getIUHSubsuperficial()
     */
    public double[][] getIUHSubsuperficial() {
        return ampisubsurface;
    }

}