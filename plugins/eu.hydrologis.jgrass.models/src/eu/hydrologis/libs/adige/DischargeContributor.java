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
package eu.hydrologis.libs.adige;

/**
 * Interface for all those objects that can supply a discharge in a given point of the network.
 * 
 * <p>The point of the network is defined by its pafafstetter number.</p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public interface DischargeContributor {

    /**
     * Returns the discharge for the given location of the network.
     * 
     * @param pfafstetterNumber the number identifying the network position.
     * @param inputDischarge the input discharge in the contributing point. May 
     *                      be useful to calculate the output discharge. 
     * @return the discharge for the given network point. Double.NaN has to be 
     *                      returned for invalid values.
     */
    public abstract Double getDischarge( String pfafstetterNumber, double inputDischarge );

}