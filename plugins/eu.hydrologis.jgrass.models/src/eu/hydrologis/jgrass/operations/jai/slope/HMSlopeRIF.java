package eu.hydrologis.jgrass.operations.jai.slope;

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
public class HMSlopeRIF implements RenderedImageFactory {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.image.renderable.RenderedImageFactory#create(java.awt.image.
     * renderable.ParameterBlock, java.awt.RenderingHints)
     */

    public RenderedImage create( ParameterBlock paramBlock, RenderingHints hints ) {
        RenderedImage source1 = paramBlock.getRenderedSource(0);
        RenderedImage source2 = paramBlock.getRenderedSource(1);
        ImageLayout layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
        // were the last parameter is true if we would to manage only a single
        // tile of the input file, if we put false we can holding every pixel in
        // the input PlanarImage
        PrintStream out= (PrintStream) paramBlock.getObjectParameter(0);
        PrintStream err= (PrintStream) paramBlock.getObjectParameter(1);        
        boolean doTile = (Boolean) paramBlock.getObjectParameter(2);
        double dx = paramBlock.getDoubleParameter(3);
        double dy = paramBlock.getDoubleParameter(4);
        BorderExtender borderExtender = null;
        // ImageLayout layout = null;
        return new HMSlopeOpImage(source1,source2, borderExtender, hints, layout,dx,dy, doTile, err,out);
    }

}
