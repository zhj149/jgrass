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
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import eu.hydrologis.jgrass.netcdf.export.core.NcFileWriter;
import eu.hydrologis.jgrass.ui.utilities.JGRasterChooserDialog;
import eu.udig.catalog.jgrass.core.JGrassMapGeoResource;

/**
 * Dialog to gather the info that describe a netcdf layer.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class LayersDialog extends Dialog implements Listener {

    public static final String TIMEKEY = "TIMEKEY";
    public static final String LEVELKEY = "LEVELKEY";
    public static final String MAPKEY = "MAPKEY";

    private final HashMap<String, String> parametersMap;

    private final boolean hasTime;
    private final boolean hasLevels;
    private final NcFileWriter ncFW;
    private Combo timeCombo;
    private Combo levelCombo;
    private Text mapText;
    private Button okButton;

    protected LayersDialog( Shell parentShell, NcFileWriter ncFW,
            HashMap<String, String> parametersMap, boolean hasTime, boolean hasLevels ) {
        super(parentShell);
        this.ncFW = ncFW;
        this.parametersMap = parametersMap;
        this.hasTime = hasTime;
        this.hasLevels = hasLevels;
        setShellStyle(SWT.DIALOG_TRIM);
    }

    @Override
    protected Control createContents( Composite parent ) {
        getShell().setText("New Layer");

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new GridLayout(3, false));

        // time
        if (hasTime) {
            Label timeLabel = new Label(container, SWT.NONE);
            timeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            timeLabel.setText("timestep");

            List<String> timesteps = ncFW.getDatesList();
            String[] timeArray = (String[]) timesteps.toArray(new String[timesteps.size()]);
            timeCombo = new Combo(container, SWT.DROP_DOWN);
            GridData ld = new GridData(SWT.FILL, SWT.CENTER, true, false);
            ld.horizontalSpan = 2;
            timeCombo.setLayoutData(ld);
            timeCombo.setItems(timeArray);
            timeCombo.select(0);
            timeCombo.addListener(SWT.Selection, this);
            parametersMap.put(TIMEKEY, timeArray[0]);
        }

        // levels
        if (hasLevels) {
            Label levelsLabel = new Label(container, SWT.NONE);
            levelsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            levelsLabel.setText("level");

            double[] levels = ncFW.getLevels();
            String[] levelsArray = new String[levels.length];
            for( int i = 0; i < levels.length; i++ ) {
                levelsArray[i] = String.valueOf(levels[i]);
            }

            levelCombo = new Combo(container, SWT.DROP_DOWN);
            GridData ld = new GridData(SWT.FILL, SWT.CENTER, true, false);
            ld.horizontalSpan = 2;
            levelCombo.setLayoutData(ld);
            levelCombo.setItems(levelsArray);
            levelCombo.select(0);
            levelCombo.addListener(SWT.Selection, this);
            parametersMap.put(LEVELKEY, levelsArray[0]);
        }

        // raster
        Label mapLabel = new Label(container, SWT.NONE);
        mapLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mapLabel.setText("raster map");

        mapText = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        mapText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mapText.setEditable(false);
        mapText.setText("");

        final Button mapButton = new Button(container, SWT.BORDER | SWT.PUSH);
        GridData gd2 = new GridData();
        gd2.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
        mapButton.setLayoutData(gd2);
        mapButton.setText("..."); //$NON-NLS-1$
        mapButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                JGRasterChooserDialog cDialog = new JGRasterChooserDialog(ncFW.getMapsetPath());
                cDialog.open(mapButton.getShell(), SWT.SINGLE);

                List<JGrassMapGeoResource> sel = cDialog.getSelectedResources();
                if (sel.size() == 0) {
                    return;
                }
                File mapFile = sel.get(0).getMapFile();

                String mapPath = mapFile.getAbsolutePath();
                String mapName = mapFile.getName();

                mapText.setText(mapName);

                parametersMap.put(MAPKEY, mapPath);
                
                okButton.setEnabled(true);
            }
        });

        // buttons
        Composite buttonComposite = new Composite(container, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        buttonComposite.setLayoutData(gd);
        buttonComposite.setLayout(new GridLayout(1, true));
        
        createButtonsForButtonBar(buttonComposite);
        okButton = getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(false);


        return container;
    }

    public void handleEvent( Event event ) {
        Widget widget = event.widget;

        if (widget.equals(timeCombo)) {
            int selectionIndex = timeCombo.getSelectionIndex();
            String item = timeCombo.getItem(selectionIndex);
            parametersMap.put(TIMEKEY, item);
        } else if (widget.equals(levelCombo)) {
            int selectionIndex = levelCombo.getSelectionIndex();
            String item = levelCombo.getItem(selectionIndex);
            parametersMap.put(LEVELKEY, item);
        }
    }

}