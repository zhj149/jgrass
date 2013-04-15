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

import java.awt.Color;

import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Series;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.Second;
import org.jfree.data.time.Year;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.RectangleInsets;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public abstract class JGrassChart {

    protected JFreeChart theChart = null;
    protected Plot thePlot = null;
    protected ChartComposite chartComposite = null;
    /**
     * value for which input data are ignored
     */

    protected Class[] timeClasses = new Class[]{Year.class, Month.class, Day.class, Hour.class,
            Minute.class, Second.class};

    /**
     * The creation of the chart object
     * 
     * @param title - title of the chart
     * @param xLabel - the x axis label
     * @param yLabel - the y axis label
     * @param porient - the orientation (if null = VERTICAL)
     * @param withLegend
     * @param withTooltips
     * @param withUrls
     * @return - the chart
     */
    public abstract JFreeChart getChart( String title, String xLabel, String yLabel,
            PlotOrientation porient, boolean withLegend, boolean withTooltips, boolean withUrls );

    /**
     * Get the final panel with the chart in it. With this method also particolar widget parts can
     * be added, like for example the hiding checkboxes to hide one or the other chart.
     * 
     * @param title - title of the chart
     * @param xLabel - the x axis label
     * @param yLabel - the y axis label
     * @param porient - the orientation (if null = VERTICAL)
     * @param withLegend
     * @param withTooltips
     * @param withUrls
     * @param hideEngine
     * @return - the panel containing the chart
     */
    public abstract void makeChartPanel( Composite parent, String title, String xLabel,
            String yLabel, PlotOrientation porient, boolean withLegend, boolean withTooltips,
            boolean withUrls, boolean hideEngine );

    /**
     * get the plot from the chart object (usefull for combined charts)
     * 
     * @return the plot
     */
    public abstract Plot getPlot();

    public abstract Series[] getSeries();

    public abstract void addDataRecordToSeries( double... record );

    /**
     * Sets the background color of the plot area
     * 
     * @param bgcolor
     */
    public void setBackgroundColor( Color bgColor ) {
        if (thePlot != null) {
            thePlot.setBackgroundPaint(bgColor);
        }
    }

    /**
     * Defines whether or not the shapes are to be filled
     * 
     * @param showShapes
     * @param fillShapes
     */
    public abstract void toggleFilledShapeDisplay( boolean showShapes, boolean fillShapes,
            boolean plotLines );

    /**
     * Possibility to zoom and unzoom with the mouse in the panel
     * 
     * @param horiz
     * @param vertical
     */
    public void setChartZooming( boolean horiz, boolean vertical ) {
        if (chartComposite != null) {
            chartComposite.setDomainZoomable(horiz);
            chartComposite.setRangeZoomable(vertical);
        }
    }

    /**
     * Sets the axis offsets (gap between the data area and the axes).
     * 
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setAxisOffset( double left, double top, double right, double bottom ) {
        if (thePlot != null) {
            ((XYPlot) thePlot).setAxisOffset(new RectangleInsets(left, top, right, bottom));
        }
    }
    /**
     * Sets the paint for the grid lines plotted against the domain axis (null = no grid lines
     * drawn).
     * 
     * @param domainGridColor - the color to use
     */
    public void setDomainGridlineColor( Color domainGridColor ) {
        if (thePlot != null) {
            ((XYPlot) thePlot).setDomainGridlinePaint(domainGridColor);
        }
    }

    /**
     * Sets the paint for the grid lines plotted against the range axis
     * 
     * @param rangeGridColor - the color to use
     */
    public void setRangeGridlineColor( Color rangeGridColor ) {
        if (thePlot != null) {
            ((XYPlot) thePlot).setRangeGridlinePaint(rangeGridColor);
        }
    }

    /**
     * Sets the flag indicating whether or not the domain crosshair is visible.
     * 
     * @param isvisible
     */
    public void setDomainCrosshair( boolean isvisible ) {
        if (thePlot != null) {
            ((XYPlot) thePlot).setDomainCrosshairVisible(isvisible);
        }
    }

    /**
     * Sets the flag indicating whether or not the range crosshair is visible.
     * 
     * @param isvisible
     */
    public void setRangeCrosshair( boolean isvisible ) {
        if (thePlot != null) {
            ((XYPlot) thePlot).setRangeCrosshairVisible(isvisible);
        }
    }

}
