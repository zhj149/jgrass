package eu.hydrologis.jgrass.operations.jai;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import javax.imageio.stream.FileCacheImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.TiledImage;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ForwardSeekableStream;
import com.sun.media.jai.codecimpl.util.DataBufferDouble;
import com.sun.media.jai.codecimpl.util.DataBufferFloat;
import com.sun.media.jai.codecimpl.util.RasterFactory;

/**
 * A collection of useful function.
 * <p>
 * There is a lot of JAI utility and some system utility.
 * <p>
 * <p>
 * 18 Feb 2009 testTiff org.andreis.daniele.util UtilityJAI.java
 *</p>
 * 
 * @author <a href="mailto:daniele.andreis@gmail.com>daniele andreis</a>
 * @version beta
 */
public class UtilityJAI {
    // some class to get the system information
    static Properties p = System.getProperties();
    static String propertyOs = p.getProperty("os.name");
    static long totalMemory = Runtime.getRuntime().totalMemory();
    static long maxMemory = Runtime.getRuntime().maxMemory();

    /**
     * Print on the console all available output devices
     */
    public void getGraphicsDevices() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        // list all fonts font families on the platform
        System.out.println("****START LISTING FONTS****");
        GraphicsDevice[] fonts = ge.getScreenDevices();
        for( int i = 0; i < fonts.length; i++ ) {
            System.out.println("AVAILABLE FONTS; i: " + i + " FONT NAME: " + fonts[i].toString());
        }
        System.out.println("****STOP LISTING FONTS****");
        GraphicsDevice dscreen = ge.getDefaultScreenDevice();
        System.out.println("DEFAULT SCREEN ID: " + dscreen.getIDstring() + " DEVICE TYPE: "
                + dscreen.getType());
        // the following gets an array of screen devices;
        // the number is usually one but sometimes many
        GraphicsDevice[] gs = ge.getScreenDevices();
        for( int i = 0; i < gs.length; i++ ) {
            GraphicsDevice gd = gs[i];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            for( int j = 0; j < gc.length; j++ ) {
                gc[j].getDevice().getAvailableAcceleratedMemory();
                Rectangle gcBounds = gc[j].getBounds();
                System.out.println("SCREEN DEVICE #: " + j +
                // " TYPE: " +gc[j].getDevice().getConfigurations();
                        // +
                        // " x bounds: " + gcBounds.x +
                        " y bounds: " + gcBounds.y);
            }
        }
    }

    /**
     * Print some system properties
     */
    static public void getPcProperties() {
        System.out.println(propertyOs);
        System.out.println(p.getProperty("os.arch"));
        System.out.println("Total Memory" + totalMemory);
        System.out.println("Max Memory" + maxMemory);
    }

    /**
     * Generate a Random Int matrix.
     * 
     * @param size the dimension of the matrix.
     * @return an int matrix which dimension is size.
     */
    static public int[][] intMatrix( int size ) {
        Random random = new Random();

        int[][] matrix = new int[size][size];
        for( int j = 0; j < matrix[0].length; j++ ) {
            for( int i = 0; i < matrix.length; i++ ) {
                matrix[i][j] = random.nextInt(100);
            }
        }
        return matrix;
    }

    /**
     * Generate a Random Double matrix.
     * 
     * @param size the dimension of the matrix.
     * @return an double matrix which dimension is size.
     */

    static public double[][] doubleMatrix( int size ) {
        Random random = new Random();

        double[][] matrix = new double[size][size];
        for( int j = 0; j < matrix[0].length; j++ ) {
            for( int i = 0; i < matrix.length; i++ ) {
                matrix[i][j] = 100 * random.nextDouble();
            }
        }
        return matrix;
    }

    /**
     * Generate a Random Float matrix.
     * 
     * @param size the dimension of the matrix.
     * @return a Float matrix which dimension is size.
     */
    static public float[][] floatMatrix( int size ) {
        Random random = new Random();

        float[][] matrix = new float[size][size];
        for( int j = 0; j < matrix[0].length; j++ ) {
            for( int i = 0; i < matrix.length; i++ ) {
                matrix[i][j] = 100 * random.nextFloat();
            }
        }
        return matrix;
    }

    /**
     * Create a Raster object from an int matrix.
     * 
     * @param valueMatrix is the databuffer of the Raster.
     * @return a raster.
     */

    public static Raster getNewRaster( int[][] valueMatrix ) {
        // the dimension of raster data
        int height = valueMatrix.length;
        int width = valueMatrix[0].length;
        // the origin of the x and y axes
        Point p = new Point(0, 0);
        // the array which will fill the dataBuffer (a raster data is a
        // DataBuffer organised in order to a sampleModel.
        int[] valueArray = new int[height * width];
        DataBufferInt buffer = new DataBufferInt(valueArray, height * width);
        int i = 0;

        for( int j = 0; j < valueMatrix[0].length; j++ ) {
            for( int k = 0; k < valueMatrix.length; k++ ) {
                valueArray[i] = valueMatrix[k][j];
                i++;
            }
        }

        SampleModel sm = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_INT, width, height,
                1);
        return Raster.createWritableRaster(sm, buffer, p);
    }

    /**
     * Create a Raster from an double matrix.
     * 
     * @param valueMatrix is the databuffer of the Raster.
     * @return a raster.
     */
    public static Raster getNewRaster( double[][] valueMatrix ) {
        // the dimension of raster data
        int height = valueMatrix.length;
        int width = valueMatrix[0].length;
        // the origin of the x and y axes
        Point p = new Point(0, 0);
        // the array which will fill the dataBuffer (a raster data is a
        // DataBuffer organised in order to a sampleModel.
        double[] valueArray = new double[height * width];
        DataBufferDouble buffer = new DataBufferDouble(valueArray, height * width);
        int i = 0;

        for( int j = 0; j < valueMatrix[0].length; j++ ) {
            for( int k = 0; k < valueMatrix.length; k++ ) {
                valueArray[i] = valueMatrix[k][j];
                i++;
            }
        }

        SampleModel sm = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_DOUBLE, width,
                height, 1);
        return Raster.createWritableRaster(sm, buffer, p);
    }

    /**
     * Create a Raster from an float matrix.
     * 
     * @param valueMatrix is the databuffer of the Raster.
     * @return a raster.
     */
    public static Raster getNewRaster( float[][] valueMatrix ) {
        // the dimension of raster data
        int height = valueMatrix.length;
        int width = valueMatrix[0].length;
        // the origin of the x and y axes
        Point p = new Point(0, 0);
        // the array which will fill the dataBuffer (a raster data is a
        // DataBuffer organised in order to a sampleModel.
        float[] valueArray = new float[height * width];
        DataBufferFloat buffer = new DataBufferFloat(valueArray, height * width);
        int i = 0;

        for( int j = 0; j < valueMatrix[0].length; j++ ) {
            for( int k = 0; k < valueMatrix.length; k++ ) {
                valueArray[i] = valueMatrix[k][j];
                i++;
            }
        }

        SampleModel sm = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT, width,
                height, 1);
        return Raster.createWritableRaster(sm, buffer, p);
    }

    /**
     * Read a file and then allocate the value in a PlanarImage.
     * <p>
     * This method read a file via a <b>FileSeekableStream</b>. </b>
     * 
     * @param fileName the path+file name of the Image file.
     * @param sizeTiling the tiles size.
     * @return a PlanarImage from a file.
     */

    public static PlanarImage getDataSourceImageFromFile( String fileName, int sizeTiling ) {
        // create an input file stream
        // ImageInputStream iis = ImageIO.createImageInputStream(f);
        // InputStream inputStream = new FileInputStream(f);
        File f = new File(fileName);
        FileSeekableStream iis;
        try {
            iis = new FileSeekableStream(f);
            ParameterBlockJAI pbj = new ParameterBlockJAI("ImageRead");
            RenderingHints hints = getRenderingHintsTailing(sizeTiling, sizeTiling);
            pbj.setParameter("Input", iis);
            // N.B. when a operation is called, the first parameter is the name
            // of
            // the operation, then there is the zero arguments which is a
            // ParameterBlock and finally there is the RenderingHints which
            // contains
            // some information about the layout (tiling) and other
            return JAI.create("ImageRead", pbj, hints);

        } catch (IOException e) {
            e.printStackTrace();
            System.out
                    .println("There is some problem in a data flux in the SpeedPlanarImageTiling class");
            return null;
        }

    }

    /**
     * Read a file and then allocate the value in a PlanarImage.
     * <p>
     * This is a generic method to read a file throught several <b>InputStream</b>.
     *</p>
     * 
     * @param iis is a generic InputStream, I have use it to run the program with
     *        {@link ForwardSeekableStream} the path+file name of the Image file.
     * @param sizeTiling the tiles size.
     * @return a PlanarImage from a file.
     */
    public static PlanarImage getDataSourceImageFromFile( InputStream iis, int sizeTiling ) {

        ParameterBlockJAI pbj = new ParameterBlockJAI("ImageRead");
        RenderingHints hints = getRenderingHintsTailing(sizeTiling, sizeTiling);
        pbj.setParameter("Input", iis);
        return JAI.create("ImageRead", pbj, hints);

    }

    /**
     * Read a file and then allocate the value in a PlanarImage
     * <p>
     * This method read a file via a <b>FileCacheImageInputStream</b>.
     *</p>
     * 
     * @param iis is a FileChaceImageInputStream, I have use it to run the program with program eith
     *        a cache in read.
     * @param sizeTiling the tiles size.
     */
    public static PlanarImage getDataSourceImageFromFile( FileCacheImageInputStream iis,
            int sizeTiling ) {

        ParameterBlockJAI pbj = new ParameterBlockJAI("ImageRead");
        RenderingHints hints = getRenderingHintsTailing(sizeTiling, sizeTiling);
        pbj.setParameter("Input", iis);
        return JAI.create("ImageRead", pbj, hints);

    }

    /**
     * Create a RenderingHints.
     * <p>
     * This is a RenderingHints which is used to tailing images.
     * </p>
     * 
     * @param widthTiling the x dimension tiles.
     * @param heightTiling the y dimension tiles.
     * @return RenderingHints which allow the tiling.
     */
    public static RenderingHints getRenderingHintsTailing( int widthTiling, int heightTiling ) {
        ImageLayout layout = new ImageLayout();
        layout.setTileWidth(widthTiling);
        layout.setTileHeight(heightTiling);
        return new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
    }
    
    /**
     * Create a RenderingHints.
     * <p>
     * This is a RenderingHints which is used to tailing images with 3 bands.
     * </p>
     * 
     * @param widthTiling the x dimension tiles.
     * @param heightTiling the y dimension tiles.
     * @return RenderingHints which allow the tiling.
     */
    
    
    public static RenderingHints getRenderingHintsTailing( int widthTiling, int heightTiling, SampleModel sm ) {
        ImageLayout layout = new ImageLayout();
        layout.setTileWidth(widthTiling);
        layout.setTileHeight(heightTiling);
        layout.setSampleModel(sm);
        return new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
    }
    /**
     * Create a TiledImage from a float matrix.
     * 
     * @param valueMatrix
     * @return a tiledImage which store the valueMatrix.
     */
    public static TiledImage getDataSourceFromFloatMatrix( float[][] valueMatrix ) {
        // create a writable raster with a give matrix.
        WritableRaster testData = (WritableRaster) getNewRaster(valueMatrix);
        // create the tiledImage with this WritableRaster.
        SampleModel sampleModel = testData.getSampleModel();
        int width = testData.getWidth();
        int height = testData.getHeight();
        // create the TiledImage and then allocate the data into the Image.
        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, null);
        tiledImage.setData(testData);
        return tiledImage;

    }

    /**
     * Create a TiledImage from a double matrix.
     * 
     * @param valueMatrix
     * @return a tiledImage which store the valueMatrix.
     */
    public static TiledImage getDataSourceFromDoubleMatrix( double[][] valueMatrix ) {
        // create a writable raster with a give matrix.
        WritableRaster testData = (WritableRaster) getNewRaster(valueMatrix);
        // create the tiledImage with this WritableRaster.
        SampleModel sampleModel = testData.getSampleModel();
        int width = testData.getWidth();
        int height = testData.getHeight();
        // create the TiledImage and then allocate the data into the Image.

        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, null);
        tiledImage.setData(testData);
        return tiledImage;

    }
}
