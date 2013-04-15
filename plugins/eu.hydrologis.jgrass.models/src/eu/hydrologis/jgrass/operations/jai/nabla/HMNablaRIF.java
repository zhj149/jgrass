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
package eu.hydrologis.jgrass.operations.jai.nabla;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.PrintStream;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

/**
 * The Horton Machine Nabla algorithm RIF.
 * 
 * @author Daniele Andreis
 */
public class HMNablaRIF implements RenderedImageFactory {

    public RenderedImage create( ParameterBlock paramBlock, RenderingHints hints ) {

        RenderedImage elevationImage = paramBlock.getRenderedSource(0);

        PrintStream err = (PrintStream) paramBlock.getObjectParameter(0);
        
        PrintStream out = (PrintStream) paramBlock.getObjectParameter(1);
        double xRes = paramBlock.getDoubleParameter(2);
        double yRes = paramBlock.getDoubleParameter(3);

        int mode = paramBlock.getIntParameter(4);

        ImageLayout layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
        double threshold = paramBlock.getDoubleParameter(5);

        boolean doTile = (Boolean) paramBlock.getObjectParameter(6);
        BorderExtender borderExtender = null;
        return new HMNablaOperation(elevationImage, borderExtender, hints, layout, xRes, yRes,
                mode, threshold, doTile, out, err);
    }

}
