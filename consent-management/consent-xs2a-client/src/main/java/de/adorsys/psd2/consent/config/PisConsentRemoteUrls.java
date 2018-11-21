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
public class PisConsentRemoteUrls {
    @Value("${consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    /**
     * Returns URL-string to CMS endpoint that creates pis consent
     *
     * @return String
     */
    public String createPisConsent() {
        return consentServiceBaseUrl + "/pis/consent/";
    }

    /**
     * Returns URL-string to CMS endpoint that updates pis consent status
     *
     * @return String
     */
    public String updatePisConsentStatus() {
        return consentServiceBaseUrl + "/pis/consent/{consentId}/status/{status}";
    }

    /**
     * Returns URL-string to CMS endpoint that gets pis consent status by ID
     *
     * @return String
     */
    public String getPisConsentStatusById() {
        return consentServiceBaseUrl + "/pis/consent/{consentId}/status";
    }

    /**
     * Returns URL-string to CMS endpoint that gets pis consent by ID
     *
     * @return String
     */
    public String getPisConsentById() {
        return consentServiceBaseUrl + "/pis/consent/{consentId}";
    }

    /**
     * @return String consentId
     * Method: POST
     * PathVariables: String paymentId
     */
    public String createPisConsentAuthorisation() {
        return consentServiceBaseUrl + "/pis/consent/{payment-id}/authorizations";
    }

    /**
     * @return Returns URL-string to CMS endpoint that create pis consent authorization cancellation
     * Method: POST
     * PathVariables: String paymentId
     */
    public String createPisConsentAuthorisationCancellation() {
        return consentServiceBaseUrl + "/pis/consent/{payment-id}/cancellation-authorisations";
    }

    /**
     * @return String authorization ID
     * Method: GET
     * PathVariables: String paymentId
     */
    public String getCancellationAuthorisationSubResources() {
        return consentServiceBaseUrl + "/pis/consent/{payment-id}/cancellation-authorisations";
    }

    public String updatePisConsentAuthorisation() {
        return consentServiceBaseUrl + "/pis/consent/authorizations/{authorization-id}";
    }

    public String updatePisConsentCancellationAuthorisation() {
        return consentServiceBaseUrl + "/pis/consent/cancellation-authorisations/{cancellation-id}";
    }

    public String getPisConsentAuthorisationById() {
        return consentServiceBaseUrl + "/pis/consent/authorizations/{authorization-id}";
    }

    public String getPisConsentCancellationAuthorisationById() {
        return consentServiceBaseUrl + "/pis/consent/cancellation-authorisations/{cancellation-id}";
    }

    /**
     * Returns URL-string to CMS endpoint that gets decrypted payment id from encrypted string
     *
     * @return String
     */
    public String getPaymentIdByEncryptedString() {
        return consentServiceBaseUrl + "/pis/payment/{payment-id}";
    }

    /**
     * Returns URL-string to CMS endpoint that updates payment data of consent by consent ID
     *
     * @return String
     */
    public String updatePisConsentPayment() {
        return consentServiceBaseUrl + "/pis/consent/{consent-id}/payment";
    }

    /**
     * Returns URL-string to CMS endpoint that gets PSU data by consent ID
     *
     * @return String
     */
    public String getPsuDataByConsentId() {
        return consentServiceBaseUrl + "/pis/consent/{consent-id}/psu-data";
    }

    /**
     * Returns URL-string to CMS endpoint that gets PSU data by payment ID
     *
     * @return String
     */
    public String getPsuDataByPaymentId() {
        return consentServiceBaseUrl + "/pis/payment/{payment-id}/psu-data";
    }
}
