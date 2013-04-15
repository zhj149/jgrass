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
import java.io.PrintStream;
import java.text.MessageFormat;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.StringSet;

/**
 * <p>
 * A linkable data object that is able to take string data and write them to disk
 * </p>
 * <p>
 * NOTE: the values are written as they come. The String has to have 
 * line feeds and formatting already present.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class OutputStringWriter extends ModelsBackbone {

    private ILink inputLink = null;

    private IInputExchangeItem stringSetInputEI = null;

    private File file;

    private static final String modelParameters = "Writes StringSets from models for usage";

    public OutputStringWriter() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public OutputStringWriter( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String fileName = null;
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("ostring")) {
                fileName = argument.getValue();
            }
        }
        if (fileName != null) {
            file = new File(fileName);
            File parentFile = file.getParentFile();
            if (parentFile == null || !parentFile.exists()) {
                file = null;
            }
        }

        stringSetInputEI = ModelsConstants.createDummyInputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        IValueSet valueSet = inputLink.getSourceComponent().getValues(time, inputLink.getID());
        if (file != null) {
            out = new PrintStream(file);
        }
        if (valueSet instanceof StringSet) {
            StringSet sS = (StringSet) valueSet;
            for( String entry : sS ) {
                out.print(entry);
            }
        } else if (valueSet instanceof ScalarSet) {
            // scalarsets carry the first value as column number
            ScalarSet sS = (ScalarSet) valueSet;
            int columns = sS.get(0).intValue();
            int size = sS.size();

            for( int i = 1; i < size; i++ ) {
                Double v = sS.get(i);
                out.print(v);
                if (i % columns == 0) {
                    out.print("\n");
                } else {
                    out.print("\t");
                }
            }

        } else {
            throw new IllegalArgumentException(
                    MessageFormat
                            .format(
                                    "A StringSet or ScalarSet was expected but a {0} was found instead. Check your syntax.",
                                    valueSet.getClass()));
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
        out.close();
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return stringSetInputEI;
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
