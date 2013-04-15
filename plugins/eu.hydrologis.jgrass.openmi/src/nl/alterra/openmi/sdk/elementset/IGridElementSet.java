/* ***************************************************************************
 *
 *    Copyright (C) 2006 Alterra, Wageningen University and Research centre.
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
 *****************************************************************************
 *
 * @author Rob Knapen, Alterra B.V., The Netherlands
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.elementset;

import org.openmi.standard.IElementSet;

/**
 * The IGridElementSet is derived from the standard OpenMI IElementSet and adds
 * some grid specific functionality.
 */
public interface IGridElementSet extends IElementSet {

    /**
     * Gets the X coordinate of the lower left corner of the grid.
     *
     * @return double Lower left X
     */
    public double getCornerX();

    /**
     * Sets the X coordinate of the lower left corner of the grid.
     *
     * @param value X coordinate to set
     */
    public void setCornerX(double value);

    /**
     * Gets the Y coordinate of the lower left corner of the grid.
     *
     * @return double Lower left Y
     */
    public double getCornerY();

    /**
     * Sets the Y coordinate of the lower left corner of the grid.
     *
     * @param value Y coordinate to set
     */
    public void setCornerY(double value);

    /**
     * Sets the size of the (square) cells.
     *
     * @param value Size to set
     */
    public void setCellSize(double value);

    /**
     * Gets the size of the (square) cells.
     *
     * @return double Cell size
     */
    public double getCellSize();

    /**
     * Sets the number of columns and rows in the grid.
     *
     * @param numberOfColumns
     * @param numberOfRows
     */
    public void setGridSize(int numberOfColumns, int numberOfRows);

    /**
     * Gets the number of columns in the grid.
     *
     * @return int Number of columns
     */
    public int getColCount();

    /**
     * Gets the number of rows in the grid.
     *
     * @return int Number of rows
     */
    public int getRowCount();

    /**
     * Gets the number of cells in the grid.
     *
     * @return int Number of cells
     */
    public int getSize();

    /**
     * Gets the NO DATA value for the element set.
     *
     * @return double
     */
    public double getNoDataValue();

    /**
     * Sets the NO DATA value for the element set.
     *
     * @param value The no data value
     */
    public void setNoDataValue(double value);

    /**
     * Tests if all relevant field values are congruent. Member fields that
     * have a meaning for proper functioning of the application rather than
     * describing a "real world" property should not be included in the test.
     * 
     * The tested object therefore does not need to be the same instance.
     *
     * @param obj to compare with
     * @return true if object is congruent
     */
    public boolean describesSameAs(Object obj);

}
