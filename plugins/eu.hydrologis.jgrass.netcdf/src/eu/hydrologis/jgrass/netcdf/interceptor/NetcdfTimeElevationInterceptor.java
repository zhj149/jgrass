package eu.hydrologis.jgrass.netcdf.interceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.interceptor.LayerInterceptor;
import net.refractions.udig.project.internal.Layer;
import net.refractions.udig.project.internal.Map;
import net.refractions.udig.project.internal.render.ViewportModel;

import org.joda.time.DateTime;

import eu.hydrologis.jgrass.netcdf.service.NetcdfMapGeoResource;

public class NetcdfTimeElevationInterceptor implements LayerInterceptor {

    public void run( Layer layer ) {
        if (layer.getMap() == null) {
            // this check is here because we could be doing a copy
            return;
        }
        Map map = layer.getMapInternal();

        ViewportModel viewportModel = map.getViewportModelInternal();
        IGeoResource geoResource = layer.getGeoResource();
        if (geoResource.canResolve(NetcdfMapGeoResource.class)) {
            try {
                viewportModel.eSetDeliver(false);
                NetcdfMapGeoResource netcdfMapGeoResource = geoResource.resolve(
                        NetcdfMapGeoResource.class, null);
                try {
                    List<DateTime> georesourceTimeValues = netcdfMapGeoResource
                            .getAvailableTimeSteps();
                    List<DateTime> copyValue = new ArrayList<DateTime>();
                    for (DateTime dateTime : georesourceTimeValues) {
						copyValue.add(new DateTime(dateTime));
					}
                    
                    if (copyValue.size() > 0) {
                        List<DateTime> availableTimesteps = viewportModel.getAvailableTimesteps();
                        for (DateTime dateTime : georesourceTimeValues) {
							if (!availableTimesteps.contains(dateTime)) {
								availableTimesteps.add(dateTime);
							}
						}

                        Collections.sort(availableTimesteps);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    double[] georesourceElevationArray = netcdfMapGeoResource
                            .getAvailableElevationLevels();
                    if (georesourceElevationArray != null) {
                        List<Double> availableElevations = viewportModel.getAvailableElevation();
                        
                        for( double d : georesourceElevationArray ) {
                        	Double dObj = d;
                        	if (!availableElevations.contains(dObj)) {
                        		availableElevations.add(d);
							}
                        }

                        Collections.sort(availableElevations);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                viewportModel.eSetDeliver(true);
            }
        }
    }

}
