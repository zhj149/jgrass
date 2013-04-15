package eu.hydrologis.jgrass.operations.jai.slope;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.PrintStream;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RIFRegistry;

import eu.hydrologis.jgrass.operations.jai.UtilityJAI;

/**
 * This class describe the mainly input file and parameter which are used in the operation
 * <b>Slope</b>.
 * <p>
 * This operation calculate the slope along the drain direction.
 * </p>
 * 
 * @see OperationDescriptorImpl
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha
 *@30 Jan 2009
 *@FlowDescriptor
 */

public class HMSlopeDescriptor extends OperationDescriptorImpl {
    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;

    // there is 2 input Raster: the dem (obtained by h.pitfiller) and the image containing the slope
    // directions. There aren't other parameters.
    private static final int numSources = 2;

    private static final String[][] resources = {{"GlobalName", "SlopeDir"},
            {"LocalName", "Slope"}, {"Vendor", "com.andreis.daniele"},
            {"Description", "Calculate the slope in the drainage direction"},
            {"DocURL", "http://jgrass.wiki.software.bz.it/jgrass/Horton_Machine"},
            {"Version", "alpha"},};

    private static final String[] paramNames = {"outputStream", "errorStream", "doTile", "dx", "dy"};
    private static final Class[] paramClasses = {java.io.PrintStream.class,
            java.io.PrintStream.class, Boolean.class, java.lang.Double.class,
            java.lang.Double.class};
    // set the default value of the 2 parameters
    private static final Object[] paramDefaults = {System.out, System.out, null, null, null};
    private static final Object[] validParamValues = {
            null,
            null,
            null,
            new javax.media.jai.util.Range(Double.class, new Double(0.0), new Double(
                    Double.MAX_VALUE)),
            new javax.media.jai.util.Range(Double.class, new Double(0.0), new Double(
                    Double.MAX_VALUE))};

    private static final String[] supportedModes = {"rendered"};

    /**
     * Call the super constructor.
     * 
     * @see OperationDescriptorImpl
     */
    public HMSlopeDescriptor() {
        // super(resources, numSources,);
        // the last parameter refer to the descriptor of the Operation Parameter
        // so I have set it as
        // null
        super(resources, supportedModes, numSources, paramNames, paramClasses, paramDefaults,
                validParamValues);
    }

    // memorize if the operation is already registered.
    private static boolean isRegistered = false;

    /**
     * This is the <i>factory</i> of the RenderedOp.
     * <p>
     * <li>Verify if the Operation is already registered (if not then register it</li>
     * <li>Force the operation to work with a sizeTilingxsizeTiling tiles (i have do it only to
     * test).</li>
     * </p>
     * 
     * @param planarImage the source image
     * @param dir
     * @param delta
     * @return the image after the operation have worked.
     */
    public static RenderedOp create( PlanarImage elevation, PlanarImage draindir, PrintStream out,
            PrintStream err, boolean doTile, double dx, double dy ) {
        if (!isRegistered) {
            register();
            isRegistered = true;
        }
        int hTiling = elevation.getTileHeight();
        int wTiling = elevation.getTileWidth();
        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(elevation);
        parameterBlock.addSource(draindir);
        parameterBlock.add(out);
        parameterBlock.add(err);
        parameterBlock.add(doTile);

        parameterBlock.add(dx);
        parameterBlock.add(dy);
        RenderingHints hints = UtilityJAI.getRenderingHintsTailing(wTiling, hTiling);
        // RenderingHints hints=null;
        return JAI.create("SlopeDir", parameterBlock, hints);
    }

    /*
     * Register the Operation if it isn't.
     */
    private static void register() {
        // register the operation "Slope"
        OperationRegistry or;
        or = JAI.getDefaultInstance().getOperationRegistry();
        or.registerDescriptor(new HMSlopeDescriptor());
        RenderedImageFactory rif = new HMSlopeRIF();
        RIFRegistry.register(or, "SlopeDir", "example", rif);
    }
}
