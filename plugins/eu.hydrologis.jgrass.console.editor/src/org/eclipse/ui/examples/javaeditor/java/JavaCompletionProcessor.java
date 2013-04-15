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
package org.eclipse.ui.examples.javaeditor.java;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.ui.examples.javaeditor.JavaEditorExamplePlugin;

/**
 * Completion processor for JGrass and GRASS commands.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class JavaCompletionProcessor implements IContentAssistProcessor {

    /**
     * Simple content assist tip closer. The tip is valid in a range of 5 characters around its
     * popup location.
     */
    protected static class Validator
            implements
                IContextInformationValidator,
                IContextInformationPresenter {

        protected int fInstallOffset;

        /*
         * @see IContextInformationValidator#isContextInformationValid(int)
         */
        public boolean isContextInformationValid( int offset ) {
            return Math.abs(fInstallOffset - offset) < 5;
        }

        /*
         * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
         */
        public void install( IContextInformation info, ITextViewer viewer, int offset ) {
            fInstallOffset = offset;
        }

        /*
         * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int,
         *      TextPresentation)
         */
        public boolean updatePresentation( int documentPosition, TextPresentation presentation ) {
            return false;
        }
    }

    /** */
    private final String[] m_proposals;

    protected IContextInformationValidator fValidator = new Validator();

    // Construction
    public JavaCompletionProcessor() {

        super();
        JavaCodeScanner scanner = (JavaCodeScanner) JavaEditorExamplePlugin.getDefault()
                .getJavaCodeScanner();
        Vector<String> proposals = new Vector<String>();
        proposals.addAll(scanner.exchangeKeywords());
        proposals.addAll(scanner.quantityKeywords());
        proposals.addAll(scanner.modelJavaKeywords());
        proposals.addAll(scanner.modelNativeKeywords());
        proposals.addAll(scanner.preprocessorKeywords());
        proposals.addAll(scanner.reservedConstants());
        proposals.addAll(scanner.reservedKeywords());
        proposals.addAll(scanner.reservedTypes());
        proposals.add("--help"); //$NON-NLS-1$
        m_proposals = new String[proposals.size()];
        proposals.toArray(m_proposals);
    } // JavaCompletionProcessor

    // Operations

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public ICompletionProposal[] computeCompletionProposals( ITextViewer viewer, int documentOffset ) {
        // get the word the user is currently writing
        String myWord = null;
        try {
            String text = viewer.getDocument().get(0, documentOffset);
            String[] textSplit = text.split("\\s+"); //$NON-NLS-1$
            myWord = textSplit[textSplit.length - 1];

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        List<ICompletionProposal> props = new ArrayList<ICompletionProposal>();
        for( int i = 0; i < m_proposals.length; i++ ) {
            // pass only those words that start with the letters the user is writing
            if (myWord != null && m_proposals[i].startsWith(myWord))
                props.add(new CompletionProposal(m_proposals[i], documentOffset - myWord.length(),
                        myWord.length(), m_proposals[i].length()));
        }
        return (ICompletionProposal[]) props.toArray(new ICompletionProposal[props.size()]);
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public IContextInformation[] computeContextInformation( ITextViewer viewer, int documentOffset ) {
        return null;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[]{'.', '-'};
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return new char[]{};
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public IContextInformationValidator getContextInformationValidator() {
        return fValidator;
    }

    /*
     * (non-Javadoc) Method declared on IContentAssistProcessor
     */
    public String getErrorMessage() {
        return null;
    }
}
