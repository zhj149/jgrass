package eu.hydrologis.jgrass.tests.utils;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import junit.framework.TestCase;
import eu.hydrologis.jgrass.libs.map.RasterData;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.*;

public class JGrassTestCase extends TestCase {

    protected String linkMessage = "there is a problem in a link";

    protected void checkMatrixEqual( RasterData rasterData, double[][] matrix ) {
        for( int i = 0; i < matrix.length; i++ ) {
            for( int j = 0; j < matrix[0].length; j++ ) {
                double expectedResult = matrix[i][j];
                double value = rasterData.getValueAt(i, j);
                if (isNovalue(value)) {
                    assertTrue(isNovalue(expectedResult));
                } else {
                    assertEquals(expectedResult, value);
                }
            }
        }

    }

    protected void checkMatrixEqual( RenderedImage image, double[][] matrix, double delta ) {
        RectIter rectIter = RectIterFactory.create(image, null);
        int y = 0;
        do {
            int x = 0;
            do {
                double value = rectIter.getSampleDouble();
                double expectedResult = matrix[y][x];
                if (isNovalue(value)) {
                    assertTrue(x+" "+y,isNovalue(expectedResult));
                } else {
                    assertEquals(x+" "+y,expectedResult, value, delta);
                }
                x++;
            } while( !rectIter.nextPixelDone() );
            rectIter.startPixels();
            y++;
        } while( !rectIter.nextLineDone() );

    }
    
    protected void checkMatrixEqual( RenderedImage image, double[][] matrix ) {
        RectIter rectIter = RectIterFactory.create(image, null);
        int y = 0;
        do {
            int x = 0;
            do {
                double value = rectIter.getSampleDouble();
                double expectedResult = matrix[y][x];
                if (isNovalue(value)) {
                    assertTrue("Difference at position: " + x + " " + y,isNovalue(expectedResult));
                } else {
                    assertEquals("Difference at position: " + x + " " + y, expectedResult, value);
                }
                x++;
            } while( !rectIter.nextPixelDone() );
            rectIter.startPixels();
            y++;
        } while( !rectIter.nextLineDone() );
        
    }

    protected void checkMatrixEqual( RasterData rasterData, double[][] matrix, double delta ) {
        for( int i = 0; i < matrix.length; i++ ) {
            for( int j = 0; j < matrix[0].length; j++ ) {
                double expectedResult = matrix[i][j];
                double value = rasterData.getValueAt(i, j);

                if (isNovalue(value)) {
                    assertTrue("Difference at position: " + i + " " + j, isNovalue(expectedResult));
                } else {
                    assertEquals("Difference at position: " + i + " " + j, expectedResult, value, delta);
                }
            }
        }

    }
    protected void checkMatrixEqual( Raster image, double[][] matrix ) {
        assertEquals("different dimension", image.getHeight(), matrix.length);
        assertEquals("different dimension", image.getWidth(), matrix[0].length);

        RandomIter randomIter = RandomIterFactory.create(image, null);
        int minX = image.getMinX();
        int minY = image.getMinY();

        for( int j = minY; j < minY + image.getHeight(); j++ ) {
            for( int i = minX; i < minX + image.getWidth(); i++ ) {
                double expectedResult = matrix[i - minX][j - minY];
                double value = randomIter.getSampleDouble(i, j, 0);
                if (isNovalue(value)) {
                    assertTrue("Difference at position: " + i + " " + j, isNovalue(expectedResult));
                } else {
                    assertEquals("Difference at position: " + i + " " + j, expectedResult, value);
                }
            }
        }

    }

}
