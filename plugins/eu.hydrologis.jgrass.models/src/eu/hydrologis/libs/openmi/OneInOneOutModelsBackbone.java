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
package eu.hydrologis.libs.openmi;

import java.io.PrintStream;

import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;

import eu.hydrologis.libs.utils.FluidUtils;

/**
 * A linkable component that takes an input and an output. With this just safeinitialize and
 * safegetValues need to be implemented.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public abstract class OneInOneOutModelsBackbone extends ModelsBackbone {

    protected final static String inID = "in"; //$NON-NLS-1$

    protected final static String outID = "out"; //$NON-NLS-1$ 

    protected ILink inLink = null;

    protected ILink outLink = null;

    protected IOutputExchangeItem outputEI = null;

    protected IInputExchangeItem inputEI = null;

    protected String modelParameters = ""; //$NON-NLS-1$
    
    public OneInOneOutModelsBackbone() {
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public OneInOneOutModelsBackbone( PrintStream output, PrintStream error ) {
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(inID)) {
            inLink = link;
        }
        if (id.equals(outID)) {
            outLink = link;
        }
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return inputEI;
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        
        return outputEI;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(inLink.getID())) {
            inLink = null;
        }
        if (linkID.equals(outLink.getID())) {
            outLink = null;
        }
    }

}
