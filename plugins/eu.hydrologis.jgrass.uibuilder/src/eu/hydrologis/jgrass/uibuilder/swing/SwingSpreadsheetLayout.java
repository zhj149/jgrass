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

package eu.hydrologis.jgrass.uibuilder.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * A custom LayoutManager which allows us to place the children on the client area in an easy but
 * advanced way.
 * 
 * @author Patrick Ohnewein
 */
public class SwingSpreadsheetLayout implements LayoutManager2 {

    /** List of children components to be lay out on the parent container. */
    private ArrayList<Entry> entryList;

    /** List of column spans. */
    private ArrayList<Span> colSpanList;

    /** List of row spans. */
    private ArrayList<Span> rowSpanList;

    private int ctnMinWidth;
    private int ctnPrefWidth;
    private int ctnMaxWidth;
    private int ctnMinHeight;
    private int ctnPrefHeight;
    private int ctnMaxHeight;

    /** Is the layout of the entries valid? */
    private boolean layoutValid;

    public final void addLayoutComponent( Component comp, Object constraints ) {
        addLayoutComponent((String) constraints, comp);
    }

    public void addLayoutComponent( String constraints, Component comp ) {
        if (entryList == null)
            entryList = new ArrayList<Entry>();
        entryList.add(new Entry(comp, constraints));
        layoutValid = false;
    }

    public void removeLayoutComponent( Component comp ) {
        if (entryList != null) {
            for( int i = entryList.size(); i-- > 0; ) {
                Entry e = entryList.get(i);
                if (e.getComponent() == comp) {
                    entryList.remove(i);
                    layoutValid = false;
                    break;
                }
            }
        }
    }

    public float getLayoutAlignmentX( Container parent ) {
        // validateLayout(parent);
        return 0.5F;
    }

    public float getLayoutAlignmentY( Container parent ) {
        // validateLayout(parent);
        return 0.5F;
    }

    public Dimension maximumLayoutSize( Container parent ) {
        validateLayout(parent);
        return new Dimension(ctnMaxWidth, ctnMaxHeight);
    }

    public Dimension minimumLayoutSize( Container parent ) {
        validateLayout(parent);
        return new Dimension(ctnMinWidth, ctnMinHeight);
    }

    public Dimension preferredLayoutSize( Container parent ) {
        validateLayout(parent);
        return new Dimension(ctnPrefWidth, ctnPrefHeight);
    }

    public void invalidateLayout( Container target ) {
        layoutValid = false;
    }

    public void layoutContainer( Container parent ) {
        validateLayout(parent);

        if (entryList != null) {
            for( int i = entryList.size(); i-- > 0; ) {
                Entry e = entryList.get(i);
                e.getComponent().setBounds(e.getEntryBounds());
            }
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
     * Checks if the layout has to be recalculated and if there is the need, it recalculates it.
     * 
     * @param parent The parent container of the children components.
     */
    private void validateLayout( Container parent ) {
        // TODO here comes the interesting part

        if (!layoutValid) {
            // reset container sizes
            ctnMinWidth = 0;
            ctnPrefWidth = 0;
            ctnMaxWidth = 0;
            ctnMinHeight = 0;
            ctnPrefHeight = 0;
            ctnMaxHeight = 0;

            Rectangle parentBounds = parent.getBounds();

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
                    Entry e = entryList.get(i);

                    // add entry to the column spans it covers
                    int fromIdx = e.getFromCol();
                    int toIdx = e.getToCol();
                    ArrayList<Span> spanList = colSpanList;
                    int spanningLen = Math.max(1, (toIdx - fromIdx) + 1);
                    int minSize = e.getComponent().getMinimumSize().width / spanningLen;
                    int prefSize = e.getComponent().getPreferredSize().width / spanningLen;
                    int maxSize = e.getComponent().getMaximumSize().width / spanningLen;
                    for( int j = toIdx; j-- >= fromIdx; ) {
                        Span span = getSpan(spanList, j);
                        if (span == null) {
                            span = new Span(j);
                            spanList.add(span);
                        }
                        span.addEntry(e);
                        span.registerSizes(minSize, prefSize, maxSize);
                    }

                    // add entry to the row spans it covers
                    fromIdx = e.getFromRow();
                    toIdx = e.getToRow();
                    spanList = rowSpanList;
                    // size to be distributed
                    spanningLen = Math.max(1, (toIdx - fromIdx) + 1);
                    minSize = e.getComponent().getMinimumSize().height / spanningLen;
                    prefSize = e.getComponent().getPreferredSize().height / spanningLen;
                    maxSize = e.getComponent().getMaximumSize().height / spanningLen;
                    for( int j = toIdx; j-- >= fromIdx; ) {
                        Span span = getSpan(spanList, j);
                        if (span == null) {
                            span = new Span(j);
                            spanList.add(span);
                        }
                        span.addEntry(e);
                        span.registerSizes(minSize, prefSize, maxSize);
                    }
                }

                // sort spans
                Collections.sort(colSpanList);
                Collections.sort(rowSpanList);

                // calculate span sizes using registered entries
                for( int i = 0, count = colSpanList.size(); i < count; i++ ) {
                    Span span = colSpanList.get(i);
                    ctnMinWidth += span.getMinimumSize();
                    ctnPrefWidth += span.getPreferredSize();
                    ctnMaxWidth += span.getMaximumSize();
                }
                if (parentBounds.width > ctnPrefWidth) {
                    // there is a delta to be distributed
                    Span span = colSpanList.get(1); // TODO this should be dynamic
                    span.setSize(span.getPreferredSize() + parentBounds.width - ctnPrefWidth);
                }

                for( int i = 0, count = rowSpanList.size(); i < count; i++ ) {
                    Span span = rowSpanList.get(i);
                    ctnMinHeight += span.getMinimumSize();
                    ctnPrefHeight += span.getPreferredSize();
                    ctnMaxHeight += span.getMaximumSize();
                }
                // distribute fixed sizes on the spans
                // distribute sizes of no spanning components
                // distribute deltas of spanning components on dynamic spans

                // TODO transfer sizes to the entries
                int start = 0;
                for( int i = 0, count = colSpanList.size(); i < count; i++ ) {
                    Span span = colSpanList.get(i);
                    int spanSize = span.getSize();
                    for( int j = span.getEntryCount(); j-- > 0; ) {
                        Entry e = span.getEntry(j);
                        int fromIdx = e.getFromCol();
                        int toIdx = e.getToCol();
                        if (fromIdx == i)
                            e.setXStart(start);
                        if (toIdx == i)
                            e.setXEnd(start + spanSize);
                    }
                    start = +spanSize;
                }

                start = 0;
                for( int i = 0, count = rowSpanList.size(); i < count; i++ ) {
                    Span span = rowSpanList.get(i);
                    int spanSize = span.getSize();
                    for( int j = span.getEntryCount(); j-- > 0; ) {
                        Entry e = span.getEntry(j);
                        int fromIdx = e.getFromRow();
                        int toIdx = e.getToRow();
                        if (fromIdx == i)
                            e.setYStart(start);
                        if (toIdx == i)
                            e.setYEnd(start + spanSize);
                    }
                    start = +spanSize;
                }
            }

            layoutValid = true;
        }
    }

    public static void main( String[] args ) {
        // String constraints = "A3-C5";
        // Entry e = new Entry(new Component() {}, constraints);
        // System.out.println("constraints: " + constraints);
        // System.out.println("fromCol: " + e.getFromCol());
        // System.out.println("fromRow: " + e.getFromRow());
        // System.out.println(" -");
        // System.out.println("toCol: " + e.getToCol());
        // System.out.println("toRow: " + e.getToRow());

        JDialog dlg = new JDialog((JFrame) null, "Test dialog");
        dlg.setLayout(new SwingSpreadsheetLayout());
        dlg.add(new JLabel("Name"), "A0-A0");
        dlg.add(new JTextField("Patrick"), "B0-B0");
        dlg.add(new JLabel("Surname"), "A1-A1");
        dlg.add(new JTextField("Ohnewein"), "B1-B1");

        dlg.pack();
        dlg.setVisible(true);
    }
}

/**
 * Every child component will be wrapped by an Entry object.
 * 
 * @author Patrick Ohnewein
 */
class Entry {

    private final Component component;
    private int fromCol;
    private int fromRow;
    private int toCol;
    private int toRow;
    private int xStart;
    private int yStart;
    private int xEnd;
    private int yEnd;

    private final Rectangle entryBounds;

    /**
     * Constructor.
     * 
     * @param component The component to wrap.
     * @param constraints The constraints describing the positioning of the component.
     */
    public Entry( Component component, String constraints ) {
        this.component = component;
        Dimension prefSize = component.getPreferredSize();
        xEnd = prefSize.width;
        yEnd = prefSize.height;
        entryBounds = new Rectangle(xStart, yStart, xEnd - xStart, yEnd - yStart);

        int len;
        if (constraints != null && (len = constraints.length()) > 0) {
            int elementIdx = 0; // 0 = fromCol, 1 = fromRow, 2 = toCol, 3 = toRow
            int i = 0;
            StringBuffer sb = new StringBuffer();
            do {
                char ch = '\0';
                if (i < len)
                    ch = constraints.charAt(i);
                switch( elementIdx ) {
                case 0:
                    if (!Character.isLetter(ch)) {
                        fromCol = convertLettersToColumnIndex(sb.toString());
                        sb.setLength(0);
                        elementIdx++;
                        i--; // unread last ch
                    } else
                        sb.append(ch);
                    break;
                case 1:
                    if (!Character.isDigit(ch)) {
                        fromRow = Integer.valueOf(sb.toString());
                        sb.setLength(0);
                        elementIdx++;
                        i--; // unread last ch
                    } else
                        sb.append(ch);
                    break;
                case 2:
                    if (!Character.isLetter(ch)) {
                        toCol = convertLettersToColumnIndex(sb.toString());
                        sb.setLength(0);
                        elementIdx++;
                        i--; // unread last ch
                    } else
                        sb.append(ch);
                    break;
                case 3:
                    if (!Character.isDigit(ch)) {
                        toRow = Integer.valueOf(sb.toString());
                        sb.setLength(0);
                        elementIdx++;
                        i--; // unread last ch
                    } else
                        sb.append(ch);
                    break;
                default:
                    throw new IllegalStateException();
                }
                // increase and skip any char which isn't a letter or a digit (i.e. '-')
                do {
                    i++;
                } while( i < len && !Character.isLetterOrDigit(ch = constraints.charAt(i)) );
            } while( i < len || sb.length() > 0 );
        }
    }

    /**
     * @return The component wrapped by this entry.
     */
    public Component getComponent() {
        return component;
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
     * @return The bounds the component of this entry should be positioned.
     */
    public Rectangle getEntryBounds() {
        entryBounds.x = xStart;
        entryBounds.y = yStart;
        entryBounds.width = xEnd - xStart;
        entryBounds.height = yEnd - yStart;
        return entryBounds;
    }

    /**
     * Helper function to convert a letter to the column index.
     * 
     * @param letters Letter representing the column (A, B, AA, ..)
     * @return the index of the column.
     */
    private static int convertLettersToColumnIndex( String letters ) {
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

    /** List of entries in this span. */
    private ArrayList<Entry> entryList;

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

    /**
     * @param e Entry to add to this span.
     */
    public void addEntry( Entry e ) {
        if (entryList == null)
            entryList = new ArrayList<Entry>();
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
    public Entry getEntry( int index ) {
        return entryList.get(index);
    }
}
