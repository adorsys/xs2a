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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfiguration;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
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

        AisAspspProfileBankSetting aisBankSetting = setting.getAis();
        ConsentTypeBankSetting consentTypeSetting = aisBankSetting.getConsentTypes();
        ConsentTypeSetting consentTypes = new ConsentTypeSetting(consentTypeSetting.isBankOfferedConsentSupported(),
                                                                 consentTypeSetting.isGlobalConsentSupported(),
                                                                 consentTypeSetting.isAvailableAccountsConsentSupported(),
                                                                 consentTypeSetting.getAccountAccessFrequencyPerDay(),
                                                                 consentTypeSetting.getNotConfirmedConsentExpirationTimeMs(),
                                                                 consentTypeSetting.getMaxConsentValidityDays(),
                                                                 consentTypeSetting.isAccountOwnerInformationSupported());
        AisRedirectLinkSetting aisRedirectLinkToOnlineBanking = new AisRedirectLinkSetting(aisBankSetting.getRedirectLinkToOnlineBanking().getAisRedirectUrlToAspsp());
        AisTransactionSetting transactionParameters = new AisTransactionSetting(aisBankSetting.getTransactionParameters().getAvailableBookingStatuses(),
                                                                                aisBankSetting.getTransactionParameters().isTransactionsWithoutBalancesSupported(),
                                                                                aisBankSetting.getTransactionParameters().getSupportedTransactionApplicationTypes());
        DeltaReportSetting deltaReportSettings = new DeltaReportSetting(aisBankSetting.getDeltaReportSettings().isEntryReferenceFromSupported(),
                                                                        aisBankSetting.getDeltaReportSettings().isDeltaListSupported());

        OneTimeConsentScaBankSetting scaRequirementsForOneTimeConsentsBankSetting = aisBankSetting.getScaRequirementsForOneTimeConsents();
        OneTimeConsentScaSetting scaRequirementsForOneTimeConsents = new OneTimeConsentScaSetting(scaRequirementsForOneTimeConsentsBankSetting.isScaByOneTimeAvailableAccountsConsentRequired(), scaRequirementsForOneTimeConsentsBankSetting.isScaByOneTimeGlobalConsentRequired());

        AisAspspProfileSetting ais = new AisAspspProfileSetting(consentTypes, aisRedirectLinkToOnlineBanking, transactionParameters, deltaReportSettings, scaRequirementsForOneTimeConsents);

        PisAspspProfileBankSetting pisBankSetting = setting.getPis();
        PisRedirectLinkSetting pisRedirectLinkToOnlineBanking = new PisRedirectLinkSetting(pisBankSetting.getRedirectLinkToOnlineBanking().getPisRedirectUrlToAspsp(),
                                                                                           pisBankSetting.getRedirectLinkToOnlineBanking().getPisPaymentCancellationRedirectUrlToAspsp(),
                                                                                           pisBankSetting.getRedirectLinkToOnlineBanking().getPaymentCancellationRedirectUrlExpirationTimeMs());
        PisAspspProfileSetting pis = new PisAspspProfileSetting(pisBankSetting.getSupportedPaymentTypeAndProductMatrix(),
                                                                pisBankSetting.getMaxTransactionValidityDays(),
                                                                pisBankSetting.getNotConfirmedPaymentExpirationTimeMs(),
                                                                pisBankSetting.isPaymentCancellationAuthorisationMandated(),
                                                                pisRedirectLinkToOnlineBanking);
        PiisAspspProfileSetting piis = new PiisAspspProfileSetting(setting.getPiis().isPiisConsentSupported());

        CommonAspspProfileBankSetting commonBankSetting = setting.getCommon();
        CommonAspspProfileSetting common = new CommonAspspProfileSetting(commonBankSetting.getScaRedirectFlow(),
                                                                         commonBankSetting.getStartAuthorisationMode() == null
                                                                             ? StartAuthorisationMode.AUTO
                                                                             : StartAuthorisationMode.getByValue(commonBankSetting.getStartAuthorisationMode()),
                                                                         commonBankSetting.isTppSignatureRequired(),
                                                                         commonBankSetting.isPsuInInitialRequestMandated(),
                                                                         commonBankSetting.getRedirectUrlExpirationTimeMs(),
                                                                         commonBankSetting.getAuthorisationExpirationTimeMs(),
                                                                         commonBankSetting.isForceXs2aBaseLinksUrl(),
                                                                         commonBankSetting.getXs2aBaseLinksUrl(),
                                                                         commonBankSetting.getSupportedAccountReferenceFields(),
                                                                         commonBankSetting.getMulticurrencyAccountLevelSupported(),
                                                                         commonBankSetting.isAisPisSessionsSupported(),
                                                                         commonBankSetting.isSigningBasketSupported());

        return new AspspSettings(ais, pis, piis, common);
    }

    @Override
    public List<ScaApproach> getScaApproaches() {
        return profileConfiguration.getSetting()
                   .getCommon()
                   .getScaApproachesSupported();
    }
}
