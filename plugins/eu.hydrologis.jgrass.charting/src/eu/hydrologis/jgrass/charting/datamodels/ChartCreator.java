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
package eu.hydrologis.jgrass.charting.datamodels;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import eu.hydrologis.jgrass.charting.impl.JGrassChart;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public abstract class ChartCreator {

    /**
     * the list of created charts
     */
    protected List<JGrassChart> chartsList = new ArrayList<JGrassChart>();

    /**
     * chart with lines and numeric x,y values
     */
    public static int XYLINECHART = 0;
    /**
     * chart with bars and numeric x,y values
     */
    public static int XYBARCHART = 1;
    /**
     * chart with lines and date x values, numeric y values
     */
    public static int TIMEYLINECHART = 2;
    /**
     * chart with bars and date x values, numeric y values
     */
    public static int TIMEYBARCHART = 3;
    /**
     * chart with points and numeric x,y values
     */
    public static int XYPOINTCHART = 4;
    /**
     * chart with points and numeric date,y values
     */
    public static int TIMEXYPOINTCHART = 5;

    /**
     * add the chart at a particular position
     */
    public boolean[][] M_HINT_CREATE_CHART = {{true}};
    /**
     * define the chart type at a particular position: <br>
     * <ul>
     * <li> {@link #XYLINECHART} </li>
     * <li> {@link #XYBARCHART} </li>
     * <li> {@link #TIMEYLINECHART} </li>
     * <li> {@link #TIMEYBARCHART} </li>
     * </ul>
     */
    public int[][] M_HINT_CHART_TYPE = {{0}};

    /**
     * add hiding of series buttons or not for the chart at a particular position
     */
    public boolean[][] M_HINT_CREATE_TOGGLEHIDESERIES = {{false}};
    /**
     * bar width for bar charts
     */
    public double HINT_barwidth = 15.0;
    public boolean HINT_doDisplayToolTips = true;
    public boolean HINT_doDisplayBaseShapes = true;
    public boolean HINT_doFillBaseShapes = true;
    public boolean HINT_doHorizontalAxisTrace = false;
    public boolean HINT_doVerticalAxisTrace = false;
    public boolean HINT_doDomainZoomable = true;
    public boolean HINT_doRangeZoomable = true;
    public boolean HINT_doDomainGridVisible = true;
    public boolean HINT_doRangeGridVisible = true;

    /**
     * annotations
     */
    public double HINT_AnnotationArrowAngle = -Math.PI / 2.0;
    public TextAnchor HINT_AnnotationTextAncor = TextAnchor.BOTTOM_CENTER;
    public Color HINT_AnnotationTextColor = Color.black;
    public Color HINT_AnnotationArrowColor = Color.black;

    /**
     * colors
     */
    public Color[][][] M_HINT_CHARTSERIESCOLOR = {{{Color.blue}}};
    /**
     * orientation
     */
    public boolean[][] M_HINT_CHARTORIENTATION_UP = {{true}};

    /**
     * add select all on none buttons
     */
    public boolean HINT_doToggleTuttiButton = true;

    /**
     * define label insets
     */
    public RectangleInsets HINT_labelInsets = null;

    /**
     * the time pattenr used for date axes
     */
    public String TIMEFORMAT = "yyyy-MM-dd HH:mm"; //$NON-NLS-1$

    public ChartCreator() {
        super();
    }

    public abstract void makePlot( Composite parentComposite, NumericChartData chartData );

    public abstract List<JGrassChart> getChartsList();

}