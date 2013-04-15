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
package eu.hydrologis.libs.utils;

import eu.hydrologis.libs.openmi.ModelsConstants;


/**
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 * @deprecated this class is not needed use {@link ModelsConstants#DIR} directly instead. This class
 *             will be removed as soon as possible.
 */
public class IndexOfPixelSelector {

    private int flow = 0;

    private int[][] dir = {{0, 0}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0},
            {1, 1}};

    public IndexOfPixelSelector() {
    }

    public IndexOfPixelSelector( int flow ) {
        this.flow = flow;
    }

    public void setFlow( int flow ) {
        this.flow = flow;
    }

    public int[] getParameters() {
        int[] parameters = new int[2];

        parameters[0] = dir[flow][0];
        parameters[1] = dir[flow][1];

        return parameters;
    }

}
