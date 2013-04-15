package eu.hydrologis.jgrass.tests.libs;

import java.io.File;
import java.io.IOException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import static java.lang.Math.*;
import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.GeometryUtilities;

import junit.framework.TestCase;

/**
 * Test the {@link GeometryUtilities} class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class TestGeometryutilities extends TestCase {

    private static final double EPSI = 0.000001;

    public void testGeometryUtilities() throws IOException {

        /*
         * azimuth calculation
         */
        // horiz to right
        Coordinate c1 = new Coordinate(1, 1);
        Coordinate c2 = new Coordinate(3, 1);
        double azimuth = GeometryUtilities.azimuth(c1, c2);
        assertEquals(90.0, azimuth, EPSI);

        // horiz to left
        c1 = new Coordinate(3, 1);
        c2 = new Coordinate(1, 1);
        azimuth = GeometryUtilities.azimuth(c1, c2);
        assertEquals(270.0, azimuth, EPSI);

        // vert up
        c1 = new Coordinate(1, 1);
        c2 = new Coordinate(1, 3);
        azimuth = GeometryUtilities.azimuth(c1, c2);
        assertEquals(0.0, azimuth, EPSI);

        // vert down
        c1 = new Coordinate(1, 3);
        c2 = new Coordinate(1, 1);
        azimuth = GeometryUtilities.azimuth(c1, c2);
        assertEquals(180.0, azimuth, EPSI);

        // negative slope
        c1 = new Coordinate(1, 2);
        c2 = new Coordinate(3, 0);
        azimuth = GeometryUtilities.azimuth(c1, c2);
        assertEquals(135.0, azimuth, EPSI);

        // just invert coordinates
        c1 = new Coordinate(1, 2);
        c2 = new Coordinate(3, 0);
        azimuth = GeometryUtilities.azimuth(c2, c1);
        assertEquals(315.0, azimuth, EPSI);

        // positive slope
        c1 = new Coordinate(0, 0);
        c2 = new Coordinate(sqrt(3), 1);
        azimuth = GeometryUtilities.azimuth(c1, c2);
        assertEquals(60.0, azimuth, EPSI);

        // just invert coordinates
        c1 = new Coordinate(0, 0);
        c2 = new Coordinate(sqrt(3), 1);
        azimuth = GeometryUtilities.azimuth(c2, c1);
        assertEquals(240.0, azimuth, EPSI);

    }
}
