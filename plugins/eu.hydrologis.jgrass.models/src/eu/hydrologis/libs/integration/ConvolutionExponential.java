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

import eu.hydrologis.jgrass.libs.utils.interpolation.Interpolate2D;

/**
 * @author silli
 */
public class ConvolutionExponential extends SimpsonIntegral implements IntegrableFunction {

    private double k = 0f;
    private final Interpolate2D timeDischargeInterpolator;

    /**
     * Calculates the integral of the exponential equation
     * 
     * @param lowerintegrationlimit
     * @param upperintegrationlimit
     * @param maximalsteps
     * @param integrationaccuracy
     * @param invasoConstant
     * @param timeDischargeInterpolator
     */
    public ConvolutionExponential( double lowerintegrationlimit, double upperintegrationlimit,
            int maximalsteps, double integrationaccuracy, double invasoConstant,
            Interpolate2D timeDischargeInterpolator ) {
        lowerlimit = lowerintegrationlimit;
        upperlimit = upperintegrationlimit;
        maxsteps = maximalsteps;
        accuracy = integrationaccuracy;
        this.timeDischargeInterpolator = timeDischargeInterpolator;
        strapezoid = 0f;
        k = invasoConstant;
    }

    /*
     * (non-Javadoc)
     * @see eu.hydrologis.libs.integration.SimpsonIntegral#equation(double)
     */
    protected double equation( double time ) {

        double d = (double) (1 / k * Math.exp(-(upperlimit - time) / k) * timeDischargeInterpolator
                .linearInterpolateY(time).doubleValue());
        return d;
    }
    /*
     * (non-Javadoc)
     * @see bsh.util.integration.IntegrableFunction#integrate()
     */
    public double integrate() {
        return simpson();
    }

}