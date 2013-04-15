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
package eu.hydrologis.jgrass.console.core.internal.analysis;

/**
 * <p>The interface <code>MessageCode</code> declares the named constants for
 * parsing decisions during the analysis phase.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public enum MessageCode {

// Attributes
	/**
	 * <p><code>SUCCESS</code> indicates a successful completion of the
	 * analysis procedure - no message.</p>
	 */
	SUCCESS
	
	/**
	 * <p><code>CONTINUE</code> indicates success but to ignore all
	 * involved arguments of the current analysis - no message.</p>
	 */
	, CONTINUE
	
	/**
	 * <p><code>IGNORE_AND_CONTINUE</code> indicates success but to ignore all
	 * involved arguments of the current analysis thread - no message.</p>
	 */
	, IGNORE_AND_CONTINUE
	
	;
	
} // MessageCode
