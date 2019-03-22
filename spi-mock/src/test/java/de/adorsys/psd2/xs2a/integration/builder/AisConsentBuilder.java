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

package de.adorsys.psd2.xs2a.integration.builder;

import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisAccountConsentAuthorisation;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS;
import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS_WITH_BALANCES;

public class AisConsentBuilder {
    private final static TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private final static PsuIdData PSU_DATA = PsuIdDataBuilder.buildPsuIdData();

    public static AisAccountConsent buildAisConsent(CreateConsentReq consentReq, String consentId, ScaApproach scaApproach) {
        return Optional.ofNullable(consentReq)
            .map(cr -> new AisAccountConsent(
                    consentId,
                    mapToAccountAccess(cr.getAccess()),
                    cr.isRecurringIndicator(),
                    cr.getValidUntil(),
                    cr.getFrequencyPerDay(),
                    LocalDate.now(),
                    ConsentStatus.RECEIVED,
                    !cr.getAccess().getBalances().isEmpty(),
                    ScaApproach.REDIRECT.equals(scaApproach),
                    getAisConsentRequestType(cr.getAccess()),
                    Collections.singletonList(PSU_DATA),
                    TPP_INFO,
                    false,
                    Collections.singletonList(new AisAccountConsentAuthorisation(PSU_DATA, ScaStatus.RECEIVED)),
                    0,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
                )
            )
            .orElse(null);
    }

    private static AisAccountAccess mapToAccountAccess(Xs2aAccountAccess access) {
        AccountAccessType availableAccounts = access.getAvailableAccounts();
        return Optional.ofNullable(access)
            .map(aa ->
                new AisAccountAccess(
                    aa.getAccounts(),
                    aa.getBalances(),
                    aa.getTransactions(),
                    availableAccounts != null ? availableAccounts.name() : null,
                    access.getAllPsd2() != null ? access.getAllPsd2().name() : null
                )
            )
            .orElse(null);
    }

    private static AisConsentRequestType getAisConsentRequestType(Xs2aAccountAccess access) {
        AisConsentRequestType aisConsentRequestType = null;
        if (!access.getAccounts().isEmpty()) {
            aisConsentRequestType = AisConsentRequestType.DEDICATED_ACCOUNTS;
        }

        if (access.getAllPsd2() == ALL_ACCOUNTS) {
            aisConsentRequestType = AisConsentRequestType.GLOBAL;
        }

        if (!access.isNotEmpty()) {
            aisConsentRequestType = AisConsentRequestType.BANK_OFFERED;
        }

        AccountAccessType availableAccounts = access.getAvailableAccounts();
        if (availableAccounts == ALL_ACCOUNTS || availableAccounts == ALL_ACCOUNTS_WITH_BALANCES) {
            aisConsentRequestType = AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        }
        return aisConsentRequestType;
    }

}
