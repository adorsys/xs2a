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

import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;
import org.springframework.web.util.UriComponentsBuilder;

public class UrlBuilder {
    public static String buildInitiatePaymentUrl(String paymentType, String paymentProduct) {
        return "/v1/" + paymentType + "/" + paymentProduct + "/";
    }

    public static String buildGetPaymentInitiationScaStatusUrl(String paymentType, String paymentProduct, String encrPaymentId, String authorisationId) {
        return UriComponentsBuilder.fromPath(UrlHolder.PIS_AUTHORISATION_LINK_URL)
                   .buildAndExpand(paymentType, paymentProduct, encrPaymentId, authorisationId)
                   .toUriString();
    }

    public static String buildGetPaymentCancellationAuthorisationUrl(String paymentType, String paymentProduct, String encrPaymentId) {
        return UriComponentsBuilder.fromPath(UrlHolder.START_PIS_CANCELLATION_AUTH_URL)
                   .buildAndExpand(paymentType, paymentProduct, encrPaymentId)
                   .toUriString();
    }

    public static String buildGetTransactionsUrl(String accountId) {
        return UriComponentsBuilder.fromPath(UrlHolder.ACCOUNT_TRANSACTIONS_URL)
                   .queryParam("bookingStatus", "booked")
                   .queryParam("dateFrom", "2019-10-18")
                   .buildAndExpand(accountId)
                   .toUriString();
    }

    public static String buildGetAccountList() {
        return "/v1/accounts";
    }

    public static String buildConsentCreation() {
        return "/v1/consents/";
    }

    public static String buildDeleteConsentUrl(String encryptedConsentId) {
        return UriComponentsBuilder.fromPath(UrlHolder.CONSENT_LINK_URL)
                   .buildAndExpand(encryptedConsentId)
                   .toUriString();
    }

    public static String buildPaymentStartAuthorisationUrl(String paymentType, String paymentProduct, String encrPaymentId) {
        return UriComponentsBuilder.fromPath(UrlHolder.START_PIS_AUTHORISATION_URL)
                   .buildAndExpand(paymentType, paymentProduct, encrPaymentId)
                   .toUriString();
    }

    public static String buildGetPaymentInitiationCancellationAuthorisationInformationUrl(String paymentType, String paymentProduct, String encrPaymentId) {
        return UriComponentsBuilder.fromPath(UrlHolder.START_PIS_CANCELLATION_AUTH_URL)
                   .buildAndExpand(paymentType, paymentProduct, encrPaymentId)
                   .toUriString();
    }

    public static String buildCancellationPaymentUrl(String paymentType, String paymentProduct, String encryptedPaymentId) {
        return UriComponentsBuilder.fromPath(UrlHolder.PAYMENT_LINK_URL)
                   .buildAndExpand(paymentType, paymentProduct, encryptedPaymentId)
                   .toUriString();
    }

    public static String buildPaymentStartCancellationAuthorisationUrl(String paymentType, String paymentProduct, String encrPaymentId) {
        return UriComponentsBuilder.fromPath(UrlHolder.START_PIS_CANCELLATION_AUTH_URL)
                   .buildAndExpand(paymentType, paymentProduct, encrPaymentId)
                   .toUriString();
    }

    public static String buildPaymentUpdateAuthorisationUrl(String paymentType, String paymentProduct, String encrPaymentId, String authorisationId) {
        return UriComponentsBuilder.fromPath(UrlHolder.PIS_AUTHORISATION_LINK_URL)
                   .buildAndExpand(paymentType, paymentProduct, encrPaymentId, authorisationId)
                   .toUriString();
    }

    public static String buildPaymentCancellationUpdateAuthorisationUrl(String paymentType, String paymentProduct, String encrPaymentId, String authorisationId) {
        return UriComponentsBuilder.fromPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL)
                   .buildAndExpand(paymentType, paymentProduct, encrPaymentId, authorisationId)
                   .toUriString();
    }

    public static String buildConsentUpdateAuthorisationUrl(String consentId, String authorisationId) {
        return UriComponentsBuilder.fromPath(UrlHolder.AIS_AUTHORISATION_URL)
                   .buildAndExpand(consentId, authorisationId)
                   .toUriString();
    }

    public static String buildGetTransactionStatusUrl(String paymentType, String paymentProduct, String encryptedPaymentId) {
        return UriComponentsBuilder.fromPath(UrlHolder.PAYMENT_STATUS_URL)
                   .buildAndExpand(paymentType, paymentProduct, encryptedPaymentId)
                   .toUriString();
    }
}
