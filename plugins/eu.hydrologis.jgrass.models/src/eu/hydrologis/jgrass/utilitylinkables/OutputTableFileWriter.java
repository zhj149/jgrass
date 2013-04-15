/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
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
package eu.hydrologis.jgrass.utilitylinkables;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.ValueSet;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * @author Andrea Antonello - www.hydrologis.com
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 */
public class OutputTableFileWriter extends ModelsBackbone {

    private ILink inputLink = null;

    private IInputExchangeItem tableFileInputEI = null;

    private static final String modelParameters = "...";

    private String filePath = null;

    private boolean doConsole = false;
    private boolean doUiTable = false;

    private String[] headerSplits;

    private int columns;

    public OutputTableFileWriter() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public OutputTableFileWriter( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) {

        String grassDb = null;
        String location = null;
        String mapset = null;
        String tableString = null;
        int numCol = 1;
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("otable")) {
                tableString = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.GRASSDB) == 0) {
                grassDb = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.LOCATION) == 0) {
                location = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.MAPSET) == 0) {
                mapset = argument.getValue();
            }
        }
        headerSplits = tableString.split("#"); //$NON-NLS-1$
        if (tableString.matches(".*" + ModelsConstants.CONSOLE + ".*")) {
            doConsole = true;
        } else if (tableString.matches(".*" + ModelsConstants.UITABLE + ".*")) {
            doUiTable = true;
        } else {
            filePath = headerSplits[0];
        }

        componentId = "";
        componentDescr = "";

        tableFileInputEI = ModelsConstants.createDummyInputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) {
        /*
         * trigger the linked model
         */
        IValueSet valueSet = inputLink.getSourceComponent().getValues(time, inputLink.getID());

        if (valueSet instanceof ValueSet) {

            ValueSet values = (ValueSet) valueSet;

            Object numObject = values.get(0);
            if (numObject instanceof Number) {
                Number number = (Number) numObject;
                columns = number.intValue();
            } else {
                columns = (int) Double.parseDouble((String) numObject);
            }

            int l = 1;
            final Object[][] dataToWrite = new Object[(values.getCount() - 1) / columns][columns];
            for( int i = 0; i < dataToWrite.length; i++ ) {
                for( int j = 0; j < dataToWrite[0].length; j++ ) {
                    dataToWrite[i][j] = values.get(l);
                    l++;
                }
            }

            try {
                if (doConsole) {
                    for( int i = 0; i < columns; i++ ) {
                        out.print(headerSplits[i] + " \t "); //$NON-NLS-1$
                    }
                    out.println();
                    for( int i = 0; i < dataToWrite.length; i++ ) {
                        for( int j = 0; j < dataToWrite[0].length; j++ ) {
                            out.print(String.valueOf(dataToWrite[i][j]) + "\t"); //$NON-NLS-1$
                        }
                        out.println();
                    }
                } else if (doUiTable) {

                    Display.getDefault().asyncExec(new Runnable(){
                        public void run() {
                            Shell shell2 = new Shell(Display.getDefault(), SWT.DIALOG_TRIM
                                    | SWT.RESIZE);
                            shell2.setSize(800, 600);
                            shell2.setLayout(new GridLayout(1, false));
                            final TableViewer v = new TableViewer(shell2);
                            v.setLabelProvider(new TableLabelProvider());
                            v.setContentProvider(new ArrayContentProvider());

                            Table table = v.getTable();
                            table.setLayoutData(new GridData(GridData.FILL_BOTH));
                            table.setHeaderVisible(true);
                            table.setLinesVisible(true);
                            TableLayout layout = new TableLayout();
                            for( int i = 0; i < columns; i++ ) {
                                layout.addColumnData(new ColumnWeightData(100 / columns, true));
                                TableColumn tmpHead = new TableColumn(table, SWT.LEFT);
                                tmpHead.setWidth(shell2.getSize().x / columns);
                                tmpHead.setText(headerSplits[i + 1]);
                            }
                            table.setLayout(layout);

                            v.setInput(dataToWrite);
                            v.getTable().setLinesVisible(true);
                            shell2.open();
                        }
                    });
                } else {
                    File f = new File(filePath);
                    if (f.getParentFile() == null || !f.getParentFile().exists()) {
                        throw new RuntimeException("The path to the output file doesn't exist: "
                                + f.getParent());
                    }
                    BufferedWriter bW = new BufferedWriter(new FileWriter(filePath));
                    for( int i = 0; i < columns; i++ ) {
                        bW.write(headerSplits[i + 1] + " \t "); //$NON-NLS-1$
                    }
                    bW.write("\n"); //$NON-NLS-1$
                    for( int i = 0; i < dataToWrite.length; i++ ) {
                        for( int j = 0; j < dataToWrite[0].length; j++ ) {
                            bW.write(String.valueOf(dataToWrite[i][j]) + "\t"); //$NON-NLS-1$
                        }
                        bW.write("\n"); //$NON-NLS-1$
                    }
                    bW.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("An error occurred in writing the output data.");
            }
        }
        return null;
    }

    public void addLink( ILink link ) {
        inputLink = link;
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return tableFileInputEI;
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 0;
    }

    public void removeLink( String linkID ) {

    }

    private class MyContentProvider implements IStructuredContentProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements( Object inputElement ) {
            return new Object[]{inputElement};
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {

        }

    }

    private class TableLabelProvider implements ITableLabelProvider {

        public Image getColumnImage( Object element, int columnIndex ) {
            return null;
        }

        public String getColumnText( Object element, int columnIndex ) {
            Object[] e = (Object[]) element;
            return String.valueOf(e[columnIndex]);
        }

        public void addListener( ILabelProviderListener listener ) {
        }

        public void dispose() {
        }

        public boolean isLabelProperty( Object element, String property ) {
            return false;
        }

        public void removeListener( ILabelProviderListener listener ) {
        }

    }
}
