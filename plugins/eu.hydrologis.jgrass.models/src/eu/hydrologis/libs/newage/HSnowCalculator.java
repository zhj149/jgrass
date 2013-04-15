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
package eu.hydrologis.libs.newage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.measure.unit.SI;

import nl.alterra.openmi.sdk.backbone.ElementSet;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.SpatialReference;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.openmi.standard.IArgument;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.libs.newage.swig.doubleArray;
import eu.hydrologis.libs.newage.swig.snow;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.JGrassIElementSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;

/**
 * <h2>Model for snow elevation calculation</h2>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 * @author Silvia Franceschi - www.hydrologis.com
 */
public class HSnowCalculator extends ModelsBackbone {
    private String quantityId;

    private String unitId;

    private String modelId;

    private String modelDescription;

    private IInputExchangeItem rainInputEI;

    private IInputExchangeItem temperatureInputEI;

    private IInputExchangeItem basinPositionInputEI;

    private IOutputExchangeItem snowOutputEI;

    private ILink basinPositionInputLink = null;

    private ILink rainInputLink = null;

    private ILink temperatureInputLink = null;

    private ILink snowOutputLink = null;

    private JGrassIElementSet basinPositionElementSet;

    private JGrassIElementSet rainElementSet;

    private JGrassIElementSet temperatureElementSet;

    private JGrassIElementSet snowElementSet;

    private int basinsNum;

    private doubleArray params;

    private doubleArray area;

    private doubleArray z0;

    private doubleArray rain;

    private doubleArray tbac;

    private doubleArray hsnowprec;

    private doubleArray hsnow;

    private doubleArray peff;

    private Double tsnow;

    private Double tmelt;

    private Double hnrif;

    private Double cneve;

    private Double qrif;

    private Double cmelt;

    private Double dt_prec;

    private Double volprec;

    static {
        try {
            System.loadLibrary("snow");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Native code library failed to load.\n" + e);
        }
    }

    public void addLink( ILink link ) {
        // if this is the target of the link, it has to be one of the input links
        if (link.getTargetComponent().equals(this)) {
            // check if the quantity is rain or temperature
            if (link.getTargetQuantity().equals(rainInputEI.getQuantity())
                    && link.getTargetElementSet().equals(rainInputEI.getElementSet())) {
                rainInputLink = link;
            } else if (link.getTargetQuantity().equals(temperatureInputEI.getQuantity())
                    && link.getTargetElementSet().equals(temperatureInputEI.getElementSet())) {
                temperatureInputLink = link;
            } else if (link.getTargetQuantity().equals(basinPositionInputEI.getQuantity())
                    && link.getTargetElementSet().equals(basinPositionInputEI.getElementSet())) {
                basinPositionInputLink = link;
            } else
                System.out.println("Wrong components");
        }
        // if this is the source of the link, it has to be one of the output links
        else if (link.getSourceComponent().equals(this)) {
            if (link.getSourceQuantity().equals(snowOutputEI.getQuantity())
                    && link.getSourceElementSet().equals(snowOutputEI.getElementSet())) {
                snowOutputLink = link;
            } else
                System.out.println("Wrong components");
        } else {
            System.out.println("Wrong components");
        }

    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return rainInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return temperatureInputEI;
        } else if (inputExchangeItemIndex == 2) {
            return basinPositionInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 3;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return snowOutputEI;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void safeInitialize( IArgument[] properties ) throws Exception{
        /*
         * arguments needed for the initialization
         */
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.compareTo("quantityid") == 0) {
                quantityId = argument.getValue();
            }
            if (key.compareTo("unitid") == 0) {
                unitId = argument.getValue();
            }
            if (key.compareTo("tsnow") == 0) {
                tsnow = Double.valueOf(argument.getValue());
            }
            if (key.compareTo("tmelt") == 0) {
                tmelt = Double.valueOf(argument.getValue());
            }
            if (key.compareTo("hnrif") == 0) {
                hnrif = Double.valueOf(argument.getValue());
            }
            if (key.compareTo("cneve") == 0) {
                cneve = Double.valueOf(argument.getValue());
            }
            if (key.compareTo("qrif") == 0) {
                qrif = Double.valueOf(argument.getValue());
            }
            if (key.compareTo("cmelt") == 0) {
                cmelt = Double.valueOf(argument.getValue());
            }
            if (key.compareTo("dt_prec") == 0) {
                dt_prec = Double.valueOf(argument.getValue());
            }
            if (key.compareTo("volprec") == 0) {
                volprec = Double.valueOf(argument.getValue());
            }

        }

        modelId = "snow calculator";
        modelDescription = modelId;

        /*
         * rain input exchange item
         */
        IQuantity rainQuantity = UtilitiesFacade.createScalarQuantity("rain", "mm/h");
        rainElementSet = new JGrassElementset("dummyrainelementset", "dummyrainelementset",
                JGrassIElementSet.JGrassElementType.PointCollection, new SpatialReference(
                        "some reference"));
        rainInputEI = UtilitiesFacade.createInputExchangeItem(this, rainQuantity, rainElementSet);
        /*
         * temperature input exchange item
         */
        IQuantity temperatureQuantity = UtilitiesFacade.createScalarQuantity("temperature",
                SI.CELSIUS.toString());
        temperatureElementSet = new JGrassElementset("dummytemperatureelementset",
                "dummytemperatureelementset", JGrassIElementSet.JGrassElementType.PointCollection,
                new SpatialReference("some reference"));
        temperatureInputEI = UtilitiesFacade.createInputExchangeItem(this, temperatureQuantity,
                temperatureElementSet);
        /*
         * basinPosition input exchange item
         */
        IQuantity basinPositionQuantity = UtilitiesFacade
                .createScalarQuantity("basinPosition", "m");
        basinPositionElementSet = new JGrassElementset("dummyareaelementset",
                "dummyareaelementset", JGrassIElementSet.JGrassElementType.PointCollection,
                new SpatialReference("some reference"));
        basinPositionInputEI = UtilitiesFacade.createInputExchangeItem(this, basinPositionQuantity,
                basinPositionElementSet);

        /*
         * output exchange item
         */
        IQuantity outputQuantity = UtilitiesFacade.createScalarQuantity(quantityId, unitId);
        snowElementSet = new JGrassElementset("dummysnowelementset", "dummysnowelementset",
                JGrassIElementSet.JGrassElementType.PointCollection, new SpatialReference(
                        "some reference"));
        snowOutputEI = UtilitiesFacade.createOutputExchangeItem(this, outputQuantity,
                snowElementSet);

    }

    public void prepare() {

        /*
         * retrieve the positions from the right elementset
         */
        basinPositionElementSet = (JGrassIElementSet) basinPositionInputLink.getSourceElementSet();
        basinsNum = basinPositionElementSet.getElementCount();

        FeatureCollection featC = basinPositionElementSet.getFeatureCollection();
        List<Geometry> basinsPositions = new ArrayList<Geometry>();
        Iterator fIterator = featC.iterator();
        if (basinPositionElementSet.getElementType().equals(
                JGrassIElementSet.JGrassElementType.PointCollection)) {
            while( fIterator.hasNext() ) {
                SimpleFeature f = (SimpleFeature) fIterator.next();
                basinsPositions.add((Geometry) f.getDefaultGeometry());
            }
            featC.close(fIterator);
        } else {
            System.out.println("Error in type");
        }
        /*
         * make it available for the other links
         */
        temperatureElementSet = basinPositionElementSet;
        rainElementSet = basinPositionElementSet;

        /*
         * tsnow = params(1) tmelt = params(2) hnrif = params(3) cneve = params(4) qrif = params(5)
         * cmelt = params(6) dt_prec = params(7) nbaric = params(8) volprec = params(9)
         */
        params = new doubleArray(9);
        params.setitem(0, tsnow); // tsnow
        params.setitem(1, tmelt); // tmelt
        params.setitem(2, hnrif); // hnrif
        params.setitem(3, cneve); // cneve
        params.setitem(4, qrif); // qrif
        params.setitem(5, cmelt); // cmelt
        params.setitem(6, dt_prec); // dt_prec
        params.setitem(7, volprec); // volprec

        /*
         * prepare for the area
         */
        area = new doubleArray(basinsNum);
        /*
         * prepare for the rain
         */
        rain = new doubleArray(basinsNum);
        /*
         * prepare for the temperature
         */
        tbac = new doubleArray(basinsNum);
        /*
         * prepare for the hsnowprec
         */
        hsnowprec = new doubleArray(basinsNum);
        for( int i = 0; i < basinsNum; i++ ) {
            hsnowprec.setitem(i, -9999.0);
        }
        /*
         * prepare for the hsnow
         */
        hsnow = new doubleArray(basinsNum);
        /*
         * prepare for the peff
         */
        peff = new doubleArray(basinsNum);
        /*
         * do Z0
         */
        z0 = new doubleArray(basinsNum);
        for( int i = 0; i < basinsPositions.size(); i++ ) {
            z0.setitem(i, basinsPositions.get(i).getCoordinate().z);
        }

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        // the only output link is the snow output link
        if (!linkID.equals(snowOutputLink.getID())) {
            return null;
        }
        // read area
        IValueSet areaValueSet = basinPositionInputLink.getSourceComponent()
                .getValues(time, linkID);
        for( int i = 0; i < areaValueSet.getCount(); i++ ) {
            area.setitem(i, ((ScalarSet) areaValueSet).getScalar(i));
        }
        // get the rain
        IValueSet rainValueSet = rainInputLink.getSourceComponent().getValues(time, linkID);
        for( int i = 0; i < rainValueSet.getCount(); i++ ) {
            rain.setitem(i, ((ScalarSet) rainValueSet).getScalar(i));
        }
        // get the rain
        IValueSet tempValueSet = temperatureInputLink.getSourceComponent().getValues(time, linkID);
        for( int i = 0; i < tempValueSet.getCount(); i++ ) {
            tbac.setitem(i, ((ScalarSet) tempValueSet).getScalar(i));
        }

        snow.cstige_neve(params.cast(), area.cast(), z0.cast(), rain.cast(), tbac.cast(), hsnowprec
                .cast(), hsnow.cast(), peff.cast());

        double[] snowOutArray = new double[basinsNum];
        for( int i = 0; i < basinsNum; i++ ) {
            System.out.print(peff.getitem(i) + " ");
            snowOutArray[i] = peff.getitem(i);
        }
        System.out.println();
        return new ScalarSet(snowOutArray);
    }

    public void removeLink( String linkID ) {
        if (rainInputLink.getID().equals(linkID)) {
            rainInputLink = null;
        }
        if (temperatureInputLink.getID().equals(linkID)) {
            temperatureInputLink = null;
        }
        if (basinPositionInputLink.getID().equals(linkID)) {
            basinPositionInputLink = null;
        }
        if (snowOutputLink.getID().equals(linkID)) {
            snowOutputLink = null;
        }
    }

    public String getModelDescription() {
        // FIXME Auto-generated method stub
        return null;
    }
}
