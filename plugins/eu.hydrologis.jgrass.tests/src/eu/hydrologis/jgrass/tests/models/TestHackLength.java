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
import eu.hydrologis.jgrass.models.h.hacklength.h_hacklength;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestHackLength extends JGrassTestCase {
    @SuppressWarnings("nls")
    public void testHackLength() throws IOException {
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
        h_hacklength hacklength= new h_hacklength(out, err);
        Argument[] hackLengthArgs = new Argument[7];
        hackLengthArgs[0] = new Argument("grassdb", global_grassdb, true);
        hackLengthArgs[1] = new Argument("location", global_location, true);
        hackLengthArgs[2] = new Argument("mapset", global_mapset, true);
        hackLengthArgs[3] = new Argument("time_start_up", global_startdate, true);
        hackLengthArgs[4] = new Argument("time_ending_up", global_enddate, true);
        hackLengthArgs[5] = new Argument("time_delta", global_deltat, true);
        hackLengthArgs[6] = new Argument("remotedburl", global_remotedb, true);
        hacklength.initialize(hackLengthArgs);

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
                hacklength, hacklength.getInputExchangeItem(1));
        
        
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
                hacklength, hacklength.getInputExchangeItem(1));

     
        OutputGrassCoverageWriter ograss_hlength = new OutputGrassCoverageWriter(out, err);
        Argument[] hacklengthArguments = new Argument[9];
        hacklengthArguments[0] = new Argument("ograss", "hackl", true);
        hacklengthArguments[1] = new Argument("quantityid", "hackl", true);
        hacklengthArguments[2] = new Argument("grassdb", global_grassdb, true);
        hacklengthArguments[3] = new Argument("location", global_location, true);
        hacklengthArguments[4] = new Argument("mapset", global_mapset, true);
        hacklengthArguments[5] = new Argument("time_start_up", global_startdate, true);
        hacklengthArguments[6] = new Argument("time_ending_up", global_enddate, true);
        hacklengthArguments[7] = new Argument("time_delta", global_deltat, true);
        hacklengthArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_hlength.initialize(hacklengthArguments);

        Link ograss_link_hlength = new Link(null, "hackl");
        ograss_link_hlength.connect(hacklength, hacklength.getOutputExchangeItem(0), ograss_hlength, ograss_hlength
                .getInputExchangeItem(0));
      

      
       

        igrass_flow.prepare();
        igrass_tca.prepare();
        hacklength.prepare();
        
        IValueSet hlengthValueSet = hacklength.getValues(null,ograss_link_hlength.getID());
        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) hlengthValueSet).getGridCoverage2D();

        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.hacklengthData,0.2);
        hlengthValueSet = hacklength.getValues(null, ograss_link_hlength.getID());


        
        
        
        // set active region to the needed
        igrass_flow.finish();
        igrass_tca.finish();
        hacklength.finish();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }

}
