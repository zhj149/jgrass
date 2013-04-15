package eu.hydrologis.jgrass.netcdf.export.wizard;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;

import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import eu.hydrologis.jgrass.libs.utils.monitor.EclipseProgressMonitorAdapter;
import eu.hydrologis.jgrass.netcdf.export.core.NcFileWriter;

public class NetcdfExportWizard extends Wizard implements IExportWizard {
    public static final String ID = "eu.hydrologis.jgrass.netcdf.export.wizard.NetcdfExportWizard";

    private GeneralWizardPage generalPage;
    private VariablesWizardPage variablesPage;

    private String outputPath = null;
    private String mapsetPath = null;
    private LinkedHashMap<String, Object> globalAttributesMap = null;
    private String startDateString = null;
    private String endDateString = null;
    private String timestepString = null;
    private String levelsString = null;

    public NetcdfExportWizard() {
    }

    public void addPages() {
        setWindowTitle("Export Grass rasters to Netcdf");

        generalPage = new GeneralWizardPage(this);
        addPage(generalPage);
        variablesPage = new VariablesWizardPage(this);
        addPage(variablesPage);
    }

    public boolean performFinish() {

        final NcFileWriter ncFileWriter = variablesPage.getNcFileWriter();
        if (ncFileWriter == null)
            return false;
        
        if (globalAttributesMap!=null) {
            ncFileWriter.addGlobalAttributes(globalAttributesMap);
        }

        IRunnableWithProgress operation = new IRunnableWithProgress(){

            public void run( IProgressMonitor pm ) throws InvocationTargetException,
                    InterruptedException {
                try {
                    ncFileWriter.writeNcDataset(new EclipseProgressMonitorAdapter(pm));
                } catch (final IOException e) {
                    
                    Display.getDefault().asyncExec(new Runnable(){
                        
                        public void run() {
                            Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                            MessageDialog.openError(shell, "Export error",
                            "An error occurred while exporting to netcdf file.");
                            e.printStackTrace();
                        }
                    });
                }
            }

        };
        PlatformGIS.runInProgressDialog("Dump netcdf file.", true, operation, true);
        return true;
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ) {
    }

    public void setOutputPath( String outputPath ) {
        this.outputPath = outputPath;
    }

    public void setGlobalAttributesMap( LinkedHashMap<String, Object> globalAttributes ) {
        globalAttributesMap = globalAttributes;
    }

    public void setStartDateString( String startDateString ) {
        this.startDateString = startDateString;
    }

    public void setEndDateString( String endDateString ) {
        this.endDateString = endDateString;
    }

    public void setTimestepString( String timestepString ) {
        this.timestepString = timestepString;
    }

    public void setLevelsString( String levelsString ) {
        this.levelsString = levelsString;
    }

    public void setMapsetPath( String mapsetPath ) {
        this.mapsetPath = mapsetPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getMapsetPath() {
        return mapsetPath;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public String getEndDateString() {
        return endDateString;
    }

    public String getTimestepString() {
        return timestepString;
    }

    public String getLevelsString() {
        return levelsString;
    }
}
