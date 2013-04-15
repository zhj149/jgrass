package eu.hydrologis.jgrass.tests.models;

import java.io.IOException;
import java.io.PrintStream;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.rescaleddistance.h_rescaleddistance;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * Test RescaledDistance.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestRescaledDistance extends JGrassTestCase {

    @SuppressWarnings("nls")
    public void testRescaledDistance() throws IOException {
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
        h_rescaleddistance rescaledDistance = new h_rescaleddistance(out, err);
        Argument[] rescaledArgs = new Argument[8];
        rescaledArgs[0] = new Argument("grassdb", global_grassdb, true);
        rescaledArgs[1] = new Argument("location", global_location, true);
        rescaledArgs[2] = new Argument("mapset", global_mapset, true);
        rescaledArgs[3] = new Argument("time_start_up", global_startdate, true);
        rescaledArgs[4] = new Argument("time_ending_up", global_enddate, true);
        rescaledArgs[5] = new Argument("time_delta", global_deltat, true);
        rescaledArgs[6] = new Argument("remotedburl", global_remotedb, true);
        rescaledArgs[7] = new Argument("number", "0.3", true);
        rescaledDistance.initialize(rescaledArgs);

        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err, GrassMapTest.flowData);
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
                rescaledDistance, rescaledDistance.getInputExchangeItem(1));

        DummyInputGrassCoverageMap igrass_net = new DummyInputGrassCoverageMap(out, err, GrassMapTest.extractNet0Data);
        Argument[] netArgs = new Argument[9];
        netArgs[0] = new Argument("igrass", "net", true);
        netArgs[1] = new Argument("quantityid", "net", true);
        netArgs[2] = new Argument("grassdb", global_grassdb, true);
        netArgs[3] = new Argument("location", global_location, true);
        netArgs[4] = new Argument("mapset", global_mapset, true);
        netArgs[5] = new Argument("time_start_up", global_startdate, true);
        netArgs[6] = new Argument("time_ending_up", global_enddate, true);
        netArgs[7] = new Argument("time_delta", global_deltat, true);
        netArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_net.initialize(netArgs);
        
        Link igrass_link_net = new Link(null, "net");
        igrass_link_net.connect(igrass_net, igrass_net.getOutputExchangeItem(0),
                rescaledDistance, rescaledDistance.getInputExchangeItem(0));

        OutputGrassCoverageWriter dummyOut = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "rdist", true);
        dummyArguments[1] = new Argument("quantityid", "rdist", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOut.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "rdist");
        dummyOutLink.connect(rescaledDistance, rescaledDistance.getOutputExchangeItem(0), dummyOut, dummyOut
                .getInputExchangeItem(0));

        assertTrue(dummyOutLink.isConnected());

        igrass_net.prepare();
        rescaledDistance.prepare();
        IValueSet valueSet = rescaledDistance.getValues(null, dummyOutLink.getID());
        igrass_net.finish();
        rescaledDistance.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.rescaledDistanceData,0.1);

        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }



}
