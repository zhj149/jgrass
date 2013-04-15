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
package eu.hydrologis.jgrass.uibuilder.jgrassdependent;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.progress.IProgressService;
import org.w3c.dom.NodeList;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;
import eu.hydrologis.jgrass.ui.console.ConsoleCommandExecutor;
import eu.hydrologis.jgrass.uibuilder.UIBuilder;
import eu.hydrologis.jgrass.uibuilder.UIBuilderPlugin;
import eu.hydrologis.jgrass.uibuilder.fields.MissingValueException;
import eu.hydrologis.jgrass.uibuilder.renderers.SWTRendererFactory;
import eu.hydrologis.jgrass.uibuilder.swt.SWTSpreadsheetLayout;

/**
 * A class that builds a dialog with the contents given by the description in the nodelist.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GuiBuilderDialog extends Dialog {
    private static final int HELP_ID = -9999;
    private UIBuilder guiBuilder;
    private Properties properties;
    private final NodeList commandNodeList;
    private final Shell parent;
    private final boolean isJGrass;
    private String commandName;
    private String helpCommand;
    private PrintStream out;
    private Object ret;
    private final boolean isTimeDependent;
    private DateTime startDate;
    private DateTime startTime;
    private DateTime endDate;
    private DateTime endTime;
    private Text deltaText;

    /**
     * Instantiate a new <code>SWTDialog</code>.
     * 
     * @param parent the parent of this <code>Dialog</code>
     * @param xmlFile the path to the XML file containing fields' definitions.
     * @param properties a <code>Properties</code> object used to pass values. This object will
     *        hold the output of the gui, but also be capable of adding value to the dialog, as for
     *        example define a title (key = {@link UIBuilderJGrassConstants#DIALOG_TITLE}) or a
     *        size (key = {@link UIBuilderJGrassConstants#DIALOG_SIZE})
     * @param isJGrass 
     */
    public GuiBuilderDialog( Shell parent, NodeList commandNodeList, Properties properties, boolean isJGrass,
            boolean isTimeDependent ) {
        super(parent);
        this.parent = parent;
        this.commandNodeList = commandNodeList;

        /*
         *  TODO this is fine only until we have GRASS and JGrass. 
         *  To be solved when new things come in (ex. R).
         */
        this.isJGrass = isJGrass;
        this.isTimeDependent = isTimeDependent;
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        this.properties = properties;
    }

    protected void configureShell( Shell newShell ) {
        commandName = properties.getProperty(UIBuilderJGrassConstants.DIALOG_TITLE);
        if (commandName != null) {
            newShell.setText(commandName);
        }

        // set the position of the shell
        Point cursorLocation = parent.getDisplay().getCursorLocation();
        cursorLocation.x = cursorLocation.x - newShell.getSize().x / 2;
        newShell.setLocation(cursorLocation);

        super.configureShell(newShell);
    }

    protected Control createDialogArea( Composite parent ) {
        Composite composite = (Composite) super.createDialogArea(parent);
        Object sizeObj = properties.get(UIBuilderJGrassConstants.DIALOG_SIZE);
        GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        // set a width dimension if given
        if (sizeObj != null && sizeObj instanceof Point) {
            Point size = (Point) sizeObj;
            gd.widthHint = size.x;
        }
        composite.setLayoutData(gd);
        try {
            composite.setLayout(new SWTSpreadsheetLayout("B=D"));
            int childOffset = 0;
            // add the time widgets, if needed
            if (isTimeDependent) {
                childOffset = 3;
                Label startDateLabel = new Label(composite, SWT.NONE);
                startDateLabel.setLayoutData("A0");
                startDateLabel.setText("Start time");
                startDate = new DateTime(composite, SWT.DATE | SWT.MEDIUM);
                startDate.setLayoutData("B0");
                startTime = new DateTime(composite, SWT.TIME | SWT.SHORT);
                startTime.setLayoutData("C0");
                Label endDateLabel = new Label(composite, SWT.NONE);
                endDateLabel.setLayoutData("A1");
                endDateLabel.setText("End time");
                endDate = new DateTime(composite, SWT.DATE | SWT.MEDIUM);
                endDate.setLayoutData("B1");
                endTime = new DateTime(composite, SWT.TIME | SWT.SHORT);
                endTime.setLayoutData("C1");
                Label deltaLabel = new Label(composite, SWT.NONE);
                deltaLabel.setLayoutData("A2");
                deltaLabel.setText("Timestep");
                deltaText = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
                deltaText.setLayoutData("B2");
                deltaText.setText("30");
                deltaText.addKeyListener(new KeyAdapter(){
                    public void keyReleased( KeyEvent e ) {
                        String text = deltaText.getText();
                        try {
                            Double.parseDouble(text);
                        } catch (Exception ex) {
                            // if the text is not a positive number, reset
                            deltaText.setText("");
                        }
                    }
                });
            }

            this.guiBuilder = new UIBuilder(commandNodeList, composite, new SWTRendererFactory(), childOffset);
        } catch (Exception e) {
            UIBuilderPlugin
                    .log("UIBuilderPlugin problem: eu.hydrologis.jgrass.uibuilder.jgrassdependent#GuiBuilderDialog#createDialogArea", e); //$NON-NLS-1$
            e.printStackTrace();
        }

        return composite;
    }

    protected void createButtonsForButtonBar( Composite parent ) {

        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        /*
         * add the help button
         */
        ((GridLayout) parent.getLayout()).numColumns++;
        Button helpButton = new Button(parent, SWT.PUSH);
        Image helpImg = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
        helpButton.setImage(helpImg);
        helpButton.setFont(JFaceResources.getDialogFont());
        helpButton.setData(new Integer(HELP_ID));
        helpButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent event ) {
                buttonPressed(((Integer) event.widget.getData()).intValue());
            }
        });
    }

    protected void buttonPressed( int buttonId ) {
        if (buttonId == OK) {
            String commandLineRepr;
            try {
                commandLineRepr = this.guiBuilder.getCommandLineRepresentation();
                this.properties.put(UIBuilder.COMMANDLINEPROPERTY, commandLineRepr);
                if (isTimeDependent) {
                    String tmp = startDate.getYear() + "-";
                    int month = startDate.getMonth() + 1;
                    tmp = tmp + (month < 10 ? "0" + month : "" + month) + "-";
                    int day = startDate.getDay();
                    tmp = tmp + (day < 10 ? "0" + day : "" + day) + " ";
                    int hour = startTime.getHours();
                    tmp = tmp + (hour < 10 ? "0" + hour : "" + hour) + ":";
                    int minute = startTime.getMinutes();
                    tmp = tmp + (minute < 10 ? "0" + minute : "" + minute);
                    this.properties.put(UIBuilder.STARTDATEPROPERTY, tmp);

                    tmp = endDate.getYear() + "-";
                    month = endDate.getMonth() + 1;
                    tmp = tmp + (month < 10 ? "0" + month : "" + month) + "-";
                    day = endDate.getDay();
                    tmp = tmp + (day < 10 ? "0" + day : "" + day) + " ";
                    hour = endTime.getHours();
                    tmp = tmp + (hour < 10 ? "0" + hour : "" + hour) + ":";
                    minute = endTime.getMinutes();
                    tmp = tmp + (minute < 10 ? "0" + minute : "" + minute);
                    this.properties.put(UIBuilder.ENDDATEPROPERTY, tmp);

                    String timeStepString = deltaText.getText();
                    this.properties.put(UIBuilder.TIMESTEPPROPERTY, timeStepString);

                }
                close();
            } catch (MissingValueException e) {
                MessageBox m = new MessageBox(getShell(), SWT.OK);
                m.setMessage(e.getMessage());
                m.open();
            }
        } else if (buttonId == HELP_ID) {
            launchHelp();
        } else {
            close();
        }
    }

    private void launchHelp() {
        helpCommand = null;

        try {
            String basePackage = "eu.hydrologis.jgrass.models";
            String[] split = commandName.split("\\.");
            String className = basePackage;
            for( String string : split ) {
                className = className + "." + string;
            }
            className = className + "." + commandName.replaceAll("\\.", "_");

            Class< ? > classForName = Class.forName(className);
            Method method = classForName.getMethod("getModelDescription", null);
            Object newInstance = classForName.newInstance();
            String helpString = (String) method.invoke(newInstance, null);

            openHelpResult(helpString);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            if(true)return;
            // ignore it and do it the usual way
        }

        StringBuilder cmdBuilder = new StringBuilder();
        cmdBuilder.append(" ");
        if (Platform.getOS().equals(Platform.OS_WIN32) && !isJGrass) {
            commandName = commandName + ".exe";
        }
        cmdBuilder.append(commandName);
        cmdBuilder.append(" --help");
        ScopedPreferenceStore m_preferences = (ScopedPreferenceStore) ConsoleEditorPlugin.getDefault().getPreferenceStore();
        final String mapset = m_preferences.getString(PreferencesInitializer.CONSOLE_ARGV_MAPSET);
        final String gisbase = m_preferences.getString(PreferencesInitializer.CONSOLE_ARGV_GISBASE);
        if (isJGrass) {
            helpCommand = UIBuilderJGrassConstants.JGRASS_MODEL_ENDSPACE + cmdBuilder.toString();
        } else {
            if (gisbase == null || !(new File(gisbase).exists())) {
                MessageBox msgBox = new MessageBox(this.getShell(), SWT.ICON_ERROR);
                msgBox.setMessage("No gisbase has been defined or doesn't exist. Please check your preferences");
                msgBox.open();
                return;
            }
            helpCommand = UIBuilderJGrassConstants.GRASS_MODEL_ENDSPACE + cmdBuilder.toString();
        }

        IWorkbench wb = PlatformUI.getWorkbench();
        IProgressService ps = wb.getProgressService();
        try {
            ps.busyCursorWhile(new IRunnableWithProgress(){
                public void run( IProgressMonitor pm ) {
                    pm.beginTask(commandName, IProgressMonitor.UNKNOWN);
                    ConsoleCommandExecutor c = new ConsoleCommandExecutor();

                    final StringBuilder sB = new StringBuilder();
                    OutputStream outS = new OutputStream(){
                        public void write( final int b ) throws IOException {
                            sB.append(String.valueOf((char) b));
                        }
                    };
                    out = new PrintStream(outS);
                    ret = c.execute(commandName, helpCommand, mapset, gisbase, ConsoleCommandExecutor.OUTPUTTYPE_SUPPLIED, out,
                            out);
                    int waitI = 0;
                    while( ret == null && waitI++ < 50 ) {
                        try {
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

                    openHelpResult(sB.toString());
                }
            });
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    private void openHelpResult( final String helpString ) {
        Display.getDefault().asyncExec(new Runnable(){

            public void run() {
                Dialog helpDialog = new Dialog(getShell()){

                    protected void configureShell( Shell newShell ) {
                        commandName = properties.getProperty(UIBuilderJGrassConstants.DIALOG_TITLE);
                        if (commandName != null) {
                            newShell.setText("Help for: " + commandName);
                            newShell.setSize(600, 400);
                        }

                        // set the position of the shell
                        Point cursorLocation = parent.getDisplay().getCursorLocation();
                        cursorLocation.x = cursorLocation.x - newShell.getSize().x / 2;
                        newShell.setLocation(cursorLocation);

                        super.configureShell(newShell);
                    }

                    protected Control createDialogArea( Composite parent ) {
                        Composite composite = (Composite) super.createDialogArea(parent);
                        final Text text = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL
                                | SWT.V_SCROLL);
                        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                        if (helpString.contains("Total run time")) {
                            String[] split1 = helpString.split("Running model... -------------------------------");
                            String[] split2 = split1[1].split("Total run time");
                            text.setText(split2[0].trim());
                        }else{
                            text.setText(helpString);
                        }
                        return composite;
                    }
                };

                helpDialog.open();
            }
        });
    }
}
