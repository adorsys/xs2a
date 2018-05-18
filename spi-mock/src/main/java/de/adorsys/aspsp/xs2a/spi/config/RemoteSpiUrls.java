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
    private String baseUrl;

    //Consents urls
    public String getConsentById() {
        return baseUrl + "/consent/{id}";
    }

    public String deleteConsentById() {
        return baseUrl + "/consent/{id}";
    }

    public String createConsent() {
        return baseUrl + "/consent/";
    }

    public String getConsentByAccess() {
        return baseUrl + "/consent/byAccess/{access}";
    }

    //Accounts urls
    public String getAccountDetailsById() {
        return baseUrl + "/account/{accountId}";
    }

    public String getBalancesByAccountId() {
        return baseUrl + "/account/{accountId}/balances";
    }

    public String getAccountDetailsByPsuId() {
        return baseUrl + "/account/psu/{psuId}";
    }

    public String getAccountDetailsByIban() {
        return baseUrl + "/account/iban/{iban}";
    }

    //Payments urls
    public String createPayment() {
        return baseUrl + "/payments/";
    }

    public String getPaymentStatus() {
        return baseUrl + "/payments/{paymentId}/status/";
    }

    public String createBulkPayment() {
        return baseUrl + "/payments/bulk-payments/";
    }

    public String createPeriodicPayment() {
        return baseUrl + "/payments/createPeriodicPayment/";
    }

}
