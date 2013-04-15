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
import eu.hydrologis.jgrass.models.h.tau.h_tau;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestTau extends JGrassTestCase{
    @SuppressWarnings("nls")
    public void testTau() throws IOException {
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
        h_tau tau = new h_tau(out, err);
        Argument[] tauArgs = new Argument[14];
        tauArgs[0] = new Argument("grassdb", global_grassdb, true);
        tauArgs[1] = new Argument("location", global_location, true);
        tauArgs[2] = new Argument("mapset", global_mapset, true);
        tauArgs[3] = new Argument("time_start_up", global_startdate, true);
        tauArgs[4] = new Argument("time_ending_up", global_enddate, true);
        tauArgs[5] = new Argument("time_delta", global_deltat, true);
        tauArgs[6] = new Argument("remotedburl", global_remotedb, true);
        tauArgs[7] = new Argument("g", "9.81", true);
        tauArgs[8] = new Argument("rho", "1000", true);
        tauArgs[9] = new Argument("ni", "0.000001", true);
        tauArgs[10] = new Argument("q", "10", true);
        tauArgs[11] = new Argument("k", "1", true);
        tauArgs[12] = new Argument("c", "2", true);
        tauArgs[13] = new Argument("t", "0", true);



        tau.initialize(tauArgs);

        DummyInputGrassCoverageMap igrass_slope = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.slopeData);
        Argument[] slope = new Argument[9];
        slope[0] = new Argument("igrass", "slope", true);
        slope[1] = new Argument("quantityid", "slope", true);
        slope[2] = new Argument("grassdb", global_grassdb, true);
        slope[3] = new Argument("location", global_location, true);
        slope[4] = new Argument("mapset", global_mapset, true);
        slope[5] = new Argument("time_start_up", global_startdate, true);
        slope[6] = new Argument("time_ending_up", global_enddate, true);
        slope[7] = new Argument("time_delta", global_deltat, true);
        slope[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_slope.initialize(slope);

        Link igrass_link_slope = new Link(null, "slope");
        igrass_link_slope.connect(igrass_slope, igrass_slope.getOutputExchangeItem(0), tau,
                tau.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_ab = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.abData);
        Argument[] abArgs = new Argument[9];
        abArgs[0] = new Argument("igrass", "ab", true);
        abArgs[1] = new Argument("quantityid", "ab", true);
        abArgs[2] = new Argument("grassdb", global_grassdb, true);
        abArgs[3] = new Argument("location", global_location, true);
        abArgs[4] = new Argument("mapset", global_mapset, true);
        abArgs[5] = new Argument("time_start_up", global_startdate, true);
        abArgs[6] = new Argument("time_ending_up", global_enddate, true);
        abArgs[7] = new Argument("time_delta", global_deltat, true);
        abArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_ab.initialize(abArgs);

        Link igrass_link_tca = new Link(null, "ab");
        igrass_link_tca.connect(igrass_ab, igrass_ab.getOutputExchangeItem(0), tau, tau
                .getInputExchangeItem(1));

        OutputGrassCoverageWriter output_tau = new OutputGrassCoverageWriter(out, err);
        Argument[] tauArguments = new Argument[9];
        tauArguments[0] = new Argument("ograss", "tau", true);
        tauArguments[1] = new Argument("quantityid", "tau", true);
        tauArguments[2] = new Argument("grassdb", global_grassdb, true);
        tauArguments[3] = new Argument("location", global_location, true);
        tauArguments[4] = new Argument("mapset", global_mapset, true);
        tauArguments[5] = new Argument("time_start_up", global_startdate, true);
        tauArguments[6] = new Argument("time_ending_up", global_enddate, true);
        tauArguments[7] = new Argument("time_delta", global_deltat, true);
        tauArguments[8] = new Argument("remotedburl", global_remotedb, true);
        output_tau.initialize(tauArguments);

        Link ograss_link_index = new Link(null, "tau");
        ograss_link_index.connect(tau, tau.getOutputExchangeItem(0), output_tau, output_tau
                .getInputExchangeItem(0));

        igrass_slope.prepare();
        igrass_ab.prepare();
        tau.prepare();
        String linkMessage = "there is a prblem in a link";

        assertTrue(linkMessage, igrass_link_slope.isConnected());
        assertTrue(linkMessage, igrass_link_tca.isConnected());

        assertTrue(linkMessage, ograss_link_index.isConnected());

        IValueSet tauValueSet = tau.getValues(null, ograss_link_index.getID());
        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) tauValueSet).getGridCoverage2D();

        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.tauData, 1);
        tauValueSet = tau.getValues(null, ograss_link_index.getID());

        igrass_slope.finish();
        igrass_ab.finish();
        tau.finish();
        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }
}
