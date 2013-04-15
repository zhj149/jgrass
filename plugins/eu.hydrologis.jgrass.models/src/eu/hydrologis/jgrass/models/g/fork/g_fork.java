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
package eu.hydrologis.jgrass.models.g.fork;

import java.io.PrintStream;

import eu.hydrologis.libs.openmi.OneInManyOutModelsBackbone;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class g_fork extends OneInManyOutModelsBackbone {

    public g_fork() {
        super();
        modelParameters = eu.hydrologis.libs.messages.help.Messages
                .getString("g_fork.usage");
    }

    public g_fork( PrintStream output, PrintStream error ) {
        super(output, error);
        modelParameters = eu.hydrologis.libs.messages.help.Messages
                .getString("g_fork.usage");
    }

}
