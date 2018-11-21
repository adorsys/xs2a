/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.web.util;

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
    public static final String DATE_TIME_PATTERN_LOCAL = "yyyy-MMdd'T'HH:mm:ss.SSS";
    public static final String DATE_TIME_PATTERN_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String UTC = "UTC";
}
