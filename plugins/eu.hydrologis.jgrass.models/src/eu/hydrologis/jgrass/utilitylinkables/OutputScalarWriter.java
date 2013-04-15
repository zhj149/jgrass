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
package eu.hydrologis.jgrass.utilitylinkables;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.LinkableComponent;
import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.ILinkableComponent;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * A {@link LinkableComponent} that writes {@link ScalarSet}s to an output stream.
 * 
 * <p>
 * <b>Cases:</b>
 * <ul>
 * <li><b>the model is run with time dependency</b>, the whole passed data 
 * are written to a single line (i.e. the first value, carrying the columns 
 * info is ignored)</li>
 * <li><b>the model is run withouttime dependency</b>, the values are
 * formatted as required by the first passed value of the scalaset.</li>
 * </p>
 * </ul>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class OutputScalarWriter extends ModelsBackbone {

    private ILink inputLink = null;

    private IInputExchangeItem scalarSetInputEI = null;

    private File file;
    private PrintStream outStream = null;
    private boolean isFileBased = false;

    private static final String modelParameters = "Returns ScalarSets from models for usage";

    public OutputScalarWriter() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public OutputScalarWriter( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String fileName = null;
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("oscalar")) {
                fileName = argument.getValue();
            }
        }
        if (fileName != null) {
            file = new File(fileName);
            File parentFile = file.getParentFile();
            if (parentFile == null || !parentFile.exists()) {
                // in that case send to console
                file = null;
            }
        }

        scalarSetInputEI = ModelsConstants.createDummyInputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        ILinkableComponent sourceComponent = inputLink.getSourceComponent();
        IValueSet valueSet = sourceComponent.getValues(time, inputLink.getID());
        if (outStream == null) {
            if (file != null) {
                outStream = new PrintStream(file);
                isFileBased = true;
            } else {
                outStream = out;
            }
        }

        if (valueSet instanceof ScalarSet) {
            ScalarSet sS = (ScalarSet) valueSet;
            if (time == null) {
                // follow formatting due to first value
                Double columnsNum = sS.get(0);
                int i = 1;
                while( i < sS.getCount() ) {
                    for( int j = 0; j < columnsNum; j++ ) {
                        outStream.print(sS.get(i));
                        outStream.print("\t"); //$NON-NLS-1$
                        i++;
                    }
                    outStream.print("\n"); //$NON-NLS-1$
                }
            } else {
                int i = 1;
                while( i < sS.getCount() ) {
                    outStream.print(sS.get(i));
                    outStream.print("\t"); //$NON-NLS-1$
                    i++;
                }
                outStream.print("\n"); //$NON-NLS-1$
            }
        } else {
            if (valueSet == null) {
                out.println("oscalar warning: ignoring null value passed.");
            } else {
                throw new IOException(
                        "oscalar: This writer is able to write only ScalarSets. Instead got: "
                                + valueSet.getClass().getSimpleName());
            }
        }
        return null;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public void addLink( ILink link ) {
        inputLink = link;
    }

    public void finish() {
        if (isFileBased)
            outStream.close();
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return scalarSetInputEI;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 0;
    }

    public void removeLink( String linkID ) {
        inputLink = null;
    }

}
