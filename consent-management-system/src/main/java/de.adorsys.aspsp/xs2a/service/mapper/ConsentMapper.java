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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.AccountAccess;
import de.adorsys.aspsp.xs2a.domain.AisAccount;
import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AccessAccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.TypeAccess;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConsentMapper {

    public SpiAccountConsent mapToSpiAccountConsent(AisConsent consent) {
        return new SpiAccountConsent(
            consent.getId().toString(),
            mapToSpiAccountAccess(consent.getAccounts()),
            consent.isRecurringIndicator(),
            convertToDate(consent.getExpireDate()),
            consent.getTppFrequencyPerDay(),
            convertToDate(consent.getRequestDate()),
            mapToSpiConsentStatus(consent.getConsentStatus()),
            false, consent.isTppRedirectPreferred());
    }

    public Map<String, Set<AccessAccountInfo>> toMap(List<AisAccount> accounts) {
        return accounts.stream()
                   .collect(Collectors.toMap(e -> e.getIban(), e -> accessAccountInfos(e.getAccesses())));
    }

    private SpiAccountAccess mapToSpiAccountAccess(List<AisAccount> aisAccounts) {
        return new SpiAccountAccess(mapToSpiAccountReference(aisAccounts, TypeAccess.ACCOUNT),
            mapToSpiAccountReference(aisAccounts, TypeAccess.BALANCE),
            mapToSpiAccountReference(aisAccounts, TypeAccess.TRANSACTION),
            null,
            null);
    }

    private List<SpiAccountReference> mapToSpiAccountReference(List<AisAccount> aisAccounts, TypeAccess typeAccess) {
        return aisAccounts.stream()
                   .map(acc -> mapToSpiAccountReference(acc, typeAccess))
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }

    private List<SpiAccountReference> mapToSpiAccountReference(AisAccount aisAccount, TypeAccess typeAccess) {
        return aisAccount.getAccesses().stream()
                   .filter(ass -> ass.getTypeAccess() == typeAccess)
                   .map(access -> new SpiAccountReference(aisAccount.getIban(), "", "", "", "", access.getCurrency()))
                   .collect(Collectors.toList());
    }

    private SpiConsentStatus mapToSpiConsentStatus(SpiConsentStatus consentStatus) {
        return SpiConsentStatus.valueOf(consentStatus.name());
    }

    private Date convertToDate(LocalDateTime dateToConvert) {
        return Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }

    private Set<AccessAccountInfo> accessAccountInfos(Set<AccountAccess> accesses) {
        return accesses.stream()
                   .map(a -> new AccessAccountInfo(a.getCurrency().getCurrencyCode(), a.getTypeAccess()))
                   .collect(Collectors.toSet());
    }
}
