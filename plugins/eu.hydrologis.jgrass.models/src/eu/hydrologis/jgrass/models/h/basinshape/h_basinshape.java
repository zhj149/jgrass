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
package eu.hydrologis.jgrass.models.h.basinshape;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.doubleNovalue;
import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.raster.RasterUtilities;
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.AreaExtractor;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;

/**
 * @author Andrea Antonello - www.hydrologis.com
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 */
public class h_basinshape extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit"; //$NON-NLS-1$

    public final static String basinsID = "basins"; //$NON-NLS-1$

    public final static String basinShapeID = "basinshape"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_basinShape.usage"); //$NON-NLS-1$

    private ILink pitLink = null;

    private ILink basinsLink = null;

    private ILink basinShapeLink = null;

    private IOutputExchangeItem basinShapeDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private IInputExchangeItem basinsDataInputEI = null;

    private JGrassRegion activeRegion = null;

    public h_basinshape() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_basinshape( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * in this method map's properties are defined... location, mapset... and
     * than IInputExchangeItem and IOutputExchangeItem are reated
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
            }
        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.basinShape";
        componentId = null;

        /*
         * create the exchange items
         */
        // basinshape output
        IElementSet basinShapeElementSet = new JGrassElementset(ModelsConstants.ACTIVEREGIONWINDOW, activeRegion, null);
        IQuantity basinShapeQuantity = UtilitiesFacade.createScalarQuantity(ModelsConstants.FEATURECOLLECTION,
                ModelsConstants.UNITID_FEATURE);
        basinShapeDataOutputEI = UtilitiesFacade.createOutputExchangeItem(this, basinShapeQuantity, basinShapeElementSet);

        // element set defining what we want to read
        // pit input
        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // basins input

        basinsDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(basinShapeLink.getID())) {

            GridCoverage2D pitData = null;
            if (pitLink != null) {
                IValueSet pitValueSet = pitLink.getSourceComponent().getValues(time, pitLink.getID());
                pitData = ((JGrassGridCoverageValueSet) pitValueSet).getGridCoverage2D();
            }
            IValueSet basinsValueSet = basinsLink.getSourceComponent().getValues(time, basinsLink.getID());
            GridCoverage2D basinsData = ((JGrassGridCoverageValueSet) basinsValueSet).getGridCoverage2D();
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = basinShape((PlanarImage) basinsData
                    .getRenderedImage(), (PlanarImage) pitData.getRenderedImage());
            if (featureCollection == null) {
                err.print(Messages.getString("Errors in execution...")); //$NON-NLS-1$
                return null;
            } else {
                return new JGrassFeatureValueSet(featureCollection);
            }
        }
        return null;
    }

    /**
     * @param basinsData
     * @param pitData
     * @return
     */
    private FeatureCollection<SimpleFeatureType, SimpleFeature> basinShape( PlanarImage basinsTmpImage, PlanarImage pitImage ) {
        out.println(Messages.getString("working") + " h.basinShape");

        // get rows and cols from the active region
        int cols = basinsTmpImage.getWidth();
        int rows = basinsTmpImage.getHeight();

        int minX = basinsTmpImage.getMinX();
        int minY = basinsTmpImage.getMinY();
        int maxX = basinsTmpImage.getMaxX();
        int maxY = basinsTmpImage.getMaxY();

        int[] nstream = new int[1];
        // nstream[0] = 1508;
        WritableRaster basinsImage = FluidUtils.createFromRenderedImage(basinsTmpImage);
        FluidUtils.setJAInoValueBorderIT(basinsImage);
        RandomIter basinsRandomIter = RandomIterFactory.create(basinsImage, null);
        for( int j = minY + 1; j < maxY; j++ ) {
            for( int i = minX + 1; i < maxX; i++ ) {
                if (!isNovalue(basinsRandomIter.getSampleDouble(i, j, 0))
                        && basinsRandomIter.getSampleDouble(i, j, 0) > (double) nstream[0]) {
                    nstream[0] = (int) basinsRandomIter.getSampleDouble(i, j, 0);
                }

            }
        }

        WritableRaster oneSubasinsImage = FluidUtils.createDoubleWritableRaster(pitImage.getWidth(), pitImage
                .getHeight(), null, pitImage.getSampleModel(), doubleNovalue);
        AreaExtractor aextra = null;

        // create the feature type
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        // set the name
        b.setName("basinshape"); //$NON-NLS-1$
        // add a geometry property
        b.add("the_geom", MultiPolygon.class); //$NON-NLS-1$
        // add some properties
        b.add("area", Float.class); //$NON-NLS-1$
        b.add("perimeter", Float.class); //$NON-NLS-1$
        b.add("netnum", Integer.class); //$NON-NLS-1$
        b.add("maxZ", Float.class); //$NON-NLS-1$
        b.add("minZ", Float.class); //$NON-NLS-1$
        b.add("avgZ", Float.class); //$NON-NLS-1$
        b.add("height", Float.class); //$NON-NLS-1$
        // build the type
        SimpleFeatureType type = b.buildFeatureType();
        // create the feature
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = FeatureCollections.newCollection();

        // for each stream correct problems with basins and create geometries
        for( int num = 1; num <= nstream[0]; num++ ) {
            Object[] values = new Object[8];

            int nordRow = -1;
            int southRow = 0;
            int eastCol = -1;
            int westCol = cols;
            int numPixel = 0;

            double minZ = Double.MAX_VALUE;
            double maxZ = Double.MIN_VALUE;
            double averageZ = 0.0;
            RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);
            WritableRandomIter oneRandomIter = RandomIterFactory.createWritable(oneSubasinsImage, null);
            for( int i = 0; i < rows; i++ ) {
                for( int j = 0; j < cols; j++ ) {
                    if (basinsRandomIter.getSampleDouble(i, j, 0) == num) {
                        if (nordRow == -1) {
                            nordRow = i;
                        }
                        if (i > nordRow) {
                            southRow = i;
                        }
                        if (westCol > j) {
                            westCol = j;
                        }
                        if (eastCol < j) {
                            eastCol = j;
                        }
                        oneRandomIter.setSample(i, j, 0, basinsRandomIter.getSampleDouble(i, j, 0));
                        double elevation = pitRandomIter.getSampleDouble(i, j, 0);
                        if (!isNovalue(elevation)) {
                            minZ = elevation < minZ ? elevation : minZ;
                            maxZ = elevation > maxZ ? elevation : maxZ;
                            averageZ = averageZ + elevation;
                        } else {
                            minZ = -1;
                            maxZ = -1;
                            averageZ = 0;
                        }
                        numPixel++;
                    }
                }
            }

            if (numPixel != 0) {
                out.println(Messages.getString("h_basinShape.basin") + " " + num + " / " + nstream[0]); //$NON-NLS-1$
                // min, max and average
                values[3] = num;
                values[4] = maxZ;
                values[5] = maxZ;
                values[6] = averageZ / numPixel;

                numPixel = 0;
                for( int i = nordRow; i < southRow + 1; i++ ) {
                    for( int j = westCol; j < eastCol + 1; j++ ) {
                        if (isNovalue(oneRandomIter.getSampleDouble(i, j, 0))) {
                            for( int k = 1; k <= 8; k++ ) {
                                // index.setFlow(k);
                                int indexI = i + ModelsConstants.DIR[k][1]; // index.getParameters()[
                                // 0];
                                int indexJ = j + ModelsConstants.DIR[k][0]; // index.getParameters()[
                                // 1];
                                if (!isNovalue(oneRandomIter.getSampleDouble(indexI, indexJ, 0))) {
                                    numPixel++;
                                }
                                k++;
                            }
                            if (numPixel == 4) {
                                oneRandomIter.setSample(i, j, 0, num);
                            }
                        }
                        numPixel = 0;
                    }
                }

                // extract the feature polygon of that basin number
                
                WritableRandomIter subbasinWritable = RandomIterFactory.createWritable(oneSubasinsImage, null);
                aextra = new AreaExtractor(subbasinWritable, activeRegion);
                SimpleFeature f = null;
                try {
                    f = aextra.extract_areas(num);
                } catch (Exception e) {
                    err.println("**********************************************");
                    err.println(e.getLocalizedMessage());
                    err.println("**********************************************");
                    continue;
                }

                MultiPolygon geometry = (MultiPolygon) f.getDefaultGeometry();
                values[0] = geometry;
                values[1] = geometry.getArea();
                values[2] = geometry.getLength();

                Point centroid = geometry.getCentroid();
                Coordinate centroidCoords = centroid.getCoordinate();
                int[] rowColPoint = RasterUtilities.putClickToCenterOfCell(activeRegion, centroidCoords);
                double centroidElevation = -1;;
                if (pitImage != null) {
                    double elev = pitRandomIter.getSampleDouble(rowColPoint[1], rowColPoint[0], 0);
                    if (!isNovalue(elev)) {
                        centroidElevation = elev;
                    }
                }
                values[6] = centroidElevation;
            }
            oneRandomIter.done();
            oneSubasinsImage = FluidUtils.createDoubleWritableRaster(pitImage.getWidth(), pitImage.getHeight(), null, pitImage
                    .getSampleModel(), doubleNovalue);
            aextra = null;

            // add the values
            builder.addAll(values);
            // build the feature with provided ID
            SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + num);
            featureCollection.add(feature);
        }

        basinsRandomIter.done();
        basinsImage = null;
        return featureCollection;
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(basinsID)) {
            basinsLink = link;
        }
        if (id.equals(basinShapeID)) {
            basinShapeLink = link;
        }
    }

    public void finish() {
    }

    /**
     * There is an IInputExchangeItem: pit, basins
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return pitDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return basinsDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 2;
    }

    /**
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: basinShapeength
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return basinShapeDataOutputEI;
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
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(basinsLink.getID())) {
            basinsLink = null;
        }
        if (linkID.equals(basinShapeLink.getID())) {
            basinShapeLink = null;
        }
    }

}
