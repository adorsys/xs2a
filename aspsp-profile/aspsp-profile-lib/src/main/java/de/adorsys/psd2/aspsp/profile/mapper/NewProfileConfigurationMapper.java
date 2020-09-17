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

package de.adorsys.psd2.aspsp.profile.mapper;

import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.migration.*;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisRedirectLinkBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkBankSetting;
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
                                                                              pisRedirectLinkToOnlineBanking);
        PiisAspspProfileBankSetting piis = new PiisAspspProfileBankSetting(setting.isPiisConsentSupported() ? PiisConsentSupported.ASPSP_CONSENT_SUPPORTED : PiisConsentSupported.NOT_SUPPORTED, new PiisRedirectLinkBankSetting(DEFAULT_PIIS_REDIRECT_URL));
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
                                                                                 setting.isSigningBasketSupported(),
                                                                                 DEFAULT_SIGNING_BASKET_MAX_ENTRIES,
                                                                                 true,
                                                                                 Collections.singletonList(NotificationSupportedMode.NONE),
                                                                                 false,
                                                                                 false,
                                                                                 false,
                                                                                 TppUriCompliance.WARNING);

        NewProfileConfiguration result = new NewProfileConfiguration();
        result.setSetting(new NewBankProfileSetting(ais, pis, piis, common));
        return result;
    }
}
