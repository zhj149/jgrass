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
import eu.hydrologis.jgrass.models.h.netnumbering.h_netnumbering;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestNetnumbering1 extends JGrassTestCase{
    @SuppressWarnings("nls")
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
        h_netnumbering netnumbering = new h_netnumbering(out, err);
        // create the arguments for the models and initialize it.
        Argument[] netnumArgs = new Argument[9];
        netnumArgs[0] = new Argument("grassdb", global_grassdb, true);
        netnumArgs[1] = new Argument("location", global_location, true);
        netnumArgs[2] = new Argument("mapset", global_mapset, true);
        netnumArgs[3] = new Argument("time_start_up", global_startdate, true);
        netnumArgs[4] = new Argument("time_ending_up", global_enddate, true);
        netnumArgs[5] = new Argument("time_delta", global_deltat, true);
        netnumArgs[6] = new Argument("remotedburl", global_remotedb, true);
        netnumArgs[7] = new Argument("mode", "1", true);
        netnumArgs[8] = new Argument("thtca", "2.0", true);
        netnumbering.initialize(netnumArgs);

        // create the input "models" to link with the markoutlets models and its arguments
        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.mflowDataBorder);
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
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), netnumbering,
                netnumbering.getInputExchangeItem(0));
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
        igrass_link_net.connect(igrass_net, igrass_net.getOutputExchangeItem(0), netnumbering,
                netnumbering.getInputExchangeItem(1));
        assertTrue(linkMessage,igrass_link_flow.isConnected());
        
        DummyInputGrassCoverageMap igrass_tca = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.tcaData);
        Argument[] inputTcaArgs = new Argument[9];
        inputTcaArgs[0] = new Argument("igrass", "tca", true);
        inputTcaArgs[1] = new Argument("quantityid", "tca", true);
        inputTcaArgs[2] = new Argument("grassdb", global_grassdb, true);
        inputTcaArgs[3] = new Argument("location", global_location, true);
        inputTcaArgs[4] = new Argument("mapset", global_mapset, true);
        inputTcaArgs[5] = new Argument("time_start_up", global_startdate, true);
        inputTcaArgs[6] = new Argument("time_ending_up", global_enddate, true);
        inputTcaArgs[7] = new Argument("time_delta", global_deltat, true);
        inputTcaArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_tca.initialize(inputTcaArgs);

        Link igrass_link_tca = new Link(null, "tca");
        igrass_link_tca.connect(igrass_tca, igrass_tca.getOutputExchangeItem(0), netnumbering,
                netnumbering.getInputExchangeItem(2));
        assertTrue(linkMessage,igrass_link_tca.isConnected());
        
        
        
        
        OutputGrassCoverageWriter dummyOutEI = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "netnumber", true);
        dummyArguments[1] = new Argument("quantityid", "netnumber", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOutEI.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "netnumber");
        dummyOutLink.connect(netnumbering, netnumbering.getOutputExchangeItem(0), dummyOutEI, dummyOutEI
                .getInputExchangeItem(0));
        
        OutputGrassCoverageWriter dummyBasinOutEI = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyBasinArguments = new Argument[9];
        dummyBasinArguments[0] = new Argument("ograss", "basin", true);
        dummyBasinArguments[1] = new Argument("quantityid", "basin", true);
        dummyBasinArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyBasinArguments[3] = new Argument("location", global_location, true);
        dummyBasinArguments[4] = new Argument("mapset", global_mapset, true);
        dummyBasinArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyBasinArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyBasinArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyBasinArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyBasinOutEI.initialize(dummyBasinArguments);

        Link dummyBasinOutLink = new Link(null, "basin");
        dummyBasinOutLink.connect(netnumbering, netnumbering.getOutputExchangeItem(1), dummyBasinOutEI, dummyBasinOutEI
                .getInputExchangeItem(0));

        assertTrue(linkMessage,dummyBasinOutLink.isConnected());

        igrass_flow.prepare();
        igrass_net.prepare();
        netnumbering.prepare();
        IValueSet valueSet = netnumbering.getValues(null, dummyOutLink.getID());
        IValueSet valueSet1 = netnumbering.getValues(null, dummyBasinOutLink.getID());

        igrass_flow.finish();
        igrass_net.finish();
        netnumbering.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();
        // compare the result
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.netNumberingChannelDataNN1, 0);
        rasterData = ((JGrassGridCoverageValueSet) valueSet1).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.basinDataNN1, 0);
        
        
        
        // reset the active region
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);

    }
        
        
}

