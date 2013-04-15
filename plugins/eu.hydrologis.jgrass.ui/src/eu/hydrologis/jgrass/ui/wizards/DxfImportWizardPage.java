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
package eu.hydrologis.jgrass.ui.wizards;

import java.io.File;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.udig.catalog.jgrass.core.ChooseCoordinateReferenceSystemDialog;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class DxfImportWizardPage extends WizardPage {

    public static final String ID = "DxfImportWizardPage"; //$NON-NLS-1$
    private File inFile = null;
    private CoordinateReferenceSystem crs;

    private boolean inIsOk = false;
    private boolean epsgIsOk = false;

    public DxfImportWizardPage( String pageName, Map<String, String> params ) {
        super(ID);
        setTitle(pageName);
        setDescription("Import the selected dxf file"); // NON-NLS-1
    }

    public void createControl( Composite parent ) {
        Composite fileSelectionArea = new Composite(parent, SWT.NONE);
        fileSelectionArea.setLayout(new GridLayout());

        Group inputGroup = new Group(fileSelectionArea, SWT.None);
        inputGroup.setText("Choose the DXF file");
        inputGroup.setLayout(new GridLayout(2, false));
        inputGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        GridData gridData1 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gridData1.horizontalSpan = 2;

        final Text dxfText = new Text(inputGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        dxfText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        dxfText.setText("");
        final Button dxfButton = new Button(inputGroup, SWT.PUSH);
        dxfButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        dxfButton.setText("...");
        dxfButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(dxfButton.getShell(), SWT.OPEN);
                String path = fileDialog.open();
                if (path != null) {
                    File f = new File(path);
                    if (f.exists()) {
                        inIsOk = true;
                        dxfText.setText(path);
                        inFile = f;
                    } else {
                        inIsOk = false;
                    }
                }
                checkFinish();
            }
        });

        // the crs choice group
        Group crsGroup = new Group(fileSelectionArea, SWT.None);
        crsGroup.setLayout(new GridLayout(2, false));
        crsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        crsGroup.setText("Coordinate reference system for the data");

        final Text crsText = new Text(crsGroup, SWT.BORDER);
        crsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        crsText.setEditable(false);

        final Button crsButton = new Button(crsGroup, SWT.BORDER);
        crsButton.setText(" Choose CRS ");
        crsButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                final ChooseCoordinateReferenceSystemDialog crsChooser = new ChooseCoordinateReferenceSystemDialog();
                crsChooser.open(new Shell(Display.getDefault()));
                CoordinateReferenceSystem readCrs = crsChooser.getCrs();
                epsgIsOk = false;
                if (readCrs == null)
                    return;

                crs = readCrs;
                crsText.setText(readCrs.getName().toString());
                epsgIsOk = true;
                checkFinish();
            }
        });

        setControl(fileSelectionArea);
    }

    public void dispose() {
    }

    public File getDxfFile() {
        return inFile;
    }

    public CoordinateReferenceSystem getCrs() {
        return crs;
    }

    private void checkFinish() {
        if (inIsOk && epsgIsOk) {
            DxfImportWizard.canFinish = true;
        } else {
            DxfImportWizard.canFinish = false;
        }
        getWizard().getContainer().updateButtons();
    }

}
