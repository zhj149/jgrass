package eu.hydrologis.jgrass.models.h.meandrop;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

public class h_meandrop extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String flowID = "flow";

    public final static String tcaID = "tca";

    public final static String summID = "summ";

    public final static String meandropID = "meandrop";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_meandrop.usage"); //$NON-NLS-1$

    private ILink flowLink = null;

    private ILink tcaLink = null;

    private ILink summLink = null;

    private ILink meandropLink = null;

    private IInputExchangeItem flowDataInputEI = null;

    private IInputExchangeItem tcaDataInputEI = null;

    private IInputExchangeItem summDataInputEI = null;

    private IOutputExchangeItem meandropDataOutputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    private boolean doTile = false;

    public h_meandrop() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_meandrop( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(tcaID)) {
            tcaLink = link;
        }
        if (id.equals(summID)) {
            summLink = link;
        }
        if (id.equals(meandropID)) {
            meandropLink = link;
        }
    }

    public void finish() {
    }

    /**
     * There is an IInputExchangeItem: flow, pit, net
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return flowDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return tcaDataInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return summDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 3;
    }

    /**
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: h2cd3d
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return meandropDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(meandropLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }

            GridCoverage2D flowGC = ModelsConstants.getGridCoverage2DFromLink(flowLink, time, err);
            GridCoverage2D tcaGC = ModelsConstants.getGridCoverage2DFromLink(tcaLink, time, err);
            GridCoverage2D summGC = ModelsConstants.getGridCoverage2DFromLink(summLink, time, err);

            PlanarImage flowImage = FluidUtils.setJaiNovalueBorder((PlanarImage) flowGC.getRenderedImage());
            PlanarImage tcaImage = (PlanarImage) tcaGC.getRenderedImage();
            PlanarImage summImage = (PlanarImage) summGC.getRenderedImage();

            WritableRaster meandropImage = langbein(flowImage, summImage, tcaImage);

            if (meandropImage == null) {
                err.println("Errors in execution...\n");
                return null;
            } else {
                CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
                jgrValueSet = new JGrassGridCoverageValueSet(meandropImage, activeRegion, crs);
                return jgrValueSet;
            }
        }
        return null;
    }

    /**
     * in this method map's properties are defined... location, mapset... and
     * than IInputExchangeItem and IOutputExchangeItem are reated
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
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.getBoolean(argument.getValue());
                }
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.meandrop";
        componentId = null;

        /*
         * create the exchange items
         */
        // meandrop output

        meandropDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // flow input

        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // tca input

        tcaDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // summ input

        summDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(tcaLink.getID())) {
            tcaLink = null;
        }
        if (linkID.equals(summLink.getID())) {
            summLink = null;
        }
        if (meandropID.equals(meandropLink.getID())) {
            meandropLink = null;
        }
    }

    /**
     * Calculates the langbein in every pixel of the map
     * 
     * @return
     */
    private WritableRaster langbein( PlanarImage flowImage, PlanarImage summImage, PlanarImage tcaImage ) {
        // get rows and cols from the active region
        int cols = activeRegion.getCols();
        int rows = activeRegion.getRows();
        int minX = flowImage.getMinX();
        int minY = flowImage.getMinY();

        int maxX = minX + cols;
        int maxY = minY + rows;

        RandomIter flowIterator = RandomIterFactory.create(flowImage, null);
        RandomIter summIterator = RandomIterFactory.create(summImage, null);
        WritableRaster meanDropImage = FluidUtils.sum_downstream(flowIterator, summIterator, cols, rows, out);
        WritableRandomIter meanDropRandomIter = RandomIterFactory.createWritable(meanDropImage, null);
        RandomIter flowRandomIter = RandomIterFactory.create(flowImage, null);
        RandomIter summRandomIter = RandomIterFactory.create(summImage, null);
        RandomIter tcaRandomIter = RandomIterFactory.create(tcaImage, null);

        // calls the subroutine sum_downstream in FluidUtils...
        if (meanDropImage == null)
            return null;

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.meandrop...", maxY - minY);
        for( int j = minY; j < maxY; j++ ) {
            for( int i = minX; i < maxX; i++ ) {
                if (!isNovalue(flowRandomIter.getSampleDouble(i, j, 0)) && !isNovalue(tcaRandomIter.getSampleDouble(i, j, 0))) {
                    meanDropRandomIter.setSample(i, j, 0, (meanDropRandomIter.getSample(i, j, 0)
                            / tcaRandomIter.getSampleDouble(i, j, 0) - summRandomIter.getSampleDouble(i, j, 0)));
                } else
                    meanDropRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
            }
            pm.worked(1);
        }
        pm.done();
        return meanDropImage;
    }
}
