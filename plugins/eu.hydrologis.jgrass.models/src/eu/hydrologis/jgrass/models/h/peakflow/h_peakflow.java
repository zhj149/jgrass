package eu.hydrologis.jgrass.models.h.peakflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import nl.alterra.openmi.sdk.backbone.ScalarSet;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
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
import eu.hydrologis.libs.openmi.ModelsIllegalargumentException;
import eu.hydrologis.libs.peakflow.core.discharge.QReal;
import eu.hydrologis.libs.peakflow.core.discharge.QStatistic;
import eu.hydrologis.libs.peakflow.core.iuh.IUHCalculator;
import eu.hydrologis.libs.peakflow.core.iuh.IUHDiffusion;
import eu.hydrologis.libs.peakflow.core.iuh.IUHKinematic;
import eu.hydrologis.libs.peakflow.core.jeff.RealJeff;
import eu.hydrologis.libs.peakflow.core.jeff.StatisticJeff;
import eu.hydrologis.libs.peakflow.utils.EffectsBox;
import eu.hydrologis.libs.peakflow.utils.ParameterBox;
import eu.hydrologis.libs.utils.FluidUtils;

@SuppressWarnings("nls")
public class h_peakflow extends ModelsBackbone {

    public final static String supID = "sup"; //$NON-NLS-1$
    public final static String subID = "sub"; //$NON-NLS-1$
    public final static String dischargeID = "discharge"; //$NON-NLS-1$

    private final static String modelParameters = eu.hydrologis.libs.messages.help.Messages
            .getString("h_peakflow.usage"); //$NON-NLS-1$

    private ILink widthSupLink;
    private ILink widthSubLink;
    private ILink dischargeLink;
    private ILink cnLink;

    private IOutputExchangeItem dischargeOutputEI = null;
    private IInputExchangeItem widthSupInputEI = null;
    private IInputExchangeItem widthSubInputEI = null;

    /*
     * peakflow stuff
     */
    private boolean isReal = false;
    private boolean isStatistics = false;
    private boolean isScs = false;
    /*
     * generic vars
     */
    private IValueSet amplitudeSuperficialValueSet;
    private IValueSet amplitudeSubSuperficialValueSet;
    private double channelCelerity = -1f;
    private double diffusion = -1f;
    private String dumpFile = null;
    /*
     * statistic
     */
    private double a = -1f;
    private double n = -1f;
    /*
     * real and scs
     */
    private File rainfallFile;
    /*
     * scs
     */
    private IValueSet cnValueSet;
    private int basinStatus = 0; // dry/normal/wet
    private double phi = -1d;
    private double celerityRatio = -1d;
    private JGrassRegion activeRegion;

    /*
     * width functions
     */
    private double[][] widthFunctionSuperficial;
    private double[][] widthFunctionSubSuperficialHelper;
    private double[][] widthFunctionSubSuperficial;

    private double residentTime = -1;
    private double[] timeSubArray;
    private double[] timeSupArray;
    private double areaSup;
    private double deltaSup;
    private double pixelTotalSup;
    private double[] pixelSupArray;
    private double xRes;
    private double yRes;
    private double areaSub;
    private double deltaSub;
    private double[] pixelSubArray;
    private double pixelTotalSub;
    private Date startDate;
    private Date endDate;
    private double startDateInMinutes;
    private double endDateInMinutes;
    private double deltaTinMilliSeconds;
    private double deltaTinMinutes;
    private int timeIntervals;
    private double deltaTinHours;

    private double oututstepArg = 100;

    private ParameterBox parameterBox = new ParameterBox();
    private EffectsBox effectsBox = new EffectsBox();
    private ScalarSet dischargeScalarSet;

    public h_peakflow() {
        super();
        err = FluidUtils.newPrintStream(null, System.err);
        out = FluidUtils.newPrintStream(null, System.out);
    }

    public h_peakflow( PrintStream output, PrintStream error ) {
        super();
        err = FluidUtils.newPrintStream(error, System.err);
        out = FluidUtils.newPrintStream(output, System.out);
    }

    public void safeInitialize( IArgument[] properties ) throws Exception {
        String grassDb = null;
        String location = null;
        String mapset = null;
        String startDateArg = null;
        String endDateArg = null;
        String deltaTArg = null;

        String aArg = null;
        String nArg = null;
        String channelCelerityArg = null;
        String diffusionArg = null;

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
                if (key.compareTo("a") == 0) {
                    aArg = argument.getValue();
                }
                if (key.compareTo("n") == 0) {
                    nArg = argument.getValue();
                }
                if (key.compareTo("channelcelerity") == 0) {
                    channelCelerityArg = argument.getValue();
                }
                if (key.compareTo("diffusion") == 0) {
                    diffusionArg = argument.getValue();
                }
                if (key.compareTo("rainfall") == 0) {
                    String rainfallFilePath = argument.getValue();
                    File tmp = new File(rainfallFilePath);
                    if (tmp.exists()) {
                        rainfallFile = tmp;
                    }
                }
                if (key.compareTo("outputtimestep") == 0) {
                    try {
                        oututstepArg = Double.parseDouble(argument.getValue());
                    } catch (Exception e) {
                        // this exception isn't used. If the user writes
                        // rubbish, the default step is used
                    }
                }
                if (key.equals(ModelsConstants.STARTDATE)) {
                    startDateArg = argument.getValue();
                }
                if (key.equals(ModelsConstants.ENDDATE)) {
                    endDateArg = argument.getValue();
                }
                if (key.equals(ModelsConstants.DELTAT)) {
                    deltaTArg = argument.getValue();
                }
            }
        }

        if (startDateArg != null && !startDateArg.equals("null")) {
            try {
                startDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
                        Locale.getDefault()).parse(startDateArg);
                endDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
                        Locale.getDefault()).parse(endDateArg);
                startDateInMinutes = startDate.getTime() * MStM;
                endDateInMinutes = endDate.getTime() * MStM;
                deltaTinMilliSeconds = Double.parseDouble(deltaTArg);
                deltaTinMinutes = deltaTinMilliSeconds * MStM;
                deltaTinHours = deltaTinMinutes / 60.0;
                timeIntervals = (int) ((endDateInMinutes - startDateInMinutes) / deltaTinMinutes);
            } catch (Exception e) {
                throw new ModelsIllegalargumentException(
                        "No time interval has been defined for this model. Please check your settings or arguments.",
                        this);
            }
        }

        try {
            if (aArg != null)
                a = new Double(aArg);
            if (nArg != null)
                n = new Double(nArg);
            if (channelCelerityArg != null)
                channelCelerity = new Double(channelCelerityArg);
            if (diffusionArg != null)
                diffusion = new Double(diffusionArg);
        } catch (NumberFormatException e) {
            throw new ModelsIllegalargumentException(
                    "Problems occured in parsing the commandline. Check your syntax.", this);
        }

        /*
         * define the map path
         */
        String activeRegionPath = grassDb + File.separator + location + File.separator + mapset
                + File.separator + JGrassConstants.WIND;
        activeRegion = new JGrassRegion(activeRegionPath);
        xRes = activeRegion.getWEResolution();
        yRes = activeRegion.getNSResolution();

        /*
         * create exchangeitems
         */
        dischargeOutputEI = ModelsConstants.createDummyOutputExchangeItem(this);
        widthSubInputEI = ModelsConstants.createDummyInputExchangeItem(this);
        widthSupInputEI = ModelsConstants.createDummyInputExchangeItem(this);

    }

    public IValueSet safeGetValues( ITime time, String linkID ) throws Exception {

        if (widthFunctionSuperficial == null) {
            if (widthSupLink != null) {
                amplitudeSuperficialValueSet = widthSupLink.getSourceComponent().getValues(time,
                        widthSupLink.getID());
                if (amplitudeSuperficialValueSet != null
                        && amplitudeSuperficialValueSet instanceof ScalarSet) {
                    setSuperficialAmplitude();
                }
            }
            if (amplitudeSuperficialValueSet == null) {
                throw new ModelsIllegalargumentException(
                        "Model was launched without an input superficial widthfunction. check your syntax.",
                        this);
            }
            if (widthSubLink != null) {
                amplitudeSubSuperficialValueSet = widthSubLink.getSourceComponent().getValues(time,
                        widthSubLink.getID());
                if (amplitudeSubSuperficialValueSet != null
                        && amplitudeSubSuperficialValueSet instanceof ScalarSet) {
                    setSubSuperficialAmplitude();
                }
            }
            // TODO timedependent
            if (cnLink != null)
                cnValueSet = cnLink.getSourceComponent().getValues(time, cnLink.getID());

            // check the case
            if (a != -1 && n != -1 && amplitudeSuperficialValueSet != null && channelCelerity != -1
                    && diffusion != -1) {
                if (startDate != null) {
                    // statistic case can't be launched with time loop
                    throw new ModelsIllegalargumentException(
                            "Peakflow in statistic mode can't be launched in a time chain. Please check your launch configuration.",
                            this);
                }
                out.println("h.peakflow launched in statistic mode...");
                isStatistics = true;
                isReal = false;
                isScs = false;
            } else if (amplitudeSuperficialValueSet != null && channelCelerity != -1
                    && diffusion != -1 && rainfallFile != null && phi != -1 && celerityRatio != -1
                    && cnValueSet != null) {
                out.println("h.peakflow launched in SCS mode...");
                isStatistics = false;
                isReal = false;
                isScs = true;
            } else if (amplitudeSuperficialValueSet != null && channelCelerity != -1
                    && diffusion != -1 && rainfallFile != null) {
                out.println("h.peakflow launched with real rain...");
                isStatistics = false;
                isReal = true;
                isScs = false;
            } else {
                throw new ModelsIllegalargumentException(
                        "Problems occurred in parsing the command arguments. Please check your arguments.",
                        this);
            }

            double tcorr = 0f;
            double timestep = 1f; // TODO check
            /*
             * Calculate the tcorr as the one calculated for the superficial discharge if we have
             * only Superficial flow, an for the subsuperficial discharge otherwise
             */
            if (timeSubArray != null) {
                tcorr = timeSubArray[timeSubArray.length - 1] / channelCelerity;
            } else {
                tcorr = timeSupArray[timeSupArray.length - 1] / channelCelerity;
            }

            /*
             * prepare all the needed parameters by the core algorithms
             */

            /*
             * this needs to be integrated into the interface
             */
            if (dumpFile != null)
                parameterBox.setFiletoDump(dumpFile);

            parameterBox.setN_idf(n);
            parameterBox.setA_idf(a);
            parameterBox.setArea(areaSup);
            parameterBox.setTimestep(timestep);
            parameterBox.setDiffusionparameter(diffusion);
            parameterBox.setVc(channelCelerity);
            parameterBox.setDelta(deltaSup);
            parameterBox.setXres(xRes);
            parameterBox.setYres(yRes);
            parameterBox.setNpixel(pixelTotalSup);
            parameterBox.setSize(amplitudeSuperficialValueSet.getCount() / 2);
            parameterBox.setTime(timeSupArray);
            parameterBox.setPxl(pixelSupArray);

            effectsBox.setAmpi(widthFunctionSuperficial);

            if (timeSubArray != null) {
                parameterBox.setSubsuperficial(true);
                parameterBox.setDelta_sub(deltaSub);
                parameterBox.setNpixel_sub(pixelTotalSub);
                parameterBox.setTime_sub(timeSubArray);
                parameterBox.setArea_sub(areaSub);
                parameterBox.setPxl_sub(pixelSubArray);
                parameterBox.setResid_time(residentTime);

                effectsBox.setAmpi_sub(widthFunctionSubSuperficial);
                effectsBox.setAmpi_help_sub(widthFunctionSubSuperficialHelper);
            }

            if (isScs) {
                parameterBox.setVcvv(celerityRatio);
                parameterBox.setBasinstate(basinStatus);
                parameterBox.setPhi(phi);
                parameterBox.setScs(true);
            }

            effectsBox.setRainDataExists(rainfallFile != null ? true : false);

            if (isStatistics) {
                IUHCalculator iuhC = null;

                if (diffusion < 10) {
                    out.println("IUH Kinematic...");
                    iuhC = new IUHKinematic(effectsBox, parameterBox, out);
                } else {
                    out.println("IUH Diffusion...");
                    iuhC = new IUHDiffusion(effectsBox, parameterBox, out);
                }
                out.println("Statistic Jeff...");
                StatisticJeff jeffC = new StatisticJeff(parameterBox, iuhC.getTpMax(), out);
                out.println("Q calculation...");
                QStatistic qtotal = new QStatistic(parameterBox, iuhC, jeffC, out);
                dischargeScalarSet = new ScalarSet();
                dischargeScalarSet.add(3.0);
                double[][] calculateQ = qtotal.calculateQ();

                out.println("Maximum rainfall duration: " + qtotal.getTpMax());
                out.println("Maximum discharge value: " + qtotal.calculateQmax());

                for( int i = 0; i < calculateQ.length; i++ ) {
                    if (i % oututstepArg != 0)
                        continue;
                    dischargeScalarSet.add(calculateQ[i][0]);
                    dischargeScalarSet.add(calculateQ[i][1]);
                    dischargeScalarSet.add(calculateQ[i][3]);
                }
                return dischargeScalarSet;
            } else if (isReal) {
                IUHCalculator iuhC = null;

                if (diffusion < 10) {
                    out.println("IUH Kinematic...");
                    iuhC = new IUHKinematic(effectsBox, parameterBox, out);
                } else {
                    out.println("IUH Diffusion...");
                    iuhC = new IUHDiffusion(effectsBox, parameterBox, out);
                }
                out.println("Read rain data...");
                Map<DateTime, Double> rainData = readRaindata();

                out.println("Real Jeff...");
                RealJeff jeffC = new RealJeff(rainData);
                out.println("Q calculation...");
                QReal qtotal = new QReal(parameterBox, iuhC, jeffC, out);
                dischargeScalarSet = new ScalarSet();
                dischargeScalarSet.add(2.0);
                double[][] calculateQ = qtotal.calculateQ();

                // out.println("Maximum rainfall duration: " + qtotal.getTpMax());
                // out.println("Maximum discharge value: " + qtotal.calculateQmax());

                for( int i = 0; i < calculateQ.length; i++ ) {
                    if (i % oututstepArg != 0)
                        continue;
                    dischargeScalarSet.add(calculateQ[i][0]);
                    dischargeScalarSet.add(calculateQ[i][1]);
                    // dischargeScalarSet.add(calculateQ[i][3]);
                }
                return dischargeScalarSet;
            } else if (isScs) {
                throw new ModelsIllegalargumentException("The SCS method is not supported yet.",
                        this);
            }
        }

        /*
         * here two ways can be taken 1) standard peakflow theory 2) peakflow hybrid with SCS
         */
        // if (isStatistics || isReal) {
        // if (!peakflowStandard()) {
        // // throw some
        // }
        // } else if (isScs) {
        // if (!peakflowScs()) {
        // // throw some
        // }
        // }
        return dischargeScalarSet;
    }

    private Map<DateTime, Double> readRaindata() throws IOException {
        Map<DateTime, Double> rainData = new LinkedHashMap<DateTime, Double>();
        DateTimeFormatter dateFormatter = JGrassConstants.dateTimeFormatterYYYYMMDDHHMM;

        BufferedReader bR = new BufferedReader(new FileReader(rainfallFile));
        String line = null;
        while( (line = bR.readLine()) != null ) {
            if(line.length() < 18){
                err.println("Ignoring line: " + line);
                continue;
            }
            String[] lineSplit = line.trim().split("\\s+");
            if (lineSplit.length != 3) {
                throw new IOException(
                        "The rain data file has to consist of 3 columns: date time value");
            }

            DateTime dateTime = dateFormatter.parseDateTime(lineSplit[0] + " " + lineSplit[1]);
            Double value = Double.valueOf(lineSplit[2]);
            rainData.put(dateTime, value);
        }
        return rainData;
    }

    private void setSuperficialAmplitude() {
        ScalarSet scalarSet = (ScalarSet) amplitudeSuperficialValueSet;
        int widthFunctionLength = (scalarSet.getCount() - 1) / 2;

        widthFunctionSuperficial = new double[widthFunctionLength][3];
        pixelTotalSup = 0.0;
        double timeTotalNum = 0.0;
        timeSupArray = new double[widthFunctionLength];
        pixelSupArray = new double[widthFunctionLength];
        int index = 0;
        for( int i = 1; i < amplitudeSuperficialValueSet.getCount(); i++ ) {
            timeSupArray[index] = scalarSet.get(i);
            pixelSupArray[index] = scalarSet.get(i + 1);
            i++;

            pixelTotalSup = pixelTotalSup + pixelSupArray[index];
            timeTotalNum = timeTotalNum + timeSupArray[index];
            index++;
        }

        areaSup = pixelTotalSup * xRes * yRes;
        deltaSup = (timeSupArray[widthFunctionLength - 1] - timeSupArray[0])
                / (widthFunctionLength - 1);
        // double avgTime = timeTotalNum / amplitudeFunctionLength;
        double cum = 0.0;
        for( int i = 0; i < widthFunctionLength; i++ ) {
            widthFunctionSuperficial[i][0] = timeSupArray[i] / channelCelerity;
            widthFunctionSuperficial[i][1] = pixelSupArray[i] * xRes * yRes / deltaSup
                    * channelCelerity;
            double tmpSum = pixelSupArray[i] / pixelTotalSup;
            cum = cum + tmpSum;
            widthFunctionSuperficial[i][2] = cum;
        }
    }

    private void setSubSuperficialAmplitude() {
        ScalarSet scalarSet = (ScalarSet) amplitudeSubSuperficialValueSet;
        int widthFunctionLength = (scalarSet.getCount() - 1) / 2;

        widthFunctionSubSuperficial = new double[widthFunctionLength][3];
        widthFunctionSubSuperficialHelper = new double[widthFunctionLength][3];
        pixelTotalSub = 0;
        double timeTotalNum = 0;
        timeSubArray = new double[widthFunctionLength];
        pixelSubArray = new double[widthFunctionLength];
        int index = 0;
        for( int i = 1; i < amplitudeSubSuperficialValueSet.getCount(); i++ ) {
            timeSubArray[index] = scalarSet.get(i);
            pixelSubArray[index] = scalarSet.get(i + 1);
            i++;

            pixelTotalSub = pixelTotalSub + pixelSubArray[index];
            timeTotalNum = timeTotalNum + timeSubArray[index];
            index++;
        }
        areaSub = pixelTotalSub * xRes * yRes;
        deltaSub = (timeSubArray[widthFunctionLength - 1] - timeSubArray[0])
                / (widthFunctionLength - 1);
        double avgTime = timeTotalNum / widthFunctionLength;

        residentTime = avgTime / channelCelerity; // TODO check if this is
        // good

        double cum = 0f;
        for( int i = 0; i < widthFunctionLength; i++ ) {
            widthFunctionSubSuperficialHelper[i][0] = timeSubArray[i] / channelCelerity;
            widthFunctionSubSuperficialHelper[i][1] = pixelSubArray[i]
                    * xRes * yRes / deltaSub
                    * channelCelerity;
            cum = cum + pixelSubArray[i] / pixelTotalSub;
            widthFunctionSubSuperficialHelper[i][2] = cum;
        }
        System.out.println();
    }

    public void addLink( ILink link ) {
        String id = link.getID();

        if (id.equals(subID)) {
            widthSubLink = link;
        }
        if (id.equals(supID)) {
            widthSupLink = link;
        }
        if (id.equals(dischargeID)) {
            dischargeLink = link;
        }
    }

    public void finish() {
    }

    public IInputExchangeItem getInputExchangeItem( int inputExchangeItemIndex ) {
        if (inputExchangeItemIndex == 0) {
            return widthSupInputEI;
        }
        if (inputExchangeItemIndex == 1) {
            return widthSubInputEI;
        }
        return null;
    }

    public int getInputExchangeItemCount() {
        return 2;
    }

    public String getModelDescription() {
        return modelParameters;
    }

    public IOutputExchangeItem getOutputExchangeItem( int outputExchangeItemIndex ) {
        if (outputExchangeItemIndex == 0) {
            return dischargeOutputEI;
        }
        return null;
    }

    public int getOutputExchangeItemCount() {
        return 1;
    }

    public void removeLink( String linkID ) {
        if (linkID.equals(widthSupLink.getID())) {
            widthSupLink = null;
        }
        if (linkID.equals(widthSubLink.getID())) {
            widthSubLink = null;
        }
        if (linkID.equals(dischargeLink.getID())) {
            dischargeLink = null;
        }
    }

}
