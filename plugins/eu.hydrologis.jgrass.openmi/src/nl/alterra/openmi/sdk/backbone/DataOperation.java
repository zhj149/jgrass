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
 * @author Rob Knapen, Alterra B.V., The Netherlands
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.backbone;

import org.openmi.standard.IArgument;
import org.openmi.standard.IDataOperation;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.IOutputExchangeItem;

import java.io.Serializable;

/**
 * The DataOperation class contains operations the providing component
 * should carry out on the data.
 * 
 * Many situations occcur where the raw data available at the source does not
 * match the request in location and time of the target component. Additional
 * data operations may be required, varying from temporal averaging to spatial
 * interpolation, etc. Especially when data is requested over a time span, it
 * should be known how a single element value needs to be computed, e.g. by
 * averaging, by accumulating, taking minumum or max., etc.
 */
public class DataOperation extends BackboneObject implements Serializable, IDataOperation {

    protected Arguments arguments;

    /**
     * Creates an instance with an empty string ID.
     */
    public DataOperation() {
        this("");
    }

    /**
     * Creates an instance with the specified ID.
     *
     * @param ID The ID value for the instance
     */
    public DataOperation(String ID) {
        this.setID(ID);
        arguments = new Arguments();
    }

    /**
     * Creates an instance based on another DataOperation.
     *
     * @param source The Data Operation to copy
     */
    public DataOperation(IDataOperation source) {
        this(source.getID());

        for (int i = 0; i < source.getArgumentCount(); i++) {
            addArgument(source.getArgument(i));
        }
    }

    /**
     * Gets the arguments collection.
     *
     * @return The arguments collection
     */
    public Arguments getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments collection.
     *
     * @param arguments The arguments collection to assign
     */
    public void setArgument(Arguments arguments) {
        this.arguments = arguments;
    }

    /**
     * adds an argument.
     *
     * @param argument The argument to add
     */
    public void addArgument(IArgument argument) {
        arguments.add(argument);
    }

    @Override
    public boolean describesSameAs(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!super.describesSameAs(obj)) {
            return false;
        }

        // see equals()
        DataOperation d = (DataOperation) obj;
        return this.getArguments().equals(d.getArguments());
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        DataOperation d = (DataOperation) obj;
        return getArguments().equals(d.getArguments());
    }

    @Override
    public int hashCode() {
        return super.hashCode() + getArguments().hashCode();
    }

    public int getArgumentCount() {
        return arguments.size();
    }

    public IArgument getArgument(int argumentIndex) {
        return arguments.get(argumentIndex);
    }

    public void initialize(IArgument[] properties) {
        arguments.addAll(properties);
    }

    /**
     * Tests if this data operation is valid for the combination of input and
     * output exchange items given the combination with other already selected
     * data operations.
     *
     * @param inputExchangeItem      The input exchange item
     * @param outputExchangeItem     The output exchange item
     * @param selectedDataOperations The already selected data operations
     * @return True if data operation is valid in combination with the selected data operations
     */
    public boolean isValid(IInputExchangeItem inputExchangeItem, 
        IOutputExchangeItem outputExchangeItem, 
        IDataOperation[] selectedDataOperations) {
        // default behaviour is to always return true
        return true;
    }

}
