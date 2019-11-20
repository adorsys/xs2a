/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator.constants;

import java.util.HashMap;
import java.util.Map;

public class Xs2aHeaderConstant {
    // HTTP headers that are used in validation:
    public static final String X_REQUEST_ID = "x-request-id";
    public static final String PSU_IP_ADDRESS = "psu-ip-address";
    public static final String CONSENT_ID = "consent-id";

    public static final String PSU_ID = "psu-id";
    public static final String PSU_ID_TYPE = "psu-id-type";
    public static final String PSU_CORPORATE_ID = "psu-corporate-id";
    public static final String PSU_CORPORATE_ID_TYPE = "psu-corporate-id-type";

    public static final String TPP_REDIRECT_PREFERRED = "tpp-redirect-preferred";
    public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";

    public static final String TPP_NOK_REDIRECT_URI = "tpp-nok-redirect-uri";
    public static final String TPP_EXPLICIT_AUTHORISATION_PREFERRED = "tpp-explicit-authorisation-preferred";

    public static final String TPP_REJECTION_NO_FUNDS_PREFERRED = "tpp-rejection-no funds-preferred";

    // This map holds arrays of HTTP headers to be validated for max length. Key is the header name, value is the
    // maximum allowed length of this header.
    public static final Map<String, Integer> HEADERS_MAX_LENGTHS = new HashMap<>();

    static {
        HEADERS_MAX_LENGTHS.put(PSU_ID, 50);
        HEADERS_MAX_LENGTHS.put(PSU_ID_TYPE, 50);
        HEADERS_MAX_LENGTHS.put(PSU_CORPORATE_ID, 50);
        HEADERS_MAX_LENGTHS.put(PSU_CORPORATE_ID_TYPE, 50);

        // These 2 values are limited by CMS columns length:
        HEADERS_MAX_LENGTHS.put(TPP_REDIRECT_URI, 255);
        HEADERS_MAX_LENGTHS.put(TPP_NOK_REDIRECT_URI, 255);

        HEADERS_MAX_LENGTHS.put(PSU_IP_ADDRESS, 140);
    }

    private Xs2aHeaderConstant() {
    }
}
