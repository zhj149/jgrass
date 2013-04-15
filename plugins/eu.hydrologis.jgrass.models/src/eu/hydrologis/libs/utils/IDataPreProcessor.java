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
package eu.hydrologis.libs.utils;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Hashtable;

import org.openmi.standard.ITime;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public interface IDataPreProcessor {

    public String KEY_TIMEINTERVAL = "KEY_TIMEINTERVAL"; //$NON-NLS-1$

    /**
     * Set parameters that can be needed by the preprocessors.
     * 
     * @param parametersTable
     */
    public void setParameters( Hashtable<String, Number> parametersTable );

    /**
     * Preparing the environment. A pointer to the data to be processed is passed here. The data is
     * hold in an array:<br>
     * Therefor a data matrix like: <br>
     * t1 v11 v12 v13 v14 <br>
     * t2 v21 v22 v23 v24 <br>
     * t3 v31 ...<br>
     * ...<br>
     * <br>
     * will be passed as an array like:<br>
     * t1 v11 v12 v13 v14 t2 v21 v22 v23 v24 t3 v31 ..<br>
     * 
     * @param timeDataArray the buffer of times in double values as of {@link Date#getTime()} and
     *        the data values in a row for every monitoring point
     * @param cols the columns of the virtual matrix the array represents
     */
    public void validate( ByteBuffer timeDataArray, int cols );

    /**
     * The method that does the needed data processing in order to return the needed value at the
     * required time
     * 
     * @param spot the time for which the data are requested
     * @return an array of values of the same column size as the valuesMatrx.
     */
    public double[] getValuesAt( ITime spot );

    /**
     * Return all the data
     * 
     * @return an array of all values
     */
    public double[] getAllValues();
}
