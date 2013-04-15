/*******************************************************************************
 * FILE:        Coor.java
 * DESCRIPTION:
 * NOTES:       ---
 * AUTHOR:      Antonello Andrea
 * EMAIL:       andrea.antonello@hydrologis.com
 * COMPANY:     HydroloGIS / Engineering, University of Trento / CUDAM
 * COPYRIGHT:   Copyright (C) 2005 HydroloGIS / University of Trento / CUDAM, ITALY, GPL
 * VERSION:     $version$
 * CREATED:     $Date: 2005/01/04 13:06:00 $
 * REVISION:    $Revision: 1.4 $
 *******************************************************************************
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 ******************************************************************************
 *
 * CHANGE LOG:
 *
 * version:
 * comments: changes
 * author:
 * created:
 *****************************************************************************/

package eu.hydrologis.libs.utils;

import com.vividsolutions.jts.geom.Coordinate;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassUtilities;

public class Coords {

	public Coords backCoorPointer = null;
	public Coords forwardCoorPointer = null; /*
												 * pointers to neighboring
												 * points
												 */
	public Coordinate coord = null;
	public boolean hasBeenUsed = false;
	JGrassRegion active = null;

	/**
	 * Coords represents a point of the boundary of a map cell, i.e. if we have
	 * a map cell given by its row and column inside the active region, this
	 * cell will have 4 corners (nodes), one of which this object will
	 * represent.
	 */
	public Coords(JGrassRegion active) {
		this.active = active;
	}

	/**
	 * set the coordinates of the node by its easting and northing
	 * 
	 * @param x -
	 *            easting
	 * @param y -
	 *            northing
	 */
	public void setCoordinate(double x, double y) {
		coord = new Coordinate(x, y);
	}

	/**
	 * set the coordinates from the top-left corner of the cell
	 * 
	 * @param row
	 * @param col
	 */
	public void setCoordinateFromTl(int row, int col) {
		double[] nsew = JGrassUtilities.rowColToNodeboundCoordinates(active,
				row, col);
		coord = new Coordinate(nsew[3], nsew[0]);
	}

	/**
	 * set the coordinates from the top-right corner of the cell
	 * 
	 * @param row
	 * @param col
	 */
	public void setCoordinateFromTr(int row, int col) {
		double[] nsew = JGrassUtilities.rowColToNodeboundCoordinates(active,
				row, col);
		coord = new Coordinate(nsew[2], nsew[0]);
	}

	/**
	 * set the coordinates from the bottom-left corner of the cell
	 * 
	 * @param row
	 * @param col
	 */
	public void setCoordinateFromBl(int row, int col) {
		double[] nsew = JGrassUtilities.rowColToNodeboundCoordinates(active,
				row, col);
		coord = new Coordinate(nsew[3], nsew[1]);
	}

	/**
	 * set the coordinates from the bottom-right corner of the cell
	 * 
	 * @param row
	 * @param col
	 */
	public void setCoordinateFromBr(int row, int col) {
		double[] nsew = JGrassUtilities.rowColToNodeboundCoordinates(active,
				row, col);
		coord = new Coordinate(nsew[2], nsew[1]);
	}
}
