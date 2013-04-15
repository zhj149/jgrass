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
import eu.hydrologis.jgrass.models.h.pitfiller.h_pitfiller;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.InputGrassCoverageReader;
import eu.hydrologis.jgrass.utilitylinkables.InputGrassRasterMap;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

/**
 * Test pitfiller.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestPitfiller extends JGrassTestCase {

    @SuppressWarnings("nls")
    public void testPitfiller() throws IOException {
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
        h_pitfiller pitfiller = new h_pitfiller(out, err);
        Argument[] pitfillerArgs = new Argument[7];
        pitfillerArgs[0] = new Argument("grassdb", global_grassdb, true);
        pitfillerArgs[1] = new Argument("location", global_location, true);
        pitfillerArgs[2] = new Argument("mapset", global_mapset, true);
        pitfillerArgs[3] = new Argument("time_start_up", global_startdate, true);
        pitfillerArgs[4] = new Argument("time_ending_up", global_enddate, true);
        pitfillerArgs[5] = new Argument("time_delta", global_deltat, true);
        pitfillerArgs[6] = new Argument("remotedburl", global_remotedb, true);
        pitfiller.initialize(pitfillerArgs);

        DummyInputGrassCoverageMap igrass_elevation = new DummyInputGrassCoverageMap(out, err, GrassMapTest.mapData);
        Argument[] inputArgs = new Argument[9];
        inputArgs[0] = new Argument("igrass", "testa", true);
        inputArgs[1] = new Argument("quantityid", "elevation", true);
        inputArgs[2] = new Argument("grassdb", global_grassdb, true);
        inputArgs[3] = new Argument("location", global_location, true);
        inputArgs[4] = new Argument("mapset", global_mapset, true);
        inputArgs[5] = new Argument("time_start_up", global_startdate, true);
        inputArgs[6] = new Argument("time_ending_up", global_enddate, true);
        inputArgs[7] = new Argument("time_delta", global_deltat, true);
        inputArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_elevation.initialize(inputArgs);

        Link igrass_link_elevation = new Link(null, "elevation");
        igrass_link_elevation.connect(igrass_elevation, igrass_elevation.getOutputExchangeItem(0),
                pitfiller, pitfiller.getInputExchangeItem(0));

        OutputGrassCoverageWriter dummyOutEI = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "pit", true);
        dummyArguments[1] = new Argument("quantityid", "pit", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOutEI.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "pit");
        dummyOutLink.connect(pitfiller, pitfiller.getOutputExchangeItem(0), dummyOutEI, dummyOutEI
                .getInputExchangeItem(0));

        assertTrue(dummyOutLink.isConnected());

        igrass_elevation.prepare();
        pitfiller.prepare();
        IValueSet valueSet = pitfiller.getValues(null, dummyOutLink.getID());
        igrass_elevation.finish();
        pitfiller.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();

        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.outPitData,0);
        
        // reset the active region 
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);

    }


    
    
}
