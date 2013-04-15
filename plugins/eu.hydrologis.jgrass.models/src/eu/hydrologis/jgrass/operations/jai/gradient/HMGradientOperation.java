/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.operations.jai.gradient;

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
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.messages.Messages;
import  static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;
/**
 * The {@link AreaOpImage area operation} that computes the gradient raster.
 * <p>
 * It estimate the gradient using finite difference formula.
 * </p>
 * <p>
 * The algoritms extends the AreaOpImage classes because it needs, to compute a sample, to acceses
 * to the near pixel (a kernel of 3x3). *
 * 
 * <pre>
 *  p=&radic{f<sub>x</sub>&sup2;+f<sub>y</sub>&sup2;}
 * f<sub>x</sub>=(f(x+1,y)-f(x-1,y))/(2 &#916 x) 
 * f<sub>y</sub>=(f(x,y+1)-f(x,y-1))/(2 &#916 y)
 * </pre>
 * 
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com), <a /href='mailto:daniele.andreis@gmail.com>Daniele
 *         andreis</a>
 */
public class HMGradientOperation extends JGrassAreaOpImage {
    // The MyAreaOpimage class add some fields and override some methods. Th efields are:
    // x resolution double xRes.
    // y resolution double yRes.
    // PrintStream err.
    // protected PrintStream out.
    // the number of tile to be computed numTile.
    // the tiles whch is computing currentTile.
    //

    private static final double DEFAULT_NO_VALUE = JGrassConstants.doubleNovalue;

    /**
     * @param source1 the elevation Image (depitted).
     * @param extender null.
     * @param config
     * @param layout
     * @param dx the x resolution.
     * @param dy y resolution.
     * @param doTile the cobbleSource parameter.
     * @param err the error output stream.
     * @param out the output stream.
     */
    public HMGradientOperation( RenderedImage source1, BorderExtender extender, Map config,
            ImageLayout layout, double dx, double dy, boolean doTile, PrintStream err,
            PrintStream out ) {
        super(source1, layout, config, doTile, extender, 1, 1, 1, 1, dx, dy, err, out);
    }

    protected void computeRect( PlanarImage[] sources, WritableRaster gradientRaster,
            Rectangle destRect ) {
        PlanarImage source = sources[0];
        // set the region, the AreaOpImage set the destRect only for tiling, so I have set the
        // destRegion, smaller than the Image region, in order to doesn't obtain an array index out
        // of boundary.
        Rectangle region = new Rectangle(destRect.x + 1, destRect.y + 1, destRect.width - 2,
                destRect.height - 2);
        gradient(source.getData(), gradientRaster, region);

    }

    protected void computeRect( Raster[] sources, WritableRaster dest, Rectangle destRect ) {
        gradient(sources[0], dest, destRect);

    }

    /**
     * Computes the gradient algoritm. p=f_{x}^{2}+f_{y}^{2}
     * 
     * @param elevation the elevation data.
     * @param gradientRaster the {@link WritableRaster output raster} to which the gradient values
     *        are written
     * @param destRect ???
     */
    private void gradient( Raster elevation, WritableRaster gradientRaster, Rectangle destRect ) {
        // set the value of the region where calculate the values, it excluded the border. N.B. when
        // applied the tiling the origin of the raster is the left upper border of the tile which is
        // processed and the destRect is computed in order that, every pixel in this region, have
        // access to the near pixel
        int xOrigin = destRect.x;
        int yOrigin = destRect.y;
        int nrows = destRect.height;
        int ncols = destRect.width;
        int maxNRows = yOrigin + nrows;
        int maxNCols = xOrigin + ncols;

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.gradients...", maxNRows - yOrigin);
        for( int y = yOrigin; y < maxNRows; y++ ) {
            for( int x = xOrigin; x < maxNCols; x++ ) {
                // extract the value to use for the algoritm. It is the finite difference approach.
                double elevIJ = elevation.getSampleDouble(x, y, 0);
                double elevIJipre = elevation.getSampleDouble(x - 1, y, 0);
                double elevIJipost = elevation.getSampleDouble(x + 1, y, 0);
                double elevIJjpre = elevation.getSampleDouble(x, y - 1, 0);
                double elevIJjpost = elevation.getSampleDouble(x, y + 1, 0);
                if (isNovalue(elevIJ) || isNovalue(elevIJipre) || isNovalue(elevIJipost) || isNovalue(elevIJjpre)
                        || isNovalue(elevIJjpost)) {
                    gradientRaster.setSample(x, y, 0, DEFAULT_NO_VALUE);
                } else if (!isNovalue(elevIJ) && !isNovalue(elevIJipre) && !isNovalue(elevIJipost) && !isNovalue(elevIJjpre)
                        && !isNovalue(elevIJjpost)) {
                    double xGrad = 0.5 * (elevIJipost - elevIJipre) / xRes;
                    double yGrad = 0.5 * (elevIJjpre - elevIJjpost) / yRes;
                    double grad = Math.sqrt(Math.pow(xGrad, 2) + Math.pow(yGrad, 2));
                    gradientRaster.setSample(x, y, 0, grad);
                } else {
                    err.println(Messages.getString("h_gradient.error")); //$NON-NLS-1$
                }
            }
            pm.worked(1);
        }
        pm.done();

    }
}
