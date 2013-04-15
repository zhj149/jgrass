/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) C.U.D.A.M. Universita' di Trento
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
package eu.hydrologis.jgrass.console.editor.actions;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.examples.javaeditor.JavaEditor;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditor;

import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;
import eu.hydrologis.jgrass.ui.utilities.date.CalendarTimeGroup;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class RuntimePreferencesComposite extends Composite {

    private final static long timeDelta = (long) (60.0 * 1000.0);
    private final static long timeDeltaDefault = (long) (15.0)
            * RuntimePreferencesComposite.timeDelta;
    private Group grassPreferencesGroup = null;
    private Button useTimeCheck = null;
    private Label grassmapsetLabel = null;
    private Text grassmapsetText = null;
    private Group timeGroup = null;
    private Composite startTimeComposite = null;
    private Composite endTimeComposite = null;
    private Label timestepLabel = null;
    private Text timestepText = null;
    private CalendarTimeGroup startCdt;
    private CalendarTimeGroup endCdt;
    private Button okButton = null;
    private final ScopedPreferenceStore preferences;
    private JavaEditor javaEditor;

    public RuntimePreferencesComposite( Composite parent, int style, ITextEditor editor ) {

        super(parent, style);
        if (false == editor instanceof JavaEditor)
            throw new IllegalArgumentException();

        preferences = (ScopedPreferenceStore) ConsoleEditorPlugin.getDefault()
        .getPreferenceStore();
        javaEditor = (JavaEditor) editor;
        initialize();
    }

    private void initialize() {
        GridData gridData8 = new GridData();
        gridData8.horizontalAlignment = GridData.CENTER;
        gridData8.grabExcessHorizontalSpace = false;
        gridData8.widthHint = 100;
        gridData8.verticalAlignment = GridData.CENTER;
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.BEGINNING;
        gridData1.verticalAlignment = GridData.FILL;
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 12;
        gridLayout.marginHeight = 14;
        createGrassPreferencesGroup();
        this.setLayout(gridLayout);
        setSize(new Point(500, 350));
        useTimeCheck = new Button(this, SWT.CHECK);
        useTimeCheck.setText("model is time dependent");
        useTimeCheck.setLayoutData(gridData1);
        useTimeCheck.setSelection(null != javaEditor.projectOptions().getOption(
                ProjectOptions.JAVA_MODEL_TIME_DELTA)
                && null != javaEditor.projectOptions().getOption(
                        ProjectOptions.JAVA_MODEL_TIME_ENDING_UP)
                && null != javaEditor.projectOptions().getOption(
                        ProjectOptions.JAVA_MODEL_TIME_START_UP));
        useTimeCheck.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {

                Button checkButton = (Button) e.widget;
                if (checkButton.getSelection()) {
                    enableTime(true);
                } else {
                    enableTime(false);
                }
            }
        });
        createTimeGroup();
        okButton = new Button(this, SWT.PUSH);
        okButton.setText("ok");
        okButton.setLayoutData(gridData8);
        okButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {

                /*
                 * assign all the values
                 */
                if (false == useTimeCheck.getSelection()) {

                    // TIMESTEP
                    javaEditor.projectOptions().setOption(ProjectOptions.JAVA_MODEL_TIME_DELTA,
                            null);

                    // ENDING UP
                    javaEditor.projectOptions().setOption(ProjectOptions.JAVA_MODEL_TIME_ENDING_UP,
                            null);

                    // START UP
                    javaEditor.projectOptions().setOption(ProjectOptions.JAVA_MODEL_TIME_START_UP,
                            null);
                } else {

                    // TIMESTEP
                    try {

                        final long __delta = Long.parseLong(timestepText.getText());
                        if (0 >= __delta)
                            javaEditor.projectOptions().setOption(
                                    ProjectOptions.JAVA_MODEL_TIME_DELTA, null);
                        else
                            javaEditor.projectOptions().setOption(
                                    ProjectOptions.JAVA_MODEL_TIME_DELTA,
                                    (long) (__delta * RuntimePreferencesComposite.timeDelta));
                    } catch (NumberFormatException e1) {

                        javaEditor.projectOptions().setOption(ProjectOptions.JAVA_MODEL_TIME_DELTA,
                                (long) (RuntimePreferencesComposite.timeDeltaDefault ));
                    }

                    // ENDING UP
                    try {

                        javaEditor.projectOptions().setOption(
                                ProjectOptions.JAVA_MODEL_TIME_ENDING_UP,
                                DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                        DateFormat.MEDIUM, Locale.getDefault()).format(
                                        endCdt.getDate()));
                    } catch (Exception e1) {

                        javaEditor.projectOptions().setOption(
                                ProjectOptions.JAVA_MODEL_TIME_ENDING_UP, null);
                    }

                    // START UP
                    try {

                        javaEditor.projectOptions().setOption(
                                ProjectOptions.JAVA_MODEL_TIME_START_UP,
                                DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                        DateFormat.MEDIUM, Locale.getDefault()).format(
                                        startCdt.getDate()));
                    } catch (Exception e1) {

                        javaEditor.projectOptions().setOption(
                                ProjectOptions.JAVA_MODEL_TIME_START_UP, null);
                    }
                }

                // DATABASE, LOCATION, MAPSET
                final String __mapset = grassmapsetText.getText().trim();
                if (0 < __mapset.length())
                    javaEditor.projectOptions().setOption(ProjectOptions.COMMON_GRASS_MAPSET,
                            __mapset);
                else
                    javaEditor.projectOptions().setOption(ProjectOptions.COMMON_GRASS_MAPSET,
                            preferences.getString(PreferencesInitializer.CONSOLE_ARGV_MAPSET));

                RuntimePreferencesComposite.this.getShell().dispose();
            }
        });
    }

    /**
     * This method initializes grassPreferencesGroup
     */
    private void createGrassPreferencesGroup() {
        GridData gridData3 = new GridData();
        gridData3.horizontalAlignment = GridData.FILL;
        gridData3.grabExcessHorizontalSpace = true;
        gridData3.verticalAlignment = GridData.CENTER;
        GridData gridData2 = new GridData();
        gridData2.horizontalAlignment = GridData.FILL;
        gridData2.grabExcessHorizontalSpace = true;
        gridData2.verticalAlignment = GridData.CENTER;
        GridData gridData11 = new GridData();
        gridData11.horizontalAlignment = GridData.FILL;
        gridData11.grabExcessHorizontalSpace = true;
        gridData11.verticalAlignment = GridData.CENTER;
        GridLayout gridLayout1 = new GridLayout();
        gridLayout1.numColumns = 2;
        gridLayout1.marginHeight = 10;
        gridLayout1.marginWidth = 10;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.FILL;
        gridData.widthHint = 400;
        grassPreferencesGroup = new Group(this, SWT.NONE);
        grassPreferencesGroup.setLayoutData(gridData);
        grassPreferencesGroup.setLayout(gridLayout1);
        grassPreferencesGroup.setText("GRASS workspace info");
        // DATABASE, LOCATION, MAPSET
        grassmapsetLabel = new Label(grassPreferencesGroup, SWT.NONE);
        grassmapsetLabel.setText("Mapset");
        grassmapsetText = new Text(grassPreferencesGroup, SWT.BORDER);
        grassmapsetText.setLayoutData(gridData3);
        grassmapsetText.setText(preferences.getString(PreferencesInitializer.CONSOLE_ARGV_MAPSET));
    }

    /**
     * This method initializes timeGroup
     */
    private void createTimeGroup() {
        GridLayout gridLayout2 = new GridLayout();
        gridLayout2.marginWidth = 10;
        gridLayout2.numColumns = 2;
        gridLayout2.marginHeight = 10;
        timeGroup = new Group(this, SWT.NONE);
        timeGroup.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL));
        timeGroup.setLayout(gridLayout2);
        createStartTimeComposite();
        timeGroup.setText("Simulation time information");
        createEndTimeComposite();
        timestepLabel = new Label(timeGroup, SWT.NONE);
        timestepLabel.setText("timestep in minutes");
        timestepLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        timestepText = new Text(timeGroup, SWT.BORDER);
        timestepText
                .setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        final Long __delta = (Long) javaEditor.projectOptions().getOption(
                ProjectOptions.JAVA_MODEL_TIME_DELTA,
                (long) (RuntimePreferencesComposite.timeDeltaDefault ));
        timestepText.setText(Long.toString(__delta / RuntimePreferencesComposite.timeDelta));
        enableTime(useTimeCheck.getSelection());
    }

    /**
     * This method initializes startTimeComposite
     */
    private void createStartTimeComposite() {
        GridData gridData5 = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        gridData5.horizontalSpan = 2;
        startTimeComposite = new Composite(timeGroup, SWT.NONE);
        startTimeComposite.setLayout(new GridLayout());
        startTimeComposite.setLayoutData(gridData5);

        startCdt = new CalendarTimeGroup(startTimeComposite, SWT.None, SWT.DATE, SWT.TIME
                | SWT.SHORT, "Start date/time", 4, 1);
        try {

            startCdt.setDate(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
                    Locale.getDefault()).parse(
                    (String) javaEditor.projectOptions().getOption(
                            ProjectOptions.JAVA_MODEL_TIME_START_UP,
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
                                    Locale.getDefault()).format(
                                    new Date(System.currentTimeMillis())))));
        } catch (ParseException e) {

            startCdt.setDate(new Date(System.currentTimeMillis()));
        }
    }

    /**
     * This method initializes endTimeComposite
     */
    private void createEndTimeComposite() {
        GridData gridData6 = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        gridData6.horizontalSpan = 2;
        endTimeComposite = new Composite(timeGroup, SWT.NONE);
        endTimeComposite.setLayout(new GridLayout());
        endTimeComposite.setLayoutData(gridData6);
        endCdt = new CalendarTimeGroup(endTimeComposite, SWT.None, SWT.DATE, SWT.TIME | SWT.SHORT,
                "End date/time", 4, 1);
        try {

            endCdt.setDate(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
                    Locale.getDefault()).parse(
                    (String) javaEditor.projectOptions().getOption(
                            ProjectOptions.JAVA_MODEL_TIME_ENDING_UP,
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
                                    Locale.getDefault()).format(
                                    new Date(System.currentTimeMillis())))));
        } catch (ParseException e) {

            endCdt.setDate(new Date(System.currentTimeMillis()));
        }
    }

    private void enableTime( boolean enable ) {
        timeGroup.setEnabled(enable);
        startCdt.setEnabled(enable);
        endCdt.setEnabled(enable);
        timestepText.setEnabled(enable);
        timestepLabel.setEnabled(enable);
    }

} // @jve:decl-index=0:visual-constraint="10,10"
