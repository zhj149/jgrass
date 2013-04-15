/*
 *    JGrass - Free Open Source Java GIS 
 *    http://www.jgrass.org
 *    (C) The JGrass Developers Group (see on www.jgrass.org)
 *
 *    This library is free software; you can redistribute it and/or         
 *    modify it under the terms of the GNU Library General Public 
 *    License as published by the Free Software Foundation; either 
 *    version 2 of the License, or (at your option) any later version. 
 *    
 *    This library is distributed in the hope that it will be useful, 
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
 *    Library General Public License for more details. 
 *    
 *    You should have received a copy of the GNU Library General Public 
 *    License along with this library; if not, write to the Free 
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 
 *    USA 
 */
package eu.hydrologis.libs.utils;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

/**
 * <p>
 * Utilities for features creation... channel network, basins...
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 * @since 1.1.0
 */
public class FluidFeatureUtils {

    /**
     * It create the shape-file of channel network
     * 
     * @param flowImage
     * @param netNumImage
     * @param nstream
     * @param active
     * @param out
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public FeatureCollection net2ShapeOnly( RenderedImage flowImage, WritableRaster netNumImage, List<Integer> nstream, JGrassRegion active, PrintStream out ) throws IOException {

        // get rows and cols from the active region
        int activecols = active.getCols();
        int activerows = active.getRows();

        int[] flow = new int[2];
        int[] flow_p = new int[2];

        CoordinateList coordlist = new CoordinateList();
        RandomIter m1RandomIter = RandomIterFactory.create(flowImage, null);
        RandomIter netNumRandomIter = RandomIterFactory.create(netNumImage, null);
        // GEOMETRY
        // creates new LineSting array
        LineString[] newGeometry = new LineString[nstream.size()];
        // creates a vector of geometry
        List<LineString> newGeometryVectorLine = new ArrayList<LineString>();
        GeometryFactory newfactory = new GeometryFactory();

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Extracting the network geometries...", nstream.size());
        for( int num = 1; num <= nstream.size(); num++ ) {
            for( int y = 0; y < activerows; y++ ) {
                for( int x = 0; x < activecols; x++ ) {
                    if (!isNovalue(m1RandomIter.getSampleDouble(x, y, 0))) {
                        flow[0] = x;
                        flow[1] = y;
                        // looks for the source
                        if (netNumRandomIter.getSampleDouble(x, y, 0) == num) {
                            // if the point is a source it starts to extract the
                            // channel...
                            if (FluidUtils.sourcesNet(m1RandomIter, flow, num, netNumRandomIter)) {
                                flow_p[0] = flow[0];
                                flow_p[1] = flow[1];
                                Coordinate coordSource = JGrassUtilities.rowColToCenterCoordinates(active, flow[1], flow[0]);
                                // creates new Object Coordinate... SOURCE
                                // POINT...
                                // adds the points to the CoordinateList
                                coordlist.add(coordSource);
                                if (!FluidUtils.go_downstream(flow, m1RandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                    return null;
                                // it extracts the other points of the
                                // channel... it
                                // continues until the next node...
                                while( !isNovalue(m1RandomIter.getSampleDouble(flow[0], flow[1], 0)) && m1RandomIter.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                        && netNumRandomIter.getSampleDouble(flow[0], flow[1], 0) == num && !isNovalue(netNumRandomIter.getSampleDouble(flow[0], flow[1], 0)) ) {
                                    Coordinate coordPoint = JGrassUtilities.rowColToCenterCoordinates(active, flow[1], flow[0]);
                                    // creates new Object Coordinate... CHANNEL
                                    // POINT...
                                    // adds new points to CoordinateList
                                    coordlist.add(coordPoint);
                                    flow_p[0] = flow[0];
                                    flow_p[1] = flow[1];
                                    if (!FluidUtils.go_downstream(flow, m1RandomIter.getSampleDouble(flow[0], flow[1], 0)))
                                        return null;
                                }
                                Coordinate coordNode = JGrassUtilities.rowColToCenterCoordinates(active, flow[1], flow[0]);
                                // creates new Object Coordinate... NODE
                                // POINT...
                                // adds new points to CoordinateList
                                coordlist.add(coordNode);
                            }
                        }
                    }
                }
            }
            // when the channel is complete creates one new geometry (new
            // channel of the network)
            newGeometry[num - 1] = newfactory.createLineString(coordlist.toCoordinateArray());
            // adds the new geometry to the vector of geometry
            newGeometryVectorLine.add(newGeometry[num - 1]);
            // it removes every element of coordlist
            coordlist.clear();
            pm.worked(1);
        }
        pm.done();

        // create the feature type
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        // set the name
        b.setName("network"); //$NON-NLS-1$
        // add a geometry property
        b.add("the_geom", LineString.class); //$NON-NLS-1$
        // build the type
        SimpleFeatureType type = b.buildFeatureType();
        // create the feature
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);

        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = FeatureCollections.newCollection();
        int index = 0;
        for( LineString lineString : newGeometryVectorLine ) {
            Object[] values = new Object[]{lineString};
            // add the values
            builder.addAll(values);
            // build the feature with provided ID
            SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + index); //$NON-NLS-1$
            index++;
            featureCollection.add(feature);
        }
        return featureCollection;
    }

    /**
     * It create the shapefile of channel network
     * 
     * @param flowIterator
     * @param netNumIterator
     * @param nstream
     * @param active
     * @param out
     * @return
     * @throws IOException
     */
    public List<MultiLineString> net2ShapeGeometries( RandomIter flowIterator, RandomIter netNumIterator, int[] nstream, JGrassRegion active, PrintStream out ) throws IOException {

        // get rows and cols from the active region
        int activecols = active.getCols();
        int activerows = active.getRows();
        int[] flow = new int[2];
        int[] flow_p = new int[2];

        CoordinateList coordlist = new CoordinateList();

        // GEOMETRY

        // creates a vector of geometry
        List<MultiLineString> newGeometryVectorLine = new ArrayList<MultiLineString>();
        GeometryFactory newfactory = new GeometryFactory();

        /* name of new geometry (polyline) */
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Extracting the network geometries...", nstream[0]);
        for( int num = 1; num <= nstream[0]; num++ ) {
            for( int y = 0; y < activerows; y++ ) {
                for( int x = 0; x < activecols; x++ ) {
                    flow[0] = x;
                    flow[1] = y;
                    // looks for the source
                    if (netNumIterator.getSampleDouble(x, y, 0) == num) {
                        // if the point is a source it starts to extract the
                        // channel...
                        if (FluidUtils.sourcesNet(flowIterator, flow, num, netNumIterator)) {
                            flow_p[0] = flow[0];
                            flow_p[1] = flow[1];
                            Coordinate coordSource = JGrassUtilities.rowColToCenterCoordinates(active, flow[1], flow[0]);
                            // creates new Object Coordinate... SOURCE POINT...
                            // adds the points to the CoordinateList
                            coordlist.add(coordSource);
                            if (!FluidUtils.go_downstream(flow, flowIterator.getSampleDouble(flow[0], flow[1], 0)))
                                return null;
                            // it extracts the other points of the channel... it
                            // continues until the next node...
                            while( !isNovalue(flowIterator.getSampleDouble(flow[0], flow[1], 0)) && flowIterator.getSampleDouble(flow[0], flow[1], 0) != 10.0
                                    && netNumIterator.getSampleDouble(flow[0], flow[1], 0) == num && !isNovalue(netNumIterator.getSampleDouble(flow[0], flow[1], 0)) ) {
                                Coordinate coordPoint = JGrassUtilities.rowColToCenterCoordinates(active, flow[1], flow[0]);
                                // creates new Object Coordinate... CHANNEL
                                // POINT...
                                // adds new points to CoordinateList
                                coordlist.add(coordPoint);
                                flow_p[0] = flow[0];
                                flow_p[1] = flow[1];
                                if (!FluidUtils.go_downstream(flow, flowIterator.getSampleDouble(flow[0], flow[1], 0)))
                                    return null;
                            }
                            Coordinate coordNode = JGrassUtilities.rowColToCenterCoordinates(active, flow[1], flow[0]);
                            // creates new Object Coordinate... NODE POINT...
                            // adds new points to CoordinateList
                            coordlist.add(coordNode);
                        }
                    }
                }
            }
            // when the channel is complete creates one new geometry (new
            // channel of the network)
            // adds the new geometry to the vector of geometry
            // if (!coordlist.isEmpty()) {
            newGeometryVectorLine.add(newfactory.createMultiLineString(new LineString[]{newfactory.createLineString(coordlist.toCoordinateArray())}));
            // } else {
            // if (out != null)
            // out.println("Found an empty geometry at " + num);
            // }
            // it removes every element of coordlist
            coordlist.clear();
            pm.worked(1);
        }
        pm.done();
        return newGeometryVectorLine;
    }
}
