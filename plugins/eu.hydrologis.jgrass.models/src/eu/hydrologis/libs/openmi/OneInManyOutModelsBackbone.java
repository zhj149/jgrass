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
import java.util.ArrayList;
import java.util.List;

import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.util.HydrologisDate;

/**
 * A linkable component that takes an input and is able to supply it to many outputs. This can be
 * usefull for example if the result has to be sent to a tablewriter as well as to a chartengine.
 * Note that in its default implementation the input is simply passed to all requesting outputs.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public abstract class OneInManyOutModelsBackbone extends ModelsBackbone {

    protected final static String inID = "in"; //$NON-NLS-1$

    protected final static String outID = "out"; //$NON-NLS-1$ 

    protected ILink inLink = null;

    protected List<ILink> outLinks = new ArrayList<ILink>();

    protected IOutputExchangeItem outputEI = null;

    protected IInputExchangeItem inputEI = null;

    protected JGrassRegion activeRegion;

    protected String modelParameters = ""; //$NON-NLS-1$

    private IValueSet valueSet;

    private HydrologisDate previoushTime;

    public OneInManyOutModelsBackbone() {
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public OneInManyOutModelsBackbone( PrintStream output, PrintStream error ) {
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        // we are source component and "in" is used
        if (id.equals(inID) && link.getSourceComponent().equals(this)) {
            outLinks.add(link);
        }
        // we are target component and "in" is used
        else if (id.equals(inID) && link.getTargetComponent().equals(this)) {
            inLink = link;
        }
        // we are source component and "out" is used
        else if (id.equals(outID) && link.getSourceComponent().equals(this)) {
            outLinks.add(link);
        }
        // we are target component and "out" is used
        else if (id.equals(outID) && link.getTargetComponent().equals(this)) {
            inLink = link;
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

        for( int i = 0; i < outLinks.size(); i++ ) {
            if (linkID.equals(outLinks.get(i))) {
                outLinks.remove(i);
                break;
            }
        }
    }

    /*
     * a default implementation that takes the input and delivers it to the outputs
     */
    public void safeInitialize( IArgument[] properties ) throws Exception {
        /*
         * create the exchange items
         */
        inputEI = ModelsConstants.createDummyInputExchangeItem(this);
        outputEI = ModelsConstants.createDummyOutputExchangeItem(this);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (time instanceof HydrologisDate) {
            HydrologisDate hTime = (HydrologisDate) time;
            if (previoushTime == null || !previoushTime.equals(hTime)) {
                valueSet = inLink.getSourceComponent().getValues(time, inLink.getID());
                previoushTime = hTime;
            }
        } else {
            if (valueSet == null) {
                valueSet = inLink.getSourceComponent().getValues(time, inLink.getID());
            }
        }
        return valueSet;
    }

}
