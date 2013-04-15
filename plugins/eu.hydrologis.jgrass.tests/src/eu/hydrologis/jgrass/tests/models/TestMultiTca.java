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
import eu.hydrologis.jgrass.models.h.multitca.h_multitca;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
/**
 * It test the h_multitca classes with mode=0.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestMultiTca extends JGrassTestCase {

    public void testMultiTca() throws IOException {
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
        h_multitca multitca = new h_multitca(out, err);
        Argument[] multitcaARgs = new Argument[7];
        multitcaARgs[0] = new Argument("grassdb", global_grassdb, true);
        multitcaARgs[1] = new Argument("location", global_location, true);
        multitcaARgs[2] = new Argument("mapset", global_mapset, true);
        multitcaARgs[3] = new Argument("time_start_up", global_startdate, true);
        multitcaARgs[4] = new Argument("time_ending_up", global_enddate, true);
        multitcaARgs[5] = new Argument("time_delta", global_deltat, true);
        multitcaARgs[6] = new Argument("remotedburl", global_remotedb, true);
        multitca.initialize(multitcaARgs);

        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.drainData1);
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
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), multitca,
                multitca.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_pit = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.pitData);
        Argument[] pitArguments = new Argument[9];
        pitArguments[0] = new Argument("igrass", "pit", true);
        pitArguments[1] = new Argument("quantityid", "pit", true);
        pitArguments[2] = new Argument("grassdb", global_grassdb, true);
        pitArguments[3] = new Argument("location", global_location, true);
        pitArguments[4] = new Argument("mapset", global_mapset, true);
        pitArguments[5] = new Argument("time_start_up", global_startdate, true);
        pitArguments[6] = new Argument("time_ending_up", global_enddate, true);
        pitArguments[7] = new Argument("time_delta", global_deltat, true);
        pitArguments[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_pit.initialize(pitArguments);

        Link igrass_link_pit = new Link(null, "pit");
        igrass_link_pit.connect(igrass_pit, igrass_pit.getOutputExchangeItem(0), multitca, multitca
                .getInputExchangeItem(1));

        DummyInputGrassCoverageMap igrass_cp3 = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.cp3Data);
        Argument[] cp3Arguments = new Argument[9];
        cp3Arguments[0] = new Argument("igrass", "casi3", true);
        cp3Arguments[1] = new Argument("quantityid", "casi3", true);
        cp3Arguments[2] = new Argument("grassdb", global_grassdb, true);
        cp3Arguments[3] = new Argument("location", global_location, true);
        cp3Arguments[4] = new Argument("mapset", global_mapset, true);
        cp3Arguments[5] = new Argument("time_start_up", global_startdate, true);
        cp3Arguments[6] = new Argument("time_ending_up", global_enddate, true);
        cp3Arguments[7] = new Argument("time_delta", global_deltat, true);
        cp3Arguments[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_cp3.initialize(cp3Arguments);

        Link igrass_link_cp3 = new Link(null, "casi3");
        igrass_link_cp3.connect(igrass_cp3, igrass_cp3.getOutputExchangeItem(0), multitca, multitca
                .getInputExchangeItem(2));

        OutputGrassCoverageWriter ograss_multitca = new OutputGrassCoverageWriter(out, err);
        Argument[] multitcaArguments = new Argument[9];
        multitcaArguments[0] = new Argument("ograss", "multitca", true);
        multitcaArguments[1] = new Argument("quantityid", "multitca", true);
        multitcaArguments[2] = new Argument("grassdb", global_grassdb, true);
        multitcaArguments[3] = new Argument("location", global_location, true);
        multitcaArguments[4] = new Argument("mapset", global_mapset, true);
        multitcaArguments[5] = new Argument("time_start_up", global_startdate, true);
        multitcaArguments[6] = new Argument("time_ending_up", global_enddate, true);
        multitcaArguments[7] = new Argument("time_delta", global_deltat, true);
        multitcaArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_multitca.initialize(multitcaArguments);

        Link ograss_link_multitca = new Link(null, "multitca");
        ograss_link_multitca.connect(multitca, multitca.getOutputExchangeItem(0), ograss_multitca,
                ograss_multitca.getInputExchangeItem(0));
        igrass_cp3.prepare();
        igrass_pit.prepare();
        igrass_flow.prepare();
        multitca.prepare();

        String linkMessage = "there is a problem in a link";

        assertTrue(linkMessage, igrass_link_flow.isConnected());
        assertTrue(linkMessage, igrass_link_cp3.isConnected());
        assertTrue(linkMessage, igrass_link_pit.isConnected());

        assertTrue(linkMessage, ograss_link_multitca.isConnected());

        IValueSet multiTcaValueSet = multitca.getValues(null, ograss_link_multitca.getID());

        GridCoverage2D multiTcaRasterData = ((JGrassGridCoverageValueSet) multiTcaValueSet)
                .getGridCoverage2D();
        
        // TODO is the below delta of 0.2 to much? To be checked.
        checkMatrixEqual(multiTcaRasterData.getRenderedImage(), GrassMapTest.multiTcaData, 0.2);

        igrass_flow.finish();
        igrass_pit.finish();
        igrass_cp3.finish();

        multitca.finish();
        // set active region to the needed
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }
}
