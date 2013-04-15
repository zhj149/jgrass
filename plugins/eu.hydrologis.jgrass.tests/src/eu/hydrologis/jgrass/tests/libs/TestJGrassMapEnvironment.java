package eu.hydrologis.jgrass.tests.libs;

import java.io.File;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;

import junit.framework.TestCase;

/**
 * Test the {@link JGrassMapEnvironment} class and the created paths.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class TestJGrassMapEnvironment extends TestCase {

    public void testJGrassMapEnvironment() {
        File mapFile = new File(".", "spearfish60/PERMANENT/cell/aspect");
        File mapsetFile = new File(".", "spearfish60/PERMANENT");

        JGrassMapEnvironment jME = new JGrassMapEnvironment(mapFile);
        checkEnvironment(jME);
        jME = new JGrassMapEnvironment(mapsetFile, "aspect");
        checkEnvironment(jME);

    }

    private void checkEnvironment( JGrassMapEnvironment jME ) {
        File cell = jME.getCELL();
        assertTrue(cell.exists());
        assertTrue(cell.getAbsolutePath().endsWith("spearfish60/PERMANENT/cell/aspect"));

        File cellFolder = jME.getCellFolder();
        assertTrue(cellFolder.exists() && cellFolder.isDirectory());
        assertTrue(cellFolder.getAbsolutePath().endsWith("spearfish60/PERMANENT/cell"));

        File fcell = jME.getFCELL();
        assertTrue(fcell.exists());
        assertTrue(fcell.getAbsolutePath().endsWith("spearfish60/PERMANENT/fcell/aspect"));

        File fcellFolder = jME.getFcellFolder();
        assertTrue(fcellFolder.exists() && fcellFolder.isDirectory());
        assertTrue(fcellFolder.getAbsolutePath().endsWith("spearfish60/PERMANENT/fcell"));

        File colr = jME.getCOLR();
        assertTrue(colr.getAbsolutePath().endsWith("spearfish60/PERMANENT/colr/aspect"));

        File colrFolder = jME.getColrFolder();
        assertTrue(colrFolder.getAbsolutePath().endsWith("spearfish60/PERMANENT/colr"));

        File defaultWind = jME.getDEFAULT_WIND();
        assertTrue(defaultWind.exists());
        assertTrue(defaultWind.getAbsolutePath().endsWith("spearfish60/PERMANENT/DEFAULT_WIND"));

        File wind = jME.getWIND();
        assertTrue(wind.exists());
        assertTrue(wind.getAbsolutePath().endsWith("spearfish60/PERMANENT/WIND"));

    }
}
