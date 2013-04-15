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
package eu.hydrologis.jgrass.database.interfaces;

import java.io.File;
import java.io.IOException;

import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;

/**
 * Database connection factory interface.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public interface IConnectionFactory {

    /**
     * Create a {@link IDatabaseConnection database connection}.
     * 
     * @param connectionProperties the {@link DatabaseConnectionProperties connection properties} to use.
     * @return the connection.
     */
    public IDatabaseConnection createDatabaseConnection( DatabaseConnectionProperties connectionProperties );

    /**
     * Create {@link DatabaseConnectionProperties connection properties} or null.
     * 
     * @param dbFile the database file.
     * @return the connection properties or null, if the dbFile is not of the right type.
     * @throws IOException
     */
    public DatabaseConnectionProperties createProperties( File dbFile ) throws IOException;

    /**
     * Creates a default set of properties for a database.
     * 
     * @return a default set of properties for the database type.
     */
    public DatabaseConnectionProperties createDefaultProperties();

    public String generateWebserverConnectionString( DatabaseConnectionProperties connectionProperties ) throws IOException;

}
