package eu.hydrologis.jgrass.netcdf;

import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;

public class NetcdfUtils {

    public static Date getTimeValue( String unitString, Variable timeVariable, int timeIndex ) throws ParseException {

        if (timeVariable instanceof CoordinateAxis && AxisType.Time.equals(((CoordinateAxis) timeVariable).getAxisType())) {

            CoordinateAxis timeAxis = (CoordinateAxis) timeVariable;

            Date epoch = null;

            /*
             * Gets the axis origin. In the particular case of time axis,
             * units are typically written in the form "days since
             * 1990-01-01 00:00:00". We extract the part before "since" as
             * the units and the part after "since" as the date.
             */
            String origin = null;
            final String[] unitsParts = unitString.split("(?i)\\s+since\\s+");
            if (unitsParts.length == 2) {
                unitString = unitsParts[0].trim();
                origin = unitsParts[1].trim();
            } else {
                final Attribute attribute = timeAxis.findAttribute("time_origin");
                if (attribute != null) {
                    origin = attribute.getStringValue();
                }
            }
            if (origin != null) {
                origin = NetCDFUtilities.trimFractionalPart(origin);
                // add 0 digits if absent
                origin = checkDateDigits(origin);

                epoch = (Date) NetCDFUtilities.getAxisFormat(timeAxis.getAxisType(), origin).parseObject(origin);
            }

            if (((CoordinateAxis) timeAxis).isNumeric() && epoch != null && timeAxis instanceof CoordinateAxis1D) {
                Calendar cal = null;
                final CoordinateAxis1D axis1D = (CoordinateAxis1D) timeAxis;
                final double[] values = axis1D.getCoordValues();
                cal = new GregorianCalendar();
                cal.setTime(epoch);

                int vi = (int) Math.floor(values[timeIndex]);
                double vd = values[timeIndex] - vi;
                cal.add(getTimeUnits(unitString, null), vi);
                if (vd != 0.0)
                    cal.add(getTimeUnits(unitString, vd), getTimeSubUnitsValue(unitString, vd));
                Date time = cal.getTime();

                return time;
            }
        } else {
            throw new IllegalArgumentException(MessageFormat.format("The variable {0} is not a valid time variable.",
                    timeVariable.getName()));
        }
        return null;

    }
    /**
     * Converts NetCDF time units into opportune Calendar ones.
     * 
     * @param units
     *                {@link String}
     * @param d
     * @return int
     */
    private static int getTimeUnits( String units, Double vd ) {
        if ("months".equalsIgnoreCase(units)) {
            if (vd == null || vd == 0.0)
                // if no day, it is the first day
                return 1;
            else {
                // TODO: FIXME
            }
        } else if ("days".equalsIgnoreCase(units)) {
            if (vd == null || vd == 0.0)
                return Calendar.DATE;
            else {
                double hours = vd * 24;
                if (hours - Math.floor(hours) == 0.0)
                    return Calendar.HOUR;

                double minutes = vd * 24 * 60;
                if (minutes - Math.floor(minutes) == 0.0)
                    return Calendar.MINUTE;

                double seconds = vd * 24 * 60 * 60;
                if (seconds - Math.floor(seconds) == 0.0)
                    return Calendar.SECOND;

                return Calendar.MILLISECOND;
            }
        }
        if ("hours".equalsIgnoreCase(units) || "hour".equalsIgnoreCase(units)) {
            if (vd == null || vd == 0.0)
                return Calendar.HOUR;
            else {
                double minutes = vd * 24 * 60;
                if (minutes - Math.floor(minutes) == 0.0)
                    return Calendar.MINUTE;

                double seconds = vd * 24 * 60 * 60;
                if (seconds - Math.floor(seconds) == 0.0)
                    return Calendar.SECOND;

                return Calendar.MILLISECOND;
            }
        }
        if ("minutes".equalsIgnoreCase(units)) {
            if (vd == null || vd == 0.0)
                return Calendar.MINUTE;
            else {
                double seconds = vd * 24 * 60 * 60;
                if (seconds - Math.floor(seconds) == 0.0)
                    return Calendar.SECOND;

                return Calendar.MILLISECOND;
            }
        }
        if ("seconds".equalsIgnoreCase(units)) {
            if (vd == null || vd == 0.0)
                return Calendar.SECOND;
            else {
                return Calendar.MILLISECOND;
            }
        }

        return -1;
    }

    /**
     * 
     */
    private static int getTimeSubUnitsValue( String units, Double vd ) {
        if ("days".equalsIgnoreCase(units)) {
            int subUnit = getTimeUnits(units, vd);
            if (subUnit == Calendar.HOUR) {
                double hours = vd * 24;
                return (int) hours;
            }

            if (subUnit == Calendar.MINUTE) {
                double hours = vd * 24 * 60;
                return (int) hours;
            }

            if (subUnit == Calendar.SECOND) {
                double hours = vd * 24 * 60 * 60;
                return (int) hours;
            }

            if (subUnit == Calendar.MILLISECOND) {
                double hours = vd * 24 * 60 * 60 * 1000;
                return (int) hours;
            }

            return 0;
        }

        if ("hours".equalsIgnoreCase(units) || "hour".equalsIgnoreCase(units)) {
            int subUnit = getTimeUnits(units, vd);
            if (subUnit == Calendar.MINUTE) {
                double hours = vd * 24 * 60;
                return (int) hours;
            }

            if (subUnit == Calendar.SECOND) {
                double hours = vd * 24 * 60 * 60;
                return (int) hours;
            }

            if (subUnit == Calendar.MILLISECOND) {
                double hours = vd * 24 * 60 * 60 * 1000;
                return (int) hours;
            }

            return 0;
        }

        if ("minutes".equalsIgnoreCase(units)) {
            int subUnit = getTimeUnits(units, vd);
            if (subUnit == Calendar.SECOND) {
                double hours = vd * 24 * 60 * 60;
                return (int) hours;
            }

            if (subUnit == Calendar.MILLISECOND) {
                double hours = vd * 24 * 60 * 60 * 1000;
                return (int) hours;
            }

            return 0;
        }

        if ("seconds".equalsIgnoreCase(units)) {
            int subUnit = getTimeUnits(units, vd);
            if (subUnit == Calendar.MILLISECOND) {
                double hours = vd * 24 * 60 * 60 * 1000;
                return (int) hours;
            }

            return 0;
        }

        return 0;
    }

    public static String checkDateDigits( String origin ) {
        String digitsCheckedOrigin = "";
        if (origin.indexOf("-") > 0) {
            String tmp = (origin.indexOf(" ") > 0 ? origin.substring(0, origin.indexOf(" ")) : origin);
            String[] originDateParts = tmp.split("-");
            for( int l = 0; l < originDateParts.length; l++ ) {
                String datePart = originDateParts[l];
                while( datePart.length() % 2 != 0 ) {
                    datePart = "0" + datePart;
                }

                digitsCheckedOrigin += datePart;
                digitsCheckedOrigin += (l < (originDateParts.length - 1) ? "-" : "");
            }
        }

        if (origin.indexOf(":") > 0) {
            digitsCheckedOrigin += " ";
            String tmp = (origin.indexOf(" ") > 0 ? origin.substring(origin.indexOf(" ") + 1) : origin);
            String[] originDateParts = tmp.split(":");
            for( int l = 0; l < originDateParts.length; l++ ) {
                String datePart = originDateParts[l];
                while( datePart.length() % 2 != 0 ) {
                    datePart = "0" + datePart;
                }

                digitsCheckedOrigin += datePart;
                digitsCheckedOrigin += (l < (originDateParts.length - 1) ? ":" : "");
            }
        }

        if (digitsCheckedOrigin.length() > 0)
            return digitsCheckedOrigin;

        return origin;
    }
}
