/* ***************************************************************************
 *
 *    Copyright (C) 2006 OpenMI Association
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *    or look at URL www.gnu.org/licenses/lgpl.html
 *
 *    Contact info:
 *      URL: www.openmi.org
 *      Email: sourcecode@openmi.org
 *      Discussion forum available at www.sourceforge.net
 *
 *      Coordinator: Roger Moore, CEH Wallingford, Wallingford, Oxon, UK
 *
 *****************************************************************************
 *
 * The classes in the utilities package are mostly a direct translation from
 * the C# version. They successfully pass the unit tests (which were also
 * taken from the C# version), but so far no extensive time as been put into
 * them.
 *
 * @author Rob Knapen, Alterra B.V., The Netherlands
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import org.openmi.standard.ITime;
import org.openmi.standard.ITimeSpan;
import org.openmi.standard.ITimeStamp;

/**
 * Helper class to simplify converting from the standard Java date classes to
 * the modified Julian Date calendar used in OpenMI. Modified Julian Date is
 * expressed as a double and is the number of days since November 17, 1858.
 */
public class CalendarConverter {

    // create the modified julian date offsets (note that months are zero-based):

    private static Calendar modifiedJulianDateZero = new GregorianCalendar(1858, 10, 17);
    private static long modifiedJulianDateZeroMsec = modifiedJulianDateZero.getTimeInMillis();
    private static double msecDay = 24 * 60 * 60 * 1000;
    private static DateFormat format = SimpleDateFormat.getDateTimeInstance();
    private static final String ERR_ILLEGAL_TYPE = "Illegal type used for time, must be org.openmi.standard.ITimeStamp or org.openmi.standard.TimeSpan";

    /**
     * Converts a Gregorian calendar date to a Modified Julian Date.
     *
     * @param gregorianDate The Gregorian date
     * @return The Modified Julian date as double
     */
    public static double gregorian2ModifiedJulian(Calendar gregorianDate) {
        long msec = gregorianDate.getTimeInMillis() - modifiedJulianDateZeroMsec;
        return ((double) msec) / msecDay;
    }

    /**
     * Converts a Modified Julian date to a Gregorian date.
     *
     * @param modifiedJulianDate to convert
     * @return Gregorian date as a Calendar
     */
    public static Calendar modifiedJulian2Gregorian(double modifiedJulianDate) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(modifiedJulianDateZeroMsec + Math.round(modifiedJulianDate * msecDay));
        return c;
    }

    /**
     * Describes an OpenMI ITime as a String. A DateFormat is used to format
     * the strings. This is accessible throught the getFormat() method, which
     * allows some control over how dates and times are represented.
     *
     * @param time to create string representation for
     * @return String representation
     */
    public static String timeToString(ITime time) {
        String timeStr;

        if (time instanceof ITimeStamp) {
            timeStr = format.format(modifiedJulian2Gregorian(((ITimeStamp) time).getModifiedJulianDay()).getTime());
        }
        else if (time instanceof ITimeSpan) {
            timeStr = "[" +
                    format.format(modifiedJulian2Gregorian(((ITimeSpan) time).getStart().getModifiedJulianDay()).getTime()) +
                    ", " +
                    format.format(modifiedJulian2Gregorian(((ITimeSpan) time).getEnd().getModifiedJulianDay()).getTime()) +
                    "]";
        }
        else {
            throw new RuntimeException(ERR_ILLEGAL_TYPE);
        }

        return timeStr;
    }

    /**
     * Sets the parameters for the date and time formatting applied when
     * creating strings from ITime instances. Use the enumerations from the
     * DateFormat and Locale classes.
     * 
     * Examples:
     * setFormat(DateFormat.SHORT, DateFormat.SHORT, new Locale("nl", "nl"));
     * will format dates as "1-2-85 0:00"
     * 
     * setFormat(DateFormat.SHORT, DateFormat.SHORT, new Locale("en", "us"));
     * will format dates as "2/1/85 12:00 AM"
     *
     * @param dateStyle DateFormat date style constant
     * @param timeStyle DateFormat time style constant
     * @param locale    Locale
     */
    public static void setFormat(int dateStyle, int timeStyle, Locale locale) {
        format = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
    }

}
