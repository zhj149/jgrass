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
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.ab.h_ab;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
/**
 * It test the h_nabla classes which estimate the drainage area per length unit.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestAb extends JGrassTestCase {

    public void testAb() throws IOException {

        JGrassMapEnvironment jGrassMapEnvironment = GrassMapTest.jME;

        // set active region to the needed
        JGrassRegion fileRegion = jGrassMapEnvironment.getFileRegion();
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
        h_ab ab = new h_ab(out, err);
        Argument[] abARgs = new Argument[7];
        abARgs[0] = new Argument("grassdb", global_grassdb, true);
        abARgs[1] = new Argument("location", global_location, true);
        abARgs[2] = new Argument("mapset", global_mapset, true);
        abARgs[3] = new Argument("time_start_up", global_startdate, true);
        abARgs[4] = new Argument("time_ending_up", global_enddate, true);
        abARgs[5] = new Argument("time_delta", global_deltat, true);
        abARgs[6] = new Argument("remotedburl", global_remotedb, true);
        ab.initialize(abARgs);

        DummyInputGrassCoverageMap igrass_tca = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.tcaData);
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
        igrass_tca.initialize(tcaArguments);

        Link igrass_link_tca = new Link(null, "tca");
        igrass_link_tca.connect(igrass_tca, igrass_tca.getOutputExchangeItem(0), ab, ab
                .getInputExchangeItem(1));

        DummyInputGrassCoverageMap igrass_plan = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.planData);
        Argument[] planArguments = new Argument[9];
        planArguments[0] = new Argument("igrass", "plan", true);
        planArguments[1] = new Argument("quantityid", "plan", true);
        planArguments[2] = new Argument("grassdb", global_grassdb, true);
        planArguments[3] = new Argument("location", global_location, true);
        planArguments[4] = new Argument("mapset", global_mapset, true);
        planArguments[5] = new Argument("time_start_up", global_startdate, true);
        planArguments[6] = new Argument("time_ending_up", global_enddate, true);
        planArguments[7] = new Argument("time_delta", global_deltat, true);
        planArguments[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_plan.initialize(planArguments);

        Link igrass_link_plan = new Link(null, "plan");
        igrass_link_plan.connect(igrass_plan, igrass_plan.getOutputExchangeItem(0), ab, ab
                .getInputExchangeItem(0));

        OutputGrassRasterMap ograss_ab = new OutputGrassRasterMap(out, err);
        Argument[] abArguments = new Argument[9];
        abArguments[0] = new Argument("ograss", "ab", true);
        abArguments[1] = new Argument("quantityid", "ab", true);
        abArguments[2] = new Argument("grassdb", global_grassdb, true);
        abArguments[3] = new Argument("location", global_location, true);
        abArguments[4] = new Argument("mapset", global_mapset, true);
        abArguments[5] = new Argument("time_start_up", global_startdate, true);
        abArguments[6] = new Argument("time_ending_up", global_enddate, true);
        abArguments[7] = new Argument("time_delta", global_deltat, true);
        abArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_ab.initialize(abArguments);

        Link ograss_link_ab = new Link(null, "ab");
        ograss_link_ab.connect(ab, ab.getOutputExchangeItem(0), ograss_ab, ograss_ab
                .getInputExchangeItem(0));

        OutputGrassRasterMap ograss_b = new OutputGrassRasterMap(out, err);
        Argument[] bArguments = new Argument[9];
        bArguments[0] = new Argument("ograss", "b", true);
        bArguments[1] = new Argument("quantityid", "b", true);
        bArguments[2] = new Argument("grassdb", global_grassdb, true);
        bArguments[3] = new Argument("location", global_location, true);
        bArguments[4] = new Argument("mapset", global_mapset, true);
        bArguments[5] = new Argument("time_start_up", global_startdate, true);
        bArguments[6] = new Argument("time_ending_up", global_enddate, true);
        bArguments[7] = new Argument("time_delta", global_deltat, true);
        bArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_b.initialize(bArguments);

        Link ograss_link_b = new Link(null, "b");
        ograss_link_b.connect(ab, ab.getOutputExchangeItem(1), ograss_b, ograss_b
                .getInputExchangeItem(0));

        igrass_plan.prepare();
        igrass_tca.prepare();
        ab.prepare();

        String linkMessage = "there is a prblem in a link";

        assertTrue(linkMessage, igrass_link_tca.isConnected());

        assertTrue(linkMessage, ograss_link_ab.isConnected());

        IValueSet abValueSet = ab.getValues(null, ograss_link_ab.getID());
        GridCoverage2D abGC = ((JGrassGridCoverageValueSet) abValueSet).getGridCoverage2D();
        GridCoverage2D view = abGC.view(ViewType.GEOPHYSICS);
        PlanarImage abImage = (PlanarImage) view.getRenderedImage();

         checkMatrixEqual(abImage, GrassMapTest.abData, 0.01);

        IValueSet bValueSet = ab.getValues(null, ograss_link_b.getID());
        GridCoverage2D bGC = ((JGrassGridCoverageValueSet) bValueSet).getGridCoverage2D();
        view = bGC.view(ViewType.GEOPHYSICS);
        PlanarImage bImage = (PlanarImage) view.getRenderedImage();
    
        checkMatrixEqual(bImage, GrassMapTest.bData, 0.01);
        igrass_tca.finish();
        igrass_plan.finish();

        ab.finish();
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }

}
