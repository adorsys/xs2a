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

package de.adorsys.psd2.consent.integration;

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.springframework.web.util.UriComponentsBuilder;

public class UrlBuilder {

    public static String getConsentsByTppUrl(String tppId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/ais/consents/tpp/{tpp-id}")
                   .buildAndExpand(tppId)
                   .toUriString();
    }

    public static String getConsentsByPsuUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/ais/consents/psu")
                   .toUriString();
    }

    public static String getConsentsByAccountUrl(String accountId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/ais/consents/account/{account-id}")
                   .buildAndExpand(accountId)
                   .toUriString();
    }

    public static String getEventsForDatesUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/events/")
                   .toUriString();
    }

    public static String createPiisConsentUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents")
                   .toUriString();
    }

    public static String getPiisConsentsByPsuUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents")
                   .toUriString();
    }

    public static String getPiisTerminateConsentUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents/{consent-id}")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String getPiisConsentsByTppUrl(String tppId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents/tpp/{tpp-id}")
                   .buildAndExpand(tppId)
                   .toUriString();
    }

    public static String getPiisConsentsByPsuUrl2() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents/psu")
                   .toUriString();
    }

    public static String getPiisConsentsByAccountUrl(String accountId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents/account/{account-id}")
                   .buildAndExpand(accountId)
                   .toUriString();
    }

    public static String getPaymentsByTppUrl(String tppId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/pis/payments/tpp/{tpp-id}")
                   .buildAndExpand(tppId)
                   .toUriString();
    }

    public static String getPaymentsByPsuUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/pis/payments/psu")
                   .toUriString();
    }

    public static String getPaymentsByAccountUrl(String accountId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/pis/payments/account/{account-id}")
                   .buildAndExpand(accountId)
                   .toUriString();
    }

    public static String updatePaymentStatusUrl(String paymentId, String status) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/pis/transaction-status/{payment-id}/status/{status}")
                   .buildAndExpand(paymentId, status)
                   .toUriString();
    }

    public static String closeAllConsentsUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/psu/consent/all")
                   .toUriString();
    }

    public static String getTppStopListRecordUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/tpp/stop-list")
                   .toUriString();
    }

    public static String blockTppUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/tpp/stop-list/block")
                   .toUriString();
    }

    public static String unblockTppUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/tpp/stop-list/unblock")
                   .toUriString();
    }

    public static String getTppInfoUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/tpp")
                   .toUriString();
    }

    public static String getAspspConsentData(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/aspsp-consent-data/consents/{consent-id}")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String updatePsuDataInConsentUrl(String consentId, String authorisationId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/psu-data")
                   .buildAndExpand(consentId, authorisationId)
                   .toUriString();
    }

    public static String updateAuthorisationStatusUrl(String consentId, String authorisationId, String status) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}")
                   .buildAndExpand(consentId, authorisationId, status)
                   .toUriString();
    }

    public static String confirmConsentUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/{consent-id}/confirm-consent")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String rejectConsentUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/{consent-id}/reject-consent")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String getConsentsForPsuUrl() {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/consents")
                   .toUriString();
    }

    public static String revokeConsentUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/{consent-id}/revoke-consent")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String authorisePartiallyConsentUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/{consent-id}/authorise-partially-consent")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String getConsentIdByRedirectIdUrl(String redirectId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/redirect/{redirect-id}")
                   .buildAndExpand(redirectId)
                   .toUriString();
    }

    public static String getConsentByConsentIdUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/{consent-id}")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String getAuthorisationByAuthorisationIdUrl(String authorisationId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/authorisation/{authorisation-id}")
                   .buildAndExpand(authorisationId)
                   .toUriString();
    }

    public static String putAccountAccessInConsentUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/{consent-id}/save-access")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String psuDataAuthorisationsUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/ais/consent/{consent-id}/authorisation/psus")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String getPiisConsentsUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/piis/consents/{consent-id}")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String getPiisConsentsForPsuUrl() {
        return UriComponentsBuilder.fromPath("/psu-api/v1/piis/consents")
                   .toUriString();
    }

    public static String revokePiisConsentUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/piis/consents/{consent-id}/revoke-consent")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String updatePsuInPaymentUrl(String authorisationId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/payment/authorisation/{authorisation-id}/psu-data")
                   .buildAndExpand(authorisationId)
                   .toUriString();
    }

    public static String getPaymentIdByRedirectIdUrl(String redirectId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/payment/redirect/{redirect-id}")
                   .buildAndExpand(redirectId)
                   .toUriString();
    }

    public static String getPaymentByPaymentIdUrl(String paymentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/payment/{payment-id}")
                   .buildAndExpand(paymentId)
                   .toUriString();
    }

    public static String getPaymentIdByRedirectIdForCancellationUrl(String redirectId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/payment/cancellation/redirect/{redirect-id}")
                   .buildAndExpand(redirectId)
                   .toUriString();
    }

    public static String getPisAuthorisationByAuthorisationIdUrl(String authorisationId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/payment/authorisation/{authorisation-id}")
                   .buildAndExpand(authorisationId)
                   .toUriString();
    }

    public static String updatePisAuthorisationStatusUrl(String paymentId, String authorisationId, ScaStatus status) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/payment/{payment-id}/authorisation/{authorisation-id}/status/{status}")
                   .buildAndExpand(paymentId, authorisationId, status)
                   .toUriString();
    }

    public static String updatePsuPaymentStatusUrl(String paymentId, String paymentStatus) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/payment/{payment-id}/status/{status}")
                   .buildAndExpand(paymentId, paymentStatus)
                   .toUriString();
    }

    public static String psuAuthorisationStatusesUrl(String paymentId) {
        return UriComponentsBuilder.fromPath("/psu-api/v1/payment/{payment-id}/authorisation/psus")
                   .buildAndExpand(paymentId)
                   .toUriString();
    }
}
