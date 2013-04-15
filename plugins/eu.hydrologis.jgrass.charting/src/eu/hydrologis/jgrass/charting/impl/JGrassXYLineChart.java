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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JCheckBox;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;


/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class JGrassXYLineChart extends JGrassChart implements ActionListener {
    private XYSeriesCollection lineDataset = null;

    private XYSeries[] chartSeries = null;

    private XYLineAndShapeRenderer renderer = null;

    private boolean showLines = true;

    private boolean showShapes = true;

    /**
     * A line chart creator basing on series made up two values per row. More series, independing
     * one from the other are supported.
     * 
     * @param chartValues - a hashmap containing as keys the name of the series and as values the
     *        double[][] representing the data. Important: the data matrix has to be passed as two
     *        rows (not two columns)
     */
    public JGrassXYLineChart( LinkedHashMap<String, double[][]> chartValues ) {
        chartSeries = new XYSeries[chartValues.size()];
        // extrapolate the data from the Hashmap and convert it to a XYSeries
        // Collection
        final Iterator<String> it = chartValues.keySet().iterator();
        int count = 0;
        while( it.hasNext() ) {
            final String key = it.next();
            final double[][] values = chartValues.get(key);

            chartSeries[count] = new XYSeries(key);
            for( int i = 0; i < values[0].length; i++ ) {
                // important: the data matrix has to be passed as two rows (not
                // two columns)
                double val = values[1][i];
                if (isNovalue(val))
                    continue;
                chartSeries[count].add(values[0][i], val);
            }
            count++;
        }

        lineDataset = new XYSeriesCollection();
        for( int i = 0; i < chartSeries.length; i++ ) {
            lineDataset.addSeries(chartSeries[i]);
        }

    }

    public JGrassXYLineChart( List<String> seriesNames, List<double[][]> chartValues ) {
        chartSeries = new XYSeries[chartValues.size()];

        for( int i = 0; i < seriesNames.size(); i++ ) {
            final String title = seriesNames.get(i);
            final double[][] values = chartValues.get(i);

            chartSeries[i] = new XYSeries(title);
            for( int j = 0; j < values[0].length; j++ ) {
                // important: the data matrix has to be passed as two rows (not
                // two columns)
                double val = values[1][j];
                if (isNovalue(val))
                    continue;
                chartSeries[i].add(values[0][j], val);
            }
        }

        lineDataset = new XYSeriesCollection();
        for( int i = 0; i < chartSeries.length; i++ ) {
            lineDataset.addSeries(chartSeries[i]);
        }

    }

    public JGrassXYLineChart( String[] seriesNames, double[][][] chartSeriesValues ) {
        chartSeries = new XYSeries[chartSeriesValues.length];

        for( int i = 0; i < seriesNames.length; i++ ) {
            final String title = seriesNames[i];
            final double[][] values = chartSeriesValues[i];

            chartSeries[i] = new XYSeries(title);
            for( int j = 0; j < values[0].length; j++ ) {
                // important: the data matrix has to be passed as two rows (not
                // two columns)
                double val = values[1][j];
                if (isNovalue(val))
                    continue;
                chartSeries[i].add(values[0][j], val);
            }
        }

        lineDataset = new XYSeriesCollection();
        for( int i = 0; i < chartSeries.length; i++ ) {
            lineDataset.addSeries(chartSeries[i]);
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

        theChart = ChartFactory.createXYLineChart(title, xLabel, yLabel, lineDataset, porient,
                withLegend, withTooltips, withUrls);
        // also create the plot obj for customizations
        thePlot = theChart.getXYPlot();
        ((XYPlot) thePlot).setRenderer(new XYLineAndShapeRenderer());
        renderer = (XYLineAndShapeRenderer) ((XYPlot) thePlot).getRenderer();

        for( int i = 0; i < chartSeries.length; i++ ) {
            ((XYLineAndShapeRenderer) renderer).setSeriesLinesVisible(i, showLines);
            ((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(i, showShapes);
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

        // Composite wrapChart

        chartComposite = new ChartComposite(parent, SWT.NONE, getChart(title, xLabel, yLabel,
                porient, withLegend, withTooltips, withUrls), true);
        chartComposite.setHorizontalAxisTrace(false);
        chartComposite.setVerticalAxisTrace(false);

        // if (hideEngine) {
        // JPanel wrapChart = new JPanel(new BorderLayout());
        // JPanel boxPanel = new JPanel();
        //
        // for( int i = 0; i < chartSeries.length; i++ ) {
        // // we want the series to be hideable
        // JCheckBox box = new JCheckBox(chartSeries[i].getDescription());
        // box.setActionCommand(chartSeries[i].getDescription());
        // box.addActionListener(this);
        // box.setSelected(true);
        // boxPanel.add(box);
        // }
        //
        // wrapChart.add(chartPanel);
        // wrapChart.add(boxPanel, BorderLayout.SOUTH);
        //
        // return wrapChart;
        // }

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
        if (thePlot != null) {
            renderer.setBaseShapesVisible(showShapes);
            renderer.setBaseShapesFilled(fillShapes);
        }
    }

    public void toggleLineShapesDisplay( boolean showLines, boolean showShapes ) {
        this.showLines = showLines;
        this.showShapes = showShapes;
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
    
    public Series[] getSeries() {
        return chartSeries;
    }
    
    public void addDataRecordToSeries( double... record ) {
        for( int i = 0; i < chartSeries.length; i++ ) {
            chartSeries[i].add(record[0], record[i + 1]);
        }
    }

}
