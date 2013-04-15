package eu.hydrologis.libs.adige;


/**
 * Interface defining the tags needed to read a REST result query for data as supplied by the USGS
 * at: http://waterdata.usgs.gov/nwis/?DailyValues_Service_Instructions
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
@SuppressWarnings({"nls", "serial"})
public interface UsgsTagConstants {

    /**
     * the main tag
     */
    public final String timeSeriesResponse = "timeSeriesResponse";

    /*
     * queryinfo
     */
    /**
     * The tag holding infos like {@link UsgsTagConstants#beginDateTime} and
     * {@link UsgsTagConstants#endDateTime}
     */
    public final String queryInfo = "queryInfo";
    public final String criteria = "criteria";
    public final String timeParam = "timeParam";
    /**
     * <p>
     * Begin time of the data.
     * </p>
     * inside {@link UsgsTagConstants#queryInfo} -> {@link UsgsTagConstants#criteria} ->
     * {@link UsgsTagConstants#timeParam}
     */
    public final String beginDateTime = "beginDateTime";
    /**
     * <p>
     * End time of the data.
     * </p>
     * inside {@link UsgsTagConstants#queryInfo} -> {@link UsgsTagConstants#criteria} ->
     * {@link UsgsTagConstants#timeParam}
     */
    public final String endDateTime = "endDateTime";

    /*
     * timeseries
     */
    public final String timeSeries = "timeSeries";
    /*
     * sourceinfo
     */
    public final String sourceInfo = "sourceInfo";
    public final String siteName = "siteName";
    public final String siteCode = "siteCode";
    public final String geoLocation = "geoLocation";
    public final String geogLocation = "geogLocation";
    public final String latitude = "latitude";
    public final String longitude = "longitude";
    public final String srs = "srs";
    /*
     * variable
     */
    public final String variable = "variable";
    public final String variableCode = "variableCode";
    public final String variableName = "variableName";
    public final String variableDescription = "variableDescription";
    public final String units = "units";
    public final String unitsAbbreviation = "unitsAbbreviation";
    public final String options = "options";
    public final String option = "option";
    public final String NoDataValue = "NoDataValue";
    public final String timeSupport = "timeSupport";
    public final String unit = "unit";
    public final String UnitName = "UnitName";
    public final String UnitAbbreviation = "UnitAbbreviation";
    public final String timeInterval = "timeInterval";
    public final String isRegular = "isRegular";
    // newage
    public final String Pfafstetter = "Pfafstetter";
    public final String RelatedSiteCode = "RelatedSiteCode";
    public final String Active = "Active";
    
    /*
     * values
     */
    public final String name = "name";
    public final String type = "type";
    public final String values = "values";
    public final String count = "count";
    public final String value = "value";
    public final String dateTime = "dateTime";
    public final String dateTimePattern = "yyyy-MM-ddTHH:mm:ss";
    public final UsgsDateFormatter usgsDateFormatter = new UsgsDateFormatter();

}
