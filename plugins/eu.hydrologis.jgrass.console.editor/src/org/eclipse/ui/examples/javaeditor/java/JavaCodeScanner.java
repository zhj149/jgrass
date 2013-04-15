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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.ui.examples.javaeditor.util.JavaColorProvider;
import org.eclipse.ui.examples.javaeditor.util.ExchangeWordDetector;
import org.eclipse.ui.examples.javaeditor.util.JavaWhitespaceDetector;
import org.eclipse.ui.examples.javaeditor.util.JavaWordDetector;

import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;

/**
 * A Java code scanner.
 */
public class JavaCodeScanner extends RuleBasedScanner {

    // Attributes
    /** */
    private Vector<String> m_annotationKeywords;

    /** */
    private Vector<String> m_exchangeKeywords;

    /** */
    private Vector<String> m_modelJavaKeywords;

    /** */
    private Vector<String> m_modelNativeKeywords;

    /** */
    private Vector<String> m_preprocessorKeywords;

    /** */
    private Vector<String> m_quantityKeywords;

    /** */
    private Vector<String> m_reservedConstants;

    /** */
    private Vector<String> m_reservedKeywords;

    /** */
    private Vector<String> m_reservedTypes;

    /** */
    private IToken m_tokenAsteriskKeyword;

    /** */
    private IToken m_tokenParametersKeyword;

    /** */
    private IToken m_tokenComment;

    /** */
    private IToken m_tokenConstant;

    /** */
    private IToken m_tokenExchangeInputKeyword;

    /** */
    private IToken m_tokenExchangeOutputKeyword;

    /** */
    private IToken m_tokenKeyword;

    /** */
    private IToken m_tokenModelKeyword;

    /** */
    private IToken m_tokenOther;

    /** */
    private IToken m_tokenPreprocessorKeyword;

    /** */
    private IToken m_tokenQuantityKeyword;

    /** */
    private IToken m_tokenString;

    /** */
    private IToken m_tokenType;

    // Construction
    /**
     * Creates a Java code scanner with the given color provider.
     * 
     * @param provider the color provider
     */
    @SuppressWarnings("unchecked")
    public JavaCodeScanner( JavaColorProvider provider ) {

        ProjectOptions options = new ProjectOptions();
        PreferencesInitializer.initialize(options);
        Projectspace projectSpace = new Projectspace();
        projectSpace.initialize(options);

        __initializeWords(projectSpace);
        __initializeTokens(provider);

        List rules = new ArrayList();

        // Add rule for single line comments.
        rules.add(new EndOfLineRule("#", m_tokenParametersKeyword)); //$NON-NLS-1$
        rules.add(new EndOfLineRule("//", m_tokenComment)); //$NON-NLS-1$
        rules.add(new MultiLineRule("/**", "*/", m_tokenComment)); //$NON-NLS-2$ //$NON-NLS-1$
        rules.add(new MultiLineRule("/*", "*/", m_tokenComment)); //$NON-NLS-2$ //$NON-NLS-1$

        // Add rule for strings and character constants.
        rules.add(new SingleLineRule("\"", "\"", m_tokenString, '\\')); //$NON-NLS-2$ //$NON-NLS-1$
        rules.add(new SingleLineRule("'", "'", m_tokenString, '\\')); //$NON-NLS-2$ //$NON-NLS-1$

        // Add generic whitespace rule.
        rules.add(new WhitespaceRule(new JavaWhitespaceDetector()));

        // Add word rule for keywords, types, and constants.
        WordRule wordRule = new WordRule(new JavaWordDetector(), m_tokenOther);

        for( int i = 0; i < m_reservedKeywords.size(); ++i )
            wordRule.addWord(m_reservedKeywords.get(i), m_tokenKeyword);

        for( int i = 0; i < m_preprocessorKeywords.size(); ++i )
            wordRule.addWord(m_preprocessorKeywords.get(i), m_tokenPreprocessorKeyword);

        for( int i = 0; i < m_reservedTypes.size(); ++i )
            wordRule.addWord(m_reservedTypes.get(i), m_tokenType);

        for( int i = 0; i < m_reservedConstants.size(); ++i )
            wordRule.addWord(m_reservedConstants.get(i), m_tokenConstant);

        for( int i = 0; i < m_modelJavaKeywords.size(); ++i )
            wordRule.addWord(m_modelJavaKeywords.get(i), m_tokenModelKeyword);
        for( int i = 0; i < m_modelJavaKeywords.size(); ++i ) {

            String candidate = m_modelJavaKeywords.get(i);
            if (true == candidate.endsWith("*")) //$NON-NLS-1$
                wordRule.addWord(candidate, m_tokenAsteriskKeyword);
            else
                wordRule.addWord(candidate, m_tokenModelKeyword);
        }

        for( int i = 0; i < m_modelNativeKeywords.size(); ++i )
            wordRule.addWord(m_modelNativeKeywords.get(i), m_tokenModelKeyword);

        WordRule wordRule2 = new WordRule(new ExchangeWordDetector(), m_tokenOther);
        wordRule2.addWord("--help", m_tokenPreprocessorKeyword); //$NON-NLS-1$
        for( int i = 0; i < m_exchangeKeywords.size(); ++i ) {

            String candidate = m_exchangeKeywords.get(i);
            if (true == candidate.endsWith("*")) //$NON-NLS-1$
                wordRule2.addWord(candidate, m_tokenAsteriskKeyword);
            else if (true == candidate.startsWith("--i")) //$NON-NLS-1$
                wordRule2.addWord(candidate, m_tokenExchangeInputKeyword);
            else if (true == candidate.startsWith("--o")) //$NON-NLS-1$
                wordRule2.addWord(candidate, m_tokenExchangeOutputKeyword);
        }

        for( int i = 0; i < m_quantityKeywords.size(); ++i )
            wordRule2.addWord(m_quantityKeywords.get(i), m_tokenQuantityKeyword);

        rules.add(wordRule2);
        rules.add(wordRule);
        IRule[] result = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    } // JavaCodeScanner

    /** */
    private void __initializeTokens( JavaColorProvider provider ) {

        m_tokenComment = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_COMMENT)));
        m_tokenParametersKeyword = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_ML_PARAMETERS_KEYWORD)));
        m_tokenString = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_STRING)));
        m_tokenOther = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_OTHER)));

        m_tokenAsteriskKeyword = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_ML_ASTERISK_KEYWORD)));
        m_tokenModelKeyword = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_ML_MODEL_KEYWORD)));
        m_tokenQuantityKeyword = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_ML_EXCHANGE_KEYWORD)));
        m_tokenExchangeInputKeyword = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_ML_INPUT_KEYWORD)));
        m_tokenExchangeOutputKeyword = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_ML_OUTPUT_KEYWORD)));
        m_tokenPreprocessorKeyword = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_ML_PREPROCESSOR_KEYWORD)));

        m_tokenConstant = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_CONSTANT)));
        m_tokenKeyword = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_KEYWORD)));
        m_tokenType = new Token(new TextAttribute(provider
                .getColor(PreferencesInitializer.IDC_TYPE)));
    } // __initializeTokens

    /** */
    private void __initializeWords( Projectspace projectSpace ) {

        m_annotationKeywords = new Vector<String>();
        m_annotationKeywords.addAll(projectSpace.annotationKeywords());

        m_exchangeKeywords = new Vector<String>();
        Vector<String> exchangeKeywords;
        exchangeKeywords = projectSpace.importedExchangeKeywords();
        for( int i = 0; i < exchangeKeywords.size(); ++i ) {

            String candidate = exchangeKeywords.get(i);
            if (true == candidate.startsWith("i") || //$NON-NLS-1$
                    true == candidate.startsWith("o") //$NON-NLS-1$
            ) {

                m_exchangeKeywords.add("--" + candidate); //$NON-NLS-1$
                m_exchangeKeywords.add("--" + candidate + "*" //$NON-NLS-2$ //$NON-NLS-1$
                );
            }
        }

        m_modelJavaKeywords = new Vector<String>();
        Vector<String> modelJavaKeywords;
        modelJavaKeywords = projectSpace.importedJavaModelKeywords();
        for( int i = 0; i < modelJavaKeywords.size(); ++i ) {

            String candidate = modelJavaKeywords.get(i);
            m_modelJavaKeywords.add(candidate);
            m_modelJavaKeywords.add(candidate + "*" //$NON-NLS-1$
            );
        }

        m_modelNativeKeywords = new Vector<String>();
        m_modelNativeKeywords.addAll(projectSpace.importedNativeModelKeywords());

        m_quantityKeywords = new Vector<String>();
        Vector<String> quantityKeywords;
        quantityKeywords = projectSpace.importedQuantityKeywords();
        for( int i = 0; i < quantityKeywords.size(); ++i )
            m_quantityKeywords.add("-" + quantityKeywords.get(i) //$NON-NLS-1$
            );

        m_preprocessorKeywords = new Vector<String>();
        m_preprocessorKeywords.addAll(projectSpace.preprocessorKeywords());

        m_reservedConstants = new Vector<String>();
        m_reservedConstants.addAll(projectSpace.reservedConstants());

        m_reservedKeywords = new Vector<String>();
        m_reservedKeywords.addAll(projectSpace.reservedKeywords());

        m_reservedTypes = new Vector<String>();
        m_reservedTypes.addAll(projectSpace.reservedTypes());
    } // __initializeWords

    // Operations
    /** */
    public Vector<String> annotationKeywords() {

        return m_annotationKeywords;
    } // annotationKeywords

    /** */
    public Vector<String> exchangeKeywords() {

        return m_exchangeKeywords;
    } // exchangeKeywords

    /** */
    public Vector<String> modelJavaKeywords() {

        return m_modelJavaKeywords;
    } // modelJavaKeywords

    /** */
    public Vector<String> modelNativeKeywords() {

        return m_modelNativeKeywords;
    } // modelNativeKeywords

    /** */
    public Vector<String> preprocessorKeywords() {

        return m_preprocessorKeywords;
    } // preprocessorKeywords

    /** */
    public Vector<String> quantityKeywords() {

        return m_quantityKeywords;
    } // quantityKeywords

    /** */
    public Vector<String> reservedConstants() {

        return m_reservedConstants;
    } // reservedConstants

    /** */
    public Vector<String> reservedKeywords() {

        return m_reservedKeywords;
    } // reservedKeywords

    /** */
    public Vector<String> reservedTypes() {

        return m_reservedTypes;
    } // reservedTypes

} // JavaCodeScanner
