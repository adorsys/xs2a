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

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.link.UrlHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RedirectLinkBuilder {
    private static final String REDIRECT_URL = "{redirect-id}";
    private static final String ENCRYPTED_CONSENT_ID = "{encrypted-consent-id}";
    private static final String ENCRYPTED_PAYMENT_ID = "{encrypted-payment-id}";
    private static final String INTERNAL_REQUEST_ID = "{inr-id}";
    private static final String PAYMENT_SERVICE = "{payment-service}";
    private static final String PAYMENT_PRODUCT = "{payment-product}";
    private static final String PAYMENT_ID = "{payment-id}";
    private static final String CONSENT_ID = "{consentId}";
    private static final String AUTHORISATION_ID = "{authorisation-id}";
    private static final String INSTANCE_ID = "{instance-id}";
    private static final String DEFAULT_INSTANCE_ID = "UNDEFINED";

    private final AspspProfileServiceWrapper aspspProfileService;

    /**
     * Builds redirect links by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-consent-id} may be used in template.
     *
     * @param encryptedConsentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @param consentType        - type of the Consent
     * @return redirect link
     */
    public String buildConsentScaRedirectLink(String encryptedConsentId, String redirectId, String internalRequestId,
                                              String instanceId, ConsentType consentType) {
        String redirectUrl = getRedirectUrlByConsentType(consentType);

        String scaRedirectLink = redirectUrl
                                     .replace(REDIRECT_URL, redirectId)
                                     .replace(ENCRYPTED_CONSENT_ID, encryptedConsentId)
                                     .replace(INTERNAL_REQUEST_ID, internalRequestId);
        return enrichByInstanceId(scaRedirectLink, instanceId);
    }

    /**
     * Builds OAuth redirect link by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-consent-id} may be used in template.
     *
     * @param encryptedConsentId - Encrypted consent ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildConsentScaOauthRedirectLink(String encryptedConsentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getOauthConfigurationUrl()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_CONSENT_ID, encryptedConsentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }

    /**
     * Builds redirect links by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-payment-id} may be used in template.
     *
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildPaymentScaRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId,
                                              String instanceId) {
        String scaRedirectLink = aspspProfileService.getPisRedirectUrlToAspsp()
                                     .replace(REDIRECT_URL, redirectId)
                                     .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                                     .replace(INTERNAL_REQUEST_ID, internalRequestId);
        return enrichByInstanceId(scaRedirectLink, instanceId);
    }

    /**
     * Builds OAuth redirect link by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-payment-id} may be used in template.
     *
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildPaymentScaOauthRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getOauthConfigurationUrl()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }

    /**
     * Builds redirect links by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-payment-id} may be used in template.
     *
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildPaymentCancellationScaRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId,
                                                          String instanceId) {
        String scaRedirectLink = aspspProfileService.getPisPaymentCancellationRedirectUrlToAspsp()
                                     .replace(REDIRECT_URL, redirectId)
                                     .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                                     .replace(INTERNAL_REQUEST_ID, internalRequestId);
        return enrichByInstanceId(scaRedirectLink, instanceId);
    }

    /**
     * Builds OAuth redirect links by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-payment-id} may be used in template.
     *
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildPaymentCancellationScaOauthRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getOauthConfigurationUrl()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }

    /**
     * Builds confirmation link for payments.
     *
     * @param paymentService     - Payment service (ex. "payments")
     * @param paymentProduct     - Payment product (ex. "sepa-credit-transfers")
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @return confirmation link
     */
    public String buildPisConfirmationLink(String paymentService, String paymentProduct, String encryptedPaymentId, String redirectId) {
        return UrlHolder.PIS_AUTHORISATION_LINK_URL
                   .replace(PAYMENT_SERVICE, paymentService)
                   .replace(PAYMENT_PRODUCT, paymentProduct)
                   .replace(PAYMENT_ID, encryptedPaymentId)
                   .replace(AUTHORISATION_ID, redirectId);
    }

    /**
     * Builds confirmation link for payment cancellations.
     *
     * @param paymentService     - Payment service (ex. "payments")
     * @param paymentProduct     - Payment product (ex. "sepa-credit-transfers")
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @return confirmation link
     */
    public String buildPisCancellationConfirmationLink(String paymentService, String paymentProduct, String encryptedPaymentId, String redirectId) {
        return UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL
                   .replace(PAYMENT_SERVICE, paymentService)
                   .replace(PAYMENT_PRODUCT, paymentProduct)
                   .replace(PAYMENT_ID, encryptedPaymentId)
                   .replace(AUTHORISATION_ID, redirectId);
    }

    /**
     * Builds confirmation link for consents.
     *
     * @param consentId  - ID of consent
     * @param redirectId - Redirect ID
     * @return confirmation link
     */
    public String buildConfirmationLink(String consentId, String redirectId, ConsentType consentType) {
        String authorisationUrl = getAuthorisationUrlByConsentType(consentType);
        return authorisationUrl
                   .replace(CONSENT_ID, consentId)
                   .replace(AUTHORISATION_ID, redirectId);
    }

    private String getAuthorisationUrlByConsentType(ConsentType consentType) {
        switch (consentType) {
            case AIS: return UrlHolder.AIS_AUTHORISATION_URL;
            case PIIS_TPP: return UrlHolder.PIIS_AUTHORISATION_URL;
            default: throw new UnsupportedOperationException("Can't find authorisation url by consent type " + consentType);
        }
    }

    private String enrichByInstanceId(String link, String instanceId) {
        return link.replace(INSTANCE_ID, Objects.requireNonNullElse(instanceId, DEFAULT_INSTANCE_ID));
    }

    private String getRedirectUrlByConsentType(ConsentType consentType) {
        switch (consentType) {
            case AIS: return aspspProfileService.getAisRedirectUrlToAspsp();
            case PIIS_TPP: return aspspProfileService.getPiisRedirectUrlToAspsp();
            default: throw new UnsupportedOperationException("Can't find redirect url by consent type " + consentType);
        }
    }
}
