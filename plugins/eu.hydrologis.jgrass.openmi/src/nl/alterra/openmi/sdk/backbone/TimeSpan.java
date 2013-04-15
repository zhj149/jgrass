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
import org.openmi.standard.ITimeSpan;
import org.openmi.standard.ITimeStamp;

/**
 * The TimeSpan class defines a time span given a start and end time, which are
 * always specified as Modified Julian Day values.
 */
public class TimeSpan implements ITimeSpan, Serializable {

    private ITimeStamp start;
    private ITimeStamp end;

    /**
     * Creates an instance with the specified values.
     *
     * @param start Beginning time
     * @param end   Endding time
     */
    public TimeSpan(ITimeStamp start, ITimeStamp end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Creates an instance based on the specified time span.
     *
     * @param source The time span to copy values from
     */
    public TimeSpan(ITimeSpan source) {
        this.start = new TimeStamp(source.getStart());
        this.end = new TimeStamp(source.getEnd());
    }

    /**
     * Gets the start time for the time span.
     *
     * @return The start time as TimeStamp
     */
    public ITimeStamp getStart() {
        return start;
    }

    /**
     * Gets the end time for the time span.
     *
     * @return The end time as TimeStamp
     */
    public ITimeStamp getEnd() {
        return end;
    }

    /**
     * Sets the end time for the time span.
     *
     * @param end End time as TimeStamp
     */
    public void setEnd(ITimeStamp end) {
        this.end = end;
    }

    /**
     * Sets the start time for the time span.
     *
     * @param start Start time as TimeStamp
     */
    public void setStart(ITimeStamp start) {
        this.start = start;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        TimeSpan s = (TimeSpan) obj;

        if ((!getStart().equals(s.getStart())) || (!getEnd().equals(s.getEnd()))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + getStart().hashCode() + getEnd().hashCode();
    }

}