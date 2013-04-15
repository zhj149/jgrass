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
package eu.hydrologis.jgrass.ui.utilities;

import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An input dialog based on input labels.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class MultiInputDialog extends Dialog {

    private HashMap<String, String> messageMap = new HashMap<String, String>();
    private final String[] labels;
    private String message = ""; //$NON-NLS-1$
    private String title = ""; //$NON-NLS-1$

    public MultiInputDialog( Shell parentShell, String title, String message, String... labels ) {
        super(parentShell);
        this.title = title;
        this.message = message;
        this.labels = labels;
        setShellStyle(SWT.DIALOG_TRIM);
    }

    @Override
    protected Control createContents( Composite parent ) {
        getShell().setText(title);

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new GridLayout(2, false));

        Label label = new Label(container, SWT.NONE);
        GridData gdLabel = new GridData(SWT.FILL, SWT.FILL, true, true);
        gdLabel.horizontalSpan = 2;
        label.setLayoutData(gdLabel);
        label.setText(message);

        Label labelFiller = new Label(container, SWT.NONE);
        GridData gdFiller = new GridData(SWT.FILL, SWT.FILL, true, true);
        gdFiller.horizontalSpan = 2;
        labelFiller.setLayoutData(gdFiller);

        for( String lab : labels ) {
            Label titleLabel = new Label(container, SWT.NONE);
            titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            titleLabel.setText(lab);

            final Text text = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            text.setData(lab);
            text.addKeyListener(new KeyAdapter(){
                public void keyReleased( KeyEvent e ) {
                    String str = text.getText();
                    String label = (String) text.getData();
                    messageMap.put(label, str);
                }
            });

        }

        Composite buttonComposite = new Composite(container, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        buttonComposite.setLayoutData(gd);
        buttonComposite.setLayout(new GridLayout(1, true));
        createButtonsForButtonBar(buttonComposite);

        return container;
    }

    public String getStringByLable( String label ) {
        return messageMap.get(label);
    }

}
