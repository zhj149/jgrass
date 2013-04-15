/* ***************************************************************************
 *
 *    Copyright (C) 2006 OpenMI Association
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *    or look at URL www.gnu.org/licenses/lgpl.html
 *
 *    Contact info:
 *      URL: www.openmi.org
 *      Email: sourcecode@openmi.org
 *      Discussion forum available at www.sourceforge.net
 *
 *      Coordinator: Roger Moore, CEH Wallingford, Wallingford, Oxon, UK
 *
 *****************************************************************************
 *
 * The classes in the utilities package are mostly a direct translation from
 * the C# version. They successfully pass the unit tests (which were also
 * taken from the C# version), but so far no extensive time as been put into
 * them.
 *
 * @author Rob Knapen, Alterra B.V., The Netherlands
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.buffer;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import nl.alterra.openmi.sdk.backbone.*;
import org.openmi.standard.*;

/**
 * The SmartBuffer class provides bufferig functionality that will store
 * values needed for a particular link in memory and functionality that
 * will interpolate, extrapolate and aggregate values from these values.
 * 
 * The content of the SmartBuffer is lists of corresponding times and ValueSets,
 * where times can be TimeStamps or TimeSpans and the ValueSets can be
 * ScalarSets or VectorSets. Or in other words the content of the SmartBuffer is
 * corresponding ScalarSets and TimeStamps, or ScalarSets and TimeSpans, or
 * VectorSets and TimeStamps, or VectorSets and TimeSpans. This data structure
 * is shown on figure 4.1 above SmartBuffer objects may not contain mixtures of
 * TimeSpans and TimeStamps and may not contain mixtures of ScalarSets and
 * VectorSets. The number of Times (TimeSpans or TimeStamps) must equal the
 * number of ValueSets ( ScalarSets or VectorSets) in the SmartBuffer.
 */
@SuppressWarnings({"OverlyLongMethod"})
public class SmartBuffer implements Serializable {

    ArrayList times;
    ArrayList values;
    double relaxationFactor; // Used for the extrapolation algorithm see also
    // RelaxationFactor property

    boolean doExtendedDataVerification;

    // @@@@@@@ CONSTRUCTORS @@@@@@@


    /**
     * default constructor
     */
    public SmartBuffer() {
        create();
    }


    /**
     * Copy constructor
     *
     * @param smartBuffer the Buffered data to copy
     */
    public SmartBuffer(SmartBuffer smartBuffer) {
        create();

        try {
            if (smartBuffer.getTimesCount() > 0) {
                if (smartBuffer.getTimeAt(0) instanceof ITimeStamp && smartBuffer.getValuesAt(0) instanceof IScalarSet) {
                    for (int i = 0; i < smartBuffer.getTimesCount(); i++) {
                        addValues(new TimeStamp((ITimeStamp) smartBuffer.getTimeAt(i)), new ScalarSet((IScalarSet) smartBuffer.getValuesAt(i)));
                    }
                }

                if (smartBuffer.getTimeAt(0) instanceof ITimeStamp && smartBuffer.getValuesAt(0) instanceof IVectorSet) {
                    for (int i = 0; i < smartBuffer.getTimesCount(); i++) {
                        addValues(new TimeStamp((ITimeStamp) smartBuffer.getTimeAt(i)), new VectorSet((IVectorSet) smartBuffer.getValuesAt(i)));
                    }
                }

                if (smartBuffer.getTimeAt(0) instanceof ITimeSpan && smartBuffer.getValuesAt(0) instanceof IScalarSet) {
                    for (int i = 0; i < smartBuffer.getTimesCount(); i++) {
                        addValues(new TimeSpan((ITimeSpan) smartBuffer.getTimeAt(i)),
                                new ScalarSet((IScalarSet) smartBuffer.getValuesAt(i)));
                    }
                }

                if (smartBuffer.getTimeAt(0) instanceof ITimeSpan && smartBuffer.getValuesAt(0) instanceof IVectorSet) {
                    for (int i = 0; i < smartBuffer.getTimesCount(); i++) {
                        addValues(new TimeSpan((ITimeSpan) smartBuffer.getTimeAt(i)),
                                new VectorSet((IVectorSet) smartBuffer.getValuesAt(i)));
                    }
                }

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // @@@@@@@ GETTERS AND SETTERS @@@@@@@


    /**
     * GETTER for number of times
     *
     * @return time size
     */
    public int getTimesCount() {
        return times.size();
    }


    /**
     * GETTER for number of values
     *
     * @return number of values
     */
    public int getValuesCount() {
        return ((IValueSet) values.get(0)).getCount();
    }


    /**
     * GETTER for Relaxation Factor
     *
     * @return Relaxation Factor
     */
    public double getRelaxationFactor() {
        return relaxationFactor;
    }


    /**
     * SETTER for Relaxation Factor
     *
     * @param value the relaxation factor to set
     * @throws Exception
     */
    public void setRelaxationFactor(double value) throws Exception {
        relaxationFactor = value;
        if (relaxationFactor < 0 || relaxationFactor > 1) {
            throw new Exception("ReleaxationFactor is OutOfMemoryException of range");
        }
    }


    /**
     * GETTER for Read/Write property flag that indicates wheather or not to perform extended data checking.
     *
     * @return the boolean flag for data verification
     */
    public boolean getDoExtendedDataVerification() {
        return doExtendedDataVerification;
    }


    /**
     * SETTER for Read/Write property flag that indicates wheather or not to perform extended data checking.
     *
     * @param value boolean for data verification
     */
    public void setDoExtendedDataVerification(boolean value) {
        doExtendedDataVerification = value;
    }

    // @@@@@@@ OTHERS @@@@@@@


    /**
     * private ...
     */
    private void create() {
        times = new ArrayList();
        values = new ArrayList();
        doExtendedDataVerification = true;
        relaxationFactor = 1.0;
    }


    /**
     * Add corresponding values for time and values to the SmartBuffer.
     * Remarks :
     * The AddValues method will internally make a copy of the added times and
     * values. The reason for doing this is that the times and values
     * arguments are references, and the correspondign values could be
     * changed by the owner of the classes
     *
     * @param time     Description of the time parameter
     * @param valueSet Description of the values parameter
     */
    public void addValues(ITime time, IValueSet valueSet) {

        if (time instanceof ITimeStamp) {
            times.add(new TimeStamp(((ITimeStamp) time).getModifiedJulianDay()));
        } else if (time instanceof ITimeSpan) {
            TimeStamp newStartTime = new TimeStamp(((ITimeSpan) time).getStart().getModifiedJulianDay());
            TimeStamp newEndTime = new TimeStamp(((ITimeSpan) time).getEnd().getModifiedJulianDay());

            TimeSpan newTimeSpan = new TimeSpan(newStartTime, newEndTime);
            times.add(newTimeSpan);
        } else {
            throw new RuntimeException("Invalid datatype used for time argument in method AddValues");
        }

        if (valueSet instanceof IScalarSet) {
            double[] x = new double[valueSet.getCount()];
            for (int i = 0; i < x.length; i++) {
                x[i] = ((IScalarSet) valueSet).getScalar(i);
            }
            ScalarSet newScalarSet = new ScalarSet(x);
            values.add(newScalarSet);
        } else if (valueSet instanceof IVectorSet) {
            Vector[] vectors = new Vector[valueSet.getCount()];

            for (int i = 0; i < vectors.length; i++) {
                vectors[i] = new Vector(((IVectorSet) valueSet).getVector(i).getXComponent(),
                        ((IVectorSet) valueSet).getVector(i).getYComponent(),
                        ((IVectorSet) valueSet).getVector(i).getZComponent());
            }
            VectorSet newVectorSet = new VectorSet(vectors);
            values.add(newVectorSet);
        } else {
            throw new RuntimeException("Invalid datatype used for values argument in method AddValues");
        }

        if (this.doExtendedDataVerification) {
            checkBuffer();
        }
    }


    /**
     * Validates a given buffer. The check made is for (empty data,type of times,type of data,time sequentiality)
     */
    public void checkBuffer() {
        if (times.size() != values.size()) {
            throw new RuntimeException("Different numbers of values and times in buffer");
        }

        if (times.size() == 0) {
            throw new RuntimeException("Buffer is empty");
        }

        for (int i = 0; i < times.size(); i++) {
            if (!(times.get(i) instanceof ITimeSpan || times.get(i) instanceof ITimeStamp)) {
                throw new RuntimeException("Illegal data type for time in buffer");
            }
        }

        for (int i = 0; i < values.size(); i++) {
            if (!(values.get(i) instanceof IScalarSet || values.get(i) instanceof IVectorSet)) {
                throw new RuntimeException("Illegal data type for values in buffer");
            }
        }

        if (times.get(0) instanceof ITimeSpan) {
            for (int i = 0; i < times.size(); i++) {
                if (((ITimeSpan) times.get(i)).getStart().getModifiedJulianDay() >= ((ITimeSpan) times.get(i)).getEnd().getModifiedJulianDay()) {
                    throw new RuntimeException("BeginTime is larger than or equal to EndTime in TimeSpan");
                }
            }

            for (int i = 1; i < times.size(); i++) {
                if (((ITimeSpan) times.get(i)).getStart().getModifiedJulianDay() != ((ITimeSpan) times.get(i - 1)).getEnd().getModifiedJulianDay()) {
                    throw new RuntimeException("EndTime is not equal to StartTime for the following time step");
                }
            }
        }
        if (times.get(0) instanceof ITimeStamp) {
            for (int i = 1; i < times.size(); i++) {
                if (((ITimeStamp) times.get(i)).getModifiedJulianDay() <= ((ITimeStamp) times.get(i - 1)).getModifiedJulianDay()) {
                    throw new RuntimeException("TimeStamps are not encreasing in buffer");
                }
            }
        }
    }


    /**
     * Validates a given time. The check made is for TimeSpan the starting time must be smaller
     * than the end time. Throws exception if the time is not valid.
     *
     * @param time
     * @throws Exception
     */
    private void checkTime(ITime time) throws Exception {
        if (time instanceof ITimeSpan) {
            if (((ITimeSpan) time).getStart().getModifiedJulianDay() >= ((ITimeSpan) time).getEnd().getModifiedJulianDay()) {
                throw new Exception("BeginTime is larger than or equal to EndTime in TimeSpan");
            }
        }
    }


    /**
     * Clears the buffer between start- and end- time of the time (TimeSpan)
     *
     * @param timeT timeSpan period to clear
     */
    public void clear(ITimeSpan timeT) {
        if (times.size() > 0) {
            if (times.get(0) instanceof ITimeStamp) {
                for (int i = 0; i < times.size(); i++) {
                    if (((ITimeStamp) times.get(i)).getModifiedJulianDay() > timeT.getStart().getModifiedJulianDay() && ((ITimeStamp) times.get(i)).getModifiedJulianDay() < timeT.getEnd().getModifiedJulianDay()) {
                        times.remove(i);
                        values.remove(i);
                    }
                }
            } else if (times.get(0) instanceof ITimeSpan) {
                for (int i = 0; i < times.size(); i++) {
                    if (((ITimeSpan) times.get(i)).getStart().getModifiedJulianDay() > timeT.getStart().getModifiedJulianDay() && ((ITimeSpan) times.get(i)).getEnd().getModifiedJulianDay() < timeT.getEnd().getModifiedJulianDay()) {
                        times.remove(i);
                        values.remove(i);
                    }
                }
            }
        }

    }


    /**
     * Clear all times and values in the buffer at or later than the specified
     * time If the specified time is type ITimeSpan the Start time is used.
     *
     * @param time the beginning period
     */
    public void clearAfter(ITime time) throws Exception {
        TimeStamp timeStamp = new TimeStamp();
        if (time instanceof ITimeStamp) {
            timeStamp.setModifiedJulianDay(((ITimeStamp) time).getModifiedJulianDay());
        } else if (time instanceof ITimeSpan) {
            timeStamp.setModifiedJulianDay(((ITimeSpan) time).getStart().getModifiedJulianDay());
        } else {
            throw new Exception("Wrong argument type for call to org.openmi.utilities.buffer.SmartBuffer.ClearAfter()");
        }

        boolean recordWasRemoved;

        if (times.size() > 0) {
            if (times.get(0) instanceof ITimeStamp) {
                do {
                    recordWasRemoved = false;
                    if (((ITimeStamp) times.get(times.size() - 1)).getModifiedJulianDay() >= timeStamp.getModifiedJulianDay()) {
                        values.remove(times.size() - 1);
                        times.remove(times.size() - 1);
                        recordWasRemoved = true;

                    }
                } while (recordWasRemoved && times.size() > 0);

            } else if (times.get(0) instanceof ITimeSpan) {
                do {
                    recordWasRemoved = false;
                    if (((ITimeSpan) times.get(times.size() - 1)).getStart().getModifiedJulianDay() >= timeStamp.getModifiedJulianDay()) {
                        values.remove(times.size() - 1);
                        times.remove(times.size() - 1);
                        recordWasRemoved = true;
                    }
                } while (recordWasRemoved && times.size() > 0);

            }
        }
    }


    /**
     * Clear all records in the buffer assocaited to time that is earlier that the
     * time specified in the argument list. However, one record associated to time
     * before the time in the argument list is left in the buffer.
     * The criteria when comparing TimeSpans is that they may not overlap in order
     * to be regarded as before each other.
     * (@see also org.openmi.utilities.buffer.Support.IsBefore(ITime ta, ITime tb)
     *
     * @param time endding time for clearing
     */
    public void clearBefore(ITimeStamp time) {
        int numberOfRecordsToRemove = 0;

        for (int i = 0; i < times.size(); i++) {
            ITime ti = (ITime) times.get(i);
            if (Support.isBefore(ti, time)) {
                numberOfRecordsToRemove++;
            }
        }
        numberOfRecordsToRemove--; // decrease index to ensure that one record before time is left back

        for (int i = 0; i < numberOfRecordsToRemove; i++) {
            times.remove(0);
            values.remove(0);
        }
    }


    /**
     * Return the time at a given index
     *
     * @param TimeStep the time step index
     * @return the true time
     * @throws Exception
     */
    public ITime getTimeAt(int TimeStep) throws Exception {
        if (this.doExtendedDataVerification) {
            checkBuffer();
        }
        return (ITime) times.get(TimeStep);
    }


    /**
     * Get the values at a given time
     *
     * @param requestedTime the requested time
     * @return the valueSet
     * @throws Exception
     */
    public IValueSet getValues(ITime requestedTime) throws Exception {
        if (this.doExtendedDataVerification) {
            checkTime(requestedTime);
            checkBuffer();
        }

        IValueSet returnValueSet;
        if (values.size() == 0) {
            returnValueSet = new ScalarSet();
        } else if (values.size() == 1) {
            returnValueSet = makeCopyOfValues();
        } else if (requestedTime instanceof ITimeStamp && times.get(0) instanceof ITimeStamp) {
            returnValueSet = mapFromTimeStampsToTimeStamp((ITimeStamp) requestedTime);
        } else if (requestedTime instanceof ITimeSpan && times.get(0) instanceof ITimeSpan) {
            returnValueSet = mapFromTimeSpansToTimeSpan((ITimeSpan) requestedTime);
        } else if (requestedTime instanceof ITimeSpan && times.get(0) instanceof ITimeStamp) {
            returnValueSet = mapFromTimeStampsToTimeSpan((ITimeSpan) requestedTime);
        } else if (requestedTime instanceof ITimeStamp && times.get(0) instanceof ITimeSpan) {
            returnValueSet = mapFromTimeSpansToTimeStamp((ITimeStamp) requestedTime);
        } else {
            throw new Exception("Requested TimeMapping not available in SmartWrapper Class");
        }
        return returnValueSet;
    }


    /**
     * Get the values at a given index
     *
     * @param TimeStep the time step index
     * @return The requested values
     * @throws Exception
     */
    public IValueSet getValuesAt(int TimeStep) throws Exception {
        if (this.doExtendedDataVerification) {
            checkBuffer();
        }
        return (IValueSet) values.get(TimeStep);
    }


    /**
     * make the copy of the values ArrayList
     *
     * @return the copy
     */
    private IValueSet makeCopyOfValues() {
        if (values.get(0) instanceof IScalarSet) {
            int NumberOfScalarsInEachScalarSet = ((IScalarSet) values.get(0)).getCount();
            double[] x = new double[NumberOfScalarsInEachScalarSet];
            for (int i = 0; i < NumberOfScalarsInEachScalarSet; i++) {
                x[i] = ((IScalarSet) values.get(0)).getScalar(i);
            }
            ScalarSet scalarSet = new ScalarSet(x);
            return scalarSet;
        } else {// values[0] instanceof VectorSet
            int NumberOfVectorsInEachVectorSet = ((IVectorSet) values.get(0)).getCount();
            Vector[] vectors = new Vector[NumberOfVectorsInEachVectorSet];
            Vector vector;

            for (int i = 0; i < NumberOfVectorsInEachVectorSet; i++) {
                double x = ((IVectorSet) values.get(0)).getVector(i).getXComponent();
                double y = ((IVectorSet) values.get(0)).getVector(i).getYComponent();
                double z = ((IVectorSet) values.get(0)).getVector(i).getZComponent();
                vector = new Vector(x, y, z);
                vectors[i] = vector;
            }
            VectorSet vectorSet = new VectorSet(vectors);
            return vectorSet;
        }
    }


    /**
     * do same mapping from TimeSpans To TimeSpan
     *
     * @param requestedTime the requested time
     * @return the requested valueSet
     * @throws Exception
     */
    private IValueSet mapFromTimeSpansToTimeSpan(ITimeSpan requestedTime)
            throws Exception {
        int M = ((IValueSet) values.get(0)).getCount();
        int N = times.size(); // Number of time steps in buffer
        double[][] xr = new double[M][]; // Values to return
        double trb = requestedTime.getStart().getModifiedJulianDay(); // Begin time in requester time
        // interval
        double tre = requestedTime.getEnd().getModifiedJulianDay(); // End time in requester time
        // interval

        int nk = 1; // number of model (scalars has only 1 and vectors has
        // 3 (3 axinstanceof))

        if (values.get(0) instanceof IVectorSet) {
            nk = 3;
        }

        for (int i = 0; i < M; i++) {
            xr[i] = new double[nk];
            for (int k = 0; k < nk; k++) {
                xr[i][k] = 0;
            }
        }

        for (int n = 0; n < times.size(); n++) {
            double tbbn = ((ITimeSpan) times.get(n)).getStart().getModifiedJulianDay();
            double tben = ((ITimeSpan) times.get(n)).getEnd().getModifiedJulianDay();

            // ---------------------------------------------------------------------------
            // B: <-------------------------->
            // R: <------------------------------------->
            // --------------------------------------------------------------------------
            if (trb <= tbbn && tre >= tben) {// Buffered TimeSpan fully
                // included in requested
                // TimeSpan
                for (int k = 1; k <= nk; k++) {
                    for (int i = 0; i < M; i++) {// for all values
                        // coorsponding to the same
                        // time interval
                        // double sbin = ((IValueSet)
                        // values.get(n)).GetVal(i,k);
                        double sbin = Support.getVal((IValueSet) values.get(n), i, k);
                        xr[i][k - 1] += sbin * (tben - tbbn) / (tre - trb);
                    }
                }
            } // ---------------------------------------------------------------------------
            // Times[i] Interval: t1|-----------------------|t2
            // Requested Interval: rt1|--------------|rt2
            // --------------------------------------------------------------------------
            else if (tbbn <= trb && tre <= tben) {// cover all
                for (int k = 1; k <= nk; k++) {
                    for (int i = 0; i < M; i++) {// for all values
                        // coorsponding to the same
                        // time interval
                        xr[i][k - 1] += Support.getVal((IValueSet) values.get(n), i, k);
                    }
                }
            } // ---------------------------------------------------------------------------
            // Times[i] Interval: t1|-----------------|t2
            // Requested Interval: rt1|--------------|rt2
            // --------------------------------------------------------------------------
            else if (tbbn < trb && trb < tben && tre > tben) {
                for (int k = 1; k <= nk; k++) {
                    for (int i = 0; i < M; i++) {// for all values
                        // coorsponding to the same
                        // time interval
                        double sbin = Support.getVal((IValueSet) values.get(n), i, k);
                        xr[i][k - 1] += sbin * (tben - trb) / (tre - trb);
                    }
                }
            } // ---------------------------------------------------------------------------
            // Times[i] Interval: t1|-----------------|t2
            // Requested Interval: rt1|--------------|rt2
            // --------------------------------------------------------------------------
            else if (trb < tbbn && tre > tbbn && tre < tben) {
                for (int k = 1; k <= nk; k++) {
                    for (int i = 0; i < M; i++) {// for all values
                        // coorsponding to the same
                        // time interval
                        double sbin = Support.getVal((IValueSet) values.get(n),
                                i, k);
                        xr[i][k - 1] += sbin * (tre - tbbn) / (tre - trb);
                    }
                }
            }
        }

        // --------------------------------------------------------------------------
        // |--------|---------|--------| B
        // |----------------| R
        // ---------------------------------------------------------------------------
        double tbb0 = ((ITimeSpan) times.get(0)).getStart().getModifiedJulianDay();
        double tbe0 = ((ITimeSpan) times.get(0)).getEnd().getModifiedJulianDay();
        double tbe1 = ((ITimeSpan) times.get(1)).getEnd().getModifiedJulianDay();

        if (trb < tbb0 && tre > tbb0) {
            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {
                    double sbi0 = Support.getVal((IValueSet) values.get(0), i,
                            k);
                    double sbi1 = Support.getVal((IValueSet) values.get(1), i,
                            k);
                    xr[i][k - 1] += ((tbb0 - trb) / (tre - trb)) * (sbi0 - (1 - relaxationFactor) * ((tbb0 - trb) * (sbi1 - sbi0) / (tbe1 - tbe0)));
                }
            }
        }

        // -------------------------------------------------------------------------------------
        // |--------|---------|--------| B
        // |----------------| R
        // -------------------------------------------------------------------------------------

        double tbeN_1 = ((ITimeSpan) times.get(N - 1)).getEnd().getModifiedJulianDay();
        double tbbN_2 = ((ITimeSpan) times.get(N - 2)).getStart().getModifiedJulianDay();

        if (tre > tbeN_1 && trb < tbeN_1) {
            double tbbN_1 = ((ITimeSpan) times.get(N - 1)).getStart().getModifiedJulianDay();

            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {
                    double sbiN_1 = Support.getVal((IValueSet) values.get(N - 1), i, k);
                    double sbiN_2 = Support.getVal((IValueSet) values.get(N - 2), i, k);
                    xr[i][k - 1] += ((tre - tbeN_1) / (tre - trb)) * (sbiN_1 + (1 - relaxationFactor) * ((tre - tbbN_1) * (sbiN_1 - sbiN_2) / (tbeN_1 - tbbN_2)));
                }
            }
        }
        // -------------------------------------------------------------------------------------
        // |--------|---------|--------| B
        // |----------------| R
        // -------------------------------------------------------------------------------------

        if (trb >= tbeN_1) {
            double tbeN_2 = ((ITimeSpan) times.get(N - 2)).getEnd().getModifiedJulianDay();
            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {
                    double sbiN_1 = Support.getVal((IValueSet) values.get(N - 1), i, k);
                    double sbiN_2 = Support.getVal((IValueSet) values.get(N - 1), i, k);
                    xr[i][k - 1] = sbiN_1 + (1 - relaxationFactor) * ((sbiN_1 - sbiN_2) / (tbeN_1 - tbbN_2)) * (trb + tre - tbeN_1 - tbeN_2);
                }
            }
        }

        // -------------------------------------------------------------------------------------
        // |--------|---------|--------| B
        // |----------------| R
        // -------------------------------------------------------------------------------------

        if (tre <= tbb0) {
            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {
                    double sbi0 = Support.getVal((IValueSet) values.get(0), i,
                            k);
                    double sbi1 = Support.getVal((IValueSet) values.get(1), i,
                            k);
                    xr[i][k - 1] = sbi0 - (1 - relaxationFactor) * ((sbi1 - sbi0) / (tbe1 - tbb0)) * (tbe0 + tbb0 - tre - trb);
                }
            }
        }

        // -------------------------------------------------------------------------------------
        if (values.get(0) instanceof IVectorSet) {
            Vector[] vectors = new Vector[M];
            for (int i = 0; i < M; i++) {
                vectors[i] = new Vector(xr[i][0], xr[i][1], xr[i][2]);
            }
            VectorSet vectorSet = new VectorSet(vectors);
            return vectorSet;
        } else {
            double[] xx = new double[M];
            for (int i = 0; i < M; i++) {
                xx[i] = xr[i][0];
            }
            ScalarSet scalarSet = new ScalarSet(xx);
            return scalarSet;
        }
    }


    /**
     * do some mapping from Time Stamps To TimeSpan
     *
     * @param requestedTime the requestedtime
     * @return the requested valueSet
     * @throws Exception
     */
    private IValueSet mapFromTimeStampsToTimeSpan(ITimeSpan requestedTime)
            throws Exception {
        int M = ((IValueSet) values.get(0)).getCount();
        int N = times.size(); // Number of time steps in buffer
        double[][] xr = new double[M][]; // Values to return
        double trb = requestedTime.getStart().getModifiedJulianDay(); // Begin time in requester time
        // interval
        double tre = requestedTime.getEnd().getModifiedJulianDay(); // End time in requester time
        // interval

        int nk = 1; // number of model (scalars has only 1 and vectors has
        // 3 (3 axinstanceof))

        if (values.get(0) instanceof IVectorSet) {
            nk = 3;
        }

        for (int i = 0; i < M; i++) {
            xr[i] = new double[nk];
        }

        for (int i = 0; i < M; i++) {
            for (int k = 0; k < nk; k++) {
                xr[i][k] = 0;
            }
        }

        for (int n = 0; n < times.size() - 1; n++) {
            double tbn = ((ITimeStamp) times.get(n)).getModifiedJulianDay();
            double tbnp1 = ((ITimeStamp) times.get(n + 1)).getModifiedJulianDay();

            // ---------------------------------------------------------------------------
            // B: <-------------------------->
            // R: <------------------------------------->
            // --------------------------------------------------------------------------
            if (trb <= tbn && tre >= tbnp1) {
                for (int k = 1; k <= nk; k++) {
                    for (int i = 0; i < M; i++) {// for all values
                        // coorsponding to the same
                        // time interval
                        double sbin = Support.getVal((IValueSet) values.get(n),
                                i, k);
                        double sbinp1 = Support.getVal((IValueSet) values.get(n + 1), i, k);
                        xr[i][k - 1] += 0.5 * (sbin + sbinp1) * (tbnp1 - tbn) / (tre - trb);
                    }
                }
            } // ---------------------------------------------------------------------------
            // Times[i] Interval: t1|-----------------------|t2
            // Requested Interval: rt1|--------------|rt2
            // --------------------------------------------------------------------------
            else if (tbn <= trb && tre <= tbnp1) {// cover all
                for (int k = 1; k <= nk; k++) {
                    for (int i = 0; i < M; i++) {// for all values
                        // coorsponding to the same
                        // time interval
                        double sbin = Support.getVal((IValueSet) values.get(n),
                                i, k);
                        double sbinp1 = Support.getVal((IValueSet) values.get(n + 1), i, k);
                        xr[i][k - 1] += sbin + ((sbinp1 - sbin) / (tbnp1 - tbn)) * ((tre + trb) / 2 - tbn);
                    }
                }
            } // ---------------------------------------------------------------------------
            // Times[i] Interval: t1|-----------------|t2
            // Requested Interval: rt1|--------------|rt2
            // --------------------------------------------------------------------------
            else if (tbn < trb && trb < tbnp1 && tre > tbnp1) {
                for (int k = 1; k <= nk; k++) {
                    for (int i = 0; i < M; i++) {// for all values
                        // coorsponding to the same
                        // time interval
                        double sbin = Support.getVal((IValueSet) values.get(n),
                                i, k);
                        double sbinp1 = Support.getVal((IValueSet) values.get(n + 1), i, k);
                        xr[i][k - 1] += (sbinp1 - (sbinp1 - sbin) / (tbnp1 - tbn) * ((tbnp1 - trb) / 2)) * (tbnp1 - trb) / (tre - trb);
                    }
                }
            } // ---------------------------------------------------------------------------
            // Times[i] Interval: t1|-----------------|t2
            // Requested Interval: rt1|--------------|rt2
            // --------------------------------------------------------------------------
            else if (trb < tbn && tre > tbn && tre < tbnp1) {
                for (int k = 1; k <= nk; k++) {
                    for (int i = 0; i < M; i++) {// for all values
                        // coorsponding to the same
                        // time interval
                        double sbin = Support.getVal((IValueSet) values.get(n),
                                i, k);
                        double sbinp1 = Support.getVal((IValueSet) values.get(n + 1), i, k);
                        xr[i][k - 1] += (sbin + (sbinp1 - sbin) / (tbnp1 - tbn) * ((tre - tbn) / 2)) * (tre - tbn) / (tre - trb);
                    }
                }
            }
        }

        // --------------------------------------------------------------------------
        // |--------|---------|--------| B
        // |----------------| R
        // ---------------------------------------------------------------------------
        double tb0 = ((ITimeStamp) times.get(0)).getModifiedJulianDay();
        double tb1 = ((ITimeStamp) times.get(0)).getModifiedJulianDay();
        double tbN_1 = ((ITimeStamp) times.get(N - 1)).getModifiedJulianDay();
        double tbN_2 = ((ITimeStamp) times.get(N - 2)).getModifiedJulianDay();

        if (trb < tb0 && tre > tb0) {
            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {
                    double sbi0 = Support.getVal((IValueSet) values.get(0), i,
                            k);
                    double sbi1 = Support.getVal((IValueSet) values.get(1), i,
                            k);
                    xr[i][k - 1] += ((tb0 - trb) / (tre - trb)) * (sbi0 - (1 - relaxationFactor) * 0.5 * ((tb0 - trb) * (sbi1 - sbi0) / (tb1 - tb0)));
                }
            }
        }

        // -------------------------------------------------------------------------------------
        // |--------|---------|--------| B
        // |----------------| R
        // -------------------------------------------------------------------------------------
        if (tre > tbN_1 && trb < tbN_1) {
            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {
                    double sbiN_1 = Support.getVal((IValueSet) values.get(N - 1), i, k);
                    double sbiN_2 = Support.getVal((IValueSet) values.get(N - 2), i, k);
                    xr[i][k - 1] += ((tre - tbN_1) / (tre - trb)) * (sbiN_1 + (1 - relaxationFactor) * 0.5 * ((tre - tbN_1) * (sbiN_1 - sbiN_2) / (tbN_1 - tbN_2)));
                }
            }
        }
        // //-------------------------------------------------------------------------------------
        // // |--------|---------|--------| B
        // // |----------------| R
        // //-------------------------------------------------------------------------------------
        //
        if (trb >= tbN_1) {
            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {
                    double sbiN_1 = Support.getVal((IValueSet) values.get(N - 1), i, k);
                    double sbiN_2 = Support.getVal((IValueSet) values.get(N - 1), i, k);
                    xr[i][k - 1] = sbiN_1 + (1 - relaxationFactor) * ((sbiN_1 - sbiN_2) / (tbN_1 - tbN_2)) * (0.5 * (trb + tre) - tbN_1);
                }
            }
        }

        // //-------------------------------------------------------------------------------------
        // // |--------|---------|--------| B
        // // |----------------| R
        // //-------------------------------------------------------------------------------------

        if (tre <= tb0) {
            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {
                    double sbi0 = Support.getVal((IValueSet) values.get(0), i,
                            k);
                    double sbi1 = Support.getVal((IValueSet) values.get(1), i,
                            k);
                    xr[i][k - 1] = sbi0 - (1 - relaxationFactor) * ((sbi1 - sbi0) / (tb1 - tb0)) * (tb0 - 0.5 * (trb + tre));
                }
            }
        }

        // -------------------------------------------------------------------------------------
        if (values.get(0) instanceof IVectorSet) {
            Vector[] vectors = new Vector[M];
            for (int i = 0; i < M; i++) {
                vectors[i] = new Vector(xr[i][0], xr[i][1], xr[i][2]);
            }
            VectorSet vectorSet = new VectorSet(vectors);
            return vectorSet;
        } else {
            double[] xx = new double[M];
            for (int i = 0; i < M; i++) {
                xx[i] = xr[i][0];
            }
            ScalarSet scalarSet = new ScalarSet(xx);
            return scalarSet;
        }
    }


    /**
     * do some mapping from Time Stamps To TimeStamp
     *
     * @param requestedTimeStamp the requested time
     * @return the requested valueSet
     * @throws Exception
     */
    private IValueSet mapFromTimeStampsToTimeStamp(ITimeStamp requestedTimeStamp)
            throws Exception {
        int M = ((IValueSet) values.get(0)).getCount();
        int N = times.size(); // Number of time steps in buffer
        double[][] xr = new double[M][]; // Values to return
        double tr = requestedTimeStamp.getModifiedJulianDay(); // Requested
        // TimeStamp

        int nk = 1; // number of model (scalars has only 1 and vectors has
        // 3 (3 axinstanceof))

        if (values.get(0) instanceof IVectorSet) {
            nk = 3;
        }

        for (int i = 0; i < M; i++) {
            xr[i] = new double[nk];
        }

        // ---------------------------------------------------------------------------
        // Buffered TimesStamps: | >tb0< >tb1< >tb2< >tbN<
        // Requested TimeStamp: | >tr<
        // -----------------------------------------> t
        // --------------------------------------------------------------------------
        if (tr <= ((ITimeStamp) times.get(0)).getModifiedJulianDay()) {
            double tb0 = ((ITimeStamp) times.get(0)).getModifiedJulianDay();
            double tb1 = ((ITimeStamp) times.get(1)).getModifiedJulianDay();
            double sbi0;
            double sbi1;

            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {// For each Vector in buffered
                    // VectorSet [0]
                    sbi0 = Support.getVal((IValueSet) values.get(0), i, k);
                    sbi1 = Support.getVal((IValueSet) values.get(1), i, k);
                    xr[i][k - 1] = ((sbi0 - sbi1) / (tb0 - tb1)) * (tr - tb0) * (1 - relaxationFactor) + sbi0;
                }
            }
        } // ---------------------------------------------------------------------------
        // Buffered TimesStamps: | >tb0< >tb1< >tb2< >tbN_2< >tbN_1<
        // Requested TimeStamp: | >tr<
        // ---------------------------------------------------> t
        // --------------------------------------------------------------------------
        else if (tr > ((ITimeStamp) times.get(N - 1)).getModifiedJulianDay()) {
            double sbiN_2;
            double sbiN_1;
            double tbN_2 = ((ITimeStamp) times.get(N - 2)).getModifiedJulianDay();
            double tbN_1 = ((ITimeStamp) times.get(N - 1)).getModifiedJulianDay();

            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {// For each Vector in buffered
                    // VectorSet [N-1]
                    sbiN_2 = Support.getVal((IValueSet) values.get(N - 2), i, k);
                    sbiN_1 = Support.getVal((IValueSet) values.get(N - 1), i, k);
                    xr[i][k - 1] = ((sbiN_1 - sbiN_2) / (tbN_1 - tbN_2)) * (tr - tbN_1) * (1 - relaxationFactor) + sbiN_1;
                }
            }
        } // ---------------------------------------------------------------------------
        // Availeble TimesStamps: | >tb0< >tb1< >tbna< >tnb< >tbN_1< >tbN_2<
        // Requested TimeStamp: | >tr<
        // -------------------------------------------------> t
        // --------------------------------------------------------------------------
        else {
            for (int n = N - 2; n >= 0; n--) {
                double tbn1 = ((ITimeStamp) times.get(n)).getModifiedJulianDay();
                double tbn2 = ((ITimeStamp) times.get(n + 1)).getModifiedJulianDay();
                if (tbn1 <= tr && tr <= tbn2) {
                    for (int k = 1; k <= nk; k++) {
                        for (int i = 0; i < M; i++) {// For each Vector in
                            // buffered VectorSet
                            // [n]
                            double sbin1 = Support.getVal((IValueSet) values.get(n), i, k);
                            double sbin2 = Support.getVal((IValueSet) values.get(n + 1), i, k);

                            xr[i][k - 1] = ((sbin2 - sbin1) / (tbn2 - tbn1)) * (tr - tbn1) + sbin1;
                        }
                    }
                    break;
                }
            }
        }

        // ----------------------------------------------------------------------------------------------

        if (values.get(0) instanceof IVectorSet) {
            Vector[] vectors = new Vector[M];
            for (int i = 0; i < M; i++) {
                vectors[i] = new Vector(xr[i][0], xr[i][1], xr[i][2]);
            }
            VectorSet vectorSet = new VectorSet(vectors);
            return vectorSet;
        } else {
            double[] xx = new double[M];
            for (int i = 0; i < M; i++) {
                xx[i] = xr[i][0];
            }
            ScalarSet scalarSet = new ScalarSet(xx);
            return scalarSet;
        }
    }


    /**
     * do some mapping from Time Spans To TimeStamp
     *
     * @param requestedTimeStamp the requested time
     * @return the requested valueSet
     * @throws Exception
     */
    private IValueSet mapFromTimeSpansToTimeStamp(ITimeStamp requestedTimeStamp)
            throws Exception {
        int M = ((IValueSet) values.get(0)).getCount();
        int N = times.size(); // Number of time steps in buffer
        double[][] xr = new double[M][]; // Values to return
        double tr = requestedTimeStamp.getModifiedJulianDay(); // Requested
        // TimeStamp
        int nk = 1; // number of model (scalars has only 1 and vectors has
        // 3 (3 axinstanceof))

        if (values.get(0) instanceof IVectorSet) {
            nk = 3;
        }

        for (int i = 0; i < M; i++) {
            xr[i] = new double[nk];
        }

        // ---------------------------------------------------------------------------
        // Buffered TimesSpans: | >tbb0< .......... >tbbN<
        // Requested TimeStamp: | >tr<
        // -----------------------------------------> t
        // --------------------------------------------------------------------------
        if (tr <= ((ITimeSpan) times.get(0)).getStart().getModifiedJulianDay()) {
            double tbb0 = ((ITimeSpan) times.get(0)).getStart().getModifiedJulianDay();
            double tbb1 = ((ITimeSpan) times.get(1)).getStart().getModifiedJulianDay();

            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {// For each Vector in buffered
                    // VectorSet [0]
                    double sbi0 = Support.getVal((IValueSet) values.get(0), i,
                            k);
                    double sbi1 = Support.getVal((IValueSet) values.get(1), i,
                            k);
                    xr[i][k - 1] = ((sbi0 - sbi1) / (tbb0 - tbb1)) * (tr - tbb0) * (1 - relaxationFactor) + sbi0;
                }
            }
        } // ---------------------------------------------------------------------------
        // Buffered TimesSpans: | >tbb0< ................. >tbbN_1<
        // Requested TimeStamp: | >tr<
        // ---------------------------------------------------> t
        // --------------------------------------------------------------------------
        else if (tr >= ((ITimeSpan) times.get(N - 1)).getEnd().getModifiedJulianDay()) {
            double tbeN_2 = ((ITimeSpan) times.get(N - 2)).getEnd().getModifiedJulianDay();
            double tbeN_1 = ((ITimeSpan) times.get(N - 1)).getEnd().getModifiedJulianDay();

            for (int k = 1; k <= nk; k++) {
                for (int i = 0; i < M; i++) {// For each Vector in buffered
                    // VectorSet [N-1]
                    double sbiN_2 = Support.getVal((IValueSet) values.get(N - 2), i, k);
                    double sbiN_1 = Support.getVal((IValueSet) values.get(N - 1), i, k);
                    xr[i][k - 1] = ((sbiN_1 - sbiN_2) / (tbeN_1 - tbeN_2)) * (tr - tbeN_1) * (1 - relaxationFactor) + sbiN_1;
                }
            }
        } // ---------------------------------------------------------------------------
        // Availeble TimesSpans: | >tbb0< ...................... >tbbN_1<
        // Requested TimeStamp: | >tr<
        // -------------------------------------------------> t
        // --------------------------------------------------------------------------
        else {
            for (int n = N - 1; n >= 0; n--) {
                double tbbn = ((ITimeSpan) times.get(n)).getStart().getModifiedJulianDay();
                double tben = ((ITimeSpan) times.get(n)).getEnd().getModifiedJulianDay();
                if (tbbn <= tr && tr < tben) {
                    for (int k = 1; k <= nk; k++) {
                        for (int i = 0; i < M; i++) {// For each Vector in
                            // buffered VectorSet
                            // [n]
                            xr[i][k - 1] = Support.getVal((IValueSet) values.get(n), i, k);
                        }
                    }
                    break;
                }
            }
        }

        // ----------------------------------------------------------------------------------------------

        if (values.get(0) instanceof IVectorSet) {
            Vector[] vectors = new Vector[M];
            for (int i = 0; i < M; i++) {
                vectors[i] = new Vector(xr[i][0], xr[i][1], xr[i][2]);
            }
            VectorSet vectorSet = new VectorSet(vectors);
            return vectorSet;
        } else {
            double[] xx = new double[M];
            for (int i = 0; i < M; i++) {
                xx[i] = xr[i][0];
            }
            ScalarSet scalarSet = new ScalarSet(xx);
            return scalarSet;
        }
    }

}
