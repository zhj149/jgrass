/*
 *    JGrass - Free Open Source Java GIS 
 *    http://www.jgrass.org
 *    (C) The JGrass Developers Group (see on www.jgrass.org)
 *
 *    This library is free software; you can redistribute it and/or         
 *    modify it under the terms of the GNU Library General Public 
 *    License as published by the Free Software Foundation; either 
 *    version 2 of the License, or (at your option) any later version. 
 *    
 *    This library is distributed in the hope that it will be useful, 
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
 *    Library General Public License for more details. 
 *    
 *    You should have received a copy of the GNU Library General Public 
 *    License along with this library; if not, write to the Free 
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 
 *    USA 
 */

package eu.hydrologis.libs.utils;

import eu.hydrologis.jgrass.libs.map.RasterData;

/**
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 */
public class MapOrConstantManager {
    private boolean isConstant = true;

    private RasterData valueMap = null;

    private double valueIJ = 0;

    /**
     * used to inizialize constant value
     * 
     * @param value constant value
     */
    public MapOrConstantManager( double value ) {
        isConstant = true;
        valueIJ = value;
    }

    /**
     * used to inizialize map
     * 
     * @param valueMap map to read
     * @param copt
     */
    public MapOrConstantManager( RasterData valueMap ) {
        isConstant = false;
        this.valueMap = valueMap;
    }

    /**
     * get a constant value or reads a value from a map
     * 
     * @param i row index
     * @param j coll index
     * @return
     */
    public double getElementAt( int i, int j ) {
        if (isConstant == false) {
            valueIJ = valueMap.getValueAt(i, j);
        }
        return valueIJ;
    }
}
