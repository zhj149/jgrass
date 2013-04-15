package eu.hydrologis.jgrass.tests.models;

import java.io.IOException;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.dd.h_dd;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

public class TestDD extends JGrassTestCase{
public void testDD() throws IOException {
        
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
        h_dd dd = new h_dd(out, err);
        // create the arguments for the models and initialize it.
        Argument[] ddArgs = new Argument[7];
        ddArgs[0] = new Argument("grassdb", global_grassdb, true);
        ddArgs[1] = new Argument("location", global_location, true);
        ddArgs[2] = new Argument("mapset", global_mapset, true);
        ddArgs[3] = new Argument("time_start_up", global_startdate, true);
        ddArgs[4] = new Argument("time_ending_up", global_enddate, true);
        ddArgs[5] = new Argument("time_delta", global_deltat, true);
        ddArgs[6] = new Argument("remotedburl", global_remotedb, true);

        dd.initialize(ddArgs);

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
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), dd,
                dd.getInputExchangeItem(0));
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
        igrass_link_net.connect(igrass_net, igrass_net.getOutputExchangeItem(0), dd,
                dd.getInputExchangeItem(2));
        assertTrue(linkMessage,igrass_link_flow.isConnected());
        
        DummyInputGrassCoverageMap igrass_tca = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.tcaData);
        Argument[] inputTcaArgs = new Argument[9];
        inputTcaArgs[0] = new Argument("igrass", "tca", true);
        inputTcaArgs[1] = new Argument("quantityid", "tca", true);
        inputTcaArgs[2] = new Argument("grassdb", global_grassdb, true);
        inputTcaArgs[3] = new Argument("location", global_location, true);
        inputTcaArgs[4] = new Argument("mapset", global_mapset, true);
        inputTcaArgs[5] = new Argument("time_start_up", global_startdate, true);
        inputTcaArgs[6] = new Argument("time_ending_up", global_enddate, true);
        inputTcaArgs[7] = new Argument("time_delta", global_deltat, true);
        inputTcaArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_tca.initialize(inputTcaArgs);

        Link igrass_link_tca = new Link(null, "tca");
        igrass_link_tca.connect(igrass_tca, igrass_tca.getOutputExchangeItem(0), dd,
                dd.getInputExchangeItem(1));
        assertTrue(linkMessage,igrass_link_tca.isConnected());
        
        
        
        
        OutputGrassRasterMap dummyOutEI = new OutputGrassRasterMap(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "dd", true);
        dummyArguments[1] = new Argument("quantityid", "dd", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOutEI.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "dd");
        dummyOutLink.connect(dd, dd.getOutputExchangeItem(0), dummyOutEI, dummyOutEI
                .getInputExchangeItem(0));


        assertTrue(linkMessage,dummyOutLink.isConnected());

        igrass_flow.prepare();
        igrass_net.prepare();
        dd.prepare();
        IValueSet ddvalueSet = dd.getValues(null, dummyOutLink.getID());
        igrass_flow.finish();
        igrass_net.finish();
        dd.finish();
        GridCoverage2D ddGC = ((JGrassGridCoverageValueSet) ddvalueSet).getGridCoverage2D();
        GridCoverage2D view = ddGC.view(ViewType.GEOPHYSICS);
        PlanarImage ddImage = (PlanarImage) view.getRenderedImage();
        // compare the result
        checkMatrixEqual(ddImage, GrassMapTest.ddData, 0.001);

        // reset the active region
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);

    }
}
