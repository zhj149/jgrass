package eu.hydrologis.jgrass.models.r.mapcalc;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.openmi.standard.IArgument;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.openmi.OneInOneOutModelsBackbone;
import eu.hydrologis.openmi.JGrassRasterValueSet;

public class r_mapcalc extends OneInOneOutModelsBackbone {

    private JGrassRegion activeRegion;
    private String mapset;

    private double[] minMaxMeans;
    private String function;
    private String[] mapsArray;
    private File cellPathFile;

    public r_mapcalc() {
        super();
        modelParameters = "";
    }

    public r_mapcalc( PrintStream output, PrintStream error ) {
        super(output, error);
        modelParameters = "";
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
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
                if (key.compareTo("function") == 0) {
                    function = argument.getValue();
                }
            }

        }

        if (function == null || function.length() < 1) {
            throw new ModelsIllegalargumentException(
                    "The function is a mandatory argument. Check your syntax.", this);
        }

        /*
         * define the map path
         */

        String mapsetPath = grassDb + File.separator + location + File.separator + mapset;

        String activeRegionPath = mapsetPath + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        String cellPath = mapsetPath + File.separator + "cell";
        cellPathFile = new File(cellPath);
        File[] filesList = cellPathFile.listFiles();
        List<String> mapNames = new ArrayList<String>();
        for( File file : filesList ) {
            if (file.isFile()) {
                mapNames.add(file.getName());
            }
        }
        mapsArray = (String[]) mapNames.toArray(new String[mapNames.size()]);

        inputEI = ModelsConstants.createDummyInputExchangeItem(this);
        outputEI = ModelsConstants.createDummyOutputExchangeItem(this);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        MapcalcJiffler jiffler = new MapcalcJiffler(function, null, mapsArray, activeRegion,
                cellPathFile.getAbsolutePath(), out);
        String exec = jiffler.exec();
        if (exec != null) {
            err.println(exec);
        }

        return null;
    }
}
