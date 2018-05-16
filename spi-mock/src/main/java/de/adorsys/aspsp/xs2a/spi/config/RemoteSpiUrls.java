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

import java.util.HashMap;
import java.util.Map;

@Component
public class RemoteSpiUrls {
    @Value("${mockspi.baseurl:http://localhost:28080}")
    private String baseUrl;
    private Map<String, String> urls;

    RemoteSpiUrls(String baseUrl) {
        this.baseUrl = baseUrl;
        this.urls = new HashMap<>();
        this.urls.put("getAllAccounts", "/account/");
        this.urls.put("getConsents", "/consent/");
        this.urls.put("getConsentById", "/consent/{id}");
        this.urls.put("deleteConsentById", "/consent/{id}");
        this.urls.put("getAccountBalances", "/account/{id}/balances");
        this.urls.put("createPayment", "/payments/");
        this.urls.put("getPaymentStatus", "/payments/{paymentId}/status/");
        this.urls.put("getAccountById", "/account/{id}");
        this.urls.put("createBulkPayments", "/payments/bulk-payments/");
        this.urls.put("getAccountByIban", "/account/{iban}/{currency}");
        this.urls.put("createPeriodicPayment", "/payments/createPeriodicPayment/");
    }

    public String getUrl(String key) {
        return this.baseUrl + this.urls.getOrDefault(key, "");
    }

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

}
