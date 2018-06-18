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

    @Value("${consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    // PIS Consents

    /**
     * Returns URL-string to CMS endpoint that creates pis consent
     *
     * @return String
     */
    public String createPisConsent() {
        return consentServiceBaseUrl + "/pis/consent/";
    }

    /**
     * Returns URL-string to CMS endpoint that creates pis consent for bulk payment
     *
     * @return String
     */
    public String createPisBulkPaymentConsent() {
        return consentServiceBaseUrl + "/pis/consent/bulk";
    }

    /**
     * Returns URL-string to CMS endpoint that creates pis consent for periodic payment
     *
     * @return String
     */
    public String createPisPeriodicPaymentConsent() {
        return consentServiceBaseUrl + "/pis/consent/periodic";
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
     * Returns URL-string to CMS endpoint that checks access by consent Id
     *
     * @return String
     */
    public String checkAccessByConsentId() {
        return consentServiceBaseUrl + "/ais/consent/available/access";
    }

    //Accounts urls

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs AccountDetails by accountId
     *
     * @return String
     */
    public String getAccountDetailsById() {
        return spiMockBaseUrl + "/account/{account-id}";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs Balances of an account by accountId
     *
     * @return String
     */
    public String getBalancesByAccountId() {
        return spiMockBaseUrl + "/account/{account-id}/balances";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs AccountDetails by PsuId
     *
     * @return String
     */
    public String getAccountDetailsByPsuId() {
        return spiMockBaseUrl + "/account/psu/{psu-id}";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs AccountDetails by IBAN
     *
     * @return String
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
        return spiMockBaseUrl + "/payments/createPeriodicPayment";
    }

    //Transactions urls

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs Transaction by transactionId and accountId
     *
     * @return String
     */
    public String readTransactionById() {
        return spiMockBaseUrl + "/transaction/{transaction-id}/{account-id}";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that GETs Transactions list by accountId and period set by dates from/to
     *
     * @return String
     */
    public String readTransactionsByPeriod() {
        return spiMockBaseUrl + "/transaction/{account-id}";
    }

    /**
     * Returns URL-string to ASPSP-Mock endpoint that Creates a new Transaction with body SpiTransaction
     *
     * @return String
     */
    public String createTransaction() {
        return spiMockBaseUrl + "/transaction";
    }
}
