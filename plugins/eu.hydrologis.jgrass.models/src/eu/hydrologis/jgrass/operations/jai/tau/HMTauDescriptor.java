package eu.hydrologis.jgrass.operations.jai.tau;

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
 * operation <b>tau</b>. It evalutate the tangential stress as:
 * 
 * 
 * <pre>
 * &#964;<sub>b</sub>=((g&sup2*k*&#961;&sup3)/(8*&#965;<sup>c</sup>))<sup>1/3</sup>*S<sup>2/3</sup>*(q*A/b-TS)<sup>(2+c)/3</sup>
 * </pre>
 * 
 * where: g is gravity, k and c are parameters linked with the law expressing
 * the resistance coefficient, &#961; the water density, &#957; the cinematic
 * viscosity of the water, S the local slope, q the effective rain per area
 * unit, A the contributing area, b the draining boundary (which can be less
 * than the pixel size), T the soil transmissivity.
 * <p>
 * For more details see the manual.
 * </p>
 * 
 * @see OperationDescriptorImpl, {@link HMAspectOpImage}
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha
 */

public class HMTauDescriptor extends OperationDescriptorImpl {
    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;

    private static final String OPERATION_NAME = "tau";

    // the source is an elevation map (depitted map)
    private static final int numSources = 2;

    private static final String[][] resources = {
            { "GlobalName", OPERATION_NAME },
            { "LocalName", OPERATION_NAME },
            { "Vendor", "com.andreis.daniele" },
            { "Description", "Calculate the shear stress" },
            { "DocURL",
                    "http://jgrass.wiki.software.bz.it/jgrass/Horton_Machine" },
            { "Version", "alpha" }, };

    /*
     * the parameters are the Stream where to print the messages, the physical
     * constants which are required to the model and the boolean cobbleSource
     * value.
     */

    private static final String[] paramNames = { "errStream", "outputStream",
            "rho", "g", "ni", "q", "k", "c", "T", "doTile" };

    private static final Class[] paramClasses = { java.io.PrintStream.class,
            java.io.PrintStream.class, Double.class, Double.class,
            Double.class, Double.class, Double.class, Double.class,
            Double.class, Boolean.class };

    // set the default value of the parameters, the default Stream is the
    // standard output
    // (System.out), and the
    // cobbleSource is set to true in order to calculate the value whit
    // tiling.
    private static final Object[] paramDefaults = { System.out, System.out,
            null, null, null, null, null, null, null, true };

    private static final String[] supportedModes = { "rendered" };

    /**
     * Call the super constructor.
     * 
     * @see OperationDescriptorImpl
     */
    public HMTauDescriptor() {

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
     * hints is null it may be some problems with tileChace and the tile will be
     * loaded several times, more than necessary).</li>
     * </p>
     * 
     * @param slope
     *            the source image(of the slope, obtained, for instance, with
     *            h.slope ) .
     * 
     * 
     @param tca
     *            the source image(of the total contributing area ).
     * 
     * @param rho
     *            water density
     * @param g
     *            gravity.
     * @param ni
     *            cinematic viscosity.
     * @param q
     *            effective rain per area unit.
     * @param k
     *            parameter of the resistance coefficient lay.
     * @param c
     *            parameter of the resistance coefficient lay.
     * @param T
     *            trasmissivity.
     * @param err
     *            , out.
     * @param doTile
     *            . is to set the cobbleSources parameter.
     * @return the image after the operation have worked, it contains the value
     *         of the stress in each pixel.
     */
    public static RenderedOp create(RenderedImage slope, RenderedImage ab,
            PrintStream err, PrintStream out, double rho, double g, double ni,
            double q, double k, double c, double T, boolean doTile) {
        if (!isRegistered) {
            register(OPERATION_NAME, "example"); //$NON-NLS-1$
            isRegistered = true;
        }
        int hTiling = slope.getTileHeight();
        int wTiling = slope.getTileWidth();
        // create the parameterblock
        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(slope);
        parameterBlock.addSource(ab);

        parameterBlock.add(err);
        parameterBlock.add(out);
        parameterBlock.add(rho);
        parameterBlock.add(g);
        parameterBlock.add(ni);
        parameterBlock.add(q);
        parameterBlock.add(k);
        parameterBlock.add(c);
        parameterBlock.add(T);
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
        // register the operation "tau"
        OperationRegistry or;
        or = JAI.getDefaultInstance().getOperationRegistry();
        or.registerDescriptor(new HMTauDescriptor());
        RenderedImageFactory rif = new HMTauRIF();
        RIFRegistry.register(or, OperationName, description, rif);
    }
}
