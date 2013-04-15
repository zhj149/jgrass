/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.database.interfaces;

import org.geotools.data.DataStore;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.classic.Session;

import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;

/**
 * Interface for all database types sessionfactories.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public interface IDatabaseConnection {

    /**
     * Setter for the connection parameters.
     * 
     * @param connectionProperties the {@link DatabaseConnectionProperties}. 
     */
    public void setConnectionParameters( DatabaseConnectionProperties connectionProperties );

    /**
     * Getter for the used {@link DatabaseConnectionProperties connection properties}.
     * 
     * @return the connection properties.
     */
    public DatabaseConnectionProperties getConnectionProperties();

    /**
     * Getter for the {@link SessionFactory}.
     * 
     * <p><b>Note that this will also create the connection to the database.</b></p>
     * 
     * @return the current session factory.
     * @throws Exception
     */
    public SessionFactory getSessionFactory() throws Exception;

    /**
     * Opens a {@link Session}.
     * 
     * <p>
     * The closing of the session is responsability of the user.
     * </p>
     * 
     * @return the session as supplied by the current active {@link SessionFactory}.
     */
    public Session openSession();

    /**
     * Closes the current {@link SessionFactory}.
     * @throws Exception 
     */
    public void closeSessionFactory() throws Exception;

    /**
     * Getter for the current {@link AnnotationConfiguration}.
     * 
     * <p>This might be needed for example for schema creation.</p>
     * 
     * @return the current annotation configuration.
     * @throws Exception 
     */
    public AnnotationConfiguration getAnnotationConfiguration() throws Exception;

    /**
     * Checks the existence of the table names passed.
     * 
     * @param tables the tables that need to be there.
     * @return true if all the tables exist.
     * @throws Exception
     */
    public boolean checkTables( String... tables ) throws Exception;

    /**
     * Creates database schemas if needed. 
     * 
     * <p>
     * This is needed, since hibernate is not able to create 
     * schemas in the database, so this has to be done through
     * an sql query in the proper way for every database type.
     * </p> 
     *
     * @param doUpdate switch to define whether to create new or update existing.
     * @throws Exception 
     */
    public void createSchemas( boolean doUpdate ) throws Exception;

    /**
     * The spatial geotools datastore in case it is needed.
     * 
     * @return a geotools {@link DataStore}.
     */
    public DataStore getSpatialDataStore();

}
