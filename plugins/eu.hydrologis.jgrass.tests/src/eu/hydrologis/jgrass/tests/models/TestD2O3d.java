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
import eu.hydrologis.jgrass.models.h.d2o3d.h_d2o3d;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
/**
 * It test the h_D2o classes, which estimate the distance to outlet.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestD2O3d extends JGrassTestCase {
    @SuppressWarnings("nls")
    public void testDiameters() throws IOException {
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
        
        h_d2o3d d2o3d = new h_d2o3d(out, err);
        Argument[] d2o3dArgs = new Argument[8];
        d2o3dArgs[0] = new Argument("grassdb", global_grassdb, true);
        d2o3dArgs[1] = new Argument("location", global_location, true);
        d2o3dArgs[2] = new Argument("mapset", global_mapset, true);
        d2o3dArgs[3] = new Argument("time_start_up", global_startdate, true);
        d2o3dArgs[4] = new Argument("time_ending_up", global_enddate, true);
        d2o3dArgs[5] = new Argument("time_delta", global_deltat, true);
        d2o3dArgs[6] = new Argument("remotedburl", global_remotedb, true);
        d2o3dArgs[7] = new Argument("mode", "0", true);
        d2o3d.initialize(d2o3dArgs);

        DummyInputGrassCoverageMap igrass_pit = new DummyInputGrassCoverageMap(out, err, GrassMapTest.pitData);
        Argument[] pitArgs = new Argument[9];
        pitArgs[0] = new Argument("igrass", "pit", true);
        pitArgs[1] = new Argument("quantityid", "pit", true);
        pitArgs[2] = new Argument("grassdb", global_grassdb, true);
        pitArgs[3] = new Argument("location", global_location, true);
        pitArgs[4] = new Argument("mapset", global_mapset, true);
        pitArgs[5] = new Argument("time_start_up", global_startdate, true);
        pitArgs[6] = new Argument("time_ending_up", global_enddate, true);
        pitArgs[7] = new Argument("time_delta", global_deltat, true);
        pitArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_pit.initialize(pitArgs);
        
        Link igrass_link_pit = new Link(null, "pit");
        igrass_link_pit.connect(igrass_pit, igrass_pit.getOutputExchangeItem(0),
                d2o3d, d2o3d.getInputExchangeItem(0));

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
                d2o3d, d2o3d.getInputExchangeItem(0));

     
        OutputGrassCoverageWriter link_d2o = new OutputGrassCoverageWriter(out, err);
        Argument[] outputArguments = new Argument[9];
        outputArguments[0] = new Argument("ograss", "d2o3d", true);
        outputArguments[1] = new Argument("quantityid", "d2o3d", true);
        outputArguments[2] = new Argument("grassdb", global_grassdb, true);
        outputArguments[3] = new Argument("location", global_location, true);
        outputArguments[4] = new Argument("mapset", global_mapset, true);
        outputArguments[5] = new Argument("time_start_up", global_startdate, true);
        outputArguments[6] = new Argument("time_ending_up", global_enddate, true);
        outputArguments[7] = new Argument("time_delta", global_deltat, true);
        outputArguments[8] = new Argument("remotedburl", global_remotedb, true);
        link_d2o.initialize(outputArguments);

        Link ograss_link_d2o3d = new Link(null, "d2o3d");
        ograss_link_d2o3d.connect(d2o3d, d2o3d.getOutputExchangeItem(0), link_d2o, link_d2o
                .getInputExchangeItem(0));

        igrass_pit.prepare();
        igrass_flow.prepare();
        d2o3d.prepare();
        
        assertTrue(linkMessage, ograss_link_d2o3d.isConnected());
        assertTrue(linkMessage, igrass_link_flow.isConnected());
        assertTrue(linkMessage, igrass_link_pit.isConnected());
        

        IValueSet d2oValueSet = d2o3d.getValues(null, ograss_link_d2o3d.getID());
        GridCoverage2D d2oGC = ((JGrassGridCoverageValueSet) d2oValueSet).getGridCoverage2D();
        GridCoverage2D view = d2oGC.view(ViewType.GEOPHYSICS);
        PlanarImage d2oImage = (PlanarImage) view.getRenderedImage();
        
        
        checkMatrixEqual(d2oImage, GrassMapTest.d2o3dData,0.01);
        d2oValueSet = d2o3d.getValues(null, ograss_link_d2o3d.getID());


        
        
        
        // set active region to the needed
        igrass_pit.finish();
        igrass_flow.finish();
        d2o3d.finish();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }
}
