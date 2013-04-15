//package eu.hydrologis.jgrass.netcdf.operations;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.List;
//
//import net.refractions.udig.catalog.IGeoResource;
//import net.refractions.udig.project.ILayer;
//import net.refractions.udig.project.internal.render.ViewportModel;
//import net.refractions.udig.project.render.IViewportModel;
//import net.refractions.udig.project.ui.ApplicationGIS;
//import net.refractions.udig.ui.operations.IOp;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.jface.dialogs.IDialogConstants;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Combo;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.ui.PlatformUI;
//import org.joda.time.DateTime;
//
//import eu.hydrologis.jgrass.netcdf.service.NetcdfMapGeoResource;
//
//public class ChangeTimeElevation implements IOp {
//
//    private List<DateTime> timevalues;
//    private double[] zvalues;
//
//    public void op( final Display display, Object target, IProgressMonitor monitor )
//            throws Exception {
//
//        if (target instanceof ILayer) {
//            ILayer layer = (ILayer) target;
//            IGeoResource geoResource = layer.getGeoResource();
//            if (!geoResource.canResolve(NetcdfMapGeoResource.class)) {
//                Display.getDefault().syncExec(new Runnable(){
//                    public void run() {
//                        MessageDialog.openInformation(display.getActiveShell(), "Wrong layer type",
//                                "The selected layer is not a Netcdf layer.");
//                    }
//                });
//            } else {
//
//                NetcdfMapGeoResource netcdfResource = geoResource.resolve(
//                        NetcdfMapGeoResource.class, monitor);
//
//                timevalues = netcdfResource.getAvailableTimeSteps();
//                zvalues = netcdfResource.getAvailableElevationLevels();
//
//                Display.getDefault().syncExec(new Runnable(){
//                    public void run() {
//                        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
//                        SelectTimeElevationDialog dialog = new SelectTimeElevationDialog(shell,
//                                timevalues, zvalues);
//                        dialog.setBlockOnOpen(true);
//                        int res = dialog.open();
//                        if (res == IDialogConstants.OK_ID) {
//                            DateTime chosenTime = dialog.getChosenTime();
//                            Double chosenElevation = dialog.getChosenElevation();
//
//                            IViewportModel viewportModel = ApplicationGIS.getActiveMap()
//                                    .getViewportModel();
//                            if (viewportModel instanceof ViewportModel) {
//                                ViewportModel vpm = (ViewportModel) viewportModel;
//                                if (chosenTime != null) {
//                                    vpm.setCurrentTimestep(chosenTime);
//                                }
//                                if (chosenElevation != null) {
//                                    vpm.setCurrentElevation(chosenElevation);
//                                }
//                            }
//                        }
//
//                    }
//                });
//            }
//        }
//
//    }
//    class SelectTimeElevationDialog extends Dialog implements SelectionListener {
//
//        private final List<DateTime> timeList;
//        private final double[] elevationArray;
//
//        private DateTime chosenTime;
//        private Double chosenElevation;
//        private Combo elevationCombo;
//        private Combo timeCombo;
//
//        protected SelectTimeElevationDialog( Shell parentShell, List<DateTime> timeList,
//                double[] elevationArray ) {
//            super(parentShell);
//            this.timeList = timeList;
//            this.elevationArray = elevationArray;
//        }
//
//        protected Control createContents( Composite parent ) {
//            getShell().setText("Set new property");
//
//            Composite container = new Composite(parent, SWT.NONE);
//            container.setLayoutData(new GridData(GridData.FILL_BOTH));
//            container.setLayout(new GridLayout(2, false));
//
//            if (timeList != null) {
//                Label timeLabel = new Label(container, SWT.NONE);
//                timeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
//                timeLabel.setText("Select a timestep");
//                timeCombo = new Combo(container, SWT.DROP_DOWN);
//                timeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//                String[] timeArray = new String[timeList.size()];
//                for( int i = 0; i < timeArray.length; i++ ) {
//                    DateTime date = timeList.get(i);
//                    timeArray[i] = NetcdfReader.dateTimeToISO8601(date);
//                }
//                timeCombo.setItems(timeArray);
//                timeCombo.addSelectionListener(this);
//            }
//            if (elevationArray != null) {
//                Label elevationLabel = new Label(container, SWT.NONE);
//                elevationLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
//                elevationLabel.setText("Select a vertical value");
//                elevationCombo = new Combo(container, SWT.DROP_DOWN);
//                elevationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//                String[] elevationArrayStr = new String[elevationArray.length];
//                for( int i = 0; i < elevationArrayStr.length; i++ ) {
//                    elevationArrayStr[i] = String.valueOf(elevationArray[i]);
//                }
//                elevationCombo.setItems(elevationArrayStr);
//                elevationCombo.addSelectionListener(this);
//            }
//
//            Composite buttonComposite = new Composite(container, SWT.NONE);
//            GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
//            gd.horizontalSpan = 2;
//            buttonComposite.setLayoutData(gd);
//            buttonComposite.setLayout(new GridLayout(1, true));
//
//            createButtonsForButtonBar(buttonComposite);
//
//            return container;
//        }
//
//        public void widgetDefaultSelected( SelectionEvent e ) {
//        }
//
//        public void widgetSelected( SelectionEvent e ) {
//
//            Object source = e.getSource();
//            if (source.equals(timeCombo)) {
//                int selectionIndex = timeCombo.getSelectionIndex();
//                chosenTime = NetcdfReader.iso8601ToDateTime(timeCombo.getItem(selectionIndex));
//            }
//
//            if (source.equals(elevationCombo)) {
//                int selectionIndex = elevationCombo.getSelectionIndex();
//                chosenElevation = Double.parseDouble(elevationCombo.getItem(selectionIndex));
//            }
//
//        }
//        public DateTime getChosenTime() {
//            return chosenTime;
//        }
//        public Double getChosenElevation() {
//            return chosenElevation;
//        }
//    }
//}
