package eu.hydrologis.jgrass.operations.jai.aspect;

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

import eu.hydrologis.jgrass.libs.jai.JGrassAreaOpImage;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.jgrass.models.h.aspect.h_aspect;
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.messages.Messages;
/**
 * This class calculate the aspect. 
 * <p>
 * It estimate the <b>aspect</b>, the inclination angle of the gradient. The value is given by the
 * formula: &#945 = arctan(f<sub>x</sub>/f<sub>y</sub>) where <i>f<sub>x</sub></i> is the x
 * derivative and <i>f<sub>y</sub></i> is the y derivative.
 * </p>
 * <p>
 * Extend the {@link AreaOpImage} because a <i>sample</i> have needed, to be calculate, to the
 * around pixel.
 * </p>
 * 
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha @30 Jan 2009
 *@FlowOpImage
 *@see {@link AreaOpImage}, {@link BorderExtender},{@link h_aspect}
 */

public class HMAspectOpImage extends JGrassAreaOpImage {

    // the conversion factor, from radiants to degree.
    private double radtodeg = 360.0 / (2 * Math.PI);

    private static final double DEFAULT_NO_VALUE = JGrassConstants.doubleNovalue;
    /**
     * Initialise the class.
     * <p>
     * Call the super constructor.
     * </p>
     */
    public HMAspectOpImage( RenderedImage source1, BorderExtender extender, Map config,
            ImageLayout layout, double dx, double dy, boolean doTile, PrintStream err,
            PrintStream out ) {

        super(source1, layout, config, doTile, null, 1, 1, 1, 1, dx, dy, err, out);

        numTile = this.getNumXTiles() * getNumYTiles();

    }

    /*
     */
    @Override
    protected void computeRect( PlanarImage[] sources, WritableRaster dest, Rectangle destRect ) {
        PlanarImage source = sources[0];
        // set the region, the AreaOpImage set the destRect only for tiling, so I have set the
        // destRegion, smaller than the Image region, in order to doesn't obtain an array index out
        // of boundary.
        Rectangle region = new Rectangle(destRect.x + 1, destRect.y + 1, destRect.width - 2,
                destRect.height - 2);
        aspect(source.getData(), dest, region);
    }

    @Override
    protected void computeRect( Raster[] sources, WritableRaster dest, Rectangle destRect ) {

        aspect(sources[0], dest, destRect);
    }

    /*
     * Evaluate the matrix of the aspect
     */

    private void aspect( Raster elevation, WritableRaster aspectRaster, Rectangle destRect ) {

        // the origin of the Rectangle where there is possible calculate the
        // slope.
        int xOrigin = destRect.x;
        int yOrigin = destRect.y;
        // the limits of the rectangle where calculate
        int maxNCols = xOrigin + destRect.width;
        int maxNRows = yOrigin + destRect.height;

        // the value of the x and y derivative
        double aData = 0.0;
        double bData = 0.0;

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + " h.aspect...", maxNRows - yOrigin);

        // Cycling into the valid region.
        for( int j = yOrigin; j < maxNRows; j++ ) {
            for( int i = xOrigin; i < maxNCols; i++ ) {
                // calculate the y derivative
                double centralValue = elevation.getSampleDouble(i, j, 0);

                if (!isNovalue(centralValue)) {
                    double valuePostJ = elevation.getSampleDouble(i, j + 1, 0);
                    double valuePreJ = elevation.getSampleDouble(i, j - 1, 0);
                    if (!isNovalue(valuePostJ) && !isNovalue(valuePreJ)) {
                        aData = Math.atan((valuePreJ - valuePostJ) / (2 * yRes));
                    }
                    if (isNovalue(valuePreJ) && (!isNovalue(valuePreJ))) {
                        aData = Math.atan((centralValue - valuePostJ) / (yRes));
                    }
                    if (!isNovalue(valuePreJ) && isNovalue(valuePostJ)) {
                        aData = Math.atan((valuePreJ - centralValue) / (yRes));
                    }
                    if (isNovalue(valuePreJ) && isNovalue(valuePostJ)) {
                        aData = DEFAULT_NO_VALUE;
                    }
                    // calculate the x derivative
                    double valuePreI = elevation.getSampleDouble(i - 1, j, 0);
                    double valuePostI = elevation.getSampleDouble(i + 1, j, 0);
                    if (!isNovalue(valuePreI) && !isNovalue(valuePostI)) {
                        bData = Math.atan((valuePreI - valuePostI) / (2 * xRes));
                    }
                    if (isNovalue(valuePreI) && !isNovalue(valuePostI)) {
                        bData = Math.atan((centralValue - valuePostI) / (xRes));
                    }
                    if (!isNovalue(valuePreI) && isNovalue(valuePostI)) {
                        bData = Math.atan((valuePreI - centralValue) / (xRes));
                    }
                    if (isNovalue(valuePreI) && isNovalue(valuePostI)) {
                        bData = DEFAULT_NO_VALUE;
                    }

                    double delta = 0.0;
                    // calculate the aspect value
                    if (aData < 0 && bData > 0) {
                        delta = Math.acos(Math.sin(Math.abs(aData))
                                * Math.cos(Math.abs(bData))
                                / (Math.sqrt((double) 1 - Math.pow(Math.cos(aData), (double) 2)
                                        * Math.pow(Math.cos(bData), (double) 2))));

                        aspectRaster.setSample(i, j, 0, Math.round(delta * radtodeg));
                    }
                    if (aData > 0 && bData > 0) {
                        delta = Math.acos(Math.sin(Math.abs(aData))
                                * Math.cos(Math.abs(bData))
                                / (Math.sqrt((double) 1 - Math.pow(Math.cos(aData), (double) 2)
                                        * Math.pow(Math.cos(bData), (double) 2))));
                        aspectRaster.setSample(i, j, 0, Math.round((Math.PI - delta) * radtodeg));
                    }
                    if (aData > 0 && bData < 0) {
                        delta = Math.acos(Math.sin(Math.abs(aData))
                                * Math.cos(Math.abs(bData))
                                / (Math.sqrt((double) 1 - Math.pow(Math.cos(aData), (double) 2)
                                        * Math.pow(Math.cos(bData), (double) 2))));
                        aspectRaster.setSample(i, j, 0, Math.round((Math.PI + delta) * radtodeg));
                    }
                    if (aData < 0 && bData < 0) {
                        delta = Math.acos(Math.sin(Math.abs(aData))
                                * Math.cos(Math.abs(bData))
                                / (Math.sqrt((double) 1 - Math.pow(Math.cos(aData), (double) 2)
                                        * Math.pow(Math.cos(bData), (double) 2))));
                        aspectRaster.setSample(i, j, 0, Math
                                .round((2 * Math.PI - delta) * radtodeg));
                    }
                    if (aData == 0 && bData > 0) {
                        aspectRaster.setSample(i, j, 0, Math.round((Math.PI / 2.) * radtodeg));
                    }
                    if (aData == 0 && bData < 0) {
                        aspectRaster.setSample(i, j, 0, Math.round((Math.PI * 3. / 2.) * radtodeg));
                    }
                    if (aData > 0 && bData == 0) {
                        aspectRaster.setSample(i, j, 0, Math.round(Math.PI * radtodeg));
                    }
                    if (aData < 0 && bData == 0) {
                        aspectRaster.setSample(i, j, 0, 360.0);
                    }
                    if (aData == 0 && bData == 0) {
                        aspectRaster.setSample(i, j, 0, 0);
                    }
                } else {
                    aspectRaster.setSample(i, j, 0, DEFAULT_NO_VALUE);
                }

            }
            pm.worked(1);
        }
        pm.done();
    }
}
