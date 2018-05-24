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

package de.adorsys.aspsp.xs2a.spi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RemoteSpiUrls {
    @Value("${mockspi.baseurl:http://localhost:28080}")
    private String spiMockBaseUrl;

    @Value("${consent-service.baseurl:http://localhost:38080/api/v1/}")
    private String consentServiceBaseUrl;

    //Consents urls
    public String deleteConsentById() {
        return consentServiceBaseUrl + "/ais/consent/{consentId}/status/revoke";
    }

    public String createConsent() {
        return consentServiceBaseUrl + "/api/v1/ais/consent/create";
    }

    public String getAccountConsentStatusById() {
        return consentServiceBaseUrl + "/ais/consent/{consentId}/status";
    }

    public String getConsentById() {
        return consentServiceBaseUrl + "/ais/consent/spi/{consentId}";
    }

    public String getConsentByAccess() {
        return spiMockBaseUrl + "/consent/byAccess/{access}";
    }

    //Accounts urls
    public String getAccountDetailsById() {
        return spiMockBaseUrl + "/account/{accountId}";
    }

    public String getBalancesByAccountId() {
        return spiMockBaseUrl + "/account/{accountId}/balances";
    }

    public String getAccountDetailsByPsuId() {
        return spiMockBaseUrl + "/account/psu/{psuId}";
    }

    public String getAccountDetailsByIban() {
        return spiMockBaseUrl + "/account/iban/{iban}";
    }

    //Payments urls
    public String createPayment() {
        return spiMockBaseUrl + "/payments/";
    }

    public String getPaymentStatus() {
        return spiMockBaseUrl + "/payments/{paymentId}/status/";
    }

    public String createBulkPayment() {
        return spiMockBaseUrl + "/payments/bulk-payments/";
    }

    public String createPeriodicPayment() {
        return spiMockBaseUrl + "/payments/createPeriodicPayment/";
    }

}
