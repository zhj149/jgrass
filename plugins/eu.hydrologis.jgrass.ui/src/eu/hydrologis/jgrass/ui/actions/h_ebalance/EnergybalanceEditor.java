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
package eu.hydrologis.jgrass.ui.actions.h_ebalance;

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
 * The energybalance model execution form. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @author Silvia Franceschi (www.hydrologis.com)
 */
public class EnergybalanceEditor extends EditorPart implements SelectionListener {

    private static final String DOTDOTDOT = "..."; //$NON-NLS-1$
    public static final String ID = "eu.hydrologis.jgrass.ui.actions.h_ebalance.EnergybalanceEditor"; //$NON-NLS-1$
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
    private String oscalaradige;

    private String idfield;
    private String swestart;
    private String usofield;
    private String glaciervalue;
    private String avo;
    private String airo;
    private String rhoglacier;
    private String glacierswe;
    private String sr;
    private String massbalancelog;
    private String iflayerbasin;
    private String itscalarrain;
    private String itscalartemperature;
    private String itscalarwind;
    private String itscalarpressure;
    private String itscalarhumidity;
    private String iscalarenergy;
    private String iscalararea;
    private String itscalardtday;
    private String itscalardtmonth;
    private String spreadpath;
    private String spwritepath;

    public EnergybalanceEditor() {
        if (preferenceStore == null)
            preferenceStore = UiPlugin.getDefault().getPreferenceStore();

        startDateStr = preferenceStore.getString(ID + Messages.START_DATE_YYYY_MM_DD_HH_MM);
        endDateStr = preferenceStore.getString(ID + Messages.END_DATE_YYYY_MM_DD_HH_MM);
        timeStepStr = preferenceStore.getString(ID + Messages.TIMESTEP_IN_MINUTES);

        oscalaradige = preferenceStore.getString(ID + Messages.EnergybalanceEditor_OUTPUT_PATH);

        idfield = preferenceStore.getString(ID + Messages.EnergybalanceEditor_FIELD_OF_THE_ID);
        swestart = preferenceStore.getString(ID + Messages.EnergybalanceEditor_START_SWE);
        usofield = preferenceStore.getString(ID
                + Messages.EnergybalanceEditor_FIELD_OF_THE_SOIL_USAGE);
        glaciervalue = preferenceStore.getString(ID
                + Messages.EnergybalanceEditor_VALUE_FOR_GLACIERS);
        avo = preferenceStore.getString(ID + Messages.EnergybalanceEditor_AVO);
        airo = preferenceStore.getString(ID + Messages.EnergybalanceEditor_AIRO);
        rhoglacier = preferenceStore.getString(ID + Messages.EnergybalanceEditor_RHO_FOR_GLACIERS);
        glacierswe = preferenceStore.getString(ID + Messages.EnergybalanceEditor_GLACIERS_SWE);
        sr = preferenceStore.getString(ID + Messages.EnergybalanceEditor_SR);
        massbalancelog = preferenceStore.getString(ID
                + Messages.EnergybalanceEditor_DO_MASSBALANCE_LOGGING);
        iflayerbasin = preferenceStore.getString(ID + Messages.EnergybalanceEditor_BASINS_LAYER);
        itscalarrain = preferenceStore.getString(ID + Messages.EnergybalanceEditor_BASINS_LAYER);
        itscalartemperature = preferenceStore.getString(ID
                + Messages.EnergybalanceEditor_TEMPERATURE_DATA);
        itscalarwind = preferenceStore.getString(ID + Messages.EnergybalanceEditor_WIND_DATA);
        itscalarpressure = preferenceStore.getString(ID
                + Messages.EnergybalanceEditor_PRESSURE_DATA);
        itscalarhumidity = preferenceStore.getString(ID
                + Messages.EnergybalanceEditor_HUMIDITY_DATA);
        iscalarenergy = preferenceStore.getString(ID + Messages.EnergybalanceEditor_ENERGY_DATA);
        iscalararea = preferenceStore.getString(ID + Messages.EnergybalanceEditor_AREA_DATA);
        itscalardtday = preferenceStore.getString(ID + Messages.EnergybalanceEditor_DT_DAY_DATA);
        itscalardtmonth = preferenceStore
                .getString(ID + Messages.EnergybalanceEditor_DT_MONTH_DATA);
        spreadpath = preferenceStore.getString(ID
                + Messages.EnergybalanceEditor_PATH_TO_SAFEPOINT_TO_READ);
        spwritepath = preferenceStore.getString(ID
                + Messages.EnergybalanceEditor_PATH_TO_SAFEPOINT_TO_WRITE);
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

        // model parameters
        createParametersSection();

        // layers and scalarsets
        createLayersSection();

        // safepoints
        createSafepointsSection();

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
        makeLabelTextButton(mainSectionComposite, oscalaradige,
                Messages.EnergybalanceEditor_OUTPUT_PATH, SWT.SAVE);

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
                Messages.EnergybalanceEditor_EXECUTE_H_ENERGYBALANCE, SWT.PUSH);
        executionButton.addSelectionListener(this);

        Section sectionImportExport = toolkit.createSection(scrolledForm.getBody(),
                Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        sectionImportExport.setText(Messages.IMPORTEXPORT_PANEL);
        sectionImportExport.setDescription(Messages.IMPORTEXPORT_PANEL_DESCR);
        td = new TableWrapData(TableWrapData.FILL_GRAB);
        sectionImportExport.setLayoutData(td);

        mainSectionComposite = toolkit.createComposite(sectionImportExport);
        layout = new GridLayout(1, false);
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
                            // MessageBox msgBox = new MessageBox(importButton.getShell(),
                            // SWT.ICON_ERROR);
                            // msgBox.setMessage("This doesn't seem to be a h.adige settings file.");
                            // msgBox.open();
                            // return;
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
                    MultiInputDialog dialog = new MultiInputDialog(exportToRemoteDatabaseButton
                            .getShell(), Messages.INFORMATION,
                            Messages.SUPPLY_INFO_FOR_THE_SIMULATION, titleLabel, descrLabel,
                            userLabel);
                    dialog.setBlockOnOpen(true);
                    int res = dialog.open();

                    if (res != IDialogConstants.OK_ID) {
                        return;
                    }

                    String title = dialog.getStringByLable(titleLabel);
                    String descr = dialog.getStringByLable(descrLabel);
                    String user = dialog.getStringByLable(userLabel);

                    IDatabaseConnection activeDatabaseConnection = DatabasePlugin.getDefault().getActiveDatabaseConnection();
                    SessionFactory sF = activeDatabaseConnection.getSessionFactory();

                    Session session = sF.openSession();
                    Transaction transaction = session.beginTransaction();

                    /*
                     * create simulation description
                     */
                    SimulationDescription simDescr = new SimulationDescription();
                    simDescr.setModel("h.energybalance"); //$NON-NLS-1$
                    simDescr.setTitle(title);
                    simDescr.setDescription(descr);
                    simDescr.setUser(user);

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

                    String outPath = textsStringsMap.get(Messages.EnergybalanceEditor_OUTPUT_PATH);
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

    private void createParametersSection() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.EnergybalanceEditor_PARAMETERS_SECTION);
        section.setDescription(Messages.EnergybalanceEditor_PARAMETERS_SECTION_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(2, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, idfield, Messages.EnergybalanceEditor_FIELD_OF_THE_ID);
        makeLabelText(mainSectionComposite, usofield,
                Messages.EnergybalanceEditor_FIELD_OF_THE_SOIL_USAGE);
        makeLabelText(mainSectionComposite, swestart, Messages.EnergybalanceEditor_START_SWE);
        makeLabelText(mainSectionComposite, glaciervalue,
                Messages.EnergybalanceEditor_VALUE_FOR_GLACIERS);
        makeLabelText(mainSectionComposite, avo, Messages.EnergybalanceEditor_AVO);
        makeLabelText(mainSectionComposite, airo, Messages.EnergybalanceEditor_AIRO);
        makeLabelText(mainSectionComposite, rhoglacier,
                Messages.EnergybalanceEditor_RHO_FOR_GLACIERS);
        makeLabelText(mainSectionComposite, glacierswe, Messages.EnergybalanceEditor_GLACIERS_SWE);
        makeLabelText(mainSectionComposite, sr, Messages.EnergybalanceEditor_SR);
    }

    private void createLayersSection() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.EnergybalanceEditor_LAYERS_AND_SCALARSETS_SECTION);
        section.setDescription(Messages.EnergybalanceEditor_LAYERS_AND_SCALARSETS_SECTION_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(3, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, iflayerbasin, Messages.EnergybalanceEditor_BASINS_LAYER);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelTextButton(mainSectionComposite, itscalarrain,
                Messages.EnergybalanceEditor_RAIN_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalartemperature,
                Messages.EnergybalanceEditor_TEMPERATURE_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalarwind,
                Messages.EnergybalanceEditor_WIND_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalarpressure,
                Messages.EnergybalanceEditor_PRESSURE_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalarhumidity,
                Messages.EnergybalanceEditor_HUMIDITY_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, iscalarenergy,
                Messages.EnergybalanceEditor_ENERGY_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, iscalararea,
                Messages.EnergybalanceEditor_AREA_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalardtday,
                Messages.EnergybalanceEditor_DT_DAY_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalardtmonth,
                Messages.EnergybalanceEditor_DT_MONTH_DATA, SWT.OPEN);
    }

    private void createSafepointsSection() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.EnergybalanceEditor_SAFEPOINTS_SECTION);
        section.setDescription(Messages.EnergybalanceEditor_SAFEPOINTS_SECTION_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(3, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelTextButton(mainSectionComposite, spreadpath,
                Messages.EnergybalanceEditor_PATH_TO_SAFEPOINT_TO_READ, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, spwritepath,
                Messages.EnergybalanceEditor_PATH_TO_SAFEPOINT_TO_WRITE, SWT.SAVE);

    }

    private void createLogging() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.EnergybalanceEditor_LOGGING_SECTION);
        section.setDescription(Messages.EnergybalanceEditor_LOGGING_SECTION_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(2, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, massbalancelog,
                Messages.EnergybalanceEditor_DO_MASSBALANCE_LOGGING);
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
        if (!ck.checkFileWritable(Messages.EnergybalanceEditor_OUTPUT_PATH,
                Messages.THERE_IS_NO_OUTPUT_FILE_OR_THE_FILE_YOU_PROVIDED_IS_NOT_VALID,
                IMessageProvider.ERROR)) {
            isError = true;
        }

        /*
         * Parameters section
         */

        if (!ck.checkString(Messages.EnergybalanceEditor_FIELD_OF_THE_ID,
                Messages.EnergybalanceEditor_THE_MONITORING_POINT_FOR_BASINS_IS_REQUIRED,
                IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck
                .checkString(
                        Messages.EnergybalanceEditor_FIELD_OF_THE_SOIL_USAGE,
                        Messages.EnergybalanceEditor_THE_SOIL_USAGE_WILL_NOT_BE_TAKEN_INTO_ACCOUNT_FOR_GLACIERS,
                        IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsDouble(
                        Messages.EnergybalanceEditor_START_SWE,
                        Messages.EnergybalanceEditor_STARTING_SWE_FOR_ALL_THE_BASIN_IS_NOT_PROVIDED_USING_THE_INIZIAL_VALUE_OF_SWE_0,
                        IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsInteger(
                        Messages.EnergybalanceEditor_VALUE_FOR_GLACIERS,
                        Messages.EnergybalanceEditor_THE_ID_OF_GLACIERS_IN_THE_SOIL_USAGE_LAYER_IS_NOT_PROVIDED,
                        IMessageProvider.WARNING)) {
        }
        if (!ck.checkIsDouble(Messages.EnergybalanceEditor_AVO,
                Messages.EnergybalanceEditor_NO_INPUT_DATA_FOR_AVO_USING_THE_DEFAULT_OF_0_85,
                IMessageProvider.WARNING)) {
        }
        if (!ck.checkIsDouble(Messages.EnergybalanceEditor_AIRO,
                Messages.EnergybalanceEditor_NO_INPUT_DATA_FOR_AIRO_USING_THE_DEFAULT_OF_0_65,
                IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsDouble(
                        Messages.EnergybalanceEditor_RHO_FOR_GLACIERS,
                        Messages.EnergybalanceEditor_NO_INPUT_DATA_FOR_THE_DENSITY_OF_THE_SNOW_ICE_ON_GLACIERS_USING_THE_DEFAULT_VALUE_OF_800_KG_M3,
                        IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsDouble(
                        Messages.EnergybalanceEditor_GLACIERS_SWE,
                        Messages.EnergybalanceEditor_NO_INPUT_DATA_FOR_MINIMUM_SWE_ON_GLACIERS_USING_THE_DEFAULT_VALUE_OF_5000_MM,
                        IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkIsDouble(
                        Messages.EnergybalanceEditor_SR,
                        Messages.EnergybalanceEditor_NO_INPUT_DATA_FOR_IRREDUCIBLE_WATER_SATURATION_ON_GLACIERS_USING_THE_DEFAULT_VALUE_OF_0_01_MM,
                        IMessageProvider.WARNING)) {
        }

        /*
         * layers section
         */
        if (!ck.checkString(Messages.EnergybalanceEditor_BASINS_LAYER,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_RAIN_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_TEMPERATURE_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_WIND_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_PRESSURE_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_HUMIDITY_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_ENERGY_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_AREA_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_DT_DAY_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_DT_MONTH_DATA,
                Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE, IMessageProvider.ERROR)) {
            isError = true;
        }

        /*
         * Savepoint section
         */
        if (!ck.checkFileExists(Messages.EnergybalanceEditor_PATH_TO_SAFEPOINT_TO_READ,
                Messages.EnergybalanceEditor_NO_INITIAL_CONDITIONS_TO_READ,
                IMessageProvider.WARNING)) {
        }
        if (!ck
                .checkFileWritable(Messages.EnergybalanceEditor_PATH_TO_SAFEPOINT_TO_WRITE,
                        Messages.EnergybalanceEditor_NO_FINAL_CONDITIONS_TO_WRITE,
                        IMessageProvider.WARNING)) {
        }

        /*
         * check if logging
         */
        if (!ck.checkString(Messages.EnergybalanceEditor_DO_MASSBALANCE_LOGGING,
                Messages.EnergybalanceEditor_LOGGING_DATA_WILL_NOT_BE_DISPLAYED_ON_J_GRASS_CONSOLE,
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

                pm.beginTask("h.energybalance", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                ConsoleCommandExecutor cCE = new ConsoleCommandExecutor();
                cCE.execute("h.energybalance", command, null, null, //$NON-NLS-1$
                        ConsoleCommandExecutor.OUTPUTTYPE_BTCONSOLE, null, null);
                pm.done();
            }
        };
        PlatformGIS.runInProgressDialog("h.energybalance", true, operation, true); //$NON-NLS-1$

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
        sB.append("h.energybalance").append("\n");

        str = textsStringsMap.get(Messages.EnergybalanceEditor_FIELD_OF_THE_ID);
        if (str.length() > 0)
            sB.append("--idfield").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_FIELD_OF_THE_SOIL_USAGE);
        if (str.length() > 0)
            sB.append("--usofield").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_START_SWE);
        if (str.length() > 0)
            sB.append("--swestart").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_VALUE_FOR_GLACIERS);
        if (str.length() > 0)
            sB.append("--glaciervalue").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_AVO);
        if (str.length() > 0)
            sB.append("--avo").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_AIRO);
        if (str.length() > 0)
            sB.append("--airo").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_RHO_FOR_GLACIERS);
        if (str.length() > 0)
            sB.append("--rhoglacier").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_GLACIERS_SWE);
        if (str.length() > 0)
            sB.append("--glacierswe").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_SR);
        if (str.length() > 0)
            sB.append("--sr").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_DO_MASSBALANCE_LOGGING);
        if (str.length() > 0)
            sB.append("--massbalancelog").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_BASINS_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-basin").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_RAIN_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-rain").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_TEMPERATURE_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-temperature").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_WIND_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-wind").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_PRESSURE_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-pressure").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_HUMIDITY_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-humidity").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_ENERGY_DATA);
        if (str.length() > 0)
            sB.append("--iscalar-energy").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_AREA_DATA);
        if (str.length() > 0)
            sB.append("--iscalar-area").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_DT_DAY_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-dtday").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_DT_MONTH_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-dtmonth").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_PATH_TO_SAFEPOINT_TO_READ);
        if (str.length() > 0)
            sB.append("--spreadpath").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_PATH_TO_SAFEPOINT_TO_WRITE);
        if (str.length() > 0)
            sB.append("--spwritepath").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.EnergybalanceEditor_OUTPUT_PATH);
        if (str.length() > 0)
            sB.append("--oscalar-adige").append(" \"").append(str).append("\"\n");

        sB.append("}");

        return sB.toString();
    }

}
