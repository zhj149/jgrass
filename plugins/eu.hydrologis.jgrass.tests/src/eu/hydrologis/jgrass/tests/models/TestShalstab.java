package eu.hydrologis.jgrass.tests.models;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.PrintStream;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.iodrivers.JGrassMapEnvironment;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.models.h.shalstab.h_shalstab;
import eu.hydrologis.jgrass.tests.utils.DummyInputGrassCoverageMap;
import eu.hydrologis.jgrass.tests.utils.GrassMapTest;
import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;
import eu.hydrologis.jgrass.utilitylinkables.OutputGrassCoverageWriter;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * Test Shalstab.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestShalstab extends JGrassTestCase {

    /**
     * This tests shalstab with all possible optional maps set to constants.
     * 
     * @throws IOException
     */
    @SuppressWarnings("nls")
    public void testShalstabConstants() throws IOException {
        JGrassMapEnvironment jGrassMapEnvironment = GrassMapTest.jME;

        // set active region to the needed
        JGrassRegion fileRegion = jGrassMapEnvironment.getFileRegion();
        JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(), fileRegion);

        PrintStream out = System.out;
        PrintStream err = System.err;

        String global_grassdb = jGrassMapEnvironment.getLOCATION().getParent();
        String global_location = jGrassMapEnvironment.getLOCATION().getName();
        String global_mapset = jGrassMapEnvironment.getMAPSET().getName();
        String global_startdate = "null";
        String global_enddate = "null";
        String global_deltat = "-1";
        String global_remotedb = "null";
        h_shalstab shalstab = new h_shalstab(out, err);
        Argument[] shalstabArgs = new Argument[13];
        shalstabArgs[0] = new Argument("grassdb", global_grassdb, true);
        shalstabArgs[1] = new Argument("location", global_location, true);
        shalstabArgs[2] = new Argument("mapset", global_mapset, true);
        shalstabArgs[3] = new Argument("time_start_up", global_startdate, true);
        shalstabArgs[4] = new Argument("time_ending_up", global_enddate, true);
        shalstabArgs[5] = new Argument("time_delta", global_deltat, true);
        shalstabArgs[6] = new Argument("remotedburl", global_remotedb, true);

        shalstabArgs[7] = new Argument("hsconst", "2.0", true);
        shalstabArgs[8] = new Argument("trasmissivityconst", "0.001", true);
        shalstabArgs[9] = new Argument("tgphiconst", "0.7", true);
        shalstabArgs[10] = new Argument("cohesionconst", "0.0", true);
        shalstabArgs[11] = new Argument("qconst", "0.05", true);
        shalstabArgs[12] = new Argument("rhoconst", "1.6", true);
        // shalstabArgs[13] = new Argument("slopelimit", "6.0", true);
        shalstab.initialize(shalstabArgs);

        DummyInputGrassCoverageMap igrass_slope = new DummyInputGrassCoverageMap(out, err, GrassMapTest.slopeData);
        Argument[] slopeArgs = new Argument[9];
        slopeArgs[0] = new Argument("igrass", "slopemap", true);
        slopeArgs[1] = new Argument("quantityid", "slopemap", true);
        slopeArgs[2] = new Argument("grassdb", global_grassdb, true);
        slopeArgs[3] = new Argument("location", global_location, true);
        slopeArgs[4] = new Argument("mapset", global_mapset, true);
        slopeArgs[5] = new Argument("time_start_up", global_startdate, true);
        slopeArgs[6] = new Argument("time_ending_up", global_enddate, true);
        slopeArgs[7] = new Argument("time_delta", global_deltat, true);
        slopeArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_slope.initialize(slopeArgs);

        Link igrass_link_slopemap = new Link(null, "slopemap");
        igrass_link_slopemap.connect(igrass_slope, igrass_slope.getOutputExchangeItem(0), shalstab, shalstab.getInputExchangeItem(0));

        DummyInputGrassCoverageMap igrass_ab = new DummyInputGrassCoverageMap(out, err, GrassMapTest.abData);
        Argument[] abArgs = new Argument[9];
        abArgs[0] = new Argument("igrass", "abmap", true);
        abArgs[1] = new Argument("quantityid", "abmap", true);
        abArgs[2] = new Argument("grassdb", global_grassdb, true);
        abArgs[3] = new Argument("location", global_location, true);
        abArgs[4] = new Argument("mapset", global_mapset, true);
        abArgs[5] = new Argument("time_start_up", global_startdate, true);
        abArgs[6] = new Argument("time_ending_up", global_enddate, true);
        abArgs[7] = new Argument("time_delta", global_deltat, true);
        abArgs[8] = new Argument("remotedburl", global_remotedb, true);
        igrass_ab.initialize(abArgs);

        Link igrass_link_abmap = new Link(null, "abmap");
        igrass_link_abmap.connect(igrass_ab, igrass_ab.getOutputExchangeItem(0), shalstab, shalstab.getInputExchangeItem(2));

        OutputGrassCoverageWriter dummyOut1 = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments1 = new Argument[9];
        dummyArguments1[0] = new Argument("ograss", "qcritmap", true);
        dummyArguments1[1] = new Argument("quantityid", "qcritmap", true);
        dummyArguments1[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments1[3] = new Argument("location", global_location, true);
        dummyArguments1[4] = new Argument("mapset", global_mapset, true);
        dummyArguments1[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments1[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments1[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments1[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOut1.initialize(dummyArguments1);

        Link dummyOutLink1 = new Link(null, "qcritmap");
        dummyOutLink1.connect(shalstab, shalstab.getOutputExchangeItem(0), dummyOut1, dummyOut1.getInputExchangeItem(0));

        assertTrue(dummyOutLink1.isConnected());

        OutputGrassCoverageWriter dummyOut2 = new OutputGrassCoverageWriter(out, err);
        Argument[] dummyArguments2 = new Argument[9];
        dummyArguments2[0] = new Argument("ograss", "classimap", true);
        dummyArguments2[1] = new Argument("quantityid", "classimap", true);
        dummyArguments2[2] = new Argument("grassdb", global_grassdb, true);
        dummyArguments2[3] = new Argument("location", global_location, true);
        dummyArguments2[4] = new Argument("mapset", global_mapset, true);
        dummyArguments2[5] = new Argument("time_start_up", global_startdate, true);
        dummyArguments2[6] = new Argument("time_ending_up", global_enddate, true);
        dummyArguments2[7] = new Argument("time_delta", global_deltat, true);
        dummyArguments2[8] = new Argument("remotedburl", global_remotedb, true);
        dummyOut2.initialize(dummyArguments2);

        Link dummyOutLink2 = new Link(null, "classimap");
        dummyOutLink2.connect(shalstab, shalstab.getOutputExchangeItem(1), dummyOut2, dummyOut2.getInputExchangeItem(0));

        assertTrue(dummyOutLink2.isConnected());

        igrass_ab.prepare();
        shalstab.prepare();
        IValueSet valueSet1 = shalstab.getValues(null, dummyOutLink1.getID());
        IValueSet valueSet2 = shalstab.getValues(null, dummyOutLink2.getID());
        igrass_ab.finish();
        shalstab.finish();

        GridCoverage2D qcritData = ((JGrassGridCoverageValueSet) valueSet1).getGridCoverage2D();
        
        GridCoverage2D classiData = ((JGrassGridCoverageValueSet) valueSet2).getGridCoverage2D();

        RenderedImage qcritImage = qcritData.getRenderedImage();
        RenderedImage renderedImage = classiData.getRenderedImage();
        checkMatrixEqual(qcritImage, GrassMapTest.qcritmapData, 0);

        checkMatrixEqual(renderedImage, GrassMapTest.classimapData, 0);

        // set active region to the needed
        JGrassRegion.writeWINDToMapset(jGrassMapEnvironment.getMAPSET().getAbsolutePath(), activeRegion);
    }

}
