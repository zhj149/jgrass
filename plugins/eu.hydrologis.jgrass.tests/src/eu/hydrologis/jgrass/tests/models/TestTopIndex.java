package eu.hydrologis.jgrass.tests.models;

import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.JGrassGridCoverage2D;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.topindex.h_topindex;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestTopIndex extends JGrassTestCase {
    @SuppressWarnings("nls")
    public void testTopIndex() throws IOException {
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
        h_topindex topIndex = new h_topindex(out, err);
        Argument[] topindexArgs = new Argument[7];
        topindexArgs[0] = new Argument("grassdb", global_grassdb, true);
        topindexArgs[1] = new Argument("location", global_location, true);
        topindexArgs[2] = new Argument("mapset", global_mapset, true);
        topindexArgs[3] = new Argument("time_start_up", global_startdate, true);
        topindexArgs[4] = new Argument("time_ending_up", global_enddate, true);
        topindexArgs[5] = new Argument("time_delta", global_deltat, true);
        topindexArgs[6] = new Argument("remotedburl", global_remotedb, true);

        topIndex.initialize(topindexArgs);

        DummyInputGrassCoverageMap igrasscoverage_slope = new DummyInputGrassCoverageMap(out, err,
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
        igrasscoverage_slope.initialize(slope);

        Link igrass_link_slope = new Link(null, "slope");
        igrass_link_slope.connect(igrasscoverage_slope, igrasscoverage_slope.getOutputExchangeItem(0), topIndex,
                topIndex.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrasscoverage_tca = new DummyInputGrassCoverageMap(out, err,
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
        igrasscoverage_tca.initialize(tcaArgs);

        Link igrass_link_tca = new Link(null, "tca");
        igrass_link_tca.connect(igrasscoverage_tca, igrasscoverage_tca.getOutputExchangeItem(0), topIndex, topIndex
                .getInputExchangeItem(1));

        OutputGrassCoverageWriter index = new OutputGrassCoverageWriter(out, err);
        Argument[] indexArguments = new Argument[9];
        indexArguments[0] = new Argument("ograss", "topindex", true);
        indexArguments[1] = new Argument("quantityid", "topindex", true);
        indexArguments[2] = new Argument("grassdb", global_grassdb, true);
        indexArguments[3] = new Argument("location", global_location, true);
        indexArguments[4] = new Argument("mapset", global_mapset, true);
        indexArguments[5] = new Argument("time_start_up", global_startdate, true);
        indexArguments[6] = new Argument("time_ending_up", global_enddate, true);
        indexArguments[7] = new Argument("time_delta", global_deltat, true);
        indexArguments[8] = new Argument("remotedburl", global_remotedb, true);
        index.initialize(indexArguments);

        Link ograss_link_index = new Link(null, "topindex");
        ograss_link_index.connect(topIndex, topIndex.getOutputExchangeItem(0), index, index
                .getInputExchangeItem(0));

        igrasscoverage_slope.prepare();
        igrasscoverage_tca.prepare();
        topIndex.prepare();
        String linkMessage = "there is a prblem in a link";

        assertTrue(linkMessage, igrass_link_slope.isConnected());
        assertTrue(linkMessage, igrass_link_tca.isConnected());

        assertTrue(linkMessage, ograss_link_index.isConnected());

        IValueSet netValueSet = topIndex.getValues(null, ograss_link_index.getID());
        GridCoverage2D topindex = ((JGrassGridCoverageValueSet) netValueSet).getGridCoverage2D();

        checkMatrixEqual(topindex.getRenderedImage(), GrassMapTest.topIndexData, 0.01);
        netValueSet = topIndex.getValues(null, ograss_link_index.getID());

        igrasscoverage_slope.finish();
        igrasscoverage_tca.finish();
        topIndex.finish();
        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }

}
