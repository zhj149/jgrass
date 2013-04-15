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
package eu.hydrologis.jgrass.netcdf.netcdfviewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import eu.hydrologis.jgrass.netcdf.NetcdfPlugin;
import eu.hydrologis.jgrass.netcdf.netcdfviewer.varscharters.ICharter;
import eu.hydrologis.jgrass.netcdf.netcdfviewer.varscharters.MonoDimsCharter;
import eu.hydrologis.jgrass.netcdf.netcdfviewer.varscharters.ThreeDimsCharter;
import eu.hydrologis.jgrass.netcdf.netcdfviewer.varscharters.TwoDimsCharter;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class NetcdfView extends ViewPart implements IPropertyListener {
    public static final String ID = "eu.hydrologis.jgrass.netcdf.netcdfviewer.netcdfview"; //$NON-NLS-1$
    private IViewSite site;

    private NetcdfDataset netcdfDataset;
    private HashMap<String, Variable> name2VariablesMap;
    private HashMap<String, Dimension> name2DimensionsMap;
    private List<Variable> acceptedVarsList;

    private HashMap<ImageDescriptor, Image> imageCache = new HashMap<ImageDescriptor, Image>();
    private TreeViewer treeViewer;
    private Composite chartComposite;
    private StackLayout chartStackLayout;
    private Combo x1Combo;
    private Combo x2Combo;
    private Variable selectedVariable;
    private Label emptyLabel;

    public NetcdfView() {
    }

    @Override
    public void createPartControl( Composite parent ) {
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parent.setLayout(new GridLayout(10, true));

        netcdfDataset = NetcdfPlugin.getDefault().getNetcdfDataset();
        if (netcdfDataset == null) {
            // ask for a dataset
            FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.OPEN);
            fileDialog.setFilterExtensions(new String[]{"*.nc", "*.NC"});
            String path = fileDialog.open();

            if (path != null && path.length() > 1) {
                try {
                    netcdfDataset = NetcdfDataset.openDataset(path);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        List<Variable> variables = netcdfDataset.getVariables();
        List<Dimension> dimensions = netcdfDataset.getDimensions();
        List<Attribute> globalAttributes = netcdfDataset.getGlobalAttributes();

        name2VariablesMap = new HashMap<String, Variable>();
        name2DimensionsMap = new HashMap<String, Dimension>();
        for( Dimension dimension : dimensions ) {
            name2DimensionsMap.put(dimension.getName(), dimension);
        }
        acceptedVarsList = new ArrayList<Variable>();
        // wrap in global attributes to view them also
        for( Attribute attribute : globalAttributes ) {
            String name = attribute.getName();
            int length = attribute.getLength();
            StringBuffer sB = new StringBuffer();
            for( int i = 0; i < length; i++ ) {

                sB.append(String.valueOf(attribute.getValue(i)) + " ");
            }
            VariableWrapper globalVar = new VariableWrapper(name, sB.toString());
            acceptedVarsList.add(globalVar);
        }

        for( Variable variable : variables ) {
            /*
             * only non georaster variables and dim <= 3 are accepted
             */
            boolean doAddVar = true;
            List<Dimension> tmpDims = variable.getDimensions();
            if (tmpDims.size() > 3) {
                continue;
            }
            for( Dimension dimension : tmpDims ) {
                Variable tmpVar = dimension.getGroup().findVariable(dimension.getName());
                if (tmpVar instanceof CoordinateAxis1D) {
                    CoordinateAxis1D axis = (CoordinateAxis1D) tmpVar;
                    AxisType axisType = axis.getAxisType();
                    if (axisType == null) {
                        continue;
                    }
                    if (axisType.equals(AxisType.GeoX) || axisType.equals(AxisType.GeoY) || axisType.equals(AxisType.Lat)
                            || axisType.equals(AxisType.Lon)) {
                        doAddVar = false;
                        break;
                    }
                }
            }
            if (doAddVar) {
                name2VariablesMap.put(variable.getName(), variable);
                acceptedVarsList.add(variable);
            }
        }

        Label filterLabel = new Label(parent, SWT.NONE);
        GridData filterLabelGd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        filterLabelGd.horizontalSpan = 1;
        filterLabel.setLayoutData(filterLabelGd);
        filterLabel.setText("Filter");

        final Text filterText = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData filterTextGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        filterTextGd.horizontalSpan = 2;
        filterText.setLayoutData(filterTextGd);
        filterText.setText("");
        filterText.addKeyListener(new KeyAdapter(){
            public void keyReleased( KeyEvent e ) {
                String text = filterText.getText();

                ArrayList<Variable> filteredVarsList = new ArrayList<Variable>();
                if (!text.startsWith("-")) {
                    for( Variable var : acceptedVarsList ) {
                        String name = var.getName();
                        if (name.matches(".*" + text + ".*")) {
                            filteredVarsList.add(var);
                        }
                    }
                } else {
                    for( Variable var : acceptedVarsList ) {
                        String name = var.getName();
                        if (!name.matches(".*" + text.substring(1) + ".*")) {
                            filteredVarsList.add(var);
                        }
                    }
                }

                treeViewer.setInput(filteredVarsList);
            }
        });

        Label dummyLabel = new Label(parent, SWT.NONE);
        GridData dummyLabelGd = new GridData(SWT.END, SWT.CENTER, true, false);
        dummyLabelGd.horizontalSpan = 2;
        dummyLabel.setLayoutData(dummyLabelGd);
        dummyLabel.setText("");

        Label x1Label = new Label(parent, SWT.NONE);
        x1Label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        x1Label.setText("Use as X");
        x1Combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        x1Combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        x1Combo.setItems(new String[0]);
        x1Combo.setEnabled(false);
        x1Combo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                if (selectedVariable != null) {
                    drawChart(selectedVariable);
                }
            }
        });

        Label dummyLabel1 = new Label(parent, SWT.NONE);
        GridData dummyLabelGd1 = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
        dummyLabel1.setLayoutData(dummyLabelGd1);
        dummyLabel1.setText("");

        Label x2Label = new Label(parent, SWT.NONE);
        x2Label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        x2Label.setText("Use for series");
        x2Combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        x2Combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        x2Combo.setItems(new String[0]);
        x2Combo.setEnabled(false);
        x2Combo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                if (selectedVariable != null)
                    drawChart(selectedVariable);
            }
        });

        treeViewer = new TreeViewer(parent);
        Tree tree = treeViewer.getTree();
        GridData treeGd = new GridData(SWT.BEGINNING, SWT.FILL, true, true);
        treeGd.horizontalSpan = 3;
        tree.setLayoutData(treeGd);
        treeViewer.setContentProvider(new NetcdfContentProvider());
        treeViewer.setLabelProvider(new NetcdfLabelProvider());
        treeViewer.addSelectionChangedListener(new NetcdfSelectionChangedListener());
        treeViewer.setInput(acceptedVarsList);
        // treeViewer.expandAll();

        chartComposite = new Composite(parent, SWT.NONE);
        GridData chartCompositeGd = new GridData(SWT.FILL, SWT.FILL, true, true);
        chartCompositeGd.horizontalSpan = 7;
        chartComposite.setLayoutData(chartCompositeGd);
        chartStackLayout = new StackLayout();
        chartComposite.setLayout(chartStackLayout);

        emptyLabel = new Label(chartComposite, SWT.NONE);
        GridData emptyLabelGd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        emptyLabelGd.verticalIndent = 20;
        emptyLabelGd.horizontalIndent = 20;
        emptyLabel.setLayoutData(emptyLabelGd);
        emptyLabel.setText("No plottable variable selected");

        chartStackLayout.topControl = emptyLabel;
        chartComposite.layout();
    }

    @Override
    public void init( IViewSite site ) throws PartInitException {
        this.site = site;
        addPropertyListener(this);
        super.init(site);
    }

    @Override
    public void setFocus() {

    }

    public void propertyChanged( Object source, int propId ) {
        System.out.println(propId);
    }

    private class NetcdfContentProvider implements ITreeContentProvider {

        public Object[] getChildren( Object parentElement ) {
            if (parentElement instanceof List< ? >) {
                List< ? > variablesList = (List< ? >) parentElement;
                Object[] kids = (Object[]) variablesList.toArray(new Object[variablesList.size()]);
                return kids;
            }
            if (parentElement instanceof Variable) {
                Variable var = (Variable) parentElement;
                List<Dimension> dimensions = var.getDimensions();
                Dimension[] dims = (Dimension[]) dimensions.toArray(new Dimension[dimensions.size()]);
                return dims;
            }
            return null;
        }

        public Object getParent( Object element ) {
            if (element instanceof Dimension) {
                Dimension dim = (Dimension) element;
            }
            // TODO Auto-generated method stub
            return null;
        }

        public boolean hasChildren( Object element ) {
            if (element instanceof Dimension) {
                return false;
            }
            if (element instanceof Variable) {
                Variable var = (Variable) element;
                List<Dimension> dimensions = var.getDimensions();
                if (dimensions.size() > 0) {
                    return true;
                }
            }
            return false;
        }

        public Object[] getElements( Object inputElement ) {
            return getChildren(inputElement);
        }

        public void dispose() {
        }

        public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        }

    }

    private class NetcdfLabelProvider implements ILabelProvider {

        public Image getImage( Object element ) {
            ImageDescriptor descriptor = null;
            if (element instanceof Dimension) {
                descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/dims.png");
            } else if (element instanceof VariableWrapper) {
                descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/global.gif");
            } else if (element instanceof Variable) {
                Variable variable = (Variable) element;
                // first check if the variable is one of the dimensions
                String name = variable.getName();
                Dimension dimension = name2DimensionsMap.get(name);
                if (dimension != null) {
                    descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/dims.png");
                } else {
                    // then check the variable dimension
                    List<Dimension> dimensions = variable.getDimensions();
                    if (dimensions == null || dimensions.size() == 0) {
                        descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/0d.gif");
                    } else if (dimensions.size() == 1) {
                        descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/1d.gif");
                    } else if (dimensions.size() == 2) {
                        descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/2d.gif");
                    } else if (dimensions.size() == 3) {
                        descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/3d.gif");
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }

            // obtain the cached image corresponding to the descriptor
            Image image = (Image) imageCache.get(descriptor);
            if (image == null) {
                image = descriptor.createImage();
                imageCache.put(descriptor, image);
            }
            return image;
        }

        public String getText( Object element ) {
            if (element instanceof VariableWrapper) {
                VariableWrapper var = (VariableWrapper) element;
                return var.getName();
            }
            if (element instanceof Variable) {
                Variable var = (Variable) element;
                return var.getName();
            }
            if (element instanceof Dimension) {
                Dimension dim = (Dimension) element;
                return dim.getName();
            }
            return null;
        }

        public void addListener( ILabelProviderListener listener ) {
        }

        public boolean isLabelProperty( Object element, String property ) {
            return false;
        }

        public void removeListener( ILabelProviderListener listener ) {
        }

        public void dispose() {
            for( Iterator< ? > i = imageCache.values().iterator(); i.hasNext(); ) {
                ((Image) i.next()).dispose();
            }
            imageCache.clear();
        }
    }

    private class NetcdfSelectionChangedListener implements ISelectionChangedListener {

        public void selectionChanged( SelectionChangedEvent event ) {
            if (!(event.getSelection() instanceof TreeSelection)) {
                return;
            }
            TreeSelection sel = (TreeSelection) event.getSelection();
            Object selectedItem = sel.getFirstElement();
            if (selectedItem instanceof Dimension) {
                x1Combo.setEnabled(false);
                x2Combo.setEnabled(false);
                showEmptyLabel();
            } else if (selectedItem instanceof VariableWrapper) {
                selectedVariable = (VariableWrapper) selectedItem;
                x1Combo.setEnabled(false);
                x2Combo.setEnabled(false);
                String cdl = selectedVariable.writeCDL("    ", true, true); //$NON-NLS-1$
                showNcDumpLabel(cdl);
            } else if (selectedItem instanceof Variable) {
                selectedVariable = (Variable) selectedItem;
                List<Dimension> dimensions = selectedVariable.getDimensions();

                if (dimensions.size() == 0) {
                    try {
                        Array read = selectedVariable.read();
                        double value = read.getDouble(0);
                        String cdl = "Value: " + value + "\n\n";
                        cdl = cdl + selectedVariable.writeCDL("    ", true, true); //$NON-NLS-1$
                        showNcDumpLabel(cdl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String[] dimNamesArray = new String[dimensions.size()];
                    List<String> dimNamesListWithoutTime = new ArrayList<String>();
                    for( int i = 0; i < dimensions.size(); i++ ) {
                        String name = dimensions.get(i).getName();
                        dimNamesArray[i] = name;
                        if (name.matches(".*[tT][iI][mM][eE].*")) {
                            continue;
                        }
                        dimNamesListWithoutTime.add(name);
                    }
                    String[] dimNamesArrayWithoutTime = (String[]) dimNamesListWithoutTime
                            .toArray(new String[dimNamesListWithoutTime.size()]);
                    x1Combo.setEnabled(true);
                    x1Combo.setItems(dimNamesArray);
                    x1Combo.select(0);

                    if (dimNamesArrayWithoutTime.length > 0) {
                        x2Combo.setEnabled(dimensions.size() >= 2);
                        x2Combo.setItems(dimNamesArrayWithoutTime);

                        if (dimNamesArray[0].equals(dimNamesArrayWithoutTime[0])) {
                            x2Combo.select(1);
                        } else {
                            x2Combo.select(0);
                        }
                    }else{
                        x2Combo.setEnabled(false);
                    }

                    drawChart(selectedVariable);
                }
            }

        }

    }

    private void showNcDumpLabel( String cdl ) {
        Label cdlLabel = new Label(chartComposite, SWT.WRAP);
        cdlLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        cdlLabel.setText(cdl);

        chartStackLayout.topControl = cdlLabel;
        chartComposite.layout();
    }

    private void showEmptyLabel() {
        chartStackLayout.topControl = emptyLabel;
        chartComposite.layout();
    }

    private void drawChart( Object selectedItem ) {
        Variable selectedVar = (Variable) selectedItem;
        List<Dimension> dimensions = selectedVar.getDimensions();

        try {
            ICharter mDC = null;
            Composite chartComp = null;
            if (dimensions.size() == 1) {
                mDC = new MonoDimsCharter(selectedVar, chartComposite);
                chartComp = mDC.dochart(0);
            } else if (dimensions.size() == 2) {
                mDC = new TwoDimsCharter(selectedVar, chartComposite);

                int selectionIndex = x1Combo.getSelectionIndex();
                String item1 = x1Combo.getItem(selectionIndex);
                int x1Index = getDimensionIndex(selectedVar, item1);

                selectionIndex = x2Combo.getSelectionIndex();
                String item2 = x2Combo.getItem(selectionIndex);
                int x2Index = getDimensionIndex(selectedVar, item2);

                if (item1.equals(item2)) {
                    return;
                }

                chartComp = mDC.dochart(x1Index, x2Index);
            } else if (dimensions.size() == 3) {
                mDC = new ThreeDimsCharter(selectedVar, chartComposite);

                int selectionIndex = x1Combo.getSelectionIndex();
                String item1 = x1Combo.getItem(selectionIndex);
                int x1Index = getDimensionIndex(selectedVar, item1);

                selectionIndex = x2Combo.getSelectionIndex();
                String item2 = x2Combo.getItem(selectionIndex);
                int x2Index = getDimensionIndex(selectedVar, item2);

                if (item1.equals(item2)) {
                    return;
                }

                chartComp = mDC.dochart(x1Index, x2Index);
            } else {
                return;
            }
            chartStackLayout.topControl = chartComp;
            chartComposite.layout();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int getDimensionIndex( Variable selectedVar, String item ) {
        int xIndex = 0;
        List<Dimension> dimList = selectedVar.getDimensions();
        for( int i = 0; i < dimList.size(); i++ ) {
            Dimension dimension = dimList.get(i);
            String name = dimension.getName();
            if (name.equals(item)) {
                xIndex = i;
                break;
            }
        }
        return xIndex;
    }

    /**
     * A wrapper to get the global attributes to be visualized as 0D variables.
     */
    private class VariableWrapper extends Variable {
        private final String name;
        private final String description;

        public VariableWrapper( String name, String description ) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String writeCDL( String indent, boolean useFullName, boolean strict ) {
            return indent + name + ": " + description;
        }

        public int hashCode() {
            return description.hashCode();
        }

        public boolean equals( Object otherObj ) {
            if (otherObj instanceof VariableWrapper) {
                VariableWrapper otherVar = (VariableWrapper) otherObj;
                return this.description.equals(otherVar.description);
            } else {
                return false;
            }
        }
    }
}
