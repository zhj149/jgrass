/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.eclipse.ui.examples.javaeditor.util;

import org.eclipse.jface.text.rules.IWordDetector;

public class ExchangeWordDetector
    implements IWordDetector {

// Attributes
	/** */
	private int m_othersCount;
	
// Construction
	public ExchangeWordDetector() {
		
		m_othersCount = 0;
	} // ExchangeWordDetector
	
// Operations
	/*(non-Javadoc)
	 * Method declared on IWordDetector.
	 */
	public boolean isWordPart( char character ) {

		final boolean retval;
		if( '-' == character && 0 == m_othersCount ) {
			
			retval = true;
		}
		else if( '*' == character && 0 != m_othersCount ) {
			
			retval = true;
		}
		else if( '-' == character ) {
			
			retval = false;
		}
		else {
			
			retval = Character.isJavaIdentifierPart( character );
			++m_othersCount;
		}

		return retval;
	} // isWordPart

	/*(non-Javadoc)
	 * Method declared on IWordDetector.
	 */
	public boolean isWordStart( char character ) {

		m_othersCount = 0;
		switch( character ) {
		case '-':
			return true;
			
		default:
			return false;
		}
	} // isWordStart
	
} // ExchangeWordDetector
