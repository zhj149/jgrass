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
import eu.hydrologis.jgrass.models.h.hacklength3d.h_hacklength3d;
import eu.hydrologis.jgrass.models.h.hackstream.h_hackstream;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestHackStream extends JGrassTestCase{
    
    
    public void testHackStream() throws IOException {
        
    
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
    h_hackstream hackstream= new h_hackstream(out, err);
    Argument[] hackStreamArgs = new Argument[8];
    hackStreamArgs[0] = new Argument("grassdb", global_grassdb, true);
    hackStreamArgs[1] = new Argument("location", global_location, true);
    hackStreamArgs[2] = new Argument("mapset", global_mapset, true);
    hackStreamArgs[3] = new Argument("time_start_up", global_startdate, true);
    hackStreamArgs[4] = new Argument("time_ending_up", global_enddate, true);
    hackStreamArgs[5] = new Argument("time_delta", global_deltat, true);
    hackStreamArgs[6] = new Argument("remotedburl", global_remotedb, true);
    hackStreamArgs[7] = new Argument("mode", "0", true);
    hackstream.initialize(hackStreamArgs);

    DummyInputGrassCoverageMap igrass_length = new DummyInputGrassCoverageMap(out, err, GrassMapTest.hacklengthData);
    Argument[] hacklArgs = new Argument[9];
    hacklArgs[0] = new Argument("igrass", "hackl", true);
    hacklArgs[1] = new Argument("quantityid", "hackl", true);
    hacklArgs[2] = new Argument("grassdb", global_grassdb, true);
    hacklArgs[3] = new Argument("location", global_location, true);
    hacklArgs[4] = new Argument("mapset", global_mapset, true);
    hacklArgs[5] = new Argument("time_start_up", global_startdate, true);
    hacklArgs[6] = new Argument("time_ending_up", global_enddate, true);
    hacklArgs[7] = new Argument("time_delta", global_deltat, true);
    hacklArgs[8] = new Argument("remotedburl", global_remotedb, true);
    igrass_length.initialize(hacklArgs);
    

    Link igrass_link_pit = new Link(null, "hackl");
    igrass_link_pit.connect(igrass_length, igrass_length.getOutputExchangeItem(0),
            hackstream, hackstream.getInputExchangeItem(2));
    
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
            hackstream, hackstream.getInputExchangeItem(0));
    
    DummyInputGrassCoverageMap igrass_net = new DummyInputGrassCoverageMap(out, err, GrassMapTest.extractNet1Data);
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
            hackstream, hackstream.getInputExchangeItem(0));
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
            hackstream, hackstream.getInputExchangeItem(1));

 
    OutputGrassCoverageWriter ograss_stream = new OutputGrassCoverageWriter(out, err);
    Argument[] hackstreamArguments = new Argument[9];
    hackstreamArguments[0] = new Argument("ograss", "hacks", true);
    hackstreamArguments[1] = new Argument("quantityid", "hacks", true);
    hackstreamArguments[2] = new Argument("grassdb", global_grassdb, true);
    hackstreamArguments[3] = new Argument("location", global_location, true);
    hackstreamArguments[4] = new Argument("mapset", global_mapset, true);
    hackstreamArguments[5] = new Argument("time_start_up", global_startdate, true);
    hackstreamArguments[6] = new Argument("time_ending_up", global_enddate, true);
    hackstreamArguments[7] = new Argument("time_delta", global_deltat, true);
    hackstreamArguments[8] = new Argument("remotedburl", global_remotedb, true);
    ograss_stream.initialize(hackstreamArguments);

    Link ograss_link_hlength = new Link(null, "hacks");
    ograss_link_hlength.connect(hackstream, hackstream.getOutputExchangeItem(0), ograss_stream, ograss_stream
            .getInputExchangeItem(0));
  

  
   

    igrass_flow.prepare();
    igrass_tca.prepare();
    igrass_length.prepare();
    hackstream.prepare();
    
    IValueSet hstreamValueSet = hackstream.getValues(null,ograss_link_hlength.getID());
    GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) hstreamValueSet).getGridCoverage2D();

    checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.hackstream,0);
    hstreamValueSet = hackstream.getValues(null, ograss_link_hlength.getID());


    
    
    
    // set active region to the needed
    igrass_flow.finish();
    igrass_tca.finish();
    hackstream.finish();
    JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
            activeRegion);
}

}
