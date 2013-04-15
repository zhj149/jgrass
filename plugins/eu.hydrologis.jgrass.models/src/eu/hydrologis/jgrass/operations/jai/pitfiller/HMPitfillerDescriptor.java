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
package eu.hydrologis.jgrass.operations.jai.pitfiller;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.PrintStream;

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RIFRegistry;

/**
 * The Horton Machine Pitfiller algorithm jai operation descriptor.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class HMPitfillerDescriptor extends OperationDescriptorImpl {

    public static final String OPERATIONNAME = "Pitfiller";

    private static final String[] paramNames = {"xres", "yres", "output"};

    private static final Object[] paramDefaults = {null, null, null};

    private static final Class[] paramClasses = {Double.class, Double.class, PrintStream.class};

    private static final int numSources = 1;

    private static final String[] supportedModes = {"rendered"};

    private static final Object[] validParamValues = {null, null, null};

    private static final String[][] resources = {{"GlobalName", OPERATIONNAME},
            {"LocalName", OPERATIONNAME}, {"Vendor", "eu.hydrologis.jgrass"},
            {"Description", "Computes the depitted elevation map"},
            {"DocURL", "http://www.hydrologis.com"}, {"Version", "Beta"}};

    public HMPitfillerDescriptor() {
        super(resources, supportedModes, numSources, paramNames, paramClasses, paramDefaults,
                validParamValues);
    }

    public boolean isImmediate() {
        boolean immediate = super.isImmediate();
        return true;
    }

    /**
     * Computes the pitfiller algorithm on an elevation map.
     *
     * <p>Creates a <code>ParameterBlockJAI</code> from all
     * supplied arguments except <code>hints</code> and invokes
     * {@link JAI#create(String,ParameterBlock,RenderingHints)}.
     *
     * @see JAI
     * @see ParameterBlockJAI
     * @see RenderedOp
     *
     * @param elevationRaster  the elevation source. 
     *              A single banded {@link RenderedImage} containing 
     *              elevation values.
     * @param xres the geographic west-east resolution of the elevation map.
     * @param yres the geographic south-north resolution of the elevation map.
     * @param out an output stream to which to log to. Can be null.
     * @param hints The {@link RenderingHints} to use. May be <code>null</code>.
     * 
     * @return The <code>RenderedOp</code> destination.
     */
    public static RenderedOp create( RenderedImage elevationRaster, double xres, double yres,
            PrintStream out, RenderingHints hints ) {

        if (!isRegistered) {
            register();
            isRegistered = true;
        }

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(elevationRaster);
        pb.add(xres);
        pb.add(yres);
        pb.add(out);
        return JAI.create(HMPitfillerDescriptor.OPERATIONNAME, pb, hints);
    }

    /* take care of registering */
    private static boolean isRegistered = false;
    private static void register() {
        OperationRegistry operationRegistry = JAI.getDefaultInstance().getOperationRegistry();
        HMPitfillerDescriptor d = new HMPitfillerDescriptor();
        HMPitfillerRIF rif = new HMPitfillerRIF();
        String productName = "eu.hydrologis.jgrass";
        operationRegistry.registerDescriptor(d);
        RIFRegistry.register(operationRegistry, OPERATIONNAME, productName, rif);
    }
}
