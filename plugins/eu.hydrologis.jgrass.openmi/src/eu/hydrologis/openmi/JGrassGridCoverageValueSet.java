/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) { 
 * HydroloGIS - www.hydrologis.com                                                   
 * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam                               
 * The JGrass developer team - www.jgrass.org                                         
 * }
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.openmi;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;

import javax.media.jai.RenderedOp;

import nl.alterra.openmi.sdk.backbone.ValueSet;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.hydrologis.jgrass.libs.iodrivers.geotools.JGrassGridCoverage2D;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.JGrassGridCoverage2D.GridCoverageBuilder;
import eu.hydrologis.jgrass.libs.iodrivers.geotools.JGrassGridCoverage2D.WritableGridCoverageBuilder;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;

/**
 * Represents a {@link ValueSet} that wraps a {@link GridCoverage2D} object.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 * @see GridCoverage2D
 * @see ValueSet
 */
public class JGrassGridCoverageValueSet extends ValueSet {

    private GridCoverage2D gridCoverage2D = null;

    /**
     * Cosntructor for {@link JGrassGridCoverageValueSet}.
     * 
     * @param gridCoverage2D a {@link GridCoverage2D}, which this object will wrap.
     */
    public JGrassGridCoverageValueSet( GridCoverage2D gridCoverage2D ) {
        this.gridCoverage2D = gridCoverage2D;
    }

    /**
     * Constructor for {@link JGrassGridCoverageValueSet} based on {@link RenderedImage}.
     * 
     * @param renderedImage the raster to wrap.
     * @param writeRegion the {@link JGrassRegion geographic region} of the raster.
     * @param crs the {@link CoordinateReferenceSystem}
     */
    public JGrassGridCoverageValueSet( RenderedImage renderedImage, JGrassRegion writeRegion, CoordinateReferenceSystem crs ) {
        GridCoverageBuilder gridCoverageBuilder = new JGrassGridCoverage2D.GridCoverageBuilder(renderedImage);
        JGrassGridCoverage2D jgrassGridCoverage2D = gridCoverageBuilder.writeRegion(writeRegion).crs(crs).dataRange(
                new double[]{0.0, 4000.0}).build();
        gridCoverage2D = jgrassGridCoverage2D.getGridCoverage2D();
    }

    public JGrassGridCoverageValueSet( WritableRaster raster, JGrassRegion writeRegion, CoordinateReferenceSystem crs ) {
        WritableGridCoverageBuilder gridCoverageBuilder = new JGrassGridCoverage2D.WritableGridCoverageBuilder(raster);
        JGrassGridCoverage2D jgrassGridCoverage2D = gridCoverageBuilder.writeRegion(writeRegion).crs(crs).dataRange(
                new double[]{0.0, 4000.0}).build();
        gridCoverage2D = jgrassGridCoverage2D.getGridCoverage2D();
    }

    /**
     * Constructor for {@link JGrassGridCoverageValueSet} based on {@link RenderedOp}.
     * 
     * @param renderedOp the {@link RenderedOp} to wrap.
     * @param writeRegion the {@link JGrassRegion geographic region} of the raster.
     * @param crs the {@link CoordinateReferenceSystem}
     */
    public JGrassGridCoverageValueSet( RenderedOp renderedOp, JGrassRegion writeRegion, CoordinateReferenceSystem crs ) {
        BufferedImage bufferedImage = renderedOp.getAsBufferedImage();
        GridCoverageBuilder gridCoverageBuilder = new JGrassGridCoverage2D.GridCoverageBuilder(bufferedImage);
        JGrassGridCoverage2D jgrassGridCoverage2D = gridCoverageBuilder.writeRegion(writeRegion).crs(crs).dataRange(
                new double[]{0.0, 4000.0}).build();
        gridCoverage2D = jgrassGridCoverage2D.getGridCoverage2D();
    }

    /**
     * Getter for the wrapped {@link GridCoverage2D}.
     * 
     * @return the wrapped GridCoverage2D.
     */
    public GridCoverage2D getGridCoverage2D() {
        return gridCoverage2D;
    }

    public String toString() {
        String msg = "JGrassGridCoverageValueSet extending ValueSet and wrapping:"; //$NON-NLS-1$
        msg = msg + gridCoverage2D.toString();
        return msg;
    }

}
