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

import java.util.HashMap;
import java.util.Map;

public class RemoteSpiUrls {
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
    }

    public String getUrl(String key) {
        return this.baseUrl + this.urls.getOrDefault(key, "");
    }
}
