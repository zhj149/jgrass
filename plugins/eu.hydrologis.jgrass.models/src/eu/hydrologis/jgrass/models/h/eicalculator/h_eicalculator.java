package eu.hydrologis.jgrass.models.h.eicalculator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.geotools.coverage.grid.GridCoverage2D;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.StringSet;

public class h_eicalculator extends ModelsBackbone {
    private final static String modelParameters = "usage...";
 
    // idbasin=0, elev=1, curvature=2, aspect=3, slopes=4
    public static String idbasinID = "idbasin";
    public static String elevID = "elev";
    public static String curvatureID = "curvature";
    public static String aspectID = "aspect";
    public static String slopesID = "slopes";

    // altimetry=0, energy=1, area=2
    public static String outAltimetricID = "altimetry";
    public static String outEnergeticID = "energy";
    public static String outAreaID = "area";

    private ILink idbasinLink = null;
    private ILink elevLink = null;
    private ILink curvatureLink = null;
    private ILink aspectLink = null;
    private ILink slopesLink = null;
    private ILink outAltimetricLink = null;
    private ILink outEnergeticLink = null;
    private ILink outAreaLink = null;

    private IInputExchangeItem idbasinInputExchangeItem = null;
    private IInputExchangeItem elevInputExchangeItem = null;
    private IInputExchangeItem curvatureInputExchangeItem = null;
    private IInputExchangeItem aspectInputExchangeItem = null;
    private IInputExchangeItem slopesInputExchangeItem = null;
    private IOutputExchangeItem outAltimetricOutputExchangeItem = null;
    private IOutputExchangeItem outEnergeticOutputExchangeItem = null;
    private IOutputExchangeItem outAreaOutputExchangeItem = null;

    private int numEs = -1;
    private int numEi = -1;
    private double dtData = -1;
    private double latitude = -1;

    private JGrassRegion activeRegion;

    private boolean doSql = false;

    private boolean alreadyCalculated = false;

    private IValueSet altimValueSet;
    private IValueSet energValueSet;
    private IValueSet areaValueSet;

    public h_eicalculator() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_eicalculator( PrintStream output, PrintStream error ) {
        super();
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
                if (key.compareTo("numes") == 0) { //$NON-NLS-1$
                    try {
                        numEs = Integer.parseInt(argument.getValue());
                    } catch (Exception e) {
                        // this is checked later
                    }
                }
                if (key.compareTo("numei") == 0) { //$NON-NLS-1$
                    try {
                        numEi = Integer.parseInt(argument.getValue());
                    } catch (Exception e) {
                        // this is checked later
                    }
                }
                if (key.compareTo("dtdata") == 0) { //$NON-NLS-1$
                    try {
                        dtData = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        // this is checked later
                    }
                }
                if (key.compareTo("latitude") == 0) { //$NON-NLS-1$
                    latitude = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("dosql") == 0) { //$NON-NLS-1$
                    doSql = new Boolean(argument.getValue());
                }
            }
        }
        // int p_num_ES, int p_num_EI, double p_Dt_data,
        // double p_latitude_deg
        if (numEs == -1) {
            numEs = 5;
            err
                    .println("The numes parameters wasn't properly supplied, going on with default value: "
                            + numEs);
        }
        if (numEi == -1) {
            numEi = 5;
            err
                    .println("The numei parameters wasn't properly supplied, going on with default value: "
                            + numEi);
        }
        if (dtData == -1) {
            dtData = 1.0;
            err
                    .println("The dtdata parameters wasn't properly supplied, going on with default value: "
                            + dtData);
        }
        if (latitude == -1) {
            err
                    .println("The latitude parameters wasn't properly supplied, but is mandatory. Exiting...");
            throw new IllegalArgumentException();
        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.eicalculator"; //$NON-NLS-1$
        componentId = null;

        idbasinInputExchangeItem = ModelsConstants
                .createRasterInputExchangeItem(this, activeRegion);
        elevInputExchangeItem = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        curvatureInputExchangeItem = ModelsConstants.createRasterInputExchangeItem(this,
                activeRegion);
        aspectInputExchangeItem = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        slopesInputExchangeItem = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        outAltimetricOutputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
        outEnergeticOutputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);
        outAreaOutputExchangeItem = ModelsConstants.createDummyOutputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (!alreadyCalculated) {
            GridCoverage2D idbasinRasterData = ModelsConstants.getGridCoverage2DFromLink(idbasinLink, time,
                    err);
            GridCoverage2D elevRasterData = ModelsConstants.getGridCoverage2DFromLink(elevLink, time, err);
            GridCoverage2D curvRasterData = ModelsConstants.getGridCoverage2DFromLink(curvatureLink, time,
                    err);
            GridCoverage2D aspRasterData = ModelsConstants.getGridCoverage2DFromLink(aspectLink, time, err);
            GridCoverage2D slopeRasterData = ModelsConstants.getGridCoverage2DFromLink(slopesLink, time,
                    err);

            EnergyIndexCalculator eiCalculator = new EnergyIndexCalculator(numEs, numEi, dtData,
                    latitude, activeRegion, idbasinRasterData, elevRasterData, curvRasterData,
                    aspRasterData, slopeRasterData, out);
            eiCalculator.execute();

            if (doSql) {
                altimValueSet = new StringSet();
                energValueSet = new StringSet();
                areaValueSet = new StringSet();
                eiCalculator.fillSqlResults((StringSet) altimValueSet, (StringSet) energValueSet,
                        (StringSet) areaValueSet);
            } else {
                altimValueSet = new ScalarSet();
                energValueSet = new ScalarSet();
                areaValueSet = new ScalarSet();
                eiCalculator.fillScalarValuesResults((ScalarSet) altimValueSet,
                        (ScalarSet) energValueSet, (ScalarSet) areaValueSet);
            }

            alreadyCalculated = true;
        }

        if (linkID.equals(outAltimetricLink.getID())) {
            return altimValueSet;
        } else if (linkID.equals(outEnergeticLink.getID())) {
            return energValueSet;
        } else if (linkID.equals(outAreaLink.getID())) {
            return areaValueSet;
        } else {
            throw new IOException("Wrong link called this component. Check your syntax.");
        }

    }

    public void addLink( ILink link ) {
        String linkID = link.getID();
        if (linkID.equals(idbasinID)) {
            idbasinLink = link;
        } else if (linkID.equals(elevID)) {
            elevLink = link;
        } else if (linkID.equals(curvatureID)) {
            curvatureLink = link;
        } else if (linkID.equals(aspectID)) {
            aspectLink = link;
        } else if (linkID.equals(slopesID)) {
            slopesLink = link;
        } else if (linkID.equals(outAltimetricID)) {
            outAltimetricLink = link;
        } else if (linkID.equals(outEnergeticID)) {
            outEnergeticLink = link;
        } else if (linkID.equals(outAreaID)) {
            outAreaLink = link;
        }
    }

    public void finish() {
    }

    @Override
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        // idbasin=0, elev=1, curvature=2, aspect=3, slopes=4
        if (inputExchangeItemIndex == 0) {
            return idbasinInputExchangeItem;
        } else if (inputExchangeItemIndex == 1) {
            return elevInputExchangeItem;
        } else if (inputExchangeItemIndex == 2) {
            return curvatureInputExchangeItem;
        } else if (inputExchangeItemIndex == 3) {
            return aspectInputExchangeItem;
        } else if (inputExchangeItemIndex == 4) {
            return slopesInputExchangeItem;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 5;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return outAltimetricOutputExchangeItem;
        } else if (outputExchangeItemIndex == 1) {
            return outEnergeticOutputExchangeItem;
        } else if (outputExchangeItemIndex == 2) {
            return outAreaOutputExchangeItem;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 3;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(idbasinID)) {
            idbasinLink = null;
        } else if (linkID.equals(elevID)) {
            elevLink = null;
        } else if (linkID.equals(curvatureID)) {
            curvatureLink = null;
        } else if (linkID.equals(aspectID)) {
            aspectLink = null;
        } else if (linkID.equals(slopesID)) {
            slopesLink = null;
        } else if (linkID.equals(outAltimetricID)) {
            outAltimetricLink = null;
        } else if (linkID.equals(outEnergeticID)) {
            outEnergeticLink = null;
        } else if (linkID.equals(outAreaID)) {
            outAreaLink = null;
        }
    }

}
