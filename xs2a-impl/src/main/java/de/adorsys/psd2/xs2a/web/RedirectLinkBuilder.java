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

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectLinkBuilder {
    private static final String REDIRECT_URL = "{redirect-id}";
    private static final String ENCRYPTED_CONSENT_ID = "{encrypted-consent-id}";
    private static final String ENCRYPTED_PAYMENT_ID = "{encrypted-payment-id}";
    private static final String INTERNAL_REQUEST_ID = "{inr-id}";

    private final AspspProfileServiceWrapper aspspProfileService;

    /**
     * Builds redirect links by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-consent-id} may be used in template.
     *
     * @param encryptedConsentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildConsentScaRedirectLink(String encryptedConsentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getAisRedirectUrlToAspsp()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_CONSENT_ID, encryptedConsentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
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
    public String buildPaymentScaRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getPisRedirectUrlToAspsp()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
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
    public String buildPaymentCancellationScaRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getPisPaymentCancellationRedirectUrlToAspsp()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }
}
