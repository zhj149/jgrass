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

import org.openmi.standard.IEvent;
import org.openmi.standard.IScalarSet;
import org.openmi.standard.ITime;
import org.openmi.standard.ITimeStamp;
import nl.alterra.openmi.sdk.backbone.Event;
import nl.alterra.openmi.sdk.backbone.ScalarSet;

/**
 * SmartInputLink class
 * a part of the smart wrapper
 */
public class SmartInputLink extends SmartLink {

    /**
     * The initialise method
     *
     * @param engineApiAccess The engine api access
     */
    public void initialize(IRunEngine engineApiAccess) {
        this.engineApiAccess = engineApiAccess;
    }

    /**
     * To update the input
     *
     * @param InputTime the new input time
     * @throws Exception
     */
    public void updateInput(ITime InputTime) throws Exception {
        Event eventA = new Event(IEvent.EventType.TargetBeforeGetValuesCall);
        eventA.setDescription("GetValues(t = " + LinkableRunEngine.iTimeToString(InputTime) + " ,");
        eventA.setDescription(eventA.getDescription() + "QS = " + link.getSourceQuantity().getID() + " ,QT = " + link.getTargetQuantity().getID());
        eventA.setDescription(eventA.getDescription() + ") ===>>>");
        eventA.setSender(this.link.getTargetComponent());
        eventA.setSimulationTime((ITimeStamp) this.engineApiAccess.getCurrentTime());
        this.link.getTargetComponent().sendEvent(eventA);

        IScalarSet sourceValueSet = (IScalarSet) link.getSourceComponent().getValues(InputTime, link.getID());
        //The input values set is copied in order to avoid the risk that it is changed be the
        //provider.
        double missingValueDefinition = engineApiAccess.getMissingValueDefinition();
        Double[] values = new Double[sourceValueSet.getCount()];
        for (int i = 0; i < sourceValueSet.getCount(); i++) {
            if (!sourceValueSet.isValid(i)) {
                values[i] = missingValueDefinition;
            }
            else {
                values[i] = sourceValueSet.getScalar(i);
            }
        }
        ScalarSet targetValueSet = new ScalarSet(values);

        Event eventB = new Event(IEvent.EventType.TargetAfterGetValuesReturn);
        eventB.setDescription("GetValues(t = " + LinkableRunEngine.iTimeToString(InputTime) + ", ");
        eventB.setDescription(eventB.getDescription() + "QS = " + link.getSourceQuantity().getID() + " ,QT = " +
                link.getTargetQuantity().getID());
        eventB.setDescription(eventB.getDescription() + ") Returned <<< ---");
        eventB.setSender(this.link.getTargetComponent());
        eventB.setSimulationTime((ITimeStamp) this.engineApiAccess.getCurrentTime());
        this.getLink().getTargetComponent().sendEvent(eventB);

        //  ProvidingComponent.GetValues((new ITime[1] {InputTime}),(Ilink.ID));
        engineApiAccess.setValues(link.getTargetQuantity().getID(), link.getTargetElementSet().getID(), targetValueSet);
    }

}
