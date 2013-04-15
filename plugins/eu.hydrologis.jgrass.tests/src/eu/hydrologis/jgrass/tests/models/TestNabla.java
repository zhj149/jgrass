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
import eu.hydrologis.jgrass.models.h.nabla.h_nabla;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

/**
 * It test the h_nabla classes which estimate the nabla value.
 * <p>
 * There is two mode to run h_nabla so there is two test (testNabla0, mode==0,
 * and testNabla1, mode=1)
 * </p>
 * 
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */

public class TestNabla extends JGrassTestCase {

    public void testNabla0() throws IOException {

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

        /*
         * create the object for the test 1 with mode=1.
         */

        h_nabla nabla = new h_nabla(out, err);
        Argument[] nablaARgs = new Argument[9];
        nablaARgs[0] = new Argument("grassdb", global_grassdb, true);
        nablaARgs[1] = new Argument("location", global_location, true);
        nablaARgs[2] = new Argument("mapset", global_mapset, true);
        nablaARgs[3] = new Argument("time_start_up", global_startdate, true);
        nablaARgs[4] = new Argument("time_ending_up", global_enddate, true);
        nablaARgs[5] = new Argument("time_delta", global_deltat, true);
        nablaARgs[6] = new Argument("remotedburl", global_remotedb, true);
        // set the mode
        nablaARgs[7] = new Argument("mode", "0", true);
        nablaARgs[8] = new Argument("threshold", "0.001", true);
        
        nabla.initialize(nablaARgs);

        /*
         * create the input object.
         */

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
        /*
         * link the models.
         */
        Link igrass_link_pit = new Link(null, "pit");
        igrass_link_pit.connect(igrass_pit, igrass_pit.getOutputExchangeItem(0), nabla, nabla
                .getInputExchangeItem(0));

        /*
         * create the output models.
         */
        OutputGrassCoverageWriter ograss_nabla = new OutputGrassCoverageWriter(out, err);
        Argument[] nablaArguments = new Argument[9];
        nablaArguments[0] = new Argument("ograss", "nabla", true);
        nablaArguments[1] = new Argument("quantityid", "nabla", true);
        nablaArguments[2] = new Argument("grassdb", global_grassdb, true);
        nablaArguments[3] = new Argument("location", global_location, true);
        nablaArguments[4] = new Argument("mapset", global_mapset, true);
        nablaArguments[5] = new Argument("time_start_up", global_startdate, true);
        nablaArguments[6] = new Argument("time_ending_up", global_enddate, true);
        nablaArguments[7] = new Argument("time_delta", global_deltat, true);
        nablaArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_nabla.initialize(nablaArguments);
        /*
         * link the models
         */
        Link ograss_link_nabla = new Link(null, "nabla");
        ograss_link_nabla.connect(nabla, nabla.getOutputExchangeItem(0), ograss_nabla,
                ograss_nabla.getInputExchangeItem(0));

        igrass_pit.prepare();
       
        nabla.prepare();

        assertTrue(linkMessage, igrass_link_pit.isConnected());

        assertTrue(linkMessage, ograss_link_nabla.isConnected());


        /*
         * run the model.
         */
        IValueSet nablaValueSet = nabla.getValues(null, ograss_link_nabla.getID());

        GridCoverage2D nablaRasterData = ((JGrassGridCoverageValueSet) nablaValueSet).getGridCoverage2D();
        checkMatrixEqual(nablaRasterData.getRenderedImage(), GrassMapTest.nablaData0, 0.01);
        // terminate the model.
        nabla.finish();
        igrass_pit.finish();
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }
}
