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
package eu.hydrologis.jgrass.database.core.postgres;

import i18n.database.Messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import eu.hydrologis.jgrass.database.DatabasePlugin;
import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.interfaces.IConnectionFactory;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseConnection;

/**
 * A connection factory for Postgresql databases.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PostgresConnectionFactory implements IConnectionFactory {

    
    public IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties ) {
        PostgresDatabaseConnection connection = new PostgresDatabaseConnection();
        connection.setConnectionParameters(connectionProperties);
        return connection;
    }

    
    public DatabaseConnectionProperties createProperties( File dbFile ) throws IOException {
        // postgres is not file based
        return null;
    }

    
    public DatabaseConnectionProperties createDefaultProperties() {
        DatabaseConnectionProperties props = new DatabaseConnectionProperties();
        props.put(DatabaseConnectionProperties.TYPE, PostgresDatabaseConnection.TYPE);
        props.put(DatabaseConnectionProperties.ISACTIVE, "false"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.TITLE, Messages.H2ConnectionFactory__default_db);
        props.put(DatabaseConnectionProperties.DESCRIPTION, Messages.H2ConnectionFactory__default_db);
        props.put(DatabaseConnectionProperties.DRIVER, PostgresDatabaseConnection.DRIVER);
        props.put(DatabaseConnectionProperties.DATABASE, "database"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.PORT, "5432"); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.USER, ""); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.PASS, ""); //$NON-NLS-1$
        props.put(DatabaseConnectionProperties.HOST, "localhost"); //$NON-NLS-1$
        return props;
    }

    @SuppressWarnings("nls")
    public String generateWebserverConnectionString( DatabaseConnectionProperties connectionProperties ) throws IOException {
        String port = DatabasePlugin.WEBSERVERPORT;
        String host = connectionProperties.getHost();
        String dbName = connectionProperties.getDatabaseName();
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
            sB.append("driver=org.postgresql.Driver&url=jdbc:postgresql:");
            if (!host.trim().equals("localhost") && !host.trim().equals("127.0.0.1")) {
                sB.append("//");
                sB.append(host);
                sB.append(":");
                sB.append(port);
                sB.append("/");
            }
            sB.append(dbName);
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
