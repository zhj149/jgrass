package eu.hydrologis.jgrass.operations.jai.tau;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.PrintStream;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

/**
 * Is the factory for the gc operation.
 * 
 * @see RenderedImageFactory.
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version
 *@FlowRIF
 */
public class HMTauRIF implements RenderedImageFactory {

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.image.renderable.RenderedImageFactory#create(java.awt.image.
     * renderable.ParameterBlock, java.awt.RenderingHints)
     */

    public RenderedImage create(ParameterBlock paramBlock, RenderingHints hints) {
        // the slope map
        RenderedImage slope = paramBlock.getRenderedSource(0);
        // the map with the total contributing
        RenderedImage ab = paramBlock.getRenderedSource(1);

        ImageLayout layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
        // were the last parameter is true if we would to manage only a single
        // tile of the input file, if we put false we can holding every pixel in
        // the input PlanarImage

        PrintStream out = (PrintStream) paramBlock.getObjectParameter(0);
        PrintStream err = (PrintStream) paramBlock.getObjectParameter(1);
        double rho = paramBlock.getDoubleParameter(2);
        double g = paramBlock.getDoubleParameter(3);
        double ni = paramBlock.getDoubleParameter(4);
        double q = paramBlock.getDoubleParameter(5);
        double k = paramBlock.getDoubleParameter(6);
        double c = paramBlock.getDoubleParameter(7);
        double T = paramBlock.getDoubleParameter(8);
        boolean doTile = (Boolean) paramBlock.getObjectParameter(9);

        BorderExtender borderExtender = null;
        // ImageLayout layout = null;
        return new HMTauOpImage(slope, ab, rho, g, ni, q, k, c, T,
                borderExtender, hints, layout, doTile, err, out);
    }
}
