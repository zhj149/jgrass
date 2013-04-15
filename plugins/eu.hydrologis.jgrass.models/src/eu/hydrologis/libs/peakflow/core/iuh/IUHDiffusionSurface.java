/*******************************************************************************
 * FILE:        IUHDiffusionSurface.java
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

import eu.hydrologis.jgrass.libs.utils.monitor.PrintstreamProgress;
import eu.hydrologis.libs.integration.ConvolutionDiffusionWidth;
import eu.hydrologis.libs.integration.IntegralConstants;
import eu.hydrologis.libs.peakflow.utils.ParameterBox;

/**
 * @author moovida
 */
public class IUHDiffusionSurface {

    private double[][] ampi_diffusion = null;
    private double[][] ampi = null;
    private double diffusionparameter = 0f;
    private double vc = 0f;
    private double delta = 0f;
    private double xres = 0f;
    private double yres = 0f;
    private double npixel = 0f;
    private double timestep = 0f;
    private double n_idf = 0f;
    private double area = 0f;
    private final PrintStream out;

    /**
     * @param out 
   * 
   */
    public IUHDiffusionSurface( double[][] _ampi, ParameterBox fixedParameters, PrintStream out ) {
        ampi = _ampi;
        this.out = out;
        delta = fixedParameters.getDelta();

        double threshold = 5000f;
        ampi_diffusion = new double[(int) (ampi.length + threshold / delta)][ampi[0].length];

        for( int i = 0; i < ampi.length; i++ ) {
            ampi_diffusion[i][0] = ampi[i][0];
        }
        for( int i = 1; i < threshold / delta; i++ ) {
            ampi_diffusion[ampi.length - 1 + i][0] = ampi[ampi.length - 1][0] + i * delta;
        }

        diffusionparameter = fixedParameters.getDiffusionparameter();
        vc = fixedParameters.getVc();
        xres = fixedParameters.getXres();
        yres = fixedParameters.getYres();
        npixel = fixedParameters.getNpixel();
    }

    public double[][] calculateIUH() {
        double cum = 0f;
        double t = 0;
        double integral = 0;

        /*
         * next part calculates the convolution between the aplitude function and the diffusion
         * equation
         */
        ConvolutionDiffusionWidth diffIntegral = new ConvolutionDiffusionWidth(0.0,
                ampi_diffusion[ampi_diffusion.length - 1][0], IntegralConstants.diffusionmaxsteps,
                IntegralConstants.diffusionaccurancy, ampi, diffusionparameter, t, vc);

        PrintstreamProgress p = new PrintstreamProgress(0, ampi_diffusion.length - 1, out);
        out.println("Calculating diffusion...");
        for( int i = 0; i < ampi_diffusion.length - 1; i++ ) {
            p.printPercent(i);

            t = ampi_diffusion[i + 1][0];

            diffIntegral.updateTime((int) t);
            integral = diffIntegral.integrate();

            ampi_diffusion[i + 1][1] = integral;
            cum += integral * delta / (xres * yres * npixel * vc);
            ampi_diffusion[i + 1][2] = cum;

        }

        return ampi_diffusion;
    }

}
