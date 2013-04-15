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

import org.openmi.standard.IElementSet;

/**
 * The MappingStrategy is an abstract class that serves as a base for classes
 * implementing mapping methods between source and target element sets, maybe
 * with different element types.
 * 
 * The ElementMappingMethod enumeration contains the list of all supported
 * mapping methods with specifications and a factory method to create the
 * corresponding mapping strategy descendent. The ElementMapper class uses
 * this in its initialisation method to obtain a mapping strategy.
 * 
 * The MappingStrategy class and the initial descendent implementations
 * started from a refactoring of the giant .NET ElementMapper original.
 */
public abstract class MappingStrategy {

    protected double[][] mappingMatrix;
    protected int numberOfRows;
    protected int numberOfColumns;
    protected boolean isInitialised = false;

    /**
     * Initialise the MappingEntry Strategy.
     *
     * @param method The mapping method to use
     * @param source The element set to map from
     * @param target The element set to map to
     * @throws Exception
     */
    public void initialise(ElementMappingMethod method, IElementSet source, IElementSet target)
            throws Exception {
        numberOfRows = target.getElementCount();
        numberOfColumns = source.getElementCount();
        mappingMatrix = new double[numberOfRows][numberOfColumns];

        // update matrix in specific implementations
        updateMappingMatrix(method, source, target);

        isInitialised = true;
    }

    /**
     * Fill the mapping matrix with values according to the selected mapping
     * method and the source and target element sets.
     *
     * @param method The mapping method to use
     * @param source The element set to map from
     * @param target The element set to map to
     * @throws Exception
     */
    public abstract void updateMappingMatrix(ElementMappingMethod method, IElementSet source, IElementSet target)
            throws Exception;

    /**
     * Extracts the (row, column) element from the mapping matrix.
     *
     * @param row    Zero based row index
     * @param column Zero based column index
     * @return Element(row,column) from the mapping matrix
     * @throws IndexOutOfBoundsException
     */
    public double getValueFromMappingMatrix(int row, int column) {
        validateIndicies(row, column);
        return mappingMatrix[row][column];
    }

    /**
     * Sets individual the (row, column) element in the mapping matrix.
     *
     * @param value  Element value to set
     * @param row    Zero based row index
     * @param column Zero based column index
     * @throws IndexOutOfBoundsException
     */
    public void setValueInMappingMatrix(double value, int row, int column) {
        validateIndicies(row, column);
        mappingMatrix[row][column] = value;
    }

    /**
     * Get the minimum column value in the matrix for the give row.
     *
     * @param row Row index
     * @return Minimum column value in the given row
     */
    public double getMinColumnValue(int row) {
        double min = mappingMatrix[row][0];

        for (int j = 1; j < numberOfColumns; j++) {
            if (mappingMatrix[row][j] < min) {
                min = mappingMatrix[row][j];
            }
        }
        return min;
    }

    /**
     * Replace matrix values with row based denominators.
     *
     * @param inverse Calculate inverse denominators, or based on minimum column value
     */
    public void calculateDenominators(boolean inverse) {
        for (int i = 0; i < numberOfRows; i++) {
            double min = getMinColumnValue(i);
            if ((!inverse) || (Double.compare(min, 0.0) == 0)) {
                setStandardColumnDenominators(i, min);
            }
            else {
                setInverseColumnDenominators(i);
            }
        }
    }

    /**
     * Replace column values with standard denominators.
     *
     * @param row   The mapping matrix row to update
     * @param value The value to calculate the denominators for
     */
    public void setStandardColumnDenominators(int row, double value) {
        int denominator = 0;

        for (int j = 0; j < numberOfColumns; j++) {
            if (Double.compare(mappingMatrix[row][j], value) == 0) {
                mappingMatrix[row][j] = 1.0;
                denominator++;
            }
            else {
                mappingMatrix[row][j] = 0.0;
            }
        }

        for (int j = 0; j < numberOfColumns; j++) {
            mappingMatrix[row][j] = mappingMatrix[row][j] / denominator;
        }
    }

    /**
     * Replace column values with inverse denominators.
     *
     * @param row The mapping matrix row to update
     */
    public void setInverseColumnDenominators(int row) {
        double denominator = 0;

        for (int j = 0; j < numberOfColumns; j++) {
            mappingMatrix[row][j] = 1 / mappingMatrix[row][j];
            denominator = denominator + mappingMatrix[row][j];
        }

        for (int j = 0; j < numberOfColumns; j++) {
            mappingMatrix[row][j] = mappingMatrix[row][j] / denominator;
        }
    }

    /**
     * Get the number of rows in the mapping matrix.
     *
     * @return The number of rows
     */
    public int getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * Get the number of columns in the mapping matrix.
     *
     * @return The number of columns
     */
    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    /**
     * Check if the mapping matrix is initialised or not.
     *
     * @return True if the mapping matrix is initialised
     */
    public boolean isInitialised() {
        return isInitialised;
    }

    /**
     * Check if indicies are within the bounds of the mapping matrix.
     *
     * @param row    Row index to validate
     * @param column Column index to validate
     * @throws IndexOutOfBoundsException
     */
    protected void validateIndicies(int row, int column) {
        if ((row < 0) || (row >= numberOfRows)) {
            throw new IndexOutOfBoundsException(String.format("Element MappingEntry: row index %d not between 0 and %d", row, numberOfRows));
        }

        if ((column < 0) || (column >= numberOfColumns)) {
            throw new IndexOutOfBoundsException(String.format("Element MappingEntry: column index %d not between 0 and %d", column, numberOfColumns));
        }
    }

}
