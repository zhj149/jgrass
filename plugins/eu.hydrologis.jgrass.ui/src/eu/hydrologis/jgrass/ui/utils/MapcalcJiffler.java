package eu.hydrologis.jgrass.ui.utils;

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
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;

import javax.media.jai.TiledImage;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.geotools.coverage.grid.GridCoverage2D;

import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReadParam;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReader;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.GrassBinaryImageWriter;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.spi.GrassBinaryImageWriterSpi;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.DummyProgressMonitor;
import eu.hydrologis.jgrass.libs.utils.monitor.EclipseProgressMonitorAdapter;
import eu.hydrologis.jgrass.ui.UiPlugin;
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

@SuppressWarnings("nls")
public class MapcalcJiffler {
    private JiffleInterpreter interp;
    private final String result;
    private String function;
    private HashMap<String, RenderedImage> imgParams = new HashMap<String, RenderedImage>();
    private String MAPWRAPPER = MapcalcGui.MAPWRAPPER;
    private final String[] mapsArray;
    private final JGrassRegion jgRegion;

    private int previousProgress = 0;

    private boolean isOk = false;
    private boolean hasFinished = false;
    private File cellFolderFile;

    /**
     * Constructor. Creates an instance of {@link jaitools.jiffle.runtime.JiffleInterpeter}
     * and sets up interpreter event handlers.
     * @param pm 
     *
     */
    public MapcalcJiffler( String function, String result, String[] mapsArray,
            JGrassRegion jgRegion, String cellFolderPath ) throws IOException {
        this.function = function;
        this.result = result;
        this.mapsArray = mapsArray;
        this.jgRegion = jgRegion;

        cellFolderFile = new File(cellFolderPath);

    }

    public void exec() {

        IRunnableWithProgress readMapsOperations = new IRunnableWithProgress(){

            public void run( final IProgressMonitor monitor ) throws InvocationTargetException,
                    InterruptedException {
                EclipseProgressMonitorAdapter pm = new EclipseProgressMonitorAdapter(monitor);

                try {
                    for( String mapName : mapsArray ) {
                        String wrappedMapName = MAPWRAPPER + mapName + MAPWRAPPER;
                        if (function.indexOf(wrappedMapName) != -1) {
                            // this is a map that needs to be read
                            GrassCoverageReader tmp = new GrassCoverageReader(null, null, true,
                                    false, pm);
                            File mapFile = new File(cellFolderFile, mapName);
                            tmp.setInput(mapFile);
                            GrassCoverageReadParam gcReadParam = new GrassCoverageReadParam(
                                    jgRegion);
                            GridCoverage2D gridCoverage2D = tmp.read(gcReadParam);
                            RenderedImage renderedImage = gridCoverage2D.getRenderedImage();
                            // GrassCoverageReader.printImage(renderedImage, System.out);
                            imgParams.put(mapName, renderedImage);
                        }
                    }
                    isOk = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    String message = "An error occurred while reading the input maps.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            UiPlugin.PLUGIN_ID, e);
                }
            }
        };
        PlatformGIS.runInProgressDialog("Reading needed maps...", true, readMapsOperations, false);

        if (!isOk) {
            return;
        }

        IRunnableWithProgress mapcalcOperation = new IRunnableWithProgress(){

            public void run( final IProgressMonitor monitor ) throws InvocationTargetException,
                    InterruptedException {

                monitor.beginTask("Processing maps...", 100);

                TiledImage returnImage = ImageUtils.createConstantImage(jgRegion.getCols(),
                        jgRegion.getRows(), 0.0);
                imgParams.put(result, returnImage);
              

                /*
                 * prepare the function to be used by jiffle
                 */
                function = function.replaceAll(MAPWRAPPER, "");
                String script = null;
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
                previousProgress = 0;

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
                            ExceptionDetailsDialog.openError(null,
                                    "An error occurred while writing the map.", IStatus.ERROR,
                                    UiPlugin.PLUGIN_ID, e);
                        } finally {
                            monitor.done();
                            hasFinished = true;
                        }
                    }

                    public void onFailureEvent( JiffleFailureEvent arg0 ) {
                        String msg = arg0.toString();
                        ExceptionDetailsDialog.openError(null,
                                "An error occurred during the map calculation.", IStatus.ERROR,
                                UiPlugin.PLUGIN_ID, new RuntimeException(msg));
                        hasFinished = true;
                    }

                    public void onProgressEvent( JiffleProgressEvent arg0 ) {
                        float progress = arg0.getProgress();
                        int current = (int) (progress * 100f);
                        int delta = current - previousProgress;

                        monitor.worked(delta);

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

                    while( !hasFinished ) {
                        Thread.sleep(300);
                    }

                    if (JGrassPlugin.getDefault() != null) {
                        File mapsetFile = cellFolderFile.getParentFile();
                        JGrassMapGeoResource addedMap = JGrassCatalogUtilities.addMapToCatalog(
                                mapsetFile.getParent(), mapsetFile.getName(), result,
                                JGrassConstants.GRASSBINARYRASTERMAP);
                        IMap activeMap = ApplicationGIS.getActiveMap();
                        if (addedMap != null) {
                            ApplicationGIS.addLayersToMap(activeMap, Collections
                                    .singletonList((IGeoResource) addedMap), activeMap
                                    .getMapLayers().size());
                        }
                        activeMap.getRenderManager().refresh(null);
                    }

                } catch (JiffleCompilationException e) {
                    e.printStackTrace();

                    String message = "An error occurred during the compilation of the function. Please check your function.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            UiPlugin.PLUGIN_ID, e);

                } catch (JiffleInterpreterException e) {
                    e.printStackTrace();
                    String message = "An error occurred during the interpretation of the function. Please check your function.";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR,
                            UiPlugin.PLUGIN_ID, e);
                }

            }
        };
        PlatformGIS.runInProgressDialog("Map calculation...", true, mapcalcOperation, false);
    }

}
