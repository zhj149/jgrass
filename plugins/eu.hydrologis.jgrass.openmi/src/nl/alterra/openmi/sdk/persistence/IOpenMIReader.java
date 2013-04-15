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
import nl.alterra.openmi.sdk.configuration.Composition;
import nl.alterra.openmi.sdk.configuration.SystemDeployer;

/**
 * Interface for classes that provide "read" persistency functionality for
 * OpenMI.
 */
public interface IOpenMIReader {
    
    /**
     * Retrieves a new SystemDeployer from the active input connection
     * (i.e. a file or a database). The reader will try to reconstruct
     * as much as possible from the input. Fatal errors will result in
     * exceptions, non fatal should be checked by calling the completed-
     * Succesfully() method. Detailed messages can then be retrieved by
     * the getLinkErrors() and getComponentErrors() methods.
     *
     * @return SystemDeployer Newly created SystemDeployer
     * @throws IOException
     */
    public SystemDeployer readSystemDeployer() throws IOException;

    /**
     * Retrieves a new Composition from the active input connection
     * (i.e. a file or a database). The reader will try to reconstruct
     * as much as possible from the input. Fatal errors will result in
     * exceptions, non fatal should be checked by calling the completed-
     * Succesfully() method. Detailed messages can then be retrieved by
     * the getLinkErrors() and getComponentErrors() methods.
     *
     * @return Composition Newly created Composition
     * @throws IOException
     */
    public Composition readComposition() throws IOException;

    /**
     * Indicates if the last read action from the IOpenMIReader interface
     * was completed successfully or not. Read methods should make an
     * attempt to restore as much as possible from the input, which can
     * result in missing links or linkable components that could not be
     * reconstructed and had to be replaced by placeholders. Where "hard"
     * errors will result in exceptions, these "soft" errors can be
     * detected by calling this method. More information can be retrieved
     * by calling getLinkErrors() and getComponentErrors().
     *
     * @return False if some kind of error or warning occurred
     */
    public boolean completedSuccesfully();

    /**
     * Gets the messages related to problems with the reconstruction of
     * links on the last read action from the IOpenMIReader interface. When
     * no problems occured the returned array will have lenth 0.
     *
     * @return String[] with zero or more messages
     */
    public String[] getLinkErrors();

    /**
     * Gets the messages related to problems with the reconstruction of
     * components on the last read action from the IOpenMIReader interface.
     * When no problems occured the returned array will have lenth 0.
     *
     * @return String[] with zero or more messages
     */
    public String[] getComponentErrors();

}
