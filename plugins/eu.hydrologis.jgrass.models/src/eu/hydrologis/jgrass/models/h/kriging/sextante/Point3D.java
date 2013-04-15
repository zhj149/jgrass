/*******************************************************************************
Point3D.java
Copyright (C) Victor Olaya

Adapted from SAGA, System for Automated Geographical Analysis.
Copyrights (c) 2002-2005 by Olaf Conrad
Portions (c) 2002 by Andre Ringeler
Portions (c) 2005 by Victor Olaya

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*******************************************************************************/ 
package eu.hydrologis.jgrass.models.h.kriging.sextante;

public class Point3D {
	
	private double m_dX, m_dY, m_dZ;
	
	public Point3D(double dX, double dY, double dZ){
		
		m_dX = dX;
		m_dY = dY;
		m_dZ = dZ;
		
	}
	
	public double getZ() {
		
		return m_dZ;
		
	}
	public void setZ(double dZ) {
		
		m_dZ = dZ;
		
	}
	
	public double getX() {
		
		return m_dX;
		
	}
	
	public void setX(double dX) {
		
		m_dX = dX;
		
	}
	
	public double getY() {
		
		return m_dY;
		
	}
	
	public void setY(double dY) {
		
		m_dY = dY;
		
	}
	
}
