package eu.hydrologis.jgrass.tests.libs;

import java.util.HashMap;
import java.util.Set;

import eu.hydrologis.jgrass.libs.map.color.PredefinedColorRules;
import junit.framework.TestCase;

/**
 * Test on grass colorrules creation.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestPredefinedColorRules extends TestCase {

    @SuppressWarnings("nls")
    public void testLoadPredefinedColorRules() {
        HashMap<String, String[][]> colorRules = PredefinedColorRules.getColorsFolder(true);
        assertTrue(colorRules.size() > 0);

        Set<String> keySet = colorRules.keySet();
        String[][] rainbowRule = null;
        for( String ruleName : keySet ) {
            if (ruleName.toLowerCase().equals("rainbow")) {
                rainbowRule = colorRules.get(ruleName);
            }
        }
        assertNotNull(rainbowRule);
        assertEquals(3, rainbowRule[0].length);
        assertEquals("255", rainbowRule[0][0]);
        assertEquals("0", rainbowRule[rainbowRule.length - 1][2]);
    }
}
