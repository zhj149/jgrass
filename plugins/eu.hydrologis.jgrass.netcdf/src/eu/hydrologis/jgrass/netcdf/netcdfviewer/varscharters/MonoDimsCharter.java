package eu.hydrologis.jgrass.netcdf.netcdfviewer.varscharters;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;

import eu.hydrologis.jgrass.netcdf.NetcdfUtils;

import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;

public class MonoDimsCharter implements ICharter {
    private final Variable variable;
    private final Composite parent;

    public MonoDimsCharter( Variable variable, Composite parent ) {
        this.variable = variable;
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see eu.hydrologis.jgrass.netcdf.netcdfviewer.varscharters.ICharter#dochart()
     */
    public Composite dochart( Integer... indexes ) throws Exception {
        List<Dimension> dimensions = variable.getDimensions();
        Dimension xDimension = null;
        if (dimensions.size() > 0) {
            xDimension = dimensions.get(0);
        } else {
            return null;
        }

        int[] varShape = variable.getShape();
        /*
         * dimension
         */
        boolean isTime = false;
        String unitsString = "";
        String dimName = xDimension.getName();
        Variable dimVar = xDimension.getGroup().findVariable(dimName);
        double[] array = null;
        if (dimVar != null) {
            int[] shape = dimVar.getShape();
            int[] origin = new int[1];
            Array tmpArray = dimVar.read(origin, shape);
            array = new double[shape[0]];
            for( int i = 0; i < shape[0]; i++ ) {
                array[i] = tmpArray.getDouble(i);
            }

            unitsString = dimVar.getUnitsString();
            if (unitsString == null) {
                unitsString = "-";
            }

            if (dimVar instanceof CoordinateAxis1D) {
                CoordinateAxis1D axis = (CoordinateAxis1D) dimVar;
                if (axis != null && axis.getAxisType() != null && axis.getAxisType().equals(AxisType.Time)) {
                    isTime = true;
                }
            }
        } else {
            array = new double[varShape[0]];
            for( int i = 0; i < varShape[0]; i++ ) {
                array[i] = i;
            }
        }
        String xTitle = dimName + (isTime ? "" : "[" + unitsString + "]");

        /*
         * variable
         */
        unitsString = variable.getUnitsString();
        if (unitsString == null) {
            unitsString = "-";
        }
        String yTitle = variable.getName() + "[" + unitsString + "]";

        int[] varOrigin = new int[1];
        Array varArray = variable.read(varOrigin, varShape);

        ChartComposite chartcomposite = null;
        if (isTime) {
            chartcomposite = makeTimeChart(dimVar, varShape, array, xTitle, yTitle, varArray);
        } else {
            chartcomposite = makeLineChart(varShape, array, xTitle, yTitle, varArray);
        }

        return chartcomposite;
    }

    private ChartComposite makeTimeChart( Variable dimVar, int[] varShape, double[] array, String xTitle, String yTitle,
            Array varArray ) throws ParseException {
        TimeSeries varSerie = new TimeSeries(dimVar.getName());
        for( int j = 0; j < varShape[0]; j++ ) {
            double value = varArray.getDouble(j);
            Date date = NetcdfUtils.getTimeValue(dimVar.getUnitsString(), dimVar, j);
            varSerie.add(new Minute(date), value);
        }

        TimeSeriesCollection timeDataset = new TimeSeriesCollection();
        timeDataset.addSeries(varSerie);
        timeDataset.setXPosition(TimePeriodAnchor.MIDDLE);

        JFreeChart theChart = ChartFactory.createTimeSeriesChart("", xTitle, yTitle, timeDataset, true, true, false);
        XYPlot plot = (XYPlot) theChart.getPlot();
        final ChartComposite chartcomposite = new ChartComposite(parent, SWT.None, theChart);
        chartcomposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        chartcomposite.setLayout(new FillLayout());
        chartcomposite.setDisplayToolTips(true);
        chartcomposite.setHorizontalAxisTrace(false);
        chartcomposite.setVerticalAxisTrace(false);
        chartcomposite.setDomainZoomable(true);
        chartcomposite.setRangeZoomable(true);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        return chartcomposite;
    }

    private ChartComposite makeLineChart( int[] varShape, double[] array, String xTitle, String yTitle, Array varArray ) {
        XYSeries varSerie = new XYSeries(variable.getName());
        for( int j = 0; j < varShape[0]; j++ ) {
            double value = varArray.getDouble(j);
            varSerie.add(array[j], value);
        }

        XYSeriesCollection lineDataset = new XYSeriesCollection();
        lineDataset.addSeries(varSerie);

        JFreeChart theChart = ChartFactory.createXYLineChart("", xTitle, yTitle, lineDataset, PlotOrientation.VERTICAL, true,
                true, false);
        XYPlot plot = (XYPlot) theChart.getPlot();
        final ChartComposite chartcomposite = new ChartComposite(parent, SWT.None, theChart);
        chartcomposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        chartcomposite.setLayout(new FillLayout());
        chartcomposite.setDisplayToolTips(true);
        chartcomposite.setHorizontalAxisTrace(false);
        chartcomposite.setVerticalAxisTrace(false);
        chartcomposite.setDomainZoomable(true);
        chartcomposite.setRangeZoomable(true);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        return chartcomposite;
    }
}
