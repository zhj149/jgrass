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
import eu.hydrologis.jgrass.models.h.netdif.h_netdif;
import eu.hydrologis.jgrass.models.h.rescaleddistance3d.h_rescaleddistance3d;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * Test Netdif.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestNetdif extends JGrassTestCase {

    @SuppressWarnings("nls")
    public void testNetdif() throws IOException {
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
        
        h_netdif netdif = new h_netdif(out, err);
        Argument[] netdifArgs = new Argument[8];
        netdifArgs[0] = new Argument("grassdb", global_grassdb, true);
        netdifArgs[1] = new Argument("location", global_location, true);
        netdifArgs[2] = new Argument("mapset", global_mapset, true);
        netdifArgs[3] = new Argument("time_start_up", global_startdate, true);
        netdifArgs[4] = new Argument("time_ending_up", global_enddate, true);
        netdifArgs[5] = new Argument("time_delta", global_deltat, true);
        netdifArgs[6] = new Argument("remotedburl", global_remotedb, true);
        netdifArgs[7] = new Argument("number", "0.3", true);
        netdif.initialize(netdifArgs);
        
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
                netdif, netdif.getInputExchangeItem(0));
        
        DummyInputGrassCoverageMap igrass_stream = new DummyInputGrassCoverageMap(out, err, GrassMapTest.strahlerData);
        Argument[] streamArgs = new Argument[9];
        streamArgs[0] = new Argument("igrass", "stream", true);
        streamArgs[1] = new Argument("quantityid", "stream", true);
        streamArgs[2] = new Argument("grassdb", global_grassdb, true);
        streamArgs[3] = new Argument("location", global_location, true);
        streamArgs[4] = new Argument("mapset", global_mapset, true);
        streamArgs[5] = new Argument("time_start_up", global_startdate, true);
        streamArgs[6] = new Argument("time_ending_up", global_enddate, true);
        streamArgs[7] = new Argument("time_delta", global_deltat, true);
        streamArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_stream.initialize(streamArgs);
        
        Link igrass_link_stream = new Link(null, "stream");
        igrass_link_stream.connect(igrass_stream, igrass_stream.getOutputExchangeItem(0),
                netdif, netdif.getInputExchangeItem(1));


        DummyInputGrassCoverageMap igrass_mapdiff = new DummyInputGrassCoverageMap(out, err, GrassMapTest.pitData);
        Argument[] mapdiffArgs = new Argument[9];
        mapdiffArgs[0] = new Argument("igrass", "mapdiff", true);
        mapdiffArgs[1] = new Argument("quantityid", "mapdiff", true);
        mapdiffArgs[2] = new Argument("grassdb", global_grassdb, true);
        mapdiffArgs[3] = new Argument("location", global_location, true);
        mapdiffArgs[4] = new Argument("mapset", global_mapset, true);
        mapdiffArgs[5] = new Argument("time_start_up", global_startdate, true);
        mapdiffArgs[6] = new Argument("time_ending_up", global_enddate, true);
        mapdiffArgs[7] = new Argument("time_delta", global_deltat, true);
        mapdiffArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_mapdiff.initialize(mapdiffArgs);
        
        Link igrass_link_mapdiff = new Link(null, "mapdiff");
        igrass_link_mapdiff.connect(igrass_mapdiff, igrass_mapdiff.getOutputExchangeItem(0),
                netdif, netdif.getInputExchangeItem(2));

        OutputGrassCoverageWriter dummyOut = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "diff", true);
        dummyArguments[1] = new Argument("quantityid", "diff", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOut.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "diff");
        dummyOutLink.connect(netdif, netdif.getOutputExchangeItem(0), dummyOut, dummyOut
                .getInputExchangeItem(0));

        assertTrue(dummyOutLink.isConnected());

        igrass_mapdiff.prepare();
        netdif.prepare();
        IValueSet valueSet = netdif.getValues(null, dummyOutLink.getID());
        igrass_mapdiff.finish();
        netdif.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.diff_forPit,0);

        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }



}
