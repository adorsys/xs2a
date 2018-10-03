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

package de.adorsys.aspsp.xs2a.config.rest.consent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AisConsentRemoteUrls implements AspspConsentDataRemoteUrls {
    @Value("${consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    /**
     * @return String consentId
     * Method: POST
     * Body: AisConsentRequest request
     */
    public String createAisConsent() {
        return consentServiceBaseUrl + "/ais/consent/";
    }

    /**
     * @return SpiAccountConsent consent
     * Method: GET
     * PathVariable: String consentId
     */
    public String getAisConsentById() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}";
    }

    /**
     * @return SpiConsentStatus status
     * Method: GET
     * PathVariable: String consentId
     */
    public String getAisConsentStatusById() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/status";
    }

    /**
     * @return VOID
     * Method: PUT
     * PathVariables: String consentId, SpiConsentStatus consentStatus
     */
    public String updateAisConsentStatus() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/status/{status}";
    }

    /**
     * @return VOID
     * Method: POST
     * PathVariables: ConsentActionRequest consentActionRequest
     */
    public String consentActionLog() {
        return consentServiceBaseUrl + "/ais/consent/action";
    }

    /**
     * @return String consentId
     * Method: POST
     * PathVariables: String consentId
     */
    public String createAisConsentAuthorization() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/authorizations";
    }

    /**
     * @return String consentId
     * Method: PUT
     * PathVariables: String consentId, String authorizationId
     */
    public String updateAisConsentAuthorization() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/authorizations/{authorization-id}";
    }

    /**
     * @return SpiAccountConsentAuthorization consent
     * Method: GET
     * PathVariables: String consentId, String authorizationId
     */
    public String getAisConsentAuthorizationById() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/authorizations/{authorization-id}";
    }

    /**
     * Returns URL-string to CMS endpoint that gets aspsp consent data by consent ID
     *
     * @return String
     */
    @Override
    public String getAspspConsentData() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/aspsp-consent-data";
    }

    /**
     * Returns URL-string to CMS endpoint that updates aspsp consent data by consent ID
     *
     * @return String
     */
    @Override
    public String updateAspspConsentData() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/aspsp-consent-data";
    }
}
