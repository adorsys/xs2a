/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
