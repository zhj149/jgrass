/*
 * Created on 15-may-2006
 *
 * gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib��ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
/* CVS MESSAGES:
 *
 * $Id$
 * $Log: RTreeJsi.java,v $
 * Revision 1.1  2007/05/24 11:22:04  volaya
 * *** empty log message ***
 *
 * Revision 1.2  2006/06/05 16:59:08  azabala
 * implementada busqueda de vecino mas proximo a partir de rectangulos
 *
 * Revision 1.1  2006/05/24 21:58:04  azabala
 * *** empty log message ***
 *
 *
 */
package eu.hydrologis.jgrass.models.h.kriging.sextante;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;

public class RTreeJsi {

    private RTree m_Tree;
    private int m_iPoints;
    private double m_dX[];
    private double m_dY[];
    private double m_dZ[];

    /**
     * @param id2valueMap the map containing the starting positions ids 
     *                    and the current value.
     * @param id2CoordinatesMap the map containing the starting positions ids 
     *                    and the coordinates.
     */
    public RTreeJsi( HashMap<Integer, Double> id2valueMap,
            HashMap<Integer, Coordinate> id2CoordinatesMap ) {

        m_Tree = new RTree();
        m_Tree.init(new Properties());

        m_iPoints = id2valueMap.size();

        m_dX = new double[m_iPoints];
        m_dY = new double[m_iPoints];
        m_dZ = new double[m_iPoints];

        Set<Integer> idsSet = id2valueMap.keySet();
        int index = 0;
        for( Integer id : idsSet ) {
            Coordinate coordinate = id2CoordinatesMap.get(id);
            Rectangle rectangle = new Rectangle((float) coordinate.x, (float) coordinate.y,
                    (float) coordinate.x, (float) coordinate.y);
            m_Tree.add(rectangle, index);
            m_dX[index] = coordinate.x;
            m_dY[index] = coordinate.y;
            m_dZ[index] = id2valueMap.get(id);
            index++;
        }

    }

    /**
     * @param x the x coordinate, i.e. easting
     * @param y the y coordinate, i.e. northing
     * @param dDistance
     * @param iMaxPoints
     * @param bAddExactPoint
     * @return
     */
    public PtAndDistance[] getClosestPoints( double x, double y, double dDistance, int iMaxPoints,
            boolean bAddExactPoint ) {

        int i;
        int iID;
        double dDist;
        IdAndDistance idAndDist;
        Point pt = new Point((float) x, (float) y);

        m_Tree.nearest(pt, (float) dDistance, iMaxPoints);
        List<IdAndDistance> arrayPts = m_Tree.getNearestIdsAndDistances();
        int iSize = arrayPts.size();
        PtAndDistance pts[] = new PtAndDistance[iSize];
        for( i = 0; i < iSize; i++ ) {
            idAndDist = arrayPts.get(i);
            iID = idAndDist.getID();
            dDist = idAndDist.getDist();
            pts[i] = new PtAndDistance(new Point3D(m_dX[iID], m_dY[iID], m_dZ[iID]), dDist);
        }

        Arrays.sort(pts);

        iSize = Math.min(iSize, iMaxPoints);
        if (!bAddExactPoint) {
            iSize--;
        }
        PtAndDistance ptsRet[] = new PtAndDistance[iSize];
        int iIndex = 0;
        for( i = 0; i < pts.length && iIndex < ptsRet.length; i++ ) {
            if (pts[i].getDist() > 0 || bAddExactPoint) {
                ptsRet[iIndex] = pts[i];
                iIndex++;
            }
        }

        return ptsRet;

    }

}
