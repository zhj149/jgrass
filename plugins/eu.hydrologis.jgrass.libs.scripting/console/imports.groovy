import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static java.lang.Math.*;

import java.lang.NullPointerException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.IndexOutOfBoundsException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.lang.RuntimeException;
import java.lang.SecurityException;

import java.text.MessageFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.gce.arcgrid.ArcGridFormat;
import org.geotools.gce.arcgrid.ArcGridWriteParams;
import org.geotools.gce.arcgrid.ArcGridWriter;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.coverage.grid.GridCoverage2D;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.spi.GrassBinaryImageWriterSpi;
import eu.hydrologis.jgrass.libs.iodrivers.imageio.GrassBinaryImageWriter;
import java.awt.image.RenderedImage;
import org.geotools.coverage.grid.ViewType;
import org.geotools.gce.arcgrid.ArcGridReader;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.ui.ApplicationGIS;

import eu.hydrologis.jgrass.libs.io.RasterWritingFailureException;
import eu.hydrologis.jgrass.libs.map.JGrassRasterData;
import eu.hydrologis.jgrass.libs.map.JGrassRasterMapReader;
import eu.hydrologis.jgrass.libs.map.JGrassRasterMapWriter;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.map.RasterData;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.FileUtilities;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource
import eu.udig.catalog.jgrass.JGrassPlugin;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities
import eu.hydrologis.openmi.JGrassFeatureValueSet;
import eu.hydrologis.openmi.JGrassRasterValueSet;
import eu.hydrologis.openmi.util.HydrologisDate;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReadParam;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageReader;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.GrassCoverageWriter;
import eu.hydrologis.jgrass.libs.scripting.MapcalcJifflerHeadless;

import groovy.sql.Sql;

import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.Link;
import nl.alterra.openmi.sdk.backbone.LinkableComponent;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import org.openmi.standard.IValueSet;
import org.openmi.standard.IArgument;
import org.openmi.standard.ILink;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.ITime;


