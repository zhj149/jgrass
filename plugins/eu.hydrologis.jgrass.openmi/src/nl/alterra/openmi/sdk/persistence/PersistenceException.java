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

/**
 * IOException which can be raised by classes in the persistency
 * package, to indicate an error.
 */
public class PersistenceException extends IOException {

    /**
     * Creates an instance with the specified message.
     *
     * @param message
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Creates an instance with the specified formatted string.
     *
     * @param format String format specifier
     * @param args Arguments to use in the formatted string
     */
    public PersistenceException(String format, Object... args) {
        super(String.format(format, args));
    }
}
