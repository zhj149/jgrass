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
  
package eu.hydrologis.jgrass.charting.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.ui.Drawable;

/**
 * An implementation of the {@link Drawable} interface, to illustrate the use of the
 * {@link org.jfree.chart.annotations.XYDrawableAnnotation} class. Used by MarkerDemo1.java.
 */
public class LineDrawer implements Drawable {

    /** The outline paint. */
    private final Paint outlinePaint;

    /** The outline stroke. */
    private final Stroke outlineStroke;

    /** The fill paint. */

    /**
     * Creates a new instance.
     * 
     * @param outlinePaint the outline paint.
     * @param outlineStroke the outline stroke.
     */
    public LineDrawer( Paint outlinePaint, Stroke outlineStroke ) {
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
    }

    /**
     * Draws the circle.
     * 
     * @param g2 the graphics device.
     * @param area the area in which to draw.
     */
    public void draw( Graphics2D g2, Rectangle2D area ) {
        if (this.outlinePaint != null && this.outlineStroke != null) {
            g2.setPaint(this.outlinePaint);
            g2.setStroke(this.outlineStroke);
        } else {
            g2.setPaint(Color.black);
            g2.setStroke(new BasicStroke(1.0f));
        }

        Line2D line = new Line2D.Double(area.getCenterX(), area.getMinY(),
                area.getCenterX(), area.getMaxY());
        g2.draw(line);
    }
}
