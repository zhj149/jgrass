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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;
import net.refractions.udig.catalog.URLUtils;

/**
 * <p>
 * Creates a service extention for the JGrass database service.
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 * @since 1.1.0
 */
public class NetcdfServiceExtension implements ServiceExtension {

    /**
     * the netcdf service key, it is used to store the url to the netcdf dataset.
     */
    public static final String KEY = "eu.hydrologis.udig.catalog.netcdf.urlKey"; //$NON-NLS-1$

    public NetcdfServiceExtension() {
        super();
    }

    /**
     * Checks the url to determine if it points to a valid <b>netcdf dataset</b>.
     * 
     * <p>
     * It must return <b>null</b> if the url is not intended for this service.
     * </p>
     * 
     * @param url the url points to the actual service itself. 
     * @return a parameter map containing the necessary info or null if the url is not for this
     *         service
     */
    public Map<String, Serializable> createParams( URL url ) {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        /*
         * does the url represent a netcdf file or remote opendap server?
         */
        if (isNetcdfUrl(url)) {
            params.put(KEY, url);
            return params;
        }
        return null;
    }

    public static boolean isNetcdfUrl( URL url ) {
        if (url == null)
            return false;
        String urlString = URLUtils.urlToString(url, false);
        return isNetcdfUrl(urlString);
    }

    public static boolean isNetcdfUrl( String urlString ) {
        if (urlString == null) {
            return false;
        } else if (urlString.startsWith("http")) {
            // check that it is not WMS, WFS or WCS
            if (urlString.matches(".*[Ss][Ee][Rr][Vv][Ii][Cc][Ee]=[Ww][Mm][Ss].*")) {
                return false;
            } else if (urlString.matches(".*[Ss][Ee][Rr][Vv][Ii][Cc][Ee]=[Ww][Ff][Ss].*")) {
                return false;
            } else if (urlString.matches(".*[Ss][Ee][Rr][Vv][Ii][Cc][Ee]=[Ww][Cc][Ss].*")) {
                return false;
            } else if (urlString.matches(".*[Oo][Ww][Ss]\\?[Ss][Ee][Rr][Vv][Ii][Cc][Ee].*")) {
                return false;
            }
            return true;
        } else if (urlString.startsWith("dods")) {
            return true;
        } else {
            // last check if it is a file of nc extention
            if (urlString.startsWith("file")) {
                urlString = urlString.replaceFirst("file:", "");
            }

            File localFile = new File(urlString);
            String path = localFile.getAbsolutePath();
            if (localFile.exists() && path.endsWith(".nc")) {
                return true;
            }
            return false;
        }
    }

    public static URL getAsNetcdfUrl( String urlString ) {
        URL url = null;
        if (urlString == null) {
            return null;
        } else if (urlString.startsWith("http") || urlString.startsWith("dods")) {
            // check that it is not WMS, WFS or WCS
            if (urlString.matches(".*[Ss][Ee][Rr][Vv][Ii][Cc][Ee]=[Ww][Mm][Ss].*")) {
                return null;
            } else if (urlString.matches(".*[Ss][Ee][Rr][Vv][Ii][Cc][Ee]=[Ww][Ff][Ss].*")) {
                return null;
            } else if (urlString.matches(".*[Ss][Ee][Rr][Vv][Ii][Cc][Ee]=[Ww][Cc][Ss].*")) {
                return null;
            }
            try {
                url = new URL(urlString);
                return url;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            // last check if it is a file of nc extention
            if (urlString.startsWith("file")) {
                urlString = urlString.replaceFirst("file:", "");
            }

            File localFile = new File(urlString);
            String path = localFile.getAbsolutePath();
            if (localFile.exists() && path.endsWith(".nc")) {
                try {
                    url = localFile.toURI().toURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }
                return url;
            }
            return null;
        }
    }

    /**
     * create the Netcdf service
     */
    public IService createService( URL id, Map<String, Serializable> params ) {
        // good defensive programming
        if (params == null)
            return null;

        // check for the properties service key
        if (params.containsKey(KEY)) {
            // found it, create the service handle
            try {
                return new NetcdfService(params);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // key not found
        return null;
    }

}
