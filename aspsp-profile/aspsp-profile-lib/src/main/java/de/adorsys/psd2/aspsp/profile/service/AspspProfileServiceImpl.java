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

package de.adorsys.psd2.aspsp.profile.service;

import de.adorsys.psd2.aspsp.profile.config.BankProfileSetting;
import de.adorsys.psd2.aspsp.profile.config.ProfileConfigurations;
import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.ais.*;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisRedirectLinkBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisRedirectLinkSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisRedirectLinkSetting;
import de.adorsys.psd2.aspsp.profile.domain.sb.SbAspspProfileBankSetting;
import de.adorsys.psd2.aspsp.profile.domain.sb.SbAspspProfileSetting;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.StartAuthorisationMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AspspProfileServiceImpl implements AspspProfileService {
    private final ProfileConfigurations profileConfigurations;

    @Override
    public AspspSettings getAspspSettings(String instanceId) {
        BankProfileSetting setting = profileConfigurations.getSetting(instanceId);

        AisAspspProfileBankSetting aisBankSetting = setting.getAis();
        ConsentTypeBankSetting consentTypeSetting = aisBankSetting.getConsentTypes();
        ConsentTypeSetting consentTypes = new ConsentTypeSetting(consentTypeSetting.isBankOfferedConsentSupported(),
                                                                 consentTypeSetting.isGlobalConsentSupported(),
                                                                 consentTypeSetting.isAvailableAccountsConsentSupported(),
                                                                 consentTypeSetting.getAccountAccessFrequencyPerDay(),
                                                                 consentTypeSetting.getNotConfirmedConsentExpirationTimeMs(),
                                                                 consentTypeSetting.getMaxConsentValidityDays(),
                                                                 consentTypeSetting.isAccountOwnerInformationSupported(),
                                                                 consentTypeSetting.isTrustedBeneficiariesSupported());
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
                                                                pisRedirectLinkToOnlineBanking,
                                                                pisBankSetting.getCountryValidationSupported(),
                                                                pisBankSetting.getSupportedTransactionStatusFormats(),
                                                                pisBankSetting.isDebtorAccountOptionalInInitialRequest());

        PiisRedirectLinkSetting piisRedirectLinkSetting = new PiisRedirectLinkSetting(Optional.ofNullable(setting.getPiis().getRedirectLinkToOnlineBanking())
                                                                                          .map(PiisRedirectLinkBankSetting::getPiisRedirectUrlToAspsp)
                                                                                          .orElse(null));
        PiisAspspProfileSetting piis = new PiisAspspProfileSetting(setting.getPiis().getPiisConsentSupported(), piisRedirectLinkSetting);

        SbAspspProfileBankSetting sbBankSetting = setting.getSb();
        SbAspspProfileSetting sb = new SbAspspProfileSetting(sbBankSetting.isSigningBasketSupported(),
                                                             sbBankSetting.getSigningBasketMaxEntries(),
                                                             sbBankSetting.getNotConfirmedSigningBasketExpirationTimeMs(),
                                                             sbBankSetting.getSbRedirectUrlToAspsp());

        CommonAspspProfileBankSetting commonBankSetting = setting.getCommon();
        CommonAspspProfileSetting common = new CommonAspspProfileSetting(commonBankSetting.getScaRedirectFlow(),
                                                                         commonBankSetting.getOauthConfigurationUrl(),
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
                                                                         commonBankSetting.isCheckTppRolesFromCertificateSupported(),
                                                                         commonBankSetting.getAspspNotificationsSupported(),
                                                                         commonBankSetting.isAuthorisationConfirmationRequestMandated(),
                                                                         commonBankSetting.isAuthorisationConfirmationCheckByXs2a(),
                                                                         commonBankSetting.isCheckUriComplianceToDomainSupported(),
                                                                         commonBankSetting.getTppUriComplianceResponse(),
                                                                         commonBankSetting.isPsuInInitialRequestIgnored(),
                                                                         commonBankSetting.isIbanValidationDisabled());

        return new AspspSettings(ais, pis, piis, sb, common);
    }

    @Override
    public List<ScaApproach> getScaApproaches(String instanceId) {
        return profileConfigurations.getSetting(instanceId)
                   .getCommon()
                   .getScaApproachesSupported();
    }

    @Override
    public boolean isMultitenancyEnabled() {
        return profileConfigurations.isMultitenancyEnabled();
    }
}
