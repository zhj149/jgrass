package eu.hydrologis.jgrass.tests.models;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.gradient.h_gradient;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassRasterMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
/**
 * It test the h_gradient classes.
 * <p>
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @since 1.2.0
 */
public class TestGradient extends JGrassTestCase {

    public void testGradient() throws IOException {
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
        h_gradient gradient = new h_gradient(out, err);
        Argument[] gradientARgs = new Argument[7];
        gradientARgs[0] = new Argument("grassdb", global_grassdb, true);
        gradientARgs[1] = new Argument("location", global_location, true);
        gradientARgs[2] = new Argument("mapset", global_mapset, true);
        gradientARgs[3] = new Argument("time_start_up", global_startdate, true);
        gradientARgs[4] = new Argument("time_ending_up", global_enddate, true);
        gradientARgs[5] = new Argument("time_delta", global_deltat, true);
        gradientARgs[6] = new Argument("remotedburl", global_remotedb, true);
        gradient.initialize(gradientARgs);

        DummyInputGrassCoverageMap igrasscoverage_pit = new DummyInputGrassCoverageMap(out, err,
                GrassMapTest.pitData);
        Argument[] pitArguments = new Argument[9];
        pitArguments[0] = new Argument("igrass", "pit", true);
        pitArguments[1] = new Argument("quantityid", "pit", true);
        pitArguments[2] = new Argument("grassdb", global_grassdb, true);
        pitArguments[3] = new Argument("location", global_location, true);
        pitArguments[4] = new Argument("mapset", global_mapset, true);
        pitArguments[5] = new Argument("time_start_up", global_startdate, true);
        pitArguments[6] = new Argument("time_ending_up", global_enddate, true);
        pitArguments[7] = new Argument("time_delta", global_deltat, true);
        pitArguments[8] = new Argument("remotedburl", global_remotedb, true);
        igrasscoverage_pit.initialize(pitArguments);

        Link igrass_link_pit = new Link(null, "pit");
        igrass_link_pit.connect(igrasscoverage_pit, igrasscoverage_pit.getOutputExchangeItem(0), gradient, gradient
                .getInputExchangeItem(0));

        OutputGrassRasterMap ograss_gradient = new OutputGrassRasterMap(out, err);
        Argument[] gradientArguments = new Argument[9];
        gradientArguments[0] = new Argument("ograss", "gradient", true);
        gradientArguments[1] = new Argument("quantityid", "gradient", true);
        gradientArguments[2] = new Argument("grassdb", global_grassdb, true);
        gradientArguments[3] = new Argument("location", global_location, true);
        gradientArguments[4] = new Argument("mapset", global_mapset, true);
        gradientArguments[5] = new Argument("time_start_up", global_startdate, true);
        gradientArguments[6] = new Argument("time_ending_up", global_enddate, true);
        gradientArguments[7] = new Argument("time_delta", global_deltat, true);
        gradientArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_gradient.initialize(gradientArguments);

        Link ograss_link_gradient = new Link(null, "gradient");
        ograss_link_gradient.connect(gradient, gradient.getOutputExchangeItem(0), ograss_gradient,
                ograss_gradient.getInputExchangeItem(0));

        igrasscoverage_pit.prepare();
        gradient.prepare();

        String linkMessage = "there is a prblem in a link";

        assertTrue(linkMessage, igrass_link_pit.isConnected());

        assertTrue(linkMessage, ograss_link_gradient.isConnected());

        IValueSet gradientValueSet = gradient.getValues(null, ograss_link_gradient.getID());

        GridCoverage2D gradientCoverage = ((JGrassGridCoverageValueSet) gradientValueSet).getGridCoverage2D();
        RenderedImage renderedImage = gradientCoverage.getRenderedImage();
        checkMatrixEqual(renderedImage, GrassMapTest.gradientData, 0.01);

        igrasscoverage_pit.finish();
        gradient.finish();
        // set active region to the needed
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }

}
