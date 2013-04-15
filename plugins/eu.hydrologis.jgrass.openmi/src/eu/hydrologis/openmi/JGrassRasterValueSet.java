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

import java.io.Serializable;

import nl.alterra.openmi.sdk.backbone.ValueSet;
import eu.hydrologis.jgrass.libs.map.RasterData;

/**
 * <p>
 * A valueset that returns a jgrass raster data matrix
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class JGrassRasterValueSet extends ValueSet implements Serializable {
    private static final long serialVersionUID = 1L;

    private RasterData jgrassRasterData = null;
    public JGrassRasterValueSet( RasterData jgrassRasterData ) {
        this.jgrassRasterData = jgrassRasterData;
    }

    
    public String toString() {
        return jgrassRasterData.toString();
    }

    public RasterData getJGrassRasterData() {
        return jgrassRasterData;
    }

}
