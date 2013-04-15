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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public class JavaColorProvider {

// Attributes
	/** */
	private final static String m_szMsgFmtRedGreenBlueToString = "{0},{1},{2}"; //$NON-NLS-1$
		
	/** Maps a unique string expression to a single color. */
	private HashMap<String, Color> m_colorAssocMap;
	
	/** Maps a unique string expression to a single color. */
	private HashMap<String, Color> m_rgbAssocMap;
	
// Construction
	/** */
	public JavaColorProvider() {
		
		super();
		m_colorAssocMap = new HashMap<String, Color>();
		m_rgbAssocMap = new HashMap<String, Color>();
	} // JavaColorProvider
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	
	protected void finalize() throws Throwable {
		
		
		try {
			
			dispose();
		}
		catch( Exception e ) {
			
			e.printStackTrace();
		}
		finally {
			
			try {
				
				super.finalize();
			}
			catch( Throwable e ) {
				
				throw e;
			}
		}
	} // finalize
	
// Operations
	/** */
	private static RGB toRGB( String rgb, RGB defaultval ) {
		
		final RGB retval;
		if( null == rgb ) {
			
			retval = defaultval;
		}
		else {
			
			String[] splited = rgb.split( "," );
			if( 3 > splited.length ) {
				
				retval = defaultval;
			}
			else {
			
				RGB __rgb = null;
				try {
					
					__rgb = new RGB(
							Integer.parseInt( splited[ 0 ].trim() )
							, Integer.parseInt( splited[ 1 ].trim() )
							, Integer.parseInt( splited[ 2 ].trim() )
						);
				}
				catch( NumberFormatException e ) {
				
					__rgb = defaultval;
				}
				finally {
					
					retval = __rgb;
				}
			}
		}
		
		return retval;
	} // toRGB
	
	/** */
	public static String toString( RGB rgb ) {
		
		return MessageFormat.format(
				m_szMsgFmtRedGreenBlueToString
				, new Object[] {
						rgb.red
						, rgb.green
						, rgb.blue
					}
			);
	} // toString
	
	/** */
	public void add( String string, RGB rgb ) {
		
		if( true == m_colorAssocMap.containsKey( string ) )
			m_colorAssocMap.remove( string );
		
		m_colorAssocMap.put( string, getColor( rgb ) );
	} // addColor
	
	/** */
	public void add( String string, String rgb ) {
		
		add( string, JavaColorProvider.toRGB( rgb, null ) );
	} // addColor
	
	/** */
	public void dispose() {
		
		try {
			
			Iterator<Color> iterator = m_rgbAssocMap.values().iterator();
			while( true == iterator.hasNext() )
				(( Color )iterator.next()).dispose();
		}
		catch( Exception e ) {
			
			e.printStackTrace();
		}
		finally {
			
			m_colorAssocMap.clear();
			m_rgbAssocMap.clear();
		}
	} // dispose
	
	/** */
	public Color getColor( RGB rgb ) {
		
		Color retval = m_rgbAssocMap.get( JavaColorProvider.toString( rgb ) );
		if( null == retval )
			m_rgbAssocMap.put(
					JavaColorProvider.toString( rgb )
					, retval = new Color( Display.getCurrent(), rgb )
				);
		
		return retval;
	} // getColor
	
	/** */
	public Color getColor( String string ) {
		
		if( true == m_colorAssocMap.containsKey( string ) )
			return m_colorAssocMap.get( string );
		
		return getColor( JavaColorProvider.toRGB( string, null ) );
	} // getColor
	
} // JavaColorProvider
