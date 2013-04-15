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
import eu.hydrologis.jgrass.models.h.trasmissivity.h_trasmissivity;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * Test Trasmissivity.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestTrasmissivity extends JGrassTestCase {

    @SuppressWarnings("nls")
    public void testTrasmissivity() throws IOException {
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
        
        h_trasmissivity trasmissivity = new h_trasmissivity(out, err);
        Argument[] trasmissivityArgs = new Argument[9];
        trasmissivityArgs[0] = new Argument("grassdb", global_grassdb, true);
        trasmissivityArgs[1] = new Argument("location", global_location, true);
        trasmissivityArgs[2] = new Argument("mapset", global_mapset, true);
        trasmissivityArgs[3] = new Argument("time_start_up", global_startdate, true);
        trasmissivityArgs[4] = new Argument("time_ending_up", global_enddate, true);
        trasmissivityArgs[5] = new Argument("time_delta", global_deltat, true);
        trasmissivityArgs[6] = new Argument("remotedburl", global_remotedb, true);
        trasmissivityArgs[7] = new Argument("hsconst", "2.0", true);
        trasmissivityArgs[8] = new Argument("conducibilityconst", "0.001", true);
        trasmissivity.initialize(trasmissivityArgs);
        
        DummyInputGrassCoverageMap igrass_slope = new DummyInputGrassCoverageMap(out, err, GrassMapTest.slopeData);
        Argument[] slopemapArgs = new Argument[9];
        slopemapArgs[0] = new Argument("igrass", "slopemap", true);
        slopemapArgs[1] = new Argument("quantityid", "slopemap", true);
        slopemapArgs[2] = new Argument("grassdb", global_grassdb, true);
        slopemapArgs[3] = new Argument("location", global_location, true);
        slopemapArgs[4] = new Argument("mapset", global_mapset, true);
        slopemapArgs[5] = new Argument("time_start_up", global_startdate, true);
        slopemapArgs[6] = new Argument("time_ending_up", global_enddate, true);
        slopemapArgs[7] = new Argument("time_delta", global_deltat, true);
        slopemapArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_slope.initialize(slopemapArgs);
        
        Link igrass_link_slopemap = new Link(null, "slopemap");
        igrass_link_slopemap.connect(igrass_slope, igrass_slope.getOutputExchangeItem(0),
                trasmissivity, trasmissivity.getInputExchangeItem(0));

        OutputGrassCoverageWriter dummyOut = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "trasmissivitymap", true);
        dummyArguments[1] = new Argument("quantityid", "trasmissivitymap", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOut.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "trasmissivitymap");
        dummyOutLink.connect(trasmissivity, trasmissivity.getOutputExchangeItem(0), dummyOut, dummyOut
                .getInputExchangeItem(0));

        assertTrue(dummyOutLink.isConnected());

        igrass_slope.prepare();
        trasmissivity.prepare();
        IValueSet valueSet = trasmissivity.getValues(null, dummyOutLink.getID());
        igrass_slope.finish();
        trasmissivity.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.trasmissivityData,0);

        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }



}
