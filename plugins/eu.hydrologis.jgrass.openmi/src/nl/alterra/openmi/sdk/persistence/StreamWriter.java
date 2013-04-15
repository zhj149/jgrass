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
import java.io.OutputStream;
import nl.alterra.openmi.sdk.configuration.Composition;
import nl.alterra.openmi.sdk.configuration.SystemDeployer;

/**
 * Base class for OpenMI writers that handle streams.
 */
public abstract class StreamWriter implements IOpenMIWriter {

    /**
     * The output connection is a stream.
     */
    protected OutputStream outStream = null;

    /**
     * Writes the specified string to the stream.
     *
     * @param str String to write
     * @throws IOException
     */
    protected void writeToStream(String str) throws IOException {
        if (outStream == null) {
            throw new PersistenceException("Output stream not ready!");
        }

        if (str != null) {
            outStream.write(str.getBytes());
        }
    }

    public abstract void write(SystemDeployer aSystem) throws IOException;

    public abstract void write(Composition aComposition) throws IOException;

    /**
     * Writes a SystemDeployer to the specified stream.
     *
     * @param aStream OutputStream to use
     * @param aSystem SystemDeployer to write
     * @throws IOException
     */
    public final void writeToStream(OutputStream aStream, SystemDeployer aSystem) throws IOException {
        outStream = aStream;
        if (outStream != null) {
            write(aSystem);
        }
    }

    /**
     * Writes a Composition to the specified stream.
     *
     * @param aStream      OutputStream to use
     * @param aComposition Composition to write
     * @throws IOException
     */
    public final void writeToStream(OutputStream aStream, Composition aComposition) throws IOException {
        outStream = aStream;
        if (outStream != null) {
            write(aComposition);
        }
    }

}
