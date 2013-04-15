/*
 * UIBuilder - a framework to build user interfaces out from XML files
 * Copyright (C) 2007-2008 Patrick Ohnewein
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

package eu.hydrologis.jgrass.uibuilder.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A custom LayoutManager which allows us to place the children on the client area in an easy but
 * advanced way.
 * 
 * <p>The layout manager positions the child components on the container using spreadsheet annotations:</p>
 * 
 * <pre>
 * A0 B0 C0 D0 E0 ... AA0 ...
 * 
 * A1 B1 C1 D1 E1 ... AA1 ...
 * 
 * ...
 * 
 * An Bn Cn Dn En ... AAn ...
 * </pre>
 * 
 * @author Patrick Ohnewein
 */
public class SWTSpreadsheetLayout extends Layout {

    /**
     * marginWidth specifies the number of pixels of horizontal margin that will be placed along the
     * left and right edges of the layout. The default value is 5.
     */
    public int marginWidth = 5;

    /**
     * marginHeight specifies the number of pixels of vertical margin that will be placed along the
     * top and bottom edges of the layout. The default value is 5.
     */
    public int marginHeight = 5;

    /**
     * marginLeft specifies the number of pixels of horizontal margin that will be placed along the
     * left edge of the layout. The default value is 0.
     */
    public int marginLeft = 0;

    /**
     * marginTop specifies the number of pixels of vertical margin that will be placed along the top
     * edge of the layout. The default value is 0.
     */
    public int marginTop = 0;

    /**
     * marginRight specifies the number of pixels of horizontal margin that will be placed along the
     * right edge of the layout. The default value is 0.
     */
    public int marginRight = 0;

    /**
     * marginBottom specifies the number of pixels of vertical margin that will be placed along the
     * bottom edge of the layout. The default value is 0.
     */
    public int marginBottom = 0;

    /**
     * horizontalSpacing specifies the number of pixels between the right edge of one cell and the
     * left edge of its neighbouring cell to the right. The default value is 5.
     */
    public int horizontalSpacing = 5;

    /**
     * verticalSpacing specifies the number of pixels between the bottom edge of one cell and the
     * top edge of its neighbouring cell underneath. The default value is 5.
     */
    public int verticalSpacing = 5;

    private boolean layoutValid;
    private Rectangle lastClientArea;
    private Control[] lastChildren;

    private int ctnPrefWidth;
    private int ctnPrefHeight;

    private ArrayList<LayoutData> entryList;
    private ArrayList<Span> colSpanList;
    private ArrayList<Span> rowSpanList;

    private final String layoutSettings;

    /**
     * Constructor.
     * 
     * @param layoutSettings
     */
    public SWTSpreadsheetLayout( String layoutSettings ) {
        this.layoutSettings = layoutSettings;
    }

    /** Constuctor. */
    public SWTSpreadsheetLayout() {
        this(null);
    }

    protected Point computeSize( Composite parent, int hint, int hint2, boolean flushCache ) {
        validateLayout(parent, flushCache);
        return new Point(ctnPrefWidth, ctnPrefHeight);
    }

    protected void layout( Composite parent, boolean flushCache ) {
        validateLayout(parent, flushCache);
        if (entryList != null) {
            for( int i = entryList.size(); i-- > 0; )
                entryList.get(i).transferBounds();
        }
    }

    /**
     * Checks if the layout has to be recalculated and if there is the need, it recalculates it.
     * 
     * @param parent The parent container of the children components.
     */
    private void validateLayout( Composite parent, boolean flushCache ) {

        Rectangle clientArea = parent.getClientArea();
        if (!clientArea.equals(lastClientArea)) {
            lastClientArea = clientArea;
            layoutValid = false;
        }
        Control[] children = parent.getChildren();
        if (children != lastChildren || !Arrays.deepEquals(children, lastChildren)) {
            lastChildren = children;
            layoutValid = false;
        }
        if (flushCache)
            layoutValid = false;

        if (!layoutValid) {
            if (entryList == null)
                entryList = new ArrayList<LayoutData>();
            else
                entryList.clear();

            for( int i = 0; i < children.length; i++ ) {
                Control control = children[i];
                Object lData = control.getLayoutData();
                LayoutData data;
                if (lData instanceof LayoutData)
                    data = (LayoutData) lData;
                else
                    data = new LayoutData(control, (String) lData);
                entryList.add(data);
            }

            // reset container sizes
            ctnPrefWidth = marginLeft + marginWidth * 2 + marginRight;
            ctnPrefHeight = marginTop + marginHeight * 2 + marginBottom;

            // reset span lists
            if (colSpanList == null)
                colSpanList = new ArrayList<Span>();
            else
                colSpanList.clear();
            if (rowSpanList == null)
                rowSpanList = new ArrayList<Span>();
            else
                rowSpanList.clear();

            // add entries to the spans they cover
            if (entryList != null) {
                for( int i = entryList.size(); i-- > 0; ) {
                    LayoutData e = entryList.get(i);

                    // add entry to the column spans it covers
                    int fromIdx = e.getFromCol();
                    int toIdx = e.getToCol();
                    ArrayList<Span> spanList = colSpanList;
                    int spanningLen = Math.max(1, (toIdx - fromIdx) + 1);
                    int minSize = e.getMinimumWidth() / spanningLen;
                    int prefSize = e.getPreferredWidth() / spanningLen;
                    int maxSize = e.getMaximumWidth() / spanningLen;
                    int spanningSpacing = horizontalSpacing * (spanningLen - 1);
                    for( int j = toIdx; j >= fromIdx; j-- ) {
                        Span span = getSpan(spanList, j);
                        if (span == null) {
                            span = new Span(j);
                            spanList.add(span);
                        }
                        span.addEntry(e);
                        span.registerSizes(minSize - spanningSpacing, prefSize - spanningSpacing,
                                maxSize - spanningSpacing);
                    }

                    // add entry to the row spans it covers
                    fromIdx = e.getFromRow();
                    toIdx = e.getToRow();
                    spanList = rowSpanList;
                    // size to be distributed
                    spanningLen = Math.max(1, (toIdx - fromIdx) + 1);
                    minSize = e.getMinimumHeight() / spanningLen;
                    prefSize = e.getPreferredHeight() / spanningLen;
                    maxSize = e.getMaximumHeight() / spanningLen;
                    spanningSpacing = verticalSpacing * (spanningLen - 1);
                    for( int j = toIdx; j >= fromIdx; j-- ) {
                        Span span = getSpan(spanList, j);
                        if (span == null) {
                            span = new Span(j);
                            spanList.add(span);
                        }
                        span.addEntry(e);
                        span.registerSizes(minSize - spanningSpacing, prefSize - spanningSpacing,
                                maxSize - spanningSpacing);
                    }
                }

                // sort spans
                Collections.sort(colSpanList);
                Collections.sort(rowSpanList);

                if (layoutSettings != null) {
                    String[] settings = layoutSettings.trim().split(",");
                    if (settings != null) {
                        for( int i = 0; i < settings.length; i++ ) {
                            String setting = settings[i].trim();
                            int equalIdx = setting.indexOf('=');
                            if (equalIdx > 0) {
                                String spanID = setting.substring(0, equalIdx).trim();
                                int spanIdx = -1;
                                Span span = null;
                                try {
                                    spanIdx = Integer.parseInt(spanID);
                                    span = getSpan(rowSpanList, spanIdx);
                                } catch (Exception ignore) {
                                }
                                if (spanIdx < 0) {
                                    spanIdx = convertLettersToColumnIndex(spanID);
                                    span = getSpan(colSpanList, spanIdx);
                                }
                                if (span != null) {
                                    String value = setting.substring(equalIdx + 1).trim();
                                    for( int valueIdx = 0, len = value.length(); valueIdx < len; valueIdx++ ) {
                                        switch( value.charAt(valueIdx) ) {
                                        case 'D':
                                        case 'd':
                                            span.setDynamicSize(true);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                int colCount = colSpanList.size();
                if (colCount > 0) {
                    ctnPrefWidth += horizontalSpacing * (colCount - 1);
                    for( int i = 0; i < colCount; i++ ) {
                        Span span = colSpanList.get(i);
                        ctnPrefWidth += span.getPreferredSize();
                    }
                }
                if (clientArea.width > ctnPrefWidth) {
                    // there is a delta to be distributed
                    int dynamicSpanCount = 0;
                    for( int i = 0; i < colCount; i++ ) {
                        Span span = colSpanList.get(i);
                        if (span.isDynamicSize())
                            dynamicSpanCount++;
                    }
                    if (dynamicSpanCount > 0) {
                        int ctnDelta = clientArea.width - ctnPrefWidth;
                        int spanDelta = ctnDelta / dynamicSpanCount;
                        for( int i = 0; i < colCount; i++ ) {
                            Span span = colSpanList.get(i);
                            if (span.isDynamicSize())
                                span.setSize(span.getPreferredSize() + spanDelta);
                        }
                    }
                }

                int rowCount = rowSpanList.size();
                if (rowCount > 0) {
                    ctnPrefHeight += verticalSpacing * (rowCount - 1);
                    for( int i = 0; i < rowCount; i++ ) {
                        Span span = rowSpanList.get(i);
                        ctnPrefHeight += span.getPreferredSize();
                    }
                }
                if (clientArea.height > ctnPrefHeight) {
                    // there is a delta to be distributed
                    int dynamicSpanCount = 0;
                    for( int i = 0; i < rowCount; i++ ) {
                        Span span = rowSpanList.get(i);
                        if (span.isDynamicSize())
                            dynamicSpanCount++;
                    }
                    if (dynamicSpanCount > 0) {
                        int ctnDelta = clientArea.height - ctnPrefHeight;
                        int spanDelta = ctnDelta / dynamicSpanCount;
                        for( int i = 0; i < rowCount; i++ ) {
                            Span span = rowSpanList.get(i);
                            if (span.isDynamicSize())
                                span.setSize(span.getPreferredSize() + spanDelta);
                        }
                    }
                }

                // distribute fixed sizes on the spans
                // distribute sizes of no spanning components
                // distribute deltas of spanning components on dynamic spans

                // transfer sizes to the entries
                int start = marginLeft + marginWidth;
                for( int i = 0, count = colSpanList.size(); i < count; i++ ) {
                    Span span = colSpanList.get(i);
                    int spanSize = span.getSize();
                    for( int j = span.getEntryCount(); j-- > 0; ) {
                        LayoutData e = span.getEntry(j);
                        int fromIdx = e.getFromCol();
                        int toIdx = e.getToCol();
                        if (fromIdx == i)
                            e.setXStart(start);
                        if (toIdx == i)
                            e.setXEnd(start + spanSize);
                    }
                    start += spanSize + horizontalSpacing;
                }

                start = marginTop + marginHeight;
                for( int i = 0, count = rowSpanList.size(); i < count; i++ ) {
                    Span span = rowSpanList.get(i);
                    int spanSize = span.getSize();
                    for( int j = span.getEntryCount(); j-- > 0; ) {
                        LayoutData e = span.getEntry(j);
                        int fromIdx = e.getFromRow();
                        int toIdx = e.getToRow();
                        if (fromIdx == i)
                            e.setYStart(start);
                        if (toIdx == i)
                            e.setYEnd(start + spanSize);
                    }
                    start += spanSize + verticalSpacing;
                }
            }

            layoutValid = true;
        }
    }

    /**
     * Returns the span with the given index contained in the given array list.
     * 
     * @param spanList ArrayList with the spans.
     * @param index Index of the span to return.
     * @return The span with the given index or null, if none of the contained spans has the
     *         specified index.
     */
    private static Span getSpan( ArrayList<Span> spanList, int index ) {
        Span span = null;
        for( int j = spanList.size(); j-- > 0; ) {
            Span s = spanList.get(j);
            int idx = s.getIndex();
            if (idx == index) {
                span = s;
                break;
            }
        }
        return span;
    }

    /**
     * Helper function to convert a letter to the column index.
     * 
     * @param letters Letter representing the column (A, B, AA, ..)
     * @return the index of the column.
     */
    public static int convertLettersToColumnIndex( String letters ) {
        int colIdx = 0;
        letters = letters.toLowerCase();
        int pos = 0;
        for( int i = letters.length(); i-- > 0; ) {
            int charValue = letters.charAt(i) - 'a';
            if (pos > 0)
                charValue = (int) Math.pow('z' - 'a' + 1, pos) * (charValue + 1);
            colIdx += charValue;
            pos++;
        }
        return colIdx;
    }

    /**
     * @param args
     */
    /**
     * @param args
     */
    public static void main( String[] args ) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Shell Title");

        shell.setLayout(new SWTSpreadsheetLayout());

        // for (int i = 0; i < 10; i++) {
        // Label l = new Label(shell, SWT.LEFT | SWT.BORDER);
        // l.setText("Label "+i+":");
        // l.setLayoutData("A" + i + "RV");
        // Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
        // text.setLayoutData("B" + i + (i % 4 == 0 ? "FR" : (i % 2 == 0 ? "H" : "LB" )));
        // }

        Label l;
        Text t1;
        Text t2;

        l = new Label(shell, SWT.LEFT | SWT.BORDER);
        l.setText("Label");
        l.setLayoutData("A0V-B0");
        t1 = new Text(shell, SWT.SINGLE | SWT.BORDER);
        t1.setLayoutData("C0");

        l = new Label(shell, SWT.LEFT | SWT.BORDER);
        l.setText("Label");
        l.setLayoutData("A1");
        t1 = new Text(shell, SWT.SINGLE | SWT.BORDER);
        t1.setLayoutData("B1V");
        t2 = new Text(shell, SWT.SINGLE | SWT.BORDER);
        t2.setLayoutData("C1");

        shell.open(); // open shell for user access

        // process all user input events
        while( !shell.isDisposed() ) {
            // process the next event, wait when none available
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose(); // must always clean up
    }

    /**
     * LayoutData
     * 
     * @author Patrick Ohnewein
     */
    public static class LayoutData {

        private final Control control;
        private int fromCol;
        private int fromRow;
        private int toCol;
        private int toRow;
        private int xStart;
        private int yStart;
        private int xEnd;
        private int yEnd;

        private static final int FILL = 0;
        private static final int BEGIN = 1;
        private static final int CENTER = 2;
        private static final int END = 3;

        private int verticalAlign = FILL;
        private int horizontalAlign = FILL;

        /**
         * Constructor.
         * 
         * @param control The control to wrap.
         * @param constraints The constraints describing the positioning of the component.
         */
        public LayoutData( Control control, String constraints ) {
            this.control = control;
            Point prefSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            xEnd = prefSize.x;
            yEnd = prefSize.y;
            
            int len;
            if (constraints != null && (len = constraints.length()) > 0) {
                boolean computeInfo = false;
                int pos = 0;
                final int FROM_COL = 0;
                final int FROM_ROW = 1;
                final int ALIGN = 2;
                final int TO_COL = 3;
                final int TO_ROW = 4;
                int elementIdx = FROM_COL; // 0 = fromCol, 1 = fromRow, 2 = align, 3 = toCol, 4 =
                                            // toRow
                boolean resetToTo = false;
                StringBuffer sbInfo = new StringBuffer();
                do {
                    if (pos < len) {
                        char ch = constraints.charAt(pos);
                        if (Character.isDigit(ch)) {
                            if (elementIdx == FROM_COL || elementIdx == ALIGN
                                    || elementIdx == TO_COL) {
                                computeInfo = true;
                            } else {
                                sbInfo.append(ch);
                                pos++;
                            }
                        } else if (Character.isLetter(ch)) {
                            if (elementIdx == FROM_ROW || elementIdx == TO_ROW) {
                                computeInfo = true;
                            } else {
                                sbInfo.append(ch);
                                pos++;
                            }
                        } else if (ch == '-') {
                            computeInfo = true;
                            resetToTo = true;
                            pos++;
                        } else {
                            // ignore special characters and white spaces
                            pos++;
                        }
                    }
                    if (computeInfo || pos == len) {
                        computeInfo = false;
                        switch( elementIdx ) {
                        case FROM_COL:
                            fromCol = SWTSpreadsheetLayout.convertLettersToColumnIndex(sbInfo
                                    .toString());
                            break;
                        case FROM_ROW:
                            fromRow = Integer.valueOf(sbInfo.toString());
                            break;
                        case ALIGN:
                            for( int infoIdx = 0, infoLen = sbInfo.length(); infoIdx < infoLen; infoIdx++ ) {
                                char ch = sbInfo.charAt(infoIdx);
                                switch( ch ) {
                                // middle for vertical center
                                case 'M':
                                case 'm':
                                    verticalAlign = CENTER;
                                    break;
                                case 'T':
                                case 't':
                                    verticalAlign = BEGIN;
                                    break;
                                case 'B':
                                case 'b':
                                    verticalAlign = END;
                                    break;
                                case 'L':
                                case 'l':
                                    horizontalAlign = BEGIN;
                                    break;
                                // center for horizontal center
                                case 'C':
                                case 'c':
                                    horizontalAlign = CENTER;
                                    break;
                                case 'R':
                                case 'r':
                                    horizontalAlign = END;
                                    break;
                                }
                            }
                            break;
                        case TO_COL:
                            toCol = SWTSpreadsheetLayout.convertLettersToColumnIndex(sbInfo
                                    .toString());
                            break;
                        case TO_ROW:
                            toRow = Integer.valueOf(sbInfo.toString());
                            break;
                        }
                        if (resetToTo) {
                            resetToTo = false;
                            elementIdx = TO_COL;
                        } else
                            elementIdx++;
                        sbInfo.setLength(0);
                    }
                } while( pos < len );

                // ensure to is at least as big as from
                if (toCol < fromCol)
                    toCol = fromCol;
                if (toRow < fromRow)
                    toRow = fromRow;
            }
        }

        /**
         * @return The control wrapped by this entry.
         */
        public Control getControl() {
            return control;
        }

        public int getMinimumWidth() {
            return 0;
        }

        public int getPreferredWidth() {
            return control.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        }

        public int getMaximumWidth() {
            return Integer.MAX_VALUE;
        }

        public int getMinimumHeight() {
            return 0;
        }

        public int getPreferredHeight() {
            return control.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        }

        public int getMaximumHeight() {
            return Integer.MAX_VALUE;
        }

        /**
         * @return Index of the column the component should starts from.
         */
        public int getFromCol() {
            return fromCol;
        }

        /**
         * @return Index of the row the component should starts from.
         */
        public int getFromRow() {
            return fromRow;
        }

        /**
         * @return Index of the column to which the component should extend.
         */
        public int getToCol() {
            return toCol;
        }

        /**
         * @return Index of the row to which the component should extend.
         */
        public int getToRow() {
            return toRow;
        }

        /**
         * Transfers the calculated bounds of this entry to the wrapped control.
         */
        public void transferBounds() {
            int x = xStart;
            int y = yStart;
            int width = xEnd - xStart;
            int height = yEnd - yStart;

            switch( horizontalAlign ) {
            case BEGIN:
                width = Math.min(width, getPreferredWidth());
                break;
            case CENTER:
                width = Math.min(width, getPreferredWidth());
                x = xStart + ((xEnd - xStart - width) / 2);
                break;
            case END:
                width = Math.min(width, getPreferredWidth());
                x = xEnd - width;
                break;
            }
            switch( verticalAlign ) {
            case BEGIN:
                height = Math.min(height, getPreferredHeight());
                break;
            case CENTER:
                height = Math.min(height, getPreferredHeight());
                y = yStart + ((yEnd - yStart - height) / 2);
                break;
            case END:
                height = Math.min(height, getPreferredHeight());
                y = yEnd - height;
                break;
            }

            // System.out.println("control: " + control);
            // System.out.println("xStart: " + xStart);
            // System.out.println("yStart: " + yStart);
            // System.out.println("xEnd : " + xEnd);
            // System.out.println("yEnd : " + yEnd);
            // System.out.println("xEnd - xStart: " + (xEnd - xStart));
            // System.out.println("yEnd - yStart: " + (yEnd - yStart));
            // System.out.println("prefWidth : " + getPreferredWidth());
            // System.out.println("prefHeight : " + getPreferredHeight());

            control.setBounds(x, y, width, height);
        }

        public int getXStart() {
            return xStart;
        }

        public void setXStart( int xStart ) {
            this.xStart = xStart;
        }

        public int getYStart() {
            return yStart;
        }

        public void setYStart( int yStart ) {
            this.yStart = yStart;
        }

        public int getXEnd() {
            return xEnd;
        }

        public void setXEnd( int xEnd ) {
            this.xEnd = xEnd;
        }

        public int getYEnd() {
            return yEnd;
        }

        public void setYEnd( int yEnd ) {
            this.yEnd = yEnd;
        }
    }
}

/**
 * A Span object represents a row or a column in the TableLayout.
 * 
 * @author Patrick Ohnewein
 */
class Span implements Comparable<Span> {

    /** The column or row index. */
    private final int index;

    /** Defines the minimum width (column) or height (row) of the span. */
    private int minSize;

    /** Defines the preferred width (column) or height (row) of the span. */
    private int prefSize;

    /** Defines the maximum width (column) or height (row) of the span. */
    private int maxSize;

    /**
     * Defines width (column) or height (row) of the span. If not set it is negative and prefSize
     * will be returned.
     */
    private int size;

    /** Has the size of this span to be dynamic? */
    private boolean dynamicSize;

    /** List of entries in this span. */
    private ArrayList<SWTSpreadsheetLayout.LayoutData> entryList;

    /**
     * Consructor.
     */
    public Span( int index ) {
        this.index = index;
        size = -1;
    }

    /** reset values for new layout. */
    public void reset() {
        minSize = 0;
        prefSize = 0;
        maxSize = 0;
        size = -1;
        dynamicSize = false;
    }

    /** @return The column or row index of this span. */
    public int getIndex() {
        return index;
    }

    public int compareTo( Span o ) {
        return o == null ? 1 : getIndex() - o.getIndex();
    }

    /**
     * Used to register new sizes. This values will be added to the actual data.
     */
    public void registerSizes( int minSize, int prefSize, int maxSize ) {
        this.minSize = Math.max(this.minSize, minSize);
        this.prefSize = Math.max(this.prefSize, prefSize);
        this.maxSize = Math.min(this.maxSize, maxSize);
    }

    /** @return The minimum width (column) or height (row) of the span. */
    public int getMinimumSize() {
        return minSize;
    }

    /** @return The preferred width (column) or height (row) of the span. */
    public int getPreferredSize() {
        return prefSize;
    }

    /** @return The maximum width (column) or height (row) of the span. */
    public int getMaximumSize() {
        return maxSize;
    }

    /** @return The size of this span. If not set, prefSize will be returned */
    public int getSize() {
        return size < 0 ? prefSize : size;
    }

    /** Sets the size of this span. */
    public void setSize( int size ) {
        this.size = size;
    }

    /** Should the size of this span be dynamic? */
    public void setDynamicSize( boolean dynamicSize ) {
        this.dynamicSize = dynamicSize;
    }

    /** Is the size of this span dynamic? */
    public boolean isDynamicSize() {
        return dynamicSize;
    }

    /**
     * @param e Entry to add to this span.
     */
    public void addEntry( SWTSpreadsheetLayout.LayoutData e ) {
        if (entryList == null)
            entryList = new ArrayList<SWTSpreadsheetLayout.LayoutData>();
        entryList.add(e);
    }

    /**
     * @return The count of entries contained in this span.
     */
    public int getEntryCount() {
        return entryList != null ? entryList.size() : 0;
    }

    /**
     * Get an entry of this span.
     * 
     * @param index 0 based index to identify the entry.
     * @return The entry.
     */
    public SWTSpreadsheetLayout.LayoutData getEntry( int index ) {
        return entryList.get(index);
    }
}
