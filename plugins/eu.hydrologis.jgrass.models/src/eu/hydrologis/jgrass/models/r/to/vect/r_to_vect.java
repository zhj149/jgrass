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
package eu.hydrologis.jgrass.models.r.to.vect;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.io.File;
import java.io.PrintStream;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IArgument;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintstreamProgress;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIOException;
import eu.hydrologis.libs.openmi.OneInOneOutModelsBackbone;
import eu.hydrologis.libs.utils.AreaExtractor;
import eu.hydrologis.libs.utils.FluidConstants;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class r_to_vect extends OneInOneOutModelsBackbone {

    private String POINT = "POINT";
    private String POLYGON = "POLYGON";

    private String mode = "POINT";
    private JGrassRegion activeRegion;

    public r_to_vect() {
        super();
        modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("r_to_vect.usage");
    }

    public r_to_vect( PrintStream output, PrintStream error ) {
        super(output, error);
        modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("r_to_vect.usage");
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String grassDb = null;
        String location = null;
        String mapset = null;
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
            if (key.compareTo("mode") == 0) {
                mode = argument.getValue();
            }

        }

        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        /*
         * create the output exchange item that will be passed over the link to which the component
         * is link to other componentes
         */
        inputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        outputEI = ModelsConstants.createFeatureCollectionOutputExchangeItem(this, null);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (linkID.equals(outLink.getID())) {

            GridCoverage2D inputGC = ModelsConstants.getGridCoverage2DFromLink(inLink, time, err);
            RandomIter inputIter = RandomIterFactory.create(inputGC.getRenderedImage(), null);

            int rows = activeRegion.getRows();
            int cols = activeRegion.getCols();

            // create the feature type
            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
            b.setName("pointtype");
            b.add("the_geom", Point.class);
            b.add("cat", Double.class);

            // build the type
            SimpleFeatureType type = b.buildFeatureType();
            if (mode.equals(POINT)) {
                GeometryFactory gF = new GeometryFactory();

                FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();

                out.println("Processing raster...");
                PrintstreamProgress mon = new PrintstreamProgress(0, rows, out);
                int index = 0;
                for( int y = 0; y < rows; y++ ) {
                    mon.printPercent(y);
                    for( int x = 0; x < cols; x++ ) {
                        double value = inputIter.getSampleDouble(x, y, 0);

                        if (isNovalue(value)) {
                            continue;
                        }

                        Coordinate coord = JGrassUtilities.rowColToCenterCoordinates(activeRegion, y, x);
                        Point point = gF.createPoint(coord);
                        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
                        Object[] values = new Object[]{point, value};
                        builder.addAll(values);
                        SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + index++);
                        newCollection.add(feature);
                    }
                }
                JGrassFeatureValueSet jgrassFeatureValueSet = new JGrassFeatureValueSet(newCollection);
                return jgrassFeatureValueSet;

            } else if (mode.equals(POLYGON)) {

                AreaExtractor aextra = new AreaExtractor(inputIter, activeRegion);
                SimpleFeature extractedAreas = aextra.extract_areas(0);
                FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection();
                newCollection.add(extractedAreas);

                JGrassFeatureValueSet jgrassFeatureValueSet = new JGrassFeatureValueSet(newCollection);
                return jgrassFeatureValueSet;
            } else {
                throw new ModelsIOException("Only points and polygons are supported at the moment.", this);
            }

        }

        return null;
    }

}
