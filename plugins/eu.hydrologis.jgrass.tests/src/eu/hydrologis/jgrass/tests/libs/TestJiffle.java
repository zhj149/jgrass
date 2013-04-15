package eu.hydrologis.jgrass.tests.libs;

import static java.lang.Double.NaN;
import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.runtime.JiffleRunner;

import java.awt.image.RenderedImage;
import java.util.Map;

import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;

import eu.hydrologis.jgrass.tests.utils.JGrassTestCase;

import junit.framework.TestCase;

/**
 * Test jiffle calculations.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class TestJiffle extends JGrassTestCase {

    public void testJiffleNaNBorders() {
        double[][] sourceData2 = new double[][]{
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1492.246826171875,1479.85302734375,1469.3839111328125,1462.6412353515625,1457.1978759765625,1447.783935546875,1441.96875,1442.300048828125,1448.004150390625,1448.287353515625,1443.7999267578125,1433.7681884765625,1416.5374755859375},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1498.082763671875,1491.2374267578125,1485.6729736328125,1479.319091796875,1469.0013427734375,1462.83203125,1465.173583984375,1471.032470703125,1466.99755859375,1458.9566650390625,1447.308349609375,1428.5506591796875},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1494.6883544921875,1487.7071533203125,1486.0543212890625,1486.989013671875,1485.5208740234375,1481.9833984375,1470.8587646484375,1457.062744140625,1436.0386962890625},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1489.60595703125,1474.2137451171875,1454.9185791015625,1433.953369140625},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1492.9229736328125,1472.89013671875,1454.9002685546875,1433.556396484375},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1491.126220703125,1472.1868896484375,1456.8177490234375,1434.237548828125},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1490.8475341796875,1475.5838623046875,1461.6810302734375,1442.6759033203125},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1491.03955078125,1477.7373046875,1465.881591796875,1450.394287109375},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1490.593505859375,1477.361328125,1463.75048828125,1444.942626953125},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1487.03955078125,1493.822265625,NaN,NaN,1498.5340576171875,1488.4581298828125,1472.7374267578125,1458.4761962890625,1435.1905517578125},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1480.4083251953125,1487.28955078125,1494.4573974609375,1496.539794921875,1493.866943359375,1486.1903076171875,1471.645751953125,1459.70947265625,1438.466796875},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1470.8729248046875,1474.9180908203125,1480.3580322265625,1485.92138671875,1489.3671875,1487.3524169921875,1482.952880859375,1474.4681396484375,1465.8465576171875,1445.33056640625},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1464.844482421875,1468.171630859375,1472.1866455078125,1475.010498046875,1476.7008056640625,1475.3671875,1474.3328857421875,1472.0654296875,1463.8251953125,1443.2310791015625},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1457.278564453125,1457.2325439453125,1458.843994140625,1461.4730224609375,1461.7655029296875,1459.7718505859375,1459.6534423828125,1463.307861328125,1463.2391357421875,1453.4974365234375,1435.7291259765625},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1450.697021484375,1450.14501953125,1448.185302734375,1448.2789306640625,1449.2679443359375,1447.9573974609375,1443.7957763671875,1442.0372314453125,1447.4853515625,1449.060791015625,1442.47265625,1429.7900390625},
                {NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,1445.5198974609375,1441.006103515625,1437.241943359375,1435.684814453125,1434.623779296875,1431.607421875,1425.200927734375,1421.7017822265625,1424.097412109375,1427.466552734375,1428.2989501953125,1420.2750244140625},
                };


        double[][] sourceData = new double[][]{
                {NaN, NaN, NaN, NaN, NaN, NaN},
                {NaN, 100, 200, 300, 400, NaN}, 
                {NaN, NaN, 500, 600, NaN, NaN},
                {NaN, 700, NaN, 800, NaN, NaN}, 
                {NaN, 100, 200, 300, 400, NaN},
                {NaN, NaN, 200, 300, 400, NaN}, 
                {NaN, NaN, NaN, NaN, NaN, NaN}
                };

        String script1 = "target = if (source > 200 , 999, source);";
        double[][] script1Result = new double[][]{
                {NaN, NaN, NaN, NaN, NaN, NaN},
                {NaN, 100, 200, 999, 999, NaN}, 
                {NaN, NaN, 999, 999, NaN, NaN},
                {NaN, 999, NaN, 999, NaN, NaN}, 
                {NaN, 100, 200, 999, 999, NaN},
                {NaN, NaN, 200, 999, 999, NaN}, 
                {NaN, NaN, NaN, NaN, NaN, NaN}
                };

        String script2 = "target = if (source > 200 , null(), source);";
        double[][] script2Result = new double[][]{
                {NaN, NaN, NaN, NaN, NaN, NaN},
                {NaN, 100, 200, NaN, NaN, NaN}, 
                {NaN, NaN, NaN, NaN, NaN, NaN},
                {NaN, NaN, NaN, NaN, NaN, NaN}, 
                {NaN, 100, 200, NaN, NaN, NaN},
                {NaN, NaN, 200, NaN, NaN, NaN}, 
                {NaN, NaN, NaN, NaN, NaN, NaN}
                };
        
        String script3 = "target = if (source > 1500 , null(), source);";
            
        TiledImage source = ImageUtils.createConstantImage(sourceData[0].length, sourceData.length,
                Double.valueOf(0));

        WritableRectIter iter = RectIterFactory.createWritable(source, null);
        int y = 0;
        do {
            int x = 0;
            do {
                iter.setSample(sourceData[y][x]);
                x++;
            } while( !iter.nextPixelDone() );
            iter.startPixels();
            y++;
        } while( !iter.nextLineDone() );

        TiledImage source2 = ImageUtils.createConstantImage(sourceData2[0].length, sourceData2.length,
                Double.valueOf(0));
        
        WritableRectIter iter2 = RectIterFactory.createWritable(source2, null);
        y = 0;
        do {
            int x = 0;
            do {
                iter2.setSample(sourceData2[y][x]);
                x++;
            } while( !iter2.nextPixelDone() );
            iter2.startPixels();
            y++;
        } while( !iter2.nextLineDone() );

        
        /*
         * test script 1
         */
        TiledImage target = ImageUtils.createConstantImage(source.getWidth(), source.getHeight(),
                Double.valueOf(0));
        Map<String, RenderedImage> params = CollectionFactory.map();
        params.put("source", source);
        params.put("target", target);

        try {
            Jiffle jiffle = new Jiffle(script1, params);
            JiffleRunner runner = new JiffleRunner(jiffle);
            runner.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        checkMatrixEqual(target, script1Result);

        /*
         * test script 2
         */
        target = ImageUtils.createConstantImage(source.getWidth(), source.getHeight(),
                Double.valueOf(0));
        params = CollectionFactory.map();
        params.put("source", source);
        params.put("target", target);
        
        try {
            Jiffle jiffle = new Jiffle(script2, params);
            JiffleRunner runner = new JiffleRunner(jiffle);
            runner.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        checkMatrixEqual(target, script2Result);

        /*
         * test script 3
         */
        target = ImageUtils.createConstantImage(source2.getWidth(), source2.getHeight(),
                Double.valueOf(0));
        params = CollectionFactory.map();
        params.put("source", source2);
        params.put("target", target);
        
        try {
            Jiffle jiffle = new Jiffle(script3, params);
            JiffleRunner runner = new JiffleRunner(jiffle);
            runner.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        checkMatrixEqual(target, sourceData2);

    }


}
