/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) C.U.D.A.M. Universita' di Trento
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
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.ui.CatalogUIPlugin;
import net.refractions.udig.catalog.ui.ISharedImages;
import net.refractions.udig.ui.graphics.AWTSWTImageUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.geotools.coverage.io.CoverageAccess;
import org.geotools.coverage.io.CoverageSource;
import org.geotools.coverage.io.CoverageAccess.AccessType;
import org.geotools.coverage.io.driver.Driver.DriverOperation;
import org.geotools.coverage.io.netcdf.NetCDFDriver;
import org.geotools.coverage.io.range.FieldType;
import org.geotools.coverage.io.range.RangeType;
import org.geotools.data.ServiceInfo;
import org.opengis.feature.type.Name;

/**
 * The service handle for netcdf files.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class NetcdfService extends IService {

    /** netcdf url field */
    private URL url = null;

    /** connection params field */
    private Map<String, Serializable> params = null;

    /** metadata info field */
    private NetcdfServiceInfo info = null;

    /** the resources members field */
    private volatile List<IResolve> mapMembers = null;

    /** error message field */
    private Throwable msg = null;

    private CoverageAccess coverageAccess;

    public NetcdfService( Map<String, Serializable> params ) throws Exception {
        this.params = params;
        // get the file url from the connection parameters
        url = (URL) this.params.get(NetcdfServiceExtension.KEY);
        NetCDFDriver netCDFDriver = new NetCDFDriver();
        if (netCDFDriver.canProcess(DriverOperation.CONNECT, url, null)) {
            coverageAccess = netCDFDriver.process(DriverOperation.CONNECT, url, null, null, null);
        }
    }

    @Override
    public Map<String, Serializable> getConnectionParams() {
        return params;
    }

    /**
     * check if the passed adaptee can resolve the file. Checks on location
     * consistency were already done in the service extention.
     */
    public <T> boolean canResolve( Class<T> adaptee ) {
        // garbage in, garbage out
        if (adaptee == null)
            return false;

        if (adaptee.isAssignableFrom(IServiceInfo.class)) {
            return true;
        }
        if (adaptee.isAssignableFrom(URL.class)) {
            return true;
        }
        if (super.canResolve(adaptee)) {
            return true;
        }
        return false;
    }

    /**
     * resolve the adaptee to the location folder file
     */
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (adaptee == null)
            return null;

        System.out.println("RESOLVE: " + adaptee.toString());
        if (adaptee.isAssignableFrom(IServiceInfo.class)) {
            // return the metadata object
            return adaptee.cast(getInfo(monitor));
        }
        if (adaptee.isAssignableFrom(List.class)) {
            // return the list of resources
            return adaptee.cast(members(monitor));
        }

        if (adaptee.isAssignableFrom(URL.class)) {
            return adaptee.cast(url);
        }
        // bad call to resolve
        return super.resolve(adaptee, monitor);
    }

    public List< ? extends IGeoResource> resources( IProgressMonitor monitor ) throws IOException {
        // seed the potentially null field
        members(monitor);
        List<NetcdfMapGeoResource> children = new ArrayList<NetcdfMapGeoResource>();
        collectChildren(this, children);

        return children;
    }

    private void collectChildren( IResolve resolve, List<NetcdfMapGeoResource> children ) throws IOException {
        List<IResolve> resolves = resolve.members(new NullProgressMonitor());

        if (resolve instanceof NetcdfMapGeoResource && resolves.isEmpty()) {
            children.add((NetcdfMapGeoResource) resolve);
        } else {
            for( IResolve resolve2 : resolves ) {
                collectChildren(resolve2, children);
            }
        }
    }

    public List<IResolve> members( IProgressMonitor monitor ) throws IOException {
        // lazily load
        if (mapMembers == null) {
            // concurrent access
            synchronized (this) {
                if (mapMembers == null) {
                    List<Name> coverageNames = coverageAccess.getNames(null);

                    mapMembers = new ArrayList<IResolve>();
                    for( Name coverageName : coverageNames ) {
                        CoverageSource coverageSource = coverageAccess.access(coverageName, null, AccessType.READ_ONLY, null,
                                null);
                        RangeType rangeType = coverageSource.getRangeType(null);
                        Set<FieldType> fieldTypes = rangeType.getFieldTypes();
                        for( FieldType fieldType : fieldTypes ) {
                            NetcdfMapGeoResource jgrassMapsetGeoResource = new NetcdfMapGeoResource(this, fieldType,
                                    coverageSource);
                            mapMembers.add(jgrassMapsetGeoResource);
                        }
                    }
                    return mapMembers;
                }
            }
        }
        return mapMembers;
    }

    public URL getIdentifier() {
        return url;
    }

    public static URL createId( String netcdfString ) {
        try {
            URL netcdfStringUrl = new File(netcdfString).toURI().toURL();
            return netcdfStringUrl;
        } catch (MalformedURLException e) {
            throw (Error) new AssertionError("Url should always work this is a bug").initCause(e);
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

        // if the folder hasn't been scanned yet
        if (mapMembers == null) {
            return Status.NOTCONNECTED;
        }

        return Status.CONNECTED;
    }

    class NetcdfServiceInfo extends IServiceInfo {
        public NetcdfServiceInfo() {
            if (coverageAccess == null) {
                this.title = "unable to load";
                this.description = title;
                return;
            }
            
            ServiceInfo sInfo = coverageAccess.getInfo(null);
            String tmp = sInfo.getTitle();
            int lastUnderscore = tmp.lastIndexOf('_');
            tmp = tmp.substring(0, lastUnderscore);

            if (tmp.equals("entry")) {
                /*
                 * it might be that it is from opendap and the name is not 
                 * properly resolved. Try to get it right.
                 */
                // http://localhost:8080/repository/entry/show/output:data.opendap/
                // entryid:7d91dde1-d184-4b8a-8937-4cbdd7f037b5/morphology_duron_new.nc/dodsC/entry.das
                String sourceString = sInfo.getSource().toString();
                sourceString = sourceString.replaceFirst("/dodsC/entry.das", "");
                int lastSlash = sourceString.lastIndexOf('/');

                String tit = sourceString.substring(lastSlash + 1, sourceString.length());
                if (tit != null && tit.length() > 0) {
                    tmp = tit;
                }
            }

            this.title = tmp;
            this.description = sInfo.getDescription();

        }

        public Icon getIcon() {
            // ImageDescriptor imgD = AbstractUIPlugin.imageDescriptorFromPlugin(
            //                    JGrassPlugin.PLUGIN_ID, "icons/obj16/jgrassloc_obj.gif"); //$NON-NLS-1$
            ImageDescriptor imgD = CatalogUIPlugin.getDefault().getImages().getImageDescriptor(ISharedImages.DATASTORE_OBJ);
            return AWTSWTImageUtils.imageDescriptor2awtIcon(imgD);

        }

        public String getTitle() {
            return title;
        }
    }

    protected IServiceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        // lazy creation
        if (info == null) {
            // support concurrent access
            synchronized (this) {
                if (info == null) {
                    info = new NetcdfServiceInfo();
                }
            }
        }

        return info;
    }

}
