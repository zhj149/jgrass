package eu.hydrologis.jgrass.utilitylinkables;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.StringSet;

/**
 * Helper class to write categories files the openmi way.
 * 
 * <p>The class takes a map name and a string containing the whole categories definition.</p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 */
public class CategoriesMapWriter extends ModelsBackbone {
    private ILink inputLink = null;

    private IInputExchangeItem catsTableInputEI = null;

    private String catsFileName;

    private static final String modelParameters = "..."; //$NON-NLS-1$

    private String activeRegionPath = null;

    public CategoriesMapWriter() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public CategoriesMapWriter( PrintStream output, PrintStream error ) {
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
        return catsTableInputEI;
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
            err.println("An error occurred while linking the models.");
            return null;
        } else {
            IValueSet valueSet = inputLink.getSourceComponent().getValues(time, inputLink.getID());
            // writes categories
            try {
                out.println("Writing categories for map: " + catsFileName);
                String catsPath = activeRegionPath + File.separator + JGrassConstants.CATS
                        + File.separator + catsFileName;
                BufferedWriter out = new BufferedWriter(new FileWriter(catsPath));
                out.write("#" + ((StringSet) valueSet).size() + "\n");
                out.write(catsFileName + "\n");
                out.write("" + "\n");
                out.write("0.00 0.00 0.00 0.00" + "\n");
                for( int i = 0; i < ((StringSet) valueSet).getCount(); i++ ) {
                    out.write(((StringSet) valueSet).getValue(i) + "\n");
                }
                out.close();
            } catch (IOException e) {
            }
        }
        return null;
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        String mapset = null;
        String unitID = "catsmap";
        int numCol = 1;
        for( IArgument argument : properties ) {
            String key = argument.getKey();
            if (key.equals("ocats")) {
                catsFileName = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.GRASSDB) == 0) {
                grassDb = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.LOCATION) == 0) {
                location = argument.getValue();
            }
            if (key.compareTo(ModelsConstants.MAPSET) == 0) {
                mapset = argument.getValue();
            }

        }
        componentId = "";
        componentDescr = "";

        /*
         * define the map path
         */
        activeRegionPath = grassDb + File.separator + location + File.separator + mapset;

        /*
         * create the output exchange item that will be passed over the link to which the
         * component is link to other components
         */
        catsTableInputEI = ModelsConstants.createDummyInputExchangeItem(this);

    }

    public void removeLink( String linkID ) {

    }
}
