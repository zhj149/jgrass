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

import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Database connection properties.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class DatabaseConnectionProperties extends Properties {
    private static final long serialVersionUID = 1L;

    /**
     * The constant used for the xml representation of the root tag for databases.
     */
    public static final String DATABASES_XML = "DATABASES"; //$NON-NLS-1$

    /**
     * The constant used for the xml representation of the single database tag.
     */
    public static final String DATABASE_XML = "DATABASE"; //$NON-NLS-1$

    /**
     * Property key defining the database type.
     */
    public static final String TYPE = "TYPE"; //$NON-NLS-1$

    /**
     * Property key defining whether the database is currently connected.
     */
    public static final String ISACTIVE = "ISACTIVE"; //$NON-NLS-1$

    /**
     * Property key defining the database title (a human readable name for tables or so).
     */
    public static final String TITLE = "TITLE"; //$NON-NLS-1$

    /**
     * Property key defining the database description.
     */
    public static final String DESCRIPTION = "DESCRIPTION"; //$NON-NLS-1$

    /**
     * Property key defining the database driver class.
     */
    public static final String DRIVER = "DRIVER"; //$NON-NLS-1$
    
    /**
     * Property key defining the database name.
     */
    public static final String DATABASE = "DATABASE"; //$NON-NLS-1$

    /**
     * Property key defining the database port.
     */
    public static final String PORT = "PORT"; //$NON-NLS-1$
    
    /**
     * Property key defining the database user.
     */
    public static final String USER = "USER"; //$NON-NLS-1$
    
    /**
     * Property key defining the database password.
     */
    public static final String PASS = "PASS"; //$NON-NLS-1$
    
    /**
     * Property key defining the database host (for remote databases).
     */
    public static final String HOST = "HOST"; //$NON-NLS-1$
    
    /**
     * Property key defining the database path (for local databases).
     */
    public static final String PATH = "PATH"; //$NON-NLS-1$

    /**
     * All available database properties key to store when doing persistence. 
     */
    public static String[] POSSIBLETAGS = {TYPE, ISACTIVE, TITLE, DESCRIPTION, DRIVER, DATABASE, PORT, USER, PASS, HOST, PATH};

    public static final String SHOW_SQL = "SHOW_SQL"; //$NON-NLS-1$
    public static final String FORMAT_SQL = "FORMAT_SQL"; //$NON-NLS-1$

    public DatabaseConnectionProperties() {
    }

    /**
     * Wraps a {@link Properties} object into a {@link DatabaseConnectionProperties}.
     * 
     * @param properties the properties object.
     */
    public DatabaseConnectionProperties( Properties properties ) {
        Set<Entry<Object, Object>> entries = properties.entrySet();
        for( Entry<Object, Object> entry : entries ) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public String getType() {
        return getProperty(TYPE);
    }

    public String getTitle() {
        return getProperty(TITLE);
    }

    public String getDescription() {
        return getProperty(DESCRIPTION);
    }

    public String getDatabaseDriver() {
        return getProperty(DRIVER);
    }

    public String getDatabaseName() {
        return "database";//getProperty(DATABASE);
    }

    public String getUser() {
        return getProperty(USER);
    }

    public String getPassword() {
        String password = getProperty(PASS);
        if (password == null) {
            password = ""; //$NON-NLS-1$
        }
        return password;
    }

    public String getPath() {
        return getProperty(PATH);
    }

    public String getHost() {
        return getProperty(HOST);
    }

    public String getPort() {
        return getProperty(PORT);
    }

    public boolean doLogSql() {
        String doLog = getProperty(SHOW_SQL);
        return Boolean.parseBoolean(doLog);
    }

    public boolean isActive() {
        String isActive = getProperty(ISACTIVE);
        if (isActive == null) {
            return false;
        }
        return Boolean.parseBoolean(isActive);
    }

    public void setActive( boolean isActive ) {
        put(ISACTIVE, String.valueOf(isActive));
    }

    @Override
    public synchronized String toString() {
        StringBuilder sB = new StringBuilder();
        Set<Entry<Object, Object>> entries = entrySet();
        for( Entry<Object, Object> entry : entries ) {
            sB.append(entry.getKey().toString());
            sB.append("="); //$NON-NLS-1$
            sB.append(entry.getValue().toString());
            sB.append("\n"); //$NON-NLS-1$
        }
        return sB.toString();
    }

    /**
     * Populates the properties from a text of properties.
     * 
     * @param propertiesString
     */
    public void fromString( String propertiesString ) {
        String[] linesSplit = propertiesString.split("\n|\r"); //$NON-NLS-1$
        for( String line : linesSplit ) {
            if (line.contains("=")) { //$NON-NLS-1$
                String[] split = line.trim().split("="); //$NON-NLS-1$
                put(split[0], split[1]);
            }
        }
    }

    @Override
    public synchronized boolean equals( Object o ) {
        if (o instanceof DatabaseConnectionProperties) {
            DatabaseConnectionProperties other = (DatabaseConnectionProperties) o;
            String equalsStringThis = getEqualsString(this);
            String equalsStringOther = getEqualsString(other);
            return equalsStringThis.equals(equalsStringOther);
        } else
            return false;
    }

    private String getEqualsString( DatabaseConnectionProperties prop ) {
        StringBuilder sb = new StringBuilder();
        sb.append(prop.getType());
        sb.append(prop.getTitle());
        sb.append(prop.getDescription());
        sb.append(prop.getDatabaseDriver());
        sb.append(prop.getDatabaseName());
        sb.append(prop.getUser());
        sb.append(prop.getPort());
        return sb.toString();
    }

    @Override
    public synchronized int hashCode() {
        String equalsStringThis = getEqualsString(this);
        int result = 17;
        result = result * equalsStringThis.length();
        result = result * equalsStringThis.indexOf(' ');
        return result;
    }

}
