package eu.hydrologis.jgrass.operations.jai.curvatures;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
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
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsConstants;
/**
 * This class calculate the curvatures.
 * <p>
 ** <li><i><b>planar curvature</b></i>: plan=f<sub>xx</sub>f<sub>y</sub>&sup2 -2 f <sub>xy</sub>
 * f<sub>x</sub> f<sub>y</sub> +f <sub>yy</sub> f<sub>x</sub>&sup2 /&radic (p &sup3)</li>
 * <li><i><b>profile curvature</i></b>: prof=f<sub>xx</sub>f<sub>x</sub>&sup2 +2 f <sub>xy</sub>
 * f<sub>x</sub> f<sub>y</sub> +f <sub>yy</sub> f<sub>y</sub>&sup2 /(p &radic (q &sup3)</li>
 * <li><i><b>tangential curvature</i></b>: tang=f<sub>xx</sub>f<sub>y</sub>&sup2 -2 f <sub>xy</sub>
 * f<sub>x</sub> f<sub>y</sub> +f <sub>yy</sub> f<sub>x</sub>&sup2 /(p &radic (q))</li> where:
 * p=f<sub>x</sub> &sup2+f<sub>y</sub> &sup2 and q=p+1;
 * </p>
 * <p>
 * Extend the {@link AreaOpImage} because a <i>sample</i> have needed, to be calculate, to the
 * around pixel. So in the simpler case extend {@link AreaOpImage} is the best way.
 * </p>
 * <p>
 * The reason is that the superclass can holding the border situation in an easy way, in order to
 * avoid to manage directly this situation.
 * </p>
 * 
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 *@version alpha @30 Jan 2009
 *@FlowOpImage
 *@see AreaOpImage, {@link BorderExtender}
 */

public class HMCurvaturesOpImage extends JGrassAreaOpImage {
    /**
     * Initialise the class.
     * <p>
     * Call the super constructor.
     * </p>
     */
    private PrintStream out;
    private static final double DEFAULT_NO_VALUE = JGrassConstants.doubleNovalue;

    private final static int[][] DIR = ModelsConstants.DIR;

    public HMCurvaturesOpImage( RenderedImage source1, BorderExtender extender, Map config,
            ImageLayout layout, PrintStream err, PrintStream out, double dx, double dy,
            boolean doTile ) {

        super(source1, layout, config, doTile, null, 1, 1, 1, 1, dx, dy, err, out);

        // This operation doesn't have a border extender so the boundary is
        // equal to 0.
        // To calculate the maximum slope direction are necessary the eight
        // point around the pixel, so the last four parameters indicate this.

        this.out = out;

    }

    /*
     * Calculate the slope and set it as a output
     */
    @Override
    protected void computeRect( PlanarImage[] sources, WritableRaster dest, Rectangle destRect ) {
        PlanarImage source = sources[0];
        // set the region, the AreaOpImage set the destRect only for tiling, so I have set the
        // destRegion, smaller than the Image region, in order to doesn't obtain an array index out
        // of boundary.
        Rectangle region = new Rectangle(destRect.x + 1, destRect.y + 1, destRect.width - 2,
                destRect.height - 2);
        curvatures(source.getData(), dest, region);
    }

    @Override
    protected void computeRect( Raster[] sources, WritableRaster dest, Rectangle destRect ) {
        curvatures(sources[0], dest, destRect);
    }

    /*
     * Evaluate the derivative with the Evans scheme
     */

    private void curvatures( Raster elevation, WritableRaster curvature, Rectangle destRect ) {

        // the origin of the Rectangle where there is possible calculate the
        // slope.
        int xOrigin = destRect.x;
        int yOrigin = destRect.y;
        // Cycling into the valid region.
        int maxNCols = xOrigin + destRect.width;
        int maxNRows = yOrigin + destRect.height;

        // create new matrix
        // first derivative
        SampleModel sm = curvature.getSampleModel();
        Point org = new Point(destRect.x, destRect.y);
        WritableRaster sxData = createWritableRaster(sm, org);
        WritableRaster syData = createWritableRaster(sm, org);
        // second derivative
        WritableRaster sxxData = createWritableRaster(sm, org);
        WritableRaster syyData = createWritableRaster(sm, org);
        WritableRaster sxyData = createWritableRaster(sm, org);
        double plan = 0.0;
        double tang = 0.0;
        double prof = 0.0;

        // calculate ...

        /*------------------------------------first derivative ----------------------------------------------*/

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask("Calculating first derivates...", maxNCols - xOrigin);
        // calculate the firs order derivative
        for( int x = xOrigin; x < maxNCols; x++ ) {
            for( int y = yOrigin; y < maxNRows; y++ ) {

                if (isNovalue(elevation.getSampleDouble(x, y, 0))) {
                    sxData.setSample(x, y, 0, DEFAULT_NO_VALUE);
                    syData.setSample(x, y, 0, DEFAULT_NO_VALUE);
                } else {
                    sxData.setSample(x, y, 0, 0.5
                            * (elevation.getSampleDouble(x, y + 1, 0) - elevation.getSampleDouble(
                                    x, y - 1, 0)) / xRes);
                    syData.setSample(x, y, 0, 0.5
                            * (elevation.getSampleDouble(x + 1, y, 0) - elevation.getSampleDouble(
                                    x - 1, y, 0)) / yRes);
                }
            }
            pm.worked(1);
        }
        pm.done();
        
        /*-------------------------------------second derivative----------------------------------------*/

        pm.beginTask("Calculating second derivates...", maxNRows - yOrigin);
        double disXX = Math.pow(xRes, 2.0);
        double disYY = Math.pow(yRes, 2.0);
        // calculate the second order derivative
        for( int j = yOrigin; j < maxNRows; j++ ) {
            for( int i = xOrigin; i < maxNCols; i++ ) {
                if (isNovalue(elevation.getSampleDouble(i, j, 0))) {
                    sxxData.setSample(i, j, 0, DEFAULT_NO_VALUE);
                    syyData.setSample(i, j, 0, DEFAULT_NO_VALUE);
                    sxyData.setSample(i, j, 0, DEFAULT_NO_VALUE);

                } else {
                    sxxData.setSample(i, j, 0, ((elevation.getSampleDouble(i, j + 1, 0) - 2
                            * elevation.getSampleDouble(i, j, 0) + elevation.getSampleDouble(i,
                            j - 1, 0)) / disXX));
                    syyData.setSample(i, j, 0, ((elevation.getSampleDouble(i + 1, j, 0) - 2
                            * elevation.getSampleDouble(i, j, 0) + elevation.getSampleDouble(i - 1,
                            j, 0)) / disYY));
                    sxyData.setSample(i, j, 0, 0.25 * ((elevation.getSampleDouble(i + 1, j + 1, 0)
                            - elevation.getSampleDouble(i + 1, j - 1, 0)
                            - elevation.getSampleDouble(i - 1, j + 1, 0) + elevation
                            .getSampleDouble(i - 1, j - 1, 0)) / (xRes * yRes)));
                }
            }
            pm.worked(1);
        }
        pm.done();

        /*---------------------------------------curvatures---------------------------------------------*/

        double p, q;

        //calculate curvatures
        pm.beginTask("Calculating curvatures...", maxNRows - yOrigin);
        for( int j = yOrigin; j < maxNRows; j++ ) {
            for( int i = xOrigin; i < maxNCols; i++ ) {
                if (isNovalue(elevation.getSampleDouble(i, j, 0))) {
                    plan = DEFAULT_NO_VALUE;
                    tang = DEFAULT_NO_VALUE;
                    prof = DEFAULT_NO_VALUE;

                } else {
                    double sxSample = sxData.getSampleDouble(i, j, 0);
                    double sySample = syData.getSampleDouble(i, j, 0);
                    p = Math.pow(sxSample, 2.0) + Math.pow(sySample, 2.0);
                    q = p + 1;
                    if (p == 0.0) {
                        plan = 0.0;
                        tang = 0.0;
                        prof = 0.0;

                    } else {

                        double sxxSample = sxxData.getSampleDouble(i, j, 0);
                        double sxySample = sxyData.getSampleDouble(i, j, 0);
                        double syySample = syyData.getSampleDouble(i, j, 0);
                        plan = (sxxSample * Math.pow(sySample, 2.0) - 2 * sxySample * sxSample
                                * sySample + syySample * Math.pow(sxSample, 2.0))
                                / (Math.pow(p, 1.5));
                        tang = (sxxSample * Math.pow(sySample, 2.0) - 2 * sxySample * sxSample
                                * sySample + syySample * Math.pow(sxSample, 2.0))
                                / (p * Math.pow(q, 0.5));
                        prof = (sxxSample * Math.pow(sxSample, 2.0) + 2 * sxySample * sxSample
                                * sySample + syySample * Math.pow(sySample, 2.0))
                                / (p * Math.pow(q, 1.5));
                    }

                }
                double[] curvatureArray = new double[]{plan, tang, prof};
                curvature.setPixel(i, j, curvatureArray);
            }
            pm.worked(1);
        }
        pm.done();

    }
}
