package eu.hydrologis.jgrass.netcdf.export.core;

public enum NcDataType {
    /**
     * Represents raster data.
     */
    GRID,
    
    /**
     * Represents a single (or series) of scalar values.
     */
    SCALAR,
    
    /**
     * Represents a tuple of rasters that are components of a vector dataset.
     */
    VECTOR;
}
