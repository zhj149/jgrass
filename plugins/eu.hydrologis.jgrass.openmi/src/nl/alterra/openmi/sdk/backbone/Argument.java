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

/**
 * Argument is a class that contains (key, value) pairs.
 */
public class Argument extends BackboneObject implements IArgument {

    private final static String EXCEPTION_READ_ONLY = "%s %s is marked as " +
        "readSystemDeployer only and can not be changed";
    protected String value = "";
    protected String key = "";
    protected boolean readOnly = false;

    /**
     * Creates an instance. The ID, caption and description are all set to an
     * empty string. As will the value and the key. By default the argument
     * will not be write protected.
     */
    public Argument() {
        this("", "", false, "");
    }

    /**
     * Creates an instance with the specified values.
     *
     * @param value
     * @param key
     * @param readOnly
     */
    public Argument(String key, String value, boolean readOnly) {
        this(key, value, readOnly, "");
    }

    /**
     * Creates an instance with the specified values.
     *
     * @param value
     * @param key
     * @param readOnly
     * @param description
     */
    public Argument(String key, String value, boolean readOnly, String description) {
        this.value = value;
        this.key = key;
        setDescription(description);
        this.readOnly = readOnly;
    }

    /**
     * Sets the key of the argument.
     *
     * @param key String key to set
     */
    public void setKey(String key) {
        if (!isReadOnly()) {
            this.key = key;
        } else {
            readOnlyException();
        }
    }

    /**
     * Sets the read-only state of the argument. If read-only is set to true
     * it will no longer allow changes to its properties.
     *
     * @param readOnly The read-only state
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Sets the description for the argument.
     *
     * @param description String description to set
     */
    @Override
    public void setDescription(String description) {
        if (!isReadOnly()) {
            super.setDescription(description);
        } else {
            readOnlyException();
        }
    }

    @Override
    public void setID(String id) {
        if (!isReadOnly()) {
            super.setID(id);
        } else {
            readOnlyException();
        }
    }

    @Override
    public void setCaption(String caption) {
        if (!isReadOnly()) {
            super.setCaption(caption);
        } else {
            readOnlyException();
        }
    }

    /**
     * Checks if key value equals the given value.
     *
     * @param key String to check
     * @return True if values are equal
     */
    public boolean equalsKey(String key) {
        return key.equals(this.key);
    }

    /**
     * Checks if "value" value equals the given value.
     *
     * @param value String to check
     * @return True if values are equal
     */
    public boolean equalsValue(String value) {
        return value.equals(getValue());
    }

    /**
     * Checks if key and "value" values are equal to the given values.
     *
     * @param key   String to check for key
     * @param value String to check for value
     * @return True if both values are equal
     */
    public boolean equalsKeyAndValue(String key, String value) {
        return equalsKey(key) && equalsValue(value);
    }

    @Override
    public boolean describesSameAs(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!super.describesSameAs(obj)) {
            return false;
        }

        return ((Argument) obj).equalsKeyAndValue(this.getKey(), this.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        Argument a = (Argument) obj;

        return (a.isReadOnly() == isReadOnly());
    }

    @Override
    public int hashCode() {
        return super.hashCode() + value.hashCode() + key.hashCode() + 
            Boolean.valueOf(readOnly).hashCode();
    }

    /**
     * Copies key, ObjectValue, description and readOnly property from the
     * specified argument.
     *
     * @param argument The IArgument to copy values from
     */
    public void assignFrom(IArgument argument) {
        if (argument != null) {
            setKey(argument.getKey());
            setDescription(argument.getDescription());
            setValue(argument.getValue());
            setReadOnly(argument.isReadOnly());
        }
    }

    /**
     * Gets the argument value ('Value' in: Key=Value pair).
     *
     * @return Value
     */
    public String getValue() {
        return value.toString();
    }

    /**
     * Sets the value of the argument.
     *
     * @param value The value to set
     */
    public void setValue(String value) {
        if (!isReadOnly()) {
            this.value = value;
        } else {
            readOnlyException();
        }
    }

    /**
     * Gets the key of the argument.
     *
     * @return Returns the key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the ReadOnly state of the argument. If read-only is true, changes
     * to the Key, value and description are not allowed.
     *
     * @return ReadOnly state.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Throws a (unchecked) runtime exception as indication that an attempt
     * is being made to change an argument when it is marked read-only.
     * Using an unchecked exception avoids that it has to be declared and
     * tested in every use of a set method.
     */
    protected void readOnlyException() {
        throw new RuntimeException(String.format(EXCEPTION_READ_ONLY, 
            this.getClass().getSimpleName(), this.getKey()));
    }

}
