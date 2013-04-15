package eu.hydrologis.jgrass.operations.jai.topindex;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.PrintStream;
import java.util.Map;

import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;

import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.models.h.aspect.h_aspect;
import eu.hydrologis.libs.messages.Messages;

/**
 * This class calculate the topographic index.
 * <p>
 * 
 * </p>
 * 
 * 
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha @30 Jan 2009
 *@FlowOpImage
 *@see {@link AreaOpImage}, {@link BorderExtender},{@link h_aspect}
 */

public class HMTopIndexOpImage extends PointOpImage {

    // the conversion f gcRasteractor, from radiants to degree.

    PrintStream out = null;

    PrintStream err = null;

    private static final double DEFAULT_NO_VALUE = JGrassConstants.doubleNovalue;

    /**
     * Initialise the class.
     * <p>
     * Call the super constructor.
     * </p>
     * 
     */
    public HMTopIndexOpImage(RenderedImage slope, RenderedImage tca,
            BorderExtender extender, Map config, ImageLayout layout,
            boolean doTile, PrintStream err, PrintStream out) {

        super(slope, tca, layout, config, doTile);
        this.out = out;
        this.err = err;

        out.println(Messages.getString("working") + " h.tc"); //$NON-NLS-1$

    }

    /*
     */
    @Override
    protected void computeRect(PlanarImage[] sources, WritableRaster dest,
            Rectangle destRect) {
        PlanarImage source = sources[0];
        // set the region, the AreaOpImage set the destRect only for tiling, so
        // I have set the
        // destRegion, smaller than the Image region, in order to doesn't obtain
        // an array index out
        // of boundary.
        Rectangle region = new Rectangle(destRect.x, destRect.y,
                destRect.width - 1, destRect.height - 1);
        topindex(sources[0].getData(), sources[1].getData(), dest, destRect);
    }

    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest,
            Rectangle destRect) {

        topindex(sources[0], sources[1], dest, destRect);
    }

    /*
     * Evaluate the matrix of the aspect
     */

    private void topindex(Raster slopeRaster, Raster tcaRaster,
            WritableRaster topIndexRaster, Rectangle destRect) {

        // the origin of the Rectangle where there is possible calculate the
        // slope.
        int xOrigin = destRect.x;
        int yOrigin = destRect.y;
        // the limits of the rectangle where calculate
        int maxNCols = xOrigin + destRect.width;
        int maxNRows = yOrigin + destRect.height;
        // calculate ...
        out.println(Messages.getString("working") + " h.gc"); //$NON-NLS-1$
        out.println(Messages.getString("working12")); //$NON-NLS-1$
        double[] esp = new double[4];
        double[] term = new double[6];

        for (int j = yOrigin; j < maxNRows; j++) {
            for (int i = xOrigin; i < maxNCols; i++) {
                if (isNovalue(tcaRaster.getSampleDouble(i, j, 0))) {
                    topIndexRaster.setSample(i, j, 0,
                            JGrassConstants.doubleNovalue);
                } else {
                    if (slopeRaster.getSampleDouble(i, j, 0) != 0) {
                        topIndexRaster.setSample(i, j, 0, Math.log(tcaRaster
                                .getSampleDouble(i, j, 0)
                                / slopeRaster.getSampleDouble(i, j, 0)));
                    } else {
                        topIndexRaster.setSample(i, j, 0,
                                JGrassConstants.doubleNovalue);
                    }
                }
            }
        }
    }

}
