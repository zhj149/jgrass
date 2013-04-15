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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.examples.javaeditor.JavaEditor;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;

/**
 * Action to open an editor
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class ConsoleEditorActionNew implements IWorkbenchWindowActionDelegate {

    private String welcome = "/*\n *  Welcome to the JGrass ConsoleEngine Editor\n *  \n *  The editor supports the groovy scripting language\n *  as well as the JGrass Modeling language based on \n *  OpenMI standards merged with the OGC feature model\n *  supplied by UDig.\n *\n *  To execute a script right click in the editor area\n *  and first set the runtime preferences, some of which\n *  are configurable in the global preferences. \n *  After that you can execute the script.\n */\n";
    private StringBuffer options;

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        if (options == null) {

            ScopedPreferenceStore preferences = (ScopedPreferenceStore) ConsoleEditorPlugin
                    .getDefault().getPreferenceStore();

            options = new StringBuffer();
            options.append("//Please uncomment and define the following options if needed:\n");

            String mapsetString = preferences.getString(PreferencesInitializer.CONSOLE_ARGV_MAPSET);
            if (mapsetString != null) {
                options.append("//# MAPSET= ").append(mapsetString);
            } else {
                options
                        .append("//# MAPSET= /home/moovida/data/bolzano/utm_newage/newage (example)");
            }
            options.append("\n");
            String gisbaseString = preferences
                    .getString(PreferencesInitializer.CONSOLE_ARGV_GISBASE);
            if (gisbaseString != null) {
                options.append("//# GISBASE= ").append(gisbaseString);
            } else {
                options.append("//# GISBASE= /usr/local/grass-6.3.0RC5/");
            }
            options.append("\n");
            options.append("//# STARTDATE = 2007-08-03 06:00");
            options.append("\n");
            options.append("//# ENDDATE = 2007-08-06 12:00");
            options.append("\n");
            options.append("//# DELTAT= 30");
            options.append("\n");
            options.append("//# RT= usually not needed");
            options.append("\n");
            options.append("//# DEBUG= false");
            options.append("\n");
            options.append("//# TRACE= false");
            options.append("\n");
            options.append("//# NATIVEDEBUG= false");
            options.append("\n");
            welcome = welcome + options.toString();
        }
    }

    public void run( IAction action ) {

        Display.getDefault().asyncExec(new Runnable(){

            public void run() {

                try {

                    File f = new File(JavaFileEditorInput.UNTITLEDFILE);
                    if (!f.exists()) {
                        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                        bw.write(welcome);
                        bw.flush();
                        bw.close();
                    }

                    JavaFileEditorInput jFile = new JavaFileEditorInput(f);
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .openEditor(jFile, JavaEditor.ID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
