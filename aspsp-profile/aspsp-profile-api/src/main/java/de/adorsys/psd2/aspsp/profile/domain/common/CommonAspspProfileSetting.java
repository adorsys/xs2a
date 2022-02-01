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
    private final boolean checkTppRolesFromCertificateSupported;
    private final List<NotificationSupportedMode> aspspNotificationsSupported;
    private final boolean authorisationConfirmationRequestMandated;
    private final boolean authorisationConfirmationCheckByXs2a;
    private final boolean checkUriComplianceToDomainSupported;
    private final TppUriCompliance tppUriComplianceResponse;
    private final boolean psuInInitialRequestIgnored;
    private final boolean ibanValidationDisabled;
}
