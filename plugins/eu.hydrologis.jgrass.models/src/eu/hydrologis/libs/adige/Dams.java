package eu.hydrologis.libs.adige;

import java.util.HashMap;

/**
 * Utility class for handling of Dams mappings and data retrival. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class Dams implements DischargeContributor {

    private final HashMap<String, Integer> dams_pfaff2idMap;
    private final HashMap<Integer, Double> dams_id2valuesQMap;

    /**
     * Constructor.
     * 
     * @param dams_pfaff2idMap {@link HashMap map} of pfafstetter numbers versus
     *                      dams points id.
     * @param dams_id2valuesQMap map of dams points id versus discharge value.
     */
    public Dams( HashMap<String, Integer> dams_pfaff2idMap,
            HashMap<Integer, Double> dams_id2valuesQMap ) {
        this.dams_pfaff2idMap = dams_pfaff2idMap;
        this.dams_id2valuesQMap = dams_id2valuesQMap;
    }

    public Double getDischarge( String pNum, double inputDischarge ) {
        Integer damId = dams_pfaff2idMap.get(pNum);
        if (damId != null) {
            Double discharge = dams_id2valuesQMap.get(damId);
            if (discharge != null) {
                return discharge;
            }
        }
        return Double.NaN;
    }

}
