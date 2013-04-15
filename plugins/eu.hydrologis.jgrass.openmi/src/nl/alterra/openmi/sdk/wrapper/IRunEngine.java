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
import org.openmi.standard.ITime;
import org.openmi.standard.ITimeStamp;
import org.openmi.standard.IValueSet;

/**
 * IRunEngine is the interface the ModelEngine component must implement when
 * used with the SmartWrapper.
 */
public interface IRunEngine {

    /**
     * The create method will be invoked just after creation of the object
     * that implements the IEngineApiAccess interface
     *
     * @param properties Hashtable with the same contents as the Component arguments
     *                   in the ILinkableComponent interface. Typically any information
     *                   needed for initialization of the model will be included in this table.
     *                   This could be path and file names for input files.
     */
    public void initialize(HashMap properties);

    /**
     * This method will be invoked after all computations are completed.
     */
    public void finish();

    /**
     * This method will be invoked after all computations are completed
     * and after the Finish method has been invoked.
     */
    public void dispose();

    /**
     * This method will make the model engine perform one time step.
     *
     * @return Returns true if the time step was completed, otherwise it will return false
     */
    public boolean performTimeStep();

    /**
     * Gets the current time of the model engine.
     *
     * @return The current time for the model engine
     */
    public ITime getCurrentTime();

    /**
     * Gets the time for which the next input is needed for a specific
     * Quantity and ElementSet combination.
     *
     * @param QuantityID   ID for the quantity
     * @param ElementSetID ID for the ElementSet
     * @return ITimeSpan or ITimeStamp
     */
    public ITime getInputTime(String QuantityID, String ElementSetID);

    /**
     * Gets the earlist needed time, which can be used to clear the buffer.
     * For most time stepping model engines this time will be the time for
     * the previous time step.
     *
     * @return TimeStamp
     */
    public ITimeStamp getEarliestNeededTime();

    /**
     * Sets values in the model engine.
     *
     * @param QuantityID   quantityID associated to the values
     * @param ElementSetID elementSetID associated to the values
     * @param values       The values
     */
    public void setValues(String QuantityID, String ElementSetID, IValueSet values);

    /**
     * Gets values from the model engine.
     *
     * @param QuantityID   quantityID associated to the requested values
     * @param ElementSetID elementSetID associated to the requested values
     * @return The requested values
     */
    public IValueSet getValues(String QuantityID, String ElementSetID);

    /**
     * A double flag for missing value definition
     *
     * @return the flag for missing value definition
     */
    public double getMissingValueDefinition();

    /**
     * Gets the identification string of the engine.
     *
     * @return the Identification string of engine
     */
    public String getComponentID();

    /**
     * Gets the additional descriptive information of the engine.
     *
     * @return the Additional descriptive information of Engine
     */
    public String getComponentDescription();

}
