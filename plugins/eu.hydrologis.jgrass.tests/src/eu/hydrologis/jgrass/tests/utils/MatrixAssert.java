package eu.hydrologis.jgrass.tests.utils;

import java.awt.image.RenderedImage;

import eu.hydrologis.jgrass.libs.map.RasterData;
import static junit.framework.Assert.fail;
public class MatrixAssert{
    
   public static void checkMatrixEqual( RasterData expected, double[][] actual) {
        checkMatrixEqual(null, expected, actual,0);

    }

public    static void checkMatrixEqual( RenderedImage actual, double[][] expected ) {

    }

public    static void checkMatrixEqual(RasterData actual, double[][] expected, double delta ) {

    }
    public static void checkMatrixEqual(String message, RasterData actual, double[][] expected, double delta ) {
        int k=0;
        for( int i = 0; i < expected.length; i++ ) {
            for( int j = 0; j < expected[0].length; j++ ) {
                double expectedResult = expected[i][j];
                double value = actual.getValueAt(i, j);
                if(Double.compare(value, expectedResult)!=0 || !(Math.abs(expectedResult-value) <= delta)){
                    System.out.println("difference in the point"+i+" "+j);
                    k++;    
                }
            }
            if(k!=0){
                
                fail(message+" there is "+k+" different point in this matrix");
            }
        }

    
    }
    
    public static void main( String[] args ) {
    System.out.print(Double.compare(Double.NaN, Double.NaN));   
    System.out.print(Double.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));

    }
    
}
