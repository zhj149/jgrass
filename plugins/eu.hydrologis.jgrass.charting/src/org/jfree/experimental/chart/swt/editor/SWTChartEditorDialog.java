/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * -------------------
 * SWTChartEditor.java
 * -------------------
 * (C) Copyright 2006, 2007, by Henry Proudhon and Contributors.
 *
 * Original Author:  Henry Proudhon (henry.proudhon AT ensmp.fr);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 01-Aug-2006 : New class (HP);
 * 
 */

package org.jfree.experimental.chart.swt.editor;

import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.editor.ChartEditor;

/**
 * An editor for chart properties.
 */
public class SWTChartEditorDialog implements ChartEditor {

    /** The shell */
    private Dialog dialog;

    /** The chart which the properties have to be edited */
    private JFreeChart chart;

    /** A composite for displaying/editing the properties of the title. */
    private SWTTitleEditor titleEditor;

    /** A composite for displaying/editing the properties of the plot. */
    private SWTPlotEditor plotEditor;

    /** A composite for displaying/editing the other properties of the chart. */
    private SWTOtherEditor otherEditor;

    /** The resourceBundle for the localization. */
    protected static ResourceBundle localizationResources = ResourceBundle
            .getBundle("org.jfree.chart.editor.LocalizationBundle");

    /**
     * Creates a new editor.
     * 
     * @param shell2 the display.
     * @param chart2edit the chart to edit.
     */
    public SWTChartEditorDialog( Shell shell2, final JFreeChart chart2edit ) {

        this.dialog = new Dialog(shell2){
            @Override
            protected Control createDialogArea( Composite parent ) {
                SWTChartEditorDialog.this.chart = chart2edit;
                GridLayout layout = new GridLayout(2, true);
                layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 5;
                parent.setLayout(layout);

                Composite main = new Composite(parent, SWT.NONE);
                main.setLayout(new FillLayout());
                main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
                main.setSize(400, 500);

                TabFolder tab = new TabFolder(main, SWT.BORDER);
                // build first tab
                TabItem item1 = new TabItem(tab, SWT.NONE);
                item1.setText(" " + localizationResources.getString("Title") + " ");
                SWTChartEditorDialog.this.titleEditor = new SWTTitleEditor(tab, SWT.NONE,
                        SWTChartEditorDialog.this.chart.getTitle());
                item1.setControl(SWTChartEditorDialog.this.titleEditor);
                // build second tab
                TabItem item2 = new TabItem(tab, SWT.NONE);
                item2.setText(" " + localizationResources.getString("Plot") + " ");
                SWTChartEditorDialog.this.plotEditor = new SWTPlotEditor(tab, SWT.NONE,
                        SWTChartEditorDialog.this.chart.getPlot());
                item2.setControl(SWTChartEditorDialog.this.plotEditor);
                // build the third tab
                TabItem item3 = new TabItem(tab, SWT.NONE);
                item3.setText(" " + localizationResources.getString("Other") + " ");
                SWTChartEditorDialog.this.otherEditor = new SWTOtherEditor(tab, SWT.NONE,
                        SWTChartEditorDialog.this.chart);
                item3.setControl(SWTChartEditorDialog.this.otherEditor);

                return super.createDialogArea(parent);
            }

            @Override
            protected void buttonPressed( int buttonId ) {
                if (buttonId == 0) {
                    updateChart(SWTChartEditorDialog.this.chart);
                }
                this.close();
            }
        };

    }

    /**
     * Opens the editor.
     */
    public void open() {
        this.dialog.setBlockOnOpen(true);
        this.dialog.open();
    }

    /**
     * Updates the chart properties.
     * 
     * @param chart the chart.
     */
    public void updateChart( JFreeChart chart ) {
        this.titleEditor.setTitleProperties(chart);
        this.plotEditor.updatePlotProperties(chart.getPlot());
        this.otherEditor.updateChartProperties(chart);
    }

}
