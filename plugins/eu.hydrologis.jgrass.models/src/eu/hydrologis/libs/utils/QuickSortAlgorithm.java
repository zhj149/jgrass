package eu.hydrologis.libs.utils;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;


import eu.hydrologis.jgrass.libs.utils.monitor.DummyProgressMonitor;
import eu.hydrologis.jgrass.libs.utils.monitor.IProgressMonitorJGrass;

public class QuickSortAlgorithm {
    private double[] valuesToSort;
    private int number;
    private double[] valuesToFollow;
    private IProgressMonitorJGrass monitor = new DummyProgressMonitor();

    public QuickSortAlgorithm( IProgressMonitorJGrass monitor ) {
        if (monitor != null)
            this.monitor = monitor;
    }

    public void sort( double[] values, double[] valuesToFollow ) {
        this.valuesToSort = values;
        this.valuesToFollow = valuesToFollow;
        number = values.length;

        monitor.beginTask("Sorting...", -1);

        monitor.worked(1);
        quicksort(0, number - 1);


        monitor.done();
    }

    private void quicksort( int low, int high ) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        double pivot = valuesToSort[(low + high) / 2];

        // Divide into two lists
        while( i <= j ) {
            // If the current value from the left list is smaller then the pivot
            // element then get the next element from the left list
            while( valuesToSort[i] < pivot  || (isNovalue(valuesToSort[i]) && !isNovalue(pivot)) ) {

            i++;
            }
            // If the current value from the right list is larger then the pivot
            // element then get the next element from the right list
           while( valuesToSort[j] > pivot || (!isNovalue(valuesToSort[j]) && isNovalue(pivot)) ) {
                j--;
            }

            // If we have found a values in the left list which is larger then
            // the pivot element and if we have found a value in the right list
            // which is smaller then the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)
            quicksort(low, j);
        if (i < high)
            quicksort(i, high);
    }

    private void exchange( int i, int j ) {
        double temp = valuesToSort[i];
        double tempFollow = valuesToFollow[i];
        valuesToSort[i] = valuesToSort[j];
        valuesToFollow[i] = valuesToFollow[j];
        valuesToSort[j] = temp;
        valuesToFollow[j] = tempFollow;
    }

}
