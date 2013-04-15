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
package eu.hydrologis.jgrass.netcdf.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.URLUtils;
import net.refractions.udig.project.internal.render.ViewportModel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.io.CoverageAccess;
import org.geotools.coverage.io.CoverageReadRequest;
import org.geotools.coverage.io.CoverageResponse;
import org.geotools.coverage.io.CoverageSource;
import org.geotools.coverage.io.CoverageAccess.AccessType;
import org.geotools.coverage.io.domain.RasterDatasetDomainManager.HorizontalDomain;
import org.geotools.coverage.io.domain.RasterDatasetDomainManager.TemporalDomain;
import org.geotools.coverage.io.domain.RasterDatasetDomainManager.VerticalDomain;
import org.geotools.coverage.io.impl.DefaultCoverageReadRequest;
import org.geotools.coverage.io.impl.range.DefaultRangeType;
import org.geotools.coverage.io.range.FieldType;
import org.geotools.coverage.io.range.RangeType;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.temporal.object.DefaultInstant;
import org.geotools.temporal.object.DefaultPosition;
import org.geotools.util.NumberRange;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.temporal.TemporalGeometricPrimitive;

import eu.hydrologis.jgrass.netcdf.NetcdfPlugin;

/**
 * The {@link IGeoResource resource} representing the netcdf variable.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class NetcdfMapGeoResource extends IGeoResource {

    public static final String READERID = "eu.hydrologis.jgrass.netcdf.service.NetcdfMapGeoResource.readerid"; //$NON-NLS-1$

    private List<DateTime> availableTimeSteps = null;
    private double[] availableElevationLevels = null;
    
    /** error message field */
    private Throwable msg = null;

    /** metadata info field */
    private NetcdfMapGeoResourceInfo info = null;

    private final String variableName;

    private CoverageSource coverageSource;

    private static DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    private final FieldType fieldType;

    public NetcdfMapGeoResource( IService parentService, FieldType fieldType, CoverageSource coverageSource ) throws IOException {
        service = parentService;
        Name name = fieldType.getName();
        this.variableName = name.getLocalPart();
        this.fieldType = fieldType;
        this.coverageSource = coverageSource;

    }

    public <T> boolean canResolve( Class<T> adaptee ) {
        // garbage in, garbage out
        if (adaptee == null)
            return false;

        boolean isAssignable = adaptee.isAssignableFrom(IService.class) || adaptee.isAssignableFrom(IGeoResource.class)
                || adaptee.isAssignableFrom(GridCoverage.class) || adaptee.isAssignableFrom(NetcdfMapGeoResource.class)
                || adaptee.isAssignableFrom(Style.class)
                || super.canResolve(adaptee);
        return isAssignable;
    }

    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (adaptee == null)
            return null;

        if (adaptee.isAssignableFrom(IService.class)) {
            return adaptee.cast(service);
        }
        if (adaptee.isAssignableFrom(IGeoResourceInfo.class)) {
            return adaptee.cast(getInfo(monitor));
        }

        if (adaptee.isAssignableFrom(GridCoverage2D.class)) {
            CoverageReadRequest readRequest = new DefaultCoverageReadRequest();
            List<DateTime> availableTimeSteps = getAvailableTimeSteps();
            if (availableTimeSteps.size() > 0) {
                SortedSet<TemporalGeometricPrimitive> temporalSubset = new TreeSet<TemporalGeometricPrimitive>();
                temporalSubset.add(new DefaultInstant(new DefaultPosition(availableTimeSteps.get(0).toDate())));
                readRequest.setTemporalSubset(temporalSubset);
            }
            double[] availableElevationLevels = getAvailableElevationLevels();
            if (availableElevationLevels != null && availableElevationLevels.length > 0) {
                Set<NumberRange<Double>> verticalSubset = new TreeSet<NumberRange<Double>>();
                NumberRange<Double> vertical = new NumberRange<Double>(Double.class, availableElevationLevels[0], true,
                        availableElevationLevels[0], true);
                verticalSubset.add(vertical);
                readRequest.setVerticalSubset(verticalSubset);
            }
            GridCoverage2D gridCoverage = getGridCoverage(readRequest);
            return adaptee.cast(gridCoverage);
        }

        if (adaptee.isAssignableFrom(NetcdfMapGeoResource.class)) {
            return adaptee.cast(this);
        }
        if (adaptee.isAssignableFrom(IGeoResource.class)) {
            return adaptee.cast(this);
        }
        
        if(adaptee.isAssignableFrom(Style.class)){
            Style style = style(monitor);
            if( style != null ){
                return adaptee.cast( style(monitor));
            }
        }
        // bad call to resolve
        return super.resolve(adaptee, monitor);
    }
    
    public Style style( IProgressMonitor monitor ) {
        URL url = service.getIdentifier();
        File file = URLUtils.urlToFile(url);
        if (file == null) {
            return null;
        }
        String mapFile = file.getAbsolutePath();

        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(GeoTools.getDefaultHints());

        // strip off the extension and check for sld
        int lastdot = mapFile.lastIndexOf('.');
        String sld = mapFile.substring(0, lastdot) + ".sld"; //$NON-NLS-1$
        File f = new File(sld);
        if (!f.exists()) {
            // try upper case
            sld = mapFile.substring(0, lastdot) + ".SLD"; //$NON-NLS-1$
            f = new File(sld);
        }

        if (f.exists()) {
            // parse it up
            SLDParser parser = new SLDParser(styleFactory);
            try {
                parser.setInput(f);
            } catch (FileNotFoundException e) {
                return null; // well that is unexpected since f.exists()
            }
            Style[] styles = parser.readXML();
            
            for( Style style : styles ) {
                String name = style.getName();
                if (name.trim().equals(info.getTitle())) {
                    return style;
                }
            }

            if (styles.length > 0 && styles[0] != null) {
                return styles[0];
            }
        }
        return null; // well nothing worked out; make your own style
    }

    public URL getIdentifier() {
        try {
            String parenturlString = URLUtils.urlToString(service.getIdentifier(), false);
            return new URL(parenturlString + "#" + variableName);
        } catch (MalformedURLException e) {
            NetcdfPlugin.log("NetcdfPlugin problem: eu.hydrologis.jgrass.netcdf.service#NetcdfMapGeoResource#getIdentifier", e); //$NON-NLS-1$
            e.printStackTrace();
            return null;
        }
    }

    public Throwable getMessage() {
        return msg;
    }

    public Status getStatus() {
        // error occured
        if (msg != null) {
            return Status.BROKEN;
        }

        // if the file hasn't been resolved yet
        // if (mapFile == null) {
        // return Status.NOTCONNECTED;
        // }

        return Status.CONNECTED;
    }

    /**
     * Get some informations about the map resource.
     */
    class NetcdfMapGeoResourceInfo extends IGeoResourceInfo {

        @SuppressWarnings("nls")
        public NetcdfMapGeoResourceInfo( IProgressMonitor monitor ) {

            monitor.beginTask("Gathering Netcdf dataset info...", 4);
            try {
                monitor.worked(1);
                /*
                 * bounds
                 */
                try {
                    final HorizontalDomain horizontalDomain = coverageSource.getDomainManager(null).getHorizontalDomain();
                    if (horizontalDomain == null)
                        bounds = null;
                    else {
                        // print the horizontal domain elements
                        final Set< ? extends BoundingBox> spatialElements = horizontalDomain.getSpatialElements(true, null);
                        for( BoundingBox bbox : spatialElements ) {
                            bounds = new ReferencedEnvelope(bbox);
                            break;
                        }
                    }
                } catch (MismatchedDimensionException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                /*
                 * depth
                 */
                monitor.worked(1);
                if (availableElevationLevels==null) {
	                try {
	                    final VerticalDomain verticalDomain = coverageSource.getDomainManager(null).getVerticalDomain();
	                    if (verticalDomain != null) {
	                        SortedSet< ? extends NumberRange<Double>> verticalElements = verticalDomain.getVerticalElements(true,
	                                null);
	
	                        if (verticalElements.size() > 0) {
	                            availableElevationLevels = new double[verticalElements.size()];
	                            int index = 0;
	                            for( NumberRange<Double> vg : verticalElements ) {
	                                availableElevationLevels[index] = vg.getMinimum();
	                                index++;
	                            }
	                        }
	                    }
	                } catch (IOException e) {
	                	e.printStackTrace();
	                }
                }

                monitor.worked(1);
                /*
                 * time
                 */
                if (availableTimeSteps==null || availableTimeSteps.size()==0) {
	                availableTimeSteps = new ArrayList<DateTime>();
	                try {
	                    final TemporalDomain temporalDomain = coverageSource.getDomainManager(null).getTemporalDomain();
	                    if (temporalDomain != null) {
	                        SortedSet< ? extends TemporalGeometricPrimitive> temporalElements = temporalDomain
	                                .getTemporalElements(null);
	                        for( TemporalGeometricPrimitive tg : temporalElements ) {
	                            DefaultInstant defaultInstant = (DefaultInstant) tg;
	                            Date date = defaultInstant.getPosition().getDate();
	                            availableTimeSteps.add(new DateTime(date));
	                            System.out.println();
	                        }
	                    }
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
                }
                monitor.worked(1);

                boolean hasTime = false;
                boolean hasElevation = false;

                StringBuilder sB = new StringBuilder();
                sB.append(variableName);
                if (availableTimeSteps != null && availableTimeSteps.size() > 0) {
                    hasTime = true;

                    sB.append(" ");
                    sB.append("(time=");
                    if (availableTimeSteps.size() == 1) {
                        sB.append(ISO_DATE_TIME_FORMATTER.print(availableTimeSteps.get(0)));
                    } else {
                        sB.append(ISO_DATE_TIME_FORMATTER.print(availableTimeSteps.get(0)));
                        sB.append(" to ");
                        sB.append(ISO_DATE_TIME_FORMATTER.print(availableTimeSteps.get(availableTimeSteps.size() - 1)));
                    }
                    sB.append(")");
                }
                if (availableElevationLevels != null && availableElevationLevels.length > 0) {
                    hasElevation = true;

                    sB.append(" ");
                    sB.append("(depth=");
                    sB.append(availableElevationLevels[0]);
                    sB.append(" to ");
                    sB.append(availableElevationLevels[availableElevationLevels.length - 1]);
                    sB.append(")");
                }

                this.name = variableName;
                this.title = this.name;
                this.description = sB.toString();

                if (hasElevation && hasTime) {
                    icon = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/grid_time_space.png");
                } else if (hasTime) {
                    icon = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/grid_time.png");
                } else if (hasElevation) {
                    icon = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/grid_space.png");
                } else {
                    icon = AbstractUIPlugin.imageDescriptorFromPlugin(NetcdfPlugin.PLUGIN_ID, "icons/grid_obj.gif");
                }
            } finally {
                monitor.done();
            }

        }

        public ReferencedEnvelope getBounds() {
            return bounds;
        }

        public List<DateTime> getAvailableTimeSteps() {
            return availableTimeSteps;
        }

        public double[] getAvailableElevationLevels() {
            return availableElevationLevels;
        }

    }

    public List<DateTime> getAvailableTimeSteps() {
    	if (availableTimeSteps!=null && availableTimeSteps.size()!=0) {
			return availableTimeSteps;
		}
        try {
            createInfo(new NullProgressMonitor());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info.getAvailableTimeSteps();
    }

    public double[] getAvailableElevationLevels() {
    	if (availableElevationLevels!=null) {
			return availableElevationLevels;
		}
        try {
            createInfo(new NullProgressMonitor());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info.getAvailableElevationLevels();
    }

    protected IGeoResourceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        if (info == null) {
            // support concurrent access
            synchronized (this) {
                if (info == null) {
                    if (monitor == null)
                        monitor = new NullProgressMonitor();
                    info = new NetcdfMapGeoResourceInfo(monitor);
                }
            }
        }

        return info;
    }

    public String getVariableName() {
        return variableName;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public CoverageSource getCoverageSource() {
        return coverageSource;
    }

    public GridCoverage2D getGridCoverage( CoverageReadRequest readRequest ) throws IOException {

        Set<FieldType> fieldTypeSet = new TreeSet<FieldType>();
        fieldTypeSet.add(fieldType);
        RangeType rangeType = new DefaultRangeType(fieldType.getName(), fieldType.getDescription(), fieldTypeSet);
        readRequest.setRangeSubset(rangeType);

        CoverageResponse response = coverageSource.read(readRequest, null);
        if (response == null || response.getStatus() != org.geotools.coverage.io.CoverageResponse.Status.SUCCESS
                || !response.getExceptions().isEmpty())
            throw new IOException("Unable to read");

        final Collection< ? extends Coverage> results = response.getResults(null);
        for( Coverage c : results ) {
            GridCoverage2D coverage = (GridCoverage2D) c;
            return coverage;
        }
        return null;
    }
}
