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
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
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
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class CompilerPreferences
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

// Attributes
	/** */
	private ScopedPreferenceStore m_preferences;
	
// Construction
	public CompilerPreferences() {

		super( GRID );
		m_preferences = new ScopedPreferenceStore(
				new ConfigurationScope()
				, ConsoleEditorPlugin.PLUGIN_ID
			);
		setPreferenceStore( m_preferences );
		setDescription(
				"General \"ML Compiler\" settings for JGrass\"."
				+ "\n"
			);
	} // CompilerPreferences

	/** */
	private void createRttiGroupEditors( Composite composite ) {
		
		GridLayout __layoutGroup = new GridLayout();
			__layoutGroup.numColumns = 1;
		GridData __dataGroup = new GridData(
				GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL
			);
			__dataGroup.horizontalIndent = 1;
			__dataGroup.verticalIndent = 1;
		Group __compositeGroup;
			__compositeGroup = new Group(
					composite
					, SWT.VERTICAL|SWT.HORIZONTAL
				);
			__compositeGroup.setLayout( __layoutGroup );
			__compositeGroup.setLayoutData( __dataGroup );
		
		__compositeGroup.setText( "Path to runtime information files" );
		addField(
				new DirectoryFieldEditor(
						PreferencesInitializer.CONSOLE_DIRECTORY_INCLUDE
						, "&Runtime informations:"
						, __compositeGroup
					)
			);
	} // createRttiGroupEditors
	
	/** */
	private void createSrcfileGroupEditors( Composite composite ) {
		
		GridLayout __layoutGroup = new GridLayout();
			__layoutGroup.numColumns = 1;
		GridData __dataGroup = new GridData(
				GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL
			);
			__dataGroup.horizontalIndent = 1;
			__dataGroup.verticalIndent = 1;
		Group __compositeGroup;
			__compositeGroup = new Group(
					composite
					, SWT.VERTICAL|SWT.HORIZONTAL
				);
			__compositeGroup.setLayout( __layoutGroup );
			__compositeGroup.setLayoutData( __dataGroup );
		
		__compositeGroup.setText( "Default script source file directoy" );
		addField(
				new DirectoryFieldEditor(
						PreferencesInitializer.CONSOLE_DIRECTORY_SOURCE
						, "&Source:"
						, __compositeGroup
					)
			);
	} // createSrcfileGroupEditors
	
	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	
    protected void createFieldEditors() {

		Composite composite = new Composite( getFieldEditorParent(), SWT.NONE );
			composite.setLayout( new GridLayout() );
			composite.setLayoutData(
					new GridData(
							GridData.FILL_VERTICAL|GridData.FILL_HORIZONTAL
							|GridData.GRAB_HORIZONTAL
						)
				);
		
		createSrcfileGroupEditors( composite );
		createRttiGroupEditors( composite );
	} // createFieldEditors

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init( IWorkbench workbench ) {
	} // init

// Operations
	/** */
	
	public boolean performOk() {
		
		try {
	    	
	    	m_preferences.save();
	    }
		catch (IOException e) {
	    	
	        e.printStackTrace();
	    }
	    
	    return super.performOk();
	} // performOk
	
} // CompilerPreferences