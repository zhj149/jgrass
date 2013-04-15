package eu.hydrologis.jgrass.tests.libs;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.GRASSBINARYRASTERMAP;
import static java.lang.Double.NaN;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.map.JGrassRasterMapReader;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;

/**
 * Test the old JGrass raster reader with different sized regions.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestOldJGrassRasterReader extends JGrassTestCase {

    public void testOldJGrassReader() throws IOException {
        File mapFile = GrassMapTest.mapFile;
        JGrassRegion fileRegion = GrassMapTest.fileRegion;
        JGrassMapEnvironment jGrassMapEnvironment = GrassMapTest.jME;

        /*
         * read the whole map
         */
        // reader 1
        JGrassRasterMapReader jgrassMapReader = new JGrassRasterMapReader.BuilderFromPathAndNames(
                fileRegion, mapFile.getName(), jGrassMapEnvironment.getMAPSET().getName(),
                jGrassMapEnvironment.getLOCATION().getAbsolutePath()).maptype(GRASSBINARYRASTERMAP)
                .build();

        assertTrue(jgrassMapReader.open());
        assertTrue(jgrassMapReader.hasMoreData());
        RasterData rasterData = jgrassMapReader.getNextData();
        jgrassMapReader.close();
        double[][] testMapFileRegion = GrassMapTest.mapData;
        checkMatrixEqual(rasterData, testMapFileRegion);

        // reader 2
        jgrassMapReader = new JGrassRasterMapReader.BuilderFromMapPath(fileRegion, mapFile
                .getAbsolutePath()).maptype(GRASSBINARYRASTERMAP).build();

        assertTrue(jgrassMapReader.open());
        assertTrue(jgrassMapReader.hasMoreData());
        rasterData = jgrassMapReader.getNextData();
        jgrassMapReader.close();
        testMapFileRegion = GrassMapTest.mapData;
        checkMatrixEqual(rasterData, testMapFileRegion);

        /*
         * read on a larger region
         */

        JGrassRegion testRegion1 = new JGrassRegion(1640620.0, 1640980.0, 5139750.0, 5140050.0,
                30.0, 30.0);
        double[][] testMapForRegion1 = new double[][]{ 
            {NaN,NaN,NaN, NaN, NaN,NaN,NaN, NaN, NaN,NaN,NaN,NaN},
            {NaN,800,900, 1000, 1000,1200,1250, 1300, 1350,1450,1500,NaN},
            {NaN,600, NaN, 750, 850, 860, 900, 1000, 1200, 1250, 1500,NaN},
            {NaN,500, 550, 700, 750, 800, 850, 900, 1000, 1100, 1500,NaN},
            {NaN,400, 410, 650, 700, 750, 800, 850, 490, 450, 1500,NaN},
            {NaN,450, 550, 430, 500, 600, 700, 800, 500, 450, 1500,NaN},
            {NaN,500, 600, 700, 750, 760, 770, 850, 1000, 1150, 1500,NaN},
            {NaN,600, 700, 750, 800, 780, 790, 1000, 1100, 1250, 1500,NaN},
            {NaN,800, 910, 980, 1001, 1150, 1200, 1250, 1300, 1450, 1500,NaN},
            {NaN,NaN,NaN, NaN, NaN,NaN,NaN,NaN, NaN,NaN,NaN,NaN}
            };
        // reader 1
        jgrassMapReader = new JGrassRasterMapReader.BuilderFromPathAndNames(testRegion1, mapFile
                .getName(), jGrassMapEnvironment.getMAPSET().getName(), jGrassMapEnvironment
                .getLOCATION().getAbsolutePath()).maptype(GRASSBINARYRASTERMAP).build();

        assertTrue(jgrassMapReader.open());
        assertTrue(jgrassMapReader.hasMoreData());
        rasterData = jgrassMapReader.getNextData();
        jgrassMapReader.close();
        checkMatrixEqual(rasterData, testMapForRegion1);

        // reader 2
        jgrassMapReader = new JGrassRasterMapReader.BuilderFromMapPath(testRegion1, mapFile
                .getAbsolutePath()).maptype(GRASSBINARYRASTERMAP).build();

        assertTrue(jgrassMapReader.open());
        assertTrue(jgrassMapReader.hasMoreData());
        rasterData = jgrassMapReader.getNextData();
        jgrassMapReader.close();
        checkMatrixEqual(rasterData, testMapForRegion1);

    }

}
