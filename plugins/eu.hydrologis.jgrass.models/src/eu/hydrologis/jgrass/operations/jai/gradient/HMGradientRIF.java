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
package eu.hydrologis.jgrass.operations.jai.gradient;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.PrintStream;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;


/**
 * The Horton Machine Gradient algorithm RIF.
 * 
 * @author Andrea Antonello (www.hydrologis.com), <a /href='mailto:daniele.andreis@gmail.com>Daniele
 *         andreis</a>
 */
public class HMGradientRIF implements RenderedImageFactory {

    /**
     * Create the Image.
     */
    public RenderedImage create( ParameterBlock paramBlock, RenderingHints hints ) {

        RenderedImage elevationImage = paramBlock.getRenderedSource(0);
        double xRes = paramBlock.getDoubleParameter(0);
        double yRes = paramBlock.getDoubleParameter(1);
        ImageLayout layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
        boolean doTile = (Boolean) paramBlock.getObjectParameter(2);
        PrintStream err = (PrintStream) paramBlock.getObjectParameter(3);
PrintStream out =(PrintStream) paramBlock.getObjectParameter(4);
        // the BorderExtender is a parameter of AreaOpImage extension, it's null becouse we haven't
        // to extends theborder.
        BorderExtender borderExtender = null;
        return new HMGradientOperation(elevationImage, borderExtender, hints, layout, xRes, yRes,
                doTile, err,out);
    }

}
