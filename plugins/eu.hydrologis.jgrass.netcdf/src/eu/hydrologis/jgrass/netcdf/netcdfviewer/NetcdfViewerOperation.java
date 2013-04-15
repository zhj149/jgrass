package eu.hydrologis.jgrass.netcdf.netcdfviewer;

import java.net.URL;

import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ucar.nc2.dataset.NetcdfDataset;
import eu.hydrologis.jgrass.netcdf.NetcdfPlugin;
import eu.hydrologis.jgrass.netcdf.service.NetcdfService;

public class NetcdfViewerOperation implements IOp {

    public void op( Display display, Object target, IProgressMonitor monitor ) throws Exception {
        if (target instanceof NetcdfService) {
            NetcdfService netcdfService = (NetcdfService) target;
            URL url = netcdfService.getIdentifier();

            final NetcdfDataset netcdfDataset = NetcdfDataset.openDataset(url.toExternalForm());
            NetcdfPlugin.getDefault().setNetcdfDataset(netcdfDataset);

            display.asyncExec(new Runnable(){

                public void run() {
                    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    IWorkbenchPage activePage = window.getActivePage();
                    try {
                        activePage.showView(NetcdfView.ID);
                        activePage.toggleZoom(activePage.findViewReference(NetcdfView.ID));
                    } catch (PartInitException es) {
                        es.printStackTrace();
                        String message = "An error occurred while opening the netcdf viewer.";
                        ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, NetcdfPlugin.PLUGIN_ID, es);
                    }
                }
            });

        }

    }

}
