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
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.flow.h_flow;
import eu.hydrologis.jgrass.models.h.rescaleddistance.h_rescaleddistance;
import eu.hydrologis.jgrass.models.h.rescaleddistance3d.h_rescaleddistance3d;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

/**
 * Test RescaledDistance3d.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestRescaledDistance3d extends JGrassTestCase {

    @SuppressWarnings("nls")
    public void testRescaledDistance3d() throws IOException {
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
        h_rescaleddistance3d rescaledDistance3d = new h_rescaleddistance3d(out, err);
        Argument[] rescaled3dArgs = new Argument[8];
        rescaled3dArgs[0] = new Argument("grassdb", global_grassdb, true);
        rescaled3dArgs[1] = new Argument("location", global_location, true);
        rescaled3dArgs[2] = new Argument("mapset", global_mapset, true);
        rescaled3dArgs[3] = new Argument("time_start_up", global_startdate, true);
        rescaled3dArgs[4] = new Argument("time_ending_up", global_enddate, true);
        rescaled3dArgs[5] = new Argument("time_delta", global_deltat, true);
        rescaled3dArgs[6] = new Argument("remotedburl", global_remotedb, true);
        rescaled3dArgs[7] = new Argument("number", "0.3", true);
        rescaledDistance3d.initialize(rescaled3dArgs);
        
        DummyInputGrassCoverageMap igrass_pit = new DummyInputGrassCoverageMap(out, err, GrassMapTest.pitData);
        Argument[] pitArgs = new Argument[9];
        pitArgs[0] = new Argument("igrass", "pit", true);
        pitArgs[1] = new Argument("quantityid", "pit", true);
        pitArgs[2] = new Argument("grassdb", global_grassdb, true);
        pitArgs[3] = new Argument("location", global_location, true);
        pitArgs[4] = new Argument("mapset", global_mapset, true);
        pitArgs[5] = new Argument("time_start_up", global_startdate, true);
        pitArgs[6] = new Argument("time_ending_up", global_enddate, true);
        pitArgs[7] = new Argument("time_delta", global_deltat, true);
        pitArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_pit.initialize(pitArgs);
        
        Link igrass_link_pit = new Link(null, "pit");
        igrass_link_pit.connect(igrass_pit, igrass_pit.getOutputExchangeItem(0),
                rescaledDistance3d, rescaledDistance3d.getInputExchangeItem(2));

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
                rescaledDistance3d, rescaledDistance3d.getInputExchangeItem(1));

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
                rescaledDistance3d, rescaledDistance3d.getInputExchangeItem(0));

        OutputGrassCoverageWriter dummyOut = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "rdist3d", true);
        dummyArguments[1] = new Argument("quantityid", "rdist3d", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOut.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "rdist3d");
        dummyOutLink.connect(rescaledDistance3d, rescaledDistance3d.getOutputExchangeItem(0), dummyOut, dummyOut
                .getInputExchangeItem(0));

        assertTrue(dummyOutLink.isConnected());

        igrass_net.prepare();
        rescaledDistance3d.prepare();
        IValueSet valueSet = rescaledDistance3d.getValues(null, dummyOutLink.getID());
        igrass_net.finish();
        rescaledDistance3d.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.rescaledDistance3dData,0);

        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }



}
