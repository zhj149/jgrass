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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

import nl.alterra.openmi.sdk.backbone.LinkableComponent;
import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.openmi.ModelsIOException;
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.utils.FluidUtils;

/**
 * A {@link LinkableComponent linkable component} that reads scalar values from file.
 * 
 * <p>
 * This component is not time aware, as instead its brother
 * {@link InputScalarTimeReader} is. This means that the data in the file 
 * are read as a single block and this may be useful just if the 
 * thing is done once in the whole time cycle.
 * </p>
 * 
 * <p>NOTES: 
 * <ul>
 * <li>the scalar values are supposed to be whitespace separated</li>
 * <li>the first value of the resulting {@link ScalarSet} is the number of 
 * columns found in the file, which is useful for formatting or data
 * matrix creation in the following modules.</li> 
 * </ul>
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @since 3.0
 */
public class InputScalarReader extends ModelsBackbone {

    private ILink outputLink = null;

    private IOutputExchangeItem scalarSetOutputEI = null;

    private static final String modelParameters = "Returns ScalarSets from models for usage";

    private File file;

    private ScalarSet valueSet;

    public InputScalarReader() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public InputScalarReader( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String fileName = null;
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("iscalar")) {
                fileName = argument.getValue();
            }
        }
        if (fileName != null) {
            file = new File(fileName);
            if (!file.exists()) {
                throw new ModelsIllegalargumentException("Scalar values file doesn't exist: "
                        + fileName, this);
            }
        }

        scalarSetOutputEI = ModelsConstants.createDummyOutputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        /*
         * this is read just once
         */
        if (valueSet == null) {
            valueSet = new ScalarSet();
            try {
                boolean isFirst = true;
                BufferedReader bR = new BufferedReader(new FileReader(file));
                String line = null;
                while( (line = bR.readLine()) != null ) {
                    String[] splitLine = line.split("\\s+"); //$NON-NLS-1$
                    if (isFirst) {
                        valueSet.add((double) splitLine.length);
                        isFirst = false;
                    }
                    for( String numStr : splitLine ) {
                        valueSet.add(new Double(numStr));
                    }
                }
                bR.close();
            } catch (Exception e) {
                throw new ModelsIOException(
                        "An error occurred while parsing the scalar values input file. Check your file format or availablility.",
                        this);
            }
        }

        return valueSet;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public void addLink( ILink link ) {
        outputLink = link;
    }

    public void finish() {
    }

    public int getInputExchangeItemCount() {
        return 0;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return null;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return scalarSetOutputEI;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        outputLink = null;
    }

}
