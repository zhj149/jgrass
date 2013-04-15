package eu.hydrologis.jgrass.operations.jai.curvatures;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.PrintStream;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

/**
 * @see RenderedImageFactory.
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version @30 Jan 2009
 *@FlowRIF
 */
public class HMCurvaturesRIF implements RenderedImageFactory {

    /*
     * (non-Javadoc)
     * @see java.awt.image.renderable.RenderedImageFactory#create(java.awt.image.
     * renderable.ParameterBlock, java.awt.RenderingHints)
     */

    public RenderedImage create( ParameterBlock paramBlock, RenderingHints hints ) {
        RenderedImage source1 = paramBlock.getRenderedSource(0);
        ImageLayout layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
        // were the last parameter is true if we would to manage only a single
        // tile of the input file, if we put false we can holding every pixel in
        // the input PlanarImage
        double dx = paramBlock.getDoubleParameter(0);
        double dy = paramBlock.getDoubleParameter(1);
        PrintStream err = (PrintStream) paramBlock.getObjectParameter(2);
        PrintStream out = (PrintStream) paramBlock.getObjectParameter(3);

        boolean doTile = (Boolean) paramBlock.getObjectParameter(4);
        BorderExtender borderExtender = null;
        // ImageLayout layout = null;
        return new HMCurvaturesOpImage(source1, borderExtender, hints, layout,err, out, dx, dy, doTile);
    }

}
