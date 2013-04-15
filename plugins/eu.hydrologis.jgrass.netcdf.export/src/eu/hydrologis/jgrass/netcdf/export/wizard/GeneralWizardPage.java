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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.jgrass.netcdf.export.core.NcFileWriter;
import eu.hydrologis.jgrass.ui.utilities.CatalogJGrassMapsetTreeViewerDialog;
import eu.udig.catalog.jgrass.core.JGrassMapsetGeoResource;

/**
 * Page for general parameters.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeneralWizardPage extends WizardPage implements KeyListener, ModifyListener {
    public static final String ID = "eu.hydrologis.jgrass.netcdf.export.wizard.GeneralWizardPage";
    
    private final NetcdfExportWizard parentWizard;
    private Text pathText;
    private ListViewerProvider lvP;
    private Text fromText;
    private Text toText;
    private Text dtText;
    private Text levelText;
    private Text mapsetText;

    protected GeneralWizardPage( NetcdfExportWizard netcdfExportWizard ) {
        super(ID);
        this.parentWizard = netcdfExportWizard;
        setTitle("General parameters");
        setDescription("General parameters to define the exported netcdf dataset structure.");
    }

    public void createControl( Composite parent ) {
        Composite container = new Composite(parent, SWT.NULL);
        GridData containerLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        container.setLayoutData(containerLayoutData);
        GridLayout containerLayout = new GridLayout(3, false);
        container.setLayout(containerLayout);
        setControl(container);

        /*
         * mapset
         */
        Label mapsetLabel = new Label(container, SWT.NONE);
        mapsetLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        mapsetLabel.setText("grass mapset to take maps from");

        mapsetText = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        mapsetText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mapsetText.setText("");
        mapsetText.setEditable(false);
        
        final Button mapsetButton = new Button(container, SWT.BORDER | SWT.PUSH);
        GridData gd2 = new GridData();
        gd2.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
        mapsetButton.setLayoutData(gd2);
        mapsetButton.setText("...");
        mapsetButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                CatalogJGrassMapsetTreeViewerDialog cDialog = new CatalogJGrassMapsetTreeViewerDialog();
                cDialog.open(mapsetButton.getShell());
                List<JGrassMapsetGeoResource> selectedLayers = cDialog.getSelectedLayers();
                if (selectedLayers == null || selectedLayers.size() == 0) {
                    return;
                }
                JGrassMapsetGeoResource selectedmapset = selectedLayers.get(0);
                File mapsetFile = selectedmapset.getFile();
                mapsetText.setText(mapsetFile.getAbsolutePath());
                updatePageComplete();
            }
        });

        
        
        /*
         * output path
         */
        Label pathLabel = new Label(container, SWT.NONE);
        pathLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        pathLabel.setText("output netcdf path");

        pathText = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pathText.setText("");
        pathText.addKeyListener(this);

        final Button pathBrowseButton = new Button(container, SWT.PUSH);
        pathBrowseButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        pathBrowseButton.setText("...");
        pathBrowseButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
                FileDialog fileDialog = new FileDialog(pathBrowseButton.getShell(), SWT.SAVE);
                String path = fileDialog.open();
                if (path == null || path.length() < 1) {
                    pathText.setText("");
                } else {
                    pathText.setText(path);
                }
                updatePageComplete();
            }
        });

        /*
         * time group
         */
        Group timeGroup = new Group(container, SWT.NONE);
        GridData timeGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        timeGroupLayoutData.horizontalSpan = 3;
        timeGroup.setLayoutData(timeGroupLayoutData);
        timeGroup.setLayout(new GridLayout(2, false));
        timeGroup.setText("time [YYYY-MM-DD HH:MM]");

        Label fromLabel = new Label(timeGroup, SWT.NONE);
        fromLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        fromLabel.setText("First time moment");
        fromText = new Text(timeGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        fromText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fromText.setText("");
        fromText.addKeyListener(this);

        Label toLabel = new Label(timeGroup, SWT.NONE);
        toLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        toLabel.setText("Last time moment");
        toText = new Text(timeGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        toText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        toText.setText("");
        toText.addKeyListener(this);

        Label dtLabel = new Label(timeGroup, SWT.NONE);
        dtLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        dtLabel.setText("Time step in minutes");
        dtText = new Text(timeGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        dtText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        dtText.setText("");
        dtText.addKeyListener(this);

        /*
         * level group
         */
        Group levelGroup = new Group(container, SWT.NONE);
        GridData levelGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        levelGroupLayoutData.horizontalSpan = 3;
        levelGroup.setLayoutData(levelGroupLayoutData);
        levelGroup.setLayout(new GridLayout(2, false));
        levelGroup.setText("levels [m]");

        Label levelLabel = new Label(levelGroup, SWT.NONE);
        levelLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        levelLabel.setText("Comma separated list of levels");

        levelText = new Text(levelGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        levelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        levelText.setText("");
        levelText.addKeyListener(this);

        /*
         * global attributes group
         */
        Group attributesGroup = new Group(container, SWT.NONE);
        GridData attributesGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        attributesGroupLayoutData.horizontalSpan = 3;
        attributesGroup.setLayoutData(attributesGroupLayoutData);
        attributesGroup.setLayout(new GridLayout(3, true));
        attributesGroup.setText("Global attributes");

        List<String> exampleItems = new ArrayList<String>();
        exampleItems.add("Conventions: CF-1.3?");
        exampleItems.add("Contact: your@email.here");
        exampleItems.add("References: http://www.yoursite.com");
        exampleItems.add("Comment: any comment here");
        exampleItems
                .add("Distribution_statement: IN NO EVENT SHALL MY COMPANY OR ITS REPRESENTATIVES BE LIABLE... blah blah");

        lvP = new ListViewerProvider();
        lvP.create(attributesGroup, exampleItems);

        setPageComplete(false);
    }

    public void setVisible( boolean visible ) {
        if (!visible) {
            // get out of the page, set all needed parameters
            LinkedHashMap<String, Object> globalAttributesMap = lvP.getItemsMap();
            parentWizard.setGlobalAttributesMap(globalAttributesMap);

            String outputPath = pathText.getText();
            parentWizard.setOutputPath(outputPath);

            String from = fromText.getText();
            if (from.length() > 0) {
                parentWizard.setStartDateString(from);
            }
            String to = toText.getText();
            if (to.length() > 0) {
                parentWizard.setEndDateString(to);
            }
            String dt = dtText.getText();
            if (dt.length() > 0) {
                parentWizard.setTimestepString(dt);
            }
            String levels = levelText.getText();
            if (levels.length() > 0) {
                parentWizard.setLevelsString(levels);
            }
        }

        super.setVisible(visible);
    }

    /**
     * Check all inputs
     */
    private void updatePageComplete() {
        setPageComplete(false);

        /*
        * check output path
        */
        String outputPathStr = pathText.getText();
        if (outputPathStr.length() > 0) {
            File f = new File(outputPathStr).getParentFile();
            if (!f.exists()) {
                setMessage(null);
                setErrorMessage("The output path's base folder doesn't exist.");
                return;
            }
            parentWizard.setOutputPath(outputPathStr);
        } else {
            return;
        }
        
        // mapset
        String mapsetPath = mapsetText.getText();
        File f = new File(mapsetPath);
        if (f.exists()) {
            parentWizard.setMapsetPath(mapsetPath);
        }else{
            return;
        }
        
        
        String fromDateString = fromText.getText();
        String toDateString = toText.getText();
        String dtDateString = dtText.getText();
        if (fromDateString.length() > 0 || toDateString.length() > 0 || dtDateString.length() > 0) {
            // if one time thing exists, all of them have to be defined
            try {
                NcFileWriter.dF.parse(fromDateString);
                NcFileWriter.dF.parse(toDateString);
                Integer.parseInt(dtDateString);
                parentWizard.setStartDateString(fromDateString);
                parentWizard.setEndDateString(toDateString);
                parentWizard.setTimestepString(dtDateString);
            } catch (Exception e) {
                return;
            }
        }

        
        String levelsString = levelText.getText();
        if (levelsString.length() > 0 ) {
            // if it exists, it has to be in comma separated doubles
            try {
                String[] split = levelsString.split(",");
                for( String level : split ) {
                    double d = Double.parseDouble(level.trim());
                    System.out.println(d);
                }
                parentWizard.setLevelsString(levelsString);
            } catch (Exception e) {
                return;
            }
        }
        
        setPageComplete(true);
    }

    public void keyPressed( KeyEvent e ) {
    }

    public void keyReleased( KeyEvent e ) {
        updatePageComplete();
    }

    public void modifyText( ModifyEvent e ) {
        updatePageComplete();
    }

}
