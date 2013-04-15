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

import java.util.ArrayList;
import org.openmi.standard.*;
import nl.alterra.openmi.sdk.backbone.OutputExchangeItem;
import nl.alterra.openmi.sdk.spatial.ElementMapper;

/**
 * An abstract class for a linkable engine.
 */
public abstract class LinkableEngine extends LinkableRunEngine {
    
    /**
     * Creates a default instance.
     */
    public LinkableEngine() {
    }

    /**
     * @see ILinkableComponent#getInputExchangeItemCount()
     */
    public int getInputExchangeItemCount() {
        int inputExchangeItemSize = ((IEngine) engineApiAccess).getInputExchangeItemCount();
        return inputExchangeItemSize;
    }

    /**
     * @see ILinkableComponent#getOutputExchangeItemCount()
     */
    public int getOutputExchangeItemCount() {
        return ((IEngine) engineApiAccess).getOutputExchangeItemCount();
    }

    /**
     * @see ILinkableComponent#getInputExchangeItem(int)
     */
    public IInputExchangeItem getInputExchangeItem(int inputExchangeItemIndex) {
        return ((IEngine) engineApiAccess).getInputExchangeItem(inputExchangeItemIndex);
    }

    /**
     * @see ILinkableComponent#getOutputExchangeItem(int)
     */
    public IOutputExchangeItem getOutputExchangeItem(int outputExchangeItemIndex) {
        IOutputExchangeItem outputExchangeItem = ((IEngine) engineApiAccess).getOutputExchangeItem(outputExchangeItemIndex);

        // Add dataoperations to outputExchangeItems
        ArrayList dataOperations;
        dataOperations = ElementMapper.getAvailableDataOperations(outputExchangeItem.getElementSet().getElementType());
        boolean dataOperationExists;
        for (int i = 0; i < dataOperations.size(); i++) {
            IDataOperation dataOperation = (IDataOperation) dataOperations.get(i);
            dataOperationExists = false;
            for (int j = 0; j < outputExchangeItem.getDataOperationCount(); j++) {
                IDataOperation existingDataOperation = outputExchangeItem.getDataOperation(j);
                if (dataOperation.getID().equals(existingDataOperation.getID())) {
                    dataOperationExists = true;
                }
            }

            if (!dataOperationExists) {
                if (outputExchangeItem instanceof OutputExchangeItem) {
                    ((OutputExchangeItem) outputExchangeItem).addDataOperation(dataOperation);
                }
            }
        }

        return outputExchangeItem;
    }

    /**
     * @see ILinkableComponent#getModelDescription()
     */
    public String getModelDescription() {
        return ((IEngine) engineApiAccess).getModelDescription();
    }

    /**
     * @see ILinkableComponent#getModelID()
     */
    public String getModelID() {
        if (engineApiAccess != null) {
            return ((IEngine) engineApiAccess).getModelID();
        }
        else {
            return null;
        }
    }

    /**
     * @see ILinkableComponent#getTimeHorizon()
     */
    public ITimeSpan getTimeHorizon() {
        return ((IEngine) engineApiAccess).getTimeHorizon();
    }

    /**
     * @see LinkableRunEngine#setEngineApiAccess()
     */
    protected abstract void setEngineApiAccess();

    // @@@@@@@  OTHERS @@@@@@@
    /* (non-Javadoc)
      * @see org.openmi.standard.ILinkableComponent#getInputExchangeItem(int)
      */
    /*

     private void populateQuantity(int index, org.openmi.backbone.Quantity quantity) throws Exception {
         //--- populate the Quantity ---
         org.openmi.backbone.Unit unit = new org.openmi.backbone.Unit();
         quantity.setID(((IEngine) engineApiAccess).getQuantityID(index));
         quantity.setDescription(((IEngine) engineApiAccess).getQuantityDescription(index));
         //quantity.Dimension =
         unit.setID(((IEngine) engineApiAccess).getQuantityUnitID(index));
         unit.setConversionFactorToSI(((IEngine) engineApiAccess).getQuantityUnitConversionToSI(index));
         unit.setOffSetToSI(((IEngine) engineApiAccess).getQuantityUnitOffSetToSI(index));
         quantity.setUnit(unit);
         if (((IEngine) engineApiAccess).getQuantityValueType(index) == 1) {
             quantity.setValueType(org.openmi.standard.ValueType.Scalar);
         }
         else if (((IEngine) engineApiAccess).getQuantityValueType(index) == 2) {
             quantity.setValueType(org.openmi.standard.ValueType.Vector);
         }
         else {
             throw new Exception("Illegal value type specified");
         }
     }

     private void populateElementSet(int index, org.openmi.backbone.ElementSet elementSet) throws Exception {
         //--- populate the elementSet ---
         elementSet.setID(((IEngine) engineApiAccess).getElementSetID(index));
         elementSet.setDescription(((IEngine) engineApiAccess).getElementSetDescription(index));
         int elementCount = ((IEngine) engineApiAccess).getElementSize(index);

         // TODO: The switch statement below should use the new private method IntToElementType
         switch(((IEngine) engineApiAccess).getElementType(index)) {
         case 0:
             elementSet.setElementType(org.openmi.standard.ElementType.IDBased);
             elementSet.setSpatialReference(new org.openmi.backbone.SpatialReference("ID Based"));
             for (int i = 0; i < elementCount; i++)
             {
                 elementSet.addElement(new org.openmi.backbone.Element());
                 elementSet.getElements()[i].setID(((IEngine) engineApiAccess).getElementID(index,i));
             }
             break;

         case 1:
             elementSet.setElementType(org.openmi.standard.ElementType.XYPoint);
             break;

         case 2:
             elementSet.setElementType(org.openmi.standard.ElementType.XYLine);
             break;

         case 3:
             elementSet.setElementType(org.openmi.standard.ElementType.XYPolyLine);
             break;

         case 4:
             elementSet.setElementType(org.openmi.standard.ElementType.XYPolygon);
             break;

         default:
             throw new Exception("Illegal ElementType specified");
         }

         if (elementSet.getElementType() != org.openmi.standard.ElementType.IDBased) {
             elementSet.setSpatialReference(new org.openmi.backbone.SpatialReference(((IEngine) engineApiAccess).
                     getElementSetSpatialReference(index)));
             for (int elementIndex = 0; elementIndex < elementCount; elementIndex++) {
                 elementSet.addElement(new org.openmi.backbone.Element());
                 elementSet.getElements()[elementIndex].setID(((IEngine) engineApiAccess).getElementID(index,elementIndex));
                 for (int vertexIndex = 0; vertexIndex < ((IEngine) engineApiAccess).getVertexSize(index,elementIndex); vertexIndex++)
                 {
                     double x = ((IEngine) engineApiAccess).getXCoordinate(index, elementIndex, vertexIndex);
                     double y = ((IEngine) engineApiAccess).getYCoordinate(index, elementIndex, vertexIndex);
                     double z = ((IEngine) engineApiAccess).getZCoordinate(index, elementIndex, vertexIndex);
                     elementSet.getElements()[elementIndex].addVertex(new org.openmi.backbone.Vertex(x,y,z));
                 }
             }
         }

     }
     */

}
