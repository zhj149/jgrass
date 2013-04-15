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
 package eu.hydrologis.jgrass.ui.utilities.chooser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Widget that supplies a textfield for the insertion of a numeric value.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class GenericValueWidget extends Composite implements ChooserWidget {
    public final static int CHOOSEFILE = 0;
    public final static int SAVEFILE = 1;
    public final static int CHOOSEFOLDER = 2;

    private Text valueText = null;
    private Label valueLabel = null;

    /**
     * @param parent parent component
     * @param style widget style
     * @param type the dialog mode, can be set thought the static fields
     */
    public GenericValueWidget( Composite parent, int style, String message ) {
        this(parent, style);
        valueLabel.setText(message);
    }

    public GenericValueWidget( Composite parent, int style ) {
        super(parent, style);
        initialize();
    }

    private void initialize() {
        GridData gridData1 = new GridData();
        gridData1.horizontalAlignment = GridData.FILL;
        gridData1.widthHint = ChooserWidget.LABELWIDTHHINT;
        gridData1.verticalAlignment = GridData.CENTER;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = GridData.CENTER;
        gridData.widthHint = ChooserWidget.TEXTFIELDWIDTHHINT;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        valueText = new Text(this, SWT.BORDER);
        valueText.setLayoutData(gridData);
        valueLabel = new Label(this, SWT.NONE);
        valueLabel.setLayoutData(gridData1);
        this.setLayout(gridLayout);
        // this.setSize(new Point(433, 37));
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.ui.utilities.chooser.ChooserWidget#getString()
     */
    public String getString() {
        return valueText.getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.ui.utilities.chooser.ChooserWidget#getObject()
     */
    public Object getObject() {
        return valueText.getText();
    }

} // @jve:decl-index=0:visual-constraint="10,10"
