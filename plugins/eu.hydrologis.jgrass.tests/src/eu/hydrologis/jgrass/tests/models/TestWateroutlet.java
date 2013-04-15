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
import eu.hydrologis.jgrass.models.h.wateroutlet.h_wateroutlet;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * Test Wateroutlet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestWateroutlet extends JGrassTestCase {

    @SuppressWarnings("nls")
    public void testWateroutlet() throws IOException {
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
        
        h_wateroutlet wateroutlet = new h_wateroutlet(out, err);
        Argument[] wateroutletArgs = new Argument[9];
        wateroutletArgs[0] = new Argument("grassdb", global_grassdb, true);
        wateroutletArgs[1] = new Argument("location", global_location, true);
        wateroutletArgs[2] = new Argument("mapset", global_mapset, true);
        wateroutletArgs[3] = new Argument("time_start_up", global_startdate, true);
        wateroutletArgs[4] = new Argument("time_ending_up", global_enddate, true);
        wateroutletArgs[5] = new Argument("time_delta", global_deltat, true);
        wateroutletArgs[6] = new Argument("remotedburl", global_remotedb, true);
        wateroutletArgs[7] = new Argument("north", "5139885", true);
        wateroutletArgs[8] = new Argument("east", "1640724", true);
        wateroutlet.initialize(wateroutletArgs);
        
        DummyInputGrassCoverageMap igrass_map = new DummyInputGrassCoverageMap(out, err, GrassMapTest.pitData);
        Argument[] mapArgs = new Argument[9];
        mapArgs[0] = new Argument("igrass", "map", true);
        mapArgs[1] = new Argument("quantityid", "map", true);
        mapArgs[2] = new Argument("grassdb", global_grassdb, true);
        mapArgs[3] = new Argument("location", global_location, true);
        mapArgs[4] = new Argument("mapset", global_mapset, true);
        mapArgs[5] = new Argument("time_start_up", global_startdate, true);
        mapArgs[6] = new Argument("time_ending_up", global_enddate, true);
        mapArgs[7] = new Argument("time_delta", global_deltat, true);
        mapArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_map.initialize(mapArgs);
        
        Link igrass_link_net = new Link(null, "map");
        igrass_link_net.connect(igrass_map, igrass_map.getOutputExchangeItem(0),
                wateroutlet, wateroutlet.getInputExchangeItem(0));

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
                wateroutlet, wateroutlet.getInputExchangeItem(1));



        OutputGrassCoverageWriter dummyOut1 = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments1 = new Argument[9];
        dummyArguments1[0] = new Argument("ograss", "basin", true);
        dummyArguments1[1] = new Argument("quantityid", "basin", true);
        dummyArguments1[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments1[3] = new Argument("location", global_location, true);
        dummyArguments1[4] = new Argument("mapset", global_mapset, true);
        dummyArguments1[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments1[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments1[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments1[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOut1.initialize(dummyArguments1);

        Link dummyOutLink1 = new Link(null, "basin");
        dummyOutLink1.connect(wateroutlet, wateroutlet.getOutputExchangeItem(0), dummyOut1, dummyOut1
                .getInputExchangeItem(0));

        assertTrue(dummyOutLink1.isConnected());
        
        OutputGrassCoverageWriter dummyOut2 = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments2 = new Argument[9];
        dummyArguments2[0] = new Argument("ograss", "trim", true);
        dummyArguments2[1] = new Argument("quantityid", "trim", true);
        dummyArguments2[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments2[3] = new Argument("location", global_location, true);
        dummyArguments2[4] = new Argument("mapset", global_mapset, true);
        dummyArguments2[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments2[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments2[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments2[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOut2.initialize(dummyArguments2);
        
        Link dummyOutLink2 = new Link(null, "trim");
        dummyOutLink2.connect(wateroutlet, wateroutlet.getOutputExchangeItem(1), dummyOut2, dummyOut2
                .getInputExchangeItem(0));
        
        assertTrue(dummyOutLink2.isConnected());

        igrass_map.prepare();
        wateroutlet.prepare();
        IValueSet valueSet1 = wateroutlet.getValues(null, dummyOutLink1.getID());
        IValueSet valueSet2 = wateroutlet.getValues(null, dummyOutLink2.getID());
   

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet1).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.basinWateroutletData,0);

        rasterData = ((JGrassGridCoverageValueSet) valueSet2).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.trimWateroutletData,0);

        igrass_map.finish();
        wateroutlet.finish();
        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }



}
