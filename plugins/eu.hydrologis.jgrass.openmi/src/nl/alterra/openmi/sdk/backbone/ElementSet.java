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

import org.openmi.standard.IElementSet;
import org.openmi.standard.ISpatialReference;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * The ElementSet class describes a collection of spatial elements.
 */
public class ElementSet extends BackboneObject implements IElementSet, Serializable {

    /**
     * Default static ID-based ElementSet.
     */
    public static final ElementSet DEFAULT = new ElementSet("default", "DefESId", ElementType.IDBased, null);
    
    private ArrayList<Element> elements;
    private ElementType elementType;
    private ISpatialReference spatialReference;

    /**
     * Creates an instance with default values.
     */
    public ElementSet() {
        this("", "", ElementType.XYPoint, new SpatialReference(""));
    }

    /**
     * Creates an instance and copy values of the specified instance.
     *
     * @param source The IElementSet to copy from
     */
    public ElementSet(IElementSet source) {
        this(source.getDescription(), source.getID(), source.getElementType(), source.getSpatialReference());

        // can copy faster with some inside knowledge
        if (source instanceof ElementSet) {
            elements = (ArrayList<Element>) ((ElementSet) source).elements.clone();
        } else {
            for (int i = 0; i < source.getElementCount(); i++) {
                Element element = new Element(source.getElementID(i));
                for (int j = 0; j < source.getVertexCount(i); j++) {
                    double x = source.getXCoordinate(i, j);
                    double y = source.getYCoordinate(i, j);
                    double z = source.getZCoordinate(i, j);

                    element.addVertex(new Vertex(x, y, z));
                }
                addElement(element);
            }
        }
    }

    /**
     * Creates an instance with the specified values.
     *
     * @param description      The description
     * @param ID               The ID
     * @param elementType      The ElementType
     * @param spatialReference The ISpatialReference
     */
    public ElementSet(String description, String ID, ElementType elementType,
            ISpatialReference spatialReference) {
        elements = new ArrayList<Element>();
        this.setDescription(description);
        this.setID(ID);
        this.elementType = elementType;
        this.spatialReference = spatialReference;
    }

    /**
     * Gets an element.
     *
     * @param index Index for element to get
     * @return The selected element
     */
    public Element getElement(int index) {
        return elements.get(index);
    }

    /**
     * Gets a copy of the elements as an array.
     *
     * @return Returns the elements
     */
    public Element[] getElements() {
        return elements.toArray(new Element[0]);
    }

    /**
     * Sets the elements from the contents of an array.
     *
     * @param values The elements to set
     */
    public void setElements(Element[] values) {
        elements.clear();
        for (Element e : values) {
            elements.add(e);
        }
    }

    /**
     * Gets the type of elements.
     *
     * @return The type of elements
     */
    public ElementType getElementType() {
        return elementType;
    }

    /**
     * Sets the type of elements.
     *
     * @param elementType The elementType to set
     */
    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    /**
     * Returns the ID for the element at the given index.
     *
     * @param index The index for the element
     * @return The ID string of the element
     */
    public String getElementID(int index) {
        return (elements.get(index)).getID();
    }

    /**
     * Gets the spatial reference.
     *
     * @return The spatial reference
     */
    public ISpatialReference getSpatialReference() {
        return spatialReference;
    }

    /**
     * Sets the spatial reference.
     *
     * @param spatialReference The spatialReference to set
     */
    public void setSpatialReference(ISpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    /**
     * Gets the X coordinate for a certain element and vertex.
     *
     * @param elementIndex Index for the element
     * @param vertexIndex  Index for the vertex
     * @return X coordinate
     */
    public double getXCoordinate(int elementIndex, int vertexIndex) {
        return elements.get(elementIndex).getVertex(vertexIndex).getX();
    }

    /**
     * Gets the Y coordinate for a certain element and vertex.
     *
     * @param elementIndex Index for the element
     * @param vertexIndex  Index for the vertex
     * @return Y coordinate
     */
    public double getYCoordinate(int elementIndex, int vertexIndex) {
        return elements.get(elementIndex).getVertex(vertexIndex).getY();
    }

    /**
     * Gets the Z coordinate for a certain element and vertex.
     *
     * @param elementIndex Index for the element
     * @param vertexIndex  Index for the vertex
     * @return Y coordinate
     */
    public double getZCoordinate(int elementIndex, int vertexIndex) {
        return elements.get(elementIndex).getVertex(vertexIndex).getZ();
    }

    /**
     * Returns the number of elements.
     *
     * @return Number of elements
     */
    public int getElementCount() {
        return elements.size();
    }

    /**
     * Returns the number of vertices for an element.
     *
     * @param index Index of the element
     * @return Number of vertices in the element
     */
    public int getVertexCount(int index) {
        Element element = elements.get(index);
        return element.getVertexCount();
    }

    /**
     * Returns the element index for a given element ID.
     *
     * @param elementID The ID of the element
     * @return The index of the element with the specified ID, -1 when not found
     * @throws Exception when no match was found
     */
    public int getElementIndex(String elementID) {
        for (Element e : elements) {
            if (elementID.equals(e.getID())) {
                return elements.indexOf(e);
            }
        }
        return -1;
    }

    /**
     * Gets the version of the ElementSet.
     *
     * @return Integer version number
     */
    public int getVersion() {
        return 0;
    }

    /**
     * Returns the number of faces for a given element.
     *
     * @param index Index for the element
     * @return The number of faces
     */
    public int getFaceCount(int index) {
        return (elements.get(index)).getFaceCount();
    }

    /**
     * Returns the list of face vertex indices for a given element and face.
     *
     * @param elementIndex The index for the element
     * @param faceIndex    The index for the face
     * @return Integer array with the face vertex indices
     */
    public int[] getFaceVertexIndices(int elementIndex, int faceIndex) {
        return (elements.get(elementIndex)).getFaceVertexIndices(faceIndex);
    }

    /**
     * Adds an element.
     *
     * @param element Element to be added
     */
    public void addElement(Element element) {
        elements.add(element);
    }

    @Override
    public boolean describesSameAs(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!super.describesSameAs(obj)) {
            return false;
        }

        ElementSet s = (ElementSet) obj;

        if (!this.spatialReference.equals(s.spatialReference)) {
            return false;
        }

        if (!this.elementType.equals(s.elementType)) {
            return false;
        }

        return elements.equals(s.elements);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + getDescription().hashCode() + getID().hashCode() +
                (getSpatialReference() != null ? getSpatialReference().hashCode() : 0) + getElementType().hashCode() +
                elements.hashCode();
    }

}
