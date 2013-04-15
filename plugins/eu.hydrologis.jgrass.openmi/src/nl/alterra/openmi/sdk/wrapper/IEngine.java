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

import org.openmi.standard.ITimeSpan;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.IOutputExchangeItem;

/**
 * Summary description for IEngineExchangeModelAccess.
 */
public interface IEngine extends IRunEngine {

    /**
     * Gets the model ID.
     *
     * @return The model ID
     */
    public String getModelID();

    /**
     * Gets the model description.
     *
     * @return a String to describe the model
     */
    public String getModelDescription();

    /**
     * Gets the Time Horizon.
     *
     * @return The time Horizon
     */
    public ITimeSpan getTimeHorizon();

    /**
     * Gets the number of input exchange items.
     *
     * @return The number of input exchange items
     */
    public int getInputExchangeItemCount();

    /**
     * Gets the number of output exchange items.
     *
     * @return The number of output exchange items
     */
    public int getOutputExchangeItemCount();

    /**
     * Returns the input exchange item at exchangeItemIndex position.
     *
     * @param exchangeItemIndex The index of expected item
     * @return the input exchange item at exchangeItemIndex position
     */
    public IOutputExchangeItem getOutputExchangeItem(int exchangeItemIndex);

    /**
     * Returns the output exchange item at exchangeItemIndex position.
     *
     * @param exchangeItemIndex The index of expected item
     * @return the output exchange item at exchangeItemIndex position
     */
    public IInputExchangeItem getInputExchangeItem(int exchangeItemIndex);

}
