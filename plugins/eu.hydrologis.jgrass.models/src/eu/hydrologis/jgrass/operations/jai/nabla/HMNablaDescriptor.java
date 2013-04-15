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
package eu.hydrologis.jgrass.operations.jai.nabla;

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
 * The Horton Machine Nabla algorithm jai operation descriptor.
 * 
 * @author <a /href='mailto:daniele.andreis@gmail.com>Daniele andreis</a>
 */
public class HMNablaDescriptor extends OperationDescriptorImpl {

    public static final String OPERATIONNAME = "HMNabla";

    private static final String[] paramNames = {"errorStream","outputStream", "xres", "yres", "mode", "threshold","isTiled"};

    private static final Object[] paramDefaults = {System.out,System.out, null, null, 0, null,false};

    private static final Class[] paramClasses = {java.io.PrintStream.class,java.io.PrintStream.class, Double.class,
            Double.class, Integer.class, Double.class,Boolean.class};

    private static final int numSources = 1;

    private static final String[] supportedModes = {"rendered"};

    private static final Object[] validParamValues = {
            null,null,
            new javax.media.jai.util.Range(Double.class, new Double(0.0), new Double(
                    Double.MAX_VALUE)),
            new javax.media.jai.util.Range(Double.class, new Double(0.0), new Double(
                    Double.MAX_VALUE)),
            new javax.media.jai.util.Range(Integer.class, new Integer(0), new Integer(1)),
            new javax.media.jai.util.Range(Double.class, new Double(0.0), new Double(
                    Double.MAX_VALUE)),null};
    private static boolean isRegistered = false;
    private static final String[][] resources = {{"GlobalName", OPERATIONNAME},
            {"LocalName", OPERATIONNAME}, {"Vendor", "eu.hydrologis.jgrass"},
            {"Description", "Computes the gradient map"}, {"DocURL", "http://www.hydrologis.com"},
            {"Version", "Beta"}};

    public HMNablaDescriptor() {
        super(resources, supportedModes, numSources, paramNames, paramClasses, paramDefaults,
                validParamValues);
    }
    /**
     * This is the <i>factory</i> of the RenderedOp.
     * <p>
     * <li>Verify if the Operation is already registered (if not then register it</li>
     * <li>Force the operation to work with a sizeTilingxsizeTiling tiles (i have do it only to
     * test).</li>
     * </p>
     * 
     * @param planarImage the source image
     * @param dir
     * @param delta
     * @return the image after the operation have worked.
     */
    public static RenderedOp create( RenderedImage elevation, double dx, double dy,
            PrintStream err, PrintStream out, int mode, double threshold, boolean doTile ) {
        if (!isRegistered) {
            register(OPERATIONNAME, "example");
            isRegistered = true;
        }
        int hTiling = elevation.getTileHeight();
        int wTiling = elevation.getTileWidth();
        ParameterBlock parameterBlock = new ParameterBlock();
        parameterBlock.addSource(elevation);
        parameterBlock.add(err);
        parameterBlock.add(out);
        parameterBlock.add(dx);
        parameterBlock.add(dy);
        parameterBlock.add(mode);
        parameterBlock.add(threshold);
        parameterBlock.add(doTile);

        RenderingHints hints = UtilityJAI.getRenderingHintsTailing(wTiling, hTiling);
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
        or.registerDescriptor(new HMNablaDescriptor());
        RenderedImageFactory rif = new HMNablaRIF();
        RIFRegistry.register(or, OperationName, description, rif);
    }
}
