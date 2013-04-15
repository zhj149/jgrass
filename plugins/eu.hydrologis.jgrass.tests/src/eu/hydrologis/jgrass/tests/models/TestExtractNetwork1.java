package eu.hydrologis.jgrass.tests.models;

import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.extractnetwork.h_extractnetwork;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.jgrass.utilitylinkables.OutputShapeWriter;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
/**
 * It test the h_extractNetwork classes with mode=1.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestExtractNetwork1 extends JGrassTestCase{
    @SuppressWarnings("nls")
    public void testExtractNetworks1() throws IOException {
        JGrassMapEnvironment jGrassMapEnvironment = GrassMapTest.jME;

        // set active region to the needed
        JGrassRegion fileRegion = jGrassMapEnvironment.getFileRegion();
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                fileRegion);
        
        PrintStream out = System.out;
        PrintStream err = System.err;

        String global_grassdb = jGrassMapEnvironment.getLOCATION().getParent();
        String global_location = jGrassMapEnvironment.getLOCATION().getName();
        String global_mapset = jGrassMapEnvironment.getMAPSET().getName();
        String global_startdate = "null";
        String global_enddate = "null";
        String global_deltat = "-1";
        String global_remotedb = "null";
        h_extractnetwork extractNetwork = new h_extractnetwork(out, err);
        Argument[] extractArgs = new Argument[9];
        extractArgs[0] = new Argument("grassdb", global_grassdb, true);
        extractArgs[1] = new Argument("location", global_location, true);
        extractArgs[2] = new Argument("mapset", global_mapset, true);
        extractArgs[3] = new Argument("time_start_up", global_startdate, true);
        extractArgs[4] = new Argument("time_ending_up", global_enddate, true);
        extractArgs[5] = new Argument("time_delta", global_deltat, true);
        extractArgs[6] = new Argument("remotedburl", global_remotedb, true);
        extractArgs[7] = new Argument("mode", "1", true);
        extractArgs[8] = new Argument("threshold", "10", true);

        extractNetwork.initialize(extractArgs);

        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err, GrassMapTest.mflowDataBorder);
        Argument[] flowArgs = new Argument[9];
        flowArgs[0] = new Argument("igrass", "flow", true);
        flowArgs[1] = new Argument("quantityid", "flow", true);
        flowArgs[2] = new Argument("grassdb", global_grassdb, true);
        flowArgs[3] = new Argument("location", global_location, true);
        flowArgs[4] = new Argument("mapset", global_mapset, true);
        flowArgs[5] = new Argument("time_start_up", global_startdate, true);
        flowArgs[6] = new Argument("time_ending_up", global_enddate, true);
        flowArgs[7] = new Argument("time_delta", global_deltat, true);
        flowArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_flow.initialize(flowArgs);

        Link igrass_link_flow = new Link(null, "flow");
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0),
                extractNetwork, extractNetwork.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_tca = new DummyInputGrassCoverageMap(out, err, GrassMapTest.tcaData);
        Argument[] tcaArgs = new Argument[9];
        tcaArgs[0] = new Argument("igrass", "tca", true);
        tcaArgs[1] = new Argument("quantityid", "tca", true);
        tcaArgs[2] = new Argument("grassdb", global_grassdb, true);
        tcaArgs[3] = new Argument("location", global_location, true);
        tcaArgs[4] = new Argument("mapset", global_mapset, true);
        tcaArgs[5] = new Argument("time_start_up", global_startdate, true);
        tcaArgs[6] = new Argument("time_ending_up", global_enddate, true);
        tcaArgs[7] = new Argument("time_delta", global_deltat, true);
        tcaArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_tca.initialize(tcaArgs);

        Link igrass_link_tca = new Link(null, "tca");
        igrass_link_tca.connect(igrass_tca, igrass_tca.getOutputExchangeItem(0),
                extractNetwork, extractNetwork.getInputExchangeItem(1));

        
        DummyInputGrassCoverageMap igrass_slope = new DummyInputGrassCoverageMap(out, err, GrassMapTest.slopeData);
        Argument[] slopeArgs = new Argument[9];
        slopeArgs[0] = new Argument("igrass", "slope", true);
        slopeArgs[1] = new Argument("quantityid", "slope", true);
        slopeArgs[2] = new Argument("grassdb", global_grassdb, true);
        slopeArgs[3] = new Argument("location", global_location, true);
        slopeArgs[4] = new Argument("mapset", global_mapset, true);
        slopeArgs[5] = new Argument("time_start_up", global_startdate, true);
        slopeArgs[6] = new Argument("time_ending_up", global_enddate, true);
        slopeArgs[7] = new Argument("time_delta", global_deltat, true);
        slopeArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_slope.initialize(slopeArgs);

        Link igrass_link_slope = new Link(null, "slope");
        igrass_link_slope.connect(igrass_slope, igrass_slope.getOutputExchangeItem(0),
                extractNetwork, extractNetwork.getInputExchangeItem(2));
        
        
        
        OutputGrassCoverageWriter net = new OutputGrassCoverageWriter(out, err);
        Argument[] netArguments = new Argument[9];
        netArguments[0] = new Argument("ograss", "net", true);
        netArguments[1] = new Argument("quantityid", "net", true);
        netArguments[2] = new Argument("grassdb", global_grassdb, true);
        netArguments[3] = new Argument("location", global_location, true);
        netArguments[4] = new Argument("mapset", global_mapset, true);
        netArguments[5] = new Argument("time_start_up", global_startdate, true);
        netArguments[6] = new Argument("time_ending_up", global_enddate, true);
        netArguments[7] = new Argument("time_delta", global_deltat, true);
        netArguments[8] = new Argument("remotedburl", global_remotedb, true);
        net.initialize(netArguments);

        Link netOutLink = new Link(null, "net");
        netOutLink.connect(extractNetwork, extractNetwork.getOutputExchangeItem(0), net, net
                .getInputExchangeItem(0));

        
        OutputShapeWriter shapeOut = new OutputShapeWriter(out, err);
        Argument[] shapeoutArguments = new Argument[9];
        shapeoutArguments[0] = new Argument("ograss", "netshape", true);
        shapeoutArguments[1] = new Argument("quantityid", "netshape", true);
        shapeoutArguments[2] = new Argument("grassdb", global_grassdb, true);
        shapeoutArguments[3] = new Argument("location", global_location, true);
        shapeoutArguments[4] = new Argument("mapset", global_mapset, true);
        shapeoutArguments[5] = new Argument("time_start_up", global_startdate, true);
        shapeoutArguments[6] = new Argument("time_ending_up", global_enddate, true);
        shapeoutArguments[7] = new Argument("time_delta", global_deltat, true);
        shapeoutArguments[8] = new Argument("remotedburl", global_remotedb, true);
        shapeOut.initialize(shapeoutArguments);
        
        Link shapeOutLink = new Link(null, "netshape");
        shapeOutLink.connect(extractNetwork, extractNetwork.getOutputExchangeItem(1), shapeOut, shapeOut
                .getInputExchangeItem(0));

        igrass_flow.prepare();
        igrass_tca.prepare();
        igrass_slope.prepare();

        extractNetwork.prepare();

        IValueSet netValueSet = extractNetwork.getValues(null, netOutLink.getID());
        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) netValueSet).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.extractNet1Data, 0.01);
        
        
        IValueSet shapeValueSet = extractNetwork.getValues(null, shapeOutLink.getID());
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = ((JGrassFeatureValueSet)shapeValueSet).getFeatureCollection();
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        while( featureIterator.hasNext() ) {
            SimpleFeature feature = featureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            Coordinate[] coordinates = geometry.getCoordinates();
            System.out.println("Coords of feature: " + feature.getID());
            for( Coordinate coordinate : coordinates ) {
                System.out.println(coordinate);
            }
        }
        
        // set active region to the needed
        igrass_flow.finish();
        igrass_slope.finish();
        igrass_tca.finish();
        extractNetwork.finish();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }

}
