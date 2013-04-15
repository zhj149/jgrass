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
package eu.hydrologis.jgrass.utilitylinkables;

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jfree.ui.RectangleInsets;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IScalarSet;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.charting.datamodels.ChartCreator;
import eu.hydrologis.jgrass.charting.datamodels.MultiXYTimeChartCreator;
import eu.hydrologis.jgrass.charting.datamodels.NumericChartData;
import eu.hydrologis.jgrass.charting.datamodels.NumericChartData.NumericChartDataItem;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * Output {@link ILinkableComponent openmi component} that takes care of charting {@link IScalarSet}s.
 * 
 * <p>
 * The charting engine works in two different ways:
 * <ol>
 * <li><b>without time dependency:</b> we assume that the whole scalarset
 * represents the values to be charted. In that case the first value of the 
 * scalarset represents the number of columns into which divide the values to
 * create the records to be charted.<BR>
 * The first value of every record is considered to be the domain axis value.</li>
 * <li><b>inside a time chain:</b> in that case the scalarset represents
 * the values of a single instant. The first value is discarded as it 
 * represents the number of columns which is the same as the scalarset size
 * minus one. The current {@link ITime time value} entering the getValues is
 * considered the domain axis value. All other values are considered range values.
 * The chart will be updates at every timestep with the new values.
 * <br>
 * It is important to note that in that case the value number of the scalarset 
 * has to be constant.
 * </li>
 * </ol>
 * </p>
 * <p>It is possible to choose several chart types:
 * <ol>
 * <li>{@link OutputChartWriter#LINE LINE}: a simple line chart</li>
 * <li>{@link OutputChartWriter#POINT POINT}: a simple point chart</li>
 * <li>{@link OutputChartWriter#HISTOGRAM HISTOGRAM}: a histogram chart</li>
 * <li>{@link OutputChartWriter#TIMELINE TIMELINE}: a simple line chart that uses the current time</li>
 * <li>{@link OutputChartWriter#TIMEPOINT TIMEPOINT}: a simple point chart that uses the current time</li>
 * <li>{@link OutputChartWriter#TIMEHISTOGRAM TIMEHISTOGRAM}: a histogram chart that uses the current time</li>
 * <li>{@link OutputChartWriter#DISCHARGERAIN DISCHARGERAIN}: a chart that
 * assumes a time chain. That chart panel is split into two charts, in which the 
 * upper one has inverted axis and will represent a histogram rain chart. The first 
 * chartable value will be set as rain, while all the other values will be charted
 * in the lower chart as line plots. 
 * </li>
 * </ol>
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class OutputChartWriter extends ModelsBackbone {

    private static final String DISCHARGERAIN = "DISCHARGERAIN"; //$NON-NLS-1$
    private static final String TIMEHISTOGRAM = "TIMEHISTOGRAM"; //$NON-NLS-1$
    private static final String TIMELINE = "TIMELINE"; //$NON-NLS-1$
    private static final String TIMEPOINT = "TIMEPOINT"; //$NON-NLS-1$
    private static final String HISTOGRAM = "HISTOGRAM"; //$NON-NLS-1$
    private static final String LINE = "LINE"; //$NON-NLS-1$
    private static final String POINT = "POINT"; //$NON-NLS-1$

    private ILink inputLink = null;

    private IInputExchangeItem tableInputEI = null;

    private static final String modelParameters = "...";

    private ChartCreator creator;
    private String deltaTArg;
    private boolean isWidgetOpen = false;
    private String chartType;
    private String chartTitle;
    private String xTitle;
    private String yTitle;
    private List<String> seriesNames;

    public OutputChartWriter() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public OutputChartWriter( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String tableString = null;
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("ochart")) {
                tableString = argument.getValue();
            }
            if (key.equals(ModelsConstants.DELTAT)) {
                deltaTArg = argument.getValue();
            }
        }
        if (tableString == null) {
            throw new ModelsIllegalargumentException(
                    "No string for chart titles was found. Check your command syntax.", this);
        }

        /*
         * header is in the best case: 
         * charttype#charttitle#xtitle#ytitle#serie1#serie2...#serien
         */
        String[] headerSplits = tableString.split("#"); //$NON-NLS-1$

        // set some default values
        List<String> chartValues = new ArrayList<String>(Arrays.asList(LINE, "Chart title",
                "X axis", "Y axis"));
        if (headerSplits.length < 4) {
            out
                    .println("The supplied parameters to the --ochart model are too few. Using default values.");
            out.println("Please consider the following usage string in future:");
            out
                    .println("--ochart-xxx charttype#charttitle#xtitle#ytitle#name_serie_1#name_serie_2...#name_serie_n");
            out.println("and in the case of rain over discharge charts:");
            out
                    .println("--ochart-xxx charttype#charttitle#xtitle#ytitlerain,ytitledischarge#name_serie_1#name_serie_2...#name_serie_n");
        }

        int defaultNum = chartValues.size();
        for( int i = 0; i < headerSplits.length; i++ ) {
            if (i < defaultNum) {
                // substitute the first ones
                chartValues.set(i, headerSplits[i]);
            } else {
                // add the series names
                chartValues.add(headerSplits[i]);
            }
        }

        chartType = chartValues.get(0);
        chartTitle = chartValues.get(1);
        xTitle = chartValues.get(2);
        yTitle = chartValues.get(3);

        seriesNames = new ArrayList<String>();
        for( int i = 4; i < chartValues.size(); i++ ) {
            seriesNames.add(chartValues.get(i));
        }

        tableInputEI = ModelsConstants.createDummyInputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) {
        /*
         * trigger the linked model
         */
        IValueSet valueSet = inputLink.getSourceComponent().getValues(time, inputLink.getID());

        if (valueSet instanceof ScalarSet && time == null && !chartType.equals(DISCHARGERAIN)) {
            // NO TIME DEPENDENCY
            ScalarSet values = (ScalarSet) valueSet;
            int columns = values.get(0).intValue();
            int rows = (values.getCount() - 1) / columns;

            /*
             * create the chart
             */
            final NumericChartData numericChartData = new NumericChartData(1);
            NumericChartDataItem tab = numericChartData.getChartDataItem(0);
            /*
             * title to be taken in the case of composed charts. In that case it
             * is ambiguos which title of which chart should be taken
             */
            tab.bigTitle = chartTitle;
            /*
             * extra string that will be taken to give the tab a name, the title
             * could be non suitable
             */
            tab.chartStringExtra = chartTitle;
            // the title for this chart
            tab.chartTitles.add(chartTitle);
            // x label
            tab.chartXLabels.add(xTitle);
            // y label
            tab.chartYLabels.add(yTitle);

            // series to read are bound to the entering data
            int seriesNum = columns - 1;
            int sIndex = 0;
            // if not all series names were supplied, add some default name
            while( seriesNum > seriesNames.size() ) {
                seriesNames.add("series_" + sIndex++);
            }
            // series labels to visualize are bound to the header supplied
            tab.seriesNames.add((String[]) seriesNames.toArray(new String[seriesNames.size()]));

            // define the data based on the header
            double[][][] chartData = new double[seriesNames.size()][2][rows];
            double[][] tmpData = new double[seriesNum + 1][rows];
            int index = 1;
            for( int j = 0; j < tmpData[0].length; j++ ) {
                for( int i = 0; i < tmpData.length; i++ ) {
                    tmpData[i][j] = values.getScalar(index);
                    index++;
                }
            }

            for( int i = 0; i < seriesNames.size(); i++ ) {
                chartData[i][0] = tmpData[0];
                chartData[i][1] = tmpData[i + 1];
            }

            tab.chartSeriesData.add(chartData);

            /*
             * create the chart using a
             */
            final ChartCreator creator = new MultiXYTimeChartCreator();
            if (tmpData[0].length > 1) {
                creator.HINT_barwidth = tmpData[0][1] - tmpData[0][0];
            }else{
                creator.HINT_barwidth = 0.2;
            }
            // tweak some stuff
            /* create all the charts in the list for every tab? Yes. */
            boolean[] hintCreate = new boolean[seriesNames.size()];
            for( int i = 0; i < hintCreate.length; i++ ) {
                hintCreate[i] = true;
            }
            creator.M_HINT_CREATE_CHART = new boolean[][]{hintCreate};

            /*
             * create the checkboxes to hide and unhide the series?
             */
            boolean[] hintToggle = new boolean[seriesNames.size()];
            for( int i = 0; i < hintToggle.length; i++ ) {
                hintToggle[i] = false;
            }
            creator.M_HINT_CREATE_TOGGLEHIDESERIES = new boolean[][]{hintToggle};

            /* 
             * define the type of chart to create 
             */
            int[] hintType = new int[seriesNames.size()];
            int type = 0;
            if (chartType.equals(LINE)) {
                type = ChartCreator.XYLINECHART;
            } else if (chartType.equals(HISTOGRAM)) {
                type = ChartCreator.XYBARCHART;
            } else if (chartType.equals(POINT)) {
                type = ChartCreator.XYPOINTCHART;
            } else if (chartType.equals(TIMEHISTOGRAM)) {
                type = ChartCreator.TIMEYBARCHART;
            } else if (chartType.equals(TIMELINE)) {
                type = ChartCreator.TIMEYLINECHART;
            } else if (chartType.equals(DISCHARGERAIN)) {
                type = ChartCreator.TIMEYLINECHART;
            } else if (chartType.equals(TIMEPOINT)) {
                type = ChartCreator.TIMEXYPOINTCHART;
            }
            for( int i = 0; i < hintType.length; i++ ) {
                hintType[i] = type;
            }
            creator.M_HINT_CHART_TYPE = new int[][]{hintType};

            /* define the vertical orientation of the chart */
            boolean[] hintOrientation = new boolean[seriesNames.size()];
            for( int i = 0; i < hintOrientation.length; i++ ) {
                hintOrientation[i] = true;
            }
            creator.M_HINT_CHARTORIENTATION_UP = new boolean[][]{hintOrientation};
            /*
             * define the colors of the series, if = null, colors are taken
             * automatically
             */
            creator.M_HINT_CHARTSERIESCOLOR = null;

            Display.getDefault().syncExec(new Runnable(){
                public void run() {
                    Shell chartShell = new Shell(Display.getDefault(), SWT.DIALOG_TRIM | SWT.RESIZE);
                    chartShell.setSize(800, 600);
                    chartShell.setLayout(new GridLayout(1, false));
                    creator.makePlot(chartShell, numericChartData);
                    chartShell.open();
                }
            });

        } else if (valueSet instanceof ScalarSet && time != null
                && !chartType.equals(DISCHARGERAIN)) {
            // chart is timedependent but not a rainchart
            ScalarSet values = (ScalarSet) valueSet;
            Date date = (Date) time;

            if (creator == null) {
                /*
                 * create the chart
                 */
                final NumericChartData numericChartData = new NumericChartData(1);
                NumericChartDataItem tab = numericChartData.getChartDataItem(0);

                /*
                 * title to be taken in the case of composed charts. In that
                 * case it is ambiguos which title of which chart should be
                 * taken
                 */
                tab.bigTitle = chartTitle;
                /*
                 * extra string that will be taken to give the tab a name, the
                 * title could be non suitable
                 */
                tab.chartStringExtra = chartTitle;
                // the title for this chart
                tab.chartTitles.add(chartTitle);
                // x label
                tab.chartXLabels.add(xTitle);
                // y label
                tab.chartYLabels.add(yTitle);

                int columns = values.get(0).intValue();
                int seriesNum = columns;
                int sIndex = 0;
                // if not all series names were supplied, add some default name
                while( seriesNum > seriesNames.size() ) {
                    seriesNames.add("series_" + sIndex++);
                }
                tab.seriesNames.add((String[]) seriesNames.toArray(new String[seriesNames.size()]));

                // define the data based on the header
                int rows = 1;
                double[][][] chartData = new double[seriesNames.size()][2][rows];

                for( int i = 0; i < chartData.length; i++ ) {
                    chartData[i][0][0] = (double) date.getTime();
                    chartData[i][1][0] = values.getScalar(i + 1);
                }

                tab.chartSeriesData.add(chartData);

                creator = new MultiXYTimeChartCreator();

                // tweak some stuff
                /* create all the charts in the list for every tab? Yes. */
                boolean[] hintCreate = new boolean[seriesNames.size()];
                for( int i = 0; i < hintCreate.length; i++ ) {
                    hintCreate[i] = true;
                }
                creator.M_HINT_CREATE_CHART = new boolean[][]{hintCreate};

                /*
                 * create the checkboxes to hide and unhide the series? First
                 * no, second yes
                 */
                boolean[] hintToggle = new boolean[seriesNames.size()];
                for( int i = 0; i < hintToggle.length; i++ ) {
                    hintToggle[i] = true;
                }
                creator.M_HINT_CREATE_TOGGLEHIDESERIES = new boolean[][]{hintToggle};

                /* define the types of chart to create */
                int[] hintType = new int[seriesNames.size()];
                int type = 0;
                if (chartType.equals(LINE)) {
                    type = ChartCreator.XYLINECHART;
                } else if (chartType.equals(HISTOGRAM)) {
                    type = ChartCreator.XYBARCHART;
                } else if (chartType.equals(POINT)) {
                    type = ChartCreator.XYPOINTCHART;
                } else if (chartType.equals(TIMEHISTOGRAM)) {
                    type = ChartCreator.TIMEYBARCHART;
                } else if (chartType.equals(TIMELINE)) {
                    type = ChartCreator.TIMEYLINECHART;
                } else if (chartType.equals(DISCHARGERAIN)) {
                    type = ChartCreator.TIMEYLINECHART;
                } else if (chartType.equals(TIMEPOINT)) {
                    type = ChartCreator.TIMEXYPOINTCHART;
                }
                for( int i = 0; i < hintType.length; i++ ) {
                    hintType[i] = type;
                }
                creator.M_HINT_CHART_TYPE = new int[][]{hintType};

                /* define the vertical orientation of the chart */
                boolean[] hintOrientation = new boolean[seriesNames.size()];
                for( int i = 0; i < hintOrientation.length; i++ ) {
                    hintOrientation[i] = true;
                }
                creator.M_HINT_CHARTORIENTATION_UP = new boolean[][]{hintOrientation};
                /*
                 * define the colors of the series, if = null, colors are taken
                 * automatically
                 */
                creator.M_HINT_CHARTSERIESCOLOR = null;

                Display.getDefault().asyncExec(new Runnable(){
                    public void run() {
                        Shell chartShell = new Shell(Display.getDefault(), SWT.DIALOG_TRIM
                                | SWT.RESIZE);
                        chartShell.setSize(800, 600);
                        chartShell.setLayout(new GridLayout(1, false));
                        creator.makePlot(chartShell, numericChartData);
                        chartShell.open();
                        isWidgetOpen = true;
                    }

                });

            } else {
                // make sure the gui thread has done properly
                int ii = 0;
                while( !isWidgetOpen ) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (ii++ > 100)
                        break;
                }
                // just add the new values to the existing series. Update the chart.
                int sNum = creator.getChartsList().get(0).getSeries().length;
                final double[] tmp = new double[sNum + 1];
                tmp[0] = (double) date.getTime();
                for( int i = 1; i < tmp.length; i++ ) {
                    tmp[i] = values.getScalar(i);
                }
                Display.getDefault().syncExec(new Runnable(){
                    public void run() {
                        creator.getChartsList().get(0).addDataRecordToSeries(tmp);
                    }
                });
            }
        } else if (valueSet instanceof ScalarSet && time != null && chartType.equals(DISCHARGERAIN)) {

            ScalarSet values = (ScalarSet) valueSet;
            Date date = (Date) time;
            if (creator == null) {

                /*
                 * create the chart
                 */
                final NumericChartData numericChartData = new NumericChartData(1);
                NumericChartDataItem tab = numericChartData.getChartDataItem(0);
                /*
                 * title to be taken in the case of composed charts. In that
                 * case it is ambiguos which title of which chart should be
                 * taken
                 */
                tab.bigTitle = chartTitle;
                /*
                 * extra string that will be taken to give the tab a name, the
                 * title could be non suitable
                 */
                tab.chartStringExtra = chartTitle;
                // the title for the rain chart
                tab.chartTitles.add(chartTitle);
                tab.chartXLabels.add(xTitle);
                String[] yTitleSplit = yTitle.split(",");
                if (yTitleSplit.length < 2) {
                    yTitleSplit = new String[]{"rain", "discharge"};
                }
                tab.chartYLabels.add(yTitleSplit[0]);
                // the title for the discharge chart
                tab.chartTitles.add("");
                tab.chartXLabels.add(xTitle);
                tab.chartYLabels.add(yTitleSplit[1]);

                tab.seriesNames.add(new String[]{yTitleSplit[0]});
                double[][][] chartData = new double[1][2][1];
                chartData[0][0][0] = (double) date.getTime();
                chartData[0][1][0] = values.getScalar(2);
                tab.chartSeriesData.add(chartData);

                tab.seriesNames.add(new String[]{yTitleSplit[1]});
                chartData = new double[1][2][1];
                chartData[0][0][0] = (double) date.getTime();
                chartData[0][1][0] = values.getScalar(1);
                tab.chartSeriesData.add(chartData);

                creator = new MultiXYTimeChartCreator();
                // tweak some stuff
                /* create all the charts in the list for every tab? Yes. */
                creator.M_HINT_CREATE_CHART = new boolean[][]{{true, true}};
                /*
                 * create the checkboxes to hide and unhide the series? First
                 * no, second yes
                 */
                creator.M_HINT_CREATE_TOGGLEHIDESERIES = new boolean[][]{{false, false}};
                /* define the types of chart to create */
                creator.M_HINT_CHART_TYPE = new int[][]{{ChartCreator.TIMEYBARCHART,
                        ChartCreator.TIMEYLINECHART}};
                /* define the vertical orientation of the chart */
                creator.M_HINT_CHARTORIENTATION_UP = new boolean[][]{{false, true}};
                /*
                 * define the colors of the series, if = null, colors are taken
                 * automatically
                 */
                creator.M_HINT_CHARTSERIESCOLOR = new Color[1][2][1];
                creator.M_HINT_CHARTSERIESCOLOR[0][0] = new Color[]{Color.blue};
                creator.M_HINT_CHARTSERIESCOLOR[0][1] = new Color[]{Color.red};

                if (deltaTArg != null) {
                    float dt = Float.parseFloat(deltaTArg);
                    creator.HINT_barwidth = dt;
                }
                creator.HINT_labelInsets = new RectangleInsets(30, 5, 5, 5);

                Display.getDefault().asyncExec(new Runnable(){

                    public void run() {
                        Shell chartShell = new Shell(Display.getDefault(), SWT.DIALOG_TRIM
                                | SWT.RESIZE);
                        chartShell.setSize(700, 600);
                        chartShell.setLayout(new GridLayout(1, false));
                        creator.makePlot(chartShell, numericChartData);
                        chartShell.open();
                    }

                });

            } else {
                // just add the new values
                final double[] tmpRain = new double[2];
                tmpRain[0] = (double) date.getTime();
                tmpRain[1] = values.getScalar(2);
                final double[] tmpQ = new double[2];
                tmpQ[0] = (double) date.getTime();
                tmpQ[1] = values.getScalar(1);
                Display.getDefault().syncExec(new Runnable(){
                    public void run() {
                        creator.getChartsList().get(0).addDataRecordToSeries(tmpRain);
                        creator.getChartsList().get(1).addDataRecordToSeries(tmpQ);
                    }
                });
            }
        }
        return null;
    }

    public void addLink( ILink link ) {
        inputLink = link;
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return tableInputEI;
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 0;
    }

    public void removeLink( String linkID ) {
    }
}
