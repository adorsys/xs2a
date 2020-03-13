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
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.AccountReferenceCollector;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS;

@Data
public class CreateConsentReq implements AccountReferenceCollector {
    @NotNull
    private AccountAccess access;

    private AccountAccessType availableAccounts;

    private AccountAccessType allPsd2;

    private AccountAccessType availableAccountsWithBalance;

    @NotNull
    private boolean recurringIndicator;

    @NotNull
    private LocalDate validUntil;

    @NotNull
    private int frequencyPerDay;

    @NotNull
    private boolean combinedServiceIndicator;

    private TppRedirectUri tppRedirectUri;

    private TppNotificationData tppNotificationData;

    private String tppBrandLoggingInformation;

    @JsonIgnore
    @Override
    public Set<AccountReference> getAccountReferences() {
        return getReferenceSet(this.access.getAccounts(), this.access.getBalances(), this.access.getTransactions());
    }

    @JsonIgnore
    public boolean isGlobalOrAllAccountsAccessConsent() {
        return isConsentGlobal() || isConsentForAllAvailableAccounts();
    }

    @SafeVarargs
    private final Set<AccountReference> getReferenceSet(List<AccountReference>... referencesList) {
        return Arrays.stream(referencesList)
                   .map(this::getReferenceList)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }

    private List<AccountReference> getReferenceList(List<AccountReference> reference) {
        return Optional.ofNullable(reference)
                   .orElseGet(Collections::emptyList);
    }

    private boolean isConsentGlobal() {
        return access.isNotEmpty(getAisConsentData())
                   && allPsd2 == ALL_ACCOUNTS;
    }

    @JsonIgnore
    public AisConsentData getAisConsentData() {
        return new AisConsentData(availableAccounts, allPsd2, availableAccountsWithBalance, combinedServiceIndicator);
    }

    @JsonIgnore
    public boolean isConsentForAllAvailableAccounts() {
        return availableAccounts == ALL_ACCOUNTS
                   || availableAccountsWithBalance == ALL_ACCOUNTS;
    }

    @JsonIgnore
    public boolean isOneAccessType() {
        return !isRecurringIndicator();
    }
}
