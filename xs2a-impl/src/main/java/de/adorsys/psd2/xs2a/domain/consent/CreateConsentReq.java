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
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    private String instanceId;

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
