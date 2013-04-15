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
import eu.hydrologis.jgrass.models.h.tca.h_tca;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
/**
 * It test the h_tca classes.
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestTca extends JGrassTestCase {

    public void testTca() throws IOException {

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
        h_tca tca = new h_tca(out, err);
        Argument[] tcaARgs = new Argument[7];
        tcaARgs[0] = new Argument("grassdb", global_grassdb, true);
        tcaARgs[1] = new Argument("location", global_location, true);
        tcaARgs[2] = new Argument("mapset", global_mapset, true);
        tcaARgs[3] = new Argument("time_start_up", global_startdate, true);
        tcaARgs[4] = new Argument("time_ending_up", global_enddate, true);
        tcaARgs[5] = new Argument("time_delta", global_deltat, true);
        tcaARgs[6] = new Argument("remotedburl", global_remotedb, true);
        tca.initialize(tcaARgs);

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
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), tca, tca
                .getInputExchangeItem(0));

        OutputGrassCoverageWriter ograss_tca = new OutputGrassCoverageWriter(out, err);
        Argument[] tcaArguments = new Argument[9];
        tcaArguments[0] = new Argument("ograss", "tca", true);
        tcaArguments[1] = new Argument("quantityid", "tca", true);
        tcaArguments[2] = new Argument("grassdb", global_grassdb, true);
        tcaArguments[3] = new Argument("location", global_location, true);
        tcaArguments[4] = new Argument("mapset", global_mapset, true);
        tcaArguments[5] = new Argument("time_start_up", global_startdate, true);
        tcaArguments[6] = new Argument("time_ending_up", global_enddate, true);
        tcaArguments[7] = new Argument("time_delta", global_deltat, true);
        tcaArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_tca.initialize(tcaArguments);

        Link ograss_link_tca = new Link(null, "tca");
        ograss_link_tca.connect(tca, tca.getOutputExchangeItem(0), ograss_tca, ograss_tca
                .getInputExchangeItem(0));

        igrass_flow.prepare();
        tca.prepare();

        assertTrue(linkMessage, igrass_link_flow.isConnected());

        assertTrue(linkMessage, ograss_link_tca.isConnected());

        IValueSet tcaValueSet = tca.getValues(null, ograss_link_tca.getID());

        GridCoverage2D tcaRasterData = ((JGrassGridCoverageValueSet) tcaValueSet).getGridCoverage2D();
        checkMatrixEqual(tcaRasterData.getRenderedImage(), GrassMapTest.tcaData, 0.0);

        igrass_flow.finish();
        tca.finish();
        // set active region to the needed
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }
}
