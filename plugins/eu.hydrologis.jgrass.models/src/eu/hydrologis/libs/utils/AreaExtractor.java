package eu.hydrologis.libs.utils;

import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.util.ArrayList;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.*;
import java.util.List;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;

/**
 * This is based on Andrea Aimes r.to.vect in GRASS
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class AreaExtractor {

    private int datarows = 0;
    private int datacols = 0;

    private RasterData dataMatrix = null;
    private RandomIter dataIterator = null;
    private Coords[][] dataNodeMatrix = null;

    private JGrassRegion active = null;
    private boolean isCoverage = false;

    public AreaExtractor( RasterData data, JGrassRegion active ) {
        dataMatrix = data;
        datarows = dataMatrix.getRows();
        datacols = dataMatrix.getCols();
        this.active = active;

        isCoverage = false;
    }

    public AreaExtractor( RandomIter dataIterator, JGrassRegion activeRegion ) {
        this.dataIterator = dataIterator;
        this.active = activeRegion;

        datarows = activeRegion.getRows();
        datacols = activeRegion.getCols();

        isCoverage = true;
    }

    /**
     * extract_areas - trace boundaries of polygons in file
     * 
     * @param id an id for the fid of the created feature
     * @return the extracted area feature
     */
    public SimpleFeature extract_areas( int id ) throws Exception {
        double tl, tr, bl, br;

        dataNodeMatrix = new Coords[datarows + 1][datacols + 1];
        for( int i = 0; i < dataNodeMatrix.length; i++ ) {
            for( int j = 0; j < dataNodeMatrix[0].length; j++ ) {
                dataNodeMatrix[i][j] = null;
            }
        }

        /*
         * process rest of file, 2 rows at a time
         */

        if (isCoverage) {
            for( int row = 0; row < datarows - 1; row++ ) {
                for( int col = 0; col < datacols - 1; col++ ) {
                    // System.out.println("ROW: " + row + " COL: " + col);
                    tl = dataIterator.getSampleDouble(col, row, 0); /* top left in window */
                    tr = dataIterator.getSampleDouble(col + 1, row, 0); /* top right */
                    bl = dataIterator.getSampleDouble(col, row + 1, 0); /* bottom left */
                    br = dataIterator.getSampleDouble(col + 1, row + 1, 0); /* bottom right */
                    int kase = nabors(tl, tr, bl, br);
                    update_list(tl, tr, bl, br, kase, row, col);
                }
            }
        } else {
            for( int row = 0; row < datarows - 1; row++ ) {
                for( int col = 0; col < datacols - 1; col++ ) {
                    // System.out.println("ROW: " + row + " COL: " + col);
                    tl = dataMatrix.getValueAt(row, col); /* top left in window */
                    tr = dataMatrix.getValueAt(row, col + 1); /* top right */
                    bl = dataMatrix.getValueAt(row + 1, col); /* bottom left */
                    br = dataMatrix.getValueAt(row + 1, col + 1); /* bottom right */
                    int kase = nabors(tl, tr, bl, br);
                    update_list(tl, tr, bl, br, kase, row, col);
                }
            }
        }
        // write_area(a_list, e_list, area_num, n_equiv);

        List<CoordinateList> coordVector = new ArrayList<CoordinateList>();

        boolean error = false;
        for( int i = 0; i < dataNodeMatrix.length; i++ ) {
            for( int j = 0; j < dataNodeMatrix[0].length; j++ ) {
                if (dataNodeMatrix[i][j] != null && !dataNodeMatrix[i][j].hasBeenUsed) {
                    CoordinateList carr = new CoordinateList();
                    /*
                     * there is some coord info in it. The Coords are linked, so that the whole
                     * polygon can be recreated. In order to not double the polygons, the used
                     * Coords are marked as used.
                     */
                    Coords begin = dataNodeMatrix[i][j];
                    begin.hasBeenUsed = true;
                    Coords next = null;
                    if (begin.forwardCoorPointer != null)
                        next = begin.forwardCoorPointer;
                    else {
                        throw new Exception("Found a lose end!, Error on area number " + id);
                    }

                    carr.add(begin.coord, true);
                    int maxcount = dataNodeMatrix[0].length * dataNodeMatrix.length;
                    int runningcount = 0;
                    while( !begin.equals(next) ) {
                        carr.add(next.coord, true);
                        next.hasBeenUsed = true;
                        if (next.forwardCoorPointer != null)
                            next = next.forwardCoorPointer;
                        else {
                            throw new Exception("Found a lose end!, Error on area number " + id);
                        }
                        if (runningcount > maxcount) {
                            throw new Exception("Found no matching end! Polygon error! Error on area number " + id);
                        }
                        runningcount++;
                    }
                    if (!error) {
                        System.out.println("adding end");
                        carr.add(begin.coord, true);
                    }
                    coordVector.add(carr);
                }
            }
        }

        /*
         * at that point the coordinate list should be complete, let's try to build the polygon
         */

        // create the feature type
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        // set the name
        b.setName("extractedarea"); //$NON-NLS-1$
        // add a geometry property
        if (!error) {
            b.add("the_geom", MultiPolygon.class); //$NON-NLS-1$
        } else {
            // FIXME this has to be checked and for sure changed.
            b.add("the_geom", MultiLineString.class); //$NON-NLS-1$
        }
        // add some properties
        b.add("area", Float.class); //$NON-NLS-1$
        b.add("perimeter", Float.class); //$NON-NLS-1$
        // build the type
        SimpleFeatureType type = b.buildFeatureType();
        // create the feature
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);

        // create the feature attributes
        GeometryFactory gf = new GeometryFactory();
        Geometry ls = null;

        if (!error) {
            Polygon[] pg = new Polygon[coordVector.size()];
            for( int i = 0; i < coordVector.size(); i++ ) {
                LinearRing ring = gf.createLinearRing(coordVector.get(i).toCoordinateArray());
                pg[i] = gf.createPolygon(ring, null);
            }
            ls = gf.createMultiPolygon(pg);
        } else {
            LineString[] pg = new LineString[coordVector.size()];
            for( int i = 0; i < coordVector.size(); i++ ) {
                pg[i] = gf.createLineString(coordVector.get(i).toCoordinateArray());
            }
            ls = gf.createMultiLineString(pg);
        }

        Object[] values = {ls, ls.getArea(), ls.getLength()};
        // add the values
        builder.addAll(values);
        // build the feature with provided ID
        SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + id); //$NON-NLS-1$
        return feature;

    }

    /**
     * update_list - maintains linked list of COOR structures which resprsent bends in and endpoints
     * of lines separating areas in input file; compiles a list of area to category number
     * correspondences; for pictures of what each case in the switch represents, see comments before
     * nabors()
     * 
     * @param i
     * @return
     */
    private void update_list( double tl, double tr, double bl, double br, int i, int row, int col ) {
        Coords c1 = null;
        Coords c2 = null;
        Coords c3 = null;

        switch( i ) {
        case 0:
            /*
             * --*--* c1 | x | | -- * * c3 c2 | | --*--*
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromTr(row, col);
            c2.setCoordinateFromBr(row, col);
            c3.setCoordinateFromBl(row, col);

            // top-center node
            if (dataNodeMatrix[row][col + 1] == null) {
                dataNodeMatrix[row][col + 1] = c1;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // middle-left node
            if (dataNodeMatrix[row + 1][col] == null) {
                dataNodeMatrix[row + 1][col] = c3;
            }
            joinCoordsToSequence(dataNodeMatrix[row][col + 1], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row + 1][col]);

            break;
        case 1:
            /*
             * --*--* c3 | | x | -- * * c1 c2 | x x | --*--*
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromBl(row, col);
            c2.setCoordinateFromBr(row, col);
            c3.setCoordinateFromTr(row, col);

            // top-center node
            if (dataNodeMatrix[row][col + 1] == null) {
                dataNodeMatrix[row][col + 1] = c3;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // middle-left node
            if (dataNodeMatrix[row + 1][col] == null) {
                dataNodeMatrix[row + 1][col] = c1;
            }
            joinCoordsToSequence(dataNodeMatrix[row + 1][col], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row][col + 1]);

            break;
        case 2:
            /*
             * --*--* c3 | | x | *--* c2 c1 | | --*--*
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromBr(row, col + 1);
            c2.setCoordinateFromBr(row, col);
            c3.setCoordinateFromTr(row, col);

            // top-center node
            if (dataNodeMatrix[row][col + 1] == null) {
                dataNodeMatrix[row][col + 1] = c3;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // middle-right node
            if (dataNodeMatrix[row + 1][col + 2] == null) {
                dataNodeMatrix[row + 1][col + 2] = c1;
            }
            joinCoordsToSequence(dataNodeMatrix[row + 1][col + 2], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row][col + 1]);

            break;
        case 3:
            /*
             * --*--* c1 | x | | *--* c2 c3 | x x | --*--*
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromTr(row, col);
            c2.setCoordinateFromBr(row, col);
            c3.setCoordinateFromBr(row, col + 1);

            // top-center node
            if (dataNodeMatrix[row][col + 1] == null) {
                dataNodeMatrix[row][col + 1] = c1;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // middle-right node
            if (dataNodeMatrix[row + 1][col + 2] == null) {
                dataNodeMatrix[row + 1][col + 2] = c3;
            }
            joinCoordsToSequence(dataNodeMatrix[row][col + 1], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row + 1][col + 2]);

            break;
        case 4:
            /*
             * --*--* | | --* * c1 c2 | x | | --*--* c3
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromTl(row + 1, col);
            c2.setCoordinateFromTr(row + 1, col);
            c3.setCoordinateFromBr(row + 1, col);

            // middle-left node
            if (dataNodeMatrix[row + 1][col] == null) {
                dataNodeMatrix[row + 1][col] = c1;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // bottom-center node
            if (dataNodeMatrix[row + 2][col + 1] == null) {
                dataNodeMatrix[row + 2][col + 1] = c3;
            }
            joinCoordsToSequence(dataNodeMatrix[row + 1][col], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row + 2][col + 1]);

            break;
        case 5:
            /*
             * --*--* | x x | --* * c3 c2 | |x | --*--* c1
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromBr(row + 1, col);
            c2.setCoordinateFromTr(row + 1, col);
            c3.setCoordinateFromTl(row + 1, col);

            // middle-left node
            if (dataNodeMatrix[row + 1][col] == null) {
                dataNodeMatrix[row + 1][col] = c3;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // bottom-center node
            if (dataNodeMatrix[row + 2][col + 1] == null) {
                dataNodeMatrix[row + 2][col + 1] = c1;
            }
            joinCoordsToSequence(dataNodeMatrix[row + 2][col + 1], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row + 1][col]);
            break;
        case 6:
            /*
             * --*--* | | *--* c2 c3 | | x | --*--* c1
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromBl(row + 1, col + 1);
            c2.setCoordinateFromTl(row + 1, col + 1);
            c3.setCoordinateFromTr(row + 1, col + 1);

            // bottom-center node
            if (dataNodeMatrix[row + 2][col + 1] == null) {
                dataNodeMatrix[row + 2][col + 1] = c1;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // middle-right node
            if (dataNodeMatrix[row + 1][col + 2] == null) {
                dataNodeMatrix[row + 1][col + 2] = c3;
            }
            joinCoordsToSequence(dataNodeMatrix[row + 2][col + 1], dataNodeMatrix[row + 1][col + 1],
                    dataNodeMatrix[row + 1][col + 2]);

            break;
        case 7:
            /*
             * --*--* | x x | *--* c2 c1 | x | | --*--* c3
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromTr(row + 1, col + 1);
            c2.setCoordinateFromTl(row + 1, col + 1);
            c3.setCoordinateFromBl(row + 1, col + 1);

            // bottom-center node
            if (dataNodeMatrix[row + 2][col + 1] == null) {
                dataNodeMatrix[row + 2][col + 1] = c3;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // middle-right node
            if (dataNodeMatrix[row + 1][col + 2] == null) {
                dataNodeMatrix[row + 1][col + 2] = c1;
            }
            joinCoordsToSequence(dataNodeMatrix[row + 1][col + 2], dataNodeMatrix[row + 1][col + 1],
                    dataNodeMatrix[row + 2][col + 1]);
            break;
        case 8:
            /*
             * --*--* c3 | | x | * * c2 | | x | --*--* c1
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromBl(row + 1, col + 1);
            c2.setCoordinateFromTl(row + 1, col + 1);
            c3.setCoordinateFromTl(row, col + 1);

            // bottom-center node
            if (dataNodeMatrix[row + 2][col + 1] == null) {
                dataNodeMatrix[row + 2][col + 1] = c1;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // top-center node
            if (dataNodeMatrix[row][col + 1] == null) {
                dataNodeMatrix[row][col + 1] = c3;
            }
            joinCoordsToSequence(dataNodeMatrix[row + 2][col + 1], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row][col + 1]);
            break;
        case 9:
            /*
             * --*--* c1 | x | | * * c2 | x | | --*--* c3
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromTl(row, col + 1);
            c2.setCoordinateFromTl(row + 1, col + 1);
            c3.setCoordinateFromBl(row + 1, col + 1);

            // bottom-center node
            if (dataNodeMatrix[row + 2][col + 1] == null) {
                dataNodeMatrix[row + 2][col + 1] = c3;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // top-center node
            if (dataNodeMatrix[row][col + 1] == null) {
                dataNodeMatrix[row][col + 1] = c1;
            }
            joinCoordsToSequence(dataNodeMatrix[row][col + 1], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row + 2][col + 1]);
            break;
        case 10:
            /*
             * --*--* | x x | --*-- * c3 c2 c1 | | --*--*
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromBr(row, col + 1);
            c2.setCoordinateFromBr(row, col);
            c3.setCoordinateFromBl(row, col);

            // middle-right node
            if (dataNodeMatrix[row + 1][col + 2] == null) {
                dataNodeMatrix[row + 1][col + 2] = c1;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // top-center node
            if (dataNodeMatrix[row + 1][col] == null) {
                dataNodeMatrix[row + 1][col] = c3;
            }
            joinCoordsToSequence(dataNodeMatrix[row + 1][col + 2], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row + 1][col]);
            break;
        case 11:
            /*
             * --*--* | | --*-- * c1 c2 c3 | x x | --*--*
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromBl(row, col);
            c2.setCoordinateFromBr(row, col);
            c3.setCoordinateFromBr(row, col + 1);

            // middle-right node
            if (dataNodeMatrix[row + 1][col + 2] == null) {
                dataNodeMatrix[row + 1][col + 2] = c3;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // top-center node
            if (dataNodeMatrix[row + 1][col] == null) {
                dataNodeMatrix[row + 1][col] = c1;
            }
            joinCoordsToSequence(dataNodeMatrix[row + 1][col], dataNodeMatrix[row + 1][col + 1], dataNodeMatrix[row + 1][col + 2]);
            break;
        case 12:
            /*
             * this would create a non valid polygon because of selfintersection. Because of
             * tecnical issues the choosen solution is that to bypass the center point in the second
             * case (i.e. when coming back d1-d2). --*--* c3 | | x | -- *--* c1 c2 d1 | x | | --*-- *
             * d2
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromBl(row, col);
            c2.setCoordinateFromBr(row, col);
            c3.setCoordinateFromTr(row, col);

            // middle-left node
            if (dataNodeMatrix[row + 1][col] == null) {
                dataNodeMatrix[row + 1][col] = c1;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // top-center node
            if (dataNodeMatrix[row][col + 1] == null) {
                dataNodeMatrix[row][col + 1] = c3;
            }

            dataNodeMatrix[row + 1][col].forwardCoorPointer = dataNodeMatrix[row + 1][col + 1];

            dataNodeMatrix[row + 1][col + 1].forwardCoorPointer = dataNodeMatrix[row][col + 1];
            dataNodeMatrix[row + 1][col + 1].backCoorPointer = dataNodeMatrix[row + 1][col];

            dataNodeMatrix[row][col + 1].backCoorPointer = dataNodeMatrix[row + 1][col + 1];

            Coords d1 = new Coords(active);
            Coords d2 = new Coords(active);
            d1.setCoordinateFromBr(row, col + 1);
            d2.setCoordinateFromBr(row + 1, col);

            // middle-right node
            if (dataNodeMatrix[row + 1][col + 2] == null) {
                dataNodeMatrix[row + 1][col + 2] = d1;
            }
            // lower-center node
            if (dataNodeMatrix[row + 2][col + 1] == null) {
                dataNodeMatrix[row + 2][col + 1] = d2;
            }

            // join them
            dataNodeMatrix[row + 1][col + 2].forwardCoorPointer = dataNodeMatrix[row + 2][col + 1];
            dataNodeMatrix[row + 2][col + 1].backCoorPointer = dataNodeMatrix[row + 1][col + 2];
            break;
        case 13:
            /*
             * this would create a non valid polygon because of selfintersection. Because of
             * tecnical issues the choosen solution is that to bypass the center point in the second
             * case (i.e. when coming back d1-d2). --*--* c1 | x | | -- *--* e2 c2 c3 | | x | --*-- *
             * e1
             */

            c1 = new Coords(active);
            c2 = new Coords(active);
            c3 = new Coords(active);
            c1.setCoordinateFromTr(row, col);
            c2.setCoordinateFromBr(row, col);
            c3.setCoordinateFromBr(row, col + 1);

            // top-center node
            if (dataNodeMatrix[row][col + 1] == null) {
                dataNodeMatrix[row][col + 1] = c1;
            }
            // middle-center node
            if (dataNodeMatrix[row + 1][col + 1] == null) {
                dataNodeMatrix[row + 1][col + 1] = c2;
            }
            // middle-right node
            if (dataNodeMatrix[row + 1][col + 2] == null) {
                dataNodeMatrix[row + 1][col + 2] = c3;
            }

            dataNodeMatrix[row][col + 1].forwardCoorPointer = dataNodeMatrix[row + 1][col + 1];

            dataNodeMatrix[row + 1][col + 1].forwardCoorPointer = dataNodeMatrix[row + 1][col + 2];
            dataNodeMatrix[row + 1][col + 1].backCoorPointer = dataNodeMatrix[row][col + 1];

            dataNodeMatrix[row + 1][col + 2].backCoorPointer = dataNodeMatrix[row + 1][col + 1];

            Coords e1 = new Coords(active);
            Coords e2 = new Coords(active);
            e1.setCoordinateFromBr(row + 1, col);
            e2.setCoordinateFromTl(row + 1, col);

            // lower-center node
            if (dataNodeMatrix[row + 2][col + 1] == null) {
                dataNodeMatrix[row + 2][col + 1] = e1;
            }
            // middle-left node
            if (dataNodeMatrix[row + 1][col] == null) {
                dataNodeMatrix[row + 1][col] = e2;
            }

            // join them
            dataNodeMatrix[row + 2][col + 1].forwardCoorPointer = dataNodeMatrix[row + 1][col];
            dataNodeMatrix[row + 1][col].backCoorPointer = dataNodeMatrix[row + 2][col + 1];
            break;
        } /* switch */

    }

    /**
     * Joins three Coords assuming that the second is the next of the first and the first the
     * previous of the second and so on. That way they are chained to each other in the right order
     * through their internal pointers.
     * 
     * @param coords1
     * @param coords2
     * @param coords3
     */
    private void joinCoordsToSequence( Coords coords1, Coords coords2, Coords coords3 ) {
        if (coords1.forwardCoorPointer == null)
            coords1.forwardCoorPointer = coords2;

        if (coords2.forwardCoorPointer == null)
            coords2.forwardCoorPointer = coords3;
        if (coords2.backCoorPointer == null)
            coords2.backCoorPointer = coords1;

        if (coords3.backCoorPointer == null)
            coords3.backCoorPointer = coords2;
    }

    /**
     * nabors - check 2 x 2 matrix and return case from table below *--*--* *--*--* *--*--* *--*--* |
     * x | | | | x| | | x | | x | | *--* * *--* * * *--* * *--* | | | x x | | | | x x | *--*--*
     * *--*--* *--*--* *--*--* 0 1 2 3 *--*--* *--*--* *--*--* *--*--* | | | x x | | | | x x | *--* *
     * *--* * * *--* * *--* | x | | | | x | | | x | | x| | *--*--* *--*--* *--*--* *--*--* 4 5 6 7
     * *--*--* *--*--* *--*--* *--*--* | |x | | x | | | x x | | | * * * * * * *--*--* *--*--* | |x | |
     * x | | | | | x x | *--*--* *--*--* *--*--* *--*--* 8 9 10 11 *--*--* *--*--* *--*--* | |x | |
     * x | | | | *--*--* * * * * * | x | | | | x| | | *--*--* *--*--* *--*--* 12 13 14
     */
    private int nabors( double tl, double tr, double bl, double br ) {
        if (!isNovalue(tl) && isNovalue(tr) && isNovalue(bl) && isNovalue(br))
            return 0;
        if (isNovalue(tl) && !isNovalue(tr) && !isNovalue(bl) && !isNovalue(br))
            return 1;
        if (isNovalue(tl) && !isNovalue(tr) && isNovalue(bl) && isNovalue(br))
            return 2;
        if (!isNovalue(tl) && isNovalue(tr) && !isNovalue(bl) && !isNovalue(br))
            return 3;
        if (isNovalue(tl) && isNovalue(tr) && !isNovalue(bl) && isNovalue(br))
            return 4;
        if (!isNovalue(tl) && !isNovalue(tr) && isNovalue(bl) && !isNovalue(br))
            return 5;
        if (isNovalue(tl) && isNovalue(tr) && isNovalue(bl) && !isNovalue(br))
            return 6;
        if (!isNovalue(tl) && !isNovalue(tr) && !isNovalue(bl) && isNovalue(br))
            return 7;
        if (isNovalue(tl) && !isNovalue(tr) && isNovalue(bl) && !isNovalue(br))
            return 8;
        if (!isNovalue(tl) && isNovalue(tr) && !isNovalue(bl) && isNovalue(br))
            return 9;
        if (!isNovalue(tl) && !isNovalue(tr) && isNovalue(bl) && isNovalue(br))
            return 10;
        if (isNovalue(tl) && isNovalue(tr) && !isNovalue(bl) && !isNovalue(br))
            return 11;
        if (isNovalue(tl) && !isNovalue(tr) && !isNovalue(bl) && isNovalue(br))
            return 12;
        if (!isNovalue(tl) && isNovalue(tr) && isNovalue(bl) && !isNovalue(br))
            return 13;

        return 14;
    }

}