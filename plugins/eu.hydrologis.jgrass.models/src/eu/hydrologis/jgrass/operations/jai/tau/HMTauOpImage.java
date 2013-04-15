package eu.hydrologis.jgrass.operations.jai.tau;

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
import eu.hydrologis.jgrass.models.h.tau.h_tau;
import eu.hydrologis.libs.messages.Messages;

/**
 * This class calculate the tangential stress in each pixel of the active
 * region.

 * <p>
 * Extend the {@link AreaOpImage} because a <i>sample</i> have needed, to be
 * calculate, to the around pixel.
 * </p>
 * 
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha @30 Jan 2009
 *@FlowOpImage
 *@see {@link AreaOpImage}, {@link BorderExtender},{@link h_tau}, {@link HMTauDescriptor}
 */

public class HMTauOpImage extends PointOpImage {

    double rho = -1;

    double g = -1;

    double ni = -1;

    double q = -1;

    double k = -1;

    double c = -1;

    double t = -1;

    PrintStream out = null;

    PrintStream err = null;

    private static final double DEFAULT_NO_VALUE = JGrassConstants.doubleNovalue;

    /**
     * Initialise the class.
     * <p>
     * Call the super constructor.
     * </p>
     * 
     * @param t
     * @param c
     * @param k
     * @param q
     * @param ni
     */
    public HMTauOpImage( RenderedImage slope, RenderedImage ab, double rho, double g, double ni,
            double q, double k, double c, double t, BorderExtender extender, Map config,
            ImageLayout layout, boolean doTile, PrintStream err, PrintStream out ) {

        super(slope, ab, layout, config, doTile);
        this.out = out;
        this.err = err;
        this.rho = rho;
        this.g = g;
        this.ni = ni;
        this.q = q;
        this.k = k;
        this.c = c;
        this.t = t;

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
        tau(sources[0].getData(), sources[1].getData(), dest, destRect);
    }

    @Override
    protected void computeRect( Raster[] sources, WritableRaster dest, Rectangle destRect ) {

        tau(sources[0], sources[1], dest, destRect);
    }

    /*
     * Evaluate the matrix of the aspect
     */

    private void tau( Raster slopeRaster, Raster abRaster, WritableRaster tauRaster,
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
        double[] esp = new double[4];
        double[] term = new double[6];

        // calculates exponents
        esp[0] = 1.0 / 3.0;
        esp[1] = -c / 3.0;
        esp[2] = (2 + c) / 3.0;
        esp[3] = 2.0 / 3.0;
        // calculates the constant term of the expression above.
        term[0] = Math.pow(g, 2);
        term[1] = Math.pow(term[0], esp[0]) * rho * 0.5;
        term[2] = Math.pow(ni, esp[1]);
        term[3] = Math.pow(k, esp[0]);

        // calculats the stress tangential...
        out.println(Messages.getString("working") + " h.tau");

        for( int i = minY; i < maxNRows; i++ ) {
            for( int j = minY; j < maxNCols; j++ ) {
                if (isNovalue(slopeRaster.getSampleDouble(j, i, 0))) {
                    tauRaster.setSample(j, i, 0, JGrassConstants.doubleNovalue);
                } else {
                    term[4] = (q * abRaster.getSampleDouble(j, i, 0) - t
                            * slopeRaster.getSampleDouble(j, i, 0));
                    if (term[4] <= 0) {
                        tauRaster.setSample(j, i, 0, 0);
                    } else {
                        term[5] = Math.pow(term[4], esp[2]);
                        tauRaster
                                .setSample(j, i, 0,
                                        (term[1]
                                                * term[3]
                                                * term[2]
                                                * Math.pow(slopeRaster.getSampleDouble(j, i, 0),
                                                        esp[3]) * term[5]));
                    }
                }
            }
        }

    }
}
