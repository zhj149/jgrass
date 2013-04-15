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
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;

public class SyntaxColoringPreferences
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

// Attributes
	/** */
	private ScopedPreferenceStore m_preferences;
	
// Construction
	public SyntaxColoringPreferences() {

		super( GRID );
		
		m_preferences = new ScopedPreferenceStore(
				new ConfigurationScope()
				, ConsoleEditorPlugin.PLUGIN_ID
			);
		setPreferenceStore( m_preferences );
		setDescription(
				"General settings for the Java based "
				+ "\"Geographic Resources Analysis Support System\" ConsoleEngine."
				+ "\n"
			);
	} // SyntaxColoringPreferences

// Operations
	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	
    protected void createFieldEditors() {

		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_ML_ASTERISK_KEYWORD
						, "Asterisk Keyword"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_ML_EXCHANGE_KEYWORD
						, "Exchange/Quantity Keyword"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_ML_INPUT_KEYWORD
						, "Input Exchange Keyword"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_ML_OUTPUT_KEYWORD
						, "Output Exchange Keyword"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_ML_MODEL_KEYWORD
						, "Model Keyword"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_COMMENT
						, "Comment (Multi/Single Line)"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_ML_PREPROCESSOR_KEYWORD
						, "Preprocessor Keyword"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_CONSTANT
						, "Constant"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_KEYWORD
						, "Keyword"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_TYPE
						, "Type"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_STRING
						, "String"
						, getFieldEditorParent()
					)
			);
		addField(
				new ColorFieldEditor(
						PreferencesInitializer.IDC_OTHER
						, "Others"
						, getFieldEditorParent()
					)
			);
	} // createFieldEditors

	/** */
	public void init( IWorkbench workbench ) {
	} // init
	
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
	
} // SyntaxColoringPreferences
