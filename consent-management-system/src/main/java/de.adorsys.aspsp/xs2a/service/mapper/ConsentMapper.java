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

import de.adorsys.aspsp.xs2a.domain.AisAccount;
import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.aspsp.xs2a.domain.AisConsentStatus;
import de.adorsys.aspsp.xs2a.domain.TypeAccess;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConsentMapper {

    public SpiAccountConsent mapToSpiAccountConsent(AisConsent consent) {
        return new SpiAccountConsent(
            consent.getId().toString(),
            mapToSpiAccountAccess(consent.getAccounts()),
            consent.isRecurringIndicator(),
            convertToDate(consent.getExpireDate()),
            consent.getFrequencyPerDay(),
            convertToDate(consent.getRequestDate()),
            mapToSpiConsentStatus(consent.getConsentStatus()),
            false, false);
    }

    private SpiAccountAccess mapToSpiAccountAccess(List<AisAccount> aisAccounts) {
        List<AisAccount> transactions = filterAccountsByTypeAccess(aisAccounts, TypeAccess.TRANSACTION);
        List<AisAccount> balances = filterAccountsByTypeAccess(aisAccounts, TypeAccess.BALANCE);
        List<AisAccount> accounts = filterAccountsByTypeAccess(aisAccounts, TypeAccess.ACCOUNT);

        return new SpiAccountAccess(mapToSpiAccountReference(accounts),
            mapToSpiAccountReference(balances),
            mapToSpiAccountReference(transactions),
            SpiAccountAccessType.ALL_ACCOUNTS,
            SpiAccountAccessType.ALL_ACCOUNTS);
    }

    private List<SpiAccountReference> mapToSpiAccountReference(List<AisAccount> aisAccounts) {
        return aisAccounts.stream()
                   .map(this::mapToSpiAccountReference)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }

    private List<SpiAccountReference> mapToSpiAccountReference(AisAccount aisAccount) {
        return aisAccount.getCurrencies().stream()
                   .map(cur -> new SpiAccountReference(aisAccount.getIban(), "", "", "", "", cur))
                   .collect(Collectors.toList());
    }

    private List<AisAccount> filterAccountsByTypeAccess(List<AisAccount> aisAccounts, TypeAccess typeAccess) {
        return aisAccounts.stream()
                   .filter(acc -> acc.getAccesses().contains(typeAccess))
                   .collect(Collectors.toList());
    }

    private SpiConsentStatus mapToSpiConsentStatus(AisConsentStatus consentStatus) {
        return SpiConsentStatus.valueOf(consentStatus.name());
    }

    private Date convertToDate(LocalDateTime dateToConvert) {
        return Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }
}
