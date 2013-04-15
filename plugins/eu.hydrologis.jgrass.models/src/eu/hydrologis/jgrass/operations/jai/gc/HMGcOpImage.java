package eu.hydrologis.jgrass.operations.jai.gc;

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
 * This class calculate the thopographic classes in the pixel which are
 * belonging to the network.
 * 
 * <p>
 * the value in the banded 0 are:
 * <li>10 planar-planar sites.</li>
 * <li>20 convex-planar sites.</li>
 * <li>30 concave-planar sites.</li>
 * <li>40 planar-convex sites.</li>
 * <li>50 convex-convex sites.</li>
 * <li>60 concave-convex sites.</li>
 * <li>70 planar-concave sites.</li>
 * <li>80 convex-concave sites.</li>
 * <li>90 concave-concave sites.</li>
 * <li>100 channel sites.</li>
 * <li>110 unconditionally unstable sites.</li>
 * 
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha @30 Jan 2009
 *@FlowOpImage
 *@see {@link BorderExtender},{@link h_gc}
 */

public class HMGcOpImage extends PointOpImage {

    private static final int UNSTABLE = 55;

    private static final int HILLSLOPE = 45;

    private static final int CHANNEL = 35;

    private static final int PLANAR = 25;

    private static final int NON_CHANNEL = 15;

    // the conversion factor, from radiants to degree.
    private static final int CHANNEL_SITE = 100;

    private static final int UNSTABLE_SITE = 110;

    private static final int PLANAR_PLANAR_SITE = 10;

    private static final int CONVEX_PLANAR_SITE = 20;

    private static final int CONCAVE_PLANAR_SITE = 30;

    private static final int PLANAR_CONVEX_SITE = 40;

    private static final int CONVEX_CONVEX_SITE = 50;

    private static final int CONCAVE_CONVEX_SITE = 60;

    private static final int PLANAR_CONCAVE_SITE = 70;

    private static final int CONVEX_CONCAVE_SITE = 80;

    private static final int CONCAVE_CONCAVE_SITE = 90;

    private double thGrad = 0;

    PrintStream out = null;

    PrintStream err = null;

    private static final double DEFAULT_NO_VALUE = JGrassConstants.doubleNovalue;

    /**
     * Initialise the class.
     * <p>
     * Call the super constructor.
     * </p>
     */
    public HMGcOpImage(RenderedImage slope, RenderedImage net,
            RenderedImage cp9, double thGrad, BorderExtender extender,
            Map config, ImageLayout layout, boolean doTile, PrintStream err,
            PrintStream out) {

        super(slope, net, cp9, layout, config, doTile);
        this.out = out;
        this.err = err;
        this.thGrad = thGrad;

        out.println(Messages.getString("working") + " h.aspect"); //$NON-NLS-1$

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
        gc(sources[0].getData(), sources[1].getData(), sources[2].getData(),
                dest, destRect);
    }

    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest,
            Rectangle destRect) {

        gc(sources[0], sources[1], sources[2], dest, destRect);
    }

    /*
     * Evaluate the matrix of the aspect
     */

    private void gc(Raster slope, Raster net, Raster cp9,
            WritableRaster gcRaster, Rectangle destRect) {

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
        for (int j = yOrigin; j < maxNRows; j++) {
            for (int i = xOrigin; i < maxNCols; i++) {
                // individuates the pixel with a slope greater than the
                // threshold
                if (slope.getSampleDouble(i, j, 0) >= thGrad) {
                    gcRaster.setSample(i, j, 0, UNSTABLE_SITE);
                }
                // individuates the network
                else if (net.getSample(i, j, 0) == 2) {
                    gcRaster.setSample(i, j, 0, CHANNEL_SITE);
                }
                if (isNovalue(slope.getSampleDouble(i, j, 0))) {
                    gcRaster.setSample(i, j, 0, JGrassConstants.intNovalue);
                }
            }
        }
        out.println(Messages.getString("working22")); //$NON-NLS-1$
        // aggregation of these classes:
        // 15 ? non-channeled valley sites (classes 70, 90, 30 )
        // 25 ? planar sites (class 10)
        // 35 ? channel sites (class 100)
        // 45 ? hillslope sites (classes 20, 40, 50, 60, 80)
        // 55 ? ravine sites (slope > critic value) (class 110).
        for (int j = yOrigin; j < maxNRows; j++) {
            for (int i = xOrigin; i < maxNCols; i++) {
                if (cp9.getSample(i, j, 0) == PLANAR_CONCAVE_SITE
                        || cp9.getSample(i, j, 0) == CONCAVE_CONCAVE_SITE
                        || cp9.getSample(i, j, 0) == CONCAVE_PLANAR_SITE) {
                    gcRaster.setSample(i, j, 1, NON_CHANNEL);
                } else if (cp9.getSample(i, j, 0) == PLANAR_PLANAR_SITE) {
                    gcRaster.setSample(i, j, 1, PLANAR);
                } else if (cp9.getSample(i, j, 0) == CHANNEL_SITE) {
                    gcRaster.setSample(i, j, 1, CHANNEL);
                } else if (cp9.getSample(i, j, 0) == UNSTABLE_SITE) {
                    gcRaster.setSample(i, j, 1, UNSTABLE);
                } else if (!isNovalue(cp9.getSample(i, j, 0))) {
                    gcRaster.setSample(i, j, 1, HILLSLOPE);
                } else if (isNovalue(cp9.getSample(i, j, 0))) {
                    gcRaster.setSample(i, j, 1, JGrassConstants.intNovalue);
                }
            }
        }

    }
}
