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

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.JGrassModelsPlugin;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassIElementSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;

/**
 * <p>
 * This class allowed to read udig feature layers. Returns a featurecollection. Openmi based.
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class InputFeatureLayerReader extends ModelsBackbone {

    private ILink outputLink = null;

    private IOutputExchangeItem featureLayerOutputEI = null;

    private static final String modelParameters = "--iflayer <loaded udig map layer>";

    private JGrassIElementSet elementSet;

    public InputFeatureLayerReader() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public InputFeatureLayerReader( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String unitID = ModelsConstants.UNITID_FEATURE;

        String layerName = null;
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("iflayer")) { //$NON-NLS-1$
                layerName = argument.getValue();
            }
        }
        if (layerName == null) {
            throw new ModelsIllegalargumentException("Missing layer name argument.", this);
        }

        /*
         * create the exchange items. Since we could need to have the elementset already available
         * before the getvalues call, we are forced to read the data already now and put them into
         * the elementset.
         */
        out.println("Reading layer: " + layerName);
        /*
         * search for the right layers
         */
        List<ILayer> mapLayers = ApplicationGIS.getActiveMap().getMapLayers();
        ILayer requestedLayer = null;
        for( ILayer layer : mapLayers ) {
            if (layer.getName().trim().equals(layerName.trim())) {
                requestedLayer = layer;
                break;
            }
        }
        if (requestedLayer == null) {
            throw new ModelsIllegalargumentException("The requested layer could not be found: " + layerName, this);
        }

        /*
         * first get the selected feature
         */
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
        try {
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = requestedLayer
                    .getResource(FeatureSource.class, new NullProgressMonitor());

            Filter filter = requestedLayer.getFilter();
            // TODO check if nothing is selected on the layer, in which case Filer.ALL should be
            // used
            featureCollection = featureSource.getFeatures(filter);
            if (featureCollection.size() == 0) {
                featureCollection = featureSource.getFeatures();
            }
        } catch (IOException e1) {
            JGrassModelsPlugin.log("Problem reading features", e1); //$NON-NLS-1$
            e1.printStackTrace();
            throw new ModelsIllegalargumentException("Problem reading features from layer.", this);
        }

        elementSet = new JGrassElementset(ModelsConstants.FEATURECOLLECTION, featureCollection,
                null);
        IQuantity quantity = UtilitiesFacade.createScalarQuantity("", unitID);

        /*
         * create the output exchange item that will be passed over the link to which the component
         * is link to other components
         */
        featureLayerOutputEI = UtilitiesFacade.createOutputExchangeItem(this, quantity, elementSet);

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
            JGrassFeatureValueSet jgFeatureValueSet = new JGrassFeatureValueSet(elementSet
                    .getFeatureCollection());
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
        return featureLayerOutputEI;
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
