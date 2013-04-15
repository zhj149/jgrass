/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) { 
 * HydroloGIS - www.hydrologis.com                                                   
 * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam                               
 * The JGrass developer team - www.jgrass.org                                         
 * }
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.openmi.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * <p>
 * Helper methods for dealing with time and date
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class DateUtilities {

    /** boolean isTimezoneSet field */
    private static boolean isTimezoneSet = false;

    /**
     * set the timezone.
     * 
     * @param timezone if null, timezone is set to <b>"Africa/Kinshasa"</b>, that has no daylight
     *        saving and is ok for Italy :)
     */
    public static void setTimeZone( String timezone ) {
        if (isTimezoneSet)
            return;

        if (timezone == null)
            System.setProperty("user.timezone", "Africa/Kinshasa");
        System.setProperty("user.timezone", timezone);
        isTimezoneSet = true;
    }

    /**
     * convert a date string of type "yyyy-mm-dd hh:mm:ss" to a java date format
     * 
     * @param datestring
     * @return a java Date object
     */
    public static Date datestringToDate( String datestring ) {
        String[] start = datestring.split("[\\s]+");
        String[] startDay = start[0].trim().split("-");
        String[] startHour = start[1].trim().split(":");
        Calendar startCal = new GregorianCalendar(Integer.parseInt(startDay[0]), Integer
                .parseInt(startDay[1]) - 1, Integer.parseInt(startDay[2]), Integer
                .parseInt(startHour[0]), Integer.parseInt(startHour[1]), Integer
                .parseInt(startHour[2]));
        return startCal.getTime();
    }

    /**
     * convert a date string of type "yyyy-mm-dd hh:mm:ss" to a java sql date format
     * 
     * @param datestring
     * @return a java sql date object
     */
    public static java.sql.Date datestringToSqlDate( String datestring ) {
        return new java.sql.Date(datestringToDate(datestring).getTime());
    }

    /**
     * convert a java date to a string of type "yyyy-mm-dd hh:mm:ss"
     * 
     * @param date
     * @param locale
     * @return string representation of the date
     */
    public static String dateToDateString( Date date, Locale locale ) {
        if (locale == null)
            locale = Locale.ITALIAN;

        String t = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale).format(date);
        String[] tmptime = t.split("\\.");

        String d = DateFormat.getDateInstance(DateFormat.SHORT, locale).format(date);
        String[] tmpdate = d.split("/");
        /*
         * time format is ok, but we have to fill wholes
         */
        String hour = tmptime[0].trim();
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        String minute = tmptime[1].trim();
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        String seconds = "00";
        /*
         * date format is in format dd-mm-yy
         */
        String day = tmpdate[0].trim();
        if (day.length() == 1) {
            day = "0" + day;
        }
        String month = tmpdate[1].trim();
        if (month.length() == 1) {
            month = "0" + day;
        }
        String year = tmpdate[2].trim();
        if (year.length() == 2) {
            Calendar cal = new GregorianCalendar();
            int calyear = cal.get(Calendar.YEAR);
            String half = String.valueOf(calyear).substring(0, 2);
            year = half + year;
        }

        String stringdate = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":"
                + seconds;

        return stringdate;
    }

}
