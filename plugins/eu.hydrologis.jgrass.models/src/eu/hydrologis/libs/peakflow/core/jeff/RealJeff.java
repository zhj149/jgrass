package eu.hydrologis.libs.peakflow.core.jeff;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class RealJeff {

    private double rain_timestep = 0f;
    private final Map<DateTime, Double> rainData;
    private DateTime firstDate;

    public RealJeff( Map<DateTime, Double> rainData ) {
        this.rainData = rainData;

        Set<DateTime> keySet = rainData.keySet();
        DateTime second = null;
        for( DateTime dateTime : keySet ) {
            if (firstDate == null) {
                firstDate = dateTime;
                continue;
            }
            if (second == null) {
                second = dateTime;
                break;
            }
        }
        Interval interval = new Interval(firstDate, second);
        rain_timestep = interval.toDuration().getStandardSeconds();

    }

    public Map<DateTime, Double> calculateJeff() {
        Map<DateTime, Double> jeffData = new LinkedHashMap<DateTime, Double>();
        /*
         * Jeff is returned in m/s instead of mm/h (which is the dimension
         * of rain height over timestep. Therefore let's do some conversion.
         */
        double converter = 1.0 / (1000.0 * 3600.0);

        Set<DateTime> dates = rainData.keySet();
        for( DateTime dateTime : dates ) {
            // rainvalue is in mm/h
            Double rainValue = rainData.get(dateTime);
            // need it in m/s 
            double jeff = converter * rainValue;
            jeffData.put(dateTime, jeff);
        }

        return jeffData;
    }

    public double getRain_timestep() {
        return rain_timestep;
    }

    public DateTime getFirstDate() {
        return firstDate;
    }

}
