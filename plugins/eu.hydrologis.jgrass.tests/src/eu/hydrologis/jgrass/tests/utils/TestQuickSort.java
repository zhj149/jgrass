package eu.hydrologis.jgrass.tests.utils;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;
import eu.hydrologis.libs.utils.QuickSortAlgorithm;

public class TestQuickSort extends TestCase {
    double a[]= new double[]{4.0, Double.NaN, 2.0,9.0,Double.NaN,6.0,Double.NaN,1.0,4.0,22.0,16,1.0,12.0,Double.NaN,1.0,2.0};
    double b[]= new double[]{Double.NaN,Double.NaN,Double.NaN,Double.NaN,1.0,1.0,1.0, 2.0,2.0,4.0,4.0,6.0,9.0,12.0,16.0,22.0};
    double f[]=new double[]{1.0, 2.0, 3.0,4.0,5.0,6.0,7.0,8.0,Double.NaN,Double.NaN,11,12,13,14,15,16};
    double f2[]=new double[]{14.0, 5.0, 7.0, 2.0, 8.0, 15.0, 12.0, 16.0, 3.0, 1.0, Double.NaN, 6.0, 4.0, 13.0, 11.0, Double.NaN};
    double a2[]=new double[100];
    double b2[]=new double[100];
    double f3[]=new double[100];
    double f4[]=new double[100];

    public void testQuickSort(){
        QuickSortAlgorithm sortAlgorithm = new QuickSortAlgorithm(null);
        sortAlgorithm.sort(a, f);
        for( int i = 0; i < a.length; i++ ) {
            assertEquals(b[i], a[i], 0);
            assertEquals(f2[i], f[i], 0);
        }
    }
    public void testQuickSort2(){
        Random r = new Random();
        
        for( int i = 0; i < a2.length; i++ ) {
            a2[i] = r.nextDouble();
            f3[i]= r.nextDouble();
            b2[i]=a2[i];
        }
        
        Arrays.sort(b2);
        QuickSortAlgorithm algorithm = new QuickSortAlgorithm(null);
        algorithm.sort(a2, f3);
        
        for( int i = 0; i < a2.length; i++ ) {
            assertEquals(b2[i], a2[i], 0);
        }
    
}
}
