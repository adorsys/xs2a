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
public class PisCommonPaymentRemoteUrls {
    @Value("${consent-service.baseurl:http://localhost:38080/api/v1}")
    private String commonPaymentServiceBaseUrl;

    /**
     * Returns URL-string to CMS endpoint that creates pis common payment
     *
     * @return String
     */
    public String createPisCommonPayment() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/";
    }

    /**
     * Returns URL-string to CMS endpoint that updates pis common payment status
     *
     * @return String
     */
    public String updatePisCommonPaymentStatus() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{paymentId}/status/{status}";
    }

    /**
     * Returns URL-string to CMS endpoint that gets pis common payment status by ID
     *
     * @return String
     */
    public String getPisCommonPaymentStatusById() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{paymentId}/status";
    }

    /**
     * Returns URL-string to CMS endpoint that gets pis common payment by ID
     *
     * @return String
     */
    public String getPisCommonPaymentById() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{paymentId}";
    }

    /**
     * @return String paymentId
     * Method: POST
     * PathVariables: String paymentId
     */
    public String createPisAuthorisation() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{payment-id}/authorizations";
    }

    /**
     * @return Returns URL-string to CMS endpoint that create pis authorization cancellation
     * Method: POST
     * PathVariables: String paymentId
     */
    public String createPisAuthorisationCancellation() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{payment-id}/cancellation-authorisations";
    }

    /**
     * @return String authorization ID
     * Method: GET
     * PathVariables: String paymentId
     */
    public String getCancellationAuthorisationSubResources() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{payment-id}/cancellation-authorisations";
    }

    public String updatePisAuthorisation() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/authorizations/{authorization-id}";
    }

    public String updatePisCancellationAuthorisation() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/cancellation-authorisations/{cancellation-id}";
    }

    public String getPisAuthorisationById() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/authorizations/{authorization-id}";
    }

    public String getPisCancellationAuthorisationById() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/cancellation-authorisations/{cancellation-id}";
    }

    /**
     * Returns URL-string to CMS endpoint that gets decrypted payment id from encrypted string
     *
     * @return String
     */
    public String getPaymentIdByEncryptedString() {
        return commonPaymentServiceBaseUrl + "/pis/payment/{payment-id}";
    }

    /**
     * Returns URL-string to CMS endpoint that updates common payment data by ID
     *
     * @return String
     */
    public String updatePisCommonPayment() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{consent-id}/payment";
    }

    /**
     * Returns URL-string to CMS endpoint that gets PSU data by common payment ID
     *
     * @return String
     */
    public String getPsuDataByCommonPaymentId() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{consent-id}/psu-data";
    }

    /**
     * Returns URL-string to CMS endpoint that gets PSU data by payment ID
     *
     * @return String
     */
    public String getPsuDataByPaymentId() {
        return commonPaymentServiceBaseUrl + "/pis/payment/{payment-id}/psu-data";
    }

    /**
     * @return String authorisation ID
     * Method: GET
     * PathVariables: String paymentId
     */
    public String getAuthorisationSubResources() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{payment-id}/authorisations";
    }

    /**
     * @return ScaStatus authorisation status
     * Method: GET
     * PathVariables: String paymentId
     * PathVariables: String authorisationId
     */
    public String getAuthorisationScaStatus() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{payment-id}/authorisations/{authorisation-id}/status";
    }

    /**
     * @return ScaStatus cancellation authorisation status
     * Method: GET
     * PathVariables: String paymentId
     * PathVariables: String cancellationId
     */
    public String getCancellationAuthorisationScaStatus() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{payment-id}/cancellation-authorisations/{cancellation-id}/status";
    }

    /**
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     * Method: GET
     * PathVariables: String authorisationId
     * PathVariables: String authenticationMethodId
     */
    public String isAuthenticationMethodDecoupled() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}";
    }

    /**
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     * Method: POST
     * PathVariables: String authorisationId
     */
    public String saveAuthenticationMethods() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/authorisations/{authorisation-id}/authentication-methods";
    }

    /**
     * @return <code>true</code> if authorisation was found and sca approach updated, <code>false</code> otherwise
     * Method: PUT
     * PathVariables: String authorisationId
     * PathVariables: String sca-approach
     */
    public String updateScaApproach() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/authorisations/{authorisation-id}/sca-approach/{sca-approach}";
    }
}
