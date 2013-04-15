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
 ****************************************************************************/
package nl.alterra.openmi.sdk.wrapper;

import java.util.HashMap;
import org.openmi.standard.IScalarSet;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.buffer.SmartBuffer;
import nl.alterra.openmi.sdk.spatial.ElementMapper;

/**
 * SmartOutputLink class
 * a part of the smart wrapper engine
 */
public class SmartOutputLink extends SmartLink {

    private SmartBuffer smartBuffer = null;
    private ElementMapper elementMapper = null;
    private boolean useSpatialMapping = false;
    private HashMap bufferStates = null;

    /**
     * GETTER for smart buffer
     *
     * @return te smart buffer
     */
    public SmartBuffer getSmartBuffer() {
        return smartBuffer;
    }

    /**
     * the initialize implementation
     *
     * @see SmartLink#initialize(IRunEngine)
     */
    public void initialize(IRunEngine engineApiAccess) {
        int i;
        smartBuffer = new SmartBuffer();
        useSpatialMapping = false;

        bufferStates = new HashMap();


        this.engineApiAccess = engineApiAccess;

        //Setup Spatial mapper - mapping method is set to default for now!
        HashMap dataOperationsHash = new HashMap();
        for (i = 0; i < link.getDataOperationsCount(); i++) {
            for (int n = 0; n < link.getDataOperation(i).getArgumentCount(); n++) {
                dataOperationsHash.put(link.getDataOperation(i).getArgument(n).getKey(),
                        link.getDataOperation(i).getArgument(n).getValue());
            }
            if (dataOperationsHash.containsKey("Type")) {
                if (dataOperationsHash.get("Type") == "SpatialMapping") {
                    useSpatialMapping = true;
                    elementMapper = new ElementMapper();
                    elementMapper.initialise((String) dataOperationsHash.get("Description"),
                            link.getSourceElementSet(), link.getTargetElementSet());
                }
            }
        }
    }

    /**
     * To update the buffer at a given time
     *
     * @param time the time of the update
     */
    public void updateBuffer(ITime time) throws Exception {
        if (link.getSourceQuantity() != null && link.getSourceElementSet() != null) {
            IValueSet valueSet = this.engineApiAccess.getValues(link.getSourceQuantity().getID(),
                    link.getSourceElementSet().getID());
            if (useSpatialMapping == true) {
                this.smartBuffer.addValues(time, elementMapper.mapValues(valueSet));
            }
            else {
                this.smartBuffer.addValues(time, valueSet);
            }
        }
        smartBuffer.clearBefore(link.getTargetComponent().getEarliestInputTime());
    }

    /**
     * The getvalue method
     *
     * @param time the time
     * @return the values
     */
    public IValueSet getValue(ITime time) {
        IValueSet values = null;
        try {
            values = smartBuffer.getValues(time);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return convertUnit(values);
    }

    /**
     * To keep the current buffer state
     *
     * @param bufferStateID the conresponding string ID of the buffer state
     */
    public void keepCurrentBufferState(String bufferStateID) {
        bufferStates.put(bufferStateID, new SmartBuffer(this.getSmartBuffer()));
    }

    /**
     * To restore the buffer state
     *
     * @param bufferStateID the conresponding string ID of the buffer state
     */
    public void restoreBufferState(String bufferStateID) {
        this.smartBuffer = new SmartBuffer((SmartBuffer) bufferStates.get(bufferStateID));
    }

    /**
     * To clear a given buffer state
     *
     * @param bufferStateID the conresponding string ID of the buffer state
     */
    public void clearBufferState(String bufferStateID) {
        bufferStates.remove(bufferStateID);
    }

    private IValueSet convertUnit(IValueSet values) {
        double aSource = link.getSourceQuantity().getUnit().getConversionFactorToSI();
        double bSource = link.getSourceQuantity().getUnit().getOffSetToSI();
        double aTarget = link.getTargetQuantity().getUnit().getConversionFactorToSI();
        double bTarget = link.getTargetQuantity().getUnit().getOffSetToSI();

        if (aSource != aTarget || bSource != bTarget) {

            double[] x = new double[values.getCount()];

            for (int i = 0; i < values.getCount(); i++) {
                x[i] = (((IScalarSet) values).getScalar(i) * aSource + bSource - bTarget) / aTarget;
            }
            return new ScalarSet(x);
        }
        return values;
    }

}
