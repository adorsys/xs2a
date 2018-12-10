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
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AspspProfileServiceImpl implements AspspProfileService {
    private final ProfileConfiguration profileConfiguration;

    @Override
    public AspspSettings getAspspSettings() {
        BankProfileSetting setting = profileConfiguration.getSetting();
        return new AspspSettings(
            setting.getFrequencyPerDay(),
            setting.isCombinedServiceIndicator(),
            setting.getAvailablePaymentProducts(),
            setting.getAvailablePaymentTypes(),
            setting.isTppSignatureRequired(),
            setting.getPisRedirectUrlToAspsp(),
            setting.getAisRedirectUrlToAspsp(),
            setting.getMulticurrencyAccountLevel(),
            setting.isBankOfferedConsentSupport(),
            setting.getAvailableBookingStatuses(),
            setting.getSupportedAccountReferenceFields(),
            setting.getConsentLifetime(),
            setting.getTransactionLifetime(),
            setting.isAllPsd2Support(),
            setting.isTransactionsWithoutBalancesSupported(),
            setting.isSigningBasketSupported(),
            setting.isPaymentCancellationAuthorizationMandated(),
            setting.isPiisConsentSupported(),
            setting.isDeltaReportSupported(),
            setting.getRedirectUrlExpirationTimeMs());
    }

    @Override
    public ScaApproach getScaApproach() {
        return profileConfiguration.getSetting()
                   .getScaApproach();
    }
}
