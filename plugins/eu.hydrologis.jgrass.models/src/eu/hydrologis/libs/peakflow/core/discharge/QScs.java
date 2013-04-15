/*******************************************************************************
 * FILE:        QScs.java
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

package eu.hydrologis.libs.peakflow.core.discharge;

import eu.hydrologis.libs.peakflow.core.iuh.IUHCalculator;
import eu.hydrologis.libs.peakflow.utils.ParameterBox;

public class QScs implements DischargeCalculator {

    private double[][] jeffsup = null;
    private double[][] jeffsub = null;
    private eu.hydrologis.libs.peakflow.utils.ParameterBox parambox = null;
    private IUHCalculator iuhCalculator = null;
    private double[][] qTot = null;
    private double tpmax = 0f;
    // private int timestep = 1;

    public QScs( double[][] jeff, ParameterBox _parambox, IUHCalculator iuhCalc ) {
        parambox = _parambox;
        iuhCalculator = iuhCalc;

        // create jsup, jsub
        jeffsup = new double[jeff.length][2];
        jeffsub = new double[jeff.length][2];
        for( int i = 0; i < jeffsub.length; i++ ) {
            jeffsup[i][0] = jeff[i][0];
            jeffsup[i][1] = jeff[i][1];
            jeffsub[i][0] = jeff[i][0];
            jeffsub[i][1] = jeff[i][2];
        }

        tpmax = (double) (jeffsup[1][0] - jeffsup[0][0]);

    }

    public double calculateQmax() {
        if (qTot == null) {
            calculateQ();
        }

        double qmax = 0f;
        for( int i = 0; i < qTot.length; i++ ) {
            if (qTot[i][1] > qmax)
                qmax = qTot[i][1];
        }

        return qmax;
    }

    public double[][] calculateQ() {
        if (true)
            throw new RuntimeException("SCS not implemented yet!!!");

        QReal qSup = null; // new QReal(parambox, iuhCalculator.getIUHSuperficial(), jeffsup);
        QReal qSub = null; // new QReal(parambox, iuhCalculator.getIUHSubsuperficial(), jeffsub);

        double[][] qsup = qSup.calculateQ();
        double[][] qsub = qSub.calculateQ();

        if (parambox.getFileToDump() != null) {
            // dump to file
            // DataSource outfile1 = new DataSource("file:" + parambox.getOutputFile()
            // + "_q_sup");
            // DataSource outfile2 = new DataSource("file:" + parambox.getOutputFile()
            // + "_q_sub");
            // OutputStreamWriter writeoutfile1 = outfile1.getOutputStreamWriter();
            // OutputStreamWriter writeoutfile2 = outfile2.getOutputStreamWriter();
            // try
            // {
            // writeoutfile1.write("time                  q_sup\n");
            // writeoutfile2.write("time                  q_sub\n");
            // for (int i = 0; i < qsup.length; i++)
            // {
            // writeoutfile1.write(qsup[i][0] + "    " + qsup[i][1] + "\n");
            // }
            // for (int i = 0; i < qsub.length; i++)
            // {
            // writeoutfile2.write(qsub[i][0] + "    " + qsub[i][1] + "\n");
            // }
            // writeoutfile1.close();
            // writeoutfile2.close();
            // }
            // catch (IOException e)
            // {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

        }

        qTot = new double[qsub.length][2];

        for( int i = 0; i < qsup.length; i++ ) {

            qTot[i][0] = qsup[i][0];
            qTot[i][1] = qsub[i][1] + qsup[i][1];
            // (double) FluidUtils.width_interpolate(qsub, (double) i, 0, 1)
        }
        for( int i = qsup.length; i < qsub.length; i++ ) {

            qTot[i][0] = qsub[i][0];
            qTot[i][1] = qsub[i][1];
        }

        return qTot;
    }

    public double getTpMax() {
        return tpmax;
    }

}
