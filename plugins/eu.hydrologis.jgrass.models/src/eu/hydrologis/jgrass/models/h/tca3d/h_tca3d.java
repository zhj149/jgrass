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
package eu.hydrologis.jgrass.models.h.tca3d;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;

import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openmi.standard.IArgument;
import org.openmi.standard.IInputExchangeItem;
import org.openmi.standard.ILink;
import org.openmi.standard.IOutputExchangeItem;
import org.openmi.standard.ITime;
import org.openmi.standard.IValueSet;

import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.libs.messages.Messages;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

;
/**
 * <p>
 * The openmi compliant representation of the tca3d model. It estimates the real
 * draining area and not only its projection on the plane as the TCA do.
 * </p>
 * <dt><strong>Inputs: </strong></dt>
 * <ol>
 * <li>the map containing the elevations (-pit);</li>
 * <li>the map containing the drainage directions (-flow);</li>
 * </ol>
 * <dt><strong>Returns:<br>
 * </strong></dt> <dd>
 * <ol>
 * <li>the map of tca3d (-tca3d)</li>
 * </ol>
 * <p></dd>
 * <p>
 * Usage: h.tca3d --igrass-pit pit --igrass-flow flow --ograss-tca3d tca3d
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Riccardo Rigon
 */
public class h_tca3d extends ModelsBackbone {
    /*
     * OPENMI VARIABLES
     */
    public final static String pitID = "pit";

    public final static String flowID = "flow";

    public final static String tca3dID = "tca3d";

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_tca3d.usage");

    private ILink pitLink = null;

    private ILink flowLink = null;

    private ILink tca3dLink = null;

    private IOutputExchangeItem tca3dDataOutputEI = null;

    private IInputExchangeItem pitDataInputEI = null;

    private IInputExchangeItem flowDataInputEI = null;

    private JGrassRegion activeRegion = null;

    private JGrassGridCoverageValueSet jgrValueSet;

    private String locationPath;

    private boolean doTile;

    public h_tca3d() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_tca3d( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    /**
     * 
     */
    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(pitID)) {
            pitLink = link;
        }
        if (id.equals(flowID)) {
            flowLink = link;
        }
        if (id.equals(tca3dID)) {
            tca3dLink = link;
        }
    }

    public void finish() {

    }

    /**
     * There is an IInputExchangeItem: pit, flow
     */
    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return pitDataInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return flowDataInputEI;
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
     * there is an IOutputExchangeItem: d2o3dength
     */
    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return tca3dDataOutputEI;
        }
        return null;
    }

    /**
     * return the number of IOutputExchangeItem
     */
    public int getOutputExchangeItemCount() {
        return 1;
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {
        if (linkID.equals(tca3dLink.getID())) {
            if (jgrValueSet != null) {
                return jgrValueSet;
            }
            IValueSet pitValueSet = pitLink.getSourceComponent().getValues(time, pitLink.getID());
            GridCoverage2D pitGC = null;
            IValueSet flowValueSet = flowLink.getSourceComponent().getValues(time, flowLink.getID());
            GridCoverage2D flowGC = null;
            if (pitValueSet != null && flowValueSet != null) {
                pitGC = ((JGrassGridCoverageValueSet) pitValueSet).getGridCoverage2D();
                flowGC = ((JGrassGridCoverageValueSet) flowValueSet).getGridCoverage2D();
            } else {
                String error = Messages.getString("erroreading"); //$NON-NLS-1$
                err.println(error);
                throw new IOException(error);
            }

            // out.println(Messages.getString("readsmap") + " AB");

            GridCoverage2D view = pitGC.view(ViewType.GEOPHYSICS);
            PlanarImage pitImage = (PlanarImage) view.getRenderedImage();
            view = flowGC.view(ViewType.GEOPHYSICS);
            PlanarImage flowImage = (PlanarImage) view.getRenderedImage();
            CoordinateReferenceSystem crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

            WritableRaster tca3DImage = tca3d(flowImage, pitImage);
            if (tca3DImage == null) {
                err.print("Errors in execution...\n");
                return null;
            } else {

                out.println(Messages.getString("writemap") + " TCA3D");
                jgrValueSet = new JGrassGridCoverageValueSet(tca3DImage, activeRegion, crs);

                return jgrValueSet;
            }
        }
        return null;
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

        componentDescr = "h.tca3d";
        componentId = null;

        /*
         * create the exchange items
         */
        // tca3d output
        tca3dDataOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);

        // pit input
        pitDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        // flow input
        flowDataInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

    }

    public void removeLink( String linkID ) {
        if (linkID.equals(pitLink.getID())) {
            pitLink = null;
        }
        if (linkID.equals(flowLink.getID())) {
            flowLink = null;
        }
        if (linkID.equals(tca3dLink.getID())) {
            tca3dLink = null;
        }
    }

    /**
     * Calculates the tca3d in every pixel of the map
     * 
     * @return
     */
    private WritableRaster tca3d( PlanarImage flowImage, PlanarImage pitImage ) {

        // Si suddivide ogni pixel in 8 triangoli e per igniuno di questi
        // si calcolano i lati e quindi l'area, si ottiene in questo modo l'area
        // 3D.
        int[][] tri = {{0, 0}, {1, 2}, /* tri 012 */
        {3, 2}, /* tri 023 */
        {3, 4}, /* tri 034 |4|3|2| */
        {5, 4}, /* tri 045 |5|0|1| */
        {5, 6}, /* tri 056 |6|7|8| */
        {7, 6}, /* tri 067 */
        {7, 8}, /* tri 078 */
        {1, 8} /* tri 089 */};

        int[][] dir = ModelsConstants.DIR_WITHFLOW_EXITING;

        int nnov = 0;

        // get rows and cols from the active region
        int rows = activeRegion.getRows();
        int cols = activeRegion.getCols();

        // get resolution of the active region
        double dx = activeRegion.getWEResolution();
        double dy = activeRegion.getNSResolution();

        double semiptr = 0.0, area = 0.0, areamed = 0.0;

        // areatr contains areas of 8 triangles having vertex in the 8 pixel
        // around
        double[] areatr = new double[9];

        double[] grid = new double[11];

        // grid contains the dimension of pixels according with flow directions
        grid[0] = grid[9] = grid[10] = 0;
        grid[1] = grid[5] = Math.abs(dx);
        grid[3] = grid[7] = Math.abs(dy);
        grid[2] = grid[4] = grid[6] = grid[8] = Math.sqrt(dx * dx + dy * dy);

        // contains the triangle's side
        double latitr[] = new double[3];

        // per ogni lato del triangolo contiene il dislivello e la distanza
        // planimetrica
        double[][] dzdiff = new double[3][2];

        // setting novalues...
        // flowImage= FluidUtils.setJaiNovalueBorder(flowImage);
        WritableRaster area3DImage = FluidUtils.createDoubleWritableRaster(pitImage.getWidth(), pitImage.getHeight(), null,
                pitImage.getSampleModel(), null);
        RandomIter pitRandomIter = RandomIterFactory.create(pitImage, null);
        WritableRandomIter area3DRandomIter = RandomIterFactory.createWritable(area3DImage, null);
        out.println(Messages.getString("h_tca3d.creating"));
        for( int j = 1; j < rows - 1; j++ ) {
            for( int i = 1; i < cols - 1; i++ ) {
                double pitAtIJ = pitRandomIter.getSampleDouble(i, j, 0);
                nnov = 0;
                area = 0;
                areamed = 0;
                areatr = new double[9];
                if (!isNovalue(pitAtIJ)) {
                    // calculates the area of the triangle
                    for( int k = 1; k <= 8; k++ ) {
                        double pitAtK0 = pitRandomIter.getSampleDouble(i + dir[tri[k][0]][0], j + dir[tri[k][0]][1], 0);
                        double pitAtK1 = pitRandomIter.getSampleDouble(i + dir[tri[k][1]][0], j + dir[tri[k][1]][1], 0);

                        if (!isNovalue(pitAtK0) && !isNovalue(pitAtK1)) {
                            nnov++;
                            // calcola per ogni lato del triangolo in dislivello
                            // e la distanza planimetrica tra i pixel
                            // considerati.
                            dzdiff[0][0] = Math.abs(pitAtIJ - pitAtK0);
                            dzdiff[0][1] = grid[dir[tri[k][0]][2]];
                            dzdiff[1][0] = Math.abs(pitAtIJ - pitAtK1);
                            dzdiff[1][1] = grid[dir[tri[k][1]][2]];
                            dzdiff[2][0] = Math.abs(pitAtK0 - pitAtK1);
                            dzdiff[2][1] = grid[1];
                            // calcola i lati del tringolo considerato
                            latitr[0] = Math.sqrt(Math.pow(dzdiff[0][0], 2) + Math.pow(dzdiff[0][1], 2));
                            latitr[1] = Math.sqrt(Math.pow(dzdiff[1][0], 2) + Math.pow(dzdiff[1][1], 2));
                            latitr[2] = Math.sqrt(Math.pow(dzdiff[2][0], 2) + Math.pow(dzdiff[2][1], 2));
                            // calcola il semiperimetro del triangolo
                            semiptr = 0.5 * (latitr[0] + latitr[1] + latitr[2]);
                            // calcola l'area di ciascun triangolo
                            areatr[k] = Math
                                    .sqrt(semiptr * (semiptr - latitr[0]) * (semiptr - latitr[1]) * (semiptr - latitr[2]));
                        }
                    }
                    if (nnov == 8)
                    // calcolo l'area del pixel sommando le aree degli 8
                    // triangoli.
                    {
                        for( int k = 1; k <= 8; k++ ) {
                            area = area + areatr[k] / 4;
                        }
                        area3DRandomIter.setSample(i, j, 0, area);
                    } else
                    // se il pixel e' circondato da novalue, non e' possibile
                    // comporre
                    // 8 triangoli, si calcola quindi l'area relativa ai
                    // triangoli completi
                    // si calcola la media dei loro valori e quindi si spalma il
                    // valore
                    // ottenuto sul pixel.
                    {
                        for( int k = 1; k <= 8; k++ ) {
                            area = area + areatr[k] / 4;
                        }
                        areamed = area / nnov;
                        area3DRandomIter.setSample(i, j, 0, areamed * 8);
                    }
                } else
                    area3DRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
            }
        }
        out.println(Messages.getString("h_tca3d.summ"));
        RandomIter flowIter = RandomIterFactory.create(flowImage, null);
        return FluidUtils.sum_downstream(flowIter, area3DRandomIter, cols, rows, out);

    }
}
