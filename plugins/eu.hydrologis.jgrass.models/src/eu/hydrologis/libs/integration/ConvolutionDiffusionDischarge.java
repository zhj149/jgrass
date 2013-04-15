/*******************************************************************************
 * FILE:        ConvolutionDiffusionDischarge.java
 * DESCRIPTION:
 * NOTES:       ---
 * AUTHOR:      Antonello Andrea
 * EMAIL:       andrea.antonello@hydrologis.com
 * COMPANY:     HydroloGIS / Engineering, University of Trento / CUDAM
 * COPYRIGHT:   Copyright (C) 2005 HydroloGIS / University of Trento / CUDAM, ITALY, GPL
 * VERSION:     $version$
 * CREATED:     $Date: 2005/07/13 21:22:45 $
 * REVISION:    $Revision: 1.1 $
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

package eu.hydrologis.libs.integration;

import eu.hydrologis.libs.utils.FluidUtils;

/**
 * @author moovida
 */
public class ConvolutionDiffusionDischarge extends SimpsonIntegral implements IntegrableFunction {

    double[][] q = null;
    private double D = 0f;
    private double t = 0;
    private double dist = 0;
    private double c = 0;

    /**
   * 
   */
    public ConvolutionDiffusionDischarge( double lowerintegrationlimit,
            double upperintegrationlimit, int maximalsteps, double integrationaccuracy,
            double[][] discharge, double diffusionparam, double time, double distance,
            double celerity ) {

        lowerlimit = lowerintegrationlimit;
        upperlimit = upperintegrationlimit;
        maxsteps = maximalsteps;
        accuracy = integrationaccuracy;
        strapezoid = 0f;
        q = discharge;
        D = diffusionparam;
        t = time;
        c = celerity;
        dist = distance;

    }

    public void updateTime( int newt ) {
        t = newt;
    }

    /*
     * (non-Javadoc)
     * @see bsh.util.integration.IntegrableFunction#integrate()
     */
    public double integrate() {
        return simpson();

    }

    /*
     * (non-Javadoc)
     * @see bsh.util.integration.SimpsonIntegral#equation(double)
     */
    protected double equation( double tau ) {

        double result = t <= tau ? 0.0 : tau > q[q.length - 1][0] ? 0.0 : 1
                / (Math.sqrt(4 * Math.PI * D * Math.pow((t - tau), 3)))
                * FluidUtils.width_interpolate(q, tau, 0, 1) * dist
                / (Math.exp(Math.pow(dist - c * (t - tau), 2) / (4 * D * (t - tau))));

        return (double) result;
    }
}
