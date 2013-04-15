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
package eu.hydrologis.jgrass.console.core.runtime.nodes;

import eu.hydrologis.jgrass.console.core.internal.nodes.AST;
import eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAST;
import eu.hydrologis.jgrass.console.core.runtime.analysis.ASTs;

/**
 * <p>The <i>abstract syntax tree</i> root.</p>
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AbstractAST
 * @see eu.hydrologis.jgrass.console.core.internal.nodes.AST
 * @see eu.hydrologis.jgrass.console.core.runtime.analysis.ASTs
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class AST_root
    extends AbstractAST<ASTs>
    implements AST<ASTs> {

// Attributes
	/** AST of construction resp. initialization, called constructors... */
	private AST<ASTs> m_ctor_call_seg;
	
	/** AST of destruction <code>finish</code>, called destructors... */
	private AST<ASTs> m_dtor_call_seg;
	
	/** AST of method calls to <code>getValues()</code> */
	private AST<ASTs> m_gtor_call_seg;
	
	/** AST of construction resp. initialization, called constructors... */
	private AST<ASTs> m_native_call_seg;
	
	/** AST of method calls to <code>prepare()</code> */
	private AST<ASTs> m_ptor_call_seg;
	
	/** AST of variable declarations... */
	private AST<ASTs> m_vdefs_seg;
	
// Construction
	/**
	 * <p>Constructs this object.</p>
	 */
	public AST_root() {
		
		super( ASTs.AST_ROOT.expression()
				, ASTs.AST_ROOT.annotation()
				, ASTs.AST_ROOT
			);
		__initialize();
	} // AST_root
	
	/**
	 * <p>Constructs this object with the specified operands.</p>
	 * @param operands
	 * 		- can be <code>null</code> or either a single operand or a list of
	 * 		operands.
	 */
	public AST_root( AST<ASTs>... operands ) {
	
		super( ASTs.AST_ROOT.expression()
				, ASTs.AST_ROOT.annotation()
				, ASTs.AST_ROOT
				, operands
			);
		__initialize();
	} // AST_root
	
	/**
	 * <p>Helps to initialize this root at construction time.</p>
	 */
	@SuppressWarnings("unchecked")
    private void __initialize() {
		
		m_dtor_call_seg   = new AST_slist();
		m_ctor_call_seg   = new AST_slist();
		m_gtor_call_seg   = new AST_slist();
		m_native_call_seg = new AST_slist();
		m_ptor_call_seg   = new AST_slist();
		m_vdefs_seg       = new AST_slist();
		
		addChild( m_vdefs_seg );
		addChild( m_ctor_call_seg );
		addChild( m_ptor_call_seg );
		addChild( m_gtor_call_seg );
		addChild( m_dtor_call_seg );
		addChild( m_native_call_seg );
	} // __initialize
	
// Operations
	/**
	 * <p>Returns the segment construction.</p>
	 */
	public AST<ASTs> __ctor_call_seg() {
		
		return m_ctor_call_seg;
	} // __ctor_call_seg
	
	/**
	 * <p>Returns the segment for destruction.</p>
	 */
	public AST<ASTs> __dtor_call_seg() {
		
		return m_dtor_call_seg;
	} // __dtor_call_seg
	
	/**
	 * <p>Returns the segment for calling the <code>getValues</code> method of
	 * a linkable component.</p>
	 */
	public AST<ASTs> __gtor_call_seg() {
		
		return m_gtor_call_seg; 
	} // __gtor_call_seg
	
	/**
	 * <p>Returns the segment for native calls.</p>
	 */
	public AST<ASTs> __native_call_seg() {
		
		return m_native_call_seg;
	} // __native_call_seg
	
	/**
	 * <p>Returns the segment for calling the <code>prepare</code> method of
	 * a linkable component.</p>
	 */
	public AST<ASTs> __ptor_call_seg() {
		
		return m_ptor_call_seg;
	} // __ptor_call_seg
	
	/**
	 * <p>Returns the segment for variable declarations.</p>
	 */
	public AST<ASTs> __vdefs_seg() {
		
		return m_vdefs_seg;
	} // __vdefs_seg
		
} // AST_root
