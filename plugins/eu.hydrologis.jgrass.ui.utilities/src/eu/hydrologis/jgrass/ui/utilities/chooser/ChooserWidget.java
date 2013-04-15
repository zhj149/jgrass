/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package eu.hydrologis.jgrass.ui.utilities.chooser;

public interface ChooserWidget {

    public static final int BUTTONWIDTHHINT = 150;
    public static final int LABELWIDTHHINT = 200;
    public static final int TEXTFIELDWIDTHHINT = 200;
    /**
     * Return the String representation of the choosen object. This could be a path for a file.
     * 
     * @return the string representation of the choosen object
     */
    public String getString();

    /**
     * Return the the choosen object. This could be a Service for a JGrass location.
     * 
     * @return the choosen object
     */
    public Object getObject();
}
