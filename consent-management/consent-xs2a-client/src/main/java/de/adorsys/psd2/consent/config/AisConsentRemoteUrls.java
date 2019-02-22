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

package de.adorsys.psd2.consent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AisConsentRemoteUrls {
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
     * @return SpiAccountConsent initial consent
     * Method: GET
     * PathVariable: String consentId
     */
    public String getInitialAisConsentById() {
        return consentServiceBaseUrl + "/ais/consent/initial/{consent-id}";
    }

    /**
     * @return ConsentStatus status
     * Method: GET
     * PathVariable: String consentId
     */
    public String getAisConsentStatusById() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/status";
    }

    /**
     * @return VOID
     * Method: PUT
     * PathVariables: String consentId, ConsentStatus consentStatus
     */
    public String updateAisConsentStatus() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/status/{status}";
    }

    /**
     * @return VOID
     * Method: DELETE
     * PathVariable: String consentId
     */
    public String findAndTerminateOldConsentsByNewConsentId() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/old-consents";
    }

    /**
     * @return VOID
     * Method: POST
     * PathVariables: AisConsentActionRequest consentActionRequest
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
        return consentServiceBaseUrl + "/ais/consent/authorizations/{authorization-id}";
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
     * Returns URL-string to CMS endpoint that updates AccountAccess
     *
     * @return String
     */
    public String updateAisAccountAccess() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/access";
    }

    /**
     * Returns URL-string to CMS endpoint that gets PSU data by consent ID
     *
     * @return String
     */
    public String getPsuDataByConsentId() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/psu-data";
    }

    /**
     * @return List of consent authorisation IDs
     * Method: GET
     * PathVariables: String consentId
     */
    public String getAuthorisationSubResources() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/authorisations";
    }

    /**
     * @return ScaStatus authorisation status
     * Method: GET
     * PathVariables: String consentId
     * PathVariables: String authorisationId
     */
    public String getAuthorisationScaStatus() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/authorisations/{authorisation-id}/status";
    }

    /**
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     * Method: GET
     * PathVariables: String authorisationId
     * PathVariables: String authenticationMethodId
     */
    public String isAuthenticationMethodDecoupled() {
        return consentServiceBaseUrl + "/ais/consent/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}";
    }

    /**
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     * Method: POST
     * PathVariables: String authorisationId
     */
    public String saveAuthenticationMethods() {
        return consentServiceBaseUrl + "/ais/consent/authorisations/{authorisation-id}/authentication-methods";
    }

    /**
     * @return <code>true</code> if authorisation was found and SCA approach updated, <code>false</code> otherwise
     * Method: PUT
     * PathVariables: String authorisationId
     * PathVariables: String sca-approach
     */
    public String updateScaApproach() {
        return consentServiceBaseUrl + "/ais/consent/authorisations/{authorisation-id}/sca-approach/{sca-approach}";
    }

    /**
     * @return <code>true</code> if consent was found and multilevel SCA required updated, <code>false</code> otherwise
     * Method: PUT
     * PathVariables: String consentId
     * RequestParam:  boolean multilevel-sca
     */
    public String updateMultilevelScaRequired() {
        return consentServiceBaseUrl + "/ais/consent/{consent-id}/multilevel-sca?multilevel-sca={multilevel-sca}";
    }
}
