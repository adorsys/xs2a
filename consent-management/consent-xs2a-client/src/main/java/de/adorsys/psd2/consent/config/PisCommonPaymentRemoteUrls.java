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
    @Value("${xs2a.cms.consent-service.baseurl:http://localhost:38080/api/v1}")
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
     * @return <code>true</code> if payment was found and multilevel sca required updated, <code>false</code> otherwise
     * Method: PUT
     * PathVariables: String paymentId
     * PathVariables: boolean isRequired
     */
    public String updateMultilevelScaRequired() {
        return commonPaymentServiceBaseUrl + "/pis/common-payments/{payment-id}/multilevel-sca?multilevel-sca={multilevel-sca}";
    }
}
