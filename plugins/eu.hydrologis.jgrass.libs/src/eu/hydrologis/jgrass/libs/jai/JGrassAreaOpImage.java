package eu.hydrologis.jgrass.libs.jai;

import java.awt.Point;
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

import eu.hydrologis.jgrass.libs.messages.Messages;
/**
 * An useful extension of AreaOpImage which adpat the Image to the geographical map.
 * <p>
 * This class is used in the Horton Machine where the models have, in order to compute a pixel, need
 * to the neighbours pixel. This class add some fields to the AreaOpImage class in order to use it
 * wiyh a geographical data in some Horton Machine models. To achieve this result I have added the
 * x and y resolution and the output and error stream.
 * </p>
 * <p>
 * I have also modified (overriden) some methods to handled the tile.
 * </p>
 * 
 * @author <a href="daniele.andreis@gmail.com">daniele andreis</a>
 * @since 1.1.0
 */

public class JGrassAreaOpImage extends AreaOpImage {
    /**
     * the x resolution.
     */
    protected double xRes = -1d;
    /**
     * the y resolution.
     */
    protected double yRes = -1d;
    /**
     * the error stream.
     */
    protected PrintStream err = null;
    /**
     * the output stream.
     */
    protected PrintStream out = null;
    /**
     *  the tiles whch is computing.
     */
    protected int currentTile = 1;
    /**
     *  the number of tile to be computed.
     */
    protected int numTile = 0;

    public JGrassAreaOpImage( RenderedImage source, ImageLayout layout, Map configuration,
            boolean cobbleSources, BorderExtender extender, int leftPadding, int rightPadding,
            int topPadding, int bottomPadding, double dx, double dy, PrintStream err,
            PrintStream out ) {
        super(source, layout, configuration, cobbleSources, extender, leftPadding, rightPadding,
                topPadding, bottomPadding);
        this.xRes = dx;
        this.yRes = dy;
        this.err = err;
        this.out = out;
        if (cobbleSources) {
            // if the cobbleSource is true (doTile=true) then print some information about the tiles
            out.println("tile size = " + this.tileHeight); //$NON-NLS-1$
            out.println("number of tile = " + numTile); //$NON-NLS-1$
            out.println("\n");

        }

    }

    @Override
    /*
     * I have override this method because I have some problems with the super method so I have
     * adapted the super method (I have deleted the last controls).
     */
    public Raster computeTile( int tileX, int tileY ) {
        // return super.computeTile(tileX, tileY);
        if (!cobbleSources) {
            return super.computeTile(tileX, tileY);
        }

        out.println(Messages.getString("working") + " " + currentTile + " of " + numTile
                + " tiles.");
        out.println(Messages.getString("readtile") + " " + tileX + " " + tileY);

        /* Create a new WritableRaster to represent this tile. */
        Point org = new Point(tileXToX(tileX), tileYToY(tileY));
        WritableRaster dest = createWritableRaster(sampleModel, org);

        /* Clip output rectangle to image bounds. */
        Rectangle rect = new Rectangle(org.x, org.y, sampleModel.getWidth(), sampleModel
                .getHeight());

        int d_x0 = getMinX() + leftPadding;
        int d_y0 = getMinY() + topPadding;

        int d_w = getWidth() - leftPadding - rightPadding;
        d_w = Math.max(d_w, 0);

        int d_h = getHeight() - topPadding - bottomPadding;
        d_h = Math.max(d_h, 0);

        Rectangle theDest = new Rectangle(d_x0, d_y0, d_w, d_h);
        Rectangle destRect = rect.intersection(theDest);
        if ((destRect.width <= 0) || (destRect.height <= 0)) {
            return dest;
        }

        /* account for padding in srcRectangle */
        PlanarImage s = getSource(0);

        destRect = destRect.intersection(s.getBounds());
        Rectangle srcRect = new Rectangle(destRect);
        srcRect.x -= getLeftPadding();
        srcRect.width += getLeftPadding() + getRightPadding();
        srcRect.y -= getTopPadding();
        srcRect.height += getTopPadding() + getBottomPadding();

        Raster[] sources = new Raster[1];

        // Fetch the padded src rectangle
        sources[0] = (extender != null) ? s.getExtendedData(srcRect, extender) : s.getData(srcRect);

        // Make a destRectangle
        computeRect(sources, dest, destRect);

        // Recycle the source tile
        if (s.overlapsMultipleTiles(srcRect)) {
            recycleTile(sources[0]);
        }
        currentTile++;

        return dest;

    }

}
