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
package eu.hydrologis.jgrass.database.core.h2;

import i18n.database.Messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import net.refractions.udig.project.IProject;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.emf.common.util.URI;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.interfaces.IConnectionFactory;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseConnection;

/**
 * A connection factory for H2 databases.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class H2ConnectionFactory implements IConnectionFactory {

    /**
     * The name for the database.
     * 
     * <p>
     * This is kept to be just <i>database</i>, in order to avoid to ask users
     * that usually have no idea. The db therefore has the name of the folder that contains it.
     * </p>
     */
    public static final String DATABASE = "database"; //$NON-NLS-1$

    public IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties ) {
        H2DatabaseConnection connection = new H2DatabaseConnection();
        connection.setConnectionParameters(connectionProperties);
        return connection;
    }

    /**
     * This creates {@link DatabaseConnectionProperties connection properties} based on a local db file.
     * 
     * <p>The type and some of the fields are guessed.</p>
     * 
     * @param dbFile the file representing a local database. 
     * @return best guessed connection properties.
     * @throws IOException 
     */
    public DatabaseConnectionProperties createProperties( File dbFile ) throws IOException {
		if (!dbFile.exists()) {
			// try to create it
			if (!dbFile.mkdirs())
				throw new IOException(
						Messages.H2ConnectionFactory__db_doesnt_exist);
		}
        if (!dbFile.isDirectory()) {
            return null;
        }

        File[] files = dbFile.listFiles(new FileFilter(){
            public boolean accept( File pathname ) {
                // H2 databases are handled always and only with the name "database"
                return pathname.getName().equals(DATABASE + ".data.db"); //$NON-NLS-1$
            }
        });

        if (files.length == 0) {
            return null;
        }

        // the database name is given by the containing folder name
        String dbName = dbFile.getName();

        DatabaseConnectionProperties props = new DatabaseConnectionProperties();
        props.put(DatabaseConnectionProperties.TYPE, H2DatabaseConnection.TYPE);
        props.put(DatabaseConnectionProperties.ISACTIVE, "false"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.TITLE, dbName);
        props.put(DatabaseConnectionProperties.DESCRIPTION, dbName);
        props.put(DatabaseConnectionProperties.DRIVER, H2DatabaseConnection.DRIVER);
        props.put(DatabaseConnectionProperties.DATABASE, DATABASE);
        props.put(DatabaseConnectionProperties.PORT, "9092"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.USER, "sa"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.PASS, ""); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.PATH, dbFile.getAbsolutePath());

        return props;

    }

    public DatabaseConnectionProperties createDefaultProperties() {
        IProject activeProject = ApplicationGIS.getActiveProject();
        File projectFile = null;
        if (activeProject != null) {
            URI id = activeProject.getID();
            String projectPath = id.toFileString();
            projectFile = new File(projectPath);
            if (!projectFile.exists()) {
                String tempdir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
                projectFile = new File(tempdir);
            } else {
                projectFile = projectFile.getParentFile().getParentFile();
            }
        }else{
            String tempdir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
            projectFile = new File(tempdir);
        }
        File databaseFolder = new File(projectFile, "databases/defaultdatabase"); //$NON-NLS-1$
        DatabaseConnectionProperties props = new DatabaseConnectionProperties();
        props.put(DatabaseConnectionProperties.TYPE, H2DatabaseConnection.TYPE);
        props.put(DatabaseConnectionProperties.ISACTIVE, "false"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.TITLE, Messages.H2ConnectionFactory__default_db);
        props.put(DatabaseConnectionProperties.DESCRIPTION, Messages.H2ConnectionFactory__default_db);
        props.put(DatabaseConnectionProperties.DRIVER, H2DatabaseConnection.DRIVER);
        props.put(DatabaseConnectionProperties.DATABASE, DATABASE);
        props.put(DatabaseConnectionProperties.PORT, "9093"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.USER, "sa"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.PASS, ""); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.PATH, databaseFolder.getAbsolutePath());
        return props;
    }

    @SuppressWarnings("nls")
    public String generateWebserverConnectionString( DatabaseConnectionProperties connectionProperties ) throws IOException {
        String port = DatabasePlugin.WEBSERVERPORT;
        String dbRoot = connectionProperties.getPath();
        String name = connectionProperties.getDatabaseName();
        String user = connectionProperties.getUser();
        String passwd = connectionProperties.getPassword();

        // get the session id
        String base = "http://localhost:" + port;
        URL url = new URL(base);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String str;
        String loginDo = "";
        while( (str = in.readLine()) != null ) {
            if (str.matches(".*jsessionid.*")) {
                str = str.replaceFirst("^.*jsessionid=", "");
                str = str.replaceFirst("';$", "");
                loginDo = "login.do?jsessionid=" + str;
                break;
            }
        }
        in.close();

        StringBuilder sB = new StringBuilder();
        sB.append(base);
        if (loginDo.length() > 0) {
            sB.append("/");
            sB.append(loginDo);
            sB.append("&");
            sB.append("driver=org.h2.Driver&url=jdbc:h2:");
            sB.append(dbRoot);
            sB.append(File.separator);
            sB.append(name);
            sB.append("&");
            sB.append("user=");
            sB.append(user);
            sB.append("&");
            sB.append("password=");
            sB.append(passwd);
        }
        return sB.toString();
    }

}
