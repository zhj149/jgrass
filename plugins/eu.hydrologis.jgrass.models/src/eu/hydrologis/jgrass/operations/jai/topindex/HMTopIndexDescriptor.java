package eu.hydrologis.jgrass.operations.jai.topindex;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.PrintStream;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RIFRegistry;

import eu.hydrologis.jgrass.operations.jai.UtilityJAI;
import eu.hydrologis.jgrass.operations.jai.aspect.HMAspectOpImage;

/**
 * This class describe the mainly input file and parameter which are used in the
 * operation <b>topindex</b>.
 * <p>
 * It calculates the topographc index.
 * </p>
 * 
 * @see OperationDescriptorImpl, {@link HMAspectOpImage}
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha
 */

public class HMTopIndexDescriptor extends OperationDescriptorImpl {
    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;

    private static final String OPERATION_NAME = "topindex";

    // the source is an elevation map (depitted map)
    private static final int numSources = 2;

    private static final String[][] resources = {
            { "GlobalName", OPERATION_NAME },
            { "LocalName", OPERATION_NAME },
            { "Vendor", "com.andreis.daniele" },
            { "Description", "Calculate the thopographic index" },
            { "DocURL",
                    "http://jgrass.wiki.software.bz.it/jgrass/Horton_Machine" },
            { "Version", "alpha" }, };

    // the parameters are the Stream where to print the messages, the x and y
    // resolution of the map,
    // and the boolean cobbleSource value.

    private static final String[] paramNames = { "errStream", "outputStream",
            "doTile" };

    private static final Class[] paramClasses = { java.io.PrintStream.class,
            java.io.PrintStream.class, Boolean.class };

    // set the default value of the parameters, the default Stream is the
    // standard output
    // (System.out), the resolution is set to null (it's necessary to pass this
    // value), and the
    // cobbleSource is setted to true in order to calculate the value whit
    // tiling.
    private static final Object[] paramDefaults = { System.out, System.out,
            true };

    private static final String[] supportedModes = { "rendered" };

    /**
     * Call the super constructor.
     * 
     * @see OperationDescriptorImpl
     */
    public HMTopIndexDescriptor() {

        super(resources, supportedModes, numSources, paramNames, paramClasses,
                paramDefaults, null);
    }

    // memorize if the operation is already registered.
    private static boolean isRegistered = false;

    /**
     * This is the <i>factory</i> of the RenderedOp.
     * <p>
     * <li>Verify if the Operation is already registered (if not then register
     * it</li>
     * <li>Verify the parameter.</li>
     * <li>Force the operation to work with a sizeTilings tiles (I have do it
     * only to force the program to pass an explicit hints, otherwise if the
     * hints is null it may be some problems with tilechace and the tile will be
     * loaded several times, more than necessary).</li>
     * </p>
     * 
     * @param elevation
     *            the source image(depitted).
     * @param out
     * @param doTile
     *            is to set the cobbleSources parameter
     * @return the image after the operation have worked, the aspect.
     */
    public static RenderedOp create(RenderedImage slope, RenderedImage tca,
            PrintStream err, PrintStream out, double rho, double g, double ni,
            double q, double k, double c, double T, boolean doTile) {
        if (!isRegistered) {
            register(OPERATION_NAME, "example"); //$NON-NLS-1$
            isRegistered = true;
        }
        int hTiling = slope.getTileHeight();
        int wTiling = slope.getTileWidth();

        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(slope);
        parameterBlock.addSource(tca);

        parameterBlock.add(err);
        parameterBlock.add(out);
        parameterBlock.add(doTile);
        RenderingHints hints = null;
        if (doTile) {
            // tiling
            hints = UtilityJAI.getRenderingHintsTailing(wTiling, hTiling);
        } else {
            // work with whole image
            hints = UtilityJAI.getRenderingHintsTailing(slope.getWidth(), slope
                    .getHeight());

        }
        // RenderingHints hints=null;
        return JAI.create(OPERATION_NAME, parameterBlock, hints);
    }

    /**
     * Register the Operation if it isn't.
     * 
     * @param OperationName
     *            the name of the operation
     * @param description
     */

    public static void register(String OperationName, String description) {
        // register the operation "Slope"
        OperationRegistry or;
        or = JAI.getDefaultInstance().getOperationRegistry();
        or.registerDescriptor(new HMTopIndexDescriptor());
        RenderedImageFactory rif = new HMTopIndexRIF();
        RIFRegistry.register(or, OperationName, description, rif);
    }
}
