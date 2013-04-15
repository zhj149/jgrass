package eu.hydrologis.openmi.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * <p>
 * Utilities for reading some particular file formats
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 * @since 1.1.0
 */
public class FileReaderUtils {
    /**
     * Read file that have coordinate triples in it.
     * 
     * @param stationsFilePath the path to the file
     * @return the vector of JTS point geometries
     */
    public static Vector<Geometry> read3DCoordinatesToPointGeometry( String stationsFilePath ) {
        Vector<Geometry> stationsList = new Vector<Geometry>();
        GeometryFactory geomFac = new GeometryFactory();
        try {
            BufferedReader stationsGeomReader = new BufferedReader(new FileReader(stationsFilePath));
            String str = null;
            while( (str = stationsGeomReader.readLine()) != null ) {
                StringTokenizer st = new StringTokenizer(str);
                int tokcount = st.countTokens();
                if (tokcount != 3) {
                    throw new RuntimeException(
                            "Stations coordinates file should have 3 coordinates");
                }

                /*
                 * get the 3 coordinates
                 */
                String token = st.nextToken();
                double x = Double.parseDouble(token.trim());
                token = st.nextToken();
                double y = Double.parseDouble(token.trim());
                token = st.nextToken();
                double z = Double.parseDouble(token.trim());

                /*
                 * add it to the coordinates list
                 */
                stationsList.add(geomFac.createPoint(new Coordinate(x, y, z)));

            }
            stationsGeomReader.close();
        } catch (IOException e) {
            throw new RuntimeException("Something strange happened");
        }

        return stationsList;
    }
}
