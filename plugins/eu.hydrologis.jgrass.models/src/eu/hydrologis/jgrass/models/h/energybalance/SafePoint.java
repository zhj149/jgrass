package eu.hydrologis.jgrass.models.h.energybalance;

import java.io.Serializable;

public class SafePoint implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public double[][][] SWE = null;
    public double[][][] U = null;
    public double[][][] Ts = null;
    public double[][][] SnAge = null;

}
