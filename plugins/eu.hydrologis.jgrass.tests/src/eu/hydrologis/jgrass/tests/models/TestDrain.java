package eu.hydrologis.jgrass.tests.models;

import java.io.IOException;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.draindir.h_draindir;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * Test flow.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestDrain extends JGrassTestCase {
    private String mode = "1";

    public void testDrain() throws IOException {
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
        h_draindir drain = new h_draindir(out, err);
        Argument[] drainArgs = new Argument[9];
        drainArgs[0] = new Argument("grassdb", global_grassdb, true);
        drainArgs[1] = new Argument("location", global_location, true);
        drainArgs[2] = new Argument("mapset", global_mapset, true);
        drainArgs[3] = new Argument("time_start_up", global_startdate, true);
        drainArgs[4] = new Argument("time_ending_up", global_enddate, true);
        drainArgs[5] = new Argument("time_delta", global_deltat, true);
        drainArgs[6] = new Argument("remotedburl", global_remotedb, true);
        drainArgs[7] = new Argument("mode", mode, true);
        drainArgs[8] = new Argument("lambda", "1", true);
        drain.initialize(drainArgs);

        DummyInputGrassCoverageMap igrass_pit = new DummyInputGrassCoverageMap(out, err, GrassMapTest.pitData);
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
        igrass_link_pit.connect(igrass_pit, igrass_pit.getOutputExchangeItem(0), drain, drain.getInputExchangeItem(0));

        assertTrue(igrass_link_pit.isConnected());

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
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), drain, drain.getInputExchangeItem(1));

        assertTrue(igrass_link_flow.isConnected());

        OutputGrassRasterMap ograss_dir = new OutputGrassRasterMap(out, err);
        Argument[] dirArguments = new Argument[9];
        dirArguments[0] = new Argument("ograss", "dir", true);
        dirArguments[1] = new Argument("quantityid", "dir", true);
        dirArguments[2] = new Argument("grassdb", global_grassdb, true);
        dirArguments[3] = new Argument("location", global_location, true);
        dirArguments[4] = new Argument("mapset", global_mapset, true);
        dirArguments[5] = new Argument("time_start_up", global_startdate, true);
        dirArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dirArguments[7] = new Argument("time_delta", global_deltat, true);
        dirArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_dir.initialize(dirArguments);

        Link ograss_link_dir = new Link(null, "dir");
        ograss_link_dir.connect(drain, drain.getOutputExchangeItem(0), ograss_dir, ograss_dir.getInputExchangeItem(0));

        assertTrue(ograss_link_dir.isConnected());

        OutputGrassRasterMap ograss_tca = new OutputGrassRasterMap(out, err);
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
        ograss_link_tca.connect(drain, drain.getOutputExchangeItem(1), ograss_tca, ograss_tca.getInputExchangeItem(0));

        assertTrue(ograss_link_tca.isConnected());

        igrass_flow.prepare();
        igrass_pit.prepare();
        drain.prepare();

        IValueSet drainValueSet = drain.getValues(null, ograss_link_dir.getID());
        GridCoverage2D ddGC = ((JGrassGridCoverageValueSet) drainValueSet).getGridCoverage2D();
        GridCoverage2D view = ddGC.view(ViewType.GEOPHYSICS);
        PlanarImage ddImage = (PlanarImage) view.getRenderedImage();

        IValueSet tcaValueSet = drain.getValues(null, ograss_link_tca.getID());
        GridCoverage2D tcaGC = ((JGrassGridCoverageValueSet) tcaValueSet).getGridCoverage2D();
        view = tcaGC.view(ViewType.GEOPHYSICS);
        PlanarImage tcaImage = (PlanarImage) view.getRenderedImage();

        igrass_flow.finish();
        igrass_pit.finish();
        drain.finish();

        checkMatrixEqual(ddImage, GrassMapTest.drainData1);
        checkMatrixEqual(tcaImage, GrassMapTest.mtcaData);

        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(), activeRegion);
    }

}
