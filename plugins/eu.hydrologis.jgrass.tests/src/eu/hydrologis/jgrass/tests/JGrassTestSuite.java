package eu.hydrologis.jgrass.tests;

import eu.hydrologis.jgrass.tests.libs.TestJGrassCoverageReader;
import eu.hydrologis.jgrass.tests.libs.TestJiffle;
import eu.hydrologis.jgrass.tests.libs.TestOldJGrassRasterReader;
import eu.hydrologis.jgrass.tests.libs.TestPredefinedColorRules;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JGrassTestSuite extends TestCase {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestPredefinedColorRules.class);
        suite.addTestSuite(TestJiffle.class);
        suite.addTestSuite(TestOldJGrassRasterReader.class);
        suite.addTestSuite(TestJGrassCoverageReader.class);
        suite.addTestSuite(TestJiffle.class);
        return suite;
    }
}
