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
package eu.hydrologis.jgrass.ui.utilities.editors;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import eu.hydrologis.jgrass.ui.utilities.UiUtilitiesPlugin;
import eu.hydrologis.jgrass.ui.utilities.widgets.ImageCombo;

/**
 * An SWT widget for freehand painting.
 * 
 * <p>
 * An swt editor that gives the possibility to draw over an image with 
 * different style. The drawn stuff is kept separated from the image.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SimpleSWTImageEditor {

    /**
     * The list of possible strokes to choose.
     */
    private final int[] STROKES = {1, 3, 10, 20, 40};
    /**
     * The list of possible stroke transparencies to choose.
     */
    private final int[] ALPHAS = {20, 40, 60, 80, 100};

    /**
     * The canvas on which painting occures.
     */
    private Canvas drawArea = null;
    /**
     * The current stroke color components.
     */
    private int[] strokeRGB = {0, 0, 0};
    /**
     * The current stroke transparency.
     */
    private int strokeAlpha = 255;
    /**
     * The current stroke width.
     */
    private final int[] strokeWidth = {1};
    /**
     * The list of lines that are drawn and saved.
     */
    private List<DressedStroke> lines = null;
    /**
     * The image that is used as background.
     */
    private Image backImage;

    private Image drawnImage;
    private boolean isRemoveMode = false;
    private boolean isDrawMode = false;
    private final Cursor defaultCursor;
    private final Composite mainComposite;
    private final Composite propsComposite;
    private final ScrolledComposite drawAreaScroller;
    private double baseScaleFactor = -1;
    private double scaleFactor = -1;
    private final boolean doZoom;

    /**
     * Constructor for the image editor.
     * 
     * @param parent the parent composite.
     * @param style the swt style for the component.
     * @param preloadedLines a list of lines to be drawn.
     * @param backGroundImage a background image to use in the canvas.
     * @param minScroll the minimum dimension for the scrolling.
     * @param doZoom flag that defines if the zoom tools should be added.
     */
    public SimpleSWTImageEditor( Composite parent, int style, List<DressedStroke> preloadedLines,
            Image backGroundImage, Point minScroll, boolean doZoom ) {
        this.doZoom = doZoom;
        if (backGroundImage != null)
            this.backImage = backGroundImage;
        if (preloadedLines == null) {
            this.lines = new ArrayList<DressedStroke>();
        } else {
            this.lines = preloadedLines;
        }
        mainComposite = new Composite(parent, style);
        mainComposite.setLayout(new GridLayout());
        propsComposite = new Composite(mainComposite, style);
        propsComposite.setLayout(new RowLayout());
        propsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));

        // stroke width
        Image img1 = AbstractUIPlugin.imageDescriptorFromPlugin(UiUtilitiesPlugin.PLUGIN_ID,
                "/icons/strokewidth_1.png").createImage();
        Image img2 = AbstractUIPlugin.imageDescriptorFromPlugin(UiUtilitiesPlugin.PLUGIN_ID,
                "/icons/strokewidth_2.png").createImage();
        Image img3 = AbstractUIPlugin.imageDescriptorFromPlugin(UiUtilitiesPlugin.PLUGIN_ID,
                "/icons/strokewidth_3.png").createImage();
        Image img4 = AbstractUIPlugin.imageDescriptorFromPlugin(UiUtilitiesPlugin.PLUGIN_ID,
                "/icons/strokewidth_4.png").createImage();
        Image img5 = AbstractUIPlugin.imageDescriptorFromPlugin(UiUtilitiesPlugin.PLUGIN_ID,
                "/icons/strokewidth_5.png").createImage();

        Composite strokeComposite = new Composite(propsComposite, SWT.None);
        strokeComposite.setLayout(new GridLayout(2, false));

        final ImageCombo strokeWidthCombo = new ImageCombo(strokeComposite, SWT.READ_ONLY);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 30;
        strokeWidthCombo.setLayoutData(gridData);
        strokeWidthCombo.add("1", img1);
        strokeWidthCombo.add("2", img2);
        strokeWidthCombo.add("3", img3);
        strokeWidthCombo.add("4", img4);
        strokeWidthCombo.add("5", img5);
        strokeWidthCombo.select(0);
        strokeWidthCombo.setToolTipText("stroke width");
        strokeWidthCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int selectedIndex = strokeWidthCombo.getSelectionIndex();
                strokeWidth[0] = STROKES[selectedIndex];
            }
        });

        // alpha
        final Combo alphaCombo = new Combo(strokeComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData gridData2 = new GridData(SWT.FILL, SWT.FILL, true, true);
        alphaCombo.setLayoutData(gridData2);
        String[] items = new String[ALPHAS.length];
        for( int i = 0; i < items.length; i++ ) {
            items[i] = ALPHAS[i] + "%";
        }
        alphaCombo.setItems(items);
        alphaCombo.select(ALPHAS.length - 1);
        alphaCombo.setToolTipText("stroke alpha");
        alphaCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int selectedIndex = alphaCombo.getSelectionIndex();
                int alphaInPercent = ALPHAS[selectedIndex];
                strokeAlpha = 255 * alphaInPercent / 100;
            }
        });

        Composite buttonsComposite = new Composite(propsComposite, SWT.NONE);
        if (doZoom) {
            buttonsComposite.setLayout(new GridLayout(6, false));
        } else {
            buttonsComposite.setLayout(new GridLayout(3, false));
        }

        // color
        final ColorSelector cs = new ColorSelector(buttonsComposite);
        Button csButton = cs.getButton();
        GridData gridData3 = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData3.widthHint = 25;
        csButton.setLayoutData(gridData3);

        cs.setColorValue(new RGB(strokeRGB[0], strokeRGB[1], strokeRGB[2]));
        cs.getButton().setToolTipText("stroke color");
        cs.getButton().addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                RGB rgb = cs.getColorValue();
                strokeRGB = new int[]{rgb.red, rgb.green, rgb.blue};
            }
        });

        // clear all
        ImageDescriptor clearID = AbstractUIPlugin.imageDescriptorFromPlugin(
                UiUtilitiesPlugin.PLUGIN_ID, "icons/trash.gif"); //$NON-NLS-1$
        Button clearButton = new Button(buttonsComposite, SWT.BORDER | SWT.PUSH);
        clearButton.setImage(clearID.createImage());
        clearButton.setToolTipText("clear the area from drawings");
        clearButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                lines.removeAll(lines);
                drawArea.redraw();
            }
        });
        // clear shape
        ImageDescriptor removeID = AbstractUIPlugin.imageDescriptorFromPlugin(
                UiUtilitiesPlugin.PLUGIN_ID, "icons/close.gif"); //$NON-NLS-1$
        Button removeButton = new Button(buttonsComposite, SWT.BORDER | SWT.PUSH);
        removeButton.setImage(removeID.createImage());
        removeButton.setToolTipText("remove selected line");
        removeButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                isRemoveMode = true;
                final Cursor cursor = new Cursor(drawArea.getDisplay(), SWT.CURSOR_CROSS);
                drawArea.setCursor(cursor);
            }
        });

        if (doZoom) {
            // zoom all
            ImageDescriptor zoomAllID = AbstractUIPlugin.imageDescriptorFromPlugin(
                    UiUtilitiesPlugin.PLUGIN_ID, "icons/zoom_all.gif"); //$NON-NLS-1$
            Button zoomAllButton = new Button(buttonsComposite, SWT.PUSH);
            zoomAllButton.setImage(zoomAllID.createImage());
            zoomAllButton.setToolTipText("zoom to the whole extend");
            zoomAllButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    calculateBaseScaleFactor();
                    scaleFactor = baseScaleFactor;
                    drawArea.redraw();
                }
            });

            // zoom in
            ImageDescriptor zoomInID = AbstractUIPlugin.imageDescriptorFromPlugin(
                    UiUtilitiesPlugin.PLUGIN_ID, "icons/zoom_in.gif"); //$NON-NLS-1$
            Button zoomInButton = new Button(buttonsComposite, SWT.PUSH);
            zoomInButton.setImage(zoomInID.createImage());
            zoomInButton.setToolTipText("zoom in");
            zoomInButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    scaleFactor = scaleFactor * 1.2;
                    drawArea.redraw();
                }
            });

            // zoom out
            ImageDescriptor zoomOutID = AbstractUIPlugin.imageDescriptorFromPlugin(
                    UiUtilitiesPlugin.PLUGIN_ID, "icons/zoom_out.gif"); //$NON-NLS-1$
            Button zoomOutButton = new Button(buttonsComposite, SWT.PUSH);
            zoomOutButton.setImage(zoomOutID.createImage());
            zoomOutButton.setToolTipText("zoom out");
            zoomOutButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    scaleFactor = scaleFactor / 1.2;
                    drawArea.redraw();
                }
            });
        }

        drawAreaScroller = new ScrolledComposite(mainComposite, SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL);
        drawArea = new Canvas(drawAreaScroller, SWT.None);
        defaultCursor = drawArea.getCursor();
        drawAreaScroller.setContent(drawArea);
        drawAreaScroller.setExpandHorizontal(true);
        drawAreaScroller.setExpandVertical(true);
        if (minScroll != null) {
            drawAreaScroller.setMinWidth(minScroll.x);
            drawAreaScroller.setMinHeight(minScroll.y);
        }
        drawAreaScroller.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL));

        Listener drawListener = new Listener(){
            int lastX = 0, lastY = 0;
            List<Integer> line = null;
            GC gc = null;

            public void handleEvent( Event event ) {

                /*
                 * REMOVE MODE
                 */
                if (isRemoveMode && event.type == SWT.MouseDown) {
                    for( int i = 0; i < lines.size(); i++ ) {
                        DressedStroke stroke = lines.get(i);
                        int x = event.x;
                        int y = event.y;
                        int[] nodes = stroke.getScaledNodes(baseScaleFactor);
                        for( int j = 0; j < nodes.length - 1; j = j + 2 ) {
                            Point2f linePoint = new Point2f(nodes[j], nodes[j + 1]);
                            Point2f clickPoint = new Point2f(x, y);
                            int threshold = stroke.strokeWidth[0];
                            threshold = (int) Math.round((double) threshold * baseScaleFactor);
                            threshold = threshold < 10 ? 10 : threshold;
                            if (clickPoint.distance(linePoint) < threshold) {
                                lines.remove(i);
                                isRemoveMode = false;
                                drawArea.setCursor(defaultCursor);
                                drawArea.redraw();
                                return;
                            }
                        }

                    }

                }
                if (isRemoveMode && (event.type == SWT.MouseMove || event.type == SWT.MouseUp)) {
                    return;
                }

                /*
                 * DRAWING MODE
                 */
                if (scaleFactor == -1) {
                    calculateBaseScaleFactor();
                    scaleFactor = baseScaleFactor;
                }

                switch( event.type ) {
                case SWT.Paint:
                    if (drawnImage != null)
                        drawnImage.dispose();
                    drawnImage = new Image(drawArea.getDisplay(), drawArea.getBounds());
                    GC gcImage = new GC(drawnImage);
                    // draw the background image
                    if (backImage != null) {
                        Rectangle imgBounds = backImage.getBounds();
                        ImageData newImageData = backImage.getImageData().scaledTo(
                                (int) Math.round(imgBounds.width * scaleFactor),
                                (int) Math.round(imgBounds.height * scaleFactor));
                        Image newImage = new Image(drawArea.getDisplay(), newImageData);
                        gcImage.drawImage(newImage, 0, 0);
                    }
                    // draw the lines
                    for( int i = 0; i < lines.size(); i = i + 1 ) {
                        DressedStroke tmpStroke = lines.get(i);
                        gcImage.setLineWidth((int) Math.round(tmpStroke.strokeWidth[0]
                                * scaleFactor));
                        gcImage.setLineCap(SWT.CAP_ROUND);
                        gcImage.setLineJoin(SWT.JOIN_ROUND);
                        gcImage.setLineStyle(SWT.LINE_SOLID);
                        int[] rgb = tmpStroke.rgb;
                        gcImage.setForeground(new Color(drawArea.getDisplay(), rgb[0], rgb[1],
                                rgb[2]));
                        gcImage.setAlpha(tmpStroke.strokeAlpha);
                        int[] nodes = tmpStroke.getScaledNodes(scaleFactor);
                        // at least 4 values to have two points
                        if (nodes.length > 3) {
                            Path p = new Path(drawArea.getDisplay());
                            p.moveTo(nodes[0], nodes[1]);
                            for( int j = 2; j < nodes.length - 1; j = j + 2 ) {
                                p.lineTo(nodes[j], nodes[j + 1]);
                            }
                            gcImage.drawPath(p);
                        }
                    }
                    gc = new GC(drawArea);
                    gc.drawImage(drawnImage, 0, 0);

                    gcImage.dispose();

                    break;
                case SWT.MouseMove:
                    if ((event.stateMask & SWT.BUTTON1) == 0)
                        break;
                    if (line == null)
                        break;
                    line.add(event.x);
                    line.add(event.y);
                    gc = new GC(drawArea);
                    gc.setLineWidth((int) Math.round(strokeWidth[0] * scaleFactor));
                    gc.setLineCap(SWT.CAP_ROUND);
                    gc.setLineJoin(SWT.JOIN_ROUND);
                    gc.setLineStyle(SWT.LINE_SOLID);
                    Color color = new Color(drawArea.getDisplay(), strokeRGB[0], strokeRGB[1],
                            strokeRGB[2]);
                    gc.setForeground(color);
                    gc.setAlpha(255 * strokeAlpha / 100);
                    gc.drawLine(lastX, lastY, event.x, event.y);
                    lastX = event.x;
                    lastY = event.y;
                    gc.dispose();
                    color.dispose();
                    break;
                case SWT.MouseDown:
                    if (isRemoveMode) {
                        break;
                    }
                    lastX = event.x;
                    lastY = event.y;
                    line = new ArrayList<Integer>();
                    line.add(lastX);
                    line.add(lastY);
                    isDrawMode = true;
                    break;
                case SWT.MouseUp:
                    if (isRemoveMode || !isDrawMode)
                        break;

                    lastX = event.x;
                    lastY = event.y;
                    DressedStroke newLine = new DressedStroke();
                    newLine.nodes = new int[line.size()];
                    for( int i = 0; i < line.size(); i++ ) {
                        newLine.nodes[i] = (int) Math.round((double) line.get(i) / scaleFactor);
                    }
                    newLine.strokeAlpha = strokeAlpha;
                    newLine.strokeWidth = new int[]{strokeWidth[0]};
                    newLine.rgb = new int[]{strokeRGB[0], strokeRGB[1], strokeRGB[2]};
                    lines.add(newLine);
                    line.clear();
                    drawArea.redraw();
                    break;

                }
            }
        };

        drawArea.addListener(SWT.MouseDown, drawListener);
        drawArea.addListener(SWT.MouseMove, drawListener);
        drawArea.addListener(SWT.MouseUp, drawListener);
        drawArea.addListener(SWT.Paint, drawListener);

        // add popup menu
        MenuManager popManager = new MenuManager();
        Menu menu = popManager.createContextMenu(drawArea);
        drawArea.setMenu(menu);
        IAction menuAction = new SaveAction(this);
        popManager.add(menuAction);

    }
    /**
     * Getter for the drawn lines.
     * 
     * @return the user drawn lines.
     */
    public List<DressedStroke> getDrawing() {
        return lines;
    }

    /**
     * Getter for the drawing canvas.
     * 
     * @return the canvas object on which the drawing occurs.
     */
    public Canvas getCanvas() {
        return drawArea;
    }

    /**
     * Getter for the parent control.
     * 
     * @return the parent control.
     */
    public Control getMainControl() {
        return mainComposite;
    }

    /**
     * Setter for the background color.
     * 
     * @param backgroundColor the color to set.
     */
    public void setBackgroundColor( Color backgroundColor ) {
        mainComposite.setBackground(backgroundColor);
        propsComposite.setBackground(backgroundColor);
        drawAreaScroller.setBackground(backgroundColor);
        drawArea.setBackground(backgroundColor);
    }

    /**
     * Getter for the image currently drawn in the canvas.
     * 
     * @return the image over which the user draw.
     */
    public Image getImage() {
        if (drawnImage != null)
            return drawnImage;

        Rectangle bounds = null;
        int maxStrokeWidth = 1;
        for( int i = 0; i < lines.size(); i = i + 1 ) {
            DressedStroke tmpStroke = lines.get(i);
            if (bounds == null) {
                bounds = tmpStroke.getBounds();
            } else {
                bounds.add(tmpStroke.getBounds());
            }
            int width = tmpStroke.strokeWidth[0];
            if (maxStrokeWidth < width) {
                maxStrokeWidth = width;
            }
        }
        if (bounds == null)
            return null;
        bounds = new Rectangle(bounds.x, bounds.y, bounds.width + maxStrokeWidth, bounds.height
                + maxStrokeWidth);

        drawnImage = new Image(drawArea.getDisplay(), bounds);
        GC gcImage = new GC(drawnImage);
        calculateBaseScaleFactor();
        scaleFactor = baseScaleFactor;
        // draw the background image
        if (backImage != null) {
            Rectangle imgBounds = backImage.getBounds();
            ImageData newImageData = backImage.getImageData().scaledTo(
                    (int) Math.round(imgBounds.width * scaleFactor),
                    (int) Math.round(imgBounds.height * scaleFactor));
            Image newImage = new Image(drawArea.getDisplay(), newImageData);
            gcImage.drawImage(newImage, 0, 0);
        }
        // draw the lines
        for( int i = 0; i < lines.size(); i = i + 1 ) {
            DressedStroke tmpStroke = lines.get(i);
            gcImage.setLineWidth((int) Math.round(tmpStroke.strokeWidth[0] * scaleFactor));
            gcImage.setLineCap(SWT.CAP_ROUND);
            gcImage.setLineJoin(SWT.JOIN_ROUND);
            gcImage.setLineStyle(SWT.LINE_SOLID);
            int[] rgb = tmpStroke.rgb;
            gcImage.setForeground(new Color(drawArea.getDisplay(), rgb[0], rgb[1], rgb[2]));
            gcImage.setAlpha(tmpStroke.strokeAlpha);
            int[] nodes = tmpStroke.getScaledNodes(scaleFactor);
            // at least 4 values to have two points
            if (nodes.length > 3) {
                Path p = new Path(drawArea.getDisplay());
                p.moveTo(nodes[0], nodes[1]);
                for( int j = 2; j < nodes.length - 1; j = j + 2 ) {
                    p.lineTo(nodes[j], nodes[j + 1]);
                }
                gcImage.drawPath(p);
            }
        }

        gcImage.dispose();

        scaleFactor = -1;
        return drawnImage;
    }

    private void calculateBaseScaleFactor() {
        if (backImage != null) {
            Rectangle mainCompositeBound = mainComposite.getBounds();
            Rectangle imageBound = backImage.getBounds();
            double scaleFactorX = (double) mainCompositeBound.width / (double) imageBound.width;
            double scaleFactorY = (double) mainCompositeBound.height / (double) imageBound.height;
            baseScaleFactor = mainCompositeBound.width < mainCompositeBound.height
                    ? scaleFactorX
                    : scaleFactorY;
        } else {
            baseScaleFactor = 1.0;
        }
    }

    public static void main( String[] args ) {

        Display display = new Display();
        ImageData imgD = new ImageData("/Users/moovida/Desktop/Picture3.png");
        Image img = new Image(display, imgD);
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        new SimpleSWTImageEditor(shell, SWT.None, null, img, new Point(600, 400), true);
        shell.open();
        while( !shell.isDisposed() ) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();

    }
}
