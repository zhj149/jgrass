/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) { 
 * HydroloGIS - www.hydrologis.com                                                   
 * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam                               
 * The JGrass developer team - www.jgrass.org                                         
 * }
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.models.h.flow;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.WIND;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * Straight port of the flowdirections calculation model found in the TARDEM suite.
 * </p>
 * <p>
 * Translated to java and adapted to be opemi based.
 * </p>
 * 
 * @author David Tarboton - http://www.neng.usu.edu/cee/faculty/dtarb/tardem.html#programs
 * @author Andrea Antonello - www.hydrologis.com
 * @author Erica Ghesla
 */
public class h_flow extends ModelsBackbone {

    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit"; //$NON-NLS-1$

    public final static String flowID = "flow"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_flow.usage"); //$NON-NLS-1$

    private ILink pitLink = null;

    private ILink flowLink = null;

    private IOutputExchangeItem flowDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private double[][] elevations = null, transposedFlow = null;

    // the hydrologic variables
    private int i1, i2, n1, n2, nx, ny;

    private int[] d1, d2, is, js, dn;

    private int[][] dir;

    private int ccheck, useww;

    private int[][] arr;

    private double[][] areaw, weight;

    private final double ndv = FluidConstants.flownovalue;

    private double dx, dy;

    private CoordinateReferenceSystem crs;

    public h_flow() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_flow( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }

    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return pitDataInputEI;
        } else {
            return null;
        }
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return flowDataOutputEI;
        } else {
            return null;
        }
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        String mapset = null;
        if (properties != null) {
            for( IArgument argument : properties ) {
                String key = argument.getKey();
                if (key.compareTo(ModelsConstants.GRASSDB) == 0) {
                    grassDb = argument.getValue();
                }
                if (key.compareTo(ModelsConstants.LOCATION) == 0) {
                    location = argument.getValue();
                }
                if (key.compareTo(ModelsConstants.MAPSET) == 0) {
                    mapset = argument.getValue();
                }
            }

        }

        /*
         * define the map path
         */
        String locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + WIND;
        activeRegion = new JGrassRegion(activeRegionPath);
        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
        componentDescr = "h.flow"; //$NON-NLS-1$
        componentId = null;

        /*
         * create the exchange items
         */
        // flow output
        IElementSet flowElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity flowQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        flowDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, flowQuantity, flowElementSet);
        // element set defining what we want to read
        // pit input
        IElementSet pitElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity pitQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.GRASSRASTERMAP,
                ModelsConstants.UNITID_RASTER);
        pitDataInputEI = UtilitiesFacade.createInputExchangeItem(this, pitQuantity, pitElementSet);
    }

    /**
     * 
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(flowLink.getID())) {
            out.println(Messages.getString("working") + " h.flow"); //$NON-NLS-1$ //$NON-NLS-2$

            int rows = activeRegion.getRows();
            int cols = activeRegion.getCols();

            GridCoverage2D pitGC = ModelsConstants.getGridCoverage2DFromLink(pitLink, time, err);
            if (pitGC != null) {
                RandomIter pitIter = RandomIterFactory.create(pitGC.getRenderedImage(), null);

                // for this function it if necessary to transpose the matrix
                elevations = new double[cols][rows];

                for( int i = 0; i < cols; i++ ) {
                    for( int j = 0; j < rows; j++ ) {
                        double pitValue = pitIter.getSampleDouble(i, j, 0);
                        if (!isNovalue(pitValue)) {
                            elevations[i][j] = pitValue;
                        } else {
                            elevations[i][j] = FluidConstants.flownovalue;
                        }
                    }
                }

                WritableRaster jgRasterData = setdird8s();
                JGrassGridCoverageValueSet jgrValueSet = new JGrassGridCoverageValueSet(jgRasterData, activeRegion, crs);
                return jgrValueSet;

            }

        }
        return null;
    }

    public void finish() {
        elevations = null;
        transposedFlow = null;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
    }

    /**
     * @return flow RasterData
     */
    private WritableRaster setdird8s() {

        /* define directions */
        d1 = new int[]{(int) FluidConstants.flownovalue, 0, -1, -1, -1, 0, 1, 1, 1};
        d2 = new int[]{(int) FluidConstants.flownovalue, 1, 1, 0, -1, -1, -1, 0, 1};

        // get resolution of the active region
        dx = activeRegion.getWEResolution();
        dy = activeRegion.getNSResolution();

        // get rows and cols from the active region
        ny = activeRegion.getRows();
        nx = activeRegion.getCols();

        i1 = 0;
        i2 = 0;
        n1 = nx;
        n2 = ny;

        setdfnoflood();

        // it is necessaty to transpose the dir matrix and than it's possible to
        // write the output
        transposedFlow = new double[dir[0].length][dir.length];
        for( int i = 0; i < dir[0].length; i++ ) {
            for( int j = 0; j < dir.length; j++ ) {
                if (dir[j][i] == 0) {
                    return null;
                }
                if (dir[j][i] != FluidConstants.flownovalue) {
                    transposedFlow[i][j] = dir[j][i];
                } else {
                    transposedFlow[i][j] = doubleNovalue;
                }
            }
        }
        return FluidUtils.createFromMatrix(transposedFlow);
    }

    /**
     * 
     */
    private void setdfnoflood() {
        int n;
        double[] fact = new double[9];

        dir = new int[elevations.length][elevations[0].length];

        out.println(Messages.getString("h_flow.initbound")); //$NON-NLS-1$
        /* Initialize boundaries */
        for( int i = i1; i < n1; i++ ) {
            dir[i][i2] = -1;
            dir[i][n2 - 1] = -1;
        }

        for( int i = i2; i < n2; i++ ) {
            dir[i1][i] = -1;
            dir[n1 - 1][i] = -1;
        }
        out.println(Messages.getString("h_flow.initpointers")); //$NON-NLS-1$
        /* initialize internal pointers */
        for( int i = (i2 + 1); i < (n2 - 1); i++ ) {
            for( int j = (i1 + 1); j < (n1 - 1); j++ ) {
                if (elevations[j][i] <= FluidConstants.flownovalue) {
                    dir[j][i] = -1;
                } else {
                    dir[j][i] = 0;
                }
            }
        }

        /* Direction factors */
        for( int k = 1; k <= 8; k++ ) {
            fact[k] = 1.0 / (Math.sqrt(d1[k] * dy * d1[k] * dy + d2[k] * d2[k] * dx * dx));
        }

        // Compute contrib area using overlayed directions for direction setting
        ccheck = 0; // dont worry about edge contamination
        useww = 0; // dont worry about weights

        arr = new int[n2][n1];
        for( int i = 0; i < arr.length; i++ ) {
            for( int j = 0; j < arr[0].length; j++ ) {
                arr[i][j] = 0;
            }
        }

        for( int i = (i2 + 1); i < (n2 - 1); i++ ) {
            for( int j = (i1 + 1); j < (n1 - 1); j++ ) {
                // This allows for a stream overlay
                if (dir[j][i] > 0)
                    darea(i, j);
            }
        }

        out.println(Messages.getString("h_flow.setpos")); //$NON-NLS-1$
        /* Set positive slope directions */
        n = 0;
        for( int i = (i2 + 1); i < (n2 - 1); i++ ) {
            for( int j = (i1 + 1); j < (n1 - 1); j++ ) {
                if (dir[j][i] == 0) {
                    if (elevations[j][i] > FluidConstants.flownovalue) {
                        set(i, j, fact);
                         if (dir[j][i] == 0) {
                             n++;
                         }
                    }
                }
            }
        }
        
        out.println(Messages.getString("h_flow.solveflats")); //$NON-NLS-1$
        /*
         * Now resolve flats following the Procedure of Garbrecht and Martz, Journal of Hydrology,
         * 1997.
         */

        /*
         * Memory is utilized as follows is, js, dn, s and elev2 are unidimensional arrays storing
         * information for flats. sloc is a indirect addressing array for accessing these - used
         * during recursive iteration spos is a grid of pointers for accessing these to facilitate
         * finding neighbors The routine flatrout is recursive and at each recursion allocates a new
         * sloc for addressing these arrays and a new elev for keeping track of the elevations for
         * that recursion level.
         */
        if (n > 0) {
            int iter = 1;
            int[][] spos = new int[nx][ny];
            dn = new int[n];
            is = new int[n];
            js = new int[n];
            int[] s = new int[n];
            int[] sloc = new int[n];
            double[] elev2 = new double[n];

            /* Put unresolved pixels on stack */
            int ip = 0;
            for( int i = i2; i < n2; i++ ) {
                for( int j = i1; j < n1; j++ ) {
                    spos[j][i] = -1; /* Initialize stack position */
                    if (dir[j][i] == 0) {
                        is[ip] = i;
                        js[ip] = j;
                        dn[ip] = 0;
                        sloc[ip] = ip;
                        /* Initialize the stage 1 array for flat routing */
                        s[ip] = 1;
                        spos[j][i] = ip; /* pointer for back tracking */
                        ip++;
                    }
                }
            }

            flatrout(n, sloc, s, spos, iter, elev2, elev2, fact, n);
            /* The direction 19 was used to flag pits. Set these to 0 */
            for( int i = i2; i < n2; i++ ) {
                for( int j = i1; j < n1; j++ ) {
                    if (dir[j][i] == 19)
                        dir[j][i] = 0;
                }
            }
        }
    }

    private void flatrout( int n, int[] sloc, int[] s, int[][] spos, int iter, double[] elev1, double[] elev2, double[] fact,
            int ns ) {
        int nu, ipp;
        int[] sloc2;
        double[] elev3;

        incfall(n, elev1, s, spos, iter, sloc);
        for( int ip = 0; ip < n; ip++ ) {
            elev2[sloc[ip]] = (s[sloc[ip]]);
            s[sloc[ip]] = 0; /* Initialize for pass 2 */
        }

        incrise(n, elev1, s, spos, iter, sloc);
        for( int ip = 0; ip < n; ip++ ) {
            elev2[sloc[ip]] += (s[sloc[ip]]);
        }

        nu = 0;
        for( int ip = 0; ip < n; ip++ ) {
            set2(is[sloc[ip]], js[sloc[ip]], fact, elev1, elev2, iter, spos, s);
            if (dir[js[sloc[ip]]][is[sloc[ip]]] == 0)
                nu++;
        }

        if (nu > 0) {
            /* Iterate Recursively */
            /*
             * Now resolve flats following the Procedure of Garbrecht and Martz, Journal of
             * Hydrology, 1997.
             */
            iter = iter + 1;
            // printf("Resolving %d Flats, Iteration: %d \n",nu,iter);
            sloc2 = new int[nu];
            elev3 = new double[ns];

            /* Initialize elev3 */
            for( int ip = 0; ip < ns; ip++ ) {
                elev3[ip] = 0.;
            }
            /* Put unresolved pixels on new stacks - keeping in same positions */
            ipp = 0;
            for( int ip = 0; ip < n; ip++ ) {
                if (dir[js[sloc[ip]]][is[sloc[ip]]] == 0) {
                    sloc2[ipp] = sloc[ip];
                    /* Initialize the stage 1 array for flat routing */
                    s[sloc[ip]] = 1;
                    ipp++;
                    // if(ipp > nu)printf("PROBLEM - Stack logic\n");
                } else {
                    s[sloc[ip]] = -1; /*
                                         * Used to designate out of remaining flat on higher
                                         * iterations
                                         */
                }
                dn[sloc[ip]] = 0; /* Reinitialize for next time round. */
            }
            flatrout(nu, sloc2, s, spos, iter, elev2, elev3, fact, ns);
        } /* end if nu > 0 */

    }

    /**
     * @param i
     * @param j
     * @param fact
     * @param elev1
     * @param elev2
     * @param iter
     * @param spos
     * @param s
     */
    private void set2( int i, int j, double[] fact, double[] elev1, double[] elev2, int iter, int[][] spos, int[] s ) {
        /*
         * This function sets directions based upon secondary elevations for assignment of flow
         * directions across flats according to Garbrecht and Martz scheme. There are two
         * possibilities: A. The neighbor is outside the flat set B. The neighbor is in the flat
         * set. In the case of A the elevation of the neighbor is set to 0 for the purposes of
         * computing slope. Since the incremental elevations are all positive there is always a
         * downwards slope to such neighbors, and if the previous elevation increment had 0 slope
         * then a flow direction can be assigned.
         */

        double slope, slope2, smax, ed;
        int spn, sp;
        int in, jn;
        smax = 0.;
        sp = spos[j][i];
        for( int k = 1; k <= 8; k++ ) {
            jn = j + d2[k];
            in = i + d1[k];
            spn = spos[jn][in];
            if (iter <= 1) {
                ed = elevations[j][i] - elevations[jn][in];
            } else {
                ed = elev1[sp] - elev1[spn];
            }
            slope = fact[k] * ed;
            if (spn < 0 || s[spn] < 0) {
                /* The neighbor is outside the flat set. */
                ed = 0.;
            } else {
                ed = elev2[spn];
            }
            slope2 = fact[k] * (elev2[sp] - ed);
            if (slope2 > smax && slope >= 0.) /*
                                                 * Only if latest iteration slope is positive and
                                                 * previous iteration slope flat
                                                 */
            {
                smax = slope2;
                dir[j][i] = k;
            }
        } /* End of for */

    }

    /**
     * @param n
     * @param elev1
     * @param s
     * @param spos
     * @param iter
     * @param sloc
     */
    private void incrise( int n, double[] elev1, int[] s2, int[][] spos, int iter, int[] sloc ) {
        /*
         * This routine implements stage 2 drainage away from higher ground dn is used to flag
         * pixels still being incremented
         */
        int done = 0, ninc, nincold, spn;
        double ed;
        int i, j, in, jn;
        nincold = 0;

        while( done < 1 ) {
            done = 1;
            ninc = 0;
            for( int ip = 0; ip < n; ip++ ) {
                for( int k = 1; k <= 8; k++ ) {
                    j = js[sloc[ip]];
                    i = is[sloc[ip]];
                    jn = j + d2[k];
                    in = i + d1[k];
                    spn = spos[jn][in];

                    if (iter <= 1) {
                        ed = elevations[j][i] - elevations[jn][in];
                    } else {
                        ed = elev1[sloc[ip]] - elev1[spn];
                    }
                    if (ed < 0.) {
                        dn[sloc[ip]] = 1;
                    }
                    if (spn >= 0) {
                        if (s2[spn] > 0) {
                            dn[sloc[ip]] = 1;
                        }
                    }
                }
            }
            for( int ip = 0; ip < n; ip++ ) {
                s2[sloc[ip]] = s2[sloc[ip]] + dn[sloc[ip]];
                ninc = ninc + dn[sloc[ip]];
                if (dn[sloc[ip]] == 0) {
                    done = 0; /*
                                 * if still some not being incremented continue looping
                                 */
                }

            }
            // printf("incrise %d %d\n",ninc,n);
            if (ninc == nincold) {
                done = 1;
            } /*
                 * If there are no new cells incremented stop - this is the case when a flat has no
                 * higher ground around it.
                 */
            nincold = ninc;
        }

    }

    /**
     * @param n
     * @param elev1
     * @param s
     * @param spos
     * @param iter
     * @param sloc
     */
    private void incfall( int n, double[] elev1, int[] s1, int[][] spos, int iter, int[] sloc ) {
        /* This routine implements drainage towards lower areas - stage 1 */
        int done = 0, donothing, ninc, nincold, spn;
        int st = 1, i, j, in, jn;
        double ed;
        nincold = -1;

        while( done < 1 ) {
            done = 1;
            ninc = 0;
            for( int ip = 0; ip < n; ip++ ) {
                /*
                 * if adjacent to same level or lower that drains or adjacent to pixel with s1 < st
                 * and dir not set do nothing
                 */
                donothing = 0;
                j = js[sloc[ip]];
                i = is[sloc[ip]];
                for( int k = 1; k <= 8; k++ ) {
                    jn = j + d2[k];
                    in = i + d1[k];
                    spn = spos[jn][in];
                    if (iter <= 1) {
                        ed = elevations[j][i] - elevations[jn][in];
                    } else {
                        ed = elev1[sloc[ip]] - elev1[spn];
                    }
                    if (ed >= 0. && dir[jn][in] != 0)
                        donothing = 1; /* If neighbor drains */
                    if (spn >= 0) /* if neighbor is in flat */
                    {
                        /* If neighbor is not being */
                        if (s1[spn] >= 0 && s1[spn] < st && dir[jn][in] == 0) {
                            donothing = 1; /* Incremented */
                        }
                    }
                }

                if (donothing == 0) {
                    s1[sloc[ip]]++;
                    ninc++;
                    done = 0;
                }
            } /* End of loop over all flats */
            st = st + 1;
            // printf("Incfall %d %d \n",ninc,n);
            if (ninc == nincold) {
                done = 1;
                // printf("There are pits remaining, direction will not be
                // set\n");
                /* Set the direction of these pits to 19 to flag them */
                for( int ip = 0; ip < n; ip++ ) /* loop 2 over all flats */
                {
                    /*
                     * if adjacent to same level or lower that drains or adjacent to pixel with s1 <
                     * st and dir not set do nothing
                     */
                    donothing = 0;
                    j = js[sloc[ip]];
                    i = is[sloc[ip]];
                    for( int k = 1; k <= 8; k++ ) {
                        jn = j + d2[k];
                        in = i + d1[k];
                        spn = spos[jn][in];
                        if (iter <= 1) {
                            ed = elevations[j][i] - elevations[jn][in];
                        } else {
                            ed = elev1[sloc[ip]] - elev1[spn];
                        }
                        if (ed >= 0. && dir[jn][in] != 0)
                            donothing = 1; /* If neighbor drains */
                        if (spn >= 0) /* if neighbor is in flat */
                        {
                            /* If neighbor is not being */
                            if (s1[spn] >= 0 && s1[spn] < st && dir[jn][in] == 0)
                                donothing = 1; /* Incremented */
                        }
                    }
                    if (donothing == 0) {
                        dir[j][i] = 19;
                        /* printf("%d %d\n",i,j); */
                    }
                } /* End of loop 2 over all flats */
            }
            nincold = ninc;
        } /* End of while done loop */

    }

    /**
     * @param i
     * @param j
     */
    private void darea( int i, int j ) {
        int in, jn, con = 0;
        /*
         * con is a flag that signifies possible contaminatin of area due to edge effects
         */
        if (i != 0 && i != ny - 1 && j != 0 && j != nx - 1 && dir[j][i] > -1)
        /* not on boundary */
        {
            if (arr[j][i] == 0) // not touched yet
            {
                arr[j][i] = 1;
                if (useww == 1)
                    areaw[j][i] = weight[j][i];
                for( int k = 1; k <= 8; k++ ) {
                    in = i + d1[k];
                    jn = j + d2[k];

                    /*
                     * test if neighbor drains towards cell excluding boundaryies
                     */
                    if (dir[jn][in] > 0 && (dir[jn][in] - k == 4 || dir[jn][in] - k == -4)) {
                        darea(in, jn);
                        if (arr[jn][in] < 0)
                            con = -1;
                        else
                            arr[j][i] = arr[j][i] + arr[jn][in];
                        if (useww == 1) {
                            if (areaw[jn][in] <= ndv || areaw[j][i] <= ndv) {
                                areaw[j][i] = ndv;
                            } else
                                areaw[j][i] = areaw[j][i] + areaw[jn][in];
                        }
                    }
                    if (dir[jn][in] < 0)
                        con = -1;
                }
                if (con == -1 && ccheck == 1) {
                    arr[j][i] = -1;
                    if (useww == 1)
                        areaw[j][i] = ndv;
                }
            }
        } else
            arr[j][i] = -1;
    }

    /**
     * @param i
     * @param j
     * @param fact
     */
    private void set( int i, int j, double[] fact ) {
        double slope, smax;
        int amax, in, jn, aneigh = -1;

        dir[j][i] = 0; /* This necessary for repeat passes after level raised */
        smax = 0.;
        amax = 0;

        for( int k = 1; k <= 8; k = k + 2 ) // examine adjacent cells first
        {
            in = i + d1[k];
            jn = j + d2[k];
            if (elevations[jn][in] <= FluidConstants.flownovalue) {
                continue;
            }

            if (dir[j][i] != -1) {
                slope = fact[k] * (elevations[j][i] - elevations[jn][in]);

                if (aneigh > amax && slope >= 0.) {
                    amax = aneigh;
                    if (Math.abs(dir[jn][in] - k) != 4)
                        dir[j][i] = k; // Dont set opposing pointers
                } else if (slope > smax && amax <= 0) {
                    smax = slope;
                    dir[j][i] = k;

                }
            }
        }

        for( int k = 2; k <= 8; k = k + 2 ) // examine diagonal cells
        {
            in = i + d1[k];
            jn = j + d2[k];
            /* if(elev[jn][in] <= mval) dir[j][i] = -1; */
            if (elevations[jn][in] <= FluidConstants.flownovalue) {
                continue;
            }
            if (dir[j][i] != -1) {
                slope = fact[k] * (elevations[j][i] - elevations[jn][in]);
                if (slope > smax && amax <= 0) // still need amax check to
                // prevent crossing
                {
                    smax = slope;
                    dir[j][i] = k;

                }
            }
        }

    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

}
