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
package eu.hydrologis.jgrass.models.h.cb;

/**
 * Support class to simulate structure to help the C port
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class SplittedVectors {
    public double[][] splittedvalues1 = null;
    public double[][] splittedvalues2 = null;
    public double[] splittedindex = null;

    public SplittedVectors() {
    }

    public void initValues( int rows, int cols ) {
        splittedvalues1 = new double[rows][cols];
        splittedvalues2 = new double[rows][cols];
    }

    public void initIndex( int count ) {
        splittedindex = new double[count];
    }

}
