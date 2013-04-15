package eu.hydrologis.jgrass.tests.models;

import java.awt.image.Raster;
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
import eu.hydrologis.jgrass.models.h.curvatures.h_curvatures;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
/**
 * It test the h_curvatures classes.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestCurvatures extends JGrassTestCase{
 


public void testCurvatures() throws IOException {
    

        JGrassMapEnvironment jGrassMapEnvironment = GrassMapTest.jME;

        // set active region to the needed
        JGrassRegion fileRegion = jGrassMapEnvironment
                .getFileRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment
                .getMAPSET().getAbsolutePath(), fileRegion);

        PrintStream out = System.out;
        PrintStream err = System.err;

        String global_grassdb = jGrassMapEnvironment.getLOCATION()
                .getParent();
        String global_location = jGrassMapEnvironment.getLOCATION()
                .getName();
        String global_mapset = jGrassMapEnvironment.getMAPSET()
                .getName();
        String global_startdate = "null";
        String global_enddate = "null";
        String global_deltat = "-1";
        String global_remotedb = "null";
        h_curvatures curvatures = new h_curvatures(out, err);
        Argument[] curvaturesARgs = new Argument[7];
        curvaturesARgs[0] = new Argument("grassdb", global_grassdb, true);
        curvaturesARgs[1] = new Argument("location", global_location, true);
        curvaturesARgs[2] = new Argument("mapset", global_mapset, true);
        curvaturesARgs[3] = new Argument("time_start_up",
                global_startdate, true);
        curvaturesARgs[4] = new Argument("time_ending_up", global_enddate,
                true);
        curvaturesARgs[5] = new Argument("time_delta", global_deltat, true);
        curvaturesARgs[6] = new Argument("remotedburl", global_remotedb,
                true);
        curvatures.initialize(curvaturesARgs);

        DummyInputGrassCoverageMap igrass_pit = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.pitData);
        Argument[] pitArguments = new Argument[9];
        pitArguments[0] = new Argument("igrass", "pit", true);
        pitArguments[1] = new Argument("quantityid", "pit", true);
        pitArguments[2] = new Argument("grassdb", global_grassdb,
                true);
        pitArguments[3] = new Argument("location", global_location,
                true);
        pitArguments[4] = new Argument("mapset", global_mapset, true);
        pitArguments[5] = new Argument("time_start_up",
                global_startdate, true);
        pitArguments[6] = new Argument("time_ending_up",
                global_enddate, true);
        pitArguments[7] = new Argument("time_delta", global_deltat,
                true);
        pitArguments[8] = new Argument("remotedburl",
                global_remotedb, true);
        igrass_pit.initialize(pitArguments);

        Link igrass_link_pit = new Link(null, "pit");
        igrass_link_pit.connect(igrass_pit, igrass_pit
                .getOutputExchangeItem(0), curvatures, curvatures
                .getInputExchangeItem(0));

        OutputGrassRasterMap ograss_tan = new OutputGrassRasterMap(
                out, err);
        Argument[] tanArguments = new Argument[9];
        tanArguments[0] = new Argument("ograss", "tang", true);
        tanArguments[1] = new Argument("quantityid", "tang", true);
        tanArguments[2] = new Argument("grassdb", global_grassdb,
                true);
        tanArguments[3] = new Argument("location", global_location,
                true);
        tanArguments[4] = new Argument("mapset", global_mapset, true);
        tanArguments[5] = new Argument("time_start_up",
                global_startdate, true);
        tanArguments[6] = new Argument("time_ending_up",
                global_enddate, true);
        tanArguments[7] = new Argument("time_delta", global_deltat,
                true);
        tanArguments[8] = new Argument("remotedburl",
                global_remotedb, true);
        ograss_tan.initialize(tanArguments);

        Link ograss_link_tan = new Link(null, "tang");
        ograss_link_tan.connect(curvatures, curvatures.getOutputExchangeItem(2),
                ograss_tan, ograss_tan.getInputExchangeItem(0));

        OutputGrassRasterMap ograss_prof = new OutputGrassRasterMap(
                out, err);
        Argument[] profArguments = new Argument[9];
        profArguments[0] = new Argument("ograss", "prof", true);
        profArguments[1] = new Argument("quantityid", "prof", true);
        profArguments[2] = new Argument("grassdb", global_grassdb,
                true);
        profArguments[3] = new Argument("location", global_location,
                true);
        profArguments[4] = new Argument("mapset", global_mapset, true);
        profArguments[5] = new Argument("time_start_up",
                global_startdate, true);
        profArguments[6] = new Argument("time_ending_up",
                global_enddate, true);
        profArguments[7] = new Argument("time_delta", global_deltat,
                true);
        profArguments[8] = new Argument("remotedburl",
                global_remotedb, true);
        ograss_prof.initialize(profArguments);

        Link ograss_link_prof = new Link(null, "prof");
        ograss_link_prof.connect(curvatures, curvatures.getOutputExchangeItem(0),
                ograss_prof, ograss_prof.getInputExchangeItem(0));
        
        OutputGrassRasterMap ograss_plan = new OutputGrassRasterMap(
                out, err);
        Argument[] planArguments = new Argument[9];
        planArguments[0] = new Argument("ograss", "plan", true);
        planArguments[1] = new Argument("quantityid", "plan", true);
        planArguments[2] = new Argument("grassdb", global_grassdb,
                true);
        planArguments[3] = new Argument("location", global_location,
                true);
        planArguments[4] = new Argument("mapset", global_mapset, true);
        planArguments[5] = new Argument("time_start_up",
                global_startdate, true);
        planArguments[6] = new Argument("time_ending_up",
                global_enddate, true);
        planArguments[7] = new Argument("time_delta", global_deltat,
                true);
        planArguments[8] = new Argument("remotedburl",
                global_remotedb, true);
        ograss_plan.initialize(planArguments);

        Link ograss_link_plan = new Link(null, "plan");
        ograss_link_plan.connect(curvatures, curvatures.getOutputExchangeItem(1),
                ograss_plan, ograss_plan.getInputExchangeItem(0));
      
        igrass_pit.prepare();
        curvatures.prepare();

        assertTrue(linkMessage, igrass_link_pit.isConnected());

        assertTrue(linkMessage, ograss_link_tan.isConnected());
        assertTrue(linkMessage, ograss_link_plan.isConnected());
        assertTrue(linkMessage, ograss_link_prof.isConnected());



        IValueSet profValueSet = curvatures.getValues(null, ograss_link_prof.getID());
        
        GridCoverage2D profGC = ((JGrassGridCoverageValueSet) profValueSet).getGridCoverage2D();
        GridCoverage2D view = profGC.view(ViewType.GEOPHYSICS);
        PlanarImage profImage = (PlanarImage) view.getRenderedImage();


        checkMatrixEqual(profImage,
                GrassMapTest.profData,0.0001);

        IValueSet planValueSet = curvatures.getValues(null, ograss_link_plan.getID());
        GridCoverage2D planGC = ((JGrassGridCoverageValueSet) planValueSet).getGridCoverage2D();
        view = planGC.view(ViewType.GEOPHYSICS);
        PlanarImage planImage = (PlanarImage) view.getRenderedImage();
 

        checkMatrixEqual(planImage,
                GrassMapTest.planData,0.0001);
        IValueSet tanValueSet = curvatures.getValues(null, ograss_link_tan.getID());
        GridCoverage2D tanGC = ((JGrassGridCoverageValueSet) tanValueSet).getGridCoverage2D();
        view = tanGC.view(ViewType.GEOPHYSICS);
        PlanarImage tanImage = (PlanarImage) view.getRenderedImage();
        checkMatrixEqual(tanImage,
                GrassMapTest.tanData,0.0001);


        // set active region to the needed
        igrass_pit.finish();
        curvatures.finish();
        JGrassRegion activeRegion = jGrassMapEnvironment
                .getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment
                .getMAPSET().getAbsolutePath(), activeRegion);
    }
}
