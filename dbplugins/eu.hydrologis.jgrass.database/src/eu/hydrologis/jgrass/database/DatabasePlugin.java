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
package eu.hydrologis.jgrass.database;

import i18n.database.Messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.h2.tools.Server;
import org.hibernate.mapping.Table;
import org.osgi.framework.BundleContext;

import eu.hydrologis.jgrass.database.core.ConnectionManager;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.core.h2.H2ConnectionFactory;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseConnection;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseEventListener;
import eu.hydrologis.jgrass.database.utils.ImageCache;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 * 
 * The activator class controls the plug-in life cycle
 */
public class DatabasePlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "eu.hydrologis.jgrass.database"; //$NON-NLS-1$

    // The shared instance
    private static DatabasePlugin plugin;

    private IDatabaseConnection activeDatabaseConnection;
    private DatabaseConnectionProperties activeDatabaseConnectionProperties;
    private List<DatabaseConnectionProperties> availableDatabaseConnectionProperties = new ArrayList<DatabaseConnectionProperties>();

    private List<IDatabaseEventListener> databaseListeners = new ArrayList<IDatabaseEventListener>();

    public static final String WEBSERVERPORT = "10101"; //$NON-NLS-1$
    private Server webServer;

    private static final String DATABASES_XML = "databases.xml"; //$NON-NLS-1$

    /**
     * The constructor
     */
    public DatabasePlugin() {
    }

    public void start( BundleContext context ) throws Exception {
        super.start(context);
        plugin = this;

        startWebserver();
    }

    public void stop( BundleContext context ) throws Exception {
        try {
            saveDatabaseConnections();
            disconnectActiveDatabaseConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        plugin = null;
        if (webServer != null) {
            webServer.stop();
        }

        ImageCache.getInstance().dispose();

        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static DatabasePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the active database connection. 
     * 
     * <p>
     * If no database connection is active, this triggers the create database connection wizard.
     * If the preferences have no database connection info to connect to, create a default one.
     * </p> 
     * <p>
     * <b>This assures to return a database connection or else to throw an Exception.</b>
     * </p>
     * 
     * @return the {@link IDatabaseConnection database connection}.
     */
    public synchronized IDatabaseConnection getActiveDatabaseConnection() throws Exception {
        if (activeDatabaseConnection == null) {
            try {
                loadSavedDatabaseConnections();
                activateDatabaseConnectionFromSaved();
            } catch (Exception e) {
                String message = Messages.DatabasePlugin__errmsg_connecting_db;
                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, DatabasePlugin.PLUGIN_ID, e);
                // fold back on the default database
            }
        }
        if (activeDatabaseConnection == null) {
            createDefaultDatabase();
        }
        return activeDatabaseConnection;
    }

    /**
     * Returns the properties of the current active database connection.
     * 
     * <p><b>This should be called after {@link #getActiveDatabaseConnection()}, to ensure consistency.</b></p>
     * 
     * @return teh propeties of the active database connection.
     */
    public DatabaseConnectionProperties getActiveDatabaseConnectionProperties() {
        return activeDatabaseConnectionProperties;
    }

    private void createDefaultDatabase() throws Exception {
        // create an embedded database inside the project folder
        DatabaseConnectionProperties defaultProperties = new H2ConnectionFactory().createDefaultProperties();

        File databaseFolder = new File(defaultProperties.getPath());
        boolean madeDirs = databaseFolder.mkdirs();
        if (!madeDirs && databaseFolder.exists()) {
            defaultProperties.put(DatabaseConnectionProperties.ISACTIVE, "true"); //$NON-NLS-1$

            activateDatabaseConnection(defaultProperties);
            if (!availableDatabaseConnectionProperties.contains(defaultProperties)) {
                availableDatabaseConnectionProperties.add(defaultProperties);
            }
        } else {
            throw new IOException(Messages.DatabasePlugin__errmsg_creating_db);
        }
    }

    /**
     * Disconnects the active database connection.
     * 
     * @return true if the database was disconnected properly.
     */
    public boolean disconnectActiveDatabaseConnection() {
        try {
            if (activeDatabaseConnection != null) {
                activeDatabaseConnection.closeSessionFactory();
                activeDatabaseConnection = null;
            }
            DatabaseConnectionProperties dbProps = activeDatabaseConnectionProperties;
            if (activeDatabaseConnectionProperties != null) {
                activeDatabaseConnectionProperties.setActive(false);
                activeDatabaseConnectionProperties = null;
            }

            for( IDatabaseEventListener dbListener : databaseListeners ) {
                dbListener.onDatabaseClosed(dbProps);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Creates a new database connection and activates it. 
     * 
     * @param newCP connection properties.
     * @throws Exception
     */
    public void activateDatabaseConnection( DatabaseConnectionProperties newCP ) throws Exception {
        // if another is active, first disconnect it
        if (activeDatabaseConnection != null) {
            disconnectActiveDatabaseConnection();
        }
        // create the new one
        activeDatabaseConnection = ConnectionManager.createDatabaseConnection(newCP);
        activeDatabaseConnectionProperties = newCP;
        // make connection
        activeDatabaseConnection.getSessionFactory();
        checkTableExistence();
        activeDatabaseConnectionProperties.setActive(true);
        if (!availableDatabaseConnectionProperties.contains(newCP)) {
            availableDatabaseConnectionProperties.add(newCP);
        }
        for( DatabaseConnectionProperties properties : availableDatabaseConnectionProperties ) {
            if (!properties.equals(activeDatabaseConnectionProperties)) {
                properties.setActive(false);
            }
        }

        for( IDatabaseEventListener dbListener : databaseListeners ) {
            dbListener.onDatabaseOpened(activeDatabaseConnectionProperties);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkTableExistence() throws Exception {
        Iterator tableMappings = activeDatabaseConnection.getAnnotationConfiguration().getTableMappings();
        List<String> tableList = new ArrayList<String>();
        while( tableMappings.hasNext() ) {
            Object next = tableMappings.next();
            if (next instanceof Table) {
                Table mappedTable = (Table) next;
                String name = mappedTable.getName();
                tableList.add(name);
            }
        }
        boolean checkTables = activeDatabaseConnection.checkTables((String[]) tableList.toArray(new String[tableList.size()]));
        if (!checkTables) {
            activeDatabaseConnection.createSchemas(true);
        }
    }

    /**
     * Loads the saved database connection properties.
     * 
     * 
     * @throws IOException
     * @throws Exception
     */
    public void loadSavedDatabaseConnections() throws IOException, Exception {
        FileReader reader = null;
        try {
            reader = new FileReader(getConfigurationsFile());
            loadSavedDatabaseConnections(XMLMemento.createReadRoot(reader));
        } catch (FileNotFoundException e) {
            // ignore, file does not exist yet
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    // /home/moovida/rcpdevelopment/WORKSPACES/runtime-New_configuration/.metadata/.plugins/eu.hydrologis.jgrass.database/databases.xml
    private void loadSavedDatabaseConnections( XMLMemento memento ) throws Exception {
        String[] possibleTags = DatabaseConnectionProperties.POSSIBLETAGS;
        IMemento[] children = memento.getChildren(DatabaseConnectionProperties.DATABASE_XML);
        for( int i = 0; i < children.length; i++ ) {
            DatabaseConnectionProperties newCP = new DatabaseConnectionProperties();
            for( String tag : possibleTags ) {
                String value = children[i].getString(tag);
                if (value != null && value.length() > 0) {
                    newCP.put(tag, value);
                }
            }
            availableDatabaseConnectionProperties.add(newCP);
        }
    }

    private void activateDatabaseConnectionFromSaved() throws Exception {
        for( DatabaseConnectionProperties properties : availableDatabaseConnectionProperties ) {
            if (properties.isActive()) {
                activateDatabaseConnection(properties);
                if (!availableDatabaseConnectionProperties.contains(properties)) {
                    availableDatabaseConnectionProperties.add(properties);
                }
                break;
            }
        }
    }

    /**
     * Saves collected database connections in the configuration area.
     * 
     * @throws IOException
     */
    public void saveDatabaseConnections() throws IOException {
        XMLMemento memento = XMLMemento.createWriteRoot(DatabaseConnectionProperties.DATABASES_XML);
        saveDatabaseConnections(memento);

        FileWriter writer = null;
        try {
            writer = new FileWriter(getConfigurationsFile());
            memento.save(writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    private void saveDatabaseConnections( XMLMemento memento ) {
        for( DatabaseConnectionProperties dcP : availableDatabaseConnectionProperties ) {
            IMemento child = memento.createChild(DatabaseConnectionProperties.DATABASE_XML);
            Set<Entry<Object, Object>> entries = dcP.entrySet();
            for( Entry<Object, Object> entry : entries ) {
                child.putString(entry.getKey().toString(), entry.getValue().toString());
            }
        }
    }

    /**
     * Checks if the supplied connection has a same name connection already and in case corrects the name.
     * 
     * @param properties the {@link DatabaseConnectionProperties properties} to check.
     */
    @SuppressWarnings("nls")
    public void checkSameNameDbconnection( DatabaseConnectionProperties properties ) {
        int index = 1;
        for( int i = 0; i < availableDatabaseConnectionProperties.size(); i++ ) {
            DatabaseConnectionProperties connProp = availableDatabaseConnectionProperties.get(i);
            String tmpName = connProp.getTitle().trim();
            String name = properties.getTitle().trim();
            if (tmpName.equals(name)) {
                // name exists, change the name of the entering
                if (name.endsWith(")")) {
                    name = name.trim().replaceFirst("\\([0-9]+\\)$", "(" + (index++) + ")");
                } else {
                    name = name + " (" + (index++) + ")";
                }
                properties.setProperty(DatabaseConnectionProperties.TITLE, name);
                // start again
                i = 0;
            }
            if (index == 1000) {
                // something odd is going on
                throw new RuntimeException();
            }
        }
    }

    private File getConfigurationsFile() {
        return getStateLocation().append(DATABASES_XML).toFile();
    }

    public List<DatabaseConnectionProperties> getAvailableDatabaseConnectionProperties() {
        return availableDatabaseConnectionProperties;
    }

    private void startWebserver() {
        Thread h2WebserverThread = new Thread(){
            public void run() {
                try {
                    //                        String[] args = {"-tcp", "-tcpPort", port}; //$NON-NLS-1$ //$NON-NLS-2$
                    // tcpServer = Server.createTcpServer(args).start();
                    String[] args = new String[]{"-web", "-webPort", WEBSERVERPORT}; //$NON-NLS-1$ //$NON-NLS-2$ 
                    webServer = Server.createWebServer(args).start();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        h2WebserverThread.start();
    }

    /**
     * Add a {@link IDatabaseEventListener database listener}.
     * 
     * @param databaseListener the listener to add.
     */
    public void addDatabaseEventListener( IDatabaseEventListener databaseListener ) {
        if (!databaseListeners.contains(databaseListener)) {
            databaseListeners.add(databaseListener);
        }
    }

    /**
     * Remove a {@link IDatabaseEventListener database listener}.
     * 
     * @param databaseListener the listener to remove.
     */
    public void removeDatabaseEventListener( IDatabaseEventListener databaseListener ) {
        databaseListeners.remove(databaseListener);
    }

}
