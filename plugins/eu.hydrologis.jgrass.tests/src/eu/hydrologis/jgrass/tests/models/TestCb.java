package eu.hydrologis.jgrass.tests.models;

import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.cb.h_cb;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputScalarWriter;

/**
 * Test for h.cb.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestCb extends JGrassTestCase {

    private double[][] expected = new double[][]{
            {1.0, 400.0, 400.0}, 
            {2.0, 420.0, 420.0}, 
            {3.0, 450.0, 450.0},
            {5.0, 498.0, 498.0}, 
            {2.0, 550.0, 550.0}, 
            {4.0, 600.0, 600.0}, 
            {6.0, 691.6666666666666, 691.6666666666666},
            {6.0, 751.6666666666666, 751.6666666666666}, 
            {2.0, 775.0, 775.0}, 
            {7.0, 798.5714285714286, 798.5714285714286},
            {5.0, 852.0, 852.0}, 
            {3.0, 900.0, 900.0}, 
            {2.0, 945.0, 945.0}, 
            {7.0, 1000.1428571428571, 1000.1428571428571},
            {2.0, 1100.0, 1100.0}, 
            {2.0, 1150.0, 1150.0}, 
            {3.0, 1200.0, 1200.0}, 
            {4.0, 1250.0, 1250.0}, 
            {2.0, 1300.0, 1300.0},
            {3.0, 1416.6666666666667, 1416.6666666666667}, 
            {7.0, 1500.0, 1500.0}
            };

    @SuppressWarnings("nls")
    public void testCb() throws IOException {
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

        h_cb cb = new h_cb(out, err);
        Argument[] cbArgs = new Argument[10];
        cbArgs[0] = new Argument("grassdb", global_grassdb, true);
        cbArgs[1] = new Argument("location", global_location, true);
        cbArgs[2] = new Argument("mapset", global_mapset, true);
        cbArgs[3] = new Argument("time_start_up", global_startdate, true);
        cbArgs[4] = new Argument("time_ending_up", global_enddate, true);
        cbArgs[5] = new Argument("time_delta", global_deltat, true);
        cbArgs[6] = new Argument("remotedburl", global_remotedb, true);
        cbArgs[7] = new Argument("numbins", "100", true);
        cbArgs[8] = new Argument("firstmoment", "1", true);
        cbArgs[9] = new Argument("lastmoment", "1", true);
        cb.initialize(cbArgs);

        DummyInputGrassCoverageMap igrass_elevation1 = new DummyInputGrassCoverageMap(out, err, GrassMapTest.mapData);
        Argument[] inputArgs1 = new Argument[9];
        inputArgs1[0] = new Argument("igrass", "testa", true);
        inputArgs1[1] = new Argument("quantityid", "map1", true);
        inputArgs1[2] = new Argument("grassdb", global_grassdb, true);
        inputArgs1[3] = new Argument("location", global_location, true);
        inputArgs1[4] = new Argument("mapset", global_mapset, true);
        inputArgs1[5] = new Argument("time_start_up", global_startdate, true);
        inputArgs1[6] = new Argument("time_ending_up", global_enddate, true);
        inputArgs1[7] = new Argument("time_delta", global_deltat, true);
        inputArgs1[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_elevation1.initialize(inputArgs1);

        Link igrass_link_elevation1 = new Link(null, "map1");
        igrass_link_elevation1.connect(igrass_elevation1, igrass_elevation1.getOutputExchangeItem(0), cb, cb
                .getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_elevation2 = new DummyInputGrassCoverageMap(out, err, GrassMapTest.mapData);
        Argument[] inputArgs2 = new Argument[9];
        inputArgs2[0] = new Argument("igrass", "testa", true);
        inputArgs2[1] = new Argument("quantityid", "map2", true);
        inputArgs2[2] = new Argument("grassdb", global_grassdb, true);
        inputArgs2[3] = new Argument("location", global_location, true);
        inputArgs2[4] = new Argument("mapset", global_mapset, true);
        inputArgs2[5] = new Argument("time_start_up", global_startdate, true);
        inputArgs2[6] = new Argument("time_ending_up", global_enddate, true);
        inputArgs2[7] = new Argument("time_delta", global_deltat, true);
        inputArgs2[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_elevation2.initialize(inputArgs2);

        Link igrass_link_elevation2 = new Link(null, "map2");
        igrass_link_elevation2.connect(igrass_elevation2, igrass_elevation2.getOutputExchangeItem(0), cb, cb
                .getInputExchangeItem(1));

        OutputScalarWriter dummyOutScalar1 = new OutputScalarWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "out1", true);
        dummyArguments[1] = new Argument("quantityid", "out1", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOutScalar1.initialize(dummyArguments);

        Link dummyOutLink1 = new Link(null, "out1");
        dummyOutLink1.connect(cb, cb.getOutputExchangeItem(0), dummyOutScalar1, dummyOutScalar1.getInputExchangeItem(0));

        assertTrue(dummyOutLink1.isConnected());

        OutputScalarWriter dummyOutScalar2 = new OutputScalarWriter(out, err);
        Argument[] dummyArguments2 = new Argument[9];
        dummyArguments2[0] = new Argument("ograss", "out2", true);
        dummyArguments2[1] = new Argument("quantityid", "out2", true);
        dummyArguments2[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments2[3] = new Argument("location", global_location, true);
        dummyArguments2[4] = new Argument("mapset", global_mapset, true);
        dummyArguments2[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments2[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments2[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments2[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOutScalar2.initialize(dummyArguments2);

        Link dummyOutLink2 = new Link(null, "out2");
        dummyOutLink2.connect(cb, cb.getOutputExchangeItem(0), dummyOutScalar2, dummyOutScalar2.getInputExchangeItem(0));

        assertTrue(dummyOutLink2.isConnected());

        igrass_elevation1.prepare();
        igrass_elevation2.prepare();
        cb.prepare();
        IValueSet valueSet1 = cb.getValues(null, dummyOutLink1.getID());
        IValueSet valueSet2 = cb.getValues(null, dummyOutLink2.getID());
        igrass_elevation1.finish();
        igrass_elevation2.finish();
        cb.finish();

        ScalarSet set1 = (ScalarSet) valueSet1;
        Double colSize = set1.get(0);
        int index = 1;
        int rowIndex = 0;
        for( int i = 1; i < set1.size(); i = (int) (i + colSize) ) {
            for( int j = 0; j < colSize; j++ ) {
                Double value = set1.get(index);
                assertEquals(value, expected[rowIndex][j], 0.01);
                index++;
            }
            rowIndex++;
        }

        ScalarSet set2 = (ScalarSet) valueSet2;
        colSize = set2.get(0);
        index = 1;
        rowIndex = 0;
        for( int i = 1; i < set2.size(); i = (int) (i + colSize) ) {
            for( int j = 0; j < colSize; j++ ) {
                Double value = set2.get(index);
                assertEquals(value, expected[rowIndex][(int) (colSize - 1 - j)], 0.01);
                index++;
            }
            rowIndex++;
        }

        // reset the active region
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(), activeRegion);

    }

}
