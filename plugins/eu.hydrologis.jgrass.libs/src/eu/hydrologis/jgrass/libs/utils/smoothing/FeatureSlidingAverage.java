package eu.hydrologis.jgrass.libs.utils.smoothing;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Applies a sliding average on linear geometries for smoothing.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class FeatureSlidingAverage {

    private final Geometry geometry;

    public FeatureSlidingAverage( Geometry geometry ) {
        this.geometry = geometry;
    }

    public List<Coordinate> smooth( int lookAhead, boolean considerZ, double slide ) {

        double sc;
        List<Coordinate> res = new ArrayList<Coordinate>();

        Coordinate[] coordinates = geometry.getCoordinates();
        int n = coordinates.length; // Points->n_points;
        int half = lookAhead / 2;

        for( int j = 0; j < n; j++ ) {
            Coordinate tmp = new Coordinate();
            res.add(tmp);
        }

        if (lookAhead % 2 == 0) {
            throw new IllegalArgumentException("Look ahead parameter must be odd, but you supplied: " + lookAhead);
        }

        if (lookAhead >= n || lookAhead == 1)
            return null;

        sc = (double) 1.0 / (double) lookAhead;

        Coordinate pCoord = new Coordinate();
        Coordinate sCoord = new Coordinate();
        pointAssign(coordinates, 0, considerZ, pCoord);
        for(int i = 1; i < lookAhead; i++ ) {
            Coordinate tmpCoord = new Coordinate();
            pointAssign(coordinates, i, considerZ, tmpCoord);
            pointAdd(pCoord, tmpCoord, pCoord);
        }

        /* and calculate the average of remaining points */
        for(int i = half; i + half < n; i++ ) {
            Coordinate tmpCoord = new Coordinate();
            pointAssign(coordinates, i, considerZ, sCoord);
            pointScalar(sCoord, 1.0 - slide, sCoord);
            pointScalar(pCoord, sc * slide, tmpCoord);
            pointAdd(tmpCoord, sCoord, res.get(i));
            if (i + half + 1 < n) {
                pointAssign(coordinates, i - half, considerZ, tmpCoord);
                pointSubtract(pCoord, tmpCoord, pCoord);
                pointAssign(coordinates, i + half + 1, considerZ, tmpCoord);
                pointAdd(pCoord, tmpCoord, pCoord);
            }
        }

        for(int i = 0; i < half; i++ ) {
            res.get(i).x = coordinates[i].x;
            res.get(i).y = coordinates[i].y;
            res.get(i).z = coordinates[i].z;
        }
        for(int i = n - half - 1; i < n; i++ ) {
            res.get(i).x = coordinates[i].x;
            res.get(i).y = coordinates[i].y;
            res.get(i).z = coordinates[i].z;
        }
        for( Coordinate coordinate : res ) {
            if (coordinate.x == 0) {
                System.out.println();
            }
        }
        // for( i = half; i + half < n; i++ ) {
        // coordinates[i].x = res.get(i).x;
        // coordinates[i].y = res.get(i).y;
        // coordinates[i].z = res.get(i).z;
        // }

        // return Points->n_points;

        return res;
    }

    private void pointAssign( Coordinate[] coordinates, int index, boolean considerZ, Coordinate newAssignedCoordinate ) {
        Coordinate coordinate = coordinates[index];
        newAssignedCoordinate.x = coordinate.x;
        newAssignedCoordinate.y = coordinate.y;
        if (considerZ) {
            newAssignedCoordinate.z = coordinate.z;
        } else {
            newAssignedCoordinate.z = 0;
        }
        return;
    }

    private void pointAdd( Coordinate a, Coordinate b, Coordinate res ) {
        res.x = a.x + b.x;
        res.y = a.y + b.y;
        res.z = a.z + b.z;
    }

    private void pointSubtract( Coordinate a, Coordinate b, Coordinate res ) {
        res.x = a.x - b.x;
        res.y = a.y - b.y;
        res.z = a.z - b.z;
    }

    private void pointScalar( Coordinate a, double k, Coordinate res ) {
        res.x = a.x * k;
        res.y = a.y * k;
        res.z = a.z * k;
    }

}
