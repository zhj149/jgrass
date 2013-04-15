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
package eu.hydrologis.jgrass.database.core.postgres;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.geotools.data.DataStore;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Environment;
import org.hibernate.classic.Session;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import eu.hydrologis.jgrass.database.core.DatabaseConnectionProperties;
import eu.hydrologis.jgrass.database.earlystartup.AnnotatedClassesCollector;
import eu.hydrologis.jgrass.database.interfaces.IDatabaseConnection;
import eu.hydrologis.jgrass.database.utils.Utils;

/**
 * A Postgresql database connection.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class PostgresDatabaseConnection implements IDatabaseConnection {
    public static final String TYPE = "PostgreSQL"; //$NON-NLS-1$
    public static final String DRIVER = "org.postgresql.Driver"; //$NON-NLS-1$
    private String user;
    private String passwd;
    private String databaseHost;
    private String databaseName;
    private String port;
    private String connectionString;

    private SessionFactory sessionFactory;
    private AnnotationConfiguration annotationConfiguration;

    private boolean doLog;
    private DatabaseConnectionProperties connectionProperties;

    
    public void setConnectionParameters( DatabaseConnectionProperties connectionProperties ) {
        this.connectionProperties = connectionProperties;
        user = connectionProperties.getUser();
        passwd = connectionProperties.getPassword();
        databaseHost = connectionProperties.getHost();
        databaseName = connectionProperties.getDatabaseName();
        port = connectionProperties.getPort();
        doLog = connectionProperties.doLogSql();

        connectionString = "jdbc:postgresql://" + databaseHost + ":" + port + "/" + databaseName; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // make sure every connection has its type
        connectionProperties.put(DatabaseConnectionProperties.TYPE, TYPE);
    }

    
    public DatabaseConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }

    
    public SessionFactory getSessionFactory() throws Exception {
        if (sessionFactory == null) {
            sessionFactory = getAnnotationConfiguration().buildSessionFactory();
        }
        return sessionFactory;
    }

    
    public Session openSession() {
        return sessionFactory.openSession();
    }

    
    public void closeSessionFactory() {
        if (sessionFactory == null) {
            return;
        }
        sessionFactory.close();
    }

    
    public AnnotationConfiguration getAnnotationConfiguration() throws Exception {
        if (annotationConfiguration == null) {
            Properties dbProps = new Properties();
            dbProps.put(Environment.DRIVER, DRIVER);
            dbProps.put(Environment.URL, connectionString);
            dbProps.put(Environment.USER, user);
            dbProps.put(Environment.PASS, passwd);
            dbProps.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect"); //$NON-NLS-1$
            dbProps.put(Environment.SHOW_SQL, String.valueOf(doLog));
            dbProps.put(Environment.FORMAT_SQL, String.valueOf(doLog));

            annotationConfiguration = new AnnotationConfiguration();

            File configFile = Utils.generateConfigFile();
            annotationConfiguration = annotationConfiguration.configure(configFile);
            annotationConfiguration.setProperties(dbProps);

            List<String> annotatedClassesList = AnnotatedClassesCollector.getAnnotatedClassesList();

            for( String annotatedClassString : annotatedClassesList ) {
                annotationConfiguration.addAnnotatedClass(Class.forName(annotatedClassString));
            }
        }
        return annotationConfiguration;
    }

    
    public DataStore getSpatialDataStore() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public boolean checkTables( String... tables ) throws Exception {
        StringBuilder sB = new StringBuilder();
        sB.append("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE "); //$NON-NLS-1$
        for( int i = 0; i < tables.length; i++ ) {
            String tableName = tables[i];
            if (i == 0) {
                sB.append("UPPER(TABLE_NAME) = UPPER('").append(tableName).append("')"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                sB.append(" OR UPPER(TABLE_NAME) = UPPER('").append(tableName).append("')"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        SessionFactory hibernateSessionFactory = getSessionFactory();
        Session session = hibernateSessionFactory.openSession();
        SQLQuery sqlQuery = session.createSQLQuery(sB.toString());
        Number foundNum = (Number) sqlQuery.list().get(0);

        session.close();

        if (tables.length == foundNum.intValue()) {
            return true;
        }
        return false;
    }

    
    public void createSchemas( boolean doUpdate ) throws Exception {
        getSessionFactory();
        if (doUpdate) {
            SchemaUpdate schemaUpdate = new SchemaUpdate(getAnnotationConfiguration());
            schemaUpdate.execute(false, true);
        } else {
            SchemaExport schemaExport = new SchemaExport(getAnnotationConfiguration());
            schemaExport.create(false, true);
        }
    }

}
