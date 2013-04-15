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
import eu.hydrologis.jgrass.models.h.sumdownstream.h_sumdownstream;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestSumdownstream extends JGrassTestCase{
    
    public void testSumDownStream() throws IOException {

        JGrassMapEnvironment jGrassMapEnvironment = GrassMapTest.jME;

        // set active region to the needed
        JGrassRegion fileRegion = jGrassMapEnvironment.getFileRegion();
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(), fileRegion);

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
        h_sumdownstream sumdownstream = new h_sumdownstream(out, err);
        // create the arguments for the models and initialize it.
        Argument[] sumdownstreamArgs = new Argument[8];
        sumdownstreamArgs[0] = new Argument("grassdb", global_grassdb, true);
        sumdownstreamArgs[1] = new Argument("location", global_location, true);
        sumdownstreamArgs[2] = new Argument("mapset", global_mapset, true);
        sumdownstreamArgs[3] = new Argument("time_start_up", global_startdate, true);
        sumdownstreamArgs[4] = new Argument("time_ending_up", global_enddate, true);
        sumdownstreamArgs[5] = new Argument("time_delta", global_deltat, true);
        sumdownstreamArgs[6] = new Argument("remotedburl", global_remotedb, true);
        sumdownstreamArgs[7] = new Argument("mode", "1", true);

        sumdownstream.initialize(sumdownstreamArgs);

        // create the input "models" to link with the markoutlets models and its arguments
        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err, GrassMapTest.mflowDataBorder);
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
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), sumdownstream, sumdownstream
                .getInputExchangeItem(1));
        assertTrue(linkMessage, igrass_link_flow.isConnected());
        // create the output "models" to link with the markoutlets models and its arguments

        double[][] summ = new double[][]{{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},};

        DummyInputGrassCoverageMap igrass_quantity = new DummyInputGrassCoverageMap(out, err, summ);
        Argument[] inputQuantityArgs = new Argument[9];
        inputQuantityArgs[0] = new Argument("igrass", "maptosum", true);
        inputQuantityArgs[1] = new Argument("quantityid", "maptosum", true);
        inputQuantityArgs[2] = new Argument("grassdb", global_grassdb, true);
        inputQuantityArgs[3] = new Argument("location", global_location, true);
        inputQuantityArgs[4] = new Argument("mapset", global_mapset, true);
        inputQuantityArgs[5] = new Argument("time_start_up", global_startdate, true);
        inputQuantityArgs[6] = new Argument("time_ending_up", global_enddate, true);
        inputQuantityArgs[7] = new Argument("time_delta", global_deltat, true);
        inputQuantityArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_quantity.initialize(inputQuantityArgs);

        Link igrass_link_quantity = new Link(null, "maptosum");
        igrass_link_quantity.connect(igrass_quantity, igrass_quantity.getOutputExchangeItem(0), sumdownstream, sumdownstream
                .getInputExchangeItem(0));
        assertTrue(linkMessage, igrass_link_flow.isConnected());

        OutputGrassCoverageWriter dummyOutEI = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "summ", true);
        dummyArguments[1] = new Argument("quantityid", "summ", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOutEI.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "summ");
        dummyOutLink.connect(sumdownstream, sumdownstream.getOutputExchangeItem(0), dummyOutEI, dummyOutEI
                .getInputExchangeItem(0));

        assertTrue(linkMessage, dummyOutLink.isConnected());

        igrass_flow.prepare();
        sumdownstream.prepare();
        IValueSet valueSet = sumdownstream.getValues(null, dummyOutLink.getID());
        igrass_flow.finish();
        sumdownstream.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();
        // compare the result
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.tcaData, 0);

        // reset the active region
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(), activeRegion);

    }

}
