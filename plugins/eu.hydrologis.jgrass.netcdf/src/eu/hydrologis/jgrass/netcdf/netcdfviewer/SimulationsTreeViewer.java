///*
// * JGrass - Free Open Source Java GIS http://www.jgrass.org 
// * (C) HydroloGIS - www.hydrologis.com 
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package eu.hydrologis.jgrass.netcdf.netcdfviewer;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.text.Format;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Set;
//import java.util.Vector;
//
//import org.eclipse.jface.layout.TableColumnLayout;
//import org.eclipse.jface.viewers.CheckStateChangedEvent;
//import org.eclipse.jface.viewers.CheckboxTreeViewer;
//import org.eclipse.jface.viewers.ColumnWeightData;
//import org.eclipse.jface.viewers.ILabelProvider;
//import org.eclipse.jface.viewers.ILabelProviderListener;
//import org.eclipse.jface.viewers.ISelectionChangedListener;
//import org.eclipse.jface.viewers.ITreeContentProvider;
//import org.eclipse.jface.viewers.SelectionChangedEvent;
//import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.events.TreeEvent;
//import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.DirectoryDialog;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.TableItem;
//import org.eclipse.ui.PlatformUI;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.labels.StandardXYToolTipGenerator;
//import org.jfree.chart.plot.CombinedDomainXYPlot;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
//import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//
///**
// * The tree viewer holding all the simulations items and calibration items.
// * 
// * @author Andrea Antonello (www.hydrologis.com)
// */
//public class SimulationsTreeViewer extends CheckboxTreeViewer
//        implements
//            ISelectionChangedListener,
//            SelectionListener {
//
//    /*
//     * different types of points
//     */
//
//    private Vector<TreeChartData>[] treeData = null;
//
//    private XYPlot rainPlot;
//    private XYPlot dischargePlot;
//
//    private int rainDatasetIndex = 0;
//    private int dischargeDatasetIndex = 0;
//
//    private final Button tableRainButton;
//
//    private final Vector<TreeChartData> dischargesVisualized = new Vector<TreeChartData>();
//
//    private List<TreeChartData> visibleData = null;
//
//    private TreeChartData lonelyBoy;
//
//    private final Composite parent;
//
//    public SimulationsTreeViewer( Composite parent, Composite related, Button tableRainButton,
//            int multi, Vector<TreeChartData>[] treeData ) {
//        super(parent, multi);
//        this.parent = parent;
//        this.tableRainButton = tableRainButton;
//        this.treeData = treeData;
//        visibleData = new ArrayList<TreeChartData>();
//
//        this.addSelectionChangedListener(this);
//
//        createChartComposite(related);
//
//        CalibrationsContentProvider mpCP = new CalibrationsContentProvider();
//        this.setContentProvider(mpCP);
//        CalibrationsLabelProvider mpLP = new CalibrationsLabelProvider();
//        this.setLabelProvider(mpLP);
//    }
//
//    private void createChartComposite( Composite related ) {
//        JFreeChart chart1 = ChartFactory.createXYBarChart("", "Tempo [h]", false, "J [mm/h]", null,
//                PlotOrientation.VERTICAL, true, true, false);
//        rainPlot = (XYPlot) chart1.getPlot();
//        rainPlot.getRangeAxis().setInverted(true);
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
//
//    }
//
//    protected void handleTreeExpand( TreeEvent event ) {
//        super.handleTreeExpand(event);
//
//        // Object data = event.item.getData();
//        // if (data instanceof String) {
//        // String title = (String) data;
//        //
//        // List<MonitoringPoint> list = itemList.get(title);
//        // for( MonitoringPoint mp : list ) {
//        // if (mp.isActive()) {
//        // setChecked(mp, true);
//        // }
//        // }
//        // }
//
//    }
//
//    protected void fireCheckStateChanged( CheckStateChangedEvent event ) {
//        super.fireCheckStateChanged(event);
//
//        Object data = event.getElement();
//        /*
//         * the title check activates and deactivates the whole tree
//         */
//        if (data instanceof TreeChartData) {
//            TreeChartData treeData = (TreeChartData) data;
//            if (getChecked(data)) {
//                /*
//                 * plot them as rain
//                 */
//                if (treeData.getType().equals(TreeChartData.RAIN)) {
//                    visualizeRainSeries(treeData);
//                    visibleData.add(treeData);
//                }
//                /*
//                 * plot them as a discharge
//                 */
//                if (treeData.getType().equals(TreeChartData.DISCHARGE)) {
//                    visualizeDischargeSeries(treeData);
//                    if (!dischargesVisualized.contains(treeData)) {
//                        dischargesVisualized.add(treeData);
//                    }
//                    visibleData.add(treeData);
//                }
//
//            } else {
//                /*
//                 * remove it from the rain chart
//                 */
//                if (treeData.getType().equals(TreeChartData.RAIN)) {
//                    hideRainSeries(treeData);
//                    visibleData.remove(treeData);
//                }
//                /*
//                 * remove it from the discharge chart
//                 */
//                if (treeData.getType().equals(TreeChartData.DISCHARGE)) {
//                    hideDischargeSeries(treeData);
//                    if (dischargesVisualized.contains(treeData)) {
//                        dischargesVisualized.remove(treeData);
//                    }
//                    visibleData.remove(treeData);
//                }
//
//            }
//
//        }
//        /*
//         * the subtree check could deactivate the title also or activate the title also
//         */
//        if (data instanceof Vector) {
//            Vector<TreeChartData> tree = (Vector<TreeChartData>) data;
//            if (getChecked(data)) {
//                for( TreeChartData treeChartData : tree ) {
//                    if (!getChecked(treeChartData)) {
//                        setChecked(treeChartData, true);
//                        if (treeChartData.getType().equals(TreeChartData.RAIN)) {
//                            visualizeRainSeries(treeChartData);
//                            visibleData.add(treeChartData);
//                        }
//                        if (treeChartData.getType().equals(TreeChartData.DISCHARGE)) {
//                            visualizeDischargeSeries(treeChartData);
//                            if (!dischargesVisualized.contains(treeChartData)) {
//                                dischargesVisualized.add(treeChartData);
//                            }
//                            visibleData.add(treeChartData);
//                        }
//                    }
//                }
//            } else {
//                for( TreeChartData treeChartData : tree ) {
//                    if (getChecked(treeChartData)) {
//                        setChecked(treeChartData, false);
//                        if (treeChartData.getType().equals(TreeChartData.RAIN)) {
//                            hideRainSeries(treeChartData);
//                            visibleData.remove(treeChartData);
//                        }
//                        if (treeChartData.getType().equals(TreeChartData.DISCHARGE)) {
//                            hideDischargeSeries(treeChartData);
//                            if (dischargesVisualized.contains(treeChartData)) {
//                                dischargesVisualized.remove(treeChartData);
//                            }
//                            visibleData.remove(treeChartData);
//                        }
//                    }
//                }
//            }
//        }
//
//        /*
//         * check if there is only one discharge active. If it does and that 
//         * one has an approac of
//         * type 2 or 3, the rain table button has to be enabled, else not
//         */
//        if (dischargesVisualized.size() == 1) {
//            TreeChartData tmp = dischargesVisualized.get(0);
//            int tipoApproccio = tmp.getRunProperties().tipoApproccio;
//            if (tipoApproccio == 1 || tipoApproccio == 2) {
//                lonelyBoy = dischargesVisualized.get(0);
//                tableRainButton.setEnabled(true);
//            } else {
//                tableRainButton.setEnabled(false);
//            }
//        } else if (dischargesVisualized.size() > 1) {
//            TreeChartData first = dischargesVisualized.get(0);
//            boolean allEqual = true;
//            for( TreeChartData tD : dischargesVisualized ) {
//                if (!first.getParentName().equals(tD.getParentName())) {
//                    allEqual = false;
//                }
//            }
//            if (allEqual) {
//                TreeChartData tmp = dischargesVisualized.get(0);
//                int tipoApproccio = tmp.getRunProperties().tipoApproccio;
//                if (tipoApproccio == 1 || tipoApproccio == 2) {
//                    lonelyBoy = dischargesVisualized.get(0);
//                    tableRainButton.setEnabled(true);
//                } else {
//                    tableRainButton.setEnabled(false);
//                }
//            } else {
//                tableRainButton.setEnabled(false);
//            }
//
//        } else {
//            tableRainButton.setEnabled(false);
//        }
//    }
//    /**
//     * @param treeData
//     */
//    private void hideDischargeSeries( TreeChartData treeData ) {
//        int index = treeData.getSeriesIndex();
//        if (index == -1) {
//            System.out.println("This should never happen");
//        }
//        dischargePlot.setDataset(index, null);
//        dischargePlot.setRenderer(index, null);
//    }
//
//    /**
//     * @param treeData
//     */
//    private void hideRainSeries( TreeChartData treeData ) {
//        int index = treeData.getSeriesIndex();
//        if (index == -1) {
//            System.out.println("This should never happen");
//        }
//        rainPlot.setDataset(index, null);
//        rainPlot.setRenderer(index, null);
//    }
//
//    /**
//     * @param treeData
//     */
//    private void visualizeRainSeries( TreeChartData treeData ) {
//        double[][] chartValues = treeData.getSeriesValues();
//        String seriesName = treeData.getSeriesName();
//        XYSeriesCollection lineDataset = null;
//
//        try {
//            XYSeries chartSeries = new XYSeries(seriesName);
//            for( int i = 0; i < chartValues[0].length; i++ ) {
//                chartSeries.add(chartValues[0][i], chartValues[1][i]);
//            }
//
//            lineDataset = new XYSeriesCollection();
//            lineDataset.addSeries(chartSeries);
//        } catch (Exception e) {
//            HydrocarePlugin.log("HydrocarePlugin problem", e); //$NON-NLS-1$
//            e.printStackTrace();
//        }
//
//        rainDatasetIndex++;
//        treeData.setSeriesIndex(rainDatasetIndex);
//
//        rainPlot.setDataset(rainDatasetIndex, lineDataset);
//        ClusteredXYBarRenderer rainRenderer = new ClusteredXYBarRenderer(0.5, false);
//        rainRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
//        rainPlot.setRenderer(rainDatasetIndex, rainRenderer);
//        rainPlot.setForegroundAlpha(0.50f);
//
//        // rainRenderer.setSeriesPaint(0, Color.blue);
//    }
//
//    /**
//     * @param treeData
//     */
//    private void visualizeDischargeSeries( TreeChartData treeData ) {
//        double[][] chartValues = treeData.getSeriesValues();
//        String seriesName = treeData.getSeriesName();
//        XYSeriesCollection lineDataset = null;
//
//        try {
//            XYSeries chartSeries = new XYSeries(seriesName);
//            for( int i = 0; i < chartValues[0].length; i++ ) {
//                chartSeries.add(chartValues[0][i], chartValues[1][i]);
//            }
//
//            lineDataset = new XYSeriesCollection();
//            lineDataset.addSeries(chartSeries);
//        } catch (Exception e) {
//            HydrocarePlugin.log("HydrocarePlugin problem", e); //$NON-NLS-1$
//            e.printStackTrace();
//        }
//
//        dischargeDatasetIndex++;
//        treeData.setSeriesIndex(dischargeDatasetIndex);
//        dischargePlot.setDataset(dischargeDatasetIndex, lineDataset);
//        StandardXYItemRenderer dischargeRenderer = new StandardXYItemRenderer();
//        dischargeRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
//        dischargePlot.setRenderer(dischargeDatasetIndex, dischargeRenderer);
//    }
//
//    public void selectionChanged( SelectionChangedEvent event ) {
//        // if (!(event.getSelection() instanceof TreeSelection)) {
//        // return;
//        // }
//        // TreeSelection sel = (TreeSelection) event.getSelection();
//        //
//        // Object selectedItem = sel.getFirstElement();
//        // if (selectedItem == null) {
//        // // assumed it is a checkstate change, which is already handled
//        // return;
//        // }
//        // if (selectedItem instanceof MonitoringPoint) {
//        // MonitoringPoint p = (MonitoringPoint) selectedItem;
//        // Control propControl = p.getPropertiesWidget(relatedInside);
//        // stackLayout.topControl = propControl;
//        // } else {
//        // Label l = new Label(relatedInside, SWT.SHADOW_ETCHED_IN);
//        // l.setText("Questo oggetto (" + selectedItem.toString()
//        // + ") non ha proprieta' configurabili.");
//        // stackLayout.topControl = l;
//        // }
//        // relatedInside.layout(true);
//    }
//
//    public void widgetDefaultSelected( SelectionEvent e ) {
//    }
//
//    public void widgetSelected( SelectionEvent e ) {
//        Button b = (Button) e.getSource();
//        Point loc = b.getLocation();
//        Shell parentShell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
//        Point pLoc = parentShell.getLocation();
//        pLoc.x = pLoc.x + loc.x;
//        pLoc.y = pLoc.y + loc.y;
//
//        // pop up the table with the rain data
//        final Shell shell = new Shell(Display.getCurrent());
//        shell.setLocation(pLoc);
//        shell.setLayout(new GridLayout(1, false));
//
//        Composite comp = new Composite(shell, SWT.NONE);
//        comp.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
//                | GridData.GRAB_VERTICAL));
//
//        Table table = new Table(comp, SWT.BORDER | SWT.V_SCROLL);
//        table.setHeaderVisible(true);
//        table.setLinesVisible(true);
//        TableColumnLayout layout = new TableColumnLayout();
//        comp.setLayout(layout);
//
//        String[][] dataTable = lonelyBoy.getTable();
//        int cWidth = 100 / dataTable[0].length;
//        for( int i = 0; i < dataTable[0].length; i++ ) {
//            TableColumn column = new TableColumn(table, SWT.NONE);
//            column.setText(dataTable[0][i]);
//            layout.setColumnData(column, new ColumnWeightData(cWidth));
//
//        }
//        for( int i = 1; i < dataTable.length; i++ ) {
//            TableItem item = new TableItem(table, SWT.NONE);
//            item.setText(dataTable[i]);
//        }
//
//        Button closeButton = new Button(shell, SWT.BORDER | SWT.PUSH);
//        closeButton.setText("    Chiudi    ");
//        closeButton.addSelectionListener(new SelectionAdapter(){
//            @Override
//            public void widgetSelected( SelectionEvent e ) {
//                shell.dispose();
//            }
//        });
//        shell.layout();
//        shell.open();
//    }
//
//    private class CalibrationsContentProvider implements ITreeContentProvider {
//
//        @SuppressWarnings("nls")
//        public Object[] getChildren( Object parentElement ) {
//
//            if (parentElement instanceof Vector) {
//
//                Vector<TreeChartData> element = (Vector<TreeChartData>) parentElement;
//                Vector<TreeChartData> newElements = new Vector<TreeChartData>();
//
//                for( TreeChartData treeChartData : element ) {
//                    if (!treeChartData.getType().equals(TreeChartData.TABLEDATA)) {
//                        newElements.add(treeChartData);
//                    }
//                }
//                return newElements.toArray(new TreeChartData[newElements.size()]);
//
//            }
//
//            return null;
//        }
//
//        public Object getParent( Object element ) {
//            if (element instanceof TreeChartData) {
//                for( int i = 0; i < treeData.length; i++ ) {
//                    TreeChartData checkElement = treeData[i].get(0);
//                    if (checkElement.getParentName().equals(
//                            ((TreeChartData) element).getParentName())) {
//                        return treeData[i];
//                    }
//                }
//            }
//            return null;
//        }
//
//        public boolean hasChildren( Object element ) {
//            if (element instanceof Vector) {
//                return true;
//            }
//            return false;
//        }
//
//        public Object[] getElements( Object inputElement ) {
//            // Arrays.sort(keys);
//            return treeData;
//        }
//
//        public void dispose() {
//            treeData = null;
//        }
//
//        public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
//        }
//
//    }
//
//    private class CalibrationsLabelProvider implements ILabelProvider {
//
//        public Image getImage( Object element ) {
//            return null;
//        }
//
//        @SuppressWarnings("nls")
//        public String getText( Object element ) {
//            if (element instanceof Vector) {
//                return ((Vector<TreeChartData>) element).get(0).getParentName();
//            }
//            if (element instanceof TreeChartData) {
//                return ((TreeChartData) element).getSeriesName();
//            }
//            return null;
//        }
//
//        public void addListener( ILabelProviderListener listener ) {
//        }
//
//        public void dispose() {
//        }
//
//        public boolean isLabelProperty( Object element, String property ) {
//            return false;
//        }
//
//        public void removeListener( ILabelProviderListener listener ) {
//        }
//
//    }
//
//    public void exportVisibleData() {
//        Format dateFormatter = new SimpleDateFormat(ArtifactsProperties.DATEPATTERN);
//
//        DirectoryDialog folderDialog = new DirectoryDialog(parent.getShell(), SWT.SAVE);
//        String path = folderDialog.open();
//
//        if (path == null || path.length() < 1) {
//            return;
//        }
//
//        /*
//         * check how many different runs are clicked and order them, since they will have different
//         * time intervals
//         */
//        LinkedHashMap<String, List<TreeChartData>> orderedTreedata = new LinkedHashMap<String, List<TreeChartData>>();
//
//        for( TreeChartData treeData : visibleData ) {
//            String runName = treeData.getParentName();
//
//            List<TreeChartData> tmpList = orderedTreedata.get(runName);
//            if (tmpList == null) {
//                orderedTreedata.put(runName, new ArrayList<TreeChartData>());
//                tmpList = orderedTreedata.get(runName);
//            }
//            tmpList.add(treeData);
//        }
//
//        /*
//         * alright, write that fucking stuff down
//         */
//        Set<String> keys = orderedTreedata.keySet();
//        for( String run : keys ) {
//
//            List<TreeChartData> tree = orderedTreedata.get(run);
//            TreeChartData[] treeDataArray = (TreeChartData[]) tree.toArray(new TreeChartData[tree
//                    .size()]);
//
//            try {
//                BufferedWriter bW = new BufferedWriter(new FileWriter(path + File.separator
//                        + run.replaceAll("\\s+", "_") + ".csv"));
//                // header
//                bW.write("Ora|");
//                for( TreeChartData treeChartData : treeDataArray ) {
//                    bW.write(treeChartData.getSeriesName() + "|");
//                }
//                bW.write("\n");
//
//                // the data
//                int dataRows = treeDataArray[0].getSeriesValues()[0].length;
//                for( TreeChartData tmp : treeDataArray ) {
//                    int t = tmp.getSeriesValues()[0].length;
//                    if (t < dataRows)
//                        dataRows = t;
//                }
//
//                for( int i = 0; i < dataRows; i++ ) {
//                    // date
//                    bW.write(treeDataArray[0].getSeriesValues()[0][i] + "|");
//                    // values
//                    for( TreeChartData treeChartData : treeDataArray ) {
//                        bW.write(String.valueOf(treeChartData.getSeriesValues()[1][i]) + "|");
//                    }
//                    bW.write("\n");
//                }
//                bW.close();
//
//            } catch (IOException e) {
//                HydrocarePlugin.log("HydrocarePlugin problem", e); //$NON-NLS-1$
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//}
