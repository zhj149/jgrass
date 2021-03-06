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
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.diameters.h_diameters;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
/**
 * It test the h_diameters classes.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestDiameters extends JGrassTestCase {
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
        h_diameters diameters = new h_diameters(out, err);
        Argument[] diametersArgs = new Argument[8];
        diametersArgs[0] = new Argument("grassdb", global_grassdb, true);
        diametersArgs[1] = new Argument("location", global_location, true);
        diametersArgs[2] = new Argument("mapset", global_mapset, true);
        diametersArgs[3] = new Argument("time_start_up", global_startdate, true);
        diametersArgs[4] = new Argument("time_ending_up", global_enddate, true);
        diametersArgs[5] = new Argument("time_delta", global_deltat, true);
        diametersArgs[6] = new Argument("remotedburl", global_remotedb, true);
        diametersArgs[7] = new Argument("mode", "0", true);
        diameters.initialize(diametersArgs);

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
                diameters, diameters.getInputExchangeItem(0));

     
        OutputGrassRasterMap diameersEI = new OutputGrassRasterMap(out, err);
        Argument[] diametersArguments = new Argument[9];
        diametersArguments[0] = new Argument("ograss", "diameters", true);
        diametersArguments[1] = new Argument("quantityid", "diameters", true);
        diametersArguments[2] = new Argument("grassdb", global_grassdb, true);
        diametersArguments[3] = new Argument("location", global_location, true);
        diametersArguments[4] = new Argument("mapset", global_mapset, true);
        diametersArguments[5] = new Argument("time_start_up", global_startdate, true);
        diametersArguments[6] = new Argument("time_ending_up", global_enddate, true);
        diametersArguments[7] = new Argument("time_delta", global_deltat, true);
        diametersArguments[8] = new Argument("remotedburl", global_remotedb, true);
        diameersEI.initialize(diametersArguments);

        Link cp9dummyOutLink = new Link(null, "diameters");
        cp9dummyOutLink.connect(diameters, diameters.getOutputExchangeItem(0), diameersEI, diameersEI
                .getInputExchangeItem(0));
     
        igrass_flow.prepare();
    

        diameters.prepare();
        IValueSet diametersValueSet = diameters.getValues(null,cp9dummyOutLink.getID());
        GridCoverage2D diametersGC = ((JGrassGridCoverageValueSet) diametersValueSet).getGridCoverage2D();
        GridCoverage2D view = diametersGC.view(ViewType.GEOPHYSICS);
        PlanarImage diametersImage = (PlanarImage) view.getRenderedImage();
        

        checkMatrixEqual(diametersImage, GrassMapTest.diametersData,0.01);
        diametersValueSet = diameters.getValues(null, cp9dummyOutLink.getID());

        igrass_flow.finish();
        diameters.finish();
        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }

}
