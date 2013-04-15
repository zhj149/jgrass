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
import eu.hydrologis.jgrass.models.h.aspect.h_aspect;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassRasterMap;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class TestAspect extends JGrassTestCase {
    /**
     * It test the h_aspect classes.
     * <p>
     * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
     * @since 1.2.0
     */
    public void testAspect() throws IOException {

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
        h_aspect aspect = new h_aspect(out, err);
        Argument[] aspectARgs = new Argument[7];
        aspectARgs[0] = new Argument("grassdb", global_grassdb, true);
        aspectARgs[1] = new Argument("location", global_location, true);
        aspectARgs[2] = new Argument("mapset", global_mapset, true);
        aspectARgs[3] = new Argument("time_start_up", global_startdate, true);
        aspectARgs[4] = new Argument("time_ending_up", global_enddate, true);
        aspectARgs[5] = new Argument("time_delta", global_deltat, true);
        aspectARgs[6] = new Argument("remotedburl", global_remotedb, true);
        aspect.initialize(aspectARgs);

        DummyInputGrassCoverageMap igrass_aspect = new DummyInputGrassCoverageMap(out, err,
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
        igrass_aspect.initialize(pitArguments);

        Link igrass_link_pit = new Link(null, "pit");
        igrass_link_pit.connect(igrass_aspect, igrass_aspect.getOutputExchangeItem(0), aspect,
                aspect.getInputExchangeItem(0));

        OutputGrassRasterMap ograss_aspect = new OutputGrassRasterMap(out, err);
        Argument[] aspectArguments = new Argument[9];
        aspectArguments[0] = new Argument("ograss", "aspect", true);
        aspectArguments[1] = new Argument("quantityid", "aspect", true);
        aspectArguments[2] = new Argument("grassdb", global_grassdb, true);
        aspectArguments[3] = new Argument("location", global_location, true);
        aspectArguments[4] = new Argument("mapset", global_mapset, true);
        aspectArguments[5] = new Argument("time_start_up", global_startdate, true);
        aspectArguments[6] = new Argument("time_ending_up", global_enddate, true);
        aspectArguments[7] = new Argument("time_delta", global_deltat, true);
        aspectArguments[8] = new Argument("remotedburl", global_remotedb, true);
        ograss_aspect.initialize(aspectArguments);

        Link ograss_link_aspect = new Link(null, "aspect");
        ograss_link_aspect.connect(aspect, aspect.getOutputExchangeItem(0), ograss_aspect,
                ograss_aspect.getInputExchangeItem(0));

        igrass_aspect.prepare();
        aspect.prepare();


        assertTrue(linkMessage, igrass_link_pit.isConnected());

        assertTrue(linkMessage, ograss_link_aspect.isConnected());

        IValueSet aspectValueSet = aspect.getValues(null, ograss_link_aspect.getID());
        GridCoverage2D aspectGC = ((JGrassGridCoverageValueSet) aspectValueSet).getGridCoverage2D();
        GridCoverage2D view = aspectGC.view(ViewType.GEOPHYSICS);
        PlanarImage aspectImage = (PlanarImage) view.getRenderedImage();
        checkMatrixEqual(aspectImage, GrassMapTest.aspectData, 0.01);

        igrass_aspect.finish();
        aspect.finish();

        // set active region to the needed
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(),
                activeRegion);
    }
}
