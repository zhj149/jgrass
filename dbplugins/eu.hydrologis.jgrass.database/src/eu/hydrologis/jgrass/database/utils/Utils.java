package eu.hydrologis.jgrass.database.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    /**
     * Generates minimum config file that needs to be on disk.
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    public static File generateConfigFile() throws IOException {
        StringBuilder sB = new StringBuilder();
        sB.append("<?xml version='1.0' encoding='utf-8'?>");
        sB.append("<!DOCTYPE hibernate-configuration PUBLIC");
        sB.append("    \"-//Hibernate/Hibernate Configuration DTD//EN\"");
        sB.append("    \"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">");
        sB.append("<hibernate-configuration>");
        sB.append(" <session-factory>");
        sB.append("     <!-- Use the C3P0 connection pool provider -->");
        sB.append("     <property name=\"hibernate.c3p0.min_size\">5</property>");
        sB.append("     <property name=\"hibernate.c3p0.max_size\">20</property>");
        sB.append("     <property name=\"hibernate.c3p0.timeout\">300</property>");
        sB.append("     <property name=\"hibernate.c3p0.max_statements\">50</property>");
        sB.append("     <property name=\"hibernate.c3p0.idle_test_period\">3000</property>");
        sB.append(" </session-factory>");
        sB.append("</hibernate-configuration>");

        File tempFile = File.createTempFile("jgrass_hibernate", null);
        BufferedWriter bW = new BufferedWriter(new FileWriter(tempFile));
        bW.write(sB.toString());
        bW.close();

        return tempFile;
    }
}
