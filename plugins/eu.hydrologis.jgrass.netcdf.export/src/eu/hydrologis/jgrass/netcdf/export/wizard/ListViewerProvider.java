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
package eu.hydrologis.jgrass.netcdf.export.wizard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Creates a listviewer that permits adding, editing and removing of properties.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ListViewerProvider {

    /**
     * The list of items.
     */
    private List<String> itemsList = new ArrayList<String>();

    /**
     * Creates the {@link ListViewer} inside the supplied parent {@link Composite}.
     * 
     * <p>Note that the parent has to be a gridlayout of 3 cols.</p>
     * 
     * @param parent the parent composite.
     * @param itemsList the initial list of items to put in the list.
     */
    public void create( Composite parent, final List<String> startItemsList ) {
        itemsList.addAll(startItemsList);

        final ListViewer listViewer = new ListViewer(parent, SWT.BORDER | SWT.SINGLE
                | SWT.VERTICAL | SWT.V_SCROLL);
        Control control = listViewer.getControl();
        GridData gD = new GridData(SWT.FILL, SWT.FILL, true, true);
        gD.horizontalSpan = 3;
        control.setLayoutData(gD);

        listViewer.setContentProvider(new IStructuredContentProvider(){
            @SuppressWarnings("unchecked")
            public Object[] getElements( Object inputElement ) {
                List<String> v = (List<String>) inputElement;
                return v.toArray();
            }

            public void dispose() {
            }

            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
                System.out.println("Input changed: old=" + oldInput + ", new=" + newInput);
            }
        });

        listViewer.setInput(itemsList);

        listViewer.setLabelProvider(new LabelProvider(){
            public Image getImage( Object element ) {
                return null;
            }

            public String getText( Object element ) {
                return ((String) element);
            }
        });

        /*
         * buttons
         */
        final Button buttonAdd = new Button(parent, SWT.PUSH);
        GridData addGd = new GridData(SWT.FILL, SWT.FILL, true, false);
        buttonAdd.setLayoutData(addGd);
        buttonAdd.setText("+");

        final Button buttonModify = new Button(parent, SWT.PUSH);
        GridData modifyGd = new GridData(SWT.FILL, SWT.FILL, true, false);
        buttonModify.setLayoutData(modifyGd);
        buttonModify.setText("?");

        final Button buttonRemove = new Button(parent, SWT.PUSH);
        GridData removeGd = new GridData(SWT.FILL, SWT.FILL, true, false);
        buttonRemove.setLayoutData(removeGd);
        buttonRemove.setText("-");

        buttonAdd.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                final String[] itemString = new String[1];

                IInputValidator validator = new IInputValidator(){
                    public String isValid( String newText ) {
                        String[] split = newText.split(":");
                        if (split.length >= 2) {
                            itemString[0] = newText;
                            return null;
                        } else {
                            return "The item format is: \"KEY: VALUE\".";
                        }
                    }
                };

                InputDialog iDialog = new InputDialog(buttonAdd.getShell(), "Add and item",
                        "Add an item in the form: \"KEY: VALUE\".", "", validator);
                iDialog.open();
                itemsList.add(itemString[0]);

                listViewer.setInput(itemsList);
            }
        });

        buttonModify.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
                String item = (String) selection.getFirstElement();
                if (item == null) {
                    return;
                }
                int indexOf = itemsList.indexOf(item);
                itemsList.remove(item);

                final String[] itemString = new String[1];

                IInputValidator validator = new IInputValidator(){
                    public String isValid( String newText ) {
                        String[] split = newText.split(":");
                        if (split.length == 2) {
                            itemString[0] = newText;
                            return null;
                        } else {
                            return "The item format is: \"KEY: VALUE\".";
                        }
                    }
                };

                InputDialog iDialog = new InputDialog(buttonAdd.getShell(), "Modify item",
                        "Modify the item in the form: \"KEY: VALUE\".", item, validator);
                iDialog.open();
                itemsList.add(indexOf, itemString[0]);

                listViewer.setInput(itemsList);
            }
        });

        buttonRemove.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
                String item = (String) selection.getFirstElement();
                if (item == null) {
                    return;
                }
                itemsList.remove(item);
                listViewer.setInput(itemsList);
            }
        });
    }

    public LinkedHashMap<String, Object> getItemsMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        for( String item : itemsList ) {
            String[] split = item.split(":");
            if (split.length == 2) {
                if (split[1].length() > 0) {
                    map.put(split[0].trim(), split[1].trim());
                }
            }
        }
        return map;
    }

}
