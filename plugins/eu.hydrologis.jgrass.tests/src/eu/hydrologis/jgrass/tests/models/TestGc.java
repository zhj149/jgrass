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
import eu.hydrologis.jgrass.models.h.gc.h_gc;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestGc extends JGrassTestCase{
    
    
    public void testGc() throws IOException{
        
        
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
        h_gc gc= new h_gc(out, err);
        Argument[] gcArgs = new Argument[8];
        gcArgs[0] = new Argument("grassdb", global_grassdb, true);
        gcArgs[1] = new Argument("location", global_location, true);
        gcArgs[2] = new Argument("mapset", global_mapset, true);
        gcArgs[3] = new Argument("time_start_up", global_startdate, true);
        gcArgs[4] = new Argument("time_ending_up", global_enddate, true);
        gcArgs[5] = new Argument("time_delta", global_deltat, true);
        gcArgs[6] = new Argument("remotedburl", global_remotedb, true);
        gcArgs[7] = new Argument("thgrad", "7", true);
        gc.initialize(gcArgs);

        DummyInputGrassCoverageMap igrass_cp9 = new DummyInputGrassCoverageMap(out, err, GrassMapTest.cp9Data);
        Argument[] cp9Args = new Argument[9];
        cp9Args[0] = new Argument("igrass", "cp9", true);
        cp9Args[1] = new Argument("quantityid", "cp9", true);
        cp9Args[2] = new Argument("grassdb", global_grassdb, true);
        cp9Args[3] = new Argument("location", global_location, true);
        cp9Args[4] = new Argument("mapset", global_mapset, true);
        cp9Args[5] = new Argument("time_start_up", global_startdate, true);
        cp9Args[6] = new Argument("time_ending_up", global_enddate, true);
        cp9Args[7] = new Argument("time_delta", global_deltat, true);
        cp9Args[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_cp9.initialize(cp9Args);
        

        Link igrass_link_pit = new Link(null, "cp9");
        igrass_link_pit.connect(igrass_cp9, igrass_cp9.getOutputExchangeItem(0),
                gc, gc.getInputExchangeItem(2));
        
        DummyInputGrassCoverageMap igrass_slope = new DummyInputGrassCoverageMap(out, err, GrassMapTest.slopeData);
        Argument[] slopeArgs = new Argument[9];
        slopeArgs[0] = new Argument("igrass", "slope", true);
        slopeArgs[1] = new Argument("quantityid", "slope", true);
        slopeArgs[2] = new Argument("grassdb", global_grassdb, true);
        slopeArgs[3] = new Argument("location", global_location, true);
        slopeArgs[4] = new Argument("mapset", global_mapset, true);
        slopeArgs[5] = new Argument("time_start_up", global_startdate, true);
        slopeArgs[6] = new Argument("time_ending_up", global_enddate, true);
        slopeArgs[7] = new Argument("time_delta", global_deltat, true);
        slopeArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_slope.initialize(slopeArgs);
        

        Link igrass_link_flow = new Link(null, "slope");
        igrass_link_flow.connect(igrass_slope, igrass_slope.getOutputExchangeItem(0),
                gc, gc.getInputExchangeItem(0));
        
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
                gc, gc.getInputExchangeItem(1));
      
     
        OutputGrassCoverageWriter ograss_gc11 = new OutputGrassCoverageWriter(out, err);
        Argument[] gc11Arguments = new Argument[9];
        gc11Arguments[0] = new Argument("ograss", "class", true);
        gc11Arguments[1] = new Argument("quantityid", "class", true);
        gc11Arguments[2] = new Argument("grassdb", global_grassdb, true);
        gc11Arguments[3] = new Argument("location", global_location, true);
        gc11Arguments[4] = new Argument("mapset", global_mapset, true);
        gc11Arguments[5] = new Argument("time_start_up", global_startdate, true);
        gc11Arguments[6] = new Argument("time_ending_up", global_enddate, true);
        gc11Arguments[7] = new Argument("time_delta", global_deltat, true);
        gc11Arguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_gc11.initialize(gc11Arguments);

        Link ograss_link_gc11 = new Link(null, "class");
        ograss_link_gc11.connect(gc, gc.getOutputExchangeItem(0), ograss_gc11, ograss_gc11
                .getInputExchangeItem(0));
      
        OutputGrassCoverageWriter ograss_gc5 = new OutputGrassCoverageWriter(out, err);
        Argument[] gc5Arguments = new Argument[9];
        gc5Arguments[0] = new Argument("ograss", "aggclass", true);
        gc5Arguments[1] = new Argument("quantityid", "aggclass", true);
        gc5Arguments[2] = new Argument("grassdb", global_grassdb, true);
        gc5Arguments[3] = new Argument("location", global_location, true);
        gc5Arguments[4] = new Argument("mapset", global_mapset, true);
        gc5Arguments[5] = new Argument("time_start_up", global_startdate, true);
        gc5Arguments[6] = new Argument("time_ending_up", global_enddate, true);
        gc5Arguments[7] = new Argument("time_delta", global_deltat, true);
        gc5Arguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_gc5.initialize(gc5Arguments);

        Link ograss_link_gc5 = new Link(null, "aggclass");
        ograss_link_gc5.connect(gc, gc.getOutputExchangeItem(0), ograss_gc5, ograss_gc5
                .getInputExchangeItem(1));
      
       

        igrass_slope.prepare();
        igrass_cp9.prepare();
        igrass_net.prepare();
        gc.prepare();
        
        IValueSet gcValueSet = gc.getValues(null,ograss_link_gc11.getID());
        GridCoverage2D rasterData = ((JGrassGridCoverageValueSet) gcValueSet).getGridCoverage2D();

        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.cp9GCData,0);
        gcValueSet = gc.getValues(null,ograss_link_gc5.getID());

        rasterData = ((JGrassGridCoverageValueSet) gcValueSet).getGridCoverage2D();

        checkMatrixEqual(rasterData.getRenderedImage(), GrassMapTest.cp3GCData,0);

        
        
        
        // set active region to the needed
        igrass_slope.finish();

        gc.finish();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }
        
        
        
        
        
    

}
