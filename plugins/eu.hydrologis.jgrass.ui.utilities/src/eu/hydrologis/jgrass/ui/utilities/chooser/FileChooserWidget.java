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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

/**
 * Widget that supplies a textfield connected to a browse button. It can handle open file, save file
 * and open folder.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class FileChooserWidget extends Composite implements SelectionListener, ChooserWidget {
    public final static int CHOOSEFILE = 0;
    public final static int SAVEFILE = 1;
    public final static int CHOOSEFOLDER = 2;

    private Composite parent = null;
    private Text chosenFileText = null;
    private Button chooseFileButton = null;
    private int type = 0;
    private String choosenFile = null;
    private String[] extentions;

    /**
     * @param parent parent component
     * @param style widget style
     * @param type the dialog mode, can be set thought the static fields
     * @param extentions extentions for filter (can be null)
     */
    public FileChooserWidget( Composite parent, int style, int type, String[] extentions) {
        this(parent, style);
        this.type = type;
        this.parent = parent;
        this.extentions = extentions;
        
    }

    /**
     * @param parent parent component
     * @param style widget style
     * @param type the dialog mode, can be set thought the static fields
     */
    public FileChooserWidget( Composite parent, int style, int type ) {
        this(parent, style);
        this.type = type;
        this.parent = parent;
    }

    public FileChooserWidget( Composite parent, int style ) {
        super(parent, style);
        initialize();
    }

    private void initialize() {
        GridData gridData1 = new GridData();
        gridData1.grabExcessHorizontalSpace = false;
        gridData1.verticalAlignment = GridData.CENTER;
        gridData1.widthHint = ChooserWidget.BUTTONWIDTHHINT;
        gridData1.horizontalAlignment = GridData.BEGINNING;
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.widthHint = ChooserWidget.TEXTFIELDWIDTHHINT;
        gridData.verticalAlignment = GridData.CENTER;
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        chosenFileText = new Text(this, SWT.BORDER);
        chosenFileText.setLayoutData(gridData);
        chooseFileButton = new Button(this, SWT.NONE);
        chooseFileButton.setText("Browse");
        chooseFileButton.setLayoutData(gridData1);
        chooseFileButton.addSelectionListener(this);
        this.setLayout(gridLayout);
        // this.setSize(new Point(433, 37));
    }

    public void setTextEditable(boolean isEditable) {
        chosenFileText.setEditable(isEditable);
    }
    
    public void addSelectionListener( SelectionListener external ) {
        chooseFileButton.addSelectionListener(external);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.ui.utilities.chooser.ChooserWidget#getString()
     */
    public String getString() {
        return chosenFileText.getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.hydrologis.jgrass.ui.utilities.chooser.ChooserWidget#getObject()
     */
    public Object getObject() {
        return chosenFileText.getText();
    }

    public void widgetDefaultSelected( SelectionEvent e ) {
    }

    public void widgetSelected( SelectionEvent e ) {
        if (type == CHOOSEFILE) {
            FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.OPEN);
            if(extentions != null) fileDialog.setFilterExtensions(extentions);
            choosenFile = fileDialog.open();

        } else if (type == SAVEFILE) {
            FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.SAVE);
            choosenFile = fileDialog.open();

        } else if (type == CHOOSEFOLDER) {
            DirectoryDialog fileDialog = new DirectoryDialog(parent.getShell(), SWT.OPEN);
            choosenFile = fileDialog.open();

        }
        if (choosenFile == null || choosenFile.length() < 1) {
            choosenFile = "";
        }
        chosenFileText.setText(choosenFile);

    }

} // @jve:decl-index=0:visual-constraint="10,10"
