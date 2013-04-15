package eu.hydrologis.jgrass.tests.models;

import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.splitsubbasin.h_splitsubbasin;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

public class TestSplitSubBasin extends JGrassTestCase {
    private String hackorder = null;
    private String threshold = null;
    private Link igrass_link_flow;
    private Link igrass_link_tca;
    private Link ograss_link_netNumber;
    private Link ograss_link_subBasin;
    private Link igrass_link_hacks;
    private h_splitsubbasin splitSubBasin;
    private DummyInputGrassCoverageMap igrass_flow;
    private ModelsBackbone igrass_hacks;
    private DummyInputGrassCoverageMap igrass_tca;
    private JGrassMapEnvironment jGrassMapEnvironment;

    public void testSplitSubBasin() throws Exception {
        hackorder = "2";
        threshold = "10";
        jGrassMapEnvironment = GrassMapTest.jME;

        // set active region to the needed
        JGrassRegion fileRegion = jGrassMapEnvironment.getFileRegion();
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
        splitSubBasin = new h_splitsubbasin(out, err);
        Argument[] splitARgs = new Argument[9];
        splitARgs[0] = new Argument("grassdb", global_grassdb, true);
        splitARgs[1] = new Argument("location", global_location, true);
        splitARgs[2] = new Argument("mapset", global_mapset, true);
        splitARgs[3] = new Argument("time_start_up", global_startdate, true);
        splitARgs[4] = new Argument("time_ending_up", global_enddate, true);
        splitARgs[5] = new Argument("time_delta", global_deltat, true);
        splitARgs[6] = new Argument("remotedburl", global_remotedb, true);
        splitARgs[7] = new Argument("hackorder", "2", true);
        splitARgs[8] = new Argument("threshold", "2", true);
        splitSubBasin.initialize(splitARgs);

        igrass_flow = new DummyInputGrassCoverageMap(out, err, GrassMapTest.mflowDataBorder);
        Argument[] flowArguments = new Argument[9];
        flowArguments[0] = new Argument("igrass", "flow", true);
        flowArguments[1] = new Argument("quantityid", "flow", true);
        flowArguments[2] = new Argument("grassdb", global_grassdb, true);
        flowArguments[3] = new Argument("location", global_location, true);
        flowArguments[4] = new Argument("mapset", global_mapset, true);
        flowArguments[5] = new Argument("time_start_up", global_startdate, true);
        flowArguments[6] = new Argument("time_ending_up", global_enddate, true);
        flowArguments[7] = new Argument("time_delta", global_deltat, true);
        flowArguments[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_flow.initialize(flowArguments);

        igrass_link_flow = new Link(null, "flow");
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), splitSubBasin, splitSubBasin
                .getInputExchangeItem(0));

        igrass_hacks = new DummyInputGrassCoverageMap(out, err, GrassMapTest.hackstream);
        Argument[] hacksArguments = new Argument[9];
        hacksArguments[0] = new Argument("igrass", "hacks", true);
        hacksArguments[1] = new Argument("quantityid", "hacks", true);
        hacksArguments[2] = new Argument("grassdb", global_grassdb, true);
        hacksArguments[3] = new Argument("location", global_location, true);
        hacksArguments[4] = new Argument("mapset", global_mapset, true);
        hacksArguments[5] = new Argument("time_start_up", global_startdate, true);
        hacksArguments[6] = new Argument("time_ending_up", global_enddate, true);
        hacksArguments[7] = new Argument("time_delta", global_deltat, true);
        hacksArguments[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_hacks.initialize(flowArguments);

        igrass_link_hacks = new Link(null, "hacks");
        igrass_link_hacks.connect(igrass_hacks, igrass_hacks.getOutputExchangeItem(0), splitSubBasin, splitSubBasin
                .getInputExchangeItem(1));

        igrass_tca = new DummyInputGrassCoverageMap(out, err, GrassMapTest.tcaData);
        Argument[] tcaArguments = new Argument[9];
        tcaArguments[0] = new Argument("igrass", "tca", true);
        tcaArguments[1] = new Argument("quantityid", "tca", true);
        tcaArguments[2] = new Argument("grassdb", global_grassdb, true);
        tcaArguments[3] = new Argument("location", global_location, true);
        tcaArguments[4] = new Argument("mapset", global_mapset, true);
        tcaArguments[5] = new Argument("time_start_up", global_startdate, true);
        tcaArguments[6] = new Argument("time_ending_up", global_enddate, true);
        tcaArguments[7] = new Argument("time_delta", global_deltat, true);
        tcaArguments[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_tca.initialize(flowArguments);

        igrass_link_tca = new Link(null, "tca");
        igrass_link_tca.connect(igrass_tca, igrass_tca.getOutputExchangeItem(0), splitSubBasin, splitSubBasin
                .getInputExchangeItem(2));
        OutputGrassCoverageWriter ograss_netNum = new OutputGrassCoverageWriter(out, err);
        Argument[] netNumArguments = new Argument[9];
        netNumArguments[0] = new Argument("ograss", "netnumber", true);
        netNumArguments[1] = new Argument("quantityid", "netnumber", true);
        netNumArguments[2] = new Argument("grassdb", global_grassdb, true);
        netNumArguments[3] = new Argument("location", global_location, true);
        netNumArguments[4] = new Argument("mapset", global_mapset, true);
        netNumArguments[5] = new Argument("time_start_up", global_startdate, true);
        netNumArguments[6] = new Argument("time_ending_up", global_enddate, true);
        netNumArguments[7] = new Argument("time_delta", global_deltat, true);
        netNumArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_netNum.initialize(netNumArguments);

        ograss_link_netNumber = new Link(null, "netnumber");
        ograss_link_netNumber.connect(splitSubBasin, splitSubBasin.getOutputExchangeItem(0), ograss_netNum, ograss_netNum
                .getInputExchangeItem(0));
        OutputGrassCoverageWriter ograss_subBasin = new OutputGrassCoverageWriter(out, err);
        Argument[] subBasinArguments1 = new Argument[9];
        subBasinArguments1[0] = new Argument("ograss", "subbasin", true);
        subBasinArguments1[1] = new Argument("quantityid", "subbasin", true);
        subBasinArguments1[2] = new Argument("grassdb", global_grassdb, true);
        subBasinArguments1[3] = new Argument("location", global_location, true);
        subBasinArguments1[4] = new Argument("mapset", global_mapset, true);
        subBasinArguments1[5] = new Argument("time_start_up", global_startdate, true);
        subBasinArguments1[6] = new Argument("time_ending_up", global_enddate, true);
        subBasinArguments1[7] = new Argument("time_delta", global_deltat, true);
        subBasinArguments1[8] = new Argument("remotedburl", global_remotedb, true);

        ograss_subBasin.initialize(subBasinArguments1);
        ograss_link_subBasin = new Link(null, "subbasin");
        ograss_link_subBasin.connect(splitSubBasin, splitSubBasin.getOutputExchangeItem(1), ograss_subBasin, ograss_subBasin
                .getInputExchangeItem(0));

        String linkMessage = "there is a prblem in a link";
        assertTrue(linkMessage, igrass_link_flow.isConnected());
        assertTrue(linkMessage, igrass_link_hacks.isConnected());
        assertTrue(linkMessage, igrass_link_hacks.isConnected());
        assertTrue(linkMessage, ograss_link_netNumber.isConnected());
        assertTrue(linkMessage, ograss_link_subBasin.isConnected());

        igrass_flow.prepare();
        igrass_hacks.prepare();
        igrass_tca.prepare();
        splitSubBasin.prepare();

        IValueSet splitSubBasinValueSet = splitSubBasin.getValues(null, ograss_link_subBasin.getID());
        GridCoverage2D splitSubBasinRasterData = ((JGrassGridCoverageValueSet) splitSubBasinValueSet).getGridCoverage2D();
        checkMatrixEqual(splitSubBasinRasterData.getRenderedImage(), GrassMapTest.splitSubBasinDataJG, 0.01);

        IValueSet netNumberingValueSet = splitSubBasin.getValues(null, ograss_link_netNumber.getID());
        GridCoverage2D netNumberingRasterData = ((JGrassGridCoverageValueSet) netNumberingValueSet).getGridCoverage2D();
        checkMatrixEqual(netNumberingRasterData.getRenderedImage(), GrassMapTest.netNumberingChannelDataJG, 0.01);

        igrass_flow.finish();
        igrass_hacks.finish();
        igrass_tca.finish();
        splitSubBasin.finish();
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(), activeRegion);

    }

}