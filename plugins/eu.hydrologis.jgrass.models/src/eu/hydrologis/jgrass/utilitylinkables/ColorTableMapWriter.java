/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) { 
 * HydroloGIS - www.hydrologis.com                                                   
 * C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam                               
 * The JGrass developer team - www.jgrass.org                                         
 * }
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package eu.hydrologis.jgrass.utilitylinkables;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.openmi.standard.IArgument;
import org.openmi.standard.IElementSet;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.IQuantity;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassElementset;
import eu.hydrologis.openmi.StringSet;
import eu.hydrologis.openmi.util.UtilitiesFacade;

/**
 * This method writes color table of maps
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 */
public class ColorTableMapWriter extends ModelsBackbone {

    private ILink inputLink = null;

    private IInputExchangeItem colorTableInputEI = null;

    private String colorFileName;

    private static final String modelParameters = "...";

    private String activeRegionPath = null;

    public ColorTableMapWriter() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public ColorTableMapWriter( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void addLink( ILink link ) {
        inputLink = link;
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        return colorTableInputEI;
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 0;
    }

    public IValueSet safeGetValues( ITime time, String linkID ) {
        /*
         * trigger the linked model
         */
        if (inputLink == null) {
            err.println("Color table not set.");
            return null;
        } else {
            IValueSet valueSet = inputLink.getSourceComponent().getValues(time, inputLink.getID());
            // writes color table
            try {
                out.println("Writing colors table");
                BufferedWriter out = new BufferedWriter(new FileWriter(activeRegionPath
                        + File.separator + "colr" + File.separator + colorFileName));
                for( int i = 0; i < ((StringSet) valueSet).getCount(); i++ ) {
                    out.write(((StringSet) valueSet).getValue(i) + "\n");
                }
                out.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    public void safeInitialize( IArgument[] properties ) throws Exception{

        String grassDb = null;
        String location = null;
        String mapset = null;
        String unitID = "colormap";
        int numCol = 1;
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("ocolor")) {
                colorFileName = argument.getValue();
            }
            if (key.compareTo("grassdb") == 0) {
                grassDb = argument.getValue();
            }
            if (key.compareTo("location") == 0) {
                location = argument.getValue();
            }
            if (key.compareTo("mapset") == 0) {
                mapset = argument.getValue();
            }

            componentId = "";
            componentDescr = "";

            /*
             * define the map path
             */
            activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                    + File.separator;
            /*
             * create the exchange items
             */
            // dummy element set
            IElementSet elementSet = new JGrassElementset("color_map", numCol);
            IQuantity quantity = UtilitiesFacade.createScalarQuantity("", unitID);

            /*
             * create the output exchange item that will be passed over the link to which the
             * component is link to other components
             */
            colorTableInputEI = UtilitiesFacade.createInputExchangeItem(this, quantity, elementSet);
        }

    }

    public void removeLink( String linkID ) {

    }

}
