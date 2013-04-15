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
import eu.hydrologis.jgrass.models.h.tca3d.h_tca3d;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestTca3D extends JGrassTestCase {
    public void testTca3D() throws IOException {
        JGrassMapEnvironment jGrassMapEnvironment = GrassMapTest.jME;

        // set active region to the needed
        JGrassRegion fileRegion = jGrassMapEnvironment.getFileRegion();
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
        h_tca3d tca3d = new h_tca3d(out, err);
        Argument[] tcaARgs = new Argument[7];
        tcaARgs[0] = new Argument("grassdb", global_grassdb, true);
        tcaARgs[1] = new Argument("location", global_location, true);
        tcaARgs[2] = new Argument("mapset", global_mapset, true);
        tcaARgs[3] = new Argument("time_start_up", global_startdate, true);
        tcaARgs[4] = new Argument("time_ending_up", global_enddate, true);
        tcaARgs[5] = new Argument("time_delta", global_deltat, true);
        tcaARgs[6] = new Argument("remotedburl", global_remotedb, true);
        tca3d.initialize(tcaARgs);

        DummyInputGrassCoverageMap igrass_pit = new DummyInputGrassCoverageMap(out, err, GrassMapTest.pitData);
        Argument[] pitArguments = new Argument[9];
        pitArguments[0] = new Argument("igrass", "pit", true);
        pitArguments[1] = new Argument("quantityid", "flow", true);
        pitArguments[2] = new Argument("grassdb", global_grassdb, true);
        pitArguments[3] = new Argument("location", global_location, true);
        pitArguments[4] = new Argument("mapset", global_mapset, true);
        pitArguments[5] = new Argument("time_start_up", global_startdate, true);
        pitArguments[6] = new Argument("time_ending_up", global_enddate, true);
        pitArguments[7] = new Argument("time_delta", global_deltat, true);
        pitArguments[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_pit.initialize(pitArguments);

        Link igrass_link_pit = new Link(null, "pit");
        igrass_link_pit.connect(igrass_pit, igrass_pit.getOutputExchangeItem(0), tca3d, tca3d
                .getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err, GrassMapTest.flowData);
        Argument[] flowArguments = new Argument[9];
        flowArguments[0] = new Argument("igrass", "flow", true);
        flowArguments[1] = new Argument("quantityid", "flow", true);
        flowArguments[2] = new Argument("grassdb", global_grassdb, true);
        flowArguments[3] = new Argument("location", global_location, true);
        flowArguments[4] = new Argument("mapset", global_mapset, true);
        flowArguments[5] = new Argument("time_start_up", global_startdate, true);
        flowArguments[6] = new Argument("time_ending_up", global_enddate, true);
        flowArguments[7] = new Argument("time_delta", global_deltat, true);
        flowArguments[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_flow.initialize(flowArguments);

        Link igrass_link_flow = new Link(null, "flow");
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), tca3d, tca3d
                .getInputExchangeItem(1));

        OutputGrassCoverageWriter ograss_tca3D = new OutputGrassCoverageWriter(out, err);
        Argument[] tca3DArguments = new Argument[9];

        tca3DArguments[0] = new Argument("ograss", "tca3d", true);
        tca3DArguments[1] = new Argument("quantityid", "tca3d", true);
        tca3DArguments[2] = new Argument("grassdb", global_grassdb, true);
        tca3DArguments[3] = new Argument("location", global_location, true);
        tca3DArguments[4] = new Argument("mapset", global_mapset, true);
        tca3DArguments[5] = new Argument("time_start_up", global_startdate, true);
        tca3DArguments[6] = new Argument("time_ending_up", global_enddate, true);
        tca3DArguments[7] = new Argument("time_delta", global_deltat, true);
        tca3DArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_tca3D.initialize(tca3DArguments);

        Link ograss_link_tca3D = new Link(null, "tca3d");
        ograss_link_tca3D.connect(tca3d, tca3d.getOutputExchangeItem(0), ograss_tca3D, ograss_tca3D
                .getInputExchangeItem(0));

        igrass_flow.prepare();
        tca3d.prepare();

    

        String linkMessage = "there is a prblem in a link";

        assertTrue(linkMessage, igrass_link_flow.isConnected());
        assertTrue(linkMessage, igrass_link_pit.isConnected());

        assertTrue(linkMessage, ograss_link_tca3D.isConnected());

    



        IValueSet tcaValueSet = tca3d.getValues(null, ograss_link_tca3D.getID());

        GridCoverage2D tcaRasterData = ((JGrassGridCoverageValueSet) tcaValueSet).getGridCoverage2D();
        checkMatrixEqual(tcaRasterData.getRenderedImage(), GrassMapTest.tca3DData, 0.01);
        // RasterData tcaRasterData = ((JGrassRasterValueSet)
        // tcaValueSet).getJGrassRasterData();
        // checkMatrixEqual(tcaRasterData, GrassMapTest.tcaData);

        // set active region to the needed
        igrass_flow.finish();
        tca3d.finish();
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);

    }
}
