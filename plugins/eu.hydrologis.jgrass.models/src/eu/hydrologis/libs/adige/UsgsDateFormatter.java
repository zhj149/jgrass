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
package eu.hydrologis.libs.adige;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Formatter for Usgs dates that are of type <b>"yyyy-MM-ddTHH:mm:ss"</b>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
@SuppressWarnings("serial")
public class UsgsDateFormatter extends SimpleDateFormat {

    public UsgsDateFormatter() {
        super("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
    }

    public Date parse( String source ) throws ParseException {
        source = source.replace('T', ' ');
        return super.parse(source);
    }

    public String formatUsgs( Date date ) {
        String format = format(date);
        format = format.replace(' ', 'T');
        return format;
    }
}
