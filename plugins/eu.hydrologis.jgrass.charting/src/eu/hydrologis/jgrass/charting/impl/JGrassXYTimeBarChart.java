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

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JCheckBox;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.Series;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

import eu.hydrologis.jgrass.charting.ChartPlugin;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class JGrassXYTimeBarChart extends JGrassChart implements ActionListener {
    private TimeSeriesCollection lineDataset = null;

    private TimeSeries[] chartSeries = null;

    private XYItemRenderer renderer = null;

    private String timeFormat;

    private Constructor<RegularTimePeriod> constructor = null;

    private XYBarDataset dataset;

    /**
     * A line chart creator basing on series made up two values per row. More series, independing
     * one from the other are supported.
     * 
     * @param chartValues - a hashmap containing as keys the name of the series and as values the
     *        double[][] representing the data. In this case the x value is assumed to ge a date.
     *        Important: the data matrix has to be passed as two rows (not two columns)
     * @param barWidth TODO
     */
    public JGrassXYTimeBarChart( LinkedHashMap<String, double[][]> chartValues,
            Class<RegularTimePeriod> timeClass, double barWidth ) {
        try {
            chartSeries = new TimeSeries[chartValues.size()];

            constructor = timeClass.getConstructor(Date.class);

            final Iterator<String> it = chartValues.keySet().iterator();
            int count = 0;
            while( it.hasNext() ) {
                final String key = it.next();
                final double[][] values = chartValues.get(key);

                chartSeries[count] = new TimeSeries(key, timeClass);
                for( int i = 0; i < values[0].length; i++ ) {
                    // important: the data matrix has to be passed as two rows (not
                    // two columns)
                    chartSeries[count].add(constructor.newInstance(new Date((long) values[0][i])),
                            values[1][i]);
                }
                count++;
            }

            lineDataset = new TimeSeriesCollection();
            for( int i = 0; i < chartSeries.length; i++ ) {
                lineDataset.addSeries(chartSeries[i]);
            }
            lineDataset.setXPosition(TimePeriodAnchor.MIDDLE);

            if (barWidth != -1)
                dataset = new XYBarDataset(lineDataset, barWidth);
        } catch (Exception e) {
            ChartPlugin.log("ChartPlugin", e); //$NON-NLS-1$
        }

    }

    public JGrassXYTimeBarChart( List<String> chartTitles, List<double[][]> chartValues,
            Class< ? > timeClass, double barWidth ) {
        try {
            chartSeries = new TimeSeries[chartValues.size()];

            constructor = (Constructor<RegularTimePeriod>) timeClass.getConstructor(Date.class);

            for( int i = 0; i < chartTitles.size(); i++ ) {
                final String title = chartTitles.get(i);
                final double[][] values = chartValues.get(i);

                chartSeries[i] = new TimeSeries(title, timeClass);
                for( int j = 0; j < values[0].length; j++ ) {
                    // important: the data matrix has to be passed as two rows (not
                    // two columns)
                    double val = values[1][j];
                    if (isNovalue(val))
                        continue;
                    chartSeries[i].add(constructor.newInstance(new Date((long) values[0][j])), val);
                }
            }

            lineDataset = new TimeSeriesCollection();
            for( int i = 0; i < chartSeries.length; i++ ) {
                lineDataset.addSeries(chartSeries[i]);
            }
            lineDataset.setXPosition(TimePeriodAnchor.MIDDLE);
            lineDataset.setDomainIsPointsInTime(true);

            if (barWidth != -1)
                dataset = new XYBarDataset(lineDataset, barWidth);

        } catch (Exception e) {
            ChartPlugin.log("ChartPlugin problem", e); //$NON-NLS-1$
        }

    }

    public JGrassXYTimeBarChart( String[] chartTitles, double[][][] chartValues,
            Class< ? > timeClass, double barWidth ) {
        try {
            chartSeries = new TimeSeries[chartValues.length];

            constructor = (Constructor<RegularTimePeriod>) timeClass.getConstructor(Date.class);

            for( int i = 0; i < chartTitles.length; i++ ) {
                final String title = chartTitles[i];
                final double[][] values = chartValues[i];

                chartSeries[i] = new TimeSeries(title, timeClass);
                for( int j = 0; j < values[0].length; j++ ) {
                    // important: the data matrix has to be passed as two rows (not
                    // two columns)
                    double val = values[1][j];
                    if (isNovalue(val))
                        continue;
                    chartSeries[i].add(constructor.newInstance(new Date((long) values[0][j])), val);
                }
            }

            lineDataset = new TimeSeriesCollection();
            for( int i = 0; i < chartSeries.length; i++ ) {
                lineDataset.addSeries(chartSeries[i]);
            }
            lineDataset.setXPosition(TimePeriodAnchor.MIDDLE);
            lineDataset.setDomainIsPointsInTime(true);

            if (barWidth != -1)
                dataset = new XYBarDataset(lineDataset, barWidth);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see jgrass.util.chart.JGrassChart#getChart(java.lang.String, java.lang.String,
     *      java.lang.String, org.jfree.chart.plot.PlotOrientation, boolean, boolean, boolean)
     */
    public JFreeChart getChart( String title, String xLabel, String yLabel,
            PlotOrientation porient, boolean withLegend, boolean withTooltips, boolean withUrls ) {
        if (porient == null) {
            porient = PlotOrientation.VERTICAL;
        }

        if (dataset != null) {
            theChart = ChartFactory.createTimeSeriesChart(title, xLabel, yLabel, dataset,
                    withLegend, withTooltips, withUrls);
        } else {
            theChart = ChartFactory.createTimeSeriesChart(title, xLabel, yLabel, lineDataset,
                    withLegend, withTooltips, withUrls);
        }
        // also create the plot obj for customizations
        thePlot = theChart.getXYPlot();

        XYBarRenderer barRenderer = new XYBarRenderer(0);
        ((XYPlot) thePlot).setRenderer(barRenderer);
        renderer = ((XYPlot) thePlot).getRenderer();

        if (timeFormat != null) {
            // PeriodAxis domainAxis = new PeriodAxis("");
            // PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[1];
            // // PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[infos.length];
            //
            // // for( int i = 0; i < infos.length; i++ ) {
            // // info[infos.length - i - 1] = new PeriodAxisLabelInfo(timeClasses[i],
            // // new SimpleDateFormat(infos[i]));
            // //
            // // }
            // info[0] = new PeriodAxisLabelInfo(Minute.class,
            // new SimpleDateFormat("yyyy-MM-dd HH:mm"));
            //
            // // domainAxis.setAutoRangeTimePeriodClass(Minute.class);
            // // domainAxis.setMajorTickTimePeriodClass(Hour.class);
            // // domainAxis.setMinorTickTimePeriodClass(Minute.class);
            // domainAxis.setLabelAngle(Math.PI);
            //
            // domainAxis.setLabelInfo(info);
            // ((XYPlot) thePlot).setDomainAxis(domainAxis);

            DateAxis axis = (DateAxis) ((XYPlot) thePlot).getDomainAxis();
            axis.setDateFormatOverride(new SimpleDateFormat(timeFormat));

        }

        return theChart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jgrass.util.chart.JGrassChart#getChartPanel(java.lang.String, java.lang.String,
     *      java.lang.String, org.jfree.chart.plot.PlotOrientation, boolean, boolean, boolean,
     *      boolean)
     */
    public void makeChartPanel( Composite parent, String title, String xLabel, String yLabel,
            PlotOrientation porient, boolean withLegend, boolean withTooltips, boolean withUrls,
            boolean hideEngine ) {

        chartComposite = new ChartComposite(parent, SWT.NONE, getChart(title, xLabel, yLabel,
                porient, withLegend, withTooltips, withUrls), true);
        chartComposite.setHorizontalAxisTrace(false);
        chartComposite.setVerticalAxisTrace(false);

    }

    public Plot getPlot() {
        return theChart.getXYPlot();
    }

    /*
     * (non-Javadoc)
     * 
     * @see jgrass.util.chart.JGrassChart#toggleFilledShapeDisplay(boolean, boolean)
     */
    public void toggleFilledShapeDisplay( boolean showShapes, boolean fillShapes, boolean plotLines ) {
    }

    /**
     * Toggel the axis ticks to show a standard thing or just integers
     * 
     * @param showIntegers - flag to activate integer ticks
     */
    public void toggleIntegerTicks( boolean showIntegers ) {
        if (thePlot != null) {
            final NumberAxis rangeAxis = (NumberAxis) ((XYPlot) thePlot).getRangeAxis();

            if (showIntegers) {
                rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            } else {
                rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
            }
        }
    }

    /**
     * Actions called be registered Objects
     * 
     * @param e the action event.
     */
    public void actionPerformed( ActionEvent e ) {
        // if the hide Checkboxes are toggled
        if (e.getSource() instanceof JCheckBox) {
            int series = -1;
            for( int i = 0; i < chartSeries.length; i++ ) {
                if (e.getActionCommand().equals(chartSeries[i].getDescription())) {
                    series = i;
                }
            }

            if (series >= 0) {
                boolean visible = this.renderer.getItemVisible(series, 0);
                this.renderer.setSeriesVisible(series, new Boolean(!visible));
            }
        }

    }

    public void setTimeAxisFormat( String timeFormat ) {
        this.timeFormat = timeFormat;
    }

    public Series[] getSeries() {
        return chartSeries;
    }

    public void addDataRecordToSeries( double... record ) {
        try {
            for( int i = 0; i < chartSeries.length; i++ ) {
                chartSeries[i].add(constructor.newInstance(new Date((long) record[0])),
                        record[i + 1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
