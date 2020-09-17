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

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CommonAspspProfileSetting {
    private ScaRedirectFlow scaRedirectFlow;
    private String oauthConfigurationUrl;
    private StartAuthorisationMode startAuthorisationMode;
    private boolean tppSignatureRequired;
    private boolean psuInInitialRequestMandated;
    private long redirectUrlExpirationTimeMs;
    private long authorisationExpirationTimeMs;
    private boolean forceXs2aBaseLinksUrl;
    private String xs2aBaseLinksUrl;
    private List<SupportedAccountReferenceField> supportedAccountReferenceFields = new ArrayList<>();
    private MulticurrencyAccountLevel multicurrencyAccountLevelSupported;
    private boolean aisPisSessionsSupported;
    private boolean signingBasketSupported;
    private int signingBasketMaxEntries;
    private boolean checkTppRolesFromCertificateSupported;
    private List<NotificationSupportedMode> aspspNotificationsSupported = new ArrayList<>();
    private boolean authorisationConfirmationRequestMandated;
    private boolean authorisationConfirmationCheckByXs2a;
    private boolean checkUriComplianceToDomainSupported;
    private TppUriCompliance tppUriComplianceResponse;
}
