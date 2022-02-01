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

package de.adorsys.psd2.aspsp.profile.mapper;

import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.migration.*;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisRedirectLinkBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.sb.SbAspspProfileBankSetting;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.PiisConsentSupported;
import de.adorsys.psd2.xs2a.core.profile.TppUriCompliance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NewProfileConfigurationMapper {
    private static final boolean DEFAULT_SCA_BY_ONE_TIME_GLOBAL_CONSENT_REQUIRED = true;
    private static final String DEFAULT_OAUTH_CONFIGURATION_URL = "http://localhost:4200/idp/";
    private static final String DEFAULT_PIIS_REDIRECT_URL = "http://localhost:4200/piis/{redirect-id}/{encrypted-consent-id}";
    private static final int DEFAULT_SIGNING_BASKET_MAX_ENTRIES = 10;
    private static final int DEFAULT_NOT_CONFIRMED_SB_EXPIRATION_TIME_MS = 86400000;
    private static final String DEFAULT_SB_REDIRECT_LINK = "http://localhost:4200/signing-basket/{redirect-id}/{encrypted-basket-id}";

    public NewProfileConfiguration mapToNewProfileConfiguration(OldProfileConfiguration oldProfileConfiguration) {
        OldBankProfileSetting setting = oldProfileConfiguration.getSetting();

        ConsentTypeBankSetting consentTypes = new ConsentTypeBankSetting(setting.isBankOfferedConsentSupport(),
                                                                         setting.isAllPsd2Support(),
                                                                         setting.isAvailableAccountsConsentSupported(),
                                                                         setting.getFrequencyPerDay(),
                                                                         setting.getNotConfirmedConsentExpirationPeriodMs(),
                                                                         setting.getConsentLifetime(),
                                                                         false,
                                                                         false);
        AisRedirectLinkBankSetting aisRedirectLinkToOnlineBanking = new AisRedirectLinkBankSetting(setting.getAisRedirectUrlToAspsp());
        AisTransactionBankSetting transactionParameters = new AisTransactionBankSetting(setting.getAvailableBookingStatuses(),
                                                                                        setting.isTransactionsWithoutBalancesSupported(),
                                                                                        Optional.ofNullable(setting.getSupportedTransactionApplicationTypes()).orElse(Collections.emptyList()));
        DeltaReportBankSetting deltaReportSettings = new DeltaReportBankSetting(setting.isEntryReferenceFromSupported(),
                                                                                setting.isDeltaListSupported());
        OneTimeConsentScaBankSetting scaRequirementsForOneTimeConsents = new OneTimeConsentScaBankSetting(setting.isScaByOneTimeAvailableAccountsConsentRequired(), DEFAULT_SCA_BY_ONE_TIME_GLOBAL_CONSENT_REQUIRED);
        AisAspspProfileBankSetting ais = new AisAspspProfileBankSetting(consentTypes, aisRedirectLinkToOnlineBanking, transactionParameters, deltaReportSettings, scaRequirementsForOneTimeConsents);
        PisRedirectLinkBankSetting pisRedirectLinkToOnlineBanking = new PisRedirectLinkBankSetting(setting.getPisRedirectUrlToAspsp(),
                                                                                                   setting.getPisPaymentCancellationRedirectUrlToAspsp(),
                                                                                                   setting.getPaymentCancellationRedirectUrlExpirationTimeMs());
        NewPisAspspProfileBankSetting pis = new NewPisAspspProfileBankSetting(setting.getSupportedPaymentTypeAndProductMatrix()
                                                                                  .entrySet().stream()
                                                                                  .collect(Collectors.toMap(e -> PaymentType.valueOf(e.getKey()),
                                                                                                            e -> new ArrayList<>(e.getValue()))),
                                                                              setting.getTransactionLifetime(),
                                                                              setting.getNotConfirmedPaymentExpirationPeriodMs(),
                                                                              setting.isPaymentCancellationAuthorizationMandated(),
                                                                              pisRedirectLinkToOnlineBanking,
                                                                              false);
        PiisAspspProfileBankSetting piis = new PiisAspspProfileBankSetting(setting.isPiisConsentSupported() ? PiisConsentSupported.ASPSP_CONSENT_SUPPORTED : PiisConsentSupported.NOT_SUPPORTED, new PiisRedirectLinkBankSetting(DEFAULT_PIIS_REDIRECT_URL));

        SbAspspProfileBankSetting sb = new SbAspspProfileBankSetting(setting.isSigningBasketSupported(),
                                                                     DEFAULT_SIGNING_BASKET_MAX_ENTRIES,
                                                                     DEFAULT_NOT_CONFIRMED_SB_EXPIRATION_TIME_MS,
                                                                     DEFAULT_SB_REDIRECT_LINK);

        CommonAspspProfileBankSetting common = new CommonAspspProfileBankSetting(setting.getScaApproaches(),
                                                                                 setting.getScaRedirectFlow(),
                                                                                 DEFAULT_OAUTH_CONFIGURATION_URL,
                                                                                 setting.getStartAuthorisationMode(),
                                                                                 setting.isTppSignatureRequired(),
                                                                                 setting.isPsuInInitialRequestMandated(),
                                                                                 setting.getRedirectUrlExpirationTimeMs(),
                                                                                 setting.getAuthorisationExpirationTimeMs(),
                                                                                 setting.isForceXs2aBaseUrl(),
                                                                                 setting.getXs2aBaseUrl(),
                                                                                 setting.getSupportedAccountReferenceFields(),
                                                                                 setting.getMulticurrencyAccountLevel(),
                                                                                 setting.isCombinedServiceIndicator(),
                                                                                 true,
                                                                                 Collections.singletonList(NotificationSupportedMode.NONE),
                                                                                 false,
                                                                                 false,
                                                                                 false,
                                                                                 TppUriCompliance.WARNING,
                                                                                 false,
                                                                                 false);

        NewProfileConfiguration result = new NewProfileConfiguration();
        result.setSetting(new NewBankProfileSetting(ais, pis, piis, sb, common));
        return result;
    }
}
