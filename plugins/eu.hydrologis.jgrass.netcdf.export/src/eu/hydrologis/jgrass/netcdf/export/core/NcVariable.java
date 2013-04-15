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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.media.jai.iterator.RandomIter;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.DummyProgressMonitor;
import eu.hydrologis.jgrass.libs.utils.monitor.IProgressMonitorJGrass;

/**
 * Class representing a netcdf variable.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class NcVariable {

    private final NcFileWriter parentNcFile;
    private String name;
    private String description;
    private boolean hasTime = false;
    private boolean hasLevel = false;
    private List<NcLayer> ncLayerList = new ArrayList<NcLayer>();
    private List<String> timestepList = new ArrayList<String>();
    private List<Double> levelList = new ArrayList<Double>();
    private Dimension latDim;
    private Dimension lonDim;
    private Dimension timeDim;
    private Dimension levelDim;
    private NetcdfFileWriteable ncDataFile;
    private boolean readyToWrite = false;
    private LinkedHashMap<String, Object> attributesMap;

    /**
     * Create a netcdf variable with the minimum amount of info.
     * 
     * @param ncFile the parent {@link NcFileWriter}.
     * @param name the name of the variable.
     * @param description a short description of the variable.
     * @param hasTime true if the variable has time levels.
     * @param hasLevel true if the variable has elevation levels.
     */
    public NcVariable( NcFileWriter ncFile, String name, String description, boolean hasTime,
            boolean hasLevel ) {
        this.parentNcFile = ncFile;
        this.name = name;
        this.description = description;
        this.hasTime = hasTime;
        this.hasLevel = hasLevel;
    }

    /**
     * Adds a new {@link NcLayer} to the variable. 
     * 
     * @param rasterNames the name of the raster from which to take data. 
     *          TODO if the rasters are 2, create a vector set.
     * @param time the timestep for this layer.
     * @param level the elevation for this layer.
     * @param ncDataType the datatype of this layer.
     */
    public void addNcLayer( String[] rasterNames, String time, Double level, NcDataType ncDataType ) {
        String[] rasterPaths = new String[rasterNames.length];
        for( int i = 0; i < rasterNames.length; i++ ) {
            rasterPaths[i] = parentNcFile.getMapsetPath() + File.separator + JGrassConstants.CELL
                    + File.separator + rasterNames[i];
        }

        NcLayer ncLayer = new NcLayer(rasterPaths, time, level, ncDataType);
        ncLayerList.add(ncLayer);

        if (time != null && !timestepList.contains(time)) {
            timestepList.add(time);
        }
        if (level != null && !levelList.contains(level)) {
            levelList.add(level);
        }
    }

    public void addNcLayer( NcLayer layer ) {
        ncLayerList.add(layer);

        String time = layer.getTime();
        if (time != null && !timestepList.contains(time)) {
            timestepList.add(time);
        }
        Double level = layer.getLevel();
        if (level != null && !levelList.contains(level)) {
            levelList.add(level);
        }
    }

    /**
     * Sets a map of attributes for the variable.
     * 
     * @param attributesMap a {@link LinkedHashMap map} of attributes definitions.
     */
    public void setAttributesMap( LinkedHashMap<String, Object> attributesMap ) {
        this.attributesMap = attributesMap;
    }

    public LinkedHashMap<String, Object> getAttributesMap() {
        return attributesMap;
    }

    /**
     * Define the data.
     * 
     * @param ncDataFile the {@link NetcdfFileWriteable}.
     */
    public void defineData( NetcdfFileWriteable ncDataFile ) {
        this.ncDataFile = ncDataFile;
        List<Dimension> dimensionsList = parentNcFile.getDimensionsList(hasTime, hasLevel);
        ncDataFile.addVariable(name, DataType.DOUBLE, dimensionsList);

        if (!hasTime && !hasLevel) {
            latDim = dimensionsList.get(0);
            lonDim = dimensionsList.get(1);
        } else if (hasTime && !hasLevel) {
            timeDim = dimensionsList.get(0);
            latDim = dimensionsList.get(1);
            lonDim = dimensionsList.get(2);
        } else if (!hasTime && hasLevel) {
            levelDim = dimensionsList.get(0);
            latDim = dimensionsList.get(1);
            lonDim = dimensionsList.get(2);
        } else if (hasTime && hasLevel) {
            timeDim = dimensionsList.get(0);
            levelDim = dimensionsList.get(1);
            latDim = dimensionsList.get(2);
            lonDim = dimensionsList.get(3);
        }
        
        if (attributesMap != null && attributesMap.size() > 0) {
            Set<String> keySet = attributesMap.keySet();
            for( String key : keySet ) {
                Object value = attributesMap.get(key);
                if (value instanceof Number) {
                    ncDataFile.addVariableAttribute(name, key, (Number) value);
                } else if (value instanceof String) {
                    ncDataFile.addVariableAttribute(name, key, (String) value);
                } else {
                    // not defined, jump over it
                    continue;
                }
            }
        }
        ncDataFile.addVariableAttribute(name, "missing_value", new Double(
                JGrassConstants.doubleNovalue));

        readyToWrite = true;
    }

    /**
     * Write all the defined data to a new netcdf dataset.
     * 
     * @throws IOException
     * @throws InvalidRangeException
     * @throws FactoryException
     * @throws TransformException
     */
    public void writeData( IProgressMonitorJGrass pm ) throws IOException, InvalidRangeException,
            FactoryException, TransformException {
        if (!readyToWrite) {
            throw new IOException("Not ready to write. Did you call defineData first?");
        }

        JGrassRegion activeRegion = parentNcFile.getActiveRegion();
        CoordinateReferenceSystem locationCrs = parentNcFile.getLocationCrs();

        int[] origin = null;
        int latLength = latDim.getLength();
        int lonLength = lonDim.getLength();

        ArrayDouble dataTemp = null;
        if (timeDim == null && levelDim == null) {
            dataTemp = new ArrayDouble.D2(latLength, lonLength);
            origin = new int[2];
        } else if (timeDim != null && levelDim == null) {
            dataTemp = new ArrayDouble.D3(timeDim.getLength(), latLength, lonLength);
            origin = new int[3];
        } else if (timeDim == null && levelDim != null) {
            dataTemp = new ArrayDouble.D3(levelDim.getLength(), latLength, lonLength);
            origin = new int[3];
        } else if (timeDim != null && levelDim != null) {
            dataTemp = new ArrayDouble.D4(timeDim.getLength(), levelDim.getLength(), latLength,
                    lonLength);
            origin = new int[4];
        }

        Index index = dataTemp.getIndex();
        for( NcLayer layer : ncLayerList ) {
            String time = layer.getTime();
            Double level = layer.getLevel();
            int timeIndex = time != null ? timestepList.indexOf(time) : -1;

            int levelIndex = -1;
            for( int j = 0; j < levelList.size(); j++ ) {
                double l = levelList.get(j);
                if (l == level) {
                    levelIndex = j;
                    break;
                }
            }

            pm.beginTask("Writing variable: " + name + " layer: " + layer.getName(), latLength);

            RandomIter dataIter = layer.getData(new DummyProgressMonitor(), activeRegion,
                    locationCrs);
            for( int lat = 0; lat < latLength; lat++ ) {
                for( int lon = 0; lon < lonLength; lon++ ) {
                    if (timeIndex == -1 && levelIndex == -1) {
                        index.set(lat, lon);
                    } else if (timeIndex != -1 && levelIndex == -1) {
                        index.set(timeIndex, lat, lon);
                    } else if (timeIndex == -1 && levelIndex != -1) {
                        index.set(levelIndex, lat, lon);
                    } else if (timeIndex != -1 && levelIndex != -1) {
                        index.set(timeIndex, levelIndex, lat, lon);
                    }
                    double sampleDouble = dataIter.getSampleDouble(lon, lat, 0);
                    dataTemp.setDouble(index, sampleDouble);
                }
                pm.worked(1);
            }
            pm.done();
        }
        
        ncDataFile.write(name, origin, dataTemp);

    }
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<NcLayer> getNcLayerList() {
        return ncLayerList;
    }

    public void setNcLayerList( List<NcLayer> ncLayerList ) {
        this.ncLayerList = ncLayerList;
    }

    public List<String> getTimesteps() {
        return timestepList;
    }

    public List<Double> getLevels() {
        return levelList;
    }

    public boolean isHasLevel() {
        return hasLevel;
    }

    public boolean isHasTime() {
        return hasTime;
    }

    public String toString() {
        final String CR = "\n";
        final String TAB = "\t";

        StringBuilder retValue = new StringBuilder();

        retValue.append("NcVariable ( \n").append(TAB).append(super.toString()).append(CR).append(
                TAB).append("name = ").append(this.name).append(CR).append(TAB).append(
                "description = ").append(this.description).append(CR).append(TAB).append(
                "hasTime = ").append(this.hasTime).append(CR).append(TAB).append("hasLevel = ")
                .append(this.hasLevel).append(CR).append(TAB).append("ncLayerList = ").append(
                        this.ncLayerList).append(CR).append(TAB).append("timestepList = ").append(
                        this.timestepList).append(CR).append(TAB).append("levelList = ").append(
                        this.levelList).append(CR).append(" )");

        return retValue.toString();
    }

}
