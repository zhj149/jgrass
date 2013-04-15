package eu.hydrologis.libs.adige;

import java.io.PrintStream;
import java.util.HashMap;

/**
 * Utility class for handling of Offtakes mappings and data retrival. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class Offtakes implements DischargeContributor {

    private final HashMap<String, Integer> offtakes_pfaff2idMap;
    private final HashMap<Integer, Double> offtakes_id2valuesQMap;
    private final PrintStream out;

    /**
     * Constructor.
     * 
     * @param offtakes_pfaff2idMap {@link HashMap map} of pfafstetter numbers versus
     *                      offtakes points id.
     * @param offtakes_id2valuesQMap map of offtakes points id versus discharge value.
     * @param out {@link PrintStream} for warning handling.
     */
    public Offtakes( HashMap<String, Integer> offtakes_pfaff2idMap,
            HashMap<Integer, Double> offtakes_id2valuesQMap, PrintStream out ) {
        this.offtakes_pfaff2idMap = offtakes_pfaff2idMap;
        this.offtakes_id2valuesQMap = offtakes_id2valuesQMap;
        this.out = out;
    }

    public Double getDischarge( String pNum, double inputDischarge ) {
        Integer damId = offtakes_pfaff2idMap.get(pNum);
        if (damId != null) {
            Double discharge = offtakes_id2valuesQMap.get(damId);
            if (discharge != null) {
                if (inputDischarge >= discharge) {
                    return inputDischarge - discharge;
                } else {
                    out
                            .println("WARNING: offtake discharge at "
                                    + pNum
                                    + " is greater than the river discharge. Offtake discharge set to 0 to continue.");
                    return inputDischarge;
                }
            }
        }
        return Double.NaN;
    }

}
