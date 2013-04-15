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
package eu.hydrologis.jgrass.models.h.orderbasins;

import java.io.PrintStream;
import java.util.List;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.libs.adige.HillSlope;
import eu.hydrologis.libs.adige.NetBasinsManager;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassFeatureValueSet;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class h_orderbasins extends ModelsBackbone {

    private static final String PFAFATTR = "pfafattr"; //$NON-NLS-1$
    private static final String NETNUMATTR = "netnumattr"; //$NON-NLS-1$

    public static String outputBasinsID = "out"; //$NON-NLS-1$
    public final static String netpfafID = "netpfaf"; //$NON-NLS-1$
    public final static String hillslopeID = "hills"; //$NON-NLS-1$

    private final static String modelParameters = "";

    private ILink netpfafLink = null;
    private ILink hillslopeLink = null;
    private ILink outputBasinsLink = null;

    private IInputExchangeItem hillslopeInputEI = null;
    private IInputExchangeItem netpfafInputEI = null;
    private IOutputExchangeItem basinsOutputEI = null;

    private FeatureCollection<SimpleFeatureType, SimpleFeature> netpfafFeatureCollection;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> hillslopeFeatureCollection;

    /*
     * ATTRIBUTES FIELDS
     */
    private String netNumAttributeName;
    private String pfaffAttributeName;
    private List<HillSlope> orderedHillslopes;
    private JGrassFeatureValueSet featureValueset;

    public h_orderbasins() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_orderbasins( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals(NETNUMATTR)) {
                netNumAttributeName = argument.getValue();
            }
            if (key.equals(PFAFATTR)) {
                pfaffAttributeName = argument.getValue();
            }
        }

        if (netNumAttributeName == null || netNumAttributeName.length() < 1) {
            throw new ModelsIllegalargumentException("Missing net num attribute name.", this);
        }
        if (pfaffAttributeName == null || pfaffAttributeName.length() < 1) {
            throw new ModelsIllegalargumentException("Missing pfafstetter attribute name.", this);
        }

        // hydrometers input
        netpfafInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        hillslopeInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        basinsOutputEI = ModelsConstants.createDummyOutputExchangeItem(this);
    }

    public void safePrepare() throws Exception {
        // retrieve net features
        JGrassElementset netElementset = (JGrassElementset) netpfafLink.getSourceElementSet();
        netpfafFeatureCollection = netElementset.getFeatureCollection();
        if (netpfafFeatureCollection == null) {
            throw new ModelsIllegalargumentException(
                    "An error occurred while reading the network geometries", this);
        }
        netpfafInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this,
                netpfafFeatureCollection);
        // retrieve basins features
        JGrassElementset basinsElementset = (JGrassElementset) hillslopeLink.getSourceElementSet();
        hillslopeFeatureCollection = basinsElementset.getFeatureCollection();
        if (hillslopeFeatureCollection == null) {
            throw new ModelsIllegalargumentException(
                    "An error occurred while reading the hillslopes geometries", this);
        }
        hillslopeInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this,
                hillslopeFeatureCollection);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (linkID.equals(outputBasinsLink.getID())) {

            if (orderedHillslopes == null) {
                // at the first round create the hillslopes and network hierarchy
                NetBasinsManager nbMan = new NetBasinsManager();
                orderedHillslopes = nbMan.operateOnLayers(netpfafFeatureCollection,
                        hillslopeFeatureCollection, netNumAttributeName, pfaffAttributeName, null,
                        null, null, null, out);

                FeatureCollection<SimpleFeatureType, SimpleFeature> fcollection = FeatureCollections
                        .newCollection();
                for( HillSlope hillS : orderedHillslopes ) {
                    fcollection.add(hillS.getHillslopeFeature());
                }

                featureValueset = new JGrassFeatureValueSet(fcollection);
                basinsOutputEI = ModelsConstants.createFeatureCollectionOutputExchangeItem(this,
                        fcollection);
            }

            return featureValueset;
        }
        return null;
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(netpfafID)) {
            netpfafLink = link;
        } else if (id.equals(hillslopeID)) {
            hillslopeLink = link;
        } else
            outputBasinsLink = link;
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return hillslopeInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return netpfafInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 2;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return basinsOutputEI;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(netpfafLink.getID())) {
            netpfafLink = null;
        } else if (linkID.equals(hillslopeLink.getID())) {
            hillslopeLink = null;
        } else
            outputBasinsLink = null;
    }

}
