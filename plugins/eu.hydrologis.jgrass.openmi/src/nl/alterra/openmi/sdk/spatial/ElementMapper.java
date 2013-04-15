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

import java.util.ArrayList;
import nl.alterra.openmi.sdk.backbone.Argument;
import nl.alterra.openmi.sdk.backbone.ScalarSet;
import nl.alterra.openmi.sdk.backbone.Vector;
import nl.alterra.openmi.sdk.backbone.VectorSet;
import org.openmi.standard.*;

/**
 * The ElementMapper converts one ValueSet (inputValues) associated one
 * ElementSet (fromElements) to a new ValuesSet (return value of MapValue) that
 * corresponds to another ElementSet (toElements). The conversion is a two step
 * workplan where the first step (Initialize) is executed at initialisation
 * time only, whereas the MapValues is executed during time stepping.
 * 
 * The initialise() method will create an appropriate mapping strategy (aka a
 * mapping method) that builds and contains a conversion matrix. This matrix
 * has the same number of rows as the number of elements in the ElementSet
 * associated to the accepting component (i.e. the toElements) and the same
 * number of columns as the number of elements in the ElementSet associated
 * to the providing component (i.e. the fromElements).
 * 
 * MappingEntry is possible for any zero-, one- and two-dimensional elements. Zero
 * dimensional elements will always be points, one-dimensional elements will
 * always be polylines and two- dimensional elements will allways be polygons.
 * 
 * The ElementMapper can use any descendent of the MappingStrategy class to
 * calculate an appropriate conversion matrix or return conversion parameters
 * in another way. Some basic implementations are provided. The enumeration
 * ElementMappingMethod contains all possibilities and a factory method to
 * instantiate a specific type.
 */
public class ElementMapper {

    private MappingStrategy strategy;

    /**
     * Create an instance. Must be initialised before it can be used!
     */
    public ElementMapper() {
        strategy = null;
    }

    /**
     * Initialises the ElementMapper. The initialisation includes setting the
     * isInitialised flag and calls UpdateMappingMatrix for claculation of the
     * mapping matrix.
     *
     * @param methodDescription String description of mapping method
     * @param fromElements      The IElementSet to map from
     * @param toElements        The IElementSet to map to
     */
    public void initialise(String methodDescription, IElementSet fromElements, IElementSet toElements) {
        try {
            XYGeometryTools.checkElementSet(fromElements);
            XYGeometryTools.checkElementSet(toElements);

            ElementMappingMethod mappingMethod = ElementMappingMethod.findMappingMethod(methodDescription,
                    fromElements.getElementType(),
                    toElements.getElementType());

            strategy = ElementMappingMethod.mappingMethodFactory(mappingMethod);
            strategy.initialise(mappingMethod, fromElements, toElements);
        }
        catch (Exception e) {
            throw new RuntimeException("Initialisation of element mapper failed: ", e);
        }
    }

    /**
     * MapValues calculates a IValueSet through multiplication of an inputValues IValueSet
     * vector or matrix (ScalarSet or VectorSet) on to the mapping matrix.
     * IScalarSets maps to IScalarSets and IVectorSets maps to IVectorSets.
     * Remark : Mapvalues is called every time a georeferenced link is evaluated.
     *
     * @param inputValues IValueSet of values to be mapped.
     * @return A IValueSet found by mapping of the inputValues on to the toElementSet.
     */
    public IValueSet mapValues(IValueSet inputValues) throws Exception {
        if (strategy == null) {
            throw new RuntimeException("ElementMapper needs to be initialised before it can be used");
        }

        if (!(inputValues.getCount() == strategy.getNumberOfColumns())) {
            throw new Exception("Dimension mismatch between inputValues and mapping matrix");
        }

        if (inputValues instanceof IScalarSet) {
            double[] outValues = new double[strategy.getNumberOfRows()];
            // --- Multiply the Values vector with the MappingMatrix ---
            for (int i = 0; i < strategy.getNumberOfRows(); i++) {
                outValues[i] = 0;
                for (int n = 0; n < strategy.getNumberOfColumns(); n++) {
                    outValues[i] += getMappingValue(i, n) * ((IScalarSet) inputValues).getScalar(n);
                }
            }
            return new ScalarSet(outValues);
        }

        if (inputValues instanceof IVectorSet) {
            Vector[] outValues = new Vector[strategy.getNumberOfRows()];
            // --- Multiply the Values vector with the MappingMatrix ---
            for (int i = 0; i < strategy.getNumberOfRows(); i++) {
                outValues[i].set(0, 0, 0);
                for (int n = 0; n < strategy.getNumberOfColumns(); n++) {
                    outValues[i].addVector(getMappingValue(i, n), 0, ((IVectorSet) inputValues).getVector(n));
                }
            }
            return new VectorSet(outValues);
        }

        throw new Exception("Invalid datatype used for inputValues parameter. MapValues failed");
    }

    /**
     * Get the (row, column) value from the mapping matrix used by the
     * mapping method (strategy) used in this element mapper.
     *
     * @param row    Zero based row index
     * @param column Zero based column index
     * @return Element(row,column) from the mapping matrix
     * @throws IndexOutOfBoundsException; Exception
     */
    public double getMappingValue(int row, int column) throws Exception {
        if (strategy == null) {
            throw new Exception("ElementMapper needs to be initialised before it can be used");
        }

        return strategy.getValueFromMappingMatrix(row, column);
    }

    /**
     * Return an arraylist of IDataOperations provided by the ElementMapper for mapping
     * from a specified element type.
     * <p/>
     * <B>remarks</B>
     * Each IDataOperation object will contain 3 IArguments:
     * <p> [Key] [Value] [ReadOnly] [Description]----------------- </p>
     * <p> ["Type"] ["SpatialMapping"] [true] ["Using the ElementMapper"] </p>
     * <p> ["ID"] [The Operation ID] [true] ["Internal ElementMapper dataoperation ID"] </p>
     * <p> ["Description"] [The Operation Description] [true] ["Using the ElementMapper"] </p>
     * <p> ["ToElementType"] [ElementType] [true] ["Valid To-Element Types"] </p>
     *
     * @param fromType The specified ElementType
     * @return ArrayList which contains the available data operations (IDataOperation)
     */
    public static ArrayList<IDataOperation> getAvailableDataOperations(IElementSet.ElementType fromType) {
        ArrayList<IDataOperation> availableDataOperations = new ArrayList<IDataOperation>();

        for (ElementMappingMethod m : ElementMappingMethod.values()) {
            if (m.getFromElementType().equals(fromType)) {
                DataOperation dataOperation = new DataOperation(String.format("ElementMapper%d", m.getID()));
                dataOperation.addArgument(new Argument("ID", String.valueOf(m.getID()), true,
                        "Internal ElementMapper dataoperation ID"));
                dataOperation.addArgument(new Argument("Description", m.getDescription(), true,
                        "Operation description"));
                dataOperation.addArgument(new Argument("Type", "SpatialMapping", true,
                        "Using the ElementMapper"));
                dataOperation.addArgument(new Argument("FromElementType", m.getFromElementType().toString(),
                        true, "Valid From-Element Types"));
                dataOperation.addArgument(new Argument("ToElementType", m.getToElementType().toString(),
                        true, "Valid To-Element Types"));
                availableDataOperations.add(dataOperation);
            }
        }

        return availableDataOperations;
    }

    /**
     * Gives a list of ID's (strings) for available mapping methods given the
     * combination of from and to element types.
     *
     * @param fromType Element type of the elements in the source element set
     * @param toType   Element type of the elements in the target element set
     * @return ArrayList of method ID's
     */
    public static ArrayList<String> getIDsForAvailableDataOperations(IElementSet.ElementType fromType, IElementSet.ElementType toType) {
        ArrayList<String> ids = new ArrayList<String>();

        for (ElementMappingMethod m : ElementMappingMethod.values()) {
            if (m.getFromElementType().equals(fromType)) {
                if (m.getToElementType().equals(toType)) {
                    ids.add(String.format("ElementMapper%d", m.getID()));
                }
            }
        }

        return ids;
    }

}
