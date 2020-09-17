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

package de.adorsys.psd2.aspsp.profile.domain.common;

import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.profile.TppUriCompliance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonAspspProfileBankSetting {

    /**
     * List of SCA Approach supported by ASPSP ordered by priority descending
     */
    private List<ScaApproach> scaApproachesSupported = new ArrayList<>();

    /**
     * Defines the variant of redirect approach to be used (REDIRECT, OAUTH, OAUTH_PRE_STEP)
     */
    private ScaRedirectFlow scaRedirectFlow;

    /**
     * URL to be used as link to IDP.
     */
    private String oauthConfigurationUrl;

    /**
     * Defines variant of forced mode of authorisation
     */
    private String startAuthorisationMode;

    /**
     * Indicates whether TPP Signature is required for the request or not
     */
    private boolean tppSignatureRequired;

    /**
     * Indicates whether PSU-ID is mandatory in the initial request for payment initiation or establishing consent
     */
    private boolean psuInInitialRequestMandated;

    /**
     * The limit of an expiration time of redirect url set in milliseconds
     */
    private long redirectUrlExpirationTimeMs;

    /**
     * The limit of authorisation time for PIS authorisation, PIS cancellation authorisation and AIS authorisation, set in milliseconds
     */
    private long authorisationExpirationTimeMs;

    /**
     * If "true", indicates that links in responses (except "scaRedirect") shall be generated with the base URL set by `xs2aBaseLinksUrl`,
     * if "false" - with the base URL of controller
     */
    private boolean forceXs2aBaseLinksUrl;

    /**
     * Is used as base url for TPP Links in case when `forceXs2aBaseLinksUrl` property is set to "true"
     */
    private String xs2aBaseLinksUrl;

    /**
     * Defines supported account identifier types
     */
    private List<SupportedAccountReferenceField> supportedAccountReferenceFields = new ArrayList<>();

    /**
     * Defines abstract level for multicurrency accounts on which the ASPSP offered services might be implemented
     */
    private MulticurrencyAccountLevel multicurrencyAccountLevelSupported;

    /**
     * If "true", indicates that a payment initiation service will be addressed in the same session
     */
    private boolean aisPisSessionsSupported;

    /**
     * Indicates whether ASPSP supports signing basket
     */
    private boolean signingBasketSupported;

    /**
     * Maximum entries in signing basket
     */
    private int signingBasketMaxEntries;

    /**
     * Indicates whether ASPSP supports validation TPP roles from certificate
     */
    private boolean checkTppRolesFromCertificateSupported;

    /**
     * List of supported notification modes
     */
    private List<NotificationSupportedMode> aspspNotificationsSupported = new ArrayList<>();

    /**
     * Shows if the confirmation of authorisation supported
     */
    private boolean authorisationConfirmationRequestMandated;

    /**
     * Shows if the authentication data should be checked by XS2A.
     */
    private boolean authorisationConfirmationCheckByXs2a;

    /**
     * Indicates whether ASPSP supports validation URIs with domain from certificate
     */
    private boolean checkUriComplianceToDomainSupported;

    /**
     * Indicates whether ASPSP returns warning with correct response or rejects request when redirect URIs don't match certificate
     */
    private TppUriCompliance tppUriComplianceResponse;
}
