package eu.hydrologis.jgrass.tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import eu.hydrologis.jgrass.tests.models.TestAb;
import eu.hydrologis.jgrass.tests.models.TestAspect;
import eu.hydrologis.jgrass.tests.models.TestCb;
import eu.hydrologis.jgrass.tests.models.TestCurvatures;
import eu.hydrologis.jgrass.tests.models.TestD2O;
import eu.hydrologis.jgrass.tests.models.TestD2O3d;
import eu.hydrologis.jgrass.tests.models.TestDD;
import eu.hydrologis.jgrass.tests.models.TestDiameters;
import eu.hydrologis.jgrass.tests.models.TestDistEuclidea;
import eu.hydrologis.jgrass.tests.models.TestDrain;
import eu.hydrologis.jgrass.tests.models.TestExtractNetwork0;
import eu.hydrologis.jgrass.tests.models.TestExtractNetwork1;
import eu.hydrologis.jgrass.tests.models.TestFlow;
import eu.hydrologis.jgrass.tests.models.TestGc;
import eu.hydrologis.jgrass.tests.models.TestGradient;
import eu.hydrologis.jgrass.tests.models.TestH2ca;
import eu.hydrologis.jgrass.tests.models.TestH2cd3D;
import eu.hydrologis.jgrass.tests.models.TestHackLength;
import eu.hydrologis.jgrass.tests.models.TestHackLength3D;
import eu.hydrologis.jgrass.tests.models.TestHackStream;
import eu.hydrologis.jgrass.tests.models.TestMagnitudo;
import eu.hydrologis.jgrass.tests.models.TestMarkOutlets;
import eu.hydrologis.jgrass.tests.models.TestMeanDrop;
import eu.hydrologis.jgrass.tests.models.TestMultiTca;
import eu.hydrologis.jgrass.tests.models.TestNabla;
import eu.hydrologis.jgrass.tests.models.TestNabla1;
import eu.hydrologis.jgrass.tests.models.TestNetdif;
import eu.hydrologis.jgrass.tests.models.TestNetnumbering;
import eu.hydrologis.jgrass.tests.models.TestNetnumbering1;
import eu.hydrologis.jgrass.tests.models.TestPitfiller;
import eu.hydrologis.jgrass.tests.models.TestRescaledDistance;
import eu.hydrologis.jgrass.tests.models.TestRescaledDistance3d;
import eu.hydrologis.jgrass.tests.models.TestSeol;
import eu.hydrologis.jgrass.tests.models.TestShalstab;
import eu.hydrologis.jgrass.tests.models.TestSlope;
import eu.hydrologis.jgrass.tests.models.TestSplitSubBasin;
import eu.hydrologis.jgrass.tests.models.TestSrahler;
import eu.hydrologis.jgrass.tests.models.TestSumdownstream;
import eu.hydrologis.jgrass.tests.models.TestTau;
import eu.hydrologis.jgrass.tests.models.TestTc;
import eu.hydrologis.jgrass.tests.models.TestTca;
import eu.hydrologis.jgrass.tests.models.TestTca3D;
import eu.hydrologis.jgrass.tests.models.TestTopIndex;
import eu.hydrologis.jgrass.tests.models.TestTrasmissivity;
import eu.hydrologis.jgrass.tests.models.TestWateroutlet;
import eu.hydrologis.jgrass.tests.models.Testh2cD1;
import eu.hydrologis.jgrass.tests.models.Testh2cd0;

public class HortonTestSuite extends TestCase {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        suite.addTestSuite(TestAb.class);
        suite.addTestSuite(TestAspect.class);
        suite.addTestSuite(TestCurvatures.class);
        suite.addTestSuite(TestD2O.class);
        suite.addTestSuite(TestD2O3d.class);
        suite.addTestSuite(TestDD.class);
        suite.addTestSuite(TestDiameters.class);
        suite.addTestSuite(TestDistEuclidea.class);
        suite.addTestSuite(TestDrain.class);
        suite.addTestSuite(TestExtractNetwork0.class);
        suite.addTestSuite(TestExtractNetwork1.class);
        suite.addTestSuite(TestFlow.class);
        suite.addTestSuite(TestGc.class);
        suite.addTestSuite(TestGradient.class);
        suite.addTestSuite(Testh2cd0.class);
        suite.addTestSuite(Testh2cD1.class);
        suite.addTestSuite(TestH2cd3D.class);
        suite.addTestSuite(TestHackLength.class);
        suite.addTestSuite(TestHackLength3D.class);
        suite.addTestSuite(TestHackStream.class);
        suite.addTestSuite(TestMagnitudo.class);
        suite.addTestSuite(TestMarkOutlets.class);
        suite.addTestSuite(TestMeanDrop.class);
        suite.addTestSuite(TestMultiTca.class);
        suite.addTestSuite(TestNabla.class);
        suite.addTestSuite(TestNabla1.class);
        suite.addTestSuite(TestNetnumbering.class);
        suite.addTestSuite(TestNetnumbering1.class);
        suite.addTestSuite(TestPitfiller.class);
        suite.addTestSuite(TestSeol.class);
        suite.addTestSuite(TestSlope.class);
        suite.addTestSuite(TestSplitSubBasin.class);
        suite.addTestSuite(TestSrahler.class);
        suite.addTestSuite(TestSumdownstream.class);
        suite.addTestSuite(TestTau.class);
        suite.addTestSuite(TestTc.class);
        suite.addTestSuite(TestTca.class);
        suite.addTestSuite(TestTca3D.class);
        suite.addTestSuite(TestTopIndex.class);
        
        suite.addTestSuite(TestCb.class);
        suite.addTestSuite(TestRescaledDistance.class);
        suite.addTestSuite(TestRescaledDistance3d.class);
        suite.addTestSuite(TestWateroutlet.class);
        suite.addTestSuite(TestH2ca.class);
        suite.addTestSuite(TestShalstab.class);

        suite.addTestSuite(TestTrasmissivity.class);
        suite.addTestSuite(TestNetdif.class);
        

        return suite;
    }
}
