//package eu.hydrologis.jgrass.netcdf.netcdfviewer.varscharters;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.widgets.Composite;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.plot.CombinedDomainXYPlot;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//
//import eu.hydrologis.jgrass.netcdf.netcdfviewer.ChartComposite;
//
//import ucar.nc2.Variable;
//
//public class ScalarCharter {
//    private final Variable variable;
//    private final Composite parent;
//
//    public ScalarCharter( Variable variable , Composite parent) {
//        this.variable = variable;
//        this.parent = parent;
//    }
//    
//    
//    public void dochart() {
//        
//        JFreeChart chart2 = ChartFactory.createXYLineChart("", "Tempo [h]", "Portata [mc/s]", null,
//                PlotOrientation.VERTICAL, true, true, false);
//        dischargePlot = (XYPlot) chart2.getPlot();
//
//        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(dischargePlot.getDomainAxis());
//
//        plot.setGap(10.0);
//        // add the subplots...
//        plot.add(rainPlot, 1);
//        plot.add(dischargePlot, 2);
//
//        plot.setOrientation(PlotOrientation.VERTICAL);
//        JFreeChart theChart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
//
//        final ChartComposite frame = new ChartComposite(related, SWT.None, theChart);
//        frame.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
//                | GridData.GRAB_VERTICAL));
//        frame.setLayout(new FillLayout());
//        frame.setDisplayToolTips(true);
//        frame.setHorizontalAxisTrace(false);
//        frame.setVerticalAxisTrace(false);
//        frame.setDomainZoomable(true);
//        frame.setRangeZoomable(true);
//    }
//    
//}
