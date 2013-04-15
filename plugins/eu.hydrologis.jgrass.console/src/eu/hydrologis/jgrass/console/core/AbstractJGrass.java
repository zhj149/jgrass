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
package eu.hydrologis.jgrass.console.core;

import eu.hydrologis.jgrass.console.core.prefs.Projectspace;

/**
 * <p>The class <code>AbstractJGrass</code> is an extend to the interface
 * <code>JGrass</code>. This abstract class provides the static functionality
 * to get the current running <i>JGRASS</i> <b>console engine</b> and to get
 * the group of running compiler/interpreter threads. However, the developer
 * need only subclass this abstract class and defines the <code>dispatch</code>
 * method.</p>
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractJGrass
	implements JGrass {

// Attributes
	/** The one and only <code>JGrass</code> console instance. */
	private static JGrass m_instance = null;
	
	/** The group of running compiler/interpreter threads. */
	private static ThreadGroup m_threadGroup = null;
	
// Construction
	/**
	 * <p>The copy constructor <code>AbstractJGrass</code> creates this
	 * <i>JGRASS console</i> with the specified program arguments.</p>
	 * @param args
	 *  	The parameter <code>args</code> holds the optional arguments in an
	 *  	array of <code>String</code> passed through the Java Virtual
	 *  	Machine command line. The value of the parameter <code>args</code>
	 *  	can be <code>null</code>.
	 */
	public AbstractJGrass( String[] args ) {
		
		super();
		try {
			
			if( null == m_threadGroup )
				m_threadGroup = new ThreadGroup(
						"AbstractJGrass::internal_root" //$NON-NLS-1$
					);
		}
		catch( Exception e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			e.printStackTrace();
		}
		finally {
			
			if( null == m_instance )
				m_instance = this;
		}
	} // AbstractJGrass
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		
		try {
			
			if( null != m_threadGroup && 0 < m_threadGroup.activeCount() )
				m_threadGroup.destroy();
			
			m_threadGroup = null;
			m_instance = null;
		}
		catch( Exception e ) {
			
			if( true == Projectspace.isErrorEnabled() )
				System.out.println( e );
			
			e.printStackTrace();
		}
		finally {
			
			try {
				
				super.finalize();
			}
			catch( Throwable e ) {
				
				if( true == Projectspace.isErrorEnabled() )
					System.out.println( e );
				
				throw e;
			}
		}
	} // finalize
	
// Operations
	/**
	 * <p>The method <code>getInstance</code> returns the current running
	 * <code>JGrass</code> console instance, if any exists, otherwise
	 * <code>null</code></p>
	 * @return
	 * 		Returns the reference to the <code>JGrass</code> console
	 * 		instance, if any exists, otherwise <code>null</code>.
	 */
	public static JGrass getInstance() {
		
		return m_instance;
	} // getInstance
	
	/**
	 * <p>Returns the associated group of running compiler/interpreter
	 * threads.</p>
	 */
	public static ThreadGroup getThreadGroup() {
		
		return m_threadGroup;
	} // getThreadGroup

} // AbstractJGrass
