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
package nl.alterra.openmi.sdk.spatial;

import org.openmi.standard.IDataOperation;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.IOutputExchangeItem;

/**
 * A specialized DataOperation class that inherits from the backbone
 * implementation and overwrites the isValid() method.
 */
public class DataOperation extends nl.alterra.openmi.sdk.backbone.DataOperation {

    public DataOperation() {
        super();
    }

    public DataOperation(String ID) {
        super(ID);
    }

    public DataOperation(IDataOperation source) {
        super(source);
    }

    /**
     * Test if this data operation is valid for the combination of input and
     * output exchange items given the combination with other already selected
     * data operations.
     *
     * @param inputItem              The input exchange item
     * @param outputItem             The output exchange item
     * @param selectedDataOperations The already selected data operations
     * @return True if data operation is valid in combination with the selected data operations
     */
    @Override
    public boolean isValid(IInputExchangeItem inputItem, IOutputExchangeItem outputItem, IDataOperation[] selectedDataOperations) {
        boolean methodAvailable = false;

        for (String id : ElementMapper.getIDsForAvailableDataOperations(outputItem.getElementSet().getElementType(),
                inputItem.getElementSet().getElementType())) {
            if (id.equals(this.getID())) {
                methodAvailable = true;
            }
        }

        if (!methodAvailable) {
            return false;
        }

        // --- check that only one SpatialMapping dataoperation is selected. ---
        int numberOfSelectedSpatialMappingDataOperations = 0;

        for (IDataOperation op : selectedDataOperations) {
            for (int i = 0; i < op.getArgumentCount(); i++) {
                if (op.getArgument(i).getKey().equals("Type") && (op.getArgument(i).getValue().equals("SpatialMapping"))) {
                    numberOfSelectedSpatialMappingDataOperations++;
                    if (op.getID().equals(getID())) {
                        return false;
                    }
                }
            }
        }

        return (numberOfSelectedSpatialMappingDataOperations <= 1);
    }

}