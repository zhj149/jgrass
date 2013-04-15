/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) C.U.D.A.M. Universita' di Trento
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
package eu.hydrologis.jgrass.ui.console;

import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public class BacktraceConsole extends MessageConsole {

    // Attributes
    /** */
    private final static String m_title = "Backtrace";

    /** */
    public final PrintStream internal;

    /** */
    public final PrintStream err;

    /** */
    public final PrintStream out;

    // Construction
    /** */
    public BacktraceConsole() {

        super(m_title, null);

        this.setTabWidth(4);
        MessageConsoleStream __internalStream;
        internal = new PrintStream(__internalStream = newMessageStream(), true);
        __internalStream.setColor(ConsoleUIPlugin.COLOR_GRAY);
        MessageConsoleStream __errorStream;
        err = new PrintStream(__errorStream = newMessageStream(), true);
        __errorStream.setColor(ConsoleUIPlugin.COLOR_RED);
        MessageConsoleStream __outputStream;
        out = new PrintStream(__outputStream = newMessageStream(), true);
        __outputStream.setColor(ConsoleUIPlugin.COLOR_BLACK);
    } // BacktraceConsole

    /** */

    protected void dispose() {

        super.dispose();
    } // dispose

    // Operations
    /** */

    public void setName( String arg0 ) {

        super.setName(arg0);
    } // setName

} // BacktraceConsole
