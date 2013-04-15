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
package eu.hydrologis.jgrass.models.g.filter;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import eu.hydrologis.openmi.util.HydrologisDate;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class g_filter extends ModelsBackbone {

    private final static String inID = "in"; //$NON-NLS-1$

    private final static String outID = "out"; //$NON-NLS-1$ 

    private ILink inLink = null;

    private ILink outLink = null;

    private IOutputExchangeItem outputEI = null;

    private IInputExchangeItem inputEI = null;

    private String modelParameters = ""; //$NON-NLS-1$

    private int[] filterIndexes;

    private List<Integer> idsNames;

    private int columnsNum;

    private String dates;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public g_filter() {
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public g_filter( PrintStream output, PrintStream error ) {
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /*
     * a default implementation that takes the input and delivers it to the outputs
     */
    public void safeInitialize( IArgument[] properties ) throws Exception {

        // --column 9 --ids "1,243,345" --fields "0,2,5"

        String fields = null;
        String ids = null;
        String columns = null;
        if (properties != null) {
            for( IArgument argument : properties ) {
                String key = argument.getKey();
                if (key.equals("fields")) {
                    fields = argument.getValue();
                }
                if (key.equals("ids")) {
                    ids = argument.getValue();
                }
                if (key.equals("dates")) {
                    dates = argument.getValue();
                }
                if (key.equals("columns")) {
                    columns = argument.getValue();
                }
            }
        }

        if (columns == null) {
            throw new IllegalArgumentException("The argument is needed: --columns.");
        }

        if (fields != null) {
            String[] filterSplit = fields.split(",");
            filterIndexes = new int[filterSplit.length];
            try {
                for( int i = 0; i < filterSplit.length; i++ ) {
                    filterIndexes[i] = Integer.parseInt(filterSplit[i].trim());
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "The fields argument has to be a comma separated list of integer values.");
            }
        } else {
            filterIndexes = new int[0];
        }

        if (ids != null) {
            String[] idsSplit = ids.split(",");
            idsNames = new ArrayList<Integer>();
            try {
                for( int i = 0; i < idsSplit.length; i++ ) {
                    idsNames.add(Integer.parseInt(idsSplit[i].trim()));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "The ids argument has to be a comma separated list of integer values.");
            }
        } else {
            idsNames = Collections.emptyList();
        }

        try {
            columnsNum = Integer.parseInt(columns);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The columns argument has to be an integer value.");
        }

        /*
         * create the exchange items
         */
        inputEI = ModelsConstants.createDummyInputExchangeItem(this);
        outputEI = ModelsConstants.createDummyOutputExchangeItem(this);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {


        /*
         * for every calling link, give the input values back
         */
        ScalarSet input = ModelsConstants.getScalarSetFromLink(inLink, time, err);

        /*
         * filter away value through dates
         */
        if (time instanceof HydrologisDate) {
            HydrologisDate currentDate = (HydrologisDate) time;
            
            if (dates != null) {
                // we want to filter dates
                String dateString = dateFormatter.format(currentDate);
                
                if (!dateString.matches(dates)) {
                    out.println("Filtering away: " + dateString);
                    return null;
                }
            }
            
        }
        ScalarSet output = new ScalarSet();

        try {
            int index = 0;
            for( int i = 1; i < input.size(); i++ ) {
                int tmpId = input.get(i).intValue();
                if (idsNames.size() == 0 || idsNames.contains(tmpId)) {
                    // add the id
                    // output.add((double) tmpId);
                    // index++;
                    // get the needed values
                    if (filterIndexes.length == 0) {
                        // take them all
                        for( int j = 0; j < columnsNum; j++ ) {
                            double value = input.get(i + j);
                            output.add(value);
                            index++;
                        }
                    } else {
                        for( int j = 0; j < filterIndexes.length; j++ ) {
                            double value = input.get(i + filterIndexes[j] - 1);
                            output.add(value);
                            index++;
                        }
                    }
                }
                i = i + columnsNum - 1;
            }
            output.add(0, (double) index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException(
                    "An error occurred while parsing the input values with the supplied columns, fields and ids. As a hint note that the input values were "
                            + (input.size() - 1));
        }

        return output;
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
