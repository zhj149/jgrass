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
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.javaeditor.javadoc;


import java.util.Vector;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.ui.examples.javaeditor.JavaEditorExamplePlugin;
import org.eclipse.ui.examples.javaeditor.java.JavaCodeScanner;

/**
 * Example Java doc completion processor.
 */
public class JavaDocCompletionProcessor implements IContentAssistProcessor {

// Attributes
	/** */
	private final String[] m_proposals;

// Construction
	public JavaDocCompletionProcessor() {
	
		super();
		JavaCodeScanner scanner = ( JavaCodeScanner )
		JavaEditorExamplePlugin.getDefault().getJavaCodeScanner();
		Vector<String> proposals = new Vector<String>();
			proposals.addAll( scanner.annotationKeywords() );
		m_proposals = new String[ proposals.size() ];
		proposals.toArray( m_proposals );
	} // JavaDocCompletionProcessor
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		ICompletionProposal[] result= new ICompletionProposal[m_proposals.length];
		for (int i= 0; i < m_proposals.length; i++)
			result[i]= new CompletionProposal(m_proposals[i], documentOffset, 0, m_proposals[i].length());
		return result;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		return null;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
	
	/* (non-Javadoc)
	 * Method declared on IContentAssistProcessor
	 */
	public String getErrorMessage() {
		return null;
	}
}
