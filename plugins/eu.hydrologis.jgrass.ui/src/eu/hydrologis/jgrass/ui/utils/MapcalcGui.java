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
package eu.hydrologis.jgrass.ui.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.ui.ExceptionDetailsDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.ui.UiPlugin;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
@SuppressWarnings("nls")
public class MapcalcGui extends SelectionAdapter implements KeyListener {

    public static final String MAPWRAPPER = "\"";

    private static final String MAPCALCS = "mapcalcs";
    private static final String ABS = "abs";
    private static final String EXP = "exp";
    private static final String LOG = "log";
    private static final String LN = "ln";
    private static final String ATAN = "atan";
    private static final String TAN = "tan";
    private static final String COS = "cos";
    private static final String SIN = "sin";
    private static final String PLUS = "+";
    private static final String SQRT = "sqrt";
    private static final String DOT = ".";
    private static final String _0 = "0";
    private static final String MIN = "-";
    private static final String _3 = "3";
    private static final String _2 = "2";
    private static final String _1 = "1";
    private static final String AST = "*";
    private static final String _6 = "6";
    private static final String _5 = "5";
    private static final String _4 = "4";
    private static final String DIV = "/";
    private static final String _9 = "9";
    private static final String _8 = "8";
    private static final String _7 = "7";
    private static final String CE = "CE";
    private static final String CAP = "^";
    private static final String CLOSE_B = ")";
    private static final String OPEN_B = "(";
    private static final String COMMA = ",";
    private static final String ISNULL = "isnull";
    private static final String NULL = "null";
    private static final String IF = "if()";
    private static final String LE = "<=";
    private static final String GE = ">=";
    private static final String LT = "<";
    private static final String GT = ">";
    private static final String OR = "OR";
    private static final String AND = "AND";
    private static final String NONEQUAL = "!=";
    private static final String EQUALS = "==";

    private StyledText functionAreaText;

    private Color greenColor = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
    private Color blueColor = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
    private Color redColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
    private Color cyanColor = Display.getDefault().getSystemColor(SWT.COLOR_CYAN);
    private Color magentaColor = Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA);

    private String[] mapsArray;
    private Dialog dialog;
    private ScopedPreferenceStore m_preferences;
    private String[] oldMapcalcsSplits;
    private Text resultText;
    private String mapsetPath;
    private String result;
    private String function;

    public MapcalcGui() {

        m_preferences = (ScopedPreferenceStore) ConsoleEditorPlugin.getDefault().getPreferenceStore();
        mapsetPath = m_preferences.getString("string@option@ConsoleMapsetFolder");
        if (mapsetPath == null || !(new File(mapsetPath).exists())) {
            Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
            msgBox.setMessage("No mapset has been defined or doesn't exist. Please check your preferences");
            msgBox.open();
            return;
        }
        // this.mapsArray = new String[]{"map1", "map2"};

        String cellPath = mapsetPath + File.separator + JGrassConstants.CELL;
        File cellFile = new File(cellPath);
        File[] filesList = cellFile.listFiles();
        List<String> mapNames = new ArrayList<String>();
        for( File file : filesList ) {
            if (file.isFile()) {
                mapNames.add(file.getName());
            }
        }
        mapsArray = (String[]) mapNames.toArray(new String[mapNames.size()]);

        // gisbase = m_preferences.getString("string@option@@ConsoleGrassenvGisbase");
        // if (gisbase == null || !(new File(gisbase).exists())) {
        // Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        // MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
        // msgBox
        // .setMessage("No gisbase has been defined or doesn't exist. Please check your preferences");
        // msgBox.open();
        // return;
        // }
        // list of old mapcalcs to load if wanted
        String oldMapcalcs = m_preferences.getString(MAPCALCS);
        oldMapcalcsSplits = oldMapcalcs.split("#");

        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        dialog = new Dialog(shell){
            protected Control createContents( Composite parent ) {
                return createGui(parent);
            }
            protected Point getInitialSize() {
                return new Point(500, 600);
            }
        };

        createGui(shell);
        dialog.setBlockOnOpen(true);
        dialog.open();

    }

    private Control createGui( Composite shell ) {
        Composite parent = new Composite(shell, SWT.None);
        final GridLayout calculatorGridLayout = new GridLayout();
        calculatorGridLayout.marginRight = 5;
        calculatorGridLayout.marginLeft = 5;
        calculatorGridLayout.marginBottom = 5;
        calculatorGridLayout.marginTop = 5;
        calculatorGridLayout.marginWidth = 10;
        calculatorGridLayout.marginHeight = 2;
        calculatorGridLayout.horizontalSpacing = 5;
        calculatorGridLayout.numColumns = 8;
        calculatorGridLayout.makeColumnsEqualWidth = true;
        parent.setLayout(calculatorGridLayout);
        GridData parentGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        parent.setLayoutData(parentGridData);

        // the text area
        Group functionGroup = new Group(parent, SWT.NONE);
        GridData functionGroupGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        functionGroupGridData.horizontalSpan = 8;
        functionGroup.setLayoutData(functionGroupGridData);
        functionGroup.setLayout(new GridLayout(1, false));
        functionGroup.setText("function area");

        functionAreaText = new StyledText(functionGroup, SWT.LEFT | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        functionAreaText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        functionAreaText.setEditable(true);
        functionAreaText.setText("");
        GridData functionAreaGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        functionAreaGridData.heightHint = 130;
        functionAreaText.setLayoutData(functionAreaGridData);
        functionAreaText.addKeyListener(this);

        final Combo oldMapcalcsCombo = new Combo(functionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData comboGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        oldMapcalcsCombo.setLayoutData(comboGD);
        String[] tmp = new String[oldMapcalcsSplits.length];
        for( int i = 0; i < tmp.length; i++ ) {
            int l = 40;
            if (l > oldMapcalcsSplits[i].length()) {
                tmp[i] = oldMapcalcsSplits[i].substring(0, oldMapcalcsSplits[i].length());
            } else {
                tmp[i] = oldMapcalcsSplits[i].substring(0, l) + " ...";
            }
        }

        oldMapcalcsCombo.setItems(tmp);
        oldMapcalcsCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int selectionIndex = oldMapcalcsCombo.getSelectionIndex();
                if (selectionIndex != -1) {
                    // String sel = oldMapcalcsCombo.getItem(selectionIndex);
                    String sel = oldMapcalcsSplits[selectionIndex];
                    if (sel.length() < 1)
                        return;
                    functionAreaText.insert(sel);
                    checkStyle();
                }

            }
        });
        // if(bacino_chiese_pit < 2000 || bacino_chiese_pit >2500, null() , 1000)
        // result setting line
        Label resultLabel = new Label(parent, SWT.NONE);
        GridData resultGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        resultGridData.horizontalSpan = 3;
        resultLabel.setLayoutData(resultGridData);
        resultLabel.setText("resulting map:");

        resultText = new Text(parent, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData resultTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        resultTextGridData.horizontalSpan = 5;
        resultText.setLayoutData(resultTextGridData);
        resultText.setText("");

        for( int i = 0; i < 8; i++ ) {
            Label label = new Label(parent, SWT.NONE);
            GridData lGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
            lGridData.heightHint = 15;
            label.setLayoutData(lGridData);

        }

        Button mapButton = new Button(parent, SWT.FLAT);
        mapButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        mapButton.setText("map:");
        final Combo mapsCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData mapsComboData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        mapsComboData.horizontalSpan = 3;
        mapsCombo.setLayoutData(mapsComboData);
        mapsCombo.setItems(mapsArray);
        mapButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int selectionIndex = mapsCombo.getSelectionIndex();
                functionAreaText.insert(MAPWRAPPER + mapsArray[selectionIndex] + MAPWRAPPER + " ");
                int offset = functionAreaText.getCaretOffset();
                int newOffset = offset + mapsArray[selectionIndex].length() + 3;
                functionAreaText.setCaretOffset(newOffset);
                checkStyle();
            }
        });

        // if and constructs
        Button ifButton = new Button(parent, SWT.FLAT);
        ifButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ifButton.setText(IF);
        ifButton.addSelectionListener(this);
        Button nullButton = new Button(parent, SWT.FLAT);
        nullButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        nullButton.setText(NULL);
        nullButton.addSelectionListener(this);
        Button isnullButton = new Button(parent, SWT.FLAT);
        isnullButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        isnullButton.setText(ISNULL);
        isnullButton.addSelectionListener(this);
        // Button sqrtButton = new Button(parent, SWT.FLAT);
        // sqrtButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        // sqrtButton.setText(SQRT);
        // sqrtButton.addSelectionListener(this);

        // add map
        // Button openBraceButton = new Button(parent, SWT.FLAT);
        // openBraceButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        // openBraceButton.setText(OPEN_B);
        // openBraceButton.addSelectionListener(this);
        // Button closeBraceButton = new Button(parent, SWT.FLAT);
        // closeBraceButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        // closeBraceButton.setText(CLOSE_B);
        // closeBraceButton.addSelectionListener(this);
        // Button capButton = new Button(parent, SWT.FLAT);
        // capButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        // capButton.setText(CAP);
        // capButton.addSelectionListener(this);
        Button backspaceButton = new Button(parent, SWT.FLAT);
        backspaceButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        backspaceButton.setText(CE);
        backspaceButton.addSelectionListener(this);

        // logical operators
        Button equalsButton = new Button(parent, SWT.FLAT);
        equalsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        equalsButton.setText(EQUALS);
        equalsButton.addSelectionListener(this);
        Button nonequalsButton = new Button(parent, SWT.FLAT);
        nonequalsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        nonequalsButton.setText(NONEQUAL);
        nonequalsButton.addSelectionListener(this);
        Button andButton = new Button(parent, SWT.FLAT);
        andButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        andButton.setText(AND);
        andButton.addSelectionListener(this);
        Button orButton = new Button(parent, SWT.FLAT);
        orButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        orButton.setText(OR);
        orButton.addSelectionListener(this);
        Button maiorButton = new Button(parent, SWT.FLAT);
        maiorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        maiorButton.setText(GT);
        maiorButton.addSelectionListener(this);
        Button minorButton = new Button(parent, SWT.FLAT);
        minorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        minorButton.setText(LT);
        minorButton.addSelectionListener(this);
        Button maiorequalButton = new Button(parent, SWT.FLAT);
        maiorequalButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        maiorequalButton.setText(GE);
        maiorequalButton.addSelectionListener(this);
        Button minorequalButton = new Button(parent, SWT.FLAT);
        minorequalButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        minorequalButton.setText(LE);
        minorequalButton.addSelectionListener(this);

        // trigon
        Button sinButton = new Button(parent, SWT.FLAT);
        sinButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        sinButton.setText(SIN);
        sinButton.addSelectionListener(this);
        Button cosButton = new Button(parent, SWT.FLAT);
        cosButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        cosButton.setText(COS);
        cosButton.addSelectionListener(this);
        Button tanButton = new Button(parent, SWT.FLAT);
        tanButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        tanButton.setText(TAN);
        tanButton.addSelectionListener(this);
        Button atanButton = new Button(parent, SWT.FLAT);
        atanButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        atanButton.setText(ATAN);
        atanButton.addSelectionListener(this);
        Button lnButton = new Button(parent, SWT.FLAT);
        lnButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        lnButton.setText(LOG);
        lnButton.addSelectionListener(this);
        Button sqrtButton1 = new Button(parent, SWT.FLAT);
        sqrtButton1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        sqrtButton1.setText(SQRT);
        sqrtButton1.addSelectionListener(this);
        Button expButton = new Button(parent, SWT.FLAT);
        expButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        expButton.setText(EXP);
        expButton.addSelectionListener(this);
        Button absButton = new Button(parent, SWT.FLAT);
        absButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        absButton.setText(ABS);
        absButton.addSelectionListener(this);

        for( int i = 0; i < 8; i++ ) {
            Label label = new Label(parent, SWT.NONE);
            GridData lGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
            lGridData.heightHint = 15;
            label.setLayoutData(lGridData);

        }

        // numbers and signs
        Button _7Button = new Button(parent, SWT.FLAT);
        GridData gD7 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD7.horizontalSpan = 2;
        _7Button.setLayoutData(gD7);
        _7Button.setText(_7);
        _7Button.addSelectionListener(this);
        Button _8Button = new Button(parent, SWT.FLAT);
        GridData gD8 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD8.horizontalSpan = 2;
        _8Button.setLayoutData(gD8);
        _8Button.setText(_8);
        _8Button.addSelectionListener(this);
        Button _9Button = new Button(parent, SWT.FLAT);
        GridData gD9 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD9.horizontalSpan = 2;
        _9Button.setLayoutData(gD9);
        _9Button.setText(_9);
        _9Button.addSelectionListener(this);
        Button _divButton = new Button(parent, SWT.FLAT);
        GridData gDdiv = new GridData(SWT.FILL, SWT.FILL, true, false);
        gDdiv.horizontalSpan = 2;
        _divButton.setLayoutData(gDdiv);
        _divButton.setText(DIV);
        _divButton.addSelectionListener(this);
        Button _4Button = new Button(parent, SWT.FLAT);
        GridData gD4 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD4.horizontalSpan = 2;
        _4Button.setLayoutData(gD4);
        _4Button.setText(_4);
        _4Button.addSelectionListener(this);
        Button _5Button = new Button(parent, SWT.FLAT);
        GridData gD5 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD5.horizontalSpan = 2;
        _5Button.setLayoutData(gD5);
        _5Button.setText(_5);
        _5Button.addSelectionListener(this);
        Button _6Button = new Button(parent, SWT.FLAT);
        GridData gD6 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD6.horizontalSpan = 2;
        _6Button.setLayoutData(gD6);
        _6Button.setText(_6);
        _6Button.addSelectionListener(this);
        Button _astButton = new Button(parent, SWT.FLAT);
        GridData gDast = new GridData(SWT.FILL, SWT.FILL, true, false);
        gDast.horizontalSpan = 2;
        _astButton.setLayoutData(gDast);
        _astButton.setText(AST);
        _astButton.addSelectionListener(this);
        Button _1Button = new Button(parent, SWT.FLAT);
        GridData gD1 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD1.horizontalSpan = 2;
        _1Button.setLayoutData(gD1);
        _1Button.setText(_1);
        _1Button.addSelectionListener(this);
        Button _2Button = new Button(parent, SWT.FLAT);
        GridData gD2 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD2.horizontalSpan = 2;
        _2Button.setLayoutData(gD2);
        _2Button.setText(_2);
        _2Button.addSelectionListener(this);
        Button _3Button = new Button(parent, SWT.FLAT);
        GridData gD3 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD3.horizontalSpan = 2;
        _3Button.setLayoutData(gD3);
        _3Button.setText(_3);
        _3Button.addSelectionListener(this);
        Button _minButton = new Button(parent, SWT.FLAT);
        GridData gDmin = new GridData(SWT.FILL, SWT.FILL, true, false);
        gDmin.horizontalSpan = 2;
        _minButton.setLayoutData(gDmin);
        _minButton.setText(MIN);
        _minButton.addSelectionListener(this);
        Button _0Button = new Button(parent, SWT.FLAT);
        GridData gD0 = new GridData(SWT.FILL, SWT.FILL, true, false);
        gD0.horizontalSpan = 2;
        _0Button.setLayoutData(gD0);
        _0Button.setText(_0);
        _0Button.addSelectionListener(this);
        Button _dotButton = new Button(parent, SWT.FLAT);
        GridData gDdot = new GridData(SWT.FILL, SWT.FILL, true, false);
        gDdot.horizontalSpan = 2;
        _dotButton.setLayoutData(gDdot);
        _dotButton.setText(DOT);
        _dotButton.addSelectionListener(this);
        Button _newcommaButton = new Button(parent, SWT.FLAT);
        GridData gDplusminus = new GridData(SWT.FILL, SWT.FILL, true, false);
        gDplusminus.horizontalSpan = 2;
        _newcommaButton.setLayoutData(gDplusminus);
        _newcommaButton.setText(COMMA);
        _newcommaButton.addSelectionListener(this);
        Button _plusButton = new Button(parent, SWT.FLAT);
        GridData gDplus = new GridData(SWT.FILL, SWT.FILL, true, false);
        gDplus.horizontalSpan = 2;
        _plusButton.setLayoutData(gDplus);
        _plusButton.setText(PLUS);
        _plusButton.addSelectionListener(this);

        for( int i = 0; i < 8; i++ ) {
            Label label = new Label(parent, SWT.NONE);
            GridData lGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
            lGridData.heightHint = 15;
            label.setLayoutData(lGridData);

        }

        for( int i = 0; i < 2; i++ ) {
            Label label = new Label(parent, SWT.NONE);
            GridData lGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
            lGridData.heightHint = 15;
            label.setLayoutData(lGridData);
        }

        Button okButton = new Button(parent, SWT.FLAT);
        GridData gDok = new GridData(SWT.FILL, SWT.FILL, true, false);
        gDok.horizontalSpan = 2;
        okButton.setLayoutData(gDok);
        okButton.setText("Ok");
        okButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                try {
                    executeMapcalc();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    String message = "An error occurred during the map calculation";
                    ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, UiPlugin.PLUGIN_ID, e1);

                }
            }
        });

        Button cancelButton = new Button(parent, SWT.FLAT);
        GridData gDcancel = new GridData(SWT.FILL, SWT.FILL, true, false);
        gDcancel.horizontalSpan = 2;
        cancelButton.setLayoutData(gDcancel);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                dialog.close();
            }
        });

        for( int i = 0; i < 2; i++ ) {
            Label label = new Label(parent, SWT.NONE);
            GridData lGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
            lGridData.heightHint = 15;
            label.setLayoutData(lGridData);
        }

        return parent;
    }
    public static void main( String[] args ) {

        Display display = new Display();

        Shell shell = new Shell(display, SWT.CLOSE | SWT.PRIMARY_MODAL);
        shell.setLayout(new GridLayout(1, false));

        MapcalcGui mGui = new MapcalcGui();
        mGui.createGui(shell);

        Monitor primary = display.getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = shell.getBounds();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;
        shell.setLocation(x, y);
        shell.setSize(500, 700);
        shell.open();

        while( !shell.isDisposed() ) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    public void widgetSelected( SelectionEvent e ) {
        Widget widget = e.widget;

        if (widget instanceof Button) {
            Button button = (Button) widget;
            String text = button.getText();
            if (text.equals(IF)) {
                functionAreaText.insert("if(?,?,?) ");
                int offset = functionAreaText.getCaretOffset();
                int newOffset = offset + 3;
                functionAreaText.setCaretOffset(newOffset);
                functionAreaText.setSelection(newOffset, newOffset + 1);
            } else if (text.equals(NULL)) {
                functionAreaText.insert("null() ");
                int offset = functionAreaText.getCaretOffset();
                int newOffset = offset + 6;
                functionAreaText.setCaretOffset(newOffset);
            } else if (text.equals(OR)) {
                functionAreaText.insert("|| ");
                int offset = functionAreaText.getCaretOffset();
                int newOffset = offset + 5;
                functionAreaText.setCaretOffset(newOffset);
            } else if (text.equals(AND)) {
                functionAreaText.insert("&& ");
                int offset = functionAreaText.getCaretOffset();
                int newOffset = offset + 5;
                functionAreaText.setCaretOffset(newOffset);
            } else if (text.equals(ISNULL)) {
                functionAreaText.insert("isnull(map) ");
                int offset = functionAreaText.getCaretOffset();
                int newOffset = offset + 7;
                functionAreaText.setCaretOffset(newOffset);
                functionAreaText.setSelection(newOffset, newOffset + 3);
            } else if (text.equals(SIN) || text.equals(COS) || text.equals(TAN) || text.equals(ATAN) || text.equals(LN)
                    || text.equals(LOG) || text.equals(EXP) || text.equals(ABS) || text.equals(SQRT)) {
                functionAreaText.insert(text + "(?) ");
                int offset = functionAreaText.getCaretOffset();
                int newOffset = offset + text.length() + 1;
                functionAreaText.setCaretOffset(newOffset);
                functionAreaText.setSelection(newOffset, newOffset + 1);
            } else if (text.equals(_0) || text.equals(_1) || text.equals(_2) || text.equals(_3) || text.equals(_4)
                    || text.equals(_5) || text.equals(_6) || text.equals(_7) || text.equals(_8) || text.equals(_9)) {
                functionAreaText.insert(text);
                int offset = functionAreaText.getCaretOffset();
                int newOffset = offset + text.length();
                functionAreaText.setCaretOffset(newOffset);
            } else if (text.equals(CE)) {
                String funtext = functionAreaText.getText();
                if (funtext.length() > 0) {
                    funtext = funtext.substring(0, funtext.length() - 1);
                    functionAreaText.setText(funtext);
                    functionAreaText.setCaretOffset(funtext.length());
                }
            } else {
                functionAreaText.insert(text + " ");
                int offset = functionAreaText.getCaretOffset();
                functionAreaText.setCaretOffset(offset + text.length() + 1);
            }

            checkStyle();
        }

    }
    public void keyPressed( KeyEvent e ) {
    }

    public void keyReleased( KeyEvent e ) {
        checkStyle();
    }

    private void checkStyle() {
        String text = functionAreaText.getText();

        // color maps
        for( int i = 0; i < mapsArray.length; i++ ) {
            String map = MAPWRAPPER + mapsArray[i] + MAPWRAPPER;

            int index = 0;
            while( (index = text.indexOf(map, index)) != -1 ) {
                StyleRange styleRange = new StyleRange();
                styleRange.start = index;
                int length = map.length();
                styleRange.length = length;
                // styleRange.foreground = greenColor;
                styleRange.fontStyle = SWT.BOLD | SWT.ITALIC;
                functionAreaText.setStyleRange(styleRange);

                index = index + length;
            }
        }
        // brackets
        String[] textSplit = text.split("\\(|\\)"); //$NON-NLS-1$
        if (textSplit.length > 1) {

            List<Integer> bracketPositions = new ArrayList<Integer>();
            int position = 0;
            for( int i = 0; i < textSplit.length - 1; i++ ) {
                position = position + textSplit[i].length() + 1;
                bracketPositions.add(position - 1);
            }

            for( Integer pos : bracketPositions ) {
                StyleRange styleRange = new StyleRange();
                styleRange.start = pos;
                styleRange.length = 1;
                styleRange.foreground = greenColor;
                styleRange.fontStyle = SWT.NORMAL;
                functionAreaText.setStyleRange(styleRange);
            }
        }

        textSplit = text.split("\\?"); //$NON-NLS-1$
        if (textSplit.length > 1) {

            List<Integer> bracketPositions = new ArrayList<Integer>();
            int position = 0;
            for( int i = 0; i < textSplit.length - 1; i++ ) {
                position = position + textSplit[i].length() + 1;
                bracketPositions.add(position - 1);
            }

            for( Integer pos : bracketPositions ) {
                StyleRange styleRange = new StyleRange();
                styleRange.start = pos;
                styleRange.length = 1;
                styleRange.foreground = redColor;
                styleRange.fontStyle = SWT.NORMAL;
                functionAreaText.setStyleRange(styleRange);
            }
        }

    }

    private void executeMapcalc() throws Exception {
        result = resultText.getText().trim();
        function = functionAreaText.getText();

        if (result == null || result.length() < 1) {
            Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
            msgBox.setMessage("The resulting map was not supplied properly.");
            msgBox.open();
            return;
        }
        if (function == null || function.length() < 1) {
            Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
            msgBox.setMessage("The function area is empty.");
            msgBox.open();
            return;
        }
        dialog.close();

        // save the new function
        saveHistory();

        
        JGrassRegion region = new JGrassRegion(mapsetPath + File.separator + JGrassConstants.WIND);
        String cellFolderPath = mapsetPath + File.separator + JGrassConstants.CELL;
        MapcalcJiffler jiffler = new MapcalcJiffler(function, result, mapsArray, region, cellFolderPath);
        jiffler.exec();

        // StrBuilder sB = new StrBuilder();
        // sB.append("# MAPSET= ").append(mapsetPath);
        // sB.append("\n");
        // sB.append("mapcalc {\n");
        // sB.append("    result \"").append(result).append("\"\n");
        // sB.append("    function \"\"\"\n");
        // sB.append(function).append("\n");
        // sB.append("    \"\"\"\n");
        // sB.append("}\n");
        //
        // ConsoleCommandExecutor cExe = new ConsoleCommandExecutor();
        // cExe.execute("mapcalc", sB.toString(), mapsetPath, null,
        // ConsoleCommandExecutor.OUTPUTTYPE_BTCONSOLE, null, null);
    }

    private void saveHistory() {
        StringBuilder sB = new StringBuilder();

        boolean contains = false;
        for( String calc : oldMapcalcsSplits ) {
            if (calc.trim().equals(function.trim())) {
                contains = true;
                break;
            }
        }
        if (!contains)
            sB.append(function).append("#");
        if (oldMapcalcsSplits.length > 20) {
            // take away one
            for( int i = 0; i < oldMapcalcsSplits.length - 1; i++ ) {
                sB.append(oldMapcalcsSplits[i]).append("#");
            }
        } else {
            for( int i = 0; i < oldMapcalcsSplits.length; i++ ) {
                sB.append(oldMapcalcsSplits[i]).append("#");
            }
        }
        m_preferences.putValue(MAPCALCS, sB.toString());
    }

}
