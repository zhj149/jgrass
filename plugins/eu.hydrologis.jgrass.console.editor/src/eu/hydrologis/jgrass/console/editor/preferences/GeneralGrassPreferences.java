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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
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

public class GeneralGrassPreferences extends FieldEditorPreferencePage
        implements
            IWorkbenchPreferencePage {

    // Attributes
    /** */
    private ScopedPreferenceStore m_preferences;
    private DirectoryFieldEditor mapsetFolder;
    private DirectoryFieldEditor gisbaseFolder;

    // Construction
    public GeneralGrassPreferences() {

        super(GRID);

        m_preferences = (ScopedPreferenceStore) ConsoleEditorPlugin.getDefault()
                .getPreferenceStore();
        setPreferenceStore(m_preferences);
        setDescription("General settings of the "
                + "\"Geographic Resources Analysis Support System\". " + "\n");
    } // GeneralGrassPreferences

    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
     * manipulate various types of preferences. Each field editor knows how to save and restore
     * itself.
     */
    protected void createFieldEditors() {

        final Composite baseComposite = new Composite(getFieldEditorParent(), SWT.NONE);
        baseComposite.setLayout(new GridLayout());
        baseComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));

        // GRASS DATABASE, LOCATION AND MAPSET SETUP
        GridLayout __layoutGrassGroup = new GridLayout();
        __layoutGrassGroup.numColumns = 1;
        GridData __dataGrassGroup = new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL);
        __dataGrassGroup.horizontalIndent = 1;
        __dataGrassGroup.verticalIndent = 1;
        final Group __groupGrassComposite;
        __groupGrassComposite = new Group(baseComposite, SWT.VERTICAL | SWT.HORIZONTAL);
        __groupGrassComposite.setLayout(__layoutGrassGroup);
        __groupGrassComposite.setLayoutData(__dataGrassGroup);
        __groupGrassComposite
                .setText("GRASS: database, location, mapset and path to the GRASS installation folder");
        mapsetFolder = new DirectoryFieldEditor(PreferencesInitializer.CONSOLE_ARGV_MAPSET,
                "&Mapset:", __groupGrassComposite);
        addField(mapsetFolder);
        gisbaseFolder = new DirectoryFieldEditor(PreferencesInitializer.CONSOLE_ARGV_GISBASE,
                "&GRASS:", __groupGrassComposite);
        addField(gisbaseFolder);

        // USER INFORMATIONS
        GridLayout __layoutUserGroup = new GridLayout();
        __layoutUserGroup.numColumns = 1;
        GridData __dataUserGroup = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        __dataUserGroup.horizontalIndent = 1;
        __dataUserGroup.verticalIndent = 1;
        Group __compositeUserGroup;
        __compositeUserGroup = new Group(baseComposite, SWT.VERTICAL | SWT.HORIZONTAL);
        __compositeUserGroup.setLayout(__layoutUserGroup);
        __compositeUserGroup.setLayoutData(__dataUserGroup);
        __compositeUserGroup.setText("User informations forwarded to a native command");
        addField(new StringFieldEditor(PreferencesInitializer.CONSOLE_ARGV_USER_NAME, "&Name:",
                __compositeUserGroup));
        addField(new DirectoryFieldEditor(PreferencesInitializer.CONSOLE_ARGV_USER_HOME,
                "&Home directory:", __compositeUserGroup));

        // DEBUG MODE SETUP
        GridLayout __layoutDbgModeGroup = new GridLayout();
        __layoutDbgModeGroup.numColumns = 1;
        GridData __dataDbgModeGroup = new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL);
        __dataDbgModeGroup.horizontalIndent = 1;
        __dataDbgModeGroup.verticalIndent = 1;
        Group __compositeDbgModeGroup;
        __compositeDbgModeGroup = new Group(baseComposite, SWT.VERTICAL | SWT.HORIZONTAL);
        __compositeDbgModeGroup.setLayout(__layoutDbgModeGroup);
        __compositeDbgModeGroup.setLayoutData(__dataDbgModeGroup);
        __compositeDbgModeGroup.setText("Debug mode");
        addField(new BooleanFieldEditor(PreferencesInitializer.CONSOLE_ARGV_DEBUG,
                "&Activate debug mode for native command execution", __compositeDbgModeGroup));
    } // createFieldEditors

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init( IWorkbench workbench ) {
    } // init

    protected void performApply() {

        String mapset = mapsetFolder.getStringValue();
        String gisbase = gisbaseFolder.getStringValue();

        ConsoleEditorPlugin.updateNativeGrassXml(mapset, gisbase);

    }

    /** */
    public boolean performOk() {

        String mapset = mapsetFolder.getStringValue();
        String gisbase = gisbaseFolder.getStringValue();

        ConsoleEditorPlugin.updateNativeGrassXml(mapset, gisbase);

        System.out.println(mapset);
        m_preferences.setValue(PreferencesInitializer.CONSOLE_ARGV_MAPSET, mapset);
        m_preferences.setValue(PreferencesInitializer.CONSOLE_ARGV_GISBASE, gisbase);

        try {
            m_preferences.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return super.performOk();
    } // performOk

} // GeneralGrassPreferences
