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
package eu.hydrologis.jgrass.models.r.zonalstats;

import jaitools.numeric.Statistic;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * The zonalstats module.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class r_zonalstats extends ModelsBackbone {

    private final static String coverageInID = "coverage"; //$NON-NLS-1$
    private final static String featureInID = "feature"; //$NON-NLS-1$

    private final static String outID = "out"; //$NON-NLS-1$ 

    private ILink coverageInLink = null;
    private ILink featureInLink = null;

    private ILink outLink = null;

    private IOutputExchangeItem outputEI = null;

    private IInputExchangeItem coverageInputEI = null;
    private IInputExchangeItem featureInputEI = null;

    private String modelParameters = ""; //$NON-NLS-1$
    private JGrassRegion activeRegion;

    private Double buffer = null;

    public r_zonalstats() {
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public r_zonalstats( PrintStream output, PrintStream error ) {
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
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
                if (key.compareTo("buffer") == 0) {
                    String bufferStr = argument.getValue();
                    try {
                        buffer = Double.parseDouble(bufferStr);
                    } catch (Exception e) {
                        // check will be done later
                    }
                }
            }

        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        coverageInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        featureInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this, null);
        outputEI = ModelsConstants.createDummyOutputExchangeItem(this);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        // get the coverage
        GridCoverage2D coverage2D = ModelsConstants.getGridCoverage2DFromLink(coverageInLink, time, err);

        // FIXME enable as soon as jaitools is uptodate 
        
        // // get the features
        // IValueSet featuresValueSet = featureInLink.getSourceComponent().getValues(time,
        // featureInLink.getID());
        // FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection =
        // ((JGrassFeatureValueSet) featuresValueSet)
        // .getFeatureCollection();
        // FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        // boolean isPolygonMode = false;
        // boolean isPointMode = false;
        // if (featureIterator.hasNext()) {
        // SimpleFeature feature = featureIterator.next();
        // Object geom = feature.getDefaultGeometry();
        // if (geom instanceof Polygon || geom instanceof MultiPolygon) {
        // isPolygonMode = true;
        // }
        // if (geom instanceof Point || geom instanceof MultiPoint) {
        // isPointMode = true;
        // }
        // }
        // featureCollection.close(featureIterator);
        //
        // Set<Statistic> statsSet = new LinkedHashSet<Statistic>();
        // statsSet.add(Statistic.MIN);
        // statsSet.add(Statistic.MAX);
        // statsSet.add(Statistic.MEAN);
        // statsSet.add(Statistic.MEDIAN);
        // statsSet.add(Statistic.VARIANCE);
        // statsSet.add(Statistic.SDEV);
        // statsSet.add(Statistic.RANGE);
        // statsSet.add(Statistic.APPROX_MEDIAN);
        // statsSet.add(Statistic.SUM);
        //
        // Integer[] bands = new Integer[]{0};
        // StatisticsTool statisticsTool = null;
        // if (isPolygonMode) {
        // statisticsTool = StatisticsTool.getInstance(statsSet, coverage2D, bands,
        // featureCollection);
        // } else if (isPointMode) {
        // if (buffer == null) {
        // throw new
        // ModelsIllegalargumentException("In point mode the buffer argument has to be set.", this);
        // }
        // statisticsTool = StatisticsTool.getInstance(statsSet, coverage2D, bands,
        // featureCollection, buffer);
        // } else {
        // throw new
        // ModelsIllegalargumentException("Zonalstats can be operated only on Point and Polygon layers.",
        // this);
        // }
        //
        // statisticsTool.run();
        //
        // featureIterator = featureCollection.features();
        // while( featureIterator.hasNext() ) {
        // SimpleFeature feature = featureIterator.next();
        // String id = feature.getID();
        // Map<Statistic, Double[]> statistics = statisticsTool.getStatistics(id);
        //
        // out.println();
        // out.println("Stats for feature of fid: " + id);
        // for( Statistic statistic : statsSet ) {
        // out.println(statistic.toString() + ":" + statistics.get(statistic)[0]);
        // }
        // }
        // featureCollection.close(featureIterator);

        ScalarSet scalarSet = new ScalarSet(new double[]{0.0});
        return scalarSet;
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(coverageInID)) {
            coverageInLink = link;
        }
        if (id.equals(featureInID)) {
            featureInLink = link;
        }
        if (id.equals(outID)) {
            outLink = link;
        }
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return coverageInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return featureInputEI;
        } else
            return null;
    }

    public int getInputExchangeItemCount() {
        return 2;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return outputEI;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(coverageInLink.getID())) {
            coverageInLink = null;
        }
        if (linkID.equals(featureInLink.getID())) {
            featureInLink = null;
        }
        if (linkID.equals(outLink.getID())) {
            outLink = null;
        }
    }

}
