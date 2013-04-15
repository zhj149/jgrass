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
import eu.hydrologis.jgrass.models.h.h2ca.h_h2ca;
import eu.hydrologis.jgrass.models.h.rescaleddistance3d.h_rescaleddistance3d;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * Test H2ca.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestH2ca extends JGrassTestCase {

    @SuppressWarnings("nls")
    public void testH2ca() throws IOException {
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
        h_h2ca h2ca = new h_h2ca(out, err);
        Argument[] h2caArgs = new Argument[7];
        h2caArgs[0] = new Argument("grassdb", global_grassdb, true);
        h2caArgs[1] = new Argument("location", global_location, true);
        h2caArgs[2] = new Argument("mapset", global_mapset, true);
        h2caArgs[3] = new Argument("time_start_up", global_startdate, true);
        h2caArgs[4] = new Argument("time_ending_up", global_enddate, true);
        h2caArgs[5] = new Argument("time_delta", global_deltat, true);
        h2caArgs[6] = new Argument("remotedburl", global_remotedb, true);
        h2ca.initialize(h2caArgs);
        
        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err, GrassMapTest.flowData);
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
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0),
                h2ca, h2ca.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_net = new DummyInputGrassCoverageMap(out, err, GrassMapTest.extractNet0Data);
        Argument[] netArgs = new Argument[9];
        netArgs[0] = new Argument("igrass", "net", true);
        netArgs[1] = new Argument("quantityid", "net", true);
        netArgs[2] = new Argument("grassdb", global_grassdb, true);
        netArgs[3] = new Argument("location", global_location, true);
        netArgs[4] = new Argument("mapset", global_mapset, true);
        netArgs[5] = new Argument("time_start_up", global_startdate, true);
        netArgs[6] = new Argument("time_ending_up", global_enddate, true);
        netArgs[7] = new Argument("time_delta", global_deltat, true);
        netArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_net.initialize(netArgs);
        
        Link igrass_link_net = new Link(null, "net");
        igrass_link_net.connect(igrass_net, igrass_net.getOutputExchangeItem(0),
                h2ca, h2ca.getInputExchangeItem(1));
        
        DummyInputGrassCoverageMap igrass_attribute = new DummyInputGrassCoverageMap(out, err, GrassMapTest.gradientData);
        Argument[] attributeArgs = new Argument[9];
        attributeArgs[0] = new Argument("igrass", "attribute", true);
        attributeArgs[1] = new Argument("quantityid", "attribute", true);
        attributeArgs[2] = new Argument("grassdb", global_grassdb, true);
        attributeArgs[3] = new Argument("location", global_location, true);
        attributeArgs[4] = new Argument("mapset", global_mapset, true);
        attributeArgs[5] = new Argument("time_start_up", global_startdate, true);
        attributeArgs[6] = new Argument("time_ending_up", global_enddate, true);
        attributeArgs[7] = new Argument("time_delta", global_deltat, true);
        attributeArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_attribute.initialize(attributeArgs);
        
        Link igrass_link_attribute = new Link(null, "attribute");
        igrass_link_attribute.connect(igrass_attribute, igrass_attribute.getOutputExchangeItem(0),
                h2ca, h2ca.getInputExchangeItem(1));

        OutputGrassCoverageWriter dummyOut = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments = new Argument[9];
        dummyArguments[0] = new Argument("ograss", "h2ca", true);
        dummyArguments[1] = new Argument("quantityid", "h2ca", true);
        dummyArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments[3] = new Argument("location", global_location, true);
        dummyArguments[4] = new Argument("mapset", global_mapset, true);
        dummyArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOut.initialize(dummyArguments);

        Link dummyOutLink = new Link(null, "h2ca");
        dummyOutLink.connect(h2ca, h2ca.getOutputExchangeItem(0), dummyOut, dummyOut
                .getInputExchangeItem(0));

        assertTrue(dummyOutLink.isConnected());

        igrass_attribute.prepare();
        h2ca.prepare();
        IValueSet valueSet = h2ca.getValues(null, dummyOutLink.getID());
        igrass_attribute.finish();
        h2ca.finish();

        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) valueSet).getGridCoverage2D();

        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.h2ca_forGradient, 0);

        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }



}
