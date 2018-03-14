package de.adorsys.aspsp.xs2a.spi.domain;

/**
 * Object for configuring date format
 */
public class ApiDateConstants {
    /**
     * Pattern for date format. A particular point in the progression of time in a calendar year expressed in the YYYY-MM-DD format
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * Pattern for date and time format. A particular point in the progression of time defined by a mandatory date and a mandatory time component, expressed in either UTC time format (YYYY-MM-DDThh:mm:ss.sssZ)
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
}
