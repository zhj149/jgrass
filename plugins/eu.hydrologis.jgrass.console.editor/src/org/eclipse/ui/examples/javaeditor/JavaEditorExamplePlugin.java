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
package org.eclipse.ui.examples.javaeditor;

import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.ui.examples.javaeditor.java.JavaCodeScanner;
import org.eclipse.ui.examples.javaeditor.javadoc.JavaDocScanner;
import org.eclipse.ui.examples.javaeditor.util.JavaColorProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;

/**
 * The example java editor plug-in class.
 * 
 * @since 3.0
 */
public class JavaEditorExamplePlugin extends AbstractUIPlugin {

    public final static String JAVA_PARTITIONING = "__java_example_partitioning"; //$NON-NLS-1$

    private static JavaEditorExamplePlugin fgInstance;
    private JavaPartitionScanner fPartitionScanner;
    private JavaColorProvider fColorProvider;
    private JavaCodeScanner fCodeScanner;
    private JavaDocScanner fDocScanner;

    /**
     * Creates a new plug-in instance.
     */
    public JavaEditorExamplePlugin() {
        fgInstance = this;
    }

    /**
     * Returns the default plug-in instance.
     * 
     * @return the default plug-in instance
     */
    public static JavaEditorExamplePlugin getDefault() {
        if (fgInstance == null) {
            new JavaEditorExamplePlugin();
        }
        return fgInstance;
    }

    /**
     * Return a scanner for creating Java partitions.
     * 
     * @return a scanner for creating Java partitions
     */
    public JavaPartitionScanner getJavaPartitionScanner() {
        if (fPartitionScanner == null)
            fPartitionScanner = new JavaPartitionScanner();
        return fPartitionScanner;
    }

    /**
     * Returns the singleton Java code scanner.
     * 
     * @return the singleton Java code scanner
     */
    public RuleBasedScanner getJavaCodeScanner() {
        if (fCodeScanner == null)
            fCodeScanner = new JavaCodeScanner(getJavaColorProvider());
        return fCodeScanner;
    }

    /**
     * Returns the singleton Java color provider.
     * 
     * @return the singleton Java color provider
     */
    public JavaColorProvider getJavaColorProvider() {
        if (fColorProvider == null)
            fColorProvider = new JavaColorProvider();

        PreferencesInitializer.initialize(fColorProvider);
        return fColorProvider;
    }

    /**
     * Returns the singleton Javadoc scanner.
     * 
     * @return the singleton Javadoc scanner
     */
    public RuleBasedScanner getJavaDocScanner() {
        if (fDocScanner == null)
            fDocScanner = new JavaDocScanner(fColorProvider);

        return fDocScanner;
    }
}
