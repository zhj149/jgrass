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
import eu.hydrologis.jgrass.models.h.disteuclidea.h_disteuclidea;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
/**
 * It test the h_dist_euclidea classes.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestDistEuclidea extends JGrassTestCase {
    @SuppressWarnings("nls")
    public void testDistEuclidea() throws IOException {
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
        h_disteuclidea distEuclidea = new h_disteuclidea(out, err);
        Argument[] distEuclideaArgs = new Argument[7];
        distEuclideaArgs[0] = new Argument("grassdb", global_grassdb, true);
        distEuclideaArgs[1] = new Argument("location", global_location, true);
        distEuclideaArgs[2] = new Argument("mapset", global_mapset, true);
        distEuclideaArgs[3] = new Argument("time_start_up", global_startdate, true);
        distEuclideaArgs[4] = new Argument("time_ending_up", global_enddate, true);
        distEuclideaArgs[5] = new Argument("time_delta", global_deltat, true);
        distEuclideaArgs[6] = new Argument("remotedburl", global_remotedb, true);
        distEuclidea.initialize(distEuclideaArgs);

        DummyInputGrassCoverageMap igrass_flow = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.mflowDataBorder);
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
        igrass_link_flow.connect(igrass_flow, igrass_flow.getOutputExchangeItem(0), distEuclidea,
                distEuclidea.getInputExchangeItem(0));

        OutputGrassRasterMap distEuclideaEI = new OutputGrassRasterMap(out, err);
        Argument[] distEuclideaArguments = new Argument[9];
        distEuclideaArguments[0] = new Argument("ograss", "dist", true);
        distEuclideaArguments[1] = new Argument("quantityid", "dist", true);
        distEuclideaArguments[2] = new Argument("grassdb", global_grassdb, true);
        distEuclideaArguments[3] = new Argument("location", global_location, true);
        distEuclideaArguments[4] = new Argument("mapset", global_mapset, true);
        distEuclideaArguments[5] = new Argument("time_start_up", global_startdate, true);
        distEuclideaArguments[6] = new Argument("time_ending_up", global_enddate, true);
        distEuclideaArguments[7] = new Argument("time_delta", global_deltat, true);
        distEuclideaArguments[8] = new Argument("remotedburl", global_remotedb, true);
        distEuclideaEI.initialize(distEuclideaArguments);

        Link cp9dummyOutLink = new Link(null, "dist");
        cp9dummyOutLink.connect(distEuclidea, distEuclidea.getOutputExchangeItem(0),
                distEuclideaEI, distEuclideaEI.getInputExchangeItem(0));

        igrass_flow.prepare();

        distEuclidea.prepare();
        IValueSet distEuclideaValueSet = distEuclidea.getValues(null,cp9dummyOutLink.getID());
        GridCoverage2D distEuclideaGC = ((JGrassGridCoverageValueSet) distEuclideaValueSet)
                .getGridCoverage2D();
        GridCoverage2D view = distEuclideaGC.view(ViewType.GEOPHYSICS);
        PlanarImage discEuclideaImage = (PlanarImage) view.getRenderedImage();

        checkMatrixEqual(discEuclideaImage, GrassMapTest.distEuclideaData, 0.01);
        distEuclideaValueSet = distEuclidea.getValues(null, cp9dummyOutLink.getID());
        igrass_flow.finish();
        distEuclidea.finish();

        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }

}
