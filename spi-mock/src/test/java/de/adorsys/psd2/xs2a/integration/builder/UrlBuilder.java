/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.integration.builder;

public class UrlBuilder {
    public static String buildInitiatePaymentUrl(String paymentType, String paymentProduct) {
        return "/v1/" + paymentType + "/" + paymentProduct + "/" ;
    }

    public static String buildGetPaymentInitiationScaStatusUrl(String paymentType, String paymentProduct, String encrPaymentId, String authorisationId) {
        return "/v1/" + paymentType + "/" + paymentProduct + "/" + encrPaymentId + "/authorisations/" + authorisationId;
    }

    public static String buildGetPaymentCancellationAuthorisationUrl(String paymentType, String paymentProduct, String encrPaymentId) {
        return "/v1/" + paymentType + "/" + paymentProduct + "/" + encrPaymentId + "/cancellation-authorisations";
    }

    public static String buildGetTransactionsUrlWithoutSlash(String accountId) {
        return "/v1/accounts/" + accountId + "/transactions" + "?bookingStatus=booked";
    }

    public static String buildGetTransactionsUrlWithSlash(String accountId) {
        return "/v1/accounts/" + accountId + "/transactions/" + "?bookingStatus=booked";
    }

    public static String buildGetAccountList() {
        return "/v1/accounts";
    }

    public static String buildConsentCreation() {
        return "/v1/consents/";
    }
}
