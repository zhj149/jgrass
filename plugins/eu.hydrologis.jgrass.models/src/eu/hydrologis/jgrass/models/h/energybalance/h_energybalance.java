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
package eu.hydrologis.jgrass.models.h.energybalance;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

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

import com.vividsolutions.jts.geom.Geometry;

import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.IProgressMonitorJGrass;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIOException;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.util.HydrologisDate;

/**
 * @author Silvia Franceschi (www.hydrologis.com)
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class h_energybalance extends ModelsBackbone {

    private static final int GLACIER_SWE = 2000;
    /*
     * OpenMi variables definition
     */

    private ILink basinsInputLink = null;
    private ILink rainInputLink = null;
    private ILink temperatureInputLink = null;
    private ILink windSpeedInputLink = null;
    private ILink pressureInputLink = null;
    private ILink relativeHumidityInputLink = null;
    private ILink dailyTempRangeInputLink = null;
    private ILink montlyTempRangeInputLink = null;
    private ILink energyIndexInputLink = null;
    private ILink areaHeigthEnergyInputLink = null;

    private ILink snowWaterEquivalentLink = null;
    private ILink netPrecipitationLink = null;
    private ILink rainPrecipitationLink = null;
    private ILink snowPrecipitationLink = null;
    private ILink AverageTemperatureLink = null;
    private ILink outuptRainLink = null;
    private ILink fullAdigeOutputLink = null;
    private ILink mainOutputLink = null;

    private IInputExchangeItem basinsInputExchangeItem = null;
    private IInputExchangeItem rainInputExchangeItem = null;
    private IInputExchangeItem temperatureInputExchangeItem = null;
    private IInputExchangeItem windSpeedInputExchangeItem = null;
    private IInputExchangeItem pressureInputExchangeItem = null;
    private IInputExchangeItem relativeHumidityInputExchangeItem = null;
    private IInputExchangeItem dailyTempRangeInputExchangeItem = null;
    private IInputExchangeItem montlyTempRangeInputExchangeItem = null;
    private IInputExchangeItem energyIndexInputExchangeItem = null;
    private IInputExchangeItem areaHeigthEnergyInputExchangeItem = null;

    private IOutputExchangeItem snowWaterEquivalentOutputExchangeItem = null;
    private IOutputExchangeItem netPrecipitationOutputExchangeItem = null;
    private IOutputExchangeItem rainPrecipitationOtputExchangeItem = null;
    private IOutputExchangeItem snowPrecipitationOtputExchangeItem = null;
    private IOutputExchangeItem averageTemperatureOtputExchangeItem = null;
    private IOutputExchangeItem outputRainOtputExchangeItem = null;
    private IOutputExchangeItem fullAdigeOutputExchangeItem = null;
    private IOutputExchangeItem mainOutputExchangeItem = null;

    public final static String basinsId = "basin";
    public final static String rainId = "rain";
    public final static String temperatureId = "temperature";
    public final static String windSpeedId = "wind";
    public final static String pressureId = "pressure";
    public final static String relativeHumidityId = "humidity";
    public final static String dailyTempRangeId = "dtday";
    public final static String monthlyTempRangeId = "dtmonth";
    public final static String energyIndexId = "energy";
    public final static String areaHeigthEnergyIndexId = "area";

    public final static String snowWaterEquivalentId = "swe";
    public final static String netPrecipitationId = "pnet";
    public final static String rainPrecipitationId = "prain";
    public final static String snowPrecipitationId = "psnow";
    public final static String averageTemperatureId = "tempout";
    public final static String outputRainId = "rainout";
    public final static String fullAdigeId = "adige";
    public final static String mainId = "main";

    public final static String usoField = "usofield";
    public final static String glacierValue = "glaciervalue";

    private final static String modelParameters = "...Usage";
    private IProgressMonitorJGrass pm;

    private HydrologisDate currentTime;

    private Calendar myCalendar = Calendar.getInstance();

    /*
     * Model's variables definition
     */

    private double train = -9999.0;
    private double tsnow = -9999.0;
    private int infittimentoDt = 1;
    private double rho_sn = -9999.0;
    private double tolerance_U0 = -9999.0;
    private double tolerance_W0 = -9999.0;
    private double tolerance_U = -9999.0;
    private double tolerance_W = -9999.0;
    private double tolerance_Ts = -9999.0;
    private int num_iter_UW = -9999;
    private int num_iter_Ts = -9999;
    private int num_ES = -9999;
    private int num_EI = -9999;
    private double sweStartParam = -9999.0;
    private double canopyconst = 0.0;
    private double snowReflVo = 0.85;
    private double snowReflIR = 0.65;

    /**
     * latitude latitude in rad.
     */
    private double latitude;

    /**
     * longitude longitude in rad.
     */
    private double longitude;

    /**
     * standard_time difference from the UTM [hour].
     */
    private double standard_time;
    private Date endDate;
    private double E0;
    private double alpha;
    private int basinNum = -1;
    private String safePointReadPath;
    private String safePointWritePath;
    private SafePoint safePoint;
    private ArrayList<SimpleFeature> basinsFeatures;
    private String basinIdField;
    private double[] Abasin;

    private HashMap<Integer, Integer> basinid2BasinindexMap;
    private HashMap<Integer, Integer> basinindex2BasinidMap;
    private double deltaTinHours;
    private double[] SWEbasin;
    private double[] Pnet;
    private double[] Prn;
    private double[] Psn;
    private double[] averageTemperature;
    private double[][][] EI;
    private double[][][] A;
    private double[] rain;
    /*
     * Full adige vector data contains for every basin in the following order:
     * 1. net precipitation
     * 2. net radiation
     * 3. net short radiation
     * 4. temperature
     * 5. relative humidity
     * 6. wind speed
     * 7. air pressure
     */
    private double[] fullAdigeData;

    private int glacierId = -1;
    private String usoFieldString = null;
    private int usoFieldIndex = -1;
    private List<Integer> usoList;;

    public h_energybalance() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
        pm = new PrintStreamProgressMonitor(out);
    }

    public h_energybalance( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
        pm = new PrintStreamProgressMonitor(out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        safePointReadPath = null;
        safePointWritePath = null;
        safePoint = new SafePoint();
        String deltaTArg = null;
        String endDateArg = null;

        /*
         * input parameters
         */
        if (properties != null) {
            for( IArgument argument : properties ) {
                String key = argument.getKey();
                if (key.equals("train")) {
                    try {
                        train = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read train, using default value = " + train);
                    }
                }
                if (key.equals("tsnow")) {
                    try {
                        tsnow = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read tsnow, using default value = " + tsnow);
                    }
                }
                if (key.equals("infittiDt")) {
                    try {
                        infittimentoDt = Integer.parseInt(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read infittimentoDt, using default value = "
                                + infittimentoDt);
                    }
                }
                if (key.equals("rhosn")) {
                    try {
                        rho_sn = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read rho_sn, using default value = " + rho_sn);
                    }
                }
                if (key.equals("tolU0")) {
                    try {
                        tolerance_U0 = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read tolU0, using default value = " + tolerance_U0);
                    }
                }
                if (key.equals("tolW0")) {
                    try {
                        tolerance_U0 = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read tolW0, using default value = " + tolerance_U0);
                    }
                }
                if (key.equals("tolU")) {
                    try {
                        tolerance_U = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read tolU, using default value = " + tolerance_U);
                    }
                }
                if (key.equals("tolW")) {
                    try {
                        tolerance_W = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read tolW, using default value = " + tolerance_W);
                    }
                }
                if (key.equals("tolTs")) {
                    try {
                        tolerance_Ts = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read tolTs, using default value = " + tolerance_Ts);
                    }
                }
                if (key.equals("iterUW")) {
                    try {
                        num_iter_UW = Integer.parseInt(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read iterUW, using default value = " + num_iter_UW);
                    }
                }
                if (key.equals("iterTs")) {
                    try {
                        num_iter_Ts = Integer.parseInt(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read iterTs, using default value = " + num_iter_Ts);
                    }
                }
                if (key.equals("spreadpath")) {
                    try {
                        safePointReadPath = argument.getValue();
                    } catch (Exception e) {
                        out.println("Could not read spreadpath, using default value = "
                                + safePointReadPath);
                    }
                }
                if (key.equals("spwritepath")) {
                    try {
                        safePointWritePath = argument.getValue();
                    } catch (Exception e) {
                        out.println("Could not read spwritepath, using default value = "
                                + safePointWritePath);
                    }
                }
                if (key.equals("idfield")) {
                    try {
                        basinIdField = argument.getValue();
                    } catch (Exception e) {
                        out
                                .println("Could not read idfield, using default value = "
                                        + basinIdField);
                    }
                }
                if (key.equals("swestart")) {
                    try {
                        sweStartParam = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read swestart, using default value = "
                                + sweStartParam);
                    }
                }
                if (key.equals("canopy")) {
                    try {
                        canopyconst = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read canopy, using default value = " + canopyconst);
                    }
                }
                if (key.equals(glacierValue)) {
                    try {
                        glacierId = Integer.parseInt(argument.getValue());
                    } catch (Exception e) {
                        throw new ModelsIllegalargumentException("Could not read " + glacierValue
                                + ".", this);
                    }
                }
                if (key.equals("avo")) {
                    try {
                        snowReflVo = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out
                                .println("Could not read visible snow reflectivity, using default value = "
                                        + snowReflVo);
                    }
                }
                if (key.equals("airo")) {
                    try {
                        snowReflIR = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        out.println("Could not read IR snow reflectivity, using default value = "
                                + snowReflIR);
                    }
                }
                if (key.equals(usoField)) {
                    usoFieldString = argument.getValue();
                }
                if (key.equals(ModelsConstants.DELTAT)) {
                    deltaTArg = argument.getValue();
                }
                if (key.equals(ModelsConstants.ENDDATE)) {
                    endDateArg = argument.getValue();
                }
            }

        }

        if (deltaTArg == null) {
            throw new ModelsIllegalargumentException(
                    "The model was launched without time interval or something is wrong in the time setting.",
                    this);
        } else {
            try {
                double deltaTinMilliSeconds = Double.parseDouble(deltaTArg);
                deltaTinHours = deltaTinMilliSeconds / 1000 / 3600.0;
            } catch (Exception e) {
                throw new ModelsIllegalargumentException(
                        "No time interval has been defined for this model. Please check your settings or arguments.",
                        this);
            }
        }
        endDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
                Locale.getDefault()).parse(endDateArg);
        /*
         * define the I/O Exchange Items
         */
        basinsInputExchangeItem = ModelsConstants.createFeatureCollectionInputExchangeItem(this,
                null);
        rainInputExchangeItem = ModelsConstants.createDummyInputExchangeItem(this);
        temperatureInputExchangeItem = ModelsConstants.createDummyInputExchangeItem(this);
        windSpeedInputExchangeItem = ModelsConstants.createDummyInputExchangeItem(this);
        pressureInputExchangeItem = ModelsConstants.createDummyInputExchangeItem(this);
        relativeHumidityInputExchangeItem = ModelsConstants.createDummyInputExchangeItem(this);
        energyIndexInputExchangeItem = ModelsConstants.createDummyInputExchangeItem(this);
        areaHeigthEnergyInputExchangeItem = ModelsConstants.createDummyInputExchangeItem(this);

        dailyTempRangeInputExchangeItem = ModelsConstants.createDummyInputExchangeItem(this);
        montlyTempRangeInputExchangeItem = ModelsConstants.createDummyInputExchangeItem(this);

        snowWaterEquivalentOutputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
        netPrecipitationOutputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
        rainPrecipitationOtputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
        snowPrecipitationOtputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
        outputRainOtputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
        fullAdigeOutputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
        averageTemperatureOtputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
        mainOutputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        HydrologisDate tmpTime = null;
        if (time instanceof HydrologisDate) {
            tmpTime = (HydrologisDate) time;
        } else {
            throw new ModelsIllegalargumentException(
                    "The model was launched without time interval or something is wrong in the time setting.",
                    this);
        }

        boolean isLastTime = false;
        /*
         * check if the time is null or if it is the first implementation of the new timestep
         */
        if (currentTime == null || !(tmpTime.compareTo(currentTime) == 0)) {
            currentTime = new HydrologisDate();
            currentTime.setTime(tmpTime.getTime());
            if (currentTime == endDate) {
                isLastTime = true;
            }

            if (basinid2BasinindexMap == null) {
                // get basin features from feature link
                FeatureCollection<SimpleFeatureType, SimpleFeature> basinsFeatureCollection = ModelsConstants
                        .getFeatureCollectionFromLink(basinsInputLink, time, err);
                basinsFeatures = new ArrayList<SimpleFeature>();
                FeatureIterator<SimpleFeature> featureIterator = basinsFeatureCollection.features();

                basinNum = basinsFeatureCollection.size();
                SimpleFeatureType featureType = basinsFeatureCollection.getSchema();

                int basinIdFieldIndex = featureType.indexOf(basinIdField);
                if (basinIdFieldIndex == -1) {
                    throw new IllegalArgumentException(
                            "The field of the basin id couldn't be found in the supplied basin data.");
                }
                if (usoFieldString != null) {
                    usoFieldIndex = featureType.indexOf(usoFieldString);
                    if (usoFieldIndex == -1) {
                        throw new IllegalArgumentException(
                                "The field of the soil type (usofield) couldn't be found in the supplied basin data.");
                    }
                }
                basinid2BasinindexMap = new HashMap<Integer, Integer>();
                basinindex2BasinidMap = new HashMap<Integer, Integer>();

                pm.beginTask("Read basins data.", basinsFeatureCollection.size());
                int index = 0;
                Abasin = new double[basinNum];
                while( featureIterator.hasNext() ) {
                    pm.worked(1);
                    SimpleFeature feature = featureIterator.next();
                    basinsFeatures.add(feature);
                    basinid2BasinindexMap.put(((Number) feature.getAttribute(basinIdFieldIndex))
                            .intValue(), index);
                    basinindex2BasinidMap.put(index, ((Number) feature
                            .getAttribute(basinIdFieldIndex)).intValue());
                    Geometry basinGeometry = (Geometry) feature.getDefaultGeometry();
                    Abasin[index] = basinGeometry.getArea() / 1000000.0; // area in km2 as the input
                    // area for energetic and
                    // altimetric bands
                    index++;

                    // read land cover if requested
                    if (usoFieldIndex != -1) {
                        if (usoList == null) {
                            usoList = new ArrayList<Integer>();
                        }
                        int uso = ((Number) feature.getAttribute(usoFieldIndex)).intValue();
                        usoList.add(uso);
                    }

                }
                basinsFeatureCollection.close(featureIterator);
                pm.done();
            }
            // get rain from scalar link
            IValueSet rainValueSet = null;
            if (rainInputLink != null) {
                rainValueSet = rainInputLink.getSourceComponent().getValues(time,
                        rainInputLink.getID());
                if (rainValueSet instanceof ScalarSet) {
                    ScalarSet rainscalarSet = (ScalarSet) rainValueSet;
                    rain = new double[basinNum];
                    pm.beginTask("Read rain data.", rainValueSet.getCount() - 1);
                    for( int i = 1; i < rainValueSet.getCount(); i = i + 2 ) {
                        pm.worked(2);
                        int rId = rainscalarSet.get(i).intValue();
                        double rainValue = rainscalarSet.get(i + 1);
                        Integer index = basinid2BasinindexMap.get(rId);
                        if (index == null) {
                            basinid2BasinindexMap.remove(rId);
                            continue;
                        }
                        if (!JGrassConstants.isNovalue(rainValue)) {
                            rain[index] = rainValue;
                        } else {
                            rain[index] = 0.0;
                        }

                    }
                    pm.done();
                }
            }
            if (rainValueSet == null) {
                throw new ModelsIOException(
                        "There are no precipitations in input for the model, please check your syntax.",
                        this);
            }

            // get energy values from scalar link ([12][num_EI][basinNum]) 12 ==
            // 0,1,2,3,4,5,5,4,3,2,1,0 ones at the beginning of the simulation
            IValueSet energyIndexValueSet = null;
            if (EI == null) {
                if (energyIndexInputLink != null) {
                    energyIndexValueSet = energyIndexInputLink.getSourceComponent().getValues(time,
                            energyIndexInputLink.getID());
                    if (energyIndexValueSet instanceof ScalarSet) {
                        ScalarSet energyIndexScalarSet = (ScalarSet) energyIndexValueSet;
                        // get the number of element for each "line"
                        int energyIndexColumnNumber = energyIndexScalarSet.get(0).intValue();
                        // get the number of energy bands with the number of basins known
                        {
                            Integer index = null;
                            Integer previndex = null;
                            int ind = 0;
                            for( int i = 1; i < energyIndexValueSet.getCount(); i = i
                                    + energyIndexColumnNumber ) {
                                int tempId = energyIndexScalarSet.get(i).intValue();
                                index = basinid2BasinindexMap.get(tempId);
                                if (index != null) {
                                    if (previndex == null || previndex.equals(index)) {
                                        ind++;
                                        previndex = index;
                                    }
                                }
                            }
                            num_EI = ind / 6;
                        }
                        EI = new double[12][num_EI][basinNum];
                        pm.beginTask("Read energy index data.", energyIndexValueSet.getCount() - 1);

                        for( int i = 1; i < energyIndexValueSet.getCount(); i = i
                                + energyIndexColumnNumber ) {
                            int tempId = energyIndexScalarSet.get(i).intValue();
                            Integer index = basinid2BasinindexMap.get(tempId);
                            if (index == null) {
                                basinid2BasinindexMap.remove(tempId);
                                continue;
                            }
                            pm.worked(energyIndexColumnNumber);
                            for( int j = 0; j < num_EI; j++ ) {
                                for( int k = 0; k < 12; k++ ) {
                                    int findex = 5;
                                    if (k < 6) {
                                        EI[k][j][index] = energyIndexScalarSet.get(i + 3);
                                    } else {
                                        EI[k][j][index] = EI[findex][j][basinid2BasinindexMap
                                                .get(tempId)];
                                        findex = findex - 1;
                                    }

                                }
                            }
                        }
                    }
                    pm.done();
                }
                if (energyIndexValueSet == null) {
                    throw new ModelsIOException(
                            "There are no energy index in input for the model, please check your syntax.",
                            this);
                }
            }
            // get area bande fascie from scalar link ([num_ES][num_EI][basinNum]) ones at the
            // beginning of the simulation
            IValueSet areaHeigthEnergyValueSet = null;
            if (A == null) {
                if (areaHeigthEnergyInputLink != null) {
                    areaHeigthEnergyValueSet = areaHeigthEnergyInputLink.getSourceComponent()
                            .getValues(time, areaHeigthEnergyInputLink.getID());
                    if (areaHeigthEnergyValueSet instanceof ScalarSet) {
                        ScalarSet areaEigthEnergyScalarSet = (ScalarSet) areaHeigthEnergyValueSet;
                        // get the number of element for each "line"
                        int areaEightEnergyColumnNumber = areaEigthEnergyScalarSet.get(0)
                                .intValue();

                        // get the number of elevation bands with the number of basins and energy
                        // bands known
                        {
                            Integer index = null;
                            Integer previndex = null;
                            int ind = 0;
                            for( int i = 1; i < areaHeigthEnergyValueSet.getCount(); i = i
                                    + areaEightEnergyColumnNumber ) {
                                int tempId = areaEigthEnergyScalarSet.get(i).intValue();
                                index = basinid2BasinindexMap.get(tempId);
                                if (index != null) {
                                    if (previndex == null || previndex.equals(index)) {
                                        ind++;
                                        previndex = index;
                                    }
                                }
                            }
                            num_ES = ind / num_EI;
                        }
                        EI = new double[12][num_EI][basinNum];

                        A = new double[num_ES][num_EI][basinNum];
                        pm.beginTask("Read area per heigth and band data.",
                                areaEigthEnergyScalarSet.size() - 2);

                        HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> idbasinMap = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>();
                        for( int j = 1; j < areaEigthEnergyScalarSet.size(); ) {
                            Integer idBas = areaEigthEnergyScalarSet.get(j).intValue();
                            HashMap<Integer, HashMap<Integer, Double>> idfasceMap = idbasinMap
                                    .get(idBas);
                            if (idfasceMap == null) {
                                idfasceMap = new HashMap<Integer, HashMap<Integer, Double>>();
                                idbasinMap.put(idBas, idfasceMap);
                            }
                            j++;
                            Integer idFasc = areaEigthEnergyScalarSet.get(j).intValue();
                            HashMap<Integer, Double> idbandeMap = idfasceMap.get(idFasc);
                            if (idbandeMap == null) {
                                idbandeMap = new HashMap<Integer, Double>();
                                idfasceMap.put(idFasc, idbandeMap);
                            }

                            j++;
                            Integer idBand = areaEigthEnergyScalarSet.get(j).intValue();
                            j++;
                            Double value = areaEigthEnergyScalarSet.get(j);
                            j++;
                            idbandeMap.put(idBand, value);
                            pm.worked(4);
                        }
                        pm.done();

                        for( int i = 0; i < basinNum; i = i + 1 ) {
                            Integer index = basinindex2BasinidMap.get(i);
                            if (index == null) {
                                basinid2BasinindexMap.remove(i);
                                continue;
                            }
                            HashMap<Integer, HashMap<Integer, Double>> fasceMap = idbasinMap
                                    .get(index);

                            for( int j = 0; j < num_ES; j++ ) {
                                HashMap<Integer, Double> bandeMap = fasceMap.get(j);
                                for( int k = 0; k < num_EI; k++ ) {
                                    A[j][k][i] = bandeMap.get(k);
                                }
                            }
                        }
                    }
                }
                if (areaHeigthEnergyValueSet == null) {
                    throw new ModelsIOException(
                            "There are no area per energy and elevation bands in input for the model, please check your syntax.",
                            this);
                }
            }

            // get T (temperatures per basin per band) from scalar input link at each time step
            IValueSet temperatureValueSet = null;
            double[][] T = null;
            if (temperatureInputLink != null) {
                temperatureValueSet = temperatureInputLink.getSourceComponent().getValues(time,
                        temperatureInputLink.getID());
                if (temperatureValueSet instanceof ScalarSet) {
                    ScalarSet temperaturescalarSet = (ScalarSet) temperatureValueSet;
                    int allColumns = temperaturescalarSet.get(0).intValue();
                    // basinid, valeBand1, valueband2, valueband3,...
                    int perBasinColumns = num_ES + 1;
                    // num_ES = perBasinColumns - 1;
                    T = new double[basinNum][num_ES];
                    pm.beginTask("Read temperature data.", temperatureValueSet.getCount() - 1);

                    for( int i = 1; i < temperatureValueSet.getCount(); i = i + perBasinColumns ) {
                        pm.worked(perBasinColumns);
                        int tempId = temperaturescalarSet.get(i).intValue();
                        Integer index = basinid2BasinindexMap.get(tempId);
                        if (index != null) {
                            for( int j = 1; j < perBasinColumns; j++ ) {
                                T[index][j - 1] = temperaturescalarSet.get(i + j);
                            }
                        }
                    }
                }
                pm.done();
            }
            if (temperatureValueSet == null) {
                throw new ModelsIOException(
                        "There are no temperature in input for the model, please check your syntax.",
                        this);
            }

            // get V (wind speed per basin per band) from scalar link at each time step
            IValueSet windValueSet = null;
            double[][] V = null;
            if (windSpeedInputLink != null) {
                windValueSet = windSpeedInputLink.getSourceComponent().getValues(time,
                        windSpeedInputLink.getID());
                if (windValueSet instanceof ScalarSet) {
                    ScalarSet windScalarSet = (ScalarSet) windValueSet;
                    int windColumnNumber = windScalarSet.get(0).intValue();
                    // basinid, valeBand1, valueband2, valueband3,...
                    int perBasinColumns = num_ES + 1;

                    V = new double[basinNum][num_ES];
                    pm.beginTask("Read wind speed data.", windValueSet.getCount() - 1);

                    for( int i = 1; i < windValueSet.getCount(); i = i + perBasinColumns ) {
                        int tempId = windScalarSet.get(i).intValue();
                        pm.worked(perBasinColumns);
                        for( int j = 1; j < perBasinColumns; j++ ) {
                            Integer index = basinid2BasinindexMap.get(tempId);
                            if (index == null) {
                                basinid2BasinindexMap.remove(tempId);
                                continue;
                            }
                            V[index][j - 1] = windScalarSet.get(i + j);
                        }
                    }
                }
                pm.done();
            }
            if (windValueSet == null) {
                throw new ModelsIOException(
                        "There are no wind speeds in input for the model, please check your syntax.",
                        this);
            }

            // get P (pressure per basin per band) from scalar link at each time step
            IValueSet pressurValueSet = null;
            double[][] P = null;
            if (pressureInputLink != null) {
                pressurValueSet = pressureInputLink.getSourceComponent().getValues(time,
                        pressureInputLink.getID());
                if (pressurValueSet instanceof ScalarSet) {
                    ScalarSet pressureScalarSet = (ScalarSet) pressurValueSet;
                    int pressureColumnNumber = pressureScalarSet.get(0).intValue();
                    // basinid, valeBand1, valueband2, valueband3,...
                    int perBasinColumns = num_ES + 1;

                    P = new double[basinNum][num_ES];
                    pm.beginTask("Read pressure data.", pressurValueSet.getCount() - 1);

                    for( int i = 1; i < pressurValueSet.getCount(); i = i + perBasinColumns ) {
                        pm.worked(perBasinColumns);
                        int tempId = pressureScalarSet.get(i).intValue();
                        for( int j = 1; j < perBasinColumns; j++ ) {
                            Integer index = basinid2BasinindexMap.get(tempId);
                            if (index == null) {
                                basinid2BasinindexMap.remove(tempId);
                                continue;
                            }
                            P[index][j - 1] = pressureScalarSet.get(i + j);
                        }
                    }
                }
                pm.done();
            }
            if (pressurValueSet == null) {
                throw new ModelsIOException(
                        "There are no pressure in input for the model, please check your syntax.",
                        this);
            }

            // get RH (relative humidity per basin per band) from scalar link at each time step
            IValueSet relHumidValueSet = null;
            double[][] RH = null;
            if (relativeHumidityInputLink != null) {
                relHumidValueSet = relativeHumidityInputLink.getSourceComponent().getValues(time,
                        relativeHumidityInputLink.getID());
                if (relHumidValueSet instanceof ScalarSet) {
                    ScalarSet relHumidScalarSet = (ScalarSet) relHumidValueSet;
                    int relHumidColumnNumber = relHumidScalarSet.get(0).intValue();
                    // basinid, valeBand1, valueband2, valueband3,...
                    int perBasinColumns = num_ES + 1;

                    RH = new double[basinNum][num_ES];
                    pm.beginTask("Read relative umidity data.", relHumidValueSet.getCount() - 1);

                    for( int i = 1; i < relHumidValueSet.getCount(); i = i + perBasinColumns ) {
                        pm.worked(perBasinColumns);
                        int tempId = relHumidScalarSet.get(i).intValue();
                        for( int j = 1; j < perBasinColumns; j++ ) {
                            Integer index = basinid2BasinindexMap.get(tempId);
                            if (index == null) {
                                basinid2BasinindexMap.remove(tempId);
                                continue;
                            }
                            RH[index][j - 1] = relHumidScalarSet.get(i + j);
                        }
                    }
                }
                pm.done();
            }
            if (relHumidValueSet == null) {
                throw new ModelsIOException(
                        "There are no pressure in input for the model, please check your syntax.",
                        this);
            }

            // get dtday (daily temperature range per basin per band) from scalar link at each time
            // step
            IValueSet dtDayValueSet = null;
            double[][] DTd = null;
            if (dailyTempRangeInputLink != null) {
                dtDayValueSet = dailyTempRangeInputLink.getSourceComponent().getValues(time,
                        dailyTempRangeInputLink.getID());
                if (dtDayValueSet instanceof ScalarSet) {
                    ScalarSet dtDayScalarSet = (ScalarSet) dtDayValueSet;
                    int dtDayColumnNumber = dtDayScalarSet.get(0).intValue();
                    // basinid, valeBand1, valueband2, valueband3,...
                    int perBasinColumns = num_ES + 1;

                    DTd = new double[basinNum][num_ES];
                    pm
                            .beginTask("Read daily temperature range data.", dtDayValueSet
                                    .getCount() - 1);

                    for( int i = 1; i < dtDayValueSet.getCount(); i = i + perBasinColumns ) {
                        pm.worked(perBasinColumns);
                        int tempId = dtDayScalarSet.get(i).intValue();
                        for( int j = 1; j < perBasinColumns; j++ ) {
                            Integer index = basinid2BasinindexMap.get(tempId);
                            if (index == null) {
                                basinid2BasinindexMap.remove(tempId);
                                continue;
                            }
                            DTd[index][j - 1] = dtDayScalarSet.get(i + j);
                        }
                    }
                }
                pm.done();
            }
            if (dtDayValueSet == null) {
                throw new ModelsIOException(
                        "There are no daily temperature range in input for the model, please check your syntax.",
                        this);
            }

            // get dtmonth (monthly temperature range per basin per band) from scalar link at each
            // time step
            IValueSet dtMonthValueSet = null;
            double[][] DTm = null;
            if (montlyTempRangeInputLink != null) {
                dtMonthValueSet = montlyTempRangeInputLink.getSourceComponent().getValues(time,
                        montlyTempRangeInputLink.getID());
                if (dtMonthValueSet instanceof ScalarSet) {
                    ScalarSet dtMonthScalarSet = (ScalarSet) dtMonthValueSet;
                    int dtMonthColumnNumber = dtMonthScalarSet.get(0).intValue();
                    // basinid, valeBand1, valueband2, valueband3,...
                    int perBasinColumns = num_ES + 1;

                    DTm = new double[basinNum][num_ES];
                    pm.beginTask("Read monthly temperature range data.",
                            dtMonthValueSet.getCount() - 1);

                    for( int i = 1; i < dtMonthValueSet.getCount(); i = i + perBasinColumns ) {
                        pm.worked(perBasinColumns);
                        int tempId = dtMonthScalarSet.get(i).intValue();
                        for( int j = 1; j < perBasinColumns; j++ ) {
                            Integer index = basinid2BasinindexMap.get(tempId);
                            if (index != null) {
                                DTm[index][j - 1] = dtMonthScalarSet.get(i + j);
                            }
                        }
                    }
                }
                pm.done();
            }
            if (dtMonthValueSet == null) {
                throw new ModelsIOException(
                        "There are no monthly temperature range in input for the model, please check your syntax.",
                        this);
            }

            /*
             * set the current time: day, month and hour
             */
            myCalendar.setTime(currentTime);

            int currentMonth = myCalendar.get(Calendar.MONTH);
            int currentDay = myCalendar.get(Calendar.DAY_OF_MONTH);
            int currentHour = myCalendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = myCalendar.get(Calendar.MINUTE);
            double hour = currentHour + currentMinute / 60.0;
            System.out.println("ora: " + hour);

            if (SWEbasin == null) {
                SWEbasin = new double[2 * basinNum];
                Pnet = new double[2 * basinNum];
                Prn = new double[2 * basinNum];
                Psn = new double[2 * basinNum];
                averageTemperature = new double[2 * basinNum];
                fullAdigeData = new double[9 * basinNum];
            } else {
                Arrays.fill(SWEbasin, 0.0);
                Arrays.fill(Pnet, 0.0);
                Arrays.fill(Prn, 0.0);
                Arrays.fill(Psn, 0.0);
                Arrays.fill(averageTemperature, 0.0);
                Arrays.fill(fullAdigeData, 0.0);
            }
            /*
             * these have to be taken from initial values 
             */
            if (safePoint.SWE == null) {
                if (safePointReadPath != null) {
                    safePoint = getSafePointData();
                } else {
                    safePoint.SWE = new double[num_ES][num_EI][basinNum];
                    if (sweStartParam == -9999.0) {
                        sweStartParam = 0.0;
                    }
                    for( int i = 0; i < basinNum; i++ ) {
                        double sweTmp = sweStartParam;
                        if (usoList != null) {
                            int usoTmp = usoList.get(i);
                            if (usoTmp == glacierId) {
                                sweTmp = GLACIER_SWE;
                            }
                        }
                        for( int k = 0; k < num_ES; k++ ) {
                            for( int j = 0; j < num_EI; j++ ) {
                                safePoint.SWE[j][k][i] = sweTmp;
                            }
                        }
                    }
                    safePoint.U = new double[num_ES][num_EI][basinNum];
                    safePoint.SnAge = new double[num_ES][num_EI][basinNum];
                    safePoint.Ts = new double[num_ES][num_EI][basinNum];
                }
            }

            // this has to be taken from a file, scalarreader
            // TODO add the input canopyLink for the canopy height for each altimetric band
            /*
             * if there is no canopy input matrix for the model create an empty canopy matrix for each elevation band and for each basin
             */
            double[][] canopy = new double[num_ES][basinNum];
            for( int i = 0; i < canopy.length; i++ ) {
                for( int j = 0; j < canopy[0].length; j++ ) {
                    canopy[i][j] = canopyconst;
                }
            }
            checkParametersAndRunEnergyBalance(deltaTinHours, rain, T, V, P, RH, currentMonth,
                    currentDay, hour, Abasin, A, EI, DTd, DTm, canopy);
        }

        if (netPrecipitationLink != null && linkID.equals(netPrecipitationLink.getID())) {
            ScalarSet pnetScalarSet = new ScalarSet(Pnet);
            pnetScalarSet.add(0, (double) 2.0 * basinNum);
            return pnetScalarSet;
        } else if (rainPrecipitationLink != null && linkID.equals(rainPrecipitationLink.getID())) {
            ScalarSet prnScalarSet = new ScalarSet(Prn);
            prnScalarSet.add(0, (double) 2.0 * basinNum);
            return prnScalarSet;
        } else if (snowPrecipitationLink != null && linkID.equals(snowPrecipitationLink.getID())) {
            ScalarSet psnScalarSet = new ScalarSet(Psn);
            psnScalarSet.add(0, (double) 2.0 * basinNum);
            return psnScalarSet;
        } else if (snowWaterEquivalentLink != null
                && linkID.equals(snowWaterEquivalentLink.getID())) {
            ScalarSet sweScalarSet = new ScalarSet(SWEbasin);
            sweScalarSet.add(0, (double) 2.0 * basinNum);
            return sweScalarSet;
        } else if (outuptRainLink != null && linkID.equals(outuptRainLink.getID())) {
            ScalarSet outRainScalarSet = new ScalarSet(rain);
            outRainScalarSet.add(0, (double) 2.0 * basinNum);
            return outRainScalarSet;
        } else if (AverageTemperatureLink != null && linkID.equals(AverageTemperatureLink.getID())) {
            ScalarSet avgTempScalarSet = new ScalarSet(averageTemperature);
            avgTempScalarSet.add(0, (double) 2.0 * basinNum);
            return avgTempScalarSet;
        } else if (fullAdigeOutputLink != null && linkID.equals(fullAdigeOutputLink.getID())) {
            ScalarSet fullAdigeScalarSet = new ScalarSet(fullAdigeData);
            fullAdigeScalarSet.add(0, (double) 9.0 * basinNum);
            return fullAdigeScalarSet;
        } else if (mainOutputLink != null && linkID.equals(mainOutputLink.getID())) {
            ScalarSet mainScalarSet = new ScalarSet();
            mainScalarSet.add(5.0 * basinNum);

            int index = 0;
            for( int i = 0; i < Psn.length; i++ ) {
                // the id of the basin
                mainScalarSet.add(Psn[i]);
                i++;
                // the values
                mainScalarSet.add(Psn[i]);
                mainScalarSet.add(averageTemperature[i]);
                mainScalarSet.add(SWEbasin[i]);

                // rain is on its own
                mainScalarSet.add(rain[index]);
                index++;
            }

            return mainScalarSet;
        }

        return null;
    }
    private SafePoint getSafePointData() {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(safePointReadPath);
            in = new ObjectInputStream(fis);
            SafePoint readSafePoint = (SafePoint) in.readObject();
            in.close();
            return readSafePoint;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void writeSafePoint() {
        if (safePointWritePath != null) {
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            try {
                fos = new FileOutputStream(safePointWritePath);
                out = new ObjectOutputStream(fos);
                out.writeObject(safePoint);
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    /**
     * Method to check the input parameters.
     * 
     * <p>
     * Due to the huge amount of parameters, this method is used to do 
     * necessary checks and set default values. This is made so the initialize 
     * method doesn't get flooded.
     * </p> 
     *
     * @param dtData
     * @param rain
     * @param T matrix of temperatures of every altimetric band for every basin.[basin][altim. band]
     * @param V
     * @param P
     * @param RH
     * @param month
     * @param day
     * @param hour
     * @param Abasin area of the different basins (as defined through the features/geometries)
     * @param A area for altim and energetic bands. Coming from eicalculator.
     * @param EI energy index matrix, coming from eicalculator.
     * @param DTd daily temperature range. 
     * @param DTm monthly temperature range.
     * @param canopy
     */
    private void checkParametersAndRunEnergyBalance( double dtData, double[] rain, double[][] T,
            double[][] V, double[][] P, double[][] RH, double month, double day, double hour,
            double[] Abasin, double[][][] A, double[][][] EI, double[][] DTd, double[][] DTm,
            double[][] canopy ) {

        double Dt = (dtData / (double) infittimentoDt) * 3600.0;

        /*
         * some hardcoded variables
         */
        boolean hasNoStations = false;
        double zmes_T = 2.0; // quota misura temperatura,pressione e umidita'
        double zmes_U = 2.0; // quota misura velocita' vento [m]
        double z0T = 0.005; // [m] roughness length della temperatura
        double z0U = 0.05; // [m] roughness length del vento
        double K = FluidConstants.ka * FluidConstants.ka
                / ((log(zmes_U / z0U)) * (log(zmes_T / z0T)));
        double eps = 0.98; // emissivita' neve
        double Lc = 0.05; // ritenzione capillare
        double Ksat = 3.0; //5.55; // conducibilita' idraulica della neve a saturazione
        double Ks = 5.55E-5; // conducibilita' termica superficiale della neve
        double aep = 50.0; // albedo extinction parameter (kg/m2==mm)
        double rho_g = 1600; // densita' del suolo [kg/m3]
        double De = 0.4; // suolo termicamente attivo
        double C_g = 890.0; // capacita' termica del suolo [J/(kg K)]
        double albedo_land = 0.2;
        double Ts_min = -20.0;
        double Ts_max = 20.0;

        /*
         * check the input parameters and set the default values
         */
        if (train == -9999.0)
            train = 2.0;
        if (tsnow == -9999.0)
            tsnow = 0;
        if (rho_sn == -9999.0)
            rho_sn = 400;
        if (tolerance_U0 == -9999.0)
            tolerance_U0 = 1000;
        if (tolerance_W0 == -9999.0)
            tolerance_W0 = 10;
        if (tolerance_U == -9999.0)
            tolerance_U = 500;
        if (tolerance_W == -9999.0)
            tolerance_W = 5;
        if (tolerance_Ts == -9999.0)
            tolerance_Ts = 1;
        if (num_iter_UW == -9999)
            num_iter_UW = 10;
        if (num_iter_Ts == -9999)
            num_iter_Ts = 5;

        // TODO check parameters and add to the model parameter
        latitude = 46.6 * Math.PI / 180.0; // [rad]
        longitude = 10.88 * Math.PI / 180.0; // [rad]
        standard_time = -1.0; // differenza rispetto a UMT [h]

        /*
         * start calculations
         */
        sun(hour, day);

        for( int i = 0; i < basinNum; i++ ) {
            calculateEnergyBalance(i, month, hasNoStations, V[i], canopy, T[i], P[i], RH[i], rain,
                    train, tsnow, Dt, A, Abasin, EI, DTd[i], DTm[i], K, eps, Lc, rho_sn, Ksat,
                    rho_g, De, C_g, aep, albedo_land, Ks, Ts_min, Ts_max);
        }
    }

    /**
     * @param i index for the basins list.
     * @param month 
     * @param hasNoStations
     * @param windSpeed vettore della velocita' del vento sulle fasce altimetriche per il bacino considerato.
     * @param canopy 
     * @param T vettore della temperatura sulle fasce altimetriche per il bacino considerato.
     * @param P vettore della pressione sulle fasce altimetriche per il bacino considerato.
     * @param RH vettore dell'umidita' relativa sulle fasce altimetriche per il bacino considerato.
     * @param rain vettore della pioggia
     * @param Train 
     * @param Tsnow 
     * @param Dt 
     * @param A 
     * @param Abasin 
     * @param EI 
     * @param DTd 
     * @param DTm 
     * @param K 
     * @param eps snow emissivity.
     * @param Lc capillar ritention. 
     * @param rho_sn snow density [kg/m3]
     * @param Ksat conducibilita' idraulica della neve a saturazione [kg/(m2 s)].
     * @param rho_g densita' del suolo [kg/m3].
     * @param De suolo termicamente attivo [m].
     * @param C_g capacita' termica del suolo [J/(kg K)].
     * @param aep albedo extinction parameter (kg/m2==mm).
     * @param albedo_land 
     * @param Ks conducibilita' superficiale della neve [m/s].
     * @param Ts_min 
     * @param Ts_max 
     * @param SWE snow water equivalent della [banda altimetrica][banda energetica][bacino].
     * @param U 
     * @param SnAge snow age [banda altimetrica][banda energetica][bacino]
     * @param Ts surface temperature
     */
    private void calculateEnergyBalance( int i, double month, boolean hasNoStations,
            double[] windSpeed, double[][] canopy, double[] T, double[] P, double[] RH,
            double[] rain, double Train, double Tsnow, double Dt, double[][][] A, double[] Abasin,
            double[][][] EI, double[] DTd, double[] DTm, double K, double eps, double Lc,
            double rho_sn, double Ksat, double rho_g, double De, double C_g, double aep,
            double albedo_land, double Ks, double Ts_min, double Ts_max ) {

        double rho, cp, ea, Psnow, T_snow, Prain, T_rain, Qp, Pn;
        double[] tausn = new double[1];
        double[] Rsw = new double[1];
        double[] Rlwin = new double[1];
        double[] netRadiation = new double[1];
        double[] netShortRadiation = new double[1];
        double[] Wice = new double[1];
        double[] Tin = new double[1];
        double[] Fliq = new double[1];
        double Tsur, Se, H0, L0, R0, M0, U0, W0, H1, L1, R1, M1, U1, W1, U2, W2;
        int tol, cont;
        int[] conv = new int[1];

        double[][][] SWE = safePoint.SWE;
        double[][][] SnAge = safePoint.SnAge;
        double[][][] Ts = safePoint.Ts;
        double[][][] U = safePoint.U;

        /*
         * set first value to the id of the basin
         */
        Integer basinId = basinindex2BasinidMap.get(i);
        Pnet[2 * i] = basinId;
        SWEbasin[2 * i] = basinId;
        Prn[2 * i] = basinId;
        Psn[2 * i] = basinId;
        averageTemperature[2 * i] = basinId;
        int filecolumnnumber = 9;
        fullAdigeData[filecolumnnumber * i] = basinId;

        double sum = 0.0;

        // valori medi per bacino
        // creo un vettore contenente un valore di Snow Water Equivalent per
        // ogni bacino

        for( int j = 0; j < num_ES; j++ ) { // per tutte le BANDE
            // ALTIMETRICHE

            // riduzione della velocita' del vento dovuta alla canopy
            windSpeed[j] *= (1.0 - 0.8 * canopy[j][i]);

            // printf("\n j=%4ld T=%10.5f",j,V[j]);

            // densita' dell'aria (kg/m3)
            rho = 1.2922 * (FluidConstants.tk / (T[j] + FluidConstants.tk)) * (P[j] / 1013.25);

            // calore specifico a pressione costante (J/(kg K))
            cp = 1005.00 + (T[j] + 23.15) * (T[j] + 23.15) / 3364.0;

            // pressione di vapore dell'aria (mbar)
            // BEFORE ea = 0.01 * RH[j] * pvap(T[j]);
            ea = 0.01 * RH[j] * pVap(T[j], P[j]);

            // precipitazione liquida e solida
            // se la temperatura della fascia altimetrica è maggiore di Train
            // (parameters)
            // tutta la precipitazione è pioggia
            if (T[j] > Train) {
                Prain = rain[i];
                Psnow = 0.0;
                // se la temperatura della fascia altimetrica è compresa tra
                // Train e Tsnow
                // ci sarà una parte di pioggia e una di neve
                // si trascura l'effetto del vento
            } else if (T[j] <= Train && T[j] >= Tsnow) {
                Prain = rain[i] * (T[j] - Tsnow) / (Train - Tsnow);
                Psnow = rain[i] - Prain;
                // se la temperatura della fascia altimetrica è minore di Tsnow
                // tutto è neve
            } else {
                Prain = 0.0;
                Psnow = rain[i];
            }
            // temperatura della neve
            T_snow = T[j];
            // se la temperatura della neve e' maggiore dello zero assegno a
            // T_snow lo zero
            if (T_snow > FluidConstants.Tf)
                T_snow = FluidConstants.Tf;
            T_rain = T[j];
            // se la temperatura dell'acqua è minore dello zero assegno a T_rain
            // lo zero
            if (T_rain < FluidConstants.Tf)
                T_rain = FluidConstants.Tf;

            // calore trasportato dalla precipitazione
            Qp = (Psnow * FluidConstants.C_ice * T_snow + Prain
                    * (FluidConstants.Lf + FluidConstants.C_liq * T_rain))
                    / Dt; // Qp[W/m^2] P[kg/m^2] c[J/(kg*K)]
            // Lf[J/kg]

            for( int k = 0; k < num_EI; k++ ) { // per tutte le BANDE
                // ENERGETICHE

                // se il SWE della banda altimetrica, banda energetica del
                // bacino i o la prec
                // nevosa sono maggiori di zero
                if (SWE[j][k][i] > 0 || Psnow > 0) {

                    // calcolo contenuto di ghiaccio e temperatura della neve da
                    // U
                    calculateTemp(Wice, Tin, Fliq, 1.0E3 * U[j][k][i], SWE[j][k][i], rho_g, De, C_g);

                    // radiazione e albedo
                    tausn[0] = SnAge[j][k][i]; // età della neve
                    // adimensionale
                    // BEFORE radiation(parameters, EI[month][k][i],
                    // alpha, E0,
                    // Ts[j][k][i], Wice[0], T[j], ea, Psnow,
                    // DTd[j], DTm[j], tausn, Rsw, Rlwin);
                    calculateRadiation(EI[(int) month][k][i], Ts[j][k][i], Wice[0], T[j], ea, P[j],
                            Psnow, DTd[j], DTm[j], tausn, Rsw, Rlwin, Dt, aep, albedo_land,
                            netRadiation, netShortRadiation, snowReflVo, snowReflIR);

                    // double Dt, double aep, double albedo_land

                    SnAge[j][k][i] = tausn[0];

                    // riduzione della canopy sui flussi radiativi
                    Rsw[0] *= (1.0 - canopy[j][i]);
                    Rlwin[0] *= (1.0 - canopy[j][i]);

                    // PREDICTOR
                    // temperatura della superficie
                    Tsur = calculateSurfaceTemperature(conv, Ts[j][k][i], Tin[0], T[j], P[j],
                            windSpeed[j], ea, Rsw[0], Rlwin[0], Qp, rho, cp, canopy[j][i], K,
                            rho_sn, Ks, eps, Ts_min, Ts_max);

                    if (Tsur != Tsur || conv[0] != 1)
                        Tsur = T[j]; // controllo in caso di non
                    // convergenza
                    if (Tsur > FluidConstants.Tf)
                        Tsur = FluidConstants.Tf; // vedi appunti OK

                    // calore sensibile
                    H0 = K * windSpeed[j] * rho * cp * (T[j] - Tsur);

                    // calore latente
                    L0 = FluidConstants.Lv * K * windSpeed[j] * rho * 0.622
                            * (ea - pVap(Tsur, P[j])) / P[j];

                    // radiazione onde lunghe uscente
                    R0 = (1.0 - canopy[j][i]) * eps * FluidConstants.sigma
                            * pow(Tsur + FluidConstants.tk, 4.0);

                    // scioglimento
                    Se = (Fliq[0] / (1.0 - Fliq[0]) - Lc)
                            / (FluidConstants.rho_w / rho_sn - FluidConstants.rho_w
                                    / FluidConstants.rho_i - Lc);
                    if (Se < 0)
                        Se = 0.0;
                    M0 = Ksat * pow(Se, 3.0); // flusso di acqua uscente
                    if (Se != Se)
                        M0 = 0.0;
                    if (M0 * Dt > SWE[j][k][i] - Wice[0])
                        M0 = (SWE[j][k][i] - Wice[0]) / Dt;

                    // aggiornamento
                    W1 = SWE[j][k][i] + rain[i] + (L0 / FluidConstants.Lv - M0) * Dt;
                    U1 = U[j][k][i] + 1.0E-3
                            * (Rsw[0] + Rlwin[0] + Qp - FluidConstants.Lf * M0 - R0 + H0 + L0) * Dt;

                    U0 = U1; // aggiorno i parametri di U e W con i valori
                    // calcolati
                    W0 = W1; // di primo tentativo

                    // contatori e controlli
                    cont = 0;
                    tol = 0;

                    // CORRECTOR
                    do {
                        // calcolo Wice, Fliq e Tin da U1
                        calculateTemp(Wice, Tin, Fliq, 1.0E3 * U1, W1, rho_g, De, C_g);

                        // temperatura della superficie
                        Tsur = calculateSurfaceTemperature(conv, Tsur, Tin[0], T[j], P[j],
                                windSpeed[j], ea, Rsw[0], Rlwin[0], Qp, rho, cp, canopy[j][i], K,
                                rho_sn, Ks, eps, Ts_min, Ts_max);
                        if (Tsur != Tsur || conv[0] != 1)
                            Tsur = T[j]; // controllo in caso di non
                        // convergenza
                        if (Tsur > FluidConstants.Tf)
                            Tsur = FluidConstants.Tf;

                        // calore sensibile
                        H1 = K * windSpeed[j] * rho * cp * (T[j] - Tsur);

                        // calore latente
                        L1 = FluidConstants.Lv * K * windSpeed[j] * rho * 0.622
                                * (ea - pVap(Tsur, P[j])) / P[j];

                        // radiazione onde lunghe uscente
                        R1 = (1.0 - canopy[j][i]) * eps * FluidConstants.sigma
                                * pow(Tsur + FluidConstants.tk, 4.0);

                        // scioglimento
                        if (Fliq[0] == 1) {
                            M1 = W1 / Dt;
                            U2 = 0.0;
                            W2 = 0.0;
                            tol = 3;
                        } else {
                            Se = (Fliq[0] / (1.0 - Fliq[0]) - Lc)
                                    / (FluidConstants.rho_w / rho_sn - FluidConstants.rho_w
                                            / FluidConstants.rho_i - Lc);
                            if (Se < 0)
                                Se = 0.0;
                            M1 = Ksat * pow(Se, 3.0);
                            if (Se != Se)
                                M1 = 0.0;
                            if (M1 * Dt > W1 - Wice[0])
                                M1 = (W1 - Wice[0]) / Dt;

                            // aggiornamento
                            W2 = SWE[j][k][i]
                                    + rain[i]
                                    + (0.5 * (L0 / FluidConstants.Lv - M0) + 0.5 * (L1
                                            / FluidConstants.Lv - M1)) * Dt;
                            U2 = U[j][k][i]
                                    + 1.0E-3
                                    * (Rsw[0] + Rlwin[0] + Qp + 0.5
                                            * (-FluidConstants.Lf * M0 - R0 + H0 + L0) + 0.5 * (-FluidConstants.Lf
                                            * M1 - R1 + H1 + L1)) * Dt;

                            cont += 1;

                            // controllo convergenza
                            if (cont == 1 && abs(U2 - U1) < tolerance_U0
                                    && abs(W2 - W1) < tolerance_W0)
                                tol = 1;
                            if (cont > 1 && abs(U2 - U1) < tolerance_U
                                    && abs(W2 - W1) < tolerance_W)
                                tol = 2;
                            if (conv[0] != 1)
                                tol = 0; // se Tsur non converge, non va bene
                            // la soluzione

                            // aggiornamento
                            U1 = U2;
                            W1 = W2;
                        }

                    } while( tol == 0 && cont <= num_iter_UW );

                    // se non c'e' convergenza cerco una soluzione esplicita
                    // (approssimata)
                    if (tol == 0) { // solo frazione liquida: ho trovato i
                        // valori e aggiorno i dati
                        U[j][k][i] = U0;
                        SWE[j][k][i] = W0;
                        Ts[j][k][i] = Tsur;
                        Pn = M0 * Dt;
                    } else if (tol == 3) { // non ho convergenza e setto a zero
                        // tutto
                        U[j][k][i] = 0.0;
                        SWE[j][k][i] = 0.0;
                        Ts[j][k][i] = 0.0;
                        Pn = M1 * Dt;
                    } else { // frazione liquida e solida: ho trovato i
                        // valori e aggiorno i dati
                        U[j][k][i] = U1;
                        SWE[j][k][i] = W1;
                        Ts[j][k][i] = Tsur;
                        Pn = (0.5 * M0 + 0.5 * M1) * Dt;
                    }

                    // se tutto lo SWE e' liquido o se c'è troppo poco SWE
                    //metto tutto lo SWE nella Pn
                    if (1.0E3 * U[j][k][i] >= FluidConstants.Lf * SWE[j][k][i] || SWE[j][k][i] <= 0) {
                        if (SWE[j][k][i] < 0)
                            SWE[j][k][i] = 0.0;
                        Pn += SWE[j][k][i];
                        if (Pn < 0)
                            Pn = 0.0;
                        SWE[j][k][i] = 0.0;
                        U[j][k][i] = 0.0;
                    }
                } else {
                    Pn = Prain;

                    Tsur = T[j];
                    Ts[j][k][i] = Tsur;
                    tausn[0] = 0.0;
                    calculateRadiation(EI[(int) month][k][i], Ts[j][k][i], 0.0, T[j], ea, P[j],
                            Psnow, DTd[j], DTm[j], tausn, Rsw, Rlwin, Dt, aep, albedo_land,
                            netRadiation, netShortRadiation, snowReflVo, snowReflIR);
                    // for( m = 2; m <= 40; m++ ) {
                    // logg[m] = -1.0;
                    // }
                }
                safePoint.SWE[j][k][i] = SWE[j][k][i];
                safePoint.U[j][k][i] = U[j][k][i];
                safePoint.Ts[j][k][i] = Ts[j][k][i];
                safePoint.SnAge[j][k][i] = SnAge[j][k][i];

                // calcolo i valori medi per bacino di SWE, Pnet, Prain, Psnow
                SWEbasin[2 * i + 1] += SWE[j][k][i] * (A[j][k][i] / Abasin[i]);
                Pnet[2 * i + 1] += Pn * (A[j][k][i] / Abasin[i]);
                Prn[2 * i + 1] += Prain * (A[j][k][i] / Abasin[i]);
                Psn[2 * i + 1] += Psnow * (A[j][k][i] / Abasin[i]);
                fullAdigeData[filecolumnnumber * i + 1] = Pnet[2 * i + 1];
                fullAdigeData[filecolumnnumber * i + 2] += netRadiation[0]
                        * (A[j][k][i] / Abasin[i]);
                fullAdigeData[filecolumnnumber * i + 3] += netShortRadiation[0]
                        * (A[j][k][i] / Abasin[i]);
                fullAdigeData[filecolumnnumber * i + 8] += SWEbasin[2 * i + 1];
                // System.out.println("swe = " + fullAdigeData[8 * i + 8]);
            }
            averageTemperature[2 * i + 1] += T[j];
            fullAdigeData[filecolumnnumber * i + 5] += RH[j];
            fullAdigeData[filecolumnnumber * i + 6] += windSpeed[j];
            fullAdigeData[filecolumnnumber * i + 7] += P[j];

        }
        // System.out.println("rad media= " + fullAdigeData[8 * i + 2]);
        // System.out.println("short media= " + fullAdigeData[8 * i + 3]);
        averageTemperature[2 * i + 1] /= num_ES;
        fullAdigeData[filecolumnnumber * i + 4] = averageTemperature[2 * i + 1];
        fullAdigeData[filecolumnnumber * i + 5] /= num_ES;
        fullAdigeData[filecolumnnumber * i + 6] /= num_ES;
        fullAdigeData[filecolumnnumber * i + 7] /= num_ES;

    }
    private void calculateTemp( double[] Wice, double[] Tin, double[] Fliq, double U, double SWE,
            double rho_g, double De, double C_g ) {
        if (U <= 0) {
            Wice[0] = SWE;
            Tin[0] = U / (SWE * FluidConstants.C_ice + rho_g * De * C_g);
            Fliq[0] = 0.0;
        } else if (U > 0 && U <= FluidConstants.Lf * SWE) {
            Wice[0] = SWE - U / FluidConstants.Lf;
            Tin[0] = 0.0;
            Fliq[0] = (SWE - Wice[0]) / SWE;
        } else {
            Wice[0] = 0.0;
            Tin[0] = (U - FluidConstants.Lf * SWE)
                    / (rho_g * De * C_g + SWE * FluidConstants.C_liq);
            Fliq[0] = 1.0;
        }

    }

    private void calculateRadiation( double EI, double Ts, double Wice, double Ta, double ea,
            double P, double Psnow, double DTd, double DTm, double[] tausn, double[] Rsw,
            double[] Rlwin, double Dt, double aep, double albedo_land, double[] netRadiation,
            double[] netShortRadiation, double avo, double airo ) {

        double coszen, r1, r2, r3, fzen, fage, avd, avis, aird, anir, albedo, rr, AtmTrans;
        double eps_clsky, CF, eps;
        double bb = 2.0, cv = 0.2, cr = 0.5, a = 0.8, c = 2.4, b;
        double diff2glob;

        // COSINE OF ZENITHAL ANGLE
        coszen = EI * sin(alpha);

        // ALBEDO
        // effect snow surface temperature
        r1 = exp(5000.0 * (1.0 / (FluidConstants.Tf + FluidConstants.tk) - 1.0 / (Ts + FluidConstants.tk)));
        // effect melt and refreezing
        r2 = pow(r1, 10);
        if (r2 > 1.0)
            r2 = 1.0;
        // effect of dirt
        r3 = 0.03;
        // non-dimensional snow age: 10 mm of snow precipitation restore snow
        // age Dt(s)
        tausn[0] = (tausn[0] + (r1 + r2 + r3) * Dt * 1.0E-6) * (1.0 - Psnow / 10.0);
        if (tausn[0] < 0.0)
            tausn[0] = 0.0;
        // dipendence from solar angle
        if (coszen < 0.5) {
            fzen = 1.0 / bb * ((bb + 1.0) / (1.0 + 2.0 * bb * coszen) - 1.0);
        } else {
            fzen = 0.0;
        }
        // dipendence from snow age
        fage = tausn[0] / (1.0 + tausn[0]);
        // diffuse visible albedo
        avd = (1.0 - cv * fage) * avo;
        // global visible albedo
        avis = avd + 0.4 * fzen * (1.0 - avd);
        // diffuse near infared albedo
        aird = (1.0 - cr * fage) * airo;
        // global near infared albedo
        anir = aird + 0.4 * fzen * (1.0 - aird);
        // albedo is taken as average
        albedo = (avis + anir) / 2.0;
        // Linear transition from snow albedo to bare ground albedo
        if (Psnow == 0) {
            albedo = albedo_land;
        } else if (Wice < aep) {
            rr = (1.0 - Wice / aep) * exp(-Wice * 0.5 / aep);
            albedo = rr * albedo_land + (1.0 - rr) * albedo;
        }

        // NET SHORTWAVE RADIATION
        if (DTm < 0)
            DTm = 0.0;
        if (DTd < 0)
            DTd = 0.0;
        // ADDED
        a = 0.48 + 0.29 * (1013.25 / P) * sin(alpha);
        if (a > 1)
            a = 1.0;

        b = 0.036 * exp(-0.154 * DTm);
        AtmTrans = a * (1.0 - exp(-b * pow(DTd, c)));

        // ADDED
        // ratio diffuse to global radiation (Erbs et al., 1982)
        if (AtmTrans <= 0.22) {
            diff2glob = 1.0 - 0.09 * AtmTrans;
        } else if (AtmTrans > 0.22 && AtmTrans <= 0.80) {
            diff2glob = 0.9511 - 0.1604 * AtmTrans + 4.388 * pow(AtmTrans, 2.0) - 16.638
                    * pow(AtmTrans, 3.0) + 12.336 * pow(AtmTrans, 4.0);
        } else {
            diff2glob = 0.165;
        }
        // Rsw=direct+diffuse
        Rsw[0] = FluidConstants.Isc * E0 * AtmTrans * (1.0 - albedo)
                * ((1.0 - diff2glob) * coszen + diff2glob * sin(alpha));

        // INCOMING LONGWAVE RADIATION
        eps_clsky = 1.08 * (1.0 - exp(-pow(ea, (Ta + FluidConstants.tk) / 2016.0)));
        CF = 1 - AtmTrans / a;
        eps = (1.0 - CF) * eps_clsky + CF;
        Rlwin[0] = eps * FluidConstants.sigma * pow(Ta + FluidConstants.tk, 4.0);

        // net radiation
        netRadiation[0] = Rsw[0] + Rlwin[0] - albedo * Rsw[0] - eps * FluidConstants.sigma
                * pow(Ts + FluidConstants.tk, 4.0);
        // System.out.println("Albedo " + albedo);
        // System.out.println("Radiazione netta: " + netRadiation[0]);
        netShortRadiation[0] = (1 - albedo) * Rsw[0];
        // System.out.println("Net Short: " + netShortRadiation[0]);

    }

    /**
     * Calcola la temperatura della superficie della neve (C).
     * 
     * <p>
     * Risolve il bilancio di energia alla superficie linearizzando e
     * iterando, secondo il metodo di Tarboton.
     * </p>
     * 
     * @param conv
     * @param Ts temperatura della superficie nevosa di primo tentativo (o dell'istante precedente).
     * @param Tin temperatura della neve di primo tentativo (o dell'istante precedente).
     * @param Ta temperatura dell'aria.
     * @param P pressione (mbar).
     * @param V velocita' del vento (m/s).
     * @param ea pressione di vapore in aria (mbar).
     * @param Rsw
     * @param Rlwin
     * @param Qp
     * @param rho
     * @param cp
     * @param Fcanopy
     * @param K 
     * @param rho_sn 
     * @param Ks 
     * @param eps 
     * @param Ts_min 
     * @param Ts_max 
     * @return
     */
    private double calculateSurfaceTemperature( int[] conv, double Ts, double Tin, double Ta,
            double P, double V, double ea, double Rsw, double Rlwin, double Qp, double rho,
            double cp, double Fcanopy, double K, double rho_sn, double Ks, double eps,
            double Ts_min, double Ts_max ) {
        double a, b, Ts0;
        short cont;

        // coefficienti non dipendenti dalla temperatura della superficie
        a = Rsw + Rlwin + Qp + K * V * Ta * rho * cp + rho_sn * FluidConstants.C_ice * Ks * Tin
                + 0.622 * K * V * FluidConstants.Lv * rho * ea / P;
        b = rho_sn * FluidConstants.C_ice * Ks + K * V * rho * cp;
        cont = 0;

        do {
            Ts0 = Ts;
            Ts = (a - 0.622 * K * V * FluidConstants.Lv * rho * (pVap(Ts0, P) - Ts * dpVap(Ts0, P))
                    / P + 3.0 * Fcanopy * FluidConstants.sigma * eps
                    * pow(Ts0 + FluidConstants.tk, 4.0))
                    / (b + 0.622 * dpVap(Ts0, P) * K * V * FluidConstants.Lv * rho / P + 4.0
                            * Fcanopy * eps * FluidConstants.sigma
                            * pow(Ts0 + FluidConstants.tk, 3.0));
            cont += 1;
        } while( abs(Ts - Ts0) > tolerance_Ts && cont <= num_iter_Ts );

        // controlli
        if (abs(Ts - Ts0) > tolerance_Ts) {
            conv[0] = 0; // non converge
        } else {
            conv[0] = 1; // converge
        }
        if (Ts < Ts_min || Ts > Ts_max)
            conv[0] = -1; // fuori dai limiti di ammissibilita'

        Ts0 = Ts;
        return Ts0;
    }

    /**
     * calcola la pressione di vapore [mbar] a saturazione in dipendenza
     * dalla temperatura [gradi Celsius].
     * 
     * @param T
     * @param P
     * @return la pressione di vapore [mbar] a saturazione
     */
    private double pVap( double T, double P ) {
        double A = 6.1121 * (1.0007 + 3.46E-6 * P);
        double b = 17.502;
        double c = 240.97;
        double e = A * exp(b * T / (c + T));
        return e;
    }

    /**
     * Calcola la derivata della pressione di vapore [mbar] a saturazione
     * rispetto dalla temperatura [gradi Celsius].
     *  
     * @param T
     * @param P
     * @return derivata della pressione di vapore.
     */
    private double dpVap( double T, double P ) {
        double A = 6.1121 * (1.0007 + 3.46E-6 * P);
        double b = 17.502;
        double c = 240.97;
        double De = (A * exp(b * T / (c + T))) * (b / (c + T) - b * T / pow(c + T, 2.0));

        return De;
    }

    /**
     * @param hour
     * @param day
     * @param E0 earth-sun distance correction.
     * @param alpha solar height (complementar to zenith angle), [rad].
     */
    private void sun( double hour, double day ) {
        // standard latitude according to standard time
        double lst = standard_time * PI / 12.0;

        // correction sideral time
        double G = 2.0 * PI * (day - 1.0) / 365.0;
        double Et = 0.000075 + 0.001868 * cos(G) - 0.032077 * sin(G) - 0.014615 * cos(2 * G)
                - 0.04089 * sin(2 * G);

        // local time
        double lh = hour + (longitude - lst) / FluidConstants.omega + Et / FluidConstants.omega;

        // earth-sun distance correction
        E0 = 1.00011 + 0.034221 * cos(G) + 0.00128 * sin(G) + 0.000719 * cos(2 * G) + 0.000077
                * sin(2 * G);

        // solar declination
        double D = 0.006918 - 0.399912 * cos(G) + 0.070257 * sin(G) - 0.006758 * cos(2 * G)
                + 0.000907 * sin(2 * G) - 0.002697 * cos(3 * G) + 0.00148 * sin(3 * G);

        // Sunrise and sunset with respect to 12pm [hour]
        double Thr = (acos(-tan(D) * tan(latitude))) / FluidConstants.omega;

        if (lh >= 12 - Thr && lh <= 12 + Thr) {
            // alpha: solar height (complementar to zenith angle), [rad]
            alpha = asin(sin(latitude) * sin(D) + cos(latitude) * cos(D)
                    * cos(FluidConstants.omega * (12.0 - lh)));
        } else {
            alpha = 0.0;
        }
    }

    public void addLink( ILink link ) {
        String linkID = link.getID();

        if (linkID.equals(basinsId)) {
            basinsInputLink = link;
        } else if (linkID.equals(rainId)) {
            rainInputLink = link;
        } else if (linkID.equals(temperatureId)) {
            temperatureInputLink = link;
        } else if (linkID.equals(windSpeedId)) {
            windSpeedInputLink = link;
        } else if (linkID.equals(pressureId)) {
            pressureInputLink = link;
        } else if (linkID.equals(relativeHumidityId)) {
            relativeHumidityInputLink = link;
        } else if (linkID.equals(dailyTempRangeId)) {
            dailyTempRangeInputLink = link;
        } else if (linkID.equals(monthlyTempRangeId)) {
            montlyTempRangeInputLink = link;
        } else if (linkID.equals(energyIndexId)) {
            energyIndexInputLink = link;
        } else if (linkID.equals(areaHeigthEnergyIndexId)) {
            areaHeigthEnergyInputLink = link;
        } else if (linkID.equals(snowWaterEquivalentId)) {
            snowWaterEquivalentLink = link;
        } else if (linkID.equals(netPrecipitationId)) {
            netPrecipitationLink = link;
        } else if (linkID.equals(rainPrecipitationId)) {
            rainPrecipitationLink = link;
        } else if (linkID.equals(snowPrecipitationId)) {
            snowPrecipitationLink = link;
        } else if (linkID.equals(averageTemperatureId)) {
            AverageTemperatureLink = link;
        } else if (linkID.equals(outputRainId)) {
            outuptRainLink = link;
        } else if (linkID.equals(fullAdigeId)) {
            fullAdigeOutputLink = link;
        } else if (linkID.equals(mainId)) {
            mainOutputLink = link;
        }
    }

    public void finish() {
        writeSafePoint();
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {

        if (inputExchangeItemIndex == 0) {
            return basinsInputExchangeItem;
        } else if (inputExchangeItemIndex == 1) {
            return rainInputExchangeItem;
        } else if (inputExchangeItemIndex == 2) {
            return temperatureInputExchangeItem;
        } else if (inputExchangeItemIndex == 3) {
            return windSpeedInputExchangeItem;
        } else if (inputExchangeItemIndex == 4) {
            return pressureInputExchangeItem;
        } else if (inputExchangeItemIndex == 5) {
            return relativeHumidityInputExchangeItem;
        } else if (inputExchangeItemIndex == 6) {
            return energyIndexInputExchangeItem;
        } else if (inputExchangeItemIndex == 7) {
            return areaHeigthEnergyInputExchangeItem;
        } else if (inputExchangeItemIndex == 8) {
            return dailyTempRangeInputExchangeItem;
        } else if (inputExchangeItemIndex == 9) {
            return montlyTempRangeInputExchangeItem;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 10;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return snowWaterEquivalentOutputExchangeItem;
        } else if (outputExchangeItemIndex == 1) {
            return netPrecipitationOutputExchangeItem;
        } else if (outputExchangeItemIndex == 2) {
            return rainPrecipitationOtputExchangeItem;
        } else if (outputExchangeItemIndex == 3) {
            return snowPrecipitationOtputExchangeItem;
        } else if (outputExchangeItemIndex == 4) {
            return outputRainOtputExchangeItem;
        } else if (outputExchangeItemIndex == 5) {
            return averageTemperatureOtputExchangeItem;
        } else if (outputExchangeItemIndex == 6) {
            return fullAdigeOutputExchangeItem;
        } else if (outputExchangeItemIndex == 7) {
            return mainOutputExchangeItem;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 8;
    }

    public void removeLink( String linkId ) {
        if (linkId.equals(basinsId)) {
            basinsInputLink = null;
        } else if (linkId.equals(rainId)) {
            rainInputLink = null;
        } else if (linkId.equals(temperatureId)) {
            temperatureInputLink = null;
        } else if (linkId.equals(windSpeedId)) {
            windSpeedInputLink = null;
        } else if (linkId.equals(pressureId)) {
            pressureInputLink = null;
        } else if (linkId.equals(relativeHumidityId)) {
            relativeHumidityInputLink = null;
        } else if (linkId.equals(energyIndexId)) {
            energyIndexInputLink = null;
        } else if (linkId.equals(areaHeigthEnergyIndexId)) {
            areaHeigthEnergyInputLink = null;
        } else if (linkId.equals(snowWaterEquivalentId)) {
            snowWaterEquivalentLink = null;
        } else if (linkId.equals(netPrecipitationId)) {
            netPrecipitationLink = null;
        } else if (linkId.equals(rainPrecipitationId)) {
            rainPrecipitationLink = null;
        } else if (linkId.equals(snowPrecipitationId)) {
            snowPrecipitationLink = null;
        } else if (linkId.equals(dailyTempRangeId)) {
            dailyTempRangeInputLink = null;
        } else if (linkId.equals(monthlyTempRangeId)) {
            montlyTempRangeInputLink = null;
        } else if (linkId.equals(outputRainId)) {
            outuptRainLink = null;
        } else if (linkId.equals(averageTemperatureId)) {
            AverageTemperatureLink = null;
        } else if (linkId.equals(fullAdigeId)) {
            fullAdigeOutputLink = null;
        } else if (linkId.equals(mainId)) {
            mainOutputLink = null;
        }

    }
}
