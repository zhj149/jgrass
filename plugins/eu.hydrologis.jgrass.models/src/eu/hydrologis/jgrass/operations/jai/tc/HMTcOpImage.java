package eu.hydrologis.jgrass.operations.jai.tc;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.PrintStream;
import java.util.Map;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;

import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.models.h.aspect.h_aspect;
import eu.hydrologis.libs.messages.Messages;

/**
 * This class calculate the topographic classes.
 * 
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha @30 Jan 2009
 *@FlowOpImage
 *@see  {@link BorderExtender},{@link h_aspect}
 */

public class HMTcOpImage extends PointOpImage {
    // the second output map value (aggregation classes)
    private static final int CONVEX = 35;

    private static final int PLANAR = 25;

    private static final int CONCAVE = 15;

    // the first output value.
    private static final int CONCAVE_CONCAVE_SITE = 90;

    private static final int CONVEX_CONCAVE_SITE = 80;

    private static final int PLANAR_CONCAVE_SITE = 70;

    private static final int CONCAVE_CONVEX_SITE = 60;

    private static final int CONVEX_CONVEX_SITE = 50;

    private static final int PLANAR_CONVEX_SITE = 40;

    private static final int CONCAVE_PLANAR_SITE = 30;

    private static final int CONVEX_PLANAR_SITE = 20;

    private static final int PLANAR_PLANAR_SITE = 10;

    // the conversion f gcRasteractor, from radiants to degree.
    private double thProf = 0;

    private double thTang = 0;

    PrintStream out = null;

    PrintStream err = null;

    private static final double DEFAULT_NO_VALUE = JGrassConstants.doubleNovalue;

    /**
     * Initialise the class.
     * <p>
     * Call the super constructor.
     * </p>
     */
    public HMTcOpImage( RenderedImage longCurvatures, RenderedImage tangCurvatures, double thLong,
            double thTang, BorderExtender extender, Map config, ImageLayout layout, boolean doTile,
            PrintStream err, PrintStream out ) {

        super(longCurvatures, tangCurvatures, layout, config, doTile);
        this.out = out;
        this.err = err;
        this.thProf = thLong;
        this.thTang = thTang;

        out.println(Messages.getString("working") + " h.tc"); //$NON-NLS-1$

    }

    /*
     */
    @Override
    protected void computeRect( PlanarImage[] sources, WritableRaster dest, Rectangle destRect ) {
        PlanarImage source = sources[0];
        // set the region, the AreaOpImage set the destRect only for tiling, so
        // I have set the
        // destRegion, smaller than the Image region, in order to doesn't obtain
        // an array index out
        // of boundary.
        Rectangle region = new Rectangle(destRect.x, destRect.y, destRect.width - 1,
                destRect.height - 1);
        tc(sources[0].getData(), sources[1].getData(), dest, destRect);
    }

    @Override
    protected void computeRect( Raster[] sources, WritableRaster dest, Rectangle destRect ) {

        tc(sources[0], sources[1], dest, destRect);
    }

    /*
     * Evaluate the matrix of the aspect
     */

    private void tc( Raster profRaster, Raster tangRaster, WritableRaster tcRaster,
            Rectangle destRect ) {

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

        for( int j = yOrigin; j < maxNRows; j++ ) {
            for( int i = xOrigin; i < maxNCols; i++ ) {
                if (isNovalue(tangRaster.getSampleDouble(i, j, 0))) {
                    tcRaster.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                } else if (Math.abs(tangRaster.getSampleDouble(i, j, 0)) <= thTang) {
                    if (Math.abs(profRaster.getSampleDouble(i, j, 0)) <= thProf) {
                        tcRaster.setSample(i, j, 0, PLANAR_PLANAR_SITE);
                    } else if (profRaster.getSampleDouble(i, j, 0) < -thProf) {
                        tcRaster.setSample(i, j, 0, CONVEX_PLANAR_SITE);
                    } else if (profRaster.getSampleDouble(i, j, 0) > thProf) {
                        tcRaster.setSample(i, j, 0, CONCAVE_PLANAR_SITE);
                    }
                } else if (tangRaster.getSampleDouble(i, j, 0) < -thTang) {
                    if (Math.abs(profRaster.getSampleDouble(i, j, 0)) <= thProf) {
                        tcRaster.setSample(i, j, 0, PLANAR_CONVEX_SITE);
                    } else if (profRaster.getSampleDouble(i, j, 0) < -thProf) {
                        tcRaster.setSample(i, j, 0, CONVEX_CONVEX_SITE);
                    } else if (profRaster.getSampleDouble(i, j, 0) > thProf) {
                        tcRaster.setSample(i, j, 0, CONCAVE_CONVEX_SITE);
                    }
                } else if (tangRaster.getSampleDouble(i, j, 0) > thTang) {
                    if (Math.abs(profRaster.getSampleDouble(i, j, 0)) <= thProf) {
                        tcRaster.setSample(i, j, 0, PLANAR_CONCAVE_SITE);
                    } else if (profRaster.getSampleDouble(i, j, 0) < -thProf) {
                        tcRaster.setSample(i, j, 0, CONVEX_CONCAVE_SITE);
                    } else if (profRaster.getSampleDouble(i, j, 0) > thProf) {
                        tcRaster.setSample(i, j, 0, CONCAVE_CONCAVE_SITE);
                    }
                }
            }
        }
        // calculate ...
        out.println(Messages.getString("h_tc.cp3"));

        for( int j = yOrigin; j < maxNRows; j++ ) {
            for( int i = xOrigin; i < maxNCols; i++ ) {
                if (!isNovalue(tcRaster.getSampleDouble(i, j, 0))) {
                    if (tcRaster.getSampleDouble(i, j, 0) == PLANAR_CONCAVE_SITE
                            || tcRaster.getSampleDouble(i, j, 0) == CONCAVE_CONCAVE_SITE
                            || tcRaster.getSampleDouble(i, j, 0) == CONCAVE_PLANAR_SITE) {
                        tcRaster.setSample(i, j, 1, CONCAVE);
                    } else if (tcRaster.getSampleDouble(i, j, 0) == PLANAR_PLANAR_SITE) {
                        tcRaster.setSample(i, j, 1, PLANAR);
                    } else {
                        tcRaster.setSample(i, j, 1, CONVEX);
                    }
                } else {
                    tcRaster.setSample(i, j, 1, tcRaster.getSampleDouble(i, j, 0));
                }
            }
        }

    }
}
