/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package eu.hydrologis.jgrass.ui.utilities.date;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;

import eu.hydrologis.jgrass.ui.utilities.UiUtilitiesPlugin;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class CalendarTimeGroup {

    public Group dateGroup = null;
    private DateTime calendar;
    private DateTime time;
    private Format standardDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$
    private NumberFormat twoNumformatter = new DecimalFormat("00"); //$NON-NLS-1$
    private NumberFormat fourNumformatter = new DecimalFormat("0000"); //$NON-NLS-1$

    public CalendarTimeGroup( Composite parent, int style, int dateStyle, int timestyle,
            String title, int columnspan, int rowspan ) {
        dateGroup = new Group(parent, SWT.None);
        dateGroup.setText(title);
        if (columnspan <= 0)
            columnspan = 1;
        if (rowspan <= 0)
            rowspan = 1;
        dateGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, columnspan, rowspan));

        dateGroup.setLayout(new GridLayout(1, true));
        calendar = new DateTime(dateGroup, dateStyle | SWT.BORDER);
        calendar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        time = new DateTime(dateGroup, SWT.BORDER | timestyle);
        time.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    public void addSelectionListener( SelectionListener listener ) {
        time.addSelectionListener(listener);
        calendar.addSelectionListener(listener);
    }

    /**
     * @return the selected date object
     */
    @SuppressWarnings("nls")
    public Date getDate() {
        Date startDate = new Date();
        try {
            startDate = (Date) standardDateFormatter.parseObject(fourNumformatter.format(calendar
                    .getYear())
                    + "-"
                    + twoNumformatter.format(calendar.getMonth() + 1)
                    + "-"
                    + twoNumformatter.format(calendar.getDay())
                    + " "
                    + twoNumformatter.format(time.getHours())
                    + ":"
                    + twoNumformatter.format(time.getMinutes()));
        } catch (ParseException e1) {
            UiUtilitiesPlugin.log("UiUtilitiesPlugin problem", e1);  //$NON-NLS-1$
            e1.printStackTrace();
        }
        return startDate;
    }

    /**
     * @param date the date object from whch to set the date and time
     */
    public void setDate( Date date ) {
        String dateStr = standardDateFormatter.format(date);
        setDate(dateStr);
    }

    /**
     * @param dateStr the date string from whch to set the date and time (format: yyyy-MM-dd HH:mm)
     */
    @SuppressWarnings("nls")
    public void setDate( String dateStr ) {
        String[] dateTime = dateStr.split("\\s");
        String[] d = dateTime[0].split("-");
        String[] t = dateTime[1].split(":");

        calendar.setYear(Integer.parseInt(d[0]));
        calendar.setMonth(Integer.parseInt(d[1]) - 1);
        calendar.setDay(Integer.parseInt(d[2]));
        time.setHours(Integer.parseInt(t[0]));
        time.setMinutes(Integer.parseInt(t[1]));
    }

    /**
     * @param pattern pattern for date formatting, can be null, in which case the format is
     *        yyyy-MM-dd HH:mm
     * @return the string of the date following the supplied pattern
     */
    public String getDateString( String pattern ) {
        Format dateFormat = null;
        if (pattern != null) {
            dateFormat = new SimpleDateFormat(pattern);
        } else {
            dateFormat = standardDateFormatter;
        }
        return dateFormat.format(getDate());
    }

    /**
     * @param ctGroup the calendar time group from whch to set the date and time
     */
    public void setToCalendarTime( CalendarTimeGroup ctGroup ) {
        setDate(ctGroup.getDate());
    }
    
    public void setEnabled(boolean enabled) {
        dateGroup.setEnabled(enabled);
        calendar.setEnabled(enabled);
        time.setEnabled(enabled);
    }
}
