package eu.hydrologis.jgrass.netcdf.export.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.netcdf.export.core.NcDataType;
import eu.hydrologis.jgrass.netcdf.export.core.NcFileWriter;
import eu.hydrologis.jgrass.netcdf.export.core.NcLayer;
import eu.hydrologis.jgrass.netcdf.export.core.NcVariable;

public class VariablesWizardPage extends WizardPage {
    public static final String ID = "eu.hydrologis.jgrass.netcdf.export.wizard.VariablesWizardPage";
    private final NetcdfExportWizard parentWizard;
    private List<NcVariable> variablesList;
    private List<NcLayer> layersList;
    private ListViewer layersViewer;
    private Text propertiesText;
    private NcFileWriter ncFW;

    protected VariablesWizardPage( NetcdfExportWizard netcdfExportWizard ) {
        super(ID);
        this.parentWizard = netcdfExportWizard;
        setTitle("Variables and Attributes");
        setDescription("Here variables and attributes can be defined and the GRASS rasters chosen to get the data from.");

    }

    public void createControl( Composite parent ) {
        Composite container = new Composite(parent, SWT.NULL);
        GridData containerLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        container.setLayoutData(containerLayoutData);
        GridLayout containerLayout = new GridLayout(6, true);
        container.setLayout(containerLayout);
        setControl(container);

        Label variableListLabel = new Label(container, SWT.NONE);
        variableListLabel.setText("Variables list");
        GridData gD = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD.horizontalSpan = 2;
        variableListLabel.setLayoutData(gD);

        Label layersListLabel = new Label(container, SWT.NONE);
        layersListLabel.setText("Layers list");
        gD = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD.horizontalSpan = 2;
        layersListLabel.setLayoutData(gD);

        Label propertiesLabel = new Label(container, SWT.NONE);
        propertiesLabel.setText("Properties");
        gD = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD.horizontalSpan = 2;
        propertiesLabel.setLayoutData(gD);

        variablesList = new ArrayList<NcVariable>();
        final ListViewer variablesViewer = new ListViewer(container, SWT.BORDER | SWT.SINGLE
                | SWT.VERTICAL | SWT.V_SCROLL);
        Control control = variablesViewer.getControl();
        gD = new GridData(SWT.FILL, SWT.FILL, true, true);
        gD.horizontalSpan = 2;
        control.setLayoutData(gD);
        variablesViewer.setContentProvider(new IStructuredContentProvider(){
            @SuppressWarnings("unchecked")
            public Object[] getElements( Object inputElement ) {
                List<NcVariable> v = (List<NcVariable>) inputElement;
                return v.toArray();
            }
            public void dispose() {
            }
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
            }
        });
        variablesViewer.setInput(variablesList);
        variablesViewer.setLabelProvider(new LabelProvider(){
            public Image getImage( Object element ) {
                return null;
            }
            public String getText( Object element ) {
                return ((NcVariable) element).getName();
            }
        });
        variablesViewer.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged( SelectionChangedEvent event ) {
                IStructuredSelection selection = (IStructuredSelection) variablesViewer
                        .getSelection();
                NcVariable item = (NcVariable) selection.getFirstElement();
                if (item == null) {
                    layersViewer.setInput(Collections.emptyList());
                    propertiesText.setText("");
                    return;
                }
                layersList = item.getNcLayerList();
                layersViewer.setInput(layersList);
                propertiesText.setText("");
            }
        });

        layersList = new ArrayList<NcLayer>();
        layersViewer = new ListViewer(container, SWT.BORDER | SWT.SINGLE | SWT.VERTICAL
                | SWT.V_SCROLL | SWT.HORIZONTAL | SWT.H_SCROLL);
        control = layersViewer.getControl();
        gD = new GridData(SWT.FILL, SWT.FILL, true, true);
        gD.horizontalSpan = 2;
        control.setLayoutData(gD);
        layersViewer.setContentProvider(new IStructuredContentProvider(){
            @SuppressWarnings("unchecked")
            public Object[] getElements( Object inputElement ) {
                List<NcLayer> v = (List<NcLayer>) inputElement;
                return v.toArray();
            }
            public void dispose() {
            }
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
            }
        });
        layersViewer.setInput(layersList);
        layersViewer.setLabelProvider(new LabelProvider(){
            public Image getImage( Object element ) {
                return null;
            }
            public String getText( Object element ) {
                String rasterPath = ((NcLayer) element).getRasterPaths()[0];
                File f = new File(rasterPath);
                return f.getName();
            }
        });
        layersViewer.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged( SelectionChangedEvent event ) {
                IStructuredSelection selection = (IStructuredSelection) variablesViewer
                        .getSelection();
                NcVariable variable = (NcVariable) selection.getFirstElement();
                selection = (IStructuredSelection) layersViewer.getSelection();
                NcLayer layer = (NcLayer) selection.getFirstElement();

                StringBuilder sB = new StringBuilder();
                sB.append("Layer").append("\n");
                sB.append("---------").append("\n");
                sB.append("raster path: ").append("\n").append(layer.getRasterPaths()[0]).append(
                        "\n").append("\n");
                String time = layer.getTime();
                if (variable.isHasTime()) {
                    sB.append("time: ").append(time).append("\n");
                }
                Double level = layer.getLevel();
                if (variable.isHasLevel()) {
                    sB.append("level: ").append(level).append("\n");
                }
                sB.append("\n");
                sB.append("Parent variable: ").append(variable.getName()).append("\n");
                sB.append("--------------------------").append("\n");
                sB.append("description: ").append(variable.getDescription()).append("\n").append(
                        "\n");
                LinkedHashMap<String, Object> attributesMap = variable.getAttributesMap();
                Set<String> keySet = attributesMap.keySet();
                for( String key : keySet ) {
                    Object value = attributesMap.get(key);
                    sB.append(key).append(": ").append(value).append("\n").append("\n");
                }

                propertiesText.setText(sB.toString());

            }
        });

        propertiesText = new Text(container, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        propertiesText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        propertiesText.setText("");
        propertiesText.setEditable(false);
        gD = new GridData(SWT.FILL, SWT.FILL, true, true);
        gD.horizontalSpan = 2;
        propertiesText.setLayoutData(gD);

        /*
         * variables buttons
         */
        final Button variablesButtonAdd = new Button(container, SWT.PUSH);
        GridData addGd1 = new GridData(SWT.FILL, SWT.FILL, true, false);
        variablesButtonAdd.setLayoutData(addGd1);
        variablesButtonAdd.setText("+");
        final Button variablesButtonRemove = new Button(container, SWT.PUSH);
        GridData removeGd1 = new GridData(SWT.FILL, SWT.FILL, true, false);
        variablesButtonRemove.setLayoutData(removeGd1);
        variablesButtonRemove.setText("-");
        variablesButtonAdd.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                String startDateString = parentWizard.getStartDateString();
                boolean hasTime = false;
                if (startDateString != null) {
                    hasTime = true;
                }
                String levelsString = parentWizard.getLevelsString();
                boolean hasLevels = false;
                if (levelsString != null) {
                    hasLevels = true;
                }
                HashMap<String, String> parametersMap = new HashMap<String, String>();
                VariablesDialog vDialog = new VariablesDialog(variablesButtonAdd.getShell(),
                        parametersMap, hasTime, hasLevels);
                vDialog.setBlockOnOpen(true);
                int res = vDialog.open();
                if (res == 0) {
                    String name = parametersMap.get(VariablesDialog.NAMEKEY);
                    if (name == null)
                        name = "-";
                    String descr = parametersMap.get(VariablesDialog.DESCRKEY);
                    if (descr == null)
                        descr = "-";
                    String units = parametersMap.get(VariablesDialog.UNITSKEY);
                    if (units == null)
                        units = "-";
                    String hasTimeStr = parametersMap.get(VariablesDialog.HASTIMEKEY);
                    if (hasTimeStr == null)
                        hasTimeStr = String.valueOf(false);
                    String hasLevelStr = parametersMap.get(VariablesDialog.HASLEVELKEY);
                    if (hasLevelStr == null)
                        hasLevelStr = String.valueOf(false);

                    NcVariable newVariable = new NcVariable(ncFW, name, descr, Boolean
                            .parseBoolean(hasTimeStr), Boolean.parseBoolean(hasLevelStr));
                    LinkedHashMap<String, Object> attributes = new LinkedHashMap<String, Object>();
                    attributes.put("units", units);
                    newVariable.setAttributesMap(attributes);

                    variablesList.add(newVariable);
                    ncFW.addVariable(newVariable);
                    
                    variablesViewer.setInput(variablesList);
                }
            }
        });
        variablesButtonRemove.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                IStructuredSelection selection = (IStructuredSelection) variablesViewer
                        .getSelection();
                NcVariable item = (NcVariable) selection.getFirstElement();
                if (item == null) {
                    return;
                }
                variablesList.remove(item);
                ncFW.removeVariable(item);

                variablesViewer.setInput(variablesList);

                layersViewer.setInput(Collections.emptyList());
                propertiesText.setText("");
            }
        });

        /*
         * layers buttons
         */
        final Button layersButtonAdd = new Button(container, SWT.PUSH);
        GridData addGd = new GridData(SWT.FILL, SWT.FILL, true, false);
        layersButtonAdd.setLayoutData(addGd);
        layersButtonAdd.setText("+");
        final Button layersButtonRemove = new Button(container, SWT.PUSH);
        GridData removeGd = new GridData(SWT.FILL, SWT.FILL, true, false);
        layersButtonRemove.setLayoutData(removeGd);
        layersButtonRemove.setText("-");

        layersButtonAdd.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                IStructuredSelection selection = (IStructuredSelection) variablesViewer
                        .getSelection();
                if (selection == null) {
                    MessageBox msBox = new MessageBox(layersButtonAdd.getShell(), SWT.ICON_WARNING);
                    msBox.setMessage("Please select the variable to which to add the layer to.");
                    msBox.open();
                    return;
                }

                NcVariable ncVariable = (NcVariable) selection.getFirstElement();
                if (ncVariable == null) {
                    return;
                }
                HashMap<String, String> parametersMap = new HashMap<String, String>();
                LayersDialog lDialog = new LayersDialog(layersButtonAdd.getShell(), ncFW,
                        parametersMap, ncVariable.isHasTime(), ncVariable.isHasLevel());
                lDialog.setBlockOnOpen(true);
                int res = lDialog.open();
                if (res == 0) {
                    String timeString = parametersMap.get(LayersDialog.TIMEKEY);
                    String levelString = parametersMap.get(LayersDialog.LEVELKEY);
                    Double level = null;
                    if (levelString != null) {
                        level = Double.parseDouble(levelString);
                    }

                    String mapString = parametersMap.get(LayersDialog.MAPKEY);

                    NcLayer ncLayer = new NcLayer(new String[]{mapString}, timeString, level,
                            NcDataType.GRID);
                    ncVariable.addNcLayer(ncLayer);
                    layersList = ncVariable.getNcLayerList();
                    layersViewer.setInput(layersList);
                }
            }
        });
        layersButtonRemove.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                IStructuredSelection selection = (IStructuredSelection) layersViewer.getSelection();
                NcLayer item = (NcLayer) selection.getFirstElement();
                if (item == null) {
                    return;
                }
                layersList.remove(item);

                IStructuredSelection vSelection = (IStructuredSelection) variablesViewer
                        .getSelection();
                NcVariable vitem = (NcVariable) vSelection.getFirstElement();
                vitem.setNcLayerList(layersList);

                layersViewer.setInput(layersList);

                propertiesText.setText("");
            }
        });

    }

    public void setVisible( boolean visible ) {
        if (visible && ncFW == null) {
            ncFW = new NcFileWriter(parentWizard.getOutputPath(), parentWizard.getMapsetPath(),
                    parentWizard.getStartDateString(), parentWizard.getEndDateString(),
                    parentWizard.getTimestepString(), parentWizard.getLevelsString());
        }
        super.setVisible(visible);
    }
    
    public NcFileWriter getNcFileWriter() {
        return ncFW;
    }
}
