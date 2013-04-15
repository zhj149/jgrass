/*******************************************************************************
 * FILE:        QReal.java
 * DESCRIPTION:
 * NOTES:       ---
 * AUTHOR:      Antonello Andrea
 * EMAIL:       andrea.antonello@hydrologis.com
 * COMPANY:     HydroloGIS / Engineering, University of Trento / CUDAM
 * COPYRIGHT:   Copyright (C) 2005 HydroloGIS / University of Trento / CUDAM, ITALY, GPL
 * VERSION:     $version$
 * CREATED:     $Date: 2005/07/05 11:10:29 $
 * REVISION:    $Revision: 1.3 $
 *******************************************************************************
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 ******************************************************************************
 *
 * CHANGE LOG:
 *
 * version:
 * comments: changes
 * author:
 * created:
 *****************************************************************************/

package eu.hydrologis.libs.peakflow.core.discharge;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.peakflow.core.iuh.IUHCalculator;
import eu.hydrologis.libs.peakflow.core.jeff.RealJeff;
import eu.hydrologis.libs.peakflow.utils.ParameterBox;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * @author moovida
 */
public class QReal implements DischargeCalculator {

    private IUHCalculator iuhC = null;

    private RealJeff jeffC = null;

    private Map<DateTime, Double> jeff = null;

    private double[][] ampi = null;

    private double tpmax = 0f;

    private ParameterBox fixedParams = null;

    private double[][] Qtot = null;

    private final PrintStream out;

    /**
     * Calculate the discharge with rainfall data
     * @param out 
     */
    public QReal( ParameterBox fixedParameters, IUHCalculator _iuhC, RealJeff _jeffC,
            PrintStream out ) {
        iuhC = _iuhC;
        jeffC = _jeffC;

        fixedParams = fixedParameters;
        this.out = out;

        jeff = jeffC.calculateJeff();
        ampi = iuhC.calculateIUH();
    }

    /**
     * Calculate the discharge with rainfall data.
     */
    // public QReal( ParameterBox fixedParameters, double[][] iuhdata, double[][] jeffdata ) {
    // fixedParams = fixedParameters;
    //
    // jeff = jeffdata;
    // ampi = iuhdata;
    //
    // System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA: ampilength " + ampi[ampi.length - 1][0]);
    // }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.peakflow.core.discharge.DischargeCalculator#calculateQ()
     */
    public double[][] calculateQ() {
        double timestep = fixedParams.getTimestep();
        double area_super = fixedParams.getArea();
        double area_sub = fixedParams.getArea_sub();
        double area_tot = 0f;

        double raintimestep = jeffC.getRain_timestep();
        DateTime firstDate = jeffC.getFirstDate();

        /*
         * The maximum rain time has no sense with the real precipitations. In this case it will use
         * the rain timestep for tp.
         */
        double tcorr = ampi[ampi.length - 1][0];
        tpmax = (double) raintimestep;
        int rainLength = jeff.size();
        double[][] totalQshiftMatrix = new double[rainLength][(int) (Math.floor((tcorr + tpmax)
                / timestep) + 1 + rainLength * raintimestep / timestep)];
        double[][] Q = new double[(int) Math.floor((tcorr + tpmax) / timestep) + 1][3];

        if (area_sub != -9999.0) {
            area_tot = area_sub + area_super;
        } else {
            area_tot = area_super;
        }

        Set<DateTime> dates = jeff.keySet();
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating discharge...", dates.size());
        int i = 0;
        for( DateTime dateTime : dates ) {
            double J = jeff.get(dateTime);
            /*
             * calculate the discharge for t < tcorr
             */
            int j = 0;
            for( int t = 1; t < tcorr; t += timestep ) {
                j = (int) Math.floor((t) / timestep);

                if (t <= tpmax) {
                    Q[j][0] = t;
                    double widthInterpolate = FluidUtils.width_interpolate(ampi, t, 0, 2);
                    Q[j][1] = (double) (J * area_tot * widthInterpolate);
                    Q[j][2] = Q[j - 1][2] + Q[j][1];
                } else {
                    Q[j][0] = t;
                    Q[j][1] = (double) (J * area_tot * (FluidUtils.width_interpolate(ampi, t, 0, 2) - FluidUtils
                            .width_interpolate(ampi, t - tpmax, 0, 2)));
                    Q[j][2] = Q[j - 1][2] + Q[j][1];
                }
            }

            /*
             * calculate the discharge for t > tcorr
             */

            for( double t = tcorr; t < (tcorr + tpmax); t += timestep ) {
                j = (int) Math.floor(((int) t) / timestep);
                Q[j][0] = t;
                Q[j][1] = (double) (J * area_tot * (ampi[ampi.length - 1][2] - FluidUtils
                        .width_interpolate(ampi, t - tpmax, 0, 2)));
                Q[j][2] = Q[j - 1][2] + Q[j][1];
            }

            /*
             * calculate the volumes
             */
            double vol = Q[Q.length - 2][2] * timestep;
            double vol2 = (double) (area_tot * J * raintimestep);

            /*
             * calculate zero padding before first value Note that jeff contains already the
             * progressive time of the rainfile.
             */
            int totalshiftmatrixindex = 0;
            int initalshiftmatrixindex = 0;
            // FIXME time in ???
            Duration duration = new Duration(firstDate, dateTime);
            long intervalSeconds = duration.getStandardSeconds();

            int paddingnumber = (int) (intervalSeconds / timestep);
            for( int m = 0; m < paddingnumber; m++ ) {
                totalQshiftMatrix[i][m] = 0;
                totalshiftmatrixindex++;
            }
            initalshiftmatrixindex = totalshiftmatrixindex;
            for( int k = initalshiftmatrixindex; k < Q.length + initalshiftmatrixindex; k++ ) {
                totalQshiftMatrix[i][k] = Q[k - initalshiftmatrixindex][1];
                totalshiftmatrixindex++;
            }
            for( int k = Q.length + totalshiftmatrixindex; k < totalQshiftMatrix[0].length; k++ ) {
                totalQshiftMatrix[i][k] = 0;
            }
            i++;
            pm.worked(1);
        }
        pm.done();

        /*
         * sum the discharge contributes
         */
        Qtot = new double[totalQshiftMatrix[0].length][2];
        double tottime = 0f;
        for( int k = 0; k < Qtot.length; k++ ) {
            double sum = 0f;
            for( int j = 0; j < totalQshiftMatrix.length; j++ ) {
                sum = sum + totalQshiftMatrix[j][k];
            }

            tottime = tottime + timestep;

            Qtot[k][1] = sum;
            Qtot[k][0] = tottime;
        }

        double total_vol = 0f;
        for( int k = 0; k < Qtot.length; k++ ) {
            total_vol = total_vol + Qtot[k][1];
        }
        double total_rain = 0.0;
        for( DateTime dateTime : dates ) {
            double J = jeff.get(dateTime);
            total_rain = total_rain + J;
        }
        total_rain = total_rain * area_tot * raintimestep;

        return Qtot;
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.peakflow.core.discharge.DischargeCalculator#calculateQmax()
     */
    public double calculateQmax() {
        if (Qtot == null) {
            calculateQ();
        }

        double qmax = 0f;
        for( int i = 0; i < Qtot.length; i++ ) {
            if (Qtot[i][1] > qmax)
                qmax = Qtot[i][1];
        }

        return qmax;
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.peakflow.core.discharge.DischargeCalculator#getTpMax()
     */
    public double getTpMax() {
        return tpmax;
    }

}