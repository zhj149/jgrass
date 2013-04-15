package eu.hydrologis.jgrass.models.v.addattributes;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIOException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassFeatureValueSet;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class v_addattributes extends ModelsBackbone {

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("rv_rc2shpattr.usage");

    public final static String attributesID = "attributes"; //$NON-NLS-1$
    public final static String inShapeID = "infeatures"; //$NON-NLS-1$
    public final static String outShapeID = "outfeatures"; //$NON-NLS-1$

    private ILink attributesLink = null;
    private ILink inShapeLink = null;
    private ILink outShapeLink = null;

    private IOutputExchangeItem shapeDataOutputEI;
    private IInputExchangeItem shapeDataInputEI;
    private IInputExchangeItem attributesInputEI;

    private String joinField;
    private String newFields;

    private int joinFieldIndex;

    private PrintStreamProgressMonitor pm;

    public v_addattributes() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
        pm = new PrintStreamProgressMonitor(out);
    }

    public v_addattributes( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
        pm = new PrintStreamProgressMonitor(out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        if (properties != null) {
            for( IArgument argument : properties ) {
                String key = argument.getKey();
                if (key.compareTo("joinfield") == 0) {
                    joinField = argument.getValue();
                }
                if (key.compareTo("newfields") == 0) {
                    newFields = argument.getValue();
                }
            }
        }

        if (joinField == null) {
            throw new IllegalArgumentException(
                    "The parameter defining the feature field to join is mandatory. Check your syntax.");
        }

        /*
         * create the exchange items
         */
        shapeDataOutputEI = ModelsConstants.createFeatureCollectionOutputExchangeItem(this, null);
        shapeDataInputEI = ModelsConstants.createFeatureCollectionInputExchangeItem(this, null);
        attributesInputEI = ModelsConstants.createDummyInputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        List<SimpleFeature> featureList = ModelsConstants.getFeatureListFromLink(inShapeLink, time,
                err);
        ScalarSet attrScalarSet = ModelsConstants.getScalarSetFromLink(attributesLink, time, err);

        if (attrScalarSet == null || featureList == null) {
            throw new ModelsIOException(
                    "An error occurred while reading the input data. Check your syntax.", this);
        }

        /*
         * check if the field is available
         */
        SimpleFeature tmpFeature = featureList.get(0);
        SimpleFeatureType featureType = tmpFeature.getFeatureType();
        joinFieldIndex = featureType.indexOf(joinField);
        if (joinFieldIndex == -1) {
            throw new ModelsIOException(MessageFormat.format(
                    "The field {0} could not be found in the input features.", joinField), this);
        }
        int featureNum = featureList.size();
        int attrNum = attrScalarSet.get(0).intValue();
        /*
         * find number of fields to be added, assuming that the 
         * incoming scalarset is of format: 
         * id1, v1, v2, v3, id2, v1, v2, v3, id3, v1, ...  , idn, v1, v1, v3
         * in one row. 
         */
        int newFieldNum = attrNum / featureNum - 1;
        String[] finalNewFieldNames = new String[newFieldNum];

        if (newFields != null) {
            String[] newFieldNames = newFields.split(",");
            if (newFieldNames.length != newFieldNum) {
                throw new ModelsIOException(
                        "The supplied fields number is not equal to the field number that should be created.",
                        this);
            }
        } else {
            for( int i = 0; i < finalNewFieldNames.length; i++ ) {
                finalNewFieldNames[i] = "new_" + i;
            }
        }

        HashMap<Integer, double[]> attributesMap = new HashMap<Integer, double[]>();
        for( int i = 1; i < attrScalarSet.size(); i = i + newFieldNum + 1 ) {
            Number id = attrScalarSet.get(i);
            double[] attrNumbers = new double[newFieldNum];
            for( int j = 0; j < attrNumbers.length; j++ ) {
                attrNumbers[j] = attrScalarSet.get(i + j + 1);
            }
            attributesMap.put(id.intValue(), attrNumbers);
        }

        FeatureCollection<SimpleFeatureType, SimpleFeature> newFeatureCollection = mergeRasterVector(
                featureList, featureType, finalNewFieldNames, attributesMap);
        return new JGrassFeatureValueSet(newFeatureCollection);
    }

    private FeatureCollection<SimpleFeatureType, SimpleFeature> mergeRasterVector(
            List<SimpleFeature> featureList, SimpleFeatureType featureType,
            String[] finalNewFieldNames, HashMap<Integer, double[]> attributesMap )
            throws Exception {

        List<AttributeDescriptor> oldAttributes = featureType.getAttributeDescriptors();
        List<AttributeDescriptor> attributesDescriptorList = new ArrayList<AttributeDescriptor>();
        for( AttributeDescriptor attributeType : oldAttributes ) {
            attributesDescriptorList.add(attributeType);
        }
        /*
         * create and add the new attribute types
         */
        for( int i = 0; i < finalNewFieldNames.length; i++ ) {
            AttributeTypeBuilder build = new AttributeTypeBuilder();
            build.setNillable(true);
            build.setBinding(Double.class);
            build.setName(finalNewFieldNames[i]);
            AttributeType doubleType = build.buildType();
            AttributeDescriptor descriptor = build.buildDescriptor(finalNewFieldNames[i],
                    doubleType);
            attributesDescriptorList.add(descriptor);
        }

        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName("newattributes");
        featureTypeBuilder.addAll(attributesDescriptorList);
        SimpleFeatureType newFeatureType = featureTypeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(newFeatureType);

        List<SimpleFeature> newFeaturesList = new ArrayList<SimpleFeature>();

        pm.beginTask("Merging features...", featureList.size());
        for( int i = 0; i < featureList.size(); i++ ) {
            pm.worked(1);

            SimpleFeature feature = featureList.get(i);

            List<Object> attributes = feature.getAttributes();
            Object[] newAttributes = new Object[attributesDescriptorList.size()];
            System.arraycopy((Object[]) attributes.toArray(new Object[attributes.size()]), 0,
                    newAttributes, 0, attributes.size());

            Number attribute = (Number) feature.getAttribute(joinFieldIndex);
            double[] attrNumbers = attributesMap.get(attribute.intValue());
            if (attrNumbers == null) {
                throw new ModelsIOException("Could not find a join given by the value: "
                        + attribute.doubleValue(), this);
            }

            for( int j = 0; j < attrNumbers.length; j++ ) {
                newAttributes[newAttributes.length - (attrNumbers.length - j)] = attrNumbers[j];
            }
            featureBuilder.addAll(newAttributes);
            SimpleFeature newF = featureBuilder.buildFeature(feature.getID());
            newFeaturesList.add(newF);
        }
        pm.done();

        FeatureCollection<SimpleFeatureType, SimpleFeature> fcollection = FeatureCollections
                .newCollection();
        for( SimpleFeature feature : newFeaturesList ) {
            fcollection.add(feature);
        }
        return fcollection;

    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(attributesID)) {
            attributesLink = link;
        }
        if (id.equals(outShapeID)) {
            outShapeLink = link;
        }
        if (id.equals(inShapeID)) {
            inShapeLink = link;
        }
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return shapeDataInputEI;
        } else {
            return attributesInputEI;
        }
    }

    public int getInputExchangeItemCount() {
        return 2;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return shapeDataOutputEI;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(attributesLink.getID())) {
            attributesLink = null;
        }
        if (linkID.equals(inShapeLink.getID())) {
            inShapeLink = null;
        }
        if (linkID.equals(outShapeLink.getID())) {
            outShapeLink = null;
        }
    }
}
