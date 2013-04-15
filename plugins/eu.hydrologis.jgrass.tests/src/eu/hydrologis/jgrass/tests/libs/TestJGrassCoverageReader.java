package eu.hydrologis.jgrass.tests.libs;

import static java.lang.Double.NaN;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import junit.framework.TestCase;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.datum.PixelInCell;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReadParam;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReader;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;

/**
 * Test JGrass coverage reader with different sized regions.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestJGrassCoverageReader extends JGrassTestCase {

    public void testJGrassCoverageReader() throws IOException {
        File mapFile = GrassMapTest.mapFile;
        JGrassMapEnvironment jME = new JGrassMapEnvironment(mapFile);

        JGrassRegion fileRegion = jME.getFileRegion();
        assertTrue(fileRegion.getWest() == 1640650.0);
        assertTrue(fileRegion.getEast() == 1640950.0);
        assertTrue(fileRegion.getNorth() == 5140020.0);
        assertTrue(fileRegion.getSouth() == 5139780.0);
        assertTrue(fileRegion.getNSResolution() == 30.0);
        assertTrue(fileRegion.getWEResolution() == 30.0);
        assertTrue(fileRegion.getRows() == 8);
        assertTrue(fileRegion.getCols() == 10);

        double[][] testMapFileRegion = GrassMapTest.mapData;

        JGrassRegion testRegion1 = new JGrassRegion(1640620.0, 1640980.0, 5139750.0, 5140050.0,
                30.0, 30.0);
        double[][] testMapRegion1 = new double[][]{ 
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


       
        // read the image in the file region
        GrassCoverageReader coverageReader = new GrassCoverageReader(PixelInCell.CELL_CENTER, null,
                true, false, null);
        GrassCoverageReadParam params = new GrassCoverageReadParam(fileRegion);
        coverageReader.setInput(jME.getCELL());
        GridCoverage2D mapCoverage = coverageReader.read(params);
        RenderedImage renderedImage = mapCoverage.getRenderedImage();

        checkMatrixEqual(renderedImage, testMapFileRegion);

        // read the image in the larger region1
        coverageReader = new GrassCoverageReader(PixelInCell.CELL_CENTER, null, true, false, null);
        params = new GrassCoverageReadParam(testRegion1);
        coverageReader.setInput(jME.getCELL());
        mapCoverage = coverageReader.read(params);
        renderedImage = mapCoverage.getRenderedImage();

        checkMatrixEqual(renderedImage, testMapRegion1);

    }

}
