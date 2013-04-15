package eu.hydrologis.jgrass.tests.models;

import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.strahler.h_strahler;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

public class TestSrahler extends JGrassTestCase{
    public void testNetNumbering() throws IOException {
        
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

        // instantiate the models
        h_strahler strahler = new h_strahler(out, err);
        // create the arguments for the models and initialize it.
        Argument[] strahlerArgs = new Argument[8];
        strahlerArgs[0] = new Argument("grassdb", global_grassdb, true);
        strahlerArgs[1] = new Argument("location", global_location, true);
        strahlerArgs[2] = new Argument("mapset", global_mapset, true);
        strahlerArgs[3] = new Argument("time_start_up", global_startdate, true);
        strahlerArgs[4] = new Argument("time_ending_up", global_enddate, true);
        strahlerArgs[5] = new Argument("time_delta", global_deltat, true);
        strahlerArgs[6] = new Argument("remotedburl", global_remotedb, true);
        strahlerArgs[7] = new Argument("mode", "1", true);

        strahler.initialize(strahlerArgs);

        // create the input "models" to link with the markoutlets models and its arguments
        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.mflowDataBorder);
        Argument[] inputArgs = new Argument[9];
        inputArgs[0] = new Argument("igrass", "flow", true);
        inputArgs[1] = new Argument("quantityid", "flow", true);
        inputArgs[2] = new Argument("grassdb", global_grassdb, true);
        inputArgs[3] = new Argument("location", global_location, true);
        inputArgs[4] = new Argument("mapset", global_mapset, true);
        inputArgs[5] = new Argument("time_start_up", global_startdate, true);
        inputArgs[6] = new Argument("time_ending_up", global_enddate, true);
        inputArgs[7] = new Argument("time_delta", global_deltat, true);
        inputArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_flow.initialize(inputArgs);

        Link igrass_link_flow = new Link(null, "flow");
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), strahler,
                strahler.getInputExchangeItem(0));
        assertTrue(linkMessage,igrass_link_flow.isConnected());
        // create the output "models" to link with the markoutlets models and its arguments

        DummyInputGrassCoverageMap igrass_net = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.extractNet1Data);
        Argument[] inputNetArgs = new Argument[9];
        inputNetArgs[0] = new Argument("igrass", "net", true);
        inputNetArgs[1] = new Argument("quantityid", "net", true);
        inputNetArgs[2] = new Argument("grassdb", global_grassdb, true);
        inputNetArgs[3] = new Argument("location", global_location, true);
        inputNetArgs[4] = new Argument("mapset", global_mapset, true);
        inputNetArgs[5] = new Argument("time_start_up", global_startdate, true);
        inputNetArgs[6] = new Argument("time_ending_up", global_enddate, true);
        inputNetArgs[7] = new Argument("time_delta", global_deltat, true);
        inputNetArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_net.initialize(inputNetArgs);

        Link igrass_link_net = new Link(null, "net");
        igrass_link_net.connect(igrass_net, igrass_net.getOutputExchangeItem(0), strahler,
                strahler.getInputExchangeItem(1));
        assertTrue(linkMessage,igrass_link_flow.isConnected());
        
        
        
        
        OutputGrassCoverageWriter dummyOutEI = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "strahler", true);
        dummyArguments[1] = new Argument("quantityid", "strahler", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOutEI.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "strahler");
        dummyOutLink.connect(strahler, strahler.getOutputExchangeItem(0), dummyOutEI, dummyOutEI
                .getInputExchangeItem(0));


        assertTrue(linkMessage,dummyOutLink.isConnected());

        igrass_flow.prepare();
        igrass_net.prepare();
        strahler.prepare();
        IValueSet valueSet = strahler.getValues(null, dummyOutLink.getID());
        igrass_flow.finish();
        igrass_net.finish();
        strahler.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();
        // compare the result
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.strahlerData, 0);

        // reset the active region
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);

    }
}
