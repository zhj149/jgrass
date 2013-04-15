package eu.hydrologis.jgrass.models.r.mapcalc;

import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleCompilationException;
import jaitools.jiffle.runtime.JiffleCompletionEvent;
import jaitools.jiffle.runtime.JiffleEventListener;
import jaitools.jiffle.runtime.JiffleFailureEvent;
import jaitools.jiffle.runtime.JiffleInterpreter;
import jaitools.jiffle.runtime.JiffleInterpreterException;
import jaitools.jiffle.runtime.JiffleProgressEvent;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;

import javax.media.jai.TiledImage;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.geotools.coverage.grid.GridCoverage2D;

import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReadParam;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReader;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.GrassBinaryImageWriter;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.spi.GrassBinaryImageWriterSpi;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.DummyProgressMonitor;
import eu.hydrologis.jgrass.libs.utils.monitor.IProgressMonitorJGrass;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

@SuppressWarnings("nls")
public class MapcalcJiffler {

    public static final String INMAPWRAPPER = "$";
    public static final String OUTMAPWRAPPER = "@";

    private JiffleInterpreter interp;
    private final String result;
    private String function;
    private HashMap<String, RenderedImage> imgParams = new HashMap<String, RenderedImage>();
    private final String[] mapsArray;
    private final JGrassRegion jgRegion;

    private int previousProgress = 0;

    private File cellFolderFile;
    private final PrintStream out;

    private String errorMessage = null;

    /**
     * Constructor. Creates an instance of {@link jaitools.jiffle.runtime.JiffleInterpeter}
     * and sets up interpreter event handlers.
     * @param pm 
     *
     */
    public MapcalcJiffler( String function, String result, String[] mapsArray,
            JGrassRegion jgRegion, String cellFolderPath, PrintStream out ) throws IOException {
        this.function = function;
        this.result = result;
        this.mapsArray = mapsArray;
        this.jgRegion = jgRegion;
        this.out = out;

        cellFolderFile = new File(cellFolderPath);

    }

    public String exec() {

        final IProgressMonitorJGrass pm = new PrintStreamProgressMonitor(out);

        try {
            for( String mapName : mapsArray ) {
                String wrappedMapName = INMAPWRAPPER + mapName + INMAPWRAPPER;
                if (function.indexOf(wrappedMapName) != -1) {
                    // this is a map that needs to be read
                    GrassCoverageReader tmp = new GrassCoverageReader(null, null, true, false, pm);
                    File mapFile = new File(cellFolderFile, mapName);
                    tmp.setInput(mapFile);
                    GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(jgRegion);
                    GridCoverage2D gridCoverage2D = tmp.read(gcReadParam);
                    RenderedImage renderedImage = gridCoverage2D.getRenderedImage();
                    // GrassCoverageReader.printImage(renderedImage, System.out);
                    imgParams.put(mapName, renderedImage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage = "An error occurred while reading the input maps.";
            return errorMessage;
        }

        pm.beginTask("Allocating output image...", 2);
        pm.worked(1);

        TiledImage returnImage = ImageUtils.createConstantImage(jgRegion.getCols(), jgRegion
                .getRows(), 0.0);

        pm.worked(1);
        pm.done();

        /*
         * prepare the function to be used by jiffle
         */
        function = function.replaceAll("\\$", "");
        String script = null;
        if (result != null) {
            String regex = result + "[\\s+]=";
            String[] split = function.split(regex);
            if (split.length > 1) {
                /*
                 * if there is the result inside the function,
                 * then 
                 */
                script = function + "\n";
            } else {
                /*
                 * if there is no result inside, then we
                 * assume a form of:
                 * result = function
                 */
                script = result + "=" + function + "\n";
            }
            imgParams.put(result, returnImage);
        }else{
            String[] split = function.split(OUTMAPWRAPPER);
            try{
                String theresult = split[1];
                imgParams.put(theresult, returnImage);
                script = function.replaceAll("@", "");
            }catch (Exception e) {
                errorMessage = "An output map has to be supplied and it has to be wrapped between @ (example: @outmap@)";
                return errorMessage;
            }
        }
        
        

        JiffleEventListener listener = new JiffleEventListener(){
            public void onCompletionEvent( JiffleCompletionEvent ev ) {
                RenderedImage resultImage = ev.getJiffle().getImage(result);
                // GrassCoverageReader.printImage(resultImage, System.out);
                try {
                    GrassBinaryImageWriterSpi writerSpi = new GrassBinaryImageWriterSpi();
                    GrassBinaryImageWriter writer = new GrassBinaryImageWriter(writerSpi,
                            new DummyProgressMonitor());
                    // RenderedImage renderedImage =
                    // gridCoverage2D.view(ViewType.GEOPHYSICS).getRenderedImage();
                    File file = new File(cellFolderFile, result);
                    writer.setOutput(file);
                    writer.write(resultImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (JGrassPlugin.getDefault() != null) {
                    File mapsetFile = cellFolderFile.getParentFile();
                    JGrassMapGeoResource addedMap = JGrassCatalogUtilities.addMapToCatalog(
                            mapsetFile.getParent(), mapsetFile.getName(), result,
                            JGrassConstants.GRASSBINARYRASTERMAP);
                    if (addedMap != null) {
                        IMap activeMap = ApplicationGIS.getActiveMap();
                        ApplicationGIS.addLayersToMap(activeMap, Collections
                                .singletonList((IGeoResource) addedMap), activeMap.getMapLayers()
                                .size());
                    }
                }
                pm.done();
            }

            public void onFailureEvent( JiffleFailureEvent arg0 ) {
                errorMessage = "An error occurred during the map calculation: " + arg0.toString();
            }

            public void onProgressEvent( JiffleProgressEvent arg0 ) {
                float progress = arg0.getProgress();
                int current = (int) (progress * 100f);
                int delta = current - previousProgress;

                pm.worked(delta);
                previousProgress = current;
            }
        };

        try {
            Jiffle j = new Jiffle(script, imgParams);
            interp = new JiffleInterpreter();
            interp.addEventListener(listener);
            if (j.isCompiled()) {
                interp.submit(j);
            }
        } catch (JiffleCompilationException e) {
            e.printStackTrace();

            errorMessage = "An error occurred during the compilation of the function. Please check your function.";
            return errorMessage;
        } catch (JiffleInterpreterException e) {
            e.printStackTrace();
            errorMessage = "An error occurred during the interpretation of the function. Please check your function.";
        }
        return errorMessage;

    }

}
