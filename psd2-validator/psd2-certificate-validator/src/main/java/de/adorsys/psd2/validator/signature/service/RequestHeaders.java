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

package de.adorsys.psd2.validator.signature.service;

import java.util.HashMap;
import java.util.Map;

public class RequestHeaders {
    public static final String X_REQUEST_ID = "X-Request-ID";
    public static final String CONSENT_ID = "Consent-ID";
    public static final String DIGEST = "Digest";
    public static final String PSU_ID = "PSU-ID";
    public static final String PSU_CORPORATE_ID = "PSU-Corporate-ID";
    public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
    public static final String DATE = "Date";
    public static final String SIGNATURE = "Signature";
    public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
    public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    public static final String PSU_ID_TYPE = "PSU-ID-Type";
    public static final String PSU_CORPORATE_ID_TYPE = "PSU-Corporate-ID-Type";
    public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
    public static final String TPP_NOK_REDIRECT_URI = "TPP-Nok-Redirect-URI";
    public static final String TPP_EXPLICIT_AUTHORISATION_PREFERRED = "TPP-Explicit-Authorisation-Preferred";
    public static final String PSU_IP_PORT = "PSU-IP-Port";
    public static final String PSU_ACCEPT = "PSU-Accept";
    public static final String PSU_ACCEPT_CHARSET = "PSU-Accept-Charset";
    public static final String PSU_ACCEPT_ENCODING = "PSU-Accept-Encoding";
    public static final String PSU_ACCEPT_LANGUAGE = "PSU-Accept-Language";
    public static final String PSU_USER_AGENT = "PSU-User-Agent";
    public static final String PSU_HTTP_METHOD = "PSU-Http-Method";
    public static final String PSU_DEVICE_ID = "PSU-Device-ID";
    public static final String PSU_GEO_LOCATION = "PSU-Geo-Location";
    // technical
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CORRELATION_ID = "Correlation-ID";

    private static Map<String, String> headerNamesLowerCased = new HashMap<>();

    static {
        headerNamesLowerCased.put(X_REQUEST_ID.toLowerCase(), X_REQUEST_ID);
        headerNamesLowerCased.put(PSU_IP_ADDRESS.toLowerCase(), PSU_IP_ADDRESS);
        headerNamesLowerCased.put(DIGEST.toLowerCase(), DIGEST);
        headerNamesLowerCased.put(SIGNATURE.toLowerCase(), SIGNATURE);
        headerNamesLowerCased.put(TPP_SIGNATURE_CERTIFICATE.toLowerCase(), TPP_SIGNATURE_CERTIFICATE);
        headerNamesLowerCased.put(PSU_ID.toLowerCase(), PSU_ID);
        headerNamesLowerCased.put(PSU_ID_TYPE.toLowerCase(), PSU_ID_TYPE);
        headerNamesLowerCased.put(PSU_CORPORATE_ID.toLowerCase(), PSU_CORPORATE_ID);
        headerNamesLowerCased.put(PSU_CORPORATE_ID_TYPE.toLowerCase(), PSU_CORPORATE_ID_TYPE);
        headerNamesLowerCased.put(CONSENT_ID.toLowerCase(), CONSENT_ID);
        headerNamesLowerCased.put(TPP_REDIRECT_PREFERRED.toLowerCase(), TPP_REDIRECT_PREFERRED);
        headerNamesLowerCased.put(TPP_REDIRECT_URI.toLowerCase(), TPP_REDIRECT_URI);
        headerNamesLowerCased.put(TPP_NOK_REDIRECT_URI.toLowerCase(), TPP_NOK_REDIRECT_URI);
        headerNamesLowerCased.put(TPP_EXPLICIT_AUTHORISATION_PREFERRED.toLowerCase(), TPP_EXPLICIT_AUTHORISATION_PREFERRED);
        headerNamesLowerCased.put(PSU_IP_PORT.toLowerCase(), PSU_IP_PORT);
        headerNamesLowerCased.put(PSU_ACCEPT.toLowerCase(), PSU_ACCEPT);
        headerNamesLowerCased.put(PSU_ACCEPT_CHARSET.toLowerCase(), PSU_ACCEPT_CHARSET);
        headerNamesLowerCased.put(PSU_ACCEPT_ENCODING.toLowerCase(), PSU_ACCEPT_ENCODING);
        headerNamesLowerCased.put(PSU_ACCEPT_LANGUAGE.toLowerCase(), PSU_ACCEPT_LANGUAGE);
        headerNamesLowerCased.put(PSU_USER_AGENT.toLowerCase(), PSU_USER_AGENT);
        headerNamesLowerCased.put(PSU_HTTP_METHOD.toLowerCase(), PSU_HTTP_METHOD);
        headerNamesLowerCased.put(PSU_DEVICE_ID.toLowerCase(), PSU_DEVICE_ID);
        headerNamesLowerCased.put(PSU_GEO_LOCATION.toLowerCase(), PSU_GEO_LOCATION);
        headerNamesLowerCased.put(ACCEPT.toLowerCase(), ACCEPT);
        headerNamesLowerCased.put(AUTHORIZATION.toLowerCase(), AUTHORIZATION);
        headerNamesLowerCased.put(CORRELATION_ID.toLowerCase(), CORRELATION_ID);
        headerNamesLowerCased.put(DATE.toLowerCase(), DATE);
    }

    private Map<String, String> headers;

    private RequestHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public static RequestHeaders fromMap(Map<String, String> headersMap) {
        Map<String, String> headers = new HashMap<>();
        headersMap.forEach((name, value) -> {
            String headerNameInLowerCase = name.toLowerCase();
            if (headerNamesLowerCased.keySet().contains(headerNameInLowerCase)) {
                headers.put(headerNamesLowerCased.get(headerNameInLowerCase), value);
            }
        });
        return new RequestHeaders(headers);
    }

    public Map<String, String> toMap() {
        return new HashMap<>(headers);
    }
}
