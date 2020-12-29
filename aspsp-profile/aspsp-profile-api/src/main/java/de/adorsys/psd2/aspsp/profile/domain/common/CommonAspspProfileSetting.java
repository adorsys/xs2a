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
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import de.adorsys.psd2.xs2a.core.profile.TppUriCompliance;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CommonAspspProfileSetting {
    private final ScaRedirectFlow scaRedirectFlow;
    private final String oauthConfigurationUrl;
    private final StartAuthorisationMode startAuthorisationMode;
    private final boolean tppSignatureRequired;
    private final boolean psuInInitialRequestMandated;
    private final long redirectUrlExpirationTimeMs;
    private final long authorisationExpirationTimeMs;
    private final boolean forceXs2aBaseLinksUrl;
    private final String xs2aBaseLinksUrl;
    private final List<SupportedAccountReferenceField> supportedAccountReferenceFields;
    private final MulticurrencyAccountLevel multicurrencyAccountLevelSupported;
    private final boolean aisPisSessionsSupported;
    private final boolean signingBasketSupported;
    private final boolean checkTppRolesFromCertificateSupported;
    private final List<NotificationSupportedMode> aspspNotificationsSupported;
    private final boolean authorisationConfirmationRequestMandated;
    private final boolean authorisationConfirmationCheckByXs2a;
    private final boolean checkUriComplianceToDomainSupported;
    private final TppUriCompliance tppUriComplianceResponse;
}
