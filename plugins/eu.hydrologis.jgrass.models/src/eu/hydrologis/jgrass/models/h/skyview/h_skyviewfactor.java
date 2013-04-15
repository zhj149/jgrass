/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
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

package eu.hydrologis.jgrass.models.h.skyview;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import jaitools.tiledimage.DiskMemImage;

import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.sun.media.jai.codecimpl.util.RasterFactory;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * This is a class which calculate the skyview factor.
 * <p>
 * To calculate the skyview factor it was followed the Corripio's article: Corripio, J. G.: 2003,
 * Vectorial algebra algorithms for calculating terrain parameters from DEMs and the position of the
 * sun for solar radiation modelling in mountainous terrain, International Journal of Geographical
 * Information Science 17(1), 1–23.
 * </p>
 * 
 * @author <a href="mailto:daniele.andreis@>Daniele Andreis</a>, Riccardo Rigon,
 */
public class h_skyviewfactor extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String elevationID = "elevation";

    public final static String skyviewID = "sky";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_skyviewfactor.usage");

    private static final double NaN = JGrassConstants.doubleNovalue;

    private ILink pitLink = null;

    private ILink skyviewLink = null;

    private IOutputExchangeItem skyviewDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    private boolean doTile;

    private double maxslope = 0;

    private WritableRaster pitImage;

    private double res;

    private WritableRandomIter normalRandomIter;

    private final static double PI = Math.PI;

    public h_skyviewfactor() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);

    }

    public h_skyviewfactor( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_topindex

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(elevationID)) {
            pitLink = link;
        }
        if (id.equals(skyviewID)) {
            skyviewLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: tca, slope
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return pitDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 1;
    }

    /**
     * return a description for the model, in this case return the string to use to execute the
     * command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: topindex
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return skyviewDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(skyviewLink.getID())) {
            // reads input maps
            if (jgrValueSet != null) {
                return jgrValueSet;
            }
            IValueSet tcaValueSet = pitLink.getSourceComponent().getValues(time, pitLink.getID());
            GridCoverage2D pitGC = null;
            if (tcaValueSet != null) {
                pitGC = ((JGrassGridCoverageValueSet) tcaValueSet).getGridCoverage2D();
            } else {
                String error = Messages.getString("erroreading"); //$NON-NLS-1$
                err.println(error);
                throw new IOException(error);
            }
            GridCoverage2D view = pitGC.view(ViewType.GEOPHYSICS);
            PlanarImage pitImageTmp = (PlanarImage) view.getRenderedImage();
            pitImage = FluidUtils.createFromRenderedImage(pitImageTmp);
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            RandomIter pitImageTmpRI = RandomIterFactory.create(pitImageTmp, null);

            int height = pitImageTmp.getHeight();
            int width = pitImageTmp.getWidth();
            for( int y = 0; y < height; y++ ) {
                for( int x = 0; x < width; x++ ) {
                    if (isNovalue(pitImageTmpRI.getSampleDouble(x, y, 0))) {
                        pitImage.setSample(x, y, 0, -9999.0);
                    }
                }
            }
            pitImageTmp = null;
            WritableRaster skyImage = skyviewfactor(pitImage);
            WritableRandomIter skyRI = RandomIterFactory.createWritable(skyImage, null);

            for( int y = 2; y < height - 2; y++ ) {
                for( int x = 2; x < width - 2; x++ ) {
                    if (pitImage.getSampleDouble(x, y, 0) == -9999.0) {
                        skyRI.setSample(x, y, 0, NaN);
                    }
                }
            }
            for( int y = 0; y < height; y++ ) {
                skyRI.setSample(0, y, 0, NaN);
                skyRI.setSample(1, y, 0, NaN);
                skyRI.setSample(width - 2, y, 0, NaN);
                skyRI.setSample(width - 1, y, 0, NaN);
            }

            for( int x = 2; x < width - 2; x++ ) {
                skyRI.setSample(x, 0, 0, NaN);
                skyRI.setSample(x, 1, 0, NaN);
                skyRI.setSample(x, height - 2, 0, NaN);
                skyRI.setSample(x, height - 1, 0, NaN);
            }
            skyRI.done();
            // the model
            if (skyImage == null) {
                return null;
            } else {

                out.println(Messages.getString("writemap") + " Skyview factor");
                jgrValueSet = new JGrassGridCoverageValueSet(skyImage, activeRegion, crs);
                return jgrValueSet;
            }
        }
        return null;
    }

    /**
     * in this method map's properties are defined... location, mapset... and than
     * IInputExchangeItem and IOutputExchangeItem are created.
     */
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
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.getBoolean(argument.getValue());
                }
            }
        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;

        String activeRegionPath = locationPath + File.separator + mapset + File.separator + "WIND";
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.skyview";
        componentId = null;

        /*
         * create the exchange items
         */
        // skyview output.

        skyviewDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // elevation input.

        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }

        if (linkID.equals(skyviewLink.getID())) {
            skyviewLink = null;
        }
    }

    /**
     * Calculate the sky view factor.
     * 
     * @param pitImage the dem ( the map of elevation).
     * @return the map of sky view factor.
     */
    private WritableRaster skyviewfactor( WritableRaster pitImage ) {
        // get rows and cols from the active region
        double resX = activeRegion.getWEResolution();
        double resY = activeRegion.getNSResolution();
        if (resX != resY) {
            return null;
        }
        res = resX;
        /*
         * evalutating the normal vector (in the center of the square compound of 4 pixel.
         */
        WritableRaster normalVectorImage = createNormalizedNormal(pitImage, res);

        WritableRaster skyviewFactorImage = FluidUtils.createDoubleWritableRaster(pitImage
                .getWidth(), pitImage.getHeight(), null, null, 0.0);
        WritableRandomIter skvFactorRandomIter = RandomIterFactory.createWritable(
                skyviewFactorImage, null);

        for( int i = 0; i < 360 - 10; i = i + 10 ) {
            double azimuth = toRadians(i * 1.0);
            WritableRaster skyviewImage = FluidUtils.createDoubleWritableRaster(
                    pitImage.getWidth(), pitImage.getHeight(), null, null, toRadians(maxslope));
            WritableRandomIter skvRandomIter = RandomIterFactory.createWritable(skyviewImage, null);
            for( int j = (int) maxslope; j >= 0; j-- ) {
                double elevation = toRadians(j * 1.0);
                calculateFactor(azimuth, elevation, skyviewImage);

            }
            for( int t = normalVectorImage.getMinY(); t < normalVectorImage.getMinY()
                    + normalVectorImage.getHeight(); t++ ) {
                for( int k = normalVectorImage.getMinX(); k < normalVectorImage.getMinX()
                        + normalVectorImage.getWidth(); k++ ) {
                    double tmp = skvRandomIter.getSampleDouble(k, t, 0);
                    skvRandomIter.setSample(k, t, 0, Math.cos(tmp) * Math.cos(tmp) * 10.0 / 360.0);
                }
            }

            for( int q = 0; q < skyviewFactorImage.getWidth(); q++ ) {
                for( int k = 0; k < skyviewFactorImage.getHeight(); k++ ) {
                    double tmp = skvFactorRandomIter.getSampleDouble(q, k, 0);
                    skvFactorRandomIter.setSample(q, k, 0, tmp
                            + skvRandomIter.getSampleDouble(q, k, 0));
                }
            }
            skvFactorRandomIter.done();
        }
        skvFactorRandomIter.done();
        return skyviewFactorImage;
    }

    /**
     * Evalutate a component of the skyview factor (the components depends of the elevation and
     * azimuth).
     * 
     * @param az the azimuth.
     * @param el the elevation.
     * @param skyview.
     */
    private void calculateFactor( double az, double el, WritableRaster skyview ) {

        /*
         * calculate the sun vector.
         */
        double SunVector[] = new double[]{sin(az) * cos(el), -cos(az) * cos(el), sin(el)};
        double SolVector[] = new double[3];
        double den = Math.max(Math.abs(sin(az) * cos(el)), Math.abs(-cos(az) * cos(el)));

        for( int i = 0; i < SolVector.length; i++ ) {
            SolVector[i] = -SunVector[i] / den;
        }

        double NormalSunVector[] = new double[3];
        NormalSunVector[2] = Math.sqrt(SunVector[0] * SunVector[0] + SunVector[1] * SunVector[1]);
        NormalSunVector[0] = -SunVector[0] * SunVector[2] / NormalSunVector[2];
        NormalSunVector[1] = -SunVector[1] * SunVector[2] / NormalSunVector[2];

        double casx = 1e6 * SunVector[0];
        double casy = 1e6 * SunVector[1];
        int f_i = 0;
        int f_j = 0;

        if (casx <= 0) {
            f_i = 0;
        } else {
            f_i = skyview.getWidth() - 1;
        }

        if (casy <= 0) {
            f_j = 0;
        } else {
            f_j = skyview.getHeight() - 1;
        }

        int j = f_j;
        for( int i = 0; i < skyview.getWidth(); i++ ) {
            shadow(el, i, j, SolVector, NormalSunVector, SunVector, skyview);
        }
        int i = f_i;
        for( int k = 0; k < skyview.getHeight(); k++ ) {
            shadow(el, i, k, SolVector, NormalSunVector, SunVector, skyview);
        }
    }

    /**
     * Calculate the angle.
     * 
     * @param elev, elevation in radians.
     * @param i the x index.
     * @param j the y index.
     * @param SolVector
     * @param normalSunVector
     * @param sunVector
     * @param skyRaster
     */
    private void shadow( double elev, int i, int j, double[] SolVector, double[] normalSunVector,
            double[] sunVector, WritableRaster skyRaster ) {
        int n = 0;
        WritableRandomIter skyRandomIter = RandomIterFactory.createWritable(skyRaster, null);
        double zcompare = -Double.MAX_VALUE;
        double dx = (SolVector[0] * n);
        double dy = (SolVector[1] * n);
        int nCols = skyRaster.getWidth();
        int nRows = skyRaster.getHeight();
        int idx = (int) (i + dx);
        int jdy = (int) (j + dy);
        double vectorToOrigin[] = new double[3];
        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);
        while( idx >= 0 && idx <= nCols - 1 && jdy >= 0 && jdy <= nRows - 1 ) {
            vectorToOrigin[0] = dx * res;
            vectorToOrigin[1] = dy * res;
            vectorToOrigin[2] = (pitRandomIter.getSampleDouble(idx, jdy, 0) + pitRandomIter
                    .getSampleDouble((int) (i + dx), (int) (j + dy), 0)) / 2;
            double zprojection = scalarProduct(vectorToOrigin, normalSunVector);
            double nGrad[] = normalRandomIter.getPixel(idx, jdy, new double[3]);
            double cosinc = scalarProduct(sunVector, nGrad);
            double elevRad = elev;
            if ((cosinc >= 0) && (zprojection > zcompare)) {
                skyRandomIter.setSample(idx, jdy, 0, elevRad);
                zcompare = zprojection;
            }
            n = n + 1;
            dy = (SolVector[1] * n);
            dx = (SolVector[0] * n);
            idx = (int) Math.round(i + dx);
            jdy = (int) Math.round(j + dy);
        }
        skyRandomIter.done();
        pitRandomIter.done();

    }

    /**
     * Return the normal vector to the surface.
     * <p>
     * It was calculated in the central point of each cells and then it is moved to the node with a
     * mean.
     * <p>
     * 
     * @param pitImage the elevation map.
     * @param res the resolution of the map.
     * @return the map with the normal vector to the surface.
     */

    private WritableRaster createNormalizedNormal( WritableRaster pitImage, double res ) {
        // the boundary
        int minX = pitImage.getMinX();
        int minY = pitImage.getMinY();
        int rows = pitImage.getHeight();
        int cols = pitImage.getWidth();

        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);
        /*
         * Initializa the Image of the normal vector in the central point of the cells, which have 3
         * components so the Image have 3 bands..
         */
        SampleModel sm = RasterFactory.createBandedSampleModel(5, cols, rows, 3);
        DiskMemImage tmpNormalVectorImage = new DiskMemImage(0, 0, pitImage.getWidth(), pitImage
                .getHeight(), 0, 0, sm, null);
        WritableRandomIter tmpNormalRandomIter = RandomIterFactory.createWritable(
                tmpNormalVectorImage, null);
        /*
         * apply the corripio's formula (is the formula (3) in the article)
         */
        for( int j = minY; j < minX + rows - 1; j++ ) {
            for( int i = minX; i < minX + cols - 1; i++ ) {
                double zij = pitRandomIter.getSampleDouble(i, j, 0);
                double zidxj = pitRandomIter.getSampleDouble(i + 1, j, 0);
                double zijdy = pitRandomIter.getSampleDouble(i, j + 1, 0);
                double zidxjdy = pitRandomIter.getSampleDouble(i + 1, j + 1, 0);
                // if (!isNovalue(zij) && !isNovalue(zidxj) && !isNovalue(zijdy)
                // && !isNovalue(zidxjdy)) {
                double firstComponent = 0.5 * res * (zij - zidxj + zijdy - zidxjdy);
                double secondComponent = 0.5 * res * (zij + zidxj - zijdy - zidxjdy);
                double thirthComponent = res * res;
                tmpNormalRandomIter.setPixel(i, j, new double[]{firstComponent, secondComponent,
                        thirthComponent});
                // } else {
                // tmpNormalRandomIter.setPixel(i, j, new double[] { NaN, NaN,
                // NaN });
                // }
            }
        }
        pitRandomIter.done();

        /*
         * Evalutate the value of the normal vector at the node as the mean of the four value
         * around, and normalize it.
         */
        WritableRaster normalVectorImage = FluidUtils.createDoubleWritableRaster(pitImage
                .getWidth(), pitImage.getHeight(), null, RasterFactory.createBandedSampleModel(5,
                cols, rows, 3), NaN);
        normalRandomIter = RandomIterFactory.createWritable(normalVectorImage, null);
        maxslope = 3.13 / 2.0;
        for( int t = normalVectorImage.getMinY(); t < normalVectorImage.getMinY()
                + normalVectorImage.getHeight(); t++ ) {
            for( int k = normalVectorImage.getMinX(); k < normalVectorImage.getMinX()
                    + normalVectorImage.getWidth(); k++ ) {
                normalRandomIter.setSample(k, t, 0, 1.0);
                normalRandomIter.setSample(k, t, 1, 1.0);
                normalRandomIter.setSample(k, t, 2, 1.0);

            }
            System.out.println();
        }
        for( int j = minY; j < minX + rows; j++ ) {
            for( int i = minX; i < minX + cols; i++ ) {
                double area = 0;
                double mean[] = new double[3];
                boolean isValidValue = true;
                for( int k = 0; k < 3; k++ ) {
                    double g00 = 1;
                    double g10 = 1;
                    double g01 = 1;
                    double g11 = 1;
                    if (j > 0 && i > 0) {
                        g00 = tmpNormalRandomIter.getSampleDouble(i - 1, j - 1, k);

                        g10 = tmpNormalRandomIter.getSampleDouble(i, j - 1, k);
                        g01 = tmpNormalRandomIter.getSampleDouble(i - 1, j, k);
                        g11 = tmpNormalRandomIter.getSampleDouble(i, j, k);
                    }

                    if (!isNovalue(g00) && !isNovalue(g01) && !isNovalue(g10) && !isNovalue(g11)) {
                        mean[k] = 1. / 4. * (g00 + g01 + g10 + g11);
                    } else {
                        isValidValue = false;
                        break;
                    }
                    area = area + mean[k] * mean[k];

                }
                if (isValidValue) {
                    area = Math.sqrt(area);
                    for( int k = 0; k < 3; k++ ) {
                        normalRandomIter.setSample(i, j, k, mean[k] / area);
                        if (i > minX && i < cols - 2 && j > minY && j < rows - 2 && k == 2) {
                            if (mean[k] / area < maxslope)
                                maxslope = mean[k] / area;
                        }
                    }

                }
            }
        }

        tmpNormalRandomIter.done();
        maxslope = (int) (Math.acos(maxslope) * 180.0 / PI);

        return normalVectorImage;
    }

    private double scalarProduct( double[] a, double[] b ) {
        double c = 0;
        for( int i = 0; i < a.length; i++ ) {
            c = c + a[i] * b[i];
        }
        return c;
    }

}
