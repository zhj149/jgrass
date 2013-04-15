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
package eu.hydrologis.jgrass.ui.actions.h_adige;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.EditorPart;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseConnection;
import eu.hydrologis.jgrass.ui.UiPlugin;
import eu.hydrologis.jgrass.ui.actions.h_utils.Checker;
import eu.hydrologis.jgrass.ui.actions.messages.Messages;
import eu.hydrologis.jgrass.ui.actions.persistence.SimulationData;
import eu.hydrologis.jgrass.ui.actions.persistence.SimulationDescription;
import eu.hydrologis.jgrass.ui.console.ConsoleCommandExecutor;
import eu.hydrologis.jgrass.ui.utilities.MultiInputDialog;

/**
 * The adige model execution form. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @author Silvia Franceschi (www.hydrologis.com)
 */
public class AdigeEditor extends EditorPart implements SelectionListener {

    private static final String DOTDOTDOT = "..."; //$NON-NLS-1$
    public static final String ID = "eu.hydrologis.jgrass.ui.actions.h_adige.AdigeEditor"; //$NON-NLS-1$
    private FormToolkit toolkit;
    private ScrolledForm scrolledForm;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$

    private HashMap<String, String> textsStringsMap = new HashMap<String, String>();
    private HashMap<String, Text> textsMap = new HashMap<String, Text>();
    private IMessageManager mesgManager;
    private IPreferenceStore preferenceStore;

    // vars
    private String startDateStr;
    private String endDateStr;
    private String timeStepStr;
    private String outputPath;
    private String vegetation;
    private String netnumattr;
    private String pfafattr;
    private String baricenterattr;
    private String startelevattr;
    private String endelevattr;
    private String idmonpointattr;
    private String avgsup10;
    private String avgsup30;
    private String avgsup60;
    private String varsup10;
    private String varsup30;
    private String varsup60;
    private String avgsub;
    private String varsub;
    private String vsup;
    private String vsub;
    private String dopenman;
    private String outpfafids;
    private String startqperarea;
    private String startsupqfract;
    private String startsubqfract;
    private String ks;
    private String mstexp;
    private String specyield;
    private String porosity;
    private String etrate;
    private String satconst;
    private String glaciersup;
    private String glaciersub;
    private String iflayerhfeatures;
    private String iflayerdfeatures;
    private String iflayertfeatures;
    private String iflayerofeatures;
    private String iflayernetpfaf;
    private String iflayerhillslope;
    private String itscalarddata;
    private String itscalarhdata;
    private String itscalartdata;
    private String itscalarodata;
    private String itscalarrain;
    private String iscalarvegetation;
    private String boundaryin;
    private String boundaryout;
    private String logging;

    public AdigeEditor() {
        if (preferenceStore == null)
            preferenceStore = UiPlugin.getDefault().getPreferenceStore();

        startDateStr = preferenceStore.getString(ID + Messages.START_DATE_YYYY_MM_DD_HH_MM);
        endDateStr = preferenceStore.getString(ID + Messages.END_DATE_YYYY_MM_DD_HH_MM);
        timeStepStr = preferenceStore.getString(ID + Messages.TIMESTEP_IN_MINUTES);

        outputPath = preferenceStore.getString(ID + Messages.AdigeMessages_OUTPUT_PATH);

        vegetation = preferenceStore.getString(ID + Messages.AdigeMessages_VEGETATION_FIELD);
        netnumattr = preferenceStore.getString(ID + Messages.NETNUM_FIELD);
        pfafattr = preferenceStore.getString(ID + Messages.AdigeMessages_PFAFSTETTER_FIELD);
        baricenterattr = preferenceStore.getString(ID + Messages.AdigeMessages_BARICENTER_FIELD);
        startelevattr = preferenceStore.getString(ID + Messages.AdigeMessages_STARTELEVATION_FIELD);
        endelevattr = preferenceStore.getString(ID + Messages.AdigeMessages_ENDELEVATION_FIELD);
        idmonpointattr = preferenceStore.getString(ID + Messages.MONITORINGPOINT_ID_FIELD);
        avgsup10 = preferenceStore.getString(ID
                + Messages.AdigeMessages_AVERAGE_SUP_DISTANCE_FIELD_SAT_20);
        avgsup30 = preferenceStore.getString(ID
                + Messages.AdigeMessages_AVERAGE_SUP_DISTANCE_FIELD_SAT_50);
        avgsup60 = preferenceStore.getString(ID + Messages.AdigeMessages_SUP_DISTANCE_FIELD_SAT_50);
        varsup10 = preferenceStore.getString(ID
                + Messages.AdigeMessages_VARIANCE_SUP_DISTANCE_FIELD_SAT_20);
        varsup30 = preferenceStore.getString(ID
                + Messages.AdigeMessages_VARIANCE_SUP_DISTANCE_FIELD_SAT_50);
        varsup60 = preferenceStore.getString(ID + Messages.AdigeMessages_DISTANCE_FIELD_SAT_50);
        avgsub = preferenceStore.getString(ID + Messages.AdigeMessages_AVERAGE_SUB_DISTANCE_FIELD);
        varsub = preferenceStore.getString(ID + Messages.AdigeMessages_VARIANCE_SUB_DISTANCE_FIELD);

        vsup = preferenceStore.getString(ID + Messages.AdigeMessages_SUPERFICIAL_VELOCITY);
        vsub = preferenceStore.getString(ID + Messages.AdigeMessages_SUBSUPERFICIAL_VELOCITY);
        dopenman = preferenceStore.getString(ID
                + Messages.AdigeMessages_DO_PENMAN_EVAPOTRANSPIRATION);
        outpfafids = preferenceStore.getString(ID + Messages.AdigeMessages_OUTPUT_PFAFSTETTER_IDS);
        startqperarea = preferenceStore.getString(ID
                + Messages.AdigeMessages_INITIAL_DISCHARGE_PER_AREA_COEFFICIENT);
        startsupqfract = preferenceStore.getString(ID
                + Messages.AdigeMessages_INITIAL_FRACTION_OF_SUP_DISCHARGE);
        startsubqfract = preferenceStore.getString(ID
                + Messages.AdigeMessages_INITIAL_FRACTION_OF_SUB_DISCHARGE);
        ks = preferenceStore.getString(ID + Messages.AdigeMessages_GAUKLER_STRICKLER_COEFFICIENT);
        mstexp = preferenceStore.getString(ID + Messages.AdigeMessages_MSTEXP_COEFFICIENT);
        specyield = preferenceStore.getString(ID + Messages.AdigeMessages_SPECYIELD_COEFFICIENT);
        porosity = preferenceStore.getString(ID + Messages.AdigeMessages_SOIL_POROSITY);
        etrate = preferenceStore.getString(ID
                + Messages.AdigeMessages_CONSTANT_VALUE_OF_EVAPOTRANSPIRATION);
        satconst = preferenceStore.getString(ID + Messages.AdigeMessages_SATURATION_CONSTANT);
        glaciersup = preferenceStore.getString(ID
                + Messages.AdigeMessages_FRACTION_OF_SUP_DISCHARGE_FROM_GLACIERS);
        glaciersub = preferenceStore.getString(ID
                + Messages.AdigeMessages_FRACTION_OF_SUB_DISCHARGE_FROM_GLACIERS);

        iflayerhfeatures = preferenceStore.getString(ID + Messages.AdigeMessages_HYDROMETERS_LAYER);
        iflayerdfeatures = preferenceStore.getString(ID + Messages.AdigeMessages_DAMS_LAYER);
        iflayertfeatures = preferenceStore.getString(ID + Messages.AdigeMessages_TRIBUTARIES_LAYER);
        iflayerofeatures = preferenceStore.getString(ID + Messages.AdigeMessages_OFFTAKES_LAYER);
        iflayernetpfaf = preferenceStore.getString(ID + Messages.AdigeMessages_NETWORK_LAYER);
        iflayerhillslope = preferenceStore.getString(ID + Messages.BASINS_LAYER);

        itscalarddata = preferenceStore.getString(ID + Messages.AdigeMessages_DAMS_DATA);
        itscalarhdata = preferenceStore.getString(ID + Messages.AdigeMessages_HYDROMETERS_DATA);
        itscalartdata = preferenceStore.getString(ID + Messages.AdigeMessages_TRIBUTARIES_DATA);
        itscalarodata = preferenceStore.getString(ID + Messages.AdigeMessages_OFFTAKES_DATA);
        itscalarrain = preferenceStore.getString(ID + Messages.AdigeMessages_RAIN_DATA);
        iscalarvegetation = preferenceStore.getString(ID + Messages.AdigeMessages_VEGETATION_DATA);

        boundaryin = preferenceStore.getString(ID
                + Messages.AdigeMessages_BOUNDARY_CONDITIONS_TO_USE);
        boundaryout = preferenceStore.getString(ID
                + Messages.AdigeMessages_BOUNDARY_CONDITIONS_TO_SAVE);

        logging = preferenceStore.getString(ID + Messages.AdigeMessages_LOGGING);

    }

    public void init( IEditorSite site, IEditorInput input ) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    public void createPartControl( Composite parent ) {
        toolkit = new FormToolkit(parent.getDisplay());
        scrolledForm = toolkit.createScrolledForm(parent);
        scrolledForm.setText(""); //$NON-NLS-1$
        Form form = scrolledForm.getForm();
        toolkit.decorateFormHeading(form);
        // form.addMessageHyperlinkListener(this);
        form.addMessageHyperlinkListener(new HyperlinkAdapter()); // NEW LINE
        // form.setMessage("This is an error message", IMessageProvider.ERROR);

        mesgManager = new ManagedForm(toolkit, scrolledForm).getMessageManager();

        // the layout
        TableWrapLayout layout = new TableWrapLayout();
        // GridLayout layout = new GridLayout();
        scrolledForm.getBody().setLayout(layout);
        layout.numColumns = 2;

        // main output panel
        createMainPanel();

        // execution panel
        createExecutionPanel();

        // shapefile fileds variables
        createShapefileFieldsVars();

        // model parameters
        createModelParametersVars();

        // input feature layers
        createInputFeatureLayers();

        // input scalarsets
        createInputScalarsets();

        // boundary conditions
        createBoundaryConditions();

        // logging
        createLogging();

    }

    private void createMainPanel() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.MAIN_PANEL);
        section.setDescription(Messages.MAIN_PANEL_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.rowspan = 2;
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(3, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, startDateStr, Messages.START_DATE_YYYY_MM_DD_HH_MM);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, endDateStr, Messages.END_DATE_YYYY_MM_DD_HH_MM);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, timeStepStr, Messages.TIMESTEP_IN_MINUTES);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelTextButton(mainSectionComposite, outputPath, Messages.AdigeMessages_OUTPUT_PATH,
                SWT.SAVE);

    }

    private void createExecutionPanel() {
        Section sectionExecution = toolkit.createSection(scrolledForm.getBody(),
                Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        sectionExecution.setText(Messages.EXECUTION_PANEL);
        sectionExecution.setDescription(Messages.EXECUTION_PANEL_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        sectionExecution.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(sectionExecution);
        GridLayout layout = new GridLayout(1, false);
        mainSectionComposite.setLayout(layout);
        sectionExecution.setClient(mainSectionComposite);

        Button executionButton = toolkit.createButton(mainSectionComposite,
                Messages.AdigeMessages_EXECUTION, SWT.PUSH);
        executionButton.addSelectionListener(this);

        Section sectionImportExport = toolkit.createSection(scrolledForm.getBody(),
                Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        sectionImportExport.setText(Messages.IMPORTEXPORT_PANEL);
        sectionImportExport.setDescription(Messages.IMPORTEXPORT_PANEL_DESCR);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        sectionImportExport.setLayoutData(td);

        mainSectionComposite = toolkit.createComposite(sectionImportExport);
        layout = new GridLayout(1, true);
        mainSectionComposite.setLayout(layout);
        sectionImportExport.setClient(mainSectionComposite);

        final Button exportButton = toolkit.createButton(mainSectionComposite,
                Messages.EXPORT_SETTINGS, SWT.PUSH);
        exportButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(exportButton.getShell(), SWT.SAVE);
                String path = fileDialog.open();

                if (path == null || path.length() < 1)
                    return;

                try {
                    BufferedWriter bW = new BufferedWriter(new FileWriter(path));
                    Set<String> keySet = textsStringsMap.keySet();
                    for( String key : keySet ) {
                        bW.write(key);
                        bW.write("="); //$NON-NLS-1$
                        bW.write(textsStringsMap.get(key));
                        bW.write("\n"); //$NON-NLS-1$
                    }
                    bW.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });

        final Button importButton = toolkit.createButton(mainSectionComposite,
                Messages.IMPORT_SETTINGS, SWT.PUSH);
        importButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(exportButton.getShell(), SWT.OPEN);
                String path = fileDialog.open();

                if (path == null || path.length() < 1)
                    return;

                try {
                    BufferedReader bR = new BufferedReader(new FileReader(path));
                    String line = null;
                    while( (line = bR.readLine()) != null ) {
                        String[] lineSplit = line.split("="); //$NON-NLS-1$
                        if (lineSplit.length != 2) {
                            continue;
                        }

                        Text text = textsMap.get(lineSplit[0]);
                        if (text != null) {
                            text.setText(lineSplit[1]);
                        }
                    }
                    bR.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        final Button exportToRemoteDatabaseButton = toolkit.createButton(mainSectionComposite,
                Messages.EXPORT_RESULTS_TO_DATABASE, SWT.PUSH);
        exportToRemoteDatabaseButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                try {

                    String userLabel = Messages.USER;
                    String titleLabel = Messages.TITLE;
                    String descrLabel = Messages.DESCRIPTION;
                    String refmodelLabel = Messages.AdigeEditor_ID_OF_USED_H_ENERGYBALANCE_MODEL;
                    MultiInputDialog dialog = new MultiInputDialog(exportToRemoteDatabaseButton
                            .getShell(), Messages.INFORMATION,
                            Messages.SUPPLY_INFO_FOR_THE_SIMULATION, titleLabel, descrLabel,
                            userLabel, refmodelLabel);
                    dialog.setBlockOnOpen(true);
                    int res = dialog.open();

                    if (res != IDialogConstants.OK_ID) {
                        return;
                    }

                    String title = dialog.getStringByLable(titleLabel);
                    String descr = dialog.getStringByLable(descrLabel);
                    String user = dialog.getStringByLable(userLabel);
                    String idEnergybalance = dialog.getStringByLable(refmodelLabel);

                    IDatabaseConnection activeDatabaseConnection = DatabasePlugin.getDefault().getActiveDatabaseConnection();
                    SessionFactory sF = activeDatabaseConnection.getSessionFactory();
                    Session session = sF.openSession();
                    Transaction transaction = session.beginTransaction();

                    /*
                     * create simulation description
                     */
                    SimulationDescription simDescr = new SimulationDescription();
                    simDescr.setModel("h.adige"); //$NON-NLS-1$
                    simDescr.setTitle(title);
                    simDescr.setDescription(descr);
                    simDescr.setUser(user);
                    try {
                        long parseLong = Long.parseLong(idEnergybalance);
                        simDescr.setReferenceModel(parseLong);
                    } catch (Exception ex) {
                        // if user inserts wrong, for now just don't link
                    }

                    Timestamp ts0 = new Timestamp(new Date().getTime());
                    simDescr.setInsertDate(ts0);

                    String startDateStr = textsStringsMap.get(Messages.START_DATE_YYYY_MM_DD_HH_MM);
                    String endDateStr = textsStringsMap.get(Messages.END_DATE_YYYY_MM_DD_HH_MM);
                    Date startDate = dateFormatter.parse(startDateStr);
                    Date endDate = dateFormatter.parse(endDateStr);
                    Timestamp ts1 = new Timestamp(startDate.getTime());
                    Timestamp ts2 = new Timestamp(endDate.getTime());
                    simDescr.setStartDate(ts1);
                    simDescr.setEndDate(ts2);

                    /*
                     * create simulation data
                     */
                    SimulationData simData = new SimulationData();

                    String outPath = textsStringsMap.get(Messages.AdigeMessages_OUTPUT_PATH);
                    File outFile = new File(outPath);
                    BufferedReader bR = new BufferedReader(new FileReader(outFile));
                    String line = null;
                    StringBuilder sB = new StringBuilder();
                    while( (line = bR.readLine()) != null ) {
                        sB.append(line).append("\n"); //$NON-NLS-1$
                    }
                    simData.setSimulationResult(sB.toString());

                    Set<String> keySet = textsStringsMap.keySet();
                    StringBuilder parameters = new StringBuilder();
                    for( String key : keySet ) {
                        parameters.append(key).append("=").append(textsStringsMap.get(key)).append( //$NON-NLS-1$
                                "\n"); //$NON-NLS-1$
                    }
                    simData.setModelParameters(parameters.toString());

                    simData.setSimulationDescription(simDescr);

                    session.save(simDescr);
                    session.save(simData);

                    transaction.commit();
                    session.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });

    }
    private void createShapefileFieldsVars() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.AdigeMessages_SHAPEFILE_FIELDS);
        section.setDescription(Messages.AdigeMessages_SHAPEFILE_FIELDS_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(2, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        String label = Messages.AdigeMessages_VEGETATION_FIELD;
        makeLabelText(mainSectionComposite, vegetation, label);
        label = Messages.NETNUM_FIELD;
        makeLabelText(mainSectionComposite, netnumattr, label);
        label = Messages.AdigeMessages_BARICENTER_FIELD;
        makeLabelText(mainSectionComposite, baricenterattr, label);
        label = Messages.AdigeMessages_PFAFSTETTER_FIELD;
        makeLabelText(mainSectionComposite, pfafattr, label);
        label = Messages.AdigeMessages_STARTELEVATION_FIELD;
        makeLabelText(mainSectionComposite, startelevattr, label);
        label = Messages.AdigeMessages_ENDELEVATION_FIELD;
        makeLabelText(mainSectionComposite, endelevattr, label);
        label = Messages.MONITORINGPOINT_ID_FIELD;
        makeLabelText(mainSectionComposite, idmonpointattr, label);
        label = Messages.AdigeMessages_AVERAGE_SUP_DISTANCE_FIELD_SAT_20;
        makeLabelText(mainSectionComposite, avgsup10, label);
        label = Messages.AdigeMessages_AVERAGE_SUP_DISTANCE_FIELD_SAT_50;
        makeLabelText(mainSectionComposite, avgsup30, label);
        label = Messages.AdigeMessages_SUP_DISTANCE_FIELD_SAT_50;
        makeLabelText(mainSectionComposite, avgsup60, label);
        label = Messages.AdigeMessages_VARIANCE_SUP_DISTANCE_FIELD_SAT_20;
        makeLabelText(mainSectionComposite, varsup10, label);
        label = Messages.AdigeMessages_VARIANCE_SUP_DISTANCE_FIELD_SAT_50;
        makeLabelText(mainSectionComposite, varsup30, label);
        label = Messages.AdigeMessages_DISTANCE_FIELD_SAT_50;
        makeLabelText(mainSectionComposite, varsup60, label);
        label = Messages.AdigeMessages_AVERAGE_SUB_DISTANCE_FIELD;
        makeLabelText(mainSectionComposite, avgsub, label);
        label = Messages.AdigeMessages_VARIANCE_SUB_DISTANCE_FIELD;
        makeLabelText(mainSectionComposite, varsub, label);
    }

    private void createModelParametersVars() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.AdigeMessages_MODEL_PARAMETERS);
        section.setDescription(Messages.AdigeMessages_MODEL_PARAMETERS_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(2, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, vsup, Messages.AdigeMessages_SUPERFICIAL_VELOCITY);
        makeLabelText(mainSectionComposite, vsub, Messages.AdigeMessages_SUBSUPERFICIAL_VELOCITY);
        makeLabelText(mainSectionComposite, dopenman,
                Messages.AdigeMessages_DO_PENMAN_EVAPOTRANSPIRATION);
        makeLabelText(mainSectionComposite, outpfafids,
                Messages.AdigeMessages_OUTPUT_PFAFSTETTER_IDS);
        makeLabelText(mainSectionComposite, startqperarea,
                Messages.AdigeMessages_INITIAL_DISCHARGE_PER_AREA_COEFFICIENT);
        makeLabelText(mainSectionComposite, startsupqfract,
                Messages.AdigeMessages_INITIAL_FRACTION_OF_SUP_DISCHARGE);
        makeLabelText(mainSectionComposite, startsubqfract,
                Messages.AdigeMessages_INITIAL_FRACTION_OF_SUB_DISCHARGE);
        makeLabelText(mainSectionComposite, ks,
                Messages.AdigeMessages_GAUKLER_STRICKLER_COEFFICIENT);
        makeLabelText(mainSectionComposite, mstexp, Messages.AdigeMessages_MSTEXP_COEFFICIENT);
        makeLabelText(mainSectionComposite, specyield, Messages.AdigeMessages_SPECYIELD_COEFFICIENT);
        makeLabelText(mainSectionComposite, porosity, Messages.AdigeMessages_SOIL_POROSITY);
        makeLabelText(mainSectionComposite, etrate,
                Messages.AdigeMessages_CONSTANT_VALUE_OF_EVAPOTRANSPIRATION);
        makeLabelText(mainSectionComposite, satconst, Messages.AdigeMessages_SATURATION_CONSTANT);
        makeLabelText(mainSectionComposite, glaciersup,
                Messages.AdigeMessages_FRACTION_OF_SUP_DISCHARGE_FROM_GLACIERS);
        makeLabelText(mainSectionComposite, glaciersub,
                Messages.AdigeMessages_FRACTION_OF_SUB_DISCHARGE_FROM_GLACIERS);
    }

    private void createInputFeatureLayers() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.AdigeMessages_INPUT_FEATURE_LAYERS);
        section.setDescription(Messages.AdigeMessages_INPUT_FEATURE_LAYERS_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(2, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, iflayerhfeatures,
                Messages.AdigeMessages_HYDROMETERS_LAYER);
        makeLabelText(mainSectionComposite, iflayerdfeatures, Messages.AdigeMessages_DAMS_LAYER);
        makeLabelText(mainSectionComposite, iflayertfeatures,
                Messages.AdigeMessages_TRIBUTARIES_LAYER);
        makeLabelText(mainSectionComposite, iflayerofeatures, Messages.AdigeMessages_OFFTAKES_LAYER);
        makeLabelText(mainSectionComposite, iflayernetpfaf, Messages.AdigeMessages_NETWORK_LAYER);
        makeLabelText(mainSectionComposite, iflayerhillslope, Messages.BASINS_LAYER);
    }

    private void createInputScalarsets() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.AdigeMessages_INPUT_SCALARSETS);
        section.setDescription(Messages.AdigeMessages_INPUT_SCALARSETS_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(3, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelTextButton(mainSectionComposite, itscalarddata, Messages.AdigeMessages_DAMS_DATA,
                SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalarhdata,
                Messages.AdigeMessages_HYDROMETERS_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalartdata,
                Messages.AdigeMessages_TRIBUTARIES_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalarodata,
                Messages.AdigeMessages_OFFTAKES_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalarrain, Messages.AdigeMessages_RAIN_DATA,
                SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, iscalarvegetation,
                Messages.AdigeMessages_VEGETATION_DATA, SWT.OPEN);

    }

    private void createBoundaryConditions() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.AdigeMessages_BOUNDARY_CONDITIONS);
        section.setDescription(Messages.AdigeMessages_BOUNDARY_CONDITIONS_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(3, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelTextButton(mainSectionComposite, boundaryin,
                Messages.AdigeMessages_BOUNDARY_CONDITIONS_TO_USE, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, boundaryout,
                Messages.AdigeMessages_BOUNDARY_CONDITIONS_TO_SAVE, SWT.SAVE);

    }

    private void createLogging() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.AdigeMessages_LOGGING);
        section.setDescription(Messages.AdigeMessages_LOGGING_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(2, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, logging, Messages.AdigeMessages_DO_LOGGING);
    }

    private void makeLabelText( Composite theComposite, String defaultText, String labelString ) {
        Label theLabel = toolkit.createLabel(theComposite, labelString, SWT.WRAP);
        GridData theLabelGD = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
        theLabel.setLayoutData(theLabelGD);
        final Text theText = toolkit.createText(theComposite, defaultText, SWT.BORDER);
        GridData theTextGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        theTextGD.horizontalIndent = 5;
        theTextGD.widthHint = 55;
        theText.setLayoutData(theTextGD);
        theText.setData(labelString);
        theText.addModifyListener(new ModifyListener(){
            public void modifyText( ModifyEvent e ) {
                String key = (String) theText.getData();
                textsStringsMap.put(key, theText.getText());
            }
        });
        if (textsStringsMap.get(labelString) == null) {
            textsStringsMap.put(labelString, theText.getText());
        }
        textsMap.put(labelString, theText);

    }

    private void makeLabelTextButton( Composite theComposite, String defaultText,
            String labelString, final int type ) {
        Label theLabel = toolkit.createLabel(theComposite, labelString, SWT.WRAP);
        GridData theLabelGD = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
        theLabel.setLayoutData(theLabelGD);
        final Text theText = toolkit.createText(theComposite, defaultText, SWT.BORDER);
        GridData theTextGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        theTextGD.horizontalIndent = 5;
        theTextGD.widthHint = 55;
        theText.setLayoutData(theTextGD);
        theText.setData(labelString);
        theText.addModifyListener(new ModifyListener(){
            public void modifyText( ModifyEvent e ) {
                String key = (String) theText.getData();
                String str = theText.getText();
                textsStringsMap.put(key, str);
            }
        });
        if (textsStringsMap.get(labelString) == null) {
            textsStringsMap.put(labelString, theText.getText());
        }
        textsMap.put(labelString, theText);

        Button theButton = toolkit.createButton(theComposite, DOTDOTDOT, SWT.PUSH);
        GridData theButtonGD = new GridData(SWT.CENTER, SWT.FILL, false, false);
        theButton.setLayoutData(theButtonGD);
        theButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(theText.getShell(), type);
                String path = fileDialog.open();
                if (path == null || path.length() < 1) {
                    theText.setText(""); //$NON-NLS-1$
                } else {
                    theText.setText(path);
                }
            }
        });
    }

    public void dispose() {
        doSave(null);

        super.dispose();
    }

    public void doSave( IProgressMonitor monitor ) {

        Set<String> keySet = textsStringsMap.keySet();
        for( String key : keySet ) {
            preferenceStore.setValue(ID + key, textsStringsMap.get(key));
        }

        UiPlugin.getDefault().savePluginPreferences();
    }

    public void doSaveAs() {
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
    }

    public void widgetDefaultSelected( SelectionEvent e ) {
    }

    public void widgetSelected( SelectionEvent e ) {
        mesgManager.removeAllMessages();

        boolean isError = false;

        Checker ck = new Checker(mesgManager, textsStringsMap, textsMap, dateFormatter);
        /*
         * check the models parameter for time execution
         */
        if (!ck.checkIsDate(Messages.START_DATE_YYYY_MM_DD_HH_MM,
                Messages.THE_DATES_HAVE_TO_BE_IN_THE_FORMAT_YYYY_MM_DD_HH_MM,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkIsDate(Messages.END_DATE_YYYY_MM_DD_HH_MM,
                Messages.THE_DATES_HAVE_TO_BE_IN_THE_FORMAT_YYYY_MM_DD_HH_MM,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkIsInteger(Messages.TIMESTEP_IN_MINUTES,
                Messages.THE_TIMESTEP_HAS_TO_BE_SUPPLIED_AS_INTEGER_IN_MINUTES,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileWritable(Messages.AdigeMessages_OUTPUT_PATH,
                Messages.THERE_IS_NO_OUTPUT_FILE_OR_THE_FILE_YOU_PROVIDED_IS_NOT_VALID,
                IMessageProvider.WARNING)) {
        }

        /*
         * check on provided shapefile fields
         */
        if (!ck.checkString(Messages.AdigeMessages_VEGETATION_FIELD,
                Messages.AdigeEditor_THE_VEGETATION_FIELD_IS_MANDATORY, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkString(Messages.NETNUM_FIELD, Messages.THE_NETNUM_FIELD_IS_MANDATORY,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkString(Messages.AdigeMessages_BARICENTER_FIELD,
                Messages.AdigeEditor_THE_BARICENTER_ELEVATION_FIELD_IS_REQUIRED,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkString(Messages.AdigeMessages_PFAFSTETTER_FIELD,
                Messages.AdigeEditor_THE_FIELD_WITH_PFAFSTETTER_ENUMERATION_IS_REQUIRED,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkString(Messages.AdigeMessages_STARTELEVATION_FIELD,
                Messages.AdigeEditor_THE_ELEVATION_OF_THE_STARTING_NETWORK_POINT_FIELD_IS_REQUIRED,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkString(Messages.AdigeMessages_ENDELEVATION_FIELD,
                Messages.AdigeEditor_THE_ELEVATION_OF_THE_LAST_NETWORK_POINT_FIELD_IS_REQUIRED,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkString(Messages.MONITORINGPOINT_ID_FIELD,
                Messages.THE_MONITORING_POINT_ID_FIELD_IS_REQUIRED, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.AdigeMessages_AVERAGE_SUP_DISTANCE_FIELD_SAT_20,
                        Messages.AdigeEditor_THE_FIELD_IN_THE_INPUT_BASIN_LAYER_WITH_THE_AVERAGE_VALUE_OF_THE_SUPERFICIAL_WIDTH_FUNCTION_FOR_A_SATURATION_LESS_THAN_20_OF_THE_BASIN_IS_REQUIRED,
                        IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.AdigeMessages_AVERAGE_SUP_DISTANCE_FIELD_SAT_50,
                        Messages.AdigeEditor_THE_FIELD_IN_THE_INPUT_BASIN_LAYER_WITH_THE_AVERAGE_VALUE_OF_THE_SUPERFICIAL_WIDTH_FUNCTION_FOR_A_SATURATION_BETWEEN_20_AND_50_OF_THE_BASIN_IS_REQUIRED,
                        IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.AdigeMessages_SUP_DISTANCE_FIELD_SAT_50,
                        Messages.AdigeEditor_THE_FIELD_IN_THE_INPUT_BASIN_LAYER_WITH_THE_AVERAGE_VALUE_OF_THE_SUPERFICIAL_WIDTH_FUNCTION_FOR_A_SATURATION_HIGHER_THAN_50_OF_THE_BASIN_IS_REQUIRED,
                        IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.AdigeMessages_VARIANCE_SUP_DISTANCE_FIELD_SAT_20,
                        Messages.AdigeEditor_THE_FIELD_IN_THE_INPUT_BASIN_LAYER_WITH_THE_STANDARD_DEVIATION_OF_THE_SUPERFICIAL_WIDTH_FUNCTION_FOR_A_SATURATION_LESS_THAN_20_OF_THE_BASIN_IS_REQUIRED,
                        IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.AdigeMessages_VARIANCE_SUP_DISTANCE_FIELD_SAT_50,
                        Messages.AdigeEditor_THE_FIELD_IN_THE_INPUT_BASIN_LAYER_WITH_THE_STANDARD_DEVIATION_OF_THE_SUPERFICIAL_WIDTH_FUNCTION_FOR_A_SATURATION_BETWEEN_20_AND_50_OF_THE_BASIN_IS_REQUIRED,
                        IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.AdigeMessages_DISTANCE_FIELD_SAT_50,
                        Messages.AdigeEditor_THE_FIELD_IN_THE_INPUT_BASIN_LAYER_WITH_THE_STANDARD_DEVIATION_OF_THE_SUPERFICIAL_WIDTH_FUNCTION_FOR_A_SATURATION_HIGHER_THAN_50_OF_THE_BASIN_IS_REQUIRED,
                        IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.AdigeMessages_AVERAGE_SUB_DISTANCE_FIELD,
                        Messages.AdigeEditor_THE_FIELD_IN_THE_INPUT_BASIN_LAYER_WITH_THE_AVERAGE_VALUE_OF_THE_SUBSUPERFICIAL_WIDTH_FUNCTION_IS_REQUIRED,
                        IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.AdigeMessages_VARIANCE_SUB_DISTANCE_FIELD,
                        Messages.AdigeEditor_THE_FIELD_IN_THE_INPUT_BASIN_LAYER_WITH_THE_STANDARD_DEVIATION_OF_THE_SUBSUPERFICIAL_WIDTH_FUNCTION_IS_REQUIRED,
                        IMessageProvider.ERROR)) {
            isError = true;
        }

        /*
         * check on input scalar sets
         */
        if (!ck.checkFileExists(Messages.AdigeMessages_HYDROMETERS_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.AdigeMessages_DAMS_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.AdigeMessages_TRIBUTARIES_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.AdigeMessages_OFFTAKES_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.WARNING)) {
        }
        if (!ck.checkFileExists(Messages.AdigeMessages_RAIN_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.AdigeMessages_VEGETATION_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }

        /*
         * check on input features layers
         */
        if (!ck.checkString(Messages.AdigeMessages_HYDROMETERS_LAYER,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkString(Messages.AdigeMessages_DAMS_LAYER,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkString(Messages.AdigeMessages_TRIBUTARIES_LAYER,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        // if (!ck.checkString(Messages.AdigeMessages_OFFTAKES_LAYER,
        // Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE,
        // IMessageProvider.ERROR)) {
        // isError = true;
        // }
        if (!ck.checkString(Messages.AdigeMessages_NETWORK_LAYER,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkString(Messages.BASINS_LAYER,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }

        /*
         * check on boundary conditions
         */
        if (!ck
                .checkFileExists(
                        Messages.AdigeMessages_BOUNDARY_CONDITIONS_TO_USE,
                        Messages.AdigeEditor_THERE_ARE_NO_INPUT_BOUNDARY_CONDITIONS_OR_THE_FILE_PROVIDED_IS_NOT_VALID_USING_THE_DEFAULT_VALUES,
                        IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkFileWritable(
                        Messages.AdigeMessages_BOUNDARY_CONDITIONS_TO_SAVE,
                        Messages.AdigeEditor_THE_FILE_WHERE_TO_SAVE_THE_FINAL_CONDITIONS_IS_NOT_PROVIDED_OR_THE_PROVIDED_PATH_IS_NOT_VALID_FINAL_CONDITIONS_WILL_NOT_BE_SAVED,
                        IMessageProvider.WARNING)) {
        }

        /*
         * check on model parameters
         */
        if (!ck.checkIsDouble(Messages.AdigeMessages_SUPERFICIAL_VELOCITY,
                Messages.AdigeEditor_THE_AVERAGE_SUPERFICIAL_VELOCITY_IS_REQUIRED,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkIsDouble(Messages.AdigeMessages_SUBSUPERFICIAL_VELOCITY,
                Messages.AdigeEditor_THE_AVERAGE_SUBSUPERFICIAL_VELOCITY_IS_REQUIRED,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.AdigeMessages_DO_PENMAN_EVAPOTRANSPIRATION,
                        Messages.AdigeEditor_THE_EVAPOTRANSPIRATION_WILL_BE_CALCULATED_WITH_A_SIMPLIFIED_METHOD_WRITE_TRUE_FOR_PENMAN_MONTEITH_EVAPOTRASPIRATION,
                        IMessageProvider.WARNING)) {
        }
        if (!ck.checkString(Messages.AdigeMessages_OUTPUT_PFAFSTETTER_IDS,
                Messages.AdigeEditor_THE_OUTPUT_VALUES_WILL_BE_REFERRED_ONLY_TO_THE_OUTLET_SECTION,
                IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsDouble(
                        Messages.AdigeMessages_INITIAL_DISCHARGE_PER_AREA_COEFFICIENT,
                        Messages.AdigeEditor_THE_INITIAL_DISCHARGE_WILL_BE_CALCULATED_USING_THE_DEFAULT_VALUE_OF_DISCHARGE_PER_UNIT_ARE_IN_SQUARE_KM_0_01,
                        IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsDouble(
                        Messages.AdigeMessages_INITIAL_FRACTION_OF_SUP_DISCHARGE,
                        Messages.AdigeEditor_THE_INITIAL_FRACTION_OF_SUPERFICIAL_DISCHARGE_IS_NOT_PROVIDED_USING_THE_DEFAULT_VALUE_OF_0_3_OF_THE_TOTAL_INITIAL_DISCHARGE,
                        IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsDouble(
                        Messages.AdigeMessages_INITIAL_FRACTION_OF_SUB_DISCHARGE,
                        Messages.AdigeEditor_THE_INITIAL_FRACTION_OF_SUBSUPERFICIAL_DISCHARGE_IS_NOT_PROVIDED_USING_THE_DEFAULT_VALUE_OF_0_7_OF_THE_TOTAL_INITIAL_DISCHARGE,
                        IMessageProvider.WARNING)) {
        }
        if (!ck.checkIsDouble(Messages.AdigeMessages_GAUKLER_STRICKLER_COEFFICIENT,
                Messages.AdigeEditor_THE_SATURATED_HYDRAULIC_CONDUCTIVITY_IS_REQUIRED,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkIsDouble(Messages.AdigeMessages_MSTEXP_COEFFICIENT,
                Messages.AdigeEditor_THE_MSTEXP_COEFFICIENT_IS_REQUIRED, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkIsDouble(Messages.AdigeMessages_SPECYIELD_COEFFICIENT,
                Messages.AdigeEditor_THE_SPECYIELD_COEFFICIENT_IS_REQUIRED, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkIsDouble(Messages.AdigeMessages_SOIL_POROSITY,
                Messages.AdigeEditor_THE_SOIL_POROSITY_COEFFICIENT_IS_REQUIRED,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkIsDouble(
                        Messages.AdigeMessages_CONSTANT_VALUE_OF_EVAPOTRANSPIRATION,
                        Messages.AdigeEditor_THE_EVAPORATION_COEFFICIENT_IS_REQUIRED_ONLY_FOR_THE_SIMPLIFIED_METHOD,
                        IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsDouble(
                        Messages.AdigeMessages_SATURATION_CONSTANT,
                        Messages.AdigeEditor_THE_PARAMETER_THAT_CORRELATE_THE_SATURATED_VOLUME_TO_THE_SATURATED_AREA_IS_REQUIRED,
                        IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkIsDouble(
                        Messages.AdigeMessages_FRACTION_OF_SUP_DISCHARGE_FROM_GLACIERS,
                        Messages.AdigeEditor_THE_FRACTION_OF_THE_GLACIER_DISCHARGE_THAT_IS_CONSIDERD_AS_SUPERFICIAL_FLOW_IS_NOT_PROVIDED_USING_THE_DEFAULT_VALUE_OF_0_7,
                        IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsDouble(
                        Messages.AdigeMessages_FRACTION_OF_SUB_DISCHARGE_FROM_GLACIERS,
                        Messages.AdigeEditor_THE_FRACTION_OF_THE_GLACIER_DISCHARGE_THAT_IS_CONSIDERD_AS_SUBSUPERFICIAL_FLOW_IS_NOT_PROVIDED_USING_THE_DEFAULT_VALUE_OF_0_3,
                        IMessageProvider.WARNING)) {
        }

        /*
         * check if logging
         */
        if (!ck.checkString(Messages.AdigeMessages_DO_LOGGING,
                Messages.AdigeEditor_LOGGING_DATA_WILL_NOT_BE_DISPLAYED_ON_J_GRASS_CONSOLE,
                IMessageProvider.WARNING)) {
        }

        if (isError) {
            return;
        }

        /*
         * if verything went well, the command has to be constructed and
         * launched.
         */

        final String command = createCommand();

        IRunnableWithProgress operation = new IRunnableWithProgress(){

            public void run( IProgressMonitor pm ) throws InvocationTargetException,
                    InterruptedException {

                pm.beginTask("h.adige", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                ConsoleCommandExecutor cCE = new ConsoleCommandExecutor();
                cCE.execute("h.adige", command, null, null, //$NON-NLS-1$
                        ConsoleCommandExecutor.OUTPUTTYPE_BTCONSOLE, null, null);
                pm.done();
            }
        };
        PlatformGIS.runInProgressDialog("h.adige", true, operation, true); //$NON-NLS-1$

    }

    @SuppressWarnings("nls")
    private String createCommand() {
        StringBuilder sB = new StringBuilder();
        sB.append("# MAPSET= ").append("asd").append("\n");
        sB.append("# GISBASE = ").append("asd").append("\n");
        String str = textsStringsMap.get(Messages.START_DATE_YYYY_MM_DD_HH_MM);
        sB.append("# STARTDATE = ").append(str).append("\n");
        str = textsStringsMap.get(Messages.END_DATE_YYYY_MM_DD_HH_MM);
        sB.append("# ENDDATE = ").append(str).append("\n");
        str = textsStringsMap.get(Messages.TIMESTEP_IN_MINUTES);
        sB.append("# DELTAT= ").append(str).append("\n");
        sB.append("\n");
        sB.append("jgrass {").append("\n");
        sB.append("h.adige").append("\n");

        str = textsStringsMap.get(Messages.NETNUM_FIELD);
        if (str.length() > 0)
            sB.append("--netnumattr").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_PFAFSTETTER_FIELD);
        if (str.length() > 0)
            sB.append("--pfafattr").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_BARICENTER_FIELD);
        if (str.length() > 0)
            sB.append("--baricenterattr").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_STARTELEVATION_FIELD);
        if (str.length() > 0)
            sB.append("--startelevattr").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_ENDELEVATION_FIELD);
        if (str.length() > 0)
            sB.append("--endelevattr").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MONITORINGPOINT_ID_FIELD);
        if (str.length() > 0)
            sB.append("--idmonpointattr").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_AVERAGE_SUP_DISTANCE_FIELD_SAT_20);
        if (str.length() > 0)
            sB.append("--avgsup10").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_AVERAGE_SUP_DISTANCE_FIELD_SAT_50);
        if (str.length() > 0)
            sB.append("--avgsup30").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_SUP_DISTANCE_FIELD_SAT_50);
        if (str.length() > 0)
            sB.append("--avgsup60").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_VARIANCE_SUP_DISTANCE_FIELD_SAT_20);
        if (str.length() > 0)
            sB.append("--varsup10").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_VARIANCE_SUP_DISTANCE_FIELD_SAT_50);
        if (str.length() > 0)
            sB.append("--varsup30").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_DISTANCE_FIELD_SAT_50);
        if (str.length() > 0)
            sB.append("--varsup60").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_AVERAGE_SUB_DISTANCE_FIELD);
        if (str.length() > 0)
            sB.append("--avgsub").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_VARIANCE_SUB_DISTANCE_FIELD);
        if (str.length() > 0)
            sB.append("--varsub").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_SUPERFICIAL_VELOCITY);
        if (str.length() > 0)
            sB.append("--vsup").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_SUBSUPERFICIAL_VELOCITY);
        if (str.length() > 0)
            sB.append("--vsub").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_VEGETATION_FIELD);
        if (str.length() > 0)
            sB.append("--vegetation").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_DO_PENMAN_EVAPOTRANSPIRATION);
        if (str.length() > 0)
            sB.append("--dopenman").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_OUTPUT_PFAFSTETTER_IDS);
        if (str.length() > 0)
            sB.append("--outpfafids").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_INITIAL_DISCHARGE_PER_AREA_COEFFICIENT);
        if (str.length() > 0)
            sB.append("--startqperarea").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_INITIAL_FRACTION_OF_SUP_DISCHARGE);
        if (str.length() > 0)
            sB.append("--startsupqfract").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_INITIAL_FRACTION_OF_SUB_DISCHARGE);
        if (str.length() > 0)
            sB.append("--startsubqfract").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_GAUKLER_STRICKLER_COEFFICIENT);
        if (str.length() > 0)
            sB.append("--ks").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_MSTEXP_COEFFICIENT);
        if (str.length() > 0)
            sB.append("--mstexp").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_SPECYIELD_COEFFICIENT);
        if (str.length() > 0)
            sB.append("--specyield").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_SOIL_POROSITY);
        if (str.length() > 0)
            sB.append("--porosity").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_CONSTANT_VALUE_OF_EVAPOTRANSPIRATION);
        if (str.length() > 0)
            sB.append("--etrate").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_SATURATION_CONSTANT);
        if (str.length() > 0)
            sB.append("--satconst").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_FRACTION_OF_SUP_DISCHARGE_FROM_GLACIERS);
        if (str.length() > 0)
            sB.append("--glaciersup").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_FRACTION_OF_SUB_DISCHARGE_FROM_GLACIERS);
        if (str.length() > 0)
            sB.append("--glaciersub").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_HYDROMETERS_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-hfeatures").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_HYDROMETERS_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-hdata").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_DAMS_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-dfeatures").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_DAMS_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-ddata").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_TRIBUTARIES_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-tfeatures").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_TRIBUTARIES_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-tdata").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_OFFTAKES_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-ofeatures").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_OFFTAKES_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-odata").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_NETWORK_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-netpfaf").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.BASINS_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-hillslope").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_RAIN_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-rain").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_VEGETATION_DATA);
        if (str.length() > 0)
            sB.append("--iscalar-vegetation").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_BOUNDARY_CONDITIONS_TO_USE);
        if (str.length() > 0)
            sB.append("--boundaryin").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_BOUNDARY_CONDITIONS_TO_SAVE);
        if (str.length() > 0)
            sB.append("--boundaryout").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_DO_LOGGING);
        if (str.length() > 0)
            sB.append("--log").append(" \"").append(str).append("\"\n");
        sB
                .append("--ochart-discharge")
                .append(" \"")
                .append(
                        "TIMELINE#Basin_response#date#discharge#PassirioCalc#sup#sub#PassirioMeas#meassup#meassub")
                .append("\"\n");

        String pfaffsForOut = outpfafids.replaceAll(",", "#");
        sB.append("--ochart-s1").append(" \"").append("TIMELINE#S1#date#nonsaturatedwc#").append(
                pfaffsForOut).append("\"\n");
        sB.append("--ochart-s2").append(" \"").append("TIMELINE#S2#date#saturatedwc#").append(
                pfaffsForOut).append("\"\n");
        sB.append("--ochart-s3").append(" \"").append("TIMELINE#satsurf#date#satsurface#").append(
                pfaffsForOut).append("\"\n");
        sB.append("--ochart-brain").append(" \"").append("TIMELINE#basinrain#date#rain#").append(
                pfaffsForOut).append("\"\n");
        str = textsStringsMap.get(Messages.AdigeMessages_OUTPUT_PATH);
        if (str.length() > 0)
            sB.append("--duffylogpath").append(" \"").append(str).append("\"\n");
        sB.append("}");

        return sB.toString();
    }

}
