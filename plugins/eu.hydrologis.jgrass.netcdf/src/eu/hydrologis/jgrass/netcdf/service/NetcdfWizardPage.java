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
package eu.hydrologis.jgrass.netcdf.service;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.URLUtils;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import eu.hydrologis.jgrass.netcdf.NetcdfPlugin;

/**
 * <p>
 * Data page responsible for netcdf datasets
 * </p>
 * <p>
 * <i>Note: based on the WMS plugin</i>
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class NetcdfWizardPage extends WizardPage implements ModifyListener, UDIGConnectionPage {

    private String urlString = ""; //$NON-NLS-1$

    private static final String NETCDF_WIZARD = "NETCDF_WIZARD"; //$NON-NLS-1$

    private static final String NETCDF_RECENT = "NETCDF_RECENT"; //$NON-NLS-1$

    private IDialogSettings settings = null;

    private static final int COMBO_HISTORY_LENGTH = 15;

    private Combo urlCombo = null;

    private List<IService> netcdfServices;

    /**
     * Construct <code>NetcdfWizardPage</code>.
     */
    public NetcdfWizardPage() {
        super("Netcdf dataset import");
        
        urlString = "";

        settings = NetcdfPlugin.getDefault().getDialogSettings().getSection(NETCDF_WIZARD);
        if (settings == null) {
            settings = NetcdfPlugin.getDefault().getDialogSettings().addNewSection(NETCDF_WIZARD);
        }
    }

    public String getId() {
        return "eu.hydrologis.jgrass.netcdf.service.netcdfwizardpage"; //$NON-NLS-1$
    }

    /** Can be called during createControl */
    protected Map<String, Serializable> defaultParams() {
        IStructuredSelection selection = (IStructuredSelection) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getSelectionService().getSelection();
        return toParams(selection);
    }

    /** Retrieve "best" netcdf guess of parameters based on provided context */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    protected Map<String, Serializable> toParams( IStructuredSelection context ) {
        if (context != null) {
            for( Iterator itr = context.iterator(); itr.hasNext(); ) {
                Map<String, Serializable> params = new NetcdfConnectionFactory()
                        .createConnectionParameters(itr.next());
                if (!params.isEmpty())
                    return params;
            }
        }
        return Collections.EMPTY_MAP;
    }

    public void createControl( Composite parent ) {
        urlString = "";
        netcdfServices = null;
        
        String[] recentNetcdf = settings.getArray(NETCDF_RECENT);
        if (recentNetcdf == null) {
            recentNetcdf = new String[0];
        }

        GridData gridData;
        Composite composite = new Composite(parent, SWT.NULL);

        GridLayout gridLayout = new GridLayout();
        int columns = 2;
        gridLayout.numColumns = columns;
        composite.setLayout(gridLayout);

        gridData = new GridData();

        Label urlLabel = new Label(composite, SWT.NONE);
        urlLabel.setText("Netcdf dataset string"); //$NON-NLS-1$
        urlLabel.setLayoutData(gridData);
        // placeholder
        new Label(composite, SWT.NONE);

        // For Drag 'n Drop as well as for general selections
        // look for a url as part of the selction
        Map<String, Serializable> params = defaultParams(); // based on
        // combo selection
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = 400;
        urlCombo = new Combo(composite, SWT.BORDER);
        urlCombo.setItems(recentNetcdf);
        urlCombo.setVisibleItemCount(15);
        urlCombo.setLayoutData(gridData);

        URL selectedURL = getURL(params);
        if (selectedURL != null) {
            File urlToFile = URLUtils.urlToFile(selectedURL);
            if (urlToFile!=null) {
                urlCombo.setText(urlToFile.getAbsolutePath());
                urlString = urlToFile.getAbsolutePath();
            }else{
                urlString = null;
                urlCombo.setText("insert dataset string here");
            }
            setPageComplete(true);
        } else if (urlString != null && urlString.length() != 0) {
            urlCombo.setText(urlString);
            setPageComplete(true);
        } else {
            urlString = null;
            urlCombo.setText("insert dataset string here");
            setPageComplete(false);
        }
        urlCombo.addModifyListener(this);

        // browse button
        Button browseButton = new Button(composite, SWT.PUSH);
        browseButton.setText("..."); //$NON-NLS-1$
        browseButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell());
                fileDialog.setFilterExtensions(new String[]{"*.nc", "*.NC"});
                fileDialog.setText("Select Netcdf dataset");
                String selectedDirectory = fileDialog.open();
                urlCombo.setText(selectedDirectory);
            }
        });
        gridData = new GridData();
        gridData.widthHint = 150;
        browseButton.setLayoutData(gridData);

        setControl(composite);
        setPageComplete(true);
    }

    public URL getURL( Map<String, Serializable> params ) {
        Object value = params.get(NetcdfServiceExtension.KEY);
        if (value == null)
            return null;
        if (value instanceof URL)
            return (URL) value;
        if (value instanceof String) {
            try {
                URL url = new File((String) value).toURI().toURL();
                return url;
            } catch (MalformedURLException erp) {
            }
        }
        return null;
    }

    /**
     * Double click in list, or return from url control.
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     * @param e
     */
    public void widgetDefaultSelected( SelectionEvent e ) {
        e.getClass();// kill warning
        if (getWizard().canFinish()) {
            getWizard().performFinish();
        }
    }

    /**
     * This should be called using the Wizard .. job when next/finish is pressed.
     */
    public List<IService> getResources( IProgressMonitor monitor ) throws Exception {
        URL location = new URL(urlString);

        NetcdfServiceExtension creator = new NetcdfServiceExtension();

        Map<String, Serializable> params = creator.createParams(location);
        IService service = creator.createService(location, params);
        service.getInfo(monitor); // load it

        netcdfServices = new ArrayList<IService>();
        netcdfServices.add(service);

        /*
         * Success! Store the URL in history.
         */
        saveWidgetValues();

        return netcdfServices;
    }

    public void modifyText( ModifyEvent e ) {
        urlString = ((Combo) e.getSource()).getText();
        setErrorMessage(null);
        setPageComplete(true);

        getWizard().getContainer().updateButtons();
        // getWizard().getContainer().updateButtons();
    }

    /**
     * Saves the widget values
     */
    private void saveWidgetValues() {
        // Update history
        if (settings != null) {
            String[] recentJGrasses = settings.getArray(NETCDF_RECENT);
            if (recentJGrasses == null) {
                recentJGrasses = new String[0];
            }
            recentJGrasses = addToHistory(recentJGrasses, urlString);
            settings.put(NETCDF_RECENT, recentJGrasses);
        }
    }

    /**
     * Adds an entry to a history, while taking care of duplicate history items and excessively long
     * histories. The assumption is made that all histories should be of length
     * <code>COMBO_HISTORY_LENGTH</code>.
     * 
     * @param history the current history
     * @param newEntry the entry to add to the history
     * @return the history with the new entry appended Stolen from
     *         org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage
     */
    private String[] addToHistory( String[] history, String newEntry ) {
        ArrayList<String> l = new ArrayList<String>(Arrays.asList(history));
        addToHistory(l, newEntry);
        String[] r = new String[l.size()];
        l.toArray(r);
        return r;
    }

    /**
     * Adds an entry to a history, while taking care of duplicate history items and excessively long
     * histories. The assumption is made that all histories should be of length
     * <code>COMBO_HISTORY_LENGTH</code>.
     * 
     * @param history the current history
     * @param newEntry the entry to add to the history Stolen from
     *            org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage
     */
    private void addToHistory( List<String> history, String newEntry ) {
        history.remove(newEntry);
        history.add(0, newEntry);

        // since only one new item was added, we can be over the limit
        // by at most one item
        if (history.size() > COMBO_HISTORY_LENGTH)
            history.remove(COMBO_HISTORY_LENGTH);
    }

    public Map<String, Serializable> getParams() {
        try {
            if (urlString == null)
                return Collections.emptyMap();

            URL location = new URL(urlString);

            NetcdfServiceExtension creator = new NetcdfServiceExtension();
            return creator.createParams(location);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    // public List<URL> getURLs() {
    // try {
    // ArrayList<URL> l = new ArrayList<URL>();
    // l.add(new URL(url));
    //
    // return l;
    // } catch (MalformedURLException e) {
    // return null;
    // }
    // }

    public Collection<URL> getResourceIDs() {
        try {
            ArrayList<URL> l = new ArrayList<URL>();
            l.add(new File(urlString).toURI().toURL());

            return l;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public Collection<IService> getServices() {
        URL netcdfUrl = NetcdfServiceExtension.getAsNetcdfUrl(urlString);
        if (netcdfUrl == null)
            return Collections.EMPTY_LIST;
        
        NetcdfServiceExtension creator = new NetcdfServiceExtension();
        Map<String, Serializable> params = creator.createParams(netcdfUrl);
        IService service = creator.createService(netcdfUrl, params);

        if (netcdfServices == null)
            netcdfServices = new ArrayList<IService>();
        netcdfServices.add(service);

        return netcdfServices;
    }

}
