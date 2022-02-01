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

package de.adorsys.psd2.core.data.ais;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.Consent;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Stream;

@SuperBuilder
public class AisConsent extends Consent<AisConsentData> {

    public AisConsent() {
        setConsentType(ConsentType.AIS);
    }

    @JsonIgnore
    public AccountAccess getAccess() {
        Optional<AccountAccessType> allPsd2Optional = Optional.ofNullable(getConsentData())
                                                          .map(AisConsentData::getAllPsd2);

        if (allPsd2Optional.isPresent()) {
            return getTppAccountAccesses();
        }

        AccountAccess aspspAccountAccesses = getAspspAccountAccesses();
        if (aspspAccountAccesses.isNotEmpty(getConsentData())) {
            return aspspAccountAccesses;
        }

        return getTppAccountAccesses();
    }

    public boolean isWithBalance() {
        return CollectionUtils.isNotEmpty(getTppAccountAccesses().getBalances())
                   || getConsentData().getAvailableAccountsWithBalance() != null
                   || isGlobalConsent();
    }

    @JsonIgnore
    public boolean isOneAccessType() {
        return !isRecurringIndicator();
    }

    @JsonIgnore
    public boolean isWrongConsentData() {
        return CollectionUtils.isEmpty(getPsuIdDataList())
                   || getConsentTppInformation() == null
                   || getConsentTppInformation().getTppInfo() == null;
    }

    @JsonIgnore
    public boolean isGlobalConsent() {
        return getConsentRequestType() == AisConsentRequestType.GLOBAL;
    }

    @JsonIgnore
    public boolean isConsentForAllAvailableAccounts() {
        return getConsentRequestType() == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
    }

    @JsonIgnore
    public boolean isConsentForDedicatedAccounts() {
        return getConsentRequestType() == AisConsentRequestType.DEDICATED_ACCOUNTS;
    }

    @JsonIgnore
    public boolean isBankOfferedConsent() {
        return getConsentRequestType() == AisConsentRequestType.BANK_OFFERED;
    }

    public boolean isConsentWithNotIbanAccount() {
        AccountAccess access = getAccess();
        if (access == null) {
            return false;
        }

        return Stream.of(access.getAccounts(), access.getBalances(), access.getTransactions())
                   .filter(Objects::nonNull)
                   .flatMap(Collection::stream)
                   .allMatch(acc -> StringUtils.isAllBlank(acc.getIban(), acc.getBban(), acc.getMsisdn()));
    }

    public boolean isConsentWithNotCardAccount() {
        AccountAccess access = getAccess();
        if (access == null) {
            return false;

        }

        return Stream.of(access.getAccounts(), access.getBalances(), access.getTransactions())
                   .filter(Objects::nonNull)
                   .flatMap(Collection::stream)
                   .allMatch(acc -> StringUtils.isAllBlank(acc.getMaskedPan(), acc.getPan()));
    }

    public Map<String, Integer> getUsageCounterMap() {
        return getUsages();
    }

    public AisConsentRequestType getAisConsentRequestType() {
        return getConsentRequestType();
    }

    @JsonIgnore
    public AisConsentRequestType getConsentRequestType() {
        AccountAccess usedAccess = getAccess();
        return getRequestType(getConsentData().getAllPsd2(),
                              getConsentData().getAvailableAccounts(),
                              getConsentData().getAvailableAccountsWithBalance(),
                              !usedAccess.isNotEmpty(getConsentData()));
    }

    private AisConsentRequestType getRequestType(AccountAccessType allPsd2,
                                                 AccountAccessType availableAccounts,
                                                 AccountAccessType availableAccountsWithBalance,
                                                 boolean isAccessesEmpty) {

        List<AccountAccessType> allAccountsType = Arrays.asList(AccountAccessType.ALL_ACCOUNTS, AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME);

        if (allAccountsType.contains(allPsd2)) {
            return AisConsentRequestType.GLOBAL;
        } else if (allAccountsType.contains(availableAccounts)) {
            return AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        } else if (allAccountsType.contains(availableAccountsWithBalance)) {
            return AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
        } else if (isAccessesEmpty) {
            return AisConsentRequestType.BANK_OFFERED;
        }
        return AisConsentRequestType.DEDICATED_ACCOUNTS;
    }
}
