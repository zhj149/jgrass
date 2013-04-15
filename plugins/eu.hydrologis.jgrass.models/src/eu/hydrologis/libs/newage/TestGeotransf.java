package eu.hydrologis.libs.newage;
//package eu.hydrologis.jgrass.models.geotransf;
//
//import javax.units.SI;
//
//import junit.framework.TestCase;
//
//import org.openmi.backbone.Argument;
//import org.openmi.backbone.Link;
//import org.openmi.standard.IArgument;
//import org.openmi.standard.ILinkableComponent;
//
//import eu.hydrologis.dummytestclasses.DataWriter;
//import eu.hydrologis.dummytestclasses.PositionAndAttributeReader;
//import eu.hydrologis.dummytestclasses.TimeSeriesInPositionReader;
//import eu.hydrologis.openmi.util.HydrologisDate;
//
//public class TestGeotransf extends TestCase {
//
//    public void testComposition() {
//
//        /*
//         * the chosen timestep
//         */
//        int timestepInMilliSeconds = 1000;
//
//        /*
//         * temperature reader
//         */
//        IArgument[] iargs = new IArgument[3];
//        iargs[0] = new Argument("datafilepath", "testdata/geotransf/temperature.txt", true);
//        iargs[1] = new Argument("quantityid", "temperature", true);
//        iargs[2] = new Argument("unitid", SI.CELSIUS.toString(), true);
//
//        ILinkableComponent temperatureReader = new TimeSeriesInPositionReader();
//        temperatureReader.initialize(iargs);
//
//        /*
//         * temperature interpolator
//         */
//        iargs = new IArgument[4];
//        iargs[0] = new Argument("quantityid", "temperature", true);
//        iargs[1] = new Argument("unitid", SI.CELSIUS.toString(), true);
//        iargs[2] = new Argument("gradient", "1", true);
//        iargs[3] = new Argument("mwt", "0", true);
//
//        ILinkableComponent temperatureInterpolator = new TemperatureInterpolator();
//        temperatureInterpolator.initialize(iargs);
//
//        /*
//         * data writer
//         */
//        iargs = new IArgument[3];
//        iargs[0] = new Argument("quantityid", "snow", true);
//        iargs[1] = new Argument("unitid", "mm/h", true);
//        iargs[2] = new Argument("datafilepath", "testdata/geotransf/basingeometries1.geo", true);
//
//        ILinkableComponent dataWriter = new DataWriter();
//        dataWriter.initialize(iargs);
//
//        /*
//         * rain reader
//         */
//        iargs = new IArgument[3];
//        iargs[0] = new Argument("quantityid", "rainfall", true);
//        iargs[1] = new Argument("unitid", "mm/h", true);
//        iargs[2] = new Argument("datafilepath", "testdata/geotransf/rain.txt", true);
//
//        ILinkableComponent rainReader = new TimeSeriesInPositionReader();
//        rainReader.initialize(iargs);
//
//        /*
//         * kriging interpolator
//         */
//        iargs = new IArgument[3];
//        iargs[0] = new Argument("quantityid", "rainfall", true);
//        iargs[1] = new Argument("unitid", "mm/h", true);
//        double timestepinseconds = (double) timestepInMilliSeconds / 1000.0;
//        iargs[2] = new Argument("dtseconds", String.valueOf(timestepinseconds), true);
//
//        ILinkableComponent krigingInterpolator = new Kriging();
//        krigingInterpolator.initialize(iargs);
//
//        /*
//         * basin geometry reader
//         */
//        iargs = new IArgument[3];
//        iargs[0] = new Argument("quantityid", "basinPosition", true);
//        iargs[1] = new Argument("unitid", "m", true);
//        iargs[2] = new Argument("datafilepath", "testdata/geotransf/basingeometries1.geo", true);
//
//        ILinkableComponent positionReader = new PositionAndAttributeReader();
//        positionReader.initialize(iargs);
//
//        /*
//         * snow model
//         */
//        iargs = new IArgument[10];
//        iargs[0] = new Argument("quantityid", "snow", true);
//        iargs[1] = new Argument("unitid", "mm/h", true);
//        iargs[2] = new Argument("tsnow", "1.0", true);
//        iargs[3] = new Argument("tmelt", "4.5", true);
//        iargs[4] = new Argument("hnrif", "0.0", true);
//        iargs[5] = new Argument("cneve", "15", true);
//        iargs[6] = new Argument("qrif", "1775", true);
//        iargs[7] = new Argument("cmelt", "0.17", true);
//        iargs[8] = new Argument("dt_prec", "3600", true);
//        iargs[9] = new Argument("volprec", "0", true);
//
//        ILinkableComponent snowModel = new HSnowCalculator();
//        snowModel.initialize(iargs);
//
//        /*
//         * link temp reader and temp interpolator
//         */
//        Link temperatureReaderInterpolatorLink = new Link(null,
//                "temperatureread->temperatureinterpol");
//        if (!temperatureReaderInterpolatorLink.connect(temperatureReader, temperatureReader
//                .getOutputExchangeItem(0), temperatureInterpolator, temperatureInterpolator
//                .getInputExchangeItem(0)))
//            System.out
//                    .println("Link " + temperatureReaderInterpolatorLink.getID() + " not created");
//        /*
//         * link rain reader and kriging interpolator
//         */
//        Link rainReaderInterpolatorLink = new Link(null, "rainread->kriging");
//        if (!rainReaderInterpolatorLink.connect(rainReader, rainReader.getOutputExchangeItem(0),
//                krigingInterpolator, krigingInterpolator.getInputExchangeItem(0)))
//            System.out
//                    .println("Link " + temperatureReaderInterpolatorLink.getID() + " not created");
//        /*
//         * link area reader and kriging
//         */
//        Link areaKrigingLink = new Link(null, "area->kriging");
//        if (!areaKrigingLink.connect(positionReader, positionReader.getOutputExchangeItem(0),
//                krigingInterpolator, krigingInterpolator.getInputExchangeItem(1)))
//            System.out.println("Link " + areaKrigingLink.getID() + " not created");
//        /*
//         * link area reader and temperature interpol
//         */
//        Link areaTemperatureInterpolLink = new Link(null, "area->temperatureinterpolator");
//        if (!areaTemperatureInterpolLink.connect(positionReader, positionReader
//                .getOutputExchangeItem(0), temperatureInterpolator, temperatureInterpolator
//                .getInputExchangeItem(1)))
//            System.out.println("Link " + areaTemperatureInterpolLink.getID() + " not created");
//        /*
//         * link area reader and snow model
//         */
//        Link areaReaderSnowmodelLink = new Link(null, "area->snow");
//        if (!areaReaderSnowmodelLink.connect(positionReader, positionReader
//                .getOutputExchangeItem(0), snowModel, snowModel.getInputExchangeItem(2)))
//            System.out.println("Link " + areaReaderSnowmodelLink.getID() + " not created");
//        /*
//         * link temperatures interpolator and snow model
//         */
//        Link temperatureSnowmodelLink = new Link(null, "temperatureinterpol->snow");
//        if (!temperatureSnowmodelLink.connect(temperatureInterpolator, temperatureInterpolator
//                .getOutputExchangeItem(0), snowModel, snowModel.getInputExchangeItem(1)))
//            System.out.println("Link " + temperatureSnowmodelLink.getID() + " not created");
//        /*
//         * link kriging interpolator and snow model
//         */
//        Link krigingSnowmodelLink = new Link(null, "kriging->snow");
//        if (!krigingSnowmodelLink.connect(krigingInterpolator, krigingInterpolator
//                .getOutputExchangeItem(0), snowModel, snowModel.getInputExchangeItem(0)))
//            System.out.println("Link " + krigingSnowmodelLink.getID() + " not created");
//
//        /*
//         * link data writer and snow model
//         */
//        Link snowWriterLink = new Link(null, "snow->writer");
//        if (!snowWriterLink.connect(snowModel, snowModel.getOutputExchangeItem(0), dataWriter,
//                dataWriter.getInputExchangeItem(0)))
//            System.out.println("Link " + snowWriterLink.getID() + " not created");
//
//        /*
//         * prepare method for all the components involved
//         */
//        dataWriter.prepare();
//        snowModel.prepare();
//        positionReader.prepare();
//        krigingInterpolator.prepare();
//        rainReader.prepare();
//        temperatureInterpolator.prepare();
//        temperatureReader.prepare();
//
//        /*
//         * TRIGGER the model
//         */
//
//        HydrologisDate currentDate = new HydrologisDate();
//        currentDate.setTime(0);
//        HydrologisDate endDate = new HydrologisDate();
//        endDate.setTime(10000);
//        while( currentDate.before(endDate) ) {
//            dataWriter.getValues(currentDate, snowWriterLink.getID());
//            currentDate.setTime(currentDate.getTime() + timestepInMilliSeconds);
//        }
//
//        /*
//         * finish
//         */
//        dataWriter.finish();
//        snowModel.finish();
//        positionReader.finish();
//        krigingInterpolator.finish();
//        rainReader.finish();
//        temperatureInterpolator.finish();
//        temperatureReader.finish();
//
//    }
//
//    public static void main( String[] args ) {
//        new TestGeotransf().testComposition();
//    }
//
//}
