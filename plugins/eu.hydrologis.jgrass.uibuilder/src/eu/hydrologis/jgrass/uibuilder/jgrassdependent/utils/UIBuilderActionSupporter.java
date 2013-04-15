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
package eu.hydrologis.jgrass.uibuilder.jgrassdependent.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.w3c.dom.NodeList;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;
import eu.hydrologis.jgrass.ui.console.ConsoleCommandExecutor;
import eu.hydrologis.jgrass.uibuilder.UIBuilder;
import eu.hydrologis.jgrass.uibuilder.jgrassdependent.GuiBuilderDialog;
import eu.hydrologis.jgrass.uibuilder.jgrassdependent.UIBuilderJGrassConstants;
import eu.hydrologis.jgrass.uibuilder.jgrassdependent.XmlCreator;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * Support class for GRASS and JGrass actions in creating GUIs
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class UIBuilderActionSupporter {

    protected boolean isGrass = false;
    protected boolean isJGrass = false;
    protected boolean isTimeDependent = false;
    private String command;
    private String gisbase;

    private Thread commandThread = null;
    private String cmd;

    /**
     * Create a gui
     * 
     * @param dialogSize
     *            TODO
     */
    protected void launchGui( IWorkbenchWindow window, Point dialogSize ) {
        try {
            String[] nameSplit = this.getClass().getCanonicalName().split("\\."); //$NON-NLS-1$
            final String className = nameSplit[nameSplit.length - 1];
            String xmlName = className + ".xml"; //$NON-NLS-1$
            InputStream xmlStream = this.getClass().getResourceAsStream(xmlName);
            if (xmlStream == null) {
                MessageDialog.openWarning(window.getShell(), "GUI not implemented",
                        "We are sorry, the gui for this command wasn't implemented yet.");
                return;
            }
            XmlCreator xmlCreator = new XmlCreator(xmlStream);
            Properties properties = new Properties();
            properties.put(UIBuilderJGrassConstants.DIALOG_TITLE, className.replaceAll("_", ".")); //$NON-NLS-1$//$NON-NLS-2$
            if (dialogSize == null) {
                properties.put(UIBuilderJGrassConstants.DIALOG_SIZE, new Point(600, 10));
            } else {
                properties.put(UIBuilderJGrassConstants.DIALOG_SIZE, dialogSize);
            }
            NodeList nodeList = xmlCreator.getNodeList();

            /*
             * check for proper set mapset
             */
            ScopedPreferenceStore m_preferences = (ScopedPreferenceStore) ConsoleEditorPlugin
                    .getDefault().getPreferenceStore();
            final String mapset = m_preferences
                    .getString(PreferencesInitializer.CONSOLE_ARGV_MAPSET);
            if (mapset == null || !(new File(mapset).exists())) {
                MessageBox msgBox = new MessageBox(window.getShell(), SWT.ICON_ERROR);
                msgBox
                        .setMessage("No mapset has been defined or doesn't exist. Please check your preferences");
                msgBox.open();
                return;
            }

            GuiBuilderDialog swtDialog = new GuiBuilderDialog(window.getShell(), nodeList,
                    properties, isJGrass, isTimeDependent);
            swtDialog.setBlockOnOpen(true);
            swtDialog.open();
            cmd = (String) properties.get(UIBuilder.COMMANDLINEPROPERTY);
            if (cmd == null) {
                return;
            }

            String[] cmdSplit = cmd.split("\\s+");
            StringBuffer cmdBuffer = new StringBuffer();
            cmdBuffer.append(cmdSplit[0]);
            if (Platform.getOS().equals(Platform.OS_WIN32) && isGrass) {
                cmdBuffer.append(".exe");
            }
            for( int i = 1; i < cmdSplit.length; i++ ) {
                cmdBuffer.append(" ");
                cmdBuffer.append(cmdSplit[i]);
            }
            cmd = cmdBuffer.toString();

            if (isJGrass) {
                command = UIBuilderJGrassConstants.JGRASS_MODEL_ENDSPACE + cmd;
            } else if (isGrass) {
                gisbase = m_preferences.getString(PreferencesInitializer.CONSOLE_ARGV_GISBASE);
                if (gisbase == null || !(new File(gisbase).exists())) {
                    MessageBox msgBox = new MessageBox(window.getShell(), SWT.ICON_ERROR);
                    msgBox
                            .setMessage("No gisbase has been defined or doesn't exist. Please check your preferences");
                    msgBox.open();
                    return;
                }
                command = UIBuilderJGrassConstants.GRASS_MODEL_ENDSPACE + cmd;
            } else {
                // for now we default to JGrass
                command = UIBuilderJGrassConstants.JGRASS_MODEL_ENDSPACE + cmd;
            }

            if (isTimeDependent) {
                String startDateStr = (String) properties.get(UIBuilder.STARTDATEPROPERTY);
                String endDateStr = (String) properties.get(UIBuilder.ENDDATEPROPERTY);
                String timeStep = (String) properties.get(UIBuilder.TIMESTEPPROPERTY);
                // # STARTDATE = 2007-08-03 06:00
                // # ENDDATE = 2007-08-06 12:00
                // # DELTAT= 30
                StringBuilder sB = new StringBuilder();
                sB.append("# STARTDATE = ");
                sB.append(startDateStr);
                sB.append("\n");
                sB.append("# ENDDATE = ");
                sB.append(endDateStr);
                sB.append("\n");
                sB.append("# DELTAT = ");
                sB.append(timeStep);
                sB.append("\n");
                sB.append(command);
                sB.append("\n");
                command = sB.toString();
            }

            Job job = new Job(cmd){
                private Object ret;

                public IStatus run( IProgressMonitor pm ) {
                    pm.beginTask(cmd, IProgressMonitor.UNKNOWN);
                    ConsoleCommandExecutor c = new ConsoleCommandExecutor();
                    ret = c.execute(className, command, mapset, gisbase,
                            ConsoleCommandExecutor.OUTPUTTYPE_BTCONSOLE, null, null);
                    int waitI = 0;
                    while( ret == null && waitI++ < 50 ) {
                        try {
                            System.out.println(cmd + " wait: " + waitI);
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while( ((Thread) ret).isAlive() ) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    pm.done();
                    JGrassCatalogUtilities.refreshJGrassService(new File(mapset).getParent(),
                            new NullProgressMonitor());

                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.schedule();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String launchGuiGetCommand( IWorkbenchWindow window, Point dialogSize ) {
        try {
            String[] nameSplit = this.getClass().getCanonicalName().split("\\."); //$NON-NLS-1$
            final String className = nameSplit[nameSplit.length - 1];
            String xmlName = className + ".xml"; //$NON-NLS-1$
            InputStream xmlStream = this.getClass().getResourceAsStream(xmlName);
            if (xmlStream == null) {
                MessageDialog.openWarning(window.getShell(), "GUI not implemented",
                        "We are sorry, the gui for this command wasn't implemented yet.");
                return null;
            }
            XmlCreator xmlCreator = new XmlCreator(xmlStream);
            Properties properties = new Properties();
            properties.put(UIBuilderJGrassConstants.DIALOG_TITLE, className.replaceAll("_", ".")); //$NON-NLS-1$//$NON-NLS-2$
            if (dialogSize == null) {
                properties.put(UIBuilderJGrassConstants.DIALOG_SIZE, new Point(600, 10));
            } else {
                properties.put(UIBuilderJGrassConstants.DIALOG_SIZE, dialogSize);
            }
            NodeList nodeList = xmlCreator.getNodeList();

            /*
             * check for proper set mapset
             */

            GuiBuilderDialog swtDialog = new GuiBuilderDialog(window.getShell(), nodeList,
                    properties, isJGrass, isTimeDependent);
            swtDialog.setBlockOnOpen(true);
            swtDialog.open();
            String cmd = (String) properties.get(UIBuilder.COMMANDLINEPROPERTY);
            if (cmd == null) {
                return null;
            }
            if (isJGrass) {
                command = UIBuilderJGrassConstants.JGRASS_MODEL_ENDSPACE + cmd;
            } else if (isGrass) {
                command = UIBuilderJGrassConstants.GRASS_MODEL_ENDSPACE + cmd;
            } else {
                command = cmd;
            }
            if (isTimeDependent) {
                String startDateStr = (String) properties.get(UIBuilder.STARTDATEPROPERTY);
                String endDateStr = (String) properties.get(UIBuilder.ENDDATEPROPERTY);
                String timeStep = (String) properties.get(UIBuilder.TIMESTEPPROPERTY);
                // # STARTDATE = 2007-08-03 06:00
                // # ENDDATE = 2007-08-06 12:00
                // # DELTAT= 30
                StringBuilder sB = new StringBuilder();
                sB.append("# STARTDATE = ");
                sB.append(startDateStr);
                sB.append("\n");
                sB.append("# ENDDATE = ");
                sB.append(endDateStr);
                sB.append("\n");
                sB.append("# DELTAT = ");
                sB.append(timeStep);
                sB.append("\n");
                sB.append(command);
                sB.append("\n");
                command = sB.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return command;
    }
}