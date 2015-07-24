package com.example.gohar.hydra.data;

/**
 * Created by Gohar on 06/07/15.
 */
public class Constants {
    // for the API authentication
    public static final String BASE64_ENCODED_CREDENTIAL = "NlhXbXZpbVdEaVFJbXZMQVBwVEdBbUFEQVZHRUdUQUI6SGtUSUFuQUd6R0RjenpyNg==";
    public static final String OAUTH_API_BASE_URL = "https://api.awhere.com/oauth/token";
    public static final String OAUTH_GRANT_TYPE = "grant_type";
    public static final String OAUTH_CLIENT_CREDENTIALS = "client_credentials";
    public static final String OAUTH_HEADER_CONTENT_TYPE = "application/x-www-form-urlencoded";

    // for the response json object
    public static final String DAILY_ATTRIBUTES = "dailyAttributes";
    public static final String DATE = "date";
    public static final String CONDITIONS_COND_CODE = "condCode";
    public static final String CONDITIONS_COND_TEXT = "condText";

    // for the request parameter
    public static final String PLANT_DATE = "plantDate";
    public static final String ATTRIBUTE = "attribute";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String START_DATE = "startDate";
    public static final String MIN_TEMPERATURE = "minTemperature";
    public static final String MAX_TEMPERATURE = "maxTemperature";
    public static final String PRECIP = "precip";
    public static final String ACC_PRECIP = "accPrecip";
    public static final String ACC_PRECIP_PRIOR_YEAR = "accPrecipPriorYear";
    public static final String ACC_PRECIP_3_YEAR_AVERAGE = "accPrecip3YearAverage";
    public static final String ACC_PRECIP_LONG_TERM_AVERAGE = "accPrecipLongTermAverage";
    public static final String SOLAR = "solar";
    public static final String MIN_HUMIDITY = "minHumidity";
    public static final String MAX_HUMIDITY = "maxHumidity";
    public static final String MORN_WIND = "mornWind";
    public static final String MAX_WIND = "maxWind";
    public static final String GDD = "gdd";
    public static final String ACC_GDD = "accGdd";
    public static final String ACC_GDD_PRIOR_YEAR = "accGddPriorYear";
    public static final String ACC_GDD_3_YEAR_AVERAGE = "accGdd3YearAverage";
    public static final String ACC_GDD_LONG_TERM_AVERAGE = "accGddLongTermAverage";
    public static final String PET = "pet";
    public static final String ACC_PET = "accPet";
    public static final String PPET = "ppet";
    public static final String CONDITIONS = "conditions";
    public static final String INTERVALS = "intervals";
    public static final String INTERVALS_VALUE = "1";
    public static final String CONDITIONS_TYPE = "conditionsType";
    public static final String CONDITIONS_TYPE_VALUE = "standard";
    public static final String UTC_OFFSET = "utcOffset";
    public static final String UTC_OFFSET_VALUE = "+5:00:00";
}
