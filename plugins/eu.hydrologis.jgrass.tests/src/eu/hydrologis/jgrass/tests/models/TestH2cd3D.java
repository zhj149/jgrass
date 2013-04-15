package eu.hydrologis.jgrass.tests.models;

import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.h2cd3d.h_h2cd3d;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestH2cd3D extends JGrassTestCase{
    public void testH2cd3D() throws IOException {
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
        h_h2cd3d h2cd = new h_h2cd3d(out, err);
        Argument[] topindexArgs = new Argument[7];
        topindexArgs[0] = new Argument("grassdb", global_grassdb, true);
        topindexArgs[1] = new Argument("location", global_location, true);
        topindexArgs[2] = new Argument("mapset", global_mapset, true);
        topindexArgs[3] = new Argument("time_start_up", global_startdate, true);
        topindexArgs[4] = new Argument("time_ending_up", global_enddate, true);
        topindexArgs[5] = new Argument("time_delta", global_deltat, true);
        topindexArgs[6] = new Argument("remotedburl", global_remotedb, true);

        h2cd.initialize(topindexArgs);

        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.flowData);
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
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), h2cd,
                h2cd.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_net = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.extractNet1Data);
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

        Link igrass_link_tca = new Link(null, "net");
        igrass_link_tca.connect(igrass_net, igrass_net.getOutputExchangeItem(0), h2cd, h2cd
                .getInputExchangeItem(2));

        DummyInputGrassCoverageMap igrass_pit = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.pitData);
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
        igrass_link_pit.connect(igrass_pit, igrass_pit.getOutputExchangeItem(0), h2cd, h2cd
                .getInputExchangeItem(1));
        
        
        
        OutputGrassCoverageWriter ograss_dist = new OutputGrassCoverageWriter(out, err);
        Argument[] distArguments = new Argument[9];
        distArguments[0] = new Argument("ograss", "h2cd3d", true);
        distArguments[1] = new Argument("quantityid", "h2cd3d", true);
        distArguments[2] = new Argument("grassdb", global_grassdb, true);
        distArguments[3] = new Argument("location", global_location, true);
        distArguments[4] = new Argument("mapset", global_mapset, true);
        distArguments[5] = new Argument("time_start_up", global_startdate, true);
        distArguments[6] = new Argument("time_ending_up", global_enddate, true);
        distArguments[7] = new Argument("time_delta", global_deltat, true);
        distArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_dist.initialize(distArguments);

        Link ograss_link_h2cd = new Link(null, "h2cd3d");
        ograss_link_h2cd.connect(h2cd, h2cd.getOutputExchangeItem(0), ograss_dist, ograss_dist
                .getInputExchangeItem(0));

        igrass_flow.prepare();
        igrass_net.prepare();
        igrass_pit.prepare();
        h2cd.prepare();
        String linkMessage = "there is a prblem in a link";

        assertTrue(linkMessage, igrass_link_flow.isConnected());
        assertTrue(linkMessage, igrass_link_tca.isConnected());

        assertTrue(linkMessage, ograss_link_h2cd.isConnected());

        IValueSet netValueSet = h2cd.getValues(null, ograss_link_h2cd.getID());
        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) netValueSet).getGridCoverage2D();

        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.h2cd3dData, 0.01);

        igrass_flow.finish();
        igrass_net.finish();
        h2cd.finish();
        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }
    
    
    
}
