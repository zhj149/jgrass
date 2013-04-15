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

import org.eclipse.swt.graphics.Rectangle;

/**
 * A styled stroke.
 * 
 * <p>
 * This class represents a dressed stroke, i.e. coordinates in pixel
 * position of it's nodes, stroke width, color and whatever will be.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DressedStroke implements Serializable {
    public int[] nodes = {0};
    public int[] strokeWidth = {1};
    public int strokeAlpha = 100;
    public int[] rgb = {0, 0, 0};
    public int[] lineStyle = {1};

    public int[] getScaledNodes( double scale ) {
        int[] newNodes = new int[nodes.length];
        for( int i = 0; i < newNodes.length; i++ ) {
            newNodes[i] = (int) Math.round((double) nodes[i] * scale);
        }
        return newNodes;
    }

    public Rectangle getBounds() {
        Rectangle bounds = new Rectangle(0, 0, 1, 1);
        for( int i = 0; i < nodes.length; i = i + 2 ) {
            int x = nodes[i];
            int y = nodes[i + 1];

            bounds.add(new Rectangle(x, y, 1, 1));
        }
        return bounds;
    }

}
