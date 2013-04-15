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
package eu.hydrologis.jgrass.models.h.shalstab;

import static eu.hydrologis.jgrass.libs.utils.JGrassConstants.isNovalue;
import static java.lang.Math.atan;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

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

import eu.hydrologis.jgrass.libs.jai.ConstantRandomIter;
import eu.hydrologis.jgrass.libs.region.JGrassRegion;
import eu.hydrologis.jgrass.libs.utils.JGrassConstants;
import eu.hydrologis.jgrass.libs.utils.monitor.PrintStreamProgressMonitor;
import eu.hydrologis.libs.openmi.ModelsBackbone;
import eu.hydrologis.libs.openmi.ModelsConstants;
import eu.hydrologis.libs.utils.FluidUtils;
import eu.hydrologis.openmi.JGrassGridCoverageValueSet;
import eu.hydrologis.openmi.StringSet;
import eu.udig.catalog.jgrass.utils.JGrassCatalogUtilities;

/**
 * <p>
 * The openmi compliant representation of the Shalstab model. It calculates the
 * the proneness to instability of each pixel based on an infinite slope model
 * with steady hydrologic conditions. The output is composed of two maps: the
 * map of the potentially unstable pixels and the map of the minimum steady
 * state rainfall to cause instability. The formula used is the following:
 * </p>
 * <p>
 * a/b >= (T * sin&#952; / q) * &#961; * [1 - (tg&#952; / tg&#934;) + C * (1 +
 * tg&#952;^2) / (tg&#934; * &#961;s * g * z)]
 * </p>
 * <p>
 * where:
 * <li>a (m^2) is the contributing area draining across</li>
 * <li>b (m) the contour length of the lower bound</li>
 * <li>T (m^2 / day) is the soil transmissivity when saturated</li>
 * <li>&#952; (degrees) is the local slope</li>
 * <li>&#961; is the ratio between soil bulk density is the friction angle</li>
 * <li>q (mm/day) the net rainfall rate</li>
 * <li>g is the gravitational acceleration</li>
 * <li>z (m) is the soil thickness</li>
 * <li>C (Pa) is the effective soil cohesion</li>
 * </p>
 * <p>
 * The output is a map of values with the following meaning:
 * <li>1 : unconditionally unstable;</li>
 * <li>2 : unconditionally stable;</li>
 * <li>3 : stable;</li>
 * <li>4 : unstable;</li>
 * <li>8888 : pixel characterized by rock (if soil thickness < 0.01)</li>
 * </p>
 * <p>
 * Minimum rainfall to instability, the formula used is the following:
 * </p>
 * <p>
 * qcrit >= (T * sin&#952; / (a/b)) * &#961; * [1 - (tg&#952; / tg&#934;) + C *
 * (1 + tg&#952;^2) / (tg&#934; * &#961;s * g * z)]
 * </p>
 * <p>
 * The output is a map of values with the following meaning:
 * <li>1 : 0 <= qcrit < 50</li>
 * <li>2 : 50 <= qcrit < 100</li>
 * <li>3 : 100 <= qcrit < 200</li>
 * <li>4 : qcrit >= 200</li>
 * <li>5 : unconditionally unstable</li>
 * <li>0 : unconditionally stable</li>
 * </p>
 * <p>
 * <DT><STRONG>Inputs:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of slope (-slopemap);</LI>
 * <LI>the map of a/b (-abmap);
 * <LI>the map of trasmissivity (-trasmissivitymap);</LI>
 * <LI>the map of cohesion (-cohesionmap);</LI>
 * <LI>the map of soil thickness(-hsmap);</LI>
 * <LI>the map of tg&#952; (-tgphimap);</LI>
 * <LI>the map of the ratio between soil bulk density is the friction angle
 * (-rhomap);</LI>
 * <LI>the map of the net rainfall rate (-qmap);</LI>
 * </LI>
 * </OL>
 * <P></DD>
 * <DT><STRONG>Returns:</STRONG></DT>
 * <DD>
 * <OL>
 * <LI>the map of the minimum rainfall to instability (-qcritmap);</LI>
 * <LI>the map of classes (-classimap);</LI>
 * </OL>
 * <P></DD>
 * Usage: h.shalstab --igrass-slopemap slope --igrass-abmap ab
 * --igrass-trasmissivitymap trasmissivity --igrass-cohesionmap cohesion
 * --igrass-hsmap hs --igrass-tgphimap tgphi --igrass-rhomap rho --igrass-qmap q
 * --ograss-qcritmap qcrit --ograss-classimap classi
 * </p>
 * <p>
 * Usage: h.shalstab --igrass-slopemap slope --igrass-abmap ab
 * --trasmissivityconst trasmissivity --cohesionconst cohesion--hsconst hs
 * --tgphiconst tgphi --rhoconst rho --qconst q --ograss-qcritmap qcrit
 * --ograss-classimap classi --ocats-catsqcrit qcrit map name --ocats-catsclass
 * class map name
 * </p>
 * <P>
 * Usage with categories: h.shalstab --igrass-slopemap slope --igrass-abmap ab
 * --igrass-trasmissivitymap trasmissivity --igrass-cohesionmap cohesion
 * --igrass-hsmap hs --igrass-tgphimap tgphi --igrass-rhomap rho --igrass-qmap q
 * --ograss-qcritmap qcrit --ograss-classimap classi --ocats-catsqcrit qcrit map
 * name --ocats-catsclass class map name
 * </p>
 * <p>
 * Usage with categories: h.shalstab --igrass-slopemap slope --igrass-abmap ab
 * --trasmissivityconst trasmissivity --cohesionconst cohesion--hsconst hs
 * --tgphiconst tgphi --rhoconst rho --qconst q --ograss-qcritmap qcrit
 * --ograss-classimap classi
 * </p>
 * <p>
 * Note: It is possible to use a map or a constant value for trasmissivity,
 * tgphi, cohesion, hs, q, rho
 * </p>
 * <p>
 * <DT><STRONG>References:</STRONG></DT>
 * <LI>R. Montgomery, W.E. Dietrich. A physically based model for the
 * topographic control on shallow landsliding, Water Resources Research, Vol. 30
 * NO.4, Pages. 1153-1171, 1994</LI>
 * <LI>R. Montgomery, K. Sullivan and H. Greenberg. Regional test of a model for
 * shallow landsliding, Hydrological Processes, 12 , Pages. 943-955, 1998</LI>
 * </p>
 * 
 * @author Erica Ghesla - erica.ghesla@ing.unitn.it, Matteo Dall’Amico, Silvano
 *         Pisoni, Andrea Antonello, Riccardo Rigon
 */
public class h_shalstab extends ModelsBackbone {
    private static final double EPS = 0.01;

    private static final double ROCK = 8888.0; /*
                                                * value to be given to pixels if
                                                * h_s < eps
                                                */

    public final static String slopeID = "slopemap";

    public final static String solidepthID = "hsmap";

    public final static String abID = "abmap";

    public final static String trasmissivityID = "trasmissivitymap";

    public final static String frictionTangentAngleID = "tgphimap";

    public final static String cohesionID = "cohesionmap";

    public final static String densityRatioID = "rhomap";

    public final static String effectivePrecipitationID = "qmap";

    public final static String qcritID = "qcritmap";

    public final static String stabilityClassificationID = "classimap";

    public final static String catsClassID = "catsclass";

    public final static String catsQcritID = "catsqcrit";

    private double slopeForRock = -9999.0;

    private ILink slopeInputLink = null;

    private ILink soilDepthInputLink = null;

    private ILink abInputLink = null;

    private ILink trasmissivityInputLink = null;

    private ILink frictionTangentAngleInputLink = null;

    private ILink cohesionInputLink = null;

    private ILink densityRatioInputLink = null;

    private ILink effectivePrecipitationInputLink = null;

    private ILink qcritOutputLink = null;

    private ILink classiOutputLink = null;

    private ILink catsClassOutputLink = null;

    private ILink catsQcritOutputLink = null;

    private IInputExchangeItem slopeInputEI = null;

    private IInputExchangeItem soilDepthInputEI = null;

    private IInputExchangeItem abInputEI = null;

    private IInputExchangeItem trasmissivityInputEI = null;

    private IInputExchangeItem frictionTangentAngleInputEI = null;

    private IInputExchangeItem cohesionInputEI = null;

    private IInputExchangeItem densityRatioInputEI = null;

    private IInputExchangeItem effectivePrecipitationInputEI = null;

    private IOutputExchangeItem qcritOutputEI = null;

    private IOutputExchangeItem classiOutputEI = null;

    private IOutputExchangeItem catsClassOutputEI = null;

    private IOutputExchangeItem catsQcritOutputEI = null;

    private JGrassRegion activeRegion = null;

    private boolean doTile;

    private String locationPath;

    private JGrassGridCoverageValueSet jgrValueSet;

    private WritableRaster[] shalstabImage;

    private static double trasmissivityConst = -1.0;

    private static double tgphiConst = -1.0;

    private static double cohesionConst = -1.0;

    private static double hsConst = -1.0;

    private static double qConst = -1.0;

    private static double rhoConst = -1.0;

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages.getString("h_shalstab.usage");

    private CoordinateReferenceSystem crs;

    public h_shalstab() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_shalstab( PrintStream output, PrintStream error ) {
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
                if (key.compareTo("trasmissivityconst") == 0) {
                    trasmissivityConst = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("tgphiconst") == 0) {
                    tgphiConst = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("cohesionconst") == 0) {
                    cohesionConst = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("hsconst") == 0) {
                    hsConst = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("qconst") == 0) {
                    qConst = Double.parseDouble(argument.getValue());
                }
                if (key.compareTo("rhoconst") == 0) {
                    rhoConst = Double.parseDouble(argument.getValue());
                }
                if (key.equals("slopelimit")) {
                    slopeForRock = new Double(argument.getValue());
                }
                if (key.compareTo(ModelsConstants.DOTILE) == 0) {
                    doTile = Boolean.getBoolean(argument.getValue());
                }
            }
        }

        if (slopeForRock == -9999.0)
            slopeForRock = 5.67;
        locationPath = grassDb + File.separator + location;
        crs = JGrassCatalogUtilities.getLocationCrs(locationPath);

        String activeRegionPath = locationPath + File.separator + mapset + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);
        componentDescr = "h.shalstab"; //$NON-NLS-1$
        componentId = null;

        slopeInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        soilDepthInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        abInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        trasmissivityInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        frictionTangentAngleInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        cohesionInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        densityRatioInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);
        effectivePrecipitationInputEI = ModelsConstants.createRasterInputExchangeItem(this, activeRegion);

        qcritOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        classiOutputEI = ModelsConstants.createRasterOutputExchangeItem(this, activeRegion);
        catsClassOutputEI = ModelsConstants.createDummyOutputExchangeItem(this);
        catsQcritOutputEI = ModelsConstants.createDummyOutputExchangeItem(this);
    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (shalstabImage == null) {
            GridCoverage2D slopeGC = ModelsConstants.getGridCoverage2DFromLink(slopeInputLink, time, err);
            GridCoverage2D abGC = ModelsConstants.getGridCoverage2DFromLink(abInputLink, time, err);

            PlanarImage slopeImage = (PlanarImage) slopeGC.getRenderedImage();
            PlanarImage abImage = (PlanarImage) abGC.getRenderedImage();

            RandomIter trasmissivityRandomIter = null;
            if (trasmissivityInputLink != null) {
                GridCoverage2D trasmissivityGC = ModelsConstants.getGridCoverage2DFromLink(trasmissivityInputLink, time, err);
                PlanarImage trasmissivityImage = (PlanarImage) trasmissivityGC.getRenderedImage();
                trasmissivityRandomIter = RandomIterFactory.create(trasmissivityImage, null);
            } else {
                trasmissivityRandomIter = new ConstantRandomIter(trasmissivityConst);
            }
            RandomIter frictionTRandomIter = null;
            if (frictionTangentAngleInputLink != null) {
                GridCoverage2D tgphiGC = ModelsConstants.getGridCoverage2DFromLink(frictionTangentAngleInputLink, time, err);
                PlanarImage frictionImage = (PlanarImage) tgphiGC.getRenderedImage();
                frictionTRandomIter = RandomIterFactory.create(frictionImage, null);
            } else {
                frictionTRandomIter = new ConstantRandomIter(tgphiConst);
            }
            RandomIter cohesionRandomIter = null;
            if (cohesionInputLink != null) {
                GridCoverage2D cohesionGC = ModelsConstants.getGridCoverage2DFromLink(cohesionInputLink, time, err);
                PlanarImage cohesionImage = (PlanarImage) cohesionGC.getRenderedImage();
                cohesionRandomIter = RandomIterFactory.create(cohesionImage, null);
            } else {
                cohesionRandomIter = new ConstantRandomIter(cohesionConst);
            }
            RandomIter soilDRandomIter = null;
            if (soilDepthInputLink != null) {
                GridCoverage2D soilGC = ModelsConstants.getGridCoverage2DFromLink(soilDepthInputLink, time, err);
                PlanarImage soilImage = (PlanarImage) soilGC.getRenderedImage();
                soilDRandomIter = RandomIterFactory.create(soilImage, null);
            } else {
                soilDRandomIter = new ConstantRandomIter(hsConst);
            }
            RandomIter effectiveRainRandomIter = null;
            if (effectivePrecipitationInputLink != null) {
                GridCoverage2D qGC = ModelsConstants.getGridCoverage2DFromLink(effectivePrecipitationInputLink, time, err);
                PlanarImage qImage = (PlanarImage) qGC.getRenderedImage();
                effectiveRainRandomIter = RandomIterFactory.create(qImage, null);
            } else {
                effectiveRainRandomIter = new ConstantRandomIter(qConst);
            }
            RandomIter densityRandomIter = null;
            if (densityRatioInputLink != null) {
                GridCoverage2D rhoGC = ModelsConstants.getGridCoverage2DFromLink(densityRatioInputLink, time, err);
                PlanarImage rhoImage = (PlanarImage) rhoGC.getRenderedImage();
                densityRandomIter = RandomIterFactory.create(rhoImage, null);
            } else {
                densityRandomIter = new ConstantRandomIter(rhoConst);

            }

            shalstabImage = qcrit(slopeImage, abImage, trasmissivityRandomIter, frictionTRandomIter, cohesionRandomIter,
                    soilDRandomIter, effectiveRainRandomIter, densityRandomIter);
        }

        if (linkID.equals(qcritOutputLink.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(shalstabImage[0], activeRegion, crs);

            return jgrValueSet;
        }
        if (linkID.equals(classiOutputLink.getID())) {
            jgrValueSet = new JGrassGridCoverageValueSet(shalstabImage[1], activeRegion, crs);

            return jgrValueSet;
        }
        if (linkID.equals(catsClassOutputLink.getID())) {
            String[] st = new String[5];
            st[0] = "1:unconditionally unstable";
            st[1] = "2:unconditionally stable";
            st[2] = "3:stable";
            st[3] = "4:unstable";
            st[4] = "8888:rock";
            StringSet jgrValueStringSet = new StringSet(st);
            return jgrValueStringSet;
        }
        if (linkID.equals(catsQcritOutputLink.getID())) {
            String[] st = new String[7];
            st[0] = "0:unconditionally stable";
            st[1] = "1:0 [mm/day]<qcrit<50 [mm/day]";
            st[2] = "2:50 [mm/day]<=qcrit <100 [mm/day]";
            st[3] = "3:100 [mm/day]<=qcrit< 200 [mm/day]";
            st[4] = "4:qcrit>=200 [mm/day]";
            st[5] = "5:unconditionally unstable";
            st[6] = "8888:rock";
            StringSet jgrValueStringSet = new StringSet(st);
            return jgrValueStringSet;
        }
        return null;
    }

    /**
     * Calculates the trasmissivity in every pixel of the map.
     */
    private WritableRaster[] qcrit( PlanarImage slope, PlanarImage ab, RandomIter trasmissivityRI, RandomIter frictionRI,
            RandomIter cohesionRI, RandomIter soildRI, RandomIter effectiveRI, RandomIter densityRI ) {
        // get rows and cols from the active region
        int cols = activeRegion.getCols();
        int rows = activeRegion.getRows();
        RandomIter slopeRI = RandomIterFactory.create(slope, null);
        RandomIter abRI = RandomIterFactory.create(ab, null);

        PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(out);
        WritableRaster qcritImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, null);
        WritableRandomIter qcritRandomIter = RandomIterFactory.createWritable(qcritImage, null);

        WritableRaster classiImage = FluidUtils.createDoubleWritableRaster(cols, rows, null, null, null);
        WritableRandomIter classiRandomIter = RandomIterFactory.createWritable(classiImage, null);
        
        pm.beginTask("Creating qcrit map...", rows);
        for( int j = 0; j < rows; j++ ) {
            pm.worked(1);
            for( int i = 0; i < cols; i++ ) {
                double slopeValue = slopeRI.getSampleDouble(i, j, 0);
                double tanPhiValue = frictionRI.getSampleDouble(i, j, 0);
                double cohValue = cohesionRI.getSampleDouble(i, j, 0);
                double rhoValue = densityRI.getSampleDouble(i, j, 0);
                double hsValue = soildRI.getSampleDouble(i, j, 0);
                if (!isNovalue(slopeValue) && !isNovalue(tanPhiValue) && !isNovalue(cohValue) && !isNovalue(rhoValue)) {
                    if (hsValue <= EPS || slopeValue > slopeForRock) {
                        qcritRandomIter.setSample(i, j, 0, ROCK);
                    } else {
                        double checkUnstable = tanPhiValue + cohValue / (9810.0 * rhoValue * hsValue) * (1 + pow(slopeValue, 2));
                        if (slopeValue >= checkUnstable) {
                            /*
                             * uncond unstable
                             */
                            qcritRandomIter.setSample(i, j, 0, 5);
                        } else {
                            double checkStable = tanPhiValue * (1 - 1 / rhoValue) + cohValue / (9810 * rhoValue * hsValue)
                                    * (1 + pow(slopeValue, 2));
                            if (slopeValue < checkStable) {
                                /*
                                 * uncond. stable
                                 */
                                qcritRandomIter.setSample(i, j, 0, 0);
                            } else {
                                double qCrit = trasmissivityRI.getSampleDouble(i, j, 0)
                                        * sin(atan(slopeValue))
                                        / abRI.getSampleDouble(i, j, 0)
                                        * rhoValue
                                        * (1 - slopeValue / tanPhiValue + cohValue / (9810 * rhoValue * hsValue * tanPhiValue)
                                                * (1 + pow(slopeValue, 2))) * 1000;
                                qcritRandomIter.setSample(i, j, 0, qCrit);
                                /*
                                 * see the Qcrit (critical effective
                                 * precipitation) that leads the slope to
                                 * instability (see article of Montgomery et Al,
                                 * Hydrological Processes, 12, 943-955, 1998)
                                 */
                                if (qcritRandomIter.getSampleDouble(i, j, 0) > 0
                                        && qcritRandomIter.getSampleDouble(i, j, 0) < 50)
                                    qcritRandomIter.setSample(i, j, 0, 1);
                                if (qcritRandomIter.getSampleDouble(i, j, 0) >= 50
                                        && qcritRandomIter.getSampleDouble(i, j, 0) < 100)
                                    qcritRandomIter.setSample(i, j, 0, 2);
                                if (qcritRandomIter.getSampleDouble(i, j, 0) >= 100
                                        && qcritRandomIter.getSampleDouble(i, j, 0) < 200)
                                    qcritRandomIter.setSample(i, j, 0, 3);
                                if (qcritRandomIter.getSampleDouble(i, j, 0) >= 200)
                                    qcritRandomIter.setSample(i, j, 0, 4);
                            }
                        }
                    }
                } else {
                    qcritRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
        }
        pm.done();

        /*
         * build the class matrix 1=inc inst 2=inc stab 3=stab 4=instab
         * rock=presence of rock
         */
        pm.beginTask("Creating stability map...", rows);
        double Tq = 0;
        for( int j = 0; j < rows; j++ ) {
            pm.worked(1);
            for( int i = 0; i < cols; i++ ) {
                Tq = trasmissivityRI.getSampleDouble(i, j, 0) / effectiveRI.getSampleDouble(i, j, 0) / 1000.0;
                double slopeValue = slopeRI.getSampleDouble(i, j, 0);
                double abValue = abRI.getSampleDouble(i, j, 0);
                double tangPhiValue = frictionRI.getSampleDouble(i, j, 0);
                double cohValue = cohesionRI.getSampleDouble(i, j, 0);
                double rhoValue = densityRI.getSampleDouble(i, j, 0);
                double hsValue = soildRI.getSampleDouble(i, j, 0);

                if (!isNovalue(slopeValue) && !isNovalue(abValue) && !isNovalue(tangPhiValue) && !isNovalue(cohValue)
                        && !isNovalue(rhoValue)) {
                    if (hsValue <= EPS || slopeValue > slopeForRock) {
                        classiRandomIter.setSample(i, j, 0, ROCK);
                    } else {
                        double checkUncondUnstable = tangPhiValue + cohValue / (9810 * rhoValue * hsValue)
                                * (1 + pow(slopeValue, 2));
                        double checkUncondStable = tangPhiValue * (1 - 1 / rhoValue) + cohValue / (9810 * rhoValue * hsValue)
                                * (1 + pow(slopeValue, 2));
                        double checkStable = Tq
                                * sin(atan(slopeValue))
                                * rhoValue
                                * (1 - slopeValue / tangPhiValue + cohValue / (9810 * rhoValue * hsValue * tangPhiValue)
                                        * (1 + pow(slopeValue, 2)));
                        if (slopeValue >= checkUncondUnstable) {
                            classiRandomIter.setSample(i, j, 0, 1);
                        } else if (slopeValue < checkUncondStable) {
                            classiRandomIter.setSample(i, j, 0, 2);
                        } else if (abValue < checkStable && classiRandomIter.getSampleDouble(i, j, 0) != 1
                                && classiRandomIter.getSampleDouble(i, j, 0) != 2) {
                            classiRandomIter.setSample(i, j, 0, 3);
                        } else {
                            classiRandomIter.setSample(i, j, 0, 4);
                        }
                    }
                } else {
                    classiRandomIter.setSample(i, j, 0, JGrassConstants.doubleNovalue);
                }
            }
        }
        pm.done();

        return new WritableRaster[]{qcritImage, classiImage};
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(slopeID)) {
            slopeInputLink = link;
        }
        if (id.equals(solidepthID)) {
            soilDepthInputLink = link;
        }
        if (id.equals(abID)) {
            abInputLink = link;
        }
        if (id.equals(trasmissivityID)) {
            trasmissivityInputLink = link;
        }
        if (id.equals(frictionTangentAngleID)) {
            frictionTangentAngleInputLink = link;
        }
        if (id.equals(cohesionID)) {
            cohesionInputLink = link;
        }
        if (id.equals(densityRatioID)) {
            densityRatioInputLink = link;
        }
        if (id.equals(effectivePrecipitationID)) {
            effectivePrecipitationInputLink = link;
        }
        if (id.equals(qcritID)) {
            qcritOutputLink = link;
        }
        if (id.equals(stabilityClassificationID)) {
            classiOutputLink = link;
        }
        if (id.equals(catsClassID)) {
            catsClassOutputLink = link;
        }
        if (id.equals(catsQcritID)) {
            catsQcritOutputLink = link;
        }
    }

    public void finish() {

    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return slopeInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return soilDepthInputEI;
        }
        if (inputExchangeItemIndex == 2) {
            return abInputEI;
        }
        if (inputExchangeItemIndex == 3) {
            return trasmissivityInputEI;
        }
        if (inputExchangeItemIndex == 4) {
            return frictionTangentAngleInputEI;
        }
        if (inputExchangeItemIndex == 5) {
            return cohesionInputEI;
        }
        if (inputExchangeItemIndex == 6) {
            return densityRatioInputEI;
        }
        if (inputExchangeItemIndex == 7) {
            return effectivePrecipitationInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 8;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return qcritOutputEI;
        }
        if (outputExchangeItemIndex == 1) {
            return classiOutputEI;
        }
        if (outputExchangeItemIndex == 2) {
            return catsClassOutputEI;
        }
        if (outputExchangeItemIndex == 3) {
            return catsQcritOutputEI;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 4;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(slopeInputLink.getID())) {
            slopeInputLink = null;
        }
        if (linkID.equals(soilDepthInputLink.getID())) {
            soilDepthInputLink = null;
        }
        if (linkID.equals(abInputLink.getID())) {
            abInputLink = null;
        }
        if (linkID.equals(trasmissivityInputLink.getID())) {
            trasmissivityInputLink = null;
        }
        if (linkID.equals(frictionTangentAngleInputLink.getID())) {
            frictionTangentAngleInputLink = null;
        }
        if (linkID.equals(cohesionInputLink.getID())) {
            cohesionInputLink = null;
        }
        if (linkID.equals(densityRatioInputLink.getID())) {
            densityRatioInputLink = null;
        }
        if (linkID.equals(effectivePrecipitationInputLink.getID())) {
            effectivePrecipitationInputLink = null;
        }
        if (linkID.equals(qcritOutputLink.getID())) {
            qcritOutputLink = null;
        }
        if (linkID.equals(classiOutputLink.getID())) {
            classiOutputLink = null;
        }
        if (linkID.equals(catsClassOutputLink.getID())) {
            catsClassOutputLink = null;
        }
        if (linkID.equals(catsQcritOutputLink.getID())) {
            catsQcritOutputLink = null;
        }
    }

}
