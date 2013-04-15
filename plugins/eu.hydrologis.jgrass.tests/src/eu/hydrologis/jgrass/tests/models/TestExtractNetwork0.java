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
import eu.hydrologis.jgrass.models.h.extractnetwork.h_extractnetwork;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
/**
 * It test the h_extractNetwork classes with mode=0.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestExtractNetwork0 extends JGrassTestCase {
    @SuppressWarnings("nls")
    public void testExtractNetwork() throws IOException {
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
        h_extractnetwork extractNetwork = new h_extractnetwork(out, err);
        Argument[] extractArgs = new Argument[9];
        extractArgs[0] = new Argument("grassdb", global_grassdb, true);
        extractArgs[1] = new Argument("location", global_location, true);
        extractArgs[2] = new Argument("mapset", global_mapset, true);
        extractArgs[3] = new Argument("time_start_up", global_startdate, true);
        extractArgs[4] = new Argument("time_ending_up", global_enddate, true);
        extractArgs[5] = new Argument("time_delta", global_deltat, true);
        extractArgs[6] = new Argument("remotedburl", global_remotedb, true);
        extractArgs[7] = new Argument("mode", "0", true);
        extractArgs[8] = new Argument("threshold", "5", true);

        extractNetwork.initialize(extractArgs);

        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.mflowDataBorder);
        Argument[] flowArgs = new Argument[9];
        flowArgs[0] = new Argument("igrass", "flow", true);
        flowArgs[1] = new Argument("quantityid", "flow", true);
        flowArgs[2] = new Argument("grassdb", global_grassdb, true);
        flowArgs[3] = new Argument("location", global_location, true);
        flowArgs[4] = new Argument("mapset", global_mapset, true);
        flowArgs[5] = new Argument("time_start_up", global_startdate, true);
        flowArgs[6] = new Argument("time_ending_up", global_enddate, true);
        flowArgs[7] = new Argument("time_delta", global_deltat, true);
        flowArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_flow.initialize(flowArgs);

        Link igrass_link_flow = new Link(null, "flow");
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), extractNetwork,
                extractNetwork.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_tca = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.tcaData);
        Argument[] tcaArgs = new Argument[9];
        tcaArgs[0] = new Argument("igrass", "tca", true);
        tcaArgs[1] = new Argument("quantityid", "tca", true);
        tcaArgs[2] = new Argument("grassdb", global_grassdb, true);
        tcaArgs[3] = new Argument("location", global_location, true);
        tcaArgs[4] = new Argument("mapset", global_mapset, true);
        tcaArgs[5] = new Argument("time_start_up", global_startdate, true);
        tcaArgs[6] = new Argument("time_ending_up", global_enddate, true);
        tcaArgs[7] = new Argument("time_delta", global_deltat, true);
        tcaArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_tca.initialize(tcaArgs);

        Link igrass_link_tca = new Link(null, "tca");
        igrass_link_tca.connect(igrass_tca, igrass_tca.getOutputExchangeItem(0), extractNetwork,
                extractNetwork.getInputExchangeItem(1));

        OutputGrassRasterMap extrxtO = new OutputGrassRasterMap(out, err);
        Argument[] extractArguments = new Argument[9];
        extractArguments[0] = new Argument("ograss", "net", true);
        extractArguments[1] = new Argument("quantityid", "net", true);
        extractArguments[2] = new Argument("grassdb", global_grassdb, true);
        extractArguments[3] = new Argument("location", global_location, true);
        extractArguments[4] = new Argument("mapset", global_mapset, true);
        extractArguments[5] = new Argument("time_start_up", global_startdate, true);
        extractArguments[6] = new Argument("time_ending_up", global_enddate, true);
        extractArguments[7] = new Argument("time_delta", global_deltat, true);
        extractArguments[8] = new Argument("remotedburl", global_remotedb, true);
        extrxtO.initialize(extractArguments);

        Link ograss_link_net = new Link(null, "net");
        ograss_link_net.connect(extractNetwork, extractNetwork.getOutputExchangeItem(0), extrxtO,
                extrxtO.getInputExchangeItem(0));

        igrass_flow.prepare();
        igrass_tca.prepare();
        extractNetwork.prepare();
        String linkMessage = "there is a prblem in a link";

        assertTrue(linkMessage, igrass_link_flow.isConnected());
        assertTrue(linkMessage, igrass_link_tca.isConnected());

        assertTrue(linkMessage, ograss_link_net.isConnected());

        IValueSet netValueSet = extractNetwork.getValues(null, ograss_link_net.getID());
        GridCoverage2D netGC = ((JGrassGridCoverageValueSet) netValueSet).getGridCoverage2D();
        GridCoverage2D view = netGC.view(ViewType.GEOPHYSICS);
        PlanarImage netImage = (PlanarImage) view.getRenderedImage();
        checkMatrixEqual(netImage, GrassMapTest.extractNet0Data, 0.01);

        igrass_flow.finish();
        igrass_tca.finish();
        extractNetwork.finish();
        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }

}
