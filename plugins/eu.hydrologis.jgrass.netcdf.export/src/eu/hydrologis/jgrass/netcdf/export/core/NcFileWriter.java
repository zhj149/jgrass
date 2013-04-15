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
package eu.hydrologis.jgrass.netcdf.export.core;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.IProgressMonitorJGrass;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * Class that takes care of writing netcdf files from GRASS rasters.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class NcFileWriter {
    private static final String LATITUDE_MAX = "latitude_max";
    private static final String LATITUDE_MIN = "latitude_min";
    private static final String LONGITUDE_MAX = "longitude_max";
    private static final String LONGITUDE_MIN = "longitude_min";
    public static final String DEGREES_EAST = "degrees_east";
    public static final String DEGREES_NORTH = "degrees_north";
    public static final String TIME = "time";
    public static final String LEVEL = "level";
    public static final String UNITS = "units";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lon";

    public static final SimpleDateFormat dF = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private String outputNcPath;

    private List<NcVariable> ncVariablesList = new ArrayList<NcVariable>();

    private boolean fileHasTime = false;
    private boolean fileHasLevel = false;

    private List<String> datesList;

    private double[] levels;

    private Dimension lvlDim;
    private Dimension timeDim;
    private Dimension lonDim;
    private Dimension latDim;
    private Date startDate;
    private int dtSeconds;
    private final String mapsetPath;
    private JGrassRegion activeRegion;
    private CoordinateReferenceSystem locationCrs;
    private LinkedHashMap<String, Object> globalAttributes;

    /**
     * Constructor for the writer.
     * 
     * @param outputNcPath the path of the output netcdf file.
     * @param mapset the mapset from which the rasters will be taken to create the netcdf.
     * @param startDateString the start date string in format [yyyy-MM-DD HH:MM].
     *                  Can be null if no time is in the game.
     * @param endDateString the end date string in format [yyyy-MM-DD HH:MM]
     *                  Can be null if no time is in the game.
     * @param dtMinutesString the string defining the timestep in minutes.
     *                  Can be null if no time is in the game.
     * @param levelsString the comma separated string of elevation levels. Have to
     *                  be double values. Can be null.
     * @throws IllegalArgumentException
     */
    public NcFileWriter( String outputNcPath, String mapset, String startDateString, String endDateString,
            String dtMinutesString, String levelsString ) throws IllegalArgumentException {
        this.outputNcPath = outputNcPath;
        this.mapsetPath = mapset;

        File mapsetFile = new File(mapset);
        File windFile = new File(mapset + File.separator + JGrassConstants.WIND);
        if (!windFile.exists()) {
            throw new IllegalArgumentException("This doesn't seem to be a valid WIND file: " + windFile.getAbsolutePath());
        }

        try {
            activeRegion = new JGrassRegion(windFile.getAbsolutePath());
        } catch (IOException e1) {
            throw new IllegalArgumentException("This doesn't seem to be a valid WIND file: " + windFile.getAbsolutePath());
        }

        File locationFile = mapsetFile.getParentFile();
        locationCrs = JGrassCatalogUtilities.getLocationCrs(locationFile.getAbsolutePath());

        /*
         * check the time and level parameters
         */
        if (startDateString != null && endDateString != null && dtMinutesString != null) {

            try {
                startDate = dF.parse(startDateString);
                Date endDate = dF.parse(endDateString);
                int dtMinutes = Integer.parseInt(dtMinutesString);
                dtSeconds = dtMinutes * 60;

                datesList = new ArrayList<String>();
                Date running = new Date(startDate.getTime());
                while( endDate.after(running) ) {
                    String date = dF.format(running);
                    datesList.add(date);
                    running.setTime(running.getTime() + dtSeconds * 1000l);
                }
                datesList.add(endDateString);

            } catch (Exception e) {
                throw new IllegalArgumentException("An error occurred while parsing the time definitions.", e);
            }
            fileHasTime = true;
        }
        if (levelsString != null) {
            try {
                String[] split = levelsString.split(",");
                levels = new double[split.length];
                for( int i = 0; i < levels.length; i++ ) {
                    levels[i] = Double.parseDouble(split[i].trim());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("An error occurred while parsing the level definitions.", e);
            }
            fileHasLevel = true;
        }

    }

    /**
     * Adds a new variable definition to the file.
     * 
     * @param name name of the variable.
     * @param description description of the variable.
     * @param units the units definition string for the variable.
     * @param hasTime true if the variable will have timesteps.
     * @param hasLevel true if the variable will have different levels.
     * @return the created variable, to which then the data have to be added.
     */
    public NcVariable addVariable( String name, String description, String units, boolean hasTime, boolean hasLevel ) {
        NcVariable ncVar = new NcVariable(this, name, description, hasTime, hasLevel);
        ncVariablesList.add(ncVar);
        return ncVar;
    }

    public void addVariable( NcVariable ncVar ) {
        ncVariablesList.add(ncVar);
    }

    /**
     * Adds a global attribute to the new netcdf dataset.
     * 
     * @param globalAttributes a {@link LinkedHashMap map} of attributes definitions.
     */
    public void addGlobalAttributes( LinkedHashMap<String, Object> globalAttributes ) {
        this.globalAttributes = globalAttributes;
    }

    /**
     * Removes a variable from the list of variables to be written.
     * 
     * @param ncVar the variable to remove.
     */
    public void removeVariable( NcVariable ncVar ) {
        ncVariablesList.remove(ncVar);
    }

    /**
     * Getter for levels.
     * 
     * @return the levels array.
     */
    public double[] getLevels() {
        return levels;
    }

    /**
     * Getter for the dates list.
     * 
     * @return the list of dates formatted as strings.
     */
    public List<String> getDatesList() {
        return datesList;
    }

    public boolean isFileHasLevel() {
        return fileHasLevel;
    }

    public boolean isFileHasTime() {
        return fileHasTime;
    }

    /**
     * Get the list of available dimensions.
     * 
     * <p>
     * The list is in the following order: lat, long,time, level.
     * </p>
     * 
     * @param withTime adds the time dimension, if available.
     * @param withLevel adds the levels dimension, if available.
     * @return the list of available dimensions.
     */
    public List<Dimension> getDimensionsList( boolean withTime, boolean withLevel ) {
        ArrayList<Dimension> dims = new ArrayList<Dimension>();
        if (withTime && timeDim != null) {
            dims.add(timeDim);
        }
        if (withLevel && lvlDim != null) {
            dims.add(lvlDim);
        }
        dims.add(latDim);
        dims.add(lonDim);
        return dims;
    }

    public JGrassRegion getActiveRegion() {
        return activeRegion;
    }

    public CoordinateReferenceSystem getLocationCrs() {
        return locationCrs;
    }

    public String getMapsetPath() {
        return mapsetPath;
    }

    /**
     * Write the dataset to file.
     * 
     * @throws IOException 
     */
    public void writeNcDataset( IProgressMonitorJGrass pm ) throws IOException {
        NetcdfFileWriteable ncDataFile = null;
        try {
            pm.beginTask("Creating file structure.", IProgressMonitorJGrass.UNKNOWN);

            // Create new netcdf-3 file with the given filename
            ncDataFile = NetcdfFileWriteable.createNew(outputNcPath, false);

            JGrassRegion latlongJGrassRegion = activeRegion.reproject(locationCrs, DefaultGeographicCRS.WGS84, true);

            /*
             * addd user global attributes and define default global 
             * attributes
             */
            if (globalAttributes != null) {
                Set<String> keySet = globalAttributes.keySet();
                for( String key : keySet ) {
                    Object value = globalAttributes.get(key);
                    if (value instanceof Number) {
                        ncDataFile.addGlobalAttribute(key, (Number) value);
                    } else if (value instanceof String) {
                        ncDataFile.addGlobalAttribute(key, (String) value);
                    } else {
                        // not defined, jump over it
                        continue;
                    }
                }
            }
            ncDataFile.addGlobalAttribute(LONGITUDE_MIN, latlongJGrassRegion.getWest());
            ncDataFile.addGlobalAttribute(LONGITUDE_MAX, latlongJGrassRegion.getEast());
            ncDataFile.addGlobalAttribute(LATITUDE_MIN, latlongJGrassRegion.getSouth());
            ncDataFile.addGlobalAttribute(LATITUDE_MAX, latlongJGrassRegion.getNorth());

            pm.done();

            /*
             * create crs axes and their values
             */
            pm.beginTask("Creating coordinate axes.", IProgressMonitorJGrass.UNKNOWN);

            /*
             * create levels and time dimensions if available: CF says: time,depth,lat,lon
             */
            ArrayDouble.D1 timeData = null;
            if (datesList != null) {
                int num = datesList.size();
                timeDim = ncDataFile.addDimension(TIME, num);
                ncDataFile.addVariable(TIME, DataType.DOUBLE, new Dimension[]{timeDim});
                // Define units attributes for data variables.
                ncDataFile.addVariableAttribute(TIME, UNITS, "seconds since 1970-01-01 00:00:00 UTC");
                // fill data
                timeData = new ArrayDouble.D1(timeDim.getLength());
                Date runningDate = new Date(startDate.getTime());
                Calendar cal = Calendar.getInstance();
                for( int i = 0; i < timeDim.getLength(); i++ ) {
                    cal.setTime(runningDate);
                    timeData.set(i, (double) cal.getTimeInMillis() / 1000l);
                    runningDate.setTime(runningDate.getTime() + (long) dtSeconds * 1000l);
                }
            }

            ArrayDouble.D1 levelsData = null;
            if (levels != null) {
                int num = levels.length;
                lvlDim = ncDataFile.addDimension(LEVEL, num);
                ncDataFile.addVariable(LEVEL, DataType.DOUBLE, new Dimension[]{lvlDim});
                // Define units attributes for data variables.
                ncDataFile.addVariableAttribute(LEVEL, UNITS, "m");
                // fill data
                levelsData = new ArrayDouble.D1(lvlDim.getLength());
                for( int i = 0; i < lvlDim.getLength(); i++ ) {
                    levelsData.set(i, levels[i]);
                }
            }

            int rows = activeRegion.getRows();
            latDim = ncDataFile.addDimension(LATITUDE, rows);
            int cols = activeRegion.getCols();
            lonDim = ncDataFile.addDimension(LONGITUDE, cols);
            // Define the coordinate variables.
            Variable latVariable = ncDataFile.addVariable(LATITUDE, DataType.DOUBLE, new Dimension[]{latDim});
            Variable lonVariable = ncDataFile.addVariable(LONGITUDE, DataType.DOUBLE, new Dimension[]{lonDim});
            // Define units attributes for data variables.
            ncDataFile.addVariableAttribute(LATITUDE, UNITS, DEGREES_NORTH);
            ncDataFile.addVariableAttribute(LONGITUDE, UNITS, DEGREES_EAST);
            ArrayDouble.D1 lats = new ArrayDouble.D1(latDim.getLength());
            ArrayDouble.D1 lons = new ArrayDouble.D1(lonDim.getLength());
            double deltaX = (latlongJGrassRegion.getEast() - latlongJGrassRegion.getWest()) / (double) cols;
            double deltaY = (latlongJGrassRegion.getNorth() - latlongJGrassRegion.getSouth()) / (double) rows;
            double runningX = latlongJGrassRegion.getWest();
            for( int j = 0; j < lonDim.getLength(); j++ ) {
                lons.set(j, runningX);
                runningX = runningX + deltaX;
            }
            double runningY = (double) latlongJGrassRegion.getNorth();
            for( int i = 0; i < latDim.getLength(); i++ ) {
                lats.set(i, runningY);
                runningY = runningY - deltaY;
            }
            // double runningY = (double) latlongJGrassRegion.getSouth();
            // for( int i = 0; i < latDim.getLength(); i++ ) {
            // lats.set(i, runningY);
            // runningY = runningY + deltaY;
            // }

            Attribute att = new Attribute("standard_name", "latitude");
            latVariable.addAttribute(att);
            att = new Attribute("long_name", "latitude");
            latVariable.addAttribute(att);
            att = new Attribute("units", "degrees_north");
            latVariable.addAttribute(att);
            att = new Attribute("unit_long", "Degrees North");
            latVariable.addAttribute(att);
            att = new Attribute("axis", "y");
            latVariable.addAttribute(att);

            att = new Attribute("standard_name", "longitude");
            lonVariable.addAttribute(att);
            att = new Attribute("long_name", "longitude");
            lonVariable.addAttribute(att);
            att = new Attribute("units", "degrees_east");
            lonVariable.addAttribute(att);
            att = new Attribute("unit_long", "Degrees East");
            lonVariable.addAttribute(att);
            att = new Attribute("axis", "x");
            lonVariable.addAttribute(att);

            /*
             * now deal with variables
             */
            for( NcVariable ncVariable : ncVariablesList ) {
                ncVariable.defineData(ncDataFile);
            }

            pm.done();

            /*
             * beyond this point the define mode is closed,
             * things can only be written to file now.
             */
            // Create the file. At this point the (empty) file will be written to disk
            ncDataFile.create();
            if (timeData != null)
                ncDataFile.write(TIME, timeData);
            if (levelsData != null)
                ncDataFile.write(LEVEL, levelsData);
            ncDataFile.write(LATITUDE, lats);
            ncDataFile.write(LONGITUDE, lons);

            for( NcVariable ncVariable : ncVariablesList ) {
                ncVariable.writeData(pm);
            }
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            if (ncDataFile != null)
                ncDataFile.close();
        }

    }

    public static void main( String[] args ) {

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out);

        String ncPath = "/home/moovida/TMP/test2.nc";
        // String mapset = "/home/moovida/data/hydrocareworkspace/grassdb/utm32n_etrf89/aidi/";
        String mapset = "/home/moovida/data/hydrocareworkspace/grassdb/utm32n_etrf89/canali";
        String startDateString = "2009-01-01 00:00";
        String endDateString = "2009-01-01 01:00";
        String dtMinutesString = "60";
        String levelsString = "0.0,100.0";
        NcFileWriter ncFW = new NcFileWriter(ncPath, mapset, startDateString, endDateString, dtMinutesString, levelsString);

        LinkedHashMap<String, Object> globalMap = new LinkedHashMap<String, Object>();
        globalMap.put("Conventions", "Would love if it really was CF-1.4 :)");
        globalMap.put("contact", "andrea.antonello@gmail.com");
        globalMap.put("references", "http://www.hydrologis.com");
        globalMap.put("Comment", "This was just one of the first tests...");
        globalMap.put("distribution_statement", "IN NO EVENT SHALL HYDROLOGIS OR ITS REPRESENTATIVES BE LIABLE... blah blah");
        ncFW.addGlobalAttributes(globalMap);

        NcVariable elevationVariable = ncFW.addVariable("elevation", "dem elevation", "m", true, true);
        LinkedHashMap<String, Object> variableAttributesMap = new LinkedHashMap<String, Object>();
        variableAttributesMap.put("units", "m");
        variableAttributesMap.put("unit_long", "meters");
        variableAttributesMap.put("standard_name", "");
        variableAttributesMap.put("long_name", "digital elevation model");
        elevationVariable.setAttributesMap(variableAttributesMap);

        String bac_chiese = "a1"; // "chiese_basin";
        String bac_chiese2 = "a2"; // "chiese_drain";
        String bac_chiese3 = "b1"; // "chiese_netnumber";
        String bac_chiese4 = "b2"; // "chiese_net_flow";
        elevationVariable.addNcLayer(new String[]{bac_chiese}, startDateString, 0.0, NcDataType.GRID);
        elevationVariable.addNcLayer(new String[]{bac_chiese2}, endDateString, 0.0, NcDataType.GRID);
        elevationVariable.addNcLayer(new String[]{bac_chiese3}, startDateString, 100.0, NcDataType.GRID);
        elevationVariable.addNcLayer(new String[]{bac_chiese4}, endDateString, 100.0, NcDataType.GRID);

        try {
            ncFW.writeNcDataset(pm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}