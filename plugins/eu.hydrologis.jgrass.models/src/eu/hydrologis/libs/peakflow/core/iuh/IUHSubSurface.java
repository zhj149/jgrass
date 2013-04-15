/*******************************************************************************
 * FILE:        IUHSubSurface.java
 * DESCRIPTION:
 * NOTES:       ---
 * AUTHOR:      Antonello Andrea, Silvia Franceschi, Pisoni Silvano
 * EMAIL:       andrea.antonello@hydrologis.com,silvia.franceschi@hydrologis.com 
 * COMPANY:     HydroloGIS / Engineering, University of Trento / CUDAM
 * COPYRIGHT:   Copyright (C) 2005 HydroloGIS / University of Trento / CUDAM, ITALY, GPL
 * VERSION:     $version$
 * CREATED:     $Date: 2005/07/13 21:22:45 $
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

package eu.hydrologis.libs.peakflow.core.iuh;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import eu.hydrologis.jgrass.libs.utils.interpolation.Interpolate2D;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintstreamProgress;
import eu.hydrologis.libs.integration.ConvolutionExponential;
import eu.hydrologis.libs.integration.ConvolutionExponentialPeakflow;
import eu.hydrologis.libs.peakflow.utils.ParameterBox;

/**
 * @author moovida
 */
public class IUHSubSurface {

    private double[][] ampi_sub = null;
    private double[][] ampi_help = null;
    private double vc = 0f;
    private double delta_sub = 0f;
    private double xres = 0f;
    private double yres = 0f;
    private double npixel_sub = 0f;
    private double resid_time = 0f;
    private double vcvv = 0f;
    private boolean isScs = false;
    private final PrintStream out;

    /**
     * @param out
     */
    public IUHSubSurface( double[][] _ampi, ParameterBox fixedParameters, PrintStream out ) {
        ampi_help = _ampi;
        this.out = out;
        ampi_sub = new double[ampi_help.length][ampi_help[0].length];

        for( int i = 0; i < ampi_help.length; i++ ) {
            ampi_sub[i][0] = ampi_help[i][0];
        }

        vc = fixedParameters.getVc();
        vcvv = fixedParameters.getVcvv();
        delta_sub = fixedParameters.getDelta_sub();
        xres = fixedParameters.getXres();
        yres = fixedParameters.getYres();
        npixel_sub = fixedParameters.getNpixel_sub();
        resid_time = fixedParameters.getResid_time();
    }

    public double[][] calculateIUH() {
        double cum = 0f;
        double t = 0;
        double integral = 0;

        /*
         * next part calculates the convolution between the aplitude function and the exponential
         * equation
         */
        PrintstreamProgress p = new PrintstreamProgress(0, ampi_help.length - 1, out);
        out.println("Calculating subsurface IUH...");
        for( int i = 0; i < ampi_help.length - 1; i++ ) {
            p.printPercent(i);
            t = ampi_sub[i + 1][0];

            double upperintegrationlimit = ampi_sub[ampi_sub.length - 1][0];
            ConvolutionExponentialPeakflow expIntegral = new ConvolutionExponentialPeakflow(0.0,
                    upperintegrationlimit, 20, 0.00001,ampi_help, resid_time, t);

            integral = expIntegral.integrate();
            ampi_sub[i + 1][1] = integral;
            /*
             * if (isScs) { cum += integral delta_sub / (xres yres npixel_sub vc / vcvv); } else {
             */
            cum += integral * delta_sub / (xres * yres * npixel_sub * vc);

            ampi_sub[i + 1][2] = cum;

        }

        return ampi_sub;
    }
}
