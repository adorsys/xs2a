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
public class AspspRemoteUrls {
    @Value("${mockspi.baseurl:http://localhost:28080}")
    private String spiMockBaseUrl;

    //Accounts urls

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs AccountDetails by accountId
     *
     * @return URL with path parameter
     */
    public String getAccountDetailsById() {
        return spiMockBaseUrl + "/account/{account-id}";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs Balances of an account by accountId
     *
     * @return URL with path parameter
     */
    public String getBalancesByAccountId() {
        return spiMockBaseUrl + "/account/{account-id}/balances";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs AccountDetails by PsuId
     *
     * @return URL with path parameter
     */
    public String getAccountDetailsByPsuId() {
        return spiMockBaseUrl + "/account/psu/{psu-id}";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs AccountDetails by IBAN
     *
     * @return URL with path parameter
     */
    public String getAccountDetailsByIban() {
        return spiMockBaseUrl + "/account/iban/{iban}";
    }

    //Payments urls
    public String createPayment() {
        return spiMockBaseUrl + "/payments/";
    }

    public String getPaymentStatus() {
        return spiMockBaseUrl + "/payments/{payment-id}/status";
    }

    public String createBulkPayment() {
        return spiMockBaseUrl + "/payments/bulk-payments";
    }

    public String createPeriodicPayment() {
        return spiMockBaseUrl + "/payments/create-periodic-payment";
    }

    //Transactions urls

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs Transaction by transactionId and accountId
     *
     * @return URL with path parameters
     */
    public String readTransactionById() {
        return spiMockBaseUrl + "/transaction/{transaction-id}/{account-id}";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs Transactions list by accountId and period set by dates from/to
     *
     * @return URL with path parameter
     */
    public String readTransactionsByPeriod() {
        return spiMockBaseUrl + "/transaction/{account-id}";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that Creates a new Transaction with body SpiTransaction
     *
     * @return URL
     */
    public String createTransaction() {
        return spiMockBaseUrl + "/transaction";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs list of allowed payment products for PSU by one of its account ibans
     *
     * @return URL
     */
    public String getAllowedPaymentProducts() {
        return spiMockBaseUrl + "/psu/allowed-payment-products/{iban}";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs payment by its ASPSP id
     *
     * @return URL
     */
    public String getPaymentById() {
        return spiMockBaseUrl + "/payments/{payment-type}/{payment-product}/{paymentId}";
    }
}
