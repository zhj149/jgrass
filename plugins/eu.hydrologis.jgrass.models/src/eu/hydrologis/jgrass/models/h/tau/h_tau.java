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
package eu.hydrologis.jgrass.models.h.tau;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.messages.MessageHelper;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the tau model. It estimates, site by
 * site, the value of the stress tangential due to surface runoff.
 * 
 * <pre>
 * &#964;<sub>b</sub>=((g&sup2*k*&#961;&sup3)/(8*&#965;<sup>c</sup>))<sup>1/3</sup>*S<sup>2/3</sup>*(q*A/b-TS)<sup>(2+c)/3</sup>
 * </pre>
 * 
 * where: g is gravity, k and c are parameters linked with the law expressing
 * the resistance coefficient, &#961; the water density, &#957; the cinematic
 * viscosity of the water, S the local slope, q the effective rain per area
 * unit, A the contributing area, b the draining boundary (which can be less
 * than the pixel size), T the soil transmissivity.
 * 
 * 
 * 
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of slope (-slope);</LI>
 * <LI>the map of the ab (-ab);</LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the tau (-tau);</LI>
 * </OL>
 * <P></DD>
 * Usage: h.tau --igrass-slope slope --igrass-ab ab --ograss-tau tau --rho rho
 * --g g --ni ni --q q --k k --c c --T T
 * </p>
 * <P>
 * <DT><STRONG>Notes:</STRONG><BR>
 * </DT>
 * <DD>If the soil transmissivity is considered null (T=0), then we estimate the
 * stress tangential to the bottom due to hortonian surface runoff, otherwise it
 * is estimated on the basis of dunnian runoff.
 * </P>
 * </DD>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Cozzini Andrea, Rigon
 *         Riccardo
 */
public class h_tau extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String slopeID = "slope";

    public final static String abID = "ab";

    public final static String tauID = "tau";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_tau.usage");

    private ILink slopeLink = null;

    private ILink abLink = null;

    private ILink tauLink = null;

    private IOutputExchangeItem tauDataOutputEI = null;

    private IInputExchangeItem slopeDataInputEI = null;

    private IInputExchangeItem abDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private double rho = -1.0;

    private double g = -1.0;

    private double ni = -1.0;

    private double q = -1.0;

    private double k = -1.0;

    private double c = -1.0;

    private double T = -1.0;

    private boolean doTile;

    private String locationPath;

    private JGrassGridCoverageValueSet jgrValueSet;

    /** */
    public h_tau() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    } // h_tau

    /** */
    public h_tau( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    } // h_tau

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(slopeID)) {
            slopeLink = link;
        }
        if (id.equals(abID)) {
            abLink = link;
        }
        if (id.equals(tauID)) {
            tauLink = link;
        }
    }

    /**
     * 
     */
    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: slope, ab
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return slopeDataInputEI;
        } else if (inputExchangeItemIndex == 1) {
            return abDataInputEI;
        }
        return null;
    }

    /**
     * return the number of IInputExchangeItem
     */
    public int getInputExchangeItemCount() {
        return 2;
    }

    /**
     * return a description for the model, in this case return the string to use
     * to execute the command from command-line
     */
    public String getModelDescription() {
        return modelParameters;
    }

    /**
     * there is an IOutputExchangeItem: tau
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return tauDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    /**
     * return the results of the model...
     */
    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(tauLink.getID())) {
            // reads input maps
            // out.println(Messages.getString("readsmap") + " SLOPE");
            if (jgrValueSet != null) {
                return jgrValueSet;
            }
            GridCoverage2D slopeGC = ModelsConstants.getGridCoverage2DFromLink(slopeLink, time, err);
            GridCoverage2D abGC = ModelsConstants.getGridCoverage2DFromLink(abLink, time, err);

            PlanarImage slopeImage = (PlanarImage) slopeGC.getRenderedImage();
            PlanarImage abImage = (PlanarImage) abGC.getRenderedImage();

            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);
            WritableRaster tauImage = tau(slopeImage, abImage);

            if (tauImage == null) {
                return null;
            } else {
                jgrValueSet = new JGrassGridCoverageValueSet(tauImage, activeRegion, crs);
                return jgrValueSet;
            }
        }
        return null;
    }

    /**
     * in this method map's properties are defined... location, mapset... and
     * than IInputExchangeItem and IOutputExchangeItem are reated
     */
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
                if (key.compareTo("rho") == 0) {
                    rho = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("g") == 0) {
                    g = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("ni") == 0) {
                    ni = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("q") == 0) {
                    q = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("k") == 0) {
                    k = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("c") == 0) {
                    c = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("t") == 0) {
                    T = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.getBoolean(argument.getValue());
                }
            }

        }

        /*
         * define the map path
         */
        locationPath = grassDb + File.separator + location;
        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);

        componentDescr = "h.tau";
        componentId = null;

        /*
         * create the exchange items
         */
        // tau output

        tauDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // element set defining what we want to read
        // slope input

        slopeDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // ab input

        abDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
    }

    /**
     * removes the links
     */
    public void removeLink( String linkID ) {
        if (linkID.equals(slopeLink.getID())) {
            slopeLink = null;
        }
        if (linkID.equals(abLink.getID())) {
            abLink = null;
        }
        if (linkID.equals(tauLink.getID())) {
            tauLink = null;
        }
    }

    /**
     * Calculates the stress tangential
     */
    private WritableRaster tau( PlanarImage slopeImage, PlanarImage abImage ) {
        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();
        // create the iter for the input maps.
        RandomIter slopeRandomIter = RandomIterFactory.create(slopeImage, null);
        RandomIter abRandomIter = RandomIterFactory.create(abImage, null);
        // create the output map and its RandomIter.
        // Rectangle imageBounds = slopeImage.getBounds();
        WritableRaster tauImage = FluidUtils.createDoubleWritableRaster(slopeImage.getWidth(), slopeImage.getHeight(), null,
                abImage.getSampleModel(), null);
        WritableRandomIter tauRandomIter = RandomIterFactory.createWritable(tauImage, null);
        // create new vector
        double[] esp = new double[4];
        double[] term = new double[6];

        // calculates exponents
        esp[0] = 1.0 / 3.0;
        esp[1] = -c / 3.0;
        esp[2] = (2 + c) / 3.0;
        esp[3] = 2.0 / 3.0;
        // calculates the constant term of the expression above.
        term[0] = Math.pow(g, 2);
        term[1] = Math.pow(term[0], esp[0]) * rho * 0.5;
        term[2] = Math.pow(ni, esp[1]);
        term[3] = Math.pow(k, esp[0]);

        // calculats the stress tangential...
        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        pm.beginTask(MessageHelper.WORKING_ON + "h.tau...", rows);
        for( int i = 0; i < rows; i++ ) {
            for( int j = 0; j < cols; j++ ) {
                if (isNovalue(slopeRandomIter.getSampleDouble(j, i, 0))) {
                    tauRandomIter.setSample(j, i, 0, JGrassConstants.doubleNovalue);
                } else {
                    term[4] = (q * abRandomIter.getSampleDouble(j, i, 0) - T * slopeRandomIter.getSampleDouble(j, i, 0));
                    if (term[4] <= 0) {
                        tauRandomIter.setSample(j, i, 0, 0);
                    } else {
                        term[5] = Math.pow(term[4], esp[2]);
                        tauRandomIter.setSample(j, i, 0, (term[1] * term[3] * term[2]
                                * Math.pow(slopeRandomIter.getSampleDouble(j, i, 0), esp[3]) * term[5]));
                    }
                }
            }
            pm.worked(1);
        }
        pm.done();

        return tauImage;
    }
}
