/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkSetting;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AspspProfileServiceImpl implements AspspProfileService {
    private final ProfileConfiguration profileConfiguration;

    @Override
    public AspspSettings getAspspSettings() {
        BankProfileSetting setting = profileConfiguration.getSetting();
        ConsentTypeSetting consentTypes = new ConsentTypeSetting(setting.getAis().getConsentTypes().isBankOfferedConsentSupported(),
                                                                 setting.getAis().getConsentTypes().isGlobalConsentSupported(),
                                                                 setting.getAis().getConsentTypes().isAvailableAccountsConsentSupported(),
                                                                 setting.getAis().getConsentTypes().getAccountAccessFrequencyPerDay(),
                                                                 setting.getAis().getConsentTypes().getNotConfirmedConsentExpirationTimeMs(),
                                                                 setting.getAis().getConsentTypes().getMaxConsentValidityDays());
        AisRedirectLinkSetting aisRedirectLinkToOnlineBanking = new AisRedirectLinkSetting(setting.getAis().getRedirectLinkToOnlineBanking().getAisRedirectUrlToAspsp());
        AisTransactionSetting transactionParameters = new AisTransactionSetting(setting.getAis().getTransactionParameters().getAvailableBookingStatuses(),
                                                                                setting.getAis().getTransactionParameters().isTransactionsWithoutBalancesSupported(),
                                                                                setting.getAis().getTransactionParameters().getSupportedTransactionApplicationType());
        DeltaReportSetting deltaReportSettings = new DeltaReportSetting(setting.getAis().getDeltaReportSettings().isEntryReferenceFromSupported(),
                                                                        setting.getAis().getDeltaReportSettings().isDeltaListSupported());
        OneTimeConsentScaSetting scaRequirementsForOneTimeConsents = new OneTimeConsentScaSetting(setting.getAis().getScaRequirementsForOneTimeConsents().isScaByOneTimeAvailableAccountsConsentRequired());
        AisAspspProfileSetting ais = new AisAspspProfileSetting(consentTypes, aisRedirectLinkToOnlineBanking, transactionParameters, deltaReportSettings, scaRequirementsForOneTimeConsents);
        PisRedirectLinkSetting pisRedirectLinkToOnlineBanking = new PisRedirectLinkSetting(setting.getPis().getRedirectLinkToOnlineBanking().getPisRedirectUrlToAspsp(),
                                                                                           setting.getPis().getRedirectLinkToOnlineBanking().getPisPaymentCancellationRedirectUrlToAspsp(),
                                                                                           setting.getPis().getRedirectLinkToOnlineBanking().getPaymentCancellationRedirectUrlExpirationTimeMs());
        PisAspspProfileSetting pis = new PisAspspProfileSetting(setting.getPis().getSupportedPaymentTypeAndProductMatrix(),
                                                                setting.getPis().getMaxTransactionValidityDays(),
                                                                setting.getPis().getNotConfirmedPaymentExpirationTimeMs(),
                                                                setting.getPis().isPaymentCancellationAuthorisationMandated(),
                                                                pisRedirectLinkToOnlineBanking);
        PiisAspspProfileSetting piis = new PiisAspspProfileSetting(setting.getPiis().isPiisConsentSupported());
        CommonAspspProfileSetting common = new CommonAspspProfileSetting(setting.getCommon().getScaRedirectFlow(),
                                                                         setting.getCommon().getStartAuthorisationMode() == null
                                                                             ? StartAuthorisationMode.AUTO
                                                                             : StartAuthorisationMode.getByValue(setting.getCommon().getStartAuthorisationMode()),
                                                                         setting.getCommon().isTppSignatureRequired(),
                                                                         setting.getCommon().isPsuInInitialRequestMandated(),
                                                                         setting.getCommon().getRedirectUrlExpirationTimeMs(),
                                                                         setting.getCommon().getAuthorisationExpirationTimeMs(),
                                                                         setting.getCommon().isForceXs2aBaseLinksUrl(),
                                                                         setting.getCommon().getXs2aBaseLinksUrl(),
                                                                         setting.getCommon().getSupportedAccountReferenceFields(),
                                                                         setting.getCommon().getMulticurrencyAccountLevelSupported(),
                                                                         setting.getCommon().isAisPisSessionsSupported(),
                                                                         setting.getCommon().isSigningBasketSupported());

        return new AspspSettings(ais, pis, piis, common);
    }

    @Override
    public List<ScaApproach> getScaApproaches() {
        return profileConfiguration.getSetting()
                   .getCommon()
                   .getScaApproachesSupported();
    }
}
