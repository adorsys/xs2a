/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.domain.consent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@Deprecated
public class AccountConsent {
    @JsonIgnore
    private final String id;

    private final AccountAccess access;
    private final AccountAccess aspspAccess;

    private final boolean recurringIndicator;

    private final LocalDate validUntil;
    private final LocalDate expireDate;

    private final int frequencyPerDay;

    private final LocalDate lastActionDate;

    private final ConsentStatus consentStatus;

    @JsonIgnore
    private final boolean withBalance;

    @JsonIgnore
    private final boolean tppRedirectPreferred;

    @JsonIgnore
    private final List<PsuIdData> psuIdDataList;

    @JsonIgnore
    private final TppInfo tppInfo;

    @JsonIgnore
    private final AisConsentRequestType aisConsentRequestType;

    private final boolean multilevelScaRequired;

    private final List<ConsentAuthorization> authorisations;

    private final OffsetDateTime statusChangeTimestamp;

    @JsonIgnore
    private Map<String, Integer> usageCounterMap;

    private final OffsetDateTime creationTimestamp;

    @JsonIgnore
    public boolean isExpired() {
        return consentStatus == ConsentStatus.EXPIRED;
    }

    @JsonIgnore
    public boolean isOneAccessType() {
        return !recurringIndicator;
    }

    @JsonIgnore
    public boolean isGlobalConsent() {
        return getAisConsentRequestType() == AisConsentRequestType.GLOBAL;
    }

    @JsonIgnore
    public boolean isConsentForAllAvailableAccounts() {
        return getAisConsentRequestType() == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
    }

    @JsonIgnore
    public boolean isConsentForDedicatedAccounts() {
        return getAisConsentRequestType() == AisConsentRequestType.DEDICATED_ACCOUNTS;
    }

    public Optional<ConsentAuthorization> findAuthorisationInConsent(String authorisationId) {
        return authorisations.stream()
                   .filter(auth -> auth.getId().equals(authorisationId))
                   .findFirst();
    }

    public boolean isConsentWithNotIbanAccount() {
        if (access == null) {
            return false;
        }

        return Stream.of(access.getAccounts(), access.getBalances(), access.getTransactions())
                   .filter(Objects::nonNull)
                   .flatMap(Collection::stream)
                   .allMatch(acc -> StringUtils.isAllBlank(acc.getIban(), acc.getBban(), acc.getMsisdn()));
    }

    public boolean isConsentWithNotCardAccount() {
        if (access == null) {
            return false;

        }

        return Stream.of(access.getAccounts(), access.getBalances(), access.getTransactions())
                   .filter(Objects::nonNull)
                   .flatMap(Collection::stream)
                   .allMatch(acc -> StringUtils.isAllBlank(acc.getMaskedPan(), acc.getPan()));
    }
}
