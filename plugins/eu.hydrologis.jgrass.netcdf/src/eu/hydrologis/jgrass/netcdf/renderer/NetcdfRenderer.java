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
package eu.hydrologis.jgrass.netcdf.renderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.ProjectPlugin;
import net.refractions.udig.project.internal.StyleBlackboard;
import net.refractions.udig.project.internal.render.ViewportModel;
import net.refractions.udig.project.internal.render.impl.RendererImpl;
import net.refractions.udig.project.render.IRenderContext;
import net.refractions.udig.project.render.IViewportModel;
import net.refractions.udig.project.render.RenderException;
import net.refractions.udig.project.render.displayAdapter.IMapDisplay;
import net.refractions.udig.style.sld.SLDContent;
import net.refractions.udig.ui.graphics.SLDs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.io.CoverageReadRequest;
import org.geotools.coverage.io.impl.DefaultCoverageReadRequest;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.renderer.lite.gridcoverage2d.GridCoverageRenderer;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.temporal.object.DefaultInstant;
import org.geotools.temporal.object.DefaultPosition;
import org.geotools.util.NumberRange;
import org.joda.time.DateTime;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.temporal.TemporalGeometricPrimitive;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import eu.hydrologis.jgrass.netcdf.service.NetcdfMapGeoResource;

/**
 * The renderer for netcdf type rasters, as wrapped by the {@link NetcdfMapGeoResource}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class NetcdfRenderer extends RendererImpl {
    private GridCoverageRenderer renderer;

    public NetcdfRenderer( IRenderContext context ) {
        setContext(context);
    }

    @SuppressWarnings("unchecked")
    public synchronized void render( Graphics2D graphics, IProgressMonitor monitor ) throws RenderException {
        try {
            // get the current context
            final IRenderContext currentContext = getContext();

            // check that actually we have something to draw
            currentContext.setStatus(ILayer.WAIT);
            currentContext.setStatusMessage("rendering map");

            // get the envelope and the screen extent
            ReferencedEnvelope envelope = getRenderBounds();
            if (envelope == null || envelope.isNull()) {
                envelope = currentContext.getImageBounds();
            }
            // Point upperLeft = currentContext.worldToPixel(new Coordinate(envelope.getMinX(),
            // envelope.getMinY()));
            // Point bottomRight = currentContext.worldToPixel(new Coordinate(envelope.getMaxX(),
            // envelope.getMaxY()));
            Point upperLeft = currentContext.worldToPixel(new Coordinate(envelope.getMinX(), envelope.getMaxY()));
            Point bottomRight = currentContext.worldToPixel(new Coordinate(envelope.getMaxX(), envelope.getMinY()));
            Rectangle screenSize = new Rectangle(upperLeft);
            screenSize.add(bottomRight);
            IMapDisplay mapDisplay = currentContext.getMapDisplay();
            CoordinateReferenceSystem destinationCRS = currentContext.getCRS();
            ReferencedEnvelope bounds = (ReferencedEnvelope) currentContext.getImageBounds();
            bounds = bounds.transform(destinationCRS, true);

            IGeoResource resource = currentContext.getGeoResource();
            final NetcdfMapGeoResource geoResource = (NetcdfMapGeoResource) resource.resolve(NetcdfMapGeoResource.class, monitor);
            List<DateTime> availableTimeSteps = geoResource.getAvailableTimeSteps();
            double[] availableElevationLevels = geoResource.getAvailableElevationLevels();

            CoverageReadRequest readRequest = new DefaultCoverageReadRequest();

            IViewportModel viewportModel = getContext().getViewportModel();
            if (viewportModel instanceof ViewportModel) {
                ViewportModel vpm = (ViewportModel) viewportModel;
                DateTime currentTimestepDate = vpm.getCurrentTimestep();
                Double currentElevationDouble = vpm.getCurrentElevation();

                if (currentTimestepDate != null && availableTimeSteps.size() > 0) {
                    SortedSet<TemporalGeometricPrimitive> temporalSubset = new TreeSet<TemporalGeometricPrimitive>();
                    temporalSubset.add(new DefaultInstant(new DefaultPosition(currentTimestepDate.toDate())));
                    readRequest.setTemporalSubset(temporalSubset);
                }
                if (currentElevationDouble != null && availableElevationLevels != null) {
                    Set<NumberRange<Double>> verticalSubset = new TreeSet<NumberRange<Double>>();
                    NumberRange<Double> vertical = new NumberRange<Double>(Double.class, currentElevationDouble, true,
                            currentElevationDouble, true);
                    verticalSubset.add(vertical);
                    readRequest.setVerticalSubset(verticalSubset);
                }
            }
            GridEnvelope range = new GridEnvelope2D(0, 0, mapDisplay.getWidth(), mapDisplay.getHeight());
            MathTransform displayToLayer = currentContext.worldToScreenMathTransform().inverse();
            ReferencingFactoryFinder.getMathTransformFactory(null).createConcatenatedTransform(displayToLayer,
                    currentContext.getLayer().mapToLayerTransform());
            GridGeometry2D geom = new GridGeometry2D(range, displayToLayer, destinationCRS);
            readRequest.setDomainSubset(geom.getEnvelope2D());

            GridCoverage2D coverage = geoResource.getGridCoverage(readRequest);

            // CoordinateReferenceSystem destinationCRS = currentContext.getCRS();
            // ReferencedEnvelope bounds = (ReferencedEnvelope) currentContext.getImageBounds();
            // bounds = bounds.transform(destinationCRS, true);

            if (coverage != null) {
                // RectIter iter = RectIterFactory.create(coverage.getRenderedImage(), null);
                // do {
                // do {
                // System.out.print(iter.getSampleDouble() + " ");
                // } while( !iter.nextPixelDone() );
                // iter.startPixels();
                // System.out.println();
                // } while( !iter.nextLineDone() );

                // setting rendering hints
                RenderingHints hints = new RenderingHints(Collections.EMPTY_MAP);
                hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
                hints.add(new RenderingHints(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE));
                hints.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION,
                        RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED));
                hints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED));
                hints.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
                hints.add(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE));
                hints.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF));
                hints.add(new RenderingHints(JAI.KEY_INTERPOLATION, new InterpolationNearest()));
                graphics.addRenderingHints(hints);

                final TileCache tempCache = currentContext.getTileCache();
                hints.add(new RenderingHints(JAI.KEY_TILE_CACHE, tempCache));

                if (CRS.getHorizontalCRS(destinationCRS) == null) {
                    destinationCRS = coverage.getCoordinateReferenceSystem2D();
                }
                // draw
                AffineTransform worldToScreen = RendererUtilities.worldToScreenTransform(envelope, screenSize, destinationCRS);
                try {
                    Style style = grabStyle(coverage);
                    Rule rule = SLDs.getRasterSymbolizerRule(style);

                    final double currentScale = currentContext.getViewportModel().getScaleDenominator();
                    double minScale = rule.getMinScaleDenominator();
                    double maxScale = rule.getMaxScaleDenominator();
                    if (minScale <= currentScale && currentScale <= maxScale) {
                        final GridCoverageRenderer paint = new GridCoverageRenderer(destinationCRS, envelope, screenSize,
                                worldToScreen, hints);
                        final RasterSymbolizer rasterSymbolizer = SLD.rasterSymbolizer(style);

                        // setState( RENDERING );
                        paint.paint(graphics, coverage, rasterSymbolizer);
                        setState(DONE);
                    }

                } catch (Exception e) {
                    final GridCoverageRenderer paint = new GridCoverageRenderer(destinationCRS, envelope, screenSize,
                            worldToScreen, hints);
                    RasterSymbolizer rasterSymbolizer = CommonFactoryFinder.getStyleFactory(null).createRasterSymbolizer();

                    // setState( RENDERING );
                    paint.paint(graphics, coverage, rasterSymbolizer);
                    setState(DONE);
                }
                // tempCache.flush();
            }
        } catch (Exception e1) {
            throw new RenderException(e1);
        } finally {
            getContext().setStatus(ILayer.DONE);
            getContext().setStatusMessage(null);
        }
    }

    /**
     *  grab the style from the blackboard, otherwise return null
     * @param coverage 
     */
    private Style grabStyle( GridCoverage2D coverage ) {
        // check for style information on the blackboard
        StyleBlackboard styleBlackboard = (StyleBlackboard) getContext().getLayer().getStyleBlackboard();

        Style style = (Style) styleBlackboard.lookup(Style.class);

        if (style == null) {
            StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
            ColorMap colorMap = sf.createColorMap();
            StyleBuilder sB = new StyleBuilder(sf);
            RasterSymbolizer rasterSym = sf.createRasterSymbolizer();

            Expression fromColorExpr = sB.colorExpression(Color.red);
            Expression toColorExpr = sB.colorExpression(Color.blue);
            Expression fromExpr = sB.literalExpression(-10.0);
            Expression toExpr = sB.literalExpression(200.0);
            // Expression opacityExpr = sB.literalExpression(opacity);

            ColorMapEntry entry = sf.createColorMapEntry();
            entry.setQuantity(fromExpr);
            entry.setColor(fromColorExpr);
            // entry.setOpacity(opacityExpr);
            colorMap.addColorMapEntry(entry);

            entry = sf.createColorMapEntry();
            entry.setQuantity(toExpr);
            // entry.setOpacity(opacityExpr);
            entry.setColor(toColorExpr);
            colorMap.addColorMapEntry(entry);

            rasterSym.setColorMap(colorMap);

            /*
             * set global transparency for the map
             */
            // rasterSym.setOpacity(sB.literalExpression(colorRulesEditor.getAlphaVAlue() / 100.0));

            style = SLD.wrapSymbolizers(rasterSym);

            // put style back on blackboard
            styleBlackboard.put(SLDContent.ID, style);
            styleBlackboard.setSelected(new String[]{SLDContent.ID});

        }

        return style;
    }

    public synchronized void render2( Graphics2D graphics, IProgressMonitor monitor ) throws RenderException {
        State state = null;
        try {
            state = prepareRender(monitor);
        } catch (IOException e1) {
            throw new RenderException(e1);
        }

        doRender(renderer, graphics, state);
    }
    /**
     * Renders a GridCoverage
     * 
     * @param renderer
     * @param graphics
     */
    public void doRender( GridCoverageRenderer renderer, Graphics2D graphics, State state ) {
        double scale = state.context.getViewportModel().getScaleDenominator();
        if (scale < state.minScale || scale > state.maxScale)
            return;

        state.context.setStatus(ILayer.WAIT);
        state.context.setStatusMessage("rendering");

        // setup composite
        Composite oldComposite = graphics.getComposite();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, state.opacity));

        // setup affine transform for on screen rendering
        Rectangle displayArea = state.displayArea;

        AffineTransform at = RendererUtilities.worldToScreenTransform(state.bounds, displayArea);
        AffineTransform tempTransform = graphics.getTransform();
        AffineTransform atg = new AffineTransform(tempTransform);
        atg.concatenate(at);
        graphics.setTransform(atg);

        GridCoverage coverage;
        try {
            coverage = getContext().getGeoResource().resolve(GridCoverage.class, null);
            StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);
            RasterSymbolizer rasterSymbolizer = factory.createRasterSymbolizer();

            renderer.paint(graphics, (GridCoverage2D) coverage, rasterSymbolizer);
        } catch (IOException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        } catch (FactoryException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        } catch (TransformException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        } catch (NoninvertibleTransformException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }

        // reset previous configuration
        graphics.setComposite(oldComposite);
        graphics.setTransform(tempTransform);

        if (state.context.getStatus() == ILayer.WAIT) {
            // status hasn't changed... everything looks good
            state.context.setStatus(ILayer.DONE);
            state.context.setStatusMessage(null);
        }

    }

    /**
     * Extract symbolizer parameters from the style blackboard
     */
    public static State getRenderState( IRenderContext context ) {
        StyleBlackboard styleBlackboard = (StyleBlackboard) context.getLayer().getStyleBlackboard();
        Style style = (Style) styleBlackboard.lookup(Style.class);
        double minScale = Double.MIN_VALUE;
        double maxScale = Double.MAX_VALUE;
        float opacity = 1.0f;
        if (style != null) {
            try {
                Rule rule = style.getFeatureTypeStyles()[0].getRules()[0];
                minScale = rule.getMinScaleDenominator();
                maxScale = rule.getMaxScaleDenominator();
                if (rule.getSymbolizers()[0] instanceof RasterSymbolizer) {
                    RasterSymbolizer rs = (RasterSymbolizer) rule.getSymbolizers()[0];
                    opacity = getOpacity(rs);
                }

            } catch (Exception e) {
                ProjectPlugin.getPlugin().log(e);
            }
        } else {
            opacity = 1;
            minScale = 0;
            maxScale = Double.MAX_VALUE;
        }

        Rectangle displayArea = new Rectangle(context.getMapDisplay().getWidth(), context.getMapDisplay().getHeight());

        return new State(context, context.getImageBounds(), displayArea, opacity, minScale, maxScale);
    }

    private static float getOpacity( RasterSymbolizer sym ) {
        float alpha = 1.0f;
        Expression exp = sym.getOpacity();
        if (exp == null)
            return alpha;
        Object obj = exp.evaluate(null);
        if (obj == null)
            return alpha;
        Number num = null;
        if (obj instanceof Number)
            num = (Number) obj;
        if (num == null)
            return alpha;
        return num.floatValue();
    }

    private State prepareRender( IProgressMonitor monitor ) throws IOException {

        try {
            CoordinateReferenceSystem contextCRS = getContext().getCRS();
            Rectangle rectangle = new Rectangle(getContext().getMapDisplay().getDisplaySize());
            Envelope bounds = getRenderBounds();
            if (bounds == null) {
                // show the bounds of the context
                bounds = getContext().getImageBounds();
                if (bounds instanceof ReferencedEnvelope) {
                    ReferencedEnvelope all = (ReferencedEnvelope) bounds;
                    if (!contextCRS.equals(all.getCoordinateReferenceSystem())) {
                        bounds = all.transform(contextCRS, true, 10);
                    }
                } else {
                    // this should not happen!
                    ReferencedEnvelope all = new ReferencedEnvelope(bounds, getContext().getViewportModel().getCRS());
                    bounds = all.transform(contextCRS, true, 10);
                }
            }
            AffineTransform worldToScreen = RendererUtilities.worldToScreenTransform(bounds, rectangle, contextCRS);
            renderer = new GridCoverageRenderer(contextCRS, bounds, rectangle, worldToScreen);

        } catch (TransformException e) {
            // TODO Handle TransformException
            throw (RuntimeException) new RuntimeException().initCause(e);
        } catch (NoninvertibleTransformException e) {
            // TODO Handle NoninvertibleTransformException
            throw (RuntimeException) new RuntimeException().initCause(e);
        } catch (FactoryException e) {
            // TODO Handle FactoryException
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
        return getRenderState(getContext());
    }

    public void stopRendering() {
        setState(STATE_EDEFAULT);
    }

    public void dispose() {
        // TODO
    }

    public void render( IProgressMonitor monitor ) throws RenderException {
        render(getContext().getImage().createGraphics(), monitor);
    }

    /**
     * Encapsulates the state required to render a GridCoverage
     * 
     * @author Jesse
     * @since 1.1.0
     */
    public static class State {

        public float opacity;
        public double minScale;
        public double maxScale;
        public IRenderContext context;
        public ReferencedEnvelope bounds;
        public Rectangle displayArea;

        public State( IRenderContext context, ReferencedEnvelope bbox, Rectangle displayArea, float opacity, double minScale,
                double maxScale ) {
            this.opacity = opacity;
            this.minScale = minScale;
            this.maxScale = maxScale;
            this.context = context;
            this.bounds = bbox;
            this.displayArea = displayArea;
        }

    }

}
