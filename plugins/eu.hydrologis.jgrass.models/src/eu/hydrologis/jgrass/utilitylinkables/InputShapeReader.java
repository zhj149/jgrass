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
package eu.hydrologis.jgrass.utilitylinkables;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassFeatureValueSet;

/**
 * <p>
 * This class allowed to read ShapeFile. Openmi compliant.
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class InputShapeReader extends ModelsBackbone {

    private ILink outputLink = null;

    private IOutputExchangeItem shapeFileOutputEI = null;

    private static final String modelParameters = "...";

    private String shapeFileName;

    private URL shapeURL = null;

    private FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;

    public InputShapeReader() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public InputShapeReader( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String unitID = "shape";
        int numFeature = 1;

        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("ishapefile")) {
                shapeFileName = argument.getValue();
            }
        }
        if (shapeFileName == null) {
            throw new ModelsIllegalargumentException("Missing shapefile argument.", this);
        }
        File file = new File(shapeFileName);
        if (!file.exists()) {
            throw new ModelsIllegalargumentException("Shapefile doesn't exist: " + shapeFileName,
                    this);
        }

        try {
            shapeURL = file.toURI().toURL();
        } catch (IOException e) {
            // should not happen if file exists
        }
        /*
         * create the exchange items. Since we could need to have the elementset already available
         * before the getvalues call, we are forced to read the data already now and put them into
         * the elementset.
         */
        try {
            out.println("Reading ShapeFile: " + shapeFileName);
            ShapefileDataStore store = new ShapefileDataStore(shapeURL);
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = store
                    .getFeatureSource();
            featureCollection = featureSource.getFeatures();

            /*
             * create the output exchange item that will be passed over the link to which the
             * component is link to other components
             */
            shapeFileOutputEI = ModelsConstants.createFeatureCollectionOutputExchangeItem(this,
                    featureCollection);
        } catch (IOException e) {
            throw new ModelsIllegalargumentException("Problems occurred in reading the shapefile: "
                    + shapeFileName, this);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.models.ModelsBackbone#getValues(org.openmi.standard.ITime,
     *      java.lang.String)
     */
    public IValueSet safeGetValues( ITime time, String linkID ) {
        if (linkID.equals(outputLink.getID())) {
            // we already have the collection in the elementset
            JGrassFeatureValueSet jgFeatureValueSet = new JGrassFeatureValueSet(
                    ((JGrassElementset) shapeFileOutputEI.getElementSet()).getFeatureCollection());
            return jgFeatureValueSet;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.models.ModelsBackbone#addLink(org.openmi.standard.ILink)
     */
    public void addLink( ILink link ) {
        outputLink = link;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.models.ModelsBackbone#finish()
     */
    public void finish() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.models.ModelsBackbone#getInputExchangeItem(int)
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.models.ModelsBackbone#getInputExchangeItemCount()
     */
    public int getInputExchangeItemCount() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.models.ModelsBackbone#getModelDescription()
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.models.ModelsBackbone#getOutputExchangeItem(int)
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return shapeFileOutputEI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.models.ModelsBackbone#getOutputExchangeItemCount()
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.models.ModelsBackbone#removeLink(java.lang.String)
     */
    public void removeLink( String linkID ) {
        outputLink = null;
    }

}
