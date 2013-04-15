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
package eu.hydrologis.jgrass.models.h.saintgeo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIOException;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.libs.utils.LinearAlgebra;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class h_saintgeo extends ModelsBackbone {
    private final double TOL_mu = 0.001;
    private final int MAX_CICLI = 1000;
    private final double TOLL = 0.001;
    private final double MIN_TIR = 0.01;
    private final double h_DEF = 0.001;
    private final double G = 9.806;
    private final double Cq = 0.41;

    private String modelDescription = "h.saintgeo\n   --itscalar-qhead qhead "
            + "\n   --itscalar-qlateral qlateral "
            + "\n   --itscalar-qlateralPosition qlateralPosition "
            + "\n   --itscalar-qconfluence qconfluence "
            + "\n   --itscalar-qconfluencePosition qconfluencePosition "
            + "\n   --iscalar-sections sections "
            + "\n   --iscalar-downstreamlevel downstreamlevel " + "\n   --otscalar-out output ";

    public final static String qHeadID = "qhead";
    private IInputExchangeItem qHeadInputEI;
    private ILink qHeadInputLink = null;

    public final static String qLateralID = "qlateral";
    private IInputExchangeItem qLateralInputEI;
    private ILink qLateralInputLink = null;

    public final static String qLateralPositionID = "qlateralPosition";
    private IInputExchangeItem qLateralPositionInputEI;
    private ILink qLateralPositionInputLink = null;

    public final static String qConfluenceID = "qconfluence";
    private IInputExchangeItem qConfluenceInputEI;
    private ILink qConfluenceInputLink = null;

    public final static String qConfluencePositionID = "qconfluencePosition";
    private IInputExchangeItem qConfluencePositionInputEI;
    private ILink qConfluencePositionInputLink = null;

    public final static String sectionID = "sections";
    private IInputExchangeItem sectionsInputEI;
    private ILink sectionsInputLink = null;

    public final static String downstreamlevelID = "downstreamlevel";
    private IInputExchangeItem downstreamlevelInputEI;
    private ILink downstreamlevelInputLink = null;

    public final static String outputID = "out";
    private IOutputExchangeItem outputOutputEI;
    private ILink outputOutputLink = null;

    private HashMap<Integer, Double> lateralId2ProgressiveMap;
    private HashMap<Integer, Double> confluenceId2ProgressiveMap;
    private HashMap<Integer, Double> confluenceId2DischargeMap;
    private double qHeadPrevious;
    private HashMap<Integer, Double> lateralId2DischargeMap;
    private List<Section> sectionsList;
    private LinearAlgebra linearAlgebra = new LinearAlgebra();
    private double downstreamLevelPrevious;
    private double[] waterLevel = null;
    private double[] discharge;
    private double[][] idrgeo;
    private double[] waterLevel_previous;
    private double[] celerity;
    private int sectionsNumber;
    private double DELT = -1;
    private int SCELTA_A_MONTE;
    private int SCELTA_A_VALLE;
    private double[] ql;
    private double[] DELXM;

    public h_saintgeo() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_saintgeo( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String deltaTArg = null;

        SCELTA_A_MONTE = 1;
        SCELTA_A_VALLE = 2;

        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals(ModelsConstants.DELTAT)) {
                deltaTArg = argument.getValue();
            }
        }

        double deltaTinMilliSeconds = Double.parseDouble(deltaTArg);
        DELT = deltaTinMilliSeconds / 1000.0;

        qHeadInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        qLateralInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        qLateralPositionInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        qConfluenceInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        qConfluencePositionInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        sectionsInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        downstreamlevelInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        outputOutputEI = ModelsConstants.createDummyOutputExchangeItem(this);
    }
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        // if (!(time instanceof HydrologisDate)) {
        // throw new ModelsIllegalargumentException("Time is not HydrologisDate!", this);
        // }
        // HydrologisDate runningDate = (HydrologisDate) time;

        /*
         * head discharge
         */
        ScalarSet qHeadScalarSet = ModelsConstants.getScalarSetFromLink(qHeadInputLink, time, err);
        // this scalarset is: colnum, id, qhead
        // read the discharge for this time
        double qHead = -1;
        if (qHeadScalarSet != null) {
            qHead = qHeadScalarSet.getScalar(2);
            qHeadPrevious = qHead;
        } else {
            qHead = qHeadPrevious;
        }

        double downstreamLevel = -1;
        if (downstreamlevelInputLink != null) {
            ScalarSet downstreamlevelScalarSet = ModelsConstants.getScalarSetFromLink(
                    downstreamlevelInputLink, time, err);
            // this scalarset is: colnum, id, qhead
            // read the discharge for this time
            if (downstreamlevelScalarSet != null) {
                downstreamLevel = downstreamlevelScalarSet.getScalar(2);
                downstreamLevelPrevious = downstreamLevel;
            } else {
                downstreamLevel = downstreamLevelPrevious;
            }
        }

        /*
         * lateral discharge tribute or offtake
         */
        if (qLateralPositionInputLink != null && qLateralInputLink != null) {
            if (lateralId2ProgressiveMap == null) {
                // read the progressive positions and id of the lateral points
                ScalarSet qLateralPositionScalarSet = ModelsConstants.getScalarSetFromLink(
                        qLateralPositionInputLink, time, err);
                lateralId2ProgressiveMap = new HashMap<Integer, Double>();
                lateralId2DischargeMap = new HashMap<Integer, Double>();
                for( int i = 1; i < qLateralPositionScalarSet.size(); i++ ) {
                    Double id = qLateralPositionScalarSet.get(i);
                    i++;
                    Double progressive = qLateralPositionScalarSet.get(i);
                    lateralId2ProgressiveMap.put(id.intValue(), progressive);
                }
            }
            // read the discharge from the lateral points for this time
            ScalarSet qLateralScalarSet = ModelsConstants.getScalarSetFromLink(qLateralInputLink,
                    time, err);
            if (qLateralScalarSet != null) {
                lateralId2DischargeMap.clear();
                // this scalarset is: colnum, id, qlat1, id, qlat2, id qlat3...
                for( int i = 1; i < qLateralScalarSet.size(); i++ ) {
                    Double id = qLateralScalarSet.get(i);
                    i++;
                    Double discharge = qLateralScalarSet.get(i);
                    lateralId2DischargeMap.put(id.intValue(), discharge);
                }
            }
        }

        /*
         * lateral immission from confluences
         */
        if (qConfluenceInputLink != null) {
            if (confluenceId2ProgressiveMap == null) {
                // read the progressive positions and id of the confluence points
                ScalarSet qConfluencePositionScalarSet = ModelsConstants.getScalarSetFromLink(
                        qConfluencePositionInputLink, time, err);
                confluenceId2ProgressiveMap = new HashMap<Integer, Double>();
                confluenceId2DischargeMap = new HashMap<Integer, Double>();
                for( int i = 1; i < qConfluencePositionScalarSet.size(); i++ ) {
                    Double id = qConfluencePositionScalarSet.get(i);
                    i++;
                    Double progressive = qConfluencePositionScalarSet.get(i);
                    confluenceId2ProgressiveMap.put(id.intValue(), progressive);
                }
            }
            // read the discharge from the lateral points for this time
            ScalarSet qConfluenceScalarSet = ModelsConstants.getScalarSetFromLink(
                    qConfluenceInputLink, time, err);
            if (qConfluenceScalarSet != null) {
                confluenceId2DischargeMap.clear();
                // this scalarset is: colnum, id, qlat1, id, qlat2, id qlat3...
                for( int i = 1; i < qConfluenceScalarSet.size(); i++ ) {
                    Double id = qConfluenceScalarSet.get(i);
                    i++;
                    Double discharge = qConfluenceScalarSet.get(i);
                    confluenceId2DischargeMap.put(id.intValue(), discharge);
                }
            }
        }

        /*
         * trasversal sections
         */
        if (sectionsList != null) {
            ScalarSet sectionsScalarSet = ModelsConstants.getScalarSetFromLink(sectionsInputLink,
                    time, err);
            sectionsList = new ArrayList<Section>();
            int columns = sectionsScalarSet.get(0).intValue();
            /*
             * the scalarset is: 
             * id, prograssive, sectionStartIndex, sectionEndIndex, 
             * trasv_progressive_i, trasv_elevation_i, ks_i
             */
            for( int i = 1; i < sectionsScalarSet.size(); ) {
                int sectionId = sectionsScalarSet.get(i).intValue();
                i++;
                Double sectionProgressive = sectionsScalarSet.get(i);
                i++;
                int startIndex = sectionsScalarSet.get(i).intValue();
                i++;
                int endIndex = sectionsScalarSet.get(i).intValue();
                i++;
                List<Double> progressiveSec = new ArrayList<Double>();
                List<Double> elevationSec = new ArrayList<Double>();
                List<Double> ksSec = new ArrayList<Double>();
                for( int j = 0; j < columns - 4; ) {
                    progressiveSec.add(sectionsScalarSet.get(j));
                    j++;
                    i++;
                    elevationSec.add(sectionsScalarSet.get(j));
                    j++;
                    i++;
                    ksSec.add(sectionsScalarSet.get(j));
                    j++;
                    i++;
                }

                Section section = new Section(sectionId, sectionProgressive, startIndex, endIndex,
                        progressiveSec, elevationSec, ksSec);
                sectionsList.add(section);
            }
            Set<Integer> confluenceIdSet = confluenceId2ProgressiveMap.keySet();
            Set<Integer> lateralIdSet = lateralId2ProgressiveMap.keySet();
            for( int i = 0; i < sectionsList.size() - 1; i++ ) {
                Section first = sectionsList.get(i);
                Section second = sectionsList.get(i + 1);
                double p1 = first.getProgressiveAlongReach();
                double p2 = second.getProgressiveAlongReach();
                if (p1 > p2) {
                    throw new ModelsIOException(
                            "The sections have to be in ascending progressive distance order.",
                            this);
                }

                for( Integer id : confluenceIdSet ) {
                    Double prog = confluenceId2ProgressiveMap.get(id);
                    if (prog > p1 && prog < p2) {
                        double d1 = prog - p1;
                        double d2 = p2 - prog;
                        if (d2 > d1) {
                            first.addQDeltaPointId(id);
                        } else {
                            second.addQDeltaPointId(id);
                        }
                    }
                }
                for( Integer id : lateralIdSet ) {
                    Double prog = lateralId2ProgressiveMap.get(id);
                    if (prog > p1 && prog < p2) {
                        first.addQDeltaPointId(id);
                    }
                }
            }

            sectionsNumber = sectionsList.size();
            Collections.sort(sectionsList);
            double minElevationFirst = sectionsList.get(0).getMinElevation();
            double minElevationLast = sectionsList.get(sectionsList.size() - 1).getMinElevation();
            if (minElevationFirst < minElevationLast) {
                Collections.reverse(sectionsList);
            }
            double proggy = 0;
            for( int i = 0; i < sectionsList.size() - 1; i++ ) {
                Section section1 = sectionsList.get(i);
                Section section2 = sectionsList.get(i + 1);

                if (i == 0) {
                    section1.setProgressiveAlongReach(0);
                }
                proggy = proggy
                        + Math.abs(section1.getProgressiveAlongReach()
                                - section2.getProgressiveAlongReach());
                section2.setProgressiveAlongReach(proggy);
            }

            ql = new double[sectionsNumber];
        }

        /*
         * defining initial conditions
         */
        if (waterLevel == null) {
            waterLevel = new double[sectionsNumber];
            waterLevel_previous = new double[sectionsNumber];
            discharge = new double[sectionsNumber - 1];
            celerity = new double[sectionsNumber - 1];
            DELXM = new double[sectionsNumber - 1];

            // tiranti primo tentativo
            for( int i = 0; i < sectionsNumber; i++ ) {
                double minsez = sectionsList.get(i).getMinElevation();
                waterLevel[i] = minsez + 0.5;
            }

            // portata di primo tentativo e' sempre la prima in ingresso
            for( int i = 0; i < sectionsNumber - 1; i++ ) {
                discharge[i] = qHead;
                DELXM[i] = sectionsList.get(i + 1).getProgressiveAlongReach()
                        - sectionsList.get(i).getProgressiveAlongReach();
            }

            /*
             * Calcolo la condizione di moto uniforme per una portata 
             * pari alla prima portata in ingresso
             */
            /* gaukler */
            ris_gauk(qHead, waterLevel, sectionsList);
            idrgeo = area_bagnata(waterLevel, sectionsList);
            double error = 100.0;

            /*
             * Considero raggiunta la condizione di moto uniforme 
             * quando lo scarto massimo fra i tiranti
             * calcolati a due istanti successivi e' minore di TOL_mu
             */
            int conta_cicli = 0;

            while( error >= (TOL_mu / 10.0) && conta_cicli <= 60000 ) {
                for( int i = 0; i < sectionsNumber; i++ )
                    waterLevel_previous[i] = waterLevel[i];
                new_tirante(sectionsList, waterLevel, discharge, celerity, DELXM, SCELTA_A_MONTE,
                        qHead, qHeadPrevious, SCELTA_A_VALLE, downstreamLevel, ql);
                error = Math.abs(waterLevel_previous[0] - waterLevel[0]);
                for( int i = 1; i < sectionsNumber - 2; i++ ) {
                    double new_err = Math.abs(waterLevel_previous[i] - waterLevel[i]);
                    if (new_err >= error)
                        error = new_err;
                }
                conta_cicli = conta_cicli + 1;
            }
            System.out.println("Cicli per il moto permanente " + conta_cicli); //$NON-NLS-1$

            /*
             * Calcolo il moto. inizio
             */
            conta_cicli = 0;
            // conta_warningqin = 1;
            // conta_warningtirout = 1;
        }

        for( int i = 0; i < sectionsNumber - 1; i++ ) {
            ql[i] = 0;
            Section section = sectionsList.get(i);
            if (section.hasQDeltas()) {
                List<Integer> ids = section.getQDeltaPointsIds();
                for( Integer id : ids ) {
                    Double discharge = lateralId2DischargeMap.get(id);
                    if (discharge == null) {
                        discharge = confluenceId2DischargeMap.get(id);
                        if (discharge == null) {
                            continue;
                        }
                    }
                    ql[i] = ql[i] + discharge / DELXM[i];
                }
            }
        }

        /*
         * a questo punto ho definito tutti gli elementi che mi 
         * servono per chiamare la funzione
         * che genera il sistema tridiagonale e calcola il nuovo tirante
         */
        new_tirante(sectionsList, waterLevel, discharge, celerity, DELXM, SCELTA_A_MONTE, qHead,
                qHeadPrevious, SCELTA_A_VALLE, downstreamLevel, ql);

        /* scrittura del file di output ogni 1 secondi (provvisorio) */
        idrgeo = area_bagnata(waterLevel, sectionsList);

        ScalarSet outScalarSet = new ScalarSet();
        outScalarSet.add(10.0);
        for( int i = 0; i < sectionsNumber - 1; i++ ) {
            Section section = sectionsList.get(i);
            outScalarSet.add(new Double(section.getId()));
            outScalarSet.add(section.getProgressiveAlongReach());
            double froudeNumber = (Math.abs(celerity[i]) / Math.sqrt(G
                    * (idrgeo[i][0] / idrgeo[i][3])));
            outScalarSet.add(froudeNumber);
            outScalarSet.add(discharge[i] < 0.0 ? 0.0 : discharge[i]);
            outScalarSet.add(celerity[i] < 0.0 ? 0.0 : celerity[i]);
            outScalarSet.add(waterLevel[i]);
            outScalarSet.add(idrgeo[i][0]);
            double minsez = section.getMinElevation();
            outScalarSet.add(minsez);
            int dx = section.getStartNodeIndex();
            outScalarSet.add(section.getElevationAt(dx));
            int sx = section.getEndNodeIndex();
            outScalarSet.add(section.getElevationAt(sx));
        }
        Section section = sectionsList.get(sectionsNumber - 1);
        double froudeNumber = (Math.abs(discharge[sectionsNumber - 2]
                / idrgeo[sectionsNumber - 1][0]) / Math.sqrt(G
                * (idrgeo[sectionsNumber - 1][0] / idrgeo[sectionsNumber - 1][3])));
        outScalarSet.add(new Double(section.getId()));
        outScalarSet.add(section.getProgressiveAlongReach());
        outScalarSet.add(froudeNumber);
        outScalarSet.add(discharge[sectionsNumber - 2] < 0.0 ? 0.0 : discharge[sectionsNumber - 2]);
        double cel = discharge[sectionsNumber - 2] / idrgeo[sectionsNumber - 1][0];
        outScalarSet.add(cel < 0.0 ? 0.0 : cel);
        outScalarSet.add(waterLevel[sectionsNumber - 1]);
        outScalarSet.add(idrgeo[sectionsNumber - 1][0]);
        double minsez = section.getMinElevation();
        outScalarSet.add(minsez);
        int dx = section.getStartNodeIndex();
        outScalarSet.add(section.getElevationAt(dx));
        int sx = section.getEndNodeIndex();
        outScalarSet.add(section.getElevationAt(sx));

        return outScalarSet;
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(qHeadID)) {
            qHeadInputLink = link;
        } else if (id.equals(qLateralID)) {
            qLateralInputLink = link;
        } else if (id.equals(qLateralPositionID)) {
            qLateralPositionInputLink = link;
        } else if (id.equals(qConfluenceID)) {
            qConfluenceInputLink = link;
        } else if (id.equals(qConfluencePositionID)) {
            qConfluencePositionInputLink = link;
        } else if (id.equals(sectionsInputLink)) {
            sectionsInputLink = link;
        } else if (id.equals(downstreamlevelInputLink)) {
            downstreamlevelInputLink = link;
        } else if (id.equals(outputID)) {
            outputOutputLink = link;
        }
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return qHeadInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return qLateralInputEI;
        } else if (inputExchangeItemIndex == 2) {
            return qLateralPositionInputEI;
        } else if (inputExchangeItemIndex == 3) {
            return qConfluenceInputEI;
        } else if (inputExchangeItemIndex == 4) {
            return qConfluencePositionInputEI;
        } else if (inputExchangeItemIndex == 5) {
            return sectionsInputEI;
        } else if (inputExchangeItemIndex == 6) {
            return downstreamlevelInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 7;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return outputOutputEI;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public String getModelDescription() {
        return modelDescription;
    }

    public void removeLink( String id ) {
        if (id.equals(qHeadID)) {
            qHeadInputLink = null;
        } else if (id.equals(qLateralID)) {
            qLateralInputLink = null;
        } else if (id.equals(qLateralPositionID)) {
            qLateralPositionInputLink = null;
        } else if (id.equals(qConfluenceID)) {
            qConfluenceInputLink = null;
        } else if (id.equals(qConfluencePositionID)) {
            qConfluencePositionInputLink = null;
        } else if (id.equals(sectionsInputLink)) {
            sectionsInputLink = null;
        } else if (id.equals(downstreamlevelInputLink)) {
            downstreamlevelInputLink = null;
        } else if (id.equals(outputID)) {
            outputOutputLink = null;
        }
    }

    /**
     * @param q
     * @param tirante
     * @param sez
     */
    private void ris_gauk( double q, double[] tirante, List<Section> sez ) {
        double toll, conta_cicli;
        double IF, max_tir, tir_dx, tir_sx, tir_med, val_dx, val_sx, val_med;
        double[][] idrgeo;
        /* mi serve un vettore completo per riuscire a fare i conti successivi */
        int imax = sez.size();
        double[] minsez = new double[imax];
        double[] maxsez = new double[imax];

        for( int i = 0; i < imax; i++ ) {
            Section section = sez.get(i);
            minsez[i] = section.getMinElevation();
            maxsez[i] = section.getMaxElevation();
            tirante[i] = minsez[i] + 1;
        }
        /* calcolo il tirante di moto uniforme per le sezioni del tratto j-esimo */
        for( int i = 0; i < imax; i++ ) {
            /* definisco la pendenza del fondo nella sezione i-esima */
            if (i == 0 || i == 1)
                IF = (minsez[i] - minsez[i + 1])
                        / (sez.get(i + 1).getProgressiveAlongReach() - sez.get(i)
                                .getProgressiveAlongReach());
            else if (i == imax - 1 || i == imax - 2)
                IF = (minsez[i - 1] - minsez[i])
                        / (sez.get(i).getProgressiveAlongReach() - sez.get(i - 1)
                                .getProgressiveAlongReach());
            else
                IF = (minsez[i - 2] - minsez[i + 2])
                        / (sez.get(i + 2).getProgressiveAlongReach() - sez.get(i - 2)
                                .getProgressiveAlongReach());
            if (IF <= 0)
                IF = (minsez[0] - minsez[imax - 1])
                        / (sez.get(imax - 1).getProgressiveAlongReach() - sez.get(0)
                                .getProgressiveAlongReach());
            /*
             * calcolo la funzione per i valori estremi iniziali cerco il valore di sponda piu'
             * piccolo
             */
            max_tir = maxsez[i];
            tir_dx = max_tir;
            tir_sx = minsez[i] + MIN_TIR;
            toll = 100;
            conta_cicli = 0;

            while( toll >= TOLL && conta_cicli <= MAX_CICLI ) {
                tirante[i] = tir_dx;
                idrgeo = area_bagnata(tirante, sez);
                val_dx = q - idrgeo[i][0] * idrgeo[i][4] * Math.pow(IF, (0.5))
                        * Math.pow(idrgeo[i][2], (2.0 / 3.0));
                tirante[i] = tir_sx;
                idrgeo = area_bagnata(tirante, sez);
                val_sx = q - idrgeo[i][0] * idrgeo[i][4] * Math.pow(IF, (0.5))
                        * Math.pow(idrgeo[i][2], (2.0 / 3.0));
                if ((val_dx * val_sx) > 0) {
                    System.out.println("non e' possibile calcolare il moto uniforme alla sezione " //$NON-NLS-1$
                            + i);
                    System.out.println("\nnon ho trovato la soluzione"); //$NON-NLS-1$
                }
                tir_med = (tir_dx + tir_sx) / 2.0;
                tirante[i] = tir_med;
                idrgeo = area_bagnata(tirante, sez);
                val_med = q - idrgeo[i][0] * idrgeo[i][4] * Math.pow(IF, (0.5))
                        * Math.pow(idrgeo[i][2], (2.0 / 3.0));
                toll = Math.abs(tir_dx - tir_sx);
                if ((val_dx * val_med) < 0)
                    tir_sx = tir_med;
                else
                    tir_dx = tir_med;
                conta_cicli = conta_cicli + 1;
            }
        }
    }

    /**
     * <p>
     * Questa funzione calcola
     * </p>
     * <ul>
     * <li> l'area bagnata </li>
     * <li> il perimetro bagnato </li>
     * <li> il raggio idraulico </li>
     * <li> la larghezza della superficie libera </li>
     * <li> la scabrezza efficace </li>
     * <li> il coeff. alfa di Coriolis </li>
     * </ul>
     * in ogni sezione noto il tirante.
     * <p>
     * La funzione restituisce una matrice tirase che ha come elementi di ogni colonna della i-esima
     * riga le grandezze precedenti relative alla i-esima sezione nello stesso ordine in cui sono
     * state elencate.
     * </p>
     * <p>
     * La funzione ha come argomenti:
     * </p>
     * <ul>
     * <li>il vettore dei tiranti</li>
     * <li>il vettore che contiene le sezioni di calcolo</li>
     * </ul>
     * <p>
     * <b>L'area bagnata</b> e' calcolata come somma dei trapezi che si ottengono tracciando da
     * ogni punto di stazione delle suddivioni verticali; ogni trapezio e' definito da una base
     * destra (base_dx) una base sinistra (base_sx) e un'altezza (altezza).
     * </p>
     * <p>
     * <b>Il perimetro bagnato</b> e' calcolato come somma dei tratti bagnati del fondo alveo.
     * </p>
     * <p>
     * <b>Il raggio idraulico</b> e' calcolato direttamente come da definizione.
     * </p>
     * <p>
     * <b>La larghezza della superficie libera</b> coincide con l'altezza dei trapezi definiti per
     * calcolare l'area bagnata, pertanto il calcolo risulta banale.
     * </p>
     * <p>
     * <b>Il coefficiente di scabrezza efficace</b> e' calcolato con il metodo di Egelund.
     * Suddivisa la sezione come per il calcolo dell'area bagnata per ogni trapezio si calcola la
     * quantita' <b>Ks(j)*Y(j)^(5/3)*B(j)</b> dove:
     * </p>
     * <ul>
     * <li><b>Ks(j)</b> e' il coeff. di Gaukler-Strickler per il tratto j-esimo</li>
     * <li><b>Y(j)</b> e' l'altezza idrica nel trapezio j-esimo che si ottiene come rapporto fra
     * l'area del trapezio j-esimo e la relativa larghezza sulla superficie libera</li>
     * <li><b>B(j)</b> e' la larghezza della superficie libera relativa al trapezio j-esimo</li>
     * </ul>
     * <p>
     * n.b. il raggio idraulico del trapezio j-esimo si ritiene approsimabile con l'altezza idrica,
     * cio' e' lecito solo nell'ipotesi di sezione larga
     * </p>
     * <p>
     * <b>Il coefficiente di scabrezza efficace</b> si ottiene dividendo la somma di tutte le
     * quantita' per <b>(A*RH^(2/3))</b>, dove A e' l'area bagnata complessiva e RH e' il raggio
     * idraulico riferito all'intera sezione.</li>
     * </p>
     * <p>
     * <b>Il coefficiente alfa di Coriolis</b> e' calcolato con il seguente metodo: suddivisa la
     * sezione come per il calcolo dell'area bagnata per ogni trapezio si calcola la quantita'
     * <b>Ks(j)^2*A(j)^(7/3)/P(j)^(4/3)</b> dove:
     * </p>
     * <ul>
     * <li><b>Ks(j)</b> e' il coeff. di G-S per il tratto j-esimo</li>
     * <li><b>A(j)</b> e' l'area del trapezio j-esimo</li>
     * <li><b>P(j)</b> e' il contorno bagnato relativo al trapezio j-esimo</li>
     * </ul>
     * <p>
     * <b>Il coefficiente di Coriolis</b> si ottiene dividendo la somma di tutte le quantita' per
     * <b>(ATOT*KsTOT^2*RHTOT^(4/3))</b> dove:
     * <ul>
     * <li>ATOT l'area dell'intera sezione</li>
     * <li>KsTOT il coefficiente di scabrezza efficace </li>
     * <li>RHTOT il raggio idraulico della sezione</li>
     * </ul>
     * </p>
     * 
     * @param tirante il vettore dei tiranti
     * @param sez il vettore che contiene le sezioni di calcolo
     * @return una matrice tirase che ha come elementi di ogni colonna della i-esima riga le
     *         grandezze precedenti relative alla i-esima sezione nello stesso ordine in cui sono
     *         state elencate
     */
    private double[][] area_bagnata( double[] tirante, List<Section> sez ) {
        double dx, sx; /* dx e sx=limimiti destro e sinistro dell'alveo */
        double area_b, base_dx, base_sx, altezza;
        double peri_b;
        double larghe_b;
        double gau_b;
        double alfa_num, alfa_den;
        double area_loc, peri_loc, gau_loc; /* *_loc si riferiscono al trapezio in esame */

        int imax = sez.size();
        double[][] tirase = new double[imax][6];

        /*
         * Calcolo l'area bagnata il perimetro bagnato la larghezza della superficie libera il
         * coefficiente di scabrezza efficace e il coefficiente alfa di Corilis nella posizione i
         */
        for( int i = 0; i < imax; i++ ) {

            Section section = sez.get(i);
            area_b = 0;
            peri_b = 0;
            larghe_b = 0;
            gau_b = 0;
            gau_loc = 0;
            alfa_num = 0;
            alfa_den = 0;
            dx = section.getStartNodeIndex();
            sx = section.getEndNodeIndex();

            for( int j = (int) dx - 1; j < sx - 1; j++ ) {
                /* Controllo se il tratto compreso fra le stazioni j e j+1 e' bagnato */
                // if (section.getYAt(j) >= tirante[i] && section.getYAt(j + 1) >= tirante[i]) {
                // area_b = area_b + 0;
                // peri_b = peri_b + 0;
                // larghe_b = larghe_b + 0;
                // gau_b = gau_b + 0;
                // alfa_num = alfa_num + 0;
                // alfa_den = alfa_den + 0;
                // }
                /* Controllo se il tratto e' parzialmente bagnato (parte destra asciutta) */

                if (section.getElevationAt(j) >= tirante[i]
                        && section.getElevationAt(j + 1) < tirante[i]) {
                    /* Calcolo l'area del triangolo */
                    base_dx = 0;
                    base_sx = tirante[i] - section.getElevationAt(j + 1);
                    altezza = base_sx
                            * (section.getProgressiveAt(j + 1) - section.getProgressiveAt(j))
                            / (section.getElevationAt(j) - section.getElevationAt(j + 1));
                    area_b = area_b + (base_dx + base_sx) * altezza / 2.0;
                    /* Calcolo il tratto di fondo bagnato */
                    peri_b = peri_b
                            + Math.sqrt(Math.abs(base_dx - base_sx) * Math.abs(base_dx - base_sx)
                                    + altezza * altezza);
                    /* Calcolo la larghezza della superficie libera */
                    larghe_b = larghe_b + altezza;
                    /* Calcolo il coefficiente di scabrezza */
                    gau_b = gau_b
                            + (section.getStricklerCoeffAt(j)
                                    * Math.pow((base_dx + base_sx) / 2.0, (5.0 / 3.0)) * altezza);
                    gau_loc = section.getStricklerCoeffAt(j);
                    /* Calcolo la sommatoria dei termini per il coeff. di Coriolis */
                    area_loc = (base_dx + base_sx) * altezza / 2.0;
                    peri_loc = Math.sqrt((base_dx - base_sx) * (base_dx - base_sx) + altezza
                            * altezza);
                    if (area_loc != 0) {
                        // ??? criterio di Einstein-Horton?
                        alfa_num = alfa_num + (gau_loc * gau_loc) * Math.pow(area_loc, (7.0 / 3.0))
                                / Math.pow(peri_loc, (4.0 / 3.0));
                    }
                    alfa_den = alfa_den + gau_loc * area_loc * Math.pow(peri_loc, (2.0 / 3.0));

                    /* Nessuno di questi valori puo' essere negativo */
                    /*
                     * if ( base_dx<0 || base_sx<0 || altezza<0 ) { printf("\n POSIZIONE %i -
                     * TIRANTE %f \n",i,tirante[i]); t_error(" ERRORE NEL CALCOLO DELL'AREA "); }
                     */
                }
                /* Controllo se il tratto e' parzialmente bagnato (parte sinstra asciutta) */

                if (section.getElevationAt(j + 1) >= tirante[i]
                        && section.getElevationAt(j) < tirante[i]) {
                    /* Calcolo l'area del tringolo */
                    base_sx = 0;
                    base_dx = tirante[i] - section.getElevationAt(j);
                    altezza = base_dx
                            * (section.getProgressiveAt(j + 1) - section.getProgressiveAt(j))
                            / (section.getElevationAt(j + 1) - section.getElevationAt(j));
                    area_b = area_b + (base_dx + base_sx) * altezza / 2.0;
                    /* Calcolo il tratto di fondo bagnato */
                    peri_b = peri_b
                            + Math.sqrt(Math.abs(base_dx - base_sx) * Math.abs(base_dx - base_sx)
                                    + altezza * altezza);
                    /* Calcolo la larghezza della superficie libera */
                    larghe_b = larghe_b + altezza;
                    /* Calcolo il coefficiente di scabrezza */
                    gau_b = gau_b
                            + (section.getStricklerCoeffAt(j)
                                    * Math.pow((base_dx + base_sx) / 2.0, (5.0 / 3.0)) * altezza);
                    gau_loc = section.getStricklerCoeffAt(j);

                    /* Calcolo la sommatoria dei termini [2] per il coeff. di Coriolis */
                    area_loc = (base_dx + base_sx) * altezza / 2.0;
                    peri_loc = Math.sqrt((base_dx - base_sx) * (base_dx - base_sx) + altezza
                            * altezza);
                    if (area_loc != 0) {
                        alfa_num = alfa_num + (gau_loc * gau_loc) * Math.pow(area_loc, (7.0 / 3.0))
                                / Math.pow(peri_loc, (4.0 / 3.0));
                    }
                    alfa_den = alfa_den + gau_loc * area_loc * Math.pow(peri_loc, (2.0 / 3.0));
                    /* Nessuno di questi valori puo' essere negativo */
                    /*
                     * if ( base_dx<0 || base_sx<0 || altezza<0 ) { printf("\n POSIZIONE %i -
                     * TIRANTE %f \n",i,tirante[i]); t_error(" ERRORE NEL CALCOLO DELL'AREA "); }
                     */
                }
                /*
                 * Calcolo l'area bagnata, il fondo bagnato, la larghezza della superficie libera e
                 * il coeff. di scabrezza degli elementi completamente immersi
                 */
                if (section.getElevationAt(j + 1) < tirante[i]
                        && section.getElevationAt(j) < tirante[i]) {
                    base_dx = tirante[i] - section.getElevationAt(j);
                    base_sx = tirante[i] - section.getElevationAt(j + 1);
                    altezza = section.getProgressiveAt(j + 1) - section.getProgressiveAt(j);
                    area_b = area_b + (base_dx + base_sx) * altezza / 2.0;

                    peri_b = peri_b
                            + Math.sqrt(Math.abs(base_dx - base_sx) * Math.abs(base_dx - base_sx)
                                    + altezza * altezza);
                    larghe_b = larghe_b + altezza;
                    gau_b = gau_b
                            + (section.getStricklerCoeffAt(j)
                                    * Math.pow((base_dx + base_sx) / 2, (5.0 / 3.0)) * altezza);
                    gau_loc = section.getStricklerCoeffAt(j);

                    area_loc = (base_dx + base_sx) * altezza / 2;
                    peri_loc = Math.sqrt((base_dx - base_sx) * (base_dx - base_sx) + altezza
                            * altezza);
                    if (area_loc != 0) {
                        alfa_num = alfa_num + (gau_loc * gau_loc) * Math.pow(area_loc, (7.0 / 3.0))
                                / Math.pow(peri_loc, (4.0 / 3.0));
                    } else {
                        alfa_num = alfa_num + 0;
                    }
                    alfa_den = alfa_den + gau_loc * area_loc * Math.pow(peri_loc, (2.0 / 3.0));
                    /* Nessuno di questi valori puo' essere negativo */
                    /*
                     * if ( base_dx<0 || base_sx<0 || altezza<0) { printf("\n POSIZIONE %i -
                     * TIRANTE %f \n",i,tirante[i]); t_error(" ERRORE NEL CALCOLO DELL'AREA "); }
                     */
                }
            }
            /* Area bagnata */
            tirase[i][0] = area_b;
            /* Perimetro bagnato */
            tirase[i][1] = peri_b;
            /* Raggio idraulico */
            tirase[i][2] = area_b / peri_b;
            /* Larghezza della superficie libera */
            tirase[i][3] = larghe_b;
            /* Coefficiente di scabrezza efficace */
            gau_b = gau_b / (area_b * Math.pow((area_b / peri_b), (2.0 / 3.0)));

            tirase[i][4] = gau_b;
            /* Coefficiente alfa di Coriolis */
            tirase[i][5] = alfa_num
                    / (area_b * Math.pow(gau_b, 2) * Math.pow((area_b / peri_b), (4.0 / 3.0)));
            /* tirase[i][6]=1; */
            /* tirase[i][6]=area_b*alfa_num/(alfa_den*alfa_den); */
        }

        return tirase;
    }

    private void new_tirante( List<Section> sez, double[] tirante, double[] Q, double[] U,
            double[] DELXM, int SCELTA_A_MONTE, double qin, double qin_old, int SCELTA_A_VALLE,
            double tiranteout, double[] ql ) {

        double uu;
        double base, C1, C2, Ci, C_old, dx;
        double omegam, zetam;
        double minsez, mindx, umax;
        /* per il calcolo della portata fiorata */
        int ds, sx;
        double T1, T2, A1dx, A2dx, A1sx, A2sx;
        double l, c;
        int imax = sez.size();
        double[][] geomid = new double[imax - 1][6];
        double[] U_I = new double[imax];
        double[] GAM = new double[imax];
        double[] F_Q = new double[imax - 1];
        double[] D = new double[imax];
        double[] DS = new double[imax - 1];
        double[] DI = new double[imax - 1];
        double[] B = new double[imax];
        double[] tirante_old = new double[imax];
        double[] qs = new double[imax - 1];

        // FIXME this variable is never initialized in the C code
        double tirantein = 0;

        /*
         * Esegue il programma area_bagnata; attraverso la variabile idrgeo posso accedere a tutte
         * le grandezze che dipendono de tirante e sezione
         */
        double[][] idrgeo = area_bagnata(tirante, sez);
        /*
         * Calcolo le stesse grandezze nelle sezioni intermedie alle quali accedo attraverso la
         * variabile goemid
         */
        for( int i = 0; i < imax - 1; i++ ) {
            for( int j = 0; j < 6; j++ ) {
                geomid[i][j] = (idrgeo[i][j] + idrgeo[i + 1][j]) / 2.0;
            }
        }
        /*
         * Calcolo la velocita' media nelle sezioni intermedie U[] e nelle sezioni rilevate U_I[].
         * inizio
         */
        U[0] = Q[0] / geomid[0][0];
        for( int i = 1; i < imax - 1; i++ ) {
            U[i] = Q[i] / geomid[i][0];
            U_I[i] = 0.5 * (U[i - 1] + U[i]);
        }
        U_I[0] = 0.5 * (U[0] + qin / (2.0 * idrgeo[0][0] - geomid[0][0]));
        /*
         * Calcolo il coefficiente gamma. inizio
         */
        /*
         * fino a questo simbolo sostituisco la portata sfiorata dall'argine con la portata sfiorata
         * per il problema della griglia
         */
        for( int i = 0; i < imax - 1; i++ ) {
            uu = U[i];
            GAM[i] = G * Math.abs(uu)
                    / (Math.pow(geomid[i][2], 4.0 / 3.0) * Math.pow(geomid[i][4], 2.0));
            GAM[i] = GAM[i] + ql[i] / geomid[i][0];
        }

        /*
         * Verifico che sia rispettata la condizione di Courant. inizio
         */
        /* Cerco il minimo intervallo spaziale e la massima velocita' */
        mindx = DELXM[0];
        umax = Math.abs(U[0]);
        for( int i = 0; i < imax - 2; i++ ) {
            dx = (DELXM[i] + DELXM[i + 1]) / 2.0;
            if (dx <= mindx)
                mindx = dx;
            if (Math.abs(U[i]) >= umax)
                umax = Math.abs(U[i]);
        }
        DELT = 0.1 * mindx / umax;

        // In the C code it was never initialized and uses rubbish!!!
        double qout = Q[imax - 2];
        /*
         * Applico la funzione FQ.
         */
        // if (SCELTA_A_VALLE != 3)
        // qout = Q[imax - 2];
        FQ(F_Q, Q, U_I, U, idrgeo, sez, DELT, qin, qout);

        /*
         * n Calcolo la portata sfiorata. inizio
         */
        for( int i = 0; i < imax - 1; i++ ) {
            Section section_i = sez.get(i);
            Section section_ip = sez.get(i + 1);
            qs[i] = 0;
            T1 = tirante[i];
            T2 = tirante[i + 1];
            ds = section_i.getStartNodeIndex();
            A1dx = section_i.getElevationAt(ds - 1);
            ds = section_ip.getStartNodeIndex();
            A2dx = section_ip.getElevationAt(ds - 1);
            /* calcolo la portata sfiorata a destra */
            if (T1 > A1dx && T2 > A2dx) {
                l = DELXM[i];
                c = (T2 - T1 - A2dx + A1dx) / l;
                qs[i] = (Cq / (2.5 * c))
                        * (Math.pow((T2 - A2dx), (5.0 / 2.0)) - Math.pow((T1 - A1dx), (5.0 / 2.0)));
            } else if (T1 > A1dx && T2 <= A2dx) {
                l = DELXM[i];
                c = (T2 - T1 - A2dx + A1dx) / l;
                qs[i] = (Cq / (2.5 * c)) * (-Math.pow((T1 - A1dx), (5.0 / 2.0)));
            } else if (T1 <= A1dx && T2 > A2dx) {
                l = DELXM[i];
                c = (T2 - T1 - A2dx + A1dx) / l;
                qs[i] = (Cq / (2.5 * c)) * (Math.pow((T2 - A2dx), (5.0 / 2.0)));
            }
            /* definisco le quote del pelo libero e degli argini */
            T1 = tirante[i];
            T2 = tirante[i + 1];
            sx = section_i.getEndNodeIndex();
            A1sx = section_i.getElevationAt(sx - 1);
            sx = section_ip.getEndNodeIndex();
            A2sx = section_ip.getElevationAt(sx - 1);
            /* calcolo la portata sfiorata a sinistra */
            if (T1 > A1sx && T2 > A2sx) {
                l = DELXM[i];
                c = (T2 - T1 - A2sx + A1sx) / l;
                qs[i] = qs[i] + (Cq / (2.5 * c))
                        * (Math.pow((T2 - A2sx), (5.0 / 2.0)) - Math.pow((T1 - A1sx), (5.0 / 2.0)));
            } else if (T1 > A1sx && T2 <= A2sx) {
                l = DELXM[i];
                c = (T2 - T1 - A2sx + A1sx) / l;
                qs[i] = qs[i] + (Cq / (2.5 * c)) * (-Math.pow((T1 - A1sx), (5.0 / 2.0)));
            } else if (T1 <= A1sx && T2 > A2sx) {
                l = DELXM[i];
                c = (T2 - T1 - A2sx + A1sx) / l;
                qs[i] = qs[i] + (Cq / (2.5 * c)) * (Math.pow((T2 - A2sx), (5.0 / 2.0)));
            }
        }

        /*******************************************************************************************
         * Definisco i coefficienti della matrice e il termine noto a seconda delle condizioni di
         * monte e di valle assegnate.
         * ******************************************************************************************
         * PRIMO CASO: CONDIZIONE DI MONTE 1 CONDIZIONE DI VALLE 1
         ******************************************************************************************/
        if (SCELTA_A_MONTE == 1 && SCELTA_A_VALLE == 1) {
            tirante[imax - 1] = tiranteout;
            /* definisco i coefficienti della prima riga */
            dx = DELXM[0];
            base = (idrgeo[0][3] + geomid[0][3]) / 2.0;
            C1 = (G * DELT * geomid[0][0]) / (DELXM[0] * (1.0 + DELT * GAM[0]));
            DS[0] = -C1 / dx;
            D[0] = C1 / dx + base / DELT;
            B[0] = (base / DELT) * tirante[0] - F_Q[0] / (dx * (1.0 + DELT * GAM[0])) + qin / dx
                    + ql[imax - 2] - qs[imax - 2];
            /* definisco i coefficienti dalla seconda alla penultima riga */
            dx = (DELXM[1] + DELXM[0]) / 2.0;
            Ci = C1;
            for( int i = 1; i < imax - 2; i++ ) {
                dx = (DELXM[i] + DELXM[i - 1]) / 2.0;
                base = (geomid[i - 1][3] + geomid[i][3]) / 2.0;
                C_old = Ci;
                Ci = (G * DELT * geomid[i][0]) / (DELXM[i] * (1.0 + DELT * GAM[i]));
                D[i] = Ci / dx + C_old / dx + (base / DELT);
                DI[i - 1] = -C_old / dx;
                DS[i] = -Ci / dx;
                B[i] = (base / DELT) * tirante[i] - F_Q[i] / (dx * (1.0 + DELT * GAM[i]))
                        + F_Q[i - 1] / (dx * (1.0 + DELT * GAM[i - 1])) + ql[i] - qs[i];
            }
            /* definisco i coefficienti dell' ultima riga */
            // FIXME ho lasciato imax -2 e imax -1, ma e' giusto?
            dx = (DELXM[imax - 3] + DELXM[imax - 2]) / 2.0;
            base = (geomid[imax - 3][3] + geomid[imax - 2][3]) / 2.0;
            C_old = (G * DELT * geomid[imax - 3][0])
                    / (DELXM[imax - 3] * (1.0 + DELT * GAM[imax - 3]));
            Ci = (G * DELT * geomid[imax - 2][0])
                    / (DELXM[imax - 2] * (1.0 + DELT * GAM[imax - 2]));
            DI[imax - 3] = -C_old / dx;
            DS[imax - 2] = 0;
            D[imax - 2] = C_old / dx + Ci / dx + base / DELT;
            B[imax - 2] = base / DELT * tirante[imax - 2] + Ci / dx * tiranteout - F_Q[imax - 2]
                    / (dx * (1.0 + DELT * GAM[imax - 2])) + F_Q[imax - 3]
                    / (dx * (1.0 + DELT * GAM[imax - 3])) + ql[imax - 2] - qs[imax - 2];
            /* Memorizzo i valori del tirante al tempo n nel vettore tirante_old[] */
            for( int i = 0; i < imax; i++ )
                tirante_old[i] = tirante[i];
            /*
             * Chiamo la funzione ris_sistema. Da questo punto in poi nel vettore tirante[] sono
             * memorizzati i valori del tirante al tempo n+1
             */
            linearAlgebra.ris_sistema(D, DS, DI, B, tirante, imax - 1);

            /*
             * Controllo sul tirante: se durante le iterazioni la quota del tirante scende al di
             * sotto della quota minima della sezione impongo un'altezza minima di default
             */
            for( int i = 0; i < imax; i++ ) {
                minsez = sez.get(i).getMinElevation();
                if (minsez >= tirante[i])
                    tirante[i] = minsez + h_DEF;
            }
            tirante[imax - 1] = tiranteout;
            /* Calcolo le portate e le velocita' al tempo n+1. */
            for( int i = 0; i < imax - 1; i++ ) {
                Q[i] = F_Q[i] / (1.0 + (DELT * GAM[i])) - (G * DELT * geomid[i][0])
                        / (DELXM[i] * (1.0 + DELT * GAM[i])) * (tirante[i + 1] - tirante[i]);
            }
            dx = (DELXM[imax - 2] + DELXM[imax - 3]) / 2.0;
            base = (geomid[imax - 3][3] + geomid[imax - 2][3]) / 2.0;
            Q[imax - 2] = Q[imax - 3];
        }

        /*******************************************************************************************
         * SECONDO CASO: CONDIZIONE DI MONTE 1 CONDIZIONE DI VALLE 2
         ******************************************************************************************/
        if (SCELTA_A_MONTE == 1 && SCELTA_A_VALLE == 2) {
            /* definisco i coefficienti della prima riga */
            dx = DELXM[0];
            base = (idrgeo[0][3] + geomid[0][3]) / 2.0;
            C1 = (G * DELT * geomid[0][0]) / (DELXM[0] * (1.0 + DELT * GAM[0]));
            DS[0] = -C1 / dx;
            D[0] = C1 / dx + base / DELT;
            B[0] = (base / DELT) * tirante[0] - F_Q[0] / (dx * (1.0 + DELT * GAM[0])) + qin / dx
                    + ql[0] - qs[0];
            /* definisco i coefficienti dalla seconda alla penultima riga */
            dx = (DELXM[1] + DELXM[0]) / 2.0;
            Ci = C1;
            for( int i = 1; i < imax - 1; i++ ) {
                dx = (DELXM[i] + DELXM[i - 1]) / 2.0;
                base = (geomid[i - 1][3] + geomid[i][3]) / 2.0;
                C_old = Ci;
                Ci = (G * DELT * geomid[i][0]) / (DELXM[i] * (1.0 + DELT * GAM[i]));
                D[i] = Ci / dx + C_old / dx + (base / DELT);
                DI[i - 1] = -C_old / dx;
                DS[i] = -Ci / dx;
                B[i] = (base / DELT) * tirante[i] - F_Q[i] / (dx * (1.0 + DELT * GAM[i]))
                        + F_Q[i - 1] / (dx * (1.0 + DELT * GAM[i - 1])) + ql[i] - qs[i];
            }
            /* definisco i coefficienti dell'ultima riga */
            dx = DELXM[imax - 2];
            base = (geomid[imax - 2][3] + idrgeo[imax - 1][3]) / 2.0;
            // FIXME what is zetam????
            // zetam = sez.get(imax - 1).getElevationAt(1);
            zetam = tirante[imax - 1] - ((idrgeo[imax - 1][0] + geomid[imax - 2][0]) / 2.0) / base;

            omegam = base * Math.sqrt(G * (tirante[imax - 1] - zetam));
            C_old = (G * DELT * geomid[imax - 2][0])
                    / (DELXM[imax - 2] * (1.0 + DELT * GAM[imax - 2]));
            DI[imax - 2] = -C_old / dx;
            D[imax - 1] = base / DELT + C_old / dx + omegam / dx;
            B[imax - 1] = base / DELT * tirante[imax - 1] + F_Q[imax - 2]
                    / (dx * (1.0 + DELT * GAM[imax - 2])) + omegam * zetam / dx + ql[imax - 2]
                    - qs[imax - 2];
            /* Memorizzo i valori del tirante al tempo n nel vettore tirante_old[] */
            for( int i = 0; i < imax; i++ )
                tirante_old[i] = tirante[i];
            /*
             * Chiamo la funzione ris_sistema. Da questo punto in poi nel vettore tirante[] sono
             * memorizzati i valori del tirante al tempo n+1
             */
            linearAlgebra.ris_sistema(D, DS, DI, B, tirante, imax);
            /* Calcolo le portate e le velocita' al tempo n+1. inizio */
            for( int i = 0; i < imax - 1; i++ ) {
                Q[i] = F_Q[i] / (1.0 + (DELT * GAM[i])) - (G * DELT * geomid[i][0])
                        / (DELXM[i] * (1.0 + DELT * GAM[i])) * (tirante[i + 1] - tirante[i]);
            }
            /*
             * Controllo sul tirante: se durante le iterazioni la quota del tirante scende al di
             * sotto della quota minima della sezione impongo un'altezza minima di default
             */
            for( int i = 0; i < imax; i++ ) {
                minsez = sez.get(i).getMinElevation();
                if (minsez >= tirante[i])
                    tirante[i] = minsez + h_DEF;
            }
        }

        /*******************************************************************************************
         * TERZO CASO: CONDIZIONE DI MONTE 1 CONDIZIONE DI VALLE 3
         ******************************************************************************************/
        if (SCELTA_A_MONTE == 1 && SCELTA_A_VALLE == 3) {
            /* definisco i coefficienti della prima riga */
            dx = DELXM[0];
            base = (idrgeo[0][3] + geomid[0][3]) / 2.0;
            C1 = (G * DELT * geomid[0][0]) / (DELXM[0] * (1.0 + DELT * GAM[0]));
            DS[0] = -C1 / dx;
            D[0] = C1 / dx + base / DELT;
            B[0] = (base / DELT) * tirante[0] - F_Q[0] / (dx * (1.0 + DELT * GAM[0])) + qin / dx
                    + ql[0] - qs[0];
            /* definisco i coefficienti dalla seconda alla penultima riga */
            dx = (DELXM[1] + DELXM[0]) / 2.0;
            Ci = C1;
            for( int i = 1; i < imax - 1; i++ ) {
                dx = (DELXM[i] + DELXM[i - 1]) / 2.0;
                base = (geomid[i - 1][3] + geomid[i][3]) / 2.0;
                C_old = Ci;
                Ci = (G * DELT * geomid[i][0]) / (DELXM[i] * (1.0 + DELT * GAM[i]));
                D[i] = Ci / dx + C_old / dx + (base / DELT);
                DI[i - 1] = -C_old / dx;
                DS[i] = -Ci / dx;
                B[i] = (base / DELT) * tirante[i] - F_Q[i] / (dx * (1.0 + DELT * GAM[i]))
                        + F_Q[i - 1] / (dx * (1 + DELT * GAM[i - 1])) + ql[i] - qs[i];
            }
            /* definisco i coefficienti dell'ultima riga, */
            dx = DELXM[imax - 2];
            base = (geomid[imax - 2][3] + idrgeo[imax - 1][3]) / 2.0;
            C_old = (G * DELT * geomid[imax - 2][0])
                    / (DELXM[imax - 2] * (1.0 + DELT * GAM[imax - 2]));
            DI[imax - 2] = -C_old / dx;
            D[imax - 1] = base / DELT + C_old / dx;
            B[imax - 1] = base / DELT * tirante[imax - 1] - Q[imax - 2] / DELXM[imax - 2]
                    + F_Q[imax - 2] / (dx * (1.0 + DELT * GAM[imax - 2])) + ql[imax - 2]
                    - qs[imax - 2];
            /* Memorizzo i valori del tirante al tempo n nel vettore tirante_old[] */
            for( int i = 0; i < imax; i++ )
                tirante_old[i] = tirante[i];
            /*
             * Chiamo la funzione ris_sistema. Da questo punto in poi nel vettore tirante[] sono
             * memorizzati i valori del tirante al tempo n+1
             */
            // FIXME check the last parameter in all ris_sistema calls, I guess it needs to be one
            // less
            linearAlgebra.ris_sistema(D, DS, DI, B, tirante, imax - 1);
            /* Calcolo le portate e le velocita' al tempo n+1. inizio */
            Q[0] = qin;
            for( int i = 1; i < imax - 2; i++ ) {
                Q[i] = F_Q[i] / (1.0 + (DELT * GAM[i])) - (G * DELT * geomid[i][0])
                        / (DELXM[i] * (1.0 + DELT * GAM[i])) * (tirante[i + 1] - tirante[i]);
            }
            Q[imax - 2] = Q[imax - 3];
            /*
             * Controllo sul tirante: se durante le iterazioni la quota del tirante scende al di
             * sotto della quota minima della sezione impongo un'altezza minima di default
             */
            for( int i = 0; i < imax; i++ ) {
                minsez = sez.get(i).getMinElevation();
                if (minsez >= tirante[i]) {
                    tirante[i] = minsez + h_DEF;
                }
                if (i == imax - 1) {
                    tirante[imax - 1] = minsez + geomid[imax - 2][0] / geomid[imax - 2][3];
                }
            }
        }

        /*******************************************************************************************
         * QUARTO CASO: CONDIZIONE DI MONTE 2 CONDIZIONE DI VALLE 1
         ******************************************************************************************/
        if (SCELTA_A_MONTE == 2 && SCELTA_A_VALLE == 1) {
            /* definisco i coefficienti della prima riga */
            C1 = (G * DELT * geomid[0][0]) / (2.0 * DELXM[0] * DELXM[0] * (1.0 + DELT * GAM[0]));
            DS[0] = -C1;
            D[0] = 0;
            B[0] = -(idrgeo[0][3] / DELT + C1) * tirantein + (idrgeo[0][3] / DELT - C1)
                    * tirante[0] + C1 * tirante[1] - F_Q[0] / (DELXM[0] * (1.0 + DELT * GAM[0]))
                    + (qin - Q[0] + qin_old) / DELXM[0];
            /* definisco i coefficienti della seconda riga */
            dx = (DELXM[0] + DELXM[1]) / 2.0;
            C1 = (G * DELT * geomid[0][0]) / (4.0 * dx * DELXM[0] * (1.0 + DELT * GAM[0]));
            C2 = (G * DELT * geomid[1][0]) / (4.0 * dx * DELXM[1] * (1.0 + DELT * GAM[1]));
            D[1] = (idrgeo[1][3] / DELT + C1 + C2);
            DS[1] = -C2;
            DI[0] = 0;
            B[1] = (idrgeo[1][3] / DELT - C1 - C2) * tirante[1] + C2 * tirante[2] + C1
                    * (tirantein + tirante[0]) - F_Q[1] / (2.0 * dx * (1.0 + DELT * GAM[1]))
                    + F_Q[0] / (2.0 * dx * (1.0 + DELT * GAM[0])) - (Q[1] - Q[0]) / (2.0 * dx);
            /* definisco i coefficienti dalla terza alla terz'ultima riga */
            dx = (DELXM[1] + DELXM[0]) / 2.0;
            Ci = (G * DELT * geomid[1][0]) / (4.0 * dx * DELXM[1] * (1.0 + DELT * GAM[1]));
            for( int i = 2; i < imax - 2; i++ ) {
                dx = (DELXM[i] + DELXM[i - 1]) / 2.0;
                C_old = Ci;
                Ci = (G * DELT * geomid[i][0]) / (4.0 * dx * DELXM[i] * (1.0 + DELT * GAM[i]));
                D[i] = Ci + C_old + (idrgeo[i][3] / DELT);
                DI[i - 1] = -C_old;
                DS[i] = -Ci;
                B[i] = (idrgeo[i][3] / DELT - Ci - C_old) * tirante[i] + Ci * tirante[i + 1]
                        + C_old * tirante[i - 1] - F_Q[i] / (2.0 * dx * (1.0 + DELT * GAM[i]))
                        + F_Q[i - 1] / (2.0 * dx * (1.0 + DELT * GAM[i - 1])) - (Q[i] - Q[i - 1])
                        / (2.0 * dx);
            }
            /* definisco i coefficienti della penultima riga */
            dx = (DELXM[imax - 3] + DELXM[imax - 2]) / 2.0;
            C_old = (G * DELT * geomid[imax - 3][0])
                    / (4.0 * dx * DELXM[imax - 3] * (1.0 + DELT * GAM[imax - 3]));
            Ci = (G * DELT * geomid[imax - 2][0])
                    / (4.0 * dx * DELXM[imax - 2] * (1.0 + DELT * GAM[imax - 2]));
            DI[imax - 3] = -C_old;
            DS[imax - 2] = 0;
            D[imax - 2] = C_old + Ci + (idrgeo[imax - 2][3]) / DELT;
            B[imax - 2] = (-C_old - Ci + (idrgeo[imax - 2][3]) / DELT) * tirante[imax - 2] + Ci
                    * (tiranteout + tirante[imax - 1]) + C_old * tirante[imax - 3] - F_Q[imax - 2]
                    / (2.0 * dx * (1.0 + DELT * GAM[imax - 2])) + F_Q[imax - 3]
                    / (2.0 * dx * (1.0 + DELT * GAM[imax - 3])) - (Q[imax - 2] - Q[imax - 3])
                    / (2.0 * dx);
            /* definisco i coefficienti dell'ultima riga */
            dx = DELXM[imax - 2];
            C_old = (G * DELT * geomid[imax - 2][0])
                    / (2.0 * dx * dx * (1.0 + DELT * GAM[imax - 2]));
            DI[imax - 2] = -C_old;
            D[imax - 1] = 1.0 / dx;
            B[imax - 1] = -C_old * (tiranteout + tirante[imax - 1] - tirante[imax - 2])
                    - (idrgeo[imax - 1][3] / DELT) * (tiranteout - tirante[imax - 1])
                    + F_Q[imax - 2] / (dx * (1.0 + DELT * GAM[imax - 2])) - (qout - Q[imax - 2])
                    / dx;
        }

        /*******************************************************************************************
         * QUINTO CASO: CONDIZIONE DI MONTE 2 CONDIZIONE DI VALLE 2
         ******************************************************************************************/
        if (SCELTA_A_MONTE == 2 && SCELTA_A_VALLE == 2) {
            /* definisco i coefficienti della prima riga */
            C1 = (G * DELT * geomid[0][0]) / (2.0 * DELXM[0] * DELXM[0] * (1.0 + DELT * GAM[0]));
            DS[0] = -C1;
            D[0] = 0;
            B[0] = -(idrgeo[0][3] / DELT + C1) * tirantein + (idrgeo[0][3] / DELT - C1)
                    * tirante[0] + C1 * tirante[1] - F_Q[0] / (DELXM[0] * (1.0 + DELT * GAM[0]))
                    + (qin - Q[0] + qin_old) / DELXM[0];
            /* definisco i coefficienti della seconda riga */
            dx = (DELXM[0] + DELXM[1]) / 2.0;
            C1 = (G * DELT * geomid[0][0]) / (4.0 * dx * DELXM[0] * (1.0 + DELT * GAM[0]));
            C2 = (G * DELT * geomid[1][0]) / (4.0 * dx * DELXM[1] * (1.0 + DELT * GAM[1]));
            D[1] = (idrgeo[1][3] / DELT + C1 + C2);
            DS[1] = -C2;
            DI[0] = 0;
            B[1] = (idrgeo[1][3] / DELT - C1 - C2) * tirante[1] + C2 * tirante[2] + C1
                    * (tirantein + tirante[0]) - F_Q[1] / (2.0 * dx * (1.0 + DELT * GAM[1]))
                    + F_Q[0] / (2.0 * dx * (1.0 + DELT * GAM[0])) - (Q[1] - Q[0]) / (2.0 * dx);
            /* definisco i coefficienti della terza alla penultima riga */
            dx = (DELXM[1] + DELXM[0]) / 2.0;
            Ci = (G * DELT * geomid[0][0]) / (4.0 * dx * DELXM[0] * (1.0 + DELT * GAM[0]));
            for( int i = 1; i < imax - 1; i++ ) {
                dx = (DELXM[i] + DELXM[i - 1]) / 2.0;
                C_old = Ci;
                Ci = (G * DELT * geomid[i][0]) / (4.0 * dx * DELXM[i] * (1.0 + DELT * GAM[i]));
                D[i] = Ci + C_old + (idrgeo[i][3] / DELT);
                DI[i - 1] = -C_old;
                DS[i] = -Ci;
                B[i] = (idrgeo[i][3] / DELT - Ci - C_old) * tirante[i] + Ci * tirante[i + 1]
                        + C_old * tirante[i - 1] - F_Q[i] / (2.0 * dx * (1 + DELT * GAM[i]))
                        + F_Q[i - 1] / (2.0 * dx * (1.0 + DELT * GAM[i - 1])) - (Q[i] - Q[i - 1])
                        / (2.0 * dx);
            }
            /* definisco i coefficienti dell'ultima riga */
            omegam = Math.sqrt(G * idrgeo[imax - 1][0] * idrgeo[imax - 1][3]);
            zetam = tirante[imax - 1] - idrgeo[imax - 1][0] / idrgeo[imax - 1][3];
            dx = DELXM[imax - 2];
            C_old = (G * DELT * geomid[imax - 2][0])
                    / (2.0 * dx * dx * (1.0 + DELT * GAM[imax - 2]));
            DI[imax - 2] = -C_old;
            D[imax - 1] = idrgeo[imax - 1][3] / DELT + C_old + omegam;
            B[imax - 1] = (idrgeo[imax - 1][3] / DELT - C_old) * tirante[imax - 1] + C_old
                    * tirante[imax - 2] + F_Q[imax - 2] / (dx * (1.0 + DELT * GAM[imax - 2]))
                    + (-qout + Q[imax - 2] + omegam * zetam) / dx;
        }

        /*******************************************************************************************
         * SESTO CASO: CONDIZIONE DI MONTE 2 CONDIZIONE DI VALLE 3
         ******************************************************************************************/
        if (SCELTA_A_MONTE == 2 && SCELTA_A_VALLE == 3) {
            /* definisco i coefficienti della prima riga */
            C1 = (G * DELT * geomid[0][0]) / (2.0 * DELXM[0] * DELXM[0] * (1.0 + DELT * GAM[0]));
            DS[0] = -C1;
            D[0] = 0;
            B[0] = -(idrgeo[0][3] / DELT + C1) * tirantein + (idrgeo[0][3] / DELT - C1)
                    * tirante[0] + C1 * tirante[1] - F_Q[0] / (DELXM[0] * (1.0 + DELT * GAM[0]))
                    + (qin - Q[0] + qin_old) / DELXM[0];
            /* definisco i coefficienti della seconda riga */
            dx = (DELXM[0] + DELXM[1]) / 2.0;
            C1 = (G * DELT * geomid[0][0]) / (4.0 * dx * DELXM[0] * (1.0 + DELT * GAM[0]));
            C2 = (G * DELT * geomid[1][0]) / (4.0 * dx * DELXM[1] * (1.0 + DELT * GAM[1]));
            D[1] = (idrgeo[1][3] / DELT + C1 + C2);
            DS[1] = -C2;
            DI[0] = 0;
            B[1] = (idrgeo[1][3] / DELT - C1 - C2) * tirante[1] + C2 * tirante[2] + C1
                    * (tirantein + tirante[0]) - F_Q[1] / (2.0 * dx * (1.0 + DELT * GAM[1]))
                    + F_Q[0] / (2.0 * dx * (1.0 + DELT * GAM[0])) - (Q[1] - Q[0]) / (2.0 * dx);
            /* definisco i coefficienti dalla terza alla penultima riga */
            dx = (DELXM[1] + DELXM[0]) / 2.0;
            Ci = (G * DELT * geomid[0][0]) / (4.0 * dx * DELXM[0] * (1.0 + DELT * GAM[0]));
            for( int i = 1; i < imax - 1; i++ ) {
                dx = (DELXM[i] + DELXM[i - 1]) / 2.0;
                C_old = Ci;
                Ci = (G * DELT * geomid[i][0]) / (4.0 * dx * DELXM[i] * (1.0 + DELT * GAM[i]));
                D[i] = Ci + C_old + (idrgeo[i][3] / DELT);
                DI[i - 1] = -C_old;
                DS[i] = -Ci;
                B[i] = (idrgeo[i][3] / DELT - Ci - C_old) * tirante[i] + Ci * tirante[i + 1]
                        + C_old * tirante[i - 1] - F_Q[i] / (2.0 * dx * (1.0 + DELT * GAM[i]))
                        + F_Q[i - 1] / (2.0 * dx * (1.0 + DELT * GAM[i - 1])) - (Q[i] - Q[i - 1])
                        / (2.0 * dx);
            }
            /* definisco i coefficienti dell'ultima riga, */
            dx = DELXM[imax - 2];
            base = (geomid[imax - 2][3] + idrgeo[imax - 1][3]) / 2.0;
            C_old = (G * DELT * geomid[imax - 2][0])
                    / (DELXM[imax - 2] * (1.0 + DELT * GAM[imax - 2]));
            DI[imax - 2] = -C_old / dx;
            D[imax - 1] = base / DELT + C_old / dx;
            B[imax - 1] = base / DELT * tirante[imax - 1] + F_Q[imax - 2]
                    / (dx * (1.0 + DELT * GAM[imax - 2])) - Q[imax - 2] / DELXM[imax - 2]
                    + ql[imax - 2] - qs[imax - 2];
            /* Memorizzo i valori del tirante al tempo n nel vettore tirante_old[] */
            for( int i = 0; i < imax; i++ )
                tirante_old[i] = tirante[i];
            /*
             * Chiamo la funzione ris_sistema. Da questo punto in poi nel vettore tirante[] sono
             * memorizzati i valori del tirante al tempo n+1
             */
            linearAlgebra.ris_sistema(D, DS, DI, B, tirante, imax);
            /* Calcolo le portate e le velocita' al tempo n+1. inizio */
            /* Q[1]=qin; */
            for( int i = 0; i < imax - 1; i++ ) {
                Q[i] = F_Q[i] / (1.0 + (DELT * GAM[i])) - (G * DELT * geomid[i][0])
                        / (DELXM[i] * (1.0 + DELT * GAM[i])) * (tirante[i + 1] - tirante[i]);
            }
            /*
             * Controllo sul tirante: se durante le iterazioni la quota del tirante scende al di
             * sotto della quota minima della sezione impongo un'altezza minima di default
             */
            for( int i = 0; i < imax; i++ ) {
                minsez = sez.get(i).getMinElevation();
                if (minsez >= tirante[i]) {
                    tirante[i] = minsez + h_DEF;
                }
                if (i == imax - 1) {
                    tirante[imax - 1] = minsez + geomid[imax - 2][0] / geomid[imax - 2][3];
                }
            }
        }
    }
    /**
     * <pre>
     *  Questa funzione calcola il valore FQ al tempo n e nella posizione i+1/2;
     *     F e' l'operatore alle differenze nel caso di discretizzazione upwind.
     *     La funzione restituisce un vettore di tipo double i cui elementi valgono FQ[i+1/2].
     * </pre>
     * 
     * @param F_Q
     * @param Q puntatore al vettore che contiene i valori di portata al tempo n;
     * @param U_I puntatore al vettore che contiene le velocita' medie calcolate nelle sezioni
     *        rilevate;
     * @param U
     * @param idrgeo puntatore alla matrice che contiene i valori di area bagnata, perimetro
     *        bagnato, raggio idraulico ... per le sezioni i, i+1 ... al tempo n;
     * @param sez puntatore alla struttura che contiene le grandezze relative alle sezioni di
     *        calcolo;
     * @param delta_T un reale che specifica il passo temporale; - le portate entranti e uscenti.
     * @param qin
     * @param qout
     */
    private void FQ( double[] F_Q, double[] Q, double[] U_I, double[] U, double[][] idrgeo,
            List<Section> sez, double delta_T, double qin, double qout ) {
        double coeff;
        double alfa, alfa_i, alfa_ii; /* Coeff. di Coriolis nelle sezioni i e i+1 */
        double u, u_i, u_ii; /* Velocita' media nelle sezioni i e i+1 */
        double q, q_i, q_ii; /* Portata nelle sezioni i-0.5, i+0.5, i+1.5 */
        /* Primo elemento di FQ */
        /* Definsco le velocita' e i coefficienti di Coriolis */
        u_i = U[0];
        q_i = Q[0];
        alfa_i = 1;
        u_ii = U[1];
        q_ii = Q[1];
        alfa_ii = 1;
        q = qin;
        u = qin / idrgeo[0][0];
        alfa = 1;
        if ((u_i) >= 0) {
            coeff = delta_T
                    / ((sez.get(1).getProgressiveAlongReach() - sez.get(0)
                            .getProgressiveAlongReach()));
            F_Q[0] = q_i - coeff * (alfa_i * u_i * q_i - alfa * u * q);
        } else {
            coeff = 2
                    * delta_T
                    / ((sez.get(2).getProgressiveAlongReach() - sez.get(0)
                            .getProgressiveAlongReach()));
            F_Q[0] = q_i - coeff * (alfa_ii * u_ii * q_ii - alfa_i * u_i * q_i);
        }
        int imax = sez.size();
        /* Elementi intermedi */
        for( int i = 1; i < imax - 2; i++ ) {
            /* Definsco le velocita' e i coefficienti di Coriolis */
            u_i = U[i];
            q_i = Q[i];
            alfa_i = 1;
            u_ii = U[i + 1];
            q_ii = Q[i + 1];
            alfa_ii = 1;
            u = U[i - 1];
            q = Q[i - 1];
            alfa = 1;
            if ((u_i) >= 0) {
                coeff = 2
                        * delta_T
                        / ((sez.get(i + 1).getProgressiveAlongReach() - sez.get(i - 1)
                                .getProgressiveAlongReach()));
                F_Q[i] = q_i - coeff * (alfa_i * u_i * q_i - alfa * u * q);
            } else {
                coeff = 2
                        * delta_T
                        / ((sez.get(i + 2).getProgressiveAlongReach() - sez.get(i)
                                .getProgressiveAlongReach()));
                F_Q[i] = q_i - coeff * (alfa_ii * u_ii * q_ii - alfa_i * u_i * q_i);
            }
        }
        /* Ultimo elemento */
        /* Definsco le velocita' e i coefficienti di Coriolis */
        u_i = U[imax - 2];
        q_i = Q[imax - 2];
        alfa_i = 1;
        u_ii = qout / idrgeo[imax - 1][0];
        q_ii = qout;
        alfa_ii = 1;
        coeff = delta_T
                / ((sez.get(imax - 1).getProgressiveAlongReach() - sez.get(imax - 2)
                        .getProgressiveAlongReach()));
        u = U[imax - 3];
        q = Q[imax - 3];
        alfa = 1;
        if ((u_i) >= 0) {
            coeff = 2
                    * delta_T
                    / ((sez.get(imax - 1).getProgressiveAlongReach() - sez.get(imax - 3)
                            .getProgressiveAlongReach()));
            F_Q[imax - 2] = q_i - coeff * (alfa_i * u_i * q_i - alfa * u * q);
        } else {
            coeff = delta_T
                    / ((sez.get(imax - 1).getProgressiveAlongReach() - sez.get(imax - 2)
                            .getProgressiveAlongReach()));
            F_Q[imax - 2] = q_i - coeff * (alfa_ii * u_ii * q_ii - alfa_i * u_i * q_i);
        }
    }

}
