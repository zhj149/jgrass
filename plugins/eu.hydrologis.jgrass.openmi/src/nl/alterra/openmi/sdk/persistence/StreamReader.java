/* ***************************************************************************
 *
 *    Copyright (C) 2006 Alterra, Wageningen University and Research centre.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *    or look at URL www.gnu.org/licenses/lgpl.html
 *
 *****************************************************************************
 *
 * @author Rob Knapen, Alterra B.V., The Netherlands
 * @author Wim de Winter, Alterra B.V., The Netherlands
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.persistence;

import java.io.IOException;
import java.io.InputStream;
import nl.alterra.openmi.sdk.configuration.Composition;
import nl.alterra.openmi.sdk.configuration.SystemDeployer;

/**
 * Base class for OpenMI readers that handle streams.
 */
public abstract class StreamReader implements IOpenMIReader {

    /**
     * The input connection is a stream.
     */
    protected InputStream inStream = null;

    /**
     * Retrieves a new SystemDeployer from the active input connection
     * (i.e. a file or a database).
     *
     * @return SystemDeployer
     * @throws IOException
     */
    public abstract SystemDeployer readSystemDeployer() throws IOException;

    /**
     * Retrieves a new Composition from the active input connection
     * (i.e. a file or a database).
     *
     * @return Composition
     * @throws IOException
     */
    public abstract Composition readComposition() throws IOException;

    /**
     * Reads a SystemDeployer from the specified stream.
     *
     * @param aStream InputStream to use
     * @return SystemDeployer, can be null
     * @throws IOException
     */
    public final SystemDeployer readSystemDeployer(InputStream aStream) throws IOException {
        inStream = aStream;
        if (inStream != null) {
            return readSystemDeployer();
        }
        return null;
    }

    /**
     * Reads a Composition from the specified stream.
     *
     * @param aStream InputStream to use
     * @return Composition, can be null
     * @throws IOException
     */
    public final Composition readComposition(InputStream aStream) throws IOException {
        inStream = aStream;
        if (inStream != null) {
            return readComposition();
        }
        return null;
    }

}
