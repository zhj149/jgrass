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
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.ui.UDIGConnectionFactory;

/**
 * Connection factory to the netcdf service.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class NetcdfConnectionFactory extends UDIGConnectionFactory {

    public boolean canProcess( Object context ) {
        if (context instanceof IResolve) {
            IResolve resolve = (IResolve) context;
            return (resolve.canResolve(File.class));
        } else if (context instanceof URL) {
            boolean canProcess = NetcdfServiceExtension.isNetcdfUrl((URL) context);
            return canProcess;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Serializable> createConnectionParameters( Object context ) {
        if (context instanceof IResolve) {
            Map<String, Serializable> params = createParams((IResolve) context);
            if (!params.isEmpty())
                return params;
        } else if (context instanceof URL) {
            URL url = (URL) context;
            if(NetcdfServiceExtension.isNetcdfUrl(url)){
                HashMap<String, Serializable> params = new HashMap<String, Serializable>();
                params.put(NetcdfServiceExtension.KEY, url);
                return params;
            }
        }
        
        return Collections.EMPTY_MAP;
    }

    @SuppressWarnings("unchecked")
    static public Map<String, Serializable> createParams( IResolve handle ) {
        if (handle instanceof NetcdfService) {
            // got a hit!
            NetcdfService jgrassService = (NetcdfService) handle;
            return jgrassService.getConnectionParams();
        }
        return Collections.EMPTY_MAP;
    }

    /** 'Create' params given the provided url, no magic occurs */
    static public Map<String, Serializable> createParams( URL url ) {
        NetcdfServiceExtension factory = new NetcdfServiceExtension();
        Map<String, Serializable> params = factory.createParams(url);
        if (params != null)
            return params;

        params = new HashMap<String, Serializable>();
        params.put(NetcdfServiceExtension.KEY, url);
        return params;
    }

    public URL createConnectionURL( Object context ) {
        if (context instanceof URL) {
            URL url = (URL) context;
            return url;
        }
        return null;
    }

}
