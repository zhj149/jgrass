/*******************************************************************************
 * FILE:        DischargeCalculator.java
 * DESCRIPTION:
 * NOTES:       ---
 * AUTHOR:      Antonello Andrea, Silvia Franceschi, Pisoni Silvano
 * EMAIL:       andrea.antonello@hydrologis.com,silvia.franceschi@hydrologis.com 
 * COMPANY:     HydroloGIS / Engineering, University of Trento / CUDAM
 * COPYRIGHT:   Copyright (C) 2005 HydroloGIS / University of Trento / CUDAM, ITALY, GPL
 * VERSION:     $version$
 * CREATED:     $Date: 2005/06/24 08:01:16 $
 * REVISION:    $Revision: 1.1 $
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

package eu.hydrologis.libs.peakflow.core.discharge;

/**
 * @author moovida
 */
public interface DischargeCalculator
{

  /**
   * Calculate the maximum discharge
   * 
   * @return
   */
  public double calculateQmax();

  /**
   * Calculate the discharge hydrogram
   * 
   * @return
   */
  public double[][] calculateQ();
  
  /**
   * @return the maximum rain time
   */
  public double getTpMax();

}
