package eu.hydrologis.jgrass.operations.jai.slope;
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
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsConstants;

/**
 * This class calculate the slope in the drainage direction.
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

public class HMSlopeOpImage extends JGrassAreaOpImage {
    /**
     * Initialise the class.
     * <p>
     * Call the super constructor.
     * </p>
     */
    private static final double DEFAULT_NO_VALUE = JGrassConstants.doubleNovalue;
    private final static int[][] DIR = ModelsConstants.DIR;

    private final RenderedImage elevation;
    public HMSlopeOpImage( RenderedImage source1, RenderedImage source2, BorderExtender extender, Map config, ImageLayout layout, double dx, double dy, boolean doTile, PrintStream err, PrintStream out ) {

        super(source1, layout, config, doTile, extender, 1, 1, 1, 1, dx, dy, err, out);
        this.elevation = source2;
        // This operation doesn't have a border extender so the boundary is
        // equal to 0.
        // To calculate the maximum slope direction are necessary the eight
        // point around the pixel, so the last four parameters indicate this.

    }

    /*
     * Calculate the slope and set it as a output
     */
    @Override
    protected void computeRect( PlanarImage[] sources, WritableRaster dest, Rectangle destRect ) {
        Rectangle region = new Rectangle(destRect.x + 1, destRect.y + 1, destRect.width - 2, destRect.height - 2);
        slope(sources[0].getData(), dest, region);
    }

    @Override
    protected void computeRect( Raster[] sources, WritableRaster dest, Rectangle destRect ) {
        slope(sources[0], dest, destRect);
    }

    /*
     * Evaluate the derivative with the Evans scheme
     */

    private void slope( Raster source, WritableRaster slopeData, Rectangle destRect ) {

        // the origin of the Rectangle where there is possible calculate the
        // slope.
        int xOrigin = destRect.x;
        int yOrigin = destRect.y;
        // Cycling into the valid region.

        Raster pitData = source;
        Raster dirData = elevation.getData(destRect);

        int[] point = new int[2];

        // grid contains the dimension of pixels according with flow directions
        double[] grid = new double[11];

        grid[0] = grid[9] = grid[10] = 0;
        grid[1] = grid[5] = Math.abs(xRes);
        grid[3] = grid[7] = Math.abs(yRes);
        grid[2] = grid[4] = grid[6] = grid[8] = Math.sqrt(xRes * xRes + yRes * yRes);
        // out.println(Messages.getString("working") + " h.slope");
        // Calculates the slope along the flow directions of elevation field, if
        // a pixel is on the border its value will be equal to novalue
        double temp;

        int maxNRows = xOrigin + destRect.width;
        int maxNCols = yOrigin + destRect.height;

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.slope...", maxNRows - xOrigin);
        for( int i = xOrigin; i < maxNRows; i++ ) {
            for( int j = yOrigin; j < maxNCols; j++ ) {
                temp = DEFAULT_NO_VALUE;
                int flowDir = (int) dirData.getSampleDouble(i, j, 0);
                if (flowDir == 10) {
                    err.println("Found a pixel of value 10, which usually defines the outlet. Try with a flow map without outlets.");
                }
                if (!isNovalue(flowDir)) {
                    point[0] = i + DIR[flowDir][1];
                    point[1] = j + DIR[flowDir][0];
                    temp = (pitData.getSampleDouble(i, j, 0) - pitData.getSampleDouble(point[0], point[1], 0)) / grid[flowDir];
                }
                slopeData.setSample(i, j, 0, temp);
            }
            pm.worked(1);
        }
        pm.done();

    }

}
