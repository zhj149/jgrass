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

import java.io.File;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.examples.javaeditor.JavaEditor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;

/**
 * A workaround for the open action of eclipse which doesn't seem to want to
 * work
 * 
 * @author Andrea Antonello - www.hydrologis.com
 * 
 */
public class OpenFileClass extends TextEditorAction {

	private ITextEditor editor;

	public OpenFileClass(ResourceBundle bundle, String prefix,
			ITextEditor editor) {
		super(bundle, prefix, editor);

		this.setText("Open");
		this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				ConsoleEditorPlugin.PLUGIN_ID, "icons/open.gif"));

		this.editor = editor;
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		try {
			Shell shell = PlatformUI.getWorkbench().getDisplay()
					.getActiveShell();
			FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
			String path = fileDialog.open();

			if (path == null || path.length() < 1) {
				return;
			}
			/*
			 * close the editor
			 */
			// PlatformUI
			// .getWorkbench()
			// .getActiveWorkbenchWindow()
			// .getActivePage()
			// .closeEditor(
			// PlatformUI.getWorkbench()
			// .getActiveWorkbenchWindow().getActivePage()
			// .findEditor(editor.getEditorInput()), false);
			/*
			 * and open it on the new file
			 */
			File f = new File(path);
			JavaFileEditorInput jFile = new JavaFileEditorInput(f);
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().openEditor(jFile, JavaEditor.ID);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
