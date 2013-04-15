package eu.hydrologis.jgrass.operations.jai.curvatures;

import java.awt.RenderingHints;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.PrintStream;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RIFRegistry;

import eu.hydrologis.jgrass.operations.jai.UtilityJAI;
import eu.hydrologis.jgrass.operations.jai.aspect.HMAspectRIF;

/**
 * This class describe the mainly input file and parameter which are used in the operation
 * <b>Aspect</b>.
 * <p>
 * </p>
 * 
 * @see OperationDescriptorImpl
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha
 *@30 Jan 2009
 *@FlowDescriptor
 */

public class HMCurvaturesDescriptor extends OperationDescriptorImpl {
    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;
    private static final String OPERATION_NAME = "Curvatures";
    // there is 2 input Raster: the dem (obtained by h.pitfiller) and the image containing the slope
    // directions. There aren't other parameters.
    private static final int numSources = 1;

    private static final String[][] resources = {{"GlobalName", OPERATION_NAME},
            {"LocalName", OPERATION_NAME}, {"Vendor", "com.andreis.daniele"},
            {"Description", "Calculate the slope in the drainage direction"},
            {"DocURL", "http://jgrass.wiki.software.bz.it/jgrass/Horton_Machine"},
            {"Version", "alpha"},};

    private static final String[] paramNames = {"dx", "dy", "outputStream", "errorStream", "doTile"};
    private static final Class[] paramClasses = {java.lang.Double.class, java.lang.Double.class,
            java.io.PrintStream.class, java.io.PrintStream.class, Boolean.class};
    // set the default value of the 2 parameters
    private static final Object[] paramDefaults = {null, null, System.out, System.out, true};
    private static final Object[] validParamValues = {

            new javax.media.jai.util.Range(Double.class, new Double(0.0), new Double(
                    Double.MAX_VALUE)),
            new javax.media.jai.util.Range(Double.class, new Double(0.0), new Double(
                    Double.MAX_VALUE)), null, null, null};

    private static final String[] supportedModes = {"rendered"};

    /**
     * Call the super constructor.
     * 
     * @see OperationDescriptorImpl
     */
    public HMCurvaturesDescriptor() {
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
    public static RenderedOp create( RenderedImage elevation, double dx, double dy,
            PrintStream err, PrintStream out, boolean doTile ) {
        if (!isRegistered) {
            register(OPERATION_NAME, "example");
            isRegistered = true;
        }
        int hTiling = elevation.getTileHeight();
        int wTiling = elevation.getTileWidth();
        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(elevation);
        parameterBlock.add(dx);
        parameterBlock.add(dy);
        parameterBlock.add(err);
        parameterBlock.add(out);
        parameterBlock.add(doTile);
        
        int cols = elevation.getWidth();
        int rows = elevation.getHeight();
        SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_DOUBLE, cols, rows, 1, cols, new int[]{0, rows * cols, 2 * rows * cols});
        RenderingHints hints = UtilityJAI.getRenderingHintsTailing(wTiling, hTiling, sm);

        // RenderingHints hints=null;
        return JAI.create(OPERATION_NAME, parameterBlock, hints);
    }
    /**
     * Register the Operation if it isn't.
     * 
     * @param OperationName the name of the operation
     * @param description
     */

    public static void register( String OperationName, String description ) {
        // register the operation "Slope"
        OperationRegistry or;
        or = JAI.getDefaultInstance().getOperationRegistry();
        or.registerDescriptor(new HMCurvaturesDescriptor());
        RenderedImageFactory rif = new HMCurvaturesRIF();
        RIFRegistry.register(or, OperationName, description, rif);
    }
}
