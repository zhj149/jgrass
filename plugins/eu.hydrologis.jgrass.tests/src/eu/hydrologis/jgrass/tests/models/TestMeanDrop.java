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
import eu.hydrologis.jgrass.models.h.meandrop.h_meandrop;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
/**
 * It test the h_meanddrop classes with mode=0.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestMeanDrop extends JGrassTestCase{
    
    @SuppressWarnings("nls")
    public void testMeanDrop() throws IOException {
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
        h_meandrop meanDrop = new h_meandrop(out, err);
        Argument[] meandropArgs = new Argument[7];
        meandropArgs[0] = new Argument("grassdb", global_grassdb, true);
        meandropArgs[1] = new Argument("location", global_location, true);
        meandropArgs[2] = new Argument("mapset", global_mapset, true);
        meandropArgs[3] = new Argument("time_start_up", global_startdate, true);
        meandropArgs[4] = new Argument("time_ending_up", global_enddate, true);
        meandropArgs[5] = new Argument("time_delta", global_deltat, true);
        meandropArgs[6] = new Argument("remotedburl", global_remotedb, true);

        meanDrop.initialize(meandropArgs);

        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err, GrassMapTest.mflowDataBorder);
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
                meanDrop, meanDrop.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_tca = new DummyInputGrassCoverageMap(out, err, GrassMapTest.tcaData);
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
        igrass_link_tca.connect(igrass_tca, igrass_tca.getOutputExchangeItem(0),
                meanDrop, meanDrop.getInputExchangeItem(1));

        DummyInputGrassCoverageMap igrass_summ = new DummyInputGrassCoverageMap(out, err, GrassMapTest.pitData);
        Argument[] summArgs = new Argument[9];
        summArgs[0] = new Argument("igrass", "summ", true);
        summArgs[1] = new Argument("quantityid", "summ", true);
        summArgs[2] = new Argument("grassdb", global_grassdb, true);
        summArgs[3] = new Argument("location", global_location, true);
        summArgs[4] = new Argument("mapset", global_mapset, true);
        summArgs[5] = new Argument("time_start_up", global_startdate, true);
        summArgs[6] = new Argument("time_ending_up", global_enddate, true);
        summArgs[7] = new Argument("time_delta", global_deltat, true);
        summArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_summ.initialize(summArgs);

        Link igrass_link_summ = new Link(null, "summ");
        igrass_link_summ.connect(igrass_summ, igrass_summ.getOutputExchangeItem(0),
                meanDrop, meanDrop.getInputExchangeItem(2));
        
        OutputGrassCoverageWriter ograss_meanDrop = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyMeanArguments = new Argument[9];
        dummyMeanArguments[0] = new Argument("ograss", "meandrop", true);
        dummyMeanArguments[1] = new Argument("quantityid", "meandrop", true);
        dummyMeanArguments[2] = new Argument("grassdb", global_grassdb, true);
        dummyMeanArguments[3] = new Argument("location", global_location, true);
        dummyMeanArguments[4] = new Argument("mapset", global_mapset, true);
        dummyMeanArguments[5] = new Argument("time_start_up", global_startdate, true);
        dummyMeanArguments[6] = new Argument("time_ending_up", global_enddate, true);
        dummyMeanArguments[7] = new Argument("time_delta", global_deltat, true);
        dummyMeanArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_meanDrop.initialize(dummyMeanArguments);

        Link ograss_link_meandrop = new Link(null, "meandrop");
        ograss_link_meandrop.connect(meanDrop, meanDrop.getOutputExchangeItem(0), ograss_meanDrop, ograss_meanDrop
                .getInputExchangeItem(0));
       

        igrass_flow.prepare();
        igrass_tca.prepare();
        igrass_summ.prepare();
        meanDrop.prepare();
        assertTrue(linkMessage, ograss_link_meandrop.isConnected());
        assertTrue(linkMessage, igrass_link_flow.isConnected());
        assertTrue(linkMessage, igrass_link_summ.isConnected());
        assertTrue(linkMessage, ograss_link_meandrop.isConnected());
        
        
        IValueSet meanDropValueSet = meanDrop.getValues(null, ograss_link_meandrop.getID());
        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) meanDropValueSet).getGridCoverage2D();
        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.meandropData,0.01);

        
        
        
        // set active region to the needed
        igrass_flow.finish();
        igrass_tca.finish();
        igrass_summ.finish();

        meanDrop.finish();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }




}
