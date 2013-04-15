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
import eu.hydrologis.jgrass.models.h.tc.h_tc;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
/**
 * It test the h_tc classes.
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */

public class TestTc extends JGrassTestCase{
    
    @SuppressWarnings("nls")
    public void testTc() throws IOException {
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
        h_tc tc = new h_tc(out, err);
        Argument[] tcArgs = new Argument[9];
        tcArgs[0] = new Argument("grassdb", global_grassdb, true);
        tcArgs[1] = new Argument("location", global_location, true);
        tcArgs[2] = new Argument("mapset", global_mapset, true);
        tcArgs[3] = new Argument("time_start_up", global_startdate, true);
        tcArgs[4] = new Argument("time_ending_up", global_enddate, true);
        tcArgs[5] = new Argument("time_delta", global_deltat, true);
        tcArgs[6] = new Argument("remotedburl", global_remotedb, true);
        tcArgs[7] = new Argument("thprof", "0.0017", true);
        tcArgs[8] = new Argument("thtang", "0.02", true);
        tc.initialize(tcArgs);

        DummyInputGrassCoverageMap igrass_prof = new DummyInputGrassCoverageMap(out, err, GrassMapTest.profData);
        Argument[] profArgs = new Argument[9];
        profArgs[0] = new Argument("igrass", "prof", true);
        profArgs[1] = new Argument("quantityid", "prof", true);
        profArgs[2] = new Argument("grassdb", global_grassdb, true);
        profArgs[3] = new Argument("location", global_location, true);
        profArgs[4] = new Argument("mapset", global_mapset, true);
        profArgs[5] = new Argument("time_start_up", global_startdate, true);
        profArgs[6] = new Argument("time_ending_up", global_enddate, true);
        profArgs[7] = new Argument("time_delta", global_deltat, true);
        profArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_prof.initialize(profArgs);

        Link igrass_link_prof = new Link(null, "prof");
        igrass_link_prof.connect(igrass_prof, igrass_prof.getOutputExchangeItem(0),
                tc, tc.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_tang = new DummyInputGrassCoverageMap(out, err, GrassMapTest.tanData);
        Argument[] tangArgs = new Argument[9];
        tangArgs[0] = new Argument("igrass", "tang", true);
        tangArgs[1] = new Argument("quantityid", "tang", true);
        tangArgs[2] = new Argument("grassdb", global_grassdb, true);
        tangArgs[3] = new Argument("location", global_location, true);
        tangArgs[4] = new Argument("mapset", global_mapset, true);
        tangArgs[5] = new Argument("time_start_up", global_startdate, true);
        tangArgs[6] = new Argument("time_ending_up", global_enddate, true);
        tangArgs[7] = new Argument("time_delta", global_deltat, true);
        tangArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_tang.initialize(tangArgs);

        Link igrass_link_tang = new Link(null, "tang");
        igrass_link_tang.connect(igrass_tang, igrass_tang.getOutputExchangeItem(0),
                tc, tc.getInputExchangeItem(1));

        OutputGrassCoverageWriter ograss_cp9 = new OutputGrassCoverageWriter(out, err);
        Argument[] c9dummyArguments = new Argument[9];
        c9dummyArguments[0] = new Argument("ograss", "cp9", true);
        c9dummyArguments[1] = new Argument("quantityid", "cp9", true);
        c9dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        c9dummyArguments[3] = new Argument("location", global_location, true);
        c9dummyArguments[4] = new Argument("mapset", global_mapset, true);
        c9dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        c9dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        c9dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        c9dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_cp9.initialize(c9dummyArguments);

        Link ograss_link_cp9 = new Link(null, "cp9");
        ograss_link_cp9.connect(tc, tc.getOutputExchangeItem(1), ograss_cp9, ograss_cp9
                .getInputExchangeItem(0));
        OutputGrassCoverageWriter ograss_cp3 = new OutputGrassCoverageWriter(out, err);
        Argument[] c3dummyArguments = new Argument[9];
        c3dummyArguments[0] = new Argument("ograss", "cp3", true);
        c3dummyArguments[1] = new Argument("quantityid", "cp3", true);
        c3dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        c3dummyArguments[3] = new Argument("location", global_location, true);
        c3dummyArguments[4] = new Argument("mapset", global_mapset, true);
        c3dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        c3dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        c3dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        c3dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_cp3.initialize(c9dummyArguments);

        Link ograss_link_cp3 = new Link(null, "cp3");
        ograss_link_cp3.connect(tc, tc.getOutputExchangeItem(0), ograss_cp3, ograss_cp3
                .getInputExchangeItem(0));

        igrass_prof.prepare();
        igrass_tang.prepare();
        tc.prepare();
        
        assertTrue(igrass_link_tang.isConnected());
        assertTrue(igrass_link_prof.isConnected());
        
        assertTrue(ograss_link_cp9.isConnected());
        assertTrue(ograss_link_cp3.isConnected());
        
        IValueSet tcvalueSet = tc.getValues(null, ograss_link_cp3.getID());
        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) tcvalueSet).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.cp3Data,0.0001);
       
        tcvalueSet = tc.getValues(null, ograss_link_cp9.getID());
        rasterData = ((JGrassGridCoverageValueSet) tcvalueSet).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.cp9Data,0.0001);

        // set active region to the needed
        igrass_prof.finish();
        igrass_tang.finish();
        tc.finish();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }



}
