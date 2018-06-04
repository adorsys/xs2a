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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.account.AccountHolder;
import de.adorsys.aspsp.xs2a.domain.AccountAccess;
import de.adorsys.aspsp.xs2a.domain.AisAccount;
import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.aspsp.xs2a.exception.ConsentException;
import de.adorsys.aspsp.xs2a.repository.AisConsentRepository;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.*;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus.RECEIVED;
import static de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus.VALID;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class AisConsentService {
    private final AccountSpi accountSpi;
    private final AspspProfileService profileService;
    private final AisConsentRepository aisConsentRepository;
    private final ConsentMapper consentMapper;

    public Optional<String> createConsent(AisConsentRequest request) {
        AisConsent consent = new AisConsent();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setConsentStatus(RECEIVED);
        consent.setExpectedFrequencyPerDay(profileService.getMinFrequencyPerDay(request.getFrequencyPerDay()));
        consent.setTppFrequencyPerDay(request.getFrequencyPerDay());
        consent.setUsageCounter(request.getFrequencyPerDay());
        consent.setRequestDate(LocalDateTime.now());
        consent.setExpireDate(request.getValidUntil());
        consent.setPsuId(request.getPsuId());
        consent.setTppId(request.getTppId());
        consent.addAccounts(readAccounts(request));
        consent.setRecurringIndicator(request.isRecurringIndicator());
        consent.setTppRedirectPreferred(request.isTppRedirectPreferred());
        consent.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
        AisConsent saved = aisConsentRepository.save(consent);
        return saved.getId() != null
                   ? Optional.of(saved.getExternalId())
                   : Optional.empty();
    }

    public Optional<SpiConsentStatus> getConsentStatusById(String consentId) {
        return getAisConsentById(consentId)
                   .map(AisConsent::getConsentStatus);
    }

    public Optional<Boolean> updateConsentStatusById(String consentId, SpiConsentStatus status) {
        return getAisConsentById(consentId)
                   .map(con -> setStatusAndSaveConsent(con, status))
                   .map(con -> con.getConsentStatus() == status);
    }

    public Optional<SpiAccountConsent> getSpiAccountConsentById(String consentId) {
        return getAisConsentById(consentId)
                   .map(consentMapper::mapToSpiAccountConsent);
    }

    public Map<String, Set<AccessAccountInfo>> checkAvailable(AvailableAccessRequest request) {
        Optional<AisConsent> consent = aisConsentRepository.findByExternalId(request.getConsentId());
        AisConsent aisConsent = consent.orElseThrow(() -> new ConsentException("Consent id not found"));
        if (!EnumSet.of(VALID, RECEIVED).contains(aisConsent.getConsentStatus())) {
            throw new ConsentException("Consent status is: " + aisConsent.getConsentStatus());
        }
        Map<String, Set<AccessAccountInfo>> targetAccounts = consentMapper.toMap(aisConsent.getAccounts());
        Map<String, Set<AccessAccountInfo>> requestedAccounts = request.getAccountsAccesses();
        return filterRequestedAccounts(targetAccounts, requestedAccounts);
    }

    private Map<String, Set<AccessAccountInfo>> filterRequestedAccounts(Map<String, Set<AccessAccountInfo>> targetAccounts, Map<String, Set<AccessAccountInfo>> requestedAccounts) {
        return requestedAccounts.entrySet().stream()
                   .filter(e -> targetAccounts.containsKey(e.getKey()))
                   .collect(Collectors.toMap(Map.Entry::getKey, e -> filterAccessAccount(e.getValue(), targetAccounts.get(e.getKey()))));
    }

    private Set<AccessAccountInfo> filterAccessAccount(Set<AccessAccountInfo> incomeAccess, Set<AccessAccountInfo> bankAccess) {
        Collection<AccessAccountInfo> subtract = CollectionUtils.subtract(incomeAccess, bankAccess);
        incomeAccess.removeAll(subtract);
        return incomeAccess;
    }

    private Optional<AisConsent> getAisConsentById(String consentId) {
        return Optional.ofNullable(consentId)
                   .flatMap(aisConsentRepository::findByExternalId);
    }

    private AisConsent setStatusAndSaveConsent(AisConsent consent, SpiConsentStatus status) {
        consent.setConsentStatus(status);
        return aisConsentRepository.save(consent);
    }

    private List<AisAccount> readAccounts(AisConsentRequest request) {
        return request.getAccess().isAllAccountAccess()
                   ? readAccountsByPsuId(request.getAccess(), request.getPsuId())
                   : readAccountsByAccess(request.getAccess());
    }

    private List<AisAccount> readAccountsByPsuId(AisAccountAccessInfo access, String psuId) {
        if (StringUtils.isBlank(psuId)) {
            throw new ConsentException("Psu id must not be empty");
        }
        AccountHolder holder = buildAccountHolderByPsu(access, psuId);
        return buildAccounts(holder.getIbansAccess());
    }

    private AccountHolder buildAccountHolderByPsu(AisAccountAccessInfo access, String psuId) {
        AccountHolder holder = new AccountHolder();
        Set<TypeAccess> accessTypes = buildAccessTypes(access);
        List<SpiAccountDetails> accountDetails = accountSpi.readAccountsByPsuId(psuId);
        accountDetails.forEach(a -> holder.addAccountAccess(a.getIban(), a.getCurrency(), accessTypes));
        return holder;
    }

    private Set<TypeAccess> buildAccessTypes(AisAccountAccessInfo access) {
        return access.isAllPsd2()
                   ? EnumSet.allOf(TypeAccess.class)
                   : EnumSet.of(TypeAccess.ACCOUNT);
    }

    private List<AisAccount> readAccountsByAccess(AisAccountAccessInfo access) {
        Map<String, AccountHolder.AccessInfo> requestedAccounts = buildAccountHolderByAccess(access);
        Map<String, Set<Currency>> bankAccounts = getBankAccountsMapByIbans(requestedAccounts.keySet());

        Map<String, AccountHolder.AccessInfo> filtered = filterAccountsAccess(requestedAccounts, bankAccounts);
        return buildAccounts(filtered);
    }

    private Map<String, AccountHolder.AccessInfo> filterAccountsAccess(Map<String, AccountHolder.AccessInfo> requestedAccounts, Map<String, Set<Currency>> bankAccounts) {
        return requestedAccounts.entrySet().stream()
                   .filter(e -> bankAccounts.containsKey(e.getKey()))
                   .collect(Collectors.toMap(Map.Entry::getKey, e -> doFilterAccess(e.getValue(), bankAccounts.get(e.getKey()))));
    }

    private AccountHolder.AccessInfo doFilterAccess(AccountHolder.AccessInfo holder, Set<Currency> bankCurrencies) {
        Set<AccountAccess> filtered = holder.getAccesses().stream()
                                          .filter(a -> bankCurrencies.contains(a.getCurrency()))
                                          .collect(toSet());
        holder.updateAccess(filtered);
        return holder;
    }

    private Map<String, Set<Currency>> getBankAccountsMapByIbans(Set<String> ibans) {
        List<SpiAccountDetails> accountDetails = Optional.ofNullable(accountSpi.readAccountDetailsByIbans(ibans))
                                                     .orElse(Collections.emptyList());
        return accountDetails.stream()
                   .collect(Collectors.groupingBy(SpiAccountDetails::getIban, Collectors.mapping(SpiAccountDetails::getCurrency, toSet())));
    }

    private Map<String, AccountHolder.AccessInfo> buildAccountHolderByAccess(AisAccountAccessInfo access) {
        AccountHolder holder = new AccountHolder();
        holder.fillAccess(access.getAccounts(), TypeAccess.ACCOUNT);
        holder.fillAccess(access.getBalances(), TypeAccess.BALANCE);
        holder.fillAccess(access.getTransactions(), TypeAccess.TRANSACTION);
        return holder.getIbansAccess();
    }

    private List<AisAccount> buildAccounts(Map<String, AccountHolder.AccessInfo> accountsDetail) {
        return accountsDetail
                   .entrySet().stream()
                   .map(e -> buildAccount(e.getKey(), e.getValue()))
                   .collect(Collectors.toList());
    }

    private AisAccount buildAccount(String iban, AccountHolder.AccessInfo accountsDetail) {
        AisAccount account = new AisAccount(iban);
        account.addAccesses(accountsDetail.getAccesses());
        return account;
    }
}
