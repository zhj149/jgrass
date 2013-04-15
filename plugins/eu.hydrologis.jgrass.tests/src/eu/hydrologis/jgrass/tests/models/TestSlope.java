package eu.hydrologis.jgrass.tests.models;

import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.junit.Test;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.slope.h_slope;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
/**
 * It is a test to validate the h.slope model.
 * <p>
 * It compare the value estimates with the model and the value calculates by
 * hand. Next step is to compare two map, which are the result of the trunk of
 * JGrass and the stable version of JGrass. This is an OpenMi complaint models,
 * so the test reproduce an OpenMi chain (request the value==>calculate==>read)
 * </p>
 * 
 * @author daniele
 * @since 1.1.0
 */

public class TestSlope extends JGrassTestCase {

    @Test
    @SuppressWarnings("nls")
    public void testSlope() throws IOException {
        JGrassMapEnvironment jGrassMapEnvironment = GrassMapTest.jME;

        // set active region to the needed
        JGrassRegion fileRegion = jGrassMapEnvironment
                .getFileRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment
                .getMAPSET().getAbsolutePath(), fileRegion);

        PrintStream out = System.out;
        PrintStream err = System.err;

        String global_grassdb = jGrassMapEnvironment.getLOCATION()
                .getParent();
        String global_location = jGrassMapEnvironment.getLOCATION()
                .getName();
        String global_mapset = jGrassMapEnvironment.getMAPSET()
                .getName();
        String global_startdate = "null";
        String global_enddate = "null";
        String global_deltat = "-1";
        String global_remotedb = "null";
        // instantiate the models and create the arguments.
        h_slope slope = new h_slope(out, err);
        Argument[] slopeArgs = new Argument[7];
        slopeArgs[0] = new Argument("grassdb", global_grassdb, true);
        slopeArgs[1] = new Argument("location", global_location, true);
        slopeArgs[2] = new Argument("mapset", global_mapset, true);
        slopeArgs[3] = new Argument("time_start_up",
                global_startdate, true);
        slopeArgs[4] = new Argument("time_ending_up", global_enddate,
                true);
        slopeArgs[5] = new Argument("time_delta", global_deltat, true);
        slopeArgs[6] = new Argument("remotedburl", global_remotedb,
                true);
        slope.initialize(slopeArgs);
        /*
         * Create the input "models" to link with the slope models and its
         * arguments. The first one (which is the link with index equals to 0 in
         * the models is the depitted map).
         */

        DummyInputGrassCoverageMap igrass_pit = new DummyInputGrassCoverageMap(
                out, err, GrassMapTest.pitData);
        Argument[] inputArgsPit = new Argument[9];
        inputArgsPit[0] = new Argument("igrass", "pit", true);
        inputArgsPit[1] = new Argument("quantityid", "pit", true);
        inputArgsPit[2] = new Argument("grassdb", global_grassdb,
                true);
        inputArgsPit[3] = new Argument("location", global_location,
                true);
        inputArgsPit[4] = new Argument("mapset", global_mapset, true);
        inputArgsPit[5] = new Argument("time_start_up",
                global_startdate, true);
        inputArgsPit[6] = new Argument("time_ending_up",
                global_enddate, true);
        inputArgsPit[7] = new Argument("time_delta", global_deltat,
                true);
        inputArgsPit[8] = new Argument("remotedburl",
                global_remotedb, true);
        igrass_pit.initialize(inputArgsPit);

        Link igrass_link_pit = new Link(null, "pit");
        igrass_link_pit.connect(igrass_pit, igrass_pit
                .getOutputExchangeItem(0), slope, slope
                .getInputExchangeItem(0));
        assertTrue(igrass_link_pit.isConnected());

        /*
         * Create the input "models" to link with the slope models and its
         * arguments. The first one (which is the link with index equals to 1 in
         * the models is the flow map).
         */
        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(
                out, err, GrassMapTest.flowData);
        Argument[] inputArgsFlow = new Argument[9];
        inputArgsFlow[0] = new Argument("igrass", "flow", true);
        inputArgsFlow[1] = new Argument("quantityid", "flow", true);
        inputArgsFlow[2] = new Argument("grassdb", global_grassdb,
                true);
        inputArgsFlow[3] = new Argument("location", global_location,
                true);
        inputArgsFlow[4] = new Argument("mapset", global_mapset, true);
        inputArgsFlow[5] = new Argument("time_start_up",
                global_startdate, true);
        inputArgsFlow[6] = new Argument("time_ending_up",
                global_enddate, true);
        inputArgsFlow[7] = new Argument("time_delta", global_deltat,
                true);
        inputArgsFlow[8] = new Argument("remotedburl",
                global_remotedb, true);
        igrass_flow.initialize(inputArgsFlow);

        Link igrass_link_flow = new Link(null, "flow");
        igrass_link_flow.connect(igrass_flow, igrass_flow
                .getOutputExchangeItem(0), slope, slope
                .getInputExchangeItem(1));
        assertTrue(igrass_link_flow.isConnected());

        // create the output "models" to link with the slope models and its
        // arguments

        OutputGrassCoverageWriter dummyOutEI = new OutputGrassCoverageWriter(
                out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "slope", true);
        dummyArguments[1] = new Argument("quantityid", "slope", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb,
                true);
        dummyArguments[3] = new Argument("location", global_location,
                true);
        dummyArguments[4] = new Argument("mapset", global_mapset,
                true);
        dummyArguments[5] = new Argument("time_start_up",
                global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up",
                global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat,
                true);
        dummyArguments[8] = new Argument("remotedburl",
                global_remotedb, true);
        dummyOutEI.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "slope");
        dummyOutLink.connect(slope, slope.getOutputExchangeItem(0),
                dummyOutEI, dummyOutEI.getInputExchangeItem(0));

        assertTrue(dummyOutLink.isConnected());

        igrass_pit.prepare();
        igrass_flow.prepare();
        slope.prepare();
        IValueSet valueSet = slope.getValues(null, dummyOutLink
                .getID());
        igrass_pit.finish();
        igrass_flow.finish();
        slope.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet)
                .getGridCoverage2D();

        checkMatrixEqual(rasterData.getRenderedImage(),
                GrassMapTest.slopeData, 0.01);

    }

}
