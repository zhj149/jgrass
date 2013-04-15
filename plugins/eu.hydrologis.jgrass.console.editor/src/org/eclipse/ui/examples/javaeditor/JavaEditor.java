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
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.examples.javaeditor;

import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.editor.actions.ConsoleEditorActionCompile;
import eu.hydrologis.jgrass.console.editor.actions.ConsoleEditorActionRun;
import eu.hydrologis.jgrass.console.editor.actions.OpenFileClass;
import eu.hydrologis.jgrass.console.editor.actions.SaveAsClass;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;
import eu.hydrologis.jgrass.ui.console.BacktraceConsole;

/**
 * Java specific text editor.
 */
public class JavaEditor extends TextEditor {

    public final static String ID = "eu.hydrologis.jgrass.console.editor.editors.JGrassConsoleEditor"; //$NON-NLS-1$

    private class DefineFoldingRegionAction extends TextEditorAction {

        public DefineFoldingRegionAction( ResourceBundle bundle, String prefix, ITextEditor editor ) {

            super(bundle, prefix, editor);
        }

        private IAnnotationModel getAnnotationModel( ITextEditor editor ) {

            return (IAnnotationModel) editor.getAdapter(ProjectionAnnotationModel.class);
        }

        /*
         * @see org.eclipse.jface.action.Action#run()
         */

        public void run() {

            ITextEditor editor = getTextEditor();
            ISelection selection = editor.getSelectionProvider().getSelection();
            if (selection instanceof ITextSelection) {
                ITextSelection textSelection = (ITextSelection) selection;
                if (!textSelection.isEmpty()) {
                    IAnnotationModel model = getAnnotationModel(editor);
                    if (model != null) {

                        int start = textSelection.getStartLine();
                        int end = textSelection.getEndLine();

                        try {
                            IDocument document = editor.getDocumentProvider().getDocument(
                                    editor.getEditorInput());
                            int offset = document.getLineOffset(start);
                            int endOffset = document.getLineOffset(end + 1);
                            Position position = new Position(offset, endOffset - offset);
                            model.addAnnotation(new ProjectionAnnotation(), position);
                        } catch (BadLocationException x) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    private class Presentation2Action extends TextEditorAction {

        /**
         * Constructs and updates the action.
         */
        public Presentation2Action( ResourceBundle bundle, String prefix, ITextEditor editor ) {

            super(bundle, prefix, editor);
            update();
        }

        /*
         * (non-Javadoc) Method declared on IAction
         */

        public void run() {

            ITextEditor editor = getTextEditor();

            editor.resetHighlightRange();
            boolean show = editor.showsHighlightRangeOnly();
            setChecked(!show);
            editor.showHighlightRangeOnly(!show);
        }

        /*
         * (non-Javadoc) Method declared on TextEditorAction
         */

        public void update() {

            setChecked(getTextEditor() != null && getTextEditor().showsHighlightRangeOnly());
            setEnabled(true);
        }
    }

    /** The outline page */
    private JavaContentOutlinePage fOutlinePage;

    /** The projection support */
    private ProjectionSupport fProjectionSupport;

    /** */
    private ProjectOptions m_projectOptions;

    /** */
    private BacktraceConsole m_textConsole;

    private boolean isDisposed = false;

    /**
     * Default constructor.
     */
    public JavaEditor() {

        super();
        setDocumentProvider(new JavaEditorDocumentProvider());
        m_textConsole = new BacktraceConsole();
        m_projectOptions = new ProjectOptions(m_textConsole.internal, m_textConsole.out,
                m_textConsole.err);
        PreferencesInitializer.initialize(m_projectOptions);

    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method
     * extend the actions to add those specific to the receiver
     */

    protected void createActions() {

        super.createActions();

        IAction a;
        // a = new TextOperationAction(JavaEditorMessages
        // .getResourceBundle(), "ContentAssistProposal." //$NON-NLS-1$
        // , this, ISourceViewer.CONTENTASSIST_PROPOSALS);
        // a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        // setAction("ContentAssistProposal", a); //$NON-NLS-1$
        //
        // a = new TextOperationAction(JavaEditorMessages.getResourceBundle(),
        // "ContentAssistTip." //$NON-NLS-1$
        // , this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
        // a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
        // setAction("ContentAssistTip", a); //$NON-NLS-1$
        //
        // a = new DefineFoldingRegionAction(JavaEditorMessages
        // .getResourceBundle(), "DefineFoldingRegion." //$NON-NLS-1$
        // , this);
        // setAction("DefineFoldingRegion", a); //$NON-NLS-1$

        // a = new Presentation2Action(JavaEditorMessages.getResourceBundle(),
        // "TogglePresentation." //$NON-NLS-1$
        // , this);
        // setAction("Present", a); //$NON-NLS-1$

        a = new ConsoleEditorActionCompile(JavaEditorMessages.getResourceBundle(),
                "ConsoleEditorActionCompile." //$NON-NLS-1$
                , this);
        setAction("ConsoleEditorActionCompile", a); //$NON-NLS-1$
        a.setEnabled(true);

        a = new ConsoleEditorActionRun(JavaEditorMessages.getResourceBundle(),
                "ConsoleEditorActionRun." //$NON-NLS-1$
                , this);
        setAction("ConsoleEditorActionRun", a); //$NON-NLS-1$
        a.setEnabled(true);

        // a = new SetRuntimePreferencesClass(JavaEditorMessages
        // .getResourceBundle(), "RuntimePreferences." //$NON-NLS-1$
        // , this);
        // setAction("RuntimePreferences", a); //$NON-NLS-1$

        a = new OpenFileClass(JavaEditorMessages.getResourceBundle(), "Open." //$NON-NLS-1$
                , this);
        setAction("Open", a); //$NON-NLS-1$

        IWorkbenchAction saveAction = ActionFactory.SAVE.create(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow());
        setAction("Save", saveAction); //$NON-NLS-1$

        a = new SaveAsClass(JavaEditorMessages.getResourceBundle(), "SaveAs." //$NON-NLS-1$
                , this);
        setAction("SaveAs", a); //$NON-NLS-1$
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method
     * performs any extra disposal actions required by the java editor.
     */

    public void dispose() {

        ConsolePlugin.getDefault().getConsoleManager()
                .removeConsoles(new IConsole[]{m_textConsole});
        if (fOutlinePage != null)
            fOutlinePage.setInput(null);

        isDisposed = true;

        super.dispose();
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method
     * performs any extra revert behavior required by the java editor.
     */

    public void doRevertToSaved() {

        super.doRevertToSaved();
        if (fOutlinePage != null)
            fOutlinePage.update();
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method
     * performs any extra save behavior required by the java editor.
     * 
     * @param monitor the progress monitor
     */

    public void doSave( IProgressMonitor monitor ) {

        super.doSave(monitor);
        if (fOutlinePage != null)
            fOutlinePage.update();
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method
     * performs any extra save as behavior required by the java editor.
     */

    public void doSaveAs() {

        super.doSaveAs();
        if (fOutlinePage != null)
            fOutlinePage.update();
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method
     * performs sets the input of the outline page after AbstractTextEditor has set input.
     * 
     * @param input the editor input
     * @throws CoreException in case the input can not be set
     */

    public void doSetInput( IEditorInput input ) throws CoreException {

        super.doSetInput(input);
        if (fOutlinePage != null)
            fOutlinePage.setInput(input);
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */

    protected void editorContextMenuAboutToShow( IMenuManager menu ) {

        // super.editorContextMenuAboutToShow(menu);
        addAction(menu, "RuntimePreferences"); //$NON-NLS-1$
        addAction(menu, "ConsoleEditorActionCompile"); //$NON-NLS-1$
        addAction(menu, "ConsoleEditorActionRun"); //$NON-NLS-1$
        menu.add(new Separator(ITextEditorActionConstants.GROUP_ASSIST));
        addAction(menu, "ContentAssistProposal"); //$NON-NLS-1$
        addAction(menu, "ContentAssistTip"); //$NON-NLS-1$
        addAction(menu, "DefineFoldingRegion"); //$NON-NLS-1$
        menu.add(new Separator(ITextEditorActionConstants.GROUP_SAVE));
        addAction(menu, "Open"); //$NON-NLS-1$
        addAction(menu, "Save"); //$NON-NLS-1$
        addAction(menu, "SaveAs"); //$NON-NLS-1$
        // addAction(menu, "Present"); //$NON-NLS-1$
    }

    /**
     * The <code>JavaEditor</code> implementation of this <code>AbstractTextEditor</code> method
     * performs gets the java content outline page if request is for a an outline page.
     * 
     * @param required the required type
     * @return an adapter for the required type or <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter( Class required ) {

        if (IContentOutlinePage.class.equals(required)) {
            if (fOutlinePage == null) {
                fOutlinePage = new JavaContentOutlinePage(getDocumentProvider(), this);
                if (getEditorInput() != null)
                    fOutlinePage.setInput(getEditorInput());
            }
            return fOutlinePage;
        }

        if (fProjectionSupport != null) {
            Object adapter = fProjectionSupport.getAdapter(getSourceViewer(), required);
            if (adapter != null)
                return adapter;
        }

        return super.getAdapter(required);
    }

    /** */
    public BacktraceConsole getTextConsole() {

        return m_textConsole;
    } // getTextConsole

    /** */
    public ProjectOptions projectOptions() {

        return m_projectOptions;
    } // projectOptions

    /*
     * (non-Javadoc) Method declared on AbstractTextEditor
     */

    protected void initializeEditor() {

        super.initializeEditor();
        setSourceViewerConfiguration(new JavaSourceViewerConfiguration());
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite,
     *      org.eclipse.jface.text.source.IVerticalRuler, int)
     */

    protected ISourceViewer createSourceViewer( Composite parent, IVerticalRuler ruler, int styles ) {

        fAnnotationAccess = createAnnotationAccess();
        fOverviewRuler = createOverviewRuler(getSharedColors());

        ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(),
                isOverviewRulerVisible(), styles);
        // ensure decoration support has been created and configured.
        getSourceViewerDecorationSupport(viewer);

        return viewer;
    }

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
     */

    public void createPartControl( Composite parent ) {

        super.createPartControl(parent);
        ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
        fProjectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
        fProjectionSupport
                .addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
        fProjectionSupport
                .addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
        fProjectionSupport.install();
        viewer.doOperation(ProjectionViewer.TOGGLE);
        m_projectOptions.projectCaption(this.getTitle());
        m_textConsole.setName(this.getTitle());
        IConsoleManager manager;
        manager = ConsolePlugin.getDefault().getConsoleManager();
        manager.addConsoles(new IConsole[]{m_textConsole});
        manager.showConsoleView(m_textConsole);
    }

    /*
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#adjustHighlightRange(int, int)
     */

    protected void adjustHighlightRange( int offset, int length ) {

        ISourceViewer viewer = getSourceViewer();
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
            extension.exposeModelRange(new Region(offset, length));
        }
    }

    /** */

    public void setFocus() {

        super.setFocus();
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        consoleManager.showConsoleView(m_textConsole);

        final int secondsInterval = 10;
        Thread saveEditors = new Thread(){
            public void run() {
                while( !isDisposed ) {

                    try {
                        Thread.sleep(secondsInterval * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Display.getDefault().syncExec(new Runnable(){
                        public void run() {
                            if (JavaEditor.this.isDirty()) {
                                JavaEditor.this.doSave(new NullProgressMonitor());
                            }
                        }
                    });
                }
            }
        };
        saveEditors.start();
    } // setFocus
}
