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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.Minute;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.RectangleInsets;

import eu.hydrologis.jgrass.charting.datamodels.NumericChartData.NumericChartDataItem;
import eu.hydrologis.jgrass.charting.datamodels.TwoVerticalMultiXYTimeChartModel.TwoVerticalMultiXYTimeChartItem;
import eu.hydrologis.jgrass.charting.impl.JGrassChart;
import eu.hydrologis.jgrass.charting.impl.JGrassXYBarChart;
import eu.hydrologis.jgrass.charting.impl.JGrassXYLineChart;
import eu.hydrologis.jgrass.charting.impl.JGrassXYTimeBarChart;
import eu.hydrologis.jgrass.charting.impl.JGrassXYTimeLineChart;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class MultiXYTimeChartCreator extends ChartCreator {

    /**
     * Make a composite with the plot of the supplied chartdata. There are several HINT* variables
     * that can be set to tweak and configure the plot.
     * 
     * @param parentComposite
     * @param chartData
     */
    public void makePlot( Composite parentComposite, NumericChartData chartData ) {
        final int tabNums = chartData.getTabItemNumbers();

        if (tabNums == 0) {
            return;
        }

        Shell dummyShell = null;
        // try {
        // dummyShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        // } catch (Exception e) {
        dummyShell = new Shell(Display.getCurrent(), SWT.None);
        // }
        /*
         * wrapping panel needed in the case of hide checks
         */
        TabFolder tabFolder = null;
        if (tabNums > 1) {
            tabFolder = new TabFolder(parentComposite, SWT.BORDER);
            tabFolder.setLayout(new GridLayout());
            tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                    | GridData.GRAB_VERTICAL));
        }

        for( int i = 0; i < tabNums; i++ ) {
            NumericChartDataItem chartItem = chartData.getChartDataItem(i);
            int chartNums = chartItem.chartSeriesData.size();
            /*
             * are there data to create the lower chart panel
             */
            List<LinkedHashMap<String, Integer>> series = new ArrayList<LinkedHashMap<String, Integer>>();
            List<XYPlot> plots = new ArrayList<XYPlot>();
            List<JFreeChart> charts = new ArrayList<JFreeChart>();

            for( int j = 0; j < chartNums; j++ ) {
                final LinkedHashMap<String, Integer> chartSeries = new LinkedHashMap<String, Integer>();
                XYPlot chartPlot = null;
                JGrassChart chart = null;
                double[][][] cLD = chartItem.chartSeriesData.get(j);

                if (M_HINT_CREATE_CHART[i][j]) {

                    final String[] cT = chartItem.seriesNames.get(j);
                    final String title = chartItem.chartTitles.get(j);
                    final String xT = chartItem.chartXLabels.get(j);
                    final String yT = chartItem.chartYLabels.get(j);

                    if (M_HINT_CHART_TYPE[i][j] == XYLINECHART) {
                        chart = new JGrassXYLineChart(cT, cLD);
                    } else if (M_HINT_CHART_TYPE[i][j] == XYBARCHART) {
                        chart = new JGrassXYBarChart(cT, cLD, HINT_barwidth);
                    } else if (M_HINT_CHART_TYPE[i][j] == TIMEYLINECHART) {
                        chart = new JGrassXYTimeLineChart(cT, cLD, Minute.class);
                        ((JGrassXYTimeLineChart) chart).setTimeAxisFormat(TIMEFORMAT);
                    } else if (M_HINT_CHART_TYPE[i][j] == TIMEYBARCHART) {
                        chart = new JGrassXYTimeBarChart(cT, cLD, Minute.class, HINT_barwidth);
                        ((JGrassXYTimeBarChart) chart).setTimeAxisFormat(TIMEFORMAT);
                    } else if (M_HINT_CHART_TYPE[i][j] == XYPOINTCHART) {
                        chart = new JGrassXYLineChart(cT, cLD);
                        ((JGrassXYLineChart) chart).toggleLineShapesDisplay(false, true);
                    } else if (M_HINT_CHART_TYPE[i][j] == TIMEXYPOINTCHART) {
                        chart = new JGrassXYTimeLineChart(cT, cLD, Minute.class);
                        ((JGrassXYTimeLineChart) chart).setTimeAxisFormat(TIMEFORMAT);
                        ((JGrassXYTimeLineChart) chart).toggleLineShapesDisplay(false, true);
                    } else {
                        chart = new JGrassXYLineChart(cT, cLD);
                    }

                    final Composite p1Composite = new Composite(dummyShell, SWT.None);
                    p1Composite.setLayout(new FillLayout());
                    p1Composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                    chart.makeChartPanel(p1Composite, title, xT, yT, null, true, true, true, true);
                    chartPlot = (XYPlot) chart.getPlot();
                    XYItemRenderer renderer = chartPlot.getRenderer();

                    chartPlot.setDomainGridlinesVisible(HINT_doDomainGridVisible);
                    chartPlot.setRangeGridlinesVisible(HINT_doRangeGridVisible);

                    if (HINT_doDisplayToolTips) {
                        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
                    }

                    if (!M_HINT_CHARTORIENTATION_UP[i][j]) {
                        chartPlot.getRangeAxis().setInverted(true);
                    }
                    if (M_HINT_CHARTSERIESCOLOR != null) {
                        final XYItemRenderer rend = renderer;

                        for( int k = 0; k < cLD.length; k++ ) {
                            rend.setSeriesPaint(k, M_HINT_CHARTSERIESCOLOR[i][j][k]);
                        }
                    }
                    chart.toggleFilledShapeDisplay(HINT_doDisplayBaseShapes, HINT_doFillBaseShapes,
                            true);

                    for( int k = 0; k < cT.length; k++ ) {
                        chartSeries.put(cT[k], k);
                    }
                    series.add(chartSeries);
                    chartPlot.setNoDataMessage("No data available");
                    chartPlot.setNoDataMessagePaint(Color.red);
                    plots.add(chartPlot);

                    charts.add(chart.getChart(title, xT, yT, null, true, HINT_doDisplayToolTips,
                            true));
                    chartsList.add(chart);

                    /*
                     * add annotations?
                     */
                    if (chartItem.annotationsOnChart.size() > 0) {
                        LinkedHashMap<String, double[]> annotations = chartItem.annotationsOnChart
                                .get(j);
                        if (annotations.size() > 0) {
                            Set<String> keys = annotations.keySet();
                            for( String key : keys ) {
                                double[] c = annotations.get(key);
                                XYPointerAnnotation ann = new XYPointerAnnotation(key, c[0], c[1],
                                        HINT_AnnotationArrowAngle);
                                ann.setTextAnchor(HINT_AnnotationTextAncor);
                                ann.setPaint(HINT_AnnotationTextColor);
                                ann.setArrowPaint(HINT_AnnotationArrowColor);
                                // ann.setArrowLength(15);
                                renderer.addAnnotation(ann);

                                // Marker currentEnd = new ValueMarker(c[0]);
                                // currentEnd.setPaint(Color.red);
                                // currentEnd.setLabel("");
                                // currentEnd.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
                                // currentEnd.setLabelTextAnchor(TextAnchor.TOP_LEFT);
                                // chartPlot.addDomainMarker(currentEnd);

                                // Drawable cd = new LineDrawer(Color.red, new BasicStroke(1.0f));
                                // XYAnnotation bestBid = new XYDrawableAnnotation(c[0], c[1]/2.0,
                                // 0, c[1],
                                // cd);
                                // chartPlot.addAnnotation(bestBid);
                                // pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));
                            }
                        }
                    }
                }

            }

            JFreeChart theChart = null;

            if (plots.size() > 1) {

                ValueAxis domainAxis = null;
                if (M_HINT_CHART_TYPE[i][0] == ChartCreator.TIMEYBARCHART
                        || M_HINT_CHART_TYPE[i][0] == ChartCreator.TIMEYLINECHART) {

                    domainAxis = (plots.get(0)).getDomainAxis();
                } else {
                    domainAxis = new NumberAxis(chartItem.chartXLabels.get(0));
                }

                final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(domainAxis);
                plot.setGap(10.0);
                // add the subplots...
                for( int k = 0; k < plots.size(); k++ ) {
                    XYPlot tmpPlot = plots.get(k);

                    if (HINT_labelInsets != null) {
                        tmpPlot.getRangeAxis().setLabelInsets(HINT_labelInsets);
                    }

                    plot.add(tmpPlot, k + 1);
                }
                plot.setOrientation(PlotOrientation.VERTICAL);

                theChart = new JFreeChart(chartItem.bigTitle, JFreeChart.DEFAULT_TITLE_FONT, plot,
                        true);
            } else if (plots.size() == 1) {
                theChart = new JFreeChart(chartItem.chartTitles.get(0),
                        JFreeChart.DEFAULT_TITLE_FONT, plots.get(0), true);
            } else {
                return;
            }

            /*
             * create the chart composite
             */
            Composite tmp;
            if (tabNums > 1 && tabFolder != null) {
                tmp = new Composite(tabFolder, SWT.None);
            } else {
                tmp = new Composite(parentComposite, SWT.None);
            }
            tmp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                    | GridData.GRAB_VERTICAL));
            tmp.setLayout(new GridLayout());
            final ChartComposite frame = new ChartComposite(tmp, SWT.None, theChart, 680, 420, 300,
                    200, 700, 500, false, true, // properties
                    true, // save
                    true, // print
                    true, // zoom
                    true // tooltips
            );

            // public static final boolean DEFAULT_BUFFER_USED = false;
            // public static final int DEFAULT_WIDTH = 680;
            // public static final int DEFAULT_HEIGHT = 420;
            // public static final int DEFAULT_MINIMUM_DRAW_WIDTH = 300;
            // public static final int DEFAULT_MINIMUM_DRAW_HEIGHT = 200;
            // public static final int DEFAULT_MAXIMUM_DRAW_WIDTH = 800;
            // public static final int DEFAULT_MAXIMUM_DRAW_HEIGHT = 600;
            // public static final int DEFAULT_ZOOM_TRIGGER_DISTANCE = 10;

            frame.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                    | GridData.GRAB_VERTICAL));
            frame.setLayout(new FillLayout());
            frame.setDisplayToolTips(HINT_doDisplayToolTips);
            frame.setHorizontalAxisTrace(HINT_doHorizontalAxisTrace);
            frame.setVerticalAxisTrace(HINT_doVerticalAxisTrace);
            frame.setDomainZoomable(HINT_doDomainZoomable);
            frame.setRangeZoomable(HINT_doRangeZoomable);

            if (tabNums > 1 && tabFolder != null) {
                final TabItem item = new TabItem(tabFolder, SWT.NONE);
                item.setText(chartData.getChartDataItem(i).chartStringExtra);
                item.setControl(tmp);
            }

            /*
             * create the hide toggling part
             */
            for( int j = 0; j < plots.size(); j++ ) {

                if (M_HINT_CREATE_TOGGLEHIDESERIES[i][j]) {
                    final LinkedHashMap<Button, Integer> allButtons = new LinkedHashMap<Button, Integer>();
                    Group checksComposite = new Group(tmp, SWT.None);
                    checksComposite.setText("");
                    RowLayout rowLayout = new RowLayout();
                    rowLayout.wrap = true;
                    rowLayout.type = SWT.HORIZONTAL;
                    checksComposite.setLayout(rowLayout);
                    checksComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                            | GridData.GRAB_HORIZONTAL));

                    final XYItemRenderer renderer = plots.get(j).getRenderer();
                    Set<String> lTitles = series.get(j).keySet();
                    for( final String title : lTitles ) {
                        final Button b = new Button(checksComposite, SWT.CHECK);
                        b.setText(title);
                        b.setSelection(true);
                        final int index = series.get(j).get(title);
                        b.addSelectionListener(new SelectionAdapter(){
                            public void widgetSelected( SelectionEvent e ) {
                                boolean visible = renderer.getItemVisible(index, 0);
                                renderer.setSeriesVisible(index, new Boolean(!visible));
                            }
                        });
                        allButtons.put(b, index);
                    }

                    /*
                     * toggle all and none
                     */
                    if (HINT_doToggleTuttiButton) {
                        Composite allchecksComposite = new Composite(tmp, SWT.None);
                        RowLayout allrowLayout = new RowLayout();
                        allrowLayout.wrap = true;
                        allrowLayout.type = SWT.HORIZONTAL;
                        allchecksComposite.setLayout(allrowLayout);
                        allchecksComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                                | GridData.GRAB_HORIZONTAL));

                        final Button tuttiButton = new Button(allchecksComposite, SWT.BORDER
                                | SWT.PUSH);
                        tuttiButton.setText("Tutti");
                        tuttiButton.addSelectionListener(new SelectionAdapter(){
                            public void widgetSelected( SelectionEvent e ) {
                                Set<Button> set = allButtons.keySet();
                                for( Button button : set ) {
                                    button.setSelection(true);
                                    int i = allButtons.get(button);
                                    if (renderer != null) {
                                        renderer.setSeriesVisible(i, new Boolean(true));
                                    }
                                }
                            }
                        });
                        final Button noneButton = new Button(allchecksComposite, SWT.BORDER
                                | SWT.PUSH);
                        noneButton.setText("Nessuno");
                        noneButton.addSelectionListener(new SelectionAdapter(){
                            public void widgetSelected( SelectionEvent e ) {
                                Set<Button> set = allButtons.keySet();
                                for( Button button : set ) {
                                    button.setSelection(false);
                                    int i = allButtons.get(button);
                                    if (renderer != null) {
                                        renderer.setSeriesVisible(i, new Boolean(false));
                                    }
                                }
                            }
                        });
                    }

                }

            }

        }
    }
    public Dataset chartSeriesToDatasetList( TwoVerticalMultiXYTimeChartModel chartData ) {
        final List<TwoVerticalMultiXYTimeChartItem> _chartsData = chartData.getChartsDataItems();
        final List<XYSeries> chartSeries = new ArrayList<XYSeries>();

        for( int i = 0; i < _chartsData.size(); i++ ) {
            final TwoVerticalMultiXYTimeChartItem tmpCD = _chartsData.get(i);
            final String title = chartData.getChartTitle(i);
            final List<double[][]> valuesList = tmpCD.getLowerChartSeriesData();

            for( final double[][] values : valuesList ) {
                final XYSeries xySeries = new XYSeries(title);
                for( int j = 0; j < values[0].length; j++ ) {
                    // important: the data matrix has to be passed as two rows (not
                    // two columns)
                    xySeries.add(values[0][j], values[1][j]);
                }
                chartSeries.add(xySeries);
            }
        }

        final XYSeriesCollection lineDataset = new XYSeriesCollection();
        for( int i = 0; i < chartSeries.size(); i++ ) {
            lineDataset.addSeries(chartSeries.get(i));
        }

        return lineDataset;
    }

    public static void main( String[] args ) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new GridLayout());

        NumericChartData numericChartData = new NumericChartData(2);

        /*
         * first tab
         */
        NumericChartDataItem tab1 = numericChartData.getChartDataItem(0);
        /*
         * title to be taken in the case of composed charts. In that case it is ambiguos which title
         * of which chart should be taken
         */
        tab1.bigTitle = "Title of tab 1";
        /*
         * extra string that will be taken to give the tab a name, the title could be non suitable
         */
        tab1.chartStringExtra = "Text of tab 1";
        /*
         * in tab 1: first chart with 2 series
         */
        // the title for this chart
        tab1.chartTitles.add("Chart title 1 in tab 1");
        // x label
        tab1.chartXLabels.add("X data");
        // y label
        tab1.chartYLabels.add("Y data");
        // define some data
        double[][][] data11 = new double[][][]{{{-1, -2, -3}, {2, 4, 6}}, {{1, 2, 3}, {2, 4, 6}}};
        tab1.chartSeriesData.add(data11);
        // give the series for this chart a name
        tab1.seriesNames.add(new String[]{"series 1", "series 2"});
        /*
         * in tab 1: second chart with 1 serie
         */
        tab1.seriesNames.add(new String[]{"series 1"});
        double[][][] data12 = new double[][][]{{{1, 2, 3}, {2, 4, 6}}};
        tab1.chartTitles.add("Chart title 2 in tab 1");
        tab1.chartXLabels.add("X data");
        tab1.chartYLabels.add("Y data");
        tab1.chartSeriesData.add(data12);

        /*
         * second tab
         */
        NumericChartDataItem tab2 = numericChartData.getChartDataItem(1);
        tab2.bigTitle = "Title of tab 2";
        tab2.chartStringExtra = "Text of tab 2";
        /*
         * in tab 2: one single chart with 3 series
         */
        tab2.chartTitles.add("Chart title 1 in tab 2");
        tab2.chartXLabels.add("X data");
        tab2.chartYLabels.add("Y data");
        double[][][] data2 = new double[][][]{{{-1, -2, -3}, {2, 4, 6}}, {{1, 2, 3}, {2, 4, 6}},
                {{1, 2, 3}, {-2, -4, -6}}};
        tab2.chartSeriesData.add(data2);
        tab2.seriesNames.add(new String[]{"series 1", "series 2", "series 3"});

        /*
         * create the chart using a
         */
        ChartCreator creator = new MultiXYTimeChartCreator();
        // tweak some stuff
        /* create all the charts in the list for every tab? Yes. */
        creator.M_HINT_CREATE_CHART = new boolean[][]{{true, true}, {true, false}};
        /* create the checkboxes to hide and unhide the series? First no, second yes */
        creator.M_HINT_CREATE_TOGGLEHIDESERIES = new boolean[][]{{false, false}, {true, false}};
        /* define the types of chart to create */
        creator.M_HINT_CHART_TYPE = new int[][]{
                {ChartCreator.XYBARCHART, ChartCreator.XYLINECHART}, {ChartCreator.XYLINECHART, -1}};
        /* define the vertical orientation of the chart */
        creator.M_HINT_CHARTORIENTATION_UP = new boolean[][]{{false, true}, {true, true}};
        /*
         * define the colors of the series, if = null, colors are taken automatically
         */
        creator.M_HINT_CHARTSERIESCOLOR = new Color[2][2][3];
        // tab 1, chart 1, all series
        creator.M_HINT_CHARTSERIESCOLOR[0][0] = new Color[]{Color.blue, Color.red, null};
        // tab 1, chart 2, all series
        creator.M_HINT_CHARTSERIESCOLOR[0][1] = new Color[]{Color.green, null, null};
        // tab 2, chart 1, all series
        creator.M_HINT_CHARTSERIESCOLOR[1][0] = new Color[]{Color.blue, Color.red, Color.yellow};

        /*
         * finally create that plot
         */
        creator.makePlot(shell, numericChartData);

        shell.pack();
        shell.open();
        while( !shell.isDisposed() ) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    public List<JGrassChart> getChartsList() {
        return chartsList;
    }
}
