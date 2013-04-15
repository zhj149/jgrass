package eu.hydrologis.libs.adige;

import java.util.HashMap;

/**
 * Utility class for handling of Hydrometers mappings and data retrival.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class Hydrometers implements DischargeContributor {

    private final HashMap<String, Integer> hydrometer_pfaff2idMap;
    private final HashMap<Integer, Double> hydrometer_id2valuesMap;

    /**
     * Constructor.
     * 
     * @param hydrometer_pfaff2idMap {@link HashMap map} of pfafstetter numbers versus
     *                      hydrometers points id.
     * @param hydrometer_id2valuesMap map of hydrometer points id versus discharge value.
     */
    public Hydrometers( HashMap<String, Integer> hydrometer_pfaff2idMap,
            HashMap<Integer, Double> hydrometer_id2valuesMap ) {
        this.hydrometer_pfaff2idMap = hydrometer_pfaff2idMap;
        this.hydrometer_id2valuesMap = hydrometer_id2valuesMap;
    }

    public Double getDischarge( String pfafstetterNumber, double inputDischarge ) {
        Integer hydroId = hydrometer_pfaff2idMap.get(pfafstetterNumber);
        if (hydroId != null) {
            Double value = hydrometer_id2valuesMap.get(hydroId);
            if (value != null) {
                return value;
            }
        }
        return Double.NaN;
    }

}
