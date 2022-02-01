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
public class PisPaymentRemoteUrls {
    @Value("${xs2a.cms.consent-service.baseurl:http://localhost:38080/api/v1}")
    private String paymentServiceBaseUrl;

    /**
     * Returns URL-string to CMS endpoint that updates payment status
     *
     * @return String
     */
    public String updatePaymentStatus() {
        return paymentServiceBaseUrl + "/pis/payment/{payment-id}/status/{status}";
    }

    public String updateInternalPaymentStatus() {
        return paymentServiceBaseUrl + "/pis/payment/{payment-id}/internal-status/{status}";
    }

    public String updatePaymentCancellationRedirectURIs() {
        return paymentServiceBaseUrl + "/pis/payment/{payment-id}/cancellation/redirects";
    }

    public String updatePaymentCancellationInternalRequestId() {
        return paymentServiceBaseUrl + "/pis/payment/{payment-id}/cancellation/internal-request-id/{internal-request-id}";
    }
}
