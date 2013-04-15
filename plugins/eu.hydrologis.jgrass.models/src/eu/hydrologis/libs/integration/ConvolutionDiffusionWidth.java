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
package eu.hydrologis.libs.integration;

import eu.hydrologis.libs.utils.FluidUtils;

/**
 * @author moovida
 */
public class ConvolutionDiffusionWidth extends SimpsonIntegral implements IntegrableFunction {

    private double[][] ampi_diffusion = null;

    private double D = 0f;

    private double t = 0;

    private double c = 0;

    /**
     * Calculates the integral of the diffusion equation
     * 
     * @param lowerintegrationlimit - the lower limit of integration
     * @param upperintegrationlimit - the upper limit of integration
     * @param maximalsteps- maximal number of bins in which to divide the interval
     * @param integrationaccuracy - value of accuracy of integration
     * @param ampiFunction
     * @param diffusionparam
     * @param time
     * @param celerity
     * @param integrationtype
     */
    public ConvolutionDiffusionWidth( double lowerintegrationlimit, double upperintegrationlimit,
            int maximalsteps, double integrationaccuracy, double[][] ampiFunction,
            double diffusionparam, double time, double celerity ) {
        lowerlimit = lowerintegrationlimit;
        upperlimit = upperintegrationlimit;
        maxsteps = maximalsteps;
        accuracy = integrationaccuracy;
        strapezoid = 0f;
        ampi_diffusion = ampiFunction;
        D = diffusionparam;
        t = time;
        c = celerity;
    }

    public void updateTime( int newt ) {
        t = newt;
    }

    /*
     * (non-Javadoc)
     * @see bsh.util.integration.IntegrableFunction#integrate(double, double, double)
     */
    public double integrate() {
        return simpson();
    }

    /*
     * (non-Javadoc)
     * @see bsh.util.integration.SimpsonIntegral#equation(double)
     */
    protected double equation( double x ) {

        double result = x > ampi_diffusion[ampi_diffusion.length - 1][0] ? 0.0 : 1
                / Math.sqrt(4 * Math.PI * D * Math.pow(t, 3.0f))
                * FluidUtils.width_interpolate(ampi_diffusion, x, 0, 1) * x
                / (Math.exp(Math.pow((x - t), 2) / (4 * D * t)));

        return result;
    }

}