package eu.hydrologis.jgrass.operations.jai.gc;

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
public class HMGcRIF implements RenderedImageFactory {

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.image.renderable.RenderedImageFactory#create(java.awt.image.
     * renderable.ParameterBlock, java.awt.RenderingHints)
     */

    public RenderedImage create(ParameterBlock paramBlock, RenderingHints hints) {
        //the slope map
        RenderedImage slope = paramBlock.getRenderedSource(0);
        //the map with the net
        RenderedImage net = paramBlock.getRenderedSource(1);
        //the map with the h.tc classification
        RenderedImage cp9 = paramBlock.getRenderedSource(2);

        ImageLayout layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
        // were the last parameter is true if we would to manage only a single
        // tile of the input file, if we put false we can holding every pixel in
        // the input PlanarImage

        PrintStream out = (PrintStream) paramBlock.getObjectParameter(0);
        PrintStream err = (PrintStream) paramBlock.getObjectParameter(1);
        double thGrad = paramBlock.getDoubleParameter(2);
        boolean doTile = (Boolean) paramBlock.getObjectParameter(3);

        BorderExtender borderExtender = null;
        // ImageLayout layout = null;
        return new HMGcOpImage(slope,net,cp9,thGrad, borderExtender, hints, layout, 
                doTile, err, out);
    }

}
