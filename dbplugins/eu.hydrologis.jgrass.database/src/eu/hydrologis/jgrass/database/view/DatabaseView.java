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
package eu.hydrologis.jgrass.database.view;

import i18n.database.Messages;

import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.IProject;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.core.h2.H2ConnectionFactory;
import eu.hydrologis.jgrass.database.core.postgres.PostgresConnectionFactory;
import eu.hydrologis.jgrass.database.utils.ImageCache;

/**
 * The database view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DatabaseView extends ViewPart {

    public static final String ID = "eu.hydrologis.jgrass.database.catalogview"; //$NON-NLS-1$

    private HashMap<DatabaseConnectionProperties, DatabaseConnectionPropertiesWidget> widgetMap = new HashMap<DatabaseConnectionProperties, DatabaseConnectionPropertiesWidget>();

    private List<DatabaseConnectionProperties> availableDatabaseConnectionProperties;
    private Composite propertiesComposite;
    private StackLayout propertiesStackLayout;

    private DatabaseConnectionProperties currentSelectedConnectionProperties;
    private List<DatabaseConnectionProperties> currentSelectedConnectionPropertiesList = new ArrayList<DatabaseConnectionProperties>();

    private TableViewer connectionsViewer;

    public DatabaseView() {
        try {
            DatabasePlugin.getDefault().getActiveDatabaseConnection();
            availableDatabaseConnectionProperties = DatabasePlugin.getDefault().getAvailableDatabaseConnectionProperties();
        } catch (Exception e) {
            // exception handled at connection time
            e.printStackTrace();
        }
    }

    @Override
    public void createPartControl( Composite parent ) {
        Composite mainComposite = new Composite(parent, SWT.None);
        mainComposite.setLayout(new GridLayout(1, false));
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        Group connectionsGroup = new Group(mainComposite, SWT.BORDER | SWT.SHADOW_ETCHED_IN);
        connectionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        connectionsGroup.setLayout(new GridLayout(3, true));
        connectionsGroup.setText(Messages.DatabaseView__connections);

        Composite connectionsListComposite = new Composite(connectionsGroup, SWT.NONE);
        connectionsListComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        connectionsListComposite.setLayout(new GridLayout(2, false));

        connectionsViewer = createTableViewer(connectionsListComposite);
        connectionsViewer.setInput(availableDatabaseConnectionProperties);
        addFilterButtons(connectionsListComposite, connectionsViewer);

        ScrolledComposite scrolledComposite = new ScrolledComposite(connectionsGroup, SWT.BORDER | SWT.V_SCROLL);
        scrolledComposite.setLayout(new GridLayout(1, false));

        propertiesComposite = new Composite(scrolledComposite, SWT.NONE);
        propertiesStackLayout = new StackLayout();
        propertiesComposite.setLayout(propertiesStackLayout);
        GridData propertiesCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        // propertiesCompositeGD.horizontalSpan = 2;
        propertiesComposite.setLayoutData(propertiesCompositeGD);
        Label l = new Label(propertiesComposite, SWT.SHADOW_ETCHED_IN);
        l.setText(Messages.DatabaseView__no_item_selected);
        propertiesStackLayout.topControl = l;

        scrolledComposite.setContent(propertiesComposite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        // scrolledComposite.setMinWidth(400);
        scrolledComposite.setMinHeight(300);
        GridData scrolledCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        scrolledCompositeGD.horizontalSpan = 2;
        scrolledComposite.setLayoutData(scrolledCompositeGD);
    }

    private TableViewer createTableViewer( Composite connectionsListComposite ) {
        final TableViewer connectionsViewer = new TableViewer(connectionsListComposite);
        Table table = connectionsViewer.getTable();
        GridData tableGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGD.horizontalSpan = 2;
        table.setLayoutData(tableGD);
        connectionsViewer.setContentProvider(new IStructuredContentProvider(){
            public Object[] getElements( Object inputElement ) {
                DatabaseConnectionProperties[] array = (DatabaseConnectionProperties[]) availableDatabaseConnectionProperties
                        .toArray(new DatabaseConnectionProperties[availableDatabaseConnectionProperties.size()]);
                return array;
            }
            public void dispose() {
            }
            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
            }
        });

        connectionsViewer.setLabelProvider(new LabelProvider(){
            public Image getImage( Object element ) {
                if (element instanceof DatabaseConnectionProperties) {
                    DatabaseConnectionProperties connProp = (DatabaseConnectionProperties) element;

                    Image image = null;
                    if (ConnectionManager.isLocal(connProp)) {
                        if (connProp.isActive()) {
                            image = ImageCache.getInstance().getImage(ImageCache.LOCAL_DB_ACTIVE);
                            return image;
                        } else {
                            image = ImageCache.getInstance().getImage(ImageCache.LOCAL_DB);
                            return image;
                        }
                    } else {
                        if (connProp.isActive()) {
                            image = ImageCache.getInstance().getImage(ImageCache.REMOTE_DB_ACTIVE);
                            return image;
                        } else {
                            image = ImageCache.getInstance().getImage(ImageCache.REMOTE_DB);
                            return image;
                        }
                    }

                }
                return null;
            }

            public String getText( Object element ) {
                if (element instanceof DatabaseConnectionProperties) {
                    DatabaseConnectionProperties connProp = (DatabaseConnectionProperties) element;
                    return connProp.getTitle();
                }
                return ""; //$NON-NLS-1$
            }
        });

        connectionsViewer.addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged( SelectionChangedEvent event ) {
                if (!(event.getSelection() instanceof IStructuredSelection)) {
                    return;
                }
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();

                Object selectedItem = sel.getFirstElement();
                if (selectedItem == null) {
                    // unselected, show empty panel
                    return;
                }

                if (selectedItem instanceof DatabaseConnectionProperties) {
                    currentSelectedConnectionProperties = (DatabaseConnectionProperties) selectedItem;
                    DatabaseConnectionPropertiesWidget widget = widgetMap.get(currentSelectedConnectionProperties);
                    if (widget == null) {
                        widget = new DatabaseConnectionPropertiesWidget(DatabaseView.this);
                        widgetMap.put(currentSelectedConnectionProperties, widget);
                    }
                    Control propControl = widget.getComposite(currentSelectedConnectionProperties, propertiesComposite);
                    propertiesStackLayout.topControl = propControl;
                    propertiesComposite.layout(true);
                    widget.checkActivationButton();

                    currentSelectedConnectionPropertiesList.clear();
                    Object[] array = sel.toArray();
                    for( Object conn : array ) {
                        if (conn instanceof DatabaseConnectionProperties) {
                            currentSelectedConnectionPropertiesList.add((DatabaseConnectionProperties) conn);
                        }
                    }

                } else {
                    putUnselected();
                }
            }

        });
        return connectionsViewer;
    }

    private void addFilterButtons( Composite connectionsListComposite, final TableViewer connectionsViewer ) {
        Button filterActive = new Button(connectionsListComposite, SWT.CHECK);
        filterActive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterActive.setText(Messages.DatabaseView__filter_active);
        final ActiveFilter activeFilter = new ActiveFilter();

        filterActive.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent event ) {
                if (((Button) event.widget).getSelection())
                    connectionsViewer.addFilter(activeFilter);
                else
                    connectionsViewer.removeFilter(activeFilter);
            }
        });
        Button filterProjectmatch = new Button(connectionsListComposite, SWT.CHECK);
        filterProjectmatch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterProjectmatch.setText(Messages.DatabaseView__filter_project);
        final ProjectMatchFilter projectFilter = new ProjectMatchFilter();

        filterProjectmatch.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent event ) {
                if (((Button) event.widget).getSelection())
                    connectionsViewer.addFilter(projectFilter);
                else
                    connectionsViewer.removeFilter(projectFilter);
            }
        });
    }
    @Override
    public void setFocus() {
    }

    private static class ActiveFilter extends ViewerFilter {
        public boolean select( Viewer arg0, Object arg1, Object arg2 ) {
            return ((DatabaseConnectionProperties) arg2).isActive();
        }
    }

    private static class ProjectMatchFilter extends ViewerFilter {
        public boolean select( Viewer arg0, Object arg1, Object arg2 ) {
            String name = ((DatabaseConnectionProperties) arg2).getTitle();
            String projectName = ApplicationGIS.getActiveProject().getName();
            if (name.matches("(?i).*" + projectName + ".*")) { //$NON-NLS-1$ //$NON-NLS-2$
                return true;
            }
            return false;
        }
    }

    /**
     * Creates a new default local {@link DatabaseConnectionProperties} and adds it to the list.
     *
     * @param databaseFolder the folder into which to create the database or <code>null</code>.
     * @param databaseName the name for the new database or <code>null</code>.
     * @return the properties for the new created database definition.
     */
    public DatabaseConnectionProperties createNewLocalDatabaseDefinition( String databaseFolder, String databaseName ) {
        DatabaseConnectionProperties defaultProperties = new H2ConnectionFactory().createDefaultProperties();

        String projectName = databaseName;
        if (projectName == null) {
            IProject activeProject = ApplicationGIS.getActiveProject();
            if (activeProject != null) {
                projectName = activeProject.getName();
            }
        }
        if (projectName == null) {
            projectName = Messages.H2ConnectionFactory__default_db;
        }
        if (projectName != null && projectName.length() != 0) {
            defaultProperties.put(DatabaseConnectionProperties.TITLE, projectName);
            defaultProperties.put(DatabaseConnectionProperties.DESCRIPTION, projectName);
        }
        if (databaseFolder != null) {
            defaultProperties.put(DatabaseConnectionProperties.PATH, databaseFolder);
        }
        DatabasePlugin.getDefault().checkSameNameDbconnection(defaultProperties);

        availableDatabaseConnectionProperties.add(defaultProperties);
        relayout();

        setDatabaseSelected(defaultProperties);
        return defaultProperties;
    }

    /**
     * Creates a {@link DatabaseConnectionProperties} definition for an existing local database.
     */
    public void createExistingLocalDatabaseDefinition( DatabaseConnectionProperties props ) {
        DatabasePlugin.getDefault().checkSameNameDbconnection(props);

        availableDatabaseConnectionProperties.add(props);
        relayout();

        setDatabaseSelected(props);
    }

    /**
     * Creates a new default remote {@link DatabaseConnectionProperties} and adds it to the list.
     */
    public void createNewRemoteDatabaseDefinition() {
        DatabaseConnectionProperties defaultProperties = new PostgresConnectionFactory().createDefaultProperties();
        String projectName = ApplicationGIS.getActiveProject().getName();
        if (projectName != null && projectName.length() != 0) {
            defaultProperties.put(DatabaseConnectionProperties.TITLE, projectName);
            defaultProperties.put(DatabaseConnectionProperties.DESCRIPTION, projectName);
        }

        DatabasePlugin.getDefault().checkSameNameDbconnection(defaultProperties);

        availableDatabaseConnectionProperties.add(defaultProperties);
        relayout();
    }

    /**
     * Sets the current selection of the viewer on the supplied {@link DatabaseConnectionProperties}.
     * 
     * @param props the properties to select.
     */
    public void setDatabaseSelected( DatabaseConnectionProperties props ) {
        IStructuredSelection sel = new StructuredSelection(props);
        connectionsViewer.setSelection(sel);
        connectionsViewer.refresh(true, true);
    }

    /**
     * Getter for the current selected {@link DatabaseConnectionProperties}.
     * 
     * @return the current selected {@link DatabaseConnectionProperties}.
     */
    public DatabaseConnectionProperties getCurrentSelectedConnectionProperties() {
        return currentSelectedConnectionProperties;
    }

    /**
     * Deletes the current selected {@link DatabaseConnectionProperties} from the viewer.
     */
    public void removeCurrentSelectedDatabaseDefinition() {
        if (currentSelectedConnectionPropertiesList.size() > 0) {
            for( DatabaseConnectionProperties conn : currentSelectedConnectionPropertiesList ) {
                if (conn.isActive()) {
                    Display.getDefault().asyncExec(new Runnable(){
                        public void run() {
                            MessageDialog.openWarning(connectionsViewer.getTable().getShell(),
                                    Messages.DatabaseConnectionPropertiesWidget__warning,
                                    Messages.DatabaseView__db_not_removed_warning);
                        }
                    });
                    continue;
                }
                availableDatabaseConnectionProperties.remove(conn);
                widgetMap.remove(conn);
            }
        }
        relayout();
        putUnselected();
    }

    public void refreshMap() {
        IMap activeMap = ApplicationGIS.getActiveMap();
        RenderedImage image = activeMap.getRenderManager().getImage();
        if (image == null) {
            return;
        }
        List<ILayer> mapLayers = activeMap.getMapLayers();
        for( ILayer iLayer : mapLayers ) {
            iLayer.refresh(null);
        }
    }

    /**
     * Resfresh the viewer.
     */
    public void relayout() {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                // refresh widgets
                Collection<DatabaseConnectionPropertiesWidget> widgets = widgetMap.values();
                for( DatabaseConnectionPropertiesWidget widget : widgets ) {
                    widget.loadData();
                }
                connectionsViewer.setInput(availableDatabaseConnectionProperties);
            }
        });
    }

    /**
     * Put the properties view to an label that defines no selection.
     */
    public void putUnselected() {
        Label l = new Label(propertiesComposite, SWT.SHADOW_ETCHED_IN);
        l.setText(Messages.DatabaseView__no_item_selected);
        propertiesStackLayout.topControl = l;
        propertiesComposite.layout(true);
    }
}
