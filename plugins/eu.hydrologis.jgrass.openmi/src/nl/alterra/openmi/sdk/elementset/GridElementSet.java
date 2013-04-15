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

import nl.alterra.openmi.sdk.backbone.SpatialReference;
import org.openmi.standard.IElementSet;
import org.openmi.standard.ISpatialReference;

/**
 * The GridElementSet implements the IGridElementSet interface, which inherits
 * from the standard OpenMI IElementSet interface. It is a special element set
 * that allows for handling grids with cells of equal size in a more optimized
 * way (as possible with the standard ElementSet).
 * 
 * Note that the (grid) element set only describes the spatial dimension, it
 * does not hold any values! These are passed as valuesets.
 */
public class GridElementSet implements IGridElementSet {

    private final static int GridElementSetVersionNumber = 1;
    private final static String SetID = "Alterra_GridElementSet";
    private final static String SetDescription = "Spatial grid of equal sized cells";
    private final static String SpatialRefID = "Undefined";
    private final static String RowColumnIDSeperator = ";";
    private double cornerX;
    private double cornerY;
    private double noDataValue;
    private double cellSize;
    private int colCount;
    private int rowCount;

    /**
     * Gets the ID of the grid element set instance.
     *
     * @return String ID
     */
    public String getID() {
        return GridElementSet.SetID;
    }

    /**
     * Gets the description of the grid element set instance.
     *
     * @return String description
     */
    public String getDescription() {
        return GridElementSet.SetDescription;
    }

    /**
     * Gets the spatial reference for the grid element set. Currently this
     * method simply returns a new SpatialReference instance with the ID set to
     * "Undefined".
     *
     * @return ISpatialReference A new "undefined" SpatialReference
     */
    public ISpatialReference getSpatialReference() {
        return new SpatialReference(GridElementSet.SpatialRefID);
    }

    /**
     * The grid element set will return single elements as XYPolygon types, to
     * conform to the OpenMI 1.2 standard. Future versions could support a real
     * Cell type. For now either the knowledge that a GridElementSet represents
     * a rectangular grid with equal sized square cells can be used, or the
     * individual elements (cells) retrieved and processed as polygons. Each
     * element returns a single polygon with 1 face and 4 vertices. The vertices
     * start at the lower left corner of the cell and run clockwise.
     *
     * @return ElementType XYPolygon
     */
    public IElementSet.ElementType getElementType() {
        return IElementSet.ElementType.XYPolygon;
    }

    /**
     * Gets the number of elements (cells) in the grid.
     *
     * @return int Total number of elements
     */
    public int getElementCount() {
        return colCount * rowCount;
    }

    /**
     * Gets the version number of the GridElementSet implementation.
     *
     * @return int Version number
     */
    public int getVersion() {
        return GridElementSet.GridElementSetVersionNumber;
    }

    /**
     * Converts the elementID string for an element in the element set to its
     * index.
     * 
     * The following mapping is used in this class: Cells are stored
     * from top left to lower right corner. Each cell has an elementID string
     * formatted like "RowNr<RowColumnIDSeperator>ColumnNr" which matches to an
     * index of: rowNr * columnCount + columnNr. Row and Column numbers of cells
     * are 0-based ints.
     *
     * @param elementID ID of element to get index for
     * @return int Index of element with the specified ID
     * @throws ArrayIndexOutOfBoundsException
     */
    public int getElementIndex(String elementID) throws ArrayIndexOutOfBoundsException {
        if (elementID == null) {
            return -1;
        }

        String[] parts = elementID.trim().split(GridElementSet.RowColumnIDSeperator);
        int rowNr = Integer.parseInt(parts[0]);
        int colNr = Integer.parseInt(parts[1]);

        if ((rowNr >= 0) && (rowNr < rowCount) && (colNr >= 0) && (colNr < colCount)) {
            return rowNr * colCount + colNr;
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Gets the ID string for an element in the element set by its index.
     * 
     * The following mapping is used in this class: Cells are stored from top
     * left to lower right corner. Each cell has an ID string formatted like
     * "RowNr<RowColumnIDSeperator>ColumnNr" which matches to an index of:
     * (rowNr - 1) * columnCount + columnNr - 1. Row and Column numbers of cells
     * are 1-based ints.
     *
     * @param elementIndex of element to get ID for
     * @return String ID for the element with the specified index
     */
    public String getElementID(int elementIndex) {
        int row = elementIndex / colCount;
        int col = elementIndex - row * colCount;
        return String.format("%d%s%d", row, GridElementSet.RowColumnIDSeperator, col);
    }

    /**
     * Gets the number of vertices in the element with the specified index. For
     * a valid element index the returned number of vertices will always be 4.
     * These vertices for each element (cell) will start at the cell's lower
     * left corner and run clockwise. The vertice index is 0-based.
     *
     * @param elementIndex of element to get vertice count for
     * @return int Number of vertices (4)
     * @throws ArrayIndexOutOfBoundsException
     */
    public int getVertexCount(int elementIndex) {
        validateElementIndex(elementIndex);
        return 4;
    }

    /**
     * Gets the number of faces in the element with the specified index. For a
     * valid element index the returned number of faces will always be 1. Each
     * element (cell) in the grid contains only a single face.
     *
     * @param elementIndex of element to get face count for
     * @return int Number of faces (1)
     * @throws ArrayIndexOutOfBoundsException
     */
    public int getFaceCount(int elementIndex) {
        validateElementIndex(elementIndex);
        return 1;
    }

    /**
     * Gets the vertice indexes for the face with the specified index in the
     * element with the specified index. For valid indexes the returned array
     * will contain { 0, 1, 2, 3 }, since each element (cell) contains a single
     * face with exactly 4 vertices to describe the square cell polygon.
     *
     * @param elementIndex of element to get vertice indexes for
     * @param faceIndex    of face to get vertice indexes for
     * @return int[] with element face vertices ({0,1,2,3})
     * @throws ArrayIndexOutOfBoundsException
     */
    public int[] getFaceVertexIndices(int elementIndex, int faceIndex) {
        validateElementIndex(elementIndex);
        validateFaceIndex(faceIndex);
        return new int[]{0, 1, 2, 3};
    }

    /**
     * Validates the specified element index and throws an exception when it is
     * out of range.
     *
     * @param index Element index to validate
     * @throws ArrayIndexOutOfBoundsException
     */
    private void validateElementIndex(int index) {
        if ((index < 0) || (index >= getSize())) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Validates the specified face index and throws an exception when it is out
     * of range. Assumes that it concerns a valid element, which for a grid has
     * only 1 face.
     *
     * @param index Face index to validate
     * @throws ArrayIndexOutOfBoundsException
     */
    private void validateFaceIndex(int index) {
        if (index != 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Validates the specified vertice index and throws an exception when it is
     * out of range. Assumes that it concerns a valid element and face of the
     * element. Which for a grid means that there are 4 vertices.
     *
     * @param index Vertice index to validate
     * @throws ArrayIndexOutOfBoundsException
     */
    private void validateVertexIndex(int index) {
        if ((index < 0) || (index > 3)) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Gets the X-coordinate of the specified vertex in the specified element
     * (cell). An exception is thrown when either index is invalid. Vertices of
     * a cell are defined to start at the lower left corner and run in a clock-
     * wise direction.
     *
     * @param elementIndex of element to get vertex information for
     * @param vertexIndex  of vertex to get X-coordinate for
     * @return double X-coordinate of vertex
     * @throws ArrayIndexOutOfBoundsException
     */
    public double getXCoordinate(int elementIndex, int vertexIndex) {
        validateElementIndex(elementIndex);
        validateVertexIndex(vertexIndex);

        int row = elementIndex / colCount;
        int col = elementIndex - row * colCount;
        double cellLowerLeftX = cornerX + col * cellSize;

        if (vertexIndex < 2) {
            return cellLowerLeftX;
        }
        else {
            return cellLowerLeftX + cellSize;
        }
    }

    /**
     * Gets the Y-coordinate of the specified vertex in the specified element
     * (cell). An exception is thrown when either index is invalid. Vertices of
     * a cell are defined to start at the lower left corner and run in a clock-
     * wise direction.
     *
     * @param elementIndex of element to get vertex information for
     * @param vertexIndex  of vertex to get Y-coordinate for
     * @return double Y-coordinate of vertex
     * @throws ArrayIndexOutOfBoundsException
     */
    public double getYCoordinate(int elementIndex, int vertexIndex) {
        validateElementIndex(elementIndex);
        validateVertexIndex(vertexIndex);

        int row = elementIndex / colCount;
        double cellLowerLeftY = cornerY - ((rowCount - row - 1) * cellSize);

        if ((vertexIndex == 0) || (vertexIndex == 3)) {
            return cellLowerLeftY;
        }
        else {
            return cellLowerLeftY - cellSize;
        }
    }

    /**
     * Gets the Z coordinate of the specified vertex in the specified element
     * (cell). An exception is thrown when either index is invalid. Otherwise a
     * value of 0.0 will always be returned for the Z-coordinate.
     *
     * @param elementIndex of element to get vertex information for
     * @param vertexIndex  of vertex to get Z-coordinate for
     * @return double Z-coordinate of vertex (0.0)
     * @throws ArrayIndexOutOfBoundsException
     */
    public double getZCoordinate(int elementIndex, int vertexIndex) {
        validateElementIndex(elementIndex);
        validateVertexIndex(vertexIndex);
        return 0;
    }

    public double getCellSize() {
        return cellSize;
    }

    public int getColCount() {
        return colCount;
    }

    public double getCornerX() {
        return cornerX;
    }

    public double getCornerY() {
        return cornerY;
    }

    public double getNoDataValue() {
        return noDataValue;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getSize() {
        return rowCount * colCount;
    }

    public void setCellSize(double value) {
        cellSize = value;
    }

    public void setCornerX(double value) {
        cornerX = value;
    }

    public void setCornerY(double value) {
        cornerY = value;
    }

    public void setGridSize(int numberOfColumns, int numberOfRows) {
        colCount = numberOfColumns;
        rowCount = numberOfRows;
    }

    public void setNoDataValue(double value) {
        noDataValue = value;
    }

    public boolean describesSameAs(Object obj) {
        if (obj == this) {
            return true;
        }

        if ((obj == null) ||
                (this.getClass() != obj.getClass())) {
            return false;
        }

        GridElementSet s = (GridElementSet) obj;

        if (s.getElementType() != getElementType()) {
            return false;
        }

        if (s.getCellSize() != getCellSize()) {
            return false;
        }

        if (s.getCornerX() != getCornerX()) {
            return false;
        }

        if (s.getCornerY() != getCornerY()) {
            return false;
        }

        if (s.getRowCount() != getRowCount()) {
            return false;
        }

        if (s.getColCount() != getColCount()) {
            return false;
        }

        return true;
    }

}
