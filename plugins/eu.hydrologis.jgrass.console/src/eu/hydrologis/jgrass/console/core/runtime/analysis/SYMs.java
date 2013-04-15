/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) C.U.D.A.M. Universita' di Trento
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
package eu.hydrologis.jgrass.console.core.runtime.analysis;

/**
 * <p>The enumeration type <code>SYMs</code> defines the named constants to
 * identify the data type of a symbol in the symbol table by its named
 * constant - this is the symbols <i>identity scope</i> - instead to identify
 * the type using the <code>instanceof</code> operator.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symtable
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.Symbol
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public enum SYMs {
	
	/**
	 * <p>The <code>SYM_CONSTANT_VALUE</code> identifies a symbole as a named
	 * constant value.</p>
	 */
	SYM_CONSTANT_VALUE,
	
	/**
	 * <p>The <code>SYM_TYPE_ARRAY</code> identifies a symbole as an array data
	 * type.</p>
	 */
	SYM_TYPE_ARRAY,
	
	/**
	 * <p>The <code>SYM_TYPE_CLASS</code> identifies a symbole as a
	 * <code>class</code> reference data type.</p>
	 */
	SYM_TYPE_CLASS,
	
	/**
	 * <p>The <code>SYM_TYPE_JAVA_MODEL</code> identifies a symbole as a java
	 * model reference data type.</p>
	 */
	SYM_TYPE_JAVA_MODEL,
	
	/**
	 * <p>The <code>SYM_TYPE_LONG</code> identifies a symbole as a primitive 
	 * numeric long data type.</p>
	 */
	SYM_TYPE_LONG,
	
	/**
	 * <p>The <code>SYM_TYPE_NATIVE_MODEL</code> identifies a symbole as a
	 * native model primitive data type.</p>
	 */
	SYM_TYPE_NATIVE_MODEL;
	
} // SYMs
