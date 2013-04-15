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

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class SetRuntimePreferencesClass extends TextEditorAction {

    public SetRuntimePreferencesClass( ResourceBundle bundle, String prefix, ITextEditor editor ) {
        super(bundle, prefix, editor);
    }

    /*
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {

        Display.getDefault().asyncExec(new Runnable(){

            public void run() {
                try {
                    Display display = Display.getDefault();
                    final Shell shell = new Shell(display);
                    shell.setText("Simulation Runtime Preferences");
                    shell.setLayout(new GridLayout());

                    GridLayout layout = new GridLayout();
                    shell.setLayout(layout);

                    RuntimePreferencesComposite rPc = new RuntimePreferencesComposite(shell,
                            SWT.None, getTextEditor());
                    rPc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

                    shell.pack();
                    Point size = shell.getSize();
                    Rectangle screen = display.getMonitors()[0].getBounds();
                    shell.setBounds((screen.width - size.x) / 2, (screen.height - size.y) / 2,
                            size.x, size.y);
                    shell.open();
                    while( !shell.isDisposed() ) {
                        if (!display.readAndDispatch())
                            display.sleep();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });

    }
}
