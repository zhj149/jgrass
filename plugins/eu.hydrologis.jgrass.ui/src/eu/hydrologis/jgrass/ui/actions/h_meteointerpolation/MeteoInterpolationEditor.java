package eu.hydrologis.jgrass.ui.actions.h_meteointerpolation;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Set;

import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
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

import eu.hydrologis.jgrass.ui.UiPlugin;
import eu.hydrologis.jgrass.ui.actions.h_utils.Checker;
import eu.hydrologis.jgrass.ui.actions.messages.Messages;
import eu.hydrologis.jgrass.ui.console.ConsoleCommandExecutor;

public class MeteoInterpolationEditor extends EditorPart implements SelectionListener {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$

    private static final String DOTDOTDOT = "..."; //$NON-NLS-1$
    public static final String ID = "eu.hydrologis.jgrass.ui.actions.h_meteointerpolation.MeteoInterpolationEditor"; //$NON-NLS-1$
    private FormToolkit toolkit;
    private ScrolledForm scrolledForm;

    private HashMap<String, String> textsStringsMap = new HashMap<String, String>();
    private HashMap<String, Text> textsMap = new HashMap<String, Text>();
    private IMessageManager mesgManager;
    private IPreferenceStore preferenceStore;

    // vars
    private String startDateStr;
    private String endDateStr;
    private String timeStepStr;

    // variogram
    private String idfield;
    private String distance;
    private String iscalarinputvalues;
    private String iflayerpositions;
    private String oscalarcloud;

    // kriging
    private String idfieldkriging;
    private String idfieldinterpolated;
    private String maxpoints;
    private String minpoints;
    private String model;
    private String nugget;
    private String sill;
    private String range;
    // private String dovariance;
    private String searchradius;
    private String itscalarinputvalues;
    private String iflayerpositionskriging;
    private String iflayerinterpolatedpositions;
    private String oscalaroutputvalues;

    // jami
    private String type;
    private String stationsnum;
    private String bins;
    private String stationid;
    private String stationelev;
    private String basinid;
    private String iscalaraltimetry;
    private String itscalarvalues;
    private String iflayerstations;
    private String iflayerbasins;
    private String oscalarout;

    private Button variogramExecutionButton;

    private Button krigingExecutionButton;

    private Button jamiExecutionButton;

    private String command;

    public MeteoInterpolationEditor() {
        if (preferenceStore == null)
            preferenceStore = UiPlugin.getDefault().getPreferenceStore();

        startDateStr = preferenceStore.getString(ID + Messages.START_DATE_YYYY_MM_DD_HH_MM);
        endDateStr = preferenceStore.getString(ID + Messages.END_DATE_YYYY_MM_DD_HH_MM);
        timeStepStr = preferenceStore.getString(ID + Messages.TIMESTEP_IN_MINUTES);

        idfield = preferenceStore.getString(ID + ID
                + Messages.MeteoInterpolationEditor_THE_NETNUM_ID_FIELD);
        distance = preferenceStore.getString(ID + ID + Messages.MeteoInterpolationEditor_DISTANCE);
        iscalarinputvalues = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_VARIOGRAM_INPUT_RAIN_DATA);
        iflayerpositions = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_VARIOGRAM_RAIN_STATIONS_LAYER);
        oscalarcloud = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_OUTPUT_VARIOGRAM_DATA_FILE);

        idfieldkriging = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_KRIGING_MONITORING_POINTS_ID_FIELD);
        idfieldinterpolated = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_ID_FIELD_OF_INTERPOLATED_POINTS);
        maxpoints = preferenceStore
                .getString(ID + Messages.MeteoInterpolationEditor_MAXIMUM_POINTS);
        minpoints = preferenceStore
                .getString(ID + Messages.MeteoInterpolationEditor_MINIMUM_POINTS);
        model = preferenceStore.getString(ID + Messages.MeteoInterpolationEditor_MODEL);
        nugget = preferenceStore.getString(ID + Messages.MeteoInterpolationEditor_NUGGET);
        sill = preferenceStore.getString(ID + Messages.MeteoInterpolationEditor_SILL);
        range = preferenceStore.getString(ID + Messages.MeteoInterpolationEditor_RANGE);
        // dovariance = preferenceStore
        // .getString(ID+Messages.MeteoInterpolationEditor_CALCULATE_VARIANCE);
        searchradius = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_SEARCH_RADIUS);
        itscalarinputvalues = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_KRIGING_INPUT_RAIN_DATA);
        iflayerpositionskriging = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_KRIGING_RAIN_STATIONS_LAYER);
        iflayerinterpolatedpositions = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_INTERPOLATED_POSITIONS_LAYER);
        oscalaroutputvalues = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_INTERPOLATED_OUTPUT_DATA);

        type = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_THE_DATA_TYPE_TO_BE_INTERPOLATED);
        stationsnum = preferenceStore
                .getString(ID
                        + Messages.MeteoInterpolationEditor_MAXIMUM_NUMBER_OF_STATIONS_TO_USE_PER_ELEVATION_BAND);
        bins = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_NUMBER_OF_BINS_TO_USE);
        stationid = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_JAMI_MONITORING_POINTS_ID_FIELD);
        stationelev = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_FIELD_OF_THE_STATION_ELEVATION);
        basinid = preferenceStore.getString(ID + Messages.MeteoInterpolationEditor_NETNUMFIELD);
        iscalaraltimetry = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_ALTIMETRY_INPUT_DATA);
        itscalarvalues = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_METEO_INPUT_DATA);
        iflayerstations = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_LAYER_OF_STATIONS);
        iflayerbasins = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_BASINS_LAYER);
        oscalarout = preferenceStore.getString(ID
                + Messages.MeteoInterpolationEditor_INTERPOLATED_OUTPUT_DATA);

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

        // variogram
        createVariogramPanel();

        // kriging
        createKrigingPanel();

        // jami
        createJamiPanel();

    }

    private void createMainPanel() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.MAIN_PANEL);
        section.setDescription(Messages.MAIN_PANEL_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
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

        variogramExecutionButton = toolkit.createButton(mainSectionComposite,
                Messages.MeteoInterpolationEditor_EXECUTE_VARIOGRAM, SWT.PUSH);
        variogramExecutionButton.addSelectionListener(this);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        variogramExecutionButton.setLayoutData(gd);

        krigingExecutionButton = toolkit.createButton(mainSectionComposite,
                Messages.MeteoInterpolationEditor_EXECUTE_KRIGING, SWT.PUSH);
        krigingExecutionButton.addSelectionListener(this);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        krigingExecutionButton.setLayoutData(gd);

        jamiExecutionButton = toolkit.createButton(mainSectionComposite,
                Messages.MeteoInterpolationEditor_EXECUTE_JAMI, SWT.PUSH);
        jamiExecutionButton.addSelectionListener(this);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        jamiExecutionButton.setLayoutData(gd);

    }

    private void createVariogramPanel() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.MeteoInterpolationEditor_VARIOGRAM);
        section.setDescription(Messages.MeteoInterpolationEditor_VARIOGRAM_DECSR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 2;
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(3, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, idfield,
                Messages.MeteoInterpolationEditor_THE_NETNUM_ID_FIELD);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, distance, Messages.MeteoInterpolationEditor_DISTANCE);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, iflayerpositions,
                Messages.MeteoInterpolationEditor_VARIOGRAM_RAIN_STATIONS_LAYER);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelTextButton(mainSectionComposite, iscalarinputvalues,
                Messages.MeteoInterpolationEditor_VARIOGRAM_INPUT_RAIN_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, oscalarcloud,
                Messages.MeteoInterpolationEditor_OUTPUT_VARIOGRAM_DATA_FILE, SWT.SAVE);

    }

    private void createKrigingPanel() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.MeteoInterpolationEditor_KRIGING);
        section.setDescription(Messages.MeteoInterpolationEditor_KRIGING_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 2;
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(3, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, idfieldkriging,
                Messages.MeteoInterpolationEditor_KRIGING_MONITORING_POINTS_ID_FIELD);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, idfieldinterpolated,
                Messages.MeteoInterpolationEditor_ID_FIELD_OF_INTERPOLATED_POINTS);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, maxpoints,
                Messages.MeteoInterpolationEditor_MAXIMUM_POINTS);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, minpoints,
                Messages.MeteoInterpolationEditor_MINIMUM_POINTS);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, model, Messages.MeteoInterpolationEditor_MODEL);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, nugget, Messages.MeteoInterpolationEditor_NUGGET);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, sill, Messages.MeteoInterpolationEditor_SILL);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, range, Messages.MeteoInterpolationEditor_RANGE);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, searchradius,
                Messages.MeteoInterpolationEditor_SEARCH_RADIUS);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        // makeLabelText(mainSectionComposite, dovariance,
        // Messages.MeteoInterpolationEditor_CALCULATE_VARIANCE);
        //        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, iflayerpositionskriging,
                Messages.MeteoInterpolationEditor_KRIGING_RAIN_STATIONS_LAYER);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, iflayerinterpolatedpositions,
                Messages.MeteoInterpolationEditor_INTERPOLATED_POSITIONS_LAYER);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelTextButton(mainSectionComposite, itscalarinputvalues,
                Messages.MeteoInterpolationEditor_KRIGING_INPUT_RAIN_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, oscalaroutputvalues,
                Messages.MeteoInterpolationEditor_INTERPOLATED_OUTPUT_DATA, SWT.SAVE);

    }

    private void createJamiPanel() {
        Section section = toolkit.createSection(scrolledForm.getBody(), Section.DESCRIPTION
                | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        section.setText(Messages.MeteoInterpolationEditor_JAMI);
        section.setDescription(Messages.MeteoInterpolationEditor_JAMI_DESCR);
        TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
        td.colspan = 2;
        section.setLayoutData(td);

        Composite mainSectionComposite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout(3, false);
        mainSectionComposite.setLayout(layout);
        section.setClient(mainSectionComposite);

        makeLabelText(mainSectionComposite, type,
                Messages.MeteoInterpolationEditor_THE_DATA_TYPE_TO_BE_INTERPOLATED);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(
                mainSectionComposite,
                stationsnum,
                Messages.MeteoInterpolationEditor_MAXIMUM_NUMBER_OF_STATIONS_TO_USE_PER_ELEVATION_BAND);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, bins,
                Messages.MeteoInterpolationEditor_NUMBER_OF_BINS_TO_USE);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, stationid,
                Messages.MeteoInterpolationEditor_JAMI_MONITORING_POINTS_ID_FIELD);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, stationelev,
                Messages.MeteoInterpolationEditor_FIELD_OF_THE_STATION_ELEVATION);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, basinid, Messages.MeteoInterpolationEditor_NETNUMFIELD);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, iflayerstations,
                Messages.MeteoInterpolationEditor_LAYER_OF_STATIONS);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelText(mainSectionComposite, iflayerbasins,
                Messages.MeteoInterpolationEditor_BASINS_LAYER);
        toolkit.createLabel(mainSectionComposite, ""); //$NON-NLS-1$
        makeLabelTextButton(mainSectionComposite, iscalaraltimetry,
                Messages.MeteoInterpolationEditor_ALTIMETRY_INPUT_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, itscalarvalues,
                Messages.MeteoInterpolationEditor_METEO_INPUT_DATA, SWT.OPEN);
        makeLabelTextButton(mainSectionComposite, oscalarout,
                Messages.MeteoInterpolationEditor_INTERPOLATED_METEO_OUTPUT_DATA, SWT.SAVE);

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

        Object source = e.getSource();
        boolean isVario = false;
        boolean isKrige = false;
        boolean isJami = false;
        if (source.equals(variogramExecutionButton)) {
            isVario = true;
        }
        if (source.equals(krigingExecutionButton)) {
            isKrige = true;
        }
        if (source.equals(jamiExecutionButton)) {
            isJami = true;
        }

        boolean isError = false;

        Checker ck = new Checker(mesgManager, textsStringsMap, textsMap, dateFormatter);

        /*
         * check the models parameter for time execution
         */
        if (!isVario) {
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
        }

        /*
         * variogram panel
         */
        if (isVario) {
            if (!ck.checkString(Messages.MeteoInterpolationEditor_THE_NETNUM_ID_FIELD,
                    Messages.THE_NETNUM_FIELD_IS_MANDATORY, IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck
                    .checkIsDouble(
                            Messages.MeteoInterpolationEditor_DISTANCE,
                            Messages.MeteoInterpolationEditor_THE_MAXIMUN_POINT_DISTANCE_FOR_CORRELATION_IS_REQUIRED,
                            IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkString(Messages.MeteoInterpolationEditor_VARIOGRAM_RAIN_STATIONS_LAYER,
                    Messages.MeteoInterpolationEditor_THE_SUPPLIED_PARAMETER_HAS_TO_BE_A_STRING,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck
                    .checkFileExists(Messages.MeteoInterpolationEditor_VARIOGRAM_INPUT_RAIN_DATA,
                            Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE,
                            IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkFileWritable(Messages.MeteoInterpolationEditor_OUTPUT_VARIOGRAM_DATA_FILE,
                    Messages.THERE_IS_NO_OUTPUT_FILE_OR_THE_FILE_YOU_PROVIDED_IS_NOT_VALID,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
        }

        /*
         * kriging panel
         */
        if (isKrige) {
            if (!ck.checkString(
                    Messages.MeteoInterpolationEditor_KRIGING_MONITORING_POINTS_ID_FIELD,
                    Messages.THE_MONITORING_POINT_ID_FIELD_IS_REQUIRED, IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkString(Messages.MeteoInterpolationEditor_ID_FIELD_OF_INTERPOLATED_POINTS,
                    Messages.THE_NETNUM_FIELD_IS_MANDATORY, IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck
                    .checkIsDouble(
                            Messages.MeteoInterpolationEditor_MAXIMUM_POINTS,
                            Messages.MeteoInterpolationEditor_THE_MAXIMUN_POINTS_TO_USE_FOR_INTERPOLATION_IS_REQUIRED,
                            IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck
                    .checkIsDouble(
                            Messages.MeteoInterpolationEditor_MINIMUM_POINTS,
                            Messages.MeteoInterpolationEditor_THE_MINIMUM_POINTS_TO_USE_FOR_INTERPOLATION_IS_REQUIRED,
                            IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkIsInteger(Messages.MeteoInterpolationEditor_MODEL,
                    Messages.MeteoInterpolationEditor_THE_ID_OF_THE_VARIOGRAM_MODEL_IS_REQUIRED,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkIsDouble(Messages.MeteoInterpolationEditor_NUGGET,
                    Messages.MeteoInterpolationEditor_THE_NUGGET_PARAMETER_IS_REQUIRED,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkIsDouble(Messages.MeteoInterpolationEditor_SILL,
                    Messages.MeteoInterpolationEditor_THE_SILL_PARAMETER_IS_REQUIRED,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkIsDouble(Messages.MeteoInterpolationEditor_RANGE,
                    Messages.MeteoInterpolationEditor_THE_RANGE_PARAMETER_IS_REQUIRED,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkIsDouble(Messages.MeteoInterpolationEditor_SEARCH_RADIUS,
                    Messages.MeteoInterpolationEditor_THE_SEARCH_RADIUS_PARAMETER_IS_REQUIRED,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkString(Messages.MeteoInterpolationEditor_KRIGING_RAIN_STATIONS_LAYER,
                    Messages.MeteoInterpolationEditor_THE_SUPPLIED_PARAMETER_HAS_TO_BE_A_STRING,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkString(Messages.MeteoInterpolationEditor_INTERPOLATED_POSITIONS_LAYER,
                    Messages.MeteoInterpolationEditor_THE_SUPPLIED_PARAMETER_HAS_TO_BE_A_STRING,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck
                    .checkFileExists(Messages.MeteoInterpolationEditor_KRIGING_INPUT_RAIN_DATA,
                            Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE,
                            IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkFileWritable(Messages.MeteoInterpolationEditor_INTERPOLATED_OUTPUT_DATA,
                    Messages.THERE_IS_NO_OUTPUT_FILE_OR_THE_FILE_YOU_PROVIDED_IS_NOT_VALID,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
        }

        /*
         * jami panel
         */
        if (isJami) {
            if (!ck.checkIsInteger(
                    Messages.MeteoInterpolationEditor_THE_DATA_TYPE_TO_BE_INTERPOLATED,
                    Messages.MeteoInterpolationEditor_THE_ID_OF_THE_DATA_TYPE_IS_REQUIRED,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck
                    .checkIsInteger(
                            Messages.MeteoInterpolationEditor_MAXIMUM_NUMBER_OF_STATIONS_TO_USE_PER_ELEVATION_BAND,
                            Messages.MeteoInterpolationEditor_THE_MAXIMUM_NUMBER_OF_STATION_TO_BE_USED_FOR_EACH_ELEVATION_BAND_IS_REQUIRED,
                            IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck
                    .checkIsInteger(
                            Messages.MeteoInterpolationEditor_NUMBER_OF_BINS_TO_USE,
                            Messages.MeteoInterpolationEditor_THE_NUMBER_OF_BINS_INTO_WHICH_SUBDIVIDE_THE_STATION_S_ELEVATION_RANGE_IS_REQUIRED,
                            IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkString(Messages.MeteoInterpolationEditor_JAMI_MONITORING_POINTS_ID_FIELD,
                    Messages.THE_MONITORING_POINT_ID_FIELD_IS_REQUIRED, IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkString(Messages.MeteoInterpolationEditor_FIELD_OF_THE_STATION_ELEVATION,
                    Messages.MeteoInterpolationEditor_THE_STATION_ELEVATION_FIELD_IS_REQUIRED,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkString(Messages.MeteoInterpolationEditor_NETNUMFIELD,
                    Messages.THE_NETNUM_FIELD_IS_MANDATORY, IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkString(Messages.MeteoInterpolationEditor_LAYER_OF_STATIONS,
                    Messages.MeteoInterpolationEditor_THE_SUPPLIED_PARAMETER_HAS_TO_BE_A_STRING,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkString(Messages.MeteoInterpolationEditor_BASINS_LAYER,
                    Messages.MeteoInterpolationEditor_THE_SUPPLIED_PARAMETER_HAS_TO_BE_A_STRING,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck
                    .checkFileExists(Messages.MeteoInterpolationEditor_ALTIMETRY_INPUT_DATA,
                            Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE,
                            IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck
                    .checkFileExists(Messages.MeteoInterpolationEditor_METEO_INPUT_DATA,
                            Messages.THE_SUPPLIED_PARAMETER_IS_NOT_AN_EXISTING_FILE,
                            IMessageProvider.ERROR)) {
                isError = true;
            }
            if (!ck.checkFileWritable(
                    Messages.MeteoInterpolationEditor_INTERPOLATED_METEO_OUTPUT_DATA,
                    Messages.THERE_IS_NO_OUTPUT_FILE_OR_THE_FILE_YOU_PROVIDED_IS_NOT_VALID,
                    IMessageProvider.ERROR)) {
                isError = true;
            }
        }

        if (isError) {
            return;
        }

        /*
         * if verything went well, the command has to be constructed and
         * launched.
         */

        if (isVario) {
            command = createVarioCommand();
        } else if (isKrige) {
            command = createKrigeCommand();
        } else if (isJami) {
            command = createJamiCommand();
        }

        IRunnableWithProgress operation = new IRunnableWithProgress(){

            public void run( IProgressMonitor pm ) throws InvocationTargetException,
                    InterruptedException {

                pm.beginTask("h.meteointerpolation", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                ConsoleCommandExecutor cCE = new ConsoleCommandExecutor();
                cCE.execute("h.meteointerpolation", command, null, null, //$NON-NLS-1$
                        ConsoleCommandExecutor.OUTPUTTYPE_BTCONSOLE, null, null);
                pm.done();
            }
        };
        PlatformGIS.runInProgressDialog("h.meteointerpolation", true, operation, true); //$NON-NLS-1$

    }

    @SuppressWarnings("nls")
    private String createVarioCommand() {
        StringBuilder sB = new StringBuilder();
        sB.append("# MAPSET= ").append("asd").append("\n");
        sB.append("# GISBASE = ").append("asd").append("\n");
        sB.append("\n");
        sB.append("jgrass {").append("\n");
        sB.append("h.variogram").append("\n");

        String str = textsStringsMap.get(Messages.MeteoInterpolationEditor_THE_NETNUM_ID_FIELD);
        if (str.length() > 0)
            sB.append("--idfield").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_VARIOGRAM_INPUT_RAIN_DATA);
        if (str.length() > 0)
            sB.append("--iscalar-inputvalues").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_VARIOGRAM_RAIN_STATIONS_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-positions").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_OUTPUT_VARIOGRAM_DATA_FILE);
        if (str.length() > 0)
            sB.append("--oscalar-cloud").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_DISTANCE);
        if (str.length() > 0)
            sB.append("--distance").append(" \"").append(str).append("\"\n");
        sB.append("}");

        return sB.toString();
    }

    @SuppressWarnings("nls")
    private String createKrigeCommand() {
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
        sB.append("h.kriging").append("\n");

        str = textsStringsMap
                .get(Messages.MeteoInterpolationEditor_KRIGING_MONITORING_POINTS_ID_FIELD);
        if (str.length() > 0)
            sB.append("--idfield").append(" \"").append(str).append("\"\n");
        str = textsStringsMap
                .get(Messages.MeteoInterpolationEditor_ID_FIELD_OF_INTERPOLATED_POINTS);
        if (str.length() > 0)
            sB.append("--idfieldinterpolated").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_MAXIMUM_POINTS);
        if (str.length() > 0)
            sB.append("--maxpoints").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_MINIMUM_POINTS);
        if (str.length() > 0)
            sB.append("--minpoints").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_MODEL);
        if (str.length() > 0)
            sB.append("--model").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_NUGGET);
        if (str.length() > 0)
            sB.append("--nugget").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_SILL);
        if (str.length() > 0)
            sB.append("--sill").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_RANGE);
        if (str.length() > 0)
            sB.append("--range").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_SEARCH_RADIUS);
        if (str.length() > 0)
            sB.append("--searchradius").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_KRIGING_INPUT_RAIN_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-inputvalues").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_KRIGING_RAIN_STATIONS_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-positions").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_INTERPOLATED_POSITIONS_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-interpolatedpositions").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_INTERPOLATED_OUTPUT_DATA);
        if (str.length() > 0)
            sB.append("--oscalar-outputvalues").append(" \"").append(str).append("\"\n");

        sB.append("}");

        return sB.toString();
    }

    @SuppressWarnings("nls")
    private String createJamiCommand() {
        StringBuilder sB = new StringBuilder();
        sB.append("# MAPSET= ").append("asd").append("\n");
        sB.append("# GISBASE = ").append("asd").append("\n");
        sB.append("# STARTDATE = ").append(startDateStr).append("\n");
        sB.append("# ENDDATE = ").append(endDateStr).append("\n");
        sB.append("# DELTAT= ").append(timeStepStr).append("\n");
        sB.append("\n");
        sB.append("jgrass {").append("\n");
        sB.append("h.jami").append("\n");

        String str = textsStringsMap
                .get(Messages.MeteoInterpolationEditor_THE_DATA_TYPE_TO_BE_INTERPOLATED);
        if (str.length() > 0)
            sB.append("--type").append(" \"").append(str).append("\"\n");
        str = textsStringsMap
                .get(Messages.MeteoInterpolationEditor_MAXIMUM_NUMBER_OF_STATIONS_TO_USE_PER_ELEVATION_BAND);
        if (str.length() > 0)
            sB.append("--stationsnum").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_NUMBER_OF_BINS_TO_USE);
        if (str.length() > 0)
            sB.append("--bins").append(" \"").append(str).append("\"\n");
        str = textsStringsMap
                .get(Messages.MeteoInterpolationEditor_JAMI_MONITORING_POINTS_ID_FIELD);
        if (str.length() > 0)
            sB.append("--stationid").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_FIELD_OF_THE_STATION_ELEVATION);
        if (str.length() > 0)
            sB.append("--stationelev").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_NETNUMFIELD);
        if (str.length() > 0)
            sB.append("--basinid").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_ALTIMETRY_INPUT_DATA);
        if (str.length() > 0)
            sB.append("--iscalar-altimetry").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_LAYER_OF_STATIONS);
        if (str.length() > 0)
            sB.append("--iflayer-stations").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_BASINS_LAYER);
        if (str.length() > 0)
            sB.append("--iflayer-basins").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_METEO_INPUT_DATA);
        if (str.length() > 0)
            sB.append("--itscalar-values").append(" \"").append(str).append("\"\n");
        str = textsStringsMap.get(Messages.MeteoInterpolationEditor_INTERPOLATED_METEO_OUTPUT_DATA);
        if (str.length() > 0)
            sB.append("--oscalar-out").append(" \"").append(str).append("\"\n");

        sB.append("}");

        return sB.toString();
    }

}
