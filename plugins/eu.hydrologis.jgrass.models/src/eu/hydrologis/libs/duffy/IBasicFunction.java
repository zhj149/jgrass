package eu.hydrologis.libs.duffy;

public interface IBasicFunction {
    public double[] eval( double currentTimeInMinutes, double[] input, double[] precipitation,
            double[] radiationArray, double[] netshortArray, double[] temperatureArray,
            double[] humidityArray, double[] windspeedArray, double[] pressureArray, double[] snowWaterEquivalentArray, boolean isMainTimeStep );
}
