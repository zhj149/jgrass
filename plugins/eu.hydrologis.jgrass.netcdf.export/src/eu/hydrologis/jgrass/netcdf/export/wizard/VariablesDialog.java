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

import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Dialog to gather the info that describe a netcdf variable.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class VariablesDialog extends Dialog implements Listener {

    public static final String NAMEKEY = "NAMEKEY";
    public static final String DESCRKEY = "DESCRKEY";
    public static final String UNITSKEY = "UNITSKEY";
    public static final String HASTIMEKEY = "HASTIMEKEY";
    public static final String HASLEVELKEY = "HASLEVELKEY";

    private final HashMap<String, String> parametersMap;

    private Text nameText;
    private Text descrText;
    private Text unitsText;
    private Button hasTimeButton;
    private Button hasLevelButton;
    private final boolean hasTime;
    private final boolean hasLevels;

    protected VariablesDialog( Shell parentShell, HashMap<String, String> parametersMap,
            boolean hasTime, boolean hasLevels ) {
        super(parentShell);
        this.parametersMap = parametersMap;
        this.hasTime = hasTime;
        this.hasLevels = hasLevels;
        setShellStyle(SWT.DIALOG_TRIM);
    }

    @Override
    protected Control createContents( Composite parent ) {
        getShell().setText("New Variable");

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new GridLayout(2, false));

        // name
        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        nameLabel.setText("name");

        nameText = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        String nameString = parametersMap.get(NAMEKEY);
        if (nameString == null) {
            nameString = "";
        }
        nameText.setText(nameString);
        nameText.addListener(SWT.KeyUp, this);

        // description
        Label descrLabel = new Label(container, SWT.NONE);
        descrLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        descrLabel.setText("descr");

        descrText = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        descrText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        String descriptionString = parametersMap.get(DESCRKEY);
        if (descriptionString == null) {
            descriptionString = "";
        }
        descrText.setText(descriptionString);
        descrText.addListener(SWT.KeyUp, this);

        // units
        Label unitsLabel = new Label(container, SWT.NONE);
        unitsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        unitsLabel.setText("units");

        unitsText = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        unitsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        String unitsDescrString = parametersMap.get(UNITSKEY);
        if (unitsDescrString == null) {
            unitsDescrString = "";
        }
        unitsText.setText(unitsDescrString);
        unitsText.addListener(SWT.KeyUp, this);

        // has time?
        if (hasTime) {
            hasTimeButton = new Button(container, SWT.CHECK);
            GridData gD1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gD1.horizontalSpan = 2;
            hasTimeButton.setLayoutData(gD1);
            hasTimeButton.setText("this variable has time definition");
            String tmpHasTimeString = parametersMap.get(HASTIMEKEY);
            boolean hasTime = false;
            if (tmpHasTimeString != null) {
                hasTime = Boolean.parseBoolean(tmpHasTimeString);
            }
            hasTimeButton.setSelection(hasTime);
            hasTimeButton.addListener(SWT.Selection, this);
        }

        // has elevation?
        if (hasLevels) {
            hasLevelButton = new Button(container, SWT.CHECK);
            GridData gD2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gD2.horizontalSpan = 2;
            hasLevelButton.setLayoutData(gD2);
            hasLevelButton.setText("this variable has vertical definition");
            String tmpHasLevelString = parametersMap.get(HASLEVELKEY);
            boolean hasLevel = false;
            if (tmpHasLevelString != null) {
                hasLevel = Boolean.parseBoolean(tmpHasLevelString);
            }
            hasLevelButton.setSelection(hasLevel);
            hasLevelButton.addListener(SWT.Selection, this);
        }

        // buttons
        Composite buttonComposite = new Composite(container, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        buttonComposite.setLayoutData(gd);
        buttonComposite.setLayout(new GridLayout(1, true));

        createButtonsForButtonBar(buttonComposite);

        return container;
    }

    public void handleEvent( Event event ) {
        Widget widget = event.widget;

        if (widget.equals(nameText)) {
            parametersMap.put(NAMEKEY, nameText.getText());
        } else if (widget.equals(descrText)) {
            parametersMap.put(DESCRKEY, descrText.getText());
        } else if (widget.equals(unitsText)) {
            parametersMap.put(UNITSKEY, unitsText.getText());
        } else if (widget.equals(hasTimeButton)) {
            parametersMap.put(HASTIMEKEY, String.valueOf(hasTimeButton.getSelection()));
        } else if (widget.equals(hasLevelButton)) {
            parametersMap.put(HASLEVELKEY, String.valueOf(hasLevelButton.getSelection()));
        }
    }

}