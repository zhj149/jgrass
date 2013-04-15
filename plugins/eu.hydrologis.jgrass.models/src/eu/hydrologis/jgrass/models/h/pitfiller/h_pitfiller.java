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
package eu.hydrologis.jgrass.models.h.pitfiller;

import java.io.File;
import java.io.PrintStream;

import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.jgrasstools.hortonmachine.modules.demmanipulation.pitfiller.Pitfiller;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;

/**
 * <p>
 * Straight port of the pitfiller correction model found in the TARDEM suite.
 * </p>
 * <p>
 * Translated to java and adapted to be opemi based.
 * </p>
 * 
 * @author David Tarboton - http://www.neng.usu.edu/cee/faculty/dtarb/tardem.html#programs
 * @author Andrea Antonello - www.hydrologis.com
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it
 */
public class h_pitfiller extends ModelsBackbone {

    /*
     * OPENMI VARIABLES
     */
    public final static String elevID = "elevation";

    public final static String pitID = "pit";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_pitfiller.usage");

    private ILink elevLink = null;

    private ILink pitLink = null;

    private IOutputExchangeItem pitDataOutputEI = null;

    private IInputExchangeItem elevDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private final PrintStream out;

    static final double FLOW_NO_VALUE = FluidConstants.flownovalue;
    private String locationPath;
    WritableRandomIter fileRandomIter = null;

    public h_pitfiller() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_pitfiller( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {

        String grassDb = null;
        String location = null;
        String mapset = null;
        if (properties != null) {
            for( IArgument argument : properties ) {
                String key = argument.getKey();
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
        }

        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator
                + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.pitfliller";
        componentId = "pit";

        /*
         * create the exchange items
         */
        // input element set
        elevDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        // output element set

        pitDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(pitLink.getID())) {
            // don't do things twice
            if (jgrValueSet != null) {
                return jgrValueSet;
            }
            
            GridCoverage2D elevData = ModelsConstants.getGridCoverage2DFromLink(elevLink, time, out);
            PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out, err);
            Pitfiller pitfiller = new Pitfiller();
            pitfiller.inDem = elevData;
            pitfiller.pm = pm;
            pitfiller.process();
            GridCoverage2D pitfillerCoverage = pitfiller.outPit;
            
            jgrValueSet = new JGrassGridCoverageValueSet(pitfillerCoverage);
            return jgrValueSet;
        }
        return null;
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(elevID)) {
            elevLink = link;
        }
        if (id.equals(pitID)) {
            pitLink = link;
        }
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return elevDataInputEI;
        } else {
            return null;
        }
    }

    public int getInputExchangeItemCount() {
        return 1;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return pitDataOutputEI;
        } else {
            return null;
        }
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(elevLink.getID())) {
            elevLink = null;
        }
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
    }

}
