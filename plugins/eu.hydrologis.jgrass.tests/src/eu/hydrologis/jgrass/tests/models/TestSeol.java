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
import eu.hydrologis.jgrass.models.h.seol.h_seol;
import eu.hydrologis.jgrass.models.h.strahler.h_strahler;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestSeol extends JGrassTestCase {

    public void testSeol() throws IOException {
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

        // instantiate the models
        h_seol seol = new h_seol(out, err);
        // create the arguments for the models and initialize it.
        Argument[] seolArgs = new Argument[8];
        seolArgs[0] = new Argument("grassdb", global_grassdb, true);
        seolArgs[1] = new Argument("location", global_location, true);
        seolArgs[2] = new Argument("mapset", global_mapset, true);
        seolArgs[3] = new Argument("time_start_up", global_startdate, true);
        seolArgs[4] = new Argument("time_ending_up", global_enddate, true);
        seolArgs[5] = new Argument("time_delta", global_deltat, true);
        seolArgs[6] = new Argument("remotedburl", global_remotedb, true);
        seolArgs[7] = new Argument("mode", "1", true);

        seol.initialize(seolArgs);

        // create the input "models" to link with the markoutlets models and its arguments
        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.mflowDataBorder);
        Argument[] inputArgs = new Argument[9];
        inputArgs[0] = new Argument("igrass", "flow", true);
        inputArgs[1] = new Argument("quantityid", "flow", true);
        inputArgs[2] = new Argument("grassdb", global_grassdb, true);
        inputArgs[3] = new Argument("location", global_location, true);
        inputArgs[4] = new Argument("mapset", global_mapset, true);
        inputArgs[5] = new Argument("time_start_up", global_startdate, true);
        inputArgs[6] = new Argument("time_ending_up", global_enddate, true);
        inputArgs[7] = new Argument("time_delta", global_deltat, true);
        inputArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_flow.initialize(inputArgs);

        Link igrass_link_flow = new Link(null, "flow");
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), seol,
                seol.getInputExchangeItem(1));
        assertTrue(linkMessage,igrass_link_flow.isConnected());
        // create the output "models" to link with the markoutlets models and its arguments

        DummyInputGrassCoverageMap igrass_net = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.extractNet1Data);
        Argument[] inputNetArgs = new Argument[9];
        inputNetArgs[0] = new Argument("igrass", "net", true);
        inputNetArgs[1] = new Argument("quantityid", "net", true);
        inputNetArgs[2] = new Argument("grassdb", global_grassdb, true);
        inputNetArgs[3] = new Argument("location", global_location, true);
        inputNetArgs[4] = new Argument("mapset", global_mapset, true);
        inputNetArgs[5] = new Argument("time_start_up", global_startdate, true);
        inputNetArgs[6] = new Argument("time_ending_up", global_enddate, true);
        inputNetArgs[7] = new Argument("time_delta", global_deltat, true);
        inputNetArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_net.initialize(inputNetArgs);

        Link igrass_link_net = new Link(null, "net");
        igrass_link_net.connect(igrass_net, igrass_net.getOutputExchangeItem(0), seol,
                seol.getInputExchangeItem(0));
        assertTrue(linkMessage,igrass_link_flow.isConnected());
        
        DummyInputGrassCoverageMap igrass_quantity = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.tcaData);
        Argument[] inputQuantityArgs = new Argument[9];
        inputQuantityArgs[0] = new Argument("igrass", "quantity", true);
        inputQuantityArgs[1] = new Argument("quantityid", "quantity", true);
        inputQuantityArgs[2] = new Argument("grassdb", global_grassdb, true);
        inputQuantityArgs[3] = new Argument("location", global_location, true);
        inputQuantityArgs[4] = new Argument("mapset", global_mapset, true);
        inputQuantityArgs[5] = new Argument("time_start_up", global_startdate, true);
        inputQuantityArgs[6] = new Argument("time_ending_up", global_enddate, true);
        inputQuantityArgs[7] = new Argument("time_delta", global_deltat, true);
        inputQuantityArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_quantity.initialize(inputQuantityArgs);

        Link igrass_link_quantity = new Link(null, "quantity");
        igrass_link_quantity.connect(igrass_quantity, igrass_quantity.getOutputExchangeItem(0), seol,
                seol.getInputExchangeItem(2));
        assertTrue(linkMessage,igrass_link_flow.isConnected());
        
        
        OutputGrassCoverageWriter dummyOutEI = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "seol", true);
        dummyArguments[1] = new Argument("quantityid", "seol", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOutEI.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "seol");
        dummyOutLink.connect(seol, seol.getOutputExchangeItem(0), dummyOutEI, dummyOutEI
                .getInputExchangeItem(0));


        assertTrue(linkMessage,dummyOutLink.isConnected());

        igrass_flow.prepare();
        igrass_net.prepare();
        seol.prepare();
        IValueSet valueSet = seol.getValues(null, dummyOutLink.getID());
        igrass_flow.finish();
        igrass_net.finish();
        seol.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();
        // compare the result
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.soelData, 0);

        // reset the active region
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);

    }
    
    
}
