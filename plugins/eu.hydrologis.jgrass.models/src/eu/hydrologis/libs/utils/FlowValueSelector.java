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

/**
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 * 
 */
public class FlowValueSelector {

    private int i = 0;

    private int j = 0;

    private int[][] dir = { { 0, 0 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
            { -1, -1 }, { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };

    public FlowValueSelector(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public void setIndexes(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public int getFlowDirection() {
        int flow = -1;
        for (int k = 1; k < 9; k++) {
            if (dir[k][0] == i && dir[k][1] == j) {
                flow = k;
            }
        }
        return flow;
    }
}
