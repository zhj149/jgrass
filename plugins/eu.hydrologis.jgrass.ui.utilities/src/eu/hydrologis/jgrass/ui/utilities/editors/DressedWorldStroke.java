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
package eu.hydrologis.jgrass.ui.utilities.editors;

import java.io.Serializable;

import org.geotools.geometry.jts.ReferencedEnvelope;

/**
 * This class represents a dressed stroke, i.e. coordinates in world position of
 * it's nodes, related to a particular CRS, stroke width, color and whatever
 * will be.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class DressedWorldStroke implements Serializable {
    public Double[] nodes = { 0.0 };
    
    public ReferencedEnvelope bounds = null;

    public int[] strokeWidth = { 1 };

    public int[] rgb = { 0, 0, 0, 128 };

    public int[] lineStyle = { 6 };

    public String crsWKT = null;

    public double scale = -1.0;

}
