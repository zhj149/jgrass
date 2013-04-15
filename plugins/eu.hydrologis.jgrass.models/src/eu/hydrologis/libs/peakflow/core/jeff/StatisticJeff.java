/*******************************************************************************
 * FILE:        StatisticJeff.java
 * DESCRIPTION:
 * NOTES:       ---
 * AUTHOR:      Antonello Andrea
 * AUTHOR:      Antonello Andrea, Silvia Franceschi, Pisoni Silvano
 * EMAIL:       andrea.antonello@hydrologis.com,silvia.franceschi@hydrologis.com 
 * COPYRIGHT:   Copyright (C) 2005 HydroloGIS / University of Trento / CUDAM, ITALY, GPL
 * VERSION:     $version$
 * CREATED:     $Date: 2005/06/30 08:09:38 $
 * REVISION:    $Revision: 1.3 $
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

package eu.hydrologis.libs.peakflow.core.jeff;

import java.io.PrintStream;

import eu.hydrologis.libs.peakflow.utils.ParameterBox;

/**
 * @author moovida
 */
public class StatisticJeff {

    private ParameterBox fixedParams = null;
    private double tpmax = 0f;
    private final PrintStream out;

    /**
     * @param fixedParameters
     * @param tp_max
     * @param out
     */
    public StatisticJeff( ParameterBox fixedParameters, double tp_max, PrintStream out ) {
        fixedParams = fixedParameters;
        tpmax = tp_max;
        this.out = out;
    }

    /*
     * (non-Javadoc)
     * @see bsh.commands.h.peakflow.jeff.JeffCalculator#calculateJeff()
     */
    public double[][] calculateJeff() {
        out.println("Calculating Jeff...");
        double n_idf = fixedParams.getN_idf();
        double a_idf = fixedParams.getA_idf();

        /*
         * multiplied by 1/3600 1/(1000*3600) gives us Jeff in m/s
         */
        double J = a_idf * Math.pow(tpmax / 3600.0, n_idf - 1) / (1000.0 * 3600.0);
        double h = a_idf * Math.pow(tpmax / 3600.0, n_idf) / 1000.0;
        double[][] result = new double[1][2];
        result[0][0] = J;
        result[0][1] = h;

        return result;
    }
}
