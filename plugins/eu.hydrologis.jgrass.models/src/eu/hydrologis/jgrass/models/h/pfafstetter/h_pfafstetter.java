package eu.hydrologis.jgrass.models.h.pfafstetter;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.raster.RasterUtilities;
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidFeatureUtils;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * <p>
 * Usage: mode 1: h.pfafstetter --mode 1 --igrass-flow flow --igrass-hacks hacks --igrass-pit pit
 * --igrass-netnumber netnumber --igrass-channel channel --ishapefile-netshape filePath
 * --oshapefile-netshapeout filePath"
 * </p>
 * <p>
 * Usege mode 0: h.pfafstetter --mode 0 --igrass-flow flow --igrass-hacks hacks --igrass-pit pit
 * --igrass-netnumber netnumber --oshapefile-netshapeout filePath
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 * @author Andrea Antonello - www.hydrologis.com
 */
public class h_pfafstetter extends ModelsBackbone {

    private static final String CHANNEL_NUM = "channelNum"; //$NON-NLS-1$
    private static final String ELEVLASTPOINT = "elevlastpoint"; //$NON-NLS-1$
    private static final String ELEVFIRSTPOINT = "elevfirstpoint"; //$NON-NLS-1$
    private static final String PFAFSTETTER = "pfafstetter"; //$NON-NLS-1$
    private static final String NET_NUM = "netnum"; //$NON-NLS-1$

    private static final String MODE = "mode"; //$NON-NLS-1$
    private static final String NULL = "null"; //$NON-NLS-1$
    private static final String ID = "id"; //$NON-NLS-1$

    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow"; //$NON-NLS-1$
    public final static String hacksID = "hacks"; //$NON-NLS-1$
    public final static String pitID = "pit"; //$NON-NLS-1$
    public final static String netnumberID = "netnumber"; //$NON-NLS-1$
    public final static String channelID = "channel"; //$NON-NLS-1$
    public final static String netShapeID = "netshape"; //$NON-NLS-1$
    public final static String netShapeOutID = "netshapeout"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_pfafstetter.usage"); //$NON-NLS-1$

    private ILink flowLink = null;
    private ILink hacksLink = null;
    private ILink pitLink = null;
    private ILink netnumberLink = null;
    private ILink channelLink = null;
    private ILink netShapeLink = null;
    private ILink netShapeOutLink = null;

    private IInputExchangeItem flowDataInputEI = null;
    private IInputExchangeItem hacksDataInputEI = null;
    private IInputExchangeItem pitDataInputEI = null;
    private IInputExchangeItem netnumberDataInputEI = null;
    private IInputExchangeItem channelDataInputEI = null;
    private IInputExchangeItem netShapeDataInputEI = null;
    private IOutputExchangeItem netShapeDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private GridCoverage2D flowData = null, hacksData = null, pitData = null, netnumberData = null,
            channelData = null;
    private RandomIter flowIterator = null, hacksIterator = null, pitIterator = null,
            netnumberIterator = null, channelIterator = null;
    private WritableRaster flowWritableRaster;

    private List<ChannelInfo> channelList = null;

    private List<Geometry> geomVect = null;

    private List<HashMap<String, ? >> attributeVect = null;

    private List<MultiLineString> newRiverGeometriesList = null;

    private List<String> attributeName = null;

    private List<Class> attributeClass = null;

    private List<Object[]> attributesList = null;

    private int[] maxHackOrder = new int[1];

    private int[] numberOfStreams = new int[1];
    private int[][] dir = ModelsConstants.DIR_WITHFLOW_ENTERING;

    private int mode = 0;
    private int flowCols;
    private int flowRows;

    public h_pfafstetter() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_pfafstetter( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * in this method map's properties are defined... location, mapset... and than
     * IInputExchangeItem and IOutputExchangeItem are reated
     */
    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        String mapset = null;
        if (properties != null) {
            for( IArgument argument : properties ) {
                String key = argument.getKey();
                if (key.compareTo(ModelsConstants.GRASSDB) == 0) {
                    grassDb = argument.getValue();
                }
                if (key.compareTo(ModelsConstants.LOCATION) == 0) {
                    location = argument.getValue();
                }
                if (key.compareTo(ModelsConstants.MAPSET) == 0) {
                    mapset = argument.getValue();
                }
                if (key.compareTo(MODE) == 0) {
                    mode = Integer.parseInt(argument.getValue());
                }
            }
        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.pfafstetter"; //$NON-NLS-1$
        componentId = null;

        /*
         * create the exchange items
         */
        // net Shape output
        netShapeDataOutputEI = ModelsConstants
                .createFeatureCollectionOutputExchangeItem(this, null);
        // flow input
        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        // hacks input
        hacksDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        // pit input
        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        // netnumber input
        netnumberDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        // channel input
        channelDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        // netshape input
        netShapeDataInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this, null);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(netShapeOutLink.getID())) {
            if (netnumberData == null) {
                IValueSet flowValueSet = flowLink.getSourceComponent().getValues(time,
                        flowLink.getID());
                flowData = ((JGrassGridCoverageValueSet) flowValueSet).getGridCoverage2D();
                RenderedImage flowImage = flowData.getRenderedImage();
                flowRows = flowImage.getHeight();
                flowCols = flowImage.getWidth();
                flowWritableRaster = FluidUtils.createFromRenderedImage(flowImage);
                FluidUtils.setJAInoValueBorderIT(flowWritableRaster);
                flowIterator = RandomIterFactory.createWritable(flowWritableRaster, null);

                IValueSet hacksValueSet = hacksLink.getSourceComponent().getValues(time,
                        hacksLink.getID());
                hacksData = ((JGrassGridCoverageValueSet) hacksValueSet).getGridCoverage2D();
                RenderedImage hacksImage = hacksData.getRenderedImage();
                hacksIterator = RandomIterFactory.create(hacksImage, null);

                IValueSet pitValueSet = pitLink.getSourceComponent().getValues(time,
                        pitLink.getID());
                pitData = ((JGrassGridCoverageValueSet) pitValueSet).getGridCoverage2D();
                RenderedImage pitImage = pitData.getRenderedImage();
                pitIterator = RandomIterFactory.create(pitImage, null);

                IValueSet netnumberValueSet = netnumberLink.getSourceComponent().getValues(time,
                        netnumberLink.getID());
                netnumberData = ((JGrassGridCoverageValueSet) netnumberValueSet)
                        .getGridCoverage2D();
                RenderedImage netnumberImage = netnumberData.getRenderedImage();
                netnumberIterator = RandomIterFactory.create(netnumberImage, null);

                if (channelLink != null) {
                    IValueSet channelValueSet = channelLink.getSourceComponent().getValues(time,
                            channelLink.getID());
                    channelData = ((JGrassGridCoverageValueSet) channelValueSet)
                            .getGridCoverage2D();
                    RenderedImage channelImage = channelData.getRenderedImage();
                    channelIterator = RandomIterFactory.create(channelImage, null);

                    HashMap<String, Object> geomMap = null;
                    IValueSet netShapeValueSet = netShapeLink.getSourceComponent().getValues(time,
                            netShapeLink.getID());
                    FeatureCollection<SimpleFeatureType, SimpleFeature> fcNet = ((JGrassFeatureValueSet) netShapeValueSet)
                            .getFeatureCollection();
                    List<String> key = new ArrayList<String>();
                    // print out a feature type header and wait for user input
                    SimpleFeatureType ft = fcNet.getSchema();
                    for( int i = 0; i < ft.getAttributeCount(); i++ ) {
                        AttributeType at = ft.getType(i);
                        // FIXME check if this is ok with toString
                        key.add(at.getName().toString());
                    }
                    geomVect = new ArrayList<Geometry>();
                    attributeVect = new ArrayList<HashMap<String, ? >>();
                    FeatureIterator<SimpleFeature> fIterator = fcNet.features();
                    while( fIterator.hasNext() ) {
                        SimpleFeature feature = fIterator.next();
                        geomMap = new HashMap<String, Object>();
                        for( int i = 0; i < feature.getAttributeCount(); i++ ) {
                            Object attribute = feature.getAttribute(i);
                            if (attribute != null) {
                                feature.getAttribute(i).getClass();
                                if (!(attribute instanceof Geometry))
                                    geomMap.put(key.get(i), attribute);
                            } else {
                                geomMap.put(key.get(i), NULL);
                            }
                        }
                        geomMap.put(ID, feature.getID());
                        geomVect.add((Geometry) feature.getDefaultGeometry());
                        attributeVect.add(geomMap);
                        geomMap = null;
                    }
                    fcNet.close(fIterator);
                }

                out.println(Messages.getString("h_pfafstetter.channelinfo")); //$NON-NLS-1$
                createChannelInfo();
                out.println(Messages.getString("h_pfafstetter.calc")); //$NON-NLS-1$
                pfafstetter();
                out.println(Messages.getString("h_pfafstetter.geom")); //$NON-NLS-1$
                createsGeometry();
                out.println(Messages.getString("h_pfafstetter.att")); //$NON-NLS-1$
                if (mode == 0) {
                    createsAttributeVect();
                } else {
                    createsAttributeVectWithChannelNum();
                }

                if (newRiverGeometriesList.size() < 1 || attributesList.size() < 1) {
                    throw new ModelsIllegalargumentException(
                            "An error occurred while generating the Pfaffstetter network. The resulting geometries or attributes are empty. Please check your syntax.",
                            this);
                }

                SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
                b.setName("pfafstetternet"); //$NON-NLS-1$
                b.add("the_geom", MultiLineString.class); //$NON-NLS-1$
                for( int j = 0; j < attributeName.size(); j++ ) {
                    b.add(attributeName.get(j), attributeClass.get(j));
                }
                SimpleFeatureType type = b.buildFeatureType();
                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);

                FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = FeatureCollections
                        .newCollection();
                int recordsNum = attributesList.get(0).length;
                for( int j = 0; j < recordsNum; j++ ) {
                    List<Object> valuesList = new ArrayList<Object>();
                    valuesList.add(newRiverGeometriesList.get(j));
                    for( int i = 0; i < attributesList.size(); i++ ) {
                        valuesList.add(attributesList.get(i)[j]);
                    }
                    builder.addAll(valuesList);
                    SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + j); //$NON-NLS-1$
                    featureCollection.add(feature);
                }

                return new JGrassFeatureValueSet(featureCollection);
            }
        }
        return null;
    }
    /**
     * Creates an Object ChannelInfo for every channel (a channel is composed by stream having the
     * same hack's order)
     */
    private void createChannelInfo() {
        ChannelInfo channel = null;
        int[] flow = new int[2];
        int f, netNumValue;

        out.println(Messages.getString("working") + " " + componentDescr); //$NON-NLS-1$//$NON-NLS-2$
        out.println(Messages.getString("working12")); //$NON-NLS-1$

        // creates a vector of object ChannelInfo
        channelList = new ArrayList<ChannelInfo>();
        List<Integer> netNumList = new ArrayList<Integer>();
        List<Integer> netNumListAll = new ArrayList<Integer>();
        int num = 0;
        for( int j = 0; j < flowRows; j++ ) {
            for( int i = 0; i < flowCols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                if (!isNovalue(netnumberIterator.getSampleDouble(i, j, 0))
                        && flowIterator.getSampleDouble(i, j, 0) != 10.0) {
                    if (netNumListAll.size() == 0) {
                        netNumListAll.add((int) netnumberIterator.getSampleDouble(i, j, 0));
                    }
                    // counts the streams
                    for( int k = 0; k < netNumListAll.size(); k++ ) {
                        if (netnumberIterator.getSampleDouble(i, j, 0) == netNumListAll.get(k)) {
                            num++;
                        }
                    }
                    if (num == 0) {
                        netNumListAll.add((int) netnumberIterator.getSampleDouble(i, j, 0));
                    }
                    num = 0;
                    f = 0;
                    // looks for the surce...
                    for( int k = 1; k <= 8; k++ ) {
                        if (flowIterator.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1],
                                0) == dir[k][2]
                                && !isNovalue(netnumberIterator.getSampleDouble(
                                        flow[0] + dir[k][0], flow[1] + dir[k][1], 0))) {
                            break;
                        } else
                            f++;
                    }
                    // if the pixel is a source...set the parameters in
                    // ChannelInfo
                    if (f == 8) {
                        // creates new object ChannelInfo for this channel
                        channel = new ChannelInfo();
                        channel.setHackOrder((int) hacksIterator.getSampleDouble(i, j, 0));
                        // set the max order of hack
                        if (hacksIterator.getSampleDouble(i, j, 0) > maxHackOrder[0]) {
                            maxHackOrder[0] = (int) hacksIterator.getSampleDouble(i, j, 0);
                        }
                        netNumValue = (int) netnumberIterator.getSampleDouble(i, j, 0);
                        channel.addNetNumComp(netNumValue);
                        netNumList.add(netNumValue);
                        if (!FluidUtils.go_downstream(flow, flowIterator.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return;
                        // while the channels have the same order add properties
                        // the ChannelInfo
                        while( !isNovalue(flowIterator.getSampleDouble(flow[0], flow[1], 0))
                                && hacksIterator.getSampleDouble(flow[0], flow[1], 0) == hacksIterator
                                        .getSampleDouble(i, j, 0)
                                && flowIterator.getSampleDouble(flow[0], flow[1], 0) != 10 ) {
                            if (netnumberIterator.getSampleDouble(flow[0], flow[1], 0) != netNumValue) {
                                netNumValue = (int) netnumberIterator.getSampleDouble(flow[0],
                                        flow[1], 0);
                                channel.addNetNumComp(netNumValue);
                                netNumList.add(netNumValue);
                                channel.setIsTrim(true);
                            }
                            if (!FluidUtils.go_downstream(flow, flowIterator.getSampleDouble(
                                    flow[0], flow[1], 0)))
                                return;
                        }
                        channel.setChannelParentNum((int) netnumberIterator.getSampleDouble(
                                flow[0], flow[1], 0));
                        // adds new channel to channelVect
                        channelList.add(channel);
                        channel = null;
                    }
                }
            }
        }

        List<Integer> netNumDiff = new ArrayList<Integer>();
        num = 0;
        if (netNumList.size() != netNumListAll.size()) {
            for( Integer integer : netNumListAll ) {
                for( Integer integer2 : netNumList ) {
                    if (integer == integer2) {
                        num++;
                    }
                }
                if (num == 0) {
                    netNumDiff.add(integer);
                }
                num = 0;
            }
        }
        numberOfStreams[0] = netNumListAll.size();
        netNumList = null;
        netNumListAll = null;
        for( Integer integer : netNumDiff ) {
            for( int j = 0; j < flowRows; j++ ) {
                for( int i = 0; i < flowCols; i++ ) {
                    if (netnumberIterator.getSampleDouble(j, i, 0) == integer) {
                        flow[0] = i;
                        flow[1] = j;
                        channel = new ChannelInfo();
                        channel.setHackOrder((int) hacksIterator.getSampleDouble(i, j, 0));
                        if (hacksIterator.getSampleDouble(i, j, 0) > maxHackOrder[0]) {
                            maxHackOrder[0] = (int) hacksIterator.getSampleDouble(i, j, 0);
                        }
                        netNumValue = (int) netnumberIterator.getSampleDouble(i, j, 0);
                        channel.addNetNumComp(netNumValue);
                        if (!FluidUtils.go_downstream(flow, flowIterator.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return;
                        while( !isNovalue(flowIterator.getSampleDouble(flow[0], flow[1], 0))
                                && hacksIterator.getSampleDouble(flow[0], flow[1], 0) == hacksIterator
                                        .getSampleDouble(i, j, 0)
                                && flowIterator.getSampleDouble(flow[0], flow[1], 0) != 10 ) {
                            if (netnumberIterator.getSampleDouble(flow[0], flow[1], 0) != netNumValue) {
                                netNumValue = (int) netnumberIterator.getSampleDouble(flow[0],
                                        flow[1], 0);
                                channel.addNetNumComp(netNumValue);
                                channel.setIsTrim(true);
                            }
                            if (!FluidUtils.go_downstream(flow, flowIterator.getSampleDouble(
                                    flow[0], flow[1], 0)))
                                return;
                        }
                        if (!FluidUtils.go_downstream(flow, flowIterator.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return;
                        channel.setChannelParentNum((int) netnumberIterator.getSampleDouble(
                                flow[0], flow[1], 0));
                        channelList.add(channel);
                        channel = null;
                        integer = 0;
                    }
                }
            }
        }
    }

    /**
     * Assignes the number of pfafstetter to every streams
     */
    private boolean pfafstetter() {
        out.println(Messages.getString("working22")); //$NON-NLS-1$

        int pfafNum = 1;
        int index = 0;
        String pfaf = ""; //$NON-NLS-1$
        for( int i = 1; i <= maxHackOrder[0]; i++ ) {
            out.println("Channel order: " + i); //$NON-NLS-1$
            for( ChannelInfo chanTemp : channelList ) {
                if (chanTemp.getHackOrder() == i) {
                    // first step assigns the number to the main channel
                    if (i == 1) {
                        // if the main channel has tributary channels (isTrim =
                        // true)...
                        if (chanTemp.getIsTrim() == true) {
                            // assigns uneven number to the streams in the main
                            // net
                            for( int j = 0; j < chanTemp.getNetNumComp().size(); j++ ) {
                                chanTemp.addPfafValue(String.valueOf(pfafNum));
                                pfafNum = pfafNum + 2;
                            }
                            // for every channel in channelVect assigns the
                            // pfafstetter parent to the corresponding channel
                            // (it is necessary to numbering the other channels)
                            for( int k = 0; k < channelList.size(); k++ ) {
                                if (k != index) {
                                    for( int j = 0; j < chanTemp.getNetNumComp().size(); j++ ) {
                                        if (channelList.get(k).getChannelParentNum() == chanTemp
                                                .getNetNumComp().get(j)) {
                                            channelList.get(k).setPfafParent(
                                                    chanTemp.getPfafValue().get(j));
                                        }
                                    }
                                }
                            }
                        } else {
                            // if the basin has only a channel "main stream" its
                            // number of pfafstetter is equal to 0
                            chanTemp.addPfafValue("0"); //$NON-NLS-1$
                        }
                    } else {
                        // assigns a number to the other channels
                        pfafNum = 1;
                        // if the main channel has tributary channels (isTrim =
                        // true)...
                        if (chanTemp.getIsTrim() == true) {
                            for( int j = 0; j < chanTemp.getNetNumComp().size(); j++ ) {
                                String pfafParent = chanTemp.getPfafParent();
                                StringTokenizer st = new StringTokenizer(pfafParent, "."); //$NON-NLS-1$
                                int order = st.countTokens();
                                // if the pfafstetter of the parent net is
                                // composed from a single number ex. "10", adds
                                // ex. ".1", ".3"... the number of this channel
                                // is ex. "10.1", "10.3"...
                                if (order == 1) {
                                    pfaf = String
                                            .valueOf(Integer.valueOf(chanTemp.getPfafParent()) + 1)
                                            + "." + String.valueOf(pfafNum); //$NON-NLS-1$
                                    chanTemp.addPfafValue(pfaf);
                                    pfafNum = pfafNum + 2;
                                } else {
                                    // if the pfafstetter parent has number like
                                    // "10.1" its pafstetter number is "10.1.1"
                                    int token = 1;
                                    String pfafToken = ""; //$NON-NLS-1$
                                    while( st.hasMoreTokens() ) {
                                        if (token < order) {
                                            pfafToken += st.nextToken() + "."; //$NON-NLS-1$
                                        } else {
                                            pfafToken += String.valueOf(Integer.valueOf(st
                                                    .nextToken()) + 1)
                                                    + "." + String.valueOf(pfafNum); //$NON-NLS-1$
                                            pfafNum = pfafNum + 2;
                                        }
                                        token++;
                                    }
                                    chanTemp.addPfafValue(pfafToken);
                                }
                            }
                        } else {
                            // if the channel has isTrim = false...
                            String pfafParent = chanTemp.getPfafParent();
                            StringTokenizer st = new StringTokenizer(pfafParent, "."); //$NON-NLS-1$
                            int order = st.countTokens();
                            if (order == 1 && Integer.valueOf(chanTemp.getPfafParent()) % 2 != 0) {
                                pfaf = String
                                        .valueOf(Integer.valueOf(chanTemp.getPfafParent()) + 1);
                                chanTemp.addPfafValue(pfaf);
                                pfafNum = pfafNum + 2;
                            } else if (order == 1
                                    && Integer.valueOf(chanTemp.getPfafParent()) % 2 == 0) {
                                pfaf = chanTemp.getPfafParent() + ".1"; //$NON-NLS-1$
                                chanTemp.addPfafValue(pfaf);
                            } else {
                                int token = 1;
                                String pfafToken = ""; //$NON-NLS-1$
                                while( st.hasMoreTokens() ) {
                                    if (token < order) {
                                        pfafToken += st.nextToken() + "."; //$NON-NLS-1$
                                    } else {
                                        pfafToken += String
                                                .valueOf(Integer.valueOf(st.nextToken()) + 1);
                                    }
                                    token++;
                                }
                                chanTemp.addPfafValue(pfafToken);
                            }
                        }
                        // for every channel in channelVect assigs the
                        // pfafstetter parent to the corresponding channel
                        // (it is necessary to numbering the other channels)
                        for( int k = 0; k < channelList.size(); k++ ) {
                            if (k != index) {
                                for( int j = 0; j < chanTemp.getNetNumComp().size(); j++ ) {
                                    if (channelList.get(k).getChannelParentNum() == chanTemp
                                            .getNetNumComp().get(j)) {
                                        channelList.get(k).setPfafParent(
                                                chanTemp.getPfafValue().get(j));
                                    }
                                }
                            }
                        }
                    }
                }
                index++;
                pfafNum = 1;
            }
        }
        return true;
    }

    /**
     * Creates geometries for every channel in the network
     */
    private void createsGeometry() {

        try {
            newRiverGeometriesList = new FluidFeatureUtils().net2ShapeGeometries(flowIterator,
                    netnumberIterator, numberOfStreams, activeRegion, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates attribuetes for every geometries (at the moment netNum of the channel and
     * pfafstetter) h.pfafstetter --igrass-flow dirnetm --igrass-hacks hacksm --igrass-pit pit
     * --igrass-netnumber netnumberm --oshapefile-netshapeout "/home/davide/s"
     */
    private void createsAttributeVect() {

        // extracts netNumber and pfafstetter for every streams
        int index = 0;
        HashMap<Double, String> numAndPfafHash = new HashMap<Double, String>();
        String pfafValue = ""; //$NON-NLS-1$
        for( ChannelInfo channelTemp : channelList ) {
            for( double numTemp : channelTemp.getNetNumComp() ) {
                pfafValue = channelTemp.getPfafValue().get(index);
                numAndPfafHash.put(numTemp, pfafValue);
                index++;
            }
            index = 0;
        }
        List<Double> first = new ArrayList<Double>();
        List<Double> last = new ArrayList<Double>();

        out.println("Extracting elevation of start and end points...");
        for( int i = 0; i < newRiverGeometriesList.size(); i++ ) {
            out.println("Processing link number " + i);
            Geometry geom = newRiverGeometriesList.get(i);
            Coordinate[] coordinates = null;
            try {
                coordinates = geom.getCoordinates();
            } catch (Exception e) {
                coordinates = new Coordinate[0];
            }
            if (coordinates.length < 2) {
                err.println("***************************************");
                err.println("Found an empty geometry at " + (i + 1));
                err.println("***************************************");
                first.add(JGrassConstants.doubleNovalue);
                last.add(JGrassConstants.doubleNovalue);
            } else {
                int[] rowColFirst = RasterUtilities.putClickToCenterOfCell(activeRegion,
                        new Point2D.Double(coordinates[0].x, coordinates[0].y));
                int[] rowColLast = RasterUtilities.putClickToCenterOfCell(activeRegion,
                        new Point2D.Double(coordinates[coordinates.length - 1].x,
                                coordinates[coordinates.length - 1].y));
                first.add(pitIterator.getSampleDouble(rowColFirst[1], rowColFirst[0], 0));
                last.add(pitIterator.getSampleDouble(rowColLast[1], rowColLast[0], 0));
            }
        }

        // ATTRIBUTES
        // create a vector of strings (it contains the name of the attributes)
        attributeName = new ArrayList<String>();
        // creates a vector of class
        attributeClass = new ArrayList<Class>();
        // creates a vector of object
        attributesList = new ArrayList<Object[]>();

        attributeName.add(NET_NUM);
        attributeName.add(PFAFSTETTER);
        attributeName.add(ELEVFIRSTPOINT);
        attributeName.add(ELEVLASTPOINT);

        // adds netNum attribute...
        Object[] netnumAttribute = new Object[numAndPfafHash.size()];
        for( int j = 0; j < numAndPfafHash.size(); j++ ) {
            netnumAttribute[j] = j + 1;
        }
        attributesList.add(netnumAttribute);
        attributeClass.add(netnumAttribute[0].getClass());

        // adds pfafstetter attribute...
        double indexDouble = 0;
        Object[] pfaffstetterAttribute = new Object[numAndPfafHash.size()];
        for( int j = 0; j < numAndPfafHash.size(); j++ ) {
            pfaffstetterAttribute[j] = numAndPfafHash.get(indexDouble + 1);
            indexDouble++;
        }
        attributesList.add(pfaffstetterAttribute);
        attributeClass.add(pfaffstetterAttribute[0].getClass());

        // adds first point attribute...
        Object[] firstpointAttribute = new Object[numAndPfafHash.size()];
        for( int j = 0; j < numAndPfafHash.size(); j++ ) {
            firstpointAttribute[j] = first.get(j);
        }
        attributesList.add(firstpointAttribute);
        attributeClass.add(firstpointAttribute[0].getClass());

        // adds last point attribute...
        Object[] lastpointAttribute = new Object[numAndPfafHash.size()];
        for( int j = 0; j < numAndPfafHash.size(); j++ ) {
            lastpointAttribute[j] = last.get(j);
        }
        attributesList.add(lastpointAttribute);
        attributeClass.add(lastpointAttribute[0].getClass());

    }
    /**
     * Creates attribuetes for every geometries (at the moment netNum of the channel, channelNumber
     * and pfafstetter)
     */
    private void createsAttributeVectWithChannelNum() {

        int[] flow = new int[2];
        int f = 0;
        double numValue = 0;
        double channelValue = 0;
        out.println("Extracting netNumber and channel number for every stream...");
        // extracts netNumber and channel number for every streams
        HashMap<Double, Double> netNumAndChannelHash = new HashMap<Double, Double>();
        for( int j = 0; j < flowRows; j++ ) {
            for( int i = 0; i < flowCols; i++ ) {
                flow[0] = i;
                flow[1] = j;
                if (!isNovalue(netnumberIterator.getSampleDouble(j, i, 0))
                        && flowIterator.getSampleDouble(j, i, 0) != 10.0) {
                    f = 0;
                    // looks for the source...
                    for( int k = 1; k <= 8; k++ ) {
                        if (flowIterator.getSampleDouble(flow[0] + dir[k][0], flow[1] + dir[k][1],
                                0) == dir[k][2]
                                && !isNovalue(netnumberIterator.getSampleDouble(
                                        flow[0] + dir[k][0], flow[1] + dir[k][1], 0))) {
                            break;
                        } else
                            f++;
                    }
                    // if the pixel is a source...
                    if (f == 8) {
                        numValue = netnumberIterator.getSampleDouble(flow[0], flow[1], 0);
                        channelValue = channelIterator.getSampleDouble(flow[0], flow[1], 0);
                        if (netNumAndChannelHash.get(netnumberIterator.getSampleDouble(flow[0],
                                flow[1], 0)) == null) {
                            netNumAndChannelHash.put(netnumberIterator.getSampleDouble(flow[0],
                                    flow[1], 0), channelIterator.getSampleDouble(flow[0], flow[1],
                                    0));
                        }
                        // insert netNum and channelNum in the HashMap
                        if (!FluidUtils.go_downstream(flow, flowIterator.getSampleDouble(flow[0],
                                flow[1], 0)))
                            return;
                        while( !isNovalue(flowIterator.getSampleDouble(flow[0], flow[1], 0))
                                && flowIterator.getSampleDouble(flow[0], flow[1], 0) != 10 ) {
                            if (netNumAndChannelHash.get(netnumberIterator.getSampleDouble(flow[0],
                                    flow[1], 0)) == null) {
                                netNumAndChannelHash.put(netnumberIterator.getSampleDouble(flow[0],
                                        flow[1], 0), channelIterator.getSampleDouble(flow[0],
                                        flow[1], 0));
                            }
                            if (channelValue != channelIterator
                                    .getSampleDouble(flow[0], flow[1], 0)
                                    && numValue == netnumberIterator.getSampleDouble(flow[0],
                                            flow[1], 0)) {
                                netNumAndChannelHash.remove(netnumberIterator.getSampleDouble(
                                        flow[0], flow[1], 0));
                                netNumAndChannelHash.put(netnumberIterator.getSampleDouble(flow[0],
                                        flow[1], 0), channelIterator.getSampleDouble(flow[0],
                                        flow[1], 0));
                            }
                            if (!FluidUtils.go_downstream(flow, flowIterator.getSampleDouble(
                                    flow[0], flow[1], 0)))
                                return;
                            if (!isNovalue(netnumberIterator.getSampleDouble(flow[0], flow[1], 0))
                                    && !isNovalue(channelIterator.getSampleDouble(flow[0], flow[1],
                                            0))) {
                                numValue = netnumberIterator.getSampleDouble(flow[0], flow[1], 0);
                                channelValue = channelIterator.getSampleDouble(flow[0], flow[1], 0);
                            }
                        }
                    }
                }
            }
        }

        // extracts netNumber and pfafstetter for every streams
        int index = 0;
        HashMap<Double, String> numAndPfafHash = new HashMap<Double, String>();
        String pfafValue = ""; //$NON-NLS-1$
        out.println("Extracting netNumber and pfafstetter for every stream...");
        for( ChannelInfo channelTemp : channelList ) {
            for( double numTemp : channelTemp.getNetNumComp() ) {
                pfafValue = channelTemp.getPfafValue().get(index);
                numAndPfafHash.put(numTemp, pfafValue);
                index++;
            }
            if (channelTemp.getOrigNetNumValue() != 0) {
                netNumAndChannelHash.put(channelTemp.getNetNumComp().get(0), (double) channelTemp
                        .getOrigNetNumValue());
            }
            index = 0;
        }
        List<Double> first = new ArrayList<Double>();
        List<Double> last = new ArrayList<Double>();
        List<Geometry> geometryVectorLine = new ArrayList<Geometry>();
        for( int i = 0; i < newRiverGeometriesList.size(); i++ ) {
            geometryVectorLine.add((Geometry) newRiverGeometriesList.toArray()[i]);
        }
        // for( Geometry geom : geometryVectorLine ) {
        out.println("Extracting elevation of start and end points...");
        for( int i = 0; i < newRiverGeometriesList.size(); i++ ) {
            out.println("Processing link number " + i);
            Geometry geom = newRiverGeometriesList.get(i);
            Coordinate[] coordinates = null;
            try {
                coordinates = geom.getCoordinates();
            } catch (Exception e) {
                coordinates = new Coordinate[0];
            }
            if (coordinates.length < 2) {
                err.println("***************************************");
                err.println("Found an empty geometry at " + (i + 1));
                err.println("***************************************");
                first.add(JGrassConstants.doubleNovalue);
                last.add(JGrassConstants.doubleNovalue);
            } else {
                int[] rowColFirst = RasterUtilities.putClickToCenterOfCell(activeRegion,
                        new Point2D.Double(coordinates[0].x, coordinates[0].y));
                int[] rowColLast = RasterUtilities.putClickToCenterOfCell(activeRegion,
                        new Point2D.Double(coordinates[coordinates.length - 1].x,
                                coordinates[coordinates.length - 1].y));
                first.add(pitIterator.getSampleDouble(rowColFirst[1], rowColFirst[0], 0));
                last.add(pitIterator.getSampleDouble(rowColLast[1], rowColLast[0], 0));
            }
        }

        // ATTRIBUTES
        // create a vector of strings (it contains the name of the attributes)
        attributeName = new ArrayList<String>();
        // creates a vector of class
        attributeClass = new ArrayList<Class>();
        // creates a vector of object
        attributesList = new ArrayList<Object[]>();

        attributeName.add(NET_NUM);
        attributeName.add(CHANNEL_NUM);
        attributeName.add(PFAFSTETTER);
        attributeName.add(ELEVFIRSTPOINT);
        attributeName.add(ELEVLASTPOINT);

        // adds netNum attribute...
        Object[] netnumAttribute = new Object[numAndPfafHash.size()];
        for( int j = 0; j < numAndPfafHash.size(); j++ ) {
            netnumAttribute[j] = j + 1;
        }
        attributesList.add(netnumAttribute);
        attributeClass.add(netnumAttribute[0].getClass());

        // adds channelNumber attribute...
        Object[] channelnumAttribute = new Object[numAndPfafHash.size()];
        double indexDouble = 0;
        for( int j = 0; j < netNumAndChannelHash.size(); j++ ) {
            channelnumAttribute[j] = netNumAndChannelHash.get(indexDouble + 1);
            indexDouble++;
        }
        attributesList.add(channelnumAttribute);
        attributeClass.add(channelnumAttribute[0].getClass());

        // adds pfafstetter attribute...
        Object[] pfafstetterAttribute = new Object[numAndPfafHash.size()];
        indexDouble = 0;
        for( int j = 0; j < numAndPfafHash.size(); j++ ) {
            pfafstetterAttribute[j] = numAndPfafHash.get(indexDouble + 1);
            indexDouble++;
        }
        attributesList.add(pfafstetterAttribute);
        attributeClass.add(pfafstetterAttribute[0].getClass());

        // adds first point attribute...
        Object[] firstpointAttribute = new Object[numAndPfafHash.size()];
        for( int j = 0; j < numAndPfafHash.size(); j++ ) {
            firstpointAttribute[j] = first.get(j);
        }
        attributesList.add(firstpointAttribute);
        attributeClass.add(firstpointAttribute[0].getClass());

        // adds last point attribute...
        Object[] lastpointAttribute = new Object[numAndPfafHash.size()];
        for( int j = 0; j < numAndPfafHash.size(); j++ ) {
            lastpointAttribute[j] = last.get(j);
        }
        attributesList.add(lastpointAttribute);
        attributeClass.add(lastpointAttribute[0].getClass());

    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(hacksID)) {
            hacksLink = link;
        }
        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(netnumberID)) {
            netnumberLink = link;
        }
        if (id.equals(channelID)) {
            channelLink = link;
        }
        if (id.equals(netShapeID)) {
            netShapeLink = link;
        }
        if (id.equals(netShapeOutID)) {
            netShapeOutLink = link;
        }
    }

    public void finish() {
        flowData = null;
        hacksData = null;
        pitData = null;
        channelData = null;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return hacksDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return pitDataInputEI;
        }
        if (inputExchangeItemIndex == 3) {
            return netnumberDataInputEI;
        }
        if (inputExchangeItemIndex == 4) {
            return channelDataInputEI;
        }
        if (inputExchangeItemIndex == 5) {
            return netShapeDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 6;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return netShapeDataOutputEI;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(hacksLink.getID())) {
            hacksLink = null;
        }
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (netnumberID.equals(netnumberLink.getID())) {
            netnumberLink = null;
        }
        if (channelID.equals(channelLink.getID())) {
            channelLink = null;
        }
    }

}
