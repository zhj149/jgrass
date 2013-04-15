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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The element class contains a spatial element.
 */
public class Element implements Serializable {

    private Vertices vertices;
    private ArrayList<int[]> faces;
    private String ID;

    /**
     * Creates an Element with an empty ID.
     */
    public Element() {
        this("");
    }

    /**
     * Creates an Element as a (shallow) copy of another element.
     *
     * @param source The element to copy
     */
    public Element(Element source) {
        this(source.getID());
        vertices = source.getVertices();
    }

    /**
     * Creates an Element with the specified ID.
     *
     * @param ID The element ID
     */
    public Element(String ID) {
        this.ID = ID;

        vertices = new Vertices();
        faces = new ArrayList<int[]>();
    }

    /**
     * Gets the vertices collection.
     *
     * @return the Vertices
     */
    public Vertices getVertices() {
        return vertices;
    }

    /**
     * Sets the vertices collection to the specified one.
     *
     * @param vertices Vertices collection to set
     */
    public void setVertices(Vertices vertices) {
        this.vertices = vertices;
    }

    /**
     * Gets the element ID.
     *
     * @return element ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Sets the element ID.
     *
     * @param ID The ID
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * Returns the vertex for a given index.
     *
     * @param index Index of the vertex to return
     * @return The vertex
     */
    public Vertex getVertex(int index) {
        return vertices.get(index);
    }

    /**
     * Returns the number of vertices.
     *
     * @return Number of vertices
     */
    public int getVertexCount() {
        return vertices.size();
    }

    /**
     * Adds a vertex.
     *
     * @param vertex The vertex to be added
     */
    public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    /**
     * Gets the number of faces in the element.
     *
     * @return Number of faces
     */
    public int getFaceCount() {
        return faces.size();
    }

    /**
     * Adds a face to the element.
     *
     * @param vertexIndices The vertex indices for the face
     */
    public void addFace(int[] vertexIndices) {
        faces.add(vertexIndices);
    }

    /**
     * Gets the face vertex indices array for a given face index.
     *
     * @param faceIndex The index of the desired face
     * @return The vertex indices for the face
     */
    public int[] getFaceVertexIndices(int faceIndex) {
        return faces.get(faceIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        Element e = (Element) obj;

        if (!this.getID().equals(e.getID())) {
            return false;
        }

        if (!this.getVertices().equals(e.getVertices())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + getID().hashCode() + getVertices().hashCode();
    }

}
