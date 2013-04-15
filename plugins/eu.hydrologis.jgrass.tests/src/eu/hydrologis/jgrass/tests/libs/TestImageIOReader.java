package eu.hydrologis.jgrass.tests.libs;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.datum.PixelInCell;

import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReadParam;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReader;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.GrassBinaryImageReader;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.spi.GrassBinaryImageReaderSpi;
import eu.hydrologis.jgrass.libs.jai.operators.GrassFileReadDescriptor;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;

public class TestImageIOReader extends JGrassTestCase {
    private static final double NaN = Double.NaN;

    private static File mapFile = new File(
            "/home/daniele/Jgrassworkspace/testLettura/test/cell/testa");

    Interpolation interpolation = Interpolation.getInstance(Interpolation.INTERP_NEAREST);

    GrassCoverageReader gcReader = new GrassCoverageReader(PixelInCell.CELL_CENTER, interpolation,
            true, false, new PrintStreamProgressMonitor(System.out));

    GrassCoverageReader gcSubsamplingReader = new GrassCoverageReader(PixelInCell.CELL_CENTER,
            interpolation, false, false, new PrintStreamProgressMonitor(System.out));

    double[][] matrix = new double[][]{
            {800.0, 900.0, 1000.0, 1000.0, 1200.0, 1250.0, 1300.0, 1350.0, 1450.0, 1500.0},
            {600.0, 650.0, 750.0, 850.0, 860.0, 900.0, 1000.0, 1200.0, 1250.0, 1500.0},
            {500.0, 550.0, 700.0, 750.0, 800.0, 850.0, 900.0, 1000.0, 1100.0, 1500.0},
            {400.0, 410.0, 650.0, 700.0, 750.0, 800.0, 850.0, 490.0, 450.0, 1500.0},
            {450.0, 550.0, 430.0, 500.0, 600.0, 700.0, 800.0, 500.0, 450.0, 1500.0},
            {500.0, 600.0, 700.0, 750.0, 760.0, 770.0, 850.0, 1000.0, 1150.0, 1500.0},
            {600.0, 700.0, 750.0, 800.0, 780.0, 790.0, 1000.0, 1100.0, 1250.0, 1500.0},
            {800.0, 910.0, 980.0, 1001.0, 1150.0, 1200.0, 1250.0, 1300.0, 1450.0, 1500.0}

    };

    double[][] matrixMore = new double[][]{
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},

            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},
            {NaN, NaN, 800.0, 900.0, 1000.0, 1000.0, 1200.0, 1250.0, 1300.0, 1350.0, 1450.0,
                    1500.0, NaN, NaN},
            {NaN, NaN, 600.0, 650.0, 750.0, 850.0, 860.0, 900.0, 1000.0, 1200.0, 1250.0, 1500.0,
                    NaN, NaN},
            {NaN, NaN, 500.0, 550.0, 700.0, 750.0, 800.0, 850.0, 900.0, 1000.0, 1100.0, 1500.0,
                    NaN, NaN},
            {NaN, NaN, 400.0, 410.0, 650.0, 700.0, 750.0, 800.0, 850.0, 490.0, 450.0, 1500.0, NaN,
                    NaN},
            {NaN, NaN, 450.0, 550.0, 430.0, 500.0, 600.0, 700.0, 800.0, 500.0, 450.0, 1500.0, NaN,
                    NaN},
            {NaN, NaN, 500.0, 600.0, 700.0, 750.0, 760.0, 770.0, 850.0, 1000.0, 1150.0, 1500.0,
                    NaN, NaN},
            {NaN, NaN, 600.0, 700.0, 750.0, 800.0, 780.0, 790.0, 1000.0, 1100.0, 1250.0, 1500.0,
                    NaN, NaN},
            {NaN, NaN, 800.0, 910.0, 980.0, 1001.0, 1150.0, 1200.0, 1250.0, 1300.0, 1450.0, 1500.0,
                    NaN, NaN},
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN}};

    double[][] matrixDifferentResolution2 = new double[][]{{800.0,    900.0,   1000.0,  1200.0,  1250.0,  1300.0,  1450.0,  1500.0 }, 
            {600.0,   650.0,   850.0,   860.0,   900.0,   1000.0,  1250.0,  1500.0 }, 
            {400.0,   410.0,   700.0,   750.0,   800.0,   850.0,   450.0,   1500.0},  
            {450.0,   550.0,   500.0,   600.0,   700.0,   800.0,   450.0,   1500.0},  
            {500.0,   600.0,   750.0,   760.0,   770.0,   850.0,   1150.0,  1500.0},  
            {800.0,   910.0,   1001.0,  1150.0,  1200.0,  1250.0,  1450.0,  1500.0}  

    };

    double[][] matrixDifferentResolution = new double[][]{{800.0, 1000.0, 1200.0, 1300.0, 1450.0},
            {500.0, 700.0, 800.0, 900.0, 1100.0,}, {450.0, 430.0, 600.0, 800.0, 450.0},
            {600.0, 750.0, 780.0, 1000.0, 1250.0}, {800.0, 980.0, 1150.0, 1250.0, 1450.0}

    };

    double matrixLess[][] = new double[][]{{700.0, 750.0, 800.0, 850.0, 900.0, 1000.0},
            {650.0, 700.0, 750.0, 800.0, 850.0, 490.0}, {430.0, 500.0, 600.0, 700.0, 800.0, 500.0},
            {700.0, 750.0, 760.0, 770.0, 850.0, 1000.0}

    };

    double differentRegion1[][] = new double[][]{

    {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},
            {1000.0, 1000.0, 1200.0, 1250.0, 1300.0, 1350.0, 1450.0, 1500.0, NaN, NaN},
            {750.0, 850.0, 860.0, 900.0, 1000.0, 1200.0, 1250.0, 1500.0, NaN, NaN},
            {700.0, 750.0, 800.0, 850.0, 900.0, 1000.0, 1100.0, 1500.0, NaN, NaN},
            {650.0, 700.0, 750.0, 800.0, 850.0, 490.0, 450.0, 1500.0, NaN, NaN},
            {430.0, 500.0, 600.0, 700.0, 800.0, 500.0, 450.0, 1500.0, NaN, NaN},
            {700.0, 750.0, 760.0, 770.0, 850.0, 1000.0, 1150.0, 1500.0, NaN, NaN}};

    double differentRegion2[][] = new double[][]{

    {NaN, NaN, 500.0, 550.0, 700.0, 750.0, 800.0, 850.0, 900.0, 1000.0},
            {NaN, NaN, 400.0, 410.0, 650.0, 700.0, 750.0, 800.0, 850.0, 490.0},
            {NaN, NaN, 450.0, 550.0, 430.0, 500.0, 600.0, 700.0, 800.0, 500.0},
            {NaN, NaN, 500.0, 600.0, 700.0, 750.0, 760.0, 770.0, 850.0, 1000.0},
            {NaN, NaN, 600.0, 700.0, 750.0, 800.0, 780.0, 790.0, 1000.0, 1100.0},
            {NaN, NaN, 800.0, 910.0, 980.0, 1001.0, 1150.0, 1200.0, 1250.0, 1300.0},
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},};

    double differentRegion3[][] = new double[][]{
            { 700.0, 750.0, 800.0, 850.0, 900.0, 1000.0, 1100.0, 1500.0, NaN, NaN},
            { 650.0, 700.0, 750.0, 800.0, 850.0, 490.0, 450.0, 1500.0, NaN, NaN},
            { 430.0, 500.0, 600.0, 700.0, 800.0, 500.0, 450.0, 1500.0, NaN, NaN},
            {700.0, 750.0, 760.0, 770.0, 850.0, 1000.0, 1150.0, 1500.0, NaN, NaN},
            { 750.0, 800.0, 780.0, 790.0, 1000.0, 1100.0, 1250.0, 1500.0, NaN, NaN},
            { 980.0, 1001.0, 1150.0, 1200.0, 1250.0, 1300.0, 1450.0, 1500.0, NaN, NaN},
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},};
    
    double differentRegion4[][] = new double[][]{
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},
            {NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN},
            {NaN, NaN,800.0, 900.0, 1000.0, 1000.0, 1200.0, 1250.0, 1300.0, 1350.0},
            {NaN, NaN,600.0, 650.0, 750.0, 850.0, 860.0, 900.0, 1000.0, 1200.0},
            {NaN, NaN,500.0, 550.0, 700.0, 750.0, 800.0, 850.0, 900.0, 1000.0},
            {NaN, NaN,400.0, 410.0, 650.0, 700.0, 750.0, 800.0, 850.0, 490.0},
            {NaN, NaN,450.0, 550.0, 430.0, 500.0, 600.0, 700.0, 800.0, 500.0},
            {NaN, NaN,500.0, 600.0, 700.0, 750.0, 760.0, 770.0, 850.0, 1000.0}};
    
    
    
    double tile11[][] = new double[][]{{700.0, 650.00}, {750.0, 700},

    };

    @Override
    protected void setUp() throws Exception {
        gcReader.setInput(mapFile);
        gcSubsamplingReader.setInput(mapFile);
    }

    /**
     * Performs checks on reading in both tiled and non tiled mode
     * and in both subsampled and non subsampled.
     * 
     * @param gcReadParam
     * @throws IOException
     */
    private void checkReading( GrassCoverageReadParam gcReadParam, double[][] matrix )
            throws IOException {
        // read with tiling
        GridCoverage2D coverage2D = gcReader.read(gcReadParam);
        PlanarImage fileImage = (PlanarImage) coverage2D.getRenderedImage();
        checkMatrixEqual(fileImage, matrix);

        // read with tiling and subsampling
        coverage2D = gcSubsamplingReader.read(gcReadParam);
        fileImage = (PlanarImage) coverage2D.getRenderedImage();
        checkMatrixEqual(fileImage, matrix);

        // read without tiling
        coverage2D = gcReader.read(gcReadParam);
        fileImage = (PlanarImage) coverage2D.getRenderedImage();
        checkMatrixEqual(fileImage, matrix);

        // read without tiling and subsampling
        coverage2D = gcSubsamplingReader.read(gcReadParam);
        fileImage = (PlanarImage) coverage2D.getRenderedImage();
        checkMatrixEqual(fileImage, matrix);
    }

    /**
     * Read the whole Image (at file region and resolution), using tailing 
     * (N.B. Only for the test I have set the tile size to 2).
     * 
     * @throws IOException
     */
    public void testReadFromFileRegion() throws IOException {
        JGrassRegion readRegion = new JGrassRegion(1640650.0, 1640950.0, 5139780.0, 5140020.0,
                30.0, 30.0);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(readRegion);

        checkReading(gcReadParam, matrix);
    }

    /**
     * Read a region which is bigger, in all direction, than the file region.
     * 
     * @throws IOException
     */

    public void testReadFromWrappingRegion() throws IOException {
        JGrassRegion readRegion = new JGrassRegion(1640590.0, 1641010.0, 5139720.0, 5140080.0,
                30.0, 30.0);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(readRegion);

        checkReading(gcReadParam, matrixMore);

    }

    /**
     * Read a region which dimension is smaller and completely contained
     * in thefile region.
     * 
     * @throws IOException
     */
    public void testReadFromContainedRegion() throws IOException {
        JGrassRegion readRegion = new JGrassRegion(1640710.0, 1640890.0, 5139840.0, 5139960.0,
                30.0, 30.0);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(readRegion);

        checkReading(gcReadParam, matrixLess);
    }

    /**
     * Test the getTile method controling that the requested tile is really read.
     * 
     * @throws IOException
     */
    public void testGetTile() throws IOException {
        JGrassRegion readRegion = new JGrassRegion(1640650.0, 1640950.0, 5139780.0, 5140020.0,
                30.0, 30.0);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(readRegion);

        GridCoverage2D coverage2D = gcReader.read(gcReadParam);
        Raster fileImage = ((PlanarImage) coverage2D.getRenderedImage()).getTile(1, 1);

        checkMatrixEqual(fileImage, tile11);

    }

    /**
     * Test the reading of the map in a region containing the upper right 
     * corner.
     * 
     * The schema is:
     * <table border=1>
     * <tr>
     * <td>11</td><td>12</td>
     * </tr>
     * <tr>
     * <td>21</td><td>22</td>
     * </tr>
     * </table>
     * 
     * @throws IOException
     */
    public void testReadFromRegion12() throws IOException {
        JGrassRegion readRegion = new JGrassRegion(1640710.0, 1641010.0, 5139840.0, 5140080.0,
                30.0, 30.0);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(readRegion);

        checkReading(gcReadParam, differentRegion1);

    }

    /**
     * Test the reading of the map in a region containing the lower left 
     * corner.
     * 
     * The schema is:
     * <table border=1>
     * <tr>
     * <td>11</td><td>12</td>
     * </tr>
     * <tr>
     * <td>21</td><td>22</td>
     * </tr>
     * </table>
     * 
     * @throws IOException
     */
    public void testReadFromRegion21() throws IOException {
        JGrassRegion readRegion = new JGrassRegion(1640590.0, 1640890.0, 5139720.0, 5139960.0,
                30.0, 30.0);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(readRegion);

        checkReading(gcReadParam, differentRegion2);

    }

    /**
     * Test the reading of the map in a region containing the lower right 
     * corner.
     * 
     * The schema is:
     * <table border=1>
     * <tr>
     * <td>11</td><td>12</td>
     * </tr>
     * <tr>
     * <td>21</td><td>22</td>
     * </tr>
     * </table>
     * 
     * @throws IOException
     */
    public void testReadFromRegion22() throws IOException {
        JGrassRegion readRegion = new JGrassRegion(1640710.0, 1641010.0, 5139720.0, 5139960.0,
                30.0, 30.0);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(readRegion);

        checkReading(gcReadParam, differentRegion3);

    }

    /**
     * Test the reading of the map in a region containing the upper left 
     * corner.
     * 
     * The schema is:
     * <table border=1>
     * <tr>
     * <td>11</td><td>12</td>
     * </tr>
     * <tr>
     * <td>21</td><td>22</td>
     * </tr>
     * </table>
     * 
     * @throws IOException
     */
    public void testReadFromRegion11() throws IOException {
        JGrassRegion readRegion = new JGrassRegion(1640590.0, 1640830.0, 5139840.0, 5140080.0,
                30.0, 30.0);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(readRegion);

        checkReading(gcReadParam, differentRegion4);

    }

    /**
     * Read the whole Image, using tiling with a different resolution than 
     * the original map.
     * 
     * @throws IOException
     */
    public void testDifferentResolution() throws IOException {
        JGrassRegion readRegion = new JGrassRegion(1640650.0, 1640950.0, 5139780.0, 5140020.0,
                60.0, 60.0);
        GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(readRegion);

        checkReading(gcReadParam, matrixDifferentResolution);

    }

     /**
     * Test for the nearster interpolator.
     */
        
     public void testDifferentResolution2() throws IOException {
     JGrassRegion readRegion = new JGrassRegion(1640650.0, 1640950.0,
     5139780.0, 5140020.0, 40.0, 40.0);
     GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(
     readRegion);
     checkReading(gcReadParam, matrixDifferentResolution2);

    
     }
     
     
     /**
      * Test the operation to read a dem in a grass database.
      * 
      */
     
     public void testReadOperation(){
         ImageReadParam imageReadParam = new ImageReadParam();
         Rectangle sourceRegion = new Rectangle(0, 0, 9,7);
         GrassBinaryImageReader imageReader = new GrassBinaryImageReader(
                 new GrassBinaryImageReaderSpi());
         ImageLayout layout = new ImageLayout();
         layout.setTileWidth(2);
         layout.setTileHeight(2);
         RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
                 layout);
         imageReadParam.setSourceRegion(sourceRegion);
         
         imageReadParam.setSourceSubsampling(1, 1, 0, 0);
         PlanarImage image= GrassFileReadDescriptor.create(mapFile, 0, false, false, null,
                 null, imageReadParam, imageReader, null, false, false,
                 hints);
         checkMatrixEqual(image, matrix);
     }
     
     
     
     
     
     
     
     

}
