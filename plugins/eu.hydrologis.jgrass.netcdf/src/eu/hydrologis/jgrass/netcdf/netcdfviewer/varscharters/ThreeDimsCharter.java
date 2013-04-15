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
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis1D;

public class ThreeDimsCharter implements ICharter {
    private final Variable variable;
    private final Composite parent;

    public ThreeDimsCharter( Variable variable, Composite parent ) {
        this.variable = variable;
        this.parent = parent;
    }

    public Composite dochart( Integer... indexes ) throws Exception {
        List<Dimension> dimensions = variable.getDimensions();
        Dimension x1Dimension = dimensions.get(indexes[0]);
        Dimension x2Dimension = dimensions.get(indexes[1]);

        int[] varShape = variable.getShape();
        /*
         * dimension 1
         */
        boolean isTime = false;
        String unitsString = "";
        String dimName = x1Dimension.getName();
        Variable dim1Var = x1Dimension.getGroup().findVariable(dimName);
        double[] x1Array = null;
        if (dim1Var != null) {
            int[] shape = dim1Var.getShape();
            int[] origin = new int[1];
            Array tmpArray = dim1Var.read(origin, shape);
            x1Array = new double[shape[0]];
            for( int i = 0; i < shape[0]; i++ ) {
                x1Array[i] = tmpArray.getDouble(i);
            }

            unitsString = dim1Var.getUnitsString();
            if (unitsString == null) {
                unitsString = "-";
            }
            
            if (dim1Var instanceof CoordinateAxis1D) {
                CoordinateAxis1D axis = (CoordinateAxis1D) dim1Var;
                if (axis != null && axis.getAxisType() != null && axis.getAxisType().equals(AxisType.Time)) {
                    isTime = true;
                }
            }
        } else {
            x1Array = new double[varShape[indexes[0]]];
            for( int i = 0; i < varShape[indexes[0]]; i++ ) {
                x1Array[i] = i;
            }
            unitsString = "-";
        }
        String x1Title = dimName + (isTime ? "" : "[" + unitsString + "]");
        /*
         * dimension 2
         */
        unitsString = "";
        dimName = x2Dimension.getName();
        Variable dim2Var = x2Dimension.getGroup().findVariable(dimName);
        double[] x2Array = null;
        if (dim2Var != null) {
            int[] shape = dim2Var.getShape();
            int[] origin = new int[1];
            Array tmpArray = dim2Var.read(origin, shape);
            x2Array = new double[shape[0]];
            for( int i = 0; i < shape[0]; i++ ) {
                x2Array[i] = tmpArray.getDouble(i);
            }
        } else {
            x2Array = new double[varShape[indexes[1]]];
            for( int i = 0; i < varShape[indexes[1]]; i++ ) {
                x2Array[i] = i;
            }
        }

        /*
         * variable
         */
        unitsString = variable.getUnitsString();
        if (unitsString == null) {
            unitsString = "-";
        }
        String yTitle = variable.getName() + "[" + unitsString + "]";

        int[] varOrigin = new int[3];
        Array varArray = variable.read(varOrigin, varShape);

        
        ChartComposite chartcomposite = null;
        if (isTime) {
            chartcomposite = makeTimeChart(dim1Var, x1Array, x1Title, x2Array, yTitle, varArray, indexes);
        } else {
            chartcomposite = makeLineChart(x1Array, x1Title, x2Array, yTitle, varArray, indexes);
        }

        return chartcomposite;
    }


    private ChartComposite makeTimeChart( Variable dimVar, double[] x1Array, String x1Title, double[] x2Array, String yTitle,
            Array varArray, Integer[] indexes ) throws ParseException {
        TimeSeries[] chartSeries = new TimeSeries[x2Array.length];
        Index index = varArray.getIndex();
        for( int i = 0; i < x2Array.length; i++ ) {
            double x2Value = x2Array[i];
            TimeSeries varSerie = new TimeSeries(variable.getName() + " - " + x2Value);

            for( int j = 0; j < x1Array.length; j++ ) {
                if (indexes[0] == 0 && indexes[1] == 1) {
                    index.set(j, i, 0);
                } else if (indexes[0] == 1 && indexes[1] == 0) {
                    index.set(i, j, 0);
                } else if (indexes[0] == 1 && indexes[1] == 2) {
                    index.set(0, j, i);
                } else if (indexes[0] == 2 && indexes[1] == 1) {
                    index.set(0, i, j);
                } else if (indexes[0] == 0 && indexes[1] == 2) {
                    index.set(j, 0, i);
                } else if (indexes[0] == 2 && indexes[1] == 0) {
                    index.set(i, 0, j);
                }
                Date date = NetcdfUtils.getTimeValue(dimVar.getUnitsString(), dimVar, j);
                double value = varArray.getDouble(index);
                varSerie.add(new Minute(date), value);
            }
            
            chartSeries[i] = varSerie;
        }
        TimeSeriesCollection timeDataset = new TimeSeriesCollection();
        for( TimeSeries varSerie : chartSeries ) {
            timeDataset.addSeries(varSerie);
        }
        timeDataset.setXPosition(TimePeriodAnchor.MIDDLE);
        
        

        JFreeChart theChart = ChartFactory.createTimeSeriesChart("", x1Title, yTitle, timeDataset, true, true, false);
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

    private ChartComposite makeLineChart( double[] x1Array, String x1Title, double[] x2Array, String yTitle, Array varArray,
            Integer... indexes ) {
        XYSeries[] chartSeries = new XYSeries[x2Array.length];
        Index index = varArray.getIndex();
        for( int i = 0; i < x2Array.length; i++ ) {
            double x2Value = x2Array[i];
            XYSeries varSerie = new XYSeries(variable.getName() + " - " + x2Value);

            for( int j = 0; j < x1Array.length; j++ ) {
                if (indexes[0] == 0 && indexes[1] == 1) {
                    index.set(j, i, 0);
                } else if (indexes[0] == 1 && indexes[1] == 0) {
                    index.set(i, j, 0);
                } else if (indexes[0] == 1 && indexes[1] == 2) {
                    index.set(0, j, i);
                } else if (indexes[0] == 2 && indexes[1] == 1) {
                    index.set(0, i, j);
                } else if (indexes[0] == 0 && indexes[1] == 2) {
                    index.set(j, 0, i);
                } else if (indexes[0] == 2 && indexes[1] == 0) {
                    index.set(i, 0, j);
                }
                double value = varArray.getDouble(index);
                varSerie.add(x1Array[j], value);
            }
            chartSeries[i] = varSerie;
        }
        XYSeriesCollection lineDataset = new XYSeriesCollection();
        for( XYSeries xySeries : chartSeries ) {
            lineDataset.addSeries(xySeries);
        }

        JFreeChart theChart = ChartFactory.createXYLineChart("", x1Title, yTitle, null, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = (XYPlot) theChart.getPlot();
        final ChartComposite chartcomposite = new ChartComposite(parent, SWT.None, theChart);
        chartcomposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
        chartcomposite.setLayout(new FillLayout());
        chartcomposite.setDisplayToolTips(true);
        chartcomposite.setHorizontalAxisTrace(false);
        chartcomposite.setVerticalAxisTrace(false);
        chartcomposite.setDomainZoomable(true);
        chartcomposite.setRangeZoomable(true);
        plot.setDataset(lineDataset);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        return chartcomposite;
    }

}
