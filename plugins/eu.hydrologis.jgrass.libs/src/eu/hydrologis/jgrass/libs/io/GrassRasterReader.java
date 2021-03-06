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
package eu.hydrologis.jgrass.libs.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import eu.hydrologis.jgrass.libs.JGrassLibsPlugin;
import eu.hydrologis.jgrass.libs.map.attribute.AttributeTable;
import eu.hydrologis.jgrass.libs.map.color.ColorMapBuffer;
import eu.hydrologis.jgrass.libs.map.color.ColorRule;
import eu.hydrologis.jgrass.libs.map.color.ColorTable;
import eu.hydrologis.jgrass.libs.map.color.GrassColorTable;
import eu.hydrologis.jgrass.libs.messages.Messages;
import eu.hydrologis.jgrass.libs.object.GDataObject;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.FileUtilities;
import eu.hydrologis.jgrass.libs.utils.Format;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.JlsTokenizer;
import eu.hydrologis.jgrass.libs.utils.monitor.IProgressMonitorJGrass;

/**
 * <p>
 * This reads any native Raster map format. It supports integer, float and double and the
 * transformation of any of those into int, float and double matrixes, as well as in the ByteBuffers
 * of the same tipes.
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 * @since 1.1.0
 */
public class GrassRasterReader extends MapReader {

    /*
     * 0 for 1-byte integer, 1 for 2-byte integer and so on, -1 for float, -2 for double
     */
    private int rasterMapType = -9999;

    private int numberOfBytesPerValue = -9999;

    /* 1 for compressed, 0 for not compressed */
    private int compressed = -9999;

    private Object novalue = new Double(Double.NaN);

    /**
     * Comment for <code>matrixType</code> this defines the tipe of matrix to return: 0 = normal
     * type[][] matrix 1 = type[][] matrix indexed from [1][1] 2 = transposed of the original
     * type[][] matrix 3 = transposed of the original type[][] matrix and indexed by [1][1]
     */
    private int matrixType = 0;

    /**
     * Comment for <code>theFilePath</code> this is the complete path including the filename
     */
    // private String theFilePath = null;
    // private String theNullFilePath = null;
    private RandomAccessFile cellFile = null;

    private RandomAccessFile nullFile = null;

    private String filename = null;

    private String locationPath = null;

    private String mapsetPath = null;

    private String reclassPath = null;

    private Vector<Object> reclassTable = null;

    /**
     * Comment for <code>addressesofrows</code> the pointers to access in the map file the begin
     * of every row
     */
    private long[] addressesofrows = null;

    private ByteBuffer rasterByteBuffer = null;

    private boolean moreData = false;

    private boolean hasChanged = true;

    private boolean isOldIntegerMap = false;

    // private ByteBuffer rowCache = null;
    private int rowCacheRow = -1;

    private int firstDataRow = -1;

    private final double[] range = new double[]{1000000.0, -1000000.0}; // min,

    // max

    private final double[] dataRange = new double[]{Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY};

    /* Storage for cell category descriptive information */
    private AttributeTable attTable = null;

    private AttributeTable legendAttribTable = null;

    private ByteBuffer cmapBuffer = ByteBuffer.allocate(1);

    /** Creates a new instance of GrassRasterReader */
    public GrassRasterReader() {
        super(MapReader.RASTER_READER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.libs.io.MapReader#open(java.lang.String)
     */
    public boolean open( String mapPath ) {
        File file = new File(mapPath);
        if (!file.exists()) {
            return false;
        }
        File mapsetFile = file.getParentFile().getParentFile();
        File locationFile = mapsetFile.getParentFile();
        return this.open(file.getName(), locationFile.getAbsolutePath(), mapsetFile.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see jgrass.io.MapReader#open(java.lang.String, jgrass.map.Mapset) this opens checks for the
     *      file existence, sets the active and map regions, defines the maptype, opens the map and
     *      extracts the header to extract the rowaddresse and check if everything is alright
     */
    public boolean open( String fileName, String locationPath, String mapsetName ) {
        cellFile = nullFile = null;

        filename = fileName;
        this.locationPath = locationPath;
        this.mapsetPath = locationPath + File.separator + mapsetName;

        if (hasChanged) {
            /* Test the type of the map. */
            if (rasterMapType == -9999) {
                if (!getRasterMapTypes(filename, mapsetPath)) {
                    return false;
                }
            }

            if (compressed == 1) {
                /*
                 * Read the header of the map and the addresses of the compressed rows.
                 */
                try {
                    addressesofrows = getRowAddressesFromHeader(readHeader(cellFile));
                } catch (IOException e) {
                    return false;
                }
            }

            /* Ok. Get ready to read data */
            moreData = true;
        }

        hasChanged(false);

        return true;
    }

    public void close() {
        try {
            cellFile.close();
            if (nullFile != null)
                nullFile.close();
        } catch (IOException e) {
            JGrassLibsPlugin.log("JGrassLibsPlugin problem", e); //$NON-NLS-1$
            e.printStackTrace();
        }
        hasChanged(true);
        moreData = true;
    }

    /**
     * 
     */
    private ColorTable loadColorTable() {
        /* Read the color map */
        try {
            colorTable = null;
            colorTable = new GrassColorTable(mapsetPath, filename, dataRange);
            // System.out.println("================================COLORTABLE================================");
            // Enumeration rules = colorTable.getColorRules();
            // while (rules.hasMoreElements())
            // System.out.println("RULE="+rules.nextElement());
            // System.out.println("================================");
            // System.exit(0);
            return colorTable;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 
     */
    public AttributeTable loadAttributeTable( String themapsetPath, String thefilename ) {
        File ds1 = null;

        try {
            /*
             * File is a standard file where the categories values are stored in the cats directory.
             */
            ds1 = new File(themapsetPath + File.separator + JGrassConstants.CATS + File.separator
                    + thefilename);
            if (!ds1.exists()) {
                return null;
            }
            BufferedReader rdr = new BufferedReader(new FileReader(ds1));
            /* Instantiate attribute table */
            attTable = new AttributeTable();
            /* Ignore first 4 lines. */
            rdr.readLine();
            rdr.readLine();
            rdr.readLine();
            rdr.readLine();
            /* Read next n lines */
            String line;
            while( (line = rdr.readLine()) != null ) {
                /* All lines other than '0:no data' are processed */
                if (line.indexOf("0:no data") == -1) { //$NON-NLS-1$
                    JlsTokenizer tk = new JlsTokenizer(line, ":"); //$NON-NLS-1$
                    if (tk.countTokens() == 2) {
                        float f = Float.parseFloat(tk.nextToken());
                        String att = tk.nextToken().trim();
                        attTable.addAttribute(f, att);
                    } else if (tk.countTokens() == 3) {
                        float f0 = Float.parseFloat(tk.nextToken());
                        float f1 = Float.parseFloat(tk.nextToken());
                        String att = tk.nextToken().trim();
                        attTable.addAttribute(f0, f1, att);
                    }
                }
            }
            return attTable;
        } catch (IOException e) {
            JGrassLibsPlugin.log("JGrassLibsPlugin problem", e); //$NON-NLS-1$
            e.printStackTrace();
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see jgrass.io.MapReader#hasMoreData() checks if there are more data and if there are not, it
     *      stops
     */
    public boolean hasMoreData( IProgressMonitorJGrass monitor ) throws Exception {
        try {
            if (dataWindow != null && moreData == true) {
                /*
                 * If the parameter 'donotloadcolortable' has not been set then load the color table
                 * here and when reading the data row by row we calculate the color map values and
                 * store it in the color map byte buffer. If there is no color table then we create
                 * a default one after we have read all the data and know what the data range is.
                 * This is done at the end of this function.
                 */
                if (cmapBuffer != null) {
                    cmapBuffer = null;
                    cmapBuffer = ByteBuffer.allocate(dataWindow.getRows() * dataWindow.getCols()
                            * 4);
                    if (colorTable == null)
                        colorTable = loadColorTable();
                    if (colorTable.isEmpty())
                        colorTable = null;
                }
                /* Allocate the space for the map data. */
                int bufferSize = dataWindow.getRows() * dataWindow.getCols()
                        * numberOfBytesPerValue;
                rasterByteBuffer = null;
                // rasterByteBuffer = ByteBuffer.allocate(bufferSize*
                // numberOfBytesPerValue);
                rasterByteBuffer = new GDataObject.GridData(bufferSize,
                        GDataObject.USE_BACKING_FILE).getReadBuffer();
                rasterByteBuffer.rewind();

                // System.out.println("retrieveing data in window: " +
                // dataWindow.toString());

                try {
                    /* Byte array that will hold a complete null row */
                    byte[] nullRow = null;
                    /* The rowDataArray holds the unpacked row data */
                    byte[] rowDataCache = new byte[dataWindow.getCols() * numberOfBytesPerValue];
                    /* The rowColorDataArray holds the unpacked row color data */
                    byte[] rowColorDataCache = new byte[dataWindow.getCols() * 4];
                    /* Reset row cache */
                    // rowCache = ByteBuffer.allocate(fileWindow.getCols() *
                    // ((rasterMapType == -2) ? 8 : 4));
                    // if (logger.isDebugEnabled())
                    // logger.debug("allocating rowDataArray["+rowDataArray.length+
                    // "], rowCache["+rowCache.limit()+"]");
                    rowCacheRow = -1;
                    firstDataRow = -1;
                    int rowindex = -1;
                    /* Get a local reference to speed things up */
                    int filerows = fileWindow.getRows();
                    double filenorth = fileWindow.getNorth();
                    double filensres = fileWindow.getNSResolution();
                    double datanorth = dataWindow.getNorth();
                    double datansres = dataWindow.getNSResolution();
                    // double datansres2 = datansres / 2;
                    /* Iterate through all the rows of the data window. */
                    // int lastpos = 0;
                    monitor
                            .beginTask(
                                    Messages.getString("GrassRasterReader.readingraster") + filename, dataWindow //$NON-NLS-1$
                                            .getRows());
                    int stepRowsForPercentage = dataWindow.getRows() / 100;
                    for( double row = 0; row < dataWindow.getRows(); row++ ) {
                        if (row % stepRowsForPercentage == 0.0)
                            monitor.worked(stepRowsForPercentage);
                        /*
                         * Calculate the map file row for the current data window row.
                         */
                        double filerow = (filenorth - (datanorth - (row * datansres))) / filensres;
                        filerow = Math.floor(filerow);
                        // int filerow = (int) ((filenorth - (datanorth - (row *
                        // datansres + datansres2))) / filensres);
                        // System.out.print("FILENORTH="+filenorth+",
                        // DATANORTH="+datanorth+", FILEROW="+row);
                        if (filerow < 0 || filerow >= filerows) {
                            // System.out.println(", NULL ROW");
                            /*
                             * If no data has been read yet, then increment first data row counter
                             */
                            if (firstDataRow == -1)
                                rowindex++;
                            /*
                             * Write a null row to the raster buffer. To speed things up the first
                             * time this is called it instantiates the buffer and fills it with null
                             * values that are reused the other times.
                             */
                            if (nullRow == null)
                                nullRow = initNullRow();
                            rasterByteBuffer.put(nullRow);
                        } else {
                            // System.out.println(", DATA ROW");
                            if (firstDataRow == -1)
                                firstDataRow = rowindex + 1;
                            /* Read row and put in raster buffer */
                            if (filerow == rowCacheRow) {
                                rasterByteBuffer.put(rowDataCache);
                                cmapBuffer.put(rowColorDataCache);
                            } else {
                                readRasterRow((int) filerow, rowDataCache, rowColorDataCache);
                                rowCacheRow = (int) filerow;
                                rasterByteBuffer.put(rowDataCache);
                                cmapBuffer.put(rowColorDataCache);
                            }
                        }
                        // System.out.println("FILEROWS="+filerows+",
                        // FILEROW="+filerow+", ROWCACHEROW="+rowCacheRow+",
                        // ROW_COUNTER="+row);
                    }
                    monitor.done();

                    rowDataCache = null;
                    rowColorDataCache = null;
                    rowCacheRow = -1;
                    nullRow = null;
                    System.gc();
                } catch (IOException e) {
                    moreData = false;
                } catch (DataFormatException e) {
                    moreData = false;
                }
                rasterByteBuffer.rewind();
            }

        } catch (Exception e) {
            JGrassLibsPlugin
                    .log(
                            "JGrassLibsPlugin problem: eu.hydrologis.jgrass.libs.io#GrassRasterReader#hasMoreData", e); //$NON-NLS-1$
            e.printStackTrace();
            throw new RasterReadingFailureException("Problems reading raster: "
                    + e.getLocalizedMessage());

        }
        return moreData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jgrass.io.MapReader#getNextData() returns the data in the required format
     */
    public Object getNextData() {
        moreData = false;
        int rows_x_cols = dataWindow.getRows() * dataWindow.getCols();

        /* Return data as type Vector */
        if (dataObject instanceof Vector) {
            // Not yet implemented
            dataObject = null;
        }
        /* Return data as type double[][] */
        else if (dataObject instanceof double[][]) {
            double[][] data = (double[][]) dataObject;
            if (data.length != rows_x_cols)
                dataObject = new double[dataWindow.getRows()][dataWindow.getCols()];
            getDoubleArray();
        }
        /* Return data as type float[][] */
        else if (dataObject instanceof float[][]) {
            float[][] data = (float[][]) dataObject;
            if (data.length != rows_x_cols)
                dataObject = new float[dataWindow.getRows()][dataWindow.getCols()];
            getFloatArray();
        }
        /* Return data as type int[][] */
        else if (dataObject instanceof int[][]) {
            int[][] data = (int[][]) dataObject;
            if (data.length != rows_x_cols)
                dataObject = new int[dataWindow.getRows()][dataWindow.getCols()];
            getIntArray();
        }
        /* Return data as type DoubleBuffer */
        else if (dataObject instanceof DoubleBuffer) {
            DoubleBuffer data = (DoubleBuffer) dataObject;
            if (data.capacity() != rows_x_cols)
                dataObject = ByteBuffer.allocate(rows_x_cols * 8).asDoubleBuffer();
            getDoubleBuffer();
        }
        /* Return data as type FloatBuffer */
        else if (dataObject instanceof FloatBuffer) {
            FloatBuffer data = (FloatBuffer) dataObject;
            if (data.capacity() != rows_x_cols)
                dataObject = ByteBuffer.allocate(rows_x_cols * 4).asFloatBuffer();
            getFloatBuffer();
        }
        /* Return data as type IntBuffer */
        else if (dataObject instanceof IntBuffer) {
            IntBuffer data = (IntBuffer) dataObject;
            if (data.capacity() != rows_x_cols)
                dataObject = ByteBuffer.allocate(rows_x_cols * 4).asIntBuffer();
            getIntBuffer();
        } else {
            // Don't know what top do yet, thro some exception
            dataObject = null;
        }

        return dataObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jgrass.io.MapReader#getNextData() returns the data in the required format
     */
    public ColorMapBuffer getNextDataColor( int attColor ) throws IOException {
        moreData = false;

        if (colorTable != null && cmapBuffer != null && cmapBuffer.capacity() > 1) {
            ColorMapBuffer colorBuffer = new ColorMapBuffer();
            colorBuffer.setRowOffset(firstDataRow);
            //            System.out.println("GET_NEXT_DATA_COLOR (1) RGB_BUFFER " //$NON-NLS-1$
            // + colorBuffer.getRGBBuffer());
            colorBuffer.setRGBBuffer((ByteBuffer) cmapBuffer.position(firstDataRow * 4).flip());
            //            System.out.println("GRASS.RASTER.READER - GET,.NEXT.DATA.COLOR - color buffer - " //$NON-NLS-1$
            // + cmapBuffer);
            // colorBuffer.setRGBBuffer(cmapBuffer);

            return colorBuffer;
        }

        /*
         * Because color maps can be very large (especially with satellite images) they are not kept
         * around in memory.
         */
        if (colorTable == null) {
            colorTable = loadColorTable();
            if (colorTable == null)
                return null;
            if (colorTable.isEmpty())
                colorTable.createDefaultColorTable(dataRange);
        }

        ColorMapBuffer colorBuffer = new ColorMapBuffer();
        colorBuffer.setRowOffset(firstDataRow);
        int dataOffset = firstDataRow * dataWindow.getCols() * numberOfBytesPerValue;
        // interpolate the data to get the color map
        colorBuffer.setRGBBuffer((cmapBuffer = colorTable.interpolateColorMap(rasterMapType,
                rasterByteBuffer, dataOffset)));
        System.out.println("GET_NEXT_DATA_COLOR (2) DATA_OFFSET=" + dataOffset //$NON-NLS-1$
                + ", RGB_BUFFER " + cmapBuffer + ", RASTER_BYTE_BUFFER " //$NON-NLS-1$ //$NON-NLS-2$
                + rasterByteBuffer);

        return colorBuffer;
    }

    /**
     * Create a string that defines how the legend will look.
     * @throws IOException 
     */
    public String getLegendString() throws IOException {
        StringBuffer legend;

        /* If colour table has not been loaded then load it */
        if (colorTable == null)
            colorTable = loadColorTable();

        /* If attribute table not loaded then load it */
        if (legendAttribTable == null) {
            if (mapsetPath == null || filename == null) {
                throw new IOException();
            }
            legendAttribTable = loadAttributeTable(mapsetPath, filename);
        }

        /*
         * If the attribute table has any values then this is a fixed color table
         */
        if (legendAttribTable.size() > 0) {
            legend = new StringBuffer(legendAttribTable.size() * 12);
            legend.append("raster"); //$NON-NLS-1$
            /* Iterate through all the category attribute values */
            Enumeration< ? > catagories = legendAttribTable.getCategories();
            while( catagories.hasMoreElements() ) {
                AttributeTable.CellAttribute catt = (AttributeTable.CellAttribute) catagories
                        .nextElement();
                byte[] clr = colorTable.getColor(catt.getLowcategoryValue());
                legend.append("|").append(catt.getText()).append("|#").append( //$NON-NLS-1$ //$NON-NLS-2$
                        Format.sprintf("%02x", clr[0] & 0xff)).append( //$NON-NLS-1$
                        Format.sprintf("%02x", clr[1] & 0xff)).append( //$NON-NLS-1$
                        Format.sprintf("%02x", clr[2] & 0xff)); //$NON-NLS-1$
            }
        } else {
            legend = new StringBuffer(colorTable.size() * 10);
            legend.append("raster"); //$NON-NLS-1$
            /* Iterate through all the color values. */
            Enumeration< ? > crules = colorTable.getColorRules();
            while( crules.hasMoreElements() ) {
                ColorRule rule = (ColorRule) crules.nextElement();
                float low = rule.getLowCategoryValue();
                float range = rule.getCategoryRange();
                if (range == 0f) {
                    byte[] clr0 = rule.getColor(low);
                    legend.append("|").append(String.valueOf(low)).append("|#") //$NON-NLS-1$ //$NON-NLS-2$
                            .append(Format.sprintf("%02x", clr0[0] & 0xff)) //$NON-NLS-1$
                            .append(Format.sprintf("%02x", clr0[1] & 0xff)) //$NON-NLS-1$
                            .append(Format.sprintf("%02x", clr0[2] & 0xff)); //$NON-NLS-1$
                } else {
                    float inc = range / 6f;
                    String text = String.valueOf(low) + " to " //$NON-NLS-1$
                            + String.valueOf(low + range);
                    for( int i = 0; i < 7; i++ ) {
                        byte[] clr0 = rule.getColor(low);
                        legend.append("|").append((i == 3) ? text : "").append( //$NON-NLS-1$ //$NON-NLS-2$
                                "|#").append( //$NON-NLS-1$
                                Format.sprintf("%02x", clr0[0] & 0xff)).append( //$NON-NLS-1$
                                Format.sprintf("%02x", clr0[1] & 0xff)).append( //$NON-NLS-1$
                                Format.sprintf("%02x", clr0[2] & 0xff)); //$NON-NLS-1$
                        low += inc;
                    }
                }
            }
        }
        /* Can't afford to keep the table around in memory */
        legendAttribTable = null;

        return legend.toString();
    }

    /**
     * 
     */
    public void hasChanged( boolean _haschanged ) {
        // if (_haschanged) {
        // novalue = new Double(Double.NaN);
        // matrixType = 0;
        // // theFilePath = null;
        // cellFile = nullFile = null;
        // filename = null;
        // mapsetPath = null;
        // locationPath = null;
        // rasterByteBuffer = null;
        // moreData = false;
        // // fileWindow = null;
        // // dataWindow = null;
        // rasterMapType = -9999;
        // numberOfBytesPerValue = -9999;
        // compressed = -9999;
        // }
        // hasChanged = _haschanged;
    }

    /**
     * @return
     */
    private Object getIntBuffer() {
        if (numberOfBytesPerValue == 8) {
            for( int i = 0; i < ((IntBuffer) dataObject).capacity(); i++ ) {
                ((IntBuffer) dataObject).put((int) rasterByteBuffer.getDouble());
            }
        } else if (numberOfBytesPerValue == 4 && rasterMapType < 0) {
            dataObject = rasterByteBuffer.asIntBuffer();
        } else if (rasterMapType > -1) {
            for( int i = 0; i < ((DoubleBuffer) dataObject).capacity(); i++ ) {
                ((IntBuffer) dataObject).put(rasterByteBuffer.getInt());
            }
        }
        ((IntBuffer) dataObject).rewind();
        return dataObject;
    }

    /**
     * @return
     */
    private Object getFloatBuffer() {
        if (numberOfBytesPerValue == 8) {
            for( int i = 0; i < ((FloatBuffer) dataObject).capacity(); i++ ) {
                float val = (float) rasterByteBuffer.getDouble();
                if (val != val)
                    ((FloatBuffer) dataObject).put(((Float) novalue).floatValue());
                else
                    ((FloatBuffer) dataObject).put(val);
            }
        } else if (numberOfBytesPerValue == 4 && rasterMapType < 0) {
            // dataObject = rasterByteBuffer.asFloatBuffer();
            float val = rasterByteBuffer.getFloat();
            if (val != val)
                ((FloatBuffer) dataObject).put(((Float) novalue).floatValue());
            else
                ((FloatBuffer) dataObject).put(val);
        } else if (rasterMapType > -1) {
            for( int i = 0; i < ((FloatBuffer) dataObject).capacity(); i++ ) {
                // ((FloatBuffer) dataObject).put((float)
                // rasterByteBuffer.getInt());
                float val = rasterByteBuffer.getInt();
                if (val != val)
                    ((FloatBuffer) dataObject).put(((Float) novalue).floatValue());
                else
                    ((FloatBuffer) dataObject).put(val);
            }
        }
        ((FloatBuffer) dataObject).rewind();
        return dataObject;
    }

    /**
     * @return
     */
    private Object getDoubleBuffer() {
        if (numberOfBytesPerValue == 8) {
            // dataObject = rasterByteBuffer.asDoubleBuffer();
            ((DoubleBuffer) dataObject).put(rasterByteBuffer.asDoubleBuffer());

        } else if (numberOfBytesPerValue == 4 && rasterMapType < 0) {
            for( int i = 0; i < ((DoubleBuffer) dataObject).capacity(); i++ ) {
                ((DoubleBuffer) dataObject).put(rasterByteBuffer.getFloat());
            }
        } else if (rasterMapType > -1) {
            for( int i = 0; i < ((DoubleBuffer) dataObject).capacity(); i++ ) {
                ((DoubleBuffer) dataObject).put(rasterByteBuffer.getInt());
            }
        }
        ((DoubleBuffer) dataObject).rewind();
        return dataObject;
    }

    /**
     * @return
     */
    private Object getIntArray() {
        if (numberOfBytesPerValue == 8) {
            dataObject = intFromDoubleGridRead();
        } else if (numberOfBytesPerValue == 4 && rasterMapType < 0) {
            dataObject = intFromFloatGridRead();
        }
        // from integer map
        else if (rasterMapType > -1) {
            dataObject = intFromIntGridRead();
        } else {
            // throw some exception
        }
        return dataObject;
    }

    /**
     * @return
     */
    private Object getFloatArray() {
        if (numberOfBytesPerValue == 8) {
            dataObject = floatFromDoubleGridRead();
        } else if (numberOfBytesPerValue == 4 && rasterMapType < 0) {
            dataObject = floatFromFloatGridRead();
        }
        // from integer map
        else if (rasterMapType > -1) {
            dataObject = floatFromIntGridRead();
        } else {
            // throw some exception
        }
        return dataObject;
    }

    /**
     * @return
     */
    private Object getDoubleArray() {
        if (numberOfBytesPerValue == 8) {
            dataObject = doubleFromDoubleGridRead();
        } else if (numberOfBytesPerValue == 4 && rasterMapType < 0) {
            dataObject = doubleFromFloatGridRead();
        }
        // from integer map
        else if (rasterMapType > -1) {
            dataObject = doubleFromIntGridRead();
        } else {
            // throw some exception
        }
        return dataObject;
    }

    /**
     * utility to set particular parameters
     */
    public void setParameter( String key, Object obj ) {
        if (key.equals("novalue")) { //$NON-NLS-1$
            novalue = obj;
        } else if (key.equals("matrixtype")) { //$NON-NLS-1$
            Integer dmtype = (Integer) obj;
            matrixType = dmtype.intValue();
        } else if (key.equals("donotloadcolortable")) { //$NON-NLS-1$
            cmapBuffer = null;
        }
    }

    public void setOutputDataObject( Object _dataObject ) {
        /* Call parent class to store data object */
        super.setOutputDataObject(_dataObject);
    }

    /**
     * Returns the path to the map relative to the mapset.
     */
    public String getMapPath() {
        return JGrassConstants.CELL;
    }

    /**
     * Reads the header part of the file into memory
     */
    private ByteBuffer readHeader( RandomAccessFile ds ) throws IOException {

        /*
         * the first byte defines the number of bytes are used to describe the row addresses in the
         * header (once it was sizeof(long) in grass but then it was turned to an offset (that
         * brought to reading problems in JGrass whenever the offset was != 4).
         */
        int first = ds.read();

        ByteBuffer fileHeader = ByteBuffer.allocate(1 + first * fileWindow.getRows() + first);

        ds.seek(0);
        /* Read header */
        ds.read(fileHeader.array());

        return fileHeader;
    }

    /**
     * Extract the row addresses from the header information of the file
     */
    private long[] getRowAddressesFromHeader( ByteBuffer header ) {
        /*
         * Jump over the no more needed first byte (used in readHeader to define the header size)
         */
        byte firstbyte = header.get();

        /* Read the data row addresses inside the file */
        long[] adrows = new long[fileWindow.getRows() + 1];
        if (firstbyte == 4) {
            for( int i = 0; i <= fileWindow.getRows(); i++ ) {
                adrows[i] = header.getInt();
            }
        } else if (firstbyte == 8) {
            for( int i = 0; i <= fileWindow.getRows(); i++ ) {
                adrows[i] = header.getLong();
            }
        } else {
            // problems
        }
        return adrows;
    }

    /**
     * Determines the map type given a file and its mapset. It reads the information from the header
     * file in the cellhd directory and determines the geographic limits, format of the data, etc
     * from the file. A Typical file header looks like: proj: 1 zone: 13 north: 4928000 south:
     * 4914000 east: 609000 west: 590000 cols: 950 rows: 700 e-w resol: 20 n-s resol: 20 format: 0
     * compressed: 1 If the first line is 'reclass' then this file is a reclassified file and the
     * original data file is given by the next two lines: reclass name: soils mapset: PERMANENT #1 5
     * 3 8 .... .... We also check the cell_misc directory to determine the raster map binary format
     * and type, the file f_format, which holds type, exists only if the map is floating point map.
     */
    private boolean getRasterMapTypes( String fname, String mapsetPath ) {
        LinkedHashMap<String, String> fileMapHeader = new LinkedHashMap<String, String>();
        /* Read contents of 'cellhd/name' file from the current mapset */
        String line;
        BufferedReader cellhead;
        String reclassFile = null;
        String reclassMapset = null;

        reclassTable = null;
        try {
            File ds4 = new File(mapsetPath + File.separator + JGrassConstants.CELLHD
                    + File.separator + fname);
            cellhead = new BufferedReader(new FileReader(ds4));
            cellhead.mark(128);
            /*
             * Read first line to determine if file is a reclasses file. If it is then open the data
             * file and continue as per usual.
             */
            if ((line = cellhead.readLine()) == null)
                return false;
            if (line.trim().equalsIgnoreCase("reclass")) { //$NON-NLS-1$
                /* The next two lines hold the orginal map file amd mapset */
                for( int i = 0; i < 2; i++ ) {
                    if ((line = cellhead.readLine()) == null)
                        return false;
                    JlsTokenizer tk = new JlsTokenizer(line, ":"); //$NON-NLS-1$
                    if (tk.countTokens() == 2) {
                        String s = tk.nextToken();
                        if (s.equalsIgnoreCase("name")) //$NON-NLS-1$
                            reclassFile = tk.nextToken().trim();
                        else if (s.equalsIgnoreCase("mapset")) //$NON-NLS-1$
                            reclassMapset = tk.nextToken().trim();
                    }
                }
                /* Instantiate the reclass table */
                reclassTable = new Vector<Object>();
                /* The next line holds the start value for categories */
                if ((line = cellhead.readLine()) == null)
                    return false;
                if (line.charAt(0) == '#') {
                    int reclassFirstCategory = Integer.parseInt(line.trim().substring(1));
                    /* Pad reclass table until the first reclass category */
                    for( int i = 0; i < reclassFirstCategory; i++ ) {
                        reclassTable.addElement(""); //$NON-NLS-1$
                    }
                } else {
                    /* Add an empty element for the 0th category */
                    reclassTable.addElement(""); //$NON-NLS-1$
                }
                /* Now read the reclass table */
                while( (line = cellhead.readLine()) != null ) {
                    reclassTable.addElement(new Integer(line));
                }
                /* Construct original data file path */
                reclassPath = locationPath + File.separator + reclassMapset + File.separator;
                /* Test for its existence */
                ds4 = new File(reclassPath + JGrassConstants.CELLHD + File.separator + reclassFile);
                if (!ds4.exists()) {
                    return false;
                }
                cellhead = new BufferedReader(new FileReader(ds4));
                // if (logger.isDebugEnabled())
                // logger.debug("map is a reclassed map, original="
                // + reclassPath + reclassFile
                // + ", reclassFirstCategory=" + reclassFirstCategory);
            } else {
                /* Push first line back onto buffered reader stack */
                cellhead.reset();
            }
            while( (line = cellhead.readLine()) != null ) {
                line = line.replaceFirst(":", "@@@@");
                StringTokenizer tok = new StringTokenizer(line, "@@@@"); //$NON-NLS-1$
                if (tok.countTokens() == 2) {
                    String key = tok.nextToken().trim();
                    String value = tok.nextToken().trim();
                    /* If key is 'ew resol' or 'ns resol' then store 'xx res' */
                    if (key.indexOf("resol") != -1) //$NON-NLS-1$
                        fileMapHeader.put(key.replaceAll("resol", "res"), value); //$NON-NLS-1$ //$NON-NLS-2$
                    else
                        fileMapHeader.put(key, value);
                }
            }

            /*
             * Setup file window object that holds the geographic limits of the file data.
             */
            fileWindow = null;
            if (fileMapHeader.containsKey("n-s res")) { //$NON-NLS-1$
                fileWindow = new JGrassRegion(Double.parseDouble(fileMapHeader.get("west")), //$NON-NLS-1$
                        Double.parseDouble(fileMapHeader.get("east")), //$NON-NLS-1$
                        Double.parseDouble(fileMapHeader.get("south")), //$NON-NLS-1$
                        Double.parseDouble(fileMapHeader.get("north")), //$NON-NLS-1$
                        Double.parseDouble(fileMapHeader.get("e-w res")), Double //$NON-NLS-1$
                                .parseDouble(fileMapHeader.get("n-s res"))); //$NON-NLS-1$
            } else if (fileMapHeader.containsKey("cols")) { //$NON-NLS-1$
                fileWindow = new JGrassRegion(Double.parseDouble(fileMapHeader.get("west")), //$NON-NLS-1$
                        Double.parseDouble(fileMapHeader.get("east")), //$NON-NLS-1$
                        Double.parseDouble(fileMapHeader.get("south")), //$NON-NLS-1$
                        Double.parseDouble(fileMapHeader.get("north")), //$NON-NLS-1$
                        Integer.parseInt(fileMapHeader.get("rows")), //$NON-NLS-1$
                        Integer.parseInt(fileMapHeader.get("cols"))); //$NON-NLS-1$
            } else {
                throw new IllegalArgumentException(
                        "Missing infor in the header file. Row/cols or resolution info have to be available.");
            }
            // if (logger.isDebugEnabled())
            // logger.debug("map file window: " + fileWindow.toString());

            if (!fileMapHeader.get("format").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                rasterMapType = new Integer(fileMapHeader.get("format")).intValue(); //$NON-NLS-1$
                if (rasterMapType > -1) {
                    rasterMapType++;
                    /*
                     * In Grass integers can be from 1 to 4 bytes. Jgrass will convert them all
                     * directly into an intger (4-bytes) at reding and decompressing time. Therefore
                     * the numberofbytespervalue is always 4.
                     */
                    numberOfBytesPerValue = 4;
                    /* Instantiate cell file object. */
                    File ds = null;
                    if (reclassPath == null)
                        ds = new File(mapsetPath + File.separator + JGrassConstants.CELL
                                + File.separator + fname);
                    else
                        ds = new File(reclassPath + JGrassConstants.CELL + File.separator
                                + reclassFile);
                    if (ds.exists()) {
                        cellFile = new RandomAccessFile(ds, "r"); //$NON-NLS-1$
                        if (cellFile == null) {
                            // if (logger.isDebugEnabled())
                            // logger
                            // .debug("cannot open integer map file, file="
                            // + ds.toString());
                            return false;
                        } else {
                            // if (logger.isDebugEnabled())
                            // logger.debug("opening integer map file: "
                            // + ds.toString());
                        }
                        /* Check if null file exists. */
                        nullFile = null;
                        if (reclassPath == null)
                            ds = new File(mapsetPath + File.separator + JGrassConstants.CELL_MISC
                                    + File.separator + filename + File.separator
                                    + JGrassConstants.CELLMISC_NULL);
                        else
                            ds = new File(reclassPath + JGrassConstants.CELL_MISC + File.separator
                                    + reclassFile + File.separator + JGrassConstants.CELLMISC_NULL);
                        if (ds.exists()) {
                            nullFile = new RandomAccessFile(ds, "r"); //$NON-NLS-1$
                            if (nullFile == null) {
                                isOldIntegerMap = false;
                                // if (logger.isDebugEnabled())
                                // logger.debug("cannot open null file: "
                                // + ds.toString());
                            } else {
                                isOldIntegerMap = true;
                                // if (logger.isDebugEnabled())
                                // logger.debug("opening null file: "
                                // + ds.toString());
                            }
                        }
                    } else {
                        // if (logger.isDebugEnabled())
                        // logger.error("integer map file doesn't exist, map="
                        // + filename);
                        return false;
                    }
                } else if (rasterMapType < 0) {
                    /*
                     * Read contents of 'cell_misc/name/f_format' file from the current mapset
                     */
                    File ds5 = null;
                    if (reclassPath == null)
                        ds5 = new File(mapsetPath + File.separator + JGrassConstants.CELL_MISC
                                + File.separator + fname + File.separator
                                + JGrassConstants.CELLMISC_FORMAT);
                    else
                        ds5 = new File(reclassPath + JGrassConstants.CELL_MISC + File.separator
                                + reclassFile + File.separator + JGrassConstants.CELLMISC_FORMAT);
                    if (ds5.exists()) {
                        /*
                         * if the file f_format exists, then we are talking about floating maps
                         */
                        BufferedReader cellmiscformat = new BufferedReader(new FileReader(ds5));
                        while( (line = cellmiscformat.readLine()) != null ) {
                            StringTokenizer tokk = new StringTokenizer(line, ":"); //$NON-NLS-1$
                            if (tokk.countTokens() == 2) {
                                String key = tokk.nextToken().trim();
                                String value = tokk.nextToken().trim();
                                fileMapHeader.put(key, value);
                            }
                        }
                        // assign the values
                        if (!fileMapHeader.get("type").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                            if ((fileMapHeader.get("type")) //$NON-NLS-1$
                                    .equalsIgnoreCase("double")) { //$NON-NLS-1$
                                rasterMapType = -2;
                                numberOfBytesPerValue = 8;
                            } else if ((fileMapHeader.get("type")) //$NON-NLS-1$
                                    .equalsIgnoreCase("float")) { //$NON-NLS-1$
                                rasterMapType = -1;
                                numberOfBytesPerValue = 4;
                            } else {
                                // if (logger.isDebugEnabled())
                                // logger.error("Type in the "
                                // + GrassLocation.CELLMISC_FORMAT
                                // + " file is not consistent");
                                return false;
                            }
                        } else {
                            // if (logger.isDebugEnabled())
                            // logger
                            // .error("FileSystem inconsistent. Floating type
                            // not defined");
                            return false;
                        }
                        cellmiscformat.close();
                    } else {
                        // if (logger.isDebugEnabled())
                        // logger
                        // .debug("floating point format file not found, file="
                        // + ds5.toString());
                        return false;
                    }
                    isOldIntegerMap = false;
                    /* Instantiate cell file and null file objects */
                    if (reclassPath == null)
                        ds5 = new File(mapsetPath + File.separator + JGrassConstants.FCELL
                                + File.separator + fname);
                    else
                        ds5 = new File(reclassPath + JGrassConstants.FCELL + File.separator
                                + reclassFile);
                    if (ds5.exists()) {
                        cellFile = new RandomAccessFile(ds5, "r"); //$NON-NLS-1$
                        if (cellFile == null) {
                            // if (logger.isDebugEnabled())
                            // logger
                            // .error("cannot open floating point map file,
                            // file: "
                            // + ds5.toString());
                            return false;
                        } else {
                            // if (logger.isDebugEnabled())
                            // logger
                            // .debug("opening floating point cell file: "
                            // + ds5.toString());
                        }
                        nullFile = null;
                        if (reclassPath == null)
                            ds5 = new File(mapsetPath + File.separator + JGrassConstants.CELL_MISC
                                    + File.separator + filename + File.separator
                                    + JGrassConstants.CELLMISC_NULL);
                        else
                            ds5 = new File(reclassPath + JGrassConstants.CELL_MISC + File.separator
                                    + reclassFile + File.separator + JGrassConstants.CELLMISC_NULL);
                        if (ds5.exists()) {
                            nullFile = new RandomAccessFile(ds5, "r"); //$NON-NLS-1$
                            if (nullFile == null) {
                                // if (logger.isDebugEnabled())
                                // logger.debug("cannot open null file: "
                                // + ds5.toString());
                            } else {
                                // if (logger.isDebugEnabled())
                                // logger.debug("opening null file: "
                                // + ds5.toString());
                            }
                        }
                    } else {
                        // if (logger.isDebugEnabled())
                        // logger.debug("cannot open file , file="
                        // + ds5.toString());
                        return false;
                    }
                }
            } else {
                // if (logger.isDebugEnabled())
                // logger
                // .error("FileSystem inconsistent. Fileformat not recognized");
                return false;
            }

            if (!fileMapHeader.get("compressed").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                compressed = new Integer(fileMapHeader.get("compressed")).intValue(); //$NON-NLS-1$
            } else {
                // if (logger.isDebugEnabled())
                // logger
                // .error("FileSystem inconsistent. Compression not defined");
                return false;
            }

            cellhead.close();
        } catch (FileNotFoundException e) {
            // if (logger.isDebugEnabled())
            // logger.error(DebugPanel.FormatExceptionTrace(e));
            return false;
        } catch (IOException e) {
            // if (logger.isDebugEnabled())
            // logger.error(DebugPanel.FormatExceptionTrace(e));
            return false;
        }
        return true;
    }

    /**
     * reads a row of data from the file into a byte array.
     * 
     * @param currentfilerow the current row to be extracted from the file
     * @param rowDataCache the byte array to store the unpacked row data
     * @return boolean TRUE for success, FALSE for failure.
     * @throws IOException
     * @throws DataFormatException
     */
    private boolean readRasterRow( int currentfilerow, byte[] rowDataCache, byte[] rowColorDataCache )
            throws IOException, DataFormatException {
        ByteBuffer rowBuffer = ByteBuffer.wrap(rowDataCache);
        ByteBuffer rowColorBuffer = ByteBuffer.wrap(rowColorDataCache);
        /*
         * Read the correct approximated row from the file. The row contents as saved in a cache for
         * along with the row number. If the row requested is the row in the cache then we do not
         * ned to read from the file.
         */

        // int currentfilecol;
        boolean iscompressed = (compressed == 1 ? true : false);

        /* Data window geographic boundaries */
        double activeewres = dataWindow.getWEResolution();
        // double activeewres2 = activeewres / 2;
        double activewest = dataWindow.getWest();

        /* Map file geographic limits */
        double filewest = fileWindow.getWest();
        double fileewres = fileWindow.getWEResolution();

        // System.out.println("currentfilerow="+currentfilerow+",
        // fileWindow.getRows()="+fileWindow.getRows());

        /* Reset row cache and read new row data */
        ByteBuffer rowCache = ByteBuffer.allocate(fileWindow.getCols()
                * ((rasterMapType == -2) ? 8 : 4));
        // rowCache.rewind();
        getMapRow(currentfilerow, rowCache, iscompressed);
        // rowCacheRow = currentfilerow;

        // if the northing is inside the file boundaries, calculate the values
        // for (double col = activewest; col < activeeast; col += activeewres)
        for( double col = 0; col < dataWindow.getCols(); col++ ) {
            /*
             * Calculate the column value of the data to be extracted from the row
             */
            double x = (((activewest + (col * activeewres)) - filewest) / fileewres);
            x = Math.floor(x);

            // currentfilecol = (int) (((activewest + (col * activeewres +
            // activeewres2)) - filewest) / fileewres);

            if (x < 0 || x >= fileWindow.getCols()) {
                // System.out.println("COL="+col+", X="+x+", NULL VALUE(1)");
                /*
                 * Depending on the map type we store a different 'NO VALUE' value.
                 */
                if (rasterMapType > 0) {
                    /*
                     * For integers the NaN pattern doesn't seem to so we we use the positive
                     * infinite value.
                     */
                    rowBuffer.putInt(Integer.MAX_VALUE);
                    if (colorTable != null)
                        colorTable.interpolateColorValue(rowColorBuffer, Integer.MAX_VALUE);
                } else if (rasterMapType == -1) {
                    /* For floats we use the Not A Number (NAN) value. */
                    rowBuffer.putFloat(Float.NaN);
                    if (colorTable != null)
                        colorTable.interpolateColorValue(rowColorBuffer, Float.NaN);
                } else if (rasterMapType == -2) {
                    /* For double values we use the NAN value. */
                    rowBuffer.putDouble(Double.NaN);
                    if (colorTable != null)
                        colorTable.interpolateColorValue(rowColorBuffer, Double.NaN);
                } else {
                    /* Don't know what to do. Probably throw some exception? */
                }
            } else if (readNullValueAtRowCol(currentfilerow, (int) x)) {
                // System.out.println("COL="+col+", X="+x+", NULL VALUE(2)");
                /*
                 * Depending on the map type we store a different 'NO VALUE' value.
                 */
                if (rasterMapType > 0) {
                    /*
                     * For integers the NaN pattern doesn't seem to so we we use the positive
                     * infinite value.
                     */
                    rowBuffer.putInt(Integer.MAX_VALUE);
                    if (colorTable != null)
                        colorTable.interpolateColorValue(rowColorBuffer, Integer.MAX_VALUE);
                } else if (rasterMapType == -1) {
                    /* For floats we use the Not A Number (NAN) value. */
                    rowBuffer.putFloat(Float.NaN);
                    if (colorTable != null)
                        colorTable.interpolateColorValue(rowColorBuffer, Float.NaN);
                } else if (rasterMapType == -2) {
                    /* For double values we use the NAN value. */
                    rowBuffer.putDouble(Double.NaN);
                    if (colorTable != null)
                        colorTable.interpolateColorValue(rowColorBuffer, Double.NaN);
                } else {
                    /* Don't know what to do. Probably throw some exception? */
                }
            } else {
                // System.out.println("COL="+col+", X="+x+", DATA VALUE");
                rowCache.position((int) x * numberOfBytesPerValue);
                if (rasterMapType > 0) {
                    /* Integers */
                    int cell = rowCache.getInt();
                    // System.out.println("x="+easthing+", y="+northing+",
                    // cell="+cell);
                    // System.out.print(cell+" ");

                    /* File is an integer map file with 0 = novalue */
                    if (cell == 0 && isOldIntegerMap) {
                        rowBuffer.putInt(Integer.MAX_VALUE);
                        if (colorTable != null)
                            colorTable.interpolateColorValue(rowColorBuffer, Integer.MAX_VALUE);
                    } else {
                        /* If map is a reclass then get the reclassed value */
                        if (reclassTable != null) {
                            cell = ((Integer) reclassTable.elementAt(cell)).intValue();
                        }
                        rowBuffer.putInt(cell);
                        if (colorTable != null)
                            colorTable.interpolateColorValue(rowColorBuffer, cell);
                        /* Update data range value */
                        if (cell < dataRange[0])
                            dataRange[0] = cell;
                        else if (cell > dataRange[1])
                            dataRange[1] = cell;
                    }
                } else if (rasterMapType == -1) {
                    /* Floating point map with float values. */
                    float cell = rowCache.getFloat();
                    if (reclassTable != null) {
                        cell = ((Integer) reclassTable.elementAt((int) cell)).floatValue();
                    }
                    rowBuffer.putFloat(cell);
                    if (colorTable != null)
                        colorTable.interpolateColorValue(rowColorBuffer, cell);
                    /* Update data range value */
                    if (cell < dataRange[0])
                        dataRange[0] = cell;
                    else if (cell > dataRange[1])
                        dataRange[1] = cell;
                } else if (rasterMapType == -2) {
                    /* Floating point map with double values. */
                    /*
                     * if (logger.isDebugEnabled()) logger.debug("RASTERREAD: rowCache size = " +
                     * rowCache.capacity() / 8 + " position = " + rowCache.position() / 8 + " total
                     * columns to read = " + dataWindow.getCols() + " actual column = " + col);
                     */
                    double cell = rowCache.getDouble();
                    /*
                     * if (logger.isDebugEnabled()) logger.debug("RASTERREAD: cell = " + cell);
                     */
                    if (reclassTable != null) {
                        cell = ((Integer) reclassTable.elementAt((int) cell)).doubleValue();
                    }
                    rowBuffer.putDouble(cell);
                    if (colorTable != null)
                        colorTable.interpolateColorValue(rowColorBuffer, cell);
                    /* Update data range value */
                    if (cell < dataRange[0])
                        dataRange[0] = cell;
                    else if (cell > dataRange[1])
                        dataRange[1] = cell;
                } else {
                    /* Don't know what to do. Probably throw some exception? */
                }
            }
        }
        // System.out.println();

        return true;
    }

    /**
     * SwingUtilities
     */
    private byte[] initNullRow() {
        int len = dataWindow.getCols() * numberOfBytesPerValue;
        byte[] nrow = new byte[len];

        if (rasterMapType > 0) {
            ByteBuffer src = ByteBuffer.allocate(4);
            src.putInt(Integer.MAX_VALUE);
            byte[] arr = src.array();
            for( int i = 0; i < len; i += 4 )
                System.arraycopy(arr, 0, nrow, i, 4);
        } else if (rasterMapType == -1) {
            ByteBuffer src = ByteBuffer.allocate(4);
            src.putFloat(Float.NaN);
            byte[] arr = src.array();
            for( int i = 0; i < len; i += 4 )
                System.arraycopy(arr, 0, nrow, i, 4);
        } else if (rasterMapType == -2) {
            ByteBuffer src = ByteBuffer.allocate(8);
            src.putDouble(Double.NaN);
            byte[] arr = src.array();
            for( int i = 0; i < len; i += 8 )
                System.arraycopy(arr, 0, nrow, i, 8);
        }

        return nrow;
    }

    /**
     * read a row of the map from the active region
     * 
     * @param currentrow
     * @param iscompressed
     * @return
     * @throws IOException
     * @throws DataFormatException
     */
    private void getMapRow( int currentrow, ByteBuffer rowdata, boolean iscompressed )
            throws IOException, DataFormatException {
        // if (logger.isDebugEnabled())
        // {
        // logger.debug("ACCESSING THE FILE at row: " + currentrow +
        // ", rasterMapType = " + rasterMapType +
        // ", numberOfBytesPerValue = " + numberOfBytesPerValue +
        // ", iscompressed = " + iscompressed);
        // }

        if (iscompressed) {
            /* Compressed maps */
            if (rasterMapType == -2) {
                /* Compressed double map */
                readCompressedFPRowByNumber(rowdata, currentrow, addressesofrows, cellFile,
                        numberOfBytesPerValue);
            } else if (rasterMapType == -1) {
                /* Compressed floating point map */
                readCompressedFPRowByNumber(rowdata, currentrow, addressesofrows, cellFile,
                        numberOfBytesPerValue);
            } else if (rasterMapType > 0) {
                /* Compressed integer map */
                readCompressedIntegerRowByNumber(rowdata, currentrow, addressesofrows, cellFile);
            } else {
                // if (logger.isDebugEnabled())
                // logger.error("format not double nor float");
            }
        } else {
            if (rasterMapType < 0) {
                /* Uncompressed floating point map */
                readUncompressedFPRowByNumber(rowdata, currentrow, cellFile, numberOfBytesPerValue);
            } else if (rasterMapType > 0) {
                /* Uncompressed integer map */
                readUncompressedIntegerRowByNumber(rowdata, currentrow, cellFile);
            } else {
                // if (logger.isDebugEnabled())
                // logger.error("Unknown case, iscompressed=" + iscompressed
                // + ", compressed=" + compressed + ", rasterMapType="
                // + rasterMapType);
            }
        }
        return;
    }

    /**
     * read a row of data from a compressed floating point map
     * 
     * @param rn
     * @param adrows
     * @param file
     * @param typeBytes
     * @return the ByteBuffer containing the data
     * @throws IOException
     * @throws DataFormatException
     */
    private void readCompressedFPRowByNumber( ByteBuffer rowdata, int rn, long[] adrows,
            RandomAccessFile thefile, int typeBytes ) throws DataFormatException, IOException {
        int offset = (int) (adrows[rn + 1] - adrows[rn]);
        /*
         * The fact that the file is compressed does not mean that the row is compressed. If the
         * first byte is 0 (49), then the row is compressed, otherwise (first byte = 48) the row has
         * to be read in simple XDR uncompressed format.
         */
        byte[] tmp = new byte[offset - 1];
        thefile.seek(adrows[rn]);
        int firstbyte = (thefile.read() & 0xff);
        if (firstbyte == 49) {
            /* The row is compressed. */
            // thefile.seek((long) adrows[rn] + 1);
            thefile.read(tmp, 0, offset - 1);
            Inflater decompresser = new Inflater();
            decompresser.setInput(tmp, 0, tmp.length);
            decompresser.inflate(rowdata.array());
            decompresser.end();
        } else if (firstbyte == 48) {
            /* The row is NOT compressed */
            // thefile.seek((long) (adrows[rn]));
            // if (thefile.read() == 48)
            // {
            // thefile.seek((long) (adrows[rn] + 1));
            thefile.read(rowdata.array(), 0, offset - 1);
            // }
        }
    }

    /**
     * read a row of data from an uncompressed floating point map
     * 
     * @param rn
     * @param file
     * @param typeBytes
     * @return the ByteBuffer containing the data
     * @throws IOException
     * @throws DataFormatException
     */
    private void readUncompressedFPRowByNumber( ByteBuffer rowdata, int rn,
            RandomAccessFile thefile, int typeBytes ) throws IOException, DataFormatException {
        int datanumber = fileWindow.getCols() * typeBytes;
        thefile.seek((rn * datanumber));

        thefile.read(rowdata.array());
    }

    /**
     * read a row of data from a compressed integer point map
     * 
     * @param rn
     * @param adrows
     * @param file
     * @return the ByteBuffer containing the data
     * @throws IOException
     */
    private void readCompressedIntegerRowByNumber( ByteBuffer rowdata, int rn, long[] adrows,
            RandomAccessFile thefile ) throws IOException, DataFormatException {
        int offset = (int) (adrows[rn + 1] - adrows[rn]);

        thefile.seek(adrows[rn]);
        /*
         * Read how many bytes the values are ex 1 => if encoded: 1 byte for the value and one byte
         * for the count = 2 2 => if encoded: 2 bytes for the value and one byte for the count = 3
         * etc... etc
         */
        int bytespervalue = (thefile.read() & 0xff);
        ByteBuffer cell = ByteBuffer.allocate(bytespervalue);
        int cellValue = 0;

        /* Create the buffer in which read the compressed row */
        byte[] tmp = new byte[offset - 1];
        thefile.read(tmp);
        ByteBuffer tmpBuffer = ByteBuffer.wrap(tmp);
        tmpBuffer.order(ByteOrder.nativeOrder());

        /*
         * Create the buffer in which read the decompressed row. The final decompressed row will
         * always contain 4-byte integer values
         */
        if ((offset - 1) == (bytespervalue * fileWindow.getCols())) {
            /* There is no compression in this row */
            for( int i = 0; i < offset - 1; i = i + bytespervalue ) {
                /* Read the value */
                tmpBuffer.get(cell.array());

                /*
                 * Integers can be of 1, 2, or 4 bytes. As rasterBuffer expects 4 byte integers we
                 * need to pad them with 0's. The order of the padding is determined by the
                 * ByteOrder of the buffer.
                 */
                if (bytespervalue == 1) {
                    cellValue = (cell.get(0) & 0xff);
                } else if (bytespervalue == 2) {
                    cellValue = cell.getShort(0);
                } else if (bytespervalue == 4) {
                    cellValue = cell.getInt(0);
                }
                // if (logger.isDebugEnabled()) logger.debug("tmpint=" + tmpint
                // );
                rowdata.putInt(cellValue);
            }
        } else {
            /*
             * If the row is compressed, then the values appear in pairs (like couples a party). The
             * couple is composed of the count and the value value (WARNING: this can be more than
             * one byte). Therefore, knowing the length of the compressed row we can calculate the
             * number of couples.
             */
            int couples = (offset - 1) / (1 + bytespervalue);

            for( int i = 0; i < couples; i++ ) {
                /* Read the count of values */
                int count = (tmpBuffer.get() & 0xff);

                /* Read the value */
                tmpBuffer.get(cell.array());

                /*
                 * Integers can be of 1, 2, or 4 bytes. As rasterBuffer expects 4 byte integers we
                 * need to pad them with 0's. The order of the padding is determined by the
                 * ByteOrder of the buffer.
                 */
                if (bytespervalue == 1) {
                    cellValue = (cell.get(0) & 0xff);
                } else if (bytespervalue == 2) {
                    cellValue = cell.getShort(0);
                } else if (bytespervalue == 4) {
                    cellValue = cell.getInt(0);
                }
                /*
                 * Now write the cell value the required number of times to the raster row data
                 * buffer.
                 */
                for( int j = 0; j < count; j++ ) {
                    // // if (logger.isDebugEnabled()) logger.debug(" " +
                    // tmpint);
                    rowdata.putInt(cellValue);
                }
            }
        }
    }

    /**
     * read a row of data from an uncompressed integer map
     * 
     * @param rn
     * @param thefile
     * @return
     * @throws IOException
     * @throws DataFormatException
     */
    private void readUncompressedIntegerRowByNumber( ByteBuffer rowdata, int rn,
            RandomAccessFile thefile ) throws IOException, DataFormatException {
        int cellValue = 0;
        ByteBuffer cell = ByteBuffer.allocate(rasterMapType);

        /* The number of bytes that are inside a row in the file. */
        int filerowsize = fileWindow.getCols() * rasterMapType;

        /* Position the file pointer to read the row */
        thefile.seek((rn * filerowsize));

        /* Read the row of data from the file */
        ByteBuffer tmpBuffer = ByteBuffer.allocate(filerowsize);
        thefile.read(tmpBuffer.array());

        /*
         * Transform the rasterMapType-size-values to a standard 4 bytes integer value
         */
        while( tmpBuffer.hasRemaining() ) {
            // read the value
            tmpBuffer.get(cell.array());

            /*
             * Integers can be of 1, 2, or 4 bytes. As rasterBuffer expects 4 byte integers we need
             * to pad them with 0's. The order of the padding is determined by the ByteOrder of the
             * buffer.
             */
            if (rasterMapType == 1) {
                cellValue = (cell.get(0) & 0xff);
            } else if (rasterMapType == 2) {
                cellValue = cell.getShort(0);
            } else if (rasterMapType == 4) {
                cellValue = cell.getInt(0);
            }
            // if (logger.isDebugEnabled()) logger.debug("tmpint=" + cellValue
            // );
            rowdata.putInt(cellValue);
        }
    }

    /**
     * read the null value from the null file (if it exists) and returns the information about the
     * particular cell (true if it is novalue, false if it is not a novalue
     * 
     * @param currentfilerow
     * @param currentfilecol
     * @return
     */
    private boolean readNullValueAtRowCol( int currentfilerow, int currentfilecol )
            throws IOException {
        /*
         * If the null file doesn't exist and the map is an integer, than it is an old integer-map
         * format, where the novalues are the cells that contain the values 0
         */
        if (nullFile != null) {
            long byteperrow = (long) Math.ceil(fileWindow.getCols() / 8.0); // in the
            // null
            // map of
            // cell_misc
            long currentByte = (long) Math.ceil((currentfilecol + 1) / 8.0); // in the
            // null
            // map

            // currentfilerow starts from 0, so it is the row before the one we
            // need
            long byteToRead = (byteperrow * currentfilerow) + currentByte;

            nullFile.seek(byteToRead - 1);

            int bitposition = (currentfilecol) % 8;

            byte[] thetmp = new byte[1];
            thetmp[0] = nullFile.readByte();
            BitSet tmp = FileUtilities.fromByteArray(thetmp);

            boolean theBit = tmp.get(7 - bitposition);
            /*
             * if (theBit) { System.out.println("1 at position: " + (7-bitposition) + " due to
             * bitposition: " + bitposition); } else { System.out.println("0 at position: " +
             * (7-bitposition) + " due to bitposition: " + bitposition); }
             */
            return theBit;

        }
        // else
        // {
        // /* There is no null file around */
        // if (rasterMapType > 0)
        // {
        // // isOldIntegerMap = true;
        // return false;
        // }
        // else
        // {
        // //throw some exception
        // return false;
        // }
        //
        // }
        return false;

    }

    /**
     * Utility method to read a double matrix of data from a double raster map
     */
    private Object doubleFromDoubleGridRead() {
        // if (logger.isDebugEnabled())
        // logger.debug("reading inside of doubleFromDoubleGridRead");

        int nx = dataWindow.getRows();
        int ny = dataWindow.getCols();
        // if (logger.isDebugEnabled())
        // logger.debug("ROWS AND COLS OF READ MATRIX = nx: " + nx + " ny: "
        // + ny);

        /*
         * this defines the tipe of matrix to return: 0 = normal type[][] matrix
         */
        if (matrixType == 0) {
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[i][k] = tmp;
                    } else {
                        ((double[][]) dataObject)[i][k] = ((Double) novalue).doubleValue();
                    }
                }
            }
        }
        /*
         * 1 = type[][] matrix indexed from [1][1]
         */
        else if (matrixType == 1) {
            dataObject = new double[nx + 1][ny + 1];
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[i + 1][k + 1] = tmp;
                    } else {
                        ((double[][]) dataObject)[i + 1][k + 1] = ((Double) novalue).doubleValue();
                    }
                }
            }
        }
        /*
         * 2 = transposed of the original type[][] matrix
         */
        else if (matrixType == 2) {
            dataObject = new double[ny][nx];

            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[k][i] = tmp;
                        // System.out.println("TMP = " + tmp + " and the used
                        // novalue is = " + ((Double) novalue).doubleValue() + "
                        // TMP CHOSEN");
                    } else {
                        ((double[][]) dataObject)[k][i] = ((Double) novalue).doubleValue();
                        // System.out.println("TMP = " + tmp + " and the used
                        // novalue is = " + ((Double) novalue).doubleValue() + "
                        // NOVALUE CHOSEN");
                    }
                }
            }
        }
        /*
         * 3 = transposed of the original type[][] matrix and indexed by [1][1]
         */
        else if (matrixType == 3) {
            double tmp;
            dataObject = new double[ny + 1][nx + 1];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[k + 1][i + 1] = tmp;
                    } else {
                        ((double[][]) dataObject)[k + 1][i + 1] = ((Double) novalue).doubleValue();
                    }
                }
            }
        } else {
            // throw something
        }
        return dataObject;
    }

    /**
     * Utility method to read a double matrix of data from a float raster map
     */
    private Object doubleFromFloatGridRead() {
        int nx = dataWindow.getRows();
        int ny = dataWindow.getCols();

        /*
         * this defines the tipe of matrix to return: 0 = normal type[][] matrix
         */
        if (matrixType == 0) {
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[i][k] = tmp;
                    } else {
                        ((double[][]) dataObject)[i][k] = ((Double) novalue).doubleValue();
                    }
                }
            }
        }
        /*
         * 1 = type[][] matrix indexed from [1][1]
         */
        else if (matrixType == 1) {
            dataObject = new double[nx + 1][ny + 1];
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[i + 1][k + 1] = tmp;
                    } else {
                        ((double[][]) dataObject)[i + 1][k + 1] = ((Double) novalue).doubleValue();
                    }
                }
            }
        }
        /*
         * 2 = transposed of the original type[][] matrix
         */
        else if (matrixType == 2) {
            double tmp;
            dataObject = new double[ny][nx];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[k][i] = tmp;
                    } else {
                        ((double[][]) dataObject)[k][i] = ((Double) novalue).doubleValue();
                    }
                }
            }
        }
        /*
         * 3 = transposed of the original type[][] matrix and indexed by [1][1]
         */
        else if (matrixType == 3) {
            double tmp;
            dataObject = new double[ny + 1][nx + 1];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[k + 1][i + 1] = tmp;
                    } else {
                        ((double[][]) dataObject)[k + 1][i + 1] = ((Double) novalue).doubleValue();
                    }
                }
            }
        } else {
            // throw something
        }
        return dataObject;
    }

    /**
     * Utility method to read a double matrix of data from a Integer raster map
     */
    private Object doubleFromIntGridRead() {
        int nx = dataWindow.getRows();
        int ny = dataWindow.getCols();

        /*
         * this defines the tipe of matrix to return: 0 = normal type[][] matrix
         */
        if (matrixType == 0) {
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getInt();
                    // the value has to be different from NaN
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[i][k] = tmp;
                    } else {
                        ((double[][]) dataObject)[i][k] = ((Double) novalue).doubleValue();
                    }
                }
            }
        }
        /*
         * 1 = type[][] matrix indexed from [1][1]
         */
        else if (matrixType == 1) {
            dataObject = new double[nx + 1][ny + 1];
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    /*
                     * integers can be of 1, 2, 3 or 4 bytes. Need to 0-pad them to get the value as
                     * a java integer
                     */
                    ByteBuffer tmpbuf = ByteBuffer.allocate(4);
                    // padding
                    for( int j = 0; j < (4 - rasterMapType); j++ ) {
                        tmpbuf.put((byte) 0);
                    }
                    for( int j = 0; j < rasterMapType; j++ ) {
                        tmpbuf.put(rasterByteBuffer.get());
                    }
                    tmpbuf.rewind();
                    tmp = tmpbuf.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[i + 1][k + 1] = tmp;
                    } else {
                        ((double[][]) dataObject)[i + 1][k + 1] = ((Double) novalue).doubleValue();
                    }
                }
            }
        }
        /*
         * 2 = transposed of the original type[][] matrix
         */
        else if (matrixType == 2) {
            dataObject = new double[ny][nx];
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    /*
                     * integers can be of 1, 2, 3 or 4 bytes. Need to 0-pad them to get the value as
                     * a java integer
                     */
                    ByteBuffer tmpbuf = ByteBuffer.allocate(4);
                    // padding
                    for( int j = 0; j < (4 - rasterMapType); j++ ) {
                        tmpbuf.put((byte) 0);
                    }
                    for( int j = 0; j < rasterMapType; j++ ) {
                        tmpbuf.put(rasterByteBuffer.get());
                    }
                    tmpbuf.rewind();
                    tmp = tmpbuf.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[k][i] = tmp;
                    } else {
                        ((double[][]) dataObject)[k][i] = ((Double) novalue).doubleValue();
                    }
                }
            }
        }
        /*
         * 3 = transposed of the original type[][] matrix and indexed by [1][1]
         */
        else if (matrixType == 3) {
            double tmp;
            dataObject = new double[ny + 1][nx + 1];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    /*
                     * integers can be of 1, 2, 3 or 4 bytes. Need to 0-pad them to get the value as
                     * a java integer
                     */
                    ByteBuffer tmpbuf = ByteBuffer.allocate(4);
                    // padding
                    for( int j = 0; j < (4 - rasterMapType); j++ ) {
                        tmpbuf.put((byte) 0);
                    }
                    for( int j = 0; j < rasterMapType; j++ ) {
                        tmpbuf.put(rasterByteBuffer.get());
                    }
                    tmpbuf.rewind();
                    tmp = tmpbuf.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[k + 1][i + 1] = tmp;
                    } else {
                        ((double[][]) dataObject)[k + 1][i + 1] = ((Double) novalue).doubleValue();
                    }
                }
            }
        } else {
            // throw something
        }
        return dataObject;
    }

    /**
     * Utility method to read a float matrix of data from a double raster map
     */
    private Object floatFromDoubleGridRead() {
        int nx = dataWindow.getRows();
        int ny = dataWindow.getCols();

        /*
         * this defines the tipe of matrix to return: 0 = normal type[][] matrix
         */
        if (matrixType == 0) {
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = (float) rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[i][k] = tmp;
                    } else {
                        ((float[][]) dataObject)[i][k] = ((Float) novalue).floatValue();
                    }
                }
            }
        }
        /*
         * 1 = type[][] matrix indexed from [1][1]
         */
        else if (matrixType == 1) {
            dataObject = new float[nx + 1][ny + 1];
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = (float) rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[i + 1][k + 1] = tmp;
                    } else {
                        ((float[][]) dataObject)[i + 1][k + 1] = ((Float) novalue).floatValue();
                    }
                }
            }
        }
        /*
         * 2 = transposed of the original type[][] matrix
         */
        else if (matrixType == 2) {
            dataObject = new float[ny][nx];
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = (float) rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[k][i] = tmp;
                    } else {
                        ((float[][]) dataObject)[k][i] = ((Float) novalue).floatValue();
                    }
                }
            }
        }
        /*
         * 3 = transposed of the original type[][] matrix and indexed by [1][1]
         */
        else if (matrixType == 3) {
            double tmp;
            dataObject = new float[ny + 1][nx + 1];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = (float) rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((double[][]) dataObject)[k + 1][i + 1] = tmp;
                    } else {
                        ((double[][]) dataObject)[k + 1][i + 1] = ((Float) novalue).floatValue();
                    }
                }
            }
        } else {
            // throw something
        }
        return dataObject;
    }

    /**
     * Utility method to read a float matrix of data from a float raster map
     */
    private Object floatFromFloatGridRead() {
        int nx = dataWindow.getRows();
        int ny = dataWindow.getCols();

        /*
         * this defines the tipe of matrix to return: 0 = normal type[][] matrix
         */
        if (matrixType == 0) {
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[i][k] = tmp;
                    } else {
                        ((float[][]) dataObject)[i][k] = ((Float) novalue).floatValue();
                    }
                }
            }
        }
        /*
         * 1 = type[][] matrix indexed from [1][1]
         */
        else if (matrixType == 1) {
            dataObject = new float[nx + 1][ny + 1];
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[i + 1][k + 1] = tmp;
                    } else {
                        ((float[][]) dataObject)[i + 1][k + 1] = ((Float) novalue).floatValue();
                    }
                }
            }
        }
        /*
         * 2 = transposed of the original type[][] matrix
         */
        else if (matrixType == 2) {
            dataObject = new float[ny][nx];
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[k][i] = tmp;
                    } else {
                        ((float[][]) dataObject)[k][i] = ((Float) novalue).floatValue();
                    }
                }
            }
        }
        /*
         * 3 = transposed of the original type[][] matrix and indexed by [1][1]
         */
        else if (matrixType == 3) {
            float tmp;
            dataObject = new float[ny + 1][nx + 1];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[k + 1][i + 1] = tmp;
                    } else {
                        ((float[][]) dataObject)[k + 1][i + 1] = ((Float) novalue).floatValue();
                    }
                }
            }
        } else {
            // throw something
        }
        return dataObject;
    }

    /**
     * Utility method to read a float matrix of data from a Integer raster map
     */
    private Object floatFromIntGridRead() {
        int nx = dataWindow.getRows();
        int ny = dataWindow.getCols();

        /*
         * this defines the tipe of matrix to return: 0 = normal type[][] matrix
         */
        if (matrixType == 0) {
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[i][k] = tmp;
                    } else {
                        ((float[][]) dataObject)[i][k] = ((Float) novalue).floatValue();
                    }
                }
            }
        }
        /*
         * 1 = type[][] matrix indexed from [1][1]
         */
        else if (matrixType == 1) {
            dataObject = new float[nx + 1][ny + 1];
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    /*
                     * integers can be of 1, 2, 3 or 4 bytes. Need to 0-pad them to get the value as
                     * a java integer
                     */
                    ByteBuffer tmpbuf = ByteBuffer.allocate(4);
                    // padding
                    for( int j = 0; j < (4 - rasterMapType); j++ ) {
                        tmpbuf.put((byte) 0);
                    }
                    for( int j = 0; j < rasterMapType; j++ ) {
                        tmpbuf.put(rasterByteBuffer.get());
                    }
                    tmpbuf.rewind();
                    tmp = tmpbuf.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[i + 1][k + 1] = tmp;
                    } else {
                        ((float[][]) dataObject)[i + 1][k + 1] = ((Float) novalue).floatValue();
                    }
                }
            }
        }
        /*
         * 2 = transposed of the original type[][] matrix
         */
        else if (matrixType == 2) {
            dataObject = new float[ny][nx];
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    /*
                     * integers can be of 1, 2, 3 or 4 bytes. Need to 0-pad them to get the value as
                     * a java integer
                     */
                    ByteBuffer tmpbuf = ByteBuffer.allocate(4);
                    // padding
                    for( int j = 0; j < (4 - rasterMapType); j++ ) {
                        tmpbuf.put((byte) 0);
                    }
                    for( int j = 0; j < rasterMapType; j++ ) {
                        tmpbuf.put(rasterByteBuffer.get());
                    }
                    tmpbuf.rewind();
                    tmp = tmpbuf.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[k][i] = tmp;
                    } else {
                        ((float[][]) dataObject)[k][i] = ((Float) novalue).floatValue();
                    }
                }
            }
        }
        /*
         * 3 = transposed of the original type[][] matrix and indexed by [1][1]
         */
        else if (matrixType == 3) {
            float tmp;
            dataObject = new float[ny + 1][nx + 1];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    /*
                     * integers can be of 1, 2, 3 or 4 bytes. Need to 0-pad them to get the value as
                     * a java integer
                     */
                    ByteBuffer tmpbuf = ByteBuffer.allocate(4);
                    // padding
                    for( int j = 0; j < (4 - rasterMapType); j++ ) {
                        tmpbuf.put((byte) 0);
                    }
                    for( int j = 0; j < rasterMapType; j++ ) {
                        tmpbuf.put(rasterByteBuffer.get());
                    }
                    tmpbuf.rewind();
                    tmp = tmpbuf.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((float[][]) dataObject)[k + 1][i + 1] = tmp;
                    } else {
                        ((float[][]) dataObject)[k + 1][i + 1] = ((Float) novalue).floatValue();
                    }
                }
            }
        } else {
            // throw something
        }
        return dataObject;
    }

    /**
     * Utility method to read a int matrix of data from a double raster map
     */
    private Object intFromDoubleGridRead() {
        int nx = dataWindow.getRows();
        int ny = dataWindow.getCols();

        /*
         * this defines the tipe of matrix to return: 0 = normal type[][] matrix
         */
        if (matrixType == 0) {
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[i][k] = (int) tmp;
                    } else {
                        ((int[][]) dataObject)[i][k] = ((Integer) novalue).intValue();
                    }
                }
            }
        }
        /*
         * 1 = type[][] matrix indexed from [1][1]
         */
        else if (matrixType == 1) {
            dataObject = new int[nx + 1][ny + 1];
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[i + 1][k + 1] = (int) tmp;
                    } else {
                        ((int[][]) dataObject)[i + 1][k + 1] = ((Integer) novalue).intValue();
                    }
                }
            }
        }
        /*
         * 2 = transposed of the original type[][] matrix
         */
        else if (matrixType == 2) {
            dataObject = new int[ny][nx];
            double tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[k][i] = (int) tmp;
                    } else {
                        ((int[][]) dataObject)[k][i] = ((Integer) novalue).intValue();
                    }
                }
            }
        }
        /*
         * 3 = transposed of the original type[][] matrix and indexed by [1][1]
         */
        else if (matrixType == 3) {
            double tmp;
            dataObject = new int[ny + 1][nx + 1];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getDouble();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[k + 1][i + 1] = (int) tmp;
                    } else {
                        ((int[][]) dataObject)[k + 1][i + 1] = ((Integer) novalue).intValue();
                    }
                }
            }
        } else {
            // throw something
        }
        return dataObject;
    }

    /**
     * Utility method to read a int matrix of data from a float raster map
     */
    private Object intFromFloatGridRead() {
        int nx = dataWindow.getRows();
        int ny = dataWindow.getCols();

        /*
         * this defines the tipe of matrix to return: 0 = normal type[][] matrix
         */
        if (matrixType == 0) {
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[i][k] = (int) tmp;
                    } else {
                        ((int[][]) dataObject)[i][k] = ((Integer) novalue).intValue();
                    }
                }
            }
        }
        /*
         * 1 = type[][] matrix indexed from [1][1]
         */
        else if (matrixType == 1) {
            dataObject = new int[nx + 1][ny + 1];
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[i + 1][k + 1] = (int) tmp;
                    } else {
                        ((int[][]) dataObject)[i + 1][k + 1] = ((Integer) novalue).intValue();
                    }
                }
            }
        }
        /*
         * 2 = transposed of the original type[][] matrix
         */
        else if (matrixType == 2) {
            dataObject = new int[ny][nx];
            float tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[k][i] = (int) tmp;
                    } else {
                        ((int[][]) dataObject)[k][i] = ((Integer) novalue).intValue();
                    }
                }
            }
        }
        /*
         * 3 = transposed of the original type[][] matrix and indexed by [1][1]
         */
        else if (matrixType == 3) {
            float tmp;
            dataObject = new int[ny + 1][nx + 1];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getFloat();
                    // the value has to be different from NaN
                    if (tmp == tmp) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[k + 1][i + 1] = (int) tmp;
                    } else {
                        ((int[][]) dataObject)[k + 1][i + 1] = ((Integer) novalue).intValue();
                    }
                }
            }
        } else {
            // throw something
        }
        return dataObject;
    }

    /**
     * Utility method to read a integer matrix of data from a Integer raster map
     */
    private Object intFromIntGridRead() {
        int nx = dataWindow.getRows();
        int ny = dataWindow.getCols();

        /*
         * this defines the tipe of matrix to return: 0 = normal type[][] matrix
         */
        if (matrixType == 0) {
            int tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    tmp = rasterByteBuffer.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[i][k] = tmp;
                    } else {
                        ((int[][]) dataObject)[i][k] = ((Integer) novalue).intValue();
                    }
                }
            }
        }
        /*
         * 1 = type[][] matrix indexed from [1][1]
         */
        else if (matrixType == 1) {
            dataObject = new int[nx + 1][ny + 1];
            int tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    /*
                     * integers can be of 1, 2, 3 or 4 bytes. Need to 0-pad them to get the value as
                     * a java integer
                     */
                    ByteBuffer tmpbuf = ByteBuffer.allocate(4);
                    // padding
                    for( int j = 0; j < (4 - rasterMapType); j++ ) {
                        tmpbuf.put((byte) 0);
                    }
                    for( int j = 0; j < rasterMapType; j++ ) {
                        tmpbuf.put(rasterByteBuffer.get());
                    }
                    tmpbuf.rewind();
                    tmp = tmpbuf.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[i + 1][k + 1] = tmp;
                    } else {
                        ((int[][]) dataObject)[i + 1][k + 1] = ((Integer) novalue).intValue();
                    }
                }
            }
        }
        /*
         * 2 = transposed of the original type[][] matrix
         */
        else if (matrixType == 2) {
            dataObject = new int[ny][nx];
            int tmp;
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    /*
                     * integers can be of 1, 2, 3 or 4 bytes. Need to 0-pad them to get the value as
                     * a java integer
                     */
                    ByteBuffer tmpbuf = ByteBuffer.allocate(4);
                    // padding
                    for( int j = 0; j < (4 - rasterMapType); j++ ) {
                        tmpbuf.put((byte) 0);
                    }
                    for( int j = 0; j < rasterMapType; j++ ) {
                        tmpbuf.put(rasterByteBuffer.get());
                    }
                    tmpbuf.rewind();
                    tmp = tmpbuf.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[k][i] = tmp;
                    } else {
                        ((int[][]) dataObject)[k][i] = ((Integer) novalue).intValue();
                    }
                }
            }
        }
        /*
         * 3 = transposed of the original type[][] matrix and indexed by [1][1]
         */
        else if (matrixType == 3) {
            int tmp;
            dataObject = new int[ny + 1][nx + 1];
            for( int i = 0; i < nx; i++ ) {
                for( int k = 0; k < ny; k++ ) {
                    /*
                     * integers can be of 1, 2, 3 or 4 bytes. Need to 0-pad them to get the value as
                     * a java integer
                     */
                    ByteBuffer tmpbuf = ByteBuffer.allocate(4);
                    // padding
                    for( int j = 0; j < (4 - rasterMapType); j++ ) {
                        tmpbuf.put((byte) 0);
                    }
                    for( int j = 0; j < rasterMapType; j++ ) {
                        tmpbuf.put(rasterByteBuffer.get());
                    }
                    tmpbuf.rewind();
                    tmp = tmpbuf.getInt();
                    // the value has to be different from max_value
                    if (tmp == tmp && tmp != Integer.MAX_VALUE) {
                        // set the range
                        setRange(tmp, i, k);
                        ((int[][]) dataObject)[k + 1][i + 1] = tmp;
                    } else {
                        ((int[][]) dataObject)[k + 1][i + 1] = ((Integer) novalue).intValue();
                    }
                }
            }
        } else {
            // throw something
        }
        return dataObject;
    }

    /**
     * @param tmp
     */
    private void setRange( double tmp, int i, int k ) {
        // set the range
        // if (logger.isDebugEnabled()) logger.debug("RANGEVALUE = " + tmp);
        if (i == 0 && k == 0 && tmp == tmp) {
            range[0] = tmp;
            range[1] = tmp;
        } else {
            if (tmp < range[0] && tmp == tmp)
                range[0] = tmp;
            if (tmp > range[1] && tmp == tmp)
                range[1] = tmp;
        }

    }

    /**
     * retrieve the range values of the map
     */
    public double[] getRange() {
        return range;
    }
}
