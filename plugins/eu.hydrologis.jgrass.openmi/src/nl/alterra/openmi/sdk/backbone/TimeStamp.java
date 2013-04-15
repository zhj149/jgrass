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
 * @author Rob Knapen, Alterra B.V., The Netherlands
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.backbone;

import java.io.Serializable;
import org.openmi.standard.ITimeStamp;

/**
 * The Timestamp class defines an instant in time. By convention all times
 * are represented as Modified Julian Day. A Modified Julian Day is the
 * Julian date minus 2400000.5. A Modified Julian Day represents the number
 * of days since midnight November 17, 1858 Universal Time on the Julian
 * calendar.
 */
public class TimeStamp implements ITimeStamp, Comparable, Serializable {

    private double time;

    /**
     * Creates a zero time timestamp instance.
     */
    public TimeStamp() {
        this(0);
    }

    /**
     * Creates an instance based on the values of the specified instance.
     *
     * @param source The instance to copy values from
     */
    public TimeStamp(ITimeStamp source) {
        this.setModifiedJulianDay(source.getModifiedJulianDay());
    }

    /**
     * Creates an instance with the specified value.
     *
     * @param modifiedJulianDay The modified julian day for the time stamp
     */
    public TimeStamp(double modifiedJulianDay) {
        time = modifiedJulianDay;
    }

    /**
     * Gets the time, as Modified Julian Day.
     *
     * @return Returns the time as Modified Julian Day
     */
    public double getTime() {
        return time;
    }

    /**
     * Gets the Modified Julian Day.
     *
     * @return double representing the Modified Julian Day
     */
    public double getModifiedJulianDay() {
        return time;
    }

    /**
     * Sets the ModifiedJulianDay.
     *
     * @param value the ModifiedJulianDay to set
     */
    public void setModifiedJulianDay(double value) {
        time = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        TimeStamp t = (TimeStamp) obj;

        if (this.getModifiedJulianDay() != t.getModifiedJulianDay()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Double.valueOf(time).hashCode();
    }

    /**
     * Compares two timestamps.
     *
     * @param obj the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this Object.
     */
    public int compareTo(Object obj) {
        if (obj instanceof TimeStamp) {
            TimeStamp ts = (TimeStamp) obj;

            return Double.compare(ts.getTime(), getTime());
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    public String toString() {
        return Double.toString(time);
    }

}