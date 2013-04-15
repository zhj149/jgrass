package eu.hydrologis.jgrass.operations.jai.pitfiller;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.PrintStream;

import javax.media.jai.AreaOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.*;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidConstants;

/**
 * The {@link PointOpImage point operation} that computes the pitfiller algorithm.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class HMPitfillerOperation extends PointOpImage {

    private double xRes = -1d;
    private double yRes = -1d;
    private final PrintStream out;
    private double[][] elevations = null, transposedElevations = null;

    /*
     * <b>the hydrologic variables:</b>drainageDirections is the drainage
     * directions, nis is the number of unresolved pixel in dir matrix (which
     * haven't a drainage direction). <p> istack and pstak are the dimension of
     * the temporary vectors which allow to resolve the undrainage pixel and
     * pool. ipool, jpool are used to memorise the index of the "pixel pool". is
     * and js are vector where the program memorise the index of the elevation
     * matrix whose point don't drain in any D8 cells. i1, i2, n1, n2, are the
     * minimum and maximum index for the activeRegion matrix (from 0 to nColumns
     * or nRows) dx,dy are the resolution, the distance between two pixels.
     */
    private int i1, i2, n1, n2, nx, ny, nis, istack, pstack, nf, pooln, npool;

    private int[] ipool, jpool, is, js, dn;

    private int[][] dir, apool, drainageDirections;

    private double et, emin;


    static final double FLOW_NO_VALUE = FluidConstants.flownovalue;

    public HMPitfillerOperation( RenderedImage source, ImageLayout imageLayout, double xRes,
            double yRes, PrintStream out ) {
        super(source, imageLayout, null, false);
        this.xRes = xRes;
        this.yRes = yRes;
        this.out = out;
    }

    protected void computeRect( PlanarImage[] sources, WritableRaster pitfillerRaster,
            Rectangle destRect ) {
        PlanarImage elevation = sources[0];
        dummyPitfiller(elevation.getData(), pitfillerRaster, destRect);

    }

    protected void computeRect( Raster[] sources, WritableRaster pitfillerRaster, Rectangle destRect ) {
        Raster elevation = sources[0];
        dummyPitfiller(elevation, pitfillerRaster, destRect);
    }

    public Raster computeTile( int tileX, int tileY ) {
        System.out.println(tileX + " " + tileY);
        return super.computeTile(tileX, tileY);
    }

    private void dummyPitfiller( Raster planarImage, WritableRaster pitfillerRaster,
            Rectangle destRect ) {
        int nCols = destRect.width;
        int nRows = destRect.height;

        out.println("Working on pitfiller...");
        double value = 0.0;
        RectIter rectIter = RectIterFactory.create(planarImage, destRect);
        elevations = new double[nRows][nCols];
        for( int i = 0; i < nRows; i++ ) {
            System.out.println("READ: " + i);
            for( int j = 0; j < nCols; j++ ) {
                value = rectIter.getSampleDouble();
                if (!isNovalue(value)) {
                    elevations[i][j] = value;
                } else {
                    elevations[i][j] = FLOW_NO_VALUE;
                }
                rectIter.nextPixel();
            }
        }

        for( int i = 0; i < elevations.length; i++ ) {
            System.out.println("WRITE: " + i);
            for( int j = 0; j < elevations[0].length; j++ ) {
                pitfillerRaster.setSample(j, i, 0, elevations[i][j]);
            }
        }
        System.out.println();
    }

    /**
     * The pitfiller algorithm.
     * 
     * @param gradientRaster
     *            the {@link WritableRaster output raster} to which the gradient
     *            values are written.
     * @param destRect
     *            the area on which the algorithm will write.
     */
    private void pitfiller( Raster planarImage, WritableRaster pitfillerRaster, Rectangle destRect ) {
        int nCols = destRect.width;
        int nRows = destRect.height;
        if (planarImage != null) {
            double value = 0.0;
            RectIter rectIter = RectIterFactory.create(planarImage, destRect);
            elevations = new double[nRows][nCols];
            for( int j = 0; j < nCols; j++ ) {
                for( int i = 0; i < nRows; i++ ) {
                    value = rectIter.getSampleDouble();
                    System.out.print(value + " ");
                    if (!isNovalue(value)) {
                        elevations[i][j] = value;
                    } else {
                        elevations[i][j] = FLOW_NO_VALUE;
                    }
                    rectIter.nextPixel();
                }
                System.out.println();

            }

            if (!flood())
                return;

            // it is necessary to transpose the dir matrix and than it's
            // possible to
            // write the output
            transposedElevations = new double[nRows][nCols];
            for( int i = 0; i < nRows; i++ ) {
                for( int j = 0; j < nCols; j++ ) {
                    if (dir[j][i] == 0) {
                        return;
                    }
                    if (elevations[j][i] != FLOW_NO_VALUE) {
                        transposedElevations[i][j] = elevations[j][i];
                    } else {
                        isNovalue(transposedElevations[i][j]);
                    }
                }
            }

            rectIter.startPixels();
            for( int i = 0; i < nCols; i++ ) {
                for( int j = 0; j < nRows; j++ ) {
                    value = transposedElevations[i][j];
                    pitfillerRaster.setSample(j, i, 0, value);

                }
            }
        }

    }

    /**
     * Takes the elevation matrix and calculate a matrix with pits filled, using
     * the flooding algorithm.
     * 
     * @return a boolean value
     * 
     * @throws Exception
     */
    private boolean flood() {

        /* define directions */
        drainageDirections = ModelsConstants.DIR;

        // get resolution of the active region

        // get rows and cols from the active region

        // Initialise the vector to a supposed dimension, if the number of
        // unresolved pixel overload the vector there are a method which resized
        // the vectors.
        istack = (int) (nx * ny * 0.1);
        pstack = istack;
        dn = new int[istack];
        is = new int[istack];
        js = new int[istack];
        ipool = new int[pstack];
        jpool = new int[pstack];
        i1 = 0;
        i2 = 0;
        n1 = nx;
        n2 = ny;

        try {
            setdf(drainageDirections);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Initialise the dir matrix and then call the set method to obtain a D8
     * matrix
     * 
     * <p>
     * It's possible summarise the logical pitfiller stream like:
     * <ol>
     * <li>Initialise DIR to 0 (no flood) if the elevation value is valid and
     * isn't apixel on the edge</li>
     * <li>If the pixel have as DIR[j][i] a valid value then check if the
     * adjacent pixels are valid values:
     * <dl>
     * <dt>Yes, there are only valid value
     * <dd>then set the DIR value to the drainage direction (if slope is greater
     * than 0) or to 0 if there aren't.
     * <dt>No, there are an invalid value
     * <dd>set DIR to -1 (impossible value)
     * </dl>
     * </li>
     * <li>If DIR=0 then keep in temporary vector the index of this pixel.</li>
     * <li>Call <b>vdn</b> method, which assign the drainage value if the slope
     * is greater or equal to 0. Notice that neighbour pixel are valid pixel if
     * DIR=0. And then calculate the minimum elevation point (where there are a
     * pool)</li>
     * <li>Start a conditioning cycle (while there are a cell which have as a
     * drainage direction 0 (is a pool);<ol type=a>
     * <li>Find if there are a pool and check the pixels which belong to</li>
     *<li>Check the lowest point of the edge</li>
     *<li>Set the pixels pool elevation to the lowest point of the edge</li>
     *<li>Call <b>vdn</b> to recalculate unresolved pixels
     * <li>
     *<li>return to the begin</li>
     * </ol>
     * 
     * 
     * </li>
     * 
     * </ol>
     * 
     * 
     * @param d1
     *            the vector which contains all the possible first components
     *            drainage direction.
     * 
     * @param d2
     *            the vector which contains all the possible second components
     *            drainage direction.
     * 
     * @throws Exception
     */
    private void setdf( int[][] drainageDirections ) throws Exception {

        int nflat;
        int ni;
        int n;
        int ip;
        int imin;
        int jn;
        int in;
        int np1;
        int nt;
        float per = 1;
        // direction factor, where the components are 1/length
        double[] fact = calculateDirectionFactor(xRes, yRes);

        dir = new int[elevations.length][elevations[0].length];
        apool = new int[elevations.length][elevations[0].length];

        out.println("h_flow.initbound");

        /* Initialize boundaries */
        for( int i = i1; i < n1; i++ ) {
            dir[i][i2] = -1;
            dir[i][n2 - 1] = -1;
        }
        for( int i = i2; i < n2; i++ ) {
            dir[i1][i] = -1;
            dir[n1 - 1][i] = -1;
        }
        out.println("h_flow.initpointers");

        /*
         * Initialise internal pointers, if the point is an invalid value then
         * set the dir value to -1 else to 0
         */
        for( int i = (i2 + 1); i < (n2 - 1); i++ ) {
            for( int j = (i1 + 1); j < (n1 - 1); j++ ) {
                if (isNovalue(elevations[j][i])) {
                    dir[j][i] = -1;
                } else {
                    dir[j][i] = 0;
                }
            }
        }

        out.println("h_flow.setpos");

        /* Set positive slope directions - store unresolved on stack */
        nis = 0;
        for( int i = (i2 + 1); i < (n2 - 1); i++ ) {
            for( int j = (i1 + 1); j < (n1 - 1); j++ ) {
                if (!isNovalue(elevations[j][i])) {
                    // set the value in the dir matrix (D8 matrix)
                    set(i, j, dir, elevations, fact);
                }
                /*
                 * Put unresolved pixels (which have, in the dir matrix, 0 as
                 * value) on stack addstack method increased nis by one unit
                 */
                if (dir[j][i] == 0) {
                    addstack(i, j);
                }
            }
        }

        nflat = nis;
        /* routine to drain flats to neighbors */
        imin = vdn(nflat);
        n = nis;
        out.println("h_pitfiller.numpit" + n);
        np1 = n;
        nt = (int) (np1 * 1 - per / 100);

        /* initialize apool to zero */
        for( int i = i2; i < n2; i++ ) {
            for( int j = i1; j < n1; j++ ) {
                apool[j][i] = 0;
            }
        }

        out.println("h_pitfiller.main");
        out.println("h_pitfiller.perc");
        out.println("0%");
        /* store unresolved stack location in apool for easy deletion */
        int i = 0, j = 0;
        while( nis > 0 ) {
            // set the index to the lowest point in the map, during the
            // iteration, which filled the elevation map, the lovest point will
            // changed
            i = is[imin];
            j = js[imin];
            pooln = 1;
            npool = 0;
            nf = 0;/* reset flag to that new min elev is found */
            // calculate recursively the pool
            pool(i, j); /*
                                                                                                                                                  * Recursive call on unresolved point with lowest
                                                                                                                                                  * elevation
                                                                                                                                                  */

            /*
             * Find the pour point of the pool: the lowest point on the edge of
             * the pool
             */
            for( ip = 1; ip <= npool; ip++ ) {
                i = ipool[ip];
                j = jpool[ip];
                for( int k = 1; k <= 8; k++ ) {
                    jn = j + drainageDirections[k][1];
                    in = i + drainageDirections[k][0];
                    // if the point isn't in this pool but on the edge then
                    // check the minimun elevation edge
                    if (apool[jn][in] != pooln) {
                        et = max2(elevations[j][i], elevations[jn][in]);
                        if (nf == 0) {
                            emin = et;
                            nf = 1;
                        } else {
                            if (emin > et) {
                                emin = et;
                            }
                        }
                    }
                }
            }

            /* Fill the pool */
            for( int k = 1; k <= npool; k++ ) {
                i = ipool[k];
                j = jpool[k];
                if (elevations[j][i] <= emin) {
                    if (dir[j][i] > 0) { /* Can be in pool, but not flat */
                        dir[j][i] = 0;
                        addstack(i, j);
                    }
                    for( ip = 1; ip <= 8; ip++ ) {
                        jn = j + drainageDirections[ip][1];
                        in = i + drainageDirections[ip][0];
                        if ((elevations[jn][in] > elevations[j][i]) && (dir[jn][in] > 0)) {
                            /*
                             * Only zero direction of neighbors that are higher
                             * - because lower or equal may be a pour point in a
                             * pit that must not be disrupted
                             */
                            dir[jn][in] = 0;
                            addstack(in, jn);
                        }
                    }
                    elevations[j][i] = emin;
                }
                apool[j][i] = 0;
            }

            /* reset unresolved stack */
            ni = 0;
            for( ip = 1; ip <= nis; ip++ ) {
                set(is[ip], js[ip], dir, elevations, fact);

                if (dir[js[ip]][is[ip]] == 0) {
                    ni++;
                    is[ni] = is[ip];
                    js[ni] = js[ip];
                }
            }

            n = nis;
            imin = vdn(ni);

            if (nis < nt) {
                if (per % 10 == 0)
                    out.println((int) per + "%");
                per = per + 1;
                nt = (int) (np1 * (1 - per / 100));
            }
        }
        out.println("Pitfiller finished...");
    }
    /*
     * Routine to add entry to is, js stack, enlarging if necessary
     * 
     * @param i
     * 
     * @param j
     */
    private void addstack( int i, int j ) {
        /* Routine to add entry to is, js stack, enlarging if necessary */
        nis = nis + 1;
        if (nis >= istack) {
            /* Try enlarging */
            istack = (int) (istack + nx * ny * .1);

            is = realloc(is, istack);
            js = realloc(js, istack);
            dn = realloc(dn, istack);

        }

        is[nis] = i;
        js[nis] = j;
        // out.println(" i = " + i + "nis = " + nis);
    }

    /*
     * @param is2
     * 
     * @param istack2
     * 
     * @return
     */
    private int[] realloc( int[] is2, int istack2 ) {

        int[] resized = new int[istack2];
        for( int i = 0; i < is2.length; i++ ) {
            resized[i] = is2[i];
        }
        is2 = null;

        return resized;
    }

    /*
     * Try to find a drainage direction for undefinite cell.<p> If the drainage
     * direction is found then put it in dir else kept its index in is and js.
     * N.B. in the set method the drainage directions is setted only if the
     * slope between two pixel is positive. At this step the dir value is setted
     * also the slope is equal to zero.</p>
     * 
     * @param n the number of indefinite cell in the dir matrix
     * 
     * @return imin or the number of unresolved pixel after have run the method
     */
    private int vdn( int n ) {
        int imin;
        double ed;
        nis = n;

        do {
            n = nis;
            nis = 0;
            for( int ip = 1; ip <= n; ip++ ) {
                dn[ip] = 0;
            }

            for( int k = 1; k <= 8; k++ ) {
                for( int ip = 1; ip <= n; ip++ ) {
                    ed = elevations[js[ip]][is[ip]]
                            - elevations[js[ip] + drainageDirections[k][1]][is[ip]
                                    + drainageDirections[k][0]];
                    if ((ed >= 0.)
                            && ((dir[js[ip] + drainageDirections[k][1]][is[ip]
                                    + drainageDirections[k][0]] != 0) && (dn[ip] == 0)))
                        dn[ip] = k;
                }
            }

            imin = 1; /* location of point on stack with lowest elevation */
            for( int ip = 1; ip <= n; ip++ ) {
                if (dn[ip] > 0) {
                    dir[js[ip]][is[ip]] = dn[ip];
                } else {
                    nis++;
                    is[nis] = is[ip];
                    js[nis] = js[ip];
                    if (elevations[js[nis]][is[nis]] < elevations[js[imin]][is[imin]])
                        imin = nis;
                }
            }
            // out.println("vdn n = " + n + "nis = " + nis);
        } while( nis < n );

        return imin;
    }

    /*
     * function to compute pool recursively and at the same time determine the
     * minimum elevation of the edge.
     */
    private void pool( int i, int j ) {
        int in;
        int jn;
        if (apool[j][i] <= 0) { /* not already part of a pool */
            if (dir[j][i] != -1) {/* check only dir since dir was initialized */
                /* not on boundary */
                apool[j][i] = pooln;/* apool assigned pool number */
                npool = npool + 1;// the number of pixel in the pool
                if (npool >= pstack) {
                    if (pstack < nx * ny) {
                        pstack = (int) (pstack + nx * ny * .1);
                        if (pstack > nx * ny) {
                            /* Pool stack too large */
                        }

                        ipool = realloc(ipool, pstack);
                        jpool = realloc(jpool, pstack);
                    }

                }

                ipool[npool] = i;
                jpool[npool] = j;

                for( int k = 1; k <= 8; k++ ) {
                    in = i + drainageDirections[k][0];
                    jn = j + drainageDirections[k][1];
                    /* test if neighbor drains towards cell excluding boundaries */
                    if (((dir[jn][in] > 0) && ((dir[jn][in] - k == 4) || (dir[jn][in] - k == -4)))
                            || ((dir[jn][in] == 0) && (elevations[jn][in] >= elevations[j][i]))) {
                        /* so that adjacent flats get included */
                        pool(in, jn);
                    }
                }

            }
        }

    }

    private double max2( double e1, double e2 ) {
        double em;
        em = e1;
        if (e2 > em)
            em = e2;
        return em;
    }

    /**
     * Calculate the drainage direction with D8 method.
     * 
     *Find the direction which have the maximum slope and set it as the
     *drainage directionthe in the cell (i,j) in dir matrix. Is used in some
     *horton like pitfiller, floe,...
     * 
     * @param i
     *        <b>j</b> are the position index of the cell in the matrix.
     * @param dir
     *        is the drainage direction matrix, a cell contains an int value in
     *        the range 0 to 8 (or 10 if it is an outlet point).
     *@param elevation
     *        is the DEM.
     *@param fact
     *        is the direction factor (1/lenght).
     *       
     */
    public void set( int i, int j, int[][] dir, double[][] elevations, double[] fact ) {
        double slope = 0;
        double smax;
        int in;
        int jn;
        int[][] DIR = ModelsConstants.DIR;
        dir[j][i] = 0; /* This necessary for repeat passes after level raised */
        smax = 0.0;

        for( int k = 1; k <= 8; k++ ) // examine adjacent cells first
        {
            in = i + DIR[k][0];
            jn = j + DIR[k][1];
            if (isNovalue(elevations[jn][in])) {
                dir[j][i] = -1;
                break;
            }

            if (dir[j][i] != -1) {
                slope = fact[k] * (elevations[j][i] - elevations[jn][in]);

                if (slope > smax) {
                    smax = slope;
                    dir[j][i] = k;
                }
            }
        }
    }

    /**
     * Calculate the drainage direction factor (is used in some horton machine
     * like pitfiller, flow,...)
     * 
     * @param dx
     *        is the resolution of a raster map in the x direction.
     * @param dy
     *        is the resolution of the raster map in the y direction.
     * @return <b>fact</b> the direction factor or 1/lenght where lenght is the
     *         distance of the pixel from the central poxel.
     */
    public double[] calculateDirectionFactor( double dx, double dy ) {
        // direction factor, where the components are 1/length
        int[][] DIR = ModelsConstants.DIR;
        double[] fact = new double[9];
        for( int k = 1; k <= 8; k++ ) {
            fact[k] = 1.0 / (Math.sqrt(DIR[k][0] * dy * DIR[k][0] * dy + DIR[k][1] * DIR[k][1] * dx
                    * dx));
        }
        return fact;
    }

}
