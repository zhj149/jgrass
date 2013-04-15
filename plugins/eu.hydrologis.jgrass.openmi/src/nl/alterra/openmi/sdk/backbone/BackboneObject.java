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

import java.util.Observable;

/**
 * Abstract base class for classes in the OpenMI backbone, introducing some
 * properties (e.g. id, caption, description).
 */
public abstract class BackboneObject extends Observable {

    /**
     * Give every instance a unique ID.
     * Id refers to unique real world object (default empty string)
     */
    private String id = "";

    /**
     * The caption is the screen name for a business object. This should not
     * be used as an ID or anything meaningful, it is only for user convenience
     * and subject to localization.
     */
    private String caption = "";

    /**
     * The description reserves room on every business object to provide some
     * textual documentation for it.
     */
    private String description = "";

    /**
     * Creates an instance. The ID, caption and description are all set to an
     * empty string.
     */
    public BackboneObject() {
        this("");
    }

    /**
     * Creates an instance with the specified ID. The ID is used as the
     * initial caption as well.
     *
     * @param id String ID
     */
    public BackboneObject(String id) {
        this.id = id;
        setCaption(id);
    }

    /**
     * Gets the ID of the object.
     *
     * @return String ID
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the ID of the instance to the specified value. Changing the ID is
     * not encouraged!
     *
     * @param id String ID to set
     */
    public void setID(String id) {
        if (id == null) {
            id = "";
        }

        if (!id.equals(this.id)) {
            this.id = id;
            setChanged();
        }
    }

    /**
     * Gets the description of the object. This is like an extended caption,
     * but might not be editable all the time by the user.
     *
     * @return String Description of the object
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the object. This is like an extended caption,
     * but might not be editable all the time by the user.
     *
     * @param description The new description
     */
    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }

        if (!description.equals(this.description)) {
            this.description = description;
            setChanged();
        }
    }

    /**
     * Gets the caption of the object. The caption is typically used for
     * display in an user interface, and the user might be allowed to change
     * it at will. So it is best to not rely on it to have a specific value.
     *
     * @return String The caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Sets the caption of the object.
     *
     * @param caption The new caption
     */
    public void setCaption(String caption) {
        if (caption == null) {
            caption = "";
        }

        if (!caption.equals(this.caption)) {
            if (description.equals(this.caption)) {
                setDescription(caption);
            }
            this.caption = caption;
            setChanged();
        }
    }

    /**
     * Internally used to set the changed flag of this observable, followed
     * by notifying the registered observers.
     */
    @Override
    protected synchronized void setChanged() {
        super.setChanged();
        notifyObservers();
    }

    /**
     * Tests if all relevant field values are congruent. Member fields that
     * have a meaning for proper functioning of the application rather than
     * describing a "real world" property should not be included in the test.
     * 
     * The tested object therefore does not need to be the same instance.
     * Hint: override this method in each subclass!
     *
     * @param obj to compare with
     * @return true if object is congruent
     */
    public boolean describesSameAs(Object obj) {
        if ((obj == null) ||
                (this.getClass() != obj.getClass())) {
            return false;
        }

        // abstract default: no fields are compared so always true
        return true;
    }

    /**
     * Tests if all field values of this object are equal (using equals
     * calls) to the specified object. Which has to be of the same class
     * type. First a call to describesSameAs() is made, after which all
     * remaining fields that are not tested there are compared.
     *
     * @param obj The object to compare with
     * @return True if all fields are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (!describesSameAs(obj)) {
            return false;
        }

        BackboneObject v = (BackboneObject) obj;

        if (!(v.id.equals(id))) {
            return false;
        }

        if (!(v.caption.equals(caption))) {
            return false;
        }

        if (!(v.description.equals(description))) {
            return false;
        }

        return true;
    }

    /**
     * Returns an integer hash code for the object based on its fields.
     *
     * @return int Hashcode
     */
    @Override
    public int hashCode() {
        return super.hashCode() + id.hashCode() + caption.hashCode() + 
            description.hashCode();
    }

    /**
     * Checks if two objects are equal, taking into account that one or both
     * can be a null reference. When both objects are not null references they
     * are checked using the equals() method, otherwise the references are
     * compared with "==".
     *
     * @param obj1 The first object
     * @param obj2 The second object
     * @return True when objects are considered equal
     */
    protected static boolean nullEquals(Object obj1, Object obj2) {
        if ((obj1 != null) && (obj2 != null)) {
            return obj1.equals(obj2);
        } else {
            return (obj1 == obj2);
        }
    }

    @Override
    public String toString() {
        if (id.length() > 0) {
            return String.format("%s (%s)", id, caption.length() > 0 ? caption : "no caption");
        } else {
            return super.toString();
        }
    }

}
