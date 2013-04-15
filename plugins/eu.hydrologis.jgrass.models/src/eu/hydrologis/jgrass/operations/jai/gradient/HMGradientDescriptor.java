/*
 * JGrass - Free Open Source Java GIS http://www.jgrassimport java.awt.Rectangle;

import javax.media.jai.OperationDescriptorImpl;
 modify
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

import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.OperationRegistry;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RIFRegistry;

import eu.hydrologis.jgrass.operations.jai.UtilityJAI;

/**
 * The Horton Machine Gradient algorithm jai operation descriptor.
 * 
 * @author Andrea Antonello (www.hydrologis.com), <a /href='mailto:daniele.andreis@gmail.com>Daniele
 *         andreis</a>
 */
public class HMGradientDescriptor extends OperationDescriptorImpl {

    /** long serialVersionUID field */
    private static final long serialVersionUID = 1L;

    public static final String OPERATIONNAME = "HMGradient";

    private static final String[] paramNames = {"xres", "yres", "doTile", "errorPrint","outputStream"};

    private static final Object[] paramDefaults = {null, null, true, null,null};

    private static final Class[] paramClasses = {Double.class, Double.class, Boolean.class,
            PrintStream.class,PrintStream.class};

    private static final int numSources = 1;

    private static final String[] supportedModes = {"rendered"};
    // I have set the valid value from 0 (it isn't possible to have a resolution which is negative.
    private static final Object[] validParamValues = {
            new javax.media.jai.util.Range(Double.class, new Double(0.0), new Double(
                    Double.MAX_VALUE)),
            new javax.media.jai.util.Range(Double.class, new Double(0.0), new Double(
                    Double.MAX_VALUE)), null, null,null};
    private static boolean isRegistered = false;
    private static final String[][] resources = {
            {"GlobalName", "HMGradient"},
            {"LocalName", "HMGradient"},
            {"Vendor", "eu.hydrologis.jgrass"},
            {"Description", "Computes the gradient map"},
            {
                    "DocURL",
                    "http://jgrass.wiki.software.bz.it/jgrass/Horton_Machine#head-829bf380f1cbec2d2fa4b61b786b3f18c7701ad7"},
            {"Version", "Beta"}};

    public HMGradientDescriptor() {
        super(resources, supportedModes, numSources, paramNames, paramClasses, paramDefaults,
                validParamValues);
    }
    /**
     * This is the <i>factory</i> of the RenderedOp.
     * <p>
     * <li>Verify if the Operation is already registered (if not then register it</li>
     * <li>Set the parameter block.</li>
     * <li>If doTile is true then force the operation to work with a size of tiles equal to the
     * input Image .</li>
     * <li>run the operation.</li>
     * </p>
     * 
     * @param planarImage the source image.
     * @param dx the resolution in x direction.
     * @param dy the resolution in y direction.
     * @param doTile is the cobbleSource parameter.
     * @return the image after the operation have worked.
     */
    public static RenderedOp create( RenderedImage elevation, double dx, double dy, boolean doTile,
            PrintStream err, PrintStream out ) {
        if (!isRegistered) {
            register(OPERATIONNAME, "example");
            isRegistered = true;
        }
        int hTiling = elevation.getTileHeight();
        int wTiling = elevation.getTileWidth();
        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(elevation);

        parameterBlock.add(dx);
        parameterBlock.add(dy);
        parameterBlock.add(doTile);
        parameterBlock.add(err);
        parameterBlock.add(out);
        // create the hints for the operation, this isn't necessary, if I pass a null object the
        // default value is the input image value, but is better pass an explicit hints in
        // order to
        // avoid a multi-call over the same tile.
        RenderingHints hints = null;
        if (doTile) {
            // tiling
            hints = UtilityJAI.getRenderingHintsTailing(wTiling, hTiling);
        } else {
            // work with whole image
            hints = UtilityJAI
                    .getRenderingHintsTailing(elevation.getWidth(), elevation.getHeight());

        }
        // hints=null;

        // RenderingHints hints=null;
        return JAI.create(OPERATIONNAME, parameterBlock, hints);
    }
    /**
     * Register the Operation if it isn't.
     * 
     * @param OperationName the name of the operation
     * @param description
     */

    public static void register( String OperationName, String description ) {
        // register the operation "Slope"
        OperationRegistry or;
        or = JAI.getDefaultInstance().getOperationRegistry();
        or.registerDescriptor(new HMGradientDescriptor());
        RenderedImageFactory rif = new HMGradientRIF();
        RIFRegistry.register(or, OPERATIONNAME, description, rif);
    }
}
