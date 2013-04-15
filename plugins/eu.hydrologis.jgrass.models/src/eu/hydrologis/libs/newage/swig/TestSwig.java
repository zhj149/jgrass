package eu.hydrologis.libs.newage.swig;

public class TestSwig {

    static {
        try {
            System.loadLibrary("geotransf");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Native code library failed to load.\n" + e);
            System.exit(1);
        }
    }

    public static void main( String[] args ) {

        double[] t = new double[]{-9999.0, -9999.0, 11.70, 13.40, 11.40, 12.00, 13.80, 10.90,
                -9999.0, 15.30};
        double[] z = new double[]{1541.69, 1371.28, 2252.34, 1978.57, 2420.47};
        double[] quotasta = new double[]{254, 560., 1250., 333., 718., 821., 863., 213., 1510.,
                943.};

        doubleArray zarray = new doubleArray(z.length);
        doubleArray tempArray = new doubleArray(t.length);
        doubleArray quotastaArray = new doubleArray(t.length);

        // output array
        doubleArray tbaciniArray = new doubleArray(z.length);

        doubleArray params = new doubleArray(2);
        params.setitem(0, 0.6);
        params.setitem(1, 0.0);
        intArray numsArray = new intArray(2);
        numsArray.setitem(0, quotasta.length);
        numsArray.setitem(1, z.length);

        for( int i = 0; i < z.length; i++ ) {
            zarray.setitem(i, z[i]);
        }
        for( int i = 0; i < t.length; i++ ) {
            tempArray.setitem(i, t[i]);
            quotastaArray.setitem(i, quotasta[i]);
        }

        temp.cstige_temperature(params.cast(), numsArray.cast(), zarray.cast(), quotastaArray
                .cast(), tempArray.cast(), tbaciniArray.cast());

        for( int i = 0; i < z.length; i++ ) {
            System.out.println(tbaciniArray.getitem(i));
        }

    }
}
