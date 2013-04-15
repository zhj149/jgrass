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
package eu.hydrologis.jgrass.console.editor.preferences;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into
 * JFace that allows us to create a page that is small and knows how to save, restore and apply
 * itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 */

public class GeneralConsolePreferences extends FieldEditorPreferencePage
        implements
            IWorkbenchPreferencePage {

    // Attributes
    /** */
    private ScopedPreferenceStore m_preferences;

    /** */
    private BooleanFieldEditor m_asyncMode;

    /** */
    private Composite m_mainComposite;

    /** */
    private Composite m_threadComposite;

    /** */
    private BooleanFieldEditor m_threadRestriction;

    /** */
    private IntegerFieldEditor m_threadMaxCnt;

    // Construction
    /** */
    public GeneralConsolePreferences() {

        super(GRID);
        try {

            m_preferences = new ScopedPreferenceStore(new ConfigurationScope(),
                    ConsoleEditorPlugin.PLUGIN_ID);
            setPreferenceStore(m_preferences);
            setDescription("General settings for the ConsoleEngine Command Line Interpreter "
                    + "of the Java based " + "\"Geographic Resources Analysis Support System\"."
                    + "\n");
        } finally {

            m_asyncMode = null;
            m_threadRestriction = null;
            m_threadMaxCnt = null;
        }
    } // GeneralConsolePreferences

    /** */
    private void createLoggingGroupEditors( Composite composite ) {

        GridLayout __layoutGroup = new GridLayout();
        __layoutGroup.numColumns = 4;
        GridData __dataGroup = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        __dataGroup.horizontalIndent = 1;
        __dataGroup.verticalIndent = 1;
        Group __compositeGroup;
        __compositeGroup = new Group(composite, SWT.VERTICAL | SWT.HORIZONTAL);
        __compositeGroup.setLayout(__layoutGroup);
        __compositeGroup.setLayoutData(__dataGroup);

        __compositeGroup.setText("Logging level");
        addField(new BooleanFieldEditor(PreferencesInitializer.CONSOLE_LOGGING_LEVEL_DEBUG,
                "Activate &debug logging level", __compositeGroup));
        addField(new BooleanFieldEditor(PreferencesInitializer.CONSOLE_LOGGING_LEVEL_TRACE,
                "Activate &trace logging level", __compositeGroup));
    } // createLoggingGroupEditors

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */

    protected void createFieldEditors() {

        Composite composite = new Composite(getFieldEditorParent(), SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));

        addField(m_asyncMode = new BooleanFieldEditor(PreferencesInitializer.CONSOLE_ASYNC_MODE,
                "&Allways run in background", composite));
        addField(m_threadRestriction = new BooleanFieldEditor(
                PreferencesInitializer.CONSOLE_THREAD_RESTRICTION, "Activate thread &restriction",
                composite));

        GridLayout __layoutGroup = new GridLayout();
        __layoutGroup.numColumns = 1;
        GridData __dataGroup = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        __dataGroup.horizontalIndent = 1;
        __dataGroup.verticalIndent = 1;

        Group __swtThreadGroup;
        __swtThreadGroup = new Group(composite, SWT.VERTICAL | SWT.HORIZONTAL);
        __swtThreadGroup.setText("Thread restriction");
        __swtThreadGroup.setLayout(__layoutGroup);
        __swtThreadGroup.setLayoutData(__dataGroup);
        addField(m_threadMaxCnt = new IntegerFieldEditor(
                PreferencesInitializer.CONSOLE_THREAD_MAX_COUNT,
                "&Maximum count of simultaneously running threads is &restricted to",
                __swtThreadGroup));

        createLoggingGroupEditors(composite);

        m_mainComposite = composite;
        m_threadComposite = __swtThreadGroup;

    } // createFieldEditors

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
     */

    protected void initialize() {

        super.initialize();
        if (null != m_asyncMode) {

            if (null != m_threadRestriction)
                m_threadRestriction.setEnabled(m_asyncMode.getBooleanValue(), m_mainComposite);
            if (null != m_threadMaxCnt)
                m_threadMaxCnt.setEnabled(m_asyncMode.getBooleanValue()
                        && m_threadRestriction.getBooleanValue(), m_threadComposite);
        }
    } // initialize

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init( IWorkbench workbench ) {
    } // init

    // Operations
    /** */

    public boolean performOk() {

        try {

            m_preferences.save();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return super.performOk();
    } // performOk

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(
     *      PropertyChangeEvent event )
     */

    public void propertyChange( PropertyChangeEvent event ) {

        super.propertyChange(event);
        Object source = event.getSource();
        if (true == (source instanceof BooleanFieldEditor)) {

            BooleanFieldEditor preferenceEditor = (BooleanFieldEditor) source;
            String property = preferenceEditor.getPreferenceName();
            if (0 == property.compareTo(PreferencesInitializer.CONSOLE_ASYNC_MODE)) {

                boolean active = ((Boolean) event.getNewValue()).booleanValue();
                m_threadRestriction.setEnabled(active, m_mainComposite);
                m_threadMaxCnt.setEnabled(active && m_threadRestriction.getBooleanValue(),
                        m_threadComposite);
            } else if (0 == property.compareTo(PreferencesInitializer.CONSOLE_THREAD_RESTRICTION)) {

                m_threadMaxCnt.setEnabled(((Boolean) event.getNewValue()).booleanValue(),
                        m_threadComposite);
            }
        }
    } // propertyChange

} // GeneralConsolePreferences
