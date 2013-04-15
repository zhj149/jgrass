/*******************************************************************************
 * FILE:        QStatistic.java
 * DESCRIPTION:
 * NOTES:       ---
 * AUTHOR:      Antonello Andrea, Silvia Franceschi, Pisoni Silvano
 * EMAIL:       andrea.antonello@hydrologis.com,silvia.franceschi@hydrologis.com 
 * COMPANY:     HydroloGIS / Engineering, University of Trento / CUDAM
 * COPYRIGHT:   Copyright (C) 2005 HydroloGIS / University of Trento / CUDAM, ITALY, GPL
 * VERSION:     $version$
 * CREATED:     $Date: 2005/07/05 11:10:29 $
 * REVISION:    $Revision: 1.6 $
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

import eu.hydrologis.jgrass.libs.utils.monitor.PrintstreamProgress;
import eu.hydrologis.libs.peakflow.core.iuh.IUHCalculator;
import eu.hydrologis.libs.peakflow.core.jeff.StatisticJeff;
import eu.hydrologis.libs.peakflow.utils.ParameterBox;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * @author moovida
 */
public class QStatistic implements DischargeCalculator {

    private ParameterBox fixedParams = null;

    private IUHCalculator iuhC = null;

    private StatisticJeff jeffC = null;

    private double tpmax = 0f;

    private double J = 0f;

    private double h = 0f;

    private double[][] ampidiff = null;

    private double diffusionparameter = 0f;

    private final PrintStream out;

    /**
     * This class calculates maximum discharge and discharge.
     * 
     * @param fixedParameters - set of initial parameters
     * @param _iuhC - abstraction of the iuh calculator
     * @param _jeffC - abstraction of the jeff calculator
     * @param out
     */
    public QStatistic( ParameterBox fixedParameters, IUHCalculator _iuhC, StatisticJeff _jeffC,
            PrintStream out ) {
        fixedParams = fixedParameters;

        iuhC = _iuhC;
        jeffC = _jeffC;
        this.out = out;

        double[][] jeff = jeffC.calculateJeff();
        J = jeff[0][0];
        h = jeff[0][1];
        tpmax = iuhC.getTpMax();

        ampidiff = iuhC.calculateIUH();
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.peakflow.discharge.DischargeCalculator#calculateQmax()
     */
    public double calculateQmax() {

        double area_super = fixedParams.getArea();
        double area_sub = fixedParams.getArea_sub();
        double area_tot = 0f;

        /* if (effectsBox.containsKey("ampi_sub")) */
        if (area_sub != 0) {
            area_tot = area_sub + area_super;
        } else {
            area_tot = area_super;
        }

        double qmax = (double) (J * area_tot * (FluidUtils.width_interpolate(ampidiff, iuhC
                .getTstarMax(), 0, 2) - FluidUtils.width_interpolate(ampidiff, iuhC.getTstarMax()
                - tpmax, 0, 2)));

        return qmax;
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.peakflow.discharge.DischargeCalculator#calculateQ()
     */
    public double[][] calculateQ() {
        double timestep = fixedParams.getTimestep();
        double area_super = fixedParams.getArea();
        double area_sub = fixedParams.getArea_sub();
        double area_tot = 0f;

        double tcorr = ampidiff[ampidiff.length - 1][0];
        double[][] Q = new double[(int) Math.floor((tcorr + tpmax) / timestep) + 1][4];

        if (area_sub != -9999.0) {
            area_tot = area_sub + area_super;
        } else {
            area_tot = area_super;
        }

        /*
         * calculate the discharge for t < tcorr
         */
        int j = 0;
        out.println("Calculating discharge...");
        PrintstreamProgress p = new PrintstreamProgress(1, (int) tcorr, out);
        for( int t = 1; t < tcorr; t += timestep ) {
            p.printPercent(t);
            j = (int) Math.floor((t) / timestep);

            if (t <= tpmax) {
                Q[j][0] = t;
                Q[j][1] = (double) (J * area_tot * FluidUtils.width_interpolate(ampidiff, t, 0, 2));
                Q[j][2] = Q[j - 1][2] + Q[j][1];
                Q[j][3] = h;
            } else {
                Q[j][0] = t;
                Q[j][1] = (double) (J * area_tot * (FluidUtils.width_interpolate(ampidiff, t, 0, 2) - FluidUtils
                        .width_interpolate(ampidiff, t - tpmax, 0, 2)));
                Q[j][2] = Q[j - 1][2] + Q[j][1];
                Q[j][3] = 0.0;
            }
        }

        /*
         * calculate the discharge for t > tcorr
         */

        for( double t = tcorr; t < (tcorr + tpmax); t += timestep ) {
            j = (int) Math.floor(((int) t) / timestep);
            Q[j][0] = t;
            Q[j][1] = (double) (J * area_tot * (ampidiff[ampidiff.length - 1][2] - FluidUtils
                    .width_interpolate(ampidiff, t - tpmax, 0, 2)));
            Q[j][2] = Q[j - 1][2] + Q[j][1];
            Q[j][3] = 0.0;
        }

        /*
         * calculate the volumes
         */
        double vol = Q[Q.length - 2][2] * timestep;
        double vol2 = (double) (area_tot * h / 1000);

        return Q;
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.peakflow.discharge.DischargeCalculator#getTpMax()
     */
    public double getTpMax() {
        return tpmax;
    }

}