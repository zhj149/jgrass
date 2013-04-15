/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.database.core;

import i18n.database.Messages;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import eu.hydrologis.jgrass.database.core.h2.H2ConnectionFactory;
import eu.hydrologis.jgrass.database.core.h2.H2DatabaseConnection;
import eu.hydrologis.jgrass.database.core.postgres.PostgresConnectionFactory;
import eu.hydrologis.jgrass.database.core.postgres.PostgresDatabaseConnection;
import eu.hydrologis.jgrass.database.interfaces.IConnectionFactory;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseConnection;

/**
 * Class taking care to find the proper {@link IDatabaseConnection}.
 * 
 * <p><b>New databases have to be registered here.</b></p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ConnectionManager {

    private static HashMap<String, IConnectionFactory> databaseDriver2ConnectionFactory = null;

    static {
        databaseDriver2ConnectionFactory = new HashMap<String, IConnectionFactory>();

        // new databases have to be registered here
        databaseDriver2ConnectionFactory.put(H2DatabaseConnection.DRIVER, new H2ConnectionFactory());
        databaseDriver2ConnectionFactory.put(PostgresDatabaseConnection.DRIVER, new PostgresConnectionFactory());
    }

    /**
     * Creates a {@link IDatabaseConnection} for the given properties.
     * 
     * @param connectionProperties
     * @return
     */
    public static synchronized IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties ) {
        String databaseDriver = connectionProperties.getDatabaseDriver();
        IConnectionFactory iConnectionFactory = databaseDriver2ConnectionFactory.get(databaseDriver);

        return iConnectionFactory.createDatabaseConnection(connectionProperties);
    }

    /**
     * Get the {@link IConnectionFactory} for the given properties.
     * 
     * @param connectionProperties
     * @return
     */
    public static synchronized IConnectionFactory getDatabaseConnectionFactory( DatabaseConnectionProperties connectionProperties ) {
        String databaseDriver = connectionProperties.getDatabaseDriver();
        IConnectionFactory iConnectionFactory = databaseDriver2ConnectionFactory.get(databaseDriver);

        return iConnectionFactory;
    }

    /**
     * Checks if the connection is local or remote.
     * 
     * @param connectionProperties the properties to check for.
     * @return true if the connection is local, false if remote.
     */
    public static boolean isLocal( DatabaseConnectionProperties connectionProperties ) {
        String databaseDriver = connectionProperties.getDatabaseDriver();
        if (databaseDriver.equals(H2DatabaseConnection.DRIVER)) {
            return true;
        } else if (databaseDriver.equals(PostgresDatabaseConnection.DRIVER)) {
            return false;
        } else {
            throw new IllegalArgumentException(Messages.ConnectionManager__unknown_db_type);
        }
    }

    /**
     * This creates {@link DatabaseConnectionProperties connection properties} based on a local db file.
     * 
     * <p>The type and some of the fields are guessed.</p>
     * 
     * <p><b>
     * Note that currently this returns H2 properties, since it is the only one supported.
     * </b></p>
     * 
     * @param dbFile the file representing a local database. 
     * @return best guessed connection properties.
     * @throws IOException 
     */
    public static DatabaseConnectionProperties createPropertiesBasedOnFolder( File dbFile ) throws IOException {
        Collection<IConnectionFactory> factories = databaseDriver2ConnectionFactory.values();
        for( IConnectionFactory iConnectionFactory : factories ) {
            DatabaseConnectionProperties properties = iConnectionFactory.createProperties(dbFile);
            if (properties != null) {
                return properties;
            }
        }
        return null;
    }

}
