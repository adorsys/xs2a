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

package de.adorsys.psd2.xs2a.integration.builder;

import de.adorsys.psd2.xs2a.web.link.UrlHolder;
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

    public static String buildGetPaymentUrl(String paymentType, String paymentProduct, String encryptedPaymentId) {
        return UriComponentsBuilder.fromPath(UrlHolder.PAYMENT_LINK_URL)
                   .buildAndExpand(paymentType, paymentProduct, encryptedPaymentId)
                   .toUriString();
    }
}
